import java.util.List;
import java.util.Objects;

/** Concrete inherited campaign resources, never a candidate rating. */
public record CampaignOpening(int cashAdjustment, List<Work> completedWork, List<String> reasons) {
    public record Work(String state, State.Task task) {
        public Work { Objects.requireNonNull(state); Objects.requireNonNull(task); }
    }
    public CampaignOpening {
        if (cashAdjustment < -500 || cashAdjustment > 1000) throw new IllegalArgumentException("Opening cash adjustment out of bounds");
        completedWork = List.copyOf(completedWork); reasons = List.copyOf(reasons);
    }
    public static CampaignOpening fresh() { return new CampaignOpening(0, List.of(), List.of()); }
}
