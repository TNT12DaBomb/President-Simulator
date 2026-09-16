import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.*;
/** Single daemon reader allows the UI thread to enforce response timeouts without blocking on Enter. */
public final class LineInput {
    public record Line(String value,boolean end){}
    private final BlockingQueue<Line> lines=new LinkedBlockingQueue<>();
    private boolean ended;
    public LineInput(InputStream input){Thread reader=new Thread(()->{
        try{Scanner scanner=new Scanner(input,StandardCharsets.UTF_8);while(scanner.hasNextLine())lines.add(new Line(scanner.nextLine(),false));}
        finally{lines.add(new Line("",true));}
    },"terminal-input");reader.setDaemon(true);reader.start();}
    public Line read(long timeoutMillis){
        if(ended)return new Line("",true);
        try{Line line=timeoutMillis<0?lines.take():lines.poll(timeoutMillis,TimeUnit.MILLISECONDS);if(line!=null&&line.end())ended=true;return line;}
        catch(InterruptedException ex){Thread.currentThread().interrupt();ended=true;return new Line("",true);}
    }
}
