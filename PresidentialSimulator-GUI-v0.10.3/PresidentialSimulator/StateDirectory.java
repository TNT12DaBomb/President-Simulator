import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
/** Searchable paper index; map and keyboard selections share one model. */
public final class StateDirectory extends JPanel {
 private static final long serialVersionUID=1L;
 private final String[] names;private final JTextField search=new JTextField();private final JList<String> list=new JList<>();private final java.util.List<ActionListener> listeners=new ArrayList<>();private String selected;private boolean updating;
 public StateDirectory(String[] names){this.names=names.clone();selected=names.length==0?null:names[0];setLayout(new BorderLayout(0,3));setOpaque(false);search.setToolTipText("Search the state index");search.putClientProperty("JTextField.placeholderText","Search states");search.getAccessibleContext().setAccessibleName("Find a state");add(search,BorderLayout.NORTH);list.setVisibleRowCount(3);list.setBackground(DeskMenuFrame.PAPER);list.setSelectionBackground(new Color(211,201,177));list.setSelectionForeground(DeskMenuFrame.INK);list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);add(new JScrollPane(list));search.getDocument().addDocumentListener(new DocumentListener(){public void insertUpdate(DocumentEvent e){filter();}public void removeUpdate(DocumentEvent e){filter();}public void changedUpdate(DocumentEvent e){filter();}});list.addListSelectionListener(e->{if(!updating&&!e.getValueIsAdjusting()&&list.getSelectedValue()!=null){selected=list.getSelectedValue();fire();}});filter();setPreferredSize(new Dimension(180,100));}
 private void filter(){updating=true;list.setListData(Arrays.stream(names).filter(n->n.toLowerCase(Locale.ROOT).contains(search.getText().toLowerCase(Locale.ROOT))).toArray(String[]::new));list.setSelectedValue(selected,true);updating=false;}
 public String getSelectedItem(){return selected;}
 public void setSelectedItem(Object name){if(name==null)return;selected=name.toString();updating=true;search.setText("");updating=false;filter();fire();}
 public void addActionListener(ActionListener listener){listeners.add(listener);}
 private void fire(){for(ActionListener listener:listeners)listener.actionPerformed(new ActionEvent(this,0,"selection"));}
}
