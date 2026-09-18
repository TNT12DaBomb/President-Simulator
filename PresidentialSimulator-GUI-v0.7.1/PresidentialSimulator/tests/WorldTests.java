import java.util.*;
import java.nio.file.*;
/** Behavioral checks for causal choices, uncertainty, continuity, and immutable replay. */
public final class WorldTests {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static CareerEngine office(long seed){var e=new CareerEngine(seed,President.Difficulty.NORMAL,President.RunningMate.FUNDRAISER,GameRules.CURRENT);accepted(e,CareerCommand.dev(CareerCommand.DeveloperAction.FORCE_WIN));accepted(e,CareerCommand.advance());accepted(e,CareerCommand.advance());return e;}
    private static void accepted(CareerEngine e,CareerCommand c){var r=e.submit(c);check(r.accepted(),r.messages().toString());}
    private static void end(CareerEngine e){if(e.view().presidency().pendingEvent()!=null)accepted(e,CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.EVENT_CHOICE,1)));accepted(e,CareerCommand.office(OfficeCommand.simple(OfficeCommand.Type.END_MONTH)));}
    private static WorldSimulation event(long seed,String wanted){var w=new WorldSimulation(seed);var log=new ArrayList<String>();w.advance(true);w.trigger(1,true,log);return w.view().pending()!=null&&w.view().pending().id().equals(wanted)?w:null;}
    public static void main(String[]args)throws Exception{
        var a=office(19);var b=office(19);
        accepted(a,CareerCommand.govern(CareerCommand.GovernanceAction.PUBLIC_BRIEFING));
        accepted(b,CareerCommand.govern(CareerCommand.GovernanceAction.REST));
        check(a.view().world().get(WorldMetric.TRUST)>b.view().world().get(WorldMetric.TRUST),"A briefing changes trust");
        check(a.view().world().get(WorldMetric.ENERGY)<b.view().world().get(WorldMetric.ENERGY),"Rest versus work tradeoff");
        double trust=a.view().world().get(WorldMetric.TRUST);accepted(a,CareerCommand.govern(CareerCommand.GovernanceAction.PUBLIC_BRIEFING));check(a.view().world().get(WorldMetric.TRUST)==trust,"No same-month briefing farming");
        var before=a.view();check(!a.submit(CareerCommand.govern(CareerCommand.GovernanceAction.TRAVEL)).accepted(),"Action cap enforced");check(before.equals(a.view()),"Rejected action leaves all state unchanged");
        long seed=0;WorldSimulation transparent=null,conceal=null;
        for(;seed<10000;seed++){transparent=event(seed,"ethics");if(transparent!=null){conceal=event(seed,"ethics");break;}}
        check(transparent!=null,"Ethics event reachable");
        transparent.choose(0);conceal.choose(1);check(!transparent.view().flags().contains("concealment")&&conceal.view().flags().contains("concealment"),"Choices select different future paths");
        check(!transparent.view().scheduled().equals(conceal.view().scheduled()),"Choices schedule different consequences");
        conceal.advance(true);conceal.trigger(1,false,new ArrayList<>());check(conceal.view().pending().id().equals("leak"),"Concealment produces leak follow-up");
        var pendingView=conceal.view();boolean blocked=false;try{conceal.trigger(0,false,new ArrayList<>());}catch(IllegalArgumentException ex){blocked=true;}
        check(blocked&&pendingView.equals(conceal.view()),"Pending event not overwritten");
        conceal.choose(1);check(!conceal.view().flags().contains("concealment"),"Resolved leak closes prerequisite");
        int scheduled=conceal.view().scheduled().size();for(int i=0;i<4;i++){conceal.advance(true);conceal.trigger(0,false,new ArrayList<>());}
        check(conceal.view().scheduled().size()<scheduled,"Turning events off does not cancel consequences");check(conceal.view().pending()==null,"Events off does not draw a new story");
        // A resource-rejected response must leave queues, flags and metrics intact.
        var low=event(seed,"ethics");low.apply(WorldEffect.of(WorldMetric.CAPITAL,-100),"fixture",new ArrayList<>());var snap=low.view();blocked=false;try{low.choose(0);}catch(IllegalArgumentException ex){blocked=true;}
        check(blocked&&snap.equals(low.view()),"Unaffordable response is atomic");low.choose(1);check(low.view().pending()==null,"Free response always available");
        for(int id=0;id<WorldEventCatalog.all().size();id++)for(int response=0;response<2;response++){
            var test=new WorldSimulation(45);test.forceEvent(id,new ArrayList<>());
            test.choose(response);check(test.view().pending()==null&&test.view().scheduled().size()==1,"Every event choice schedules exactly one outcome");
            for(int m=0;m<5;m++)test.advance(true);check(test.view().scheduled().isEmpty(),"Every event delay settles");
        }
        var fixture=office(51);accepted(fixture,CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.DEV_APPROVAL,22)));check(Math.abs(fixture.view().world().approval()-22)<.00001,"Developer approval fixture");
        accepted(fixture,CareerCommand.office(OfficeCommand.policy(OfficeCommand.Type.PROPOSE,Policy.Issue.ECONOMY,Policy.Approach.EXPAND_PROGRAM)));
        accepted(fixture,CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.DEV_CAPITAL,0)));var zero=fixture.view();check(!fixture.submit(CareerCommand.office(OfficeCommand.simple(OfficeCommand.Type.NEGOTIATE))).accepted()&&zero.equals(fixture.view()),"Capital requirement atomic");
        accepted(fixture,CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.DEV_CAPITAL,100)));accepted(fixture,CareerCommand.office(OfficeCommand.simple(OfficeCommand.Type.NEGOTIATE)));check(fixture.view().world().get(WorldMetric.CAPITAL)==92,"Negotiation consumes 8 capital");
        var delivered=new WorldSimulation(88);delivered.delivered(Policy.Issue.HEALTHCARE,Policy.Approach.EXPAND_PROGRAM,new ArrayList<>());double readiness=delivered.get(WorldMetric.PREPAREDNESS);delivered.delivered(Policy.Issue.HEALTHCARE,Policy.Approach.REORGANIZE_PROGRAM,new ArrayList<>());check(readiness==delivered.get(WorldMetric.PREPAREDNESS),"Repeat policy delivery cannot farm metrics");
        var promised=new WorldSimulation(4);double priorTrust=promised.get(WorldMetric.TRUST);promised.law(Policy.Issue.TAXES,Policy.Approach.REORGANIZE_PROGRAM,Policy.Approach.EXPAND_PROGRAM,new ArrayList<>());check(promised.get(WorldMetric.TRUST)<priorTrust,"Broken promise hurts trust");
        var popular=new WorldSimulation(4);var unpopular=new WorldSimulation(4);popular.apply(WorldEffect.of(WorldMetric.PARTY_APPROVAL,15,WorldMetric.INDEPENDENT_APPROVAL,15,WorldMetric.OPPOSITION_APPROVAL,15),"fixture",new ArrayList<>());unpopular.apply(WorldEffect.of(WorldMetric.PARTY_APPROVAL,-15,WorldMetric.INDEPENDENT_APPROVAL,-15,WorldMetric.OPPOSITION_APPROVAL,-15),"fixture",new ArrayList<>());
        var strong=new MidtermCampaign(GameRules.CURRENT);var weak=new MidtermCampaign(GameRules.CURRENT);strong.resolveWorld(popular.electionMood(),new Random(17),new ArrayList<>());strong.resolve();weak.resolveWorld(unpopular.electionMood(),new Random(17),new ArrayList<>());weak.resolve();check(strong.houseSeats()>=weak.houseSeats()&&strong.senateSeats()>=weak.senateSeats(),"Same-seed midterms respond monotonically to approval");
        // Bounds and deterministic independent worlds through long economic trajectories.
        WorldSimulation x=new WorldSimulation(100),y=new WorldSimulation(100);
        for(int m=0;m<150;m++){check(x.advance(true).equals(y.advance(true)),"Seeded monthly reports match");for(var metric:WorldMetric.values())check(Double.isFinite(x.get(metric))&&x.get(metric)>=metric.min&&x.get(metric)<=metric.max,"Metric bounds "+metric);}
        check(x.view().equals(y.view()),"Identical history and values");
        boolean immutable=false;try{x.view().metrics().put(WorldMetric.TRUST,0.0);}catch(UnsupportedOperationException ex){immutable=true;}check(immutable,"Metric snapshots immutable");
        // Governing record changes the same synthetic electorate without altering seed.
        State state=new ElectoralCollege(9).getStates()[0];var favorable=new SyntheticElectorate(90,4,President.Difficulty.NORMAL);var poor=new SyntheticElectorate(90,-4,President.Difficulty.NORMAL);
        check(favorable.vote(state,Set.of(),Set.of(),true).share()>poor.vote(state,Set.of(),Set.of(),true).share(),"Record affects votes");
        check(favorable.vote(state,Set.of(State.Task.TOWN_HALL),Set.of(),true).share()>favorable.vote(state,Set.of(),Set.of(),true).share(),"Campaign action affects vote share");
        boolean split=false;for(long n=0;n<100;n++){var college=new ElectoralCollege(n);var model=new SyntheticElectorate(n,0,President.Difficulty.NORMAL);for(State st:college.getAllStates()){var v=model.vote(st,Set.of(),Set.of(),true);check(v.player()+v.opponent()<=v.eligible(),"Votes bounded by eligible electorate");if((st.getName().equals("Maine")||st.getName().equals("Nebraska"))&&v.playerEV()>0&&v.playerEV()<v.totalEV())split=true;}}
        check(split,"Maine/Nebraska split allocation reachable");
        int wins=0,losses=0;for(long n=0;n<60;n++){GameEngine g=new GameEngine(n,President.Difficulty.NORMAL,President.RunningMate.FUNDRAISER,CampaignOpening.fresh(),true,0);int guard=0;while(!g.view().complete()&&guard++<100){var r=g.submit(g.view().pendingEvent()==null?GameCommand.rest():GameCommand.respond(1));check(r.accepted(),"Campaign advance");}var result=g.result();if(result.playerWon())wins++;else losses++;check(result.getPlayerEV()+result.getOpponentEV()==538,"Electors sum to 538");check(result.getVotes().size()==51,"All states plus DC accounted");check(g.view().playerEV()==result.getPlayerEV(),"Final dashboard equals split EV totals");check(result.playerPopularVotes()+result.opponentPopularVotes()<=result.eligibleVoters(),"Popular totals conserved");}
        check(wins>0&&losses>0,"Neither unconditional win nor unconditional loss");
        // Save with a pending world event; replay both branches and future RNG identically.
        var career=office(717);accepted(career,CareerCommand.dev(CareerCommand.DeveloperAction.ADVANCE_MONTH));if(career.view().presidency().pendingEvent()==null)accepted(career,CareerCommand.dev(CareerCommand.DeveloperAction.TRIGGER_EVENT));
        Path file=Files.createTempFile("world-replay",".save");
        try{CareerSave.write(career,file);var loaded=CareerSave.read(file);check(career.view().equals(loaded.view()),"Pending world snapshot replay exact");for(int i=0;i<20;i++){end(career);end(loaded);check(career.view().equals(loaded.view()),"Future randomness replays identically");}}
        finally{Files.deleteIfExists(file);for(int i=1;i<=3;i++)Files.deleteIfExists(CareerSave.backup(file,i));}
        // Full two-term path; delayed effects and world time survive the inauguration.
        accepted(career,CareerCommand.dev(CareerCommand.DeveloperAction.FINISH_TERM));double debt=career.view().world().get(WorldMetric.DEBT);int clock=career.view().world().month();
        accepted(career,CareerCommand.runAgain());accepted(career,CareerCommand.dev(CareerCommand.DeveloperAction.FORCE_WIN));accepted(career,CareerCommand.advance());accepted(career,CareerCommand.advance());
        check(career.view().world().month()==clock&&career.view().world().get(WorldMetric.DEBT)==debt,"World not reset at second inauguration");
        accepted(career,CareerCommand.dev(CareerCommand.DeveloperAction.FINISH_TERM));check(career.view().phase()==CareerView.Phase.RETIRED&&career.view().servedMonths()==96,"Two-term limit retained");check(career.view().world().month()==96,"World clock spans both terms");
        var defeat=new CareerEngine(14,President.Difficulty.NORMAL,President.RunningMate.FUNDRAISER,GameRules.CURRENT);double reputation=defeat.view().world().get(WorldMetric.REPUTATION);accepted(defeat,CareerCommand.dev(CareerCommand.DeveloperAction.FORCE_LOSS));check(defeat.view().world().get(WorldMetric.REPUTATION)<reputation,"Loss changes reputation");accepted(defeat,CareerCommand.advance());accepted(defeat,CareerCommand.rebuild(CareerCommand.RebuildAction.COMMUNITY_WORK));check(defeat.view().world().month()==12,"World continues outside office");
        System.out.println("WorldTests: "+checks+" checks passed; sampled campaign outcomes "+wins+" wins / "+losses+" losses.");
    }
}
