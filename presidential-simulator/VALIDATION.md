# Validation performed

Environment: OpenJDK 17.0.20 on Linux; headless execution.

- All six source files and the Java regression test compiled with `-Xlint:all`, without warnings.
- Java regression suite: **10,673 checks passed**, covering 100 seeds × two difficulties × four running mates, with **800 winning campaigns and 800 losing campaigns**.
- Checked 50 states plus D.C., 538 EV, reachable exact-270 wins, inactivity losses, separate D.C. allocation, and 269–269 tie handling.
- Checked scheduled challenges, protection/recovery, actual running-mate benefits, insufficient funds, duplicate/irrelevant actions, fundraising, turn limits, early-election rejection, defensive copies, immutable results/history, malformed result rejection, and seed reproducibility.
- **10 console integration checks passed**, including a complete win, complete loss, invalid numeric/text input recovery, EOF, menus, cancellation, malformed command-line arguments, and automatic headless fallback.
- The GUI's headless path executed successfully. Actual Swing window appearance was **not visually tested**, because no desktop/display server was available.
- Windows `.bat` launcher supplied but **not executed on Windows**. The compiler-module command it uses was verified on Linux.

The deterministic scenario checks prove both outcomes are reachable and that the tested invariants hold. They do not establish realism, game balance, or fun. The documented winning test route is a regression fixture, not evidence of broad strategic depth.
