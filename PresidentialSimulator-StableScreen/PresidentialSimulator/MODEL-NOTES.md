# World model specification

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

All coefficients below are authored, versioned game parameters. Sources establish institutional distinctions, not the size of simulated political or economic effects. WORLD careers use this model; EXECUTIVE/LEGACY replay their original mechanics.

## Public opinion and resources

National job approval = 0.35 × own-party approval + 0.30 × independent approval + 0.35 × opposition approval. Groups are exclusive fictional party-identification categories; these are not ideological or demographic estimates. There is no sampled poll error, undecided share or representative survey yet. Popularity is a separate personal-favorability percentage. Trust, unity, reputation, donor support, political capital, readiness, resilience, ethics exposure, confidence and energy are 0–100 indices. Changes in percentage metrics are percentage points, not percent multipliers.

Initial own-party / independent / opposition approval is 82 / 48 / 20, producing 50.1% overall approval. Popularity begins at 54; trust 55; party unity 70; donor support 55; reputation 50; capital 60; energy 80; preparedness/resilience 45; tension 25; ethics exposure 0. These are scenario defaults, not observations of a president.

Monthly common performance pressure is:

`0.12*(trust-55) + 0.5*(growth-2) - 0.8*(unemployment-4.5) - 0.7*max(inflation-2,0) - 0.12*ethicsExposure - 0.025*max(tension-30,0)`.

Approval gradually moves toward a group-specific baseline adjusted by this pressure (and unity for the own-party group), with a small seeded disturbance. Independent approval reacts more strongly. Favorability gradually responds to trust and ethics, separately from the job-approval calculation. Direct actions, promises and events can produce immediate changes before the next monthly update. Bounds prevent invalid percentages.

Trends store month-end observations plus an initial observation. Dashboard change compares the two most recent monthly observations. Actions since the latest closure affect the current rating immediately and enter the next monthly change. Highs/lows include intervening actions. Recent explanations show actual bounded changes, not just intended deltas. Full accepted reports remain in the career journal.

Work normally consumes four energy; rest restores sixteen. Positive direct action benefits are halved below 25 energy. Repeating the same action during one month gives no additional public benefit. Cabinet reports are separately limited by department/year. An issue's completed-policy metric reward is limited to once per term. All metrics are exposed through immutable views.

## Economic units and dynamics

- Growth: annualized real-GDP-growth model rate, bounded −12 to +12%.
- Unemployment: percentage, bounded 1–25%.
- Inflation: year-over-year model rate, bounded −3 to +20%.
- Policy rate: percentage, bounded 0–20%.
- Deficit: annualized deficit/GDP percentage; a negative value is a surplus, bounded −10 to +25%.
- Debt: debt/GDP ratio percentage, bounded 0–300%.
- Confidence: 0–100 authored index.

Initial values are growth 2, unemployment 4.5, inflation 2.5, policy rate 3.5, deficit 4, debt 95, confidence 55. They are not current US statistics.

Each month growth moves 14% toward a 2% anchor, with credit-condition and confidence effects plus a bounded outside disturbance. Unemployment adjusts gradually to activity. Inflation moves toward 2% with activity pressure and a disturbance. The independent rate rule responds gradually to inflation and unemployment; the player cannot set it. Deficit shocks decay slowly toward the 4% scenario baseline. Debt/GDP uses a coarse monthly approximation:

`debtRatioChange = (annualDeficitRatio - debtRatio*(realGrowth + inflation)/100)/12`.

This is a reduced-form game model, not a national-accounts or general-equilibrium model. There are no GDP levels, actual federal dollar totals, bond maturities, interest-expense ledger, supply-sector detail or regional distribution. Rate/risk responses omit substantial real-world complexity. Administrative initiatives have deliberately small authored effects; their office cash costs are not dollar-for-dollar national spending.

## Event engine

`WorldEvent` → requirements/weight → eligible draw → pending response → validated capital spending → `WorldEffect` → `Scheduled` outcome → later branch eligibility.

Definitions are immutable. Effects contain typed metric deltas and named flags to add/remove. Scheduled records include due world-month, probability, success effect, failure effect and explanation. Probabilities are evaluated only when an outcome settles. Separate RNG streams cover economic variation, story selection and delayed outcomes. None run while browsing. Save replay regenerates the same pending stories, queues, flags and future outcomes.

Hurricane/outbreak response probabilities are adjusted by `(preparedness−45)/300`; cyber responses use resilience instead. Adjusted chances are bounded 5–98% and displayed on the pending event. Root events need an eligible metric threshold where defined. Follow-up events require the corresponding story flag. A resolved chain removes its flag. Any eligible follow-up has priority over a new random root. An event ID has an 18-world-month cooldown. Frequency-off does not cancel existing obligations.

One decision is active at a time; several delayed consequences can coexist. Each story has a zero-capital alternative, avoiding a resource deadlock. The world clock persists across terms and advances twelve times per opposition-year decision. Campaign interludes remain compressed with a paused world clock. Retirement ends active simulation; outstanding obligations remain visible in the final snapshot.

## Elections

The model is two-candidate and synthetic. State baselines are seeded from state names and do not encode actual partisan geography. Fictional eligible population is 300,000 per House-equivalent unit, with a 600,000-voter DC scenario. These numbers are an explicit weighting convention, not census data. Maine/Nebraska have equal-sized synthetic districts with distinct lean offsets.

Each completed local task shifts player two-party vote share +2.8 points; opponent tasks shift it −2.8. Field offices on either side add two turnout points. Base turnout is 60%, with election uncertainty. A national election-day shock and state/district shocks separate displayed projections from final votes. All draws are reproducible functions of the saved seed; views never advance RNG.

The next campaign's governing-record share shift is bounded to ±4 percentage points:

`(approval−50)/10 + 0.3*(growth−2) − 0.3*(unemployment−4.5) + (popularity−54)/25 + (reputation−50)/50 − ethicsExposure/40`.

Career reputation, donors and approval also change campaign starting cash, bounded −$250 to +$400, separately from earlier comeback/administrative adjustments. No public operating cash is transferred.

Popular votes are integer counts and sum from voting units. Maine and Nebraska allocate one elector per district and two statewide; other states/DC use statewide totals. An exact unit tie uses a documented fictional deterministic tiebreak, not a simulation of state recount law. A statewide tie in a split-allocation state is assigned to the opposing candidate under that same simplified tie convention. A 269–269 national tie still needs a proper contingent-election phase; the legacy next-cycle path is a known unresolved correctness gap.

The midterm board keeps fixed 210 House/48 Senate seats and twelve competitive bundles. Contest win probability = `clamp(0.30 + 0.17*completedVisits + 0.06*governingMood, 0.05, 0.95)`. Results are seeded, recorded and installed in January. This is not a full congressional election model.

## Institutional grounding

Approval and favorability measure different concepts. [Gallup explanation](https://news.gallup.com/poll/14797/presidential-approval-vs-favorability-ratings.aspx).

US monetary policy is conducted by the Federal Reserve toward congressionally established employment, price-stability and long-term-rate objectives. The simulation's independent rate rule is an abstraction of that institutional separation. [Federal Reserve monetary policy](https://www.federalreserve.gov/monetarypolicy.htm).

The 2024/2028 allocation totals 538 electoral votes; Maine/Nebraska use district plus statewide allocations. [National Archives allocation](https://www.archives.gov/electoral-college/allocation). The same allocation remains a fixed future scenario; no future reapportionment is predicted.
