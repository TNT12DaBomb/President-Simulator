import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Campaign-to-presidency state machine. No console, GUI, or filesystem dependencies. */
public final class CareerEngine {
    public static final int MONTHS_PER_TERM = 48;
    public static final int MAX_TERMS = 2;
    public static final int MAX_COMMANDS = 10_000;
    public static final int RECEIPTS = 100;
    public static final int ROUTINE_EXPENSES = 75;
    private final long seed;
    private final President.Difficulty difficulty;
    private final President.RunningMate mate;
    private final List<CareerCommand> journal = new ArrayList<>();
    private final List<String> history = new ArrayList<>();
    private final List<CareerView.ElectionSummary> elections = new ArrayList<>();
    private final EnumSet<CareerCommand.GovernanceAction> annualWork = EnumSet.noneOf(CareerCommand.GovernanceAction.class);
    private final EnumSet<State.Task> transitionCredits = EnumSet.noneOf(State.Task.class);
    private final EnumSet<State.Task> returnWork = EnumSet.noneOf(State.Task.class);
    private GameEngine campaign;
    private Presidency presidency;
    private int eventFrequency = 1;
    private final java.util.Map<Policy.Issue, Policy.Approach> pledges = new java.util.EnumMap<>(Policy.Issue.class);
    private CareerView.Phase phase = CareerView.Phase.CAMPAIGN;
    private ElectionResult latestResult;
    private String latestOutcome = "";
    private int year = 1, cycle = 1, wins, losses, completedTerms, termMonths, servedMonths, comebackYears;
    private int treasury = 1200;
    private boolean donorReviewDue, campaignReviewDue, communityReconnected, budgetPublished, developerUsed;
    private String publicFeedback = "First campaign: public questions are being collected.";
    private String support = "A new campaign team is forming.";

