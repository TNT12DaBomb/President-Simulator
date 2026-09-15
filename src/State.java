public class State {

    private final String name;
    private final int electoralVotes;
    private final int politicalLeaning;

    public State(
            String name,
            int electoralVotes,
            int politicalLeaning) {

        this.name = name;
        this.electoralVotes = electoralVotes;
        this.politicalLeaning = politicalLeaning;
    }

    public String getName() {
        return name;
    }

    public int getElectoralVotes() {
        return electoralVotes;
    }

    public int getPoliticalLeaning() {
        return politicalLeaning;
    }
}
