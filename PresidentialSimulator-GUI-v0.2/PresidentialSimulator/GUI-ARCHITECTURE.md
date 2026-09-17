# Desktop adapter

DesktopLauncher owns the window, focus lifecycle and 100ms Swing timer. DesktopPanel renders immutable CareerView / GameView snapshots. DesktopController owns the current CareerEngine and submits existing typed commands. The engine imports neither Swing nor terminal code.

All controller access occurs on the Swing event dispatch thread. Saves run on a SwingWorker while model mutation is locked; loads create a separate engine off-thread and adopt it only after success. UI painting and dialogs remain responsive. Phase-transition autosaves use the same serialized mechanism. No concurrent writes are queued against a mutable engine.

Clock pulses convert monotonic elapsed time into whole-second CLOCK_TICK commands. Intermediate ticks update only the timer label; they do not recreate controls or reset focus. Expiry refreshes the decision and report. Answer callbacks capture a decision identity and settle elapsed time before checking that identity, preventing a late queued click from answering the next debate question. Pauses are nested for dialogs and focus changes. Settings retain the engine's real-time toggle; game-turn deadlines still apply.

DesktopPanel can be constructed and painted without a native window, enabling headless button/controller integration tests and actual Swing render captures. Native window activation, modal interaction and operating-system launch behavior still require device testing.

The terminal is retained as a secondary entrypoint and is frozen for feature development. No gameplay code was changed in this GUI milestone. Existing procedural and realism limitations remain documented; UI wording avoids inventing calendar dates, candidate polling or geographic data.
