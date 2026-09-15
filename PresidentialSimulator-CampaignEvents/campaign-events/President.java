import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** Internal campaign resource model; only GameEngine owns these mutable objects. */
public final class President {
    public static final int CAMPAIGN_TURNS = 16;
    public enum Difficulty { NORMAL, HARD }
    public enum RunningMate { COMMUNITY_ORGANIZER, PARTY_ORGANIZER, FUNDRAISER, LOGISTICS_COORDINATOR }
    private int funds;
    private final RunningMate mate;
    private final Map<String, EnumSet<State.Task>> completed = new HashMap<>();
    President(int funds, RunningMate mate) {
        if (funds < 0 || mate == null) throw new IllegalArgumentException("Invalid campaign setup");
        this.funds = funds; this.mate = mate;
    }
    public int getFunds() { return funds; }
    public Set<State.Task> getCompletedTasks(String state) {
        return Set.copyOf(completed.getOrDefault(state, EnumSet.noneOf(State.Task.class)));
    }
    int baseCost(State.Task task) {
        boolean discount = (mate == RunningMate.COMMUNITY_ORGANIZER && task == State.Task.TOWN_HALL)
            || (mate == RunningMate.PARTY_ORGANIZER && task == State.Task.FIELD_OFFICE)
            || (mate == RunningMate.LOGISTICS_COORDINATOR && task == State.Task.OUTREACH);
        return task.getCost() - (discount ? 50 : 0);
    }
    int changeFunds(int amount) {
        int old = funds;
        funds = Math.max(0, Math.addExact(funds, amount));
        return funds - old;
    }
    void complete(String state, State.Task task) {
        completed.computeIfAbsent(state, key -> EnumSet.noneOf(State.Task.class)).add(task);
    }
    void remove(String state, State.Task task) {
        completed.computeIfAbsent(state, key -> EnumSet.noneOf(State.Task.class)).remove(task);
    }
}
