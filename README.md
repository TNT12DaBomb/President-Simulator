# Presidential Simulator v0.2 — Desktop Prototype

Extract this entire folder. Install Java 17 or newer if needed.

- Windows: double-click `run-windows.bat`. The game opens in its own window.
- macOS/Linux: run `sh run.sh`, or open the JAR with Java if your desktop supports it.
- Terminal prototype: `java -jar presidential-simulator.jar --terminal`.
- Source build: `sh build.sh` or `build-windows.bat` (JDK 17+).
- Tests: `sh test.sh` (JDK 17+).

Swing is included with Java; no JavaFX, Maven, network service, account, or extra GUI dependency is required. This is a desktop application, not a hosted website or native installer. Java must already be installed. Windows/macOS device testing is still needed.

## Start playing

Choose **New career**, a difficulty and a running mate. The seed makes a run reproducible. **States** shows the actual state objectives and action costs. Each campaign action spends a turn; fundraising replenishes your resources. Debates occur on turns 4, 8 and 12. Finish the 16-turn campaign to reach election review.

The current decision stays below your selected page. Click an answer; unavailable choices are disabled. A live response clock counts actual elapsed seconds. Help, modal dialogs, file operations, and switching to another window pause it. Other event deadlines depend on game turns. Settings can disable live countdowns while retaining in-game deadlines.

After an election, **Dashboard** continues into transition, presidency or rebuilding. The office offers monthly actions and **End month**. **Policy** exposes pledges and the legislative sequence. Invalid actions produce an engine explanation in **Latest report**, accessible from every page. Stats remain on the right; **Statistics** contains all metrics and **History** preserves your record.

## Saves

**Save / Load** offers three named-number slots, quicksave, transition autosave and an open-file picker. Saves live under your home folder in `PresidentialSimulator/saves`. Career phase changes autosave; individual actions do not. Use manual save or **Save and exit** to preserve later actions. Three rotating backup generations accompany replaced saves. Loading replaces the current career only after successful replay; failed reads preserve your session. A failed save leaves the window open with an error in Latest report.

File work runs off the Swing event thread, with commands and clocks frozen until it completes. Current-rule save files use the existing format. There is no legacy format migration.

## Scope and model limits

The GUI uses the existing simulation without adding new political scoring. Values describe a fictional game model; electoral projections are not real-world polls. Office funds are administrative resources, not a federal budget. Growth and other national metrics retain the existing model's assumptions (see MODEL-NOTES.md and REALISM-AUDIT.md).

This release has an electoral **state list**, not a geographic map. Schedule uses actual campaign turns rather than inventing dates or appointments. Cabinet, correspondence and midterm field operations remain available in the terminal but do not yet have dedicated desktop controls. General office actions, legislation, world responses, election reviews and career continuation are connected. Developer scenarios are available and mark the career as modified.

## Next GUI milestone

1. Dedicated cabinet, correspondence and midterm screens; expose all existing commands.
2. Searchable geographic electoral map and richer state comparisons.
3. Full choice-effect previews, debate recap cards and accessible large-text presets.
4. Save-slot metadata cards, backup recovery and configurable autosave frequency.
5. Keyboard shortcuts, focus/assistive-technology review and Windows/macOS playtesting.
6. Installer/runtime bundles after device testing; no engine rewrite required.
