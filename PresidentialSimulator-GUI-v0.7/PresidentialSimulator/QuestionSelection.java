import java.util.*;
/** Seeded topic diversity and unseen-first selection; no voting or candidate evaluation. */
public final class QuestionSelection {
 private QuestionSelection(){}
 public static String topic(DebateFactChecks.Card c){
  if(c.source().contains("starwars.com"))return "Star Wars";
  if(c.source().contains("solar-system"))return "Space";
  if(c.source().contains("constitution-transcript"))return "Constitution";
  if(c.source().contains("bls.gov"))return "Consumer prices";
  if(c.source().contains("bea.gov"))return "Economic measurement";
  int split=c.title().indexOf(':');return split<0?c.title():c.title().substring(0,split);
 }
 public static List<DebateFactChecks.Card> select(List<DebateFactChecks.Card> bank,long seed,List<LiveDebate.Statement> memory){
  var shuffled=new ArrayList<>(bank);Collections.shuffle(shuffled,new Random(seed));
  Set<String> seen=new HashSet<>();for(var s:memory)seen.add(s.topic());
  shuffled.sort(Comparator.comparing(c->seen.contains(c.title())));
  var used=new HashSet<String>();var selected=new ArrayList<DebateFactChecks.Card>();
  for(var c:shuffled)if(used.add(topic(c))){selected.add(c);if(selected.size()==3)break;}
  if(selected.size()!=3)throw new IllegalArgumentException("Live bank requires at least three distinct topics");
  return List.copyOf(selected);
 }
}
