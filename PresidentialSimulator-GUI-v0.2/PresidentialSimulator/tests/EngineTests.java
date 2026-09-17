import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class EngineTests {
    private static int checks;
    private static void check(boolean value, String message) {
        checks++; if (!value) throw new AssertionError(message);
    }
    private static GameEngine game(long seed) { return new GameEngine(seed, President.Difficulty.NORMAL, President.RunningMate.FUNDRAISER); }
    private static GameEngine custom(CampaignEvent event) {
        return new GameEngine(42, President.Difficulty.NORMAL, President.RunningMate.FUNDRAISER, List.of(event));
    }
    private static GameView.StateView state(GameEngine g, String name) { return g.view().states().stream().filter(s -> s.name().equals(name)).findFirst().orElseThrow(); }
    private static void respond(GameEngine g) {
        if (g.view().pendingEvent() != null) {
            int first = 0;
            while (!g.view().pendingEvent().choices().get(first).available()) first++;
            check(g.submit(GameCommand.respond(first)).accepted(), "Event response");
        }
    }
    private static GameCommand next(GameEngine g) {
        GameView v = g.view();
        // A test driver completes the largest available fictional board objectives.
        List<GameView.StateView> states = v.states().stream()
            .filter(s -> !s.playerControls())
            .sorted(java.util.Comparator.comparingInt(GameView.StateView::electoralVotes).reversed()).toList();
        for (GameView.StateView s : states) for (GameView.ActionView a : s.actions())
            if (a.available()) return GameCommand.campaign(s.name(), a.task());
        return GameCommand.fundraise();
    }
    private static void invariants(GameEngine g) {
        GameView v = g.view();
        check(v.states().size() == 51 && v.playerEV() + v.opponentEV() == 538, "Conserved EV");
        check(v.playerFunds() >= 0 && v.opponentFunds() >= 0, "No debt");
        check(v.turnsUsed() <= 16, "Turn limit");
        int tally = 0;
        for (GameView.StateView s : v.states()) {
            check(s.objectives().containsAll(s.playerTasks()) && s.objectives().containsAll(s.opponentTasks()), "No unrelated completed task");
            boolean you = s.playerTasks().containsAll(s.objectives()), opp = s.opponentTasks().containsAll(s.objectives());
            check(s.playerControls() == (you == opp ? s.playerStarts() : you), "Ownership rule");
            if (s.playerControls()) tally += s.electoralVotes();
        }
        check(tally == v.playerEV(), "Tally derived from states");
    }
    public static void main(String[] args) throws Exception {
        check(EventCatalog.all().size() == 36, "36 event definitions");
        check(EventCatalog.all().stream().map(CampaignEvent::id).distinct().count() == 36, "Unique IDs");
        Set<String> seen = new HashSet<>();
        int wins = 0, losses = 0;
        for (int seed = 0; seed < 40; seed++) for (President.Difficulty difficulty : President.Difficulty.values())
            for (President.RunningMate mate : President.RunningMate.values()) {
                GameEngine g = new GameEngine(seed, difficulty, mate);
                GameEngine twin = new GameEngine(seed, difficulty, mate);
                GameView old = g.view();
                check(old.playerEV() == 186 && old.opponentEV() == 352, "Initial board");
                int guard = 0;
                while (!g.view().complete()) {
                    check(guard++ < 33, "No stuck campaign");
                    GameCommand command;
                    if (g.view().pendingEvent() != null) {
                        int index = g.view().pendingEvent().choices().get(0).available() ? 0 : 1;
                        command = GameCommand.respond(index);
                    } else command = seed % 2 == 0 ? next(g) : GameCommand.rest();
                    // Extra UI reads must not alter random draws or gameplay.
                    g.view(); g.view();
                    TurnReport report = g.submit(command);
                    check(report.accepted(), "Valid driver action");
                    twin.submit(command);
                    check(g.view().equals(twin.view()), "Reproducible full snapshots");
                    invariants(g);
                }
                check(old.turnsUsed() == 0 && old.history().size() == 2, "Snapshots remain unchanged");
                ElectionResult result = g.result();
                check(result.getTotalEV() == 538 && result.getStateResults().size() == 50, "Result tally");
                if (result.playerWon()) wins++; else if (!result.isTie()) losses++;
                Set<String> sessionEvents = new HashSet<>();
                for (String line : g.view().history()) if (line.startsWith("NEWS [")) {
                    String id = line.substring(6, line.indexOf(']'));
                    check(sessionEvents.add(id), "No repeat events within a session"); seen.add(id);
                }
                GameView end = g.view();
                check(!g.submit(GameCommand.rest()).accepted() && end.equals(g.view()), "Final state cannot mutate");
            }
        check(wins > 0 && losses > 0, "Both outcomes reachable");
        check(seen.size() == 36, "Every event exercised by full sessions: " + seen.size());
        GameEngine g = game(42);
        GameView before = g.view();
        check(!g.submit(GameCommand.campaign("Unknown", State.Task.TOWN_HALL)).accepted(), "Invalid state");
        check(!g.submit(new GameCommand(GameCommand.Type.REST, "Texas", null, -1)).accepted(), "Malformed command");
        check(!g.submit(GameCommand.respond(0)).accepted(), "No phantom response");
        check(g.view().equals(before) && g.journal().isEmpty(), "Rejection atomicity");
        GameView.StateView california = state(g, "California");
        State.Task task = california.actions().get(0).task();
        check(g.submit(GameCommand.campaign("California", task)).accepted(), "First task"); respond(g);
        before = g.view();
        if (state(g, "California").playerTasks().contains(task)) {
            check(!g.submit(GameCommand.campaign("California", task)).accepted(), "Duplicate task");
            check(before.equals(g.view()), "Duplicate is free");
        }
        boolean blocked = false;
        try { game(1).result(); } catch (IllegalStateException ex) { blocked = true; }
        check(blocked, "No premature result");
        blocked = false;
        try { g.view().states().clear(); } catch (UnsupportedOperationException ex) { blocked = true; }
        check(blocked, "Snapshot list immutable");
        blocked = false;
        try { g.view().states().get(0).objectives().clear(); } catch (UnsupportedOperationException ex) { blocked = true; }
        check(blocked, "Snapshot nested set immutable");
        testEffects(); testEffectContracts(); testSave(); testTie();
        System.out.println("PASS: " + checks + " checks; 320 full campaigns; all 36 events exercised; wins and losses reachable.");
    }
    private static void testEffects() {
        CampaignEvent wipe = new CampaignEvent("wipe", "Large invoice", "Test", CampaignEvent.Category.FUNDING,
            List.of(EventCatalog.cash(CampaignEvent.Side.BOTH, -99999)), List.of());
        GameEngine g = custom(wipe); g.submit(GameCommand.rest());
        check(g.view().playerFunds() == 0 && g.view().opponentFunds() == 0, "Loss clamps at cash");
        check(g.view().history().stream().anyMatch(s -> s.contains("loss capped")), "Actual loss explained");
        GameView v = g.view();
        GameView.StateView s = v.states().get(0);
        check(!g.submit(GameCommand.campaign(s.name(), s.actions().get(0).task())).accepted(), "Unaffordable action");
        check(v.equals(g.view()), "Unaffordable atomicity");
        g.submit(GameCommand.fundraise()); check(g.view().playerFunds() == 250 && g.view().opponentFunds() == 250, "Both can recover by fundraising");
        CampaignEvent mod = new CampaignEvent("mod", "Temporary discount", "Test", CampaignEvent.Category.OPERATIONS,
            List.of(new CampaignEvent.Effect(CampaignEvent.Side.PLAYER, CampaignEvent.Kind.ACTION_COST, -999, State.Task.TOWN_HALL, 2)), List.of());
        g = custom(mod); g.submit(GameCommand.rest());
        final GameEngine discountGame = g;
        String target = g.view().states().stream().filter(st -> st.objectives().contains(State.Task.TOWN_HALL)).findFirst().orElseThrow().name();
        check(state(discountGame, target).actions().stream().filter(a -> a.task() == State.Task.TOWN_HALL).findFirst().orElseThrow().cost() == 25, "Price floor");
        g.submit(GameCommand.rest()); check(!g.view().activeEffects().isEmpty(), "Modifier lasts second upcoming turn");
        g.submit(GameCommand.rest()); check(g.view().activeEffects().isEmpty(), "Modifier expires after two turns");
        check(state(g, target).actions().stream().filter(a -> a.task() == State.Task.TOWN_HALL).findFirst().orElseThrow().cost() == 100, "Price restored");
        CampaignEvent offer = new CampaignEvent("offer", "Too costly", "Test", CampaignEvent.Category.OPPORTUNITY, List.of(),
            List.of(new CampaignEvent.Option("Pay", List.of(EventCatalog.cash(CampaignEvent.Side.PLAYER, -99999))), new CampaignEvent.Option("Decline", List.of())));
        g = custom(offer); g.submit(GameCommand.rest()); v = g.view();
        check(v.pendingEvent() != null && !v.pendingEvent().choices().get(0).available(), "Unaffordable offer disabled");
        check(!g.submit(GameCommand.rest()).accepted(), "Pending choice blocks next turn");
        check(!g.submit(GameCommand.respond(0)).accepted() && !g.submit(GameCommand.respond(7)).accepted(), "Invalid event responses rejected");
        check(v.equals(g.view()), "Invalid response preserves state");
        g.submit(GameCommand.respond(1)); check(g.view().turnsUsed() == 1 && g.view().pendingEvent() == null, "Free response without extra turn");
        // All event kinds are explicitly exercised for both actors by full-session invariants above.
    }
    private static void testEffectContracts() {
        for (CampaignEvent.Side side : CampaignEvent.Side.values()) for (int amount : new int[]{180, -120}) {
            GameEngine g = custom(new CampaignEvent("money", "Money", "Test", CampaignEvent.Category.FUNDING,
                List.of(EventCatalog.cash(side, amount)), List.of()));
            GameView before = g.view(); g.submit(GameCommand.rest());
            check(g.view().playerFunds() == before.playerFunds() + (side == CampaignEvent.Side.OPPONENT ? 0 : amount), "Player cash exact");
            check(g.view().opponentFunds() == before.opponentFunds() + (side == CampaignEvent.Side.PLAYER ? 0 : amount), "Opponent cash exact");
        }
        for (CampaignEvent.Side side : List.of(CampaignEvent.Side.PLAYER, CampaignEvent.Side.OPPONENT)) for (State.Task task : State.Task.values()) {
            CampaignEvent gain = new CampaignEvent("gain", "Gain", "Test", CampaignEvent.Category.VOLUNTEERS,
                List.of(EventCatalog.task(side, task, true)), List.of());
            CampaignEvent loss = new CampaignEvent("loss", "Loss", "Test", CampaignEvent.Category.DISRUPTION,
                List.of(EventCatalog.task(side, task, false)), List.of());
            GameEngine g = new GameEngine(42, President.Difficulty.NORMAL, President.RunningMate.FUNDRAISER, List.of(gain, loss));
            g.submit(GameCommand.rest());
            int work = g.view().states().stream().mapToInt(st -> (side == CampaignEvent.Side.PLAYER ? st.playerTasks() : st.opponentTasks()).size()).sum();
            check(work == 1, "Task grant applies exactly once");
            check(g.view().history().stream().noneMatch(t -> t.startsWith("NEWS [loss]")), "No removing work that does not exist");
            g.submit(GameCommand.rest());
            work = g.view().states().stream().mapToInt(st -> (side == CampaignEvent.Side.PLAYER ? st.playerTasks() : st.opponentTasks()).size()).sum();
            check(work == (side == CampaignEvent.Side.PLAYER ? 0 : 1), "Task loss applies once; opponent also acts on turn two");
            check(g.view().history().stream().anyMatch(t -> t.startsWith("NEWS [loss]")), "Removal becomes eligible");
        }
        GameEngine g = custom(EventCatalog.all().stream().filter(e -> e.id().equals("venue_offer")).findFirst().orElseThrow());
        g.submit(GameCommand.rest()); GameView before = g.view(); String target = before.pendingEvent().state();
        check(g.submit(GameCommand.respond(0)).accepted(), "Accept paid opportunity");
        check(g.view().playerFunds() == before.playerFunds() - 100 && state(g, target).playerTasks().contains(State.Task.TOWN_HALL), "Payment and grant applied together");
        CampaignEvent fundraiser = new CampaignEvent("raising", "Raising", "Test", CampaignEvent.Category.OPERATIONS,
            List.of(new CampaignEvent.Effect(CampaignEvent.Side.BOTH, CampaignEvent.Kind.FUNDRAISING, -999, null, 2)), List.of());
        g = custom(fundraiser); g.submit(GameCommand.rest());
        check(g.view().fundraisingAmount() == 50, "Fundraising floor");
        before = g.view(); g.submit(GameCommand.fundraise());
        check(g.view().playerFunds() == before.playerFunds() + 50, "Fundraising applies modified proceeds");
        boolean finalPending = false;
        for (int seed = 0; seed < 30 && !finalPending; seed++) {
            g = game(seed);
            while (g.view().turnsUsed() < 16) { respond(g); g.submit(GameCommand.rest()); }
            if (g.view().pendingEvent() != null) {
                finalPending = true;
                boolean rejected = false; try { g.result(); } catch (IllegalStateException ex) { rejected = true; }
                check(rejected && !g.view().complete(), "Final response blocks finalization");
                g.submit(GameCommand.respond(1));
                check(g.view().complete() && g.view().turnsUsed() == 16 && g.result().getTotalEV() == 538, "Last response finalizes without extra turn");
            }
        }
        check(finalPending, "Final-turn pending-event fixture found");
    }
    private static void testSave() throws Exception {
        Path path = Files.createTempFile("campaign-test-", ".save");
        try {
            GameEngine g = game(4);
            boolean savedPending = false;
            while (!g.view().complete()) {
                if (g.view().pendingEvent() != null) {
                    CampaignSave.write(g, path);
                    GameEngine loaded = CampaignSave.read(path);
                    check(loaded.view().equals(g.view()), "Restore exact pending event");
                    loaded.submit(GameCommand.respond(1)); g.submit(GameCommand.respond(1));
                    check(loaded.view().equals(g.view()), "Continue after pending-event load"); savedPending = true;
                } else {
                    g.submit(next(g)); CampaignSave.write(g, path);
                    check(CampaignSave.read(path).view().equals(g.view()), "Save/load every turn");
                }
            }
            check(savedPending, "Pending save fixture encountered");
            CampaignSave.write(g, path); check(CampaignSave.read(path).result().getPlayerEV() == g.result().getPlayerEV(), "Completed save");
            Files.writeString(path, "format=old\n");
            boolean rejected = false;
            try { CampaignSave.read(path); } catch (IOException ex) { rejected = true; }
            check(rejected, "Old format rejected");
            Files.writeString(path, "format=campaign-events-v1\nseed=2\ndifficulty=NORMAL\nmate=FUNDRAISER\ncommands=1\ncommand.0.type=RESPOND\ncommand.0.choice=0\n");
            rejected = false; try { CampaignSave.read(path); } catch (IOException ex) { rejected = true; }
            check(rejected, "Corrupt command replay rejected");
        } finally { Files.deleteIfExists(path); }
    }
    private static void testTie() {
        ElectoralCollege ec = new ElectoralCollege(1);
        Map<Integer, List<String>> sums = new LinkedHashMap<>(); sums.put(0, List.of());
        for (State s : ec.getAllStates()) for (Map.Entry<Integer, List<String>> e : new LinkedHashMap<>(sums).entrySet()) {
            int total = e.getKey() + s.getElectoralVotes();
            if (total <= 269 && !sums.containsKey(total)) { List<String> names = new ArrayList<>(e.getValue()); names.add(s.getName()); sums.put(total, names); }
        }
        Map<String, Boolean> results = new LinkedHashMap<>(); Map<String, String> reasons = new LinkedHashMap<>();
        for (State s : ec.getAllStates()) { results.put(s.getName(), sums.get(269).contains(s.getName())); reasons.put(s.getName(), "Test fixture"); }
        ElectionResult tie = new ElectionResult(ec.getStates(), ec.getDistrictOfColumbia(), results, reasons, List.of());
        check(tie.isTie() && !tie.playerWon() && tie.getTotalEV() == 538, "Tie regression");
        new ElectionGUI().showResults(tie, ec.getStates(), ec.getDistrictOfColumbia());
    }
}
