import java.util.List;

/** Immutable snapshot for any career UI; terms and money are counts/resources, not ratings. */
public record CareerView(Phase phase, int year, int cycle, int electionsWon, int electionsLost,
                         int completedTerms, int termQuarters, int servedQuarters, int comebackYears,
                         int treasury, int quarterlyReceipts, int routineExpenses, String publicFeedback,
                         String reputation, String support, String economicConditions,
                         List<String> transitionResources, List<String> annualWork, List<String> nextCampaignEffects,
                         List<GovernanceOption> governanceOptions, List<ElectionSummary> elections,
                         List<String> history, GameView campaign, boolean developerUsed) {
    public enum Phase { CAMPAIGN, ELECTION_REVIEW, TRANSITION, PRESIDENCY, TERM_REVIEW, OPPOSITION, RETIRED }
    public record GovernanceOption(CareerCommand.GovernanceAction action, int cost, boolean available, String reason) { }
    public record ElectionSummary(int cycle, int year, String outcome, boolean forced, Integer playerEV,
                                  Integer opponentEV, List<String> consequences) {
        public ElectionSummary { consequences = List.copyOf(consequences); }
    }
    public CareerView {
        transitionResources = List.copyOf(transitionResources); annualWork = List.copyOf(annualWork);
        nextCampaignEffects = List.copyOf(nextCampaignEffects); governanceOptions = List.copyOf(governanceOptions);
        elections = List.copyOf(elections); history = List.copyOf(history);
    }
}
