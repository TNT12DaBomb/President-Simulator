import java.nio.file.*;
import java.util.*;
public final class DebateTests {
 static int checks;
 static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 static CareerEngine game(){return new CareerEngine(71,President.Difficulty.NORMAL,President.RunningMate.FUNDRAISER,GameRules.CURRENT);}
 static void accept(CareerEngine e,GameCommand c){var r=e.submit(CareerCommand.campaign(c));check(r.accepted(),r.messages().toString());}
 static void toDebate(CareerEngine e,int turn){int guard=0;while(e.view().campaign().turnsUsed()<turn){check(++guard<200,"progress bounded");if(e.view().campaign().pendingEvent()!=null)accept(e,GameCommand.respond(1));else accept(e,GameCommand.rest());}}
 static int answer(LiveDebate d,boolean correct,DebateSettings.Mode mode){var event=d.event();var card=DebateFactChecks.forMode(mode).stream().filter(c->event.description().startsWith(c.prompt())).findFirst().orElseThrow();for(int i=0;i<event.options().size();i++)if(event.options().get(i).label().equals(card.answer())==correct)return i;throw new AssertionError();}
 public static void main(String[] args)throws Exception{
  check(DebateFactChecks.all().size()==32&&QuestionBank.fun().size()==20,"52-question bank");
  for(var mode:DebateSettings.Mode.values()){var bank=DebateFactChecks.forMode(mode);check(bank.stream().map(DebateFactChecks.Card::title).distinct().count()==bank.size(),"unique questions");for(var c:bank){check(new HashSet<>(c.choices()).size()==c.choices().size(),"distinct answers");check(c.choices().stream().filter(x->x.equals(c.answer())).count()==1,"one factual answer");}}
  boolean challengedCorrect=false,unchallengedCorrect=false,falseClaim=false,trueClaim=false;
  for(int seed=0;seed<30;seed++)for(var mode:DebateSettings.Mode.values())for(boolean correct:new boolean[]{false,true}){
   var settings=new DebateSettings(mode,DebateSettings.Pace.STANDARD,18,true);var memory=new ArrayList<LiveDebate.Statement>();var d=new LiveDebate("economy",seed,memory,settings);
   check(d.event().description().contains("No cash"),"clear preparation cost");d.respond(0);check(d.event().description().contains("4 of 6"),"review spends exactly two briefing slots");d.respond(1);check(d.seconds()==30,"knowledge reading time");d.respond(answer(d,correct,mode));d.respond(0);
   if(d.stage()==LiveDebate.Stage.CHALLENGE){if(correct)challengedCorrect=true;d.timeout();check(d.seconds()==20,"recovery time");d.respond(0);falseClaim|=d.event().description().contains("opponent's correction was inaccurate");trueClaim|=d.event().description().contains("opponent's correction was accurate");}else if(correct)unchallengedCorrect=true;
   check(d.event().title().startsWith(correct?"CORRECT":"INCORRECT"),"explicit factual feedback");check(d.event().description().contains("Source: https://"),"source supplied");
   while(d.stage()!=LiveDebate.Stage.POLICY){if(d.seconds()>0)d.timeout();else d.respond(1);}int choice=0;while(d.event().options().get(choice).label().equals("Make no commitment tonight"))choice++;d.respond(choice);check(memory.stream().anyMatch(x->LiveDebate.commitment(x).isPresent()),"policy persists immediately");d.leave();check(memory.stream().anyMatch(x->LiveDebate.commitment(x).isPresent()),"leaving cannot erase statement");
   var next=new LiveDebate("economy",seed+1,memory,settings);next.respond(1);while(next.stage()!=LiveDebate.Stage.POLICY){if(next.seconds()>0)next.timeout();else next.respond(1);}next.respond(0);check(next.event().description().contains("In the economy debate"),"later debate quotes record");
  }
  check(challengedCorrect&&unchallengedCorrect&&falseClaim&&trueClaim,"all challenge branches exercised");
  var e=game();e.submit(CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.DEBATE_MODE,1)));e.submit(CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.DEBATE_PACE,1)));toDebate(e,4);int funds=e.view().campaign().playerFunds();accept(e,GameCommand.respond(0));check(e.view().campaign().playerFunds()==funds&&e.view().campaign().turnsUsed()==4,"review costs no cash or turns");accept(e,GameCommand.respond(1));check(e.view().campaign().pendingEvent().secondsRemaining()==60,"relaxed pace used");accept(e,GameCommand.tick(7));
  Path save=Files.createTempFile("debate",".save");try{CareerSave.write(e,save);var loaded=CareerSave.read(save);check(e.view().equals(loaded.view())&&e.debateSettings().equals(loaded.debateSettings()),"settings and clock replay exact");accept(e,GameCommand.respond(0));accept(loaded,GameCommand.respond(0));check(e.view().equals(loaded.view()),"next stage replay exact");}finally{Files.deleteIfExists(save);}
  var untimed=new LiveDebate("record",1,new ArrayList<>(),new DebateSettings(DebateSettings.Mode.FUN,DebateSettings.Pace.UNTIMED,24,false));untimed.respond(1);check(untimed.seconds()==0,"untimed mode");
  System.out.println("DebateTests: "+checks+" checks passed");
 }
}
