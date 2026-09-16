# Presidential Simulator — Career Edition

This release extends the campaign-events game into a continuing presidential career. The campaign remains a guided text game, but the rules now run through a UI-independent career state machine that a future GUI can use directly.

## Start playing

On Windows, extract the ZIP and double-click `run-windows.bat`. On macOS or Linux, run `sh run.sh`. The included `career.jar` requires a Java 17+ runtime. It starts the guided menu; players choose numbered options and press Enter. Read `START-HERE.txt` for a short first-use guide.

The launcher prefers `career.jar`. After editing source, run `build-windows.bat` or `sh build.sh` to rebuild it. A JDK 17+ is required for rebuilding. No external libraries or internet connection are required.

## Career loop

The career contains these phases:

1. **Campaign:** the existing 16-turn campaign with fictional objectives, an active opponent, and 36 random events.
2. **Election review:** the player sees the result and the consequences for the career record.
3. **Transition:** a winner reviews campaign work that supplies one-use transition resources, then takes office.
4. **Presidency:** one quarterly administrative decision per quarter for a four-year term.
5. **Term review:** after four years, the player can run again or retire.
6. **Opposition/comeback:** after a loss, the player gets four annual rebuilding decisions before running again.
7. **Retired:** the normal career ends after two elected terms or voluntary retirement.

The system allows a comeback after a lost election and allows nonconsecutive terms. Two election victories are the traditional eight-year limit. Dictatorship, wars, scandals, and other extraordinary paths are intentionally reserved for later systems.

A natural loss remains in the career history. Until the player takes the donor-meeting rebuilding action, the next campaign starts with $200 less. Campaign review changes the public record, community work prepares an outreach objective, and final-year governing work can prepare campaign objectives or a fictional fundraising benefit. These effects are visible on the Career Status screen.

## Presidential framework

The presidency currently focuses on structure and functionality rather than policy simulation. Every quarter, the player chooses one action:

| Action | Effect |
|---|---|
| Public briefing | Records public-facing work and improves the feedback description |
| Cabinet meeting | Records organizational coordination and improves the support description |
| Budget review | Costs $25; the first review each year recovers a fictional $100 duplicate payment |
| Service review | Costs $150; records service-delivery administration |
| Routine administration | No special work; normal receipts and operating expenses still occur |

Each quarter adds $250 in fictional operating receipts and subtracts $200 in routine expenses. A transition credit can pay for one corresponding first action, or reduce the service review by $75. Final-year work can prepare the next campaign. This is an accounting and state-machine framework; it does not claim to model GDP, inflation, employment, or real presidential approval.

The dashboard displays public feedback, reputation, support, treasury, economic operating conditions, term progress, and next-campaign effects. It uses descriptive statuses instead of pretending that a small prototype can produce meaningful real-world approval ratings.

## Menus and accessibility

The player interacts through a small number of commands at a time:

- The campaign screen has separate actions, briefing, status, and game-menu choices.
- State pages show seven states at once and provide search, next, previous, and all-state navigation.
- State details show objectives, completed work, ownership, costs, and explanations before a player confirms an action.
- Presidency actions are grouped into public/cabinet and budget/service areas.
- The 0 option opens the Game Menu from campaign, election, transition, presidency, comeback, and pending-event screens.
- Browsing, status, help, cancellation, invalid input, and rejected actions are free.
- Autosave follows accepted commands. Three manual slots are available. Save errors are reported without falsely claiming that a save succeeded.
- Optional ANSI heading color can be enabled with `--color`; plain text is the default for readable logs and older terminals.

## Developer tools

The developer menu is intentionally visible in this development build. It can:

- Force a win or loss in the active campaign.
- Jump to the middle of the first term.
- Start the reelection campaign.
- Jump to the middle or final quarter of the second term.
- Add campaign cash or public treasury cash.
- Finish the active term with routine quarters.

Developer commands are typed commands through the same controller as normal gameplay. They mark the career save with `developerUsed`, and the status screen identifies the run. Scenario jumps are useful for testing future presidency and GUI work without playing every earlier turn. They deliberately replace the active timeline.

## Saving and importing

The default autosave is `saves/career.save`. The Game Menu can save to three manual slots. Saves use a versioned, validated command journal rather than Java object deserialization. Loading replays accepted commands with the same seeds, event draws, opponent choices, career phases, and pending event responses.

The loader can import the previous campaign-events `campaign.save` format from the same saves directory. The old file remains unchanged. The imported campaign starts a new career record at its current campaign point; earlier elections are not invented.

Change `CareerSave.FORMAT` when changing replay semantics, phase rules, or event-catalog behavior. There is no automatic migration for future format changes beyond the explicitly supported legacy campaign import.

## Source structure

There are 19 production classes in this release:

| File | Responsibility |
|---|---|
| `CareerEngine.java` | Campaign-to-presidency state machine, career record, term and comeback rules |
| `CareerCommand.java` | Typed career/campaign/governance/rebuild/developer commands |
| `CareerView.java` | Immutable career dashboard for terminal or future GUI |
| `CareerReport.java` | Accepted/rejected command result |
| `CareerSave.java` | Versioned career save and legacy campaign import |
| `CampaignOpening.java` | Validated resources carried into a new campaign |
| `GameEngine.java` | Campaign turns, opponent, events, objectives, and election result |
| `TextUI.java` | Guided menus, wrapping, pages, search, confirmation, and display |
| `GameView.java` | Immutable campaign snapshot |
| `GameCommand.java` | Typed campaign commands |
| `TurnReport.java` | Campaign command result |
| `CampaignEvent.java` / `EventCatalog.java` | Event data and 36 authored events |
| `President.java` / `State.java` | Campaign resources and immutable state cards |
| `ElectoralCollege.java` / `ElectionResult.java` | Roster and validated final tally |
| `ElectionGUI.java` | Existing optional Swing results viewer |
| `PresidentialSimulator.java` | Launch options and application entry point |

A renderer creates `CareerCommand` objects, sends them to `CareerEngine.submit`, and renders `CareerView` plus `CareerReport.messages()`. It never changes engine fields. `CareerView.campaign()` exposes the campaign snapshot for the state cards and event response screens.

## Build and test

```text
javac -Xlint:all -d build *.java tests/*.java
java -Djava.awt.headless=true -cp build CareerTests
java -Djava.awt.headless=true -cp build CareerUITests
java -Djava.awt.headless=true -cp build EngineTests
```

If `javac` is unavailable but Java's compiler module is present, use:

```text
java -m jdk.compiler/com.sun.tools.javac.Main -Xlint:all -d build *.java tests/*.java
```

Validation covers natural wins/losses, election-review transitions, transition credits, quarterly ledgers, final-year carryover, loss reputation and donor withholding, four-year comeback, nonconsecutive terms, two-term cap, developer jumps, save/load, legacy import, invalid commands, manual menus, pending events, and save failures. See `ARCHITECTURE.md`, `EVENTS.md`, and `VALIDATION.md`.

## Boundaries for later work

The structure is intentionally ready for additional presidency systems. Later additions can introduce policy, scandals, wars, crises, approval/reputation numbers, legislative support, and a dictator path as new commands, event effects, or phase rules. The current version establishes the lifecycle and interfaces without pretending those systems already exist.
