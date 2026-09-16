# Presidential Simulator — Interface Playtest

A text-based political career: campaign, debate, win or lose, govern month by month, and face reelection. This release focuses on a readable interface and a reliable first-time experience.

**Start a new career. Older saves are intentionally unsupported.** There is one current ruleset and no import menu.

## Open the game

Requires Java 17 or newer. Extract the entire ZIP into a writable folder.

- **Windows:** double-click `run-windows.bat`.
- **macOS/Linux:** run `sh run.sh` from the extracted folder.
- **Direct launch:** `java -jar presidential-simulator.jar`.

Choose **1 — Start a new career**, then pick a difficulty and running mate. Choose **3 — How to Play** first if you want a short guide; it works without creating a career.

## Your first few minutes

1. Open **Take a campaign action → Campaign in a state**. Inspect the available work and its cost. Confirm a visit to spend cash and one campaign turn.
2. Use **Fundraise** when cash runs low. Passing a turn still lets the opponent and events advance.
3. Read event descriptions and compare the answers. The displayed main effects are a summary; **4 — Details** shows the full effects and missed-response consequence.
4. Check **ST → Statistics**. Campaign forecasts are uncertain, and both candidates can win.
5. Use **0 → Game menu** for saving, settings, help, developer tools, or leaving the game.

The campaign has 16 turns. Victory leads through transition to the presidency; defeat opens four annual rebuilding decisions before another campaign. Each presidential month allows up to two actions, then you explicitly choose **End month**. Your record affects future elections. The normal career permits two elected terms.

## Controls and readable screens

| Control | Result |
| --- | --- |
| Number + Enter | Choose the displayed option |
| 0 + Enter | Back or Game menu, as labeled on that screen |
| ? + Enter | Open short help topics, then return to your screen |
| ST + Enter | Open statistics during a career |
| > / < + Enter | Read the next or previous page |
| D + Enter | Open the full report after an action |

The default frame is **100 columns × 28 rows**. Descriptions, headings, status lines and prompts wrap instead of being cut off with ellipses. Long screens use pages; the footer tells you which page is open. Decision prompts keep response/navigation shortcuts visible. Repeated invalid entries display one helpful correction without growing the screen indefinitely.

Interactive terminals use a fixed frame. Rows redraw only when changed; timer ticks update one reserved area without appending lines or moving your input cursor. `--plain` gives an ANSI-free transcript and omits intermediate tick output (timeouts still apply). `--no-color` disables color. Resize the frame through **Game menu → Game settings → Display size**, or launch with `--width 100 --height 28`. At launch, the requested frame is capped to the detected visible window, including on Windows. The Windows launcher explicitly selects full-screen mode. Automatic resizing while playing remains future work; use Display size after changing the window.

## Live countdowns

Three campaign debates occur on turns 4, 8 and 12. Each has three questions with a fresh **60-second** answer clock. Live press briefings also allow 60 seconds; live interviews allow 45.

The footer is the authoritative **Time left** display. It visibly changes each second without Enter, using elapsed real time. Choose and confirm before it reaches zero. An unanswered debate question is recorded as missed and advances to the next question. Explicitly leaving skips the remaining questions.

**Menus, help, statistics and effects details pause the clock.** Confirmation and page browsing do not. The next question's timer waits while you read the previous report. Saving preserves whole seconds; offline time is not charged. A fractional second may be lost on save/restart.

For untimed answers, turn live clocks off under **Game menu → Game settings → 5**. Ordinary campaign offers and world decisions still use their displayed turn/month deadlines.

Debate answers have resource and scoring tradeoffs. Opponents have a consistent style. The final score margin produces a small bounded vote-share effect that persists to election day. **ST → 7** shows the current campaign's debate record. The scoring and economic coefficients are fictional game parameters, not calibrated forecasts.

## Saving and exiting

Accepted actions autosave to `saves/career.save`. Three manual slots (`slot-1.save` through `slot-3.save`) support names and three rotating backups each. Manual saving displays confirmation. Use **Load / resume** for autosaves, slots and backups.

Use **Game menu → Leave or end career → Save and exit** to leave safely. Retirement is separate and asks for confirmation. Closing the input stream also attempts an autosave. Older-format files are rejected with an explanation and are not overwritten by loading.

## Development and limits

Source code and the runnable JAR are included. Rebuild with `sh build.sh` or `build-windows.bat`; run the current test suite with `sh test.sh`.

The engine remains independent of the terminal: commands change state, immutable views describe it, and the UI submits elapsed-clock commands. Save compatibility is not a development requirement. Older helper code may still be refactored, but older rule profiles and career imports are no longer available.

[VALIDATION.md](VALIDATION.md) lists actual checks and platform limits. [IMPLEMENTATION-LOG.md](IMPLEMENTATION-LOG.md) tracks remaining work, including a guided tutorial career, automatic resize handling, richer debate dynamics and the existing institutional/calendar audit. [MODEL-NOTES.md](MODEL-NOTES.md) and [REALISM-AUDIT.md](REALISM-AUDIT.md) document simulation assumptions; historical release notes in those files are superseded by this build's single-ruleset/save policy.

## Stable-screen patch

The renderer no longer writes linefeeds or clears the whole screen during normal full-screen redraws. It temporarily disables automatic line wrapping, keeps a spare row below input, and restores normal wrapping on exit. The timer preserves the input cursor and edits only its reserved footer cells. See [STABILITY.md](STABILITY.md) for verification and platform limits.
