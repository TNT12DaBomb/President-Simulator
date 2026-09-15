import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Exercises the real menu adapter without a terminal emulator or external libraries. */
public final class TextUITests {
    private static int checks;
    private static void check(boolean condition, String message) { checks++; if (!condition) throw new AssertionError(message); }
    private static String run(String text, long seed, Path save) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new TextUI(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)), new PrintStream(output, true, StandardCharsets.UTF_8),
            seed, save, false, false).run();
        return output.toString(StandardCharsets.UTF_8);
    }
    private static String setup() { return "1\n1\n3\n\n"; }
    public static void main(String[] args) throws Exception {
        Path folder = Files.createTempDirectory("campaign-ui-");
        try {
            Path save = folder.resolve("campaign.save");
            String text = run("hello\n9\n0\n", 42, save);
            check(text.contains("Please type a number from 0 to 3"), "Friendly input recovery");
            text = run("2\n0\n", 42, save);
            check(text.contains("Could not resume"), "Missing save is recoverable");
            text = run(setup() + "1\ns\nCalifornia\n1\n0\n0\n6\n", 42, save);
            check(text.contains("CALIFORNIA") && text.contains("Your completed work"), "State search and detail");
            check(CampaignSave.read(save).view().turnsUsed() == 0, "Browsing and cancellation use no turns");
            text = run("2\n2\n0\n6\n", 42, save);
            check(CampaignSave.read(save).view().turnsUsed() == 0, "Cancelled fundraiser is free");
            text = run("2\n2\n1\n\n", 42, save);
            check(text.contains("TURN RECAP") && text.contains("You fundraised"), "Actual turn recap");
            check(CampaignSave.read(save).view().turnsUsed() == 1, "Accepted turn autosaved");
            text = run("1\n0\n0\n", 42, save);
            check(text.contains("replaces the existing autosave"), "Overwrite confirmation");
            check(CampaignSave.read(save).view().turnsUsed() == 1, "Declining overwrite preserves save");
            text = run("2\n", 42, save);
            check(text.contains("Input closed"), "Clean EOF");
            // Generate a complete text playthrough with real responses from an independent engine.
            GameEngine driver = new GameEngine(9, President.Difficulty.NORMAL, President.RunningMate.FUNDRAISER);
            StringBuilder script = new StringBuilder(setup());
            while (!driver.view().complete()) {
                if (driver.view().pendingEvent() == null) { driver.submit(GameCommand.rest()); script.append("7\n1\n\n"); }
                else { driver.submit(GameCommand.respond(1)); script.append("2\n1\n\n"); }
            }
            script.append("0\n");
            Path complete = folder.resolve("complete.save");
            text = run(script.toString(), 9, complete);
            check(text.contains("ELECTION NIGHT"), "Full guided game reaches results");
            check(CampaignSave.read(complete).view().equals(driver.view()), "Text adapter and engine agree exactly");
            // A pending event can be saved and exited without accepting or rerolling it.
            GameEngine pending = new GameEngine(9, President.Difficulty.NORMAL, President.RunningMate.FUNDRAISER);
            while (pending.view().pendingEvent() == null && !pending.view().complete()) pending.submit(GameCommand.rest());
            check(pending.view().pendingEvent() != null, "Pending fixture");
            CampaignSave.write(pending, save);
            text = run("2\n0\n", 9, save);
            check(text.contains("saved with this decision pending"), "Explicit pending exit");
            check(CampaignSave.read(save).view().equals(pending.view()), "Pending exit preserves event");
            check(!text.contains("Input closed"), "Explicit exit is not reported as EOF");
            // Save failures must be visible and Save & Exit must leave the game running.
            Path badSave = folder.resolve("directory-instead-of-save"); Files.createDirectories(badSave); Files.writeString(badSave.resolve("keep"), "x");
            text = run("1\n1\n1\n3\n\n6\n", 1, badSave);
            check(text.contains("Could not save"), "Save error surfaced");
            check(!text.contains("Choose Resume the next time"), "No false save success");
            System.out.println("PASS: " + checks + " guided-interface checks.");
        } finally {
            try (var files = Files.walk(folder)) { for (Path p : files.sorted(java.util.Comparator.reverseOrder()).toList()) Files.deleteIfExists(p); }
        }
    }
}
