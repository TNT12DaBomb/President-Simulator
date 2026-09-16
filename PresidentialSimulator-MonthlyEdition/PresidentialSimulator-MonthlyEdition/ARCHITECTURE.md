# Architecture

The 25 production Java sources have no external dependencies. The supplied twenty sources are retained and integrated with five new domain types.

| Layer | Responsibility |
| --- | --- |
| `PresidentialSimulator`, `TextUI`, existing `ElectionGUI` | Launch, input validation, menus, display; Swing remains the existing election-results adapter |
| `CareerEngine` | Career phases, elections, inauguration, total service, term limits, comeback, carryover, replay journal |
| `Presidency` | One term's two-action monthly budget, administrative records, bills, pledges, events, delayed work, seeded RNG |
| `Congress` | Authored seats/control, one-bill agreement, deterministic midterm branch |
| `Policy`, `PresidencyEvent` | Issue/bill/promise values; immutable event catalog, prerequisites, immediate/delayed choices |
| `CareerCommand`, `OfficeCommand`, `CareerView`, `CareerReport` | Typed input and immutable snapshots/reports; no renderer required |
| `GameEngine` and campaign models | Existing state-objective campaign and event resolution |
| `CareerSave`, `CampaignSave` | Versioned properties serialization and accepted-command replay |

A future GUI submits `CareerCommand` objects and renders `CareerView`; office-specific data is in its nested `Presidency.View`. Controllers own mutable state and publish copies of lists and immutable records. No UI methods occur inside the career or presidency model.

A normal month executes actions independently, then an END_MONTH command. End-month order: settle routine resources; increment month; apply due follow-ups; run month-24 midterms; close public annual correspondence; draw an eligible event if configured and not at term end. CareerEngine then captures annual carryover and handles term completion. The final month settles existing follow-ups but never introduces an unresolved new event before term review.

Campaign promises are distinct from signed policies. The active policy map reflects the latest signed approach; the command/history archive retains reversals. One active bill prevents silent replacement. All preconditions are checked before consuming actions or cash. Legislative disagreement is an explicit prerequisite, not a hidden probability roll.

Congress and policy effects are intentionally shallow authored game rules. New mechanics should extend typed effects and views, with tests for persistence and phase boundaries. Do not add console prompts inside engines. Do not silently change replay behavior under `presidency-monthly-v1`: introduce a version or explicit migration when the same command sequence would produce a different game.

The general event framework supports eligibility, weighted random selection, immediate treasury/messages, delayed treasury/messages, and mandatory responses. It does not yet support arbitrary predicates/effect scripting, recurring events, wars, or scandal state. Add those as typed domain fields, not string parsing.

`ROUTINE_QUARTER` and `FINAL_QUARTER` remain internal enum identifiers from the supplied source; their displayed labels and runtime behavior are now monthly. There is no runtime dependency on old quarterly saves or quarter counters.
