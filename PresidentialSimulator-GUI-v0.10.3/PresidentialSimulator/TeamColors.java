import java.awt.Color;
import java.util.*;
/** UI palette only: no real-party affiliation or simulation effect. */
public enum TeamColors {
 BLUE(0x73B2FF,0x306CB0), CORAL(0xFF9E92,0xA34840), TEAL(0x62DBC5,0x24776D), VIOLET(0xC5AAFF,0x72529B), ROSE(0xF6A7D1,0x994D77);
 public final Color accent,fill;
 TeamColors(int accent,int fill){this.accent=new Color(accent);this.fill=new Color(fill);}
 public static TeamColors opponent(long seed,int player){var choices=new ArrayList<TeamColors>();for(var c:values())if(c.ordinal()!=player&&distance(values()[player].accent,c.accent)>110)choices.add(c);return choices.get(new Random(seed^0xC010A5L).nextInt(choices.size()));}
 private static double distance(Color a,Color b){return Math.sqrt(Math.pow(a.getRed()-b.getRed(),2)+Math.pow(a.getGreen()-b.getGreen(),2)+Math.pow(a.getBlue()-b.getBlue(),2));}
}
