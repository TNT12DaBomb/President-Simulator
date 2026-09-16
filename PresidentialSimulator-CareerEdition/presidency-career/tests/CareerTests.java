import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.List;

public final class CareerTests {
    private static int checks;
    private static void check(boolean value, String message) { checks++; if (!value) throw new AssertionError(message); }
    private static CareerEngine fresh(long seed) { return new CareerEngine(seed, President.Difficulty.NORMAL, President.RunningMate.FUNDRAISER); }
    private static void accepted(CareerEngine c, CareerCommand command) {
        CareerReport report = c.submit(command); check(report.accepted(), "Rejected: " + command + " " + report.messages()); invariants(c);
    }
    private static void invariants(CareerEngine c) {
        CareerView v = c.view();
        check(v.electionsWon() <= 2 && v.completedTerms() <= 2 && v.servedQuarters() <= 32, "Term cap");
        check(v.treasury() >= 0 && v.termQuarters() <= 16 && v.comebackYears() <= 4, "Resource/time bounds");
        check(v.campaign().playerEV() + v.campaign().opponentEV() == 538, "Board total");
        check(v.elections().stream().filter(e -> e.outcome().equals("WIN")).count() == v.electionsWon(), "Win history");
        check(v.elections().stream().filter(e -> e.outcome().equals("LOSS")).count() == v.electionsLost(), "Loss history");
    }
    private static void forceWinIntoOffice(CareerEngine c) {
        accepted(c, CareerCommand.dev(CareerCommand.DeveloperAction.FORCE_WIN));
        check(c.latestResult() == null && c.view().elections().get(c.view().elections().size() - 1).playerEV() == null, "Forced outcome has no fake tally");
        accepted(c, CareerCommand.advance()); accepted(c, CareerCommand.advance());
    }
    private static void balancedTerm(CareerEngine c) {
        CareerCommand.GovernanceAction[] actions = {CareerCommand.GovernanceAction.PUBLIC_BRIEFING, CareerCommand.GovernanceAction.CABINET_MEETING,
            CareerCommand.GovernanceAction.BUDGET_REVIEW, CareerCommand.GovernanceAction.SERVICE_REVIEW};
        while (c.view().phase() == CareerView.Phase.PRESIDENCY) accepted(c, CareerCommand.govern(actions[c.view().termQuarters() % 4]));
    }
    private static void privateYears(CareerEngine c) {
        for (int i = 0; i < 4; i++) accepted(c, CareerCommand.rebuild(CareerCommand.RebuildAction.PRIVATE_LIFE));
    }
    private static void playCampaign(CareerEngine c, boolean act) {
        while (c.view().phase() == CareerView.Phase.CAMPAIGN) {
            GameView v = c.view().campaign();
            if (v.pendingEvent() != null) { accepted(c, CareerCommand.campaign(GameCommand.respond(1))); continue; }
            GameCommand cmd = GameCommand.rest();
            if (act) {
                cmd = GameCommand.fundraise();
                outer: for (GameView.StateView s : v.states().stream().filter(st -> !st.playerControls())
                    .sorted(java.util.Comparator.comparingInt(GameView.StateView::electoralVotes).reversed()).toList())
                    for (GameView.ActionView a : s.actions()) if (a.available()) { cmd = GameCommand.campaign(s.name(), a.task()); break outer; }
            }
            accepted(c, CareerCommand.campaign(cmd));
        }
    }
    public static void main(String[] args) throws Exception {
        CareerEngine c = fresh(42);
        CareerView original = c.view();
        check(!c.submit(CareerCommand.govern(CareerCommand.GovernanceAction.PUBLIC_BRIEFING)).accepted(), "No governing before office");
        check(!c.submit(CareerCommand.advance()).accepted() && !c.submit(CareerCommand.runAgain()).accepted(), "Phase gates");
        check(original.equals(c.view()) && c.journal().isEmpty(), "Rejected command has no side effects");
        forceWinIntoOffice(c); balancedTerm(c);
        check(c.view().phase() == CareerView.Phase.TERM_REVIEW && c.view().year() == 5, "First term ends after four years");
        check(c.view().servedQuarters() == 16 && c.view().completedTerms() == 1, "First term service");
        check(c.view().treasury() == 1200, "Balanced administrative ledger");
        check(c.view().nextCampaignEffects().size() == 4, "Final-year work and private fundraiser prepared");
        accepted(c, CareerCommand.runAgain());
        check(c.view().campaign().playerFunds() == 1700, "Private fundraiser adds $100, not treasury funds");
        check(c.view().campaign().states().stream().mapToInt(s -> s.playerTasks().size()).sum() == 3, "Three inherited tasks");
        check(c.view().nextCampaignEffects().size() == 1, "Carryover consumed once");
        forceWinIntoOffice(c); balancedTerm(c);
        check(c.view().phase() == CareerView.Phase.RETIRED && c.view().servedQuarters() == 32, "Eight years ends normal career");
        check(!c.submit(CareerCommand.runAgain()).accepted(), "No third run");
        check(!c.submit(CareerCommand.govern(CareerCommand.GovernanceAction.ROUTINE_QUARTER)).accepted(), "No ninth year");
        testLosses(); testNonconsecutive(); testBudgets(); testDeveloperScenarios(); testNaturalCampaign(); testPersistence();
        System.out.println("PASS: " + checks + " career checks; phase transitions, comeback, reelection, eight-year cap, developer tools, and save replay.");
    }
    private static void testLosses() {
        CareerEngine c = fresh(7);
        playCampaign(c, false);
        check(c.view().electionsLost() == 1 && !c.view().developerUsed(), "Natural loss recorded");
        check(c.view().reputation().contains("loss") && c.view().support().contains("$200"), "Loss has persistent narrative/resource consequences");
        accepted(c, CareerCommand.advance());
        check(!c.submit(CareerCommand.runAgain()).accepted(), "Comeback needs four years");
        privateYears(c); accepted(c, CareerCommand.runAgain());
        check(c.view().year() == 5 && c.view().campaign().playerFunds() == 1400, "Unresolved donor withholding persists");
        accepted(c, CareerCommand.dev(CareerCommand.DeveloperAction.FORCE_LOSS)); accepted(c, CareerCommand.advance());
        accepted(c, CareerCommand.rebuild(CareerCommand.RebuildAction.DONOR_MEETINGS));
        accepted(c, CareerCommand.rebuild(CareerCommand.RebuildAction.CAMPAIGN_REVIEW));
        accepted(c, CareerCommand.rebuild(CareerCommand.RebuildAction.COMMUNITY_WORK));
        accepted(c, CareerCommand.rebuild(CareerCommand.RebuildAction.PRIVATE_LIFE));
        check(!c.submit(CareerCommand.rebuild(CareerCommand.RebuildAction.DONOR_MEETINGS)).accepted(), "No farming extra off-years");
        accepted(c, CareerCommand.runAgain());
        check(c.view().electionsLost() == 2 && c.view().year() == 9, "Losses retained across cycles");
        check(c.view().campaign().playerFunds() == 1600, "Donor meeting restores withheld resources");
        check(c.view().campaign().states().stream().mapToInt(s -> s.playerTasks().size()).sum() == 2, "Rebuilding prepares real objectives");
        check(c.view().reputation().contains("review published"), "Review is documented");
    }
    private static void testNonconsecutive() {
        CareerEngine c = fresh(11); forceWinIntoOffice(c); balancedTerm(c); accepted(c, CareerCommand.runAgain());
        accepted(c, CareerCommand.dev(CareerCommand.DeveloperAction.FORCE_LOSS)); accepted(c, CareerCommand.advance());
        privateYears(c); accepted(c, CareerCommand.runAgain()); forceWinIntoOffice(c); balancedTerm(c);
        check(c.view().year() == 13 && c.view().servedQuarters() == 32 && c.view().completedTerms() == 2, "Nonconsecutive terms count together");
        check(c.view().electionsLost() == 1 && c.view().phase() == CareerView.Phase.RETIRED, "Nonconsecutive cap retains loss");
    }
    private static void testBudgets() {
        CareerEngine c = fresh(1); forceWinIntoOffice(c);
        int start = c.view().treasury();
        accepted(c, CareerCommand.govern(CareerCommand.GovernanceAction.BUDGET_REVIEW));
        check(c.view().treasury() == start + 125, "First annual recovery");
        accepted(c, CareerCommand.govern(CareerCommand.GovernanceAction.BUDGET_REVIEW));
        check(c.view().treasury() == start + 150, "No duplicate recovery");
        accepted(c, CareerCommand.govern(CareerCommand.GovernanceAction.ROUTINE_QUARTER));
        accepted(c, CareerCommand.govern(CareerCommand.GovernanceAction.ROUTINE_QUARTER));
        check(c.view().annualWork().isEmpty(), "New year starts with a clear current-year checklist");
        int before = c.view().treasury(); accepted(c, CareerCommand.govern(CareerCommand.GovernanceAction.BUDGET_REVIEW));
        check(c.view().treasury() == before + 125, "Recovery becomes available next year");
        c = fresh(2); forceWinIntoOffice(c);
        while (c.view().phase() == CareerView.Phase.PRESIDENCY && c.view().treasury() + 50 >= 150)
            accepted(c, CareerCommand.govern(CareerCommand.GovernanceAction.SERVICE_REVIEW));
        CareerView snapshot = c.view();
        check(!c.submit(CareerCommand.govern(CareerCommand.GovernanceAction.SERVICE_REVIEW)).accepted(), "Unaffordable governance rejected");
        check(snapshot.equals(c.view()), "Unaffordable quarter is atomic");
        accepted(c, CareerCommand.govern(CareerCommand.GovernanceAction.ROUTINE_QUARTER));
        check(c.view().treasury() == snapshot.treasury() + 50, "Routine operations recover reserve");
    }
    private static void testDeveloperScenarios() {
        for (CareerCommand.DeveloperAction action : List.of(CareerCommand.DeveloperAction.MID_FIRST_TERM, CareerCommand.DeveloperAction.MID_SECOND_TERM,
            CareerCommand.DeveloperAction.FINAL_QUARTER, CareerCommand.DeveloperAction.REELECTION_START)) {
            CareerEngine c = fresh(99); accepted(c, CareerCommand.dev(action)); check(c.view().developerUsed(), "Developer flag persists");
            if (action == CareerCommand.DeveloperAction.MID_FIRST_TERM) check(c.view().servedQuarters() == 8 && c.view().year() == 3, "First midpoint");
            if (action == CareerCommand.DeveloperAction.MID_SECOND_TERM) check(c.view().servedQuarters() == 24 && c.view().year() == 7, "Second midpoint");
            if (action == CareerCommand.DeveloperAction.FINAL_QUARTER) {
                accepted(c, CareerCommand.govern(CareerCommand.GovernanceAction.ROUTINE_QUARTER));
                check(c.view().phase() == CareerView.Phase.RETIRED && c.view().servedQuarters() == 32, "Final-quarter fixture retires correctly");
            }
            if (action == CareerCommand.DeveloperAction.REELECTION_START) check(c.view().phase() == CareerView.Phase.CAMPAIGN && c.view().completedTerms() == 1, "Reelection fixture");
        }
        CareerEngine c = fresh(9); int cash = c.view().campaign().playerFunds(); accepted(c, CareerCommand.dev(CareerCommand.DeveloperAction.ADD_CAMPAIGN_CASH));
        check(c.view().campaign().playerFunds() == cash + 500, "Developer cash"); forceWinIntoOffice(c);
        int treasury = c.view().treasury(); accepted(c, CareerCommand.dev(CareerCommand.DeveloperAction.ADD_TREASURY));
        check(c.view().treasury() == treasury + 500, "Developer treasury");
        accepted(c, CareerCommand.dev(CareerCommand.DeveloperAction.FINISH_TERM));
        check(c.view().servedQuarters() == 16 && c.view().phase() == CareerView.Phase.TERM_REVIEW, "Term skip uses actual routine quarters");
        check(c.view().nextCampaignEffects().get(0).contains("No inherited"), "Skipped governing does not invent achievements");
    }
    private static void testNaturalCampaign() {
        CareerEngine winner = null;
        for (int seed = 0; seed < 30 && winner == null; seed++) {
            CareerEngine candidate = fresh(seed); playCampaign(candidate, true);
            if (candidate.view().electionsWon() == 1) winner = candidate;
        }
        check(winner != null && !winner.view().developerUsed() && winner.latestResult().playerWon(), "Natural campaign leads to a real presidency");
        check(!winner.view().transitionResources().isEmpty(), "Campaign work affects transition resources");
        accepted(winner, CareerCommand.advance()); accepted(winner, CareerCommand.advance());
        CareerView.GovernanceOption credit = winner.view().governanceOptions().stream().filter(o -> o.cost() < o.action().cost()).findFirst().orElseThrow();
        accepted(winner, CareerCommand.govern(credit.action()));
        CareerView.GovernanceOption used = winner.view().governanceOptions().stream().filter(o -> o.action() == credit.action()).findFirst().orElseThrow();
        check(used.cost() == used.action().cost(), "Transition discount consumed once");
        CareerEngine snapshotOwner = winner;
        boolean immutable = false; try { snapshotOwner.view().elections().clear(); } catch (UnsupportedOperationException ex) { immutable = true; }
        check(immutable, "Career snapshots immutable");
    }
    private static void testPersistence() throws Exception {
        Path file = Files.createTempFile("career-test-", ".save");
        try {
            CareerEngine c = fresh(9);
            while (c.view().campaign().pendingEvent() == null && c.view().phase() == CareerView.Phase.CAMPAIGN) accepted(c, CareerCommand.campaign(GameCommand.rest()));
            check(c.view().campaign().pendingEvent() != null, "Pending fixture exists"); roundTrip(c, file);
            accepted(c, CareerCommand.dev(CareerCommand.DeveloperAction.FORCE_LOSS)); roundTrip(c, file);
            accepted(c, CareerCommand.advance()); roundTrip(c, file); privateYears(c); accepted(c, CareerCommand.runAgain()); roundTrip(c, file);
            forceWinIntoOffice(c); roundTrip(c, file);
            for (int i = 0; i < 16; i++) { accepted(c, CareerCommand.govern(CareerCommand.GovernanceAction.BUDGET_REVIEW)); roundTrip(c, file); }
            accepted(c, CareerCommand.runAgain()); roundTrip(c, file); forceWinIntoOffice(c); balancedTerm(c); roundTrip(c, file);
            accepted(c, CareerCommand.dev(CareerCommand.DeveloperAction.MID_FIRST_TERM)); roundTrip(c, file);
            accepted(c, CareerCommand.dev(CareerCommand.DeveloperAction.ADD_TREASURY)); roundTrip(c, file);
            accepted(c, CareerCommand.dev(CareerCommand.DeveloperAction.REELECTION_START)); roundTrip(c, file);
            accepted(c, CareerCommand.dev(CareerCommand.DeveloperAction.ADD_CAMPAIGN_CASH)); roundTrip(c, file);
            GameEngine legacy = new GameEngine(9, President.Difficulty.NORMAL, President.RunningMate.FUNDRAISER);
            legacy.submit(GameCommand.rest()); CampaignSave.write(legacy, file);
            CareerEngine imported = CareerSave.read(file);
            check(imported.view().campaign().equals(legacy.view()) && !imported.view().developerUsed(), "Legacy import preserves campaign");
            CareerSave.write(imported, file); roundTrip(imported, file);
            Files.writeString(file, "format=unknown\n"); boolean failed = false;
            try { CareerSave.read(file); } catch (IOException ex) { failed = true; }
            check(failed, "Unknown format rejected");
            Files.writeString(file, "format=presidency-career-v1\nseed=1\ndifficulty=NORMAL\nmate=FUNDRAISER\ncommands=1\ncommand.0.type=GOVERN\ncommand.0.governance=PUBLIC_BRIEFING\n");
            failed = false; try { CareerSave.read(file); } catch (IOException ex) { failed = true; }
            check(failed, "Illegal phase replay rejected");
        } finally { Files.deleteIfExists(file); }
    }
    private static void roundTrip(CareerEngine c, Path file) throws Exception {
        CareerSave.write(c, file); CareerEngine restored = CareerSave.read(file);
        check(restored.view().equals(c.view()), "Exact career save/load: " + c.view().phase());
        check(restored.journal().equals(c.journal()), "Journal restored");
    }
}
