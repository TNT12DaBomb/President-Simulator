import java.nio.file.*;
/** Calendar bookkeeping only; does not assert political outcomes or metrics. */
public final class MonthFlowTests {
 static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
 public static void main(String[] args)throws Exception{
  CareerEngine e=new CareerEngine(91,President.Difficulty.NORMAL,President.RunningMate.FUNDRAISER);
  e.submit(CareerCommand.dev(CareerCommand.DeveloperAction.FORCE_WIN));e.submit(CareerCommand.advance());e.submit(CareerCommand.advance());
  e.submit(CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.EVENT_FREQUENCY,0)));
  int month=e.view().presidency().monthsCompleted();
  check(e.submit(CareerCommand.govern(CareerCommand.GovernanceAction.REST)).accepted(),"first action");
  check(e.view().presidency().monthsCompleted()==month&&e.view().presidency().actionsLeft()==1,"one action stays in month");
  check(e.submit(CareerCommand.govern(CareerCommand.GovernanceAction.REST)).accepted(),"second action");
  check(e.view().presidency().monthsCompleted()==month+1&&e.view().presidency().actionsLeft()==2,"automatic reset");
  Path save=Files.createTempFile("calendar-check", ".properties");try{CareerSave.write(e,save);CareerEngine loaded=CareerSave.read(save);check(loaded.view().presidency().monthsCompleted()==month+1&&loaded.view().presidency().actionsLeft()==2,"replay keeps calendar");}finally{Files.deleteIfExists(save);}
  System.out.println("PASS: two-action calendar progression and save replay.");
 }
}
