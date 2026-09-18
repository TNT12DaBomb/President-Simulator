/** Serializable, renderer-independent presidency and campaign-pledge input. */
public record OfficeCommand(Type type, Policy.Issue issue, Policy.Approach approach, int choice) {
    public enum Type { END_MONTH, PROPOSE, SIGN, VETO, NEGOTIATE, EVENT_CHOICE, PLEDGE, EVENT_FREQUENCY, REPLY, MIDTERM_TASK, COMMITTEE_REVIEW, FLOOR_VOTE, WITHDRAW_BILL, NOMINATE, CONFIRM, NOMINEE_SUPPORT, DISMISS, DELEGATE, DEV_APPROVAL, DEV_CAPITAL, DEV_WORLD_EVENT, CLOCK_TICK, REALTIME_MODE, DEBATE_MODE, DEBATE_PACE, DEBATE_TEXT, DEBATE_CELEBRATIONS, LEAVE_LIVE, CAMPAIGN_COLOR }
    public static OfficeCommand simple(Type type) { return new OfficeCommand(type, null, null, 0); }
    public static OfficeCommand policy(Type type, Policy.Issue issue, Policy.Approach approach) { return new OfficeCommand(type, issue, approach, 0); }
    public static OfficeCommand choice(Type type, int choice) { return new OfficeCommand(type, null, null, choice); }
    public boolean valid() {
        if (type == null) return false;
        boolean policy = type == Type.PROPOSE || type == Type.PLEDGE;
        if ((issue != null) != policy || (approach != null) != policy) return false;
        return switch (type) {
            case CAMPAIGN_COLOR -> choice>=0&&choice<5;
            case CLOCK_TICK -> choice >= 1 && choice <= 3600;
            case DEBATE_MODE, DEBATE_CELEBRATIONS -> choice==0||choice==1;
            case DEBATE_PACE -> choice>=0&&choice<=2;
            case DEBATE_TEXT -> choice>=16&&choice<=24;
            case REALTIME_MODE -> choice == 0 || choice == 1;
            case DEV_APPROVAL, DEV_CAPITAL -> choice >= 0 && choice <= 100;
            case DEV_WORLD_EVENT -> choice >= 0 && choice < WorldStories.all().size();
            case NOMINATE -> choice >= 0 && choice < 12;
            case CONFIRM, NOMINEE_SUPPORT, DISMISS, DELEGATE -> choice >= 0 && choice < 6;
            case REPLY -> choice >= 0 && choice < 16;
            case MIDTERM_TASK -> choice >= 0 && choice < 24;
            case EVENT_CHOICE -> choice >= 0 && choice <= 1;
            case EVENT_FREQUENCY -> choice >= 0 && choice <= 2;
            default -> choice == 0;
        };
    }
}
