import java.util.ArrayList;
import java.util.List;

/** Persistent public requests. A reply acknowledges a concern; only delivery fulfills it. */
public final class PublicCorrespondence {
    public enum Status { OPEN, ACKNOWLEDGED, DELIVERY_SCHEDULED, DELIVERED }
    public record Request(int id, String group, Policy.Issue issue, Policy.Approach requested,
                          String request, Status status, int updatedMonth, boolean answered) { }
    public record Update(int month, int requestId, Status status, String message) { }
    public record View(List<Request> requests, List<Update> history) {
        public View { requests = List.copyOf(requests); history = List.copyOf(history); }
    }
    private final List<Request> requests = new ArrayList<>();
    private final List<Update> history = new ArrayList<>();
    public PublicCorrespondence() {
        for (Policy.Issue issue : Policy.Issue.values()) for (Policy.Approach approach : Policy.Approach.values()) {
            PolicyCatalog.Option o = PolicyCatalog.option(issue, approach);
            requests.add(new Request(requests.size(), o.group(), issue, approach, o.title(), Status.OPEN, 0, false));
        }
    }
    public View view() { return new View(requests, history); }
    public String replyProblem(int id) { return requests.get(id).answered() ? "This request already has a published reply." : ""; }
    public String reply(int id, int month) {
        Request r = requests.get(id);
        String message = "Published reply to " + r.group() + ": request acknowledged; no service delivery claimed.";
        update(id, r.status() == Status.OPEN ? Status.ACKNOWLEDGED : r.status(), month, true, message);
        return message;
    }
    public List<String> schedule(Policy.Issue issue, Policy.Approach approach, int month) {
        List<String> messages = new ArrayList<>();
        for (Request r : List.copyOf(requests)) if (r.issue() == issue) {
            boolean selected = r.requested() == approach;
            String message = r.group() + (selected ? ": requested initiative funded; awaiting delivery." : ": its alternative request remains unresolved under the selected policy.");
            update(r.id(), selected ? Status.DELIVERY_SCHEDULED : Status.OPEN, month, r.answered(), message);
            messages.add(message);
        }
        return messages;
    }
    public String deliver(Policy.Issue issue, Policy.Approach approach, int month) {
        int id = issue.ordinal() * 2 + approach.ordinal(); Request r = requests.get(id);
        String message = r.group() + ": " + r.request() + " delivered. The competing request remains visible.";
        update(id, Status.DELIVERED, month, r.answered(), message); return message;
    }
    private void update(int id, Status status, int month, boolean answered, String message) {
        Request r = requests.get(id);
        requests.set(id, new Request(r.id(), r.group(), r.issue(), r.requested(), r.request(), status, month, answered));
        history.add(new Update(month, id, status, message));
    }
}
