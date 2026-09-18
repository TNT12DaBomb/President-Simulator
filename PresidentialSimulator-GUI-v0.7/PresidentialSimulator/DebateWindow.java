import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
/** One owned live window. Closing pauses; returning resumes the same journaled decision. */
public final class DebateWindow {
 private final DesktopPanel owner;
 private final DesktopController controller;
 private final Runnable practice;
 private JDialog window;
 private DebateWindowPanel panel;
 private String root="";
 private boolean heldPause,queued;
 public DebateWindow(DesktopPanel owner,DesktopController controller,Runnable practice){this.owner=owner;this.controller=controller;this.practice=practice;}
 private GameView.PendingEvent current(){var v=controller.view();return v!=null&&v.phase()==CareerView.Phase.CAMPAIGN?v.campaign().pendingEvent():null;}
 public boolean active(){var e=current();return e!=null&&e.id().startsWith("debate_live_");}
 public Component parent(){return window!=null&&window.isVisible()?window:owner;}
 public void updateClock(){if(panel!=null)panel.updateClock();}
 public void sync(){
  if(!active()){if(window!=null){window.dispose();window=null;panel=null;}root="";releasePause();return;}
  var e=current();String next=e.id().substring(0,e.id().lastIndexOf('_'));boolean newAppearance=!root.equals(next);root=next;
  if(panel!=null)panel.refresh();
  if(newAppearance&&!GraphicsEnvironment.isHeadless()&&!queued){queued=true;SwingUtilities.invokeLater(()->{queued=false;if(active())open();});}
 }
 private void releasePause(){if(heldPause){heldPause=false;controller.resume();}}
 public void pauseAndHide(){if(!heldPause){heldPause=true;controller.pause();}if(window!=null)window.setVisible(false);}
 public void open(){
  if(!active()||GraphicsEnvironment.isHeadless())return;releasePause();
  if(window==null){Window parent=SwingUtilities.getWindowAncestor(owner);if(parent==null)return;
   window=new JDialog(parent,"Presidential Simulator — Live debate",Dialog.ModalityType.DOCUMENT_MODAL);window.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
   panel=new DebateWindowPanel(this::current,controller::debateSettings,(e,index)->controller.respond("c:"+e.id()+":"+e.title(),index),()->controller.seconds()>0?(controller.paused()?"PAUSED · ":!controller.realtime()?"CLOCK DISABLED · ":"RESPOND IN ")+controller.seconds()+"s":"Untimed — continue when ready",practice,this::pauseAndHide);
   window.setContentPane(panel);window.addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){pauseAndHide();}});
   Rectangle bounds=DesktopLauncher.usableBounds(parent.getGraphicsConfiguration().getBounds(),Toolkit.getDefaultToolkit().getScreenInsets(parent.getGraphicsConfiguration()));
   window.setSize(Math.min(940,bounds.width-24),Math.min(660,bounds.height-48));window.setMinimumSize(new Dimension(Math.min(560,bounds.width-24),Math.min(380,bounds.height-48)));window.setLocationRelativeTo(parent);
  }
  panel.refresh();window.setVisible(true);
 }
}
