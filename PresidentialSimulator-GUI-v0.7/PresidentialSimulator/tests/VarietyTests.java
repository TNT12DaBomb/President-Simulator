import java.util.*;
import java.awt.*;
import javax.swing.*;
public final class VarietyTests {
 static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
 static GameView.PendingEvent view(LiveDebate d){if(d.complete())return null;var e=d.event();return new GameView.PendingEvent(e.id(),e.title(),e.description(),null,e.options().stream().map(o->new GameView.ChoiceView(o.label(),true,"")).toList(),0,d.seconds(),d.missed());}
 static void begin(LiveDebate d){var o=d.event().options();d.respond(o.size()-1);check(d.stage()==LiveDebate.Stage.ANSWER,"begin last");}
 static java.util.List<String> run(long seed,DebateSettings.Mode mode){var d=new LiveDebate("practice",seed,new ArrayList<>(),new DebateSettings(mode,DebateSettings.Pace.STANDARD,18,true));var log=new ArrayList<String>();int guard=0;while(!d.complete()){check(++guard<40,"completion");log.add(d.event().description()+d.event().options());if(d.stage()==LiveDebate.Stage.PREP)begin(d);else d.respond(0);}check(d.statements().stream().noneMatch(s->LiveDebate.commitment(s).isPresent()),"practice has no public commitment");return log;}
 static JButton button(Container c,String name){for(Component x:c.getComponents()){if(x instanceof JButton b&&name.equals(b.getName()))return b;if(x instanceof Container n){var b=button(n,name);if(b!=null)return b;}}return null;}
 static void layout(Container c){c.doLayout();for(Component x:c.getComponents())if(x instanceof Container n)layout(n);}
 public static void main(String[] args)throws Exception{
 int count=0;Set<String> prompts=new HashSet<>();
 for(var mode:DebateSettings.Mode.values()){
 var bank=DebateFactChecks.forMode(mode);count+=bank.size();for(var c:bank){check(prompts.add(c.prompt()),"duplicate prompt: "+c.prompt());check(c.choices().contains(c.answer()),"answer missing");check(new HashSet<>(c.choices()).size()==c.choices().size(),"duplicate answers");check(!c.source().isBlank(),"source missing");}
 Set<String> selections=new HashSet<>();for(int seed=0;seed<200;seed++){var selected=QuestionSelection.select(bank,seed,java.util.List.of());check(selected.equals(QuestionSelection.select(bank,seed,java.util.List.of())),"replay");check(selected.stream().map(QuestionSelection::topic).distinct().count()==3,"topic repeats");selections.add(selected.toString());}check(selections.size()>1,"seed variety");
 check(run(1,mode).equals(run(999,mode)),"practice fixed across seeds");
 }
 check(count==140,"bank size "+count);for(var mode:DebateSettings.Mode.values())for(var c:PracticeBank.forMode(mode))check(!prompts.contains(c.prompt()),"practice overlap");
 SwingUtilities.invokeAndWait(()->{var d=new LiveDebate("practice",0,new ArrayList<>());final String[] reply={""};var p=new DebateWindowPanel(()->view(d),()->DebateSettings.DEFAULT,(e,i)->reply[0]=e.id(),()->"29s",()->{},()->{});p.setSize(800,500);layout(p);var b=button(p,"Decision option 0");check(b!=null&&b.getFont().getSize()>=18,"readable choices");p.updateClock();check(button(p,"Decision option 0")==b,"clock rebuilt buttons");String old=d.event().id();begin(d);p.refresh();b.doClick();check(reply[0].equals(old),"callback must capture displayed revision");d.timeout();p.refresh();check(button(p,"Decision option 0").getText().contains("Continue"),"single continue");check(button(p,"Decision option 1")==null,"duplicate continue");});
 System.out.println("PASS: 140 live cards; isolated fixed practice; 200 seeds per mode; topic rotation; protocol completion; stable Swing choices.");
 }
}
