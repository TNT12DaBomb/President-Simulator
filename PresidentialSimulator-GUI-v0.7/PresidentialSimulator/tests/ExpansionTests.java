import java.awt.*;
import java.nio.file.*;
import javax.swing.*;
public final class ExpansionTests {
 static int checks;
 static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 public static void main(String[] args)throws Exception{
  SwingUtilities.invokeAndWait(()->{try{
   DesktopController c=new DesktopController();DesktopPanel p=new DesktopPanel(c);c.start(42,President.Difficulty.NORMAL,President.RunningMate.FUNDRAISER,new DebateSettings(DebateSettings.Mode.FUN,DebateSettings.Pace.UNTIMED,24,true));
   DesktopTests.find(p,"Map").doClick();check(MapDesktopTests.named(p,"Campaign identity")!=null,"explicit team identity");
   while(c.view().campaign().turnsUsed()<4){if(c.view().campaign().pendingEvent()!=null)c.respond(c.decisionKey(),1);else c.submit(CareerCommand.campaign(GameCommand.rest()));}
   check(DesktopTests.find(p,"Practice with your team · free")!=null,"rehearsal in preparation");c.respond(c.decisionKey(),1);JTextArea prompt=(JTextArea)MapDesktopTests.named(p,"Debate prompt");check(prompt.getFont().getSize()==24,"large-text setting applied");var question=c.view().campaign().pendingEvent();var card=QuestionBank.fun().stream().filter(q->question.description().startsWith(q.prompt())).findFirst().orElseThrow();int index=0;while(!question.choices().get(index).label().equals(card.answer()))index++;c.respond(c.decisionKey(),index);c.respond(c.decisionKey(),0);
   if(c.view().campaign().pendingEvent().title().equals("Opponent speaking")){c.respond(c.decisionKey(),1);c.respond(c.decisionKey(),0);}
   check(((JLabel)MapDesktopTests.named(p,"Debate feedback banner")).getText().contains("WELL ANSWERED"),"correct-answer celebration");MapDesktopTests.shot(p,1024,600,"GUI-DEBATE-CORRECT.png");
   int guard=0;while(!c.view().campaign().pendingEvent().title().startsWith("Policy tradeoff")){check(++guard<30,"reach policy round");c.respond(c.decisionKey(),1);}
   var policy=c.view().campaign().pendingEvent();index=0;while(policy.choices().get(index).label().equals("Make no commitment tonight"))index++;c.respond(c.decisionKey(),index);check(!c.debateCommitments().isEmpty(),"commitment public before clarification");c.submit(CareerCommand.campaign(GameCommand.leaveLive()));c.submit(CareerCommand.dev(CareerCommand.DeveloperAction.FORCE_WIN));c.submit(CareerCommand.advance());c.submit(CareerCommand.advance());
   DesktopTests.find(p,"Policy").doClick();var tabs=PolishTests.findTabs(p);tabs.setSelectedIndex(2);var statement=c.debateStatements().stream().filter(s->LiveDebate.commitment(s).isPresent()).findFirst().orElseThrow();var commitment=LiveDebate.commitment(statement).orElseThrow();String name="Introduce promised initiative: "+PolicyCatalog.option(commitment.issue(),commitment.approach()).title();JButton fulfill=DesktopTests.find(p,name);check(fulfill!=null&&fulfill.isEnabled(),"presidency offers promised initiative");fulfill.doClick();check(c.view().presidency().bill().issue()==commitment.issue()&&c.view().presidency().bill().approach()==commitment.approach(),"promise opens matching real bill");
   check(!c.submit(CareerCommand.office(OfficeCommand.simple(OfficeCommand.Type.VETO))),"unpassed bill cannot be vetoed");check(c.view().history().stream().anyMatch(x->x.startsWith("DEBATE COMMITMENT REVIEW:")),"proposal is compared with prior commitment");
  }catch(Exception ex){throw new RuntimeException(ex);}});
  System.out.println("ExpansionTests: "+checks+" checks passed");
 }
}
