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
    private GameEngine game;
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
            title("PRESIDENTIAL SIMULATOR  /  CAMPAIGN EVENTS");
            text("Build your campaign one decision at a time. Type a menu number and press Enter. No terminal commands are needed.");
            text("This is a fictional campaign board game. Its events and state objectives are invented.");
            while (game == null) {
                out.println("\n  1  Start a new campaign\n  2  Resume saved campaign\n  3  How to play\n  0  Exit");
                switch (choose("Welcome", 0, 3)) {
                    case 0 -> { return; }
                    case 1 -> setup();
                    case 2 -> {
                        try { game = CampaignSave.read(savePath); text("Saved campaign restored."); }
                        catch (IOException ex) { text("Could not resume: " + ex.getMessage()); }
                    }
                    case 3 -> help();
                    default -> throw new IllegalStateException();
                }
            }
            play();
        } catch (NoSuchElementException ex) {
            if (game != null) save();
            text("Input closed. You can use Resume next time if saving succeeded.");
        }
    }
    private void setup() {
        if (Files.exists(savePath) && !confirm("Starting a new campaign replaces the existing autosave. Continue?")) return;
        title("NEW CAMPAIGN  /  DIFFICULTY");
        out.println("  1  Normal   You start with $1,400; opponent with $1,200\n  2  Hard     You start with $1,000; opponent with $1,600");
        int difficulty = choose("Difficulty", 1, 2);
        title("NEW CAMPAIGN  /  RUNNING MATE");
        out.println("  1  Community organizer   Town halls cost $50 less\n  2  Party organizer       Field offices cost $50 less\n  3  Fundraiser            Start with $200 extra\n  4  Logistics coordinator Outreach costs $50 less");
        int mate = choose("Running mate", 1, 4);
        game = new GameEngine(seed, President.Difficulty.values()[difficulty - 1], President.RunningMate.values()[mate - 1]);
        title("YOUR FIRST BRIEFING");
        text("You have 16 turns. Complete both listed objectives to contest a state. When both teams finish, or neither finishes, starting ownership decides. The opponent acts after every second turn.");
        text("An eligible event is drawn after each turn. Some need a response. Funds and completed work can change for either side. Read the turn recap to see exactly what happened.");
        text("Browsing is free. Actions use a turn only after you confirm. Your campaign autosaves after each accepted decision, including unresolved events.");
        save(); pause();
    }
    private void play() {
        while (true) {
            GameView view = game.view();
            if (view.pendingEvent() != null) { if (event(view.pendingEvent())) return; continue; }
            if (view.complete()) { results(); return; }
            dashboard(view);
            out.println("\n  1  Campaign in a state\n  2  Fundraise\n  3  Inspect the state board\n  4  Read the campaign journal\n  5  How to play\n  6  Save and return to desktop\n  7  Pass this turn");
            switch (choose("Your next step", 1, 7)) {
                case 1 -> browse(true);
                case 2 -> {
                    if (confirm("Fundraise for $" + view.fundraisingAmount() + "? This uses one turn, then the opponent and event steps run.")) submit(GameCommand.fundraise());
                }
                case 3 -> browse(false);
                case 4 -> journal();
                case 5 -> help();
                case 6 -> { if (save()) { text("Campaign saved. Choose Resume the next time you play."); return; } }
                case 7 -> { if (confirm("Pass this turn? You gain no resources; the opponent and event steps still run.")) submit(GameCommand.rest()); }
                default -> throw new IllegalStateException();
            }
        }
    }
    private void dashboard(GameView v) {
        title("CAMPAIGN DESK  /  TURN " + (v.turnsUsed() + 1) + " OF " + v.totalTurns());
        out.printf("  YOUR FUNDS  $%-7d  OPPONENT FUNDS  $%d%n", v.playerFunds(), v.opponentFunds());
        out.printf("  CURRENT BOARD  You %d EV  |  Opponent %d EV  |  270 needed%n", v.playerEV(), v.opponentEV());
        text("Current board ownership is a game tally, not an election forecast.");
        if (!v.activeEffects().isEmpty()) { out.println("\n  ACTIVE EFFECTS"); v.activeEffects().forEach(this::text); }
        List<String> recent = v.history().stream().filter(s -> s.startsWith("NEWS") || s.startsWith("BOARD")).toList();
        if (!recent.isEmpty()) { out.println("\n  LATEST UPDATE"); text(recent.get(recent.size() - 1)); }
    }
    private void browse(boolean campaign) {
        int page = 0;
        String filter = "";
        while (true) {
            final String query = filter;
            List<GameView.StateView> states = game.view().states().stream()
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
            out.println("\n  N  Next page    P  Previous page    S  Search\n  A  All states   0  Return to campaign desk");
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
        return submit(GameCommand.campaign(s.name(), selected.task()));
    }
    private boolean submit(GameCommand command) {
        TurnReport report = game.submit(command);
        title(report.accepted() ? "TURN RECAP" : "ACTION NOT TAKEN");
        report.messages().forEach(this::text);
        if (report.accepted()) save();
        pause();
        return report.accepted();
    }
    private boolean event(GameView.PendingEvent event) {
        title("A DECISION IS NEEDED  /  " + event.title().toUpperCase(Locale.ROOT));
        GameView current = game.view();
        out.printf("  YOUR FUNDS  $%d  |  OPPONENT FUNDS  $%d%n", current.playerFunds(), current.opponentFunds());
        if (event.state() != null) {
            text("Location: " + event.state());
            current.states().stream().filter(s -> s.name().equals(event.state())).findFirst().ifPresent(s -> {
                text("Your remaining objectives: " + missing(s.objectives(), s.playerTasks()));
                text("Current owner: " + (s.playerControls() ? "You" : "Opponent") + " (" + s.electoralVotes() + " EV).");
            });
        }
        text(event.description()); text("Responding does not use another turn.");
        for (int i = 0; i < event.choices().size(); i++) {
            GameView.ChoiceView choice = event.choices().get(i);
            text((i + 1) + "  " + choice.label() + (choice.available() ? "" : " [Unavailable: " + choice.reason() + "]"));
        }
        out.println("\n  0  Save and exit with this decision pending");
        int response = choose("Your response", 0, event.choices().size());
        if (response == 0) {
            if (save()) { text("Campaign saved with this decision pending."); return true; }
            return false;
        }
        if (!event.choices().get(response - 1).available()) { text("Choose an affordable response; declining is always free."); return false; }
        if (confirm("Apply this event response?")) submit(GameCommand.respond(response - 1));
        return false;
    }
    private void journal() {
        title("CAMPAIGN JOURNAL");
        List<String> entries = game.view().history();
        for (int i = 0; i < entries.size(); i++) {
            text(entries.get(i));
            if ((i + 1) % 12 == 0 && i + 1 < entries.size()) {
                out.println("\n  1  More entries\n  0  Return");
                if (choose("Journal", 0, 1) == 0) return;
            }
        }
        pause();
    }
    private void help() {
        title("HOW TO PLAY");
        text("Win 270 of the 538 electoral votes by the end of turn 16. Each state has two fictional objectives. If only one campaign completes both, it owns that state. If both or neither complete both, the state's starting owner keeps it.");
        text("Choose Campaign in a state, search or browse, inspect the requirements, then confirm one action. A town hall costs $100, a field office $175, and outreach $150 before discounts or events. Fundraising normally adds $250 and uses one turn.");
        text("The opponent takes one action after every second turn. Events can add or remove money, complete or reopen work, or change costs temporarily for either side. They do not directly assign electoral votes; ownership follows the same objective rules.");
        text("An event appears after each turn when an unused eligible event is available. Some offer a choice with an exact cost. A temporary effect lasts the stated number of upcoming turns, even if you do not use it. Effects can combine; action costs never fall below $25 and fundraising never below $50.");
        text("The board and journal are free to inspect. Invalid inputs and cancelled actions use no turns. Your progress autosaves after actions and responses. Resume restores the same events, including unanswered choices.");
        text("A tie ends without declaring a winner. This edition ends at the election; governing and reelection are future features.");
        pause();
    }
    private boolean save() {
        try { CampaignSave.write(game, savePath); return true; }
        catch (IOException ex) { text("Could not save: " + ex.getMessage() + ". Your game is still in memory; choose Save again before exiting."); return false; }
    }
    private void results() {
        ElectionResult result = game.result();
        title("ELECTION NIGHT");
        out.printf("  YOU  %d EV       OPPONENT  %d EV%n", result.getPlayerEV(), result.getOpponentEV());
        text(result.getOutcomeText()); text("Your final state results and full campaign journal are available below.");
        if (gui && !GraphicsEnvironment.isHeadless()) new ElectionGUI().showResults(result, game.states(), game.districtOfColumbia());
        while (true) {
            out.println("\n  1  Inspect state results\n  2  Read campaign journal\n  0  Finish");
            int choice = choose("Election night", 0, 2);
            if (choice == 0) return;
            if (choice == 1) browse(false); else journal();
        }
    }
}
