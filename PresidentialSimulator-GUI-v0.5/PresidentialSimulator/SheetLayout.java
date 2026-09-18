import java.awt.*;
import javax.swing.*;
/** Width-first vertical layout: no dependency on a child's previous bounds. */
public final class SheetLayout implements LayoutManager {
    public void addLayoutComponent(String name,Component c){}
    public void removeLayoutComponent(Component c){}
    public Dimension minimumLayoutSize(Container p){return new Dimension(0,0);}
    public Dimension preferredLayoutSize(Container p){int width=p.getWidth()>0?p.getWidth():600;return new Dimension(width,height(p,width));}
    private static int height(Container p,int width){Insets i=p.getInsets();int h=i.top+i.bottom;for(Component c:p.getComponents())if(c.isVisible())h+=measure(c,Math.max(40,width-i.left-i.right));return h;}
    public void layoutContainer(Container p){Insets i=p.getInsets();int width=Math.max(0,p.getWidth()-i.left-i.right),y=i.top;for(Component c:p.getComponents())if(c.isVisible()){int h=measure(c,width);c.setBounds(i.left,y,width,h);y+=h;}}
    static int measure(Component c,int width){
        if(c instanceof Container p&&p.getLayout() instanceof SheetLayout)return height(p,width);
        if(c instanceof JTextArea area){Insets i=area.getInsets();return lines(area.getText(),area.getFontMetrics(area.getFont()),Math.max(20,width-i.left-i.right-6))*area.getFontMetrics(area.getFont()).getHeight()+i.top+i.bottom;}
        if(c instanceof JComboBox<?>)return 34;
        if(c instanceof JButton b){Insets i=b.getInsets();String plain=b.getText().replaceAll("<[^>]*>","").replace("&amp;","&").replace("&lt;","<").replace("&gt;",">");return Math.max(32,lines(plain,b.getFontMetrics(b.getFont()),Math.max(20,width-i.left-i.right-12))*b.getFontMetrics(b.getFont()).getHeight()+i.top+i.bottom+4);}
        return c.getPreferredSize().height;
    }
    static int lines(String text,FontMetrics fm,int room){int total=0;for(String paragraph:text.split("\n",-1)){total++;int used=0;for(String word:paragraph.split(" ")){int w=fm.stringWidth(word+" ");if(used>0&&used+w>room){total++;used=0;}used+=w;while(used>room){total++;used-=room;}}}return total;}
}
