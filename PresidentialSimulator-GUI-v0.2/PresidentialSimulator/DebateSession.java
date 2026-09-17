import java.util.*;
/** Three authored questions. Scoring is a transparent fictional model, not a forecast. */
public final class DebateSession {
    public record Round(int number,String topic,String answer,String opponent,int playerScore,int opponentScore,boolean missed){}
    public record Result(String topic,List<Round> rounds,double lastingShare){public Result{rounds=List.copyOf(rounds);}}
    private final String topic;
    private final int style;
    private final List<Round> rounds=new ArrayList<>();
    public DebateSession(String topic,long seed){this.topic=topic;style=Math.floorMod(Long.hashCode(seed),3);}
    public static boolean handles(String id){return id.startsWith("debate_round_");}
    public static List<CampaignEvent> catalog(){var result=new ArrayList<CampaignEvent>();for(String t:List.of("economy","record","security"))for(int r=0;r<3;r++)result.add(event(t,r));return List.copyOf(result);}
    public CampaignEvent question(){var e=event(topic,rounds.size());return new CampaignEvent(e.id(),e.title(),e.description()+" Opponent style: "+opponentStyle()+".",e.category(),e.effects(),e.options());}
    public boolean complete(){return rounds.size()==3;}
    public String rootId(){return "debate_"+topic;}
    private static String title(String t){return switch(t){case "economy"->"Household costs";case "record"->"Your record";default->"Foreign affairs";};}
    public String opponentStyle(){return List.of("policy-focused","combative","reassuring").get(style);}
    public Round answer(int choice){
        if(complete())throw new IllegalStateException("Debate complete");
        int index=rounds.size();int opposing=List.of(3,2,2).get(style);
        if(index==1&&style==1)opposing=4;
        if(index==2&&style==2)opposing=4;
        int score=choice<0?-2:choice==0?4:2;
        String response=choice<0?"No answer":question().options().get(choice).label();
        String opponent=List.of("The opponent cites a specific policy mechanism.","The opponent challenges your consistency.","The opponent stresses stability and shared concerns.").get(style);
        Round r=new Round(index+1,title(topic),response,opponent,score,opposing,choice<0);rounds.add(r);return r;
    }
    public Result result(){if(!complete())throw new IllegalStateException("Debate incomplete");int margin=rounds.stream().mapToInt(r->r.playerScore()-r.opponentScore()).sum();return new Result(title(topic),rounds,Math.max(-.30,Math.min(.30,margin*.025)));}
    private static CampaignEvent event(String topic,int round){
        String[][] prompts={
            {"Prices are straining households. What can your administration realistically do?","How would you pay for the policy without hiding its tradeoffs?","A moderator challenges a statistic in your opening answer. How do you respond?"},
            {"Which documented result shows that you can deliver your promises?","Your opponent points to an unmet commitment. How do you explain it?","What would you change if your preferred policy failed to deliver?"},
            {"A fictional regional confrontation is escalating. What is your first step?","An ally wants a commitment before consultations are complete. How do you respond?","What limit would you place on the response, and how would you explain the risk?"}};
        int t=topic.equals("economy")?0:topic.equals("record")?1:2;
        String[][] detailed={
            {"Explain targeted relief and its limits","Name the funding source and tradeoffs","Correct the statistic and cite its source"},
            {"Cite a documented result and its limits","Explain the shortfall and a revised milestone","Set a review date and conditions for changing course"},
            {"Explain consultations and a diplomatic sequence","Condition the commitment on consultations","State the limits and a review trigger"}};
        String[][] concise={
            {"Focus on household experiences","Emphasize restraint without a funding breakdown","Commit to publishing the source afterward"},
            {"Emphasize your broader priorities","Acknowledge the gap and focus on future goals","Promise to listen and adapt"},
            {"Emphasize stability and restraint","Reassure the ally without a firm commitment","Commit to explaining the decision afterward"}};
        return new CampaignEvent("debate_round_"+topic+"_"+(round+1),title(topic)+" | Question "+(round+1)+"/3",prompts[t][round],CampaignEvent.Category.OPPORTUNITY,List.of(),List.of(
            new CampaignEvent.Option(detailed[t][round],List.of(new CampaignEvent.Effect(CampaignEvent.Side.PLAYER,CampaignEvent.Kind.FUNDS,-50,null,0))),
            new CampaignEvent.Option(concise[t][round],List.of(new CampaignEvent.Effect(CampaignEvent.Side.PLAYER,CampaignEvent.Kind.FUNDRAISING,15,null,2)))));
    }
}
