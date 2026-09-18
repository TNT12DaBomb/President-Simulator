import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/** Results only; campaign decisions remain in the terminal. */
public final class ElectionGUI {
    public void showResults(ElectionResult result, State[] states, State dc) {
        if (result == null || states == null || dc == null) throw new IllegalArgumentException("Missing results");
        if (GraphicsEnvironment.isHeadless()) return;
        State[] snapshot = states.clone();
        Runnable render = () -> buildWindow(result, snapshot, dc);
        if (SwingUtilities.isEventDispatchThread()) render.run();
        else SwingUtilities.invokeLater(render);
    }
    private void buildWindow(ElectionResult result, State[] states, State dc) {
        JFrame frame = new JFrame("Presidential Simulator - Election Results");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JLabel header = new JLabel("You: " + result.getPlayerEV() + " EV | Opponent: " + result.getOpponentEV()
            + " EV | " + result.getOutcomeText(), SwingConstants.CENTER);
        header.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        frame.add(header, BorderLayout.NORTH);
        DefaultTableModel model = new DefaultTableModel(new String[]{"State / district", "EV", "Winner", "Explanation"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public Class<?> getColumnClass(int column) { return column == 1 ? Integer.class : String.class; }
        };
        for (State state : states) addRow(model, result, state, result.getStateResults().get(state.getName()));
        addRow(model, result, dc, result.didPlayerWinDC());
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(26);
        table.getColumnModel().getColumn(0).setPreferredWidth(175);
        table.getColumnModel().getColumn(1).setPreferredWidth(45);
        table.getColumnModel().getColumn(2).setPreferredWidth(90);
        table.getColumnModel().getColumn(3).setPreferredWidth(680);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JTextArea history = new JTextArea(String.join("\n", result.getHistory()));
        history.setEditable(false);
        history.setLineWrap(true);
        history.setWrapStyleWord(true);
        history.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("State results and reasons", new JScrollPane(table));
        tabs.addTab("Your decisions", new JScrollPane(history));
        frame.add(tabs, BorderLayout.CENTER);
        JLabel footer = new JLabel("Fictional board-game rules | 270 EV to win | State objectives are not political forecasts", SwingConstants.CENTER);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        frame.add(footer, BorderLayout.SOUTH);
        frame.setMinimumSize(new Dimension(800, 500));
        frame.setSize(1120, 740);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    private void addRow(DefaultTableModel model, ElectionResult result, State state, boolean won) {
        model.addRow(new Object[]{state.getName(), state.getElectoralVotes(), result.getVotes().isEmpty() ? (won ? "YOU" : "OPPONENT") : "YOU " + result.getVotes().get(state.getName()).playerEV() + " EV", result.getReasons().get(state.getName())});
    }
}
