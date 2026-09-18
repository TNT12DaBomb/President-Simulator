import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.imageio.ImageIO;
import javax.swing.*;
public final class RefinedTests {
    static int checks;
    static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
    static void layout(DesktopPanel p){p.setSize(1024,600);for(int i=0;i<4;i++)DesktopTests.layout(p);}
    static void stacks(Container root){if(root.getLayout() instanceof SheetLayout){int previous=root.getInsets().top;for(Component c:root.getComponents())if(c.isVisible()){check(c.getY()>=previous,"controls do not overlap");check(c.getX()+c.getWidth()<=root.getWidth(),"controls fit parent width");previous=c.getY()+c.getHeight();}}for(Component c:root.getComponents())if(c instanceof Container p)stacks(p);}
    public static void main(String[]args)throws Exception{SwingUtilities.invokeAndWait(()->{try{
        DesktopController c=new DesktopController();DesktopPanel p=new DesktopPanel(c);c.start(42,President.Difficulty.NORMAL,President.RunningMate.FUNDRAISER);DesktopTests.find(p,"Map").doClick();layout(p);Component work=MapDesktopTests.named(p,"Main workspace");Rectangle before=new Rectangle(work.getBounds());
        p.notice(new DesktopController.Notice("Test notice","Long notification text. ".repeat(60),true));layout(p);check(before.equals(work.getBounds()),"notification never moves workspace");DesktopTests.find(p,"Dismiss").doClick();layout(p);check(before.equals(work.getBounds()),"dismiss never moves workspace");
        ElectoralMap map=(ElectoralMap)MapDesktopTests.named(p,"Electoral map");BufferedImage temp=new BufferedImage(map.getWidth(),map.getHeight(),BufferedImage.TYPE_INT_RGB);Graphics2D g=temp.createGraphics();map.paint(g);g.dispose();
        for(String state:new String[]{"Maine","Vermont","New Hampshire","Massachusetts","Rhode Island","Connecticut","New Jersey","Delaware","Maryland","District of Columbia"}){Point at=map.geographicTarget(state);check(at!=null,"geographic hover target exists: "+state);map.dispatchEvent(new MouseEvent(map,MouseEvent.MOUSE_MOVED,0,0,at.x,at.y,0,false));JLabel info=(JLabel)MapDesktopTests.named(p,"State hover preview");check(info.getText().contains(state),"visible hover panel updates: "+state);Point callout=map.target(state);map.dispatchEvent(new MouseEvent(map,MouseEvent.MOUSE_MOVED,0,0,callout.x,callout.y,0,false));check(info.getText().contains(state),"callout updates: "+state);}
        MapDesktopTests.shot(p,1024,600,"GUI-HOVER-DETAILS.png");
        c.start(8,President.Difficulty.NORMAL,President.RunningMate.FUNDRAISER);c.submit(CareerCommand.dev(CareerCommand.DeveloperAction.FORCE_WIN));c.submit(CareerCommand.advance());c.submit(CareerCommand.advance());DesktopTests.find(p,"Office").doClick();layout(p);stacks(p);MapDesktopTests.shot(p,1024,600,"GUI-OFFICE-REFINED.png");
        DesktopTests.find(p,"Policy").doClick();layout(p);stacks(p);check(PolishTests.findTabs(p).getTabCount()==3,"policy separated into three tabs");MapDesktopTests.shot(p,1024,600,"GUI-POLICY-REFINED.png");JComboBox<?> box=findCombo(p);box.setSelectedIndex(3);layout(p);stacks(p);DesktopTests.find(p,"Office").doClick();DesktopTests.find(p,"Policy").doClick();check(findCombo(p).getSelectedIndex()==3,"policy selection survives navigation");
        var original=c.view();PracticeDebatePanel practice=new PracticeDebatePanel(42,new DebateSettings(DebateSettings.Mode.FUN,DebateSettings.Pace.UNTIMED,20,true));practice.setSize(800,500);for(int i=0;i<4;i++)DesktopTests.layout(practice);DesktopTests.find(practice,"Practice option 1").doClick();for(int i=0;i<4;i++)DesktopTests.layout(practice);BufferedImage image=new BufferedImage(800,500,BufferedImage.TYPE_INT_RGB);Graphics2D fg=image.createGraphics();practice.paint(fg);fg.dispose();ImageIO.write(image,"png",Path.of("GUI-TEAM-PRACTICE.png").toFile());
        int steps=0;while(DesktopTests.find(practice,"Practice option 0")!=null){check(++steps<40,"full practice terminates");DesktopTests.find(practice,"Practice option 0").doClick();}check(original.equals(c.view()),"practice has no campaign or presidency effects");

    }catch(Exception e){throw new RuntimeException(e);}});System.out.println("RefinedTests: "+checks+" checks passed");}
    static JComboBox<?> findCombo(Container p){for(Component c:p.getComponents()){if(c instanceof JComboBox<?> b)return b;if(c instanceof Container child){var found=findCombo(child);if(found!=null)return found;}}return null;}
}
