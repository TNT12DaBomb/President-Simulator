/** Saved gameplay preferences, not candidate attributes. */
public record DebateSettings(Mode mode, Pace pace, int textSize, boolean celebrations) {
    public enum Mode { POLITICS, FUN }
    public enum Pace { STANDARD, RELAXED, UNTIMED }
    public static final DebateSettings DEFAULT=new DebateSettings(Mode.POLITICS,Pace.STANDARD,18,true);
    public DebateSettings {if(mode==null||pace==null||textSize<16||textSize>24)throw new IllegalArgumentException("Invalid debate settings");}
    public int seconds(LiveDebate.Stage stage){if(pace==Pace.UNTIMED)return 0;int s=switch(stage){case ANSWER->30;case CONFIDENCE->15;case CHALLENGE->5;case RECOVERY->20;case POLICY,RECORD->40;default->0;};return pace==Pace.RELAXED?s*2:s;}
}
