# Revision review

## What changed from the supplied directory

The supplied version had an eight-turn objective campaign, terminal-owned turn flow, and two hard-coded opponent challenges. This release preserves the fictional state-objective foundation while expanding it into a sixteen-turn, event-driven session.

| Area | Supplied revision | This release |
|---|---|---|
| Campaign control | Terminal class mutates the player directly | `GameEngine` accepts typed commands |
| Opponent | Scheduled Texas/Illinois challenges | Own cash and work; one action every second turn |
| State rules | Starting states mostly protected by special rules | Same two-objective comparison for both sides |
| Randomness | Objective layout only | Separate seeded event and opponent streams |
| Events | None | 36 definitions across six categories and five effect kinds |
| Response choices | None | Six optional offers, with exact costs and a free decline |
| Temporary effects | None | Prices and fundraising proceeds change for upcoming turns |
| Persistence | None | Versioned replay autosave, including pending responses |
| Text interaction | Long board and action-number prompts | Guided welcome, dashboard, search, seven-row pages, state details, confirmations |
| Distribution | Compile the sources | Prebuilt JAR plus launch and rebuild scripts |

## Design choices

- **No direct event-to-EV shortcut.** Events change cash, completed work, or upcoming costs. The normal ownership rules still determine each state's electoral votes.
- **Explicit ties in state objectives.** If both teams complete their work, the starting owner retains the state. The same applies if neither team finishes. This keeps ownership explainable without a hidden candidate rating.
- **Events report actual effects.** A cash loss capped at zero reports the actual amount removed; a completed or reopened task names the state.
- **Pending events block advancement.** A final-turn offer must be resolved before the result can be displayed. Declining never costs money or another turn.
- **Effects count turns, not action uses.** The dashboard shows how long each modifier remains active. This avoids an unused discount lasting forever.
- **One save slot with confirmation.** New games ask before replacing the default autosave. The CLI can select a different slot using `--save`.
- **Plain-text default.** No cursor clearing, timed input, required color, or decorative character dependence. Optional heading color is available with `--color`.

## Limitations and next development targets

1. **Balance and variety:** automated tests verify rules and reachable outcomes, not player enjoyment. The starting bloc is unchanged and the opponent uses a modest scripted decision procedure. Human playtesting is the next useful step.
2. **Event breadth:** there are 36 authored operational events, not an exhaustive real-world campaign simulation. Some share an effect type, while others offer choices or alter future costs. Political ratings and real personalities are not modeled.
3. **Event pacing:** one unused eligible event is drawn each turn. Some events strengthen work without flipping a state immediately. Final-turn cash changes affect resources but do not change EV on their own.
4. **Save migrations:** replay saves are tied to this rules/catalog version. Bump the format identifier when changing replay behavior; future migration tools would be a separate feature.
5. **Native installation:** the package needs Java 17+. It does not bundle a runtime or include a native installer. The Windows launcher and graphical results view still need testing on a Windows desktop.
6. **Full GUI:** rules are now decoupled, but the campaign remains text-based. A future GUI should use `GameView` and `GameCommand`, rather than duplicating the engine's decisions.
7. **Scope:** governing, reelection, district allocations, popular-vote totals, and contingent-election gameplay remain unimplemented.

The production source set is now fourteen classes. The old six-file API has changed; migrate other callers to `GameEngine`. Tests and source-level extension documentation are included.
