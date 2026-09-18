# Presidential Simulator v0.5 — Debate & Interface Edition

Extract this entire folder. Install Java 17 or newer if needed.

- Windows: double-click `run-windows.bat`. The game opens in its own window.
- macOS/Linux: run `sh run.sh`, or open the JAR with Java if your desktop supports it.
- Terminal prototype: `java -jar presidential-simulator.jar --terminal`.
- Source build: `sh build.sh` or `build-windows.bat` (JDK 17+).
- Tests: `sh test.sh` (JDK 17+).

Swing is included with Java; no JavaFX, Maven, network service, account, or extra GUI dependency is required. This is a desktop application, not a hosted website or native installer. Java must already be installed. Windows/macOS device testing is still needed.

## Start playing

Choose **New career**, a difficulty and a running mate. The seed makes a run reproducible. **Map** shows state boundaries; hover for information, then select a state to see objectives and action costs in the fixed inspector. Small Northeast states and DC have enlarged callouts. The selector provides a keyboard alternative. Each campaign action spends a turn; fundraising replenishes your resources. Debates occur on turns 4, 8 and 12. Finish the 16-turn campaign to reach election review.

New decisions automatically open the **Decisions** page. You can switch back to Map without losing the selected state; a footer reminder remains while a decision is waiting. Click an answer; unavailable choices are disabled. A live response clock counts actual elapsed seconds. Help, modal dialogs, file operations, and switching to another window pause it. Other event deadlines depend on game turns. Settings can disable live countdowns while retaining in-game deadlines.

After an election, **Dashboard** continues into transition, presidency or rebuilding. The office offers monthly actions and **End month**. **Policy** exposes pledges and the legislative sequence. Invalid actions display a visible notification immediately. Routine notifications disappear after a short interval; important updates remain until dismissed. Details opens the full update without navigating to another page. Stats remain at the top; **Statistics** contains all metrics and **History** preserves your record.

## Saves

**Menu → Save / Load** offers three named-number slots, quicksave, transition autosave and an open-file picker. Saves live under your home folder in `PresidentialSimulator/saves`. Career phase changes autosave; individual actions do not. Use manual save or **Save and exit** to preserve later actions. Three rotating backup generations accompany replaced saves. Loading replaces the current career only after successful replay; failed reads preserve your session. A failed save leaves the window open with a visible error notification.

File work runs off the Swing event thread, with commands and clocks frozen until it completes. Current saves use presidency-debate-protocol-v2. Earlier saves are intentionally rejected because debate command sequences changed. There is no legacy format migration.

## Scope and model limits

The GUI uses the existing simulation without adding new political scoring. Values describe a fictional game model; electoral projections are not real-world polls. Office funds are administrative resources, not a federal budget. Growth and other national metrics retain the existing model's assumptions (see MODEL-NOTES.md and REALISM-AUDIT.md).

This release has a bundled geographic map with an independently scrollable state inspector. Geography is a simplified display, not a survey map. Schedule uses actual campaign turns rather than inventing dates or appointments. Office now has dedicated Cabinet, Public requests and Midterms tabs connected to those existing systems. General office actions, legislation, world responses, election reviews and career continuation are connected. Developer scenarios are available and mark the career as modified.

## Next GUI milestone

1. Richer cabinet roles, request filtering and more detailed midterm presentation.
2. Map search, zoom/pan and more state overlays.
3. Expanded debate content, transcript browsing and accessible large-text presets.
4. Save-slot metadata cards, backup recovery and configurable autosave frequency.
5. Keyboard shortcuts, focus/assistive-technology review and Windows/macOS playtesting.
6. Installer/runtime bundles after device testing; no engine rewrite required.

## Seed behavior

The same seed, difficulty, running mate and commands reproduce the same run. A save restores its stored seed and command history. New career setup offers a Randomize seed button.

The existing model deliberately uses fixed fictional state baselines. The seed changes objectives, event draws, opponent actions and later random variation, but does not randomize the opening baseline. A repeated opening projection therefore does not indicate a broken RNG. This UI release does not change the simulation model or invent a new election forecast. A separately specified alternate scenario system remains backlog work.

## Laptop layout

The launcher uses the current monitor's usable bounds and subtracts taskbar/dock insets. It works in Java's logical display coordinates for scaled desktops. The map scales proportionally; detail pages scroll independently. Navigation stays compact with secondary controls under Menu. Automated renders cover 1280×680, 1024×600 and 800×480 content areas. Actual OS chrome, assistive technology and Windows/macOS hardware testing remain necessary; no claim of universal pixel-perfect fit is made.

## Map source

The offline map uses [US Atlas](https://github.com/topojson/us-atlas), derived from the Census Bureau's 2017 cartographic boundaries in an Albers USA projection. Alaska and Hawaii use insets; Northeast states and DC also have enlarged click targets. Its license is included under assets. Map colors read the existing state snapshot; they do not represent real party affiliations. Maine/Nebraska split allocations remain available in State details.

## v0.4 changes

**Map:** The custom Swing component now explicitly clears its background on each paint. Its previous opaque flag did not clear pixels because the component had no painting delegate. Old hover outlines and resized map pixels could remain. Pixel-comparison tests now cover repeated hovering, resize and clipped repaints. Blue means your statewide lead, red means the opponent's; these are team colors, not modeled party identities. Gold marks selection.

**Debates:** Prepare briefings, answer shuffled factual questions, choose confidence, hear an opponent claim, then clarify, concede or dispute it. Opponents can be wrong. Feedback cites official sources. Policy answers and revisions stay in a transcript and may be quoted in later debates, including subsequent campaigns. See DEBATE-DESIGN.md for timings and current limitations. The new protocol has no numerical electoral effect yet.

**Office:** Cabinet lets you choose a department, appoint or nominate a candidate, secure support, confirm when eligible, and delegate an annual project. Removing an appointment requires confirmation. Public requests lets you acknowledge a request and track its delivery status. Midterms lets you complete listening and organizing visits when the term calendar permits. All share the existing monthly action allowance. Completed and unavailable actions explain their status; selectors and tabs persist while you work.

**Clarity:** Policy now previews benefit, tradeoff, signing cost and delivery time, and shows the current bill's stage. Setup selectors use readable labels. Help scrolls within the laptop-sized dialog. Immediate notifications remain visible without opening the report screen. This release aims for clear, satisfying feedback rather than artificial rewards for repeated clicks.

Run the included test.sh to reproduce model, desktop and new regression tests. Native Windows/macOS device testing remains outstanding.


## v0.5 layout fixes

Notifications reserve a fixed strip. Office/Policy controls wrap to available width; Policy uses separate initiative, bill and promise tabs. The presidency header adds existing GDP growth, unemployment, inflation and Congress figures. Hovering a state updates a permanent preview; eastern geographic shapes and enlarged callouts both work.

BACKLOG.md now groups the full career, media, campaign, institutional and pressure-event roadmap with honest delivered/deferred status.
