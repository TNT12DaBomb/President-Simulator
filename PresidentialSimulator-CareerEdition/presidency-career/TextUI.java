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
        title("PRESIDENTIAL SIMULATOR  /  CAREER EDITION");
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
        text("Each presidential term has 16 quarterly decisions. Two elected terms end the career. The 0 menu contains saves, developer tools, help, and exit options.");
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
        out.println("\n  1  Take a campaign action\n  2  Campaign briefing\n  3  Career status\n  0  Game menu");
        switch (choose("Campaign desk", 0, 3)) {
            case 0 -> gameMenu();
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
            case 1 -> { if (confirm("Begin this four-year term? You will take one decision per quarter.")) submit(CareerCommand.advance()); }
            case 2 -> { title("TRANSITION RESOURCES"); if (v.transitionResources().isEmpty()) text("No campaign preparation credits are available."); else v.transitionResources().forEach(this::text); pause(); }
            case 3 -> status();
        }
    }
    private void presidencyStep(CareerView v) {
        title("PRESIDENTIAL DESK  /  TERM " + v.electionsWon());
        out.printf("  CAREER YEAR %d    TERM QUARTER %d / 16%n", v.year(), v.termQuarters() + 1);
        out.printf("  TREASURY $%d    NEXT RECEIPTS +$%d    ROUTINE COSTS -$%d%n", v.treasury(), v.quarterlyReceipts(), v.routineExpenses());
        text(v.publicFeedback());
        out.println("\n  1  Plan this quarter\n  2  Administration reports\n  3  Career journal\n  0  Game menu");
        switch (choose("Presidential desk", 0, 3)) {
            case 0 -> gameMenu();
            case 1 -> governMenu();
            case 2 -> status();
            case 3 -> journal(v.history(), "CAREER JOURNAL");
        }
    }
    private void governMenu() {
        title("PLAN THIS QUARTER");
        out.println("  1  Public briefings and cabinet work\n  2  Budget and service administration\n  3  Continue routine administration\n  0  Back");
        switch (choose("Work area", 0, 3)) {
            case 0 -> { }
            case 1 -> {
                out.println("\n  1  Hold a public briefing\n  2  Coordinate the cabinet\n  0  Back");
                int c = choose("Action", 0, 2);
                if (c != 0) govern(c == 1 ? CareerCommand.GovernanceAction.PUBLIC_BRIEFING : CareerCommand.GovernanceAction.CABINET_MEETING);
            }
            case 2 -> {
                out.println("\n  1  Reconcile the annual budget\n  2  Fund a service-delivery review\n  0  Back");
                int c = choose("Action", 0, 2);
                if (c != 0) govern(c == 1 ? CareerCommand.GovernanceAction.BUDGET_REVIEW : CareerCommand.GovernanceAction.SERVICE_REVIEW);
            }
            case 3 -> govern(CareerCommand.GovernanceAction.ROUTINE_QUARTER);
        }
    }
    private void govern(CareerCommand.GovernanceAction action) {
        CareerView v = career.view();
        CareerView.GovernanceOption option = v.governanceOptions().stream().filter(o -> o.action() == action).findFirst().orElseThrow();
        if (!option.available()) { text(option.reason()); pause(); return; }
        title("QUARTERLY DECISION");
        text(action + ". Action cost $" + option.cost() + "; routine receipts +$250 and expenses -$200 also apply.");
        text(switch (action) {
            case PUBLIC_BRIEFING -> "Records a public briefing in this year's administrative record. Final-year records can supply a town-hall objective next campaign.";
            case CABINET_MEETING -> "Records cabinet coordination. Final-year records can supply a field-office objective next campaign.";
            case BUDGET_REVIEW -> "The first reconciliation each year recovers one $100 duplicate payment. Published final-year accounts also unlock a private fundraising event next campaign.";
            case SERVICE_REVIEW -> "Records a funded service review. Final-year records can supply an outreach objective next campaign.";
            case ROUTINE_QUARTER -> "Routine operations continue, but no briefing, coordination, budget review, or service review is recorded.";
        });
        if (confirm("Advance one quarter with this action?")) submit(CareerCommand.govern(action));
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
        out.printf("  TIME IN OFFICE %d years, %d quarter(s)%n", v.servedQuarters() / 4, v.servedQuarters() % 4);
        out.println("\n  PUBLIC FEEDBACK"); text(v.publicFeedback());
        out.println("\n  REPUTATION / RECORD"); text(v.reputation());
        out.println("\n  ORGANIZATIONAL SUPPORT"); text(v.support());
        pause();
        title("ECONOMY AND OPERATING ACCOUNTS");
        text("This framework tracks public operating cash and administrative work. It does not yet model GDP, inflation, or unemployment.");
        out.printf("  TREASURY $%d    QUARTERLY RECEIPTS $%d    ROUTINE EXPENSES $%d%n", v.treasury(), v.quarterlyReceipts(), v.routineExpenses());
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
        out.println("  1  Save / load\n  2  Developer tools\n  3  How to play\n  4  Leave or end career\n  0  Return to game");
        switch (choose("Menu", 0, 4)) {
            case 0 -> { }
            case 1 -> saveMenu();
            case 2 -> developerMenu();
            case 3 -> help();
            case 4 -> leaveMenu();
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
    private Path slotPath(int slot) { return savePath.toAbsolutePath().getParent().resolve("career-slot-" + slot + ".save"); }
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
        out.println("\n  1  Force current election outcome\n  2  Jump to a test scenario\n  3  Add test resources\n  4  Advance to end of current term\n  0  Back");
        switch (choose("Developer menu", 0, 4)) {
            case 0 -> { }
            case 1 -> {
                out.println("\n  1  Force victory\n  2  Force defeat\n  0  Back"); int c = choose("Outcome", 0, 2);
                if (c > 0) developer(c == 1 ? CareerCommand.DeveloperAction.FORCE_WIN : CareerCommand.DeveloperAction.FORCE_LOSS);
            }
            case 2 -> {
                out.println("\n  1  Midway through first term\n  2  Start reelection campaign\n  3  Midway through second term\n  4  Final quarter of second term\n  0  Back");
                int c = choose("Scenario", 0, 4);
                if (c > 0) developer(new CareerCommand.DeveloperAction[]{CareerCommand.DeveloperAction.MID_FIRST_TERM,
                    CareerCommand.DeveloperAction.REELECTION_START, CareerCommand.DeveloperAction.MID_SECOND_TERM, CareerCommand.DeveloperAction.FINAL_QUARTER}[c - 1]);
            }
            case 3 -> {
                out.println("\n  1  Add $500 campaign cash\n  2  Add $500 public treasury\n  0  Back"); int c = choose("Resource", 0, 2);
                if (c > 0) developer(c == 1 ? CareerCommand.DeveloperAction.ADD_CAMPAIGN_CASH : CareerCommand.DeveloperAction.ADD_TREASURY);
            }
            case 4 -> developer(CareerCommand.DeveloperAction.FINISH_TERM);
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
        title("HOW TO PLAY  /  CAMPAIGNS");
        text("The campaign has 16 turns and 36 fictional events. Complete a state's two objectives. If only your team finishes both, you hold it; if both teams finish or neither does, starting ownership applies. Reach 270 EV to win.");
        text("Actions cost a turn; browsing and cancelling do not. Some events require a response. Use 0 to reach saves and other game-menu options even while a response is pending.");
        pause();
        title("HOW TO PLAY  /  YOUR CAREER");
        text("A win opens a transition, then sixteen quarterly administrative decisions. Completed campaign work in held states supplies one-use transition credits. The final year's completed administrative work can prepare objectives and a private fundraising event for your next campaign.");
        text("A loss remains in the record. Donors withhold $200 from the next campaign until you meet them. Spend four annual decisions reviewing the campaign, rebuilding contacts, or living privately before running again.");
        text("Two four-year elected terms are the limit, even when nonconsecutive. The treasury and economic screen currently covers operating cash and administrative records. Public feedback and reputation are descriptive; there are no numerical approval ratings.");
        text("0 opens the game menu. Autosaves, three manual slots, load, help, developer tools, and exit are available there. Developer scenarios are marked and can replace the active timeline. Wars, scandals, and dictatorship mechanics are not part of this version.");
        pause();
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
