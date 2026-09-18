import java.nio.file.*;
import java.util.*;
public final class DebateTests {
 static int checks;
 static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 static CareerEngine game(){return new CareerEngine(71,President.Difficulty.NORMAL,President.RunningMate.FUNDRAISER,GameRules.CURRENT);}
 static void accept(CareerEngine e,GameCommand c){var r=e.submit(CareerCommand.campaign(c));check(r.accepted(),r.messages().toString());}
 static void toDebate(CareerEngine e,int turn){int guard=0;while(e.view().campaign().turnsUsed()<turn){check(++guard<200,"progress bounded");if(e.view().campaign().pendingEvent()!=null)accept(e,GameCommand.respond(1));else accept(e,GameCommand.rest());}}
 public static void main(String[] args)throws Exception{
  var e=game();toDebate(e,4);check(e.view().campaign().pendingEvent().secondsRemaining()==0,"prep untimed");var initial=e.view();check(!e.submit(CareerCommand.campaign(GameCommand.rest())).accepted()&&initial.equals(e.view()),"cannot bypass debate");accept(e,GameCommand.respond(0));accept(e,GameCommand.respond(1));check(e.view().campaign().pendingEvent().secondsRemaining()==12,"fresh question clock");
  accept(e,GameCommand.respond(1));check(e.view().campaign().pendingEvent().title().contains("certain"),"confidence separate");accept(e,GameCommand.respond(0));check(e.view().campaign().pendingEvent().secondsRemaining()==3,"opponent delivery window");accept(e,GameCommand.tick(3));check(e.view().campaign().pendingEvent().secondsRemaining()==6,"short recovery window");
  Path save=Files.createTempFile("debate",".save");try{CareerSave.write(e,save);var loaded=CareerSave.read(save);check(e.view().equals(loaded.view()),"save replay midchallenge exact");accept(e,GameCommand.respond(3));accept(loaded,GameCommand.respond(3));check(e.view().equals(loaded.view()),"same recovery and source after replay");check(e.view().campaign().pendingEvent().description().contains("Source: https://"),"fact feedback sourced");}finally{Files.deleteIfExists(save);for(int i=1;i<=3;i++)Files.deleteIfExists(CareerSave.backup(save,i));}
  int guard=0;while(e.view().campaign().pendingEvent()!=null){check(++guard<50,"debate terminates");accept(e,GameCommand.respond(1));}check(e.view().campaign().turnsUsed()==4,"debate stages use no campaign turns");toDebate(e,8);accept(e,GameCommand.leaveLive());check(e.view().campaign().pendingEvent()==null,"leave during untimed prep");
  boolean truthful=false,bluff=false;Set<String> orders=new HashSet<>();
  for(int seed=0;seed<30;seed++){var memory=new ArrayList<LiveDebate.Statement>();var d=new LiveDebate("economy",seed,memory);d.respond(1);orders.add(d.event().options().toString());d.respond(0);d.respond(0);d.timeout();d.respond(3);String feedback=d.event().description();truthful|=feedback.contains("opponent's correction was accurate");bluff|=feedback.contains("opponent's correction was inaccurate");while(!d.complete())d.respond(1);check(memory.stream().anyMatch(x->x.topic().equals("Grant priorities")),"position retained");var next=new LiveDebate("record",seed,memory);next.respond(1);while(next.stage()!=LiveDebate.Stage.POLICY){if(next.seconds()>0)next.timeout();else next.respond(1);}next.respond(0);check(next.event().description().contains("In the economy debate"),"later debate quotes earlier position");}
  check(truthful&&bluff,"opponent may be right or wrong");check(orders.size()>1,"seed changes selection/order");
  var d=new LiveDebate("record",2,new ArrayList<>());d.respond(1);d.timeout();check(d.stage()==LiveDebate.Stage.FEEDBACK&&d.event().description().contains("No answer"),"timeout never invents answer");boolean invalid=false;try{d.respond(99);}catch(IllegalArgumentException ex){invalid=true;}check(invalid,"invalid response rejected");
  System.out.println("DebateTests: "+checks+" checks passed");
 }
}
