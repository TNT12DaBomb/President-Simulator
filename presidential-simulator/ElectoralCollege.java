import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** Deterministic board-game rules, not an election forecasting model. */
public final class ElectoralCollege {
    public static final int TOTAL_STATE_ELECTORAL_VOTES = 535;
    public static final int DC_ELECTORAL_VOTES = 3;
    public static final int TOTAL_ELECTORAL_VOTES = 538;
    public static final int ELECTORAL_VOTES_TO_WIN = 270;
    private final State[] states;
    private final State districtOfColumbia;
    private final long seed;

    public ElectoralCollege() { this(System.nanoTime()); }
    public ElectoralCollege(long seed) {
        this.seed = seed;
        Random random = new Random(seed);
        states = new State[] {
            card("Alabama", 9, true, 0, random),
            card("Alaska", 3, false, 0, random),
            card("Arizona", 11, false, 0, random),
            card("Arkansas", 6, true, 0, random),
            card("California", 54, false, 0, random),
            card("Colorado", 10, false, 0, random),
            card("Connecticut", 7, true, 0, random),
            card("Delaware", 3, false, 0, random),
            card("Florida", 30, false, 0, random),
            card("Georgia", 16, true, 0, random),
            card("Hawaii", 4, false, 0, random),
            card("Idaho", 4, false, 0, random),
            card("Illinois", 19, true, 6, random),
            card("Indiana", 11, false, 0, random),
            card("Iowa", 6, false, 0, random),
            card("Kansas", 6, true, 0, random),
            card("Kentucky", 8, false, 0, random),
            card("Louisiana", 8, false, 0, random),
            card("Maine", 4, true, 0, random),
            card("Maryland", 10, false, 0, random),
            card("Massachusetts", 11, false, 0, random),
            card("Michigan", 15, true, 0, random),
            card("Minnesota", 10, false, 0, random),
            card("Mississippi", 6, false, 0, random),
            card("Missouri", 10, true, 0, random),
            card("Montana", 4, false, 0, random),
            card("Nebraska", 5, false, 0, random),
            card("Nevada", 6, true, 0, random),
            card("New Hampshire", 4, false, 0, random),
            card("New Jersey", 14, false, 0, random),
            card("New Mexico", 5, true, 0, random),
            card("New York", 28, false, 0, random),
            card("North Carolina", 16, false, 0, random),
            card("North Dakota", 3, true, 0, random),
            card("Ohio", 17, false, 0, random),
            card("Oklahoma", 7, false, 0, random),
            card("Oregon", 8, true, 0, random),
            card("Pennsylvania", 19, false, 0, random),
            card("Rhode Island", 4, false, 0, random),
            card("South Carolina", 9, true, 0, random),
            card("South Dakota", 3, false, 0, random),
            card("Tennessee", 11, false, 0, random),
            card("Texas", 40, true, 3, random),
            card("Utah", 6, false, 0, random),
            card("Vermont", 3, false, 0, random),
            card("Virginia", 13, true, 0, random),
            card("Washington", 12, false, 0, random),
            card("West Virginia", 4, false, 0, random),
            card("Wisconsin", 10, true, 0, random),
            card("Wyoming", 3, false, 0, random)
        };
        districtOfColumbia = card("District of Columbia", 3, false, 0, random);
        int total = Arrays.stream(states).mapToInt(State::getElectoralVotes).sum();
        if (states.length != 50 || total != TOTAL_STATE_ELECTORAL_VOTES
            || total + districtOfColumbia.getElectoralVotes() != TOTAL_ELECTORAL_VOTES)
            throw new IllegalStateException("Invalid Electoral College roster");
    }
    private static State card(String name, int ev, boolean starting, int challenge, Random random) {
        EnumSet<State.Task> tasks = EnumSet.allOf(State.Task.class);
        tasks.remove(State.Task.values()[random.nextInt(State.Task.values().length)]);
        return new State(name, ev, starting, tasks, challenge);
    }
    public long getSeed() { return seed; }
    public State[] getStates() { return states.clone(); }
    public State getDistrictOfColumbia() { return districtOfColumbia; }
    public List<State> getAllStates() {
        List<State> all = new ArrayList<>(Arrays.asList(states));
        all.add(districtOfColumbia);
        return java.util.Collections.unmodifiableList(all);
    }
    public boolean controls(President player, State state) {
        Set<State.Task> done = player.getCompletedTasks(state.getName());
        if (state.hasStartingSupport()) {
            return state.getChallengeTurn() == 0 || player.getTurnsUsed() < state.getChallengeTurn()
                || done.contains(State.Task.FIELD_OFFICE);
        }
        return done.containsAll(state.getObjectives());
    }
    public String explain(President player, State state) {
        Set<State.Task> done = player.getCompletedTasks(state.getName());
        if (state.hasStartingSupport()) {
            if (state.getChallengeTurn() == 0) return "Starting bloc; no challenge scheduled.";
            if (done.contains(State.Task.FIELD_OFFICE)) return "Field office completed; challenge defended (or state recovered).";
            if (player.getTurnsUsed() < state.getChallengeTurn()) return "Starting bloc; field office needed for challenge after turn " + state.getChallengeTurn() + ".";
            return "Opponent challenge after turn " + state.getChallengeTurn() + "; missing field office.";
        }
        EnumSet<State.Task> missing = EnumSet.copyOf(state.getObjectives());
        missing.removeAll(done);
        return missing.isEmpty() ? "All objectives completed: " + state.getObjectives() + "."
            : "Completed " + done + "; missing " + missing + ".";
    }
    /** Display only: current rule-based board allocation, not a projection. */
    public int currentPlayerEV(President player) {
        int total = 0;
        for (State state : getAllStates()) if (controls(player, state)) total += state.getElectoralVotes();
        return total;
    }
    public ElectionResult runElection(President player) {
        if (player == null || !player.isCampaignComplete())
            throw new IllegalStateException("Finish all campaign turns before running the election");
        Map<String, Boolean> outcomes = new LinkedHashMap<>();
        Map<String, String> reasons = new LinkedHashMap<>();
        for (State state : getAllStates()) {
            outcomes.put(state.getName(), controls(player, state));
            reasons.put(state.getName(), explain(player, state));
        }
        return new ElectionResult(states, districtOfColumbia, outcomes, reasons, player.getHistory());
    }
}
