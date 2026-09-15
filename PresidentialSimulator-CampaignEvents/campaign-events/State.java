import java.util.EnumSet;
import java.util.Collections;
import java.util.Set;

/** Immutable scenario card. Objectives and starting ownership are fictional. */
public final class State {
    public enum Task {
        TOWN_HALL("Town hall", 100), FIELD_OFFICE("Field office", 175), OUTREACH("Outreach event", 150);
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
    public State(String name, int electoralVotes, boolean startingSupport, Set<Task> objectives) {
        if (name == null || name.isBlank() || electoralVotes <= 0 || objectives == null || objectives.isEmpty())
            throw new IllegalArgumentException("Invalid state card");
        this.name = name; this.electoralVotes = electoralVotes; this.startingSupport = startingSupport;
        this.objectives = Collections.unmodifiableSet(EnumSet.copyOf(objectives));
    }
    public String getName() { return name; }
    public int getElectoralVotes() { return electoralVotes; }
    public boolean hasStartingSupport() { return startingSupport; }
    public Set<Task> getObjectives() { return objectives; }
}
