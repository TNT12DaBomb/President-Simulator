import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class ElectionGUI {

    private JFrame frame;

    private JLabel playerEVLabel;
    private JLabel opponentEVLabel;
    private JLabel statusLabel;

    public void showResults(
            ElectionResult result,
            State[] states,
            State districtOfColumbia) {

        frame = new JFrame("Presidential Simulator - Electoral College");

        frame.setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );

        frame.setSize(1000, 750);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel =
                new JPanel(new BorderLayout(10, 10));

        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        15, 15, 15, 15
                )
        );

        // ------------------------
        // HEADER
        // ------------------------

        JPanel headerPanel =
                new JPanel(new GridLayout(2, 1));

        JLabel title =
                new JLabel(
                        "ELECTORAL COLLEGE",
                        SwingConstants.CENTER
                );

        title.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        28
                )
        );

        headerPanel.add(title);

        JPanel scorePanel =
                new JPanel(
                        new GridLayout(1, 3)
                );

        playerEVLabel =
                new JLabel(
                        "YOUR EV: " +
                        result.getPlayerEV(),
                        SwingConstants.CENTER
                );

        opponentEVLabel =
                new JLabel(
                        "OPPONENT EV: " +
                        result.getOpponentEV(),
                        SwingConstants.CENTER
                );

        statusLabel =
                new JLabel(
                        result.playerWon()
                                ? "YOU WIN!"
                                : "YOU LOST",
                        SwingConstants.CENTER
                );

        playerEVLabel.setFont(
                new Font("Arial", Font.BOLD, 20)
        );

        opponentEVLabel.setFont(
                new Font("Arial", Font.BOLD, 20)
        );

        statusLabel.setFont(
                new Font("Arial", Font.BOLD, 20)
        );

        scorePanel.add(playerEVLabel);
        scorePanel.add(opponentEVLabel);
        scorePanel.add(statusLabel);

        headerPanel.add(scorePanel);

        mainPanel.add(
                headerPanel,
                BorderLayout.NORTH
        );

        // ------------------------
        // STATE GRID
        // ------------------------

        JPanel statePanel =
                new JPanel(
                        new GridLayout(0, 4, 8, 8)
                );

        statePanel.setBorder(
                BorderFactory.createTitledBorder(
                        "States"
                )
        );

        Map<String, Boolean> results =
                result.getStateResults();

        for (State state : states) {

            boolean won =
                    results.get(state.getName());

            JPanel card =
                    createStateCard(
                            state,
                            won
                    );

            statePanel.add(card);
        }

        JScrollPane scrollPane =
                new JScrollPane(statePanel);

        mainPanel.add(
                scrollPane,
                BorderLayout.CENTER
        );

        // ------------------------
        // D.C.
        // ------------------------

        JPanel dcPanel =
                createStateCard(
                        districtOfColumbia,
                        result.didPlayerWinDC()
                );

        dcPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "District of Columbia"
                )
        );

        // ------------------------
        // BOTTOM
        // ------------------------

        JPanel bottomPanel =
                new JPanel(
                        new BorderLayout()
                );

        bottomPanel.add(
                dcPanel,
                BorderLayout.CENTER
        );

        JLabel neededLabel =
                new JLabel(
                        "270 EV NEEDED TO WIN",
                        SwingConstants.CENTER
                );

        neededLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        18
                )
        );

        bottomPanel.add(
                neededLabel,
                BorderLayout.SOUTH
        );

        mainPanel.add(
                bottomPanel,
                BorderLayout.SOUTH
        );

        frame.add(mainPanel);

        frame.setVisible(true);
    }

    // ------------------------
    // CREATE STATE CARD
    // ------------------------

    private JPanel createStateCard(
            State state,
            boolean playerWon) {

        JPanel panel =
                new JPanel(
                        new GridLayout(3, 1)
                );

        Color color;

        if (playerWon) {
            color = new Color(
                    180, 210, 255
            );
        }
        else {
            color = new Color(
                    255, 190, 190
            );
        }

        panel.setBackground(color);

        panel.setBorder(
                BorderFactory.createLineBorder(
                        Color.GRAY,
                        1
                )
        );

        JLabel nameLabel =
                new JLabel(
                        state.getName(),
                        SwingConstants.CENTER
                );

        JLabel evLabel =
                new JLabel(
                        state.getElectoralVotes() +
                        " EV",
                        SwingConstants.CENTER
                );

        JLabel resultLabel =
                new JLabel(
                        playerWon
                                ? "YOU"
                                : "OPPONENT",
                        SwingConstants.CENTER
                );

        nameLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        14
                )
        );

        evLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        16
                )
        );

        resultLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        13
                )
        );

        panel.add(nameLabel);
        panel.add(evLabel);
        panel.add(resultLabel);

        return panel;
    }
}
