# Validation performed

Environment: OpenJDK 17.0.20, Linux, headless.

- All 14 production Java sources and both test classes compile with `-Xlint:all`, without warnings.
- `EngineTests`: **673,373 assertions passed** across **320 full campaign sessions**, plus focused regression cases. This is an assertion count, not a measure of game balance.
- All **36 event IDs** were exercised by complete sessions. No event repeated within a tested session. Both winning and losing game endings were reached.
- Verified exact cash effects for each side, cash-loss clamping, task gains/removals, task eligibility, paid-response atomicity, unavailable response handling, price/fundraising floors, modifier expiry, final-turn response blocking, and finalization after response.
- Verified nonnegative cash, valid completed objectives, the ownership rule, 51 jurisdictions, and conserved 538 EV after every accepted command in the session suite.
- Verified deterministic full snapshots with identical seeds and commands, even when one controller makes extra snapshot reads.
- Verified snapshot immutability, invalid/duplicate/unaffordable command rejection, turn limits, and premature/final-state command rejection.
- Verified save/load throughout a session, exact pending-event restoration and continuation, completed saves, old-format rejection, and malformed replay rejection.
- Retained an explicit 269–269 result test and the headless Swing fallback test.
- `TextUITests`: **18 guided-interface checks passed**, including search, state detail, navigation without spending turns, cost confirmation/cancellation, autosave, overwrite protection, EOF, a full game, pending-event exit, and visible save failure.
- The production JAR was compiled separately from test classes and its launch/help path and a guided action/save path were exercised. The Linux build and launch scripts also ran successfully.

Not verified: native Windows batch execution, Windows terminal appearance, visual rendering of the Swing results window, screen-reader behavior, and subjective game balance. The interface was exercised through its actual input/output adapter on Linux. No desktop visual inspection is claimed.
