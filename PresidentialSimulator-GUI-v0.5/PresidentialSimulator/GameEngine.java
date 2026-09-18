import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/** Single-session game engine. All gameplay is independent of presentation and file I/O. */
public final class GameEngine {
    private record Modifier(CampaignEvent.Side side, CampaignEvent.Kind kind, State.Task task,
                            int amount, int throughTurn, String source) { }
    private record Pending(CampaignEvent event, String target) { }
    private final long seed;
    private final President.Difficulty difficulty;
    private final President.RunningMate mate;
    private final ElectoralCollege college;
    private final President player;
    private final President opponent;
    private final Random eventRandom;
    private final Random opponentRandom;
    private final List<CampaignEvent> catalog;
    private final Set<String> usedEvents = new HashSet<>();
    private final List<Modifier> modifiers = new ArrayList<>();
    private final List<String> history = new ArrayList<>();
    private final List<GameCommand> journal = new ArrayList<>();
    private int turn;
    private Pending pending;
    private final SyntheticElectorate electorate;
    private final boolean timedDecisions;
    private boolean advancedDebates;
    private DebateSession debate;
    private PressureEvent liveDebate;
    private final List<LiveDebate.Statement> debateMemory=new ArrayList<>();
    private int transcriptCursor;
    private final List<DebateSession.Result> debateResults=new ArrayList<>();
    private double debateShare;

    private int pendingTurn, pendingSeconds;
    private final Set<String> answeredDebates = new HashSet<>();
    public List<DebateSession.Result> debateResults(){return List.copyOf(debateResults);}
    public double debateShare(){return debateShare;}
    public boolean timedDecisions() { return timedDecisions; }
    public boolean synthetic() { return electorate != null; }

