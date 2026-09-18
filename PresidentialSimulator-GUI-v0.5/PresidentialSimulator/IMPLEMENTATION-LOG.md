# v0.5 update

Fixed notification layout, geographic hover targets, width-aware Office/Policy controls and persistent presidency metrics. Added a staged PressureEvent debate protocol with preparation, confidence, claims, recovery, sourced feedback and career statement continuity. See DEBATE-DESIGN.md and BACKLOG.md; older entries below are historical, not current feature specifications.

# Comprehensive implementation log and roadmap

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

Updated for the Decisions Edition. This is the maintained inventory of completed work, explicit simplifications, and future additions. “Done” means the stated bounded criterion works; it does not imply the whole subject is realistically simulated. “Partial” separates implemented pieces from remaining work.

Priorities: P0 = correctness/foundation; P1 = next gameplay depth; P2 = broader expansion; P3 = experimental. Costs and voter behaviors remain fictional unless a source-backed rule is explicitly identified in REALISM-AUDIT.md.

## Work completed in this release

- Numerical job approval by partisan group, national approval, favorability, trust, reputation, unity, donor support, capital, energy and world/economic indicators.
- Thirty-eight random world events, 76 responses, persistent prerequisites/flags and probabilistic delayed consequences.
- Causal action effects, readiness-dependent odds, policy delivery effects, pledge consequences and anti-farming rules.
- Synthetic popular votes, turnout, uncertain elections and Maine/Nebraska district allocation; governing performance affects reelection and midterms.
- World continuity through reelection/opposition, deterministic replay, metric/trend/reason screens and developer fixtures.
- All earlier full-screen, cabinet, legislature, save-slot and archive features retained.

## Recommended next execution sequence

1. CAL-06: contingent elections; then CAL-05: concurrent incumbent campaigns and the January transfer.
2. Expand electoral units, Senate classes, third parties and sampled polling rather than calling the synthetic model historical.
3. Federal budget authorization, presentment deadlines and structured institutional effects.
4. Ideology/issue salience, advisor depth, simultaneous crises and calibrated economic distribution.
5. Foreign affairs, accountability and constitutional paths on top of tested institutions.

## Core and saves

| ID | Priority | Status | Addition | Acceptance criterion / remaining scope |
| --- | --- | --- | --- | --- |
| CORE-01 | P0 | Done | Current development rules | One CURRENT profile; no old save imports. |
| CORE-02 | — | Retired | Backward compatibility | Not a development requirement; older saves deliberately rejected. |
| CORE-03 | P0 | Done | Named slots and recovery | Three named manual slots, metadata and three generations; recovery can target manual slots in the new renderer. |
| CORE-04 | P0 | Planned | Quarterly and Monthly save migration | Explicit state migration or retained old engines; never reinterpret old journals silently. |
| CORE-05 | P0 | Partial | Unified monthly pipeline | Existing ordered calls remain; introduce typed subsystem updates and one shared effect queue. |
| CORE-06 | P0 | Partial | Reusable effects and requirements | World effects and requirements now cover metrics/events; legacy cabinet/policy effects still need full unification. |
| CORE-07 | P1 | Planned | Versioned content packs | Stable IDs, schema validation and rule/content version recorded in saves. |
| CORE-08 | P1 | Planned | Recovery under interruption | Fault-injection tests around every copy/rename and a recoverable manifest. |

## Terminal interface and accessibility

