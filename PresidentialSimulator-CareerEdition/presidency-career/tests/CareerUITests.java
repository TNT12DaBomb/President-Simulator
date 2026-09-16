import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CareerUITests {
    private static int checks;
    private static void check(boolean value, String message) { checks++; if (!value) throw new AssertionError(message); }
    private static String run(String commands, Path path) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new TextUI(new ByteArrayInputStream(commands.getBytes(StandardCharsets.UTF_8)), new PrintStream(output, true, StandardCharsets.UTF_8),
            42, path, false, false).run();
        return output.toString(StandardCharsets.UTF_8);
    }
    private static String resume() { return "2\n1\n\n"; }
    private static String exitMenu() { return "0\n4\n2\n"; }
    public static void main(String[] args) throws Exception {
        Path folder = Files.createTempDirectory("career-ui-");
        try {
            Path save = folder.resolve("career.save");
            String output = run("text\n9\n0\n", save);
            check(output.contains("Please type a number from 0 to 3"), "Input recovery");
            output = run("2\n1\n\n0\n", save);
            check(output.contains("Could not load"), "Missing save handled");
            output = run("1\n1\n3\n\n1\n1\ns\nCalifornia\n1\n0\n0\n" + exitMenu(), save);
            check(output.contains("CALIFORNIA") && output.contains("Your completed work"), "Search and detail retained");
            check(CareerSave.read(save).view().campaign().turnsUsed() == 0, "Browse is free");
            output = run(resume() + "1\n2\n0\n" + exitMenu(), save);
            check(CareerSave.read(save).view().campaign().turnsUsed() == 0, "Fundraiser cancellation free");
            output = run(resume() + "3\n\n\n\n" + exitMenu(), save);
            check(output.contains("PUBLIC FEEDBACK") && output.contains("ECONOMY AND OPERATING ACCOUNTS"), "Status and economy pages");
            output = run(resume() + "0\n2\n1\n1\n1\n\n1\n\n1\n1\n\n" + exitMenu(), save);
            check(output.contains("TRANSITION") && output.contains("PRESIDENTIAL DESK"), "Developer victory to presidency through actual menus");
            check(CareerSave.read(save).view().phase() == CareerView.Phase.PRESIDENCY, "Office state saved");
            check(CareerSave.read(save).view().developerUsed(), "Dev flag saved");
            output = run(resume() + "1\n1\n1\n1\n\n0\n1\n2\n1\n" + exitMenu(), save);
            CareerEngine current = CareerSave.read(save);
            check(current.view().termQuarters() == 1, "Quarter action routed from submenu");
            check(Files.exists(folder.resolve("career-slot-1.save")), "Manual slot created");
            check(CareerSave.read(folder.resolve("career-slot-1.save")).view().equals(current.view()), "Manual slot exact");
            output = run(resume() + "1\n3\n1\n\n0\n1\n3\n2\n1\n1\n\n" + exitMenu(), save);
            check(CareerSave.read(save).view().termQuarters() == 1, "Loading manual slot restores earlier quarter");
            output = run(resume() + "0\n1\n3\n2\n2\n1\n\n" + exitMenu(), save);
            check(output.contains("Could not load") && CareerSave.read(save).view().termQuarters() == 1, "Failed load keeps current game");
            output = run(resume() + "0\n2\n2\n4\n1\n\n1\n3\n1\n\n" + exitMenu(), save);
            check(output.contains("CAREER COMPLETE") && CareerSave.read(save).view().servedQuarters() == 32, "Final-quarter scenario ends at eight years");
            CareerEngine pending = new CareerEngine(9, President.Difficulty.NORMAL, President.RunningMate.FUNDRAISER);
            while (pending.view().campaign().pendingEvent() == null) pending.submit(CareerCommand.campaign(GameCommand.rest()));
            CareerSave.write(pending, save);
            output = run(resume() + exitMenu(), save);
            check(output.contains("EVENT RESPONSE") && output.contains("GAME MENU"), "Game menu reachable from pending event");
            check(CareerSave.read(save).view().equals(pending.view()), "Pending event preserved on exit");
            GameEngine legacy = new GameEngine(5, President.Difficulty.NORMAL, President.RunningMate.FUNDRAISER);
            legacy.submit(GameCommand.fundraise()); CampaignSave.write(legacy, folder.resolve("campaign.save"));
            output = run("2\n3\n\n" + exitMenu(), save);
            check(output.contains("Career restored") && CareerSave.read(save).view().campaign().equals(legacy.view()), "Legacy import from menu");
            check(Files.readString(folder.resolve("campaign.save")).contains(CampaignSave.FORMAT), "Legacy file kept intact");
            output = run(resume(), save);
            check(output.contains("Input closed. Your career was saved"), "EOF saves");
            Path bad = folder.resolve("invalid.save"); Files.createDirectories(bad); Files.writeString(bad.resolve("keep"), "x");
            output = run("1\n1\n1\n3\n\n" + exitMenu(), bad);
            check(output.contains("Could not save"), "Save errors visible");
            check(!output.contains("Input closed. Your career was saved"), "No false EOF success");
            System.out.println("PASS: " + checks + " career-menu checks.");
        } finally {
            try (var paths = Files.walk(folder)) { for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).toList()) Files.deleteIfExists(path); }
        }
    }
}
