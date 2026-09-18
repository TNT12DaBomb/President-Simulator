import java.util.*;
/** Seeded, UI-independent world state. Coefficients are transparent authored game parameters. */
public final class WorldSimulation {
    public record Point(int month,double approval,double popularity,double growth,double unemployment,double inflation){}
    public record Scheduled(int dueMonth,String reason,double probability,WorldEffect success,WorldEffect failure){}
    public record View(int month,Map<WorldMetric,Double> metrics,double approval,double change,double high,double low,
                       List<Point> trend,List<String> explanations,List<Scheduled> scheduled,Set<String> flags,WorldEvent pending,int dueMonth,int secondsRemaining,String missedResponse){
        public View {metrics=Collections.unmodifiableMap(new EnumMap<>(metrics));trend=List.copyOf(trend);explanations=List.copyOf(explanations);scheduled=List.copyOf(scheduled);flags=Set.copyOf(flags);}
        public double get(WorldMetric metric){return metrics.get(metric);}
    }
    private final EnumMap<WorldMetric,Double> values=new EnumMap<>(WorldMetric.class);
    private final Set<String> flags=new TreeSet<>();
    private final List<Scheduled> scheduled=new ArrayList<>();
    private final List<Point> trend=new ArrayList<>();
    private final List<String> explanations=new ArrayList<>();
    private final Map<String,Integer> lastEvents=new HashMap<>();
    private final EnumSet<CareerCommand.GovernanceAction> actions=EnumSet.noneOf(CareerCommand.GovernanceAction.class);
    private final Random economyRandom,eventRandom,outcomeRandom;
    private final Set<Policy.Issue> creditedDelivery=EnumSet.noneOf(Policy.Issue.class);
    private WorldEvent pending;private int month;
    private final boolean timedDecisions; private int pendingDue,pendingSeconds;private double high,low;
    public WorldSimulation(long seed){this(seed,false);}
    public WorldSimulation(long seed,boolean timedDecisions){this.timedDecisions=timedDecisions;for(var m:WorldMetric.values())values.put(m,m.initial);economyRandom=new Random(seed^0x173fe31L);eventRandom=new Random(seed^0x1e71a9L);outcomeRandom=new Random(seed^0x33aceL);high=low=approval();recordPoint();}
    public double get(WorldMetric m){return values.get(m);}
    public double approval(){return get(WorldMetric.PARTY_APPROVAL)*.35+get(WorldMetric.INDEPENDENT_APPROVAL)*.30+get(WorldMetric.OPPOSITION_APPROVAL)*.35;}
    public View view(){return new View(month,values,approval(),trend.get(trend.size()-1).approval()-trend.get(Math.max(0,trend.size()-2)).approval(),high,low,trend,explanations.subList(Math.max(0,explanations.size()-40),explanations.size()),scheduled,flags,pending,pendingDue,pendingSeconds,pending!=null&&timedDecisions?DecisionDeadline.world(pending).missed():"");}
    private static double clamp(double value,double min,double max){return Math.max(min,Math.min(max,value));}
    private void note(String text,List<String> log){String line="WORLD month "+month+": "+text;explanations.add(line);log.add(line);}
    public void apply(WorldEffect effect,String reason,List<String> log){
        var changes=new ArrayList<String>();
        effect.changes().forEach((m,d)->{double old=get(m),next=clamp(old+d,m.min,m.max);values.put(m,next);if(Math.abs(next-old)>.00001)changes.add(m.label+String.format(Locale.ROOT," %+.2f (%.2f)",next-old,next));});
        flags.removeAll(effect.removeFlags());flags.addAll(effect.addFlags());
        if(!changes.isEmpty()||!effect.addFlags().isEmpty()||!effect.removeFlags().isEmpty())note(reason+": "+String.join("; ",changes)+(effect.addFlags().isEmpty()?"":"; path opened "+new TreeSet<>(effect.addFlags()))+(effect.removeFlags().isEmpty()?"":"; path closed "+new TreeSet<>(effect.removeFlags())),log);
        high=Math.max(high,approval());low=Math.min(low,approval());
    }
    public void election(boolean won,boolean tied,Integer ev,int campaignTasks,List<String> log){
        if(tied){note("Unresolved election: no victory mandate assigned.",log);return;}
        double scale=ev==null?0:clamp(Math.abs(ev-269)/100.0,0,2);
        if(won){apply(WorldEffect.of(WorldMetric.REPUTATION,3+scale*2,WorldMetric.UNITY,3+scale*2,WorldMetric.CAPITAL,5+scale*4),"Victory / electoral coalition breadth (not popular-vote margin)",log);
            apply(WorldEffect.of(WorldMetric.TRUST,Math.min(3,campaignTasks*.06),WorldMetric.POPULARITY,Math.min(2,campaignTasks*.04)),"Documented campaign contact work",log);
        }else apply(WorldEffect.of(WorldMetric.REPUTATION,-4-scale*4,WorldMetric.DONORS,-5-scale*4,WorldMetric.UNITY,-3-scale*2,WorldMetric.POPULARITY,-2-scale),"Defeat severity",log);
    }
    public void beginTerm(List<String> log){actions.clear();creditedDelivery.clear();note("Inauguration carries existing reputation, economy, risks and pending consequences. Approval is weighted 35% own-party, 30% independent, 35% opposition; fictional fixed electorate.",log);}
    public static String actionHelp(CareerCommand.GovernanceAction a) {
        String effect = switch(a) {
            case PUBLIC_BRIEFING -> "Trust +1.5, popularity +1, independent approval +0.4, capital -1.";
            case CABINET_MEETING -> "Party unity +2, political capital +2.";
            case BUDGET_REVIEW -> "Trust +1, donor support +0.5; this is office accounting, not national deficit control.";
            case SERVICE_REVIEW -> "Resilience +2, trust +1.";
            case MANAGE_CRISIS -> "Preparedness +5, capital -2. Readiness improves emergency-event probabilities.";
            case CAMPAIGN_ALLIES -> "Unity +2, own-party approval +1.";
            case FUNDRAISE -> "Donor support +3, personal popularity -0.5.";
            case TRAVEL -> "Popularity +1.5, independent approval +0.5.";
            case REST -> "Energy +16; consumes an action, leaves other work undone.";
            case APPOINT_OFFICIAL -> "Capital -2, unity +1.";
            default -> "No special public metric bonus.";
        };
        return effect + (a==CareerCommand.GovernanceAction.REST ? "" : " Energy -4. Positive action benefits are halved below 25 energy; repeating this action in the same month gives no further public bonus.");
    }
    public String actionProblem(CareerCommand.GovernanceAction a) {
        int cost=switch(a){case PUBLIC_BRIEFING -> 1;case MANAGE_CRISIS, APPOINT_OFFICIAL -> 2;default -> 0;};
        return get(WorldMetric.CAPITAL)<cost ? "Insufficient political capital; need "+cost+"." : "";
    }
    public void act(CareerCommand.GovernanceAction a,List<String> log){
        if(a==CareerCommand.GovernanceAction.REST){apply(WorldEffect.of(WorldMetric.ENERGY,16),"Personal recovery",log);return;}
        boolean first=actions.add(a);double effectiveness=get(WorldMetric.ENERGY)<25?.5:1;
        apply(WorldEffect.of(WorldMetric.ENERGY,-4),"Workload",log);
        if(!first){note("Repeated action this month: no additional public metric benefit.",log);return;}
        WorldEffect e=switch(a){
            case PUBLIC_BRIEFING -> WorldEffect.of(WorldMetric.TRUST,1.5*effectiveness,WorldMetric.POPULARITY,1*effectiveness,WorldMetric.INDEPENDENT_APPROVAL,.4*effectiveness,WorldMetric.CAPITAL,-1);
            case CABINET_MEETING -> WorldEffect.of(WorldMetric.UNITY,2*effectiveness,WorldMetric.CAPITAL,2*effectiveness);
            case BUDGET_REVIEW -> WorldEffect.of(WorldMetric.TRUST,1*effectiveness,WorldMetric.DONORS,.5*effectiveness);
            case SERVICE_REVIEW -> WorldEffect.of(WorldMetric.RESILIENCE,2*effectiveness,WorldMetric.TRUST,1*effectiveness);
            case MANAGE_CRISIS -> WorldEffect.of(WorldMetric.PREPAREDNESS,5*effectiveness,WorldMetric.CAPITAL,-2);
            case CAMPAIGN_ALLIES -> WorldEffect.of(WorldMetric.UNITY,2*effectiveness,WorldMetric.PARTY_APPROVAL,1*effectiveness);
            case FUNDRAISE -> WorldEffect.of(WorldMetric.DONORS,3*effectiveness,WorldMetric.POPULARITY,-.5);
            case TRAVEL -> WorldEffect.of(WorldMetric.POPULARITY,1.5*effectiveness,WorldMetric.INDEPENDENT_APPROVAL,.5*effectiveness);
            case APPOINT_OFFICIAL -> WorldEffect.of(WorldMetric.CAPITAL,-2,WorldMetric.UNITY,1);
            default -> WorldEffect.of();};apply(e,a.toString(),log);
    }
    public void law(Policy.Issue issue,Policy.Approach approach,Policy.Approach promise,List<String> log){
        apply(WorldEffect.of(WorldMetric.CAPITAL,3,WorldMetric.UNITY,1),"Legislation enacted: "+issue,log);
        if(promise!=null)apply(promise==approach?WorldEffect.of(WorldMetric.PARTY_APPROVAL,2,WorldMetric.TRUST,2,WorldMetric.REPUTATION,1):WorldEffect.of(WorldMetric.PARTY_APPROVAL,-5,WorldMetric.TRUST,-4,WorldMetric.UNITY,-4),promise==approach?"Legislative promise honored; delivery still pending":"Campaign promise contradicted",log);
        // Signing alone does not magically deliver a national economic benefit.
    }
    public void delivered(Policy.Issue issue,Policy.Approach approach,List<String> log){
        if(!creditedDelivery.add(issue)){note("Revised delivery recorded; no repeat-farming benefit for this issue this term.",log);return;}
        apply(policyEffect(issue,approach),"Delivered small administrative initiative: "+issue+" / "+approach,log);
    }
    public static WorldEffect policyEffect(Policy.Issue issue,Policy.Approach approach) {
        boolean expand=approach==Policy.Approach.EXPAND_PROGRAM;
        var deltas=new EnumMap<WorldMetric,Double>(WorldMetric.class);
        deltas.put(WorldMetric.TRUST,1.5);deltas.put(WorldMetric.INDEPENDENT_APPROVAL,expand?1.2:.5);deltas.put(WorldMetric.DEFICIT,expand?.04:-.02);
        switch(issue){
            case ECONOMY -> {deltas.put(WorldMetric.GROWTH,expand?.08:.04);deltas.put(WorldMetric.UNEMPLOYMENT,expand?-.03:-.01);}
            case TAXES -> {deltas.put(WorldMetric.DONORS,expand?-1.0:1.0);deltas.put(WorldMetric.TRUST,expand?2.0:1.0);}
            case HEALTHCARE -> deltas.put(WorldMetric.PREPAREDNESS,expand?3.0:2.0);
            case DEFENSE -> {deltas.put(WorldMetric.PREPAREDNESS,2.0);deltas.put(WorldMetric.TENSION,expand?.5:-.5);}
            case ENVIRONMENT -> deltas.put(WorldMetric.RESILIENCE,expand?3.0:1.5);
            case EDUCATION -> deltas.put(WorldMetric.GROWTH,.03);
            case IMMIGRATION -> {deltas.put(WorldMetric.TRUST,2.0);deltas.put(WorldMetric.UNITY,expand?-1.0:1.0);}
            case CIVIL_RIGHTS -> {deltas.put(WorldMetric.TRUST,2.5);deltas.put(WorldMetric.OPPOSITION_APPROVAL,.5);}
        }
        return new WorldEffect(deltas,Set.of(),Set.of());
    }
    public void spendCapital(int amount,List<String> log){if(get(WorldMetric.CAPITAL)<amount)throw new IllegalArgumentException("Need "+amount+" political capital.");apply(WorldEffect.of(WorldMetric.CAPITAL,-amount),"Agenda bargaining",log);}
    public List<String> advance(boolean inOffice){
        var log=new ArrayList<String>();month++;actions.clear();
        if(timedDecisions&&pending!=null&&month>=pendingDue)expire("DEADLINE MISSED",log);
        for(Scheduled s:List.copyOf(scheduled))if(s.dueMonth()<=month){boolean success=outcomeRandom.nextDouble()<s.probability();apply(success?s.success():s.failure(),"FOLLOW-UP "+s.reason()+" / "+(success?"expected branch":"adverse branch"),log);scheduled.remove(s);}
        double growth=get(WorldMetric.GROWTH),inflation=get(WorldMetric.INFLATION),unemployment=get(WorldMetric.UNEMPLOYMENT),rate=get(WorldMetric.RATE);
        double rateTarget=clamp(2.5+.7*(inflation-2)-.4*(unemployment-4.5),0,15);
        double dg=.14*(2-growth)-.06*(rate-inflation-1)+.003*(get(WorldMetric.CONFIDENCE)-50)+(economyRandom.nextDouble()-.5)*.5;
        double du=.06*(4.5-unemployment)-.06*(growth-2)+(economyRandom.nextDouble()-.5)*.10;
        double di=.08*(2-inflation)+.015*(growth-2)+(economyRandom.nextDouble()-.5)*.14;
        apply(WorldEffect.of(WorldMetric.GROWTH,dg,WorldMetric.UNEMPLOYMENT,du,WorldMetric.INFLATION,di,WorldMetric.RATE,.18*(rateTarget-rate),WorldMetric.DEFICIT,.025*(4-get(WorldMetric.DEFICIT)),WorldMetric.DEBT,(get(WorldMetric.DEFICIT)-get(WorldMetric.DEBT)*(growth+inflation)/100)/12,WorldMetric.CONFIDENCE,.10*(55+2*growth-3*(unemployment-4.5)-2*(inflation-2)-get(WorldMetric.CONFIDENCE))),"Economy: inertia, credit conditions and external variation; rate set independently",log);
        if(inOffice){
            double pressure=.12*(get(WorldMetric.TRUST)-55)+.5*(get(WorldMetric.GROWTH)-2)-.8*(get(WorldMetric.UNEMPLOYMENT)-4.5)-.7*Math.max(0,get(WorldMetric.INFLATION)-2)-.12*get(WorldMetric.SCANDAL)-.025*Math.max(0,get(WorldMetric.TENSION)-30);
            double noise=(economyRandom.nextDouble()-.5)*.3;
            apply(WorldEffect.of(WorldMetric.PARTY_APPROVAL,.09*(clamp(82+pressure+.12*(get(WorldMetric.UNITY)-70),5,98)-get(WorldMetric.PARTY_APPROVAL))+noise,
                WorldMetric.INDEPENDENT_APPROVAL,.12*(clamp(48+pressure*1.6,3,95)-get(WorldMetric.INDEPENDENT_APPROVAL))+noise,
                WorldMetric.OPPOSITION_APPROVAL,.07*(clamp(20+pressure*.65,2,85)-get(WorldMetric.OPPOSITION_APPROVAL))+noise,
                WorldMetric.POPULARITY,.05*(clamp(54+.25*(get(WorldMetric.TRUST)-55)-.15*get(WorldMetric.SCANDAL),0,100)-get(WorldMetric.POPULARITY)),
                WorldMetric.CAPITAL,clamp((approval()-40)/15,-2,2),WorldMetric.ENERGY,2,WorldMetric.PREPAREDNESS,-.2,WorldMetric.RESILIENCE,-.1),"Monthly public response: economy, trust, party unity, ethics and tension",log);
        }
        recordPoint();return log;
    }
    private void recordPoint(){trend.add(new Point(month,approval(),get(WorldMetric.POPULARITY),get(WorldMetric.GROWTH),get(WorldMetric.UNEMPLOYMENT),get(WorldMetric.INFLATION)));high=Math.max(high,approval());low=Math.min(low,approval());}
    public void trigger(int frequency,boolean force,List<String> log){
        if(pending!=null)throw new IllegalArgumentException("Resolve the current world event first.");
        if(!force&&frequency==0)return;
        var eligible=catalog().stream().filter(e->e.eligible(month,flags,values)&&month-lastEvents.getOrDefault(e.id(),-100)>=18).toList();
        var followups=eligible.stream().filter(e->e.requiresFlag()!=null).toList();
        if(!followups.isEmpty())eligible=followups;
        else if(!force&&eventRandom.nextDouble()>(frequency==1?.35:.60))return;
        if(eligible.isEmpty()){note("No eligible world event.",log);return;}
        int draw=eventRandom.nextInt(eligible.stream().mapToInt(WorldEvent::weight).sum());
        for(var event:eligible){draw-=event.weight();if(draw<0){pending=adjust(event);break;}}
        startClock();lastEvents.put(pending.id(),month);note("EVENT: "+pending.title()+". Choose a response; save/menu remain available.",log);
    }
    private WorldEvent adjust(WorldEvent e){
        if(!Set.of("storm","health","cyber").contains(e.id()))return e;
        double resilience=get(e.id().equals("cyber")?WorldMetric.RESILIENCE:WorldMetric.PREPAREDNESS);
        var choices=e.choices().stream().map(c->new WorldEvent.Choice(c.label(),c.capitalCost(),c.immediate(),c.delay(),clamp(c.successChance()+(resilience-45)/300,.05,.98),c.success(),c.failure(),c.consequence()+" Probability includes current readiness.")).toList();
        return new WorldEvent(e.id(),e.title(),e.description(),e.weight(),e.earliestMonth(),e.requiresFlag(),e.belowMetric(),e.below(),choices);
    }
    public void forceEvent(int index,List<String> log) {
        if(pending!=null)throw new IllegalArgumentException("Resolve the pending event before replacing it.");
        if(index<0||index>=catalog().size())throw new IllegalArgumentException("Unknown event.");
        pending=adjust(catalog().get(index));startClock();lastEvents.put(pending.id(),month);
        note("DEVELOPER forced "+pending.title()+"; prerequisites bypassed for this fixture.",log);
    }
    public List<String> choose(int choice){
        if(pending==null||choice<0||choice>=pending.choices().size())throw new IllegalArgumentException("Choose an available world response.");
        WorldEvent.Choice c=pending.choices().get(choice);
        if(get(WorldMetric.CAPITAL)<c.capitalCost())throw new IllegalArgumentException("Insufficient political capital; the second response costs none.");
        var log=new ArrayList<String>();spendCapital(c.capitalCost(),log);apply(c.immediate(),pending.title()+" / "+c.label(),log);
        scheduled.add(new Scheduled(month+c.delay(),pending.title()+" / "+c.consequence(),c.successChance(),c.success(),c.failure()));
        note("Scheduled consequence at world month "+(month+c.delay())+"; probability "+Math.round(c.successChance()*100)+"%.",log);pending=null;pendingDue=0;pendingSeconds=0;return log;
    }
    private List<WorldEvent> catalog(){return timedDecisions?WorldStories.all():WorldEventCatalog.all();}
    private void startClock(){if(timedDecisions){DecisionDeadline d=DecisionDeadline.world(pending);pendingDue=month+d.turns();pendingSeconds=d.seconds();}}
    private void expire(String reason,List<String> log){DecisionDeadline d=DecisionDeadline.world(pending);note(reason+": "+pending.title()+". "+d.missed(),log);apply(d.effect(),"No response",log);pending=null;pendingDue=0;pendingSeconds=0;}
    public List<String> leaveLive(){if(!timedDecisions||pending==null||pendingSeconds<=0)throw new IllegalArgumentException("No live response.");var log=new ArrayList<String>();expire("LIVE APPEARANCE ENDED",log);return log;}
    public List<String> tick(int seconds){if(!timedDecisions||pending==null||pendingSeconds<=0||seconds<1||seconds>3600)throw new IllegalArgumentException("No valid live response clock.");var log=new ArrayList<String>();pendingSeconds=Math.max(0,pendingSeconds-seconds);if(pendingSeconds==0)expire("TIME EXPIRED",log);return log;}
    public int campaignCash(){return (int)Math.round(clamp((get(WorldMetric.REPUTATION)-50)*4+(get(WorldMetric.DONORS)-55)*4+(approval()-50)*5,-250,400));}
    public double electionMood(){return clamp((approval()-50)/10+(get(WorldMetric.GROWTH)-2)*.3-(get(WorldMetric.UNEMPLOYMENT)-4.5)*.3+(get(WorldMetric.POPULARITY)-54)/25+(get(WorldMetric.REPUTATION)-50)/50-get(WorldMetric.SCANDAL)/40,-4,4);}
}