| ID | Priority | Status | Addition | Acceptance criterion / remaining scope |
| --- | --- | --- | --- | --- |
| UI-01 | P0 | Done | Full-screen refresh | Clear and redraw complete frames on input; restore cursor and screen on exit/EOF. |
| UI-02 | P0 | Done | Expanded layouts | Configurable 60–180 columns and 20–70 rows; dashboard columns stack on narrow frames. |
| UI-03 | P0 | Done | Overflow navigation | Wrap long content and page every screen with > and <; no gameplay time spent. |
| UI-04 | P0 | Done | Plain mode | Readable output without ANSI; default for redirected sessions. |
| UI-05 | P0 | Done | Encoding and Windows support | UTF-8 build/launch and best-effort VT enablement; plain fallback. |
| UI-06 | P1 | Partial | Display adaptation | Manual resize controls and initial environment dimensions; automatic live resize detection remains. |
| UI-07 | P1 | Partial | Action previews | Core actions show costs and requirements; standardize all previews in immutable option models. |
| UI-08 | P1 | Planned | Tutorial career | Guided but skippable campaign-to-office tutorial with saved progress. |
| UI-09 | P1 | Partial | Global shortcuts and preferences | ? help and ST stats work; persisted display preferences remain. |
| UI-10 | P2 | Planned | GUI adapter | Use the existing commands/views without copying game logic into widgets. |

## Calendar and constitutional procedures

| ID | Priority | Status | Addition | Acceptance criterion / remaining scope |
| --- | --- | --- | --- | --- |
| CAL-01 | P0 | Done | November midterm checkpoint | New rules resolve in month 23 instead of December. |
| CAL-02 | P0 | Done | Incoming versus outgoing Congress | Results are pending until the January boundary; preserve December governing. |
| CAL-03 | P0 | Done | Election-day calculation | Compute the first Tuesday after the first Monday; tested over multiple decades. |
| CAL-04 | P0 | Partial | Inauguration and congressional dates | Display January 20 and January 3; full partial-month service model remains. |
| CAL-05 | P0 | Planned | Incumbent campaign overlap | Reelection actions occur during final-year service, with November vote and January handoff. |
| CAL-06 | P0 | Planned | Contingent-election phase | House votes by state delegation, Senate selects VP, and unresolved outcomes follow constitutional procedures. |
| CAL-07 | P1 | Planned | Presentment deadlines | Signing, automatic enactment, return veto and pocket veto need day-aware scheduling. |
| CAL-08 | P2 | Planned | Succession | Acting service, vacancies and the Twenty-second Amendment succession exception. |

## Voters and presidential elections

| ID | Priority | Status | Addition | Acceptance criterion / remaining scope |
| --- | --- | --- | --- | --- |
| VOTE-01 | P0 | Done | Replace objective ownership | WORLD campaigns use synthetic vote preferences; completing tasks no longer automatically awards a state. |
| VOTE-02 | P0 | Done | Popular votes and turnout | Synthetic eligible populations, turnout and integer candidate vote totals are auditable; real demographic data remains future work. |
| VOTE-03 | P0 | Done | Maine and Nebraska allocation | Synthetic district votes plus two statewide electors; split outcomes and 538-EV conservation tested. |
| VOTE-04 | P0 | Partial | Uncertainty and polling | Seeded forecast/result uncertainty implemented; sampled surveys, undecided voters and polling error remain. |
| VOTE-05 | P1 | Planned | Campaign issue priorities | Connect promises and tone to documented fictional voter preferences. |
| VOTE-06 | P1 | Partial | Realistic election aftermath | Popular mandate, electoral breadth and defeat severity affect metrics; richer expectations, rivalries and tone remain. |
| VOTE-07 | P1 | Planned | Opponent adaptation | Named challengers prioritize plausible goals under the same action/resource constraints. |
| VOTE-08 | P1 | Planned | Electoral rule packs | Versioned apportionment, district data and scenario assumptions; no silent future census prediction. |

## Congress and legislation

