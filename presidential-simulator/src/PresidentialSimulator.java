import java.util.Scanner;

public class PresidentialSimulator {

    private static final Scanner scanner =
            new Scanner(System.in);

    public static void main(String[] args) {

        // ------------------------
        // CREATE PRESIDENT
        // ------------------------

        President player =
                new President(
                        50,   // approval
                        50,   // economy
                        50,   // public trust
                        60,   // party support
                        1000, // treasury
                        0,    // scandal
                        50,   // national security
                        100,  // health
                        50    // international relations
                );

        // ------------------------
        // CREATE ELECTORAL COLLEGE
        // ------------------------

        ElectoralCollege electoralCollege =
                new ElectoralCollege();

        System.out.println(
                "=== PRESIDENTIAL SIMULATOR ==="
        );

        System.out.println();

        System.out.println(
                "Electoral College: 538 EV"
        );

        System.out.println(
                "270 EV needed to win"
        );

        System.out.println();

        // ------------------------
        // DIFFICULTY
        // ------------------------

        System.out.println(
                "Select Difficulty:"
        );

        System.out.println(
                "1. Normal"
        );

        System.out.println(
                "2. Hard"
        );

        int difficulty =
                scanner.nextInt();

        // ------------------------
        // RUNNING MATE
        // ------------------------

        chooseRunningMate(player);

        // ------------------------
        // CAMPAIGN
        // ------------------------

        campaignPhase(player);

        // ------------------------
        // ELECTION
        // ------------------------

        ElectionResult result =
                electoralCollege.runElection(
                        player
                );

        // ------------------------
        // DISPLAY GUI
        // ------------------------

        ElectionGUI gui =
                new ElectionGUI();

        gui.showResults(
                result,
                electoralCollege.getStates(),
                electoralCollege.getDistrictOfColumbia()
        );

        // ------------------------
        // RESULT
        // ------------------------

        if (result.playerWon()) {

            System.out.println(
                    "\nYou won the presidency!"
            );

        }
        else {

            System.out.println(
                    "\nYou lost the election."
            );
        }
    }

    // ------------------------
    // RUNNING MATE
    // ------------------------

    private static void chooseRunningMate(
            President p) {

        System.out.println(
                "\nChoose Your Running Mate:"
        );

        System.out.println(
                "1. Reform Governor (+Trust)"
        );

        System.out.println(
                "2. Party Insider (+Party Support)"
        );

        System.out.println(
                "3. Business Leader (+Economy)"
        );

        System.out.println(
                "4. Firebrand Senator (+Approval, +Scandal)"
        );

        int choice =
                scanner.nextInt();

        switch (choice) {

            case 1:
                p.changePublicTrust(15);
                break;

            case 2:
                p.changePartySupport(15);
                break;

            case 3:
                p.changeEconomy(15);
                break;

            case 4:
                p.changeApproval(15);
                p.changeScandal(10);
                break;

            default:
                System.out.println(
                        "Invalid choice."
                );
        }
    }

    // ------------------------
    // CAMPAIGN
    // ------------------------

    private static void campaignPhase(
            President p) {

        System.out.println(
                "\n--- CAMPAIGN PHASE ---"
        );

        System.out.println(
                "1. Focus Economy"
        );

        System.out.println(
                "2. Focus Social Issues"
        );

        System.out.println(
                "3. Attack Opponent"
        );

        int choice =
                scanner.nextInt();

        if (choice == 1) {

            p.changeEconomy(10);

        }
        else if (choice == 2) {

            p.changePublicTrust(10);

        }
        else if (choice == 3) {

            p.changeApproval(10);
            p.changeScandal(10);

        }
        else {

            System.out.println(
                    "Invalid choice."
            );
        }
    }
}
