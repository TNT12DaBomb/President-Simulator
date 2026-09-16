import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** Monthly domain model. All mutation is reached through CareerEngine's replayable commands. */
public final class Presidency {
    public static final int MONTHS_PER_TERM = 48, ACTIONS_PER_MONTH = 2, RECEIPTS = 100, EXPENSES = 75;
    private final Random random;
    private final Cabinet cabinet;
    private final GameRules rules;
    private final WorldSimulation world;
    private final Congress congress;
    private final PublicCorrespondence correspondence = new PublicCorrespondence();
    private final MidtermCampaign midtermCampaign;
    private final List<Implementation> implementations = new ArrayList<>();
    public record Implementation(Policy.Issue issue, Policy.Approach approach, int dueMonth, String title) { }
    private final Map<Policy.Issue, Policy.Approach> pledges;
    private final Map<Policy.Issue, Policy.Approach> laws = new EnumMap<>(Policy.Issue.class);
    private final Set<String> usedEvents = new HashSet<>();
    private final List<DueEffect> delayed = new ArrayList<>();
    private final EnumSet<CareerCommand.GovernanceAction> annualWork = EnumSet.noneOf(CareerCommand.GovernanceAction.class);
    private final EnumSet<CareerCommand.GovernanceAction> termWork = EnumSet.noneOf(CareerCommand.GovernanceAction.class);
    private final EnumSet<State.Task> credits;
    private final int inaugurationYear;
    private int months, actions, treasury = 1200, lawsPassed, billsVetoed;
    private String feedback, support = "Cabinet coordination is pending.";
    private Policy.Bill bill;
    private PresidencyEvent pending;
    private record DueEffect(int dueMonth, PresidencyEvent.Effect effect) { }
    public record DelayedView(int dueMonth, String description) { }
    public record View(String date, int monthsCompleted, int actionsLeft, int treasury, String publicFeedback,
                       String support, Congress.View congress, Policy.Bill bill, List<Policy.Promise> promises,
                       List<String> laws, int lawsPassed, int billsVetoed, PresidencyEvent pendingEvent,
                       List<DelayedView> delayedEffects, List<String> annualWork, PublicCorrespondence.View correspondence,
                       List<MidtermCampaign.Contest> midtermContests, List<Implementation> implementations, Cabinet.View cabinet, Congress.Proceedings proceedings, WorldSimulation.View world) {
        public View { promises = List.copyOf(promises); laws = List.copyOf(laws); delayedEffects = List.copyOf(delayedEffects); annualWork = List.copyOf(annualWork); midtermContests = List.copyOf(midtermContests); implementations = List.copyOf(implementations); }
    }
    public Presidency(long seed, int inaugurationYear, Map<Policy.Issue, Policy.Approach> pledges,
                      Set<State.Task> credits, String openingFeedback) {
        this(seed, inaugurationYear, pledges, credits, openingFeedback, GameRules.CURRENT);
    }
    public Presidency(long seed, int inaugurationYear, Map<Policy.Issue, Policy.Approach> pledges,
                      Set<State.Task> credits, String openingFeedback, GameRules rules) {
        this(seed, inaugurationYear, pledges, credits, openingFeedback, rules, rules.world() ? new WorldSimulation(seed,rules.timed()) : null);
    }
    public Presidency(long seed, int inaugurationYear, Map<Policy.Issue, Policy.Approach> pledges,
                      Set<State.Task> credits, String openingFeedback, GameRules rules, WorldSimulation world) {
        this.world = world;
        if(world!=null && world.view().pending()!=null)pending=world.view().pending().display();
        this.rules = rules; this.congress = new Congress(rules); this.cabinet = new Cabinet(rules); this.midtermCampaign = new MidtermCampaign(rules);
        this.random = new Random(seed); this.inaugurationYear = inaugurationYear;
        this.pledges = new EnumMap<>(Policy.Issue.class); this.pledges.putAll(pledges);
        this.credits = credits.isEmpty() ? EnumSet.noneOf(State.Task.class) : EnumSet.copyOf(credits);
        feedback = openingFeedback;
        if (credits.contains(State.Task.FIELD_OFFICE)) support = "Campaign organizing contacts are available to the incoming cabinet.";
    }
    public View view() {
        List<Policy.Promise> promises = pledges.entrySet().stream().map(e -> new Policy.Promise(e.getKey(), e.getValue(),
            !laws.containsKey(e.getKey()) ? "Pending" : laws.get(e.getKey()) == e.getValue() ? "Kept" : "Contradicted by signed law")).toList();
        return new View(java.time.YearMonth.of(inaugurationYear, 1).plusMonths(months).toString(), months,
            ACTIONS_PER_MONTH - actions, treasury, feedback, support, congress.view(), bill, promises,
            laws.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).toList(), lawsPassed, billsVetoed, pending,
            delayed.stream().map(d -> new DelayedView(d.dueMonth, d.effect.message())).toList(),
            annualWork.stream().map(Object::toString).toList(), correspondence.view(), midtermCampaign.view(), implementations, cabinet.view(), congress.proceedings(), world == null ? null : world.view());
    }
    public Set<CareerCommand.GovernanceAction> annualWork() { return Set.copyOf(annualWork); }
    public Set<State.Task> credits() { return Set.copyOf(credits); }
    public int cost(CareerCommand.GovernanceAction a) {
        State.Task credit = creditFor(a);
        return credit != null && credits.contains(credit) ? (a == CareerCommand.GovernanceAction.SERVICE_REVIEW ? 75 : 0) : a.cost();
    }
    private State.Task creditFor(CareerCommand.GovernanceAction a) {
        return switch (a) { case PUBLIC_BRIEFING -> State.Task.TOWN_HALL; case CABINET_MEETING -> State.Task.FIELD_OFFICE;
            case SERVICE_REVIEW -> State.Task.OUTREACH; default -> null; };
    }
    public String actionProblem(int cost) {
        if (months >= MONTHS_PER_TERM) return "The term has ended.";
        if (pending != null && !rules.timed()) return "Resolve the event before taking another action.";
        if (actions >= ACTIONS_PER_MONTH) return "No actions remain; end the month to continue.";
        if (treasury < cost) return "Insufficient public operating cash.";
        return "";
    }
    private void spend(int cost) {
        if (months % 12 == 0 && actions == 0) annualWork.clear();
        treasury -= cost; actions++;
    }
    public List<String> act(CareerCommand.GovernanceAction a) {
        int cost = cost(a); String problem = actionProblem(cost);
        if (!problem.isEmpty()) throw new IllegalArgumentException(problem);
        if (world != null && !world.actionProblem(a).isEmpty()) throw new IllegalArgumentException(world.actionProblem(a));
        spend(cost); credits.remove(creditFor(a));
        boolean first = annualWork.add(a); termWork.add(a);
        List<String> messages = new ArrayList<>(); messages.add(view().date() + ": " + a + "; action cost $" + cost + ".");
        if (a == CareerCommand.GovernanceAction.BUDGET_REVIEW && first) { treasury += 100; messages.add("Annual reconciliation recovered $100; once per administrative year."); }
        if (a == CareerCommand.GovernanceAction.PUBLIC_BRIEFING || a == CareerCommand.GovernanceAction.SERVICE_REVIEW) feedback = a + " completed; the public correspondence log has been updated.";
        if (a == CareerCommand.GovernanceAction.CABINET_MEETING || a == CareerCommand.GovernanceAction.APPOINT_OFFICIAL) support = a + " completed; administration contacts updated.";
        if (a == CareerCommand.GovernanceAction.CAMPAIGN_ALLIES) support = "General ally meeting recorded; specific midterm visits must be completed on the contest board.";
        if (a == CareerCommand.GovernanceAction.FUNDRAISE) messages.add("Private donor meeting recorded. No public money was transferred to the campaign.");
        if (a == CareerCommand.GovernanceAction.REST) messages.add("Personal time reserved; no policy or campaign objective credited.");
        if (world != null) world.act(a, messages);
        return messages;
    }
    public List<String> office(OfficeCommand c) {
        if(c.type()==OfficeCommand.Type.LEAVE_LIVE){if(world==null)throw new IllegalArgumentException("No live response.");List<String> log=world.leaveLive();pending=null;return log;}
        if(c.type()==OfficeCommand.Type.CLOCK_TICK){if(world==null)throw new IllegalArgumentException("No world clock.");List<String> log=world.tick(c.choice());pending=world.view().pending()==null?null:world.view().pending().display();return log;}
        if (c.type() == OfficeCommand.Type.EVENT_CHOICE) {
            if (pending == null) throw new IllegalArgumentException("No event awaits a response.");
            if (world != null) { List<String> messages = world.choose(c.choice()); pending = null; feedback = messages.get(messages.size()-1); return messages; }
            PresidencyEvent.Choice choice = pending.choices().get(c.choice());
            if (treasury < choice.cost()) throw new IllegalArgumentException("Cannot afford that response. The second response is free.");
            treasury -= choice.cost(); treasury += choice.immediate().treasury(); feedback = choice.immediate().message();
            if (choice.delayed() != null) delayed.add(new DueEffect(Math.min(MONTHS_PER_TERM, months + choice.delayMonths()), choice.delayed()));
            pending = null;
            return List.of(feedback + " Response cost $" + choice.cost() + ".");
        }
        if (c.type().name().startsWith("DEV_")) {
            if (world == null) throw new IllegalArgumentException("World tools require a WORLD career.");
            var log = new ArrayList<String>();
            if (c.type() == OfficeCommand.Type.DEV_WORLD_EVENT) { world.forceEvent(c.choice(),log); pending=world.view().pending().display(); }
            else if(c.type()==OfficeCommand.Type.DEV_CAPITAL) world.apply(WorldEffect.of(WorldMetric.CAPITAL,c.choice()-world.get(WorldMetric.CAPITAL)),"DEVELOPER capital",log);
            else world.apply(WorldEffect.of(WorldMetric.PARTY_APPROVAL,c.choice()-world.get(WorldMetric.PARTY_APPROVAL),WorldMetric.INDEPENDENT_APPROVAL,c.choice()-world.get(WorldMetric.INDEPENDENT_APPROVAL),WorldMetric.OPPOSITION_APPROVAL,c.choice()-world.get(WorldMetric.OPPOSITION_APPROVAL)),"DEVELOPER uniform approval fixture",log);
            return log;
        }
        String problem = actionProblem(0); if (!problem.isEmpty()) throw new IllegalArgumentException(problem);
        if (c.type() == OfficeCommand.Type.REPLY) {
            problem = correspondence.replyProblem(c.choice());
            if (!problem.isEmpty()) throw new IllegalArgumentException(problem);
            spend(0); feedback = correspondence.reply(c.choice(), months + 1);
            var replyLog = new ArrayList<String>(); replyLog.add(feedback);
            if (world != null) world.apply(WorldEffect.of(WorldMetric.TRUST,.3), "Acknowledged a specific request (not delivered)", replyLog);
            return replyLog;
        }
        if (c.type() == OfficeCommand.Type.MIDTERM_TASK) {
            problem = midtermCampaign.problem(c.choice(), months);
            if (!problem.isEmpty()) throw new IllegalArgumentException(problem);
            spend(0); return List.of(midtermCampaign.complete(c.choice()) + " One monthly action used; no public treasury spent.");
        }
        List<String> details = new ArrayList<>();
        if (java.util.Set.of(OfficeCommand.Type.NOMINATE, OfficeCommand.Type.CONFIRM, OfficeCommand.Type.NOMINEE_SUPPORT,
                OfficeCommand.Type.DISMISS, OfficeCommand.Type.DELEGATE).contains(c.type())) {
            problem = cabinet.problem(c.type(), c.choice(), months, congress.view().senateSeats());
            if (!problem.isEmpty()) throw new IllegalArgumentException(problem);
            int price = c.type() == OfficeCommand.Type.DELEGATE ? cabinet.projectCost(c.choice()) : 0;
            if (treasury < price) throw new IllegalArgumentException("This assignment costs $" + price + ". Insufficient operating cash.");
            spend(price); support = cabinet.execute(c.type(), c.choice(), months); return List.of(support);
        }
        Policy.Bill beforeBill = bill;
        switch (c.type()) {
            case PROPOSE -> {
                if (bill != null) throw new IllegalArgumentException("Finish or veto the existing bill before proposing another.");
                spend(0); if (rules.current()) congress.finishBill(); bill = new Policy.Bill(c.issue(), c.approach(), congress.stage());
            }
            case NEGOTIATE -> {
                if (rules.current() && (bill == null || congress.canPass())) throw new IllegalArgumentException("Introduce a bill still awaiting passage before negotiating.");
                if (world != null) world.spendCapital(8, details);
                spend(0); congress.negotiate(); if (bill != null) bill = new Policy.Bill(bill.issue(), bill.approach(), rules.current() ? congress.stage() : "Ready for signature");
                support = rules.current() ? "Fictional coalition negotiation: +8 House and +4 Senate commitments, capped by available seats. Commitments apply only to this bill." : "Leadership agreement secured for one bill.";
            }
            case COMMITTEE_REVIEW, FLOOR_VOTE -> {
                if (!rules.current()) throw new IllegalArgumentException("This imported career retains legacy legislative rules.");
                if (bill == null) throw new IllegalArgumentException("Introduce a bill first.");
                String gate = c.type() == OfficeCommand.Type.COMMITTEE_REVIEW ? congress.committeeProblem() : congress.floorProblem();
                if (!gate.isEmpty()) throw new IllegalArgumentException(gate);
                spend(0);
                if (c.type() == OfficeCommand.Type.COMMITTEE_REVIEW) congress.reportCommittee(); else congress.passFloor();
                bill = new Policy.Bill(bill.issue(), bill.approach(), congress.stage());
                details.add(c.type() == OfficeCommand.Type.FLOOR_VOTE ? "House passage and Senate cloture/final passage recorded. All pledged members are assumed to attend and vote together." : "Committee reports the bill; floor votes still required.");
            }
            case WITHDRAW_BILL -> {
                if (!rules.current() || bill == null || congress.canPass()) throw new IllegalArgumentException("Withdraw only an unpassed bill in the current ruleset.");
                spend(0); bill = null; congress.finishBill(); feedback = "Legislative proposal withdrawn; no veto or law recorded.";
            }
            case SIGN -> {
                if (bill == null || !congress.canPass()) throw new IllegalArgumentException("The bill must satisfy the active rules and be ready for signature.");
                PolicyCatalog.Option option = PolicyCatalog.option(bill.issue(), bill.approach());
                if (laws.get(bill.issue()) == bill.approach()) throw new IllegalArgumentException("That initiative is already law. Veto this duplicate bill or choose another issue.");
                if (treasury < option.cost()) throw new IllegalArgumentException("Signing requires $" + option.cost() + " of public operating cash.");
                if (world != null) world.law(bill.issue(), bill.approach(), pledges.get(bill.issue()), details);
                spend(option.cost()); laws.put(bill.issue(), bill.approach()); lawsPassed++;
                Policy.Issue signedIssue = bill.issue();
                if (implementations.removeIf(i -> i.issue() == signedIssue)) details.add("Previous implementation superseded; spent funds are not refunded.");
                implementations.add(new Implementation(bill.issue(), bill.approach(), Math.min(48, months + option.deliveryMonths()), option.title()));
                details.add(option.title() + ": funded for $" + option.cost() + ". " + option.benefit() + ". Tradeoff: " + option.tradeoff() + ".");
                details.addAll(correspondence.schedule(bill.issue(), bill.approach(), months + 1));
                feedback = pledges.containsKey(bill.issue()) ? (pledges.get(bill.issue()) == bill.approach() ? "Campaign promise kept: " : "Supporter correspondence records a broken promise: ") + bill.issue() : "Signed initiative published: " + bill.issue();
                bill = null; congress.finishBill();
            }
            case VETO -> {
                if (bill == null) throw new IllegalArgumentException("No bill is on the desk.");
                if (rules.current() && !congress.canPass()) throw new IllegalArgumentException("A presidential veto applies only after passage. Withdraw an unpassed proposal instead.");
                spend(0); billsVetoed++; bill = null; congress.finishBill(); feedback = "Bill vetoed; the decision is recorded in the public register.";
            }
            default -> throw new IllegalArgumentException("That command is not an office action.");
        }
        details.add(0, c.type() + " completed. " + (c.type() == OfficeCommand.Type.PROPOSE ? bill : beforeBill) + ". " + feedback);
        return details;
    }
    public List<String> endMonth(int frequency) {
        if (pending != null && !rules.timed()) throw new IllegalArgumentException("Resolve the event before ending the month.");
        if (months >= MONTHS_PER_TERM) throw new IllegalArgumentException("The term has ended.");
        if (months % 12 == 0 && actions == 0) annualWork.clear();
        List<String> log = new ArrayList<>();
        treasury += RECEIPTS - EXPENSES; months++; actions = 0;
        log.add("Month " + months + " closed: receipts +$100; routine operations -$75; treasury $" + treasury + ".");
        for (DueEffect d : List.copyOf(delayed)) if (d.dueMonth <= months) { treasury += d.effect.treasury(); feedback = d.effect.message(); log.add("FOLLOW-UP: " + feedback); delayed.remove(d); }
        for (Implementation i : List.copyOf(implementations)) if (i.dueMonth() <= months) {
            feedback = correspondence.deliver(i.issue(), i.approach(), months);
            log.add("POLICY DELIVERY: " + feedback); implementations.remove(i);
            if (world != null) world.delivered(i.issue(), i.approach(), log);
        }
        for (Cabinet.Delivery delivery : cabinet.advanceMonth(months)) {
            boolean first = annualWork.add(delivery.work());
            log.add("CABINET REPORT: " + delivery.message()); support = delivery.message();
            if (world != null) world.apply(WorldEffect.of(WorldMetric.TRUST,.5,WorldMetric.CAPITAL,.5),"Delivered cabinet report: "+delivery.message(),log);
            if (delivery.work() == CareerCommand.GovernanceAction.PUBLIC_BRIEFING) feedback = delivery.message();
            if (delivery.work() == CareerCommand.GovernanceAction.BUDGET_REVIEW && first) { treasury += 100; log.add("Annual reconciliation recovered $100; shared with the manual reconciliation limit."); }
        }
        if (world != null) log.addAll(world.advance(true));
        if (months == 12) log.add("MIDTERM CAMPAIGN OPEN: targeted listening and organizing visits are now available through month " + rules.midtermElectionMonth() + ".");
        if (months == rules.midtermElectionMonth()) {
            if (world != null) midtermCampaign.resolveWorld(world.electionMood(), random, log);
            log.add(congress.midterm(midtermCampaign));
            midtermCampaign.view().forEach(c -> log.add(c.name() + ": " + c.outcome() + " (" + c.seats() + " seats)."));
            if (bill != null) bill = new Policy.Bill(bill.issue(), bill.approach(), congress.stage());
        }
        if (rules.current() && months == rules.midtermSeatingMonth() && congress.hasIncomingCongress()) {
            if (bill != null && !congress.canPass()) log.add("Pending bill closed at the new Congress boundary: " + bill.issue() + ". Reintroduce it if still desired.");
            if (!congress.canPass()) bill = null; log.add(congress.seatIncomingCongress());
        }
        if (months % 12 == 0 && !annualWork.contains(CareerCommand.GovernanceAction.PUBLIC_BRIEFING)) { feedback = "Annual correspondence records an unanswered request for a public briefing."; log.add(feedback); }
        if (world != null && months < MONTHS_PER_TERM) { if(world.view().pending()==null)world.trigger(frequency, false, log); pending = world.view().pending() == null ? null : world.view().pending().display(); }
        else if (world == null && months < MONTHS_PER_TERM && frequency > 0 && months % (frequency == 1 ? 6 : 3) == 0) triggerEvent(log);
        if(world!=null)pending=world.view().pending()==null?null:world.view().pending().display();
        return log;
    }
    public void triggerEvent(List<String> log) {
        if (pending != null) throw new IllegalArgumentException("Resolve the current event first.");
        if (world != null) { world.trigger(1, true, log); pending = world.view().pending() == null ? null : world.view().pending().display(); return; }
        List<PresidencyEvent> eligible = PresidencyEvent.catalog().stream().filter(e -> !usedEvents.contains(e.id()) && months >= e.earliestMonth() && (!e.requiresLaw() || lawsPassed > 0)).toList();
        if (eligible.isEmpty()) { log.add("No eligible unused presidency events remain this term."); return; }
        int pick = random.nextInt(eligible.stream().mapToInt(PresidencyEvent::weight).sum());
        for (PresidencyEvent e : eligible) { pick -= e.weight(); if (pick < 0) { pending = e; usedEvents.add(e.id()); break; } }
        log.add("EVENT: " + pending.title() + ". A response is required; responding does not spend a monthly action.");
    }
    public void grantFunds() { treasury += 500; }
    public void toggleCongress() { congress.developerToggle(); if (bill != null) bill = new Policy.Bill(bill.issue(), bill.approach(), congress.stage()); }
}
