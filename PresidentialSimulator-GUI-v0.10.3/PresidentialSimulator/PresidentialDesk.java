import java.awt.*;
import java.util.function.Consumer;
import javax.swing.*;
/** Procedural work board: uses existing commands and recorded task status. */
public final class PresidentialDesk extends JPanel {
 private static final long serialVersionUID=1L;
 public PresidentialDesk(DesktopController controller,Consumer<String> navigate){
  setLayout(new BorderLayout(8,8));setBackground(DesktopPanel.CARD);setBorder(new javax.swing.border.EmptyBorder(10,10,10,10));
  var v=controller.view();var office=v.presidency();boolean pending=!controller.decisionKey().isEmpty();
  JPanel top=DesktopPanel.column();top.setBorder(null);top.add(DesktopPanel.label("THE PRESIDENT'S DESK",21,DesktopPanel.INK));top.add(DesktopPanel.label(office.date()+" · "+office.actionsLeft()+" actions remaining",14,DesktopPanel.ACCENT));
  String next=pending?"A decision is waiting. Review it before continuing your agenda.":office.actionsLeft()==0?"Your actions are used. Review work in progress, then end the month.":"Choose a task below. Every action uses part of this month’s allowance.";
  top.add(DesktopPanel.text(next));JProgressBar month=new JProgressBar(0,48);month.setValue(office.monthsCompleted());month.setStringPainted(true);month.setString("Term calendar: "+office.monthsCompleted()+" / 48 months completed");top.add(month);add(top,BorderLayout.NORTH);
  JPanel board=new JPanel(new GridLayout(2,2,8,8));board.setOpaque(false);
  board.add(card("Administration",office.cabinet().seats().stream().filter(s->s.official()!=null).count()+" filled posts · "+office.cabinet().seats().stream().filter(s->s.nominee()!=null).count()+" nominations pending","Appointments & projects",()->navigate.accept("Office")));
  board.add(card("Legislative desk",office.bill()==null?"No bill on the desk. Open Policy to review available initiatives.":DesktopPanel.friendlyName(office.bill().issue())+" · "+office.bill().stage(),"Open legislation",()->navigate.accept("Policy")));
  String deliveries=office.implementations().isEmpty()?"No policy deliveries in progress.":office.implementations().stream().map(i->i.title()+" — due month "+i.dueMonth()).reduce((a,b)->a+"\n"+b).orElse("");
  board.add(card("Work in progress",deliveries,"Review schedule",()->navigate.accept("Schedule")));
  board.add(card("Public correspondence",office.correspondence().requests().stream().filter(r->!r.answered()).count()+" requests await acknowledgment. Replies and delivery are tracked separately.","Read requests",()->navigate.accept("Requests")));add(board,BorderLayout.CENTER);
  JPanel bottom=DesktopPanel.column();bottom.setBorder(null);
  if(pending)bottom.add(DesktopPanel.button("Review pending decision",()->navigate.accept("Decisions")));
  else{var options=v.governanceOptions();PaperChoice<String> pick=new PaperChoice<>(options.stream().map(a->DesktopPanel.friendlyName(a.action())+" · $"+a.cost()).toArray(String[]::new));JButton act=DesktopPanel.button("Take selected action",()->controller.submit(CareerCommand.govern(options.get(pick.getSelectedIndex()).action())));Runnable update=()->{var a=options.get(pick.getSelectedIndex());act.setEnabled(a.available()&&!controller.busy());act.setToolTipText(a.available()?WorldSimulation.actionHelp(a.action()):a.reason());};pick.addActionListener(e->update.run());update.run();JPanel row=new JPanel(new BorderLayout(8,0));row.setOpaque(false);row.add(pick);row.add(act,BorderLayout.EAST);row.setPreferredSize(new Dimension(0,180));bottom.add(row);}
  JButton end=DesktopPanel.button("End month — resolve ongoing work",()->{if(office.actionsLeft()>0){controller.pause();try{if(JOptionPane.showConfirmDialog(this,"You have "+office.actionsLeft()+" unused actions. End the month anyway?","End month",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;}finally{controller.resume();}}controller.submit(CareerCommand.office(OfficeCommand.simple(OfficeCommand.Type.END_MONTH)));});end.setEnabled(!pending&&!controller.busy());bottom.add(end);add(bottom,BorderLayout.SOUTH);
 }
 private JPanel card(String title,String detail,String action,Runnable run){JPanel p=new JPanel(new BorderLayout(4,4));p.setBackground(new Color(30,46,63));p.setBorder(new javax.swing.border.EmptyBorder(8,10,8,10));p.add(DesktopPanel.label(title,16,DesktopPanel.ACCENT),BorderLayout.NORTH);JTextArea text=DesktopPanel.text(detail);text.setBorder(null);JScrollPane scroll=new JScrollPane(text);scroll.setBorder(null);scroll.getViewport().setBackground(p.getBackground());p.add(scroll);p.add(DesktopPanel.button(action,run),BorderLayout.SOUTH);return p;}
}
