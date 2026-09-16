/** Best-effort virtual-terminal enablement for Windows console hosts. No persistent settings are changed. */
public final class TerminalSupport {
    private TerminalSupport() { }
    public static int[] dimensions(int fallbackWidth, int fallbackHeight) {
        try {
            boolean windows = System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).contains("win");
            String probe="Add-Type -TypeDefinition 'using System; using System.Runtime.InteropServices; public class Viewport { [StructLayout(LayoutKind.Sequential)] public struct Coord { public short X,Y; } [StructLayout(LayoutKind.Sequential)] public struct Rect { public short Left,Top,Right,Bottom; } [StructLayout(LayoutKind.Sequential)] public struct Info { public Coord Size,Cursor; public ushort Attributes; public Rect Window; public Coord Maximum; } [DllImport(\"kernel32.dll\", CharSet=CharSet.Unicode)] public static extern IntPtr CreateFile(string n,uint a,uint s,IntPtr p,uint d,uint f,IntPtr t); [DllImport(\"kernel32.dll\")] public static extern bool GetConsoleScreenBufferInfo(IntPtr h,out Info i); [DllImport(\"kernel32.dll\")] public static extern bool CloseHandle(IntPtr h); }'; $h=[Viewport]::CreateFile('CONOUT$',2147483648,3,[IntPtr]::Zero,3,0,[IntPtr]::Zero); try { $i=New-Object Viewport+Info; if([Viewport]::GetConsoleScreenBufferInfo($h,[ref]$i)){ Write-Output ((($i.Window.Bottom-$i.Window.Top+1).ToString())+' '+(($i.Window.Right-$i.Window.Left+1).ToString())) } } finally { [void][Viewport]::CloseHandle($h) }";
            ProcessBuilder builder = windows ? new ProcessBuilder("powershell.exe","-NoProfile","-NonInteractive","-Command",probe) : new ProcessBuilder("stty", "size");
            if (!windows) builder.redirectInput(new java.io.File("/dev/tty"));
            Process process = builder.redirectError(ProcessBuilder.Redirect.DISCARD).start();
            if (!process.waitFor(windows ? 5 : 2, java.util.concurrent.TimeUnit.SECONDS)) { process.destroyForcibly(); return new int[]{fallbackWidth, fallbackHeight}; }
            String result = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            String[] parts=result.strip().split("\\s+");
            int height=Integer.parseInt(parts[0]),width=Integer.parseInt(parts[1]);
            if(width<=0||height<=0)return new int[]{fallbackWidth,fallbackHeight};
            return new int[]{Math.min(180, width), Math.min(70, height)};
        } catch (java.io.IOException | IllegalArgumentException | IndexOutOfBoundsException ex) { return new int[]{fallbackWidth, fallbackHeight}; }
        catch (InterruptedException ex) { Thread.currentThread().interrupt(); return new int[]{fallbackWidth, fallbackHeight}; }
    }
    public static boolean enableWindowsAnsi() {
        if(!System.getProperty("os.name","").toLowerCase(java.util.Locale.ROOT).contains("win"))return true;
        if(System.getenv("WT_SESSION")!=null || System.getenv("ANSICON")!=null)return true;
        String script="Add-Type -TypeDefinition 'using System; using System.Runtime.InteropServices; public class VT { [DllImport(\"kernel32.dll\")] public static extern IntPtr GetStdHandle(int n); [DllImport(\"kernel32.dll\")] public static extern bool GetConsoleMode(IntPtr h, out uint m); [DllImport(\"kernel32.dll\")] public static extern bool SetConsoleMode(IntPtr h, uint m); }'; $h=[VT]::GetStdHandle(-11); [uint32]$m=0; if([VT]::GetConsoleMode($h,[ref]$m) -and [VT]::SetConsoleMode($h,($m -bor 4))) { exit 0 } else { exit 1 }";
        try {
            Process process=new ProcessBuilder("powershell.exe","-NoProfile","-NonInteractive","-Command",script)
                .redirectInput(ProcessBuilder.Redirect.INHERIT).redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.DISCARD).start();
            if(!process.waitFor(5,java.util.concurrent.TimeUnit.SECONDS)){process.destroyForcibly();return false;}
            return process.exitValue()==0;
        }catch(java.io.IOException ex){return false;}catch(InterruptedException ex){Thread.currentThread().interrupt();return false;}
    }
}
