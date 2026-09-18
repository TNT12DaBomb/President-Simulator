import java.util.*;

/** UI-independent debate protocol. Factual feedback is sourced, not a political forecast. */
public final class LiveDebate implements PressureEvent {
    public enum Stage { PREP, ANSWER, CONFIDENCE, CHALLENGE, RECOVERY, FEEDBACK, POLICY, RECORD, CLOSING, COMPLETE }
    public record Statement(String debate,String topic,String words,String confidence) {}
    private final String topic;
    private final Random random;
    private final List<DebateFactChecks.Card> cards;
    private final List<Statement> memory;
    private final List<String> transcript=new ArrayList<>();
    private final Set<String> prepared=new LinkedHashSet<>();
    private final String style;
    private Stage stage=Stage.PREP;
    private int round, revision, prep=6;
    private String answer="", confidence="", claim="", feedback="", position="";
    private boolean challengeTrue;
    private List<String> options=List.of();

    public LiveDebate(String topic,long seed,List<Statement> memory) {
        this.topic=Objects.requireNonNull(topic);this.memory=Objects.requireNonNull(memory);
        random=new Random(seed ^ topic.hashCode() ^ 0xD3BA7EL);
        var all=new ArrayList<>(DebateFactChecks.all());Collections.shuffle(all,random);all.sort(Comparator.comparing(c->memory.stream().anyMatch(m->m.topic().equals(c.title()))));cards=List.copyOf(all.subList(0,3));
        style=List.of("The Prosecutor","The Brawler","The Statesman").get(random.nextInt(3));
        transcript.add("DEBATE OPEN: "+topic+". Opponent: "+style+". Fictional exchanges; verify claims against the moderator's source.");
        refreshOptions();
    }
    public Stage stage(){return stage;}
    public boolean complete(){return stage==Stage.COMPLETE;}
    public List<String> transcript(){return List.copyOf(transcript);}
    public List<Statement> statements(){return List.copyOf(memory);}
    public int seconds(){return switch(stage){case ANSWER->round==2?8:12;case CONFIDENCE->10;case CHALLENGE->3;case RECOVERY->6;case POLICY,RECORD->25;default->0;};}
    public String missed(){return seconds()==0?"No clock here. Continue when ready.":switch(stage){case CHALLENGE->"The opponent finishes, then you have six seconds to respond.";case ANSWER->"Silence is recorded; the moderator publishes the correction.";case CONFIDENCE->"Without a confidence choice, your answer is recorded as hedged.";default->"The moderator moves on; your silence stays in the transcript.";};}
    public CampaignEvent event(){
        if(complete())throw new IllegalStateException("Debate complete");
        String title=switch(stage){case PREP->"Debate preparation";case ANSWER->(round==2?"Rapid fire":"Factual round")+" · "+(round+1)+"/3";case CONFIDENCE->"How certain are you?";case CHALLENGE->"Opponent speaking";case RECOVERY->"Your response to the challenge";case FEEDBACK->"Moderator's fact check";case POLICY->"Policy tradeoff · long answer";case RECORD->"Your words on the record";case CLOSING->"Post-debate debrief";default->"Debate";};
        String body=switch(stage){
            case PREP->"Opponent: "+style+". "+styleDescription()+"\nSix briefing slots; each topic costs two. Prepared rounds reveal a source note. Live answers last 8–12 seconds, recovery six. Menus pause the clock.\nBriefing slots left: "+prep+". Reviewed: "+(prepared.isEmpty()?"none":String.join(", ",prepared))+"."+(prepared.contains("Record")?"\nRecord briefing: "+(memory.isEmpty()?"No previous statements.":memory.get(memory.size()-1).words()):"");
            case ANSWER->card().prompt()+(prepared.contains(subject())?"\nStaff briefing: "+card().explanation():"");
            case CONFIDENCE->"You said: “"+answer+"”\nChoose how to present that answer. Admitting uncertainty withdraws the claim; confidence may invite a challenge.";
            case CHALLENGE->"Opponent: “"+(claim.equals(answer)?"I agree with that statement. ":"I dispute that. ")+claim+"”\nYou may interrupt, or let the speaker finish. A challenge is a claim, not an official correction.";
            case RECOVERY->"You: “"+answer+"”\nOpponent: “"+claim+"”\nDo you stand by your answer?";
            case FEEDBACK->feedback+"\nSource: "+card().source();
            case POLICY->"A fictional grant program cannot fund every applicant this year. What will you promise? Each approach leaves someone waiting. Your statement can be quoted in a later debate.";
            case RECORD->recordPrompt();
            case CLOSING->"Your transcript records answers, confidence, challenges and corrections. Read it in History.\n"+feedback+"\nYour policy statement: “"+position+"”\nThis debate protocol currently changes the conversation and statement record; electoral-response balancing is still on the backlog.";
            default->"";
        };
        return new CampaignEvent("debate_live_"+topic+"_"+revision,title,body,CampaignEvent.Category.OPPORTUNITY,List.of(),options.stream().map(x->new CampaignEvent.Option(x,List.of())).toList());
    }
    private DebateFactChecks.Card card(){return cards.get(round);}
    private String subject(){return card().source().contains("bls.gov")?"Economy":"Constitution";}
    private String styleDescription(){return switch(style){case "The Prosecutor"->"Usually checks the source before challenging.";case "The Brawler"->"Challenges forcefully, including with unreliable corrections.";default->"Lets answers finish and asks for clarification.";};}
    private String recordPrompt(){
        Statement earlier=memory.stream().filter(s->s.topic().equals("Grant priorities")).findFirst().orElse(null);
        return (earlier==null?"Tonight you said: “"+position+"”": "In the "+earlier.debate()+" debate you said: “"+earlier.words()+"”\nTonight: “"+position+"”")+"\nA moderator asks whether your commitment still stands. Explain a revision or reaffirm it; the record retains both statements.";
    }
    private void refreshOptions(){
        options=switch(stage){
            case PREP->{var choices=new ArrayList<String>();choices.add(prep>=2&&!prepared.contains("Economy")?"Review economy briefing · 2 slots":"Review your briefing notes");choices.add("Begin debate");if(prep>=2&&!prepared.contains("Constitution"))choices.add("Review constitutional briefing · 2 slots");if(prep>=2&&!prepared.contains("Record"))choices.add("Review your previous statements · 2 slots");yield List.copyOf(choices);}
            case ANSWER->{var choices=new ArrayList<>(card().choices());Collections.shuffle(choices,random);yield List.copyOf(choices);}
            case CONFIDENCE->List.of("Answer confidently","Hedge: this is my understanding","Admit I don't know; withdraw the claim");
            case CHALLENGE->List.of("Interrupt: ask them to cite the source","Let the opponent finish");
            case RECOVERY->List.of("Double down on my original answer","Clarify: distinguish my claim from their correction","Concede and accept their correction","Counterattack: their correction is wrong too","Question their source before accepting it");
            case FEEDBACK->List.of("Continue","Move to the next question");
            case POLICY->{var choices=new ArrayList<>(List.of("Prioritize the most urgent needs; other eligible applicants will wait.","Fund applicants in filing order; urgent late applications may wait.","Spread smaller awards across applicants; some projects may remain unfinished."));Collections.shuffle(choices,random);yield List.copyOf(choices);}
            case RECORD->List.of("Reaffirm tonight's commitment","Revise: publish new criteria and explain who is affected","Decline to clarify tonight");
            case CLOSING->List.of("Return to campaign","Finish debate");
            default->List.of();
        };
    }
    public void respond(int index){
        if(index<0||index>=options.size())throw new IllegalArgumentException("Choose a listed response");
        String choice=options.get(index);transcript.add(stage+": "+choice);
        switch(stage){
            case PREP->{if(index==1)stage=Stage.ANSWER;else if(choice.startsWith("Review economy")||choice.startsWith("Review constitutional")||choice.startsWith("Review your previous")){String s=choice.contains("economy")?"Economy":choice.contains("constitutional")?"Constitution":"Record";prepared.add(s);prep-=2;if(s.equals("Record"))transcript.add("BRIEFING: "+(memory.isEmpty()?"No earlier statements.":memory.toString()));}}
            case ANSWER->{answer=choice;stage=Stage.CONFIDENCE;}
            case CONFIDENCE->{confidence=choice;memory.add(new Statement(topic,card().title(),answer,confidence));if(index==2){feedback="You withdrew the claim instead of asserting a fact. "+correction();stage=Stage.FEEDBACK;}else{challengeTrue=style.equals("The Prosecutor")||(!style.equals("The Brawler")&&random.nextBoolean());claim=challengeTrue?card().answer():card().choices().stream().filter(x->!x.equals(card().answer())).findFirst().orElseThrow();stage=Stage.CHALLENGE;}}
            case CHALLENGE->{if(index==0)transcript.add("MODERATOR: One interruption; state your objection, then let the source settle it.");stage=Stage.RECOVERY;}
            case RECOVERY->{feedback=switch(index){case 0->"You stood by your original statement. ";case 1->"You requested a distinction between the two claims. ";case 2->challengeTrue?"You accepted an accurate correction. ":"You conceded to an inaccurate correction. ";case 3->challengeTrue?"Your counterattack disputed an accurate correction. ":"You correctly disputed the opponent's correction. ";default->"You requested verification before accepting the claim. ";};feedback+=(answer.equals(card().answer())?"Your original answer was accurate. ":"Your original answer was inaccurate. ")+(challengeTrue?"The opponent's correction was accurate. ":"The opponent's correction was inaccurate. ")+correction();stage=Stage.FEEDBACK;}
            case FEEDBACK->{transcript.add("FACT CHECK: "+feedback+" Source: "+card().source());if(++round<cards.size())stage=Stage.ANSWER;else stage=Stage.POLICY;}
            case POLICY->{position=choice;stage=Stage.RECORD;}
            case RECORD->{memory.add(new Statement(topic,"Grant priorities",position,"Public commitment"));if(index==1)memory.add(new Statement(topic,"Grant priorities","Revise criteria and explain who is affected","Revision promised"));feedback=index==0?"You reaffirmed the commitment.":index==1?"Your promise to revise the criteria is on the record.":"The clarification request remains unanswered.";transcript.add("RECORD: "+feedback);stage=Stage.CLOSING;}
            case CLOSING->stage=Stage.COMPLETE;
            default->throw new IllegalStateException("Debate complete");
        }
        revision++;refreshOptions();
    }
    private String correction(){return "Verified answer: "+card().answer()+" "+card().explanation();}
    public void timeout(){
        if(seconds()==0)throw new IllegalStateException("No active deadline");
        transcript.add("TIMEOUT: "+stage);
        switch(stage){case CHALLENGE->stage=Stage.RECOVERY;case CONFIDENCE->{respond(1);return;}case ANSWER->{answer="No answer";memory.add(new Statement(topic,card().title(),answer,"Timed out"));feedback="No answer before the deadline. "+correction();stage=Stage.FEEDBACK;}case RECOVERY->{feedback="No recovery before the deadline. "+correction();stage=Stage.FEEDBACK;}case POLICY->{position="No commitment before the deadline";stage=Stage.RECORD;}case RECORD->{memory.add(new Statement(topic,"Grant priorities",position,"No clarification"));feedback="The moderator moved on without clarification.";stage=Stage.CLOSING;}default->throw new IllegalStateException();}
        revision++;refreshOptions();
    }
    public void leave(){transcript.add("DEBATE ENDED: You left the appearance. Unanswered questions are not invented.");stage=Stage.COMPLETE;revision++;options=List.of();}
}
