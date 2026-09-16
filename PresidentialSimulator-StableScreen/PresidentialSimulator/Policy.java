/** Fictional administrative initiatives: no modeled claims about real policy outcomes. */
public final class Policy {
    private Policy() { }
    public enum Issue { ECONOMY, TAXES, HEALTHCARE, IMMIGRATION, DEFENSE, ENVIRONMENT, EDUCATION, CIVIL_RIGHTS }
    public enum Approach { EXPAND_PROGRAM, REORGANIZE_PROGRAM }
    public record Bill(Issue issue, Approach approach, String stage) { }
    public record Promise(Issue issue, Approach approach, String status) { }
}
