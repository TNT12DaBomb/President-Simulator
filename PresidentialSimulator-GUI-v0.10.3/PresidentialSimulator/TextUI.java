import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

/** Guided text adapter. The same commands and snapshots can drive a future GUI. */
public final class TextUI {
    private static final int PAGE_SIZE = 7;
    private final LineInput input;
    private boolean clockArmed,statsOpen,helpOpen;
    private long clockFraction;
    private String clockKey="",seenCampaignDecision="",seenOfficeDecision="";
    private List<String> expiredMessages=List.of();
    private static final class DecisionTimeout extends RuntimeException { private static final long serialVersionUID=1L; }
    private final PrintStream out;
    private final TerminalScreen screen;
    private final long seed;
    private final Path savePath;
    private final boolean gui;
    private final boolean color;
    private CareerEngine career;
    private boolean exiting;
    public TextUI(InputStream input, PrintStream out, long seed, Path savePath, boolean gui, boolean color) {
        this(input, out, seed, savePath, gui, color, null);
    }
    public TextUI(InputStream input, PrintStream out, long seed, Path savePath, boolean gui, boolean color, TerminalScreen.Options options) {
        this.screen = options == null ? null : new TerminalScreen(out, options);
        this.input = new LineInput(input); this.out = screen == null ? out : screen.content(); this.seed = seed;
        this.savePath = savePath; this.gui = gui; this.color = color;
    }
    private void title(String heading) {
        if (screen != null) {
            String status = career == null ? "A continuing political career  |  Number keys + Enter  |  0 Back" :
                career.view().phase() + "  |  Career year " + career.view().year() + "  |  " + career.rules() + " rules" + (career.view().developerUsed() ? "  |  DEVELOPER RUN" : "");
            if(career!=null&&career.rules().timed())status=statsStrip();
            screen.statsAvailable(career!=null);screen.begin(heading, status); return;
        }
        out.println();
        out.println(color ? "\033[1;36m" + heading + "\033[0m" : heading);
        out.println("------------------------------------------------------------");
    }
    private void text(String paragraph) {
        StringBuilder line = new StringBuilder();
        for (String word : paragraph.split("\\s+")) {
            if (line.length() + word.length() + 1 > (screen == null ? 72 : screen.options().width() - 8)) { out.println("  " + line); line.setLength(0); }
            if (line.length() > 0) line.append(' ');
            line.append(word);
        }
        if (line.length() > 0) out.println("  " + line);
    }
    private String read(String prompt) {
        int page = 0;
        while (true) {
            if (screen == null) { out.print("\n  " + prompt + " > "); out.flush(); }
            else {screen.timer(activeSeconds());screen.render(page, prompt);}
            int budget=activeSeconds();String key=decisionKey();
            if(!key.equals(clockKey)){clockKey=key;clockFraction=0;}
            long started=System.nanoTime();LineInput.Line line;
            while(true){
                line=input.read(budget>0?Math.min(1000,Math.max(1,(budget*1_000_000_000L-clockFraction-(System.nanoTime()-started))/1_000_000)):-1);
                long elapsed=clockFraction+System.nanoTime()-started;
                if(budget>0&&elapsed>=budget*1_000_000_000L){advanceClock(budget);clockFraction=0;throw new DecisionTimeout();}
                if(line!=null)break;
                if(screen!=null&&budget>0)screen.countdown(Math.max(1,budget-(int)(elapsed/1_000_000_000L)));
            }
            if(budget>0){long elapsed=clockFraction+System.nanoTime()-started;int seconds=(int)(elapsed/1_000_000_000L);clockFraction=elapsed%1_000_000_000L;if(seconds>0)advanceClock(seconds);}
            if(line.end())throw new NoSuchElementException();
            String value = line.value().trim();
            if(value.equals("?") && !helpOpen){
                TerminalScreen.Frame frame=screen==null?null:screen.snapshot();
                try{help();}finally{if(frame!=null)screen.restore(frame);}continue;
            }
            if(value.equalsIgnoreCase("ST") && career!=null && screen!=null && !statsOpen){
                TerminalScreen.Frame frame=screen.snapshot();boolean armed=clockArmed;clockArmed=false;statsOpen=true;
                try{compactStats();}finally{statsOpen=false;clockArmed=armed;screen.restore(frame);}continue;
            }
            if (screen != null && (value.equals(">") || value.equals("<"))) {
                page = Math.floorMod(page + (value.equals(">") ? 1 : -1), screen.pages()); continue;
            }
            return value;
        }
    }
    private void separator(){out.println("  "+"─".repeat(screen==null?58:Math.max(20,screen.options().width()-8)));}
    private String statsStrip(){
        CareerView v=career.view();
        if(v.phase()==CareerView.Phase.CAMPAIGN)return "CAMPAIGN | Turn "+v.campaign().turnsUsed()+"/16 | Cash $"+v.campaign().playerFunds()+" | Projected "+v.campaign().playerEV()+" EV";
        if(v.world()!=null)return String.format(Locale.ROOT,"%s | Approval %.1f%% | Popularity %.1f%% | Capital %.0f | Energy %.0f",v.phase(),v.world().approval(),v.world().get(WorldMetric.POPULARITY),v.world().get(WorldMetric.CAPITAL),v.world().get(WorldMetric.ENERGY));
        return v.phase()+" | Career year "+v.year();
    }
    private String decisionKey(){if(career==null)return "";var v=career.view();if(v.phase()==CareerView.Phase.CAMPAIGN&&v.campaign().pendingEvent()!=null){var e=v.campaign().pendingEvent();return "c/"+e.id()+"/"+e.dueTurn();}if(v.phase()==CareerView.Phase.PRESIDENCY&&v.world()!=null&&v.world().pending()!=null)return "w/"+v.world().pending().id()+"/"+v.world().dueMonth();return "";}
    private int activeSeconds(){if(!clockArmed||career==null||!career.realtimeEnabled())return 0;var v=career.view();if(v.phase()==CareerView.Phase.CAMPAIGN&&v.campaign().pendingEvent()!=null)return v.campaign().pendingEvent().secondsRemaining();return v.phase()==CareerView.Phase.PRESIDENCY&&v.world()!=null?v.world().secondsRemaining():0;}
    private void advanceClock(int seconds){CareerCommand command=career.view().phase()==CareerView.Phase.CAMPAIGN?CareerCommand.campaign(GameCommand.tick(seconds)):CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.CLOCK_TICK,seconds));CareerReport result=career.submit(command);if(result.accepted()){expiredMessages=result.messages();save(savePath,false);}}
    private void pausedMenu(){boolean armed=clockArmed;clockArmed=false;try{gameMenu();}finally{clockArmed=armed;}}
    private int choose(String prompt, int min, int max) {
        TerminalScreen.Frame base=screen==null?null:screen.snapshot();
        while (true) {
            try {
                int value = Integer.parseInt(read(prompt));
                if (value >= min && value <= max) return value;
            } catch (NumberFormatException ignored) { }
            if(base!=null)screen.restore(base);
            text("Please type a number from " + min + " to " + max + ", then press Enter.");
        }
    }
    private boolean confirm(String prompt) {
        separator(); text(prompt); out.println("\n  1  Confirm\n  0  Go back");
        return choose("Choose", 0, 1) == 1;
    }
    private void pause() { read("Press Enter to continue"); }
    public void run() {
        if (screen != null) screen.start();
        try {
            while (!exiting) {
                if (career == null) welcome();
                else try { careerStep(); } catch(DecisionTimeout ex) {
                    clockArmed=false;title("RESPONSE WINDOW CLOSED");
                    text("Time expired. No response was submitted.");separator();expiredMessages.stream().limit(2).forEach(this::text);text("Press Enter to dismiss. Any late answer is discarded.");pause();
                }
            }
        } catch (NoSuchElementException ex) {
            boolean saved = career != null && save(savePath, false);
            String message = saved ? "Input closed. Your career was saved; choose Resume next time." : "Input closed. Resume will use your most recent successful save.";
            if (screen == null) text(message); else { screen.close(); screen.notification(message); }
        } finally { if (screen != null) screen.close(); }
    }
    private void welcome() {
        title("PRESIDENTIAL SIMULATOR  /  PLAYTEST");
        text("Campaign, take office, and build a continuing career. Type a menu number and press Enter.");
        out.println("\n  1  Start a new career\n  2  Load or resume\n  3  How to play\n  0  Exit");
        switch (choose("Welcome", 0, 3)) {
            case 0 -> exiting = true;
            case 1 -> setup();
            case 2 -> loadMenu();
            case 3 -> help();
            default -> throw new IllegalStateException();
        }
    }
    private void setup() {
        if ((career != null || Files.exists(savePath)) && !confirm("A new career replaces the autosave. Manual save slots are kept. Continue?")) return;
        title("NEW CAREER  /  DIFFICULTY");
        out.println("  1  Normal   Starting campaign cash: you $1,400; opponent $1,200\n  2  Hard     Starting campaign cash: you $1,000; opponent $1,600");
        int difficulty = choose("Difficulty", 1, 2);
        title("NEW CAREER  /  RUNNING MATE");
        out.println("  1  Community organizer   Town halls cost $50 less\n  2  Party organizer       Field offices cost $50 less\n  3  Fundraiser            $200 extra starting campaign cash\n  4  Logistics coordinator Outreach costs $50 less");
        int mate = choose("Running mate", 1, 4);
        career = new CareerEngine(GameSeeds.fresh(), President.Difficulty.values()[difficulty - 1], President.RunningMate.values()[mate - 1], GameRules.CURRENT);
        title("YOUR FIRST BRIEFING");
        text("The campaign still lasts 16 turns. After the election, your career continues: a win opens the transition into office; a loss opens four annual comeback decisions.");
        text("Each presidential term has 48 monthly turns with two actions each. Two elected terms end the career. The 0 menu contains saves, developer tools, help, and exit options.");
        text("New careers use a fictional electorate and economic model. Debates occur on turns 4, 8 and 12; live answers have real-time clocks. Menus, details and ST statistics pause those clocks.");
        text("Autosave follows every accepted decision. Browsing, cancelling, and opening menus are free.");
        save(savePath, false); pause();
    }
    private void careerStep() {
        CareerView v = career.view();
        if (v.developerUsed()) text("DEVELOPER RUN - shortcuts are recorded in this save.");
        switch (v.phase()) {
            case CAMPAIGN -> campaignStep(v);
            case ELECTION_REVIEW -> electionStep(v);
            case TRANSITION -> transitionStep(v);
            case PRESIDENCY -> presidencyStep(v);
            case TERM_REVIEW -> termReview(v);
            case OPPOSITION -> oppositionStep(v);
            case RETIRED -> retiredStep(v);
        }
    }
    private void campaignStep(CareerView v) {
        GameView g = v.campaign();
        if (g.pendingEvent() != null && (!career.rules().timed() || g.pendingEvent().secondsRemaining()>0 || !seenCampaignDecision.equals(decisionKey()))) { seenCampaignDecision=decisionKey(); event(g.pendingEvent()); return; }
        title("CAMPAIGN " + v.cycle() + "  /  TURN " + (g.turnsUsed() + 1) + " OF " + g.totalTurns());
        out.printf("  YOUR CASH $%d    OPPONENT CASH $%d%n", g.playerFunds(), g.opponentFunds());
        out.printf("  %s  You %d EV  |  Opponent %d EV  |  270 needed%n", career.rules().world() ? "SYNTHETIC PROJECTION" : "CURRENT BOARD", g.playerEV(), g.opponentEV());
        if(career.rules().timed()){
            text("Debates: campaign turns 4, 8 and 12. ST opens your stats.");
            if(g.pendingEvent()!=null)text("PENDING: "+g.pendingEvent().title()+" — before end of turn "+g.pendingEvent().dueTurn());
            separator();
        }else text("Career year " + v.year() + ". " + v.reputation());
        if (!g.activeEffects().isEmpty()) text("Temporary effects active: review the campaign briefing for details.");
        out.println("\n  1  Take a campaign action\n  2  Campaign briefing\n  3  Career status\n  4  Record campaign promise\n  5  Pending decision\n  0  Game menu");
        switch (choose("Campaign desk", 0, 5)) {
            case 0 -> gameMenu();
            case 4 -> choosePolicy(OfficeCommand.Type.PLEDGE);
            case 5 -> {if(g.pendingEvent()!=null)event(g.pendingEvent());else{text("No pending decision.");pause();}}
            case 1 -> campaignActions();
            case 2 -> campaignBriefing();
            case 3 -> status();
        }
    }
    private void campaignActions() {
        title("CAMPAIGN ACTIONS");
        out.println("  1  Campaign in a state\n  2  Fundraise\n  3  Pass this turn\n  0  Back");
        switch (choose("Action", 0, 3)) {
            case 0 -> { }
            case 1 -> browse(true);
            case 2 -> { if (confirm("Fundraise for $" + career.view().campaign().fundraisingAmount() + "? This uses one turn.")) submit(CareerCommand.campaign(GameCommand.fundraise())); }
            case 3 -> { if (confirm("Pass without completing work? The opponent and event steps still happen.")) submit(CareerCommand.campaign(GameCommand.rest())); }
        }
    }
    private void campaignBriefing() {
        title("CAMPAIGN BRIEFING");
        out.println("  1  State board\n  2  Campaign journal\n  3  Temporary effects\n  0  Back");
        switch (choose("Inspect", 0, 3)) {
            case 0 -> { }
            case 1 -> browse(false);
            case 2 -> journal(career.view().campaign().history(), "CAMPAIGN JOURNAL");
            case 3 -> {
                title("TEMPORARY EFFECTS");
                List<String> effects = career.view().campaign().activeEffects();
                if (effects.isEmpty()) text("No temporary effects are active."); else effects.forEach(this::text);
                pause();
            }
        }
    }
    private void electionStep(CareerView v) {
        CareerView.ElectionSummary e = v.elections().get(v.elections().size() - 1);
        title("ELECTION NIGHT  /  " + e.outcome());
        if (e.forced()) text("Developer-forced outcome. No electoral tally was assigned.");
        else out.printf("  YOU %d EV    OPPONENT %d EV%n", e.playerEV(), e.opponentEV());
        e.consequences().forEach(this::text);
        out.println("\n  1  Continue your career\n  2  Inspect election results\n  3  Career status\n  0  Game menu");
        switch (choose("Next step", 0, 3)) {
            case 0 -> gameMenu();
            case 1 -> submit(CareerCommand.advance());
            case 2 -> {
                ElectionResult result = career.latestResult();
                if (result == null) { text("This developer outcome bypassed the election. The campaign board is not a final result."); pause(); }
                else {
                    if (gui && !GraphicsEnvironment.isHeadless()) new ElectionGUI().showResults(result, career.states(), career.districtOfColumbia());
                    browse(false);
                }
            }
            case 3 -> status();
        }
    }
    private void transitionStep(CareerView v) {
        title("TRANSITION  /  PREPARING TERM " + v.electionsWon());
        text(v.publicFeedback()); text(v.support());
        out.println("\n  1  Take office\n  2  Review transition resources\n  3  Career status\n  0  Game menu");
        switch (choose("Transition desk", 0, 3)) {
            case 0 -> gameMenu();
            case 1 -> { if (confirm("Begin this four-year term? You will take up to two actions per month, the month advances after your second action.")) submit(CareerCommand.advance()); }
            case 2 -> { title("TRANSITION RESOURCES"); if (v.transitionResources().isEmpty()) text("No campaign preparation credits are available."); else v.transitionResources().forEach(this::text); pause(); }
            case 3 -> status();
        }
    }
    private void presidencyStep(CareerView v) {
        Presidency.View p = v.presidency();
        if (p.pendingEvent() != null && (!career.rules().timed() || (career.view().world()!=null&&career.view().world().secondsRemaining()>0) || !seenOfficeDecision.equals(decisionKey()))) { seenOfficeDecision=decisionKey(); officeEvent(p.pendingEvent()); return; }
        if(career.rules().timed()){compactDesk(v);return;}
        title(p.date() + "  /  PRESIDENTIAL DESK  /  TERM " + v.electionsWon());
        columns("MONTH " + (v.termMonths() + 1) + " / 48  |  ACTIONS LEFT " + p.actionsLeft() + " / 2", "OFFICE FUNDS $" + p.treasury());
        columns("HOUSE " + p.congress().houseSeats() + " / 435  |  " + p.congress().houseControl(), "SENATE " + p.congress().senateSeats() + " / 100  |  " + p.congress().senateControl());
        long staff = p.cabinet().seats().stream().filter(x -> x.official() != null).count();
        long reports = p.cabinet().seats().stream().filter(x -> x.job() != null).count();
        columns("CABINET " + staff + " / 6 staffed  |  " + reports + " assigned reports", "POLICIES " + p.lawsPassed() + " signed  |  " + p.implementations().size() + " awaiting delivery");
        if (p.world() != null) {
            WorldSimulation.View w=p.world();
            columns(String.format(java.util.Locale.ROOT,"APPROVAL %.1f%% (%+.1f last monthly change)",w.approval(),w.change()),String.format(java.util.Locale.ROOT,"POPULARITY %.1f%% | CAPITAL %.1f",w.get(WorldMetric.POPULARITY),w.get(WorldMetric.CAPITAL)));
            columns(String.format(java.util.Locale.ROOT,"GROWTH %.1f%% | UNEMPLOYMENT %.1f%%",w.get(WorldMetric.GROWTH),w.get(WorldMetric.UNEMPLOYMENT)),String.format(java.util.Locale.ROOT,"INFLATION %.1f%% | TRUST %.1f",w.get(WorldMetric.INFLATION),w.get(WorldMetric.TRUST)));
            text("World consequences pending: " + w.scheduled().size() + "; active paths: " + w.flags());
        }
        text("PUBLIC BRIEF: " + p.publicFeedback());
        int midterm = v.rules().midtermElectionMonth();
        text(v.termMonths() < midterm ? "Midterms: " + (midterm - v.termMonths()) + " months away" + (v.rules().current() ? " / Election Day " + GameCalendar.federalElectionDay(Integer.parseInt(p.date().substring(0,4)) + (v.termMonths() < 12 ? 1 : 0)) : "") : p.proceedings().pendingHouse() != null ? "Midterm results declared; new Congress takes office January 3." : "Midterms complete; " + (48 - v.termMonths()) + " months remain in this term.");
        if (p.bill() != null) text("ON THE AGENDA: " + p.bill().issue() + " / " + p.bill().stage());
        long delivered = p.correspondence().requests().stream().filter(r -> r.status() == PublicCorrespondence.Status.DELIVERED).count();
        text("PUBLIC REQUESTS: " + delivered + " delivered / " + p.correspondence().requests().size() + " on record");
        java.util.List<String> upcoming = new java.util.ArrayList<>();
        p.cabinet().seats().stream().filter(s -> s.job() != null).forEach(s -> upcoming.add("Month " + s.job().dueMonth() + ": " + s.job().title()));
        p.implementations().forEach(i -> upcoming.add("Month " + i.dueMonth() + ": " + i.title()));
        p.delayedEffects().forEach(d -> upcoming.add("Month " + d.dueMonth() + ": " + d.description()));
        if (!upcoming.isEmpty()) { text("UPCOMING WORK"); upcoming.stream().limit(3).forEach(this::text); }
        if (p.actionsLeft() == 0) text("Resolve pending decisions to complete this month.");
        if (screen != null) out.println();
        out.println("\n  1  Office & administration\n  2  Legislative agenda\n  3  Public & personal\n  4  Party & elections\n  5  Statistics & records\n  6  End month\n  0  Game menu");
        switch (choose("Presidential desk", 0, 6)) {
            case 0 -> gameMenu();
            case 1 -> administrationMenu();
            case 2 -> legislationMenu();
            case 3 -> publicMenu();
            case 4 -> midtermMenu();
            case 5 -> officeRecords();
            case 6 -> { if (confirm("Close this month? Unused actions expire. Receipts +$100 and routine costs -$75 apply; events or midterms may follow.")) submit(CareerCommand.office(OfficeCommand.simple(OfficeCommand.Type.END_MONTH))); }
        }
    }
    private void compactDesk(CareerView v){
        var p=v.presidency();title(p.date()+" / PRESIDENTIAL DESK / TERM "+v.electionsWon());
        columns("Actions "+p.actionsLeft()+"/2 | Office $"+p.treasury(),"House "+p.congress().houseSeats()+"/435 | Senate "+p.congress().senateSeats()+"/100");
        var w=v.world();text(String.format(Locale.ROOT,"Growth %.1f%%  |  Unemployment %.1f%%  |  Inflation %.1f%%",w.get(WorldMetric.GROWTH),w.get(WorldMetric.UNEMPLOYMENT),w.get(WorldMetric.INFLATION)));
        separator();
        if(p.pendingEvent()!=null)text("DECISION: "+p.pendingEvent().title()+" | "+Math.max(0,w.dueMonth()-w.month())+" month-end step(s) left");
        else text("No response due. "+w.scheduled().size()+" world follow-up(s) scheduled.");
        if(p.bill()!=null)text("Bill: "+p.bill().issue()+" — "+p.bill().stage());
        separator();
        out.println("  1  Office & administration\n  2  Legislative agenda\n  3  Public & personal\n  4  Party & elections\n  5  Statistics & records\n  6  End month\n  7  Pending decision\n  0  Game menu");
        switch(choose("Presidential desk",0,7)){
            case 0->gameMenu();case 1->administrationMenu();case 2->legislationMenu();case 3->publicMenu();case 4->midtermMenu();case 5->officeRecords();
            case 6->{if(confirm(p.pendingEvent()==null?"Close this month?":"Close this month? An unanswered decision may expire."))submit(CareerCommand.office(OfficeCommand.simple(OfficeCommand.Type.END_MONTH)));}
            case 7->{if(p.pendingEvent()!=null)officeEvent(p.pendingEvent());else{text("No pending decision.");pause();}}
        }
    }
    private void columns(String left, String right) {
        int width = screen == null ? 76 : screen.options().width() - 6;
        if (width < 90) { text(left); text(right); return; }
        int half = width / 2;
        out.printf("  %-" + half + "s  %s%n", left, right);
    }
    private void administrationMenu() {
        title("OFFICE & ADMINISTRATION");
        out.println("  1  Cabinet & appointments\n  2  Administrative actions\n  3  Cabinet history\n  0  Back");
        switch (choose("Administration", 0, 3)) {
            case 1 -> cabinetMenu();
            case 2 -> actionMenu("ADMINISTRATIVE ACTIONS", new CareerCommand.GovernanceAction[]{CareerCommand.GovernanceAction.CABINET_MEETING,
                CareerCommand.GovernanceAction.BUDGET_REVIEW, CareerCommand.GovernanceAction.SERVICE_REVIEW, CareerCommand.GovernanceAction.MANAGE_CRISIS});
            case 3 -> journal(career.view().presidency().cabinet().history().stream().map(e -> "Month " + e.month() + " / " + Cabinet.departmentLabel(e.department(), career.rules()) + ": " + e.message()).toList(), "CABINET HISTORY");
        }
    }
    private void cabinetMenu() {
        title("CABINET & APPOINTMENTS");
        List<Cabinet.Seat> seats = career.view().presidency().cabinet().seats();
        for (int i = 0; i < seats.size(); i++) {
            Cabinet.Seat s = seats.get(i);
            text((i + 1) + "  " + Cabinet.departmentLabel(s.department(), career.rules()) + " / " + (s.official() != null ? s.official().name() : s.nominee() != null ? "Nominee: " + s.nominee().name() : "Vacant"));
        }
        out.println("  0  Back"); int choice = choose("Department", 0, seats.size()); if (choice == 0) return;
        int d = choice - 1; Cabinet.Seat seat = seats.get(d);
        if (seat.official() == null && seat.nominee() == null) {
            for (int i = 0; i < 2; i++) {
                Cabinet.Candidate c = Cabinet.candidates().get(d * 2 + i);
                text((i + 1) + "  " + c.name() + ". " + c.background());
                text("Delegated reports cost $" + c.projectCost() + " and take " + c.deliveryMonths() + " month closures.");
            }
            out.println("  0  Back"); int c = choose("Candidate", 0, 2);
            if (c > 0 && confirm("Use one action to select this candidate? Chief of Staff appointments are immediate; other positions require confirmation after a hearing."))
                submit(CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.NOMINATE, d * 2 + c - 1)));
        } else if (seat.nominee() != null) {
            text("Hearing completes after month " + seat.hearingMonth() + ". " + (career.rules().current() ? "Nomination votes use a simple majority; the VP can break a 50–50 tie. A consensus nominee or nominee-specific agreement supplies cross-caucus support in this simplified model." : "Confirmation needs a Senate majority, a consensus nominee, or a nominee-specific agreement."));
            out.println("  1  Confirm appointment\n  2  Negotiate confirmation support\n  3  Withdraw nomination\n  0  Back");
            int c = choose("Nomination", 0, 3);
            if (c > 0 && confirm("Spend one monthly action on this appointment decision?")) submit(CareerCommand.office(OfficeCommand.choice(
                new OfficeCommand.Type[]{OfficeCommand.Type.CONFIRM, OfficeCommand.Type.NOMINEE_SUPPORT, OfficeCommand.Type.DISMISS}[c - 1], d)));
        } else {
            text("Annual assignment: " + Cabinet.project(seat.department()) + ". One assignment per department per administrative year.");
            text(seat.job() == null ? "No active assignment." : seat.job().title() + " due at end of month " + seat.job().dueMonth());
            text("Assignment cost $" + seat.official().projectCost() + "; " + seat.official().deliveryMonths() + " month closures. Dismissal cancels unfinished work without refund.");
            out.println("  1  Delegate annual assignment\n  2  Dismiss official\n  0  Back"); int c = choose("Official", 0, 2);
            if (c > 0 && confirm("Use one action for this decision?")) submit(CareerCommand.office(OfficeCommand.choice(c == 1 ? OfficeCommand.Type.DELEGATE : OfficeCommand.Type.DISMISS, d)));
        }
    }
    private void actionMenu(String heading, CareerCommand.GovernanceAction[] actions) {
        title(heading);
        for (int i = 0; i < actions.length; i++) out.println("  " + (i + 1) + "  " + actions[i]);
        out.println("  0  Back"); int c = choose("Action", 0, actions.length);
        if (c > 0) {
            CareerView.GovernanceOption option = career.view().governanceOptions().stream().filter(o -> o.action() == actions[c - 1]).findFirst().orElseThrow();
            if (!option.available()) { text(option.reason()); pause(); return; }
            if (career.rules().world()) text(WorldSimulation.actionHelp(actions[c - 1]));
            text("Uses one of this month's two actions. Public operating cost: $" + option.cost() + ".");
            if (confirm("Proceed with " + actions[c - 1] + "?")) submit(CareerCommand.govern(actions[c - 1]));
        }
    }
    private void legislationMenu() {
        Presidency.View p = career.view().presidency(); title("LEGISLATIVE DESK");
        text(p.congress().rule());
        text("Current bill: " + (p.bill() == null ? "None" : p.bill().issue() + " / " + p.bill().stage()));
        if (p.bill() != null) describePolicy(p.bill().issue(), p.bill().approach());
        if (career.rules().current()) {
            text(p.proceedings().speaker() + " | " + p.proceedings().senateLeader());
            columns("HOUSE COMMITMENTS " + p.proceedings().houseSupport() + " / 218", "SENATE CLOTURE COMMITMENTS " + p.proceedings().senateSupport() + " / 60");
            text("Negotiation adds 8 House and 4 Senate commitments in this authored coalition model. These are game mechanics, not predicted legislator behavior.");
            if(career.rules().world()) text("Each negotiation also spends 8 political capital. Public briefings, cabinet coordination and successful legislation can rebuild influence.");
            out.println("\n  1  Propose an initiative\n  2  Negotiate commitments\n  3  Committee review\n  4  Floor votes\n  5  Presidential decision / withdraw\n  0  Back");
            int c = choose("Legislation", 0, 5);
            if (c == 1) choosePolicy(OfficeCommand.Type.PROPOSE);
            else if (c > 1 && c < 5) {
                if (confirm("Use one action for this legislative step? Requirements are checked before spending it.")) submit(CareerCommand.office(OfficeCommand.simple(new OfficeCommand.Type[]{OfficeCommand.Type.NEGOTIATE, OfficeCommand.Type.COMMITTEE_REVIEW, OfficeCommand.Type.FLOOR_VOTE}[c - 2])));
            } else if (c == 5) {
                if (p.bill() == null) { text("No bill is active."); pause(); return; }
                if (!p.proceedings().passed()) {
                    if (confirm("Withdraw this unpassed proposal? A presidential veto is available only after both chambers pass a bill.")) submit(CareerCommand.office(OfficeCommand.simple(OfficeCommand.Type.WITHDRAW_BILL)));
                } else {
                    out.println("  1  Sign and fund\n  2  Veto\n  0  Back"); int decision = choose("Presidential decision", 0, 2);
                    if (decision > 0 && confirm("Use one action on this decision? Signing spends the displayed operating appropriation.")) submit(CareerCommand.office(OfficeCommand.simple(decision == 1 ? OfficeCommand.Type.SIGN : OfficeCommand.Type.VETO)));
                }
            }
            return;
        }
        out.println("\n  1  Propose an initiative\n  2  Negotiate with leadership\n  3  Sign bill\n  4  Veto bill\n  0  Back");
        int c = choose("Legislation", 0, 4);
        if (c == 1) choosePolicy(OfficeCommand.Type.PROPOSE);
        else if (c > 1 && confirm("Use one monthly action on this legislative decision?")) submit(CareerCommand.office(OfficeCommand.simple(new OfficeCommand.Type[]{OfficeCommand.Type.NEGOTIATE, OfficeCommand.Type.SIGN, OfficeCommand.Type.VETO}[c - 2])));
    }
    private void choosePolicy(OfficeCommand.Type type) {
        title(type == OfficeCommand.Type.PLEDGE ? "CAMPAIGN PROMISES / UP TO TWO" : "POLICY ISSUE");
        Policy.Issue[] issues = Policy.Issue.values();
        out.println("  1  Economy / taxes / healthcare / immigration\n  2  Defense / environment / education / civil rights\n  0  Back");
        int group = choose("Issue group", 0, 2); if (group == 0) return;
        int base = (group - 1) * 4;
        for (int i = 0; i < 4; i++) out.println("  " + (i + 1) + "  " + issues[base + i]);
        out.println("  0  Back"); int issue = choose("Issue", 0, 4); if (issue == 0) return;
        for (Policy.Approach a : Policy.Approach.values()) {
            text((a.ordinal() + 1) + "  " + PolicyCatalog.option(issues[base + issue - 1], a).title());
            describePolicy(issues[base + issue - 1], a);
        }
        out.println("  0  Back");
        int approach = choose("Approach", 0, 2); if (approach == 0) return;
        text(type == OfficeCommand.Type.PLEDGE ? "Promises are public and cannot be rewritten during this campaign. No campaign turn is spent." : "Proposing uses one monthly action. Signing a conflicting approach later records a contradicted promise.");
        if (confirm("Record this choice?")) submit(CareerCommand.office(OfficeCommand.policy(type, issues[base + issue - 1], Policy.Approach.values()[approach - 1])));
    }
    private void describePolicy(Policy.Issue issue, Policy.Approach approach) {
        PolicyCatalog.Option o = PolicyCatalog.option(issue, approach);
        text("Signing cost $" + o.cost() + "; delivery after " + o.deliveryMonths() + " month closures, or by term end. " + o.benefit() + ".");
        text("Tradeoff: " + o.tradeoff() + ". Requested by " + o.group() + ".");
        if(career.rules().world())text("First completed delivery this issue/term: "+WorldSimulation.policyEffect(issue,approach).description()+". Enactment adds capital +3 / unity +1. Matching a pledge adds trust +2 / party approval +2; contradicting it costs trust -4 / party approval -5 / unity -4.");
    }
    private void publicMenu() {
        title("PUBLIC & PERSONAL");
        out.println("  1  Public correspondence\n  2  Briefings, visits & personal time\n  3  Correspondence history\n  0  Back");
        switch (choose("Public desk", 0, 3)) {
            case 1 -> correspondenceMenu();
            case 2 -> actionMenu("PUBLIC ACTIONS", new CareerCommand.GovernanceAction[]{CareerCommand.GovernanceAction.PUBLIC_BRIEFING, CareerCommand.GovernanceAction.TRAVEL, CareerCommand.GovernanceAction.REST});
            case 3 -> journal(career.view().presidency().correspondence().history().stream()
                .map(u -> "Month " + u.month() + " / " + u.status() + ": " + u.message()).toList(), "PUBLIC CORRESPONDENCE HISTORY");
        }
    }
    private void correspondenceMenu() {
        int page = 0;
        while (true) {
            List<PublicCorrespondence.Request> all = career.view().presidency().correspondence().requests();
            title("PUBLIC REQUESTS / PAGE " + (page + 1) + " OF 4");
            text("A reply uses one action and acknowledges a concern. Delivery requires funding the corresponding initiative. Competing requests remain visible.");
            for (int i = 0; i < 4; i++) {
                PublicCorrespondence.Request r = all.get(page * 4 + i);
                text((i + 1) + "  " + r.group() + " / " + r.status() + " / " + (r.answered() ? "Reply published" : "No reply"));
                text(r.request());
            }
            out.println("  5  Next page\n  6  Previous page\n  0  Back");
            int c = choose("Reply to request or change page", 0, 6);
            if (c == 0) return;
            if (c == 5) { page = (page + 1) % 4; continue; }
            if (c == 6) { page = (page + 3) % 4; continue; }
            PublicCorrespondence.Request r = all.get(page * 4 + c - 1);
            if (r.answered()) { text("A reply is already on the record."); pause(); continue; }
            if (confirm("Publish an acknowledgment to " + r.group() + "? This spends one action and does not mark the request delivered.")) {
                submit(CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.REPLY, r.id()))); return;
            }
        }
    }
    private void midtermMenu() {
        title("PARTY & MIDTERM ELECTIONS");
        int month = career.view().termMonths();
        if (career.rules().world()) text("WORLD rules: visits improve chances, not guaranteed wins. Probability = 30% + 17 percentage points per visit + 6 points per governing-mood unit, bounded 5–95%. Approval and the economy drive mood. Fixed seats remain an abstraction.");
        text("Targeted visits open in month 13 and close after month " + career.rules().midtermElectionMonth() + ". Visits improve a contest’s chances. Each costs one monthly action and no public money.");
        text("Eight fictional House slates each carry two seats; four Senate races each carry one. Fixed seats: your caucus 210 House / 48 Senate. Other seats belong to the other caucus. Winning four House slates and " + (career.rules().current() ? "two" : "three") + " Senate races yields control of both chambers (assuming unified caucuses and the current vice president).");
        text(month < 12 ? "Campaign opens in " + (12 - month) + " months." : month < career.rules().midtermElectionMonth() ? "Campaign open; " + (career.rules().midtermElectionMonth() - month) + " months until results." : "Results are final for this term.");
        out.println("  1  House contests 1–4\n  2  House contests 5–8\n  3  Senate contests\n  4  Donor meeting\n  0  Back");
        int group = choose("Party desk", 0, 4); if (group == 0) return;
        if (group == 4) { actionMenu("PRIVATE DONORS", new CareerCommand.GovernanceAction[]{CareerCommand.GovernanceAction.FUNDRAISE}); return; }
        List<MidtermCampaign.Contest> contests = career.view().presidency().midtermContests();
        int base = (group - 1) * 4;
        for (int i = 0; i < 4; i++) {
            MidtermCampaign.Contest c = contests.get(base + i);
            text((i + 1) + "  " + c.name() + " / " + c.seats() + " seats / " + c.outcome());
            text("Listening: " + (c.listened() ? "complete" : "needed") + "; organizing: " + (c.organized() ? "complete" : "needed"));
        }
        out.println("  0  Back"); int chosen = choose("Contest", 0, 4); if (chosen == 0) return;
        if (month < 12 || month >= career.rules().midtermElectionMonth()) { text("Visits are available from month 13 through month " + career.rules().midtermElectionMonth() + "."); pause(); return; }
        out.println("  1  Listening visit\n  2  Organizing visit\n  0  Back");
        int task = choose("Visit", 0, 2); if (task == 0) return;
        if (confirm("Spend one monthly action on this visit?")) submit(CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.MIDTERM_TASK, (base + chosen - 1) * 2 + task - 1)));
    }
    private void officeEvent(PresidencyEvent event) {
        if(career.rules().timed()){worldDecision();return;}
        title("OFFICE EVENT / " + event.title()); text(event.description());
        boolean world = career.view().world() != null;
        if (world) text(String.format(java.util.Locale.ROOT,"Available political capital: %.1f. Probabilities are model risks, not guaranteed results.",career.view().world().get(WorldMetric.CAPITAL)));
        for (int i = 0; i < event.choices().size(); i++) {
            PresidencyEvent.Choice c = event.choices().get(i);
            text((i + 1) + "  " + c.label() + " ($" + c.cost() + ")");
            if (world) text(c.immediate().message());
            if (c.delayed() != null) text("Follow-up in " + c.delayMonths() + " months, or at term end: " + c.delayed().message());
        }
        out.println("  0  Game menu"); int c = choose("Response", 0, event.choices().size());
        if (c == 0) gameMenu();
        else if (confirm("Apply this response? It uses no monthly action.")) submit(CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.EVENT_CHOICE, c - 1)));
    }
    private void officeRecords() {
        title("OFFICE RECORDS");
        out.println("  1  Career & operating accounts\n  2  Congress & promises\n  3  Laws & follow-ups\n  4  Career journal\n  5  World metrics / trends / consequences\n  0  Back");
        switch (choose("Record", 0, 5)) {
            case 1 -> status();
            case 2 -> {
                Presidency.View p = career.view().presidency();
                text("Your caucus: " + p.congress().houseSeats() + "/435 House seats, " + p.congress().senateSeats() + "/100 Senate seats.");
                text(p.congress().rule());
                text("Midterm results resolve after month " + career.rules().midtermElectionMonth() + ". Each targeted contest requires listening and organizing visits. Outcomes depend on visits and governing performance. This remains a simplified contest board, not a full district/turnout model.");
                if (p.promises().isEmpty()) text("No campaign promises were recorded.");
                p.promises().forEach(x -> text(x.issue() + " / " + x.approach() + ": " + x.status())); pause();
            }
            case 3 -> {
                Presidency.View p = career.view().presidency();
                text("Laws signed: " + p.lawsPassed() + "; vetoes: " + p.billsVetoed() + "."); p.laws().forEach(this::text);
                p.implementations().forEach(i -> text("Policy delivery at end of month " + i.dueMonth() + ": " + i.title()));
                if (p.delayedEffects().isEmpty() && p.implementations().isEmpty()) text("No pending follow-ups.");
                p.delayedEffects().forEach(x -> text("Term month " + x.dueMonth() + ": " + x.description())); pause();
            }
            case 4 -> journal(career.view().history(), "CAREER JOURNAL");
            case 5 -> worldRecords();
        }
    }
    private void worldRecords() {
        if(career.rules().timed()){compactStats();return;}
        WorldSimulation.View w=career.view().world();
        if(w==null){text("This imported career preserves its original rules; start a new career for the world simulation.");pause();return;}
        title("WORLD / METRICS AND CONSEQUENCES");
        out.println("  1  Approval and all metrics\n  2  Monthly trend\n  3  Why numbers changed\n  4  Pending consequences and paths\n  0  Back");
        int choice=choose("World records",0,4);if(choice==0)return;
        title("WORLD RECORDS / " + choice);
        if(choice==1){
            text(String.format(java.util.Locale.ROOT,"National job approval %.2f%% | high %.2f%% | low %.2f%%",w.approval(),w.high(),w.low()));
            text("Approval measures job performance. Popularity means personal favorability. Weighted approval uses fictional fixed 35/30/35 partisan groups; these are model values, not sampled polls.");
            for(WorldMetric metric:WorldMetric.values())text(String.format(java.util.Locale.ROOT,"%-35s %7.2f %s",metric.label,w.get(metric),metric.unit));
        }else if(choice==2){
            text("Month | Approval | Popularity | Growth | Unemployment | Inflation");
            for(var t:w.trend())text(String.format(java.util.Locale.ROOT,"%5d | %7.2f%% | %8.2f%% | %5.2f%% | %11.2f%% | %8.2f%%",t.month(),t.approval(),t.popularity(),t.growth(),t.unemployment(),t.inflation()));
        }else if(choice==3)w.explanations().forEach(this::text);
        else{
            text("Persistent story paths: "+w.flags());
            if(w.scheduled().isEmpty())text("No scheduled world consequences.");
            for(var s:w.scheduled()){text("World month "+s.dueMonth()+": "+s.reason());text("Chance "+Math.round(s.probability()*100)+"%: "+s.success().description());text("Otherwise: "+s.failure().description());}
        }
        pause();
    }
    private void termReview(CareerView v) {
        title("TERM COMPLETE  /  WHAT COMES NEXT?");
        text("You have finished a four-year term. Another campaign is available.");
        v.nextCampaignEffects().forEach(this::text);
        out.println("\n  1  Run for reelection\n  2  Review career status\n  3  Retire\n  0  Game menu");
        switch (choose("Next chapter", 0, 3)) {
            case 0 -> gameMenu();
            case 1 -> { if (confirm("Open the next campaign using this administration's completed record?")) submit(CareerCommand.runAgain()); }
            case 2 -> status();
            case 3 -> retire();
        }
    }
    private void oppositionStep(CareerView v) {
        title("OUT OF OFFICE  /  COMEBACK PERIOD");
        text(v.reputation()); text(v.support());
        out.printf("  CAREER YEAR %d    ANNUAL DECISIONS COMPLETED %d / 4%n", v.year(), v.comebackYears());
        out.println("\n  1  " + (v.comebackYears() < 4 ? "Plan the next year" : "Run again") + "\n  2  Career status\n  3  Career journal\n  0  Game menu");
        switch (choose("Next chapter", 0, 3)) {
            case 0 -> gameMenu();
            case 1 -> {
                if (v.comebackYears() == 4) { if (confirm("Start another campaign? Your past results and rebuilding decisions carry forward.")) submit(CareerCommand.runAgain()); }
                else rebuildMenu();
            }
            case 2 -> status();
            case 3 -> journal(v.history(), "CAREER JOURNAL");
        }
    }
    private void rebuildMenu() {
        title("PLAN A YEAR OUT OF OFFICE");
        CareerCommand.RebuildAction[] actions = CareerCommand.RebuildAction.values();
        for (int i = 0; i < actions.length; i++) out.println("  " + (i + 1) + "  " + actions[i]);
        out.println("  0  Back");
        int choice = choose("Annual focus", 0, actions.length);
        if (choice == 0) return;
        CareerCommand.RebuildAction action = actions[choice - 1];
        text(switch (action) {
            case CAMPAIGN_REVIEW -> "Publish the review requested after a loss. Prepare one town-hall objective for the next campaign.";
            case DONOR_MEETINGS -> "Resolve the loss-related $200 withholding from the next campaign's starting cash.";
            case COMMUNITY_WORK -> "Renew community contacts and prepare one outreach objective for the next campaign.";
            case PRIVATE_LIFE -> "Advance a year without completing any rebuilding tasks.";
        });
        if (confirm("Spend one year on this focus?")) submit(CareerCommand.rebuild(action));
    }
    private void retiredStep(CareerView v) {
        title("CAREER COMPLETE");
        text(v.completedTerms() == 2 ? "You completed two four-year terms. The eight-year limit has been reached." : "This career has ended. Your record is preserved.");
        out.println("\n  1  Career status\n  2  Career journal\n  3  Return to title\n  0  Game menu");
        switch (choose("Career archive", 0, 3)) {
            case 0 -> gameMenu();
            case 1 -> status();
            case 2 -> journal(v.history(), "CAREER JOURNAL");
            case 3 -> { if (save(savePath, false)) career = null; }
        }
    }
    private void status() {
        CareerView v = career.view();
        title("CAREER STATUS  /  YEAR " + v.year());
        text("Phase: " + v.phase().toString().replace('_', ' '));
        out.printf("  ELECTION WINS %d    LOSSES %d    COMPLETED TERMS %d / 2%n", v.electionsWon(), v.electionsLost(), v.completedTerms());
        out.printf("  TIME IN OFFICE %d years, %d month(s)%n", v.servedMonths() / 12, v.servedMonths() % 12);
        out.println("\n  PUBLIC FEEDBACK"); text(v.publicFeedback());
        out.println("\n  REPUTATION / RECORD"); text(v.reputation());
        out.println("\n  ORGANIZATIONAL SUPPORT"); text(v.support());
        pause();
        title("ECONOMY AND OPERATING ACCOUNTS");
        text(v.world() == null ? "This imported ruleset tracks operating cash only." : "World metrics model growth, unemployment, inflation and debt separately from the small office operating account. Values are fictional scenario indicators.");
        if(v.world()!=null) { text(String.format(java.util.Locale.ROOT,"Approval %.1f%% / Popularity %.1f%% / Reputation %.1f / Donors %.1f",v.world().approval(),v.world().get(WorldMetric.POPULARITY),v.world().get(WorldMetric.REPUTATION),v.world().get(WorldMetric.DONORS))); }
        out.printf("  TREASURY $%d    MONTHLY RECEIPTS $%d    ROUTINE EXPENSES $%d%n", v.treasury(), v.monthlyReceipts(), v.routineExpenses());
        text(v.economicConditions());
        text("Public treasury and campaign cash are separate.");
        text("Annual work: " + (v.annualWork().isEmpty() ? "No special administrative tasks recorded this year." : String.join("; ", v.annualWork())));
        pause();
        title("NEXT CAMPAIGN / CAREER RECORD");
        v.nextCampaignEffects().forEach(this::text);
        for (CareerView.ElectionSummary e : v.elections()) text("Election " + e.cycle() + ", year " + e.year() + ": " + e.outcome()
            + (e.forced() ? " (developer-forced)" : " - " + e.playerEV() + " to " + e.opponentEV() + " EV"));
        pause();
    }
    private void gameMenu() {
        title("GAME MENU");
        out.println("  1  Save / load\n  2  Developer tools\n  3  How to play\n  4  Leave or end career\n  5  Game settings\n  6  Career archive\n  0  Return to game");
        switch (choose("Menu", 0, 6)) {
            case 0 -> { }
            case 1 -> saveMenu();
            case 2 -> developerMenu();
            case 3 -> help();
            case 4 -> leaveMenu();
            case 6 -> archiveMenu();
            case 5 -> {
                title("GAME SETTINGS");
                text("Seed: " + career.seed() + "; campaign difficulty: " + career.difficulty() + ". Two elected terms. Fictional scenario rules: "+career.rules()+".");
                text("Presidency events: " + (career.rules().world()?new String[]{"Off","35% monthly baseline","60% monthly baseline"}:new String[]{"Off", "Every six months", "Every three months"})[career.view().eventFrequency()]);
                out.println("  1  Turn presidency events off\n  2  Normal events (35% monthly baseline)\n  3  Frequent events (60% monthly baseline)\n  4  Display size\n  5  Live response clocks on/off\n  0  Back");
                text("Live response clocks: "+(career.realtimeEnabled()?"On":"Off / paused"));
                int c = choose("Setting", 0, 5);
                if(c==5)submit(CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.REALTIME_MODE,career.realtimeEnabled()?0:1)));
                else if (c == 4) displaySettings();
                else if (c > 0) submit(CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.EVENT_FREQUENCY, c - 1)));
            }
        }
    }
    private void saveMenu() {
        title("SAVE / LOAD");
        out.println("  1  Save current career\n  2  Save to a manual slot\n  3  Load a saved career\n  0  Back");
        switch (choose("Save menu", 0, 3)) {
            case 0 -> { }
            case 1 -> { save(savePath, true); pause(); }
            case 2 -> {
                int slot = choose("Manual slot: 1, 2, or 3; 0 cancels", 0, 3);
                if (slot > 0) {
                    Path target = slotPath(slot);
                    if (!Files.exists(target) || confirm("Replace manual save slot " + slot + "?")) {
                        String label = read("Save name (up to 60 characters; blank uses a default)");
                        try { CareerSave.write(career, target, label); text("Named save written."); }
                        catch (IOException ex) { text("Could not save: " + ex.getMessage()); }
                        pause();
                    }
                }
            }
            case 3 -> loadMenu();
        }
    }
    private Path slotPath(int slot) { return savePath.toAbsolutePath().getParent().resolve("slot-" + slot + ".save"); }
    private void loadMenu() {
        title("LOAD / RESUME");
        describeSave(savePath, "Autosave");
        for (int i = 1; i <= 3; i++) describeSave(slotPath(i), "Slot " + i);
        out.println("  1  Resume autosave\n  2  Load a manual slot\n  3  Recover a backup\n  0  Back");
        int choice=choose("Load",0,3);if(choice==0)return;
        Path target=savePath;
        if(choice==2){int slot=choose("Manual slot: 1–3; 0 cancels",0,3);if(slot==0)return;target=slotPath(slot);}
        if(choice==3){int slot=choose("Backup source: 0 autosave, 1–3 manual slot",0,3);Path source=slot==0?savePath:slotPath(slot);
            for(int i=1;i<=3;i++)describeSave(CareerSave.backup(source,i),"Backup "+i);
            int generation=choose("Backup generation: 1 newest, 0 back",0,3);if(generation==0)return;target=CareerSave.backup(source,generation);
        }
        if (career != null && !confirm("Load this save and replace the current in-memory career?")) return;
        try {
            CareerEngine loaded = CareerSave.read(target);
            career = loaded;
            text("Career restored. Your choices, pending events and remaining time are saved.");
            save(savePath, false);
        } catch (IOException ex) { text("Could not load: " + ex.getMessage()); }
        pause();
    }
    private void leaveMenu() {
        title("LEAVE / END CAREER");
        out.println("  1  Save and return to title\n  2  Save and exit\n  3  Retire / end this career\n  0  Back");
        switch (choose("Leave", 0, 3)) {
            case 0 -> { }
            case 1 -> { if (save(savePath, false)) career = null; else pause(); }
            case 2 -> { if (save(savePath, false)) exiting = true; else pause(); }
            case 3 -> retire();
        }
    }
    private void retire() { if (confirm("End this career? Its history stays available, but normal play cannot resume this career.")) submit(CareerCommand.retire()); }
    private void developerMenu() {
        String password=read("Developer password: ");
        if(password==null||!DeveloperAccess.accepts(password.toCharArray())){text("Incorrect password.");return;}
        title("DEVELOPER TOOLS");
        text("Using a shortcut marks the save as a developer run. Scenario jumps replace the active career timeline; save a manual slot first if you want to keep it.");
        out.println("\n  1  Force current election outcome\n  2  Jump to a test scenario\n  3  Add test resources\n  4  Advance to end of current term\n  5  Monthly systems\n  6  World metrics and event fixtures\n  0  Back");
        switch (choose("Developer menu", 0, 6)) {
            case 0 -> { }
            case 1 -> {
                out.println("\n  1  Force victory\n  2  Force defeat\n  0  Back"); int c = choose("Outcome", 0, 2);
                if (c > 0) developer(c == 1 ? CareerCommand.DeveloperAction.FORCE_WIN : CareerCommand.DeveloperAction.FORCE_LOSS);
            }
            case 2 -> {
                out.println("\n  1  Midway through first term\n  2  Start reelection campaign\n  3  Midway through second term\n  4  Final month of second term\n  5  Start midterm campaigning\n  0  Back");
                int c = choose("Scenario", 0, 5);
                if (c > 0) developer(new CareerCommand.DeveloperAction[]{CareerCommand.DeveloperAction.MID_FIRST_TERM,
                    CareerCommand.DeveloperAction.REELECTION_START, CareerCommand.DeveloperAction.MID_SECOND_TERM, CareerCommand.DeveloperAction.FINAL_QUARTER, CareerCommand.DeveloperAction.MIDTERM_START}[c - 1]);
            }
            case 3 -> {
                out.println("\n  1  Add $500 campaign cash\n  2  Add $500 public treasury\n  0  Back"); int c = choose("Resource", 0, 2);
                if (c > 0) developer(c == 1 ? CareerCommand.DeveloperAction.ADD_CAMPAIGN_CASH : CareerCommand.DeveloperAction.ADD_TREASURY);
            }
            case 4 -> developer(CareerCommand.DeveloperAction.FINISH_TERM);
            case 6 -> {
                if(!career.rules().world() || career.view().phase()!=CareerView.Phase.PRESIDENCY){text("World fixtures require an active WORLD presidency.");pause();return;}
                out.println("  1  Set approval fixture\n  2  Set political capital\n  3  Force a specific world event\n  4  Inspect metrics\n  0  Back");
                int tool=choose("World tools",0,4);if(tool==0)return;
                if(tool==4){worldRecords();return;}
                if(tool<3){int value=choose("Value 0–100",0,100);if(confirm("Set this developer fixture?"))submit(CareerCommand.office(OfficeCommand.choice(tool==1?OfficeCommand.Type.DEV_APPROVAL:OfficeCommand.Type.DEV_CAPITAL,value)));}
                else{var events=career.rules().timed()?WorldStories.all():WorldEventCatalog.all();for(int i=0;i<events.size();i++)text((i+1)+"  "+events.get(i).title());int event=choose("Event (0 cancels)",0,events.size());if(event>0&&confirm("Force this event, bypassing prerequisites?"))submit(CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.DEV_WORLD_EVENT,event-1)));}
            }
            case 5 -> {
                out.println("  1  Advance one month\n  2  Toggle Congress control\n  3  Trigger an eligible event\n  0  Back");
                int c = choose("Monthly tools", 0, 3);
                if (c > 0) developer(new CareerCommand.DeveloperAction[]{CareerCommand.DeveloperAction.ADVANCE_MONTH, CareerCommand.DeveloperAction.CHANGE_CONGRESS, CareerCommand.DeveloperAction.TRIGGER_EVENT}[c - 1]);
            }
        }
    }
    private void developer(CareerCommand.DeveloperAction action) { if (confirm(action + "?")) submit(CareerCommand.dev(action)); }
    private boolean submit(CareerCommand command) {
        CareerView before=career.view();
        CareerReport report = career.submit(command);
        title(report.accepted() ? "CHAPTER UPDATE" : "ACTION NOT TAKEN");
        if(career.rules().timed() && report.accepted()){
            text(DecisionText.headline(command));text(DecisionText.change(before,report.view()));separator();
            var messages=report.messages();
            for(int i=0;i<messages.size();i++){
                String message=messages.get(i);
                if(message.startsWith("NEWS [")){text(message);if(i+1<messages.size())text(messages.get(++i));}
                else if(message.startsWith("DEBATE")||message.contains("EVENT:")||message.contains("DEADLINE MISSED")||message.contains("TIME EXPIRED")||message.startsWith("POLICY DELIVERY")||message.startsWith("NOVEMBER MIDTERMS"))text(message);
            }
        }else report.messages().forEach(this::text);
        if (report.accepted()) save(savePath, false);
        if(career.rules().timed()){
            TerminalScreen.Frame frame=screen==null?null:screen.snapshot();
            while(read("Enter: continue | D: full report").equalsIgnoreCase("D")){boolean armed=clockArmed;clockArmed=false;try{journal(report.messages(),"FULL REPORT");}finally{clockArmed=armed;if(frame!=null)screen.restore(frame);}}
        }else pause();return report.accepted();
    }
    private void event(GameView.PendingEvent event) {
        if(career.rules().timed()){campaignDecision();return;}
        title("EVENT RESPONSE  /  " + event.title().toUpperCase(Locale.ROOT));
        GameView current = career.view().campaign();
        out.printf("  YOUR CASH $%d    OPPONENT CASH $%d%n", current.playerFunds(), current.opponentFunds());
        if (event.state() != null) text("Location: " + event.state());
        text(event.description()); text("Responding uses no additional turn.");
        for (int i = 0; i < event.choices().size(); i++) {
            GameView.ChoiceView c = event.choices().get(i);
            text((i + 1) + "  " + c.label() + (c.available() ? "" : " [Unavailable: " + c.reason() + "]"));
        }
        out.println("\n  0  Game menu (save, load, and exit remain available)");
        int response = choose("Response", 0, event.choices().size());
        if (response == 0) { gameMenu(); return; }
        if (!event.choices().get(response - 1).available()) { text("That response is unavailable. Declining is free."); return; }
        if (confirm("Apply this response?")) submit(CareerCommand.campaign(GameCommand.respond(response - 1)));
    }
    private void displaySettings() {
        if (screen == null) { text("Start with --screen for the expanded renderer, or --plain for accessible output."); pause(); return; }
        title("DISPLAY SIZE");
        text("Match the frame to your visible terminal window. No gameplay time is spent. Long screens can always be paged with > and <.");
        int width = choose("Columns (60–180)", 60, 180), height = choose("Rows (20–70)", 20, 70);
        screen.options(new TerminalScreen.Options(width, height, screen.options().refresh(), screen.options().color()));
    }
    private void archiveMenu() {
        title("CAREER ARCHIVE");
        out.println("  1  Search all history\n  2  Election history\n  3  Policy history\n  4  Cabinet history\n  5  Public correspondence history\n  6  Term summaries\n  0  Back");
        int c = choose("Archive", 0, 6); if (c == 0) return;
        if (c == 6) { journal(CareerArchive.termSummaries(career.view()), "TERM ARCHIVE"); return; }
        CareerArchive.Category category = new CareerArchive.Category[]{CareerArchive.Category.ALL, CareerArchive.Category.ELECTIONS,
            CareerArchive.Category.POLICIES, CareerArchive.Category.CABINET, CareerArchive.Category.PUBLIC}[c - 1];
        String query = read("Search text (blank shows all entries in category)");
        List<String> matches = CareerArchive.search(career.view(), category, query).stream().map(e -> "#" + e.number() + " [" + e.category() + "] " + e.text()).toList();
        journal(matches, "ARCHIVE / " + matches.size() + " MATCHES");
    }
    private void journal(List<String> entries, String heading) {
        int page = 0, size = screen == null ? 10 : 6;
        int pages = Math.max(1, (entries.size() + size - 1) / size);
        while (true) {
            title(heading + " / PAGE " + (page + 1) + " OF " + pages);
            if (entries.isEmpty()) text("No matching entries yet.");
            for (int i = page * size; i < Math.min(entries.size(), (page + 1) * size); i++) text(entries.get(i));
            out.println("\n  N  Next entries    P  Previous entries    0  Back");
            String c = read("Archive navigation").toLowerCase(Locale.ROOT);
            if (c.equals("0") || c.isEmpty()) return;
            if (c.equals("n") || c.equals("1")) page = Math.min(pages - 1, page + 1);
            else if (c.equals("p")) page = Math.max(0, page - 1);
        }
    }
    private void describeSave(Path path, String label) {
        if (!Files.exists(path)) { text(label + ": Empty"); return; }
        try { CareerSave.Metadata m = CareerSave.metadata(path); text(label + ": " + m.label() + " / " + m.savedAt() + " / " + m.summary()); }
        catch (IOException ex) { text(label + ": Unreadable; try a backup."); }
    }
    private boolean save(Path path, boolean notify) {
        try { CareerSave.write(career, path); if (notify) text("Career saved."); return true; }
        catch (IOException ex) { text("Could not save: " + ex.getMessage() + ". The current career remains in memory."); return false; }
    }
    private void help() {
        boolean wasArmed=clockArmed;clockArmed=false;helpOpen=true;
        try { while(true){
            title("HOW TO PLAY");
            text("Choose a short guide. You can open this help from any screen with ? then Enter.");separator();
            out.println("  1  Start here\n  2  Campaigns & debates\n  3  Running the presidency\n  4  Clocks, controls & statistics\n  5  Saves & leaving the game\n  0  Return");
            int topic=choose("Help topic",0,5);if(topic==0)return;
            title(new String[]{"START HERE","CAMPAIGNS & DEBATES","RUNNING THE PRESIDENCY","CLOCKS, CONTROLS & STATISTICS","SAVES & EXIT"}[topic-1]);
            switch(topic){
                case 1 -> {
                    text("Your goal is to win an election, govern, and build a political career. Losing opens a comeback path; you can run again.");separator();
                    text("Start a new career, choose a difficulty and a running mate, then open Take a campaign action. A state visit costs cash and one of your 16 campaign turns. Fundraise when cash runs low.");
                    text("Choose a number and press Enter. Read the cost before confirming. Zero goes back or opens the game menu; the screen labels say which.");
                    text("You can inspect states and statistics without using a turn. The forecast is uncertain: leading now does not guarantee a win.");
                }
                case 2 -> {
                    text("Local campaign work changes support; field offices also help turnout. Your opponent acts too. Fundraising, promises and event choices compete for attention.");separator();
                    text("Debates arrive on turns 4, 8 and 12. Each starts with preparation, then three factual exchanges, a policy tradeoff and a record follow-up. Standard answers take 30 seconds; recovery takes 20. Desktop settings also offer relaxed and untimed pacing.");
                    text("Confidence and recovery are separate decisions. Opponent claims may be wrong; sourced corrections appear afterward. History preserves your words. Leaving ends the appearance.");
                    text("Ordinary offers expire after their displayed number of campaign turns. An election victory leads to inauguration; a defeat leads to rebuilding.");
                }
                case 3 -> {
                    text("Each presidential turn is one month. Complete two actions to advance the month automatically. Resolve any pending decision first.");separator();
                    text("Approval measures job performance; popularity measures personal favorability. Political capital supports negotiations and difficult choices. Rest restores energy but uses an action.");
                    text("Legislation follows a sequence: propose, negotiate support, complete committee review, hold floor votes, then sign or veto. Benefits may arrive later.");
                    text("Events and policy choices affect future conditions. Midterms can change Congress. At the end of a term, your record affects reelection. Normal careers allow two elected terms.");
                }
                case 4 -> {
                    text("The footer shows the live seconds remaining. It updates without Enter. Confirmation and page browsing use time; menus, help, statistics and effects details pause it.");separator();
                    text("Use > and < to read additional pages. Descriptions wrap in full. ST opens statistics; ? opens help. Enter your shortcut and press Enter.");
                    text("Game menu → Game settings → 5 disables live clocks. Turn and month deadlines still apply. Display size is under the same settings menu.");
                    text("After a decision, D opens the complete report. Browsing does not spend actions. The next debate question's clock waits while you read the previous report.");
                }
                case 5 -> {
                    text("Accepted actions autosave. For a named checkpoint, use Game menu → Save / load → Save to a manual slot. Three slots are available.");separator();
                    text("Use Load / resume at the title screen to reopen an autosave, a slot or one of its three backup generations. Loading replaces the in-memory career after confirmation.");
                    text("Game menu → Leave or end career lets you save and return to the title, or save and exit. Retirement ends the career and requires confirmation.");
                    text("This development build starts fresh: saves from older editions are not supported. Developer tools mark your career as a test run.");
                }
            }
            separator();pause();
        }} finally {helpOpen=false;clockArmed=wasArmed;}
    }

    private void browse(boolean campaign) {
        int page = 0;
        String filter = "";
        while (true) {
            final String query = filter;
            List<GameView.StateView> states = career.view().campaign().states().stream()
                .filter(s -> s.name().toLowerCase(Locale.ROOT).contains(query))
                .sorted(Comparator.comparing(GameView.StateView::name)).toList();
            int pages = Math.max(1, (states.size() + PAGE_SIZE - 1) / PAGE_SIZE);
            page = Math.min(page, pages - 1);
            title((campaign ? "PLAN A STATE VISIT" : "STATE BOARD") + "  /  PAGE " + (page + 1) + " OF " + pages);
            if (!filter.isEmpty()) text("Search: " + filter);
            List<GameView.StateView> visible = states.subList(page * PAGE_SIZE, Math.min(states.size(), (page + 1) * PAGE_SIZE));
            for (int i = 0; i < visible.size(); i++) {
                GameView.StateView s = visible.get(i);
                out.printf("  %d  %-22s %2d EV   %s%n", i + 1, s.name(), s.electoralVotes(), s.playerControls() ? "YOU LEAD" : "OPPONENT LEADS");
                text("Your remaining work: " + missing(s.objectives(), s.playerTasks()));
            }
            if (visible.isEmpty()) text("No states match. Search again or return to the full list.");
            out.println("\n  N  Next page    P  Previous page    S  Search\n  A  All states   0  Return");
            String selection = read("Choose a state number or a letter").toLowerCase(Locale.ROOT);
            switch (selection) {
                case "0" -> { return; }
                case "n" -> page = (page + 1) % pages;
                case "p" -> page = (page + pages - 1) % pages;
                case "s" -> { filter = read("State name (part of a name works)").toLowerCase(Locale.ROOT); page = 0; }
                case "a" -> { filter = ""; page = 0; }
                default -> {
                    try {
                        int index = Integer.parseInt(selection) - 1;
                        if (index < 0 || index >= visible.size()) throw new NumberFormatException();
                        if (stateDetail(visible.get(index), campaign)) return;
                    } catch (NumberFormatException ex) { text("Choose a displayed number, N, P, S, A, or 0."); }
                }
            }
        }
    }
    private static String tasks(Set<State.Task> tasks) {
        return tasks.isEmpty() ? "None" : tasks.stream().sorted().map(Object::toString).collect(Collectors.joining(", "));
    }
    private static String missing(Set<State.Task> needed, Set<State.Task> done) {
        return needed.stream().filter(t -> !done.contains(t)).sorted().map(Object::toString).collect(Collectors.collectingAndThen(Collectors.joining(", "), s -> s.isEmpty() ? "All objectives complete" : s));
    }
    private boolean stateDetail(GameView.StateView s, boolean campaign) {
        title(s.name().toUpperCase(Locale.ROOT) + "  /  " + s.electoralVotes() + " ELECTORAL VOTES");
        text("Objectives: " + tasks(s.objectives()));
        text("Your completed work: " + tasks(s.playerTasks()));
        text("Opponent's completed work: " + tasks(s.opponentTasks()));
        text("Why this state belongs to " + (s.playerControls() ? "you: " : "the opponent: ") + s.explanation());
        if (!campaign) { pause(); return false; }
        out.println();
        for (int i = 0; i < s.actions().size(); i++) {
            GameView.ActionView action = s.actions().get(i);
            out.printf("  %d  %s  $%d%s%n", i + 1, action.task(), action.cost(), action.available() ? "" : "  [Unavailable]");
            if (!action.available()) text(action.reason());
        }
        out.println("  0  Back to states");
        int action = choose("Choose an action", 0, s.actions().size());
        if (action == 0) return false;
        GameView.ActionView selected = s.actions().get(action - 1);
        if (!selected.available()) { text(selected.reason()); return false; }
        if (!confirm(selected.task() + " in " + s.name() + " costs $" + selected.cost() + " and one turn. Continue?")) return false;
        return submit(CareerCommand.campaign(GameCommand.campaign(s.name(), selected.task())));
    }
    private void compactStats(){
        title("STATISTICS");separator();
        out.println("  1  Public & political\n  2  Economy\n  3  Readiness & world\n  4  Approval trend\n  5  Why things changed\n  6  Upcoming consequences\n  7  Debate record\n  0  Back");
        int c=choose("Statistics",0,7);if(c==0)return;
        if(c==7){title("CAMPAIGN DEBATE RECORD");var game=career.view().campaign();
            if(game==null||game.debateResults().isEmpty()){text("Debate transcript (answers, confidence and fact checks):");if(game!=null)for(String entry:game.history())if(entry.startsWith("DEBATE")||entry.startsWith("FACT CHECK")||entry.startsWith("RECORD")||entry.startsWith("ANSWER")||entry.startsWith("CONFIDENCE")||entry.startsWith("RECOVERY"))text(entry);}
            else {text(String.format(Locale.ROOT,"Combined election-day share effect: %+.3f percentage points",game.debateShare()));for(var result:game.debateResults()){separator();text(String.format(Locale.ROOT,"%s | lasting effect %+.3f pp",result.topic(),result.lastingShare()));for(var round:result.rounds())text("Q"+round.number()+" | You "+round.playerScore()+" : Opponent "+round.opponentScore()+" | "+round.answer());}}
            pause();return;
        }
        WorldSimulation.View w=career.view().world();
        if(w==null){text("This career uses an older model; detailed world metrics are unavailable.");pause();return;}
        title(new String[]{"PUBLIC & POLITICAL","ECONOMY","READINESS & WORLD","APPROVAL TREND","CHANGE LOG","UPCOMING CONSEQUENCES"}[c-1]);
        separator();
        if(c<=3){
            WorldMetric[] metrics=c==1?new WorldMetric[]{WorldMetric.POPULARITY,WorldMetric.PARTY_APPROVAL,WorldMetric.INDEPENDENT_APPROVAL,WorldMetric.OPPOSITION_APPROVAL,WorldMetric.TRUST,WorldMetric.UNITY,WorldMetric.DONORS,WorldMetric.REPUTATION,WorldMetric.CAPITAL,WorldMetric.ENERGY}:
                c==2?new WorldMetric[]{WorldMetric.GROWTH,WorldMetric.UNEMPLOYMENT,WorldMetric.INFLATION,WorldMetric.RATE,WorldMetric.CONFIDENCE,WorldMetric.DEFICIT,WorldMetric.DEBT}:
                new WorldMetric[]{WorldMetric.PREPAREDNESS,WorldMetric.RESILIENCE,WorldMetric.TENSION,WorldMetric.SCANDAL};
            if(c==1)text(String.format(Locale.ROOT,"Job approval %.1f%% | last monthly change %+.1f",w.approval(),w.change()));
            for(WorldMetric m:metrics)out.printf(Locale.ROOT,"  %-20s %7.1f %s%n",DecisionText.metric(m),w.get(m),m.unit);
            if(c==2){separator();text("Growth is annualized. Figures are fictional model indicators.");}
            if(c==3){separator();text("Open paths: "+(w.flags().isEmpty()?"None":w.flags()));}
        }else if(c==4){
            out.println("  Month   Approval   Popularity   Growth");
            for(var t:w.trend().subList(Math.max(0,w.trend().size()-12),w.trend().size()))out.printf(Locale.ROOT,"  %5d   %6.1f%%    %7.1f%%   %5.1f%%%n",t.month(),t.approval(),t.popularity(),t.growth());
            text("Latest twelve observations. Full explanations are in the record.");
        }else if(c==5){journal(w.explanations(),"METRIC CHANGES");return;}
        else {if(w.scheduled().isEmpty())text("No scheduled consequences.");for(var due:w.scheduled()){text("Month "+due.dueMonth()+" | "+Math.round(due.probability()*100)+"% expected-branch chance");text(due.reason());separator();}}
        pause();
    }
    private void worldDecision(){
        clockArmed=true;
        try{while(true){
            var w=career.view().world();if(w==null||w.pending()==null)return;var event=w.pending();
            title("DECISION / "+event.title());text(event.description());separator();
            text(w.secondsRemaining()>0?(career.realtimeEnabled()?"Live briefing. Choose and confirm before the countdown ends.":"Live clock paused in settings. Choose at your own pace."):"Due before month-end step "+w.dueMonth()+" ("+(w.dueMonth()-w.month())+" left).");
            for(int i=0;i<event.choices().size();i++){
                var choice=event.choices().get(i);text((i+1)+"  "+choice.label());
                text("Capital "+choice.capitalCost()+" | "+DecisionText.shortEffects(choice.immediate()));
                text("Follow-up: "+choice.delay()+" month(s), "+Math.round(choice.successChance()*100)+"% expected branch.");
                if(i==0)separator();
            }
            separator();out.println("  3  "+(w.secondsRemaining()>0?"Leave live briefing":"Return to desk")+"   4  Details\n  0  Game menu");
            int response=choose("Answer 1/2 | 3 "+(w.secondsRemaining()>0?"leave":"back")+" | 4 details | 0 menu",0,4);
            if(response==0){pausedMenu();return;}if(response==3){if(w.secondsRemaining()>0){if(!confirm("Leave without answering? "+w.missedResponse()))continue;submit(CareerCommand.office(OfficeCommand.simple(OfficeCommand.Type.LEAVE_LIVE)));}return;}
            if(response==4){boolean armed=clockArmed;clockArmed=false;try{title("DECISION DETAILS / "+event.title());text("No response: "+w.missedResponse());text(DecisionDeadline.world(event).effect().description());for(var choice:event.choices()){separator();text(choice.label());text(choice.preview());}pause();}finally{clockArmed=armed;}continue;}
            var chosen=event.choices().get(response-1);
            if(w.get(WorldMetric.CAPITAL)<chosen.capitalCost()){text("Not enough capital. The second response is free.");pause();continue;}
            if(confirm("Choose: "+chosen.label()+"?")){submit(CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.EVENT_CHOICE,response-1)));return;}
        }}finally{clockArmed=false;}
    }
    private void campaignDecision(){
        clockArmed=true;
        try{while(true){
            var g=career.view().campaign();var event=g.pendingEvent();if(event==null)return;
            title((event.id().startsWith("debate_")?"CAMPAIGN DEBATE / ":"CAMPAIGN DECISION / ")+event.title());text(event.description());separator();
            text(event.secondsRemaining()>0?(career.realtimeEnabled()?"Live answer. Choose and confirm before the countdown ends.":"Live clock paused in settings. Choose at your own pace."):"Due before end of turn "+event.dueTurn()+".");
            if(event.id().startsWith("debate_live_")){
                for(int i=0;i<event.choices().size();i++){text((i+1)+"  "+event.choices().get(i).label());separator();}
                int leave=event.choices().size()+1;out.println("  "+leave+"  Leave debate   0  Menu");
                int response=choose("Response",0,leave);
                if(response==0){pausedMenu();return;}
                if(response==leave){if(confirm("End this appearance?"))submit(CareerCommand.campaign(GameCommand.leaveLive()));return;}
                clockArmed=false;submit(CareerCommand.campaign(GameCommand.respond(response-1)));return;
            }
            CampaignEvent definition=(DebateSession.handles(event.id())?DebateSession.catalog():CampaignStories.all()).stream().filter(e->e.id().equals(event.id())).findFirst().orElseThrow();
            for(int i=0;i<event.choices().size();i++){
                var choice=event.choices().get(i);text((i+1)+"  "+choice.label()+(choice.available()?"":" [unavailable]"));
                text(campaignEffectText(definition.options().get(i).effects(),2)+(DebateSession.handles(event.id())?" | Score "+(i==0?4:2):""));if(i==0)separator();
            }
            separator();out.println("  3  "+(event.secondsRemaining()>0?"Leave live appearance":"Return to campaign")+"   4  Details\n  0  Game menu");
            int response=choose("Answer 1/2 | 3 "+(event.secondsRemaining()>0?"leave":"back")+" | 4 details | 0 menu",0,4);if(response==0){pausedMenu();return;}if(response==3){if(event.secondsRemaining()>0){if(!confirm("Leave without answering? "+(DebateSession.handles(event.id())?"All remaining questions will count as missed.":event.missedResponse())))continue;submit(CareerCommand.campaign(GameCommand.leaveLive()));}return;}
            if(response==4){boolean armed=clockArmed;clockArmed=false;try{title("CAMPAIGN DECISION DETAILS");text(DebateSession.handles(event.id())?"No answer scores -2 and moves to the next question. Leaving skips all remaining questions. Final margin × 0.025 becomes a lasting share shift, capped at ±0.30 pp per debate. Opponent scores: policy-focused 3/3/3; combative 2/4/2; reassuring 2/2/4.":"No response: "+event.missedResponse());for(var choice:definition.options()){separator();text(choice.label());text(campaignEffectText(choice.effects(),100));}pause();}finally{clockArmed=armed;}continue;}
            if(!event.choices().get(response-1).available()){text("Not enough campaign cash for this response.");pause();continue;}
            if(confirm("Choose this response?")){clockArmed=false;submit(CareerCommand.campaign(GameCommand.respond(response-1)));return;}
        }}finally{clockArmed=false;}
    }
    private String campaignEffectText(List<CampaignEvent.Effect> effects,int limit){
        if(effects.isEmpty())return "No resource change.";
        var descriptions=effects.stream().limit(limit).map(e->{String side=e.side()==CampaignEvent.Side.PLAYER?"You":e.side()==CampaignEvent.Side.OPPONENT?"Opponent":"Both";return side+" "+switch(e.kind()){
            case FUNDS->String.format(Locale.ROOT,"cash $%+d",e.amount());
            case SUPPORT->String.format(Locale.ROOT,"support %+.2f pp (%d turns)",e.amount()/100.0,e.duration());
            case FUNDRAISING->String.format(Locale.ROOT,"fundraising $%+d (%d turns)",e.amount(),e.duration());
            case ACTION_COST->e.task()+String.format(Locale.ROOT," cost %+d (%d turns)",e.amount(),e.duration());
            case COMPLETE_TASK->"complete "+e.task();case REMOVE_TASK->"redo "+e.task();};}).toList();
        return String.join(" | ",descriptions)+(effects.size()>limit?" | more in details":"");
    }
}
