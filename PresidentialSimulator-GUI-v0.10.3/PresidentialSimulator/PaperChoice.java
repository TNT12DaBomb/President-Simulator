import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
/** Direct document selections, sharing the small selection API used by forms. */
public final class PaperChoice<T> extends JPanel {
 private static final long serialVersionUID=1L;
 private final T[] values;private int selected;private final java.util.List<ActionListener> listeners=new ArrayList<>();private final java.util.List<JToggleButton> buttons=new ArrayList<>();
 public PaperChoice(T[] values){this.values=values.clone();setOpaque(false);setLayout(new GridLayout(0,values.length>6?3:Math.max(1,Math.min(3,values.length)),5,5));ButtonGroup group=new ButtonGroup();for(int i=0;i<values.length;i++){final int index=i;T value=values[i];JToggleButton b=new JToggleButton(value instanceof Enum<?>?DesktopPanel.friendlyName(value):String.valueOf(value));b.setUI(new javax.swing.plaf.metal.MetalToggleButtonUI(){@Override protected Color getSelectColor(){return new Color(211,201,177);}});b.setBackground(DeskMenuFrame.PAPER);b.setForeground(DeskMenuFrame.INK);DeskStyle.focus(b);b.setMargin(new Insets(6,8,6,8));if(value instanceof TeamColors color){b.setText("●");b.setForeground(color.accent);b.setToolTipText(color.toString());b.getAccessibleContext().setAccessibleName(color.toString());}group.add(b);buttons.add(b);add(b);b.addActionListener(e->setSelectedIndex(index));}if(!buttons.isEmpty())buttons.get(0).setSelected(true);}
 public int getSelectedIndex(){return values.length==0?-1:selected;}
 public T getSelectedItem(){return values.length==0?null:values[selected];}
 public void setSelectedIndex(int i){if(i<0||i>=values.length)return;selected=i;buttons.get(i).setSelected(true);for(ActionListener l:listeners)l.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"selection"));}
 public void setSelectedItem(Object value){for(int i=0;i<values.length;i++)if(Objects.equals(values[i],value)){setSelectedIndex(i);return;}}
 public void addActionListener(ActionListener l){listeners.add(l);}
 public void setMaximumRowCount(int ignored){}
 @Override public void setMaximumSize(Dimension ignored){} // Keep all choices visible in document layouts.
}
