import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
/** Shared materials for the desk shell; no simulation dependencies. */
public final class DeskStyle {
 private DeskStyle(){}
 public static boolean keyboardFocus(JComponent c){return Boolean.TRUE.equals(c.getClientProperty("keyboardFocus"));}
 public static void focus(AbstractButton b){b.setFocusPainted(false);if(Boolean.TRUE.equals(b.getClientProperty("focusInstalled")))return;b.putClientProperty("focusInstalled",true);b.addFocusListener(new java.awt.event.FocusAdapter(){public void focusGained(java.awt.event.FocusEvent e){b.putClientProperty("keyboardFocus",e.getCause()!=java.awt.event.FocusEvent.Cause.MOUSE_EVENT);if(keyboardFocus(b))b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,2,0,GOLD),new EmptyBorder(7,12,5,12)));}public void focusLost(java.awt.event.FocusEvent e){b.putClientProperty("keyboardFocus",false);b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(112,111,85)),new EmptyBorder(7,12,7,12)));}});}

 public static final Color LEATHER=new Color(30,48,44), GOLD=new Color(194,169,113), CREAM=new Color(242,233,211);
 public static JPanel surface(){return new JPanel(){private static final long serialVersionUID=1L;
  @Override protected void paintComponent(Graphics graphics){super.paintComponent(graphics);Graphics2D g=(Graphics2D)graphics.create();g.setPaint(new GradientPaint(0,0,new Color(76,58,43),getWidth(),getHeight(),new Color(32,36,31)));g.fillRect(0,0,getWidth(),getHeight());g.setColor(new Color(255,231,183,9));for(int y=0;y<getHeight();y+=18)g.drawLine(0,y,getWidth(),y+7);g.setColor(LEATHER);g.fillRoundRect(20,20,getWidth()-40,getHeight()-40,24,24);g.setColor(new Color(145,126,87));g.drawRoundRect(26,26,getWidth()-52,getHeight()-52,20,20);g.dispose();}};}
 public static void button(JButton b){b.setDefaultCapable(false);focus(b);b.setBackground(LEATHER);b.setForeground(CREAM);b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(112,111,85)),new EmptyBorder(7,12,7,12)));b.setRolloverEnabled(true);b.addMouseListener(new java.awt.event.MouseAdapter(){public void mouseEntered(java.awt.event.MouseEvent e){if(b.isEnabled())b.setBackground(new Color(53,72,61));}public void mouseExited(java.awt.event.MouseEvent e){b.setBackground(LEATHER);}});}
 public static void paper(Component c){
  if(c instanceof JPanel||c instanceof JViewport||c instanceof JScrollPane)c.setBackground(DeskMenuFrame.PAPER);
  if(c instanceof JTextArea||c instanceof JLabel)c.setForeground(DeskMenuFrame.INK);
  if(c instanceof JTextArea t)t.setBackground(DeskMenuFrame.PAPER);
  if(c instanceof AbstractButton b){b.setBackground(LEATHER);b.setForeground(CREAM);focus(b);}
  if(c instanceof Container parent)for(Component child:parent.getComponents())paper(child);
 }
 public static JPanel dock(){JPanel p=new JPanel(new BorderLayout(10,0));p.setBackground(new Color(32,40,35));p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(2,0,0,0,GOLD),new EmptyBorder(5,12,5,12)));return p;}
}
