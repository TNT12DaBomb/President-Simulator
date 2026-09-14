import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class ElectoralCollege {

    public static final int TOTAL_STATE_ELECTORAL_VOTES = 535;
    public static final int DC_ELECTORAL_VOTES = 3;
    public static final int TOTAL_ELECTORAL_VOTES = 538;
    public static final int ELECTORAL_VOTES_TO_WIN = 270;

    private final State[] states;

    private final State districtOfColumbia;

    private final Random random;

    public ElectoralCollege() {

        random = new Random();

        states = new State[] {

            new State("Alabama", 9, 0),
            new State("Alaska", 3, 0),
            new State("Arizona", 11, 0),
            new State("Arkansas", 6, 0),
            new State("California", 54, 0),
            new State("Colorado", 10, 0),
            new State("Connecticut", 7, 0),
            new State("Delaware", 3, 0),
            new State("Florida", 30, 0),
            new State("Georgia", 16, 0),
            new State("Hawaii", 4, 0),
            new State("Idaho", 4, 0),
            new State("Illinois", 19, 0),
            new State("Indiana", 11, 0),
            new State("Iowa", 6, 0),
            new State("Kansas", 6, 0),
            new State("Kentucky", 8, 0),
            new State("Louisiana", 8, 0),
            new State("Maine", 4, 0),
            new State("Maryland", 10, 0),
            new State("Massachusetts", 11, 0),
            new State("Michigan", 15, 0),
            new State("Minnesota", 10, 0),
            new State("Mississippi", 6, 0),
            new State("Missouri", 10, 0),
            new State("Montana", 4, 0),
            new State("Nebraska", 5, 0),
            new State("Nevada", 6, 0),
            new State("New Hampshire", 4, 0),
            new State("New Jersey", 14, 0),
            new State("New Mexico", 5, 0),
            new State("New York", 28, 0),
            new State("North Carolina", 16, 0),
            new State("North Dakota", 3, 0),
            new State("Ohio", 17, 0),
            new State("Oklahoma", 7, 0),
            new State("Oregon", 8, 0),
            new State("Pennsylvania", 19, 0),
            new State("Rhode Island", 4, 0),
            new State("South Carolina", 9, 0),
            new State("South Dakota", 3, 0),
            new State("Tennessee", 11, 0),
            new State("Texas", 40, 0),
            new State("Utah", 6, 0),
            new State("Vermont", 3, 0),
            new State("Virginia", 13, 0),
            new State("Washington", 12, 0),
            new State("West Virginia", 4, 0),
            new State("Wisconsin", 10, 0),
            new State("Wyoming", 3, 0)
        };

        // D.C. is intentionally separate from the states.
        districtOfColumbia =
                new State(
                        "District of Columbia",
                        DC_ELECTORAL_VOTES,
                        0
                );

        validateElectoralVotes();
    }

    // ------------------------
    // GET STATES
    // ------------------------

    public State[] getStates() {
        return states.clone();
    }

    // ------------------------
    // GET D.C.
    // ------------------------

    public State getDistrictOfColumbia() {
        return districtOfColumbia;
    }

    // ------------------------
    // RUN ELECTION
    // ------------------------

    public ElectionResult runElection(President president) {

        Map<String, Boolean> stateResults =
                new LinkedHashMap<>();

        int playerEV = 0;
        int opponentEV = 0;

        // ------------------------
        // STATES
        // ------------------------

        for (State state : states) {

            int score = calculateStateScore(
                    president,
                    state
            );

            boolean playerWon = score > 60;

            stateResults.put(
                    state.getName(),
                    playerWon
            );

            if (playerWon) {
                playerEV += state.getElectoralVotes();
            }
            else {
                opponentEV += state.getElectoralVotes();
            }
        }

        // ------------------------
        // D.C.
        // ------------------------

        int dcScore = calculateStateScore(
                president,
                districtOfColumbia
        );

        boolean dcWon = dcScore > 60;

        if (dcWon) {
            playerEV +=
                    districtOfColumbia.getElectoralVotes();
        }
        else {
            opponentEV +=
                    districtOfColumbia.getElectoralVotes();
        }

        return new ElectionResult(
                stateResults,
                dcWon,
                playerEV,
                opponentEV
        );
    }

    // ------------------------
    // STATE SCORE
    // ------------------------

    private int calculateStateScore(
            President president,
            State state) {

        int randomModifier =
                random.nextInt(40) - 20;

        return
                president.getApproval()
                + president.getEconomy()
                + president.getPublicTrust()
                - president.getScandal()
                + state.getPoliticalLeaning()
                + randomModifier;
    }

    // ------------------------
    // VALIDATE EV
    // ------------------------

    private void validateElectoralVotes() {

        int total = 0;

        for (State state : states) {
            total += state.getElectoralVotes();
        }

        if (total != TOTAL_STATE_ELECTORAL_VOTES) {

            throw new IllegalStateException(
                    "State Electoral Votes total " +
                    total +
                    " instead of " +
                    TOTAL_STATE_ELECTORAL_VOTES
            );
        }

        int grandTotal =
                total +
                districtOfColumbia.getElectoralVotes();

        if (grandTotal != TOTAL_ELECTORAL_VOTES) {

            throw new IllegalStateException(
                    "Electoral College totals " +
                    grandTotal +
                    " instead of " +
                    TOTAL_ELECTORAL_VOTES
            );
        }
    }
}
