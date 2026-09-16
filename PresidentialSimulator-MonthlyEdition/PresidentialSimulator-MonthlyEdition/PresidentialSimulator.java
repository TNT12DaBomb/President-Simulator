import java.nio.file.Path;

/** Application entry point. Gameplay is controlled by CareerEngine and GameEngine. */
public final class PresidentialSimulator {
    public static void main(String[] args) {
        long seed = System.nanoTime();
        boolean gui = true, color = false;
        Path save = Path.of("saves", "monthly.save");
        try {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "--seed" -> seed = Long.parseLong(args[++i]);
                    case "--save" -> save = Path.of(args[++i]);
                    case "--no-gui" -> gui = false;
                    case "--color" -> color = true;
                    case "--help" -> { usage(); return; }
                    default -> throw new IllegalArgumentException();
                }
            }
        } catch (IllegalArgumentException | IndexOutOfBoundsException ex) { usage(); return; }
        new TextUI(System.in, System.out, seed, save, gui, color).run();
    }
    private static void usage() {
        System.out.println("Presidential Simulator - Monthly Edition");
        System.out.println("Start without options for the guided menu.");
        System.out.println("Options: --seed <integer>  --save <file>  --no-gui  --color  --help");
    }
}
