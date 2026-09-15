import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/** Immutable state card. Objectives are fictional game rules, not political data. */
public final class State {
    public enum Task {
        TOWN_HALL("Town hall", 100), FIELD_OFFICE("Field office", 175),
        OUTREACH("Outreach event", 150);
        private final String label;
        private final int cost;
        Task(String label, int cost) { this.label = label; this.cost = cost; }
        public int getCost() { return cost; }
        @Override public String toString() { return label; }
    }

    private final String name;
    private final int electoralVotes;
    private final boolean startingSupport;
    private final Set<Task> objectives;
    private final int challengeTurn;

    public State(String name, int electoralVotes, boolean startingSupport,
                 Set<Task> objectives, int challengeTurn) {
        if (name == null || name.trim().isEmpty() || electoralVotes <= 0
                || objectives == null || objectives.isEmpty()
                || challengeTurn < 0 || challengeTurn > President.CAMPAIGN_TURNS
                || (!startingSupport && challengeTurn != 0)) {
            throw new IllegalArgumentException("Invalid state card");
        }
        this.name = name;
        this.electoralVotes = electoralVotes;
        this.startingSupport = startingSupport;
        this.objectives = Collections.unmodifiableSet(EnumSet.copyOf(objectives));
        this.challengeTurn = challengeTurn;
    }
    public String getName() { return name; }
    public int getElectoralVotes() { return electoralVotes; }
    public boolean hasStartingSupport() { return startingSupport; }
    public Set<Task> getObjectives() { return objectives; }
    public int getChallengeTurn() { return challengeTurn; }
    public String objectiveDescription() {
        if (startingSupport) return challengeTurn == 0 ? "Starting bloc; no action required"
            : "Field office protects against challenge after turn " + challengeTurn;
        return "Complete " + objectives;
    }
}
