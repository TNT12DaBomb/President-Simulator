/** Authored policy dilemmas. Costs are game operating resources, not national budget estimates. */
public final class PolicyCatalog {
    private PolicyCatalog() { }
    public record Option(String title, int cost, int deliveryMonths, String benefit, String tradeoff, String group) { }
    private static final String[][] CONTENT = {
        {"Open regional apprenticeship desks", "Centralize business permit processing", "Job-seeker association", "Small-business filing cooperative", "Local access to training applications", "A single permit application queue", "More offices require staff and operating funds", "Applicants lose the option of a local permit desk"},
        {"Extend tax-help office hours", "Consolidate tax forms into one portal", "Taxpayer assistance network", "Independent preparer association", "More staffed filing appointments", "Fewer duplicate forms to maintain", "Extended hours require additional staffing", "Paper filers need help with the new portal"},
        {"Add community clinic sessions", "Unify clinic referral scheduling", "Community patient council", "Clinic referral coordinators", "More appointment sessions", "One referral queue across participating clinics", "Additional sessions use more operating funds", "Existing appointments need rescheduling during transition"},
        {"Expand application assistance appointments", "Reorganize case tracking", "Applicant assistance council", "Casework staff association", "More staffed application help", "A shared case-tracking workflow", "New appointments increase office workloads", "Staff must learn the replacement workflow"},
        {"Expand service-family support desks", "Consolidate equipment maintenance scheduling", "Service-family council", "Maintenance workforce council", "More family assistance appointments", "A common maintenance timetable", "The additional desks need funding", "Units lose local scheduling flexibility"},
        {"Add local water-monitoring stations", "Reorganize environmental reporting", "Watershed volunteer network", "Municipal reporting offices", "More sampling locations", "One reporting format for participating offices", "New stations add operating work", "Local offices must convert existing records"},
        {"Expand after-school tutoring sessions", "Pool school purchasing services", "Family learning council", "School operations council", "More scheduled tutoring places", "A shared purchasing desk", "Extra sessions require staffing", "Schools give up some purchasing flexibility"},
        {"Open accessibility assistance desks", "Unify complaint case tracking", "Accessibility advocates forum", "Public complaint reviewers", "More help navigating accessibility requests", "A shared complaint tracking system", "Extra desks need ongoing administration", "Open cases must be migrated carefully"}
    };
    public static Option option(Policy.Issue issue, Policy.Approach approach) {
        String[] c = CONTENT[issue.ordinal()]; boolean expand = approach == Policy.Approach.EXPAND_PROGRAM;
        return new Option(c[expand ? 0 : 1], expand ? 180 : 100, expand ? 3 : 2,
            c[expand ? 4 : 5], c[expand ? 6 : 7], c[expand ? 2 : 3]);
    }
}
