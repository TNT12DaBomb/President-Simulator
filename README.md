# Presidential Simulator — Monthly Edition

A playable career skeleton built from the latest twenty supplied Java files. Requires Java 17 or newer; no external libraries. Includes the executable `monthly.jar`, all source, tests, launchers, and a categorized backlog.

## Start playing

Extract the entire ZIP into a writable folder. On Windows, double-click `run-windows.bat`. On macOS/Linux, open a terminal in that folder and run `sh run.sh`. Alternatively run `java -jar monthly.jar`. All gameplay uses numbered menus; no commands need to be memorized. `0` returns or opens the game menu. Optional color: `sh run.sh --color`. Repeatable new career: `sh run.sh --seed 42 --no-gui`.

## What this milestone delivers

Campaign → election review → transition → inauguration → 48 monthly turns → midterms after month 24 → term review → another campaign → second inauguration → retirement after 96 total months of service. Losing opens four annual rebuilding choices, followed by another run. Nonconsecutive terms are allowed; two elected terms is the normal cap.

The campaign still uses the supplied fictional state-objective board, 16 turns, 36 campaign events, running-mate discounts, and opponent actions. State names and EV totals label a game board; outcomes are not forecasts of real elections. No automatic victory was added to normal play.

Every presidency month permits two actions. **Actions do not advance time: choose End month explicitly.** That settles $100 receipts and $75 operating costs, processes delayed effects, checks midterms, and may open an event. Unused actions expire. Events must be answered before more office actions or time advancement, with a free response available. Save/load/exit remain available during a pending event.

### Menus and action depth

| Menu | Working mechanics |
| --- | --- |
| Office & administration | Cabinet coordination, annual reconciliation, service review, appointment and preparedness hooks |
| Legislative agenda | Eight issue areas; propose, negotiate, sign, veto; one active bill at a time |
| Public & personal | Briefings, visits, personal time; all recorded and limited by monthly actions |
| Party & elections | Ally campaign commitment for the midterm branch; donor-meeting hook |
| Statistics & records | Operating accounts, Congress, promises, signed initiatives, vetoes, follow-ups, full career journal |
| Game menu | Autosave, three manual slots, load, new game via title, settings, developer tools, retirement, exit |

Visits, appointments, preparedness, donor meetings, and rest are shallow hooks with costs/action use and history entries. They do not yet simulate appointments requiring confirmation, active crises, fatigue, or donor fundraising returns. Briefings, cabinet work, budget review, service work, promises, and midterm organizing have additional consequences described below.

### Congress and policies

Congress begins with your caucus holding 222 of 435 House seats and 49 of 100 Senate seats. A bill requires 218 House seats and 51 Senate seats, or an explicitly negotiated agreement. Negotiation uses one action and grants agreement for one bill; signing or vetoing consumes it. No random legislative pass/fail roll. Midterms or a developer control change cancel existing agreements and refresh the bill's displayed stage.

The midterm is deliberately an **authored game scenario**, not an election simulation. Completing an ally campaign commitment before the checkpoint selects 225 House seats; otherwise 210. Completing a public briefing selects 52 Senate seats; otherwise 48. These are transparent placeholder rules for exercising control changes. There are no ideological scores, ideological composition, leadership approval, or probability forecasts.

Issue areas: economy, taxes, healthcare, immigration, defense, environment, education, civil rights. Each initiative expands or reorganizes a fictional program. These choices establish a reusable workflow; they do not claim macroeconomic or voter-bloc effects.

### Promises and reelection continuity

Record up to two promises from the campaign desk, for free, before the election. Once recorded, a pledge cannot be overwritten during that campaign. Signing its matching approach marks it kept; signing the opposing approach records a contradiction. A later matching law restores its current status, while the reversal remains in history. A new campaign starts a fresh pledge list. Term-end law and pledge details are archived before a new presidency replaces the active model.

Completed town hall, field office, and outreach work in states you hold supplies one-use transition credits. The final administrative year's briefing, cabinet, and service records can supply one objective of each corresponding type in the next campaign. Annual budget reconciliation recovers $100 only once per year; published final-year accounts also unlock a $100 fictional private fundraising benefit next campaign. A kept promise supplies an outreach objective; any contradicted promise withholds that outreach carryover. Public treasury is never transferred to campaign cash.

The existing defeat consequences remain: loss recorded, review outstanding, and a $200 campaign reserve withheld until donor meetings occur. This release does not yet vary those consequences by loss margin.

### Presidency events

Six initial events: records backlog, supplier correction, implementation review, building maintenance, archive request, staff training. The implementation review requires a signed law; each event has an earliest month, weight, choices, immediate effects, and optional delayed effects. Each appears at most once per term. Default opportunities every six months; settings can switch to every three months or off. Selection uses the career seed and never rerolls on browsing or reload. Delayed work due beyond the term is settled at term end; no new event is drawn after month 48. Turning events off retains already pending and scheduled effects.

### Saves and developer tools

Autosave follows accepted commands and EOF. Default `saves/monthly.save`; manual slots `saves/monthly-slot-1.save` through `monthly-slot-3.save`. Atomic replacement where supported. Save/load uses versioned deterministic command replay, including pending events, delayed work, settings, and developer actions.

**Quarterly `presidency-career-v1` saves are not migrated.** They must be opened with the previous Career Edition; this release rejects them with a clear message and uses different default filenames. `campaign-events-v1` imports remain supported through the load menu (`saves/campaign.save`). Do not intentionally point `--save` at an older career file you want to preserve.

Developer tools force campaign victory/defeat; jump to month 25 of either term, the final month of term two, or reelection; grant resources; toggle chamber control; trigger an eligible event; advance one month; or finish a term automatically. Finish-term declines pending events using the free response. All shortcuts mark the save. Scenario jumps replace the active timeline and retain earlier journal entries as development history. They do not fabricate EV totals. Normal term limits are not unlocked.

## Scope boundaries

Public feedback and organizational support are descriptive; **numerical approval, candidate ratings, national GDP, inflation, unemployment, popular vote, turnout, scandals, wars, voter blocs, and margin-based mandate calculations are not implemented**. Operating cash is not labeled as the national economy. No dictatorship path is included. The milestone establishes the monthly workflow and extension points; larger feature families are tracked in `BACKLOG.md`.

The calendar starts with inauguration in January 2029. Each campaign is a separate 16-turn interlude between four-year governing blocks. Midterms use the exact halfway checkpoint after 24 months, not a real-world November schedule. No additional presidential service accrues during the campaign interlude.

## Build and test

`sh build.sh` or `build-windows.bat` rebuilds `monthly.jar` with JDK 17+. Launch scripts prefer the included JAR; rebuild after editing source.

```sh
java -m jdk.compiler/com.sun.tools.javac.Main -Xlint:all -d test-build ./*.java tests/*.java
java -cp test-build EngineTests
java -cp test-build MonthlyTests
```

See `ARCHITECTURE.md` for model boundaries and `VALIDATION.md` for what was checked.
