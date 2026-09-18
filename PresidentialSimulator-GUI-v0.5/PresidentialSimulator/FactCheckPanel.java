import java.awt.*;
import java.net.URI;
import java.util.*;
import javax.swing.*;
/** Unscored, explanatory fact-check practice; never submits simulation commands. */
public final class FactCheckPanel extends JPanel {
    private static final long serialVersionUID=1L;
    private int cardIndex;
    private final long seed;
    public FactCheckPanel(long seed,int start){this.seed=seed;cardIndex=Math.floorMod(start,DebateFactChecks.all().size());setLayout(new BorderLayout());render();}
    private void render(){removeAll();var card=DebateFactChecks.all().get(cardIndex);JPanel sheet=DesktopPanel.column();sheet.add(DesktopPanel.label(card.title(),18,DesktopPanel.INK));sheet.add(DesktopPanel.text(card.prompt()));JTextArea feedback=DesktopPanel.text("");feedback.setName("Fact check feedback");
        java.util.List<String> choices=new ArrayList<>(card.choices());Collections.shuffle(choices,new Random(seed ^ (long)cardIndex*7919));JPanel answers=DesktopPanel.column();
        for(String choice:choices){JButton b=DesktopPanel.button(choice,()->{feedback.setText((choice.equals(card.answer())?"That matches the source. ":"Check this distinction: ")+card.explanation()+"\nSource: "+card.source());for(Component c:answers.getComponents())c.setEnabled(false);revalidate();repaint();});b.setName("Fact answer "+choice);answers.add(b);}sheet.add(answers);sheet.add(feedback);
        sheet.add(DesktopPanel.button("Open source",()->{try{if(!Desktop.isDesktopSupported())throw new IllegalStateException();Desktop.getDesktop().browse(URI.create(card.source()));}catch(Exception e){feedback.setText("Open this source in your browser:\n"+card.source());revalidate();}}));
        sheet.add(DesktopPanel.button("Next fact check",()->{cardIndex=(cardIndex+1)%DebateFactChecks.all().size();render();}));sheet.add(DesktopPanel.text("Unscored practice. These answers do not change your career or campaign. Close this window to resume the live response."));JScrollPane scroll=new JScrollPane(sheet);scroll.setBorder(null);scroll.getVerticalScrollBar().setUnitIncrement(18);add(scroll);revalidate();repaint();
    }
}
