# Presidential Simulator — Campaign Events Edition

A text-based campaign game with 36 fictional events, a working opponent, and a UI-independent Java engine. This release builds on the six-class objective-based revision you supplied. The underlying state objectives are invented game rules, not real political data or election forecasts.

## Start playing

**Windows:** extract the full ZIP, open `campaign-events`, and double-click `run-windows.bat`. Choose a menu number and press Enter. Read `START-HERE.txt` if you are new to the game.

**Mac/Linux:** run `sh run.sh` from the extracted folder.

The included `campaign.jar` needs a Java 17+ runtime, with no external dependencies. A JDK is needed only to rebuild edited source. The launcher prefers the JAR; after editing any Java file, run `build-windows.bat` or `sh build.sh` to include your changes.

## What's new

- **36 events:** funding gains/losses, volunteer work, reopened objectives, cost changes, fundraising changes, optional offers, and quiet weeks.
- **Both campaigns matter:** the opponent has its own money and completed objectives. It acts after every second player turn and can be helped or hurt by events.
- **16 turns:** expanded from eight to give event choices and temporary effects room to matter.
- **Guided interface:** welcome screen, short briefing, compact dashboard, seven-state pages, search, state detail, confirmations, and readable recaps.
- **Save/resume:** autosave after every accepted action and event response, including unanswered offers. Reloading replays the exact accepted decisions with the same random sequence.
- **GUI-ready rules:** commands enter `GameEngine`; immutable `GameView` snapshots and `TurnReport` messages come out. `TextUI` renders them without owning gameplay rules.
- **Results:** console details, journal, and the existing optional Swing results window.

## Play loop

1. Pick a difficulty and running mate.
2. Inspect a state and choose one unfinished objective, or fundraise.
3. Confirm the turn and any cost. Unavailable actions explain why they cannot run.
4. Read the recap: your action, the opponent's action when scheduled, and the random event.
5. Respond to an offer if one appears. This does not consume another turn.
6. Continue through turn 16, then inspect the final result and journal.

Browsing, searching, help, invalid input, and cancelled actions are free. An event response must be resolved before another turn or the final election can proceed. Passing a turn still allows the opponent and event steps to happen.

## Board rules

The supplied electoral-vote roster is retained: 50 states plus D.C., totalling 538, with 270 required to win. The fictional starting allocation is 186 EV for the player and 352 for the opponent, using the supplied alphabetical starting-bloc rule.

Every state now uses the same two-objective rule for both sides:

| Completed objectives | State ownership |
|---|---|
| Only your campaign has completed both | Yours |
| Only the opponent has completed both | Opponent's |
| Both campaigns have completed both | Starting owner |
| Neither campaign has completed both | Starting owner |

The prior scripted Texas/Illinois challenges have been removed. The opponent can contest states through the same objective rules. Events never assign EV directly; an objective change can change ownership, and the recap names each state that changes hands.

All states still use winner-take-all allocation in this game. District-level rules, popular-vote totals, contingent-election play, governing, and reelection are not implemented. A 269–269 tie is explicitly reported without a winner.

## Resources and difficulty

| Resource rule | Normal | Hard |
|---|---:|---:|
| Your starting funds | $1,400 | $1,000 |
| Opponent starting funds | $1,200 | $1,600 |
| Campaign length | 16 turns | 16 turns |
| Opponent cadence | Every second turn | Every second turn |

Base costs are $100 for a town hall, $175 for a field office, and $150 for outreach. Fundraising normally adds $250 and costs a turn. Running mates reduce one action's cost by $50, or add $200 to the starting budget. Cash is a fictional resource, not a representation of real campaign costs.

Temporary event effects combine. Action prices cannot fall below $25, fundraising proceeds cannot fall below $50, and mandatory cash losses stop at zero. Optional paid offers must be affordable in full. All effects report their actual consequence.

## Event behavior

One unused, eligible event is selected after each accepted turn. If none is eligible, the game reports that no new event applies. Events do not repeat in a campaign. An event that reopens work is eligible only when that work exists; an event that completes work needs an unfinished matching objective.

State-specific events choose an eligible state. This can strengthen a position without immediately changing ownership. The affected state is always shown. There are no numerical candidate ratings or random vote rolls.

Temporary effects apply to the next two **game turns**, not the next two uses of an action. Because the opponent acts every other turn, a two-turn effect may affect only one opponent action. Timed-effect events are not drawn on the final turn. Cash events can still occur on the final turn; unused final cash does not count toward victory.

See `EVENTS.md` for every event and exact effect.

## Saving

There is one default slot at `saves/campaign.save`. Starting a new campaign asks before replacing it. A save error is shown without claiming success. The Save and Exit menu returns to the game if saving fails.

A save is a versioned setup plus accepted-command journal. It preserves random events, opponent actions, cash, objectives, temporary effects, and pending responses by replay. This format is intentionally tied to the current game rules and event catalog; it is not a cross-version save migration system.

## Developer commands

```text
java -jar campaign.jar --seed 42 --no-gui
java -jar campaign.jar --save saves/second-campaign.save
java -jar campaign.jar --color
```

`--color` enables optional ANSI heading colors for compatible terminals. The default uses plain text and avoids cursor-control sequences, so logs and older terminals remain readable.

Compile and run the tests with a JDK 17+:

```text
javac -Xlint:all -d build *.java tests/EngineTests.java tests/TextUITests.java
java -Djava.awt.headless=true -cp build EngineTests
java -Djava.awt.headless=true -cp build TextUITests
```

If `javac` is not on PATH but the compiler module is present:

```text
java -m jdk.compiler/com.sun.tools.javac.Main -Xlint:all -d build *.java tests/EngineTests.java tests/TextUITests.java
```

There are now **14 production Java files**. Copy the whole source set; replacing only the old six files is insufficient. Remove duplicate filename-suffixed source copies from your build directory. See `ARCHITECTURE.md` for integration and extension points, `REVIEW.md` for design decisions, and `VALIDATION.md` for tested scope.
