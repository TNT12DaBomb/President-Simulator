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
        if(engine==null||busy)return false;
        CareerView.Phase previous=view().phase();CareerReport report=engine.submit(command); messages=report.messages();last=clock.getAsLong();changed.run();
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
        if(!key.equals(decisionKey()))return;
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
        if(!key.equals(decisionKey())||elapsed>=remaining){messages=report.messages();last=now;changed.run();}
        else timerChanged.run();
    }
    public void save(Path path,Runnable done) {
        if(engine==null||busy)return;busy=true;changed.run();CareerEngine snapshot=engine;
        new SwingWorker<Void,Void>() {
            protected Void doInBackground() throws Exception {CareerSave.write(snapshot,path,"Desktop career");return null;}
            protected void done(){busy=false;last=clock.getAsLong();try{get();var lines=new java.util.ArrayList<>(messages);lines.add("Saved to "+path.getFileName());messages=List.copyOf(lines);changed.run();done.run();}
                catch(Exception e){messages=List.of("Save failed: "+cause(e));changed.run();}}
        }.execute();
    }
    public void load(Path path) {
        if(busy)return;busy=true;changed.run();
        new SwingWorker<CareerEngine,Void>() {
            protected CareerEngine doInBackground() throws Exception{return CareerSave.read(path);}
            protected void done(){busy=false;last=clock.getAsLong();try{engine=get();messages=List.of("Loaded "+path.getFileName());}
                catch(Exception e){messages=List.of("Load failed; your current career is unchanged. "+cause(e));}changed.run();}
        }.execute();
    }
    private static String cause(Exception e){return e.getCause()==null?e.getMessage():e.getCause().getMessage();}
}
