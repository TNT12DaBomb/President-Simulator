import java.util.*;
/** Short presentation labels. Full model effects remain available in the detail view. */
public final class DecisionText {
 private DecisionText(){}
 public static String metric(WorldMetric m){return switch(m){
  case PARTY_APPROVAL->"Party approval";case INDEPENDENT_APPROVAL->"Ind. approval";case OPPOSITION_APPROVAL->"Opp. approval";
  case POPULARITY->"Popularity";case TRUST->"Trust";case UNITY->"Unity";case DONORS->"Donors";case REPUTATION->"Reputation";
  case CAPITAL->"Capital";case ENERGY->"Energy";case PREPAREDNESS->"Readiness";case RESILIENCE->"Resilience";
  case TENSION->"Tension";case SCANDAL->"Ethics risk";case CONFIDENCE->"Confidence";case GROWTH->"Growth";
  case UNEMPLOYMENT->"Unemployment";case INFLATION->"Inflation";case RATE->"Policy rate";case DEFICIT->"Deficit/GDP";case DEBT->"Debt/GDP";};}
 public static String shortEffects(WorldEffect e){
  List<String> values=e.changes().entrySet().stream().sorted(Comparator.<Map.Entry<WorldMetric,Double>>comparingDouble(x->-Math.abs(x.getValue())).thenComparing(x->x.getKey().ordinal())).limit(2).map(x->metric(x.getKey())+String.format(Locale.ROOT," %+.1f",x.getValue())).toList();
  return values.isEmpty()?"No immediate metric change":String.join(" | ",values)+(e.changes().size()>2?" | more in details":"");
 }
 public static String headline(CareerCommand c){return switch(c.type()){
  case GOVERN->c.governance()+" completed.";
  case CAMPAIGN->switch(c.campaign().type()){case CAMPAIGN->"Campaign visit completed.";case FUNDRAISE->"Fundraising completed.";case REST->"Campaign turn advanced.";case RESPOND->"Response recorded.";case CLOCK_TICK->"Response clock updated.";case LEAVE_LIVE->"Live appearance ended without an answer.";};
  case OFFICE->switch(c.office().type()){case END_MONTH->"Month closed.";case EVENT_CHOICE->"Response recorded; follow-up scheduled.";case SIGN->"Bill signed; delivery scheduled.";case PROPOSE->"Proposal introduced.";case REALTIME_MODE->"Response-clock preference saved.";default->c.office().type().toString().replace('_',' ')+" completed.";};
  default->"Career updated.";};}
 public static String change(CareerView before,CareerView after){
  if(before.world()!=null&&after.world()!=null){double approval=after.world().approval()-before.world().approval();var delta=new EnumMap<WorldMetric,Double>(WorldMetric.class);for(var m:WorldMetric.values()){double d=after.world().get(m)-before.world().get(m);if(Math.abs(d)>=.05)delta.put(m,d);}return String.format(Locale.ROOT,"Approval %+.1f | ",approval)+shortEffects(new WorldEffect(delta,Set.of(),Set.of()));}
  return "Campaign cash $"+after.campaign().playerFunds()+" | "+after.campaign().turnsUsed()+"/"+after.campaign().totalTurns()+" turns";
 }
}