| ID | Priority | Status | Addition | Acceptance criterion / remaining scope |
| --- | --- | --- | --- | --- |
| CON-01 | P0 | Done | Cloture versus passage | New contested ordinary-bill path distinguishes 60-vote cloture from majority passage. |
| CON-02 | P0 | Done | Nomination tie-break | 50 supporting senators plus the assumed supportive VP can confirm in current rules. |
| CON-03 | P0 | Done | Bill stages | Introduce, report from committee, secure commitments, vote, then sign/veto; withdraw unpassed proposals. |
| CON-04 | P1 | Partial | Named congressional leaders | Fictional leadership names and chamber-control updates exist; personal relationships do not. |
| CON-05 | P1 | Partial | Vote commitments | Visible per-bill commitments replace a boolean shortcut in new rules; bargaining increments remain authored. |
| CON-06 | P1 | Planned | Congressional factions | Distinct priorities, defections, pivotal members and explainable vote decisions. |
| CON-07 | P1 | Planned | Amendments and compromises | Narrow scope, costs and beneficiary effects; preserve bill revision history. |
| CON-08 | P1 | Planned | Chamber differences | Separate House/Senate bills, conference committees and final passage of identical text. |
| CON-09 | P1 | Planned | Additional vote procedures | Reconciliation eligibility, unanimous consent, absences, vacancies and veto overrides. |
| CON-10 | P1 | Planned | Legislative calendar | Floor time, recesses, deadlines, committee jurisdiction and multiple queued bills. |

## Policies and implementation

| ID | Priority | Status | Addition | Acceptance criterion / remaining scope |
| --- | --- | --- | --- | --- |
| POL-01 | P1 | Done | Authored initiatives | Sixteen alternatives with costs, tradeoffs, requests and delayed delivery remain playable. |
| POL-02 | P1 | Done | Safe policy reversal | Cancel superseded implementation and retain history without refund exploits. |
| POL-03 | P1 | Planned | Promise lifecycle | Announced, proposed, passed, funded, delivered, partial, abandoned and contradicted states. |
| POL-04 | P1 | Planned | Authorization and appropriation | Separate legal permission from the money needed to implement it. |
| POL-05 | P1 | Planned | Department capacity | Queue limits and workloads affect completion; demonstrate causes of delays. |
| POL-06 | P1 | Planned | Delivery quality | Distinguish nominal completion, service quality and accessibility. |
| POL-07 | P2 | Planned | Regional effects | Model regional benefits/burdens using documented fictional assumptions. |
| POL-08 | P2 | Planned | Sunsets and judicial challenges | Expiry, renewal, repeal, injunctions and continued implementation constraints. |

## Public opinion and correspondence

| ID | Priority | Status | Addition | Acceptance criterion / remaining scope |
| --- | --- | --- | --- | --- |
| OPN-01 | P1 | Done | Distinct correspondence records | Acknowledgment, funded work and delivery remain separate with dated history. |
| OPN-02 | P1 | Partial | Synthetic public opinion model | National and three partisan groups implemented; ideology, representative demographics and opinion calibration remain. |
| OPN-03 | P1 | Partial | Approval trend and explanations | Monthly trends, highs/lows and causal explanations implemented; sampled polling uncertainty remains. |
| OPN-04 | P1 | Planned | Issue salience | Public priorities shift with circumstances and vary across groups. |
| OPN-05 | P1 | Partial | Credibility and expectations | Broken/kept pledges affect trust and approval; campaign ambition and granular expectations remain. |
| OPN-06 | P1 | Done | Diminishing returns | Same-month action repetition supplies no extra public bonus; energy and per-issue delivery limits prevent simple farming. |
| OPN-07 | P1 | Planned | Awareness and delayed reaction | Separate actual policy outcomes from what people know and when. |
| OPN-08 | P2 | Planned | Media ecosystem | Fictional outlets, interviews, coverage and corrections without treating headlines as objective truth. |

## Political capital and relationships

| ID | Priority | Status | Addition | Acceptance criterion / remaining scope |
| --- | --- | --- | --- | --- |
| REL-01 | P1 | Planned | Political influence budget | Difficult initiatives consume influence; explain acquisition and recovery. |
| REL-02 | P1 | Planned | Persistent relationships | Trust, cooperation and grievances attached to named officials. |
| REL-03 | P1 | Planned | Commitment ledger | Track who promised what, deadlines and conflicting obligations. |
| REL-04 | P1 | Planned | Coalition durability | Differentiate one-vote, issue-specific and enduring agreements. |
| REL-05 | P1 | Planned | Meaningful failure | Failed initiatives constrain future choices without permanently ending viable play. |
| REL-06 | P2 | Planned | Private and public promises | Disclosure and accountability differ; neither erases prior commitments. |

