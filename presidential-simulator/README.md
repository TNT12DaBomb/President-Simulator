# Presidential Simulator — playable campaign revision

This package replaces all six supplied Java classes. It provides an eight-turn text campaign and an optional Swing results window. **This is a deterministic, fictional board-game adaptation, not a realistic election model or forecast.** It does not model real parties, candidates, public opinion, or voter preferences.

## Start on Windows

1. Extract the entire ZIP into a normal folder.
2. Install a **JDK 17 or newer** if Java is not available.
3. Open `presidential-simulator`, then double-click `run-windows.bat`.
4. Make campaign choices in the terminal. The results window opens after turn eight.

Alternatively, in PowerShell opened in the extracted folder:

```powershell
.\run-windows.bat --seed 42
```

For console output only:

```powershell
.\run-windows.bat --seed 42 --no-gui
```

On macOS/Linux, run `sh run.sh --seed 42`. The seed determines the fictional state objectives. Reusing a seed and the same decisions gives the same result. Omitting it creates a new objective layout each run.

Standard Java commands also work:

```text
javac -d build *.java
java -cp build PresidentialSimulator --seed 42
```

The launchers use Java's compiler module so they also work when `java` is available but there is no separate `javac` launcher. A runtime without the compiler module still requires a JDK.

## Replacing files in an existing project

Copy **all six `.java` files together** into your source folder, replacing the old versions. Remove old duplicates such as `President(2).java`; public Java class names must match their filenames exactly. Do not compile the old and new versions together. No external Java libraries, Maven, Gradle, or network connection are needed at runtime.

`President` now holds campaign resources and decisions. Its old numerical attribute constructor/getters and `State`'s political-leaning API have been removed. `ElectionResult` derives tallies from validated state outcomes. Any code outside the six supplied files that uses those old APIs would need migration.

## How this version works

- The supplied 50-state electoral-vote roster and separate three-vote D.C. entry are retained: 538 total, 270 to win.
- The invented starting bloc contains 186 EV. It is selected by a simple alphabetical index rule, not real political alignment.
- Other states require two named objectives: a town hall, field office, or outreach event. Both objectives must be complete before the state changes hands.
- Every successful action uses one of eight turns. Town halls cost $100, field offices $175, and outreach $150 before running-mate discounts.
- Texas faces a scripted opponent challenge after turn three; Illinois after turn six. A field office protects either state or recovers it later. The schedule is visible from the start.
- Normal starts with $1,000; Hard with $700. A fundraising turn adds $250. Skipping uses a turn without completing objectives.
- Running mates offer a stated $50 discount on one action type, or $200 additional starting funds.
- Board/history views, cancelled selections, invalid actions, repeat actions, and unaffordable actions consume no time or money.
- On turn eight the current board becomes the election result. There is no random vote roll or hidden numerical candidate rating. Unspent funds have no direct effect on the tally.
- Every result includes state-by-state reasons and the complete decision history. A 269–269 tie ends without declaring a winner; contingent-election gameplay is not implemented.

The objective system intentionally trades realism for transparent, testable consequences. It is a substantial gameplay redesign, not a one-line rebalance of the original formula. The only randomized element is objective assignment at setup. The opponent follows a fixed, announced script.

## Verification

Compile and run dependency-free tests:

```text
javac -Xlint:all -d build *.java tests/SimulatorTests.java
java -Djava.awt.headless=true -cp build SimulatorTests
```

If your environment has the compiler module but no `javac` executable:

```text
java -m jdk.compiler/com.sun.tools.javac.Main -Xlint:all -d build *.java tests/SimulatorTests.java
```

Optional text-interface tests require Python 3:

```text
python tests/console_checks.py
```

See `VALIDATION.md` for the checks actually performed and `REVIEW.md` for the source audit, design changes, and suggested next features. Windows launcher execution and visual rendering of the Swing window were not tested in the Linux/headless environment.
