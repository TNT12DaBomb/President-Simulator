import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;

public final class GovernanceUITests {
    private static int checks;
    private static void check(boolean b, String m) { checks++; if(!b)throw new AssertionError(m); }
    private static CareerEngine office() {
        CareerEngine e=new CareerEngine(21,President.Difficulty.NORMAL,President.RunningMate.FUNDRAISER);
        e.submit(CareerCommand.dev(CareerCommand.DeveloperAction.FORCE_WIN)); e.submit(CareerCommand.advance()); e.submit(CareerCommand.advance());
        e.submit(CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.EVENT_FREQUENCY,0)));return e;
    }
    private record Result(String output, CareerEngine engine) { }
    private static Result run(CareerEngine e,String input) throws Exception {
        Path dir=Files.createTempDirectory("governance-ui");Path save=dir.resolve("governance.save");
        try { CareerSave.write(e,save);ByteArrayOutputStream bytes=new ByteArrayOutputStream();
            new TextUI(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),new PrintStream(bytes),21,save,false,false).run();
            return new Result(bytes.toString(StandardCharsets.UTF_8),CareerSave.read(save));
        } finally {Files.deleteIfExists(save);Files.delete(dir);}
    }
    public static void main(String[] args) throws Exception {
        Result reply=run(office(),"2\n1\n\n3\n1\n1\n1\n\n");
        check(reply.output().contains("PUBLIC REQUESTS"),"Request menu reachable");
        check(reply.engine().view().presidency().correspondence().requests().get(0).answered(),"Targeted reply saved from UI");
        check(reply.engine().view().presidency().actionsLeft()==1,"Reply consumed one action");
        Result policy=run(office(),"2\n1\n\n2\n1\n1\n1\n1\n1\n\n");
        check(policy.output().contains("Open regional apprenticeship desks"),"Named policy displayed");
        check(policy.output().contains("Tradeoff:")&&policy.output().contains("$180"),"Tradeoff and cost shown before selection");
        check(policy.engine().view().presidency().bill()!=null,"UI proposal saved");
        CareerEngine m=office();m.submit(CareerCommand.dev(CareerCommand.DeveloperAction.MIDTERM_START));
        Result visit=run(m,"2\n1\n\n4\n1\n1\n1\n1\n\n");
        check(visit.output().contains("Harbor House slate"),"Contest board reachable");
        check(visit.engine().view().presidency().midtermContests().get(0).listened(),"Correct contest/task selected");
        check(visit.engine().view().presidency().actionsLeft()==1,"Visit spends one action");
        CareerEngine untouched=office();Result browse=run(untouched,"2\n1\n\n3\n1\n5\n6\n0\n");
        check(browse.engine().view().equals(untouched.view()),"Paging/back/EOF don't change gameplay");
        check(browse.output().contains("PAGE 2 OF 4"),"Correspondence paginated");
        System.out.println("GovernanceUITests: "+checks+" checks passed.");
    }
}
