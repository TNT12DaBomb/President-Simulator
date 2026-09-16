import java.nio.file.*;
public final class DebateTests {
 private static int checks;
 private static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 private static CareerEngine game(){return new CareerEngine(71,President.Difficulty.NORMAL,President.RunningMate.FUNDRAISER,GameRules.CURRENT);}
 private static void accept(CareerEngine e,GameCommand c){var r=e.submit(CareerCommand.campaign(c));check(r.accepted(),r.messages().toString());}
 private static void toDebate(CareerEngine e,int turn){while(e.view().campaign().turnsUsed()<turn){if(e.view().campaign().pendingEvent()!=null)accept(e,GameCommand.respond(1));else accept(e,GameCommand.rest());}}
 public static void main(String[]args)throws Exception{
  var e=game();toDebate(e,4);check(e.view().campaign().pendingEvent().title().contains("1/3"),"First of three questions");var initial=e.view();check(!e.submit(CareerCommand.campaign(GameCommand.rest())).accepted()&&initial.equals(e.view()),"Cannot bypass live debate with campaign action");
  accept(e,GameCommand.respond(0));check(e.view().campaign().pendingEvent().title().contains("2/3"),"Second question");check(e.view().campaign().turnsUsed()==4,"Answer uses no game turn");accept(e,GameCommand.tick(60));check(e.view().campaign().pendingEvent().title().contains("3/3")&&e.view().campaign().pendingEvent().secondsRemaining()==60,"Timeout advances with fresh clock");
  Path save=Files.createTempFile("debate",".save");try{CareerSave.write(e,save);var loaded=CareerSave.read(save);check(e.view().equals(loaded.view()),"Round and timer save replay exact");accept(e,GameCommand.respond(1));accept(loaded,GameCommand.respond(1));check(e.view().equals(loaded.view()),"Result replay exact");}finally{Files.deleteIfExists(save);for(int i=1;i<=3;i++)Files.deleteIfExists(CareerSave.backup(save,i));}
  var result=e.view().campaign().debateResults().get(0);check(result.rounds().size()==3&&result.rounds().get(1).missed(),"All answers recorded including timeout");double effect=e.view().campaign().debateShare();toDebate(e,8);check(effect==e.view().campaign().debateShare(),"Effect outlasts temporary campaign modifiers");accept(e,GameCommand.leaveLive());check(e.view().campaign().pendingEvent()==null&&e.view().campaign().debateResults().size()==2,"Leaving skips remaining rounds");check(e.view().campaign().debateResults().get(1).rounds().stream().allMatch(DebateSession.Round::missed),"All skipped answers marked missed");
  toDebate(e,12);for(int i=0;i<3;i++)accept(e,GameCommand.respond(0));double finalEffect=e.view().campaign().debateShare();toDebate(e,16);check(e.view().campaign().complete()&&e.view().campaign().debateShare()==finalEffect,"All three debates affect election-day model");check(Math.abs(finalEffect)<=.900001,"Lifetime campaign debate shift bounded");
  var good=game();var poor=game();toDebate(good,4);toDebate(poor,4);for(int i=0;i<3;i++){accept(good,GameCommand.respond(0));accept(poor,GameCommand.tick(60));}check(good.view().campaign().debateShare()>poor.view().campaign().debateShare(),"Same opponent; answers improve record");
  for(var r:good.view().campaign().debateResults())check(!r.rounds().get(0).opponent().isBlank(),"Opponent response recorded");
  boolean immutable=false;try{good.view().campaign().debateResults().clear();}catch(UnsupportedOperationException ex){immutable=true;}check(immutable,"GUI snapshot immutable");
  System.out.println("DebateTests: "+checks+" checks passed.");
 }
}
