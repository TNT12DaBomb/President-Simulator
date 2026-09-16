# World Edition

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

- Added 21 public/political/economic metrics plus weighted national approval, trend history and causal reports.
- Fourteen world events and 28 responses with costs, immediate effects, uncertain delayed outcomes and persistent prerequisite flags.
- Action fatigue/diminishing returns, promise consequences, delivery rewards, political-capital spending and preparedness-dependent odds.
- Synthetic vote/turnout model replaces objective ownership for new careers; Maine/Nebraska allocation and popular mandate included.
- Governing record affects reelection vote share/campaign resources and midterm probabilities.
- World state persists across terms and advances during opposition; save replay preserves future uncertainty.
- New metric screens, effect previews, event fixtures and approval/capital developer setters.
- Explicit WORLD rules, presidency-world-v1 saves and unchanged Executive/Administration/Governance import semantics.

Known institutional gaps remain documented in REALISM-AUDIT.md, particularly contingent elections and incumbent campaign overlap.

---

# Executive Edition

- Full-screen terminal refresh with width/height settings, wrapping, paging, plain mode and exit restoration.
- Wider presidency dashboard with upcoming work, correspondence and action reminders.
- Searchable/filterable career archive and in-menu manual-slot backup recovery.
- Saved EXECUTIVE/LEGACY profiles; Administration and Governance imports preserve original rules.
- New-career committee/floor stages, per-bill commitments, Senate cloture distinction, 50–50 nomination tie-break, and withdrawal versus veto.
- November midterm results separated from January congressional seating; unfinished proposals expire.
- Corrected cabinet titles and explicit inauguration/election/seating date helpers.
- Added source-backed realism audit, 144-item roadmap and three additional regression suites.

Known gaps: objective-based campaigns, no popular vote/turnout/approval/economy model, simplified midterm contest board, compressed incumbent campaign/calendar, no Maine/Nebraska split or contingent-election phase. See REALISM-AUDIT.md.

---

# Administration Edition

- Added six cabinet positions and twelve named fictional candidates with different confirmation requirements, project costs and delivery times.
- Added nominations, next-month hearings, confirmations, nominee-specific support, dismissals and vacancies.
- Added annual delegation with persistent departmental usage limits, delayed reports, shared administrative credit and non-duplicating budget refunds.
- Added named slots, UTC save timestamps, career summaries and three backup generations. Autosave recovery is accessible from the load menu.
- Added direct Governance Edition save imports, preserving original core rules and leaving the imported file intact. Earlier Monthly/Quarterly imports remain future work.
- Retained the monthly career, policies, correspondence, midterms, campaign events, developer tools and eight-year limit.
- Added previous-release golden fixtures, cabinet tests and UI recovery tests. Updated backlog completion status and remaining compatibility work.
