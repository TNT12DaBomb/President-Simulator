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
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

/** Guided text adapter. The same commands and snapshots can drive a future GUI. */
public final class TextUI {
    private static final int PAGE_SIZE = 7;
    private final Scanner input;
    private final PrintStream out;
    private final long seed;
    private final Path savePath;
    private final boolean gui;
    private final boolean color;
    private CareerEngine career;
    private boolean exiting;
    public TextUI(InputStream input, PrintStream out, long seed, Path savePath, boolean gui, boolean color) {
        this.input = new Scanner(input); this.out = out; this.seed = seed;
        this.savePath = savePath; this.gui = gui; this.color = color;
    }
    private void title(String heading) {
        out.println();
        out.println(color ? "\033[1;36m" + heading + "\033[0m" : heading);
        out.println("------------------------------------------------------------");
    }
    private void text(String paragraph) {
        StringBuilder line = new StringBuilder();
        for (String word : paragraph.split("\\s+")) {
            if (line.length() + word.length() + 1 > 72) { out.println("  " + line); line.setLength(0); }
            if (line.length() > 0) line.append(' ');
            line.append(word);
        }
        if (line.length() > 0) out.println("  " + line);
    }
    private String read(String prompt) {
        out.print("\n  " + prompt + " > "); out.flush();
        if (!input.hasNextLine()) throw new NoSuchElementException();
        return input.nextLine().trim();
    }
    private int choose(String prompt, int min, int max) {
        while (true) {
            try {
                int value = Integer.parseInt(read(prompt));
                if (value >= min && value <= max) return value;
            } catch (NumberFormatException ignored) { }
            text("Please type a number from " + min + " to " + max + ", then press Enter.");
        }
    }
    private boolean confirm(String prompt) {
        text(prompt); out.println("\n  1  Confirm\n  0  Go back");
        return choose("Choose", 0, 1) == 1;
    }
    private void pause() { read("Press Enter to continue"); }
    public void run() {
        try {
            while (!exiting) {
                if (career == null) welcome();
                else careerStep();
            }
        } catch (NoSuchElementException ex) {
            boolean saved = career != null && save(savePath, false);
            text(saved ? "Input closed. Your career was saved; choose Resume next time."
                : "Input closed. Resume will use your most recent successful save.");
        }
    }
    private void welcome() {
        title("PRESIDENTIAL SIMULATOR  /  GOVERNANCE EDITION");
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
        career = new CareerEngine(seed, President.Difficulty.values()[difficulty - 1], President.RunningMate.values()[mate - 1]);
        title("YOUR FIRST BRIEFING");
        text("The campaign still lasts 16 turns. After the election, your career continues: a win opens the transition into office; a loss opens four annual comeback decisions.");
        text("Each presidential term has 48 monthly turns with two actions each. Two elected terms end the career. The 0 menu contains saves, developer tools, help, and exit options.");
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
        if (g.pendingEvent() != null) { event(g.pendingEvent()); return; }
        title("CAMPAIGN " + v.cycle() + "  /  TURN " + (g.turnsUsed() + 1) + " OF " + g.totalTurns());
        out.printf("  YOUR CASH $%d    OPPONENT CASH $%d%n", g.playerFunds(), g.opponentFunds());
        out.printf("  CURRENT BOARD  You %d EV  |  Opponent %d EV  |  270 needed%n", g.playerEV(), g.opponentEV());
        text("Career year " + v.year() + ". " + v.reputation());
        if (!g.activeEffects().isEmpty()) text("Temporary effects active: review the campaign briefing for details.");
        out.println("\n  1  Take a campaign action\n  2  Campaign briefing\n  3  Career status\n  4  Record campaign promise\n  0  Game menu");
        switch (choose("Campaign desk", 0, 4)) {
            case 0 -> gameMenu();
            case 4 -> choosePolicy(OfficeCommand.Type.PLEDGE);
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
            case 1 -> { if (confirm("Begin this four-year term? You will take up to two actions per month, then choose End month.")) submit(CareerCommand.advance()); }
            case 2 -> { title("TRANSITION RESOURCES"); if (v.transitionResources().isEmpty()) text("No campaign preparation credits are available."); else v.transitionResources().forEach(this::text); pause(); }
            case 3 -> status();
        }
    }
    private void presidencyStep(CareerView v) {
        Presidency.View p = v.presidency();
        if (p.pendingEvent() != null) { officeEvent(p.pendingEvent()); return; }
        title(p.date() + "  /  PRESIDENTIAL DESK  /  TERM " + v.electionsWon());
        out.printf("  MONTH %d / 48    ACTIONS LEFT %d    TREASURY $%d%n", v.termMonths() + 1, p.actionsLeft(), p.treasury());
        text(p.publicFeedback());
        text("Congress: " + p.congress().houseControl() + " House / " + p.congress().senateControl() + " Senate.");
        text(v.termMonths() < 24 ? "Midterms in " + (24 - v.termMonths()) + " months." : "Midterms complete; term ends in " + (48 - v.termMonths()) + " months.");
        out.println("\n  1  Office & administration\n  2  Legislative agenda\n  3  Public & personal\n  4  Party & elections\n  5  Statistics & records\n  6  End month\n  0  Game menu");
        switch (choose("Presidential desk", 0, 6)) {
            case 0 -> gameMenu();
            case 1 -> actionMenu("OFFICE & ADMINISTRATION", new CareerCommand.GovernanceAction[]{CareerCommand.GovernanceAction.CABINET_MEETING,
                CareerCommand.GovernanceAction.BUDGET_REVIEW, CareerCommand.GovernanceAction.SERVICE_REVIEW,
                CareerCommand.GovernanceAction.APPOINT_OFFICIAL, CareerCommand.GovernanceAction.MANAGE_CRISIS});
            case 2 -> legislationMenu();
            case 3 -> publicMenu();
            case 4 -> midtermMenu();
            case 5 -> officeRecords();
            case 6 -> { if (confirm("Close this month? Unused actions expire. Receipts +$100 and routine costs -$75 apply; events or midterms may follow.")) submit(CareerCommand.office(OfficeCommand.simple(OfficeCommand.Type.END_MONTH))); }
        }
    }
    private void actionMenu(String heading, CareerCommand.GovernanceAction[] actions) {
        title(heading);
        for (int i = 0; i < actions.length; i++) out.println("  " + (i + 1) + "  " + actions[i]);
        out.println("  0  Back"); int c = choose("Action", 0, actions.length);
        if (c > 0) {
            CareerView.GovernanceOption option = career.view().governanceOptions().stream().filter(o -> o.action() == actions[c - 1]).findFirst().orElseThrow();
            if (!option.available()) { text(option.reason()); pause(); return; }
            text("Uses one of this month's two actions. Public operating cost: $" + option.cost() + ".");
            if (confirm("Proceed with " + actions[c - 1] + "?")) submit(CareerCommand.govern(actions[c - 1]));
        }
    }
    private void legislationMenu() {
        Presidency.View p = career.view().presidency(); title("LEGISLATIVE DESK");
        text(p.congress().rule()); text("Current bill: " + (p.bill() == null ? "None" : p.bill().toString()));
        if (p.bill() != null) describePolicy(p.bill().issue(), p.bill().approach());
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
        text("Targeted visits open in month 13 and close after month 24. Complete both visits to win a contest. Each costs one monthly action and no public money.");
        text("Eight fictional House slates each carry two seats; four Senate races each carry one. Fixed seats: your caucus 210 House / 48 Senate. Other seats belong to the other caucus. Winning four House slates and three Senate races yields control of both chambers.");
        text(month < 12 ? "Campaign opens in " + (12 - month) + " months." : month < 24 ? "Campaign open; " + (24 - month) + " months until results." : "Results are final for this term.");
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
        if (month < 12 || month >= 24) { text("Visits are only available during months 13–24."); pause(); return; }
        out.println("  1  Listening visit\n  2  Organizing visit\n  0  Back");
        int task = choose("Visit", 0, 2); if (task == 0) return;
        if (confirm("Spend one monthly action on this visit?")) submit(CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.MIDTERM_TASK, (base + chosen - 1) * 2 + task - 1)));
    }
    private void officeEvent(PresidencyEvent event) {
        title("OFFICE EVENT / " + event.title()); text(event.description());
        for (int i = 0; i < event.choices().size(); i++) {
            PresidencyEvent.Choice c = event.choices().get(i);
            text((i + 1) + "  " + c.label() + " ($" + c.cost() + ")");
            if (c.delayed() != null) text("Follow-up in " + c.delayMonths() + " months, or at term end: " + c.delayed().message());
        }
        out.println("  0  Game menu"); int c = choose("Response", 0, event.choices().size());
        if (c == 0) gameMenu();
        else if (confirm("Apply this response? It uses no monthly action.")) submit(CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.EVENT_CHOICE, c - 1)));
    }
    private void officeRecords() {
        title("OFFICE RECORDS");
        out.println("  1  Career & operating accounts\n  2  Congress & promises\n  3  Laws & follow-ups\n  4  Career journal\n  0  Back");
        switch (choose("Record", 0, 4)) {
            case 1 -> status();
            case 2 -> {
                Presidency.View p = career.view().presidency();
                text("Your caucus: " + p.congress().houseSeats() + "/435 House seats, " + p.congress().senateSeats() + "/100 Senate seats.");
                text(p.congress().rule());
                text("Midterms resolve after month 24. Each contest requires a listening visit and an organizing visit during months 13–24. Incomplete contests go to the other caucus.");
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
        }
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
        text("This framework tracks public operating cash and administrative work. It does not yet model GDP, inflation, or unemployment.");
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
        out.println("  1  Save / load\n  2  Developer tools\n  3  How to play\n  4  Leave or end career\n  5  Game settings\n  0  Return to game");
        switch (choose("Menu", 0, 5)) {
            case 0 -> { }
            case 1 -> saveMenu();
            case 2 -> developerMenu();
            case 3 -> help();
            case 4 -> leaveMenu();
            case 5 -> {
                title("GAME SETTINGS");
                text("Seed: " + career.seed() + "; campaign difficulty: " + career.difficulty() + ". Two elected terms. Fictional objective-based election rules.");
                text("Presidency events: " + new String[]{"Off", "Every six months", "Every three months"}[career.view().eventFrequency()]);
                out.println("  1  Turn presidency events off\n  2  Events every six months\n  3  Events every three months\n  0  Back");
                int c = choose("Event frequency", 0, 3);
                if (c > 0) submit(CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.EVENT_FREQUENCY, c - 1)));
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
                if (slot > 0) { Path target = slotPath(slot); if (!Files.exists(target) || confirm("Replace manual save slot " + slot + "?")) save(target, true); }
            }
            case 3 -> loadMenu();
        }
    }
    private Path slotPath(int slot) { return savePath.toAbsolutePath().getParent().resolve("governance-slot-" + slot + ".save"); }
    private void loadMenu() {
        title("LOAD / RESUME");
        out.println("  1  Resume autosave\n  2  Load a manual slot\n  3  Import the previous campaign save\n  0  Back");
        int choice = choose("Load", 0, 3);
        if (choice == 0) return;
        Path target = savePath;
        if (choice == 2) { int slot = choose("Manual slot: 1, 2, or 3; 0 cancels", 0, 3); if (slot == 0) return; target = slotPath(slot); }
        if (choice == 3) { target = savePath.toAbsolutePath().getParent().resolve("campaign.save"); text("Looking for the old campaign.save beside your career autosave. A successful import leaves the old file intact."); }
        if (career != null && !confirm("Load this save and replace the current in-memory career?")) return;
        try {
            CareerEngine loaded = CareerSave.read(target);
            career = loaded;
            text("Career restored. Pending events and developer status are preserved.");
            save(savePath, false);
        } catch (IOException ex) { text("Could not load: " + ex.getMessage()); }
        pause();
    }
    private void leaveMenu() {
        title("LEAVE / END CAREER");
        out.println("  1  Save and return to title\n  2  Save and exit\n  3  Retire / end this career\n  0  Back");
        switch (choose("Leave", 0, 3)) {
            case 0 -> { }
            case 1 -> { if (save(savePath, false)) career = null; }
            case 2 -> { if (save(savePath, false)) exiting = true; }
            case 3 -> retire();
        }
    }
    private void retire() { if (confirm("End this career? Its history stays available, but normal play cannot resume this career.")) submit(CareerCommand.retire()); }
    private void developerMenu() {
        title("DEVELOPER TOOLS");
        text("Using a shortcut marks the save as a developer run. Scenario jumps replace the active career timeline; save a manual slot first if you want to keep it.");
        out.println("\n  1  Force current election outcome\n  2  Jump to a test scenario\n  3  Add test resources\n  4  Advance to end of current term\n  5  Monthly systems\n  0  Back");
        switch (choose("Developer menu", 0, 5)) {
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
            case 5 -> {
                out.println("  1  Advance one month\n  2  Toggle Congress control\n  3  Trigger an eligible event\n  0  Back");
                int c = choose("Monthly tools", 0, 3);
                if (c > 0) developer(new CareerCommand.DeveloperAction[]{CareerCommand.DeveloperAction.ADVANCE_MONTH, CareerCommand.DeveloperAction.CHANGE_CONGRESS, CareerCommand.DeveloperAction.TRIGGER_EVENT}[c - 1]);
            }
        }
    }
    private void developer(CareerCommand.DeveloperAction action) { if (confirm(action + "?")) submit(CareerCommand.dev(action)); }
    private boolean submit(CareerCommand command) {
        CareerReport report = career.submit(command);
        title(report.accepted() ? "CHAPTER UPDATE" : "ACTION NOT TAKEN");
        report.messages().forEach(this::text);
        if (report.accepted()) save(savePath, false);
        pause(); return report.accepted();
    }
    private void event(GameView.PendingEvent event) {
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
    private void journal(List<String> entries, String heading) {
        title(heading);
        for (int i = 0; i < entries.size(); i++) {
            text(entries.get(i));
            if ((i + 1) % 10 == 0 && i + 1 < entries.size()) {
                out.println("\n  1  More entries\n  0  Return"); if (choose("Journal", 0, 1) == 0) return;
            }
        }
        pause();
    }
    private boolean save(Path path, boolean notify) {
        try { CareerSave.write(career, path); if (notify) text("Career saved."); return true; }
        catch (IOException ex) { text("Could not save: " + ex.getMessage() + ". The current career remains in memory."); return false; }
    }
    private void help() {
        title("HOW TO PLAY");
        text("Type a menu number and press Enter. Zero goes back or opens the game menu. Browsing and cancelling are free. Accepted decisions autosave; three manual slots are available.");
        text("Campaigns last 16 turns. Complete each state's two listed objectives to claim it; if both sides or neither completes them, its starting owner keeps it. Events affect both campaigns. You can lose.");
        text("Record up to two campaign promises before election night. Victories lead through transition and inauguration; defeats lead to four annual rebuilding decisions and another run.");
        text("In office, take up to two actions per month, then choose End month. Each term has 48 months. Pending events must be answered; a free response is always available. Midterms occur after month 24.");
        text("Public correspondence has separate acknowledgment and delivery states. Policy signing spends the listed cost and schedules delivery; alternatives remain unresolved. Targeted midterm visits open in months 13–24 from Party & Elections.");
        text("Bills need chamber majorities or a one-bill negotiated agreement. Propose, negotiate, then sign or veto. Your promises and final-year administrative work affect inherited preparation for reelection.");
        text("Public feedback is descriptive. Operating cash is separate from campaign money; GDP and numerical approval are not modeled. The normal career ends after two elected terms.");
        text("Developer tools mark the save. Earlier quarterly and monthly career saves need their original editions; campaign-events-v1 saves can be imported. See README.md and BACKLOG.md for scope and future plans."); pause();
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
                out.printf("  %d  %-22s %2d EV   %s%n", i + 1, s.name(), s.electoralVotes(), s.playerControls() ? "YOURS" : "OPPONENT");
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
}
