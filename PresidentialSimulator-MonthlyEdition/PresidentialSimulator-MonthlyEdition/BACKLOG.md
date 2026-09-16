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
| PRES-04 | Presidency | P1 | Planned | Replace generic programs with authored policy dilemmas and documented consequences | PRES-02 |
| PRES-05 | Presidency | P1 | Planned | Distinct public correspondence groups and longitudinal descriptive feedback | PRES-04 |
| ELEC-01 | Elections | P0 | Done | One midterm checkpoint/term with visible authored chamber-control branches | PRES-01 |
| ELEC-02 | Elections | P1 | Planned | Replace two-branch midterm fixture with a playable seat-contest system | ELEC-01 |
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

Suggested next bounded milestone: improve the presidency's public correspondence and policy-dilemma records, then replace the midterm fixture with actual player decisions. Preserve the end-to-end loop and save determinism before adding more content families.

Numeric political ratings/probability forecasts are not part of this implementation. National economic simulation, turnout/popular-vote data, and historical scenarios also require their own explicit future designs; no fabricated statistics are supplied by the current dashboard.
