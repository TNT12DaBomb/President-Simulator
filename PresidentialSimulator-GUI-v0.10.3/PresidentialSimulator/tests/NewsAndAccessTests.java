import java.nio.file.*;
import java.util.*;
public class NewsAndAccessTests {
 static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
 public static void main(String[] args)throws Exception{
  Set<Long> seeds=new HashSet<>();for(int i=0;i<10000;i++)check(seeds.add(GameSeeds.fresh()),"seed collision in sample");
  char[] yes="devtools".toCharArray();check(DeveloperAccess.accepts(yes),"correct password");for(char c:yes)check(c=='\0',"clear password buffer");check(!DeveloperAccess.accepts("wrong".toCharArray()),"wrong password");check(!DeveloperAccess.accepts("DEVTOOLS".toCharArray()),"case sensitive");
  Path dir=Files.createTempDirectory("activity-test");Path file=dir.resolve("activity.properties");try{var inbox=new ActivityInbox(file);var first=new DesktopController.Notice("Action unavailable","Cannot afford this action",true);var second=new DesktopController.Notice("Completed","The report remains",false);inbox.add(first);inbox.add(second);check(new ActivityInbox(file).entries().equals(List.of(first,second)),"restart retention");inbox.dismiss(0);check(new ActivityInbox(file).entries().equals(List.of(second)),"only dismissed item removed");}finally{Files.deleteIfExists(file);Files.deleteIfExists(dir);}
  var previous=HeadlineCatalog.forTurn(24,0);Set<String> distinct=new HashSet<>();for(int turn=0;turn<12;turn++){var now=HeadlineCatalog.forTurn(24,turn);check(now.size()==4,"four ambient stories");check(now.equals(HeadlineCatalog.forTurn(24,turn)),"no refresh reroll");if(turn>0)check(!now.equals(previous),"turn refresh");distinct.addAll(now);previous=now;}check(distinct.size()==48,"full authored bank");
  System.out.println("PASS: 10,000 fresh seeds; dev password; persistent explicit dismissal; 48 stable rotating headlines.");
 }
}
