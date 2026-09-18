import java.util.*;
/** Fictional two-candidate electorate. No actual demographic or partisan data is implied. */
public final class SyntheticElectorate {
    public record Vote(long eligible,long player,long opponent,int playerEV,int totalEV,List<String> districts){
        public Vote {districts=List.copyOf(districts);if(eligible<1||player<0||opponent<0||player+opponent>eligible||playerEV<0||playerEV>totalEV)throw new IllegalArgumentException("Invalid vote accounting");}
        public double share(){return 100.0*player/(player+opponent);}
        public double turnout(){return 100.0*(player+opponent)/eligible;}
    }
    private static long mix(long x){x=(x^(x>>>30))*0xbf58476d1ce4e5b9L;x=(x^(x>>>27))*0x94d049bb133111ebL;return x^(x>>>31);}
    private final long seed;private final double recordSwing;private final President.Difficulty difficulty;
    public SyntheticElectorate(long seed,double recordSwing,President.Difficulty difficulty){this.seed=seed;this.recordSwing=Math.max(-6,Math.min(6,recordSwing));this.difficulty=difficulty;}
    public Vote vote(State state,Set<State.Task> yours,Set<State.Task> theirs,boolean election){
        return vote(state,yours,theirs,election,0);
    }
    public Vote vote(State state,Set<State.Task> yours,Set<State.Task> theirs,boolean election,double campaignSwing){
        Random geography=new Random(mix(seed ^ (state.getName().hashCode()*0x9E3779B97F4A7C15L))); // reproducible campaign geography
        double lean=(geography.nextDouble()-.5)*16;
        double local=(yours.size()-theirs.size())*2.8;
        double national=election?(new Random(seed^0x987aL).nextDouble()-.5)*4:0;
        int districts=state.getName().equals("Maine")?2:state.getName().equals("Nebraska")?3:1;
        long eligible=(state.getName().equals("District of Columbia")?2:Math.max(1,state.getElectoralVotes()-2))*300000L;
        long player=0,opponent=0;int districtEV=0;List<String> detail=new ArrayList<>();
        for(int d=0;d<districts;d++){
            Random noise=new Random(seed ^ (state.getName().hashCode()*31L+d*7919L));
            double districtLean=districts==1?0:(d-(districts-1)/2.0)*5;
            double share=Math.max(.08,Math.min(.92,(50+lean+districtLean+local+recordSwing+campaignSwing+(difficulty==President.Difficulty.HARD?-2:0)+national+(election?(noise.nextDouble()-.5)*4:0))/100));
            double turnout=Math.max(.35,Math.min(.85,.60+(yours.contains(State.Task.FIELD_OFFICE)?.02:0)+(theirs.contains(State.Task.FIELD_OFFICE)?.02:0)+(election?(noise.nextDouble()-.5)*.06:0)));
            long people=eligible/districts+(d<eligible%districts?1:0);long ballots=Math.round(people*turnout),pv=Math.round(ballots*share);
            // Exact unit ties use a deterministic fictional recount tiebreak; state-specific legal procedures remain deferred.
            if(pv*2==ballots && pv>0)pv--;
            long ov=ballots-pv;player+=pv;opponent+=ov;
            if(pv>ov)districtEV++;
            if(districts>1)detail.add("District "+(d+1)+": "+pv+" / "+ov+" votes; 1 EV to "+(pv>ov?"you":"opponent"));
        }
        int ev=districts==1?(player>opponent?state.getElectoralVotes():0):(player>opponent?2:0)+districtEV;
        return new Vote(eligible,player,opponent,ev,state.getElectoralVotes(),detail);
    }
    public String explanation(State s,Set<State.Task> yours,Set<State.Task> theirs,boolean election){
        return explanation(s,yours,theirs,election,0);
    }
    public String explanation(State s,Set<State.Task> yours,Set<State.Task> theirs,boolean election,double campaignSwing){
        Vote v=vote(s,yours,theirs,election,campaignSwing);
        return String.format(Locale.ROOT,"Synthetic %s: %.2f%% vote share; %.1f%% turnout; you %d/%d EV. Local campaign shift %+.1f pp; governing-record shift %+.1f pp. %s",election?"result":"projection",v.share(),v.turnout(),v.playerEV(),v.totalEV(),(yours.size()-theirs.size())*2.8,recordSwing,String.join("; ",v.districts())) + (campaignSwing==0?"":String.format(Locale.ROOT," News/debate shift %+.2f pp.",campaignSwing));
    }
}
