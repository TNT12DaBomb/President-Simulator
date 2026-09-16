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
    private final Congress congress = new Congress();
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
                       List<DelayedView> delayedEffects, List<String> annualWork) {
        public View { promises = List.copyOf(promises); laws = List.copyOf(laws); delayedEffects = List.copyOf(delayedEffects); annualWork = List.copyOf(annualWork); }
    }
    public Presidency(long seed, int inaugurationYear, Map<Policy.Issue, Policy.Approach> pledges,
                      Set<State.Task> credits, String openingFeedback) {
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
            annualWork.stream().map(Object::toString).toList());
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
        if (pending != null) return "Resolve the event before taking another action.";
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
        spend(cost); credits.remove(creditFor(a));
        boolean first = annualWork.add(a); termWork.add(a);
        List<String> messages = new ArrayList<>(); messages.add(view().date() + ": " + a + "; action cost $" + cost + ".");
        if (a == CareerCommand.GovernanceAction.BUDGET_REVIEW && first) { treasury += 100; messages.add("Annual reconciliation recovered $100; once per administrative year."); }
        if (a == CareerCommand.GovernanceAction.PUBLIC_BRIEFING || a == CareerCommand.GovernanceAction.SERVICE_REVIEW) feedback = a + " completed; the public correspondence log has been updated.";
        if (a == CareerCommand.GovernanceAction.CABINET_MEETING || a == CareerCommand.GovernanceAction.APPOINT_OFFICIAL) support = a + " completed; administration contacts updated.";
        if (a == CareerCommand.GovernanceAction.CAMPAIGN_ALLIES) support = "Ally campaign commitment completed for this term's midterm scenario.";
        if (a == CareerCommand.GovernanceAction.FUNDRAISE) messages.add("Private donor meeting recorded. No public money was transferred to the campaign.");
        if (a == CareerCommand.GovernanceAction.REST) messages.add("Personal time reserved; no policy or campaign objective credited.");
        return messages;
    }
    public List<String> office(OfficeCommand c) {
        if (c.type() == OfficeCommand.Type.EVENT_CHOICE) {
            if (pending == null) throw new IllegalArgumentException("No event awaits a response.");
            PresidencyEvent.Choice choice = pending.choices().get(c.choice());
            if (treasury < choice.cost()) throw new IllegalArgumentException("Cannot afford that response. The second response is free.");
            treasury -= choice.cost(); treasury += choice.immediate().treasury(); feedback = choice.immediate().message();
            if (choice.delayed() != null) delayed.add(new DueEffect(Math.min(MONTHS_PER_TERM, months + choice.delayMonths()), choice.delayed()));
            pending = null;
            return List.of(feedback + " Response cost $" + choice.cost() + ".");
        }
        String problem = actionProblem(0); if (!problem.isEmpty()) throw new IllegalArgumentException(problem);
        Policy.Bill beforeBill = bill;
        switch (c.type()) {
            case PROPOSE -> {
                if (bill != null) throw new IllegalArgumentException("Finish or veto the existing bill before proposing another.");
                spend(0); bill = new Policy.Bill(c.issue(), c.approach(), congress.canPass() ? "Ready for signature" : "Awaiting cross-caucus agreement");
            }
            case NEGOTIATE -> {
                spend(0); congress.negotiate(); if (bill != null) bill = new Policy.Bill(bill.issue(), bill.approach(), "Ready for signature");
                support = "Leadership agreement secured for one bill.";
            }
            case SIGN -> {
                if (bill == null || !congress.canPass()) throw new IllegalArgumentException("A bill and a chamber majority or negotiated agreement are required.");
                spend(0); laws.put(bill.issue(), bill.approach()); lawsPassed++;
                feedback = pledges.containsKey(bill.issue()) ? (pledges.get(bill.issue()) == bill.approach() ? "Campaign promise kept: " : "Supporter correspondence records a broken promise: ") + bill.issue() : "Signed initiative published: " + bill.issue();
                bill = null; congress.finishBill();
            }
            case VETO -> {
                if (bill == null) throw new IllegalArgumentException("No bill is on the desk.");
                spend(0); billsVetoed++; bill = null; congress.finishBill(); feedback = "Bill vetoed; the decision is recorded in the public register.";
            }
            default -> throw new IllegalArgumentException("That command is not an office action.");
        }
        return List.of(c.type() + " completed. " + (c.type() == OfficeCommand.Type.PROPOSE ? bill : beforeBill) + ". " + feedback);
    }
    public List<String> endMonth(int frequency) {
        if (pending != null) throw new IllegalArgumentException("Resolve the event before ending the month.");
        if (months >= MONTHS_PER_TERM) throw new IllegalArgumentException("The term has ended.");
        if (months % 12 == 0 && actions == 0) annualWork.clear();
        List<String> log = new ArrayList<>();
        treasury += RECEIPTS - EXPENSES; months++; actions = 0;
        log.add("Month " + months + " closed: receipts +$100; routine operations -$75; treasury $" + treasury + ".");
        for (DueEffect d : List.copyOf(delayed)) if (d.dueMonth <= months) { treasury += d.effect.treasury(); feedback = d.effect.message(); log.add("FOLLOW-UP: " + feedback); delayed.remove(d); }
        if (months == 24) {
            log.add(congress.midterm(termWork.contains(CareerCommand.GovernanceAction.CAMPAIGN_ALLIES), termWork.contains(CareerCommand.GovernanceAction.PUBLIC_BRIEFING)));
            if (bill != null) bill = new Policy.Bill(bill.issue(), bill.approach(), congress.canPass() ? "Ready for signature" : "Awaiting cross-caucus agreement");
        }
        if (months % 12 == 0 && !annualWork.contains(CareerCommand.GovernanceAction.PUBLIC_BRIEFING)) { feedback = "Annual correspondence records an unanswered request for a public briefing."; log.add(feedback); }
        if (months < MONTHS_PER_TERM && frequency > 0 && months % (frequency == 1 ? 6 : 3) == 0) triggerEvent(log);
        return log;
    }
    public void triggerEvent(List<String> log) {
        if (pending != null) throw new IllegalArgumentException("Resolve the current event first.");
        List<PresidencyEvent> eligible = PresidencyEvent.catalog().stream().filter(e -> !usedEvents.contains(e.id()) && months >= e.earliestMonth() && (!e.requiresLaw() || lawsPassed > 0)).toList();
        if (eligible.isEmpty()) { log.add("No eligible unused presidency events remain this term."); return; }
        int pick = random.nextInt(eligible.stream().mapToInt(PresidencyEvent::weight).sum());
        for (PresidencyEvent e : eligible) { pick -= e.weight(); if (pick < 0) { pending = e; usedEvents.add(e.id()); break; } }
        log.add("EVENT: " + pending.title() + ". A response is required; responding does not spend a monthly action.");
    }
    public void grantFunds() { treasury += 500; }
    public void toggleCongress() { congress.developerToggle(); if (bill != null) bill = new Policy.Bill(bill.issue(), bill.approach(), congress.canPass() ? "Ready for signature" : "Awaiting cross-caucus agreement"); }
}
