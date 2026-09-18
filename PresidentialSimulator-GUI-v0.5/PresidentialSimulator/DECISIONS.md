# Response windows and event design

## Interface Playtest — current behavior

One CURRENT ruleset; older career save formats and import menus are intentionally unsupported. Help works before career creation, provides five short topics, and is available globally with ?. The footer displays actual elapsed countdown ticks; descriptions no longer repeat a frozen timer. Full text wraps and paginates without ellipsis clipping. Decision shortcuts stay in the prompt, report prose is complete, invalid input does not accumulate, and manual saves display confirmation. Default size remains 100×28.

Remaining UI work: automatic resize detection, a skippable tutorial career, preference persistence, keyboard-only immediate selection without Enter, and human playtesting on Windows/macOS terminals. Prior compatibility commitments below are historical and no longer part of the development plan.

## Prior milestones and background

## Debates Edition update

New careers use DEBATES rules. Each scheduled debate now contains three questions, seeded opponent style, independent answer clocks, and immutable round/result snapshots. Missing a question moves to the next; leaving marks all remaining questions missed. The result supplies a small persistent election-day share shift (score margin × 0.025 percentage points, ±0.30 per debate). ST → 7 shows the current campaign's debate record. Result screens pause the next question's timer. Campaign actions cannot bypass an active debate.

Previous DECISIONS careers replay without these changes. A prior-binary live-debate fixture verifies the original history and 43-second remaining clock. Remaining work: issue-specific voter blocs, richer opponent arguments, debate invitations/eligibility and preparation actions, configurable clock durations, and the existing institutional/calendar backlog. Monetary amounts and scores remain abstract authored game rules.

## Previous Decisions Edition baseline

| Situation | Real seconds | Game deadline | No response |
| --- | --- | --- | --- |
| Scheduled debate (turns 4/8/12) | 60 | Next campaign turn | Temporary support penalty |
| Live interview | 45 | Next campaign turn | Temporary support penalty |
| Live press briefing | 60 | Next month-end | Trust/favorability penalty |
| Ordinary campaign offer | None | Up to two turns; capped at election | Offer expires, or displayed credibility penalty |
| Presidency request | None | One or two month-end steps | Event-specific default |

Menus, stats and effects details pause the real clock. Confirmation and paging do not. Normal simulation deadlines advance only when the player takes a campaign turn or ends a month. A live appearance resumes when returning from menus. Leaving it explicitly counts as no answer. Turning clocks off pauses seconds only; it does not erase choices or their game deadlines. Expiry never automatically spends money or approves policy.

Saving replays clock commands as well as choices. Time away from the game is not counted. Whole remaining seconds persist; a fractional second can be lost on save/restart. Late input at the timeout notice only dismisses it. These deliberate accessibility rules are not intended as competitive anti-cheat enforcement.

The engine uses only remaining time, not operating-system clocks. A future GUI can call CLOCK_TICK with elapsed whole seconds and display the same immutable snapshots. Invalid ticks, unavailable choices and insufficient resources preserve game state. Older rules have no new deadlines or stories, protecting saved histories.

All events are fictional. Follow-up flags connect choices with future eligibility; delayed effects settle using the saved seeded random streams. See WORLD-EVENTS.md and CAMPAIGN-EVENTS.md for the actual catalogs. The campaign debate model currently gives one question per scheduled encounter; answers change modeled support for two turns and can affect fundraising. Support is measured in percentage points, not electoral votes.
