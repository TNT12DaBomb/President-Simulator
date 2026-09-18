# v0.7 status

Completed: dedicated live debate window; fixed isolated practice; topic rotation; expanded factual bank; clearer preparation order.

Next validation: native Windows/macOS modal focus, screen-reader announcement and keyboard navigation; migrate older inline-interface regression tests. More content: additional pop-culture franchises and authored follow-up questions. Simulation issues remain documented in SEED-AUDIT.md.

# Development backlog — v0.6

This is an implementation roadmap, not a statement that every feature exists. Old milestone notes were superseded by this status list. Systems use fictional game assumptions; factual educational material must identify its source and date. Do not confuse hidden game state with empirical forecasts.

## Delivered in v0.6

- 52 questions across Politics and Fun modes; choose before a new career.
- Full free team rehearsal, with optional clocks and no public/career side effects.
- Explicit preparation budget and cost; free rereading.
- Longer Standard/Relaxed clocks and Untimed mode; saved expanded settings.
- Larger debate text, correct/incorrect/withdrawal feedback and optional celebratory wording.
- Policy commitments recorded at utterance, available as actual proposal actions in office, and compared with proposal/signature/veto records.
- Persistent blue-player/red-opponent identity from setup through map play.
- Obsolete practice widget, historic screenshots and superseded docs removed from the distribution.

Next content priorities: broader Fun franchises, more economics topics, question metadata/difficulty filters, source revision workflow and additional authored follow-ups. Next framework priorities: interviews using PressureEvent, linked evidence IDs, delayed consequences and media archives. Numerical reputation/polling consequences remain unimplemented.

## Retained from v0.5

- Width-aware Office and Policy layouts; Policy tabs separate initiatives, bill proceedings and promises.
- Fixed-height notifications: showing or dismissing a message does not shift the workspace.
- Persistent map hover information; geographic and enlarged callout targets for eastern states and DC.
- Expanded persistent presidency dashboard using existing metrics.
- `PressureEvent` contract: current situation/options, response deadline, response, expiry, leaving, transcript and completion. First implementation: `LiveDebate`.
- Scheduled debate preparation with six briefing slots, source notes, shuffled factual answers, explicit confidence, opponent claims that may be wrong, interruption, short recovery, verified feedback, policy statement and record follow-up.
- Nine source-backed factual/rebuttal cards; three per debate, preferring questions not previously answered in the career.
- Real-time stage clocks; untimed preparation/feedback; menus and focus changes pause. Existing setting disables clocks.
- Durable transcript and structured statements, quoted in later debates and inherited by subsequent campaigns.
- Updated command replay format; no old-save migration. Tests cover replay during a challenge.

Not delivered: the new protocol does not alter candidate ratings, campaign finances or electoral support. Old authored debate scoring is not invoked for current careers. Opponent styles are scripted behaviors, not numerical personality assessments. Debate commitments now link to proposal actions and decision comparisons; they do not automatically deliver a law.

## Next engineering milestones

1. Apply the pressure-event contract to one interview and one presidency briefing. Extract shared stage hosting only after both exercise it; preserve distinct rules and time scales.
2. Build an evidence/statement ledger with stable IDs, dates, subject, exact wording, sources, claim status, correction links and context. Never silently overwrite a prior statement.
3. Add a delayed follow-up queue with prerequisites, cancellation, cooldown, expiry and idempotent application. Save pending consequences and information visibility.
4. Add a news inbox and event archive, with links back to the actual statement/decision. Keep allegations, confirmed facts and unresolved claims visibly distinct.
5. Expand authored content and accessibility, then device-test the complete campaign-to-presidency loop.

## Pressure events and debates

- Data-defined stages and transitions; actor, prompt, accessible choices, source/evidence references, real-time versus game-time deadline and explicit default action.
- Preparation topics: economy, constitutional knowledge, foreign affairs, own record, opponent research and current events. Distinguish inventory/resources from purported measures of candidate worth.
- Factual, policy, attack, rebuttal, values, consistency, rapid-fire and long-form formats; varied plausible distractors reviewed against authoritative sources.
- Record-based questions must quote something actually said. Distinguish corrections, explicit revisions and unresolved apparent inconsistencies without inventing motives.
- Moderator protocols: strict turn-taking, source requests, interruptions and clear remaining speaking time.
- Opponent arguments must match the question and known record; separate unverified claims from moderator findings.
- Prepared notes and source inspection; post-debate fact-check review, transcript export and player-selected excerpts.
- Accessibility: configurable durations, untimed mode, keyboard shortcuts including context-specific interruption, scalable text and screen-reader announcements that do not repeat every second.
- No fabricated applause or focus-group findings presented as evidence. Any future authored reactions must be labeled fictional.
- Invitation/eligibility and scheduling; distinct primary/general formats; VP debate hooks.

## Career and character continuity