    public GameEngine(long seed, President.Difficulty difficulty, President.RunningMate mate) {
        this(seed, difficulty, mate, EventCatalog.all(), CampaignOpening.fresh());
    }
    GameEngine(long seed, President.Difficulty difficulty, President.RunningMate mate, List<CampaignEvent> catalog) {
        this(seed, difficulty, mate, catalog, CampaignOpening.fresh());
    }
    public GameEngine(long seed, President.Difficulty difficulty, President.RunningMate mate, CampaignOpening opening) {
        this(seed, difficulty, mate, EventCatalog.all(), opening);
    }
    private GameEngine(long seed, President.Difficulty difficulty, President.RunningMate mate, List<CampaignEvent> catalog, CampaignOpening opening) {
        this(seed,difficulty,mate,catalog,opening,false,0);
    }
    public GameEngine(long seed, President.Difficulty difficulty, President.RunningMate mate, CampaignOpening opening, boolean world, double recordSwing) {
        this(seed,difficulty,mate,EventCatalog.all(),opening,world,recordSwing);
    }
    private GameEngine(long seed, President.Difficulty difficulty, President.RunningMate mate, List<CampaignEvent> catalog, CampaignOpening opening, boolean world, double recordSwing) {
        this(seed,difficulty,mate,catalog,opening,world,recordSwing,false);
    }
    public GameEngine(long seed, President.Difficulty difficulty, President.RunningMate mate, CampaignOpening opening, boolean world, double recordSwing, boolean timed) {
        this(seed,difficulty,mate,timed?CampaignStories.all():EventCatalog.all(),opening,world,recordSwing,timed);
    }
    public GameEngine(long seed, President.Difficulty difficulty, President.RunningMate mate, CampaignOpening opening, boolean world, double recordSwing, boolean timed, boolean advanced){
        this(seed,difficulty,mate,opening,world,recordSwing,timed);this.advancedDebates=advanced;
    }
    private GameEngine(long seed, President.Difficulty difficulty, President.RunningMate mate, List<CampaignEvent> catalog, CampaignOpening opening, boolean world, double recordSwing, boolean timed) {
        this.timedDecisions=timed;
        electorate = world ? new SyntheticElectorate(seed,recordSwing,difficulty) : null;
        this.seed = seed; this.difficulty = Objects.requireNonNull(difficulty); this.mate = Objects.requireNonNull(mate);
        college = new ElectoralCollege(seed);
        player = new President((difficulty == President.Difficulty.HARD ? 1000 : 1400) + (mate == President.RunningMate.FUNDRAISER ? 200 : 0), mate);
        opponent = new President(difficulty == President.Difficulty.HARD ? 1600 : 1200, President.RunningMate.FUNDRAISER);
        eventRandom = new Random(seed ^ 0x51A7E5L);
        opponentRandom = new Random(seed ^ 0x0B07L);
        this.catalog = List.copyOf(catalog);
        if (catalog.stream().map(CampaignEvent::id).distinct().count() != catalog.size()) throw new IllegalArgumentException("Duplicate event IDs");
        Objects.requireNonNull(opening);
        for (CampaignOpening.Work work : opening.completedWork()) {
            State target = state(work.state());
            if (target == null || !target.getObjectives().contains(work.task())) throw new IllegalArgumentException("Invalid inherited work");
        }
        player.changeFunds(opening.cashAdjustment());
        for (CampaignOpening.Work work : opening.completedWork()) player.complete(work.state(), work.task());
        history.add("Campaign opened. Seed " + seed + "; " + difficulty + "; running mate " + mate + ".");
        history.add("Starting funds: you $" + player.getFunds() + ", opponent $" + opponent.getFunds() + ".");
        history.addAll(opening.reasons());
    }
    public long seed() { return seed; }
    public President.Difficulty difficulty() { return difficulty; }
    public President.RunningMate runningMate() { return mate; }
    public List<GameCommand> journal() { return List.copyOf(journal); }
    public State[] states() { return college.getStates(); }
    public State districtOfColumbia() { return college.getDistrictOfColumbia(); }
    private State state(String name) {
        return college.getAllStates().stream().filter(s -> s.getName().equals(name)).findFirst().orElse(null);
    }
    private President actor(CampaignEvent.Side side) { return side == CampaignEvent.Side.PLAYER ? player : opponent; }
    private String who(CampaignEvent.Side side) { return side == CampaignEvent.Side.PLAYER ? "You" : "Opponent"; }
    private boolean applies(Modifier m, CampaignEvent.Side side, int actionTurn) {
        return (m.side() == side || m.side() == CampaignEvent.Side.BOTH) && actionTurn <= m.throughTurn();
    }
    private int price(CampaignEvent.Side side, State.Task task, int actionTurn) {
        int amount = actor(side).baseCost(task);
        for (Modifier m : modifiers) if (applies(m, side, actionTurn) && m.kind() == CampaignEvent.Kind.ACTION_COST && m.task() == task) amount += m.amount();
        return Math.max(25, amount);
    }
    private int fundraising(CampaignEvent.Side side, int actionTurn) {
        int amount = 250;
        for (Modifier m : modifiers) if (applies(m, side, actionTurn) && m.kind() == CampaignEvent.Kind.FUNDRAISING) amount += m.amount();
        return Math.max(50, amount);
    }
    private SyntheticElectorate.Vote tally(State s, boolean election) { return electorate.vote(s, player.getCompletedTasks(s.getName()), opponent.getCompletedTasks(s.getName()), election, supportSwing()); }
    private double supportSwing() { return debateShare + modifiers.stream().filter(m -> m.kind()==CampaignEvent.Kind.SUPPORT && turn<=m.throughTurn()).mapToDouble(m -> (m.side()==CampaignEvent.Side.PLAYER ? 1 : m.side()==CampaignEvent.Side.OPPONENT ? -1 : 0)*m.amount()/100.0).sum(); }
    private boolean controls(State s) {
        if (electorate != null) { var t=tally(s,turn == President.CAMPAIGN_TURNS && pending == null); return t.player()>t.opponent(); }
        boolean yours = player.getCompletedTasks(s.getName()).containsAll(s.getObjectives());
        boolean theirs = opponent.getCompletedTasks(s.getName()).containsAll(s.getObjectives());
        return yours == theirs ? s.hasStartingSupport() : yours;
    }
    private String explanation(State s) {
        if (electorate != null) return electorate.explanation(s,player.getCompletedTasks(s.getName()),opponent.getCompletedTasks(s.getName()),turn == President.CAMPAIGN_TURNS && pending == null,supportSwing());
        boolean yours = player.getCompletedTasks(s.getName()).containsAll(s.getObjectives());
        boolean theirs = opponent.getCompletedTasks(s.getName()).containsAll(s.getObjectives());
        if (yours && !theirs) return "Only your campaign has completed both objectives.";
        if (theirs && !yours) return "Only the opponent has completed both objectives.";
        return (yours ? "Both campaigns completed both objectives; " : "Neither campaign completed both objectives; ")
            + (s.hasStartingSupport() ? "your" : "opponent's") + " starting ownership applies.";
    }
    private Map<String, Boolean> ownership() {
        Map<String, Boolean> owners = new LinkedHashMap<>();
        for (State s : college.getAllStates()) owners.put(s.getName(), controls(s));
        return owners;
    }
    private void logFlips(Map<String, Boolean> before) {
        for (State s : college.getAllStates()) if (before.get(s.getName()) != controls(s))
            history.add("BOARD: " + s.getName() + " (" + s.getElectoralVotes() + " EV) now belongs to " + (controls(s) ? "you" : "the opponent") + ".");
    }
    private String actionProblem(State s, State.Task task, CampaignEvent.Side side, int actionTurn) {
        if (s == null || task == null) return "Choose a valid state and action.";
        if (!s.getObjectives().contains(task)) return "This is not an objective in that state.";
        if (actor(side).getCompletedTasks(s.getName()).contains(task)) return "Already completed in this state.";
        if (actor(side).getFunds() < price(side, task, actionTurn)) return "Not enough funds; fundraise first.";
        return "";
    }
    public TurnReport submit(GameCommand command) {
        if (command == null || command.type() == null) return reject("Choose an action.");
        if (!timedDecisions && pending != null && command.type() != GameCommand.Type.RESPOND) return reject("Resolve the event before taking another turn.");
        if (pending == null && command.type() == GameCommand.Type.RESPOND) return reject("There is no event awaiting a response.");
        if (turn == President.CAMPAIGN_TURNS && pending == null) return reject("This campaign is complete.");
        if(command.type()==GameCommand.Type.LEAVE_LIVE){
            if(!timedDecisions||pending==null||(pendingSeconds<=0&&liveDebate==null)||command.state()!=null||command.task()!=null||command.choice()!=-1)return reject("No live appearance to leave.");
            int start=history.size();if(liveDebate!=null){liveDebate.leave();syncLiveDebate();}else if(debate!=null){while(debate!=null)finishDebateAnswer(-1);}else expireDecision("LIVE APPEARANCE ENDED");journal.add(command);return new TurnReport(true,history.subList(start,history.size()),view());
        }
        if (command.type()==GameCommand.Type.CLOCK_TICK) {
            if(!timedDecisions || pending==null || pendingSeconds<=0 || command.state()!=null || command.task()!=null || command.choice()<1 || command.choice()>3600)return reject("No valid live response clock.");
            int start=history.size();pendingSeconds=Math.max(0,pendingSeconds-command.choice());
            if(pendingSeconds==0)expireDecision("TIME EXPIRED");
            journal.add(command);return new TurnReport(true,history.subList(start,history.size()),view());
        }
        if((debate!=null||liveDebate!=null) && command.type()!=GameCommand.Type.RESPOND)return reject("Finish or leave the live debate before taking a campaign turn.");
        if (turn>=President.CAMPAIGN_TURNS && command.type()!=GameCommand.Type.RESPOND)return reject("No campaign turns remain.");
        if ((command.type() != GameCommand.Type.CAMPAIGN && (command.state() != null || command.task() != null))
            || (command.type() != GameCommand.Type.RESPOND && command.choice() != -1)) return reject("Malformed command.");
        int start = history.size();
        Map<String, Boolean> before = ownership();
        if (command.type() == GameCommand.Type.RESPOND) {
            if (command.choice() < 0 || command.choice() >= pending.event().options().size()) return reject("Choose a listed response.");
            CampaignEvent.Option choice = pending.event().options().get(command.choice());
            if (!affordable(choice.effects())) return reject("You cannot afford that response. Choose another option.");
            history.add("RESPONSE: " + choice.label());
            apply(choice.effects(), pending.target(), pending.event().title());
            if(liveDebate!=null){liveDebate.respond(command.choice());syncLiveDebate();}
            else if(debate!=null)finishDebateAnswer(command.choice());
            else {
                if(timedDecisions && pending.event().id().startsWith("debate_"))answeredDebates.add(pending.event().id());
                pending = null;pendingTurn=0;pendingSeconds=0;
            }
        } else {
            int actionTurn = turn + 1;
            State target = state(command.state());
            if (command.type() == GameCommand.Type.CAMPAIGN) {
                String problem = actionProblem(target, command.task(), CampaignEvent.Side.PLAYER, actionTurn);
                if (!problem.isEmpty()) return reject(problem);
            }
            turn = actionTurn;
            history.add("TURN " + turn + " / " + President.CAMPAIGN_TURNS);
            switch (command.type()) {
                case CAMPAIGN -> perform(CampaignEvent.Side.PLAYER, target, command.task());
                case FUNDRAISE -> raise(CampaignEvent.Side.PLAYER);
                case REST -> history.add("You passed this turn without completing an objective.");
                default -> throw new IllegalStateException("Unreachable command type");
            }
            // The opponent acts every second turn. Events may affect either side every turn.
            if (turn % 2 == 0) opponentTurn();
            else history.add("Opponent's next scheduled action is after turn " + (turn + 1) + ".");
            if(timedDecisions && pending!=null && turn>=pendingTurn)expireDecision("DEADLINE MISSED");
            if(pending==null)drawEvent();
        }
        logFlips(before);
        journal.add(command);
        if (turn == President.CAMPAIGN_TURNS && pending == null) history.add("Election ready: all turns and event responses are complete.");
        return new TurnReport(true, history.subList(start, history.size()), view());
    }
    private void syncLiveDebate(){
        var entries=liveDebate.transcript();
        history.addAll(entries.subList(transcriptCursor,entries.size()));transcriptCursor=entries.size();
        if(liveDebate.complete()){liveDebate=null;pending=null;pendingTurn=0;pendingSeconds=0;return;}
        pending=new Pending(liveDebate.event(),null);pendingSeconds=liveDebate.seconds();pendingTurn=turn;
    }
    void inheritDebateStatements(List<LiveDebate.Statement> previous){if(turn!=0)throw new IllegalStateException("Campaign already started");debateMemory.addAll(previous);}
    public List<LiveDebate.Statement> debateStatements(){return List.copyOf(debateMemory);}
    private void finishDebateAnswer(int choice){
        var r=debate.answer(choice);
        history.add("DEBATE: Question "+r.number()+" | You "+r.playerScore()+", opponent "+r.opponentScore()+". "+r.opponent());
        if(!debate.complete()){pending=new Pending(debate.question(),null);pendingSeconds=60;return;}
        var result=debate.result();debateResults.add(result);debateShare+=result.lastingShare();
        if(result.rounds().stream().anyMatch(x->!x.missed()))answeredDebates.add(debate.rootId());
        history.add(String.format(java.util.Locale.ROOT,"DEBATE RESULT: %s | election-day share effect %+.3f pp; all debates %+.3f pp. Saved in campaign record.",result.topic(),result.lastingShare(),debateShare));
        debate=null;pending=null;pendingTurn=0;pendingSeconds=0;
    }
    private void expireDecision(String reason) {
        if(liveDebate!=null){liveDebate.timeout();syncLiveDebate();return;}
        if(debate!=null){history.add(reason+": No answer to this question; the moderator moves on.");finishDebateAnswer(-1);return;}
        String id=pending.event().id();history.add(reason+": "+pending.event().title()+". "+DecisionDeadline.campaignMissedText(id));
        apply(DecisionDeadline.campaignMissed(id),pending.target(),"Missed response");pending=null;pendingTurn=0;pendingSeconds=0;
    }
    private TurnReport reject(String text) { return new TurnReport(false, List.of(text), view()); }
    private void perform(CampaignEvent.Side side, State target, State.Task task) {
        int cost = price(side, task, turn);
        actor(side).changeFunds(-cost);
        actor(side).complete(target.getName(), task);
        history.add(who(side) + ": " + task + " in " + target.getName() + " (-$" + cost + ").");
    }
    private void raise(CampaignEvent.Side side) {
        int amount = fundraising(side, turn);
        actor(side).changeFunds(amount);
        history.add(who(side) + " fundraised (+$" + amount + ").");
    }
    private void opponentTurn() {
        List<State> candidates = college.getAllStates().stream()
            .filter(s -> !opponent.getCompletedTasks(s.getName()).containsAll(s.getObjectives()))
            .filter(s -> controls(s) || !player.getCompletedTasks(s.getName()).isEmpty()).toList();
        if (candidates.isEmpty()) { raise(CampaignEvent.Side.OPPONENT); return; }
        // Finish ongoing projects first; otherwise choose a seeded random eligible state.
        List<State> ongoing = candidates.stream().filter(s -> !opponent.getCompletedTasks(s.getName()).isEmpty()).toList();
        List<State> pool = ongoing.isEmpty() ? candidates : ongoing;
        State target = pool.get(opponentRandom.nextInt(pool.size()));
        List<State.Task> missing = target.getObjectives().stream().filter(t -> !opponent.getCompletedTasks(target.getName()).contains(t)).sorted().toList();
        State.Task task = missing.stream().min(Comparator.comparingInt(t -> price(CampaignEvent.Side.OPPONENT, t, turn))).orElseThrow();
        if (opponent.getFunds() < price(CampaignEvent.Side.OPPONENT, task, turn)) raise(CampaignEvent.Side.OPPONENT);
        else perform(CampaignEvent.Side.OPPONENT, target, task);
    }
    private boolean hasTaskEffect(CampaignEvent event) {
        return event.allEffects().stream().anyMatch(e -> e.kind() == CampaignEvent.Kind.COMPLETE_TASK || e.kind() == CampaignEvent.Kind.REMOVE_TASK);
    }
    private List<State> targets(CampaignEvent event) {
        return college.getAllStates().stream().filter(s -> event.allEffects().stream().allMatch(e -> {
            if (e.kind() != CampaignEvent.Kind.COMPLETE_TASK && e.kind() != CampaignEvent.Kind.REMOVE_TASK) return true;
            boolean done = actor(e.side()).getCompletedTasks(s.getName()).contains(e.task());
            return s.getObjectives().contains(e.task()) && (e.kind() == CampaignEvent.Kind.REMOVE_TASK ? done : !done);
        })).toList();
    }
    private boolean eligible(CampaignEvent event) {
        if(timedDecisions && (event.id().startsWith("debate_") || event.id().equals("fact_check")))return false;
        if(timedDecisions && !event.options().isEmpty() && (turn==President.CAMPAIGN_TURNS || Set.of(3,7,11).contains(turn)))return false;
        if (turn == President.CAMPAIGN_TURNS && event.allEffects().stream().anyMatch(e -> e.duration() > 0)) return false;
        return !hasTaskEffect(event) || !targets(event).isEmpty();
    }
    private void drawEvent() {
        CampaignEvent event;
        String debate=timedDecisions?CampaignStories.debateAt(turn):null;
        if(debate!=null && advancedDebates){liveDebate=new LiveDebate(debate.substring(7),seed,debateMemory);transcriptCursor=0;syncLiveDebate();return;}
        else if(debate!=null)event=CampaignStories.byId(debate);
        else if(timedDecisions && turn==6 && answeredDebates.contains("debate_economy"))event=CampaignStories.byId("fact_check");
        else {
            List<CampaignEvent> possible=catalog.stream().filter(e->!usedEvents.contains(e.id())&&eligible(e)).toList();
            if(possible.isEmpty()){history.add("NEWS: No new applicable event this turn.");return;}
            event=possible.get(eventRandom.nextInt(possible.size()));
        }
        usedEvents.add(event.id());
        String target = null;
        if (hasTaskEffect(event)) {
            List<State> candidates = targets(event);
            target = candidates.get(eventRandom.nextInt(candidates.size())).getName();
        }
        history.add("NEWS [" + event.id() + "]: " + event.title() + (target == null ? "" : " - " + target));
        history.add(event.description());
        if (event.options().isEmpty()) apply(event.effects(), target, event.title());
        else { pending = new Pending(event, target); if(timedDecisions){pendingTurn=Math.min(President.CAMPAIGN_TURNS,turn+DecisionDeadline.campaignTurns(event.id()));pendingSeconds=DecisionDeadline.campaignSeconds(event.id());} }
    }
    private boolean affordable(List<CampaignEvent.Effect> effects) {
        int playerBalance = player.getFunds(), opponentBalance = opponent.getFunds();
        for (CampaignEvent.Effect e : effects) if (e.kind() == CampaignEvent.Kind.FUNDS) {
            if (e.side() != CampaignEvent.Side.OPPONENT) { playerBalance += e.amount(); if (playerBalance < 0) return false; }
            if (e.side() != CampaignEvent.Side.PLAYER) { opponentBalance += e.amount(); if (opponentBalance < 0) return false; }
        }
        return true;
    }
    private void apply(List<CampaignEvent.Effect> effects, String target, String source) {
        for (CampaignEvent.Effect e : effects) {
            if (e.duration() > 0) {
                modifiers.add(new Modifier(e.side(), e.kind(), e.task(), e.amount(), turn + e.duration(), source));
                if(e.kind()==CampaignEvent.Kind.SUPPORT){history.add("Effect: "+e.side()+String.format(java.util.Locale.ROOT," support %+.2f pp through turn %d.",e.amount()/100.0,turn+e.duration()));continue;}
                history.add("Effect: " + e.side() + " " + (e.task() == null ? "fundraising proceeds" : e.task() + " cost")
                    + " " + signedMoney(e.amount()) + " through turn " + (turn + e.duration()) + ".");
                continue;
            }
            List<CampaignEvent.Side> sides = e.side() == CampaignEvent.Side.BOTH
                ? List.of(CampaignEvent.Side.PLAYER, CampaignEvent.Side.OPPONENT) : List.of(e.side());
            for (CampaignEvent.Side side : sides) {
                President team = actor(side);
                switch (e.kind()) {
                    case FUNDS -> {
                        int actual = team.changeFunds(e.amount());
                        history.add("Effect: " + who(side) + " " + signedMoney(actual) + "; funds $" + team.getFunds()
                            + (actual != e.amount() ? " (loss capped at available cash)" : "") + ".");
                    }
                    case COMPLETE_TASK -> { team.complete(target, e.task()); history.add("Effect: " + who(side) + " completed " + e.task() + " in " + target + "."); }
                    case REMOVE_TASK -> { team.remove(target, e.task()); history.add("Effect: " + who(side) + " must redo " + e.task() + " in " + target + "."); }
                    default -> throw new IllegalStateException("Unknown immediate effect");
                }
            }
        }
    }
    private static String signedMoney(int amount) { return (amount < 0 ? "-$" : "+$") + Math.abs(amount); }
    public GameView view() {
        List<GameView.StateView> views = new ArrayList<>();
        int ev = 0;
        boolean complete = turn == President.CAMPAIGN_TURNS && pending == null;
        for (State s : college.getAllStates()) {
            boolean owner = controls(s); if (electorate != null) ev += tally(s,complete).playerEV(); else if (owner) ev += s.getElectoralVotes();
            List<GameView.ActionView> actions = new ArrayList<>();
            for (State.Task task : s.getObjectives().stream().sorted().toList()) {
                String reason = liveDebate!=null ? "Finish or leave the debate first." : complete ? "Campaign complete." : pending != null && !timedDecisions ? "Resolve the current event first."
                    : actionProblem(s, task, CampaignEvent.Side.PLAYER, turn + 1);
                actions.add(new GameView.ActionView(task, price(CampaignEvent.Side.PLAYER, task, turn + 1), reason.isEmpty(), reason));
            }
            views.add(new GameView.StateView(s.getName(), s.getElectoralVotes(), owner, s.hasStartingSupport(), s.getObjectives(),
                player.getCompletedTasks(s.getName()), opponent.getCompletedTasks(s.getName()), actions, explanation(s)));
        }
        GameView.PendingEvent eventView = null;
        if (pending != null) eventView = new GameView.PendingEvent(pending.event().id(), pending.event().title(), pending.event().description(), pending.target(),
            pending.event().options().stream().map(o -> new GameView.ChoiceView(o.label(), affordable(o.effects()), affordable(o.effects()) ? "" : "Not enough funds.")).toList(),pendingTurn,pendingSeconds,liveDebate!=null?liveDebate.missed():debate!=null?"No answer scores -2; the moderator moves to the next question.":timedDecisions?DecisionDeadline.campaignMissedText(pending.event().id()):"");
        List<String> active = modifiers.stream().filter(m -> turn + 1 <= m.throughTurn()).map(m -> m.source() + ": " + m.side()
            + " " + (advancedDebates && m.kind()==CampaignEvent.Kind.SUPPORT ? String.format(java.util.Locale.ROOT,"support %+.2f pp",m.amount()/100.0) : (m.task() == null ? "fundraising proceeds" : m.task() + " cost") + " " + signedMoney(m.amount()))
            + " (" + (m.throughTurn() - turn) + " turn(s) left)").toList();
        return new GameView(seed, turn, President.CAMPAIGN_TURNS, player.getFunds(), opponent.getFunds(), ev, 538 - ev, complete,
            views, active, eventView, history, fundraising(CampaignEvent.Side.PLAYER, turn + 1), debateResults, debateShare);
    }
    void developerGrantFunds() {
        player.changeFunds(500);
        history.add("DEVELOPER: added $500 campaign cash.");
    }
    public ElectionResult result() {
        if (!view().complete()) throw new IllegalStateException("Finish turns and pending events first");
        Map<String, String> reasons = new LinkedHashMap<>();
        for (State s : college.getAllStates()) reasons.put(s.getName(), explanation(s));
        if (electorate != null) { Map<String,SyntheticElectorate.Vote> votes=new LinkedHashMap<>(); for(State s:college.getAllStates())votes.put(s.getName(),tally(s,true)); return new ElectionResult(states(),districtOfColumbia(),ownership(),reasons,history,votes); }
        return new ElectionResult(states(), districtOfColumbia(), ownership(), reasons, history);
    }
}
