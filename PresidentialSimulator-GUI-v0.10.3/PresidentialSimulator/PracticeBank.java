import java.util.List;
/** Fixed tutorial cards. They never enter either live bank. */
public final class PracticeBank {
 private PracticeBank(){}
 public static List<DebateFactChecks.Card> forMode(DebateSettings.Mode mode){return mode==DebateSettings.Mode.POLITICS?List.of(
  card("Practice: budget arithmetic","A fictional office has 50 tokens and spends 15. How many remain?","35","25","40","50 − 15 = 35."),
  card("Practice: reading a table","A report lists 6 completed tasks and 4 pending tasks. How many tasks are listed?","10","2","24","6 + 4 = 10."),
  card("Practice: checking a claim","A fictional memo is marked DRAFT. Does that label itself mean it is an enacted law?","No","Yes","Only on a Monday","The word DRAFT does not establish enactment.")
 ):List.of(
  card("Practice: number puzzle","What is 17 plus 8?","25","24","26","17 + 8 = 25."),
  card("Practice: word puzzle","How many letters are in the word debate?","Six","Five","Seven","d-e-b-a-t-e has six letters."),
  card("Practice: sequence","What comes next in 3, 6, 9, ... when adding three each time?","12","11","15","9 + 3 = 12.")
 );}
 private static DebateFactChecks.Card card(String t,String q,String a,String b,String c,String e){return new DebateFactChecks.Card(t,q,List.of(a,b,c),a,e,"Training example: "+e);}
}
