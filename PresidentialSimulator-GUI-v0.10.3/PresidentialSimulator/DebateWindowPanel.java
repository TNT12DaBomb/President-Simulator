import java.awt.*;
import java.util.function.*;
import javax.swing.*;
/** Live debate view. No career metrics, voting logic, or state mutation inside the view. */
public final class DebateWindowPanel extends JPanel {
 private static final long serialVersionUID=1L;
 private final Supplier<GameView.PendingEvent> eventSource;
 private final Supplier<DebateSettings> settings;
 private final BiConsumer<GameView.PendingEvent,Integer> respond;
 private final Runnable practice,pause;
 private final Supplier<String> clockText;
 private final JLabel clock=DesktopPanel.label("",18,DesktopPanel.ACCENT);
 private String rendered="";
 public DebateWindowPanel(Supplier<GameView.PendingEvent> source,Supplier<DebateSettings> settings,BiConsumer<GameView.PendingEvent,Integer> respond,Supplier<String> clockText,Runnable practice,Runnable pause){
  this.eventSource=source;this.settings=settings;this.respond=respond;this.clockText=clockText;this.practice=practice;this.pause=pause;setLayout(new BorderLayout());setBackground(DeskMenuFrame.PAPER);setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(DeskStyle.LEATHER,8),new javax.swing.border.EmptyBorder(8,8,8,8)));refresh();
 }
 public void updateClock(){clock.setText(clockText.get());}
 public void refresh(){
  var e=eventSource.get();if(e==null){removeAll();add(DesktopPanel.text("Debate complete. Return to your campaign."));revalidate();repaint();return;}
  var preferences=settings.get();String signature=e.id()+preferences.toString();updateClock();if(signature.equals(rendered))return;rendered=signature;removeAll();
  JPanel sheet=DesktopPanel.column();sheet.add(DesktopPanel.label("LIVE DEBATE",22,DesktopPanel.ACCENT));sheet.add(clock);
  String title=e.title().startsWith("CORRECT")?(preferences.celebrations()?"WELL ANSWERED! — fact verified":"CORRECT — source verified"):e.title();
  JLabel heading=DesktopPanel.label(title,22,e.title().startsWith("CORRECT")?DesktopPanel.ACCENT:e.title().startsWith("INCORRECT")?new Color(255,158,146):DesktopPanel.INK);heading.setName("Debate feedback banner");sheet.add(heading);
  JTextArea prompt=DesktopPanel.text(e.description());prompt.setName("Debate prompt");prompt.setFont(prompt.getFont().deriveFont((float)preferences.textSize()));sheet.add(prompt);
  if(e.title().equals("Debate preparation"))sheet.add(DesktopPanel.button("Practice with your team · fixed tutorial",practice));
  for(int i=0;i<e.choices().size();i++){int choice=i;JButton b=DesktopPanel.button(e.choices().get(i).label(),()->respond.accept(e,choice));b.setName("Decision option "+i);b.setEnabled(e.choices().get(i).available());b.setFont(b.getFont().deriveFont((float)preferences.textSize()));sheet.add(b);sheet.add(Box.createVerticalStrut(8));}
  sheet.add(DesktopPanel.text(e.missedResponse()));JScrollPane scroll=new JScrollPane(sheet);scroll.setBorder(null);scroll.getViewport().setBackground(DesktopPanel.CARD);scroll.getVerticalScrollBar().setUnitIncrement(24);add(scroll);
  JPanel footer=new JPanel(new FlowLayout(FlowLayout.RIGHT));footer.setBackground(DesktopPanel.CARD);footer.add(DesktopPanel.button("Return to campaign",pause));add(footer,BorderLayout.SOUTH);DeskStyle.paper(this);heading.setForeground(e.title().startsWith("CORRECT")?new Color(54,105,65):e.title().startsWith("INCORRECT")?new Color(153,66,45):DeskMenuFrame.INK);revalidate();repaint();
 }
}
