import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

/** Current playtest saves only; no older-format import or migration. */
public final class CareerSave {
    public static final String FORMAT = "presidency-consequences-v5";
    private CareerSave() { }
    public static void write(CareerEngine engine, Path destination) throws IOException {
        write(engine, destination, null);
    }
    public static void write(CareerEngine engine, Path destination, String label) throws IOException {
        if(engine.rules()!=GameRules.CURRENT)throw new IOException("Only current-rule careers can be saved by this build.");
        Properties data = new Properties();
        String name = label;
        if (name == null && Files.exists(destination)) {
            try { name = metadata(destination).label(); } catch (IOException ignored) { }
        }
        if (name == null || name.isBlank()) name = "Career save";
        if (name.length() > 60) throw new IOException("Save names may contain at most 60 characters.");
        data.setProperty("label", name.strip());
        data.setProperty("format", FORMAT);
        data.setProperty("rules", engine.rules().name());
        data.setProperty("savedAt", java.time.Instant.now().toString());
        data.setProperty("summary", "Year " + engine.view().year() + " / " + engine.view().phase() + " / " + engine.view().servedMonths() + " months served" + (engine.view().developerUsed() ? " / Developer" : ""));
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
            if (command.office() != null) {
                OfficeCommand o = command.office();
                data.setProperty(prefix + "office.type", o.type().name());
                data.setProperty(prefix + "office.choice", Integer.toString(o.choice()));
                if (o.issue() != null) data.setProperty(prefix + "office.issue", o.issue().name());
                if (o.approach() != null) data.setProperty(prefix + "office.approach", o.approach().name());
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
            if (Files.exists(target)) {
                for (int n = 3; n >= 2; n--) if (Files.exists(backup(target, n - 1)))
                    Files.copy(backup(target, n - 1), backup(target, n), StandardCopyOption.REPLACE_EXISTING);
                Files.copy(target, backup(target, 1), StandardCopyOption.REPLACE_EXISTING);
            }
            try { Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException ex) { Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporary); }
    }
    public static CareerEngine read(Path path) throws IOException {
        if (Files.size(path) > 4_000_000) throw new IOException("Save exceeds the supported size.");
        Properties data = new Properties();
        try (InputStream stream = Files.newInputStream(path)) { data.load(stream); }
        catch (IllegalArgumentException ex) { throw new IOException("Malformed save file.", ex); }
        if(!FORMAT.equals(data.getProperty("format")))throw new IOException("This save is from another build. Start a new career; older saves are not supported.");
        if(!GameRules.CURRENT.name().equals(data.getProperty("rules")))throw new IOException("This build supports only the current game rules.");
        try {
            int count = Integer.parseInt(required(data, "commands"));
            if (count < 0 || count > CareerEngine.MAX_COMMANDS) throw new IllegalArgumentException("Invalid command count");
            CareerEngine engine = new CareerEngine(Long.parseLong(required(data, "seed")),
                President.Difficulty.valueOf(required(data, "difficulty")), President.RunningMate.valueOf(required(data, "mate")),
                GameRules.CURRENT);
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
                OfficeCommand office = null;
                if (data.containsKey(prefix + "office.type")) {
                    String issue = data.getProperty(prefix + "office.issue"), approach = data.getProperty(prefix + "office.approach");
                    office = new OfficeCommand(OfficeCommand.Type.valueOf(required(data, prefix + "office.type")),
                        issue == null ? null : Policy.Issue.valueOf(issue), approach == null ? null : Policy.Approach.valueOf(approach),
                        Integer.parseInt(required(data, prefix + "office.choice")));
                }
                CareerCommand command = new CareerCommand(type, campaign,
                    governance == null ? null : CareerCommand.GovernanceAction.valueOf(governance),
                    rebuild == null ? null : CareerCommand.RebuildAction.valueOf(rebuild),
                    developer == null ? null : CareerCommand.DeveloperAction.valueOf(developer), office);
                CareerReport result = engine.submit(command);
                if (!result.accepted()) throw new IllegalArgumentException("Command " + (i + 1) + " rejected: " + result.messages().get(0));
            }
            return engine;
        } catch (IllegalArgumentException ex) { throw new IOException("Cannot restore this career: " + ex.getMessage(), ex); }
    }
    public record Metadata(String label, String savedAt, String summary, String format) { }
    public static Path backup(Path path, int generation) {
        if (generation < 1 || generation > 3) throw new IllegalArgumentException("Backup generation must be 1–3");
        return path.resolveSibling(path.getFileName() + ".bak" + generation);
    }
    public static Metadata metadata(Path path) throws IOException {
        if (Files.size(path) > 4_000_000) throw new IOException("Save exceeds the supported size.");
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(path)) { p.load(in); }
        catch (IllegalArgumentException ex) { throw new IOException("Malformed save metadata.", ex); }
        return new Metadata(clean(p.getProperty("label", "Unnamed career")),
            clean(p.getProperty("savedAt", Files.getLastModifiedTime(path).toInstant().toString())),
            clean(p.getProperty("summary", "Older save; details available after loading")), clean(p.getProperty("format", "Unknown format")));
    }
    private static String clean(String text) { return text.replaceAll("[\\p{Cntrl}]", " "); }
    private static String required(Properties data, String key) {
        String value = data.getProperty(key);
        if (value == null) throw new IllegalArgumentException("Missing " + key);
        return value;
    }
}
