import java.util.*;
/** Headlines summarize actual recorded game events; viewing news consumes no random draws. */
public final class NewsFeed {
 private NewsFeed(){}
 public static List<String> headlines(CareerView v){
  var out=new LinkedHashSet<String>();
  if(v.phase()==CareerView.Phase.CAMPAIGN&&v.campaign().pendingEvent()!=null)out.add(v.campaign().pendingEvent().title());
  if(v.phase()==CareerView.Phase.PRESIDENCY&&v.world().pending()!=null)out.add(v.world().pending().title());
  var history=v.phase()==CareerView.Phase.CAMPAIGN?v.campaign().history():v.history();
  for(int i=history.size()-1;i>=0&&out.size()<3;i--){String line=history.get(i);
   if(line.startsWith("NEWS ["))out.add(line.substring(line.indexOf("]: ")+3));
   else if(line.startsWith("FACT CHECK:"))out.add(line.contains("Your original answer was inaccurate")||line.matches("(?s).*Public credibility -[0-9].*")?"Debate fact-check raises questions about candidate credibility":"Debate fact-check confirms candidate’s knowledge");
   else if(line.startsWith("ELECTION ")||line.startsWith("INAUGURATION:")||line.startsWith("EVENT:")||line.startsWith("MIDTERM"))out.add(line);
  }
  if(out.isEmpty())out.add(v.phase()==CareerView.Phase.CAMPAIGN?"Campaign opens — candidates prepare to meet voters":"Administration begins work — awaiting the next development");
  var result=new ArrayList<String>();result.add(out.iterator().next());long turn=v.phase()==CareerView.Phase.CAMPAIGN?v.campaign().turnsUsed():v.year()*12L+v.termMonths();var ambient=HeadlineCatalog.forTurn(v.campaign().seed(),turn);result.add(ambient.get(Math.floorMod(turn,2)==0?0:2));return List.copyOf(result);
 }
}
