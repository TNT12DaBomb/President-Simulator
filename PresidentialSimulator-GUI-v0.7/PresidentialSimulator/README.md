# Presidential Simulator v0.7 — Debate Windows

Extract the ZIP and install Java 17 or newer. On Windows, double-click `run-windows.bat`. On macOS/Linux, run `sh run.sh`. The bundled JAR opens a desktop window. No extra GUI libraries or online account are needed.

## Your campaign

**You are BLUE. Your opponent is RED.** The setup screen, campaign dashboard, persistent header and map identify your team. Blue/red are the current game's team colors; gold marks the selected state. Select a red state to challenge the opponent there, or strengthen work in a blue state. Hover previews and enlarged eastern callouts remain available.

## Choose a question mode

New career setup offers **Politics** (52 questions) or **Fun** (88 questions). The live bank contains 140 cards. Each appearance selects three distinct topics, preferring unseen questions. Fun now includes literature, music, computing, chemistry, genetics, mathematics, Earth science, space and Star Wars.

Both modes include a separate governing-policy question after the knowledge rounds. Policy preferences are not marked correct or incorrect. The public statement is recorded immediately and follows you into office, even if you leave the debate before the clarification round.

## Preparation and team practice

Each debate starts with **6 briefing slots**. Reviewing one topic costs **2 slots**, not campaign cash or a campaign turn. Purchased notes appear before/during the relevant questions. Slots reset at the next debate; repeated note reading is free.

**Practice with your team** is available below the question mode in Settings and during debate preparation. It runs the full debate sequence with a separate session and statement record. It spends no career resources and creates no public commitments. Practice defaults to untimed; enable rehearsal clocks if desired.

## Timers, reading and feedback

| Stage | Standard | Relaxed |
| --- | --- | --- |
| Knowledge answer | 30 seconds | 60 seconds |
| Confidence | 15 seconds | 30 seconds |
| Opponent delivery | 5 seconds | 10 seconds |
| Recovery | 20 seconds | 40 seconds |
| Policy / record | 40 seconds | 80 seconds |

Preparation, source feedback and debriefs are untimed. Untimed mode removes every debate deadline. Dialogs, file operations and switching windows pause the live career clock.

Correct factual answers receive a green **Well answered** banner, whether challenged or accepted without a challenge. Incorrect answers get a distinct correction; withdrawing a claim gets neutral source review. Feedback separates your original answer from the opponent's claim, which may itself be wrong. It does not score the worth of a candidate or a political position.

## Settings

Menu → Settings includes question mode, Standard/Relaxed/Untimed pace, the master live-clock switch, debate text size (16–24, default 18), correct-answer celebrations and presidency event frequency. Mode and pace changes apply to the **next** debate; text and feedback style update immediately. Turning off the master clock pauses a current live response. Preferences are included in career saves. Before starting a career, settings become the defaults for the next setup in that app session.

## Governing continuity

Policy → Promises lists debate commitments. In office, **Introduce promised initiative** opens the matching proposal using the normal action allowance. The initiative preview also shows relevant prior statements. Proposals, signatures and vetoes append a comparison with those statements to History. A proposal is not treated as a law already delivered. Numerical reputation/media effects are not added by this release.

## Saving and building

Menu → Save / Load offers slots, quicksave, autosave and file opening. Phase transitions autosave; use manual save or Save and exit for later actions. Saves live in `PresidentialSimulator/saves` under your home directory. Current format: `presidency-debate-settings-v3`. Old-format saves are intentionally unsupported; their files are not modified by a failed load.

Build source with `sh build.sh` or `build-windows.bat` using JDK 17+. Run focused debate tests with `sh test-debates.sh`. Terminal access remains `java -jar presidential-simulator.jar --terminal`; the expanded setup/settings and team-practice screens are desktop features.

Native Windows/macOS scaling and assistive-technology testing remain outstanding. This is a fictional simulation, not a political forecast. See MODEL-NOTES.md and REALISM-AUDIT.md for the inherited model's limits, and BACKLOG.md for future work.


## v0.7 navigation
Regular debates open in a dedicated scrollable window with persistent clock and large response buttons. Close or choose **Pause and return to campaign** to suspend the appearance; use **Decisions → Open / resume live debate** to return. Practice is offered only below the question mode in Settings and during debate preparation. Each mode has three fixed tutorial questions, separate from the live bank, plus a rehearsal-only planning exercise. Tutorial results never enter career history.

Review buttons precede Begin debate. Feedback has one Continue button; closing has one Return to campaign button. Answer options remain shuffled so their position does not reveal correctness.

**Known unresolved issue:** opening map generation still uses fixed geography, and the current live debate protocol does not change voting calculations. See SEED-AUDIT.md. This release does not fix those simulation issues.

Run `sh test-debates.sh` for the focused v0.7 regression checks. The older full-suite script includes assumptions about the previous inline debate interface and has not been revalidated for this release. Saves use a new format; start a new career.
