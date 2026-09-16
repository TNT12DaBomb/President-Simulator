import java.nio.file.*;
import java.util.*;

public final class GovernanceTests {
    private static int checks;
    private static void check(boolean b, String m) { checks++; if (!b) throw new AssertionError(m); }
    private static void send(CareerEngine e, CareerCommand c) { CareerReport r = e.submit(c); check(r.accepted(), r.messages().toString()); }
    private static void office(CareerEngine e, OfficeCommand.Type t) { send(e, CareerCommand.office(OfficeCommand.simple(t))); }
    private static void choice(CareerEngine e, OfficeCommand.Type t, int c) { send(e, CareerCommand.office(OfficeCommand.choice(t,c))); }
    private static void reject(CareerEngine e, CareerCommand c) { CareerView before=e.view(); int count=e.journal().size(); check(!e.submit(c).accepted(),"Expected rejection");check(before.equals(e.view())&&count==e.journal().size(),"Rejection changed state"); }
    private static CareerEngine game() {
        CareerEngine e=new CareerEngine(42, President.Difficulty.NORMAL, President.RunningMate.FUNDRAISER);
        send(e,CareerCommand.dev(CareerCommand.DeveloperAction.FORCE_WIN));send(e,CareerCommand.advance());send(e,CareerCommand.advance());
        choice(e,OfficeCommand.Type.EVENT_FREQUENCY,0);return e;
    }
    private static void next(CareerEngine e) { office(e,OfficeCommand.Type.END_MONTH); }
    private static void propose(CareerEngine e, Policy.Issue i, Policy.Approach a) { send(e,CareerCommand.office(OfficeCommand.policy(OfficeCommand.Type.PROPOSE,i,a))); }
    private static void replay(CareerEngine e) throws Exception {
        Path f=Files.createTempFile("governance-test", ".save");
        try { CareerSave.write(e,f);check(e.view().equals(CareerSave.read(f).view()),"Snapshot replay differs"); } finally {Files.delete(f);}
    }
    public static void main(String[] args) throws Exception {
        CareerEngine e=game();
        check(e.view().presidency().correspondence().requests().size()==16,"Distinct issue requests");
        choice(e,OfficeCommand.Type.REPLY,0);
        check(e.view().presidency().correspondence().requests().get(0).status()==PublicCorrespondence.Status.ACKNOWLEDGED,"Reply acknowledges only");
        reject(e,CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.REPLY,0)));
        reject(e,CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.REPLY,16)));
        replay(e); next(e);
        propose(e,Policy.Issue.ECONOMY,Policy.Approach.EXPAND_PROGRAM);office(e,OfficeCommand.Type.NEGOTIATE);next(e);
        int cash=e.view().treasury();office(e,OfficeCommand.Type.SIGN);
        check(e.view().treasury()==cash-180,"Signing funded appropriation");
        check(e.view().presidency().correspondence().requests().get(0).status()==PublicCorrespondence.Status.DELIVERY_SCHEDULED,"Funding isn't delivery");
        check(e.view().presidency().correspondence().requests().get(1).status()==PublicCorrespondence.Status.OPEN,"Competing request retained");
        int due=e.view().presidency().implementations().get(0).dueMonth();replay(e);
        while(e.view().termMonths()<due)next(e);
        check(e.view().presidency().correspondence().requests().get(0).status()==PublicCorrespondence.Status.DELIVERED,"Delivery changes public record");
        check(e.view().presidency().implementations().isEmpty(),"Delivery removed from queue");
        int updates=e.view().presidency().correspondence().history().size();next(e);
        check(updates==e.view().presidency().correspondence().history().size(),"Delivery applied once");
        propose(e,Policy.Issue.ECONOMY,Policy.Approach.EXPAND_PROGRAM);office(e,OfficeCommand.Type.NEGOTIATE);next(e);
        reject(e,CareerCommand.office(OfficeCommand.simple(OfficeCommand.Type.SIGN)));office(e,OfficeCommand.Type.VETO);replay(e);
        CareerEngine reversal=game();
        propose(reversal,Policy.Issue.HEALTHCARE,Policy.Approach.EXPAND_PROGRAM);office(reversal,OfficeCommand.Type.NEGOTIATE);next(reversal);office(reversal,OfficeCommand.Type.SIGN);
        propose(reversal,Policy.Issue.HEALTHCARE,Policy.Approach.REORGANIZE_PROGRAM);next(reversal);office(reversal,OfficeCommand.Type.NEGOTIATE);office(reversal,OfficeCommand.Type.SIGN);
        check(reversal.view().presidency().implementations().size()==1,"Superseded delivery cancelled");
        while(reversal.view().termMonths()<5)next(reversal);
        check(reversal.view().presidency().correspondence().requests().get(4).status()==PublicCorrespondence.Status.OPEN,"Cancelled initiative never delivered");
        check(reversal.view().presidency().correspondence().requests().get(5).status()==PublicCorrespondence.Status.DELIVERED,"Replacement delivered");replay(reversal);
        CareerEngine budget=game();
        while(budget.view().treasury()>=150){if(budget.view().presidency().actionsLeft()==0)next(budget);send(budget,CareerCommand.govern(CareerCommand.GovernanceAction.SERVICE_REVIEW));}
        if(budget.view().presidency().actionsLeft()<2)next(budget);
        propose(budget,Policy.Issue.TAXES,Policy.Approach.EXPAND_PROGRAM);office(budget,OfficeCommand.Type.NEGOTIATE);next(budget);
        check(budget.view().treasury()<180,"Insufficient budget scenario");reject(budget,CareerCommand.office(OfficeCommand.simple(OfficeCommand.Type.SIGN)));replay(budget);
        CareerEngine m=game();
        reject(m,CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.MIDTERM_TASK,0)));
        while(m.view().termMonths()<12)next(m);
        choice(m,OfficeCommand.Type.MIDTERM_TASK,0);reject(m,CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.MIDTERM_TASK,0)));next(m);
        // Partial work must not flip a slate; both objectives are required.
        choice(m,OfficeCommand.Type.MIDTERM_TASK,2);choice(m,OfficeCommand.Type.MIDTERM_TASK,3);next(m);
        choice(m,OfficeCommand.Type.MIDTERM_TASK,16);choice(m,OfficeCommand.Type.MIDTERM_TASK,17);replay(m);
        while(m.view().termMonths()<24)next(m);
        check(m.view().presidency().congress().houseSeats()==212 && m.view().presidency().congress().senateSeats()==49,"Partial and complete contest results");
        check(m.view().presidency().midtermContests().get(0).outcome().equals("Other caucus"),"Partial objective loses");
        reject(m,CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.MIDTERM_TASK,1)));replay(m);
        CareerEngine all=game();while(all.view().termMonths()<12)next(all);
        for(int id=0;id<12;id++){choice(all,OfficeCommand.Type.MIDTERM_TASK,id*2);choice(all,OfficeCommand.Type.MIDTERM_TASK,id*2+1);next(all);}
        check(all.view().presidency().congress().houseSeats()==226&&all.view().presidency().congress().senateSeats()==52,"All contest seats conserved");replay(all);
        CareerEngine late=game();while(late.view().termMonths()<46)next(late);
        propose(late,Policy.Issue.EDUCATION,Policy.Approach.EXPAND_PROGRAM);office(late,OfficeCommand.Type.NEGOTIATE);next(late);office(late,OfficeCommand.Type.SIGN);next(late);
        check(late.view().presidency().implementations().isEmpty(),"Term-end delivery settled");
        check(late.view().history().stream().anyMatch(x->x.contains("Archived request:")&&x.contains("DELIVERED")),"Cross-term request archive retained");replay(late);
        Path old=Files.createTempFile("old-monthly", ".save");try{Files.writeString(old,"format=presidency-monthly-v1\n");try{CareerSave.read(old);throw new AssertionError("Old rules silently reinterpreted");}catch(java.io.IOException expected){check(expected.getMessage().contains("Monthly Edition"),"Compatibility message");}}finally{Files.delete(old);}
        System.out.println("GovernanceTests: "+checks+" checks passed.");
    }
}
