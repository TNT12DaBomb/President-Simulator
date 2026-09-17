# v0.2 Desktop validation

- All production and test sources compile under Java 17 with `-Xlint:all`.
- EngineTests: 673,373 checks; 320 full campaigns.
- DebateTests: 60 checks.
- WorldTests: 9,968 checks.
- InterfaceTests: 85 checks.
- StableScreenTests: 393 checks.
- DesktopTests: 14 integration checks: actual action button, campaign election review, inauguration, monthly progression, clock pause/tick/expiry, stale-response rejection, asynchronous save/load and failed-load preservation.
- Actual Swing panel renders: GUI-CAMPAIGN.png, GUI-DEBATE.png, GUI-OFFICE.png at 1320 × 850; inspected for persistent layout and readable decisions.
- Built JAR defaults to DesktopLauncher; headless execution prints a useful terminal fallback command.

No native display server is available here. Windows/macOS launchers, focus events, native dialogs, screen readers and live resizing require device testing. Screenshot rendering is not a substitute for those checks. Autosave wiring is implemented; desktop tests exercise the shared asynchronous save path, not every OS shutdown scenario. The previous terminal-only validation is preserved below.

---

# Interface Playtest validation

Java 17 on Linux; 46 production sources compile with UTF-8 and `-Xlint:all` without warnings. The release supports one CURRENT ruleset and only `presidency-interface-v1` career saves. Compatibility suites and old golden fixtures have been retired from this release.

| Suite | Passed checks | Coverage |
| --- | ---: | --- |
| EngineTests | 673,373 | Campaign component invariants, 320 campaigns, original event effects, wins and losses |
| DebateTests | 60 | Three-question progression, timeout, leaving, persistent election effect, immutable results and save replay |
| WorldTests | 9,968 | Current-rule careers, resources, event consequences, economic bounds, two terms, opposition, election/midterm effects and save replay |
| StableScreenTests | 393 | Simulated viewport across four sizes; input/cursor preservation, fixed timer row, no renderer linefeeds, no scrolling, differential redraw and wrap restoration |
| InterfaceTests | 85 | Title/game help, all help topics, new career setup, live countdown, pause/resume, manual slots, full text, statistics, office menus, month confirmation and old-format rejection |

The real-time integration test starts a debate with three seconds remaining, opens help, waits 3.2 seconds, verifies no expiry, then returns. Without further input it observes `Time left: 2s`, `Time left: 1s`, expiry, and an autosaved second question. It also verifies screen restoration on exit. This uses the actual input queue, UI loop and elapsed monotonic clock, not mocked timer calls.

The narrow-screen test renders a long title, status and description across a 60×20 frame's pages. All end markers survive, no ellipsis is introduced, and output lines stay inside the configured width. Default-size help, debate and presidency previews are captured from real UI output.

Guided interaction checks cover new career setup; help before and during play; invalid input; all three debate answers; debate stats; a named manual slot and loading it; cabinet/administration, legislation, public, election and statistics menus; and a confirmed month advance. Browsing leaves the game snapshot unchanged.

Run `sh test.sh` to reproduce. `sh build.sh` produces the runnable JAR.

Limitations: Windows launchers and Windows console rendering were not executed on this Linux host. The current input system requires Enter after a selection. Automatic window-resize detection, native mouse input, and a guided tutorial career are not implemented. Tests do not prove usability for every player or empirical realism of political/economic coefficients.


## Stable-screen patch verification

StableScreenTests and InterfaceTests passed after the renderer changes. A real Linux PTY integration check also passed: an actual 80×24 terminal received an oversized 100×28 request, which was capped correctly. Help/back/exit used one initial screen clear and no renderer-generated newlines. The only linefeeds observed were the console's echo of three Enter presses. Run `python tests/terminal_pty.py` after building to reproduce (POSIX only).

The Windows viewport probe and batch launcher were inspected, but not executed on Windows. In-place window resizing during an active prompt is not automatically detected; adjust Display size or reopen the game. The existing numerical gameplay suites are unchanged; this patch changes presentation and startup sizing only.
