public class President {

    private int approval;
    private int economy;
    private int publicTrust;
    private int partySupport;
    private int treasury;
    private int scandal;
    private int nationalSecurity;
    private int health;
    private int internationalRelations;

    public President(
            int approval,
            int economy,
            int publicTrust,
            int partySupport,
            int treasury,
            int scandal,
            int nationalSecurity,
            int health,
            int internationalRelations) {

        this.approval = approval;
        this.economy = economy;
        this.publicTrust = publicTrust;
        this.partySupport = partySupport;
        this.treasury = treasury;
        this.scandal = scandal;
        this.nationalSecurity = nationalSecurity;
        this.health = health;
        this.internationalRelations = internationalRelations;
    }

    // ------------------------
    // GETTERS
    // ------------------------

    public int getApproval() {
        return approval;
    }

    public int getEconomy() {
        return economy;
    }

    public int getPublicTrust() {
        return publicTrust;
    }

    public int getPartySupport() {
        return partySupport;
    }

    public int getTreasury() {
        return treasury;
    }

    public int getScandal() {
        return scandal;
    }

    public int getNationalSecurity() {
        return nationalSecurity;
    }

    public int getHealth() {
        return health;
    }

    public int getInternationalRelations() {
        return internationalRelations;
    }

    // ------------------------
    // MODIFIERS
    // ------------------------

    public void changeApproval(int amount) {
        approval += amount;
    }

    public void changeEconomy(int amount) {
        economy += amount;
    }

    public void changePublicTrust(int amount) {
        publicTrust += amount;
    }

    public void changePartySupport(int amount) {
        partySupport += amount;
    }

    public void changeTreasury(int amount) {
        treasury += amount;
    }

    public void changeScandal(int amount) {
        scandal += amount;
    }

    public void changeNationalSecurity(int amount) {
        nationalSecurity += amount;
    }

    public void changeHealth(int amount) {
        health += amount;
    }

    public void changeInternationalRelations(int amount) {
        internationalRelations += amount;
    }
}