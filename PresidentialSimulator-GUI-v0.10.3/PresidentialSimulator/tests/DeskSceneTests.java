import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;
public class DeskSceneTests {
 static void check(boolean b,String reason){if(!b)throw new AssertionError(reason);}
 public static void main(String[] args)throws Exception{SwingUtilities.invokeAndWait(()->{try{
  for(Dimension size:new Dimension[]{new Dimension(620,385),new Dimension(450,340)}){
   var selected=new ArrayList<DeskScene.Item>();var desk=new DeskScene(new DeskScene.State(true,true,2,5,2,8),selected::add);desk.setSize(size);desk.doLayout();check(desk.getComponentCount()==12,"twelve objects");var rects=new ArrayList<Rectangle>();
   for(Component c:desk.getComponents()){check(c instanceof JButton,"keyboard button");JButton b=(JButton)c;check(b.isFocusable(),"focusable");check(b.getAccessibleContext().getAccessibleName()!=null,"accessible name");check(new Rectangle(0,0,size.width,size.height).contains(b.getBounds()),"in bounds");for(Rectangle r:rects)check(!r.intersects(b.getBounds()),"overlap");rects.add(b.getBounds());b.doClick();}
   check(new HashSet<>(selected).size()==12,"each callback distinct");desk.motion(false);desk.select(DeskScene.Item.SECURE_PHONE);
   if(size.width==620){BufferedImage image=new BufferedImage(size.width,size.height,BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();desk.printAll(g);g.dispose();ImageIO.write(image,"png",Path.of("docs","desk-preview.png").toFile());}
  }
  System.out.println("PASS: 12 keyboard-accessible objects, distinct callbacks, no overlap at two laptop pane sizes; preview rendered.");
 }catch(Exception e){throw new RuntimeException(e);}});}
}
