import java.util.List;

/** Data-only event definitions; effects are interpreted by GameEngine. */
public record CampaignEvent(String id, String title, String description, Category category,
                            List<Effect> effects, List<Option> options) {
    public enum Category { FUNDING, OPERATIONS, VOLUNTEERS, DISRUPTION, OPPORTUNITY, QUIET }
    public enum Side { PLAYER, OPPONENT, BOTH }
    public enum Kind { FUNDS, COMPLETE_TASK, REMOVE_TASK, ACTION_COST, FUNDRAISING }
    public record Effect(Side side, Kind kind, int amount, State.Task task, int duration) {
        public Effect {
            if (side == null || kind == null) throw new IllegalArgumentException("Missing effect type");
            boolean timed = kind == Kind.ACTION_COST || kind == Kind.FUNDRAISING;
            boolean needsTask = kind == Kind.ACTION_COST || kind == Kind.COMPLETE_TASK || kind == Kind.REMOVE_TASK;
            if (duration < 0 || timed != (duration > 0) || needsTask != (task != null)
                || ((kind == Kind.COMPLETE_TASK || kind == Kind.REMOVE_TASK) && side == Side.BOTH))
                throw new IllegalArgumentException("Invalid event effect");
        }
    }
    public record Option(String label, List<Effect> effects) {
        public Option { if (label == null || label.isBlank()) throw new IllegalArgumentException("Missing option label"); effects = List.copyOf(effects); }
    }
    public CampaignEvent {
        if (id == null || id.isBlank() || title == null || description == null || category == null)
            throw new IllegalArgumentException("Invalid event");
        effects = List.copyOf(effects); options = List.copyOf(options);
        if (!effects.isEmpty() && !options.isEmpty()) throw new IllegalArgumentException("Use effects or choices, not both");
    }
    public List<Effect> allEffects() {
        return options.isEmpty() ? effects : options.stream().flatMap(o -> o.effects().stream()).toList();
    }
}
