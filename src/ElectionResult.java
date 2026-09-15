import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ElectionResult {

    private final Map<String, Boolean> stateResults;
    private final boolean dcWon;
    private final int playerEV;
    private final int opponentEV;

    public ElectionResult(
            Map<String, Boolean> stateResults,
            boolean dcWon,
            int playerEV,
            int opponentEV) {

        this.stateResults =
                new LinkedHashMap<>(stateResults);

        this.dcWon = dcWon;
        this.playerEV = playerEV;
        this.opponentEV = opponentEV;
    }

    public Map<String, Boolean> getStateResults() {
        return Collections.unmodifiableMap(stateResults);
    }

    public boolean didPlayerWinDC() {
        return dcWon;
    }

    public int getPlayerEV() {
        return playerEV;
    }

    public int getOpponentEV() {
        return opponentEV;
    }

    public int getTotalEV() {
        return playerEV + opponentEV;
    }

    public boolean playerWon() {
        return playerEV >= 270;
    }
}
