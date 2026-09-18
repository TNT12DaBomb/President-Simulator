import java.nio.file.*;
import java.io.*;
import java.util.*;
/** Separate UI inbox; simulation saves and news selection do not control dismissal. */
public final class ActivityInbox {
 private final Path path;
 private final List<DesktopController.Notice> entries=new ArrayList<>();
 public ActivityInbox(Path path)throws IOException{this.path=path;if(path!=null&&Files.exists(path)){Properties p=new Properties();try(var in=Files.newInputStream(path)){p.load(in);}try{int n=Integer.parseInt(p.getProperty("count","0"));for(int i=0;i<n;i++)entries.add(new DesktopController.Notice(p.getProperty(i+".title","Activity"),p.getProperty(i+".detail",""),Boolean.parseBoolean(p.getProperty(i+".important","false"))));}catch(NumberFormatException e){throw new IOException("Activity file could not be read",e);}}}
 public List<DesktopController.Notice> entries(){return List.copyOf(entries);}
 public void add(DesktopController.Notice entry)throws IOException{entries.add(entry);save();}
 public void dismiss(int index)throws IOException{var removed=entries.remove(index);try{save();}catch(IOException e){entries.add(index,removed);throw e;}}
 private void save()throws IOException{if(path==null)return;Files.createDirectories(path.toAbsolutePath().getParent());Properties p=new Properties();p.setProperty("count",Integer.toString(entries.size()));for(int i=0;i<entries.size();i++){var e=entries.get(i);p.setProperty(i+".title",e.title());p.setProperty(i+".detail",e.detail());p.setProperty(i+".important",Boolean.toString(e.important()));}Path temp=Files.createTempFile(path.toAbsolutePath().getParent(),"activity-",".tmp");try{try(var out=Files.newOutputStream(temp)){p.store(out,"Undismissed game activity");}try{Files.move(temp,path,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}catch(AtomicMoveNotSupportedException e){Files.move(temp,path,StandardCopyOption.REPLACE_EXISTING);}}finally{Files.deleteIfExists(temp);}}
}
