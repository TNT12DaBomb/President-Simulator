import java.awt.*;
import java.nio.file.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Persistent desktop shell. Reads snapshots and submits typed commands only. */
public final class DesktopPanel extends JPanel {
    private static final long serialVersionUID=1L;
    static final Color BG=new Color(15,23,36), CARD=new Color(24,36,52), INK=new Color(232,239,245), MUTED=new Color(155,176,194), ACCENT=new Color(91,216,187);
    final DesktopController controller;
    private final JPanel content=new JPanel(new BorderLayout(10,10)), stats=new JPanel(new GridLayout(1,0,8,0)), event=column();
    private final JPanel alert=new JPanel(new BorderLayout(8,0));
    private final JLabel heading=new JLabel(), timer=new JLabel(), status=new JLabel();
    private final JTextArea alertText=text("");
    private final javax.swing.Timer toastTimer=new javax.swing.Timer(6500,e->hideNotice());
    private DesktopController.Notice currentNotice;
    private String page="Dashboard", selectedState="Pennsylvania", shownDecision="";
    private ElectoralMap map;
    private final OfficeWorkspace.Selection officeSelection=new OfficeWorkspace.Selection();
    private CareerView.Phase lastPhase;
    private int policyTab,policyIssue,policyApproach;
    private final Path saves=Path.of(System.getProperty("user.home"),"PresidentialSimulator","saves");
    public DesktopPanel(DesktopController controller) {
        this.controller=controller;setLayout(new BorderLayout(10,10));setBackground(BG);setBorder(new EmptyBorder(10,12,10,12));
        JPanel north=new JPanel(new BorderLayout(8,8));north.setOpaque(false);
        JPanel brand=new JPanel(new BorderLayout());brand.setOpaque(false);brand.add(label("PRESIDENTIAL SIMULATOR",18,INK),BorderLayout.WEST);heading.setForeground(ACCENT);brand.add(heading,BorderLayout.EAST);north.add(brand,BorderLayout.NORTH);
        stats.setOpaque(false);north.add(stats,BorderLayout.CENTER);
        alert.setBorder(new EmptyBorder(7,12,7,7));alertText.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,13));alertText.setBorder(null);JScrollPane notificationText=new JScrollPane(alertText);notificationText.setBorder(null);notificationText.setOpaque(false);notificationText.getViewport().setOpaque(false);notificationText.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);notificationText.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);alert.add(notificationText,BorderLayout.CENTER);
        JPanel buttons=new JPanel(new FlowLayout(FlowLayout.RIGHT,4,0));buttons.setOpaque(false);buttons.add(button("Details",()->showReport(currentNotice==null?controller.messages:java.util.List.of(currentNotice.detail()))));buttons.add(button("Dismiss",this::hideNotice));alert.add(buttons,BorderLayout.EAST);alert.setVisible(false);JPanel notificationSlot=new JPanel(new BorderLayout());notificationSlot.setOpaque(false);notificationSlot.setPreferredSize(new Dimension(0,56));notificationSlot.setName("Notification slot");notificationSlot.add(alert);north.add(notificationSlot,BorderLayout.SOUTH);add(north,BorderLayout.NORTH);toastTimer.setRepeats(false);
        JPanel nav=column();nav.setBorder(new EmptyBorder(10,8,10,8));
        for(String name:new String[]{"Dashboard","Map","Office","Decisions","Schedule","Policy","Statistics","History"}){JButton item=button(name,()->{page=name;render();});item.setMargin(new Insets(4,8,4,8));nav.add(item);nav.add(Box.createVerticalStrut(3));}
        nav.add(Box.createVerticalStrut(8));nav.add(button("Menu",this::menu));nav.add(Box.createVerticalStrut(5));nav.add(button("How to play",this::help));
        JScrollPane navigation=scroll(nav);navigation.setPreferredSize(new Dimension(135,100));add(navigation,BorderLayout.WEST);content.setOpaque(false);add(content,BorderLayout.CENTER);
        status.setForeground(MUTED);status.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,11));add(status,BorderLayout.SOUTH);
        controller.changed=this::render;controller.timerChanged=this::updateTimer;controller.notified=this::notice;render();
    }
    private void menu(){JPopupMenu menu=new JPopupMenu();String[] names={"Latest report","Save / Load","New career","Settings","Developer","Exit"};Runnable[] actions={()->showReport(controller.messages),this::saveMenu,this::newCareer,this::settings,this::developer,()->{Window w=SwingUtilities.getWindowAncestor(this);if(w!=null)requestClose(w::dispose);}};for(int i=0;i<names.length;i++){JMenuItem item=new JMenuItem(names[i]);Runnable action=actions[i];item.addActionListener(e->action.run());menu.add(item);}controller.pause();menu.addPopupMenuListener(new javax.swing.event.PopupMenuListener(){public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e){}public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e){controller.resume();}public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e){}});menu.show(this,12,Math.min(420,getHeight()-200));}
    private void showReport(java.util.List<String> lines){showText("Update details",lines);}
    private void showText(String title,java.util.List<String> lines){dialog(()->{JTextArea report=text(String.join("\n\n",lines));report.setForeground(Color.DARK_GRAY);JScrollPane area=new JScrollPane(report);area.setPreferredSize(new Dimension(Math.min(650,Math.max(320,getWidth()-100)),Math.min(380,Math.max(180,getHeight()-180))));JOptionPane.showMessageDialog(this,area,title,JOptionPane.INFORMATION_MESSAGE);});}
    public void notice(DesktopController.Notice n){
        if(currentNotice!=null&&currentNotice.title().equals("Action unavailable")&&!n.important()&&!n.title().equals("Action completed"))return;
        currentNotice=n;toastTimer.stop();alert.setBackground(n.important()?new Color(81,51,43):new Color(26,67,67));
        // Show complete first two report lines here; the entire report is in Details.
        String[] lines=n.detail().split("\\R");String summary=String.join(" ",Arrays.copyOf(lines,Math.min(2,lines.length)));
        if(summary.length()>240&&!n.important())summary=lines[0];
        alertText.setText(n.title()+" — "+summary);alert.setVisible(true);alertText.setRows(2);revalidate();repaint();if(!n.important()&&isShowing())toastTimer.restart();
    }
    private void hideNotice(){toastTimer.stop();currentNotice=null;alert.setVisible(false);revalidate();repaint();}
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
    static void friendly(JComboBox<?> combo){combo.setRenderer(new DefaultListCellRenderer(){private static final long serialVersionUID=1L;public Component getListCellRendererComponent(JList<?> l,Object v,int i,boolean selected,boolean focus){return super.getListCellRendererComponent(l,v==null?"":friendlyName(v),i,selected,focus);}});combo.setMaximumSize(new Dimension(Integer.MAX_VALUE,32));}
    private static final class WrappingButton extends JButton {
        private static final long serialVersionUID=1L;
        WrappingButton(String label){super(label);}
        @Override public Dimension getPreferredSize(){Dimension d=super.getPreferredSize();String name=getText().replaceAll("<[^>]*>","");if(name.length()>80)return new Dimension(Math.min(600,d.width),Math.max(80,d.height));return d;}
        @Override public Dimension getMaximumSize(){return new Dimension(Integer.MAX_VALUE,getPreferredSize().height);}
        @Override public Dimension getMinimumSize(){return new Dimension(20,getPreferredSize().height);}
    }
    static JButton button(String name,Runnable action){JButton b=new WrappingButton("<html><div style='text-align:center'>"+name.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")+"</div></html>");b.setName(name);b.setAlignmentX(LEFT_ALIGNMENT);b.setMaximumSize(new Dimension(Integer.MAX_VALUE,42));b.setBackground(new Color(39,57,76));b.setForeground(INK);b.setFocusPainted(true);b.setMargin(new Insets(7,9,7,9));b.addActionListener(e->action.run());b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));return b;}
    private void action(JPanel p,String name,CareerCommand cmd){p.add(button(name,()->controller.submit(cmd)));}
    private JScrollPane scroll(JComponent p){JScrollPane s=new JScrollPane(p);s.setBorder(null);s.getViewport().setBackground(CARD);s.getVerticalScrollBar().setUnitIncrement(18);return s;}
    public void render(){
        CareerView v=controller.view();content.setName("Main workspace");content.removeAll();stats.removeAll();event.removeAll();
        if(v!=null&&lastPhase!=null&&lastPhase!=v.phase())page="Dashboard";lastPhase=v==null?null:v.phase();
        String key=controller.decisionKey();if(!key.isEmpty()&&!key.equals(shownDecision))page="Decisions";if(key.isEmpty()&&page.equals("Decisions")&&!shownDecision.isEmpty())page="Dashboard";shownDecision=key;
        heading.setText(v==null?"v0.5  /  DESKTOP":"Year "+v.year()+"  /  "+v.phase().name().replace('_',' ')+(v.developerUsed()?"  /  DEV":""));
        JPanel body=column();body.add(label(page,22,INK));body.add(Box.createVerticalStrut(8));
        if(v==null){body.add(text("Build a campaign. Earn a mandate. Lead the country.\n\nStart a career or load a save to continue."));body.add(button("Start a new career",this::newCareer));body.add(button("Open a save",this::saveMenu));content.add(scroll(body));}
        else if(page.equals("Policy")){content.add(policyPage(v));}
        else if(page.equals("Office")){content.add(new OfficeWorkspace(controller,officeSelection));}
        else if(page.equals("Map")){content.add(mapPage(v));}
        else if(page.equals("Decisions")){decision(v);if(event.getComponentCount()==0)event.add(text("No decision is waiting. Continue your campaign or advance a month in office."));content.add(scroll(event));}
        else{switch(page){case "Schedule"->schedule(body,v);case "Statistics"->statistics(body,v);case "History"->history(body,v);default->dashboard(body,v);}content.add(scroll(body));}
        if(v!=null)sidebar(v);else stats.add(label("CAMPAIGN  →  ELECTION  →  PRESIDENCY",12,MUTED));
        status.setText(controller.busy()?"Saving / loading… controls and clocks paused.":v==null?"Offline desktop simulation · Java 17+":"Seed "+v.campaign().seed()+"  ·  "+(controller.decisionKey().isEmpty()?"No pending decision":"Decision waiting — open Decisions")+"  ·  Menu for saves and settings");
        setEnabledTree(content,!controller.busy());updateTimer();revalidate();repaint();
    }
    private static void setEnabledTree(Container p,boolean enabled){if(!enabled)for(Component c:p.getComponents()){c.setEnabled(false);if(c instanceof Container child)setEnabledTree(child,false);}}
    private void dashboard(JPanel p,CareerView v){
        p.add(text(switch(v.phase()){case CAMPAIGN->"Campaign turn "+v.campaign().turnsUsed()+" of "+v.campaign().totalTurns()+". Complete state objectives to build your electoral coalition.";case PRESIDENCY->v.presidency().date()+"  •  "+v.presidency().actionsLeft()+" actions remaining this month";default->"Your career continues. Review the outcome and choose your next step.";}));
        switch(v.phase()){
            case CAMPAIGN->{p.add(button("Open electoral map",()->{page="Map";render();}));action(p,"Fundraise (+"+v.campaign().fundraisingAmount()+" campaign funds)",CareerCommand.campaign(GameCommand.fundraise()));action(p,"Rest / advance campaign",CareerCommand.campaign(GameCommand.rest()));}
            case ELECTION_REVIEW->{var e=v.elections().get(v.elections().size()-1);p.add(label(e.outcome(),22,ACCENT));p.add(text("Electoral votes: "+e.playerEV()+" / opponent "+e.opponentEV()+"\n"+String.join("\n",e.consequences())));action(p,"Continue",CareerCommand.advance());}
            case TRANSITION->{p.add(text(String.join("\n",v.transitionResources())));action(p,"Begin presidency",CareerCommand.advance());}
            case PRESIDENCY->{
                p.add(button("Open Office — cabinet, requests and midterms",()->{page="Office";render();}));p.add(Box.createVerticalStrut(8));String[] names=v.governanceOptions().stream().map(a->a.action()+" · "+a.cost()+" office funds").toArray(String[]::new);
                JComboBox<String> options=new JComboBox<>(names);options.setMaximumSize(new Dimension(Integer.MAX_VALUE,36));options.setAlignmentX(LEFT_ALIGNMENT);p.add(options);
                JTextArea explanation=text("");p.add(explanation);
                JButton take=button("Take presidential action",()->controller.submit(CareerCommand.govern(v.governanceOptions().get(options.getSelectedIndex()).action())));p.add(take);
                Runnable select=()->{var a=v.governanceOptions().get(options.getSelectedIndex());explanation.setText(a.available()?WorldSimulation.actionHelp(a.action()):a.reason());take.setEnabled(a.available());};options.addActionListener(e->select.run());select.run();
                p.add(Box.createVerticalStrut(12));action(p,"End month",CareerCommand.office(OfficeCommand.simple(OfficeCommand.Type.END_MONTH)));
            }
            case TERM_REVIEW->{action(p,"Run again",CareerCommand.runAgain());action(p,"Retire",CareerCommand.retire());}
            case OPPOSITION->{p.add(text("Rebuild year "+v.comebackYears()+" of 4. Losses carry forward into future campaigns."));for(var a:CareerCommand.RebuildAction.values())action(p,a.toString(),CareerCommand.rebuild(a));action(p,"Launch comeback campaign",CareerCommand.runAgain());action(p,"Retire",CareerCommand.retire());}
            case RETIRED->{p.add(text("Career complete. Your elections and record remain available in History and Statistics."));p.add(button("Start another career",this::newCareer));}
        }
        p.add(Box.createVerticalStrut(18));p.add(new JSeparator());p.add(label("LATEST REPORT",12,ACCENT));p.add(text(controller.messages.isEmpty()?"No recent update.":controller.messages.get(0)));p.add(button("View full update",()->showReport(controller.messages)));
    }
    private JComponent mapPage(CareerView v){
        JPanel panel=new JPanel(new BorderLayout(10,10));panel.setOpaque(false);
        JPanel west=new JPanel(new BorderLayout(4,4));west.setOpaque(false);
        JPanel title=new JPanel(new BorderLayout());title.setOpaque(false);title.add(label("Electoral map",22,INK));west.add(title,BorderLayout.NORTH);
        JPanel inspector=column();inspector.setBorder(new EmptyBorder(10,10,10,10));
        JComboBox<String> selector=new JComboBox<>(v.campaign().states().stream().map(GameView.StateView::name).sorted().toArray(String[]::new));selector.setName("State selector");selector.setMaximumRowCount(12);selector.setSelectedItem(selectedState);selector.setAlignmentX(LEFT_ALIGNMENT);selector.setMaximumSize(new Dimension(Integer.MAX_VALUE,32));
        JPanel details=column();details.setBorder(new EmptyBorder(8,0,0,0));
        Runnable update=()->{selectedState=(String)selector.getSelectedItem();map.select(selectedState);details.removeAll();var s=v.campaign().states().stream().filter(x->x.name().equals(selectedState)).findFirst().orElseThrow();details.add(label(s.name()+" · "+s.electoralVotes()+" EV",17,ACCENT));details.add(text((s.playerControls()?"Your statewide lead":"Opponent statewide lead")+"\nCompleted: "+s.playerTasks().size()+" of "+s.objectives().size()+" objectives"));
            for(var a:s.actions()){String reason=v.phase()!=CareerView.Phase.CAMPAIGN?"Campaign actions are available during a campaign.":!controller.decisionKey().isEmpty()?"Respond to the current decision first.":!a.available()?a.reason():"Costs "+a.cost()+" funds and one campaign turn.";JButton b=button(a.task()+" · "+a.cost()+" funds",()->controller.submit(CareerCommand.campaign(GameCommand.campaign(s.name(),a.task()))));b.setName("State action "+a.task().name());b.setEnabled(v.phase()==CareerView.Phase.CAMPAIGN&&controller.decisionKey().isEmpty()&&a.available()&&!controller.busy());b.setToolTipText(reason);details.add(b);JTextArea hint=text(reason);hint.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,12));details.add(hint);}
            details.add(button("State details",()->showReport(java.util.List.of(s.explanation(),"Objectives: "+s.objectives(),"Your completed work: "+s.playerTasks(),"Opponent completed work: "+s.opponentTasks()))));details.revalidate();details.repaint();};
        map=new ElectoralMap(v.campaign(),selectedState,name->selector.setSelectedItem(name));map.setName("Electoral map");west.add(map,BorderLayout.CENTER);
        JTextArea legend=text("Blue: your statewide lead · Red: opponent · Gold: selected\nMaine / Nebraska can split EV; see State details.");legend.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,12));west.add(legend,BorderLayout.SOUTH);
        inspector.add(label("SELECTED STATE",11,MUTED));inspector.add(Box.createVerticalStrut(8));inspector.add(selector);inspector.add(details);selector.addActionListener(e->update.run());update.run();
        JScrollPane info=scroll(inspector);
        JPanel right=new JPanel(new BorderLayout(0,8));right.setOpaque(false);right.setPreferredSize(new Dimension(265,200));
        JPanel preview=new JPanel(new BorderLayout());preview.setBackground(CARD);preview.setBorder(new EmptyBorder(8,10,8,10));preview.setPreferredSize(new Dimension(265,108));
        JLabel hoverLabel=new JLabel("<html>HOVER PREVIEW<br>Move over a state or eastern callout.</html>");hoverLabel.setForeground(INK);hoverLabel.setName("State hover preview");preview.add(hoverLabel);
        map.onHover(name->{if(name==null)return;var state=v.campaign().states().stream().filter(x->x.name().equals(name)).findFirst().orElseThrow();hoverLabel.setText("<html><b>"+state.name()+" · "+state.electoralVotes()+" EV</b><br>"+(state.playerControls()?"Your statewide lead":"Opponent statewide lead")+"<br>Your completed work: "+state.playerTasks().size()+" / "+state.objectives().size()+"<br>Opponent work: "+state.opponentTasks().size()+"<br>Click to select and take an action.</html>");});
        right.add(preview,BorderLayout.NORTH);right.add(info,BorderLayout.CENTER);panel.add(west,BorderLayout.CENTER);panel.add(right,BorderLayout.EAST);return panel;
    }
    private void schedule(JPanel p,CareerView v){p.add(text("Campaign time advances through actions. Only live response windows use a real-time countdown."));for(int t:new int[]{4,8,12})p.add(text("DEBATE  /  Turn "+t+"  ·  "+(v.campaign().turnsUsed()>=t?"Reached":"In "+(t-v.campaign().turnsUsed())+" turns")));p.add(text("ELECTION DAY  /  After "+v.campaign().totalTurns()+" campaign turns"));if(v.presidency()!=null)p.add(text("OFFICE  /  "+v.presidency().date()+"\nTerm progress: "+v.termMonths()+" of 48 months\n"+String.join("\n",v.presidency().delayedEffects().stream().map(d->"Month "+d.dueMonth()+": "+d.description()).toList())));for(var s:v.world().scheduled())p.add(text("World month "+s.dueMonth()+"  /  "+s.reason()));}
    private JComponent policyPage(CareerView v){
        JPanel root=new JPanel(new BorderLayout(8,8));root.setBackground(CARD);root.setBorder(new EmptyBorder(12,12,12,12));root.add(label("Policy & legislation",22,INK),BorderLayout.NORTH);
        JTabbedPane tabs=new JTabbedPane();tabs.setBackground(CARD);tabs.setForeground(INK);JPanel p=column(),billPanel=column(),promises=column();tabs.addTab("Choose initiative",scroll(p));tabs.addTab("Current bill",scroll(billPanel));tabs.addTab("Promises",scroll(promises));tabs.setSelectedIndex(policyTab);tabs.addChangeListener(e->policyTab=tabs.getSelectedIndex());root.add(tabs,BorderLayout.CENTER);
        if(v.phase()!=CareerView.Phase.CAMPAIGN&&v.phase()!=CareerView.Phase.PRESIDENCY){p.add(text("Set campaign pledges during a campaign, or manage legislation while in office."));return root;}
        p.add(text("Choose an initiative and review its benefit and tradeoff before committing. A pledge is a promise; signing a passed bill funds its delivery."));
        JComboBox<Policy.Issue> issue=new JComboBox<>(Policy.Issue.values());JComboBox<Policy.Approach> approach=new JComboBox<>(Policy.Approach.values());friendly(issue);friendly(approach);issue.setSelectedIndex(policyIssue);approach.setSelectedIndex(policyApproach);p.add(issue);p.add(approach);JTextArea preview=text("");p.add(preview);
        Runnable describe=()->{policyIssue=issue.getSelectedIndex();policyApproach=approach.getSelectedIndex();var o=PolicyCatalog.option((Policy.Issue)issue.getSelectedItem(),(Policy.Approach)approach.getSelectedItem());preview.setText(o.title()+"\nBenefit: "+o.benefit()+"\nTradeoff: "+o.tradeoff()+"\nSigning cost: "+o.cost()+" office funds · Delivery: "+o.deliveryMonths()+" months.");};issue.addActionListener(e->describe.run());approach.addActionListener(e->describe.run());describe.run();
        JButton propose=button(v.phase()==CareerView.Phase.CAMPAIGN?"Make campaign pledge":"Introduce bill · 1 action",()->controller.submit(CareerCommand.office(OfficeCommand.policy(v.phase()==CareerView.Phase.CAMPAIGN?OfficeCommand.Type.PLEDGE:OfficeCommand.Type.PROPOSE,(Policy.Issue)issue.getSelectedItem(),(Policy.Approach)approach.getSelectedItem()))));p.add(propose);
        if(v.presidency()!=null){var o=v.presidency();propose.setEnabled(o.bill()==null&&o.actionsLeft()>0);if(o.bill()!=null){var bill=o.bill();billPanel.add(text("Current bill: "+PolicyCatalog.option(bill.issue(),bill.approach()).title()+"\nStage: "+bill.stage()+"\nCommitments: "+o.proceedings().houseSupport()+" House / "+o.proceedings().senateSupport()+" Senate\n"+o.congress().rule()));
            for(var t:new OfficeCommand.Type[]{OfficeCommand.Type.NEGOTIATE,OfficeCommand.Type.COMMITTEE_REVIEW,OfficeCommand.Type.FLOOR_VOTE,OfficeCommand.Type.SIGN,OfficeCommand.Type.VETO,OfficeCommand.Type.WITHDRAW_BILL}){boolean passed=o.proceedings().passed();boolean enabled=o.actionsLeft()>0&&switch(t){case NEGOTIATE->!passed&&v.world().get(WorldMetric.CAPITAL)>=8;case COMMITTEE_REVIEW->!passed&&!o.proceedings().committeeReported();case FLOOR_VOTE->!passed&&o.proceedings().committeeReported()&&o.proceedings().houseSupport()>=218&&o.proceedings().senateSupport()>=60;case SIGN->passed&&o.treasury()>=PolicyCatalog.option(bill.issue(),bill.approach()).cost();case VETO->passed;case WITHDRAW_BILL->!passed;default->false;};JButton action=button(friendlyName(t)+(t==OfficeCommand.Type.NEGOTIATE?" · 8 capital":"")+" · 1 action",()->controller.submit(CareerCommand.office(OfficeCommand.simple(t))));action.setEnabled(enabled);action.setToolTipText(enabled?"Advance the current bill.":"Check the bill stage, commitments and available resources above.");billPanel.add(action);}}
        }
        if(billPanel.getComponentCount()==0)billPanel.add(text("No bill is awaiting action. Choose an initiative to begin while in office."));
        promises.add(text("Your pledges"));for(var promise:v.campaignPromises())promises.add(text(PolicyCatalog.option(promise.issue(),promise.approach()).title()+" — "+promise.status()));
        if(v.campaignPromises().isEmpty())promises.add(text("No campaign pledges recorded yet."));return root;
    }
    private void statistics(JPanel p,CareerView v){p.add(text("Fictional simulation metrics. Office funds are an administrative resource, not the federal budget. Approval measures job performance; popularity is personal favorability."));for(var m:WorldMetric.values())p.add(text(String.format(Locale.ROOT,"%s    %.2f %s",m.label,v.world().get(m),m.unit)));p.add(text("Approval high / low: "+String.format(Locale.ROOT,"%.1f / %.1f",v.world().high(),v.world().low())+"\nMonths served: "+v.servedMonths()+"\nElections won / lost: "+v.electionsWon()+" / "+v.electionsLost()));}
    private void history(JPanel p,CareerView v){for(var e:v.elections())p.add(text(e.year()+"  /  "+e.outcome()+"  /  EV "+e.playerEV()+"–"+e.opponentEV()));var lines=new ArrayList<>(v.history());Collections.reverse(lines);p.add(text(String.join("\n\n",lines)));p.add(text("Campaign record\n"+String.join("\n",v.campaign().history())));}
    private void sidebar(CareerView v){
        boolean president=v.phase()==CareerView.Phase.PRESIDENCY;stats.setLayout(new GridLayout(president?2:1,4,8,4));stats.setPreferredSize(new Dimension(0,president?100:50));
        if(v.phase()==CareerView.Phase.CAMPAIGN){metric("CAMPAIGN FUNDS",Integer.toString(v.campaign().playerFunds()));metric("YOUR EV / OPPONENT",v.campaign().playerEV()+" / "+v.campaign().opponentEV());metric("CAMPAIGN TURN",v.campaign().turnsUsed()+" / "+v.campaign().totalTurns());metric("POLITICAL CAPITAL",String.format(Locale.ROOT,"%.0f",v.world().get(WorldMetric.CAPITAL)));}
        else{metric("JOB APPROVAL",String.format(Locale.ROOT,"%.1f%%",v.world().approval()));metric("OFFICE FUNDS",Integer.toString(v.treasury()));metric("POLITICAL CAPITAL",String.format(Locale.ROOT,"%.0f",v.world().get(WorldMetric.CAPITAL)));metric("ENERGY",String.format(Locale.ROOT,"%.0f",v.world().get(WorldMetric.ENERGY)));if(president){metric("GDP GROWTH · ANNUALIZED",String.format(Locale.ROOT,"%.1f%%",v.world().get(WorldMetric.GROWTH)));metric("UNEMPLOYMENT",String.format(Locale.ROOT,"%.1f%%",v.world().get(WorldMetric.UNEMPLOYMENT)));metric("INFLATION · YEAR OVER YEAR",String.format(Locale.ROOT,"%.1f%%",v.world().get(WorldMetric.INFLATION)));metric("YOUR CONGRESS SEATS",v.presidency().congress().houseSeats()+" H / "+v.presidency().congress().senateSeats()+" S");}}
    }
    private void metric(String name,String value){JPanel tile=column();tile.setBorder(new EmptyBorder(7,10,7,10));tile.add(label(name,10,MUTED));tile.add(label(value,20,INK));stats.add(tile);}
    private void decision(CareerView v){
        String key=controller.decisionKey();if(key.isEmpty())return;event.add(label("CURRENT DECISION",11,ACCENT));event.add(timer);if(v.phase()==CareerView.Phase.CAMPAIGN){var e=v.campaign().pendingEvent();event.add(label(e.title(),18,INK));event.add(text(e.description()));if(DebateSession.handles(e.id()))event.add(button("Fact check / rebuttal practice",()->dialog(()->{FactCheckPanel practice=new FactCheckPanel(v.campaign().seed(),e.id().hashCode());practice.setPreferredSize(new Dimension(Math.min(700,getWidth()-80),Math.min(440,getHeight()-130)));JOptionPane.showMessageDialog(this,practice,"Moderator practice — clock paused",JOptionPane.PLAIN_MESSAGE);})));for(int index:DebatePresentation.order(v.campaign().seed(),e.id(),e.choices().size())){var c=e.choices().get(index);JButton b=button(c.label()+(DebateSession.handles(e.id())?(index==0?" — preparation: 50 funds":" — supports later fundraising"):""),()->controller.respond(key,index));b.setName("Decision option "+index);b.setEnabled(c.available());b.setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));b.setToolTipText(c.reason());event.add(b);event.add(Box.createVerticalStrut(8));}event.add(text(e.missedResponse()));if(DebateSession.handles(e.id()))event.add(text("Read each response: preparation uses 50 campaign funds; the broader message supports later fundraising. Answer order varies by question."));}else{var e=v.world().pending();event.add(label(e.title(),18,INK));event.add(text(e.description()));for(int i=0;i<e.choices().size();i++){int index=i;var c=e.choices().get(i);JButton b=button(c.label()+"  ·  "+c.capitalCost()+" capital",()->controller.respond(key,index));b.setEnabled(v.world().get(WorldMetric.CAPITAL)>=c.capitalCost());b.setToolTipText(c.consequence());event.add(b);}event.add(text(v.world().missedResponse()));}}
    void updateTimer(){timer.setForeground(ACCENT);timer.setText(controller.seconds()>0?(!controller.realtime()?"CLOCK DISABLED  ·  ":controller.paused()?"PAUSED  ·  ":"RESPOND IN  ")+controller.seconds()+"s":turnDeadline());}
    private String turnDeadline(){var v=controller.view();if(v!=null&&v.phase()==CareerView.Phase.CAMPAIGN&&v.campaign().pendingEvent()!=null&&v.campaign().pendingEvent().id().startsWith("debate_live_"))return "UNTIMED  ·  Continue when ready";if(v==null)return "";if(v.phase()==CareerView.Phase.CAMPAIGN&&v.campaign().pendingEvent()!=null)return "DEADLINE  ·  Campaign turn "+v.campaign().pendingEvent().dueTurn();return "DEADLINE  ·  World month "+v.world().dueMonth();}
    private void dialog(Runnable task){controller.pause();try{task.run();}finally{controller.resume();}}
    private void help(){showText("How to play",java.util.List.of(
        "GET STARTED\nOpen Map, select a state and review its available actions. Each campaign action uses one turn. Fundraise on Dashboard when cash is low.",
        "DECISIONS & DEBATES\nNew events open Decisions automatically. Debates begin with six briefing slots. Answer a factual question, choose your confidence, then respond to an opponent challenge. Corrections cite sources; the opponent may be wrong. Timers range from 6 to 25 seconds. Answer positions vary; the order stays stable while you read. Menus and switching away pause the live clock.",
        "LEADING THE COUNTRY\nUse Dashboard for general actions and Office for appointments, public requests and midterm visits. These share the same monthly action allowance. End month advances delivery and world events.",
        "LEGISLATION\nPolicy explains each initiative. Introduce a bill, negotiate commitments, complete committee review and floor passage, then sign or veto. Signing pays the displayed delivery cost.",
        "KEEP YOUR PROGRESS\nMenu contains Save / Load, settings and exit. Phase changes autosave; save manually before leaving to preserve your latest actions. Statistics and History show your full record."));}
    private void newCareer(){if(controller.busy())return;dialog(()->{if(controller.view()!=null&&JOptionPane.showConfirmDialog(this,"Start over? Save your current career first if you want to keep it.","New career",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;JComboBox<President.Difficulty>d=new JComboBox<>(President.Difficulty.values());JComboBox<President.RunningMate>m=new JComboBox<>(President.RunningMate.values());friendly(d);friendly(m);JTextField seed=new JTextField(Long.toString(System.currentTimeMillis()));JButton random=button("Randomize seed",()->seed.setText(Long.toString(new java.security.SecureRandom().nextLong())));Object[] fields={"Difficulty",d,"Running mate",m,"Seed: same setup + choices reproduces a run.",seed,random,"Opening state baselines are fixed; the seed varies objectives and later events."};if(JOptionPane.showConfirmDialog(this,fields,"New career",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION)try{page="Dashboard";controller.start(Long.parseLong(seed.getText().strip()),(President.Difficulty)d.getSelectedItem(),(President.RunningMate)m.getSelectedItem());}catch(NumberFormatException e){JOptionPane.showMessageDialog(this,"Please enter a whole-number seed.");}});}
    private void saveMenu(){if(controller.busy())return;dialog(()->{String[] choices={"Load autosave","Save slot 1","Save slot 2","Save slot 3","Load slot 1","Load slot 2","Load slot 3","Quicksave","Load quicksave","Open save file"};Object selected=JOptionPane.showInputDialog(this,"Choose a save action. Loading replaces the current unsaved career.","Save / Load",JOptionPane.PLAIN_MESSAGE,null,choices,choices[0]);if(selected==null)return;String s=selected.toString();if(s.equals("Open save file")){JFileChooser f=new JFileChooser(saves.toFile());if(f.showOpenDialog(this)==JFileChooser.APPROVE_OPTION)controller.load(f.getSelectedFile().toPath());return;}Path path=saves.resolve(s.equals("Load autosave")?"autosave.properties":s.toLowerCase().contains("quicksave")?"quicksave.properties":"slot-"+s.charAt(s.length()-1)+".properties");if(s.startsWith("Load")){if(JOptionPane.showConfirmDialog(this,"Replace the current career with this save?","Load",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION)controller.load(path);}else if(controller.view()!=null){if(!Files.exists(path)||JOptionPane.showConfirmDialog(this,"Replace this slot? Previous versions are kept as backups.","Save",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION)controller.save(path,()->{});}});}
    private void settings(){if(controller.view()==null)return;dialog(()->{int x=JOptionPane.showOptionDialog(this,"Live response countdowns","Settings",JOptionPane.DEFAULT_OPTION,JOptionPane.QUESTION_MESSAGE,null,new String[]{"Enable","Disable","Cancel"},"Enable");if(x==0||x==1)controller.submit(CareerCommand.office(OfficeCommand.choice(OfficeCommand.Type.REALTIME_MODE,x==0?1:0)));});}
    private void developer(){if(controller.view()==null)return;dialog(()->{Object x=JOptionPane.showInputDialog(this,"Developer actions mark this career as modified.","Developer tools",JOptionPane.WARNING_MESSAGE,null,CareerCommand.DeveloperAction.values(),CareerCommand.DeveloperAction.FORCE_WIN);if(x!=null)controller.submit(CareerCommand.dev((CareerCommand.DeveloperAction)x));});}
    public void requestClose(Runnable close){if(controller.busy())return;if(controller.view()==null){close.run();return;}dialog(()->{int x=JOptionPane.showOptionDialog(this,"Save before leaving?","Exit",JOptionPane.DEFAULT_OPTION,JOptionPane.QUESTION_MESSAGE,null,new String[]{"Save and exit","Exit without saving","Cancel"},"Save and exit");if(x==0)controller.save(saves.resolve("quicksave.properties"),close);else if(x==1)close.run();});}
}
