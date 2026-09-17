# v0.2 GUI priority update

Delivered: Swing desktop shell, persistent statistics, electoral state board, turn schedule, clickable campaign/world decisions, monotonic live clocks, paused dialogs, campaign-to-career progression, basic office and legislative actions, developer scenarios, manual saves and phase autosaves.

Next: port cabinet/correspondence/midterm commands; searchable geographic map; save metadata/recovery cards; full consequence previews; keyboard/accessibility work; native Windows/macOS testing and packaging. Terminal feature work is frozen. The institutional/realism backlog below remains active; this release changes presentation only.

---

# Backlog — World Edition

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

[IMPLEMENTATION-LOG.md](IMPLEMENTATION-LOG.md) remains the canonical 144-item inventory with completion criteria. [REALISM-AUDIT.md](REALISM-AUDIT.md) reviews every earlier factual gap and distinguishes implemented game models from unfinished institutions.

Delivered: public opinion and economic metrics, consequential actions, fourteen world events with persistent chains, synthetic vote/turnout accounting, Maine/Nebraska splits, record-sensitive reelection and midterms, world continuity, developer fixtures and readable metric screens.

Next P0: contingent-election phase; concurrent incumbent campaigning and January handoff; correct district/Senate-class elections; day-aware presentment and budget authorization. Next depth: ideology/issue salience, polling uncertainty, advisor recommendations, simultaneous crises, distributional economics and constitutional accountability. The long-term authoritarian path remains dependent on those institutions, not a standalone button.
