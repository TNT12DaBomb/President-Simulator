import java.util.ArrayList;
import java.util.List;

/** Fictional named appointments and delegated work. No renderer or persistence dependencies. */
public final class Cabinet {
    private final GameRules rules;
    public Cabinet() { this(GameRules.CURRENT); }
    public Cabinet(GameRules rules) { this.rules = rules; }
    public static String departmentLabel(Department d, GameRules rules) {
        return switch(d) {
            case CHIEF_OF_STAFF -> "White House Chief of Staff";
            case STATE -> "Secretary of State";
            case DEFENSE -> "Secretary of Defense";
            case TREASURY -> "Secretary of the Treasury";
            case JUSTICE -> "Attorney General";
            case PUBLIC_SERVICES -> rules.current() ? "Secretary of Health and Human Services" : "Public Services (legacy fictional office)";
        };
    }
    public enum Department { CHIEF_OF_STAFF, STATE, DEFENSE, TREASURY, JUSTICE, PUBLIC_SERVICES }
    public record Candidate(int id, String name, Department department, String background, boolean consensus,
                            int deliveryMonths, int projectCost) { }
    public record Job(String title, int dueMonth, int assignedYear) { }
    public record Seat(Department department, Candidate official, Candidate nominee, int hearingMonth,
                       boolean agreement, Job job, int lastAssignedYear) { }
    public record Entry(int month, Department department, String message) { }
    public record View(List<Seat> seats, List<Entry> history) {
        public View { seats=List.copyOf(seats);history=List.copyOf(history); }
    }
    public record Delivery(Department department, String message, CareerCommand.GovernanceAction work) { }
    private final Candidate[] officials=new Candidate[6], nominees=new Candidate[6];
    private final int[] hearing=new int[6], lastYear={-1,-1,-1,-1,-1,-1};
    private final boolean[] agreement=new boolean[6];
    private final Job[] jobs=new Job[6];
    private final List<Entry> history=new ArrayList<>();
    public static List<Candidate> candidates() {
        String[] names={"Alex Morgan","Jordan Ellis","Maya Chen","Rafael Ortiz","Morgan Reed","Samira Hale",
            "Elena Brooks","Adrian Wells","Nora Patel","Casey Bennett","Leah Moreno","Devon Carter"};
        List<Candidate> list=new ArrayList<>();
        for(int i=0;i<12;i++)list.add(new Candidate(i,names[i],Department.values()[i/2],
            i%2==0?"Technical specialist; rapid reports; requires a majority or negotiated confirmation agreement."
                     :"Coalition administrator; slower reports; existing cross-caucus confirmation support.",i%2==1,i%2==0?1:2,i%2==0?80:40));
        return List.copyOf(list);
    }
    public View view() {
        List<Seat> seats=new ArrayList<>();
        for(int i=0;i<6;i++)seats.add(new Seat(Department.values()[i],officials[i],nominees[i],hearing[i],agreement[i],jobs[i],lastYear[i]));
        return new View(seats,history);
    }
    public String problem(OfficeCommand.Type type,int choice,int months,int senateSeats) {
        int d=type==OfficeCommand.Type.NOMINATE?choice/2:choice;
        return switch(type) {
            case NOMINATE -> officials[d]!=null?"Dismiss the current official before choosing a replacement.":nominees[d]!=null?"Withdraw the existing nomination first.":"";
            case CONFIRM -> nominees[d]==null?"No nominee awaits confirmation.":months<hearing[d]?"The hearing completes after the next month closes.":
                senateSeats<(rules.current()?50:51)&&!nominees[d].consensus()&&!agreement[d]?"Secure confirmation support or choose a consensus nominee.":"";
            case NOMINEE_SUPPORT -> nominees[d]==null?"Nominate someone first.":agreement[d]?"Confirmation support is already secured.":"";
            case DISMISS -> officials[d]==null&&nominees[d]==null?"This position is already vacant.":"";
            case DELEGATE -> officials[d]==null?"A confirmed official is required for delegation.":jobs[d]!=null?"This official already has an assignment.":lastYear[d]==months/12?"This department's annual assignment has already been used.":"";
            default -> "Unknown cabinet command.";
        };
    }
    public int projectCost(int department) { return officials[department].projectCost(); }
    public String execute(OfficeCommand.Type type,int choice,int months) {
        int d=type==OfficeCommand.Type.NOMINATE?choice/2:choice;String message;
        switch(type) {
            case NOMINATE -> {
                Candidate c=candidates().get(choice);
                if(d==0){officials[d]=c;message=c.name()+" appointed Chief of Staff; no Senate vote required.";}
                else {nominees[d]=c;hearing[d]=months+1;agreement[d]=false;message=c.name()+" nominated; hearing due after month "+hearing[d]+" closes.";}
            }
            case CONFIRM -> {officials[d]=nominees[d];nominees[d]=null;agreement[d]=false;message=officials[d].name()+" confirmed.";}
            case NOMINEE_SUPPORT -> {agreement[d]=true;message="Confirmation agreement secured for "+nominees[d].name()+" only. It does not cover legislation.";}
            case DISMISS -> {
                message=(officials[d]!=null?officials[d].name():nominees[d].name())+" removed from this appointment process.";
                if(jobs[d]!=null)message+=" Assignment cancelled; costs and annual assignment capacity are not refunded.";
                officials[d]=null;nominees[d]=null;jobs[d]=null;agreement[d]=false;
            }
            case DELEGATE -> {
                Candidate c=officials[d];lastYear[d]=months/12;
                jobs[d]=new Job(project(Department.values()[d]),Math.min(48,months+c.deliveryMonths()),lastYear[d]);
                message=c.name()+" assigned "+jobs[d].title()+"; due at end of month "+jobs[d].dueMonth()+". Cost $"+c.projectCost()+".";
            }
            default -> throw new IllegalArgumentException("Unknown cabinet command");
        }
        history.add(new Entry(months+1,Department.values()[d],message));return message;
    }
    public List<Delivery> advanceMonth(int month) {
        List<Delivery> result=new ArrayList<>();
        for(int d=0;d<6;d++)if(jobs[d]!=null&&jobs[d].dueMonth()<=month){
            Department department=Department.values()[d];String message=officials[d].name()+" delivered "+jobs[d].title()+".";
            result.add(new Delivery(department,message,work(department)));history.add(new Entry(month,department,message));jobs[d]=null;
        }
        return List.copyOf(result);
    }
    public static String project(Department d) { return switch(d){
        case CHIEF_OF_STAFF -> "annual cabinet coordination plan";case STATE -> "public diplomatic briefing";
        case DEFENSE -> "preparedness review";case TREASURY -> "annual accounts reconciliation";
        case JUSTICE -> "complaint handling review";case PUBLIC_SERVICES -> "service delivery review";}; }
    private static CareerCommand.GovernanceAction work(Department d) { return switch(d){
        case CHIEF_OF_STAFF -> CareerCommand.GovernanceAction.CABINET_MEETING;case STATE -> CareerCommand.GovernanceAction.PUBLIC_BRIEFING;
        case DEFENSE -> CareerCommand.GovernanceAction.MANAGE_CRISIS;case TREASURY -> CareerCommand.GovernanceAction.BUDGET_REVIEW;
        case JUSTICE, PUBLIC_SERVICES -> CareerCommand.GovernanceAction.SERVICE_REVIEW;}; }
}
