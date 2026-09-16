import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

/** Career replay persistence; imports the supplied campaign-events-v1 format. */
public final class CareerSave {
    public static final String FORMAT = "presidency-career-v1";
    private CareerSave() { }
    public static void write(CareerEngine engine, Path destination) throws IOException {
        Properties data = new Properties();
        data.setProperty("format", FORMAT);
        data.setProperty("seed", Long.toString(engine.seed()));
        data.setProperty("difficulty", engine.difficulty().name());
        data.setProperty("mate", engine.runningMate().name());
        data.setProperty("commands", Integer.toString(engine.journal().size()));
        for (int i = 0; i < engine.journal().size(); i++) {
            CareerCommand command = engine.journal().get(i);
            String prefix = "command." + i + ".";
            data.setProperty(prefix + "type", command.type().name());
            if (command.campaign() != null) {
                GameCommand c = command.campaign();
                data.setProperty(prefix + "campaign.type", c.type().name());
                data.setProperty(prefix + "campaign.choice", Integer.toString(c.choice()));
                if (c.state() != null) data.setProperty(prefix + "campaign.state", c.state());
                if (c.task() != null) data.setProperty(prefix + "campaign.task", c.task().name());
            }
            if (command.governance() != null) data.setProperty(prefix + "governance", command.governance().name());
            if (command.rebuild() != null) data.setProperty(prefix + "rebuild", command.rebuild().name());
            if (command.developer() != null) data.setProperty(prefix + "developer", command.developer().name());
        }
        Path target = destination.toAbsolutePath();
        Files.createDirectories(target.getParent());
        Path temporary = Files.createTempFile(target.getParent(), "career-", ".tmp");
        try {
            try (OutputStream stream = Files.newOutputStream(temporary)) { data.store(stream, "Presidential Simulator career save"); }
            try { Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException ex) { Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporary); }
    }
    public static CareerEngine read(Path path) throws IOException {
        if (Files.size(path) > 4_000_000) throw new IOException("Save exceeds the supported size.");
        Properties data = new Properties();
        try (InputStream stream = Files.newInputStream(path)) { data.load(stream); }
        catch (IllegalArgumentException ex) { throw new IOException("Malformed save file.", ex); }
        if (CampaignSave.FORMAT.equals(data.getProperty("format"))) {
            GameEngine legacy = CampaignSave.read(path);
            CareerEngine imported = new CareerEngine(legacy.seed(), legacy.difficulty(), legacy.runningMate());
            for (GameCommand command : legacy.journal()) {
                if (!imported.submit(CareerCommand.campaign(command)).accepted()) throw new IOException("Legacy campaign could not be imported.");
            }
            return imported;
        }
        if (!FORMAT.equals(data.getProperty("format"))) throw new IOException("Unsupported save version.");
        try {
            int count = Integer.parseInt(required(data, "commands"));
            if (count < 0 || count > CareerEngine.MAX_COMMANDS) throw new IllegalArgumentException("Invalid command count");
            CareerEngine engine = new CareerEngine(Long.parseLong(required(data, "seed")),
                President.Difficulty.valueOf(required(data, "difficulty")), President.RunningMate.valueOf(required(data, "mate")));
            for (int i = 0; i < count; i++) {
                String prefix = "command." + i + ".";
                CareerCommand.Type type = CareerCommand.Type.valueOf(required(data, prefix + "type"));
                GameCommand campaign = null;
                if (data.containsKey(prefix + "campaign.type")) {
                    String task = data.getProperty(prefix + "campaign.task");
                    campaign = new GameCommand(GameCommand.Type.valueOf(required(data, prefix + "campaign.type")),
                        data.getProperty(prefix + "campaign.state"), task == null ? null : State.Task.valueOf(task),
                        Integer.parseInt(required(data, prefix + "campaign.choice")));
                }
                String governance = data.getProperty(prefix + "governance"), rebuild = data.getProperty(prefix + "rebuild"), developer = data.getProperty(prefix + "developer");
                CareerCommand command = new CareerCommand(type, campaign,
                    governance == null ? null : CareerCommand.GovernanceAction.valueOf(governance),
                    rebuild == null ? null : CareerCommand.RebuildAction.valueOf(rebuild),
                    developer == null ? null : CareerCommand.DeveloperAction.valueOf(developer));
                CareerReport result = engine.submit(command);
                if (!result.accepted()) throw new IllegalArgumentException("Command " + (i + 1) + " rejected: " + result.messages().get(0));
            }
            return engine;
        } catch (IllegalArgumentException ex) { throw new IOException("Cannot restore this career: " + ex.getMessage(), ex); }
    }
    private static String required(Properties data, String key) {
        String value = data.getProperty(key);
        if (value == null) throw new IllegalArgumentException("Missing " + key);
        return value;
    }
}