## Cabinet and administration

| ID | Priority | Status | Addition | Acceptance criterion / remaining scope |
| --- | --- | --- | --- | --- |
| ADM-01 | P1 | Done | Named officials and vacancies | Six positions with candidate choices, hearings, confirmation and dismissal. |
| ADM-02 | P1 | Done | Delegated reports | Annual department assignments use time and money, with shared anti-farming limits. |
| ADM-03 | P0 | Done | Correct displayed office titles | Use Attorney General and HHS; retain internal legacy identifiers for replay. |
| ADM-04 | P1 | Planned | Department-specific expertise | Replace generic speed/cost archetypes with experience and project suitability. |
| ADM-05 | P1 | Planned | Advice and disagreement | Officials offer distinct recommendations and explainable objections. |
| ADM-06 | P1 | Planned | Resignation and succession | Departures, replacements and acting officials have persistent consequences. |
| ADM-07 | P1 | Planned | Vice-presidential role | Actual named VP, legislative support, diplomacy and succession. |
| ADM-08 | P2 | Planned | Complete cabinet roster | Add remaining departments only with meaningful responsibilities and realistic appointment rules. |

## Economy and budget

| ID | Priority | Status | Addition | Acceptance criterion / remaining scope |
| --- | --- | --- | --- | --- |
| ECO-01 | P0 | Done | National versus office accounts | Office cash, private campaign resources and national fiscal ratios are separate. |
| ECO-02 | P1 | Partial | Economic state | Growth, unemployment, inflation, rates, debt/deficit ratios and confidence implemented as an explicit reduced-form model. |
| ECO-03 | P1 | Done | Momentum and external shocks | Momentum, external variation and event shocks prevent direct economic control; authored coefficients remain labeled. |
| ECO-04 | P1 | Partial | Policy transmission lags | Delayed probabilistic event outcomes and delayed policy delivery implemented; sector-specific transmission remains. |
| ECO-05 | P1 | Done | Central bank independence | Independent rule adjusts rates to economic conditions; no presidential rate-setting action. |
| ECO-06 | P1 | Planned | Annual budget process | Proposal, authorization, appropriations, deadlines and continuing resolutions. |
| ECO-07 | P2 | Planned | Shutdown consequences | Unfunded functions are affected according to defined rules. |
| ECO-08 | P2 | Planned | Distribution and briefings | National indicators coexist with regional hardship; reports explain uncertainty. |

## Midterm expansion

| ID | Priority | Status | Addition | Acceptance criterion / remaining scope |
| --- | --- | --- | --- | --- |
| MID-01 | P1 | Done | Targeted contest board | Existing fictional slates remain playable with finite monthly actions. |
| MID-02 | P0 | Partial | Election and seating dates | November results/January seating corrected; district and Senate-class modeling remains. |
| MID-03 | P1 | Planned | All-seat roster | 435 House districts and staggered Senate classes; expose targeted races without implying other seats skip elections. |
| MID-04 | P1 | Planned | Named candidates and incumbents | Backgrounds, vacancies, retirements and local priorities persist. |
| MID-05 | P1 | Planned | Opposition activity | Opponents compete under resource constraints and can defend seats. |
| MID-06 | P1 | Planned | Governing record effects | Delivery, public opinion and presidential involvement feed local races. |
| MID-07 | P2 | Planned | Recruitment and endorsements | Candidate selection occurs before the campaign window. |
| MID-08 | P2 | Planned | Congressional continuity | Winners become persistent legislators with relationships and committee roles. |

## Events and crises

