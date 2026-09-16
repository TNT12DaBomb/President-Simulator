import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Immutable election snapshot. Tallies are derived from state outcomes. */
public final class ElectionResult {
    private final Map<String, Boolean> stateResults;
    private final Map<String, String> reasons;
    private final List<String> history;
    private final boolean dcWon;
    private final int playerEV;
    private final int opponentEV;

    public ElectionResult(State[] states, State dc, Map<String, Boolean> outcomes,
                          Map<String, String> reasons, List<String> history) {
        if (states == null || states.length != 50 || dc == null || dc.getElectoralVotes() != 3
            || !"District of Columbia".equals(dc.getName()) || outcomes == null || reasons == null || history == null)
            throw new IllegalArgumentException("Incomplete result");
        Map<String, Boolean> copy = new LinkedHashMap<>();
        Map<String, String> detail = new LinkedHashMap<>();
        int total = 0, won = 0;
        List<State> all = new ArrayList<>();
        Collections.addAll(all, states);
        all.add(dc);
        for (State state : all) {
            if (state == null || copy.containsKey(state.getName()) || outcomes.get(state.getName()) == null
                || reasons.get(state.getName()) == null) throw new IllegalArgumentException("Missing or duplicate state result");
            boolean win = outcomes.get(state.getName());
            copy.put(state.getName(), win);
            detail.put(state.getName(), reasons.get(state.getName()));
            total += state.getElectoralVotes();
            if (win) won += state.getElectoralVotes();
        }
        if (total != ElectoralCollege.TOTAL_ELECTORAL_VOTES || outcomes.size() != copy.size()
            || reasons.size() != copy.size()) throw new IllegalArgumentException("Invalid result totals or extra states");
        dcWon = copy.remove(dc.getName());
        stateResults = Collections.unmodifiableMap(copy);
        this.reasons = Collections.unmodifiableMap(detail);
        this.history = Collections.unmodifiableList(new ArrayList<>(history));
        playerEV = won;
        opponentEV = total - won;
    }
    public Map<String, Boolean> getStateResults() { return stateResults; }
    public Map<String, String> getReasons() { return reasons; }
    public List<String> getHistory() { return history; }
    public boolean didPlayerWinDC() { return dcWon; }
    public int getPlayerEV() { return playerEV; }
    public int getOpponentEV() { return opponentEV; }
    public int getTotalEV() { return playerEV + opponentEV; }
    public boolean playerWon() { return playerEV >= ElectoralCollege.ELECTORAL_VOTES_TO_WIN; }
    public boolean isTie() { return playerEV == opponentEV; }
    public String getOutcomeText() {
        return isTie() ? "269-269 TIE: no winner; contingent-election gameplay is not implemented."
            : playerWon() ? "You won the presidency!" : "You lost the election.";
    }
}
