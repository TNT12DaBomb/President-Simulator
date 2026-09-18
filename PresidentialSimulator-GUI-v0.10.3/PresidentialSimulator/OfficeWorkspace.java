import java.awt.*;
import java.util.function.Consumer;
import javax.swing.*;
/** Physical desk hub. Drawer options navigate existing workflows without spending an action. */
public final class OfficeWorkspace extends JPanel {
 private static final long serialVersionUID=1L;
 public static final class Selection {int tab,department,request,contest;DeskScene.Item object=DeskScene.Item.CALENDAR;boolean administration,motion=true;}
 private final JLabel deadline=DesktopPanel.label("",12,DesktopPanel.ACCENT);
 public void updateClock(){deadline.setText(controller.decisionKey().isEmpty()?"Select an object to open its menu":controller.seconds()>0?(controller.paused()?"Paused · ":"Incoming decision · ")+controller.seconds()+" seconds remaining":"Incoming decision · open the secure phone");}
 private final DesktopController controller;private final Selection selection;private final Consumer<String> navigate;private final JPanel body=new JPanel(new BorderLayout(8,8));
 public OfficeWorkspace(DesktopController controller,Selection selection){this(controller,selection,x->{});}
 public OfficeWorkspace(DesktopController controller,Selection selection,Consumer<String> navigate){this.controller=controller;this.selection=selection;this.navigate=navigate;setLayout(new BorderLayout(6,6));setBackground(DesktopPanel.BG);if(controller.view().phase()!=CareerView.Phase.PRESIDENCY){add(DesktopPanel.text("The desk opens after inauguration."));return;}
  JPanel top=new JPanel(new BorderLayout(8,0));top.setOpaque(false);var v=controller.view().presidency();top.add(DesktopPanel.label(v.date()+" · "+v.actionsLeft()+" actions left",15,DesktopPanel.INK));JCheckBox motion=new JCheckBox("Phone light animation",selection.motion);motion.setOpaque(false);motion.setForeground(DesktopPanel.MUTED);motion.addActionListener(e->{selection.motion=motion.isSelected();render();});top.add(motion,BorderLayout.EAST);top.add(deadline,BorderLayout.SOUTH);updateClock();add(top,BorderLayout.NORTH);body.setOpaque(false);add(body);render();
 }
 private void render(){body.removeAll();if(selection.administration){JButton back=DesktopPanel.button("← Back to desk",()->{selection.administration=false;render();});body.add(back,BorderLayout.NORTH);body.add(new AdministrationWorkspace(controller,selection));}else{
  var office=controller.view().presidency();var state=new DeskScene.State(!controller.decisionKey().isEmpty(),office.bill()!=null,(int)office.cabinet().seats().stream().filter(s->s.nominee()!=null).count(),(int)office.correspondence().requests().stream().filter(r->!r.answered()).count(),office.implementations().size(),office.monthsCompleted());
  JPanel drawer=DesktopPanel.column();drawer.setBorder(new javax.swing.border.EmptyBorder(10,10,10,10));JScrollPane scroll=new JScrollPane(drawer);scroll.setBorder(null);scroll.setPreferredSize(new Dimension(225,200));scroll.getVerticalScrollBar().setUnitIncrement(16);
  DeskScene scene=new DeskScene(state,item->{selection.object=item;drawer(drawer,item);});scene.motion(selection.motion);scene.select(selection.object);body.add(scene);body.add(scroll,BorderLayout.EAST);drawer(drawer,selection.object);
 }body.revalidate();body.repaint();}
 private void option(JPanel panel,String label,Runnable action){JButton button=DesktopPanel.button(label,action);button.setEnabled(!controller.busy());panel.add(button);panel.add(Box.createVerticalStrut(6));}
 private void administration(int tab){selection.tab=tab;selection.administration=true;render();}
 private void drawer(JPanel p,DeskScene.Item item){p.removeAll();p.add(DesktopPanel.label(item.label,18,DesktopPanel.ACCENT));var office=controller.view().presidency();String description=switch(item){
  case SECURE_PHONE->controller.decisionKey().isEmpty()?"The line is quiet. This phone lights up when an existing presidential decision needs your response.":"An incoming decision needs attention. Read the briefing before choosing a response.";
  case PHONE->"Open staff and public correspondence. Dedicated calls with foreign leaders and congressional contacts are planned.";
  case FOLIO->office.bill()==null?"No bill is currently on your desk. Review initiatives or recorded commitments in Policy.":DesktopPanel.friendlyName(office.bill().issue())+" · "+office.bill().stage()+". Review the bill before signing or vetoing.";
  case CABINET->"Choose officials, review nominations and delegate the department projects already supported by the simulation.";
  case NEWS->"Today's fictional headlines. The paper is an information object; opening it does not advance time.";
  case MEMO->"Read the existing statistics page and recorded changes. This memo is a shortcut, not a new polling system.";
  case CALENDAR->office.actionsLeft()+" actions remain this month. Check delivery dates, choose a general action, or end the month.";
  case LETTERS->"Open public requests. The paper stack grows with unanswered letters; a reply is separate from delivering the requested initiative.";
  case NOTEBOOK->"Your campaign commitments remain on the record. Review them alongside legislative decisions.";
  case OUTBOX->"Review actions, results and undismissed activity. Filing and dismissal work through Latest Activity.";
  case LAPTOP->"Open the compact work board for general presidential actions and progress through the term.";
  case SEAL->"Review your election and career history. This plaque is an overview shortcut.";
 };p.add(DesktopPanel.text(description));
 switch(item){
  case SECURE_PHONE->{if(!controller.decisionKey().isEmpty())option(p,"Answer incoming decision",()->navigate.accept("Decisions"));}
  case PHONE->{option(p,"Contact administration",()->administration(0));option(p,"Open correspondence",()->administration(1));}
  case FOLIO,NOTEBOOK->option(p,"Open Policy",()->navigate.accept("Policy"));
  case CABINET->option(p,"Appointments & projects",()->administration(0));
  case NEWS->{for(String headline:NewsFeed.headlines(controller.view()))p.add(DesktopPanel.text(headline));option(p,"Read activity reports",()->navigate.accept("Latest Activity"));}
  case MEMO->option(p,"Open Statistics",()->navigate.accept("Statistics"));
  case CALENDAR->{option(p,"Review schedule",()->navigate.accept("Schedule"));option(p,"Choose a presidential action",()->navigate.accept("Dashboard"));option(p,"Midterm visits",()->administration(2));if(controller.decisionKey().isEmpty())option(p,"End month…",()->endMonth());}
  case LETTERS->option(p,"Read public requests",()->administration(1));
  case OUTBOX->option(p,"Open Latest Activity",()->navigate.accept("Latest Activity"));
  case LAPTOP->option(p,"Open work board",()->navigate.accept("Dashboard"));
  case SEAL->option(p,"Open career history",()->navigate.accept("History"));
 }
 p.add(new JSeparator());p.add(DesktopPanel.text("Selecting objects is free. Confirmed actions follow the normal costs and monthly limits."));p.revalidate();p.repaint();}
 private void endMonth(){controller.pause();try{int left=controller.view().presidency().actionsLeft();if(left>0&&JOptionPane.showConfirmDialog(this,"End the month with "+left+" unused actions?","End month",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;controller.submit(CareerCommand.office(OfficeCommand.simple(OfficeCommand.Type.END_MONTH)));}finally{controller.resume();}}
}
