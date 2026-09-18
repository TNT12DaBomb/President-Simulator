import java.awt.*;
import java.nio.file.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Persistent desktop shell. Reads snapshots and submits typed commands only. */
public final class DesktopPanel extends JPanel {
    private static final long serialVersionUID=1L;
    static final Color BG=new Color(28,37,31), CARD=new Color(36,49,41), INK=new Color(242,233,211), MUTED=new Color(180,178,157), ACCENT=new Color(194,169,113);
    final DesktopController controller;
    private final DebateWindow debateWindow;
    private final JPanel content=new JPanel(new BorderLayout(10,10)), stats=new JPanel(new GridLayout(1,0,8,0)), event=column();
    private final JLabel heading=new JLabel(), timer=new JLabel(), status=new JLabel();
    private final JPanel newsPanel=column();
    private ActivityInbox activity;
    private String activityError="";
    private Long nextDeveloperSeed;
    private DesktopController.Notice currentNotice;
    private String page="Dashboard", selectedState="Pennsylvania", shownDecision="";
    private ElectoralMap map;
    private final OfficeWorkspace.Selection officeSelection=new OfficeWorkspace.Selection();
    private CareerView.Phase lastPhase;
    private JButton officeNav;
    private OfficeWorkspace currentOffice;
    private DeskDefinition.Item openedItem;
    private JButton homeButton, navigation;
    private JPanel bottomDock;
    private boolean menuPaused;
    private String menuReturn="Desk";
    private final Map<String,String> previousResourceValues=new HashMap<>();
    private final Map<String,Long> resourceChanges=new HashMap<>();
    private int policyTab,policyIssue,policyApproach;
    private DebateSettings setupSettings=DebateSettings.DEFAULT;
    private boolean setupRealtime=true;
    private int setupFrequency=1;
    private TeamColors setupColor=TeamColors.BLUE;
    private TeamColors playerColor(){return TeamColors.values()[controller.campaignColor()];}
    private TeamColors opponentColor(){return TeamColors.opponent(controller.careerSeed(),controller.campaignColor());}
    private final Path saves=Path.of(System.getProperty("user.home"),"PresidentialSimulator","saves");
    public DesktopPanel(DesktopController controller) {
        try{activity=new ActivityInbox(GraphicsEnvironment.isHeadless()?null:Path.of(System.getProperty("user.home"),"PresidentialSimulator","activity.properties"));}catch(java.io.IOException e){try{activity=new ActivityInbox(null);}catch(java.io.IOException impossible){throw new IllegalStateException(impossible);}activityError="Activity storage unavailable: "+e.getMessage();}
        this.controller=controller;debateWindow=new DebateWindow(this,controller,this::practice);setLayout(new BorderLayout());setBackground(BG);
        content.setOpaque(false);add(content,BorderLayout.CENTER);
        JPanel dock=DeskStyle.dock();bottomDock=dock;dock.setName("Bottom dock");dock.setPreferredSize(new Dimension(0,112));
        JPanel readout=new JPanel(new BorderLayout(0,4));readout.setOpaque(false);
        heading.setForeground(DeskStyle.GOLD);heading.setFont(new Font(Font.SANS_SERIF,Font.BOLD,11));readout.add(heading,BorderLayout.SOUTH);
        stats.setOpaque(false);readout.add(stats,BorderLayout.CENTER);
        newsPanel.setBorder(new EmptyBorder(0,0,0,0));newsPanel.setOpaque(false);newsPanel.setName("News banner");newsPanel.setLayout(new GridLayout(2,1,0,0));newsPanel.setPreferredSize(new Dimension(0,30));readout.add(newsPanel,BorderLayout.NORTH);dock.add(readout,BorderLayout.CENTER);
        JPanel statsRow=new JPanel(new BorderLayout(16,0));statsRow.setOpaque(false);readout.remove(stats);statsRow.add(stats,BorderLayout.CENTER);
        navigation=button("Menu",()->{if(page.equals("Menu")){page=menuReturn;render();}else if(controller.view()!=null&&!page.equals("Desk")&&!page.equals("Dashboard")){openPage("Desk");}else menu();});navigation.setPreferredSize(new Dimension(116,40));statsRow.add(navigation,BorderLayout.EAST);readout.add(statsRow,BorderLayout.CENTER);
        timer.setForeground(ACCENT);timer.setFont(new Font(Font.SANS_SERIF,Font.BOLD,12));JPanel footer=new JPanel(new BorderLayout());footer.setOpaque(false);readout.remove(heading);footer.add(heading);footer.add(timer,BorderLayout.EAST);readout.add(footer,BorderLayout.SOUTH);
        homeButton=button("← Desk",null);add(dock,BorderLayout.SOUTH);
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ESCAPE"),"back");getActionMap().put("back",new AbstractAction(){private static final long serialVersionUID=1L;public void actionPerformed(java.awt.event.ActionEvent e){if(page.equals("Menu")){page=menuReturn;render();}else menu();}});
        controller.changed=this::render;controller.timerChanged=this::updateTimer;controller.notified=this::notice;render();
    }
    private void menu(){if(!page.equals("Menu")){menuReturn=page;page="Menu";openedItem=null;if(!menuPaused){controller.pause();menuPaused=true;}}render();}
    private JComponent menuPage(){JPanel outer=DeskStyle.surface();outer.setLayout(new GridBagLayout());JPanel panel=column();panel.setOpaque(false);panel.setPreferredSize(new Dimension(540,365));panel.add(label("MENU",11,DeskStyle.GOLD));panel.add(label("Take a moment.",28,DeskStyle.CREAM));panel.add(text("Save your progress or adjust your preferences."));JPanel actions=new JPanel(new GridLayout(0,2,10,10));actions.setOpaque(false);String[] names={"Save / Load","Settings","Latest Activity","Career history","Statistics","Electoral map","Developer tools","New career"};Runnable[] commands={this::saveMenu,this::settings,()->openPage("Latest Activity"),()->openPage("History"),()->openPage("Statistics"),()->openPage("Map"),this::developer,this::newCareer};for(int i=0;i<names.length;i++){JButton b=button(names[i],commands[i]);if(controller.view()==null&&(i>=2&&i<=5))continue;actions.add(b);}actions.setPreferredSize(new Dimension(500,190));panel.add(actions);panel.add(Box.createVerticalStrut(12));JPanel footer=new JPanel(new GridLayout(1,3,10,0));footer.setOpaque(false);footer.add(button("Resume",()->{page=menuReturn;render();}));footer.add(button("How to play",this::help));footer.add(button("Exit game",()->{Window w=SwingUtilities.getWindowAncestor(this);if(w!=null)requestClose(w::dispose);}));footer.setPreferredSize(new Dimension(500,36));panel.add(footer);outer.add(panel);return outer;}
    private JComponent startPage(){
        JPanel scene=DeskStyle.surface();scene.setLayout(new GridBagLayout());
        JPanel welcome=new JPanel();welcome.setOpaque(false);welcome.setLayout(new BoxLayout(welcome,BoxLayout.Y_AXIS));welcome.setBorder(new EmptyBorder(20,24,20,24));welcome.setPreferredSize(new Dimension(560,340));
        JLabel edition=label("A CAREER IN PUBLIC LIFE",12,DeskStyle.GOLD);welcome.add(edition);welcome.add(Box.createVerticalStrut(16));
        welcome.add(label("PRESIDENTIAL",36,DeskStyle.CREAM));welcome.add(label("SIMULATOR",36,DeskStyle.CREAM));welcome.add(Box.createVerticalStrut(15));
        JLabel line=label("Earn the vote. Shape your presidency.",16,DeskStyle.CREAM);welcome.add(line);welcome.add(Box.createVerticalStrut(26));
        JPanel actions=new JPanel(new GridLayout(1,2,12,0));actions.setOpaque(false);actions.setAlignmentX(LEFT_ALIGNMENT);actions.setMaximumSize(new Dimension(510,46));actions.add(button("Begin a career",this::newCareer));actions.add(button("Continue from save",this::saveMenu));welcome.add(actions);welcome.add(Box.createVerticalStrut(20));
        scene.add(welcome);scene.setFocusable(true);scene.addHierarchyListener(e->{if((e.getChangeFlags()&java.awt.event.HierarchyEvent.SHOWING_CHANGED)!=0&&scene.isShowing())SwingUtilities.invokeLater(scene::requestFocusInWindow);});return scene;
    }
    private void openPage(String destination){page=destination;openedItem=null;render();}
    private JComponent dialogForm(Object[] fields){JPanel form=new JPanel();form.setLayout(new BoxLayout(form,BoxLayout.Y_AXIS));form.setBackground(DeskMenuFrame.PAPER);for(Object field:fields){JComponent item;if(field instanceof JComponent component)item=component;else{JTextArea label=text(String.valueOf(field));label.setForeground(DeskMenuFrame.INK);label.setBorder(new EmptyBorder(6,0,3,0));item=label;}item.setAlignmentX(LEFT_ALIGNMENT);form.add(item);form.add(Box.createVerticalStrut(4));}JScrollPane scroller=new JScrollPane(form);scroller.setBorder(null);scroller.getVerticalScrollBar().setUnitIncrement(20);scroller.setPreferredSize(new Dimension(Math.min(570,Math.max(320,getWidth()-130)),Math.min(430,Math.max(240,getHeight()-190))));return scroller;}
    private void showReport(java.util.List<String> lines){showText("Update details",lines);}
    private void showText(String title,java.util.List<String> lines){dialog(()->{JTextArea report=text(String.join("\n\n",lines));report.setForeground(Color.DARK_GRAY);JScrollPane area=new JScrollPane(report);area.getViewport().setBackground(DeskMenuFrame.PAPER);area.setPreferredSize(new Dimension(Math.min(650,Math.max(320,getWidth()-100)),Math.min(380,Math.max(180,getHeight()-180))));JOptionPane.showMessageDialog(this,area,title,JOptionPane.INFORMATION_MESSAGE);});}
    public void notice(DesktopController.Notice n){currentNotice=n;try{activity.add(n);}catch(java.io.IOException e){activityError="Activity is visible but could not be saved: "+e.getMessage();}if(n.title().equals("Action unavailable")||n.title().equals("Save failed")||n.title().equals("Load failed"))page="Latest Activity";render();}
    private int activityPageIndex;
    private void activityPage(JPanel p){
        if(!activityError.isEmpty())p.add(text(activityError));var entries=activity.entries();if(entries.isEmpty()){p.add(text("No undismissed reports."));return;}
        activityPageIndex=Math.min(activityPageIndex,entries.size()-1);int index=entries.size()-1-activityPageIndex;var n=entries.get(index);
        p.add(label(n.title(),20,INK));p.add(text(n.detail()));JPanel controls=new JPanel(new GridLayout(1,3,8,0));controls.setOpaque(false);
        controls.add(button("Newer",()->{activityPageIndex=Math.max(0,activityPageIndex-1);render();}));controls.add(button("Dismiss report",()->{try{activity.dismiss(index);}catch(java.io.IOException e){activityError=e.getMessage();}render();}));controls.add(button("Older",()->{activityPageIndex=Math.min(entries.size()-1,activityPageIndex+1);render();}));p.add(controls);p.add(label((activityPageIndex+1)+" / "+entries.size()+" reports",12,MUTED));
    }

