import java.awt.*;
import javax.swing.*;
public final class DesktopLauncher {
    private DesktopLauncher(){}
    static Rectangle usableBounds(Rectangle bounds,Insets insets){return new Rectangle(bounds.x+insets.left,bounds.y+insets.top,Math.max(1,bounds.width-insets.left-insets.right),Math.max(1,bounds.height-insets.top-insets.bottom));}
    public static void main(String[] args){
        if(java.util.Arrays.asList(args).contains("--terminal")){PresidentialSimulator.main(new String[0]);return;}
        if(GraphicsEnvironment.isHeadless()){System.err.println("A desktop display is required. For terminal play: java -jar presidential-simulator.jar --terminal");return;}
        SwingUtilities.invokeLater(()->{
            UIManager.put("Button.font",new Font(Font.SANS_SERIF,Font.PLAIN,14));
            DesktopController controller=new DesktopController();controller.autosaveTo(java.nio.file.Path.of(System.getProperty("user.home"),"PresidentialSimulator","saves","autosave.properties"));DesktopPanel panel=new DesktopPanel(controller);JFrame window=new JFrame("Presidential Simulator • v0.7");window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);window.setContentPane(panel);
            Rectangle usable=usableBounds(window.getGraphicsConfiguration().getBounds(),Toolkit.getDefaultToolkit().getScreenInsets(window.getGraphicsConfiguration()));
            window.setBounds(usable.x+8,usable.y+8,Math.max(1,Math.min(1280,usable.width-16)),Math.max(1,Math.min(760,usable.height-16)));
            window.setMinimumSize(new Dimension(Math.min(800,usable.width-16),Math.min(480,usable.height-16)));
            Timer timer=new Timer(100,e->controller.pulse());
            window.addWindowListener(new java.awt.event.WindowAdapter(){public void windowClosing(java.awt.event.WindowEvent e){panel.requestClose(()->{timer.stop();window.dispose();});}});
            final boolean[] focusPaused={false};
            java.beans.PropertyChangeListener focus=e->SwingUtilities.invokeLater(()->{Window active=KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();boolean inside=false;for(Window w=active;w!=null;w=w.getOwner())if(w==window){inside=true;break;}if(!inside&&!focusPaused[0]){focusPaused[0]=true;controller.pause();}else if(inside&&focusPaused[0]){focusPaused[0]=false;controller.resume();}});
            KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("activeWindow",focus);
            window.addWindowListener(new java.awt.event.WindowAdapter(){public void windowClosed(java.awt.event.WindowEvent e){KeyboardFocusManager.getCurrentKeyboardFocusManager().removePropertyChangeListener("activeWindow",focus);}});
            window.addWindowListener(new java.awt.event.WindowAdapter(){public void windowClosed(java.awt.event.WindowEvent e){timer.stop();}});window.setVisible(true);timer.start();
        });
    }
}
