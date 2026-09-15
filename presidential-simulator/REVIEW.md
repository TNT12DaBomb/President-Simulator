# Source audit and revision notes

## Confirmed problems in the supplied source

| Finding | Evidence in original code | Change in this revision |
|---|---|---|
| The initial election is guaranteed | `calculateStateScore()` adds starting approval, economy, and trust (50 each), with random variation from −20 to +19; the minimum before choices is 130, but victory requires only `score > 60`. All offered setup choices preserve a guaranteed win. | Removed the threshold calculation. Explicit state objectives determine outcomes, and both winning and losing campaigns are tested. |
| States lack meaningful differences | All 50 state leanings and D.C.'s leaning are initialized to zero. | Each state has fictional objectives and a visible starting ownership rule; these are not real leanings. |
| Difficulty does nothing | `difficulty` is read but never subsequently used. | Normal and Hard have different starting funds. |
| One running-mate benefit does nothing | Party Insider changes party support, but party support is absent from the election formula. | Every running mate changes campaign costs or available funds. |
| Some choices are equivalent or self-cancelling | Economy and social-issue choices each add 10 to the same overall calculation. Attacking adds 10 approval and 10 scandal, cancelling exactly. | Separate actions fulfill distinct objectives; costs and remaining objectives are displayed. |
| Almost no campaign play | `campaignPhase()` accepts only one choice, then immediately runs the election. | Eight campaign turns, per-state actions, fundraising, and announced opponent challenges. |
| Unused systems imply gameplay that does not exist | Treasury, health, security, international relations, and party support do not affect the result. | Replaced the old numerical attributes with a focused campaign resource model. Governing systems are explicitly outside this release. |
| Stats have no bounds | Every modifier simply adds its amount. | Removed the unbounded numerical ratings; funds cannot be spent below zero and turns cannot exceed eight. |
| Invalid input can crash or bypass decisions | `Scanner.nextInt()` throws on text; invalid menu choices print an error and continue. | Line-based validated input, re-prompting, cancellation, and clean EOF handling. |
| A tie is labelled as defeat | Every result below 270 takes the loss branch. | Explicit tie outcome in console and GUI. |
| Players cannot inspect why they won or lost | Result object records only state booleans and tallies. | Immutable state explanations, current objectives, and a complete decision log. |
| Result totals can contradict outcomes | Original `ElectionResult` accepts arbitrary EV totals independently of its map. | Derives totals from validated complete outcomes, rejects missing/extra states and invalid totals. |
| Desktop assumptions can break execution | GUI always opens; Swing is constructed outside its event thread. | Console-only mode, automatic headless fallback, and event-thread scheduling. |
| Uploaded filenames do not match public classes | Filenames carry `(2)` suffixes. | Correct canonical filenames in the ZIP. |

## Intentional changes to assess before adopting

This is a transparent **board-game campaign**, not a public-opinion simulation. The player is unnamed, state objectives are invented, and support is determined by completion rules rather than quantified candidate ratings or election probabilities. The original approval/economy/trust system and four original running-mate identities are replaced, not silently retained.

The fixed starting bloc is every third entry in the alphabetic state list, beginning with Alabama. That yields 186 EV. The scripted challenges can remove Texas's 40 EV and Illinois's 19 EV, yielding 127 EV if the player does nothing. The two-objective setup is deliberately simple enough to inspect and test. A completed field office can recover a challenged state, so missing a deadline does not make later action useless.

The seed changes objective pairs, not the starting bloc or opponent challenge targets. All changes are causal and deterministic after setup. There is no popular-vote model, polling model, adaptive opponent, or hidden opposition rating.

## Existing strengths preserved

The original roster's arithmetic totals 535 state EV plus 3 for D.C. The separate D.C. representation, cloned state array, and read-only result-map approach were useful foundations. The revision keeps those properties and extends defensive copying to objectives and history. Swing remains a results viewer, while the campaign stays text-based.

## Further changes worth considering

1. **Campaign variety:** the same starting bloc and two challenges will become solvable. Add authored scenarios with distinct objectives, budgets, and announced event schedules. The tests establish reachable outcomes, not long-term replayability or balance.
2. **Save/load:** persist seed, difficulty, running mate, and action history. Validate loaded actions before resuming. There is currently no persistence between runs.
3. **Governing loop:** introduce an explicit transition from campaigning to office, then a separate turn loop and ending conditions. None of the supplied files implemented a term in office or reelection.
4. **Election detail:** this version preserves winner-take-all handling of every state from the original code. It does not implement district allocations in Maine/Nebraska, popular-vote counts, or contingent elections. Treat those as separate rules work rather than implying they exist.
5. **More accessible interaction:** the board is verbose in a terminal. A future campaign screen could show selectable state cards, outstanding objectives, and remaining resources without scrolling through 51 rows.
6. **Architectural separation:** `President` currently owns campaign state for compatibility with the six-file structure. As the project grows, introduce separate `Campaign`, `GameSession`, and governing classes rather than loading every responsibility into `President`.
7. **Further content:** policy choices, crises, and alternate political paths need their own authored rules and consequences. They have not been added by this bug-fix package.

## Reading the result

State explanations show whether the player completed both objectives, retained an unchallenged starting state, defended/recovered a challenged state, or left a challenge unanswered. The history tab records each successful turn and its actual resource cost. Views and rejected actions are deliberately omitted because they do not change game state.
