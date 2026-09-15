import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** Immutable fictional scenario roster; election rules live in GameEngine. */
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
            card("Alabama", 9, true, random),
            card("Alaska", 3, false, random),
            card("Arizona", 11, false, random),
            card("Arkansas", 6, true, random),
            card("California", 54, false, random),
            card("Colorado", 10, false, random),
            card("Connecticut", 7, true, random),
            card("Delaware", 3, false, random),
            card("Florida", 30, false, random),
            card("Georgia", 16, true, random),
            card("Hawaii", 4, false, random),
            card("Idaho", 4, false, random),
            card("Illinois", 19, true, random),
            card("Indiana", 11, false, random),
            card("Iowa", 6, false, random),
            card("Kansas", 6, true, random),
            card("Kentucky", 8, false, random),
            card("Louisiana", 8, false, random),
            card("Maine", 4, true, random),
            card("Maryland", 10, false, random),
            card("Massachusetts", 11, false, random),
            card("Michigan", 15, true, random),
            card("Minnesota", 10, false, random),
            card("Mississippi", 6, false, random),
            card("Missouri", 10, true, random),
            card("Montana", 4, false, random),
            card("Nebraska", 5, false, random),
            card("Nevada", 6, true, random),
            card("New Hampshire", 4, false, random),
            card("New Jersey", 14, false, random),
            card("New Mexico", 5, true, random),
            card("New York", 28, false, random),
            card("North Carolina", 16, false, random),
            card("North Dakota", 3, true, random),
            card("Ohio", 17, false, random),
            card("Oklahoma", 7, false, random),
            card("Oregon", 8, true, random),
            card("Pennsylvania", 19, false, random),
            card("Rhode Island", 4, false, random),
            card("South Carolina", 9, true, random),
            card("South Dakota", 3, false, random),
            card("Tennessee", 11, false, random),
            card("Texas", 40, true, random),
            card("Utah", 6, false, random),
            card("Vermont", 3, false, random),
            card("Virginia", 13, true, random),
            card("Washington", 12, false, random),
            card("West Virginia", 4, false, random),
            card("Wisconsin", 10, true, random),
            card("Wyoming", 3, false, random)
        };
        districtOfColumbia = card("District of Columbia", 3, false, random);
        int total = Arrays.stream(states).mapToInt(State::getElectoralVotes).sum();
        if (states.length != 50 || total != TOTAL_STATE_ELECTORAL_VOTES
            || total + districtOfColumbia.getElectoralVotes() != TOTAL_ELECTORAL_VOTES)
            throw new IllegalStateException("Invalid Electoral College roster");
    }
    private static State card(String name, int ev, boolean starting, Random random) {
        EnumSet<State.Task> tasks = EnumSet.allOf(State.Task.class);
        tasks.remove(State.Task.values()[random.nextInt(State.Task.values().length)]);
        return new State(name, ev, starting, tasks);
    }
    public long getSeed() { return seed; }
    public State[] getStates() { return states.clone(); }
    public State getDistrictOfColumbia() { return districtOfColumbia; }
    public List<State> getAllStates() {
        List<State> all = new ArrayList<>(Arrays.asList(states));
        all.add(districtOfColumbia);
        return java.util.Collections.unmodifiableList(all);
    }
}
