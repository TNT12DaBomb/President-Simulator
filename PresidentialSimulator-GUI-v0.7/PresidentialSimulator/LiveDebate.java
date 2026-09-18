import java.util.*;

/** UI-independent debate protocol. Factual feedback is sourced, not a political forecast. */
public final class LiveDebate implements PressureEvent {
    public enum Stage { PREP, ANSWER, CONFIDENCE, CHALLENGE, RECOVERY, FEEDBACK, POLICY, RECORD, CLOSING, COMPLETE }
    public record Statement(String debate,String topic,String words,String confidence) {}
    private final String topic;
    private final DebateSettings settings;
    private Policy.Approach promisedApproach;
    private final Policy.Issue policyIssue;
    private final Random random;
    private final List<DebateFactChecks.Card> cards;
    private final List<Statement> memory;
    private final List<String> transcript=new ArrayList<>();
    private final Set<String> prepared=new LinkedHashSet<>();
    private final String style;
    private Stage stage=Stage.PREP;
    private int round, revision, prep=6;
    private String answer="", confidence="", claim="", feedback="", position="";
    private boolean challengeTrue, withdrawn;
    private List<String> options=List.of();

    public LiveDebate(String topic,long seed,List<Statement> memory) {
        this(topic,seed,memory,DebateSettings.DEFAULT);
    }
    public LiveDebate(String topic,long seed,List<Statement> memory,DebateSettings settings) {
        this.settings=Objects.requireNonNull(settings);policyIssue=topic.equals("economy")?Policy.Issue.ECONOMY:topic.equals("security")?Policy.Issue.DEFENSE:Policy.Issue.EDUCATION;
        this.topic=Objects.requireNonNull(topic);this.memory=Objects.requireNonNull(memory);
        random=new Random((topic.equals("practice")?0:seed) ^ topic.hashCode() ^ 0xD3BA7EL);
        cards=topic.equals("practice")?PracticeBank.forMode(settings.mode()):QuestionSelection.select(DebateFactChecks.forMode(settings.mode()),seed ^ topic.hashCode(),memory);
        style=List.of("The Prosecutor","The Brawler","The Statesman").get(random.nextInt(3));
        transcript.add("DEBATE OPEN: "+topic+". Opponent: "+style+". Fictional exchanges; verify claims against the moderator's source.");
        refreshOptions();
    }
    public Stage stage(){return stage;}
    public boolean complete(){return stage==Stage.COMPLETE;}
    public List<String> transcript(){return List.copyOf(transcript);}
    public List<Statement> statements(){return List.copyOf(memory);}
    public int seconds(){return settings.seconds(stage);}
    public boolean factualCorrect(){return answer.equals(card().answer());}
    public String missed(){return seconds()==0?"No clock here. Continue when ready.":switch(stage){case CHALLENGE->"The opponent finishes, then your recovery window opens.";case ANSWER->"Silence is recorded; the moderator publishes the correction.";case CONFIDENCE->"Without a confidence choice, your answer is recorded as hedged.";default->"The moderator moves on; your silence stays in the transcript.";};}
    public CampaignEvent event(){
        if(complete())throw new IllegalStateException("Debate complete");
        String title=switch(stage){case PREP->"Debate preparation";case ANSWER->"Knowledge round"+" · "+(round+1)+"/3";case CONFIDENCE->"How certain are you?";case CHALLENGE->"Opponent speaking";case RECOVERY->"Your response to the challenge";case FEEDBACK->withdrawn?"CLAIM WITHDRAWN — source review":answer.equals("No answer")?"TIME EXPIRED — review the answer":factualCorrect()?"CORRECT — well answered!":"INCORRECT — learn the distinction";case POLICY->"Policy tradeoff · long answer";case RECORD->"Your words on the record";case CLOSING->"Post-debate debrief";default->"Debate";};
        String body=switch(stage){
            case PREP->"Question mode: "+settings.mode()+". Opponent: "+style+".\nPreparation budget: "+prep+" of 6 briefing slots left. Each review costs 2 slots. No cash or campaign turns are spent; slots reset next debate. Practice is free.\n"+(settings.pace()==DebateSettings.Pace.UNTIMED?"Untimed responses.":"Answer: "+settings.seconds(Stage.ANSWER)+"s · recovery: "+settings.seconds(Stage.RECOVERY)+"s. Menus pause clocks.")+"\nReviewed: "+(prepared.isEmpty()?"none":String.join(", ",prepared))+"."+briefingNotes();
            case ANSWER->"Topic: "+QuestionSelection.topic(card())+"\n"+card().prompt()+(prepared.contains(subject())?"\nStaff briefing: "+card().explanation():"");
            case CONFIDENCE->"You said: “"+answer+"”\nChoose how to present that answer. Admitting uncertainty withdraws the claim; confidence may invite a challenge.";
            case CHALLENGE->"Opponent: “"+(claim.equals(answer)?"I agree with that statement. ":"I dispute that. ")+claim+"”\nYou may interrupt, or let the speaker finish. A challenge is a claim, not an official correction.";
            case RECOVERY->"You: “"+answer+"”\nOpponent: “"+claim+"”\nDo you stand by your answer?";
            case FEEDBACK->feedback+"\nSource: "+card().source();
            case POLICY->topic.equals("practice")?"Training exercise: your team has one rehearsal room. Commit to a quiet individual session or a shared group session. This tutorial creates no campaign promise.":"Governing question: "+policyIssue+". Which approach will you publicly commit to? This is a policy choice, not a trivia answer. Your commitment will appear beside presidential legislation and decisions will be compared with it.";
            case RECORD->recordPrompt();
            case CLOSING->topic.equals("practice")?"Practice complete. Your answers and training choices stay in this rehearsal; nothing is copied to your career.":"Your transcript records answers, confidence, challenges and corrections. Read it in History.\n"+feedback+"\nYour policy statement: “"+position+"”\nThis debate protocol currently changes the conversation and statement record; your policy commitment will also be available at the presidential desk.";
            default->"";
        };
        return new CampaignEvent("debate_live_"+topic+"_"+revision,title,body,CampaignEvent.Category.OPPORTUNITY,List.of(),options.stream().map(x->new CampaignEvent.Option(x,List.of())).toList());
    }
    private DebateFactChecks.Card card(){return cards.get(round);}
    private String subject(){return category(card());}
    private String category(DebateFactChecks.Card c){String t=QuestionSelection.topic(c);return settings.mode()==DebateSettings.Mode.FUN?(Set.of("Star Wars","Literature","Music").contains(t)?firstSubject():secondSubject()):(Set.of("Economic measurement","Consumer prices","Data literacy").contains(t)?firstSubject():secondSubject());}
    private String firstSubject(){return settings.mode()==DebateSettings.Mode.FUN?"Arts and culture":"Economics and data";}
    private String secondSubject(){return settings.mode()==DebateSettings.Mode.FUN?"Science and puzzles":"Civics";}
    private String briefingNotes(){if(prepared.isEmpty())return "";String notes="";for(var c:cards)if(prepared.contains(category(c)))notes+="\nCoach: "+c.explanation();if(prepared.contains("Record"))notes+="\nYour record: "+(memory.isEmpty()?"No previous statements.":memory.get(memory.size()-1).words());return notes;}
    public static Optional<Policy.Bill> commitment(Statement s){try{if(!s.confidence().startsWith("POLICY:"))return Optional.empty();String[] fields=s.confidence().split(":");return Optional.of(new Policy.Bill(Policy.Issue.valueOf(fields[1]),Policy.Approach.valueOf(fields[2]),"Debate commitment"));}catch(IllegalArgumentException|ArrayIndexOutOfBoundsException ex){return Optional.empty();}}
    private String policyLabel(Policy.Approach approach){var p=PolicyCatalog.option(policyIssue,approach);return p.title()+" — "+p.tradeoff();}
    private String styleDescription(){return switch(style){case "The Prosecutor"->"Usually checks the source before challenging.";case "The Brawler"->"Challenges forcefully, including with unreliable corrections.";default->"Lets answers finish and asks for clarification.";};}
    private String recordPrompt(){
        Statement earlier=memory.stream().filter(s->s.topic().equals("Policy: "+policyIssue)).findFirst().orElse(null);
        return (earlier==null?"Tonight you said: “"+position+"”": "In the "+earlier.debate()+" debate you said: “"+earlier.words()+"”\nTonight: “"+position+"”")+"\nA moderator asks whether your commitment still stands. Explain a revision or reaffirm it; the record retains both statements.";
    }
    private void refreshOptions(){
        options=switch(stage){
            case PREP->{var choices=new ArrayList<String>();for(String subject:List.of(firstSubject(),secondSubject(),"Record"))if(prep>=2&&!prepared.contains(subject))choices.add("Review "+subject+" · costs 2 briefing slots");if(!prepared.isEmpty())choices.add("Read reviewed notes · free");choices.add("Begin debate");yield List.copyOf(choices);}
            case ANSWER->{var choices=new ArrayList<>(card().choices());Collections.shuffle(choices,random);yield List.copyOf(choices);}
            case CONFIDENCE->List.of("Answer confidently","Hedge: this is my understanding","Admit I don't know; withdraw the claim");
            case CHALLENGE->List.of("Interrupt: ask them to cite the source","Let the opponent finish");
            case RECOVERY->List.of("Double down on my original answer","Clarify: distinguish my claim from their correction","Concede and accept their correction","Counterattack: their correction is wrong too","Question their source before accepting it");
            case FEEDBACK->List.of("Continue");
            case POLICY->{if(topic.equals("practice"))yield List.of("Reserve quiet individual sessions","Reserve a shared group session","Wait for the team’s availability");yield List.of(policyLabel(Policy.Approach.EXPAND_PROGRAM),policyLabel(Policy.Approach.REORGANIZE_PROGRAM),"Make no commitment tonight");}
            case RECORD->List.of("Reaffirm tonight's commitment","Revise: publish new criteria and explain who is affected","Decline to clarify tonight");
            case CLOSING->List.of("Return to campaign");
            default->List.of();
        };
    }
    public void respond(int index){
        if(index<0||index>=options.size())throw new IllegalArgumentException("Choose a listed response");
        String choice=options.get(index);transcript.add(stage+": "+choice);
        switch(stage){
            case PREP->{if(choice.equals("Begin debate"))stage=Stage.ANSWER;else if(choice.startsWith("Review ")){String subject=choice.substring(7,choice.indexOf(" ·"));if(prep<2||!prepared.add(subject))throw new IllegalStateException("Review unavailable");prep-=2;}}
            case ANSWER->{withdrawn=false;answer=choice;stage=Stage.CONFIDENCE;}
            case CONFIDENCE->{confidence=choice;memory.add(new Statement(topic,card().title(),answer,confidence));if(index==2){withdrawn=true;feedback="You withdrew the claim instead of asserting a fact. "+correction();stage=Stage.FEEDBACK;}else if(answer.equals(card().answer())&&random.nextBoolean()){feedback="The moderator confirms your answer; no challenge follows. "+correction();stage=Stage.FEEDBACK;}else{challengeTrue=style.equals("The Prosecutor")||(!style.equals("The Brawler")&&random.nextBoolean());claim=challengeTrue?card().answer():card().choices().stream().filter(x->!x.equals(card().answer())&&!x.equals(answer)).findFirst().orElseThrow();if(claim.equals(answer)){feedback="The opponent agrees. "+correction();stage=Stage.FEEDBACK;}else stage=Stage.CHALLENGE;}}
            case CHALLENGE->{if(index==0)transcript.add("MODERATOR: One interruption; state your objection, then let the source settle it.");stage=Stage.RECOVERY;}
            case RECOVERY->{feedback=switch(index){case 0->"You stood by your original statement. ";case 1->"You requested a distinction between the two claims. ";case 2->challengeTrue?"You accepted an accurate correction. ":"You conceded to an inaccurate correction. ";case 3->challengeTrue?"Your counterattack disputed an accurate correction. ":"You correctly disputed the opponent's correction. ";default->"You requested verification before accepting the claim. ";};feedback+=(answer.equals(card().answer())?"Your original answer was accurate. ":"Your original answer was inaccurate. ")+(challengeTrue?"The opponent's correction was accurate. ":"The opponent's correction was inaccurate. ")+correction();stage=Stage.FEEDBACK;}
            case FEEDBACK->{transcript.add("FACT CHECK: "+feedback+" Source: "+card().source());if(++round<cards.size())stage=Stage.ANSWER;else stage=Stage.POLICY;}
            case POLICY->{position=choice;for(var a:Policy.Approach.values())if(choice.equals(policyLabel(a)))promisedApproach=a;memory.add(new Statement(topic,"Policy: "+policyIssue,position,promisedApproach==null?"No commitment":"POLICY:"+policyIssue+":"+promisedApproach));stage=Stage.RECORD;}
            case RECORD->{if(index==1)memory.add(new Statement(topic,"Policy: "+policyIssue,"Revision promised; original commitment remains public","Revision pending"));feedback=index==0?"You reaffirmed the commitment.":index==1?"Your promise to revise the criteria is on the record.":"The clarification request remains unanswered.";transcript.add("RECORD: "+feedback);stage=Stage.CLOSING;}
            case CLOSING->stage=Stage.COMPLETE;
            default->throw new IllegalStateException("Debate complete");
        }
        revision++;refreshOptions();
    }
    private String correction(){return "Verified answer: "+card().answer()+(card().answer().endsWith(".")?" ":". ")+card().explanation();}
    public void timeout(){
        if(seconds()==0)throw new IllegalStateException("No active deadline");
        transcript.add("TIMEOUT: "+stage);
        switch(stage){case CHALLENGE->stage=Stage.RECOVERY;case CONFIDENCE->{respond(1);return;}case ANSWER->{withdrawn=false;answer="No answer";memory.add(new Statement(topic,card().title(),answer,"Timed out"));feedback="No answer before the deadline. "+correction();stage=Stage.FEEDBACK;}case RECOVERY->{feedback="No recovery before the deadline. "+correction();stage=Stage.FEEDBACK;}case POLICY->{position="No commitment before the deadline";stage=Stage.RECORD;}case RECORD->{feedback="The moderator moved on without clarification.";stage=Stage.CLOSING;}default->throw new IllegalStateException();}
        revision++;refreshOptions();
    }
    public void leave(){transcript.add("DEBATE ENDED: You left the appearance. Unanswered questions are not invented.");stage=Stage.COMPLETE;revision++;options=List.of();}
}
