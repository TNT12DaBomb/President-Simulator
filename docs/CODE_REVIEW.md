# Prototype review

Reviewed September 14, 2026. Scope: the six uploaded Java files. This is a source review, not a compiled or interactively tested build. The environment has a Java runtime but no `javac` compiler.

## Strengths to preserve

- Separate classes already distinguish player data, state data, election calculation, result data, and rendering.
- `State` is immutable; `ElectionResult` copies its input map and exposes an unmodifiable map.
- The electoral allocation is validated at construction.
- `getStates()` returns a cloned array.
- Only standard-library dependencies appear in the files.

## Findings

| Priority | Location | Finding | Next action |
| --- | --- | --- | --- |
| Blocker | `ElectoralCollege.calculateStateScore` | Every normal menu path wins every state and D.C.; randomness cannot overcome the starting advantage. | Replace the unbalanced placeholder model; add deterministic fixtures demonstrating both win and loss. |
| High | `PresidentialSimulator.main` | `difficulty` is assigned but unused. | Define documented difficulty behavior and pass configuration into the engine. |
| High | `PresidentialSimulator` | Console input and unconditional GUI creation prevent headless play. | Make console results the default; add an explicit GUI option. |
| High | `PresidentialSimulator` | `nextInt()` can throw on text or exhausted input. Invalid numeric choices continue without retry. | Centralize validated menus; handle EOF and cancellation cleanly. |
| High | `ElectionResult.playerWon` and both displays | A no-majority outcome is collapsed into a loss. | Introduce an outcome enum and a separately resolved no-majority branch. |
| Medium | `ElectoralCollege` | Randomness is constructed internally. | Inject the random source and retain seed/replay metadata. |
| Medium | `ElectoralCollege` | Every state's political leaning is zero. All differences in state outcomes come from random draws under an otherwise shared formula. | Use versioned fictional scenario data and distinct regional conditions. |
| Medium | `President` | Attributes are mutable without bounds or validation. | Define attribute semantics, enforce appropriate bounds, and separate campaign funds from government finances. |
| Medium | `President` and election formula | Party support, treasury, national security, health, and international relations do not affect the election calculation. | Explain their future responsibilities; avoid cosmetic choices presented as meaningful strategy. |
| Medium | `ElectionGUI.showResults` | Swing components are constructed from the main flow rather than scheduled on the event-dispatch thread. | Create and update Swing views through `SwingUtilities.invokeLater`. |
| Medium | Election data | Maine and Nebraska have a single state-level winner. | Label simplified mode or implement districts before claiming faithful allocation. |
| Low | `ElectionResult` | The win threshold is duplicated; constructor accepts inconsistent totals. | Share rules configuration and validate result invariants. |
| Low | `ElectionGUI` | Unboxing a missing map value could fail if a future result is incomplete. | Define completeness validation at the result boundary. |
| Low | Uploaded filenames | Public class names do not include `(1)`, but uploaded filenames do. | Normalize filenames in the repository; done in this package only. |

## Why the sweep is a defect

Inspection of the initial attributes, all running-mate effects, all campaign effects, the random modifier's range, and the winning threshold proves the current reachable election paths cannot lose. This is a property of the program's placeholder arithmetic, not a statement about any real candidate or election. Repeating simulated runs is unnecessary to establish this bug.

## Meaningful tests to add with the engine changes

1. Electoral allocations sum correctly and every jurisdiction is represented exactly once.
2. Explicit fixtures cover player majority, opponent majority, and no majority.
3. Same seed, configuration, content version, and action sequence produce the same result.
4. Invalid actions neither consume a turn nor mutate resources.
5. Input errors retry; EOF exits gracefully; console mode never creates a Swing window.
6. Save/load continuation matches uninterrupted play; unsupported save versions fail clearly.
7. Difficulty changes the documented resources or opponent behavior, not just menu text.
8. Event eligibility and cooldowns prevent impossible or repeated one-time events.

Do not certify balance using a single successful run. Use deterministic regression fixtures plus small playtests and batch analysis after the replacement engine exists.

## Rules reference

The supplied allocation matches the National Archives table for 2024/2028. The Archives also describes Maine/Nebraska district allocation and the congressional process when no candidate receives a majority. [Allocation](https://www.archives.gov/electoral-college/allocation) · [FAQ](https://www.archives.gov/electoral-college/faq)
