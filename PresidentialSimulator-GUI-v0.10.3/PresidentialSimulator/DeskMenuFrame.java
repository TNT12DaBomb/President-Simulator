import java.awt.*;
import javax.swing.*;
/** One visual language: warm paper workspaces in restrained leather/chrome framing. */
public final class DeskMenuFrame extends JPanel {
 private static final long serialVersionUID=1L;
 public static final Color PAPER=new Color(235,229,213), INK=new Color(44,49,47), LEATHER=new Color(36,54,61);
 private final String theme;
 public DeskMenuFrame(String theme,String title,JComponent content){this(theme,title,content,null);}
 public DeskMenuFrame(String theme,String title,JComponent content,Runnable close){
  this.theme=theme;setName("Workspace theme "+theme);setLayout(new BorderLayout(8,8));
  boolean paper=!theme.equals("phone")&&!theme.equals("monitor");
  setBackground(paper?PAPER:DeskStyle.LEATHER);
  setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(DeskStyle.LEATHER,8),new javax.swing.border.EmptyBorder(theme.equals("calendar")?20:12,theme.equals("binder")?34:18,14,18)));
  apply(content,paper);
  JPanel header=new JPanel(new BorderLayout(6,3));header.setOpaque(false);
  String caption=switch(theme){case "binder"->"WORKING FOLIO / ADMINISTRATION";case "newspaper"->"THE DAILY RECORD";case "calendar"->"APPOINTMENT BOOK";case "phone"->"COMMUNICATIONS / INCOMING CALLS";case "monitor"->"CAMPAIGN SYSTEMS / ANALYTICS";case "map"->"STRATEGY TABLE";case "letter"->"OFFICE CORRESPONDENCE";case "archive"->"THE PRESIDENTIAL RECORD";case "report"->"RESEARCH & ECONOMIC BRIEFING";default->"STAFF RECORD / ACTIVITY";};
  JLabel eyebrow=new JLabel(caption);eyebrow.setFont(new Font(Font.SANS_SERIF,Font.BOLD,10));eyebrow.setForeground(paper?new Color(111,98,74):DeskStyle.GOLD);header.add(eyebrow,BorderLayout.NORTH);
  JLabel heading=new JLabel(theme.equals("newspaper")?"The Daily Record":title);heading.setForeground(paper?INK:DeskStyle.CREAM);heading.setFont(new Font(theme.equals("monitor")||theme.equals("phone")?Font.SANS_SERIF:Font.SERIF,Font.BOLD,theme.equals("newspaper")?32:26));header.add(heading,BorderLayout.CENTER);
  header.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0,0,1,0,paper?new Color(165,153,126):DeskStyle.GOLD),new javax.swing.border.EmptyBorder(2,0,10,0)));
  if(theme.equals("newspaper")){eyebrow.setText("NATIONAL EDITION  /  CAMPAIGN & GOVERNMENT");eyebrow.setHorizontalAlignment(SwingConstants.CENTER);heading.setHorizontalAlignment(SwingConstants.CENTER);}
  if(theme.equals("monitor")){content.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new Color(84,99,90),2),new javax.swing.border.EmptyBorder(8,8,8,8)));}
  if(close!=null){JButton back=DesktopPanel.button("Close document",close);back.setName("← Desk");header.add(back,BorderLayout.EAST);}add(header,BorderLayout.NORTH);add(content);
 }
 private static void apply(Component c,boolean paper){Color bg=paper?PAPER:DeskStyle.LEATHER,fg=paper?INK:DesktopPanel.INK;
  if(c instanceof JPanel||c instanceof JViewport||c instanceof JScrollPane){c.setBackground(bg);if(c instanceof JComponent j)j.setOpaque(true);}
  if(c instanceof JTextArea t){t.setForeground(fg);t.setBackground(bg);}
  if(c instanceof JLabel l)l.setForeground(fg);
  if(c instanceof JButton b)DeskStyle.button(b);
  if(c instanceof JTabbedPane t){t.setBackground(bg);t.setForeground(fg);t.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI(){
   @Override protected void installDefaults(){super.installDefaults();highlight=PAPER;lightHighlight=PAPER;shadow=new Color(171,157,125);darkShadow=shadow;focus=DeskStyle.GOLD;}
   @Override protected void paintTabBackground(Graphics g,int placement,int index,int x,int y,int w,int h,boolean selected){g.setColor(selected?new Color(218,207,180):PAPER);g.fillRect(x,y,w,h);}
  });}
  if(c instanceof JComboBox<?> box){box.setBackground(PAPER);box.setForeground(INK);}

  if(c instanceof JScrollPane s){s.setBorder(null);s.getViewport().setBackground(bg);}
  if(c instanceof Container p)for(Component child:p.getComponents())apply(child,paper);
 }
 @Override protected void paintComponent(Graphics graphics){
  super.paintComponent(graphics);Graphics2D g=(Graphics2D)graphics.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
  if(theme.equals("binder")||theme.equals("archive")){g.setColor(DeskStyle.LEATHER);g.fillRect(8,8,22,getHeight()-16);g.setColor(DeskStyle.GOLD);g.setStroke(new BasicStroke(3));for(int y=110;y<getHeight()-25;y+=110)g.drawOval(16,y,19,12);}
  if(theme.equals("calendar")){g.setColor(new Color(134,69,51));g.fillRect(8,8,getWidth()-16,12);g.setColor(DeskStyle.GOLD);g.setStroke(new BasicStroke(3));for(int x=45;x<getWidth()-25;x+=50)g.drawLine(x,9,x,25);}
  if(theme.equals("letter")){g.setColor(new Color(169,69,52));g.fillOval(getWidth()-56,22,23,23);g.setColor(DeskStyle.GOLD);g.drawOval(getWidth()-52,26,15,15);}
  g.dispose();
 }
}
