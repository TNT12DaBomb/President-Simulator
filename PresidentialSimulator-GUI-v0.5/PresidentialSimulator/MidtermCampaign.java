import java.util.ArrayList;
import java.util.List;

/** Small fictional objective board: eight two-seat House slates and four Senate races. */
public final class MidtermCampaign {
    private final GameRules rules;
    public MidtermCampaign() { this(GameRules.CURRENT); }
    public MidtermCampaign(GameRules rules) { this.rules = rules; }
    public enum Task { LISTENING_VISIT, ORGANIZING_VISIT }
    public record Contest(int id, String name, String chamber, int seats, boolean listened, boolean organized, String outcome) { }
    private final boolean[][] work = new boolean[12][2];
    private boolean resolved;
    private boolean[] worldWinners;
    public List<Contest> view() {
        List<Contest> result = new ArrayList<>();
        String[] names = {"Harbor", "Pine", "Valley", "Lake", "Prairie", "Ridge", "River", "Coast", "Alder", "Juniper", "Cedar", "Willow"};
        for (int i = 0; i < 12; i++) result.add(new Contest(i, names[i] + (i < 8 ? " House slate" : " Senate race"),
            i < 8 ? "House" : "Senate", i < 8 ? 2 : 1, work[i][0], work[i][1],
            !resolved ? "Open" : (worldWinners != null ? worldWinners[i] : work[i][0] && work[i][1]) ? "Your caucus" : "Other caucus"));
        return List.copyOf(result);
    }
    public String problem(int choice, int months) {
        if (resolved || months >= rules.midtermElectionMonth()) return "This term's midterm campaign has closed.";
        if (months < 12) return "Midterm campaigning opens at the start of month 13.";
        if (work[choice / 2][choice % 2]) return "That visit is already completed. Choose another objective.";
        return "";
    }
    public String complete(int choice) {
        work[choice / 2][choice % 2] = true;
        return view().get(choice / 2).name() + ": " + Task.values()[choice % 2] + " completed.";
    }
    public void resolveWorld(double mood, java.util.Random random, List<String> log) {
        if (resolved) throw new IllegalArgumentException("Midterms already resolved.");
        worldWinners = new boolean[12];
        for (int i=0;i<12;i++) {
            double probability = Math.max(.05, Math.min(.95, .30 + (work[i][0] ? .17 : 0) + (work[i][1] ? .17 : 0) + mood*.06));
            worldWinners[i] = random.nextDouble() < probability;
            log.add("MIDTERM " + i + ": governing mood " + String.format(java.util.Locale.ROOT,"%+.2f",mood) + ", visits " + ((work[i][0]?1:0)+(work[i][1]?1:0)) + ", modeled win chance " + Math.round(probability*100) + "%.");
        }
    }
    public void resolve() { resolved = true; }
    public int houseSeats() { return 210 + view().stream().filter(c -> c.chamber().equals("House") && (worldWinners != null ? worldWinners[c.id()] : c.listened() && c.organized())).mapToInt(Contest::seats).sum(); }
    public int senateSeats() { return 48 + (int)view().stream().filter(c -> c.chamber().equals("Senate") && (worldWinners != null ? worldWinners[c.id()] : c.listened() && c.organized())).count(); }
}
