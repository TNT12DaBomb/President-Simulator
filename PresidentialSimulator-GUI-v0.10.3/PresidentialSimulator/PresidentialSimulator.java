import java.nio.file.Path;

/** Interactive consoles use refreshed full-screen frames; redirected output defaults to plain text. */
public final class PresidentialSimulator {
    public static void main(String[] args) {
        long seed=GameSeeds.fresh(); boolean gui=false, color=true, screen=System.console()!=null;
        int width=environmentSize("COLUMNS",100,60,180),height=environmentSize("LINES",28,20,70);
        Path save=Path.of("saves","career.save");
        try {
            for(int i=0;i<args.length;i++)switch(args[i]){
                case "--seed" -> throw new IllegalArgumentException("Seed overrides are available only in password-protected developer tools.");case "--save" -> save=Path.of(args[++i]);
                case "--no-gui" -> gui=false;case "--gui" -> gui=true;
                case "--color" -> color=true;case "--no-color" -> color=false;
                case "--screen" -> screen=true;case "--plain" -> {screen=false;color=false;}
                case "--width" -> width=Integer.parseInt(args[++i]);case "--height" -> height=Integer.parseInt(args[++i]);
                case "--help" -> {usage();return;}default -> throw new IllegalArgumentException();
            }
            if(screen){
                int[] visible=TerminalSupport.dimensions(width,height);
                if(visible[0]<60||visible[1]<20){System.out.println("Enlarge this window to at least 60 columns by 20 rows, then reopen the game.");return;}
                width=Math.min(width,visible[0]);height=Math.min(height,visible[1]);
            }
            if(screen&&!TerminalSupport.enableWindowsAnsi()){System.out.println("Console refresh unavailable. Using plain mode; Windows Terminal supports full-screen mode.");screen=false;color=false;}
            new TextUI(System.in,System.out,seed,save,gui,color,new TerminalScreen.Options(width,height,screen,color&&screen)).run();
        }catch(IllegalArgumentException|IndexOutOfBoundsException ex){System.out.println("Invalid launch options: "+ex.getMessage());usage();}
    }
    private static int environmentSize(String name,int fallback,int min,int max){try{return Math.max(min,Math.min(max,Integer.parseInt(System.getenv(name))));}catch(NumberFormatException ex){return fallback;}}
    private static void usage(){
        System.out.println("Presidential Simulator — Interface Playtest (Java 17+)");
        System.out.println("Interactive consoles refresh each screen. The compact default frame is 100 x 28; override with --width / --height.");
        System.out.println("--screen | --plain   --width 60..180   --height 20..70   --no-color | --color");
        System.out.println("--save <file>   --gui | --no-gui   --help");
        System.out.println("Inside long screens: > next page, < previous page. Default rules use researched procedures with documented simulation limits.");
    }
}