    String selectedState(){return selectedState;}
    String notificationTitle(){return currentNotice==null?"":currentNotice.title();}
    String currentPage(){return page;}
    private static final class WidthColumn extends JPanel implements Scrollable {
        private static final long serialVersionUID=1L;
        public Dimension getPreferredScrollableViewportSize(){return getPreferredSize();}
        public int getScrollableUnitIncrement(Rectangle r,int o,int d){return 18;}
        public int getScrollableBlockIncrement(Rectangle r,int o,int d){return Math.max(18,r.height-30);}
        
        public boolean getScrollableTracksViewportWidth(){return true;}
        public boolean getScrollableTracksViewportHeight(){return false;}
    }
    static JPanel column(){JPanel p=new WidthColumn();p.setLayout(new SheetLayout());p.setBackground(CARD);p.setAlignmentX(LEFT_ALIGNMENT);p.setBorder(new EmptyBorder(12,12,12,12));return p;}
    static JLabel label(String s,int size,Color color){JLabel l=new JLabel(s);l.setFont(new Font(Font.SANS_SERIF,Font.BOLD,size));l.setForeground(color);l.setAlignmentX(LEFT_ALIGNMENT);return l;}
    static JTextArea text(String s){JTextArea t=new JTextArea(s);t.setLineWrap(true);t.setWrapStyleWord(true);t.setEditable(false);t.setOpaque(false);t.setForeground(INK);t.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,14));t.setBorder(new EmptyBorder(8,0,12,0));t.setAlignmentX(LEFT_ALIGNMENT);return t;}
    static String friendlyName(Object value){String text=value.toString().toLowerCase(Locale.ROOT).replace('_',' ');return text.isEmpty()?text:Character.toUpperCase(text.charAt(0))+text.substring(1);}
    static void friendly(PaperChoice<?> choice){}

    private static final class WrappingButton extends JButton {
        private static final long serialVersionUID=1L;
        WrappingButton(String label){super(label);}
        @Override public Dimension getPreferredSize(){Dimension d=super.getPreferredSize();String name=getText().replaceAll("<[^>]*>","");if(name.length()>80)return new Dimension(Math.min(600,d.width),Math.max(80,d.height));return d;}
        @Override public Dimension getMaximumSize(){return new Dimension(Integer.MAX_VALUE,getPreferredSize().height);}
        @Override public Dimension getMinimumSize(){return new Dimension(20,getPreferredSize().height);}
    }
    static JButton button(String name,Runnable action){JButton b=new WrappingButton("<html><div style='text-align:center'>"+name.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")+"</div></html>");b.setName(name);b.setAlignmentX(LEFT_ALIGNMENT);b.setMaximumSize(new Dimension(Integer.MAX_VALUE,42));b.setBackground(new Color(39,57,76));b.setForeground(INK);b.setFocusPainted(true);b.setMargin(new Insets(7,9,7,9));b.addActionListener(e->action.run());DeskStyle.button(b);b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));return b;}
    private void action(JPanel p,String name,CareerCommand cmd){p.add(button(name,()->controller.submit(cmd)));}
    private JScrollPane scroll(JComponent p){JScrollPane s=new JScrollPane(p);s.setBorder(null);s.getViewport().setBackground(CARD);s.getVerticalScrollBar().setUnitIncrement(18);return s;}
    public void render(){
        if(menuPaused&&!page.equals("Menu")){menuPaused=false;controller.resume();}
        CareerView v=controller.view();currentOffice=null;content.setName("Main workspace");content.removeAll();stats.removeAll();event.removeAll();
        boolean active=v!=null&&(v.phase()==CareerView.Phase.CAMPAIGN||v.phase()==CareerView.Phase.PRESIDENCY);
        if(v!=null&&(lastPhase==null||lastPhase!=v.phase())){page=active?"Desk":"Dashboard";openedItem=null;officeSelection.administration=false;}
        bottomDock.setPreferredSize(new Dimension(0,v==null?62:112));newsPanel.setVisible(v!=null);navigation.setText(page.equals("Menu")?"Resume":active&&!page.equals("Desk")?"Return":"Menu");navigation.setName(page.equals("Menu")?"Resume":active&&!page.equals("Desk")?"← Desk":"Menu");
        lastPhase=v==null?null:v.phase();homeButton.setEnabled(active&&!page.equals("Desk"));homeButton.setVisible(active&&!page.equals("Desk"));
        if(!active&&page.equals("Desk"))page="Dashboard";
        String key=controller.decisionKey();
        if(key.isEmpty()&&page.equals("Decisions")&&!shownDecision.isEmpty()){page=active?"Desk":"Dashboard";openedItem=null;}shownDecision=key;
        heading.setText(v==null?"": "Year "+v.year()+" / "+v.phase().name().replace('_',' '));
        JPanel body=column();if(v==null)body.add(label("Welcome",22,INK));
        if(page.equals("Menu")){content.add(menuPage());}
        else if(page.equals("Latest Activity")){activityPage(body);content.add(scroll(body));}
        else if(v==null){content.add(startPage());}
        else if(page.equals("Desk")){content.add(deskHub(v));}
        else if(page.equals("PresidentialActions")){content.add(scroll(presidentialActions(v)));}
        else if(page.equals("Dashboard")&&v.phase()==CareerView.Phase.PRESIDENCY){content.add(deskHub(v));}
        else if(page.equals("Policy")||page.equals("Legislation")||page.equals("Promises")){JComponent policy=policyPage(v);if(page.equals("Promises"))filterPolicyTabs(policy,true);content.add(policy);}
        else if(page.equals("Cabinet")||page.equals("Correspondence")||page.equals("Midterms")){officeSelection.tab=page.equals("Cabinet")?0:page.equals("Correspondence")?1:2;content.add(new AdministrationWorkspace(controller,officeSelection,false));}
        else if(page.equals("Fundraising")){body.add(text("Campaign cash pays for state actions. Fundraising advances one campaign turn."));action(body,"Fundraise (+$"+v.campaign().fundraisingAmount()+")",CareerCommand.campaign(GameCommand.fundraise()));content.add(scroll(body));}
        else if(page.equals("DebatePrep")){body.add(text("Review mode, response pace and text size in Settings. Briefing slots are allocated before each real appearance. Practice uses a separate fixed question set."));body.add(button("Open debate settings",this::settings));if(debateWindow.active())body.add(button("Resume live debate",debateWindow::open));content.add(scroll(body));}
        else if(page.equals("News")){content.add(new DocumentPages(newspaperPages(v)));}
        else if(page.equals("Office")){content.add(deskHub(v));}
        else if(page.equals("Map")){content.add(mapPage(v));}
        else if(page.equals("Decisions")){if(debateWindow.active()){event.add(label("Your debate has its own window",22,INK));event.add(text("The phone keeps your pending response. Deadlines continue while you use other workspaces."));event.add(button("Open / resume live debate",debateWindow::open));}else decision(v);if(event.getComponentCount()==0)event.add(text("No decision is waiting. Continue your campaign or advance a month in office."));content.add(scroll(event));}
        else{switch(page){case "Schedule"->schedule(body,v);case "Statistics"->{statistics(body,v);body.add(button("Career archive",()->openPage("History")));body.add(button("Activity records",()->openPage("Latest Activity")));}case "History"->history(body,v);case "Latest Activity"->activityPage(body);default->dashboard(body,v);}content.add(scroll(body));}
        if(v!=null&&!page.equals("Desk")&&!page.equals("Menu")&&content.getComponentCount()>0){Component screen=content.getComponent(0);content.remove(screen);String theme=openedItem!=null&&openedItem.target().equals(page)?openedItem.menuTheme():themeFor(page);content.add(new DeskMenuFrame(theme,openedItem!=null&&openedItem.target().equals(page)?openedItem.label():page,(JComponent)screen,null));}
        newsPanel.removeAll();var headlines=v==null?java.util.List.of(""):NewsFeed.headlines(v);
        for(int i=0;i<Math.min(2,headlines.size());i++){String line=headlines.get(i);JLabel item=label((i==0?"NEWS  •  ":"             ")+line,11,i==0?INK:MUTED);item.setName("News headline");item.setToolTipText(line);newsPanel.add(item);}
        if(v!=null)sidebar(v);else stats.add(label("",12,MUTED));
        status.setText(controller.busy()?"Saving / loading… controls and clocks paused.":v==null?"Offline desktop simulation · Java 17+":"Latest Activity: "+activity.entries().size()+" undismissed  ·  "+(controller.decisionKey().isEmpty()?"No pending decision":"Decision waiting — open Decisions")+"  ·  Menu for saves and settings");
        setEnabledTree(content,!controller.busy());updateTimer();debateWindow.sync();revalidate();repaint();
    }
    private java.util.List<DocumentPages.Page> newspaperPages(CareerView v){
        var stories=new ArrayList<DocumentPages.Page>();
        if(v.phase()==CareerView.Phase.CAMPAIGN&&v.campaign().pendingEvent()!=null){var e=v.campaign().pendingEvent();stories.add(new DocumentPages.Page(e.title(),e.description()+"\n\nA response is awaiting the campaign.\nSource: current event briefing."));}
        if(v.phase()==CareerView.Phase.PRESIDENCY&&v.world().pending()!=null){var e=v.world().pending();stories.add(new DocumentPages.Page(e.title(),e.display().description()+"\n\nThe administration has a pending decision.\nSource: current event briefing."));}
        var log=v.phase()==CareerView.Phase.CAMPAIGN?v.campaign().history():v.history();
        var latest=new ArrayList<String>();for(int i=log.size()-1;i>=0&&latest.size()<4;i--){String line=log.get(i);if(line.startsWith("NEWS [")||line.startsWith("EVENT:")||line.startsWith("Inauguration:"))latest.add(line.replaceFirst("^NEWS \\[.*?\\]: ",""));}if(latest.isEmpty())latest.add("There are no additional dispatches in this edition. New event briefings and recorded developments will appear here as the career progresses.");
        stories.add(new DocumentPages.Page("The latest dispatch",String.join("\n\n",latest)+"\n\nSource: recent career dispatches, newest first."));
        stories.add(new DocumentPages.Page("The working calendar",v.phase()==CareerView.Phase.CAMPAIGN?"The campaign is on turn "+v.campaign().turnsUsed()+" of "+v.campaign().totalTurns()+".\n\n"+v.campaign().activeEffects().stream().collect(java.util.stream.Collectors.joining("\n\n")):v.phase()==CareerView.Phase.PRESIDENCY?v.presidency().date()+"\n\n"+v.presidency().implementations().stream().map(x->x.title()+" — due in month "+x.dueMonth()).collect(java.util.stream.Collectors.joining("\n\n")):"The career is in "+friendlyName(v.phase())+". Review the next step in your career record."));
        stories.add(new DocumentPages.Page("Around town",NewsFeed.headlines(v).get(1)+"\n\nFrom the local-interest column. This background item has no attached decision or additional reported details."));return stories;
    }
    private JPanel presidentialActions(CareerView v){JPanel p=column();p.add(text(v.presidency().date()+" · "+v.presidency().actionsLeft()+" actions remaining. Select an action to read its purpose before confirming."));var actions=v.governanceOptions();PaperChoice<String> pick=new PaperChoice<>(actions.stream().map(a->friendlyName(a.action())+" · $"+a.cost()).toArray(String[]::new));p.add(pick);JTextArea explanation=text("");p.add(explanation);JButton take=button("Take selected action",()->controller.submit(CareerCommand.govern(actions.get(pick.getSelectedIndex()).action())));p.add(take);Runnable update=()->{var action=actions.get(pick.getSelectedIndex());explanation.setText(action.available()?WorldSimulation.actionHelp(action.action()):action.reason());take.setEnabled(action.available()&&!controller.busy());};pick.addActionListener(e->update.run());update.run();return p;}

    private JComponent deskHub(CareerView v){
        boolean presidency=v.phase()==CareerView.Phase.PRESIDENCY;var notices=new HashMap<String,Integer>();notices.put("decision",controller.decisionKey().isEmpty()?0:1);notices.put("activity",activity.entries().size());notices.put("news",1);notices.put("debate",debateWindow.active()?1:0);
        if(presidency){var o=v.presidency();notices.put("bill",o.bill()==null?0:1);notices.put("nominees",(int)o.cabinet().seats().stream().filter(x->x.nominee()!=null).count());notices.put("letters",(int)o.correspondence().requests().stream().filter(x->!x.answered()).count());notices.put("projects",o.implementations().size());}
        return new DeskHub(presidency?DeskDefinition.presidency():DeskDefinition.campaign(),new DeskHub.Snapshot(Set.of("always"),notices),item->{openedItem=item;page=item.target();render();});
    }
    private static String themeFor(String page){return switch(page){case "Policy","Legislation","Promises","Cabinet","DebatePrep","Fundraising"->"binder";case "News"->"newspaper";case "Schedule"->"calendar";case "Decisions"->"phone";case "Map","Midterms"->"map";case "Statistics"->"report";case "Correspondence","PresidentialActions"->"letter";default->"clipboard";};}
    private static void filterPolicyTabs(Container root,boolean promises){for(Component c:root.getComponents()){if(c instanceof JTabbedPane tabs){for(int i=tabs.getTabCount()-1;i>=0;i--)if(promises?i!=2:i==2)tabs.removeTabAt(i);return;}if(c instanceof Container n)filterPolicyTabs(n,promises);}}
    private static void setEnabledTree(Container p,boolean enabled){if(!enabled)for(Component c:p.getComponents()){c.setEnabled(false);if(c instanceof Container child)setEnabledTree(child,false);}}
    private void dashboard(JPanel p,CareerView v){
        p.add(text(switch(v.phase()){case CAMPAIGN->"Campaign turn "+v.campaign().turnsUsed()+" of "+v.campaign().totalTurns()+". Complete state objectives to build your electoral coalition.";case PRESIDENCY->v.presidency().date()+"  •  "+v.presidency().actionsLeft()+" actions remaining this month";default->"Your career continues. Review the outcome and choose your next step.";}));
        switch(v.phase()){
            case CAMPAIGN->{p.add(label("YOU — "+playerColor()+" CAMPAIGN",20,playerColor().accent));p.add(text(opponentColor()+" states favor your opponent. Gold outlines your selected state."));p.add(button("Open electoral map",()->{page="Map";render();}));action(p,"Fundraise (+$"+v.campaign().fundraisingAmount()+" campaign funds)",CareerCommand.campaign(GameCommand.fundraise()));action(p,"Rest / advance campaign",CareerCommand.campaign(GameCommand.rest()));}
            case ELECTION_REVIEW->{var e=v.elections().get(v.elections().size()-1);p.add(label(e.outcome(),22,ACCENT));p.add(text("Electoral votes: "+e.playerEV()+" / opponent "+e.opponentEV()+"\n"+String.join("\n",e.consequences())));action(p,"Continue",CareerCommand.advance());}
            case TRANSITION->{p.add(text(String.join("\n",v.transitionResources())));action(p,"Begin presidency",CareerCommand.advance());}
            case PRESIDENCY->{
                p.add(button("Open Office — cabinet, requests and midterms",()->{page="Office";render();}));p.add(Box.createVerticalStrut(8));String[] names=v.governanceOptions().stream().map(a->a.action()+" · "+a.cost()+" office funds").toArray(String[]::new);
                PaperChoice<String> options=new PaperChoice<>(names);options.setMaximumSize(new Dimension(Integer.MAX_VALUE,36));options.setAlignmentX(LEFT_ALIGNMENT);p.add(options);
                JTextArea explanation=text("");p.add(explanation);
                JButton take=button("Take presidential action",()->controller.submit(CareerCommand.govern(v.governanceOptions().get(options.getSelectedIndex()).action())));p.add(take);
                Runnable select=()->{var a=v.governanceOptions().get(options.getSelectedIndex());explanation.setText(a.available()?WorldSimulation.actionHelp(a.action()):a.reason());take.setEnabled(a.available());};options.addActionListener(e->select.run());select.run();
                
            }
            case TERM_REVIEW->{action(p,"Run again",CareerCommand.runAgain());action(p,"Retire",CareerCommand.retire());}
            case OPPOSITION->{p.add(text("Rebuild year "+v.comebackYears()+" of 4. Losses carry forward into future campaigns."));for(var a:CareerCommand.RebuildAction.values())action(p,a.toString(),CareerCommand.rebuild(a));action(p,"Launch comeback campaign",CareerCommand.runAgain());action(p,"Retire",CareerCommand.retire());}
            case RETIRED->{p.add(text("Career complete. Your elections and record remain available in History and Statistics."));p.add(button("Start another career",this::newCareer));}
        }
        p.add(Box.createVerticalStrut(18));p.add(new JSeparator());p.add(label("LATEST ACTIVITY",12,ACCENT));
        if(activity.entries().isEmpty())p.add(text("No undismissed reports."));else{var latest=activity.entries().get(activity.entries().size()-1);JLabel preview=label(latest.title()+" — "+latest.detail().replaceAll("\\R"," "),13,MUTED);preview.setToolTipText(latest.detail());p.add(preview);}
        p.add(button("Open Latest Activity ("+activity.entries().size()+")",()->{page="Latest Activity";render();}));
    }
    private JComponent mapPage(CareerView v){
        JPanel panel=new JPanel(new BorderLayout(10,10));panel.setOpaque(false);
        JPanel west=new JPanel(new BorderLayout(4,4));west.setOpaque(false);
        JPanel title=new JPanel(new BorderLayout());title.setOpaque(false);title.add(label("Electoral map",22,INK));west.add(title,BorderLayout.NORTH);
        JPanel inspector=column();inspector.setBorder(new EmptyBorder(10,10,10,10));
        StateDirectory selector=new StateDirectory(v.campaign().states().stream().map(GameView.StateView::name).sorted().toArray(String[]::new));selector.setName("State selector");selector.setSelectedItem(selectedState);selector.setAlignmentX(LEFT_ALIGNMENT);
        JPanel details=column();details.setBorder(new EmptyBorder(8,0,0,0));
        Runnable update=()->{selectedState=selector.getSelectedItem();map.select(selectedState);details.removeAll();var s=v.campaign().states().stream().filter(x->x.name().equals(selectedState)).findFirst().orElseThrow();details.add(label(s.name()+" · "+s.electoralVotes()+" EV",17,ACCENT));details.add(text((s.playerControls()?"Your statewide lead":"Opponent statewide lead")+"\nCompleted: "+s.playerTasks().size()+" of "+s.objectives().size()+" objectives"));
            for(var a:s.actions()){String reason=v.phase()!=CareerView.Phase.CAMPAIGN?"Campaign actions are available during a campaign.":!a.available()?a.reason():"Costs $"+a.cost()+" funds and one campaign turn.";JButton b=button(a.task()+" · $"+a.cost()+" funds",()->controller.submit(CareerCommand.campaign(GameCommand.campaign(s.name(),a.task()))));b.setName("State action "+a.task().name());b.setEnabled(v.phase()==CareerView.Phase.CAMPAIGN&&a.available()&&!controller.busy());b.setToolTipText(reason);details.add(b);}
            details.add(button("State details",()->showReport(java.util.List.of(s.explanation(),"Objectives: "+s.objectives(),"Your completed work: "+s.playerTasks(),"Opponent completed work: "+s.opponentTasks()))));details.revalidate();details.repaint();};
        map=new ElectoralMap(v.campaign(),selectedState,name->selector.setSelectedItem(name));map.teamColors(playerColor(),opponentColor());map.setName("Electoral map");west.add(map,BorderLayout.CENTER);
        JTextArea legend=text(playerColor()+": your lead · "+opponentColor()+": opponent · Gold: selected");legend.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,12));west.add(legend,BorderLayout.SOUTH);JLabel identity=label("YOU — "+playerColor()+" / OPPONENT — "+opponentColor(),15,playerColor().accent);identity.setName("Campaign identity");JPanel clippings=new JPanel(new BorderLayout(0,3));clippings.setOpaque(false);clippings.add(identity,BorderLayout.NORTH);JLabel news=label(NewsFeed.headlines(v).get(0),12,DeskMenuFrame.INK);news.setToolTipText(String.join(" · ",NewsFeed.headlines(v)));JPanel strip=new JPanel(new BorderLayout(8,0));strip.setOpaque(false);strip.add(news);strip.add(button("Newspaper",()->openPage("News")),BorderLayout.EAST);clippings.add(strip,BorderLayout.CENTER);west.add(clippings,BorderLayout.NORTH);
        inspector.add(label("SELECTED STATE",11,MUTED));inspector.add(Box.createVerticalStrut(8));inspector.add(selector);inspector.add(details);selector.addActionListener(e->update.run());update.run();
        JScrollPane info=scroll(inspector);
        JPanel right=new JPanel(new BorderLayout(0,8));right.setOpaque(false);right.setPreferredSize(new Dimension(265,200));
        JPanel preview=new JPanel(new BorderLayout());preview.setBackground(CARD);preview.setBorder(new EmptyBorder(8,10,8,10));preview.setPreferredSize(new Dimension(265,108));
        JLabel hoverLabel=new JLabel("<html>STATE NOTES</html>");hoverLabel.setForeground(INK);hoverLabel.setName("State hover preview");preview.add(hoverLabel);
        map.onHover(name->{if(name==null)return;var state=v.campaign().states().stream().filter(x->x.name().equals(name)).findFirst().orElseThrow();hoverLabel.setText("<html><b>"+state.name()+" · "+state.electoralVotes()+" EV</b><br>"+(state.playerControls()?"Your statewide lead":"Opponent statewide lead")+"<br>Your completed work: "+state.playerTasks().size()+" / "+state.objectives().size()+"<br>Opponent work: "+state.opponentTasks().size()+"</html>");});
        JPanel mapTools=new JPanel(new BorderLayout(0,4));mapTools.setOpaque(false);JButton fundraise=button("Fundraise +$"+v.campaign().fundraisingAmount(),()->controller.submit(CareerCommand.campaign(GameCommand.fundraise())));fundraise.setEnabled(v.phase()==CareerView.Phase.CAMPAIGN&&!controller.busy());mapTools.add(fundraise,BorderLayout.NORTH);mapTools.add(preview,BorderLayout.CENTER);right.add(mapTools,BorderLayout.NORTH);right.add(info,BorderLayout.CENTER);panel.add(west,BorderLayout.CENTER);panel.add(right,BorderLayout.EAST);return panel;
    }
    private void schedule(JPanel p,CareerView v){
        if(v.phase()==CareerView.Phase.CAMPAIGN){p.add(text("Campaign actions advance one turn. Real-time clocks apply only to live decisions."));for(int t:new int[]{4,8,12})p.add(text("DEBATE / Turn "+t+" · "+(v.campaign().turnsUsed()>=t?"Reached":"In "+(t-v.campaign().turnsUsed())+" turns")));p.add(text("ELECTION DAY / After "+v.campaign().totalTurns()+" campaign turns"));action(p,"Rest / advance one campaign turn",CareerCommand.campaign(GameCommand.rest()));}
        if(v.phase()==CareerView.Phase.PRESIDENCY){p.add(button("Presidential actions",()->openPage("PresidentialActions")));p.add(label(v.presidency().date(),20,INK));p.add(text(v.presidency().actionsLeft()+" actions remain. The month ends automatically after your second action."));for(var delivery:v.presidency().implementations())p.add(text("Month "+delivery.dueMonth()+" / "+delivery.title()));for(var d:v.presidency().delayedEffects())p.add(text("Month "+d.dueMonth()+" / "+d.description()));for(var due:v.world().scheduled())p.add(text("World month "+due.dueMonth()+" / "+due.reason()));}
    }
    private JComponent policyPage(CareerView v){
        JPanel root=new JPanel(new BorderLayout(8,8));root.setBackground(CARD);root.setBorder(new EmptyBorder(12,12,12,12));
        JTabbedPane tabs=new JTabbedPane();tabs.setBackground(CARD);tabs.setForeground(INK);JPanel p=column(),billPanel=column(),promises=column();tabs.addTab("Choose initiative",scroll(p));tabs.addTab("Current bill",scroll(billPanel));tabs.addTab("Promises",scroll(promises));if(v.phase()==CareerView.Phase.CAMPAIGN){JPanel prep=column();prep.add(button("Debate settings",this::settings));if(debateWindow.active())prep.add(button("Resume debate",debateWindow::open));tabs.addTab("Debate preparation",prep);}tabs.setSelectedIndex(Math.min(policyTab,tabs.getTabCount()-1));tabs.addChangeListener(e->policyTab=tabs.getSelectedIndex());root.add(tabs,BorderLayout.CENTER);
        if(v.phase()!=CareerView.Phase.CAMPAIGN&&v.phase()!=CareerView.Phase.PRESIDENCY){p.add(text("Set campaign pledges during a campaign, or manage legislation while in office."));return root;}
        
        PaperChoice<Policy.Issue> issue=new PaperChoice<>(Policy.Issue.values());PaperChoice<Policy.Approach> approach=new PaperChoice<>(Policy.Approach.values());friendly(issue);friendly(approach);issue.setSelectedIndex(policyIssue);approach.setSelectedIndex(policyApproach);p.add(issue);p.add(approach);JTextArea preview=text("");p.add(preview);
        Runnable describe=()->{policyIssue=issue.getSelectedIndex();policyApproach=approach.getSelectedIndex();var o=PolicyCatalog.option(issue.getSelectedItem(),approach.getSelectedItem());preview.setText(commitmentNotes(issue.getSelectedItem())+o.title()+"\nBenefit: "+o.benefit()+"\nTradeoff: "+o.tradeoff()+"\nSigning cost: "+o.cost()+" office funds · Delivery: "+o.deliveryMonths()+" months.");};issue.addActionListener(e->describe.run());approach.addActionListener(e->describe.run());describe.run();
        JButton propose=button(v.phase()==CareerView.Phase.CAMPAIGN?"Make campaign pledge":"Introduce bill · 1 action",()->controller.submit(CareerCommand.office(OfficeCommand.policy(v.phase()==CareerView.Phase.CAMPAIGN?OfficeCommand.Type.PLEDGE:OfficeCommand.Type.PROPOSE,issue.getSelectedItem(),approach.getSelectedItem()))));p.add(propose);
        if(v.presidency()!=null){var o=v.presidency();propose.setEnabled(o.bill()==null&&o.actionsLeft()>0);if(o.bill()!=null){var bill=o.bill();billPanel.add(text("Current bill: "+PolicyCatalog.option(bill.issue(),bill.approach()).title()+"\nStage: "+bill.stage()+"\nCommitments: "+o.proceedings().houseSupport()+" House / "+o.proceedings().senateSupport()+" Senate\n"+o.congress().rule()));
            for(var t:new OfficeCommand.Type[]{OfficeCommand.Type.NEGOTIATE,OfficeCommand.Type.COMMITTEE_REVIEW,OfficeCommand.Type.FLOOR_VOTE,OfficeCommand.Type.SIGN,OfficeCommand.Type.VETO,OfficeCommand.Type.WITHDRAW_BILL}){boolean passed=o.proceedings().passed();boolean enabled=o.actionsLeft()>0&&switch(t){case NEGOTIATE->!passed&&v.world().get(WorldMetric.CAPITAL)>=8;case COMMITTEE_REVIEW->!passed&&!o.proceedings().committeeReported();case FLOOR_VOTE->!passed&&o.proceedings().committeeReported()&&o.proceedings().houseSupport()>=218&&o.proceedings().senateSupport()>=60;case SIGN->passed&&o.treasury()>=PolicyCatalog.option(bill.issue(),bill.approach()).cost();case VETO->passed;case WITHDRAW_BILL->!passed;default->false;};JButton action=button(friendlyName(t)+(t==OfficeCommand.Type.NEGOTIATE?" · 8 capital":"")+" · 1 action",()->controller.submit(CareerCommand.office(OfficeCommand.simple(t))));action.setEnabled(enabled);action.setToolTipText(enabled?"Advance the current bill.":"Check the bill stage, commitments and available resources above.");billPanel.add(action);}}
        }
        if(billPanel.getComponentCount()==0)billPanel.add(text("No bill is awaiting action. Choose an initiative to begin while in office."));
        promises.add(text("DEBATE COMMITMENTS — retained through presidency"));for(var statement:controller.debateStatements()){var commitment=LiveDebate.commitment(statement);if(commitment.isEmpty())continue;promises.add(text(statement.debate()+": "+statement.words()));if(v.phase()==CareerView.Phase.PRESIDENCY){var bill=commitment.get();JButton follow=button("Introduce promised initiative: "+PolicyCatalog.option(bill.issue(),bill.approach()).title(),()->controller.submit(CareerCommand.office(OfficeCommand.policy(OfficeCommand.Type.PROPOSE,bill.issue(),bill.approach()))));follow.setEnabled(v.presidency().actionsLeft()>0&&v.presidency().bill()==null);follow.setToolTipText("Uses one normal presidential action. Requires no other active bill.");promises.add(follow);}}promises.add(text("These statements are compared with later signing/veto decisions in History. They do not automatically introduce a bill."));promises.add(text("Your pledges"));for(var promise:v.campaignPromises())promises.add(text(PolicyCatalog.option(promise.issue(),promise.approach()).title()+" — "+promise.status()));
        if(v.campaignPromises().isEmpty())promises.add(text("No campaign pledges recorded yet."));return root;
    }
    private void statistics(JPanel p,CareerView v){
        for(String headline:NewsFeed.headlines(v))p.add(text(headline));p.add(new JSeparator());
        JTabbedPane tabs=new JTabbedPane();JPanel overview=column(),records=column();boolean campaign=v.phase()==CareerView.Phase.CAMPAIGN;
        if(campaign){overview.add(text("Campaign turn "+v.campaign().turnsUsed()+" / "+v.campaign().totalTurns()+"\nAvailable funds: $"+v.campaign().playerFunds()));for(String effect:v.campaign().activeEffects())records.add(text(effect));if(records.getComponentCount()==0)records.add(text("No active campaign effects."));tabs.addTab("Campaign",scroll(overview));tabs.addTab("Active effects",scroll(records));}
        else if(v.phase()==CareerView.Phase.PRESIDENCY){overview.add(text(v.presidency().date()+"\nActions available: "+v.presidency().actionsLeft()+"\nOffice funds: $"+v.treasury()));JPanel economy=column();for(var m:new WorldMetric[]{WorldMetric.CAPITAL,WorldMetric.ENERGY})overview.add(text(m.label+": "+String.format(Locale.ROOT,"%.1f",v.world().get(m))+" "+m.unit));for(var m:new WorldMetric[]{WorldMetric.GROWTH,WorldMetric.UNEMPLOYMENT,WorldMetric.INFLATION})economy.add(text(m.label+": "+String.format(Locale.ROOT,"%.1f",v.world().get(m))+" "+m.unit));tabs.addTab("Office",scroll(overview));tabs.addTab("Economy",scroll(economy));records.add(text("Laws signed: "+v.presidency().lawsPassed()+"\nBills vetoed: "+v.presidency().billsVetoed()));tabs.addTab("Legislative record",scroll(records));}
        else{overview.add(text("Career stage: "+friendlyName(v.phase())+"\nMonths served: "+v.servedMonths()));tabs.addTab("Career",scroll(overview));}p.add(tabs);
    }

    private void history(JPanel p,CareerView v){
        JTabbedPane tabs=new JTabbedPane();var career=new ArrayList<DocumentPages.Page>();var campaign=new ArrayList<DocumentPages.Page>();
        var records=new ArrayList<>(v.history());Collections.reverse(records);for(int i=0;i<records.size();i++)career.add(new DocumentPages.Page("Career record "+(records.size()-i),records.get(i)));
        var campaignRecords=new ArrayList<>(v.campaign().history());Collections.reverse(campaignRecords);for(int i=0;i<campaignRecords.size();i++)campaign.add(new DocumentPages.Page("Campaign record "+(campaignRecords.size()-i),campaignRecords.get(i)));
        tabs.addTab("Career",new DocumentPages(career));tabs.addTab("Campaign",new DocumentPages(campaign));p.add(tabs);
    }

    private void sidebar(CareerView v){
        boolean president=v.phase()==CareerView.Phase.PRESIDENCY;stats.setLayout(new GridLayout(1,president?8:4,4,0));stats.setPreferredSize(new Dimension(0,40));
        if(v.phase()==CareerView.Phase.CAMPAIGN){metric("CAMPAIGN FUNDS",String.format(Locale.US,"$%,d",v.campaign().playerFunds()));metric("YOUR EV / OPPONENT",v.campaign().playerEV()+" / "+v.campaign().opponentEV());metric("CAMPAIGN TURN",v.campaign().turnsUsed()+" / "+v.campaign().totalTurns());metric("POLITICAL CAPITAL",String.format(Locale.ROOT,"%.0f",v.world().get(WorldMetric.CAPITAL)));}
        else{metric("JOB APPROVAL",String.format(Locale.ROOT,"%.1f%%",v.world().approval()));metric("OFFICE FUNDS",String.format(Locale.US,"$%,d",v.treasury()));metric("POLITICAL CAPITAL",String.format(Locale.ROOT,"%.0f",v.world().get(WorldMetric.CAPITAL)));metric("ENERGY",String.format(Locale.ROOT,"%.0f",v.world().get(WorldMetric.ENERGY)));if(president){metric("GDP GROWTH · ANNUALIZED",String.format(Locale.ROOT,"%.1f%%",v.world().get(WorldMetric.GROWTH)));metric("UNEMPLOYMENT",String.format(Locale.ROOT,"%.1f%%",v.world().get(WorldMetric.UNEMPLOYMENT)));metric("INFLATION · YEAR OVER YEAR",String.format(Locale.ROOT,"%.1f%%",v.world().get(WorldMetric.INFLATION)));metric("YOUR CONGRESS SEATS",v.presidency().congress().houseSeats()+"H · "+v.presidency().congress().senateSeats()+"S");}}
    }
    private void metric(String name,String value){boolean resource=true;String old=previousResourceValues.put(name,value);if(resource&&old!=null&&!old.equals(value))resourceChanges.put(name,System.nanoTime());JPanel tile=new JPanel(new BorderLayout(0,1));tile.setBackground(new Color(43,53,45));tile.setToolTipText(name+": "+value);if(resource&&resourceChanges.containsKey(name)){long began=resourceChanges.get(name);long age=System.nanoTime()-began;if(age>=StatPulse.DURATION){resourceChanges.remove(name);}else{tile.setBackground(StatPulse.color(age));javax.swing.Timer fade=new javax.swing.Timer(50,null);fade.addActionListener(e->{long elapsed=System.nanoTime()-began;tile.setBackground(StatPulse.color(elapsed));if(elapsed>=StatPulse.DURATION||!tile.isDisplayable())fade.stop();});fade.start();}}tile.setBorder(new EmptyBorder(3,5,3,5));String shortName=switch(name){case "CAMPAIGN FUNDS","OFFICE FUNDS"->"FUNDS";case "POLITICAL CAPITAL"->"CAPITAL";case "GDP GROWTH · ANNUALIZED"->"GDP GROWTH";case "INFLATION · YEAR OVER YEAR"->"INFLATION";case "YOUR CONGRESS SEATS"->"CONGRESS";case "UNEMPLOYMENT"->"UNEMPLOY.";case "JOB APPROVAL"->"APPROVAL";default->name;};tile.add(label(shortName,9,MUTED),BorderLayout.NORTH);tile.add(label(value,name.equals("YOUR CONGRESS SEATS")?12:14,INK),BorderLayout.CENTER);stats.add(tile);}
    private String commitmentNotes(Policy.Issue issue){StringBuilder note=new StringBuilder();for(var statement:controller.debateStatements()){var commitment=LiveDebate.commitment(statement);if(commitment.isPresent()&&commitment.get().issue()==issue)note.append("ON THE RECORD: ").append(statement.words()).append("\n");}return note.toString();}
    private void decision(CareerView v){
        String key=controller.decisionKey();if(key.isEmpty())return;event.add(label("CURRENT DECISION",11,ACCENT));timer.setFont(new Font(Font.SANS_SERIF,Font.BOLD,12));
        if(v.phase()==CareerView.Phase.CAMPAIGN){var e=v.campaign().pendingEvent();boolean live=e.id().startsWith("debate_live_");boolean correct=e.title().startsWith("CORRECT"),incorrect=e.title().startsWith("INCORRECT");
            String title=correct?(controller.debateSettings().celebrations()?"WELL ANSWERED! — fact verified":"CORRECT — source verified"):e.title();JLabel banner=label(title,22,correct?ACCENT:incorrect?new Color(255,158,146):INK);banner.setName("Debate feedback banner");event.add(banner);
            JTextArea prompt=text(e.description());prompt.setName("Debate prompt");if(live)prompt.setFont(prompt.getFont().deriveFont((float)controller.debateSettings().textSize()));event.add(prompt);
            if(live&&e.title().equals("Debate preparation"))event.add(button("Practice with your team · free",this::practice));
            for(int index:DebatePresentation.order(v.campaign().seed(),e.id(),e.choices().size())){var choice=e.choices().get(index);if(live&&(choice.label().equals("Move to the next question")||choice.label().equals("Finish debate")))continue;JButton b=button(choice.label(),()->controller.respond(key,index));b.setName("Decision option "+index);b.setEnabled(choice.available());if(live)b.setFont(b.getFont().deriveFont((float)controller.debateSettings().textSize()));b.setToolTipText(choice.reason());event.add(b);event.add(Box.createVerticalStrut(8));}
            event.add(text(e.missedResponse()));
        }else{var e=v.world().pending();event.add(label(e.title(),18,INK));event.add(text(e.description()));for(int i=0;i<e.choices().size();i++){int index=i;var c=e.choices().get(i);JButton b=button(c.label()+"  ·  "+c.capitalCost()+" capital",()->controller.respond(key,index));b.setEnabled(v.world().get(WorldMetric.CAPITAL)>=c.capitalCost());b.setToolTipText(c.consequence());event.add(b);}event.add(text(v.world().missedResponse()));}
    }
    void updateTimer(){if(currentOffice!=null)currentOffice.updateClock();debateWindow.updateClock();timer.setForeground(ACCENT);timer.setToolTipText(turnDeadline());timer.setText(controller.busy()?"Saving / loading…":controller.paused()?"":controller.seconds()>0?(controller.realtime()?"Respond: "+controller.seconds()+"s":"Clock off"):controller.decisionKey().isEmpty()?"":"Decision waiting");}
    private String turnDeadline(){if(controller.decisionKey().isEmpty())return "";var v=controller.view();if(v!=null&&v.phase()==CareerView.Phase.CAMPAIGN&&v.campaign().pendingEvent()!=null&&v.campaign().pendingEvent().id().startsWith("debate_live_"))return "UNTIMED  ·  Continue when ready";if(v==null)return "";if(v.phase()==CareerView.Phase.CAMPAIGN&&v.campaign().pendingEvent()!=null)return "DEADLINE  ·  Campaign turn "+v.campaign().pendingEvent().dueTurn();return "DEADLINE  ·  World month "+v.world().dueMonth();}
    private void dialog(Runnable task){controller.pause();try{task.run();}finally{controller.resume();}}
    private void help(){showText("How to play",java.util.List.of(
        "GET STARTED\nYour desk is home. Open the Campaign map to select states, the Fundraising folder for cash, and the Calendar to rest or advance time. Your color identifies your team. Use Desk to return from any workspace.",
        "DECISIONS & DEBATES\nNew events open Decisions automatically. Debates begin with six briefing slots. Answer a factual question, choose your confidence, then respond to an opponent challenge. Corrections cite sources; the opponent may be wrong. Standard knowledge answers last 30 seconds; recovery lasts 20. Menu → Settings offers relaxed or untimed responses and larger text. Team practice is free. Answer positions vary; the order stays stable while you read. Menus and switching away pause the live clock.",
        "LEADING THE COUNTRY\nUse the planner for presidential actions, the cabinet book for appointments, requests and midterms, and the legislation folio for bills and promises. After two actions, the month advances automatically; pending decisions must be resolved first.",
        "LEGISLATION\nPolicy explains each initiative. Introduce a bill, negotiate commitments, complete committee review and floor passage, then sign or veto. Signing pays the displayed delivery cost.",
        "KEEP YOUR PROGRESS\nMenu contains Save / Load, settings and exit. Phase changes autosave; save manually before leaving to preserve your latest actions. Statistics and History show your full record. Latest Activity keeps reports until you dismiss them. The news panel refreshes each turn; ambient headlines are flavor stories."));}
    private void newCareer(){if(controller.busy())return;dialog(()->{
        if(controller.view()!=null&&JOptionPane.showConfirmDialog(this,"Start over? Save first to keep this career.","New career",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        PaperChoice<President.Difficulty>d=new PaperChoice<>(President.Difficulty.values());PaperChoice<President.RunningMate>m=new PaperChoice<>(President.RunningMate.values());friendly(d);friendly(m);
        PaperChoice<DebateSettings.Mode> mode=new PaperChoice<>(DebateSettings.Mode.values());friendly(mode);mode.setSelectedItem(setupSettings.mode());
        PaperChoice<DebateSettings.Pace> pace=new PaperChoice<>(DebateSettings.Pace.values());friendly(pace);pace.setSelectedItem(setupSettings.pace());
        PaperChoice<TeamColors> color=new PaperChoice<>(TeamColors.values());color.setSelectedItem(setupColor);friendly(color);
        Object[] fields={"Your campaign color (opponent receives a distinct random color)",color,"Difficulty",d,"Running mate",m,"Debate questions: Politics = economics/civics; Fun = trivia/pop culture",mode,"Pace: Standard 30s answers / 20s recovery; Relaxed doubles; Untimed",pace,"A fresh random world is generated for every new career."};
        if(JOptionPane.showConfirmDialog(this,dialogForm(fields),"New career — choose your debate mode",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION){setupSettings=new DebateSettings(mode.getSelectedItem(),pace.getSelectedItem(),setupSettings.textSize(),setupSettings.celebrations());setupColor=color.getSelectedItem();page="Desk";openedItem=null;long seed=nextDeveloperSeed==null?GameSeeds.fresh():nextDeveloperSeed;nextDeveloperSeed=null;controller.start(seed,d.getSelectedItem(),m.getSelectedItem(),setupSettings);controller.submit(CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.CAMPAIGN_COLOR,setupColor.ordinal())));controller.submit(CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.REALTIME_MODE,setupRealtime?1:0)));controller.submit(CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.EVENT_FREQUENCY,setupFrequency)));}

    });}
    private void practice(){practice(null);}
    private void practice(DebateSettings.Mode requestedMode){if(controller.busy())return;dialog(()->{
        var base=controller.view()==null?setupSettings:controller.debateSettings();JCheckBox timed=new JCheckBox("Use timed responses during practice",false);PaperChoice<DebateSettings.Mode> mode=new PaperChoice<>(DebateSettings.Mode.values());friendly(mode);mode.setSelectedItem(requestedMode==null?base.mode():requestedMode);
        if(JOptionPane.showConfirmDialog(debateWindow.parent(),new Object[]{"A complete rehearsal with your team. Free: no cash, campaign turns or public promises.","Question bank",mode,timed},"Team practice",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        var options=new DebateSettings(mode.getSelectedItem(),timed.isSelected()?base.pace()==DebateSettings.Pace.UNTIMED?DebateSettings.Pace.STANDARD:base.pace():DebateSettings.Pace.UNTIMED,base.textSize(),base.celebrations());
        PracticeDebatePanel practice=new PracticeDebatePanel(0,options);practice.setPreferredSize(new Dimension(Math.max(440,Math.min(850,getWidth()-100)),Math.max(280,Math.min(530,getHeight()-140))));practice.startClock();try{JOptionPane.showMessageDialog(debateWindow.parent(),practice,"Team rehearsal — campaign paused",JOptionPane.PLAIN_MESSAGE);}finally{practice.stopClock();}
    });}
    private void saveMenu(){if(controller.busy())return;dialog(()->{String[] choices={"Load autosave","Save slot 1","Save slot 2","Save slot 3","Load slot 1","Load slot 2","Load slot 3","Quicksave","Load quicksave","Open save file"};PaperChoice<String> slots=new PaperChoice<>(choices);Object selected=JOptionPane.showConfirmDialog(this,dialogForm(new Object[]{"Save / Load",slots}),"Save / Load",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION?slots.getSelectedItem():null;if(selected==null)return;String s=selected.toString();if(s.equals("Open save file")){JFileChooser f=new JFileChooser(saves.toFile());if(f.showOpenDialog(this)==JFileChooser.APPROVE_OPTION)controller.load(f.getSelectedFile().toPath());return;}Path path=saves.resolve(s.equals("Load autosave")?"autosave.properties":s.toLowerCase().contains("quicksave")?"quicksave.properties":"slot-"+s.charAt(s.length()-1)+".properties");if(s.startsWith("Load")){if(JOptionPane.showConfirmDialog(this,"Replace the current career with this save?","Load",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION)controller.load(path);}else if(controller.view()!=null){if(!Files.exists(path)||JOptionPane.showConfirmDialog(this,"Replace this slot? Previous versions are kept as backups.","Save",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION)controller.save(path,()->{});}});}
    private void settings(){if(controller.busy())return;dialog(()->{
        var current=controller.view()==null?setupSettings:controller.debateSettings();PaperChoice<DebateSettings.Mode> mode=new PaperChoice<>(DebateSettings.Mode.values());mode.setSelectedItem(current.mode());friendly(mode);PaperChoice<DebateSettings.Pace> pace=new PaperChoice<>(DebateSettings.Pace.values());pace.setSelectedItem(current.pace());friendly(pace);
        JSpinner size=new JSpinner(new SpinnerNumberModel(current.textSize(),16,24,1));JCheckBox feedback=new JCheckBox("Celebrate correct answers",current.celebrations());JCheckBox clocks=new JCheckBox("Enable all live response clocks",controller.view()==null?setupRealtime:controller.realtime());
        PaperChoice<String> frequency=new PaperChoice<>(new String[]{"Off","Normal","Frequent"});frequency.setSelectedIndex(controller.view()==null?setupFrequency:controller.view().eventFrequency());
        JButton rehearsal=button("Practice selected mode · fixed tutorial",()->practice(mode.getSelectedItem()));
        PaperChoice<TeamColors> colors=new PaperChoice<>(TeamColors.values());colors.setSelectedItem(controller.view()==null?setupColor:playerColor());friendly(colors);
        JCheckBox fullscreen=new JCheckBox("Fullscreen",DisplayMode.fullscreen());fullscreen.setEnabled(DisplayMode.supported(this));fullscreen.setToolTipText("Available when the display supports native fullscreen.");Object[] controls={fullscreen,"CAMPAIGN COLOR",colors,"DEBATE CONTENT",mode,rehearsal,"Politics: "+DebateFactChecks.all().size()+" questions · Fun: "+DebateFactChecks.fun().size()+" questions","Mode and pace changes apply to the next debate.","RESPONSE PACE",pace,"Standard: 30s answer / 20s recovery. Relaxed doubles these.",clocks,"READABILITY — debate font size",size,feedback,"PRESIDENCY EVENT FREQUENCY",frequency,"Pending events are kept. Settings are saved with your career."};
        if(JOptionPane.showConfirmDialog(this,dialogForm(controls),"Settings",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION){DisplayMode.set(this,fullscreen.isSelected());setupSettings=new DebateSettings(mode.getSelectedItem(),pace.getSelectedItem(),(Integer)size.getValue(),feedback.isSelected());setupColor=colors.getSelectedItem();setupRealtime=clocks.isSelected();setupFrequency=frequency.getSelectedIndex();if(controller.view()!=null){controller.applyDebateSettings(setupSettings);controller.submit(CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.CAMPAIGN_COLOR,setupColor.ordinal())));controller.submit(CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.REALTIME_MODE,clocks.isSelected()?1:0)));controller.submit(CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.EVENT_FREQUENCY,frequency.getSelectedIndex())));}render();}
    });}
    private void developer(){if(controller.busy())return;dialog(()->{
        JPasswordField password=new JPasswordField();if(JOptionPane.showConfirmDialog(this,password,"Developer password",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        if(!DeveloperAccess.accepts(password.getPassword())){JOptionPane.showMessageDialog(this,"Incorrect password.");return;}
        java.util.List<String> choices=new ArrayList<>();choices.add("Set seed for next new career");choices.add("Use random seeds / clear override");if(controller.view()!=null)for(var a:CareerCommand.DeveloperAction.values())choices.add(a.name());
        PaperChoice<String> tools=new PaperChoice<>(choices.toArray(String[]::new));Object selected=JOptionPane.showConfirmDialog(this,dialogForm(new Object[]{"Developer tools",tools}),"Developer tools",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION?tools.getSelectedItem():null;if(selected==null)return;
        String x=selected.toString();if(x.equals(choices.get(0))){String value=JOptionPane.showInputDialog(this,"Enter a signed 64-bit integer for the next new career:");if(value!=null)try{nextDeveloperSeed=Long.parseLong(value.strip());}catch(NumberFormatException e){JOptionPane.showMessageDialog(this,"Enter a whole number between -9223372036854775808 and 9223372036854775807.");}}
        else if(x.equals(choices.get(1)))nextDeveloperSeed=null;else controller.submit(CareerCommand.dev(CareerCommand.DeveloperAction.valueOf(x)));
    });}

    public void requestClose(Runnable close){if(controller.busy())return;if(controller.view()==null){close.run();return;}dialog(()->{int x=JOptionPane.showOptionDialog(this,"Save before leaving?","Exit",JOptionPane.DEFAULT_OPTION,JOptionPane.QUESTION_MESSAGE,null,new String[]{"Save and exit","Exit without saving","Cancel"},"Save and exit");if(x==0)controller.save(saves.resolve("quicksave.properties"),close);else if(x==1)close.run();});}
}
