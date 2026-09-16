# Development backlog

Status meanings: **Done** = playable and tested in this release; **Hook** = typed action/data and basic bookkeeping only; **Planned** = absent. P0 establishes the next coherent feature; P1 follows it; P2 is later expansion. This is a development plan, not a promise that every item is implemented.

| ID | Category | Priority | Status | Feature and completion criterion | Depends on |
| --- | --- | --- | --- | --- | --- |
| CORE-01 | Core | P0 | Done | Election → inauguration → 48 months → reelection → 96-month retirement | — |
| CORE-02 | Core | P0 | Done | Two actions/month; reject invalid commands without mutation | CORE-01 |
| CORE-03 | Core | P1 | Planned | Explicit calendar for elections and transition months; no unexplained date jumps | CORE-01 |
| CAM-01 | Campaign | P0 | Done | Preserve 16-turn objective campaign and 36 events | — |
| CAM-02 | Campaign | P0 | Done | Two immutable campaign pledges carried into signed-policy record | CAM-01 |
| CAM-03 | Campaign | P1 | Planned | Richer campaign tone, public concerns, and challenger identity without hidden mechanics | CAM-01 |
| PRES-01 | Presidency | P0 | Done | Monthly desk, explicit end-month, separate public operating account | CORE-01 |
| PRES-02 | Presidency | P0 | Done | Bill proposal/negotiation/sign/veto; eight issue areas | PRES-01 |
| PRES-03 | Presidency | P1 | Hook | Appointments/preparedness: add named officials, vacancies, and concrete follow-up tasks | PRES-01 |
| PRES-04 | Presidency | P1 | Done | Sixteen authored initiatives, signing costs, tradeoffs, delayed delivery, and safe supersession | PRES-02 |
| PRES-05 | Presidency | P1 | Done | Sixteen distinct requests, acknowledgment/delivery states, and dated history | PRES-04 |
| ELEC-01 | Elections | P0 | Done | One midterm checkpoint per term, now resolving the playable contest board | PRES-01 |
| ELEC-02 | Elections | P1 | Done | Twelve fictional contests with targeted visits, action limits, and exact seat accounting | ELEC-01 |
| ELEC-03 | Elections | P0 | Done | Administrative record and kept/contradicted promises change next campaign preparation | CAM-02, PRES-02 |
| ELEC-04 | Elections | P1 | Planned | Separate narrative aftermath for narrow/large wins and losses; preserve exact result history | CAM-01 |
| ELEC-05 | Elections | P2 | Planned | Explicit tie/contingent-election flow instead of skipping to next cycle | CAM-01 |
| WORLD-01 | World Events | P0 | Done | Prerequisites, weighted draw, choices, delayed effects, six office events | PRES-01 |
| WORLD-02 | World Events | P1 | Planned | Expand event catalog and typed effects; no repeat-farming exploits | WORLD-01 |
| WORLD-03 | World Events | P2 | Planned | Crises, scandals, disasters, diplomacy, wars as separate domain systems | WORLD-02 |
| CHAR-01 | Character | P0 | Done | Loss history, four comeback years, donor review, community rebuilding | CORE-01 |
| CHAR-02 | Character | P1 | Planned | Books, endorsements, named relationships, playable governor/senator paths | CHAR-01 |
| CHAR-03 | Character | P1 | Hook | Rest/travel hooks become energy, itinerary, and personal-life decisions | PRES-01 |
| CHAR-04 | Character | P1 | Planned | Dedicated cross-term archive with filters and office timeline | CORE-01 |
| UI-01 | UI | P0 | Done | Small categorized menus, confirmations, free browsing, pending-event menu access | CORE-01 |
| UI-02 | UI | P0 | Done | Autosave, three slots, load, title/new game, exit | CORE-01 |
| UI-03 | UI | P1 | Planned | Slot names/timestamps, help text per action, keyboard shortcuts | UI-02 |
| UI-04 | UI | P2 | Planned | Full GUI adapter consuming the same commands/views | UI-01 |
| DEV-01 | Dev Tools | P0 | Done | Marked force outcome, scenario jumps, funds, control toggle, event trigger, time skip | CORE-01 |
| DEV-02 | Dev Tools | P1 | Planned | Typed state inspector, selectable event IDs, fixture export and replay diff | DEV-01 |
| SETTINGS-01 | Core | P0 | Done | Seed, campaign difficulty, office-event frequency | CORE-01 |
| SETTINGS-02 | Core | P2 | Planned | Authored alternative rosters/election rules; save-versioned rule packs | SETTINGS-01 |
| EXP-01 | Long-Term/Experimental | P2 | Planned | Constitutional power/institution systems and consequences; no single dictator button | PRES-04, WORLD-03 |
| EXP-02 | Long-Term/Experimental | P2 | Planned | Alternate term-limit modes isolated from normal two-term careers | EXP-01 |

Completed in this release: PRES-04, PRES-05, and ELEC-02. The new midterm-start developer scenario supports testing them. See CHANGELOG.md for save compatibility.

Next bounded milestone: PRES-03 named appointments with vacancies and follow-up tasks, followed by CHAR-04's dedicated cross-term archive. Calendar refinement (CORE-03) and save-slot descriptions (UI-03) remain planned. WORLD-02 remains planned: this release uses the existing six random office events; policy deliveries are scheduled work, not additional random events.

The new midterm board remains an explicit fictional objective game, not a real electoral forecast. Public feedback remains descriptive. National economic simulation and historical scenarios remain separate future designs.
