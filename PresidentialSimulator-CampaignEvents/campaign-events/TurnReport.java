import java.util.List;
/** Command outcome. Rejections never advance time, spend resources, or draw randomness. */
public record TurnReport(boolean accepted, List<String> messages, GameView view) {
    public TurnReport { messages = List.copyOf(messages); }
}
