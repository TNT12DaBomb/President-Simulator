import java.awt.*;
import javax.swing.*;
public final class DesktopLauncher {
    private DesktopLauncher(){}
    public static void main(String[] args){
        if(java.util.Arrays.asList(args).contains("--terminal")){PresidentialSimulator.main(new String[0]);return;}
        if(GraphicsEnvironment.isHeadless()){System.err.println("A desktop display is required. For terminal play: java -jar presidential-simulator.jar --terminal");return;}
        SwingUtilities.invokeLater(()->{
            UIManager.put("Button.font",new Font(Font.SANS_SERIF,Font.PLAIN,14));
            DesktopController controller=new DesktopController();controller.autosaveTo(java.nio.file.Path.of(System.getProperty("user.home"),"PresidentialSimulator","saves","autosave.properties"));DesktopPanel panel=new DesktopPanel(controller);JFrame window=new JFrame("Presidential Simulator • v0.2");window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);window.setContentPane(panel);
            Dimension screen=Toolkit.getDefaultToolkit().getScreenSize();window.setSize(Math.min(1320,screen.width),Math.min(850,screen.height-40));window.setMinimumSize(new Dimension(Math.min(1050,screen.width),Math.min(650,screen.height-40)));window.setLocationRelativeTo(null);
            Timer timer=new Timer(100,e->controller.pulse());
            window.addWindowListener(new java.awt.event.WindowAdapter(){public void windowClosing(java.awt.event.WindowEvent e){panel.requestClose(()->{timer.stop();window.dispose();});}});
            window.addWindowFocusListener(new java.awt.event.WindowAdapter(){public void windowLostFocus(java.awt.event.WindowEvent e){controller.pause();}public void windowGainedFocus(java.awt.event.WindowEvent e){controller.resume();}});
            window.setVisible(true);timer.start();
        });
    }
}