- Generated fictional biography: work, service, prior offices, public statements, disclosed controversies and known vulnerabilities.
- Persistent offices held, elections, appointments, legislation, public statements, resignations, endorsements and corrections.
- Evidence-based reputation history: record actions and attributed reactions before designing additional evaluation mechanics.
- Relationships and unresolved commitments with staff, former rivals, donors, allies and legislators; carry them through transitions and losses.
- Rebuilding after defeat: book, endorsement, local office, public service, retirement and later runs with explicit prerequisites.
- Legacy archive with dated records, later revelations, source attribution and export.

## Media and information quality

- Fictional national, local, cable, partisan, tabloid, podcast and social channels with distinct editorial formats.
- News-cycle lifecycle: publication, follow-up, correction, expiry and renewed attention; avoid duplicate stories each refresh.
- Rumors and leaks: evidence withheld from player, provenance, confirmation state and eventual resolution.
- Gaffes: town halls, rope lines, interviews, hot microphones and ambiguous phrasing; preserve exact context.
- Interviews with follow-ups, interviewer protocols, direct answers, deferral and leaving.
- Press conferences driven by current stories; reporter selection, unanswered questions and follow-up queue.
- Polling UI must distinguish estimates and uncertainty from hidden simulation state. Sample metadata, field dates and methodological caveats need a separately reviewed design.
- Internal research requests should return documented information, not magically reveal all hidden facts.

## Campaign organization and resources

- Campaign manager, communications, policy, finance, field, polling and debate-preparation staff; assignments and conflicting advice with reasons.
- Staffing costs, travel calendar, stamina/rest, morale and volunteer organization; distinct from public support.
- Burn rate, cash-flow ledger, scheduled expenses, fundraising fatigue and inability-to-pay paths.
- Donor/interest-group invitations, disclosures, expectations, acceptance/refusal and later accountability.
- Ad drafting: actual text, evidence, tone, format, schedule, cost and repetition history.
- Surrogate scheduling, availability, travel conflicts and reporting.
- Meaningful VP selection with biographies, existing records, availability and relationship context.
- Convention schedule, speakers, platform disputes, protests and unexpected interruptions.
- Opposition research: evidence review, legal/source context, release timing and corrections for false material.
- Endorsement provenance and consent; unexpected endorsements with an explicit response option.
- Party requests and competing commitments; former primary rivals retain interaction history.

## Elections and transition

- Primaries: nomination calendar, delegate rules, ballot access, distinct candidate field and persistent platform statements. Research rule sets before implementation.
- Platform drafting and promise ledger shared with governing; edits must retain earlier versions.
- Election night: staged result reporting, uncertainty, completed tallies, concession/victory and a skip option. Do not invent authoritative calls from unfinished results.
- Transition: briefings, personnel vetting, agenda selection and inauguration calendar.
- Preserve existing midterm/reelection/term-limit loop while adding richer presentation and institutional details.
- Audit election rules, vacancies, tie/contingent-election handling and state allocation against dated official sources.

## Presidency and institutions

- Legislative negotiation with named fictional actors, explicit requests, bill amendments and recorded commitments.
- Separate committee process, floor consideration, cloture where applicable, passage, presentment, signature, veto and override.
- Cabinet disagreement, delegation, operational failures, resignation/firing, vacancies and confirmation requirements appropriate to each office.
- Agency projects with milestones, budgets, implementation delays, audits and judicial orders.
- Executive orders with legal authority, limits, review and reversal; no universal executive power button.
- Court challenges with procedural stages, partial relief and implementation consequences.
- Congressional relationships that reference campaign assistance and actual previous negotiations.
- Presidency press conferences and briefings reuse evidence/pressure-event infrastructure.
- Issue-specific public reporting needs a separately documented model; avoid conflating personal popularity with job approval.
- Crisis content: disasters, public health, cyber incidents, banking strain, shutdowns, protests and foreign developments. Each requires researched responsibilities and imperfect options.
- Deep foreign affairs, wars and national-security institutions remain later work.

## UI, saves and developer tools

- Native Windows/macOS laptop checks at 100%, 125%, 150% and 200% scaling.
- Dark combo boxes/tabs/scrollbars across platform look-and-feels; large-text presets and color-vision alternatives.
- Map zoom/pan, keyboard state navigation and persistent selection across every refresh.
- Decision content must remain reachable with all choices visible or clearly scrollable; no accidental click-through between stages.
- Toast priority and deduplication; fixed layout; urgent choices visible without a report-tab detour.
- Save cards, backup recovery, explicit format/version errors and configurable transition checkpoints.
- Developer scene launcher for every pressure stage, forced truthful/false claim fixtures and delayed-event inspection.
- Separate random streams for presentation, events and simulation; same seed/settings/commands must replay exactly.

## Long-term / experimental

- Personal life, additional offices, historical content packs and fictional rule sets.
- Institutional change and constitutional-crisis narratives require explicit legal context, guardrails in the model and a separate design review.
- Modding/content schema, localization, runtime bundles and distribution packaging after core stability.

## Acceptance gates

For every new event family: no impossible choices; no hidden mandatory resource requirement; no duplicated delayed effects; source-backed factual feedback; truthful timeout text; stable IDs; replay through every stage; menu/focus pause checks; minimum-window layout; clear implemented/deferred documentation. Prototype fiction must never be presented as a calibrated political prediction.
