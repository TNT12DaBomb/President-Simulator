import java.util.List;
/** Fictional scenarios, not forecasts. Each branch has benefits, costs, and explicit delayed risk. */
public final class WorldEventCatalog {
    private WorldEventCatalog(){}
    private static WorldEffect e(Object... p){return WorldEffect.of(p);}
    private static WorldEvent.Choice c(String label,int cost,WorldEffect now,int delay,double chance,WorldEffect good,WorldEffect bad,String note){return new WorldEvent.Choice(label,cost,now,delay,chance,good,bad,note);}
    private static WorldEvent event(String id,String title,String description,int weight,String flag,WorldMetric metric,double below,WorldEvent.Choice a,WorldEvent.Choice b){return new WorldEvent(id,title,description,weight,1,flag,metric,below,List.of(a,b));}
    public static List<WorldEvent> all(){return List.of(
      event("storm","Major hurricane forecast","Governors request federal coordination. Preparation can reduce damage but requires scarce political attention.",5,null,null,0,
       c("Coordinate early emergency assistance",12,e(WorldMetric.PREPAREDNESS,8,WorldMetric.DEFICIT,.2),2,.85,e(WorldMetric.TRUST,3,WorldMetric.INDEPENDENT_APPROVAL,2,WorldMetric.RESILIENCE,3),e(WorldMetric.GROWTH,-.3,WorldMetric.TRUST,-1),"Preparation limits, but does not eliminate, damage."),
       c("Keep the current posture; reserve resources",0,e(WorldMetric.CAPITAL,3),2,.35,e(WorldMetric.CONFIDENCE,1),e(WorldMetric.RESILIENCE,-10,WorldMetric.TRUST,-5,WorldMetric.GROWTH,-.5).flag("storm_damage"),"Severe damage opens a recovery decision.")),
      event("recovery","Storm recovery backlog","Local services remain disrupted after the earlier hurricane response.",12,"storm_damage",null,0,
       c("Fund a monitored rebuilding program",10,e(WorldMetric.DEFICIT,.3,WorldMetric.TRUST,2).clear("storm_damage"),3,.8,e(WorldMetric.RESILIENCE,12,WorldMetric.CONFIDENCE,4),e(WorldMetric.SCANDAL,3),"Oversight reduces procurement risk."),
       c("Delay new funding; use existing capacity",0,e(WorldMetric.DEFICIT,-.1).clear("storm_damage"),3,1,e(WorldMetric.TRUST,-4,WorldMetric.UNEMPLOYMENT,.2,WorldMetric.RESILIENCE,-4),e(),"Damage becomes a continuing economic drag.")),
      event("banks","Regional banking stress","Deposit flight threatens credit availability. Regulators request coordination; no intervention guarantees recovery.",4,null,WorldMetric.CONFIDENCE,65,
       c("Coordinate conditional stabilization with regulators",15,e(WorldMetric.DEFICIT,.4,WorldMetric.DONORS,3,WorldMetric.PARTY_APPROVAL,-2),3,.8,e(WorldMetric.CONFIDENCE,6,WorldMetric.GROWTH,.4),e(WorldMetric.GROWTH,-.6,WorldMetric.UNEMPLOYMENT,.3),"Support has fiscal cost and a moral-hazard tradeoff."),
       c("Use ordinary resolution; avoid exceptional support",0,e(WorldMetric.TRUST,1,WorldMetric.DONORS,-4),2,.45,e(WorldMetric.CONFIDENCE,2),e(WorldMetric.GROWTH,-1,WorldMetric.UNEMPLOYMENT,.5,WorldMetric.CONFIDENCE,-8).flag("credit_crunch"),"A credit crunch can require a later jobs response.")),
      event("jobs","Credit crunch reaches employers","The earlier banking decision is now affecting payrolls and hiring.",12,"credit_crunch",null,0,
       c("Deploy authorized workforce assistance",12,e(WorldMetric.DEFICIT,.4).clear("credit_crunch"),3,.75,e(WorldMetric.UNEMPLOYMENT,-.3,WorldMetric.GROWTH,.4,WorldMetric.INFLATION,.15),e(WorldMetric.DEFICIT,.1),"Authorized temporary assistance may add inflation pressure."),
       c("Allow adjustment without a new package",0,e(WorldMetric.DEFICIT,-.1).clear("credit_crunch"),3,.5,e(WorldMetric.GROWTH,.3),e(WorldMetric.UNEMPLOYMENT,.4,WorldMetric.INDEPENDENT_APPROVAL,-3),"Recovery is uncertain without intervention.")),
      event("cyber","Federal service cyberattack","Agency systems are disrupted. Protecting services and explaining the breach compete for attention.",4,null,WorldMetric.RESILIENCE,80,
       c("Disclose the breach and fund containment",10,e(WorldMetric.TRUST,2,WorldMetric.RESILIENCE,4,WorldMetric.DEFICIT,.1),2,.8,e(WorldMetric.RESILIENCE,7,WorldMetric.CONFIDENCE,2),e(WorldMetric.TRUST,-2),"Disclosure can still reveal deeper failures."),
       c("Limit disclosure until an internal review finishes",0,e(WorldMetric.CAPITAL,2),2,.35,e(WorldMetric.RESILIENCE,2),e(WorldMetric.TRUST,-6,WorldMetric.SCANDAL,8).flag("concealment"),"An uncovered omission may trigger a records leak.")),
      event("ethics","Procurement conflict alleged","Investigators identify a possible conflict involving an administration official.",3,null,null,0,
       c("Cooperate with independent review",8,e(WorldMetric.SCANDAL,4,WorldMetric.PARTY_APPROVAL,-1),3,.85,e(WorldMetric.SCANDAL,-6,WorldMetric.TRUST,5,WorldMetric.REPUTATION,3),e(WorldMetric.SCANDAL,4,WorldMetric.TRUST,-2),"Cooperation cannot predetermine the evidence."),
       c("Defend the official and restrict disclosure",0,e(WorldMetric.UNITY,3,WorldMetric.SCANDAL,5).flag("concealment"),2,1,e(WorldMetric.TRUST,-2),e(),"Restricted disclosure opens a later leak investigation.")),
      event("leak","Internal records become public","An earlier disclosure decision is contradicted by newly published records.",15,"concealment",null,0,
       c("Correct the record and accept independent oversight",15,e(WorldMetric.TRUST,-3,WorldMetric.SCANDAL,4).clear("concealment"),3,.85,e(WorldMetric.TRUST,4,WorldMetric.SCANDAL,-8),e(WorldMetric.SCANDAL,3),"Trust recovers only if follow-through succeeds."),
       c("Contest the records and keep the existing position",0,e(WorldMetric.UNITY,2,WorldMetric.TRUST,-5,WorldMetric.SCANDAL,10).clear("concealment"),3,.25,e(WorldMetric.SCANDAL,-2),e(WorldMetric.REPUTATION,-6,WorldMetric.INDEPENDENT_APPROVAL,-5),"Base solidarity can come at a broader credibility cost.")),
      event("health","New regional outbreak","Public-health officials report uncertainty about transmission and severity.",4,null,WorldMetric.PREPAREDNESS,85,
       c("Expand surveillance and coordinate local guidance",10,e(WorldMetric.DEFICIT,.2,WorldMetric.PREPAREDNESS,5),3,.8,e(WorldMetric.TRUST,3,WorldMetric.UNEMPLOYMENT,-.1),e(WorldMetric.GROWTH,-.3),"Preparedness improves the response, not certainty."),
       c("Maintain existing capacity while gathering evidence",0,e(WorldMetric.CONFIDENCE,1),2,.4,e(WorldMetric.CAPITAL,2),e(WorldMetric.GROWTH,-.5,WorldMetric.TRUST,-4).flag("health_strain"),"An adverse outcome can overload local services.")),
      event("hospitals","Hospitals request support","The earlier outbreak has strained local capacity.",12,"health_strain",null,0,
       c("Coordinate surge support",12,e(WorldMetric.DEFICIT,.3,WorldMetric.PREPAREDNESS,6).clear("health_strain"),2,.8,e(WorldMetric.TRUST,4,WorldMetric.CONFIDENCE,3),e(WorldMetric.TRUST,-1),"Some capacity constraints may remain."),
       c("Prioritize normal operations",0,e(WorldMetric.CAPITAL,2).clear("health_strain"),2,1,e(WorldMetric.TRUST,-5,WorldMetric.GROWTH,-.4,WorldMetric.POPULARITY,-3),e(),"The unmet response remains part of your record.")),
      event("diplomacy","Border confrontation abroad","Two fictional neighboring countries request different forms of US support.",4,null,null,0,
       c("Pursue multilateral mediation",10,e(WorldMetric.TENSION,-3,WorldMetric.PARTY_APPROVAL,-1),3,.7,e(WorldMetric.TENSION,-8,WorldMetric.REPUTATION,3),e(WorldMetric.TENSION,5),"Diplomacy can fail despite a serious effort."),
       c("Issue a unilateral warning",0,e(WorldMetric.PARTY_APPROVAL,2,WorldMetric.TENSION,6),2,.4,e(WorldMetric.TENSION,-4),e(WorldMetric.TENSION,10,WorldMetric.CONFIDENCE,-3).flag("standoff"),"Escalation may create another diplomatic decision.")),
      event("standoff","Diplomatic standoff hardens","The earlier warning did not settle the dispute.",12,"standoff",null,0,
       c("Open a back channel",12,e(WorldMetric.UNITY,-2).clear("standoff"),2,.75,e(WorldMetric.TENSION,-12,WorldMetric.CONFIDENCE,3),e(WorldMetric.TENSION,4),"De-escalation may frustrate hardliners."),
       c("Maintain pressure",0,e(WorldMetric.TENSION,4,WorldMetric.DEFICIT,.2).clear("standoff"),2,.4,e(WorldMetric.TENSION,-5),e(WorldMetric.TENSION,8,WorldMetric.GROWTH,-.2),"This is a diplomatic abstraction; no war is automatically declared.")),
      event("protest","Large accountability demonstrations","Protest organizers request a public meeting and independent review.",4,null,WorldMetric.TRUST,55,
       c("Meet organizers and publish a review timetable",8,e(WorldMetric.TRUST,3,WorldMetric.UNITY,-2),2,.8,e(WorldMetric.INDEPENDENT_APPROVAL,3,WorldMetric.POPULARITY,2),e(WorldMetric.TRUST,-2),"Party allies may dislike concessions."),
       c("Decline a meeting; keep the current agenda",0,e(WorldMetric.UNITY,2),2,1,e(WorldMetric.TRUST,-3,WorldMetric.POPULARITY,-2),e(),"Dismissed concerns remain visible.")),
      event("energy","International energy supply disruption","Prices rise after a supply interruption outside presidential control.",4,null,null,0,
       c("Coordinate a temporary supply response",10,e(WorldMetric.INFLATION,.15,WorldMetric.DEFICIT,.1),2,.75,e(WorldMetric.INFLATION,-.2,WorldMetric.CONFIDENCE,2),e(WorldMetric.INFLATION,.3),"Limited relief cannot control global prices."),
       c("Allow prices to adjust; preserve reserves",0,e(WorldMetric.INFLATION,.4,WorldMetric.CONFIDENCE,-3),3,.6,e(WorldMetric.INFLATION,-.2),e(WorldMetric.INFLATION,.2,WorldMetric.GROWTH,-.2),"Preserving capacity shifts more near-term cost onto households.")),
      event("innovation","Research partnership opportunity","Universities propose a competitively awarded pilot program.",3,null,null,0,
       c("Fund a transparent pilot with evaluation",8,e(WorldMetric.DEFICIT,.15,WorldMetric.DONORS,-1),4,.65,e(WorldMetric.GROWTH,.3,WorldMetric.REPUTATION,2,WorldMetric.CONFIDENCE,3),e(WorldMetric.TRUST,-1),"Innovation is uncertain and benefits arrive slowly."),
       c("Defer funding and preserve agenda space",0,e(WorldMetric.CAPITAL,3,WorldMetric.DEFICIT,-.05),4,1,e(WorldMetric.CONFIDENCE,-1),e(),"No research benefit is credited."))
    );}
}
