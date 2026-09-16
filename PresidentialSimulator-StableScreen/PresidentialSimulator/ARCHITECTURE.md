# Architecture — World Edition

## Stable-screen patch

Implemented fixed-position, changed-row rendering; footer-only countdown writes with preserved input cursor; no per-tick plain-mode lines; automatic wrap suppression during full-screen play; visible-window startup sizing on Windows; oversized launch-frame clamping; explicit Windows full-screen launcher. Verified with 393 VT-model checks, 85 UI checks, and a real 80×24 Linux PTY session. Windows device testing and live resize detection remain.

## Interface Playtest — current behavior

One CURRENT ruleset; older career save formats and import menus are intentionally unsupported. Help works before career creation, provides five short topics, and is available globally with ?. The footer displays actual elapsed countdown ticks; descriptions no longer repeat a frozen timer. Full text wraps and paginates without ellipsis clipping. Decision shortcuts stay in the prompt, report prose is complete, invalid input does not accumulate, and manual saves display confirmation. Default size remains 100×28.

Remaining UI work: automatic resize detection, a skippable tutorial career, preference persistence, keyboard-only immediate selection without Enter, and human playtesting on Windows/macOS terminals. Prior compatibility commitments below are historical and no longer part of the development plan.

## Prior milestones and background

## Debates Edition update

New careers use DEBATES rules. Each scheduled debate now contains three questions, seeded opponent style, independent answer clocks, and immutable round/result snapshots. Missing a question moves to the next; leaving marks all remaining questions missed. The result supplies a small persistent election-day share shift (score margin × 0.025 percentage points, ±0.30 per debate). ST → 7 shows the current campaign's debate record. Result screens pause the next question's timer. Campaign actions cannot bypass an active debate.

Previous DECISIONS careers replay without these changes. A prior-binary live-debate fixture verifies the original history and 43-second remaining clock. Remaining work: issue-specific voter blocs, richer opponent arguments, debate invitations/eligibility and preparation actions, configurable clock durations, and the existing institutional/calendar backlog. Monetary amounts and scores remain abstract authored game rules.

## Previous Decisions Edition baseline

## Decisions Edition update

Implemented: three scheduled campaign debate questions (turns 4/8/12), 60-second live debate/press responses and 45-second interviews; automatic no-answer consequences; one/two-step ordinary deadlines; accessibility toggle; saved remaining time; 54 total campaign stories and 38 total world stories. Compact 100×28 screens, consistent ST statistics, separators and optional detail/report views replace verbose new-career decision screens. Older save profiles retain their original mechanics.

`DecisionDeadline`, `CampaignStories`, `WorldStories`, `DecisionText` and `LineInput` supply the new content, typed defaults, concise presentation and nonblocking terminal input. Clock updates are journaled commands and consume neither game turns nor random draws. The terminal owns elapsed monotonic time; the engine owns remaining seconds and consequences. Future GUI adapters can submit the same tick/response commands.

Remaining: multi-question debate rounds, opponent personalities and issue-specific judging; configurable response durations; simultaneous decision inbox; finer-than-month crisis calendars; deeper event chains; existing contingent-election/calendar and institutional roadmap. Authored timers, monetary scales, support shifts and response probabilities are gameplay assumptions, not observed or statutory quantities. No wars or authoritarian powers were added.

40 production Java sources; no external dependencies. Renderers submit typed commands to CareerEngine and consume immutable views. Simulation classes contain no terminal or filesystem I/O.

| Component | Responsibility |
| --- | --- |
| CareerEngine | Accepted-command journal, phases, elections, opposition, term limits, world ownership and carryover |
| Presidency | Monthly action validation, office cash, legislative/cabinet/policy orchestration |
| WorldSimulation | Persistent metrics, independent RNG streams, month clock, trends, causal reports, flags and scheduled outcomes |
| WorldMetric / WorldEffect | Units/bounds and immutable typed changes |
| WorldEvent / WorldEventCatalog | Prerequisites, weighted stories, response costs, immediate/delayed effects and authored probabilities |
| SyntheticElectorate | Fictional geography, vote share, turnout, integer ballots and split-elector allocation |
| GameEngine | Campaign actions/events, symmetric candidate work, synthetic or legacy election profile |
| ElectionResult | Immutable vote and electoral totals; legacy constructors retained |
| Congress / MidtermCampaign | Procedure gates, vote commitments, capital-backed negotiation, outgoing/incoming membership and contest resolution |
| Cabinet / PolicyCatalog / PublicCorrespondence | Named staffing, annual work, policy deliveries, promise and correspondence state |
| CareerSave | Versioned deterministic command replay, slot metadata and rotating recovery backups |
| TextUI / TerminalScreen | Menus, complete frame redraw, pagination, metric/event explanations and plain mode |
| GameRules | LEGACY, EXECUTIVE, WORLD; no silent reinterpretation of historical journals |

WorldSimulation is owned by CareerEngine and shared with the current Presidency. A new term does not reconstruct it. Opposition advances twelve passive world months per annual decision; the current campaign interlude pauses world time. Developer timeline replacement explicitly constructs a new world.

Month closure processes old administrative follow-ups and implementations, cabinet reports, world delayed outcomes/economy/opinion, midterm outcomes/seating, annual records and new world stories. WORLD uses its event engine instead of the older six office events. Legacy profiles retain their old ordering and RNG behavior. Do not change WORLD coefficients/content under the same replay version after release without migration/versioning.

Views copy collections. Narrative category search remains heuristic over history strings; it is not a structured historical database. World effects/queues are typed but other older subsystems retain bespoke effects, so a completely unified effect pipeline remains future work.

The save format is presidency-world-v1. It accepts executive-v1 with its saved profile, and Administration/Governance as LEGACY. No quarterly/monthly migration is claimed. RNG state is regenerated by command replay; browsing never calls mutable RNG. Rejected commands validate before consuming resources. Developer-only office commands mark the career only after acceptance.

World events can be added in WorldEventCatalog without new engine branches: specify an ID, eligibility, weight and two immutable choices. Every choice supplies immediate effects, a delay, probability and two outcome effects. Named flags create chains; root events can recur after a cooldown. A no-cost alternative is a content invariant. Tests cover every current response and delayed outcome scheduling.

See MODEL-NOTES.md for equations and the limits of each simulation abstraction.
