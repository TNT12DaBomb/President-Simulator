import java.io.*;
import java.nio.file.*;
import java.util.*;

public final class MonthlyTests {
    private static int checks;
    private static void check(boolean test, String message) { checks++; if (!test) throw new AssertionError(message); }
    private static CareerEngine fresh(long seed) { return new CareerEngine(seed, President.Difficulty.NORMAL, President.RunningMate.FUNDRAISER); }
    private static void send(CareerEngine e, CareerCommand c) { CareerReport r = e.submit(c); check(r.accepted(), c + ": " + r.messages()); }
    private static void office(CareerEngine e, OfficeCommand.Type t) { send(e, CareerCommand.office(OfficeCommand.simple(t))); }
    private static void govern(CareerEngine e, CareerCommand.GovernanceAction a) { send(e, CareerCommand.govern(a)); }
    private static void pending(CareerEngine e) { if (e.view().presidency().pendingEvent() != null) send(e, CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.EVENT_CHOICE, 1))); }
    private static void month(CareerEngine e) { pending(e); office(e, OfficeCommand.Type.END_MONTH); }
    private static CareerEngine inaugurated(long seed) {
        CareerEngine e = fresh(seed); send(e, CareerCommand.dev(CareerCommand.DeveloperAction.FORCE_WIN));
        check(e.view().presidency() == null, "Not inaugurated on election night");
        send(e, CareerCommand.advance()); check(e.view().phase() == CareerView.Phase.TRANSITION, "Transition");
        send(e, CareerCommand.advance()); check(e.view().presidency() != null, "Presidency object created"); return e;
    }
    private static void replay(CareerEngine e) throws Exception {
        Path path = Files.createTempFile("monthly-replay-", ".save");
        try { CareerSave.write(e, path); CareerEngine loaded = CareerSave.read(path);
            check(loaded.view().equals(e.view()), "Exact snapshot replay"); check(loaded.journal().equals(e.journal()), "Exact command replay");
        } finally { Files.delete(path); }
    }
    public static void main(String[] args) throws Exception {
        CareerEngine e = inaugurated(42);
        check(e.view().presidency().date().equals("2029-01"), "Calendar starts January 2029");
        govern(e, CareerCommand.GovernanceAction.BUDGET_REVIEW); int cash = e.view().treasury();
        govern(e, CareerCommand.GovernanceAction.BUDGET_REVIEW);
        check(e.view().treasury() == cash - 25, "Annual refund not farmable");
        int journal = e.journal().size(); CareerView before = e.view();
        check(!e.submit(CareerCommand.govern(CareerCommand.GovernanceAction.REST)).accepted(), "Two action cap");
        check(e.view().equals(before) && e.journal().size() == journal, "Rejection doesn't mutate");
        month(e); check(e.view().termMonths() == 1 && e.view().presidency().actionsLeft() == 2, "Explicit month advancement");
        govern(e, CareerCommand.GovernanceAction.CAMPAIGN_ALLIES); govern(e, CareerCommand.GovernanceAction.PUBLIC_BRIEFING);
        while(e.view().termMonths() < 12) month(e);
        for (int contest : new int[]{0, 1, 2, 3, 8, 9, 10}) {
            pending(e);
            send(e, CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.MIDTERM_TASK, contest * 2)));
            send(e, CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.MIDTERM_TASK, contest * 2 + 1)));
            month(e);
        }
        while(e.view().termMonths() < 23) month(e);
        check(e.view().presidency().congress().midterms() == 0, "No early midterm");
        month(e); check(e.view().presidency().congress().midterms() == 1, "Month 24 midterm");
        check(e.view().presidency().congress().houseSeats() == 218 && e.view().presidency().congress().senateSeats() == 51, "Targeted contest work wins chamber majorities");
        replay(e);
        while(e.view().phase() == CareerView.Phase.PRESIDENCY) month(e);
        check(e.view().phase() == CareerView.Phase.TERM_REVIEW && e.view().servedMonths() == 48, "Full first term");
        check(e.view().presidency().congress().midterms() == 1, "Exactly one midterm per term");
        send(e, CareerCommand.runAgain()); check(e.view().cycle() == 2, "Second election opens");
        send(e, CareerCommand.dev(CareerCommand.DeveloperAction.FORCE_WIN)); send(e, CareerCommand.advance()); send(e, CareerCommand.advance());
        while(e.view().phase() == CareerView.Phase.PRESIDENCY) month(e);
        check(e.view().phase() == CareerView.Phase.RETIRED && e.view().servedMonths() == 96, "Eight-year retirement");
        check(!e.submit(CareerCommand.runAgain()).accepted(), "No third run"); replay(e);
        CareerEngine idle = inaugurated(0);
        while (idle.view().termMonths() < 24) month(idle);
        check(idle.view().presidency().congress().houseSeats() == 210 && idle.view().presidency().congress().senateSeats() == 48, "Idle midterm branch differs");
        CareerEngine policy = fresh(17);
        send(policy, CareerCommand.office(OfficeCommand.policy(OfficeCommand.Type.PLEDGE, Policy.Issue.TAXES, Policy.Approach.EXPAND_PROGRAM)));
        check(!policy.submit(CareerCommand.office(OfficeCommand.policy(OfficeCommand.Type.PLEDGE, Policy.Issue.TAXES, Policy.Approach.REORGANIZE_PROGRAM))).accepted(), "Pledges cannot be overwritten");
        send(policy, CareerCommand.dev(CareerCommand.DeveloperAction.FORCE_WIN)); send(policy, CareerCommand.advance()); send(policy, CareerCommand.advance());
        send(policy, CareerCommand.office(OfficeCommand.policy(OfficeCommand.Type.PROPOSE, Policy.Issue.TAXES, Policy.Approach.EXPAND_PROGRAM)));
        check(!policy.submit(CareerCommand.office(OfficeCommand.simple(OfficeCommand.Type.SIGN))).accepted(), "Split Congress blocks signature");
        office(policy, OfficeCommand.Type.NEGOTIATE); month(policy); office(policy, OfficeCommand.Type.SIGN);
        check(policy.view().presidency().promises().get(0).status().equals("Kept"), "Matching law keeps promise");
        check(!policy.view().presidency().congress().agreement(), "Agreement consumed");
        send(policy, CareerCommand.office(OfficeCommand.policy(OfficeCommand.Type.PROPOSE, Policy.Issue.TAXES, Policy.Approach.REORGANIZE_PROGRAM)));
        month(policy); office(policy, OfficeCommand.Type.NEGOTIATE); office(policy, OfficeCommand.Type.SIGN);
        check(policy.view().presidency().promises().get(0).status().startsWith("Contradicted"), "Reversal recorded"); replay(policy);
        while(policy.view().phase() == CareerView.Phase.PRESIDENCY) month(policy);
        check(policy.view().nextCampaignEffects().stream().noneMatch(s -> s.contains("OUTREACH")), "Broken promise withholds outreach carryover");
        CareerEngine event = inaugurated(7);
        send(event, CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.EVENT_FREQUENCY, 2)));
        while(event.view().termMonths() < 3) month(event);
        check(event.view().presidency().pendingEvent() != null, "Scheduled event"); replay(event);
        before = event.view(); check(!event.submit(CareerCommand.office(OfficeCommand.simple(OfficeCommand.Type.END_MONTH))).accepted(), "Pending event blocks time");
        check(before.equals(event.view()), "Rejected event bypass unchanged");
        send(event, CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.EVENT_CHOICE, 0)));
        check(!event.view().presidency().delayedEffects().isEmpty(), "Paid response schedules follow-up");
        int due = event.view().presidency().delayedEffects().get(0).dueMonth(); replay(event);
        while(event.view().termMonths() < due) month(event);
        check(event.view().presidency().delayedEffects().isEmpty(), "Due effect applied once"); replay(event);
        CareerEngine loss = fresh(2); send(loss, CareerCommand.dev(CareerCommand.DeveloperAction.FORCE_LOSS)); send(loss, CareerCommand.advance());
        for(int i=0;i<4;i++) send(loss, CareerCommand.rebuild(CareerCommand.RebuildAction.PRIVATE_LIFE));
        send(loss, CareerCommand.runAgain()); check(loss.view().campaign().playerFunds() == 1400, "Unresolved loss withholds 200 from fundraiser start");
        replay(loss);
        for(CareerCommand.DeveloperAction d : List.of(CareerCommand.DeveloperAction.MIDTERM_START, CareerCommand.DeveloperAction.MID_FIRST_TERM, CareerCommand.DeveloperAction.MID_SECOND_TERM, CareerCommand.DeveloperAction.FINAL_QUARTER, CareerCommand.DeveloperAction.REELECTION_START)) {
            CareerEngine dev = fresh(77); send(dev, CareerCommand.dev(d)); replay(dev);
            if(dev.view().phase() == CareerView.Phase.PRESIDENCY) { send(dev, CareerCommand.dev(CareerCommand.DeveloperAction.FINISH_TERM)); replay(dev); }
        }
        CareerEngine late = inaugurated(92);
        send(late, CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.EVENT_FREQUENCY, 0)));
        while(late.view().termMonths() < 47) month(late);
        send(late, CareerCommand.dev(CareerCommand.DeveloperAction.TRIGGER_EVENT));
        check(!late.view().presidency().pendingEvent().requiresLaw(), "Law-only event excluded without a law");
        send(late, CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.EVENT_CHOICE, 0)));
        check(late.view().presidency().delayedEffects().get(0).dueMonth() == 48, "Late effect clamped to term boundary");
        month(late); check(late.view().presidency().delayedEffects().isEmpty(), "Term-end effects settled");
        check(late.view().presidency().pendingEvent() == null, "No fresh event at term completion"); replay(late);
        CareerEngine annual = inaugurated(11);
        govern(annual, CareerCommand.GovernanceAction.BUDGET_REVIEW);
        while(annual.view().termMonths() < 12) month(annual);
        pending(annual); int openingCash = annual.view().treasury();
        govern(annual, CareerCommand.GovernanceAction.BUDGET_REVIEW);
        check(annual.view().treasury() == openingCash + 75, "New-year reconciliation available once again");
        Path oldCampaign = Files.createTempFile("campaign-import-", ".save");
        try { GameEngine g = new GameEngine(5, President.Difficulty.NORMAL, President.RunningMate.FUNDRAISER);
            g.submit(GameCommand.rest()); CampaignSave.write(g, oldCampaign);
            CareerEngine imported = CareerSave.read(oldCampaign);
            check(imported.view().campaign().equals(g.view()), "Old campaign import preserves exact campaign");
        } finally { Files.delete(oldCampaign); }
        Path legacy = Files.createTempFile("legacy-quarter", ".save");
        try { Files.writeString(legacy, "format=presidency-career-v1\n");
            try { CareerSave.read(legacy); throw new AssertionError("Old quarter save accepted"); } catch(IOException expected) { check(expected.getMessage().contains("Quarterly"), "Explicit migration boundary"); }
        } finally { Files.delete(legacy); }
        Path dir = Files.createTempDirectory("monthly-ui-"); Path save = dir.resolve("monthly.save");
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            String input = "1\n1\n1\n\n0\n2\n1\n1\n1\n\n1\n\n1\n1\n\n";
            new TextUI(new ByteArrayInputStream(input.getBytes(java.nio.charset.StandardCharsets.UTF_8)), new PrintStream(bytes), 3, save, false, false).run();
            String output = bytes.toString(java.nio.charset.StandardCharsets.UTF_8);
            check(output.contains("PRESIDENTIAL DESK") && output.contains("ACTIONS LEFT"), "Guided UI reaches monthly desk");
            check(CareerSave.read(save).view().phase() == CareerView.Phase.PRESIDENCY, "EOF autosave resumes office");
        } finally { if(Files.exists(save)) Files.delete(save); Files.delete(dir); }
        System.out.println("MonthlyTests: " + checks + " checks passed.");
    }
}
