import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Full-text wrapping and pagination; rendering never advances the game clock. */
public final class TerminalScreen {
    public record Options(int width,int height,boolean refresh,boolean color){
        public Options{if(width<60||width>180||height<20||height>70)throw new IllegalArgumentException("Use width 60–180 and height 20–70.");}
    }
    public record Frame(String heading,String status,String body){}
    private final PrintStream sink;
    private final ByteArrayOutputStream buffer=new ByteArrayOutputStream();
    private final PrintStream content=new PrintStream(buffer,true,StandardCharsets.UTF_8);
    private Options options;
    private String heading="",status="",prompt="";
    private boolean started,statisticsAvailable;
    private int seconds,footerRow;
    private List<String> painted=List.of();
    private boolean invalidate=true;
    public TerminalScreen(PrintStream sink,Options options){this.sink=sink;this.options=options;}
    public Frame snapshot(){return new Frame(heading,status,buffer.toString(StandardCharsets.UTF_8));}
    public void restore(Frame f){heading=f.heading();status=f.status();buffer.reset();buffer.writeBytes(f.body().getBytes(StandardCharsets.UTF_8));}
    public void statsAvailable(boolean value){statisticsAvailable=value;}
    public void timer(int value){seconds=Math.max(0,value);}
    private String timerLabel(){return seconds>0?String.format(java.util.Locale.ROOT,"Time left: %2ds",seconds):"";}
    public void countdown(int value){
        if(value==seconds)return;seconds=value;
        if(options.refresh())sink.print("\0337\033["+footerRow+";"+(options.width()-19)+"H"+String.format(java.util.Locale.ROOT,"%-18s",timerLabel())+"\0338");
        // Plain mode is a transcript: never append a line for each clock tick.
        sink.flush();
    }
    public PrintStream content(){return content;}
    public Options options(){return options;}
    public void options(Options value){options=value;invalidate=true;}
    public void begin(String heading,String status){buffer.reset();this.heading=heading;this.status=status;prompt="";}
    public void start(){if(options.refresh()){sink.print("\033[?1049h\033[?7l");sink.flush();started=true;}}
    public void close(){if(started){sink.print("\033[0m\033[?7h\033[?25h\033[?1049l");sink.flush();started=false;}}
    public void notification(String message){sink.println(message);sink.flush();}
    private List<String> wrap(String source){
        List<String> result=new ArrayList<>();int width=options.width()-4;
        source=source.replaceAll("\\x1B\\[[0-?]*[ -/]*[@-~]","");
        for(String raw:source.split("\\R",-1)){
            String line=raw.replace("\t","    ").replaceAll("[\\p{Cntrl}]","");
            while(line.length()>width){int cut=line.lastIndexOf(' ',width);if(cut<width/3)cut=width;result.add(line.substring(0,cut));line=line.substring(cut).stripLeading();}
            result.add(line);
        }
        return result;
    }
    public List<String> lines(){var result=wrap(buffer.toString(StandardCharsets.UTF_8));while(!result.isEmpty()&&result.get(result.size()-1).isBlank())result.remove(result.size()-1);return List.copyOf(result);}
    private int promptRows(){return wrap(prompt+" > ").size();}
    private int bodyRows(){return Math.max(1,options.height()-6-wrap(heading).size()-wrap(status).size()-(promptRows()-1));}
    public int pages(){return Math.max(1,(lines().size()+bodyRows()-1)/bodyRows());}
    public void render(int page,String prompt){
        this.prompt=prompt;
        var lines=lines();int pages=pages();page=Math.max(0,Math.min(page,pages-1));
        if(options.refresh()){renderFixed(page,lines,pages);return;}
        sink.println();
        styled("PRESIDENTIAL SIMULATOR | PLAYTEST","1;36");
        for(String line:wrap(heading))styled(line,"1;33");
        wrap(status).forEach(sink::println);
        String rule="─".repeat(options.width()-2);sink.println(rule);
        for(int i=0;i<bodyRows();i++){int n=page*bodyRows()+i;sink.println(n<lines.size()?lines.get(n):"");}
        sink.println(rule);footerRow=options.height()-2-(promptRows()-1);
        String navigation="Page "+(page+1)+"/"+pages+" >/< ? Help"+(statisticsAvailable?" ST Stats":"");
        sink.println(String.format(java.util.Locale.ROOT,"%-"+(options.width()-20)+"s%s",navigation,timerLabel()));
        var prompts=wrap(prompt+" > ");for(int i=0;i<prompts.size()-1;i++)sink.println(prompts.get(i));sink.print(prompts.get(prompts.size()-1));
        if(options.refresh())sink.print("\033[?25h");sink.flush();
    }
    private String styledText(String text,String style){return options.color()?"\033["+style+"m"+text+"\033[0m":text;}
    private void renderFixed(int page,List<String> lines,int pages){
        var frame=new ArrayList<String>();
        frame.add(styledText("PRESIDENTIAL SIMULATOR | PLAYTEST","1;36"));
        for(String line:wrap(heading))frame.add(styledText(line,"1;33"));
        frame.addAll(wrap(status));String rule="─".repeat(options.width()-2);frame.add(rule);
        for(int i=0;i<bodyRows();i++){int n=page*bodyRows()+i;frame.add(n<lines.size()?lines.get(n):"");}
        frame.add(rule);footerRow=frame.size()+1;
        String navigation="Page "+(page+1)+"/"+pages+" >/< ? Help"+(statisticsAvailable?" ST Stats":"");
        frame.add(String.format(java.util.Locale.ROOT,"%-"+(options.width()-20)+"s%s",navigation,timerLabel()));
        frame.addAll(wrap(prompt+" > "));
        var update=new StringBuilder("\033[?25l");
        if(invalidate){update.append("\033[2J");painted=List.of();invalidate=false;}
        int rows=Math.min(options.height()-1,Math.max(frame.size(),painted.size()));
        for(int i=0;i<rows;i++){
            String line=i<frame.size()?frame.get(i):"";
            // Clear the input row even when the prompt has not changed: it contains local echo.
            if(i==frame.size()-1||i>=painted.size()||!line.equals(painted.get(i)))
                update.append("\033[").append(i+1).append(";1H\033[2K").append(line);
        }
        int inputRow=Math.min(frame.size(),options.height()-1);
        int inputColumn=frame.get(frame.size()-1).length()+1;
        update.append("\033[").append(inputRow).append(';').append(inputColumn).append("H\033[?25h");
        sink.print(update);sink.flush();painted=List.copyOf(frame);
    }
    private void styled(String s,String style){if(options.color())sink.print("\033["+style+"m");sink.print(s);if(options.color())sink.print("\033[0m");sink.println();}
}
