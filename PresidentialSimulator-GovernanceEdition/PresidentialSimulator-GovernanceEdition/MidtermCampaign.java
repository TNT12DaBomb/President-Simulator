import java.util.ArrayList;
import java.util.List;

/** Small fictional objective board: eight two-seat House slates and four Senate races. */
public final class MidtermCampaign {
    public enum Task { LISTENING_VISIT, ORGANIZING_VISIT }
    public record Contest(int id, String name, String chamber, int seats, boolean listened, boolean organized, String outcome) { }
    private final boolean[][] work = new boolean[12][2];
    private boolean resolved;
    public List<Contest> view() {
        List<Contest> result = new ArrayList<>();
        String[] names = {"Harbor", "Pine", "Valley", "Lake", "Prairie", "Ridge", "River", "Coast", "Alder", "Juniper", "Cedar", "Willow"};
        for (int i = 0; i < 12; i++) result.add(new Contest(i, names[i] + (i < 8 ? " House slate" : " Senate race"),
            i < 8 ? "House" : "Senate", i < 8 ? 2 : 1, work[i][0], work[i][1],
            !resolved ? "Open" : work[i][0] && work[i][1] ? "Your caucus" : "Other caucus"));
        return List.copyOf(result);
    }
    public String problem(int choice, int months) {
        if (resolved || months >= 24) return "This term's midterm campaign has closed.";
        if (months < 12) return "Midterm campaigning opens at the start of month 13.";
        if (work[choice / 2][choice % 2]) return "That visit is already completed. Choose another objective.";
        return "";
    }
    public String complete(int choice) {
        work[choice / 2][choice % 2] = true;
        return view().get(choice / 2).name() + ": " + Task.values()[choice % 2] + " completed.";
    }
    public void resolve() { resolved = true; }
    public int houseSeats() { return 210 + view().stream().filter(c -> c.chamber().equals("House") && c.listened() && c.organized()).mapToInt(Contest::seats).sum(); }
    public int senateSeats() { return 48 + (int)view().stream().filter(c -> c.chamber().equals("Senate") && c.listened() && c.organized()).count(); }
}
