import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Campaign resources and decision history for an unnamed fictional player. */
public final class President {
    public static final int CAMPAIGN_TURNS = 8;
    public enum Difficulty { NORMAL, HARD }
    public enum RunningMate { COMMUNITY_ORGANIZER, PARTY_ORGANIZER, FUNDRAISER, LOGISTICS_COORDINATOR }
    private final Difficulty difficulty;
    private final RunningMate runningMate;
    private int funds;
    private int turnsUsed;
    private final Map<String, EnumSet<State.Task>> completed = new LinkedHashMap<>();
    private final List<String> history = new ArrayList<>();

    public President(Difficulty difficulty, RunningMate runningMate) {
        if (difficulty == null || runningMate == null) throw new IllegalArgumentException("Choose difficulty and running mate");
        this.difficulty = difficulty;
        this.runningMate = runningMate;
        funds = difficulty == Difficulty.HARD ? 700 : 1000;
        if (runningMate == RunningMate.FUNDRAISER) funds += 200;
        history.add("Setup: " + difficulty + "; running mate " + runningMate + "; funds $" + funds);
    }
    public Difficulty getDifficulty() { return difficulty; }
    public RunningMate getRunningMate() { return runningMate; }
    public int getFunds() { return funds; }
    public int getTurnsUsed() { return turnsUsed; }
    public int getTurnsRemaining() { return CAMPAIGN_TURNS - turnsUsed; }
    public boolean isCampaignComplete() { return turnsUsed == CAMPAIGN_TURNS; }
    public List<String> getHistory() { return Collections.unmodifiableList(new ArrayList<>(history)); }
    public Set<State.Task> getCompletedTasks(String state) {
        EnumSet<State.Task> tasks = completed.get(state);
        return Collections.unmodifiableSet(tasks == null ? EnumSet.noneOf(State.Task.class) : EnumSet.copyOf(tasks));
    }
    public int cost(State.Task task) {
        if (task == null) throw new IllegalArgumentException("Choose an action");
        boolean discounted = (runningMate == RunningMate.COMMUNITY_ORGANIZER && task == State.Task.TOWN_HALL)
            || (runningMate == RunningMate.PARTY_ORGANIZER && task == State.Task.FIELD_OFFICE)
            || (runningMate == RunningMate.LOGISTICS_COORDINATOR && task == State.Task.OUTREACH);
        return task.getCost() - (discounted ? 50 : 0);
    }
    /** Returns feedback. A rejected action never spends money or a turn. */
    public String perform(State state, State.Task task) {
        if (state == null || task == null) return "Choose a valid state and action.";
        if (isCampaignComplete()) return "Campaign is already complete.";
        if (state.hasStartingSupport() && (state.getChallengeTurn() == 0 || task != State.Task.FIELD_OFFICE))
            return "That action is not needed here. Check the state's objectives.";
        if (!state.hasStartingSupport() && !state.getObjectives().contains(task))
            return "That action does not fulfill an objective here.";
        if (getCompletedTasks(state.getName()).contains(task)) return "Already completed here; choose another action.";
        int price = cost(task);
        if (funds < price) return "Insufficient funds. Fundraise or choose a cheaper action.";
        funds -= price;
        completed.computeIfAbsent(state.getName(), key -> EnumSet.noneOf(State.Task.class)).add(task);
        turnsUsed++;
        return record(task + " in " + state.getName() + " (-$" + price + ")");
    }
    public String fundraise() {
        if (isCampaignComplete()) return "Campaign is already complete.";
        funds += 250;
        turnsUsed++;
        return record("Fundraised (+$250)");
    }
    public String rest() {
        if (isCampaignComplete()) return "Campaign is already complete.";
        turnsUsed++;
        return record("Skipped campaigning; no objectives completed");
    }
    private String record(String action) {
        String entry = "Turn " + turnsUsed + ": " + action + "; funds $" + funds;
        history.add(entry);
        return entry;
    }
}
