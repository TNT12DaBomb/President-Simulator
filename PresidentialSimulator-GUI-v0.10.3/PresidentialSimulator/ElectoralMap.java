import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;

/** Offline geometry renderer. Colors reflect existing snapshots; no election logic lives here. */
public final class ElectoralMap extends JComponent {
    private static final long serialVersionUID=1L;
    private static final Map<String,Path2D> GEOMETRY=load();
    private static final String[] SMALL={"Vermont","New Hampshire","Massachusetts","Rhode Island","Connecticut","New Jersey","Delaware","Maryland","District of Columbia"};
    private final Map<String,GameView.StateView> states=new LinkedHashMap<>();
    private Color yours=TeamColors.BLUE.fill,theirs=TeamColors.CORAL.fill;
    public void teamColors(TeamColors player,TeamColors opponent){yours=player.fill;theirs=opponent.fill;repaint();}
    private String selected,hover;
    private final Consumer<String> onSelect;
    private final Map<String,Shape> hits=new LinkedHashMap<>();
    private final Map<String,Shape> geographicHits=new LinkedHashMap<>();
    private Consumer<String> onHover=name->{};
    public void onHover(Consumer<String> listener){onHover=listener;}
    public Point geographicTarget(String name){Shape shape=geographicHits.get(name);if(shape==null)return null;Rectangle b=shape.getBounds();for(int y=b.y;y<=b.y+b.height;y++)for(int x=b.x;x<=b.x+b.width;x++)if(shape.contains(x,y)&&name.equals(stateAt(new Point(x,y))))return new Point(x,y);return null;}
    public ElectoralMap(GameView view,String selected,Consumer<String> onSelect){
        this.selected=selected;this.onSelect=onSelect;for(var s:view.states())states.put(s.name(),s);
        setOpaque(true);setBackground(DeskMenuFrame.PAPER);setPreferredSize(new Dimension(750,460));setMinimumSize(new Dimension(250,170));
        setToolTipText("");getAccessibleContext().setAccessibleName("United States electoral map. Use the state selector for keyboard navigation.");
        addMouseMotionListener(new MouseMotionAdapter(){public void mouseMoved(MouseEvent e){String next=stateAt(e.getPoint());if(!Objects.equals(next,hover)){hover=next;onHover.accept(next);setCursor(Cursor.getPredefinedCursor(next==null?Cursor.DEFAULT_CURSOR:Cursor.HAND_CURSOR));repaint();}}});
        addMouseListener(new MouseAdapter(){public void mouseExited(MouseEvent e){hover=null;onHover.accept(null);repaint();}public void mouseClicked(MouseEvent e){String s=stateAt(e.getPoint());if(s!=null){ElectoralMap.this.selected=s;onSelect.accept(s);repaint();}}});
    }
    @Override public javax.accessibility.AccessibleContext getAccessibleContext(){if(accessibleContext==null)accessibleContext=new AccessibleJComponent() {private static final long serialVersionUID=1L;};return accessibleContext;}
    public String selected(){return selected;}
    public void select(String name){if(states.containsKey(name)){selected=name;repaint();}}
    public Set<String> mappedStates(){return Collections.unmodifiableSet(states.keySet());}
    public String stateAt(Point p){String result=null;for(var e:hits.entrySet())if(e.getValue().contains(p))result=e.getKey();if(result==null)for(var e:geographicHits.entrySet())if(e.getValue().contains(p))result=e.getKey();return result;}
    public Point target(String name){Shape shape=hits.get(name);if(shape==null)return null;var b=shape.getBounds();for(int y=b.y;y<=b.y+b.height;y++)for(int x=b.x;x<=b.x+b.width;x++)if(shape.contains(x,y))return new Point(x,y);return null;}
    @Override public String getToolTipText(MouseEvent e){String name=stateAt(e.getPoint());if(name==null)return null;var s=states.get(name);return "<html><b>"+name+" · "+s.electoralVotes()+" electoral votes</b><br>"+(s.playerControls()?"Your statewide lead":"Opponent statewide lead")+"<br>Completed objectives: "+s.playerTasks().size()+" / "+s.objectives().size()+"<br>Click to view available campaign actions.</html>";}
    @Override protected void paintComponent(Graphics graphics){
        super.paintComponent(graphics);Graphics2D g=(Graphics2D)graphics.create();
        // Bare JComponent has no UI delegate to clear its opaque background.
        // Clear the dirty region before painting, including gaps and old outlines.
        g.setColor(getBackground());g.fillRect(0,0,getWidth(),getHeight());g.setColor(new Color(168,150,111));g.drawRect(5,5,getWidth()-11,getHeight()-11);g.setColor(new Color(168,150,111,28));g.drawLine(getWidth()/2,12,getWidth()/2,getHeight()-12);g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        double scale=Math.max(.01,Math.min((getWidth()-16)/1110.0,(getHeight()-16)/640.0));AffineTransform tx=new AffineTransform();tx.translate((getWidth()-1110*scale)/2,(getHeight()-640*scale)/2);tx.scale(scale,scale);hits.clear();geographicHits.clear();
        for(var e:states.entrySet()){Path2D p=GEOMETRY.get(e.getKey());if(p==null)continue;Shape shape=tx.createTransformedShape(p);hits.put(e.getKey(),shape);geographicHits.put(e.getKey(),shape);g.setColor(e.getValue().playerControls()?yours:theirs);g.fill(shape);g.setColor(new Color(13,25,41));g.setStroke(new BasicStroke(1));g.draw(shape);}
        Shape dcShape=geographicHits.get("District of Columbia");if(dcShape!=null){Rectangle2D b=dcShape.getBounds2D();Shape marker=new Ellipse2D.Double(b.getCenterX()-4,b.getCenterY()-4,8,8);geographicHits.put("District of Columbia",marker);g.setColor(Color.WHITE);g.draw(marker);}
        for(int i=0;i<SMALL.length;i++){String name=SMALL[i];Shape rect=tx.createTransformedShape(new RoundRectangle2D.Double(975,125+i*43,128,36,5,5));hits.put(name,rect);g.setColor(states.get(name).playerControls()?yours:theirs);g.fill(rect);g.setColor(DesktopPanel.INK);g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,Math.max(9,(int)(14*scale))));var b=rect.getBounds();String title=abbreviation(name)+" · "+states.get(name).electoralVotes();g.drawString(title,b.x+7,b.y+b.height/2+4);}
        for(String name:new String[]{hover,selected})if(name!=null){Shape shape=hits.get(name);if(shape!=null){g.setColor(name.equals(selected)?new Color(255,208,108):Color.WHITE);g.setStroke(new BasicStroke(name.equals(selected)?3:2));g.draw(shape);}Path2D path=GEOMETRY.get(name);if(path!=null)g.draw(tx.createTransformedShape(path));}
        g.setColor(DesktopPanel.MUTED);g.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,11));g.dispose();
    }
    static void paperOutline(Graphics2D g,int x,int y,int width,int height){Graphics2D paper=(Graphics2D)g.create();paper.translate(x,y);double scale=Math.min(width/1110.0,height/640.0);paper.scale(scale,scale);paper.setColor(new Color(100,128,107));for(Path2D path:GEOMETRY.values()){paper.fill(path);paper.setColor(new Color(231,222,195));paper.setStroke(new BasicStroke(2));paper.draw(path);paper.setColor(new Color(100,128,107));}paper.dispose();}
    private static String abbreviation(String name){return switch(name){case "Vermont"->"VT";case "New Hampshire"->"NH";case "Massachusetts"->"MA";case "Rhode Island"->"RI";case "Connecticut"->"CT";case "New Jersey"->"NJ";case "Delaware"->"DE";case "Maryland"->"MD";default->"DC";};}
    private static Map<String,Path2D> load(){
        try{InputStream input=ElectoralMap.class.getResourceAsStream("/assets/states.bin");if(input==null)input=Files.newInputStream(Path.of("assets","states.bin"));Map<String,Path2D> paths=new LinkedHashMap<>();
            try(DataInputStream in=new DataInputStream(input)){int count=in.readInt();for(int i=0;i<count;i++){String name=in.readUTF();Path2D p=new Path2D.Float(Path2D.WIND_EVEN_ODD);int rings=in.readInt();for(int r=0;r<rings;r++){int points=in.readInt();for(int j=0;j<points;j++){float x=in.readFloat(),y=in.readFloat();if(j==0)p.moveTo(x,y);else p.lineTo(x,y);}p.closePath();}paths.put(name,p);}}return Collections.unmodifiableMap(paths);
        }catch(IOException e){throw new IllegalStateException("Map resource missing or damaged. Extract the complete distribution or rebuild with assets.",e);}
    }
}
