import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
/** Full team rehearsal: separate session, memory and clock; never touches the career. */
public final class PracticeDebatePanel extends JPanel {
    private static final long serialVersionUID=1L;
    private final LiveDebate debate;
    private final DebateSettings settings;
    private final Timer clock;
    private final JLabel timer=DesktopPanel.label("",18,DesktopPanel.ACCENT);
    private int remaining;
    private long last;
    public PracticeDebatePanel(long seed,DebateSettings settings){
        this.settings=settings;debate=new LiveDebate("practice",0,new ArrayList<>(),settings);setLayout(new BorderLayout());
        clock=new Timer(150,e->pulse());render();
    }
    public void startClock(){last=System.nanoTime();clock.start();}
    public void stopClock(){clock.stop();}
    private void pulse(){if(!isShowing())return;Window w=SwingUtilities.getWindowAncestor(this);long now=System.nanoTime();if(w!=null&&!w.isFocused()){last=now;return;}int elapsed=(int)((now-last)/1_000_000_000L);if(elapsed<1)return;last+=elapsed*1_000_000_000L;if(remaining>0){remaining=Math.max(0,remaining-elapsed);if(remaining==0){debate.timeout();render();return;}}timer.setText(remaining>0?"Rehearsal clock: "+remaining+"s":"Untimed — read at your pace");}
    private void render(){
        removeAll();JPanel sheet=DesktopPanel.column();sheet.add(DesktopPanel.label("TEAM PRACTICE · FREE",22,DesktopPanel.ACCENT));sheet.add(DesktopPanel.text("No campaign cash, turns or briefing slots are spent. Nothing said here becomes a public promise. These fixed tutorial questions never appear in live debates."));
        if(debate.complete()){sheet.add(DesktopPanel.text("Rehearsal complete. Your team has walked you through answers, confidence, challenges and a policy question. Close this window when ready."));stopClock();}
        else{var event=debate.event();String key=event.id();remaining=debate.seconds();last=System.nanoTime();timer.setText(remaining>0?"Rehearsal clock: "+remaining+"s":"Untimed — read at your pace");sheet.add(timer);sheet.add(DesktopPanel.label(event.title().startsWith("CORRECT")?(settings.celebrations()?"WELL ANSWERED! — fact verified":"CORRECT — source verified"):event.title(),22,event.title().startsWith("CORRECT")?DesktopPanel.ACCENT:DesktopPanel.INK));JTextArea prompt=DesktopPanel.text(event.description());prompt.setFont(prompt.getFont().deriveFont((float)settings.textSize()));sheet.add(prompt);
            for(int i=0;i<event.options().size();i++){int choice=i;if(event.options().get(i).label().equals("Move to the next question")||event.options().get(i).label().equals("Finish debate"))continue;JButton b=DesktopPanel.button(event.options().get(i).label(),()->{pulse();if(!debate.complete()&&debate.event().id().equals(key)){debate.respond(choice);render();}});b.setFont(b.getFont().deriveFont((float)settings.textSize()));b.setName("Practice option "+i);sheet.add(b);sheet.add(Box.createVerticalStrut(8));}
        }
        JScrollPane scroll=new JScrollPane(sheet);scroll.setBorder(null);scroll.getViewport().setBackground(DesktopPanel.CARD);scroll.getVerticalScrollBar().setUnitIncrement(24);add(scroll);revalidate();repaint();
    }
}