| ID | Priority | Status | Addition | Acceptance criterion / remaining scope |
| --- | --- | --- | --- | --- |
| EVT-01 | P1 | Done | Basic event framework | Existing weighted eligibility, choices, immediate/delayed effects and saved RNG remain. |
| EVT-02 | P1 | Done | Event chains | Persistent branch flags select later events; delayed success/failure paths survive saves and term transitions. |
| EVT-03 | P1 | Done (bounded) | Deadlines and urgency | Turn/month deadlines and saved real-time live answers, automatic defaults, pause controls; configurable durations and finer calendars remain. |
| EVT-04 | P1 | Planned | Multiple crises | Finite attention forces prioritization; prevent unwinnable event pileups. |
| EVT-05 | P1 | Partial | Preparedness and delegation | Preparedness/resilience change event probabilities; cabinet-specific crisis delegation remains. |
| EVT-06 | P1 | Planned | Information quality | Conflicting reports and uncertainty are distinguished from engine bugs. |
| EVT-07 | P2 | Partial | Content families | Thirty-eight world events cover disaster, health, banking, cyber, diplomacy, ethics, protests, energy and innovation; expand depth/content. |
| EVT-08 | P2 | Partial | After-action reviews | Causal reports and pending obligations retained; formal playable after-action reviews remain. |

## Character and careers

| ID | Priority | Status | Addition | Acceptance criterion / remaining scope |
| --- | --- | --- | --- | --- |
| CHR-01 | P1 | Done | Loss and comeback loop | Four rebuilding years, recorded defeats and donor-review consequences remain. |
| CHR-02 | P1 | Planned | Character creation | Name, background, age, home region, party and prior career. |
| CHR-03 | P1 | Planned | Energy and stress | Rest becomes an actual tradeoff with safeguards against repetitive grind. |
| CHR-04 | P1 | Planned | Personal finances | Separate private assets from campaign and public accounts. |
| CHR-05 | P1 | Planned | Alternate offices | Governor, senator, representative and cabinet paths with office-specific mechanics. |
| CHR-06 | P2 | Planned | Books and endorsements | Out-of-office decisions affect relationships and future campaigns. |
| CHR-07 | P2 | Planned | Optional personal life | Family, friends and personal events with adjustable prominence. |
| CHR-08 | P2 | Planned | Long career timeline | Aging, absences and returns retain history and changing circumstances. |

## Foreign affairs

| ID | Priority | Status | Addition | Acceptance criterion / remaining scope |
| --- | --- | --- | --- | --- |
| FOR-01 | P2 | Planned | Fictional world roster | Countries and leaders with stable IDs and persistent interests. |
| FOR-02 | P2 | Planned | Diplomatic relations | Summits, negotiations, agreements and credible commitments. |
| FOR-03 | P2 | Planned | Treaties and domestic approval | Differentiate agreements and treaties; model the relevant institutional requirements. |
| FOR-04 | P2 | Planned | Trade, aid and sanctions | Economic and humanitarian consequences with delayed effects. |
| FOR-05 | P2 | Planned | Alliance commitments | Partners have agency and can disagree. |
| FOR-06 | P2 | Planned | Security and intelligence | Readiness, uncertainty, escalation and de-escalation. |
| FOR-07 | P2 | Planned | Conflict consequences | Congressional involvement, domestic reaction, casualties, displacement and reconstruction. |

## Ethics and accountability

| ID | Priority | Status | Addition | Acceptance criterion / remaining scope |
| --- | --- | --- | --- | --- |
| ETH-01 | P2 | Planned | Conflicts of interest | Persistent interests and disclosure obligations create choices. |
| ETH-02 | P2 | Planned | Staff misconduct | Responsibility and corrective action do not collapse into a single scandal score. |
| ETH-03 | P2 | Planned | Leaks and investigations | Evidence, procedural stages and uncertain allegations remain distinct. |
| ETH-04 | P2 | Planned | Response choices | Disclosure, correction, denial and concealment have different downstream effects. |
| ETH-05 | P2 | Planned | Oversight and impeachment | Institutional actors and separate House/Senate processes. |
| ETH-06 | P2 | Planned | Recovery and accountability | Remedies and changed conduct can matter without erasing history. |

## Constitutional power and courts

