import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;
public final class MapDesktopTests {
    static int checks;
    static void check(boolean b,String message){checks++;if(!b)throw new AssertionError(message);}
    static Component named(Container root,String name){for(Component c:root.getComponents()){if(name.equals(c.getName()))return c;if(c instanceof Container child){Component found=named(child,name);if(found!=null)return found;}}return null;}
    static void shot(DesktopPanel p,int w,int h,String file)throws Exception{p.setSize(w,h);for(int i=0;i<3;i++)DesktopTests.layout(p);BufferedImage image=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);p.printAll(g);g.dispose();ImageIO.write(image,"png",Path.of(file).toFile());}
    public static void main(String[] args)throws Exception{
        SwingUtilities.invokeAndWait(()->{try{
            DesktopController c=new DesktopController();DesktopPanel p=new DesktopPanel(c);c.start(42,President.Difficulty.NORMAL,President.RunningMate.COMMUNITY_ORGANIZER);DesktopTests.find(p,"Map").doClick();
            shot(p,1280,680,"GUI-MAP-LAPTOP.png");ElectoralMap map=(ElectoralMap)named(p,"Electoral map");check(map!=null,"map replaces list");
            for(String item:new String[]{"Dashboard","Map","Decisions","Schedule","Policy","Statistics","History","Menu","How to play"}){JButton button=DesktopTests.find(p,item);check(button!=null,"navigation includes "+item);Rectangle bounds=SwingUtilities.convertRectangle(button.getParent(),button.getBounds(),p);check(bounds.y>=0&&bounds.y+bounds.height<=p.getHeight(),"navigation fits: "+item);Container parent=button.getParent();check(button.getY()+button.getHeight()<=parent.getHeight(),"navigation not clipped: "+item);}check(map.mappedStates().size()==51,"50 states plus DC");
            for(String name:map.mappedStates()){Point hit=map.target(name);check(hit!=null,name+" has hit area");check(name.equals(map.stateAt(hit)),name+" is selectable");check(map.getToolTipText(new MouseEvent(map,MouseEvent.MOUSE_MOVED,0,0,hit.x,hit.y,0,false)).contains(name),"hover describes "+name);}
            Point texas=map.target("Texas");map.dispatchEvent(new MouseEvent(map,MouseEvent.MOUSE_CLICKED,0,0,texas.x,texas.y,1,false));check(p.selectedState().equals("Texas"),"click updates inspector");
            JButton action=null;for(State.Task task:State.Task.values()){JButton b=(JButton)named(p,"State action "+task.name());if(b!=null&&b.isEnabled()){action=b;break;}}check(action!=null,"selected state has action");int turn=c.view().campaign().turnsUsed();action.doClick();check(c.view().campaign().turnsUsed()==turn+1,"map action reaches engine");check(p.selectedState().equals("Texas"),"selection survives refresh");
            c.submit(CareerCommand.office(OfficeCommand.simple(OfficeCommand.Type.SIGN)));check(p.notificationTitle().equals("Action unavailable"),"rejection visible automatically");shot(p,1024,600,"GUI-MAP-COMPACT.png");
            DesktopTests.find(p,"Dismiss").doClick();int guard=0;while(c.view().campaign().turnsUsed()<4&&guard++<30){if(!c.decisionKey().isEmpty())c.respond(c.decisionKey(),1);else c.submit(CareerCommand.campaign(GameCommand.rest()));}check(p.currentPage().equals("Decisions"),"new debate auto-opens");shot(p,1024,600,"GUI-DECISION-LAPTOP.png");check(c.view().campaign().pendingEvent()!=null,"decision renderer uses a real pending event");
            shot(p,800,480,"GUI-SMALL-WINDOW.png");int seconds=c.seconds();c.pulse();check(c.seconds()==seconds,"rendering does not spend response time");
            Rectangle usable=DesktopLauncher.usableBounds(new Rectangle(100,50,1093,614),new Insets(0,0,40,0));check(usable.equals(new Rectangle(100,50,1093,574)),"taskbar and scaled desktop bounds");
            // Seed contract: repeatability of objectives, not random initial ownership.
            CareerEngine a=new CareerEngine(123,President.Difficulty.NORMAL,President.RunningMate.FUNDRAISER),b=new CareerEngine(123,President.Difficulty.NORMAL,President.RunningMate.FUNDRAISER),d=new CareerEngine(456,President.Difficulty.NORMAL,President.RunningMate.FUNDRAISER);
            check(a.view().campaign().equals(b.view().campaign()),"same seed repeats initial snapshot");check(!a.view().campaign().states().stream().map(GameView.StateView::objectives).toList().equals(d.view().campaign().states().stream().map(GameView.StateView::objectives).toList()),"different seeds vary objectives");
            for(GameCommand command:java.util.List.of(GameCommand.rest(),GameCommand.fundraise())){a.submit(CareerCommand.campaign(command));b.submit(CareerCommand.campaign(command));}check(a.view().equals(b.view()),"same seed and choices repeat progression");
        }catch(Exception e){throw new RuntimeException(e);}});
        System.out.println("MapDesktopTests: "+checks+" checks passed");
    }
}