    public CareerEngine(long seed, President.Difficulty difficulty, President.RunningMate mate) {
        this.seed = seed; this.difficulty = Objects.requireNonNull(difficulty); this.mate = Objects.requireNonNull(mate);
        campaign = new GameEngine(seed, difficulty, mate);
        history.add("Career opened. The game permits two elected four-year terms, including nonconsecutive terms.");
    }
    public long seed() { return seed; }
    public President.Difficulty difficulty() { return difficulty; }
    public President.RunningMate runningMate() { return mate; }
    public List<CareerCommand> journal() { return List.copyOf(journal); }
    public ElectionResult latestResult() { return latestResult; }
    public State[] states() { return campaign.states(); }
    public State districtOfColumbia() { return campaign.districtOfColumbia(); }
    public CareerReport submit(CareerCommand command) {
        if (journal.size() >= MAX_COMMANDS) return reject("Career journal limit reached; this session is read-only. Save it or start a new career.");
        if (!wellFormed(command)) return reject("Choose a valid career command.");
        if (command.type() == CareerCommand.Type.DEVELOPER) {
            String problem = developerProblem(command.developer());
            if (!problem.isEmpty()) return reject(problem);
        } else if (phase == CareerView.Phase.RETIRED) return reject("This career has ended. Start a new career or use a developer scenario.");
        int start = history.size();
        switch (command.type()) {
            case OFFICE -> {
                OfficeCommand o = command.office();
                if (o.type() == OfficeCommand.Type.PLEDGE) {
                    if (phase != CareerView.Phase.CAMPAIGN || campaign.view().pendingEvent() != null) return reject("Pledges are set during an active campaign, after resolving events.");
                    if (!pledges.containsKey(o.issue()) && pledges.size() >= 2) return reject("Choose at most two campaign priorities.");
                    if (pledges.containsKey(o.issue())) return reject("That promise is already on the public record and cannot be rewritten during this campaign.");
                    pledges.put(o.issue(), o.approach()); history.add("Campaign promise recorded: " + o.issue() + " / " + o.approach() + ".");
                } else if (o.type() == OfficeCommand.Type.EVENT_FREQUENCY) {
                    eventFrequency = o.choice(); history.add("Presidency event frequency: " + new String[]{"Off", "Every six months", "Every three months"}[eventFrequency] + ". Pending events are retained.");
                } else {
                    if (phase != CareerView.Phase.PRESIDENCY) return reject("This command requires an active presidency.");
                    try {
                        if (o.type() == OfficeCommand.Type.END_MONTH) endMonth();
                        else { history.addAll(presidency.office(o)); syncOffice(); }
                    } catch (IllegalArgumentException ex) { return reject(ex.getMessage()); }
                }
            }
            case CAMPAIGN -> {
                if (phase != CareerView.Phase.CAMPAIGN) return reject("Campaign actions are only available during a campaign.");
                TurnReport report = campaign.submit(command.campaign());
                if (!report.accepted()) return reject(report.messages().get(0));
                history.addAll(report.messages());
                if (report.view().complete()) recordElection(campaign.result(), null);
            }
            case CONTINUE -> {
                if (phase == CareerView.Phase.ELECTION_REVIEW) {
                    if (latestOutcome.equals("WIN")) { phase = CareerView.Phase.TRANSITION; history.add("Transition opened. Review the resources carried from your campaign before taking office."); }
                    else { phase = CareerView.Phase.OPPOSITION; comebackYears = 0; history.add("Out of office. The next campaign opens after four annual rebuilding decisions."); }
                } else if (phase == CareerView.Phase.TRANSITION) beginTerm();
                else return reject("There is no transition to continue from this screen.");
            }
            case GOVERN -> {
                if (phase != CareerView.Phase.PRESIDENCY) return reject("You must be in office to take a monthly action.");
                try { govern(command.governance()); }
                catch (IllegalArgumentException ex) { return reject(ex.getMessage()); }
            }
            case REBUILD -> {
                if (phase != CareerView.Phase.OPPOSITION || comebackYears >= 4) return reject("Annual rebuilding is only available during the four-year period out of office.");
                rebuild(command.rebuild());
            }
            case RUN_AGAIN -> {
                if (wins >= MAX_TERMS) return reject("Two election victories already recorded: another presidential run is unavailable.");
                if (phase != CareerView.Phase.TERM_REVIEW && !(phase == CareerView.Phase.OPPOSITION && comebackYears == 4))
                    return reject("Finish your term or the four annual comeback decisions before running again.");
                startNextCampaign();
            }
            case RETIRE -> {
                phase = CareerView.Phase.RETIRED;
                history.add("Career ended voluntarily. Election and service history remain available.");
            }
            case DEVELOPER -> {
                developerUsed = true;
                history.add("DEVELOPER: " + command.developer() + ". This save is marked as a developer run.");
                developer(command.developer());
            }
        }
        journal.add(command);
        return new CareerReport(true, history.subList(start, history.size()), view());
    }
    private boolean wellFormed(CareerCommand c) {
        if (c == null || c.type() == null) return false;
        return (c.campaign() != null) == (c.type() == CareerCommand.Type.CAMPAIGN)
            && (c.governance() != null) == (c.type() == CareerCommand.Type.GOVERN)
            && (c.rebuild() != null) == (c.type() == CareerCommand.Type.REBUILD)
            && (c.developer() != null) == (c.type() == CareerCommand.Type.DEVELOPER)
            && (c.office() != null) == (c.type() == CareerCommand.Type.OFFICE)
            && (c.office() == null || c.office().valid());
    }
    private CareerReport reject(String message) { return new CareerReport(false, List.of(message), view()); }
    private void recordElection(ElectionResult result, Boolean forcedWin) {
        latestResult = result;
        boolean forced = forcedWin != null;
        boolean won = forced ? forcedWin : result.playerWon();
        boolean tied = !forced && result.isTie();
        latestOutcome = tied ? "TIE" : won ? "WIN" : "LOSS";
        List<String> consequences = new ArrayList<>();
        transitionCredits.clear();
        if (won) {
            wins++;
            for (GameView.StateView s : campaign.view().states()) if (s.playerControls()) transitionCredits.addAll(s.playerTasks());
            publicFeedback = transitionCredits.contains(State.Task.TOWN_HALL)
                ? "Town-hall questions are on file for the incoming administration."
                : "The incoming administration still owes its first public briefing.";
            support = transitionCredits.contains(State.Task.FIELD_OFFICE)
                ? "Campaign organizers have provided a transition contact list."
                : "The transition contact list needs to be assembled.";
            consequences.add("Election victory recorded; term " + wins + " is available.");
            if (transitionCredits.isEmpty()) consequences.add("No completed campaign work in held states supplied transition credits.");
            for (State.Task task : transitionCredits) consequences.add("Transition resource: " + transitionDescription(task));
            donorReviewDue = false;
        } else if (!tied) {
            losses++; donorReviewDue = true; campaignReviewDue = true; communityReconnected = false;
            publicFeedback = "The loss remains on your record; public campaign questions await a review.";
            support = "Donors requested a meeting before restoring the next campaign's $200 reserve.";
            consequences.add("Loss retained in career history. Until donor meetings occur, the next campaign starts with $200 less.");
            consequences.add("The review and volunteer records need rebuilding; unused campaign objectives do not carry over automatically.");
            returnWork.clear(); budgetPublished = false;
        } else {
            publicFeedback = "No winner declared. This version does not simulate a contingent election.";
            consequences.add("Tie recorded without counting it as a victory or loss. Continue to the next four-year cycle.");
        }
        elections.add(new CareerView.ElectionSummary(cycle, year, latestOutcome, forced,
            result == null ? null : result.getPlayerEV(), result == null ? null : result.getOpponentEV(), consequences));
        phase = CareerView.Phase.ELECTION_REVIEW;
        history.add("ELECTION " + cycle + ", career year " + year + ": " + latestOutcome + (forced ? " (developer-forced; no electoral tally assigned)" : "; you " + result.getPlayerEV() + " EV, opponent " + result.getOpponentEV() + " EV."));
        history.addAll(consequences);
    }
    private String transitionDescription(State.Task task) {
        return switch (task) {
            case TOWN_HALL -> "Town-hall records cover the cost of your first public briefing.";
            case FIELD_OFFICE -> "Organizing contacts cover the cost of your first cabinet coordination meeting.";
            case OUTREACH -> "Outreach records reduce your first service review's cost by $75.";
        };
    }
    private void beginTerm() {
        phase = CareerView.Phase.PRESIDENCY;
        termMonths = 0; annualWork.clear(); returnWork.clear(); budgetPublished = false;
        presidency = new Presidency(seed + cycle * 7919L, 2028 + year, pledges, transitionCredits, publicFeedback);
        syncOffice();
        history.add("INAUGURATION: term " + wins + " begins. 48 monthly turns, two actions each month; end the month explicitly. Midterms follow month 24.");
    }
    private int governanceCost(CareerCommand.GovernanceAction action) { return presidency == null ? action.cost() : presidency.cost(action); }
    private void syncOffice() {
        Presidency.View p = presidency.view(); treasury = p.treasury(); publicFeedback = p.publicFeedback(); support = p.support();
        annualWork.clear(); annualWork.addAll(presidency.annualWork());
        transitionCredits.clear(); transitionCredits.addAll(presidency.credits());
    }
    private void govern(CareerCommand.GovernanceAction action) {
        history.addAll(presidency.act(action)); syncOffice();
    }
    private void endMonth() {
        history.addAll(presidency.endMonth(eventFrequency));
        termMonths++; servedMonths++; syncOffice();
        if (termMonths % 12 == 0) {
            captureAnnualRecord(); year++;
            history.add("Annual record closed. Completed work: " + annualDescription() + ".");
        }
        if (termMonths == MONTHS_PER_TERM) {
            completedTerms++;
            long kept = presidency.view().promises().stream().filter(p -> p.status().equals("Kept")).count();
            long contradicted = presidency.view().promises().stream().filter(p -> p.status().startsWith("Contradicted")).count();
            if (kept > 0) returnWork.add(State.Task.OUTREACH);
            if (contradicted > 0) { returnWork.remove(State.Task.OUTREACH); history.add("Contradicted campaign promises withhold the inherited outreach objective."); }
            history.add("Term archive: " + presidency.view().lawsPassed() + " laws signed; " + presidency.view().billsVetoed() + " vetoes; promises kept " + kept + ".");
            presidency.view().correspondence().requests().forEach(r -> history.add("Archived request: " + r.group() + " / " + r.request() + " / " + r.status()));
            presidency.view().laws().forEach(law -> history.add("Archived law: " + law));
            presidency.view().promises().forEach(p -> history.add("Archived promise: " + p.issue() + " / " + p.approach() + " / " + p.status()));
            if (wins >= MAX_TERMS || servedMonths >= MAX_TERMS * MONTHS_PER_TERM) {
                phase = CareerView.Phase.RETIRED;
                history.add("Eight years of service complete. The two-term career limit has been reached.");
            } else { phase = CareerView.Phase.TERM_REVIEW; history.add("Four-year term complete. Review your record, then run again or retire."); }
        }
    }
    private String annualDescription() {
        return annualWork.stream().map(Object::toString).collect(java.util.stream.Collectors.joining(", "));
    }
    private void captureAnnualRecord() {
        returnWork.clear();
        if (annualWork.contains(CareerCommand.GovernanceAction.PUBLIC_BRIEFING)) returnWork.add(State.Task.TOWN_HALL);
        if (annualWork.contains(CareerCommand.GovernanceAction.CABINET_MEETING)) returnWork.add(State.Task.FIELD_OFFICE);
        if (annualWork.contains(CareerCommand.GovernanceAction.SERVICE_REVIEW)) returnWork.add(State.Task.OUTREACH);
        budgetPublished = annualWork.contains(CareerCommand.GovernanceAction.BUDGET_REVIEW);
        if (!returnWork.contains(State.Task.TOWN_HALL)) publicFeedback = "The annual correspondence log records requests for an unanswered public briefing.";
        if (!returnWork.contains(State.Task.FIELD_OFFICE)) support = "The annual cabinet coordination meeting remains outstanding.";
    }
    private void rebuild(CareerCommand.RebuildAction action) {
        switch (action) {
            case CAMPAIGN_REVIEW -> { campaignReviewDue = false; returnWork.add(State.Task.TOWN_HALL); publicFeedback = "A campaign review addressing the loss has been published; the election record is retained."; }
            case DONOR_MEETINGS -> { donorReviewDue = false; support = "Donor meetings are complete; the withheld campaign reserve is available again."; }
            case COMMUNITY_WORK -> { communityReconnected = true; returnWork.add(State.Task.OUTREACH); }
            case PRIVATE_LIFE -> { }
        }
        comebackYears++; year++;
        history.add("Year out of office " + comebackYears + " / 4: " + action + ".");
        if (comebackYears == 4) history.add("The next election cycle is open. You may run again or retire.");
    }
    private List<String> nextCampaignEffects() {
        List<String> effects = new ArrayList<>();
        if (donorReviewDue) effects.add("Unresolved donor meeting: next campaign cash -$200.");
        else if (losses > 0) effects.add("Donor meeting resolved: no loss-related cash withholding; past losses remain in history.");
        if (budgetPublished) effects.add("Published final-year accounts: a fictional private fundraising event adds $100 to the next campaign. No treasury transfer occurs.");
        for (State.Task task : returnWork) effects.add("Prepared records supply one completed " + task + " objective in an eligible state next campaign.");
        if (effects.isEmpty()) effects.add("No inherited cash or completed objectives are currently prepared.");
        return effects;
    }
    private void startNextCampaign() {
        int adjustment = (donorReviewDue ? -200 : 0) + (budgetPublished ? 100 : 0);
        cycle++;
        long nextSeed = seed + (cycle - 1L) * 104729L;
        ElectoralCollege roster = new ElectoralCollege(nextSeed);
        List<CampaignOpening.Work> work = new ArrayList<>();
        Set<String> usedStates = new java.util.HashSet<>();
        for (State.Task task : returnWork) {
            State target = roster.getAllStates().stream().filter(s -> s.getObjectives().contains(task) && !usedStates.contains(s.getName())).findFirst().orElseThrow();
            usedStates.add(target.getName()); work.add(new CampaignOpening.Work(target.getName(), task));
        }
        List<String> reasons = new ArrayList<>(nextCampaignEffects());
        for (CampaignOpening.Work item : work) reasons.add("Inherited " + item.task() + " preparation in " + item.state() + ".");
        campaign = new GameEngine(nextSeed, difficulty, mate, new CampaignOpening(adjustment, work, reasons));
        latestResult = null; latestOutcome = ""; comebackYears = 0;
        phase = CareerView.Phase.CAMPAIGN;
        history.add("Campaign " + cycle + " begins in career year " + year + "."); history.addAll(reasons);
        // Carryover is consumed once; it is rebuilt by the next term or opposition period.
        returnWork.clear(); budgetPublished = false; pledges.clear();
    }
    private String developerProblem(CareerCommand.DeveloperAction action) {
        return switch (action) {
            case FORCE_WIN, FORCE_LOSS -> phase != CareerView.Phase.CAMPAIGN ? "Force an outcome only from an active campaign."
                : action == CareerCommand.DeveloperAction.FORCE_WIN && wins >= MAX_TERMS ? "Two victories already recorded." : "";
            case ADD_CAMPAIGN_CASH -> phase != CareerView.Phase.CAMPAIGN ? "Campaign cash is available only during a campaign." : "";
            case ADD_TREASURY, FINISH_TERM, ADVANCE_MONTH, CHANGE_CONGRESS, TRIGGER_EVENT -> phase != CareerView.Phase.PRESIDENCY ? "This developer action requires an active presidency." : (action == CareerCommand.DeveloperAction.TRIGGER_EVENT || action == CareerCommand.DeveloperAction.ADVANCE_MONTH) && presidency.view().pendingEvent() != null ? "Resolve the pending event first." : "";
            default -> "";
        };
    }
    private void developer(CareerCommand.DeveloperAction action) {
        switch (action) {
            case FORCE_WIN -> recordElection(null, true);
            case FORCE_LOSS -> recordElection(null, false);
            case ADD_CAMPAIGN_CASH -> { campaign.developerGrantFunds(); history.add("Campaign cash +$500."); }
            case ADD_TREASURY -> { presidency.grantFunds(); syncOffice(); history.add("Public treasury +$500."); }
            case FINISH_TERM -> finishTerm();
            case ADVANCE_MONTH -> endMonth();
            case CHANGE_CONGRESS -> { presidency.toggleCongress(); history.add("Developer chamber fixture changed; existing agreement cleared."); }
            case TRIGGER_EVENT -> presidency.triggerEvent(history);
            case MIDTERM_START -> scenario(1, 12);
            case MID_FIRST_TERM -> scenario(1, 24);
            case MID_SECOND_TERM -> scenario(2, 24);
            case FINAL_QUARTER -> scenario(2, 47);
            case REELECTION_START -> { scenario(1, 47); finishTerm(); startNextCampaign(); }
        }
    }
    private void finishTerm() {
        while (phase == CareerView.Phase.PRESIDENCY) {
            if (presidency.view().pendingEvent() != null) { history.addAll(presidency.office(OfficeCommand.choice(OfficeCommand.Type.EVENT_CHOICE, 1))); syncOffice(); }
            endMonth();
        }
    }
    private void scenario(int term, int month) {
        wins = term; losses = 0; completedTerms = term - 1; servedMonths = (term - 1) * 48;
        year = 1 + servedMonths / 12; cycle = term; comebackYears = 0;
        donorReviewDue = false; campaignReviewDue = false; communityReconnected = false;
        budgetPublished = false; returnWork.clear(); annualWork.clear(); transitionCredits.clear(); elections.clear(); pledges.clear();
        latestResult = null; latestOutcome = "WIN";
        campaign = new GameEngine(seed + (cycle - 1L) * 104729L, difficulty, mate);
        for (int i = 1; i <= term; i++) elections.add(new CareerView.ElectionSummary(i, (i - 1) * 4 + 1, "WIN", true, null, null,
            List.of("Developer fixture; electoral tally bypassed.")));
        publicFeedback = "Developer scenario: no campaign work was completed.";
        beginTerm();
        for (int i = 0; i < month; i++) {
            if (presidency.view().pendingEvent() != null) history.addAll(presidency.office(OfficeCommand.choice(OfficeCommand.Type.EVENT_CHOICE, 1)));
            endMonth();
        }
        history.add("Timeline replaced with developer fixture; prior entries are development history. Service: " + servedMonths + " months.");
    }
    public CareerView view() {
        List<CareerView.GovernanceOption> options = new ArrayList<>();
        for (CareerCommand.GovernanceAction action : CareerCommand.GovernanceAction.values()) {
            int cost = governanceCost(action);
            String reason = phase != CareerView.Phase.PRESIDENCY ? "You are not currently in office."
                : presidency.actionProblem(cost);
            options.add(new CareerView.GovernanceOption(action, cost, reason.isEmpty(), reason));
        }
        String reputation = losses == 0 ? (wins == 0 ? "First-time candidate; no election result recorded." : "An elected term is on the career record.")
            : "Past election loss remains on record. " + (campaignReviewDue ? "Campaign review outstanding." : "Campaign review published.");
        String economy = (treasury >= ROUTINE_EXPENSES ? "Cash reserve covers the next routine operating bill. " : "Cash reserve is below the routine operating bill. ")
            + (annualWork.contains(CareerCommand.GovernanceAction.BUDGET_REVIEW) ? "Annual accounts reconciled." : "Annual reconciliation pending.");
        return new CareerView(phase, year, cycle, wins, losses, completedTerms, termMonths, servedMonths, comebackYears,
            treasury, RECEIPTS, ROUTINE_EXPENSES, publicFeedback, reputation,
            support + (communityReconnected ? " Community volunteer contacts have been renewed." : ""), economy,
            transitionCredits.stream().map(this::transitionDescription).toList(), annualWork.stream().map(Object::toString).toList(),
            nextCampaignEffects(), options, elections, history, campaign.view(), developerUsed, presidency == null ? null : presidency.view(), eventFrequency,
            pledges.entrySet().stream().map(e -> new Policy.Promise(e.getKey(), e.getValue(), "Campaign pledge")).toList());
    }
}
