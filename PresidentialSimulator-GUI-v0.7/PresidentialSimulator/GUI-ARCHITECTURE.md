# v0.7 adapter additions

Desktop setup/settings submit validated debate preferences through CareerEngine. LiveDebate snapshots content/pace for each appearance; the renderer reads text/celebration preferences immediately. PracticeDebatePanel owns its own LiveDebate, statement list and clock. The career clock is paused while the rehearsal modal is open. Policy views expose recorded commitments and invoke normal proposal commands. Candidate/team identity is explicit in setup, header, dashboard and map. No new political rating model is introduced.

# v0.5 update

Fixed notification layout, geographic hover targets, width-aware Office/Policy controls and persistent presidency metrics. Added a staged PressureEvent debate protocol with preparation, confidence, claims, recovery, sourced feedback and career statement continuity. See DEBATE-DESIGN.md and BACKLOG.md; older entries below are historical, not current feature specifications.

# v0.4 adapter additions

ElectoralMap explicitly fills its background inside the supplied graphics clip before drawing. JComponent's default paintComponent does not fill an opaque background without a UI delegate. Tests compare repeated hover/resize and clipped painting against a fresh render.

DebatePresentation shuffles canonical option indices using the campaign seed and question ID. Rendering consumes no engine randomness. Command indices stay canonical, and ordering remains stable for that question. DebateSession prose changed; evaluation formulas and effects did not.

OfficeWorkspace renders existing immutable snapshots and submits existing OfficeCommand values. Selection stores tab/department/request/contest indices across refreshes. UI gates describe expected availability; the engine remains authoritative. No UI mutates Cabinet, PublicCorrespondence or MidtermCampaign directly.

Long buttons and wrapped text reserve enough vertical room in width-tracking scrollable panels. Menu notifications use ordinary compact buttons. Help uses the same bounded dialog mechanism as report details.

---

# v0.3 presentation changes

ElectoralMap renders bundled binary paths derived from US Atlas TopoJSON. Hit testing uses the same scaled shapes as painting; additional targets make Northeast states/DC accessible. A state selector provides keyboard access. DesktopPanel retains the selected name across snapshots and applies actions through existing commands.

Scrollable width-tracking columns prevent preferred text sizes from growing the window. The map and inspector use bounded BorderLayout regions. New decision identities auto-select a dedicated page. DesktopController emits structured Notice values for acceptance, rejection, deadlines and I/O results. DesktopPanel displays these immediately; full reports remain available through Details. No simulation model changed.

---

# Desktop adapter

DesktopLauncher owns the window, focus lifecycle and 100ms Swing timer. DesktopPanel renders immutable CareerView / GameView snapshots. DesktopController owns the current CareerEngine and submits existing typed commands. The engine imports neither Swing nor terminal code.

All controller access occurs on the Swing event dispatch thread. Saves run on a SwingWorker while model mutation is locked; loads create a separate engine off-thread and adopt it only after success. UI painting and dialogs remain responsive. Phase-transition autosaves use the same serialized mechanism. No concurrent writes are queued against a mutable engine.

Clock pulses convert monotonic elapsed time into whole-second CLOCK_TICK commands. Intermediate ticks update only the timer label; they do not recreate controls or reset focus. Expiry refreshes the decision and report. Answer callbacks capture a decision identity and settle elapsed time before checking that identity, preventing a late queued click from answering the next debate question. Pauses are nested for dialogs and focus changes. Settings retain the engine's real-time toggle; game-turn deadlines still apply.

DesktopPanel can be constructed and painted without a native window, enabling headless button/controller integration tests and actual Swing render captures. Native window activation, modal interaction and operating-system launch behavior still require device testing.

The terminal is retained as a secondary entrypoint and is frozen for feature development. No gameplay code was changed in this GUI milestone. Existing procedural and realism limitations remain documented; UI wording avoids inventing calendar dates, candidate polling or geographic data.
