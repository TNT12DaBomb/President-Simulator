import java.util.List;

/** Data-only event definitions with prerequisites, weighted selection, choices and delayed effects. */
public record PresidencyEvent(String id, String title, String description, int weight, int earliestMonth,
                              boolean requiresLaw, List<Choice> choices) {
    public record Effect(int treasury, String message) { }
    public record Choice(String label, int cost, Effect immediate, int delayMonths, Effect delayed) { }
    public PresidencyEvent { choices = List.copyOf(choices); }
    public static List<PresidencyEvent> catalog() {
        return List.of(
            event("records", "Records backlog", "The correspondence office requests temporary help.", 1, false,
                "Hire temporary clerks", 60, 2, 0, "The correspondence backlog has been cleared."),
            event("supplier", "Supplier correction", "A supplier offers a documented refund after an invoice review.", 2, false,
                "Review the invoice", 20, 1, 80, "A verified supplier refund arrives: +$80."),
            event("delivery", "Implementation review", "A signed initiative is ready for a service-delivery review.", 3, true,
                "Commission the review", 70, 3, 0, "The implementation report is published; questions are recorded."),
            event("maintenance", "Building maintenance", "An office needs repairs. An alternative workspace is available.", 1, false,
                "Repair the workspace", 90, 2, 0, "Repairs are complete; normal service resumes."),
            event("archive", "Archive request", "Archivists request help publishing administrative records.", 4, false,
                "Fund digitization", 40, 1, 0, "The public archive receives a new collection."),
            event("training", "Staff training", "An agency offers a joint training session.", 2, false,
                "Arrange joint training", 50, 2, 25, "Shared training costs returned $25 to the operating account.")
        );
    }
    private static PresidencyEvent event(String id, String title, String description, int month, boolean law,
                                          String choice, int cost, int delay, int refund, String report) {
        return new PresidencyEvent(id, title, description, 1, month, law, List.of(
            new Choice(choice, cost, new Effect(0, choice + " authorized."), delay, new Effect(refund, report)),
            new Choice("Use existing resources; record the outstanding request", 0,
                new Effect(0, title + ": request retained in the public correspondence record."), 0, null)));
    }
}
