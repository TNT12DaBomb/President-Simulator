import java.nio.file.Path;
import java.util.List;
import java.util.function.LongSupplier;
import javax.swing.SwingWorker;

/** UI adapter. All access occurs on the EDT; disk operations freeze commands, not painting. */
public final class DesktopController {
    private CareerEngine engine;
    private final LongSupplier clock;
    private long last;
    private int pauses;
    private boolean busy;
    private Path autosave;
    public void autosaveTo(Path path){autosave=path;}
    public boolean realtime(){return engine!=null&&engine.realtimeEnabled();}
    public record Notice(String title, String detail, boolean important) {}
    public java.util.function.Consumer<Notice> notified = n -> {};
    private void publishNotice(String title,String detail,boolean important){notified.accept(new Notice(title,detail,important));}
    public Runnable changed = () -> {};
    public Runnable timerChanged = () -> {};
    public List<String> messages = List.of("Welcome. Start a career or load a save.");
    public DesktopController() { this(System::nanoTime); }
    DesktopController(LongSupplier clock) { this.clock=clock; last=clock.getAsLong(); }
    public CareerView view() { return engine==null?null:engine.view(); }
    public boolean busy() { return busy; }
    public boolean paused() { return pauses>0 || busy; }
    public void pause() { pauses++; last=clock.getAsLong(); timerChanged.run(); }
    public void resume() { pauses=Math.max(0,pauses-1); last=clock.getAsLong(); timerChanged.run(); }
    public void start(long seed, President.Difficulty difficulty, President.RunningMate mate) {
        if(busy)return;
        engine=new CareerEngine(seed,difficulty,mate); messages=List.of("Your campaign begins. Visit States to choose where to work. Each campaign action uses one turn.");
        last=clock.getAsLong();changed.run();
    }
    public boolean submit(CareerCommand command) {
        if(engine==null)return false;if(busy){publishNotice("Please wait", "A save or load is still finishing. Try again when it completes.",false);return false;}
        CareerView.Phase previous=view().phase();String oldDecision=decisionKey();CareerReport report=engine.submit(command); messages=report.messages();last=clock.getAsLong();changed.run();
        if(!report.accepted())publishNotice("Action unavailable",String.join("\n",report.messages()),true);
        else if(!oldDecision.equals(decisionKey())&&!decisionKey().isEmpty())publishNotice("Decision required",view().phase()==CareerView.Phase.CAMPAIGN?view().campaign().pendingEvent().title():view().world().pending().title(),true);
        else if(previous!=view().phase())publishNotice("Career update",String.join("\n",report.messages()),true);
        else publishNotice("Action completed",String.join("\n",report.messages()),false);
        if(report.accepted()&&previous!=view().phase()&&autosave!=null)save(autosave,()->{});return report.accepted();
    }
    public String decisionKey() {
        var v=view();if(v==null)return "";
        if(v.phase()==CareerView.Phase.CAMPAIGN && v.campaign().pendingEvent()!=null) return "c:"+v.campaign().pendingEvent().id()+":"+v.campaign().pendingEvent().title();
        if(v.phase()==CareerView.Phase.PRESIDENCY && v.world().pending()!=null)return "w:"+v.world().pending().id()+":"+v.world().dueMonth();
        return "";
    }
    public int seconds() {
        var v=view();if(v==null)return 0;
        if(v.phase()==CareerView.Phase.CAMPAIGN && v.campaign().pendingEvent()!=null)return v.campaign().pendingEvent().secondsRemaining();
        if(v.phase()==CareerView.Phase.PRESIDENCY)return v.world().secondsRemaining();return 0;
    }
    public void respond(String key,int choice) {
        pulse(); // A queued click cannot answer a question whose deadline already elapsed.
        if(!key.equals(decisionKey())){publishNotice("Response window changed","That response window has ended. Review the current decision.",true);return;}
        submit(view().phase()==CareerView.Phase.CAMPAIGN?CareerCommand.campaign(GameCommand.respond(choice)):
            CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.EVENT_CHOICE,choice)));
    }
    public void pulse() {
        long now=clock.getAsLong();
        if(engine==null||paused()||seconds()==0||!engine.realtimeEnabled()){last=now;timerChanged.run();return;}
        int elapsed=(int)Math.min(3600,(now-last)/1_000_000_000L);if(elapsed<1)return;
        last+=elapsed*1_000_000_000L;String key=decisionKey();int remaining=seconds();
        CareerReport report=engine.submit(view().phase()==CareerView.Phase.CAMPAIGN?
            CareerCommand.campaign(GameCommand.tick(Math.min(elapsed,remaining))):
            CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.CLOCK_TICK,Math.min(elapsed,remaining))));
        if(!key.equals(decisionKey())||elapsed>=remaining){messages=report.messages();last=now;changed.run();publishNotice("Response deadline reached",String.join("\n",report.messages()),true);}
        else timerChanged.run();
    }
    public void save(Path path,Runnable done) {
        if(engine==null||busy)return;busy=true;changed.run();CareerEngine snapshot=engine;
        new SwingWorker<Void,Void>() {
            protected Void doInBackground() throws Exception {CareerSave.write(snapshot,path,"Desktop career");return null;}
            protected void done(){busy=false;last=clock.getAsLong();try{get();var lines=new java.util.ArrayList<>(messages);lines.add("Saved to "+path.getFileName());messages=List.copyOf(lines);changed.run();publishNotice("Saved", "Your career was saved to "+path.getFileName()+".",false);done.run();}
                catch(Exception e){messages=List.of("Save failed: "+cause(e));changed.run();publishNotice("Save failed",messages.get(0),true);}}
        }.execute();
    }
    public void load(Path path) {
        if(busy)return;busy=true;changed.run();
        new SwingWorker<CareerEngine,Void>() {
            protected CareerEngine doInBackground() throws Exception{return CareerSave.read(path);}
            protected void done(){busy=false;last=clock.getAsLong();try{engine=get();messages=List.of("Loaded "+path.getFileName());publishNotice("Career loaded",messages.get(0),false);}
                catch(Exception e){messages=List.of("Load failed; your current career is unchanged. "+cause(e));publishNotice("Load failed",messages.get(0),true);}changed.run();}
        }.execute();
    }
    private static String cause(Exception e){return e.getCause()==null?e.getMessage():e.getCause().getMessage();}
}
