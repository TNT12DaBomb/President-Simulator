# Realism audit — World Edition

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

Reviewed 2026-09-16. This update implements the approval/economy/event foundations and revisits every gap in the earlier audit. **Implemented does not mean empirically calibrated.** New WORLD careers use fictional populations and transparent authored coefficients; older saves preserve their original rules.

## Disposition of the previous audit

| Earlier gap | Current status | Implementation or remaining work |
| --- | --- | --- |
| Automatic objective ownership | Replaced in WORLD | Tasks affect synthetic vote share; seeded uncertainty determines final votes. Old profiles retain the board. |
| Maine/Nebraska allocation | Implemented for synthetic districts | One EV per district plus two statewide. Actual district population/geography remains absent. |
| Contingent elections | **Still open** | A 269–269 result still uses the old next-cycle path. House state-delegation balloting, Senate VP selection and acting succession require a dedicated phase. |
| Popular vote and turnout | Implemented synthetically | Integer votes, explicit eligible populations and turnout; a popular mandate is distinct from EV breadth. No real census or polling data. |
| Fixed apportionment | Explicit scenario limitation | The roster matches the 2024/2028 538-EV allocation. Later census changes are not forecast. |
| Midterm contest bundles | Improved, still partial | Visits, approval and other governing metrics affect probabilities. Most seats remain fixed; no full district or staggered Senate-class simulation. |
| Authored congressional commitments | Still simplified | Negotiation now consumes political capital. Committees, cloture and passage gates remain; individual senators, factions and real bargaining are not modeled. |
| Office cash confused with national economy | Separated | Office funds, campaign cash and macro/fiscal ratios are distinct. No complete national-budget or debt-service ledger. |
| Policy benefits lack causal model | Partial model added | Delivery, not signing, supplies small authored metric effects; costs, tradeoffs and repeat limits are visible. Effects are not research-based estimates. |
| Legislative promise versus implementation | Consequences added, lifecycle still partial | Keeping/contradicting a pledge affects trust, unity and party approval. Delivery is separately tracked; reversals remain in history. |
| Fixed nomination hearing delay | Still simplified | One-month hearing gate, full attendance and supportive VP assumptions remain. |
| Incumbent election/calendar overlap | **Still open** | Campaign interludes remain after governing blocks. World simulation pauses during these interludes. Partial January and presentment deadlines remain compressed. |
| Descriptive feedback without approval | Implemented in WORLD | National/group job approval, personal favorability, trust, reputation, trends and explanations now respond to choices and conditions. Ideology, salience, undecideds and survey sampling are future work. |
| Limited institutions and world systems | Framework expanded; many institutions remain | Stateful disaster, finance, health, cyber, ethics, protest and diplomatic event families. No full wars, courts, federalism, impeachment, succession or constitutional-power simulation. |

## Institutional rules retained

- Modeled contested ordinary Senate legislation requires 60 cloture commitments, followed by majority passage. This is not presented as a universal 60-vote passage requirement. Reconciliation and unanimous-consent paths remain absent. [Senate cloture overview](https://www.senate.gov/about/powers-procedures/filibusters-cloture/overview.htm).
- A supportive VP can break a 50–50 nomination vote. Attendance, vacancies and opposition-party VPs are not yet simulated. [Senate: Vice President](https://www.senate.gov/about/officers-staff/vice-president.htm).
- Only passed bills can be vetoed; unpassed proposals are withdrawn. Ten-day presentment, pocket vetoes and overrides need day-aware rules.
- Midterm results occur in November, followed by January seating. Congressional terms begin January 3 and presidential terms January 20. The normal eight-year career omits the succession exception to the twice-elected rule. [National Archives amendments](https://www.archives.gov/founding-docs/amendments-11-27).
- Maine/Nebraska district allocations and the published state EV totals are reflected in WORLD results. [National Archives allocation](https://www.archives.gov/electoral-college/allocation). Contingent-election requirements remain an explicit next milestone. [National Archives FAQ](https://www.archives.gov/electoral-college/faq).
- The central bank has its own rate response; the president cannot directly set rates. This reflects institutional separation, not a claim that our equation reproduces actual Fed decisions. [Federal Reserve monetary policy](https://www.federalreserve.gov/monetarypolicy.htm).
- Approval evaluates job performance; favorability evaluates the person. They are separate variables. [Gallup explanation](https://news.gallup.com/poll/14797/presidential-approval-vs-favorability-ratings.aspx).

## New modeling limitations to keep visible

1. The electorate is fictional. State-name seeds, population weights, turnout, campaign effects and partisan-group weights are authored, not estimated from real voters. A field office does not have an empirically asserted 2.8-point effect.
2. Economic values are reduced-form indicators with bounded noise and recovery anchors. No causal forecast, seasonal adjustment, production sectors, income distribution or full national accounts are implied. GDP growth is annualized; a displayed 2% is not monthly 2% growth.
3. Fiscal event responses assume existing lawful contingency authority/resources. They do not replace congressional appropriations or create a presidential power to legislate unilaterally. Full fiscal authorization remains on the roadmap.
4. Diplomatic tension is an index. Neither a warning nor an event choice declares a war. Foreign governments, alliances, force deployment and treaty ratification still need separate models.
5. Ethics exposure is unresolved institutional risk; it is not a finding of criminal guilt. Investigation outcomes are authored uncertainty rather than legal adjudication.
6. One immediate event choice is pending at a time, with multiple delayed consequences allowed. There is no simultaneous crisis command center or day-by-day deadline model.
7. Opinion figures are exact game-state values, not samples of a polling population. Approval by ideology, undecided respondents, polling error and delayed media awareness remain incomplete.
8. Retirement ends the simulation. Pending consequences are retained in the final snapshot rather than erased or automatically declared successful.
9. No automatic conversion of old careers into the new model is offered. Replaying a different model would change historical outcomes; an explicit state-migration design is still required.

## Next correctness gates

1. Implement a real contingent-election phase before describing all presidential outcomes as covered.
2. Overlap incumbent campaigning with the final governing year and preserve the correct January transfer.
3. Model the full House election and Senate classes, actual voting-unit data, recount procedures and third-party/no-majority outcomes.
4. Add day-aware bill presentment, appropriations/authorization and a richer nomination/succession model.
5. Calibrate only where credible data and uncertainty estimates support it; keep fictional scenario parameters labeled.

See [MODEL-NOTES.md](MODEL-NOTES.md) for exact formulas and [VALIDATION.md](VALIDATION.md) for verification. All previous gaps have been reviewed; the open rows above have **not** been silently marked complete.
