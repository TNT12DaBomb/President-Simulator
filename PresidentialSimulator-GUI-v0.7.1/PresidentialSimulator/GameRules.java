/** Single development ruleset. Earlier save formats are intentionally unsupported. */
public enum GameRules {
    CURRENT;
    public boolean current(){return true;}
    public boolean world(){return true;}
    public boolean timed(){return true;}
    public boolean debates(){return true;}
    public int midtermElectionMonth(){return 23;}
    public int midtermSeatingMonth(){return 24;}
}
