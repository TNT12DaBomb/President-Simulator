import java.awt.*;
import javax.swing.*;
/** Native full-screen mode preserves the frame, listeners and windowed bounds. */
public final class DisplayMode {
 private DisplayMode(){}
 private static Rectangle previous;private static int previousState;private static boolean fullscreen;
 public static boolean fullscreen(){return fullscreen;}
 public static boolean supported(Component owner){Window w=SwingUtilities.getWindowAncestor(owner);return w!=null&&w.getGraphicsConfiguration().getDevice().isFullScreenSupported();}
 public static void set(Component owner,boolean enabled){Window w=SwingUtilities.getWindowAncestor(owner);if(!(w instanceof JFrame f)||enabled==fullscreen)return;GraphicsDevice device=f.getGraphicsConfiguration().getDevice();if(!device.isFullScreenSupported())return;
  if(enabled){previous=f.getBounds();previousState=f.getExtendedState();device.setFullScreenWindow(f);}else{device.setFullScreenWindow(null);f.setExtendedState(JFrame.NORMAL);if(previous!=null)f.setBounds(previous);f.setExtendedState(previousState);}fullscreen=enabled;
 }
}
