import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicLong;
import javax.imageio.ImageIO;
import javax.swing.*;
public final class DesktopTests {
    static int checks;
    static void check(boolean value,String msg){checks++;if(!value)throw new AssertionError(msg);}
    static void layout(Container c){c.doLayout();for(Component x:c.getComponents())if(x instanceof Container child)layout(child);}
    static JButton find(Container c,String name){for(Component x:c.getComponents()){if(x instanceof JButton b&&name.equals(b.getName()))return b;if(x instanceof Container child){var b=find(child,name);if(b!=null)return b;}}return null;}
    static void screenshot(DesktopPanel p,String path)throws Exception{p.setSize(1320,850);layout(p);BufferedImage i=new BufferedImage(1320,850,BufferedImage.TYPE_INT_RGB);Graphics2D g=i.createGraphics();p.printAll(g);g.dispose();ImageIO.write(i,"png",Path.of(path).toFile());}
    public static void main(String[] args)throws Exception{
        SwingUtilities.invokeAndWait(()->{try{
            AtomicLong now=new AtomicLong();DesktopController c=new DesktopController(now::get);DesktopPanel p=new DesktopPanel(c);check(find(p,"Start a new career")!=null,"onboarding");c.start(42,President.Difficulty.NORMAL,President.RunningMate.COMMUNITY_ORGANIZER);
            int turn=c.view().campaign().turnsUsed();find(p,"Fundraise (+"+c.view().campaign().fundraisingAmount()+" campaign funds)").doClick();check(c.view().campaign().turnsUsed()==turn+1,"button submits real action");
            screenshot(p,"GUI-CAMPAIGN.png");
            int guard=0;while(c.view().campaign().turnsUsed()<4&&guard++<30){if(!c.decisionKey().isEmpty())c.respond(c.decisionKey(),1);else c.submit(CareerCommand.campaign(GameCommand.rest()));}
            c.respond(c.decisionKey(),1);check(c.seconds()>0,"debate has clock");String key=c.decisionKey();int before=c.seconds();c.pause();now.addAndGet(10_000_000_000L);c.pulse();check(c.seconds()==before,"dialog pause");c.resume();now.addAndGet(2_000_000_000L);c.pulse();check(c.seconds()==before-2,"live tick");screenshot(p,"GUI-DEBATE.png");now.addAndGet(120_000_000_000L);c.pulse();check(!key.equals(c.decisionKey()),"timeout advances round");String next=c.decisionKey();c.respond(key,0);check(next.equals(c.decisionKey()),"stale response rejected");
            guard=0;while(c.view().phase()==CareerView.Phase.CAMPAIGN&&guard++<100){if(!c.decisionKey().isEmpty())c.respond(c.decisionKey(),1);else c.submit(CareerCommand.campaign(GameCommand.rest()));}check(c.view().phase()==CareerView.Phase.ELECTION_REVIEW,"campaign reaches election");
            c.start(23,President.Difficulty.NORMAL,President.RunningMate.FUNDRAISER);c.submit(CareerCommand.dev(CareerCommand.DeveloperAction.FORCE_WIN));c.submit(CareerCommand.advance());c.submit(CareerCommand.advance());check(c.view().phase()==CareerView.Phase.PRESIDENCY,"inauguration");int months=c.view().termMonths();c.submit(CareerCommand.office(OfficeCommand.simple(OfficeCommand.Type.END_MONTH)));check(c.view().termMonths()==months+1,"monthly loop");screenshot(p,"GUI-OFFICE.png");
        }catch(Exception e){throw new RuntimeException(e);}});
        DesktopController c=new DesktopController();Path save=Files.createTempDirectory("desktop-test").resolve("career.properties");SwingUtilities.invokeAndWait(()->{c.start(99,President.Difficulty.NORMAL,President.RunningMate.FUNDRAISER);c.save(save,()->{});check(!c.submit(CareerCommand.campaign(GameCommand.rest())),"commands frozen while saving");});await(c);check(Files.exists(save),"async save completes");SwingUtilities.invokeAndWait(()->{c.start(88,President.Difficulty.HARD,President.RunningMate.COMMUNITY_ORGANIZER);c.load(save);});await(c);SwingUtilities.invokeAndWait(()->check(c.view().campaign().seed()==99,"save restores model"));SwingUtilities.invokeAndWait(()->c.load(save.resolveSibling("missing")));await(c);SwingUtilities.invokeAndWait(()->check(c.view().campaign().seed()==99,"failed load preserves career"));System.out.println("DesktopTests: "+checks+" checks passed");
    }
    static void await(DesktopController c)throws Exception{for(int i=0;i<200;i++){boolean[] busy={false};SwingUtilities.invokeAndWait(()->busy[0]=c.busy());if(!busy[0])return;Thread.sleep(25);}throw new AssertionError("save/load did not complete");}
}
