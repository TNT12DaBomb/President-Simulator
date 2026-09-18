import java.nio.file.*;
import java.util.*;
public class ColorTests {
 public static void main(String[] args)throws Exception{
 for(var color:TeamColors.values())for(long seed=0;seed<100;seed++){if(TeamColors.opponent(seed,color.ordinal())==color)throw new AssertionError("same opponent color");}
 var career=new CareerEngine(22,President.Difficulty.NORMAL,President.RunningMate.FUNDRAISER,GameRules.CURRENT);for(var color:TeamColors.values()){var report=career.submit(CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.CAMPAIGN_COLOR,color.ordinal())));if(!report.accepted()||career.campaignColor()!=color.ordinal())throw new AssertionError("color command");}
 Path p=Files.createTempFile("color-test",".save");try{CareerSave.write(career,p);if(CareerSave.read(p).campaignColor()!=career.campaignColor())throw new AssertionError("save color");}finally{Files.deleteIfExists(p);}
 if(NewsFeed.headlines(career.view()).size()!=2)throw new AssertionError("news compact");
 System.out.println("PASS: distinct deterministic opponent colors; saved color choices; two headlines.");
 }
}
