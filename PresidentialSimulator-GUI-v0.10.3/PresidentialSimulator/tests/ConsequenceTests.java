import java.util.*;
import java.nio.file.*;
public class ConsequenceTests {
 static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
 static GameEngine game(long seed){return new GameEngine(seed,President.Difficulty.NORMAL,President.RunningMate.FUNDRAISER,CampaignOpening.fresh(),true,0,true,true);}
 static List<LiveDebate.Statement> performance(boolean good){var mem=new ArrayList<LiveDebate.Statement>();var d=new LiveDebate("economy",11,mem);d.respond(d.event().options().size()-1);while(!d.complete()){
  var e=d.event();int choice=0;
  if(d.stage()==LiveDebate.Stage.ANSWER){var card=DebateFactChecks.all().stream().filter(c->e.description().contains(c.prompt())).findFirst().orElseThrow();for(int i=0;i<e.options().size();i++)if(e.options().get(i).label().equals(card.answer())==good){choice=i;break;}}
  d.respond(choice);
 }return mem;}
 public static void main(String[] args)throws Exception{
  Set<Integer> openings=new HashSet<>();for(int seed=0;seed<40;seed++){var a=game(seed);check(a.view().equals(game(seed).view()),"same seed replay");openings.add(a.view().playerEV());}check(openings.size()>10,"opening EV variety");
  var good=performance(true);var bad=performance(false);var high=game(44);var low=game(44);high.inheritDebateStatements(good);low.inheritDebateStatements(bad);check(high.credibility()>50&&low.credibility()<50,"earned reputation");check(high.view().playerEV()>low.view().playerEV(),"reputation changes map");
  var el=new SyntheticElectorate(44,0,President.Difficulty.NORMAL);for(State st:high.states()){var a=el.vote(st,Set.of(),Set.of(),true,high.reputationSwing());var b=el.vote(st,Set.of(),Set.of(),true,low.reputationSwing());check(a.share()>b.share(),"reputation changes actual ballots");}
  var career=new CareerEngine(44,President.Difficulty.NORMAL,President.RunningMate.FUNDRAISER,GameRules.CURRENT);
  int guard=0;while(career.view().campaign().turnsUsed()<4||career.view().campaign().pendingEvent()!=null){check(++guard<100,"bounded campaign");var e=career.view().campaign().pendingEvent();int choice=0;if(e!=null&&e.title().equals("Debate preparation"))choice=e.choices().size()-1;var report=career.submit(CareerCommand.campaign(e==null?GameCommand.rest():GameCommand.respond(choice)));check(report.accepted(),report.messages().toString());}
  check(career.view().campaign().activeEffects().stream().anyMatch(x->x.contains("Public credibility")),"visible reputation");Path file=Files.createTempFile("consequence-save",".properties");try{CareerSave.write(career,file);var loaded=CareerSave.read(file);check(loaded.view().campaign().equals(career.view().campaign()),"save replay including earned consequences");check(NewsFeed.headlines(loaded.view()).equals(NewsFeed.headlines(career.view())),"news replay");}finally{Files.deleteIfExists(file);}
  System.out.println("PASS: opening variation, seed replay, earned credibility, map and ballot impact, save/load, deterministic news.");
 }
}
