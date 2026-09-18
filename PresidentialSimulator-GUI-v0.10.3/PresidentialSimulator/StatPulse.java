import java.awt.Color;
/** Time-based tint; rerendering must never restart an expired pulse. */
public final class StatPulse {
 private StatPulse(){}
 public static final Color BASE=new Color(43,53,45),HIGHLIGHT=new Color(96,86,57);
 public static final long DURATION=1_600_000_000L;
 public static Color color(long elapsed){double t=Math.max(0,Math.min(1,elapsed/(double)DURATION));return new Color((int)(HIGHLIGHT.getRed()+(BASE.getRed()-HIGHLIGHT.getRed())*t),(int)(HIGHLIGHT.getGreen()+(BASE.getGreen()-HIGHLIGHT.getGreen())*t),(int)(HIGHLIGHT.getBlue()+(BASE.getBlue()-HIGHLIGHT.getBlue())*t));}
}
