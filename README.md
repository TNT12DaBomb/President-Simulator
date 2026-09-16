# Presidential Simulator — Governance Edition

This release completes three next steps from the backlog: policy dilemmas (PRES-04), public correspondence and history (PRES-05), and playable midterms (ELEC-02). It retains the monthly career loop, campaign system, saves, and developer tools.

## Start

Extract the full ZIP into a writable folder. Windows: double-click `run-windows.bat`. macOS/Linux: run `sh run.sh` in that folder. Direct launch: `java -jar governance.jar`. Requires Java 17 or newer. The runnable JAR is included; no external libraries are needed.

Type a menu number and press Enter. `0` goes back or opens the game menu. New campaign difficulty and running mate are selected during setup. Use `--seed 42` for repeatable new games, `--color` for optional color, `--no-gui` for text only, or `--help` for launch options. Rebuild after editing source: `sh build.sh` or `build-windows.bat`.

## Monthly career loop

Election → transition → inauguration → 48 monthly turns → another election → second term → retirement after 96 months of service. Nonconsecutive terms remain possible. A defeat opens four annual rebuilding decisions; unresolved donor meetings withhold $200 from the next campaign. Two victories is the normal election limit. Developer overrides mark the save and never fabricate EV totals.

Take up to two actions per month, then select **End month**. Closing a month adds $100 operating receipts and deducts $75 routine expenses, processes follow-ups and policy deliveries, resolves midterms after month 24, and may draw an office event. Browsing and cancelling are free. Pending random events must be answered before spending more actions or advancing time; a free response is always available.

The calendar starts with inauguration in January 2029. Campaigns remain separate 16-turn interludes between governing blocks. Midterms use the exact halfway point after 24 months, not the real-world November calendar. There is no extra presidential service during campaign interludes.

## New: concrete policy choices

Each of eight issues offers two named alternatives. Examples include community clinic sessions versus unified referral scheduling, and tutoring sessions versus pooled purchasing. The policy menu displays the benefit, competing concern, signing cost, and delivery period before confirmation.

| Approach | Signing cost | Delivery | What the choice means |
| --- | --- | --- | --- |
| Expand program | $180 | After three month closures | Additional local service capacity, with extra staffing/administrative work |
| Reorganize program | $100 | After two month closures | A shared process, with transition work or reduced local flexibility |

These are authored game costs and service records, not national budget estimates. The actual tradeoff mechanics are money, delivery time, and which group's request is addressed; the game does not simulate macroeconomic or voter-bloc consequences.

Propose an initiative, secure Congress's support, then sign or veto it. Proposing, negotiating, signing, and vetoing each use one action. Signing requires sufficient treasury funds and starts an implementation record. Signing an already-active identical policy is rejected without spending anything; veto the duplicate bill to clear the desk. Reversing an initiative cancels unfinished work for that issue, spends the new appropriation, and does not refund the old one. Only the replacement can subsequently deliver.

No more than one bill is active at once. Congress requires 218 of 435 House seats and 51 of 100 Senate seats, or a negotiated agreement for one bill. Signing or vetoing consumes the agreement. Midterms and developer chamber-control changes invalidate it and refresh the bill's stage.

## New: public correspondence

**Public & personal → Public correspondence** lists sixteen requests across distinct authored groups. Each issue has two competing requests, one for each policy approach. Pages show four requests at a time.

- **Open:** the request is unresolved.
- **Acknowledged:** a specific reply was published; it does not claim the service exists.
- **Delivery scheduled:** the requested initiative was signed and funded.
- **Delivered:** its scheduled implementation finished.

A targeted reply costs one monthly action and no money. Each request can receive one reply per term; repeated replies are rejected. Replying never downgrades an existing delivery state. Choosing one approach leaves the alternative request open. A later reversal reopens the displaced request, including one previously delivered, while dated history preserves what happened.

**Public & personal → Correspondence history** shows the dated updates. The dashboard retains a concise latest feedback message. The full correspondence history is separate from general briefings, which do not automatically answer or fulfill every request. At term end, request statuses, laws, and promises are archived into the career journal.

## New: playable midterms

