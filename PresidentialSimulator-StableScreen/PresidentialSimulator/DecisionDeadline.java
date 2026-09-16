import java.util.*;
/** Fictional response windows, not statutory deadlines. No inaction response authorizes a policy. */
public record DecisionDeadline(int turns,int seconds,String missed,WorldEffect effect) {
    public static DecisionDeadline world(WorldEvent e){
        int months=Set.of("storm","cyber","health","hospitals","energy","water_alert","rail_spill","wildfire","press_pool").contains(e.id())?1:2;
        WorldEffect effect=WorldEffect.of(WorldMetric.TRUST,-2,WorldMetric.CAPITAL,-2);
        String text="The request goes unanswered; trust and influence fall.";
        switch(e.id()){
            case "storm" -> {effect=WorldEffect.of(WorldMetric.TRUST,-3,WorldMetric.RESILIENCE,-5).flag("storm_damage");text="Coordination is delayed; damage opens a recovery case.";}
            case "banks" -> {effect=WorldEffect.of(WorldMetric.CONFIDENCE,-5,WorldMetric.GROWTH,-.3).flag("credit_crunch");text="Credit conditions worsen without a response.";}
            case "health" -> {effect=WorldEffect.of(WorldMetric.PREPAREDNESS,-4,WorldMetric.TRUST,-2).flag("health_strain");text="Local services face an unresolved capacity shortfall.";}
            case "ethics","leak" -> {effect=WorldEffect.of(WorldMetric.TRUST,-3,WorldMetric.SCANDAL,4);text="Unanswered questions deepen the credibility problem.";}
            case "innovation","research_grant","apprenticeships" -> {effect=WorldEffect.of(WorldMetric.CONFIDENCE,-1);text="The proposal lapses; no funds or political capital are committed.";}
            case "press_pool" -> {effect=WorldEffect.of(WorldMetric.POPULARITY,-1,WorldMetric.TRUST,-1);text="The briefing ends without an answer.";}
            default -> { }
        }
        if(e.requiresFlag()!=null)effect=effect.clear(e.requiresFlag());
        return new DecisionDeadline(months,e.id().equals("press_pool")?60:0,text,effect);
    }
    public static int campaignTurns(String id){return id.startsWith("debate_")||id.equals("live_interview")?1:2;}
    public static int campaignSeconds(String id){return id.startsWith("debate_")?60:id.equals("live_interview")?45:0;}
    public static List<CampaignEvent.Effect> campaignMissed(String id){
        if(id.startsWith("debate_")||id.equals("live_interview")||id.equals("fact_check"))return List.of(new CampaignEvent.Effect(CampaignEvent.Side.PLAYER,CampaignEvent.Kind.SUPPORT,-35,null,2));
        return List.of();
    }
    public static String campaignMissedText(String id){return campaignMissed(id).isEmpty()?"The offer expires; your campaign makes no commitment.":"The unanswered question costs 0.35 points of modeled support for two turns.";}
}
