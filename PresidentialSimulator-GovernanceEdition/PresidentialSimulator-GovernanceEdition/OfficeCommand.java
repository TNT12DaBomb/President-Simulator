/** Serializable, renderer-independent presidency and campaign-pledge input. */
public record OfficeCommand(Type type, Policy.Issue issue, Policy.Approach approach, int choice) {
    public enum Type { END_MONTH, PROPOSE, SIGN, VETO, NEGOTIATE, EVENT_CHOICE, PLEDGE, EVENT_FREQUENCY, REPLY, MIDTERM_TASK }
    public static OfficeCommand simple(Type type) { return new OfficeCommand(type, null, null, 0); }
    public static OfficeCommand policy(Type type, Policy.Issue issue, Policy.Approach approach) { return new OfficeCommand(type, issue, approach, 0); }
    public static OfficeCommand choice(Type type, int choice) { return new OfficeCommand(type, null, null, choice); }
    public boolean valid() {
        if (type == null) return false;
        boolean policy = type == Type.PROPOSE || type == Type.PLEDGE;
        if ((issue != null) != policy || (approach != null) != policy) return false;
        return switch (type) {
            case REPLY -> choice >= 0 && choice < 16;
            case MIDTERM_TASK -> choice >= 0 && choice < 24;
            case EVENT_CHOICE -> choice >= 0 && choice <= 1;
            case EVENT_FREQUENCY -> choice >= 0 && choice <= 2;
            default -> choice == 0;
        };
    }
}
