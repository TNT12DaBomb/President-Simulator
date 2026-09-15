import java.awt.GraphicsEnvironment;
import java.util.NoSuchElementException;
import java.util.Scanner;

/** Text campaign with an optional Swing results window. */
public final class PresidentialSimulator {
    private final Scanner input;
    private final ElectoralCollege college;
    private PresidentialSimulator(Scanner input, ElectoralCollege college) {
        this.input = input;
        this.college = college;
    }
    public static void main(String[] args) {
        boolean consoleOnly = false;
        long seed = System.nanoTime();
        try {
            for (int i = 0; i < args.length; i++) {
                if ("--no-gui".equals(args[i])) consoleOnly = true;
                else if ("--seed".equals(args[i]) && i + 1 < args.length) seed = Long.parseLong(args[++i]);
                else throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException ex) {
            System.err.println("Usage: java PresidentialSimulator [--no-gui] [--seed <integer>]");
            return;
        }
        try (Scanner scanner = new Scanner(System.in)) {
            new PresidentialSimulator(scanner, new ElectoralCollege(seed)).play(consoleOnly);
        } catch (NoSuchElementException ex) {
            System.out.println("\nCampaign cancelled: input closed. No election was run.");
        }
    }
    private int choose(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt + " ");
            if (!input.hasNextLine()) throw new NoSuchElementException();
            String line = input.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) return value;
            } catch (NumberFormatException ignored) { }
            System.out.println("Enter a whole number from " + min + " to " + max + ".");
        }
    }
    private void play(boolean consoleOnly) {
        System.out.println("=== PRESIDENTIAL SIMULATOR ===");
        System.out.println("Fictional campaign board game. State objectives and starting support are invented.");
        System.out.println("538 EV; 270 needed. Eight turns. Complete BOTH objectives to claim a state.");
        System.out.println("No hidden vote rolls: the final board determines the result.");
        System.out.println("Scenario seed: " + college.getSeed());
        System.out.println("Normal starts with $1000; Hard starts with $700.");
        int difficulty = choose("Difficulty: 1. Normal  2. Hard", 1, 2);
        System.out.println("Running mate benefits (game costs only):");
        System.out.println("1. Community organizer: town halls cost $50 instead of $100");
        System.out.println("2. Party organizer: field offices cost $125 instead of $175");
        System.out.println("3. Fundraiser: start with an extra $200");
        System.out.println("4. Logistics coordinator: outreach costs $100 instead of $150");
        int mate = choose("Choose running mate:", 1, 4);
        President player = new President(President.Difficulty.values()[difficulty - 1], President.RunningMate.values()[mate - 1]);
        System.out.println("\nOpponent schedule: Texas after turn 3; Illinois after turn 6.");
        System.out.println("A field office protects or recovers each challenged state. All other starting states stay yours.");
        System.out.println("Unfinished objectives do not claim a state. Funds left over do not buy votes.");
        printBoard(player);
        while (!player.isCampaignComplete()) {
            System.out.printf("%nTurn %d/%d | Funds $%d | Current board %d EV (not a forecast)%n",
                player.getTurnsUsed() + 1, President.CAMPAIGN_TURNS, player.getFunds(), college.currentPlayerEV(player));
            System.out.printf("1. Town hall ($%d)  2. Field office ($%d)  3. Outreach ($%d)%n",
                player.cost(State.Task.TOWN_HALL), player.cost(State.Task.FIELD_OFFICE), player.cost(State.Task.OUTREACH));
            System.out.println("4. Fundraise (+$250; one turn)  5. Skip turn  6. View board  7. Decision history  8. Quit");
            int choice = choose("Action:", 1, 8);
            int beforeTurn = player.getTurnsUsed();
            int beforeEV = college.currentPlayerEV(player);
            if (choice <= 3) {
                int target = choose("State number (1-51; 0 cancels; choose 6 from the main menu to view the board):", 0, 51);
                if (target == 0) continue;
                State state = college.getAllStates().get(target - 1);
                System.out.println(player.perform(state, State.Task.values()[choice - 1]));
                System.out.println(state.getName() + ": " + college.explain(player, state));
            } else if (choice == 4) System.out.println(player.fundraise());
            else if (choice == 5) System.out.println(player.rest());
            else if (choice == 6) printBoard(player);
            else if (choice == 7) player.getHistory().forEach(System.out::println);
            else { System.out.println("Campaign cancelled. No election was run."); return; }
            if (player.getTurnsUsed() != beforeTurn) {
                for (State state : college.getAllStates()) {
                    if (state.getChallengeTurn() == player.getTurnsUsed())
                        System.out.println("Opponent challenge: " + state.getName() + ": " + college.explain(player, state));
                }
                int afterEV = college.currentPlayerEV(player);
                System.out.printf("Board change this turn: %+d EV; now %d EV. Turns left: %d.%n",
                    afterEV - beforeEV, afterEV, player.getTurnsRemaining());
            }
        }
        ElectionResult result = college.runElection(player);
        System.out.println("\n=== FINAL RESULT ===");
        System.out.printf("You: %d EV | Opponent: %d EV | Total: %d%n",
            result.getPlayerEV(), result.getOpponentEV(), result.getTotalEV());
        System.out.println(result.getOutcomeText());
        for (State state : college.getAllStates()) {
            boolean won = state == college.getDistrictOfColumbia() ? result.didPlayerWinDC() : result.getStateResults().get(state.getName());
            System.out.printf("%-22s %2d EV | %-8s | %s%n", state.getName(), state.getElectoralVotes(), won ? "YOU" : "OPPONENT", result.getReasons().get(state.getName()));
        }
        System.out.println("\nDECISION HISTORY");
        result.getHistory().forEach(System.out::println);
        System.out.println("This version ends at the election; governing gameplay is not implemented.");
        if (!consoleOnly && !GraphicsEnvironment.isHeadless())
            new ElectionGUI().showResults(result, college.getStates(), college.getDistrictOfColumbia());
        else if (!consoleOnly) System.out.println("No desktop detected; results are shown in the console.");
    }
    private void printBoard(President player) {
        System.out.println("\nSTATE BOARD - all objectives are fictional, not real political leanings");
        int index = 1;
        for (State state : college.getAllStates()) {
            System.out.printf("%2d. %-22s %2d EV | %-8s | %s | Done: %s%n", index++, state.getName(),
                state.getElectoralVotes(), college.controls(player, state) ? "YOU" : "OPPONENT",
                state.objectiveDescription(), player.getCompletedTasks(state.getName()));
        }
    }
}
