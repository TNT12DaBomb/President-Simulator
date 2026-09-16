import java.util.List;
import java.util.Locale;

/** Read-only index over the durable career journal; preserves original entries and ordering. */
public final class CareerArchive {
    private CareerArchive() { }
    public enum Category { ALL, ELECTIONS, GOVERNING, CABINET, POLICIES, PUBLIC, DEVELOPMENT }
    public record Entry(int number, Category category, String text) { }
    public static List<Entry> search(CareerView view, Category category, String query) {
        String q=query.strip().toLowerCase(Locale.ROOT);
        return java.util.stream.IntStream.range(0,view.history().size()).mapToObj(i->new Entry(i+1,classify(view.history().get(i)),view.history().get(i)))
            .filter(e->(category==Category.ALL||e.category()==category)&&e.text().toLowerCase(Locale.ROOT).contains(q)).toList();
    }
    private static Category classify(String text) {
        String s=text.toLowerCase(Locale.ROOT);
        if(s.contains("developer")||s.contains("fixture"))return Category.DEVELOPMENT;
        if(s.contains("nomina")||s.contains("cabinet")||s.contains("confirmed")||s.contains("assignment")||s.contains("delegat"))return Category.CABINET;
        if(s.contains("midterm")||s.contains("election")||s.contains("campaign")||s.contains("slate")||s.contains("senate race"))return Category.ELECTIONS;
        if(s.contains("policy")||s.contains("law")||s.contains("bill")||s.contains("promise")||s.contains("sign completed")||s.contains("veto"))return Category.POLICIES;
        if(s.contains("request")||s.contains("correspondence")||s.contains("public")||s.contains("reply"))return Category.PUBLIC;
        return Category.GOVERNING;
    }
    public static List<String> termSummaries(CareerView view) {
        return view.history().stream().filter(s->s.startsWith("Term archive:")||s.startsWith("Archived law:")||s.startsWith("Archived promise:")||s.startsWith("Archived cabinet seat:")||s.startsWith("INAUGURATION:")).toList();
    }
}
