import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Dependency-free regression tests; throws on failure even without -ea. */
public final class SimulatorTests {
    private static int checks;
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
    private static State find(ElectoralCollege ec, String name) {
        return ec.getAllStates().stream().filter(s -> s.getName().equals(name)).findFirst().orElseThrow();
    }
    private static President player(President.Difficulty d, President.RunningMate m) { return new President(d, m); }
    private static void act(President p, State s, State.Task t) {
        if (p.getFunds() < p.cost(t)) p.fundraise();
        int before = p.getTurnsUsed();
        String feedback = p.perform(s, t);
        check(p.getTurnsUsed() == before + 1, "Action rejected: " + feedback);
    }
    private static void finish(President p) { while (!p.isCampaignComplete()) p.rest(); }
    public static void main(String[] args) {
        for (long seed = 0; seed < 100; seed++) {
            ElectoralCollege ec = new ElectoralCollege(seed);
            check(ec.getStates().length == 50 && ec.getAllStates().size() == 51, "Roster size");
            check(ec.getAllStates().stream().mapToInt(State::getElectoralVotes).sum() == 538, "EV total");
            for (President.Difficulty d : President.Difficulty.values()) {
                for (President.RunningMate mate : President.RunningMate.values()) {
                    President winner = player(d, mate);
                    check(ec.currentPlayerEV(winner) == 186, "Starting support");
                    act(winner, find(ec, "Texas"), State.Task.FIELD_OFFICE);
                    act(winner, find(ec, "Illinois"), State.Task.FIELD_OFFICE);
                    for (String name : new String[]{"California", "Florida"})
                        for (State.Task task : find(ec, name).getObjectives()) act(winner, find(ec, name), task);
                    finish(winner);
                    ElectionResult win = ec.runElection(winner);
                    check(win.playerWon() && win.getPlayerEV() == 270, "Reachable win: seed " + seed + " " + d + " " + mate);
                    check(win.getTotalEV() == 538 && win.getOpponentEV() == 268, "Win tally");
                    check(win.getHistory().size() == 9, "All eight turns logged");
                    check(!win.didPlayerWinDC() && win.getStateResults().size() == 50, "Separate DC");
                    check(ec.runElection(winner).getStateResults().equals(win.getStateResults()), "No reroll");
                    President loser = player(d, mate);
                    finish(loser);
                    ElectionResult loss = ec.runElection(loser);
                    check(!loss.playerWon() && loss.getPlayerEV() == 127 && !loss.isTie(), "Inactivity must lose");
                }
            }
        }
        ElectoralCollege ec = new ElectoralCollege(42);
        President p = player(President.Difficulty.NORMAL, President.RunningMate.COMMUNITY_ORGANIZER);
        check(p.cost(State.Task.TOWN_HALL) == 50, "Organizer benefit");
        check(player(President.Difficulty.NORMAL, President.RunningMate.PARTY_ORGANIZER).cost(State.Task.FIELD_OFFICE) == 125, "Party benefit");
        check(player(President.Difficulty.NORMAL, President.RunningMate.LOGISTICS_COORDINATOR).cost(State.Task.OUTREACH) == 100, "Logistics benefit");
        check(player(President.Difficulty.HARD, President.RunningMate.FUNDRAISER).getFunds() == 900, "Hard and fundraiser");
        boolean blocked = false;
        try { ec.runElection(p); } catch (IllegalStateException ex) { blocked = true; }
        check(blocked, "Early election blocked");
        p.rest(); p.rest(); p.rest();
        check(!ec.controls(p, find(ec, "Texas")), "Challenge activates on turn three");
        p.perform(find(ec, "Texas"), State.Task.FIELD_OFFICE);
        check(ec.controls(p, find(ec, "Texas")), "Can recover challenged state");
        int funds = p.getFunds(), turn = p.getTurnsUsed();
        p.perform(find(ec, "Texas"), State.Task.FIELD_OFFICE);
        p.perform(find(ec, "Alabama"), State.Task.TOWN_HALL);
        p.perform(null, null);
        check(p.getFunds() == funds && p.getTurnsUsed() == turn, "Invalid or duplicate actions are free");
        List<String> oldHistory = p.getHistory();
        p.rest();
        check(oldHistory.size() + 1 == p.getHistory().size(), "History snapshots");
        finish(p);
        funds = p.getFunds(); p.fundraise(); p.rest(); p.perform(find(ec, "California"), State.Task.TOWN_HALL);
        check(p.getFunds() == funds && p.getTurnsUsed() == 8, "Campaign cannot exceed eight turns");
        President poor = player(President.Difficulty.HARD, President.RunningMate.COMMUNITY_ORGANIZER);
        for (State s : ec.getAllStates()) {
            if (!s.hasStartingSupport() && s.getObjectives().contains(State.Task.FIELD_OFFICE)) poor.perform(s, State.Task.FIELD_OFFICE);
            if (poor.getFunds() == 0) break;
        }
        check(poor.getFunds() == 0 && poor.getTurnsUsed() == 4, "Spend all funds");
        turn = poor.getTurnsUsed();
        poor.perform(find(ec, "Texas"), State.Task.FIELD_OFFICE);
        check(poor.getFunds() == 0 && poor.getTurnsUsed() == turn, "Unaffordable action rejected atomically");
        poor.fundraise(); check(poor.getFunds() == 250 && poor.getTurnsUsed() == turn + 1, "Fundraising consumes turn");
        President dcPlayer = player(President.Difficulty.NORMAL, President.RunningMate.FUNDRAISER);
        for (State.Task task : ec.getDistrictOfColumbia().getObjectives()) act(dcPlayer, ec.getDistrictOfColumbia(), task);
        finish(dcPlayer);
        ElectionResult dcWin = ec.runElection(dcPlayer);
        check(dcWin.didPlayerWinDC() && dcWin.getPlayerEV() == 130, "DC awards exactly three EV");
        boolean immutable = false;
        try { dcWin.getStateResults().put("Texas", true); } catch (UnsupportedOperationException ex) { immutable = true; }
        check(immutable, "Result outcomes immutable");
        immutable = false;
        try { ec.getDistrictOfColumbia().getObjectives().clear(); } catch (UnsupportedOperationException ex) { immutable = true; }
        check(immutable, "Objectives immutable");
        State[] roster = ec.getStates(); roster[0] = null;
        check(ec.getStates()[0] != null, "Roster defensively copied");
        ElectoralCollege repeated = new ElectoralCollege(42);
        for (int i = 0; i < 51; i++) check(ec.getAllStates().get(i).getObjectives().equals(repeated.getAllStates().get(i).getObjectives()), "Seed reproducibility");
        // Subset-sum constructs a real 269/269 tally to exercise tie handling.
        Map<Integer, List<String>> subsets = new LinkedHashMap<>();
        subsets.put(0, new ArrayList<>());
        for (State s : ec.getAllStates()) {
            for (Map.Entry<Integer, List<String>> e : new LinkedHashMap<>(subsets).entrySet()) {
                int sum = e.getKey() + s.getElectoralVotes();
                if (sum <= 269 && !subsets.containsKey(sum)) {
                    List<String> names = new ArrayList<>(e.getValue()); names.add(s.getName()); subsets.put(sum, names);
                }
            }
        }
        check(subsets.containsKey(269), "Tie fixture");
        Map<String, Boolean> outcomes = new LinkedHashMap<>();
        Map<String, String> reasons = new LinkedHashMap<>();
        for (State s : ec.getAllStates()) { outcomes.put(s.getName(), subsets.get(269).contains(s.getName())); reasons.put(s.getName(), "Test fixture"); }
        ElectionResult tie = new ElectionResult(ec.getStates(), ec.getDistrictOfColumbia(), outcomes, reasons, new ArrayList<>());
        check(tie.isTie() && !tie.playerWon() && tie.getOutcomeText().contains("no winner"), "Tie is not a loss");
        outcomes.remove("Texas"); blocked = false;
        try { new ElectionResult(ec.getStates(), ec.getDistrictOfColumbia(), outcomes, reasons, new ArrayList<>()); }
        catch (IllegalArgumentException ex) { blocked = true; }
        check(blocked, "Incomplete results rejected");
        new ElectionGUI().showResults(tie, ec.getStates(), ec.getDistrictOfColumbia());
        System.out.println("PASS: " + checks + " checks; 800 winning and 800 losing campaign scenarios.");
    }
}