| ID | Priority | Status | Addition | Acceptance criterion / remaining scope |
| --- | --- | --- | --- | --- |
| INS-01 | P2 | Planned | Courts and vacancies | Nominations, confirmations, judicial review and independent decisions. |
| INS-02 | P2 | Planned | Executive orders | Defined authority, scope, limits and review. |
| INS-03 | P2 | Planned | Federalism | States/local governments have separate responsibilities and choices. |
| INS-04 | P2 | Planned | Emergency powers | Scope, duration, oversight and expiry are explicit. |
| INS-05 | P3 | Planned | Institutional independence | Civil service, media, elections and courts have distinct state and actors. |
| INS-06 | P3 | Planned | Constitutional amendments | Model institutional requirements instead of a policy toggle. |
| INS-07 | P3 | Planned | Backsliding and resistance | Cumulative choices, institutional opposition, public reaction and possible restoration. |
| INS-08 | P3 | Planned | Alternate endings | Retirement, defeat, resignation, removal, succession and authoritarian outcomes; no unrestricted dictator button. |

## History and legacy

| ID | Priority | Status | Addition | Acceptance criterion / remaining scope |
| --- | --- | --- | --- | --- |
| ARC-01 | P1 | Done | Searchable career archive | Read-only category and text search, forward/backward paging, term summaries. |
| ARC-02 | P1 | Done | Cross-term source history | Original events and order are retained; cabinet/law/promise summaries survive replacement of active term state. |
| ARC-03 | P1 | Partial | Categorized records | Current categories use documented text heuristics; migrate to typed historical events for precise filtering. |
| ARC-04 | P1 | Planned | Structured term snapshots | Store immutable term objects, office timelines and exact quantitative histories. |
| ARC-05 | P1 | Planned | Cause-and-effect links | Later events point to the decisions that enabled or worsened them. |
| ARC-06 | P2 | Planned | Multiple legacy perspectives | Different groups interpret the same administration differently. |
| ARC-07 | P2 | Planned | Successor policies and post-presidency | Later events can preserve or change previous achievements. |
| ARC-08 | P2 | Planned | Exportable biography | Generate a portable summary from actual records, never invented achievements. |

## Developer tools and release quality

| ID | Priority | Status | Addition | Acceptance criterion / remaining scope |
| --- | --- | --- | --- | --- |
| DEV-01 | P0 | Done | Deterministic replay tests | Legacy fixtures and current-rule careers test state, history and resource preservation. |
| DEV-02 | P0 | Done | Screen-fit regression tests | Widths/heights, wrapping, paging, no-op navigation and terminal restoration are tested. |
| DEV-03 | P1 | Partial | Platform validation | Linux and deterministic render checks performed; real Windows/macOS console testing remains. |
| DEV-04 | P1 | Planned | Scenario inspector and editor | Validated subsystem views and fixture export instead of source edits. |
| DEV-05 | P1 | Planned | Event/content authoring tools | Select IDs, preview prerequisites, validate unreachable content and duplicate references. |
| DEV-06 | P1 | Planned | Balance and soak reports | Find dominant strategies, impossible objectives and resource starvation across seeds. |
| DEV-07 | P1 | Planned | Issue templates and release process | Acceptance criteria, reproducible bugs, versioned releases and known issues. |
| DEV-08 | P1 | Planned | Shared content proposals | Political-science contributors can submit structured evidence, scenarios and tradeoffs without writing engine code. |
| DEV-09 | P2 | Planned | Distribution preparation | Packaging, license decision, store/demo materials and feedback channels after a stable vertical slice. |

## Release gates

- A new gameplay rule must be versioned or migrated; older journals must not silently change outcomes.
- Rejected commands leave state and journals unchanged. Browsing and paging never advance time or draw randomness.
- Every new delayed effect has tests for save/load, cancellation, expiry and term boundaries.
- UI changes are checked at narrow and wide frame sizes and in plain mode.
- Every realism claim has a primary source, a stated date, and a clear boundary between procedure and game assumption.
- A whole feature family is not marked complete merely because a menu item or hook exists.
