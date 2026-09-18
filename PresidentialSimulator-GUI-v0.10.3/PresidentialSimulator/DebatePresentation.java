import java.util.*;
/** Stable display ordering only. Commands still carry the original option index. */
public final class DebatePresentation {
    private DebatePresentation(){}
    public static List<Integer> order(long seed,String id,int count){
        List<Integer> order=new ArrayList<>();for(int i=0;i<count;i++)order.add(i);
        if(DebateSession.handles(id)){
            long mixed=seed ^ ((long)id.hashCode()*0x9e3779b97f4a7c15L);
            mixed=(mixed^(mixed>>>30))*0xbf58476d1ce4e5b9L;
            Collections.shuffle(order,new Random(mixed^(mixed>>>27)));
        }
        return List.copyOf(order);
    }
}
