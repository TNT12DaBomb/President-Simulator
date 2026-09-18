/** Simplified fictional commitments with researched procedural thresholds; see REALISM-AUDIT.md. */
public final class Congress {
    private final GameRules rules;
    private int house = 222, senate = 49;
    private boolean agreement, committeeReported, passed;
    private int midterms, housePledges, senatePledges;
    private Integer pendingHouse, pendingSenate;
    public Congress() { this(GameRules.CURRENT); }
    public Congress(GameRules rules) { this.rules = rules; }
    public record View(int houseSeats, int senateSeats, String houseControl, String senateControl,
                       boolean agreement, int midterms, String rule) { }
    public record Proceedings(int houseSupport, int senateSupport, boolean committeeReported, boolean passed,
                              Integer pendingHouse, Integer pendingSenate, String speaker, String senateLeader,
                              String oppositionHouseLeader, String oppositionSenateLeader, String stage) { }
    public View view() { return new View(house, senate, house >= 218 ? "Your caucus" : "Other caucus",
        senate >= (rules.current() ? 50 : 51) ? "Your caucus" : "Other caucus", agreement, midterms,
        rules.current() ? "Full attendance assumed. House: 218 votes; contested ordinary Senate bills: 60 for cloture, then a majority (50 + VP). Committee report and floor passage precede signature."
        : "A bill needs 218 House seats and 51 Senate seats, or a negotiated cross-caucus agreement. Ties require negotiation."); }
    public Proceedings proceedings() {
        return new Proceedings(Math.min(435, house + housePledges), Math.min(100, senate + senatePledges), committeeReported, passed,
            pendingHouse, pendingSenate, house >= 218 ? "Speaker Avery Cole (your caucus)" : "Speaker Robin Shaw (other caucus)",
            senate >= 50 ? "Majority Leader Morgan Vale (your caucus)" : "Majority Leader Cameron Brooks (other caucus)",
            "House leader: " + (house >= 218 ? "Robin Shaw" : "Avery Cole"), "Senate leader: " + (senate >= 50 ? "Cameron Brooks" : "Morgan Vale"), stage());
    }
    public String stage() {
        if (!rules.current()) return canPass() ? "Ready for signature" : "Awaiting cross-caucus agreement";
        return passed ? "Passed both chambers; on presidential desk" : !committeeReported ? "Introduced; committee review required"
            : "Reported from committee; floor vote required";
    }
    public void negotiate() {
        agreement = true;
        if (rules.current()) { housePledges = Math.min(435 - house, housePledges + 8); senatePledges = Math.min(100 - senate, senatePledges + 4); }
    }
    public boolean canPass() { return rules.current() ? passed : house >= 218 && senate >= 51 || agreement; }
    public String committeeProblem() { return passed ? "This bill has already passed both chambers." : committeeReported ? "Committee review is already complete." : ""; }
    public void reportCommittee() { committeeReported = true; }
    public String floorProblem() {
        if (passed) return "This bill has already passed both chambers.";
        if (!committeeReported) return "The committee must report the bill before floor consideration.";
        if (proceedings().houseSupport() < 218) return "At least 218 House commitments are needed with all seats voting.";
        if (proceedings().senateSupport() < 60) return "This ordinary bill faces a filibuster. Secure 60 cloture commitments first; 60 is not the final-passage threshold.";
        return "";
    }
    public void passFloor() { passed = true; }
    public void finishBill() { agreement = false; housePledges = 0; senatePledges = 0; committeeReported = false; passed = false; }
    public String midterm(MidtermCampaign campaign) {
        campaign.resolve(); midterms++;
        if (rules.current()) {
            pendingHouse = campaign.houseSeats(); pendingSenate = campaign.senateSeats();
            return "NOVEMBER MIDTERMS: your caucus wins " + pendingHouse + "/435 House seats and " + pendingSenate
                + "/100 Senate seats in the simplified contest board. Outgoing Congress remains until January 3.";
        }
        house = campaign.houseSeats(); senate = campaign.senateSeats(); agreement = false;
        return "MIDTERMS: completed contest objectives yield " + house + "/435 House seats and " + senate
            + "/100 Senate seats for your caucus. Incomplete slates go to the other caucus. Any agreement must be renewed.";
    }
    public boolean hasIncomingCongress() { return pendingHouse != null; }
    public String seatIncomingCongress() {
        house = pendingHouse; senate = pendingSenate; pendingHouse = null; pendingSenate = null; boolean onDesk = passed; finishBill(); passed = onDesk;
        return "JANUARY 3: new Congress seated. Unfinished legislation from the old Congress expires; new bills need fresh consideration.";
    }
    public void developerToggle() { house = house >= 218 ? 210 : 225; senate = senate >= 51 ? 48 : 52; finishBill(); }
}
