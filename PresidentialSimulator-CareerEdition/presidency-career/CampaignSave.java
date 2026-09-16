import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

/** Versioned command replay. No Java object deserialization; no hidden random rerolls. */
public final class CampaignSave {
    public static final String FORMAT = "campaign-events-v1";
    private CampaignSave() { }
    public static void write(GameEngine engine, Path destination) throws IOException {
        Path path = destination.toAbsolutePath();
        Path parent = path.getParent();
        Files.createDirectories(parent);
        Properties data = new Properties();
        data.setProperty("format", FORMAT);
        data.setProperty("seed", Long.toString(engine.seed()));
        data.setProperty("difficulty", engine.difficulty().name());
        data.setProperty("mate", engine.runningMate().name());
        data.setProperty("commands", Integer.toString(engine.journal().size()));
        for (int i = 0; i < engine.journal().size(); i++) {
            GameCommand c = engine.journal().get(i);
            String prefix = "command." + i + ".";
            data.setProperty(prefix + "type", c.type().name());
            if (c.state() != null) data.setProperty(prefix + "state", c.state());
            if (c.task() != null) data.setProperty(prefix + "task", c.task().name());
            data.setProperty(prefix + "choice", Integer.toString(c.choice()));
        }
        Path temporary = Files.createTempFile(parent, "campaign-", ".tmp");
        try {
            try (OutputStream out = Files.newOutputStream(temporary)) { data.store(out, "Presidential Simulator save; engine format " + FORMAT); }
            try { Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException ex) { Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporary); }
    }
    public static GameEngine read(Path path) throws IOException {
        if (Files.size(path) > 1_000_000) throw new IOException("Save file is too large.");
        Properties data = new Properties();
        try (InputStream in = Files.newInputStream(path)) { data.load(in); }
        catch (IllegalArgumentException ex) { throw new IOException("Save file is malformed.", ex); }
        if (!FORMAT.equals(data.getProperty("format"))) throw new IOException("This save belongs to a different game version.");
        try {
            int count = Integer.parseInt(required(data, "commands"));
            if (count < 0 || count > President.CAMPAIGN_TURNS * 2) throw new IllegalArgumentException("Invalid command count");
            GameEngine engine = new GameEngine(Long.parseLong(required(data, "seed")),
                President.Difficulty.valueOf(required(data, "difficulty")), President.RunningMate.valueOf(required(data, "mate")));
            for (int i = 0; i < count; i++) {
                String prefix = "command." + i + ".";
                String task = data.getProperty(prefix + "task");
                GameCommand command = new GameCommand(GameCommand.Type.valueOf(required(data, prefix + "type")),
                    data.getProperty(prefix + "state"), task == null ? null : State.Task.valueOf(task), Integer.parseInt(required(data, prefix + "choice")));
                TurnReport report = engine.submit(command);
                if (!report.accepted()) throw new IllegalArgumentException("Invalid command " + (i + 1) + ": " + report.messages().get(0));
            }
            return engine;
        } catch (IllegalArgumentException ex) { throw new IOException("Cannot load this save: " + ex.getMessage(), ex); }
    }
    private static String required(Properties data, String name) {
        String value = data.getProperty(name);
        if (value == null) throw new IllegalArgumentException("Missing " + name);
        return value;
    }
}
