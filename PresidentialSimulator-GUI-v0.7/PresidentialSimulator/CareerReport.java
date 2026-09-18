import java.util.List;
/** Accepted/rejected career command with the exact messages and new immutable view. */
public record CareerReport(boolean accepted, List<String> messages, CareerView view) {
    public CareerReport { messages = List.copyOf(messages); }
}
