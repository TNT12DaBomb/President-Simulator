import java.util.List;
import java.util.Set;

/** Deeply immutable UI-facing snapshot. No Swing, terminal, or mutable model references. */
public record GameView(long seed, int turnsUsed, int totalTurns, int playerFunds, int opponentFunds,
                       int playerEV, int opponentEV, boolean complete, List<StateView> states,
                       List<String> activeEffects, PendingEvent pendingEvent, List<String> history,
                       int fundraisingAmount) {
    public record ActionView(State.Task task, int cost, boolean available, String reason) { }
    public record StateView(String name, int electoralVotes, boolean playerControls,
                            boolean playerStarts, Set<State.Task> objectives, Set<State.Task> playerTasks,
                            Set<State.Task> opponentTasks, List<ActionView> actions, String explanation) {
        public StateView {
            objectives = Set.copyOf(objectives); playerTasks = Set.copyOf(playerTasks);
            opponentTasks = Set.copyOf(opponentTasks); actions = List.copyOf(actions);
        }
    }
    public record ChoiceView(String label, boolean available, String reason) { }
    public record PendingEvent(String id, String title, String description, String state, List<ChoiceView> choices) {
        public PendingEvent { choices = List.copyOf(choices); }
    }
    public GameView {
        states = List.copyOf(states); activeEffects = List.copyOf(activeEffects); history = List.copyOf(history);
    }
}
