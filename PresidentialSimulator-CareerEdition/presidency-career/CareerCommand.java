/** Typed mutations for the career controller; renderers only construct commands. */
public record CareerCommand(Type type, GameCommand campaign, GovernanceAction governance,
                            RebuildAction rebuild, DeveloperAction developer) {
    public enum Type { CAMPAIGN, CONTINUE, GOVERN, REBUILD, RUN_AGAIN, RETIRE, DEVELOPER }
    public enum GovernanceAction {
        PUBLIC_BRIEFING("Hold a public briefing", 50), CABINET_MEETING("Coordinate the cabinet", 75),
        BUDGET_REVIEW("Reconcile the annual budget", 25), SERVICE_REVIEW("Fund a service-delivery review", 150),
        ROUTINE_QUARTER("Continue routine administration", 0);
        private final String label;
        private final int cost;
        GovernanceAction(String label, int cost) { this.label = label; this.cost = cost; }
        public int cost() { return cost; }
        @Override public String toString() { return label; }
    }
    public enum RebuildAction {
        CAMPAIGN_REVIEW("Review the election and publish lessons"), DONOR_MEETINGS("Meet donors and review finances"),
        COMMUNITY_WORK("Reconnect with community volunteers"), PRIVATE_LIFE("Spend a year outside public life");
        private final String label;
        RebuildAction(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }
    public enum DeveloperAction {
        FORCE_WIN("Force a win in the current campaign"), FORCE_LOSS("Force a loss in the current campaign"),
        MID_FIRST_TERM("Replace timeline: start year 3 of the first term"),
        REELECTION_START("Replace timeline: start the reelection campaign"),
        MID_SECOND_TERM("Replace timeline: start year 3 of the second term"),
        FINAL_QUARTER("Replace timeline: start the final quarter of term two"),
        ADD_CAMPAIGN_CASH("Add $500 campaign cash"), ADD_TREASURY("Add $500 to the treasury"),
        FINISH_TERM("Run routine quarters to the end of this term");
        private final String label;
        DeveloperAction(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }
    public static CareerCommand campaign(GameCommand command) { return new CareerCommand(Type.CAMPAIGN, command, null, null, null); }
    public static CareerCommand advance() { return new CareerCommand(Type.CONTINUE, null, null, null, null); }
    public static CareerCommand govern(GovernanceAction action) { return new CareerCommand(Type.GOVERN, null, action, null, null); }
    public static CareerCommand rebuild(RebuildAction action) { return new CareerCommand(Type.REBUILD, null, null, action, null); }
    public static CareerCommand runAgain() { return new CareerCommand(Type.RUN_AGAIN, null, null, null, null); }
    public static CareerCommand retire() { return new CareerCommand(Type.RETIRE, null, null, null, null); }
    public static CareerCommand dev(DeveloperAction action) { return new CareerCommand(Type.DEVELOPER, null, null, null, action); }
}
