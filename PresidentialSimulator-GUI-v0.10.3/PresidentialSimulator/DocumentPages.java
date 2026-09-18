import java.awt.*;
import java.util.List;
import javax.swing.*;
/** One readable record at a time, with direct page controls and no popup chooser. */
public final class DocumentPages extends JPanel {
 private static final long serialVersionUID=1L;
 public record Page(String title,String body){}
 private int index;private final JLabel heading=new JLabel(),counter=new JLabel();private final JTextArea body=new JTextArea();private final List<Page> pages;
 public DocumentPages(List<Page> pages){this.pages=List.copyOf(pages);setLayout(new BorderLayout(8,8));setBackground(DeskMenuFrame.PAPER);heading.setFont(new Font(Font.SERIF,Font.BOLD,22));heading.setForeground(DeskMenuFrame.INK);add(heading,BorderLayout.NORTH);body.setEditable(false);body.setLineWrap(true);body.setWrapStyleWord(true);body.setFont(new Font(Font.SERIF,Font.PLAIN,17));body.setForeground(DeskMenuFrame.INK);body.setBackground(DeskMenuFrame.PAPER);JScrollPane scroller=new JScrollPane(body);scroller.setBorder(null);add(scroller);JPanel controls=new JPanel(new BorderLayout());controls.setOpaque(false);JButton previous=DesktopPanel.button("Previous",()->{index=Math.max(0,index-1);showPage();});JButton next=DesktopPanel.button("Next",()->{index=Math.min(this.pages.size()-1,index+1);showPage();});controls.add(previous,BorderLayout.WEST);counter.setHorizontalAlignment(SwingConstants.CENTER);controls.add(counter);controls.add(next,BorderLayout.EAST);add(controls,BorderLayout.SOUTH);setPreferredSize(new Dimension(500,310));showPage();}
 private void showPage(){if(pages.isEmpty()){heading.setText("No records yet");body.setText("");counter.setText("0 pages");return;}Page p=pages.get(index);heading.setText(p.title());body.setText(p.body());body.setCaretPosition(0);counter.setText((index+1)+" / "+pages.size());}
}