Open **Party & elections** to inspect the board. Visits are available only during months 13–24, before the close of month 24. Each contest requires **a listening visit and an organizing visit**. Each visit spends one of the same two monthly actions as governing; no public treasury money is spent. Duplicate visits are rejected.

| Board component | Seats for your caucus |
| --- | --- |
| Fixed House seats | 210 |
| Eight House slates | Two seats for each slate with both visits completed |
| Fixed Senate seats | 48 |
| Four Senate races | One seat for each race with both visits completed |

Incomplete contests go to the other caucus. All other seats are fixed for that caucus; House totals remain 435 and Senate totals remain 100. Four complete House slates and three complete Senate races produce control of both chambers. Winning every contest requires all 24 available actions during the campaign window, leaving no room for other actions then. The board stays visible after resolution, with each outcome recorded.

These are fictional contest bundles and deterministic objective rules. There is no hidden pass/fail roll or attempt to forecast real elections. General ally meetings and public briefings no longer select chamber totals. The old generic ally command remains a bookkeeping hook for source compatibility but is not offered as a substitute for targeted visits.

## Campaign promises and reelection

Record up to two named policy promises during a campaign, for free. A promise cannot be rewritten within that campaign. A matching signed law marks it kept; a conflicting law records a contradiction. This is a **legislative** promise status: actual delivery is separately visible in correspondence. Reversals remain in history even if a later law restores the promised approach.

Completed town halls, field offices, and outreach in states you hold supply one-use transition credits. Final-year administrative work prepares corresponding objectives for the next campaign. A kept promise adds outreach preparation; any contradicted promise withholds it. Annual budget reconciliation recovers $100 once per administrative year. Published final-year accounts unlock a $100 fictional private fundraising benefit next campaign. Public treasury is never transferred to campaign cash.

## Events and shallow hooks

The original campaign has 36 random events. The presidency retains six random events with prerequisites, choices, costs, and delayed effects. Office event frequency is selectable: off, every six months, or every three months. Eligible events are selected with the saved seed and appear at most once per term. Browsing/reloading does not reroll them. Turning events off retains existing pending responses and follow-ups.

Policy deliveries are separate scheduled work, not random events. Both kinds of follow-up settle no later than term end. No new random event is drawn after month 48.

Appointments, preparedness, visits, rest, and donor meetings remain shallow hooks. Named officials, confirmations, fatigue, active crises, and donor returns are not implemented. Public feedback is descriptive; national approval percentages, GDP, unemployment, wars, scandals, and authoritarian paths remain outside this release. The operating account is not the national economy.

## Saves and developer tools

Default autosave: `saves/governance.save`. Three manual slots: `saves/governance-slot-1.save` through `governance-slot-3.save`. Accepted commands and end-of-input autosave. Save/load/exit remain available while an event is pending. Writes replace atomically where supported. The versioned command journal reproduces proposals, implementation queues, requests, midterm work, settings, and developer actions.

**Earlier monthly and quarterly career saves require their original editions.** This release uses `presidency-governance-v1` because the same actions now have different costs and midterm outcomes. It rejects older career rules rather than silently changing a saved game. New default and slot filenames keep those files separate. Do not intentionally point `--save` at an older career file you want to retain. `campaign-events-v1` import from `saves/campaign.save` is still supported.

Developer tools include forced outcomes, money, chamber control, event triggering, time advancement, reelection and term jumps. **Jump to a test scenario → Start midterm campaigning** opens month 13; this is the fastest way to test the new board. A scheduled event may need a response first. Scenario jumps replace the active timeline and mark earlier entries as development history. Finishing a term automatically declines pending events and leaves uncompleted midterm contests to the other caucus.

## Development

28 production Java sources, no external dependencies. The model remains independent of the terminal renderer. See `ARCHITECTURE.md`, `BACKLOG.md`, `CHANGELOG.md`, and `VALIDATION.md`.

```sh
java -m jdk.compiler/com.sun.tools.javac.Main -Xlint:all -d test-build ./*.java tests/*.java
java -cp test-build EngineTests
java -cp test-build MonthlyTests
java -cp test-build GovernanceTests
java -cp test-build GovernanceUITests
```
