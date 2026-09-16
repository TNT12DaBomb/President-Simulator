# Validation performed

Environment: OpenJDK 17.0.20 on Linux, headless mode.

- 19 production classes plus three test classes compiled with `-Xlint:all` without warnings.
- `CareerTests`: **1,490 assertions passed**, covering election-review transitions, wins/losses, natural and nonconsecutive reelection, four-year comeback, donor withholding and resolution, reputation/support effects, transition credits, quarterly treasury math, annual budget recovery, final-year carryover, two-term cap, developer fixtures, malformed commands, and exact career save/load.
- `CareerUITests`: **22 guided-menu checks passed**, covering friendly numeric input, state search, free browsing/cancellation, status/economy screens, developer victory through transition into office, manual save slots, load failure recovery, final-term developer scenario, pending-event menu access, legacy campaign import, and visible save errors.
- Existing campaign regression suite: **673,373 assertions passed**, with 320 full campaign sessions, all 36 event definitions exercised, and wins/losses reachable.
- Verified campaign and career snapshots are immutable and repeated view reads do not alter random state.
- Verified forced developer outcomes do not fabricate electoral vote totals and developer usage persists in the view and save journal.
- Built the production `career.jar` separately from tests. Its help path and a guided campaign/menu path were exercised.
- The Linux `run.sh` and `build.sh` scripts were executed successfully.

Not verified: native Windows batch execution, Windows terminal fonts/appearance, Swing visual layout on a desktop display, screen-reader behavior, or subjective balance and fun. Those require a Windows/desktop playtest.
