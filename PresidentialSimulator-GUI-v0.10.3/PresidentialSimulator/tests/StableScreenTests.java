import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
/** Small VT screen model: detects scrolling and input-cursor movement, not just timer text. */
public final class StableScreenTests {
 static int checks;
 static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 static final class VT {
  final char[][] cells;int x,y,sx,sy,scrolls;boolean wrap=true;
  VT(int width,int height){cells=new char[height][width];clear();}
  void clear(){for(char[] r:cells)Arrays.fill(r,' ');}
  String row(int n){return new String(cells[n]);}
  void feed(String s){for(int i=0;i<s.length();i++){char c=s.charAt(i);
   if(c==27){char command=s.charAt(++i);if(command=='7'){sx=x;sy=y;}else if(command=='8'){x=sx;y=sy;}else if(command=='['){int start=++i;while(i<s.length()&&(s.charAt(i)<'@'||s.charAt(i)>'~'))i++;String a=s.substring(start,i);char op=s.charAt(i);
    if(op=='H'){String[] xy=a.split(";");y=xy[0].isEmpty()?0:Integer.parseInt(xy[0])-1;x=xy.length<2?0:Integer.parseInt(xy[1])-1;check(y>=0&&y<cells.length&&x>=0&&x<cells[0].length,"Cursor within viewport");}
    else if(op=='J'&&a.equals("2"))clear();else if(op=='K'&&a.equals("2"))Arrays.fill(cells[y],' ');
    else if((op=='h'||op=='l')&&a.equals("?7"))wrap=op=='h';
   }}else if(c=='\r')x=0;else if(c=='\n'){if(++y>=cells.length){y=cells.length-1;scrolls++;}}else{cells[y][x]=c;if(x+1<cells[0].length)x++;else if(wrap){x=0;if(++y>=cells.length){y=cells.length-1;scrolls++;}}}
  }}
 }
 public static void main(String[]args){
  for(int[] size:new int[][]{{60,20},{80,24},{100,28},{120,40}}){
   var bytes=new ByteArrayOutputStream();var screen=new TerminalScreen(new PrintStream(bytes,true,StandardCharsets.UTF_8),new TerminalScreen.Options(size[0],size[1],true,true));var vt=new VT(size[0],size[1]);
   screen.start();screen.begin("A readable debate question","Campaign | Turn 4 | Cash $1400");screen.content().println("Choose a clear answer without moving the screen.");screen.statsAvailable(true);screen.timer(60);screen.render(0,"Answer 1/2 | 0 menu");String first=bytes.toString(StandardCharsets.UTF_8);check(!first.contains("\n")&&!first.contains("\r"),"No full-frame linefeeds");vt.feed(first);vt.feed("1");int x=vt.x,y=vt.y;String input=vt.row(y);String[] before=new String[size[1]];for(int row=0;row<size[1];row++)before[row]=vt.row(row);
   bytes.reset();for(int sec=59;sec>=1;sec--)screen.countdown(sec);String ticks=bytes.toString(StandardCharsets.UTF_8);check(!ticks.contains("\n")&&!ticks.contains("\r")&&!ticks.contains("[2J"),"Timer never appends rows or clears frame");vt.feed(ticks);check(vt.x==x&&vt.y==y&&vt.row(y).equals(input),"Typing and cursor survive countdown");int changed=0;for(int row=0;row<size[1];row++)if(!before[row].equals(vt.row(row)))changed++;check(changed==1,"Only timer row changes");check(vt.scrolls==0,"Countdown never scrolls viewport");
   vt.feed("\r\n");bytes.reset();screen.render(0,"Answer 1/2 | 0 menu");String redraw=bytes.toString(StandardCharsets.UTF_8);check(!redraw.contains("[2J"),"No repeated full-screen clears");vt.feed(redraw);check(vt.scrolls==0,"Enter and redraw stay inside reserved space");
   bytes.reset();screen.close();vt.feed(bytes.toString(StandardCharsets.UTF_8));check(vt.wrap,"Wrapping restored on exit");
  }
  var out=new ByteArrayOutputStream();var plain=new TerminalScreen(new PrintStream(out),new TerminalScreen.Options(80,24,false,false));plain.timer(60);for(int i=59;i>=0;i--)plain.countdown(i);check(out.size()==0,"Transcript mode does not spam live tick lines");
  System.out.println("StableScreenTests: "+checks+" checks passed.");
 }
}
