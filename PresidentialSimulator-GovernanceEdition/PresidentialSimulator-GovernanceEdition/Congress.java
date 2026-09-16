/** Authored board-game seats, not a model of actual electoral or legislative probabilities. */
public final class Congress {
    private int house = 222, senate = 49;
    private boolean agreement;
    private int midterms;
    public record View(int houseSeats, int senateSeats, String houseControl, String senateControl,
                       boolean agreement, int midterms, String rule) { }
    public View view() { return new View(house, senate, house >= 218 ? "Your caucus" : "Other caucus",
        senate >= 51 ? "Your caucus" : "Other caucus", agreement, midterms,
        "A bill needs 218 House seats and 51 Senate seats, or a negotiated cross-caucus agreement. Ties require negotiation."); }
    public void negotiate() { agreement = true; }
    public boolean canPass() { return house >= 218 && senate >= 51 || agreement; }
    public void finishBill() { agreement = false; }
    public String midterm(MidtermCampaign campaign) {
        campaign.resolve(); house = campaign.houseSeats(); senate = campaign.senateSeats();
        agreement = false; midterms++;
        return "MIDTERMS: completed contest objectives yield " + house + "/435 House seats and " + senate
            + "/100 Senate seats for your caucus. Incomplete slates go to the other caucus. Any agreement must be renewed.";
    }
    public void developerToggle() { house = house >= 218 ? 210 : 225; senate = senate >= 51 ? 48 : 52; agreement = false; }
}
