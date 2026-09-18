import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.io.File;
public class NewsUITests {
 static void layout(Container c){c.doLayout();for(var x:c.getComponents())if(x instanceof Container n)layout(n);}
 static JLabel label(Container c,String text){for(var x:c.getComponents()){if(x instanceof JLabel l&&(text.equals(l.getName())||text.equals(l.getText())))return l;if(x instanceof Container n){var l=label(n,text);if(l!=null)return l;}}return null;}
 public static void main(String[] args)throws Exception{SwingUtilities.invokeAndWait(()->{try{var c=new DesktopController(()->0L);var p=new DesktopPanel(c);c.start(44,President.Difficulty.NORMAL,President.RunningMate.FUNDRAISER);p.setSize(1024,650);layout(p);var n=label(p,"News headline");if(n==null||label(p,"$1,600")==null)throw new AssertionError("news and currency");int y=SwingUtilities.convertPoint(n,0,0,p).y;p.render();layout(p);if(y!=SwingUtilities.convertPoint(n,0,0,p).y)throw new AssertionError("news movement");var img=new BufferedImage(1024,650,BufferedImage.TYPE_INT_RGB);var g=img.createGraphics();p.printAll(g);g.dispose();ImageIO.write(img,"png",new File(System.getProperty("java.io.tmpdir"),"presidential-simulator-news-qa.png"));System.out.println("PASS: currency labels, fixed headline position and laptop render.");}catch(Exception e){throw new RuntimeException(e);}});}
}
