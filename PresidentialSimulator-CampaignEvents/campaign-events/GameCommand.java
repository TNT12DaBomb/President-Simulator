/** Commands are the only public mutation API. UI code never changes model fields. */
public record GameCommand(Type type, String state, State.Task task, int choice) {
    public enum Type { CAMPAIGN, FUNDRAISE, REST, RESPOND }
    public static GameCommand campaign(String state, State.Task task) { return new GameCommand(Type.CAMPAIGN, state, task, -1); }
    public static GameCommand fundraise() { return new GameCommand(Type.FUNDRAISE, null, null, -1); }
    public static GameCommand rest() { return new GameCommand(Type.REST, null, null, -1); }
    public static GameCommand respond(int index) { return new GameCommand(Type.RESPOND, null, null, index); }
}
