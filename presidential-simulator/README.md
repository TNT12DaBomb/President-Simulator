# Presidential Simulator

A political strategy game about earning office, governing under pressure, and deciding what legacy to leave.

Started as a passion project for a friend, Presidential Simulator is intended to grow from a Java prototype into a replayable, open-source game. The first milestone is a complete text adventure with consequential choices, inspired by the pacing of *The Oregon Trail*. The longer-term direction is an original map-driven interface with the strategic readability of *Plague Inc.*

**Status: early prototype.** This package documents the supplied code and a proposed development direction. It is not a finished game or a published release. No ads, account system, save system, or governing phase exist yet. Open-source licensing is proposed and must be finalized before publication; see [licensing and revenue](docs/LICENSING_AND_REVENUE.md).

## Contents

- [What works today](#what-works-today)
- [Run the current prototype](#run-the-current-prototype)
- [Known limitations](#known-limitations)
- [Game vision](#game-vision)
- [First complete text release](#first-complete-text-release)
- [Future customization and governing](#future-customization-and-governing)
- [Architecture](#architecture)
- [Roadmap](#roadmap)
- [Feedback and contributions](#feedback-and-contributions)
- [Licensing and revenue](#licensing-and-revenue)

## What works today

The current program asks for a difficulty, a running mate, and one campaign focus; it then runs an election and opens a Swing results window.

| Component | Current implementation |
| --- | --- |
| Entry point | `PresidentialSimulator` coordinates console input and election results. |
| Player data | `President` stores nine mutable attributes, including approval, economy, trust, treasury, and health. |
| Running mate | Four choices apply fixed attribute changes. |
| Campaign | One of three choices applies fixed attribute changes. |
| State data | `State` stores name, electoral votes, and political leaning. Every supplied leaning is zero. |
| Election calculation | `ElectoralCollege` calculates outcomes for 50 states and D.C. using player attributes and randomness. |
| Result data | `ElectionResult` defensively copies the state result map and exposes an unmodifiable view. |
| Results UI | `ElectionGUI` shows colored state cards and electoral totals. It is a grid, not a geographic map. |
| Data validation | The constructor checks the state electoral total and the total including D.C. |

These descriptions come from inspection of the six supplied Java files. See the [code review](docs/CODE_REVIEW.md) for defects and verification limits.

## Run the current prototype

### Requirements

- A Java Development Kit; JDK 17 is the proposed baseline to verify.
- A desktop environment for the Swing window.
- A terminal or IDE able to provide standard input.

The package uses only Java standard-library imports. No Maven or Gradle build is included yet. Source files in this package have been renamed to match their public class names; their contents are unchanged from the uploads.

From the repository root, on macOS/Linux or Windows Command Prompt:

```sh
mkdir out
javac -d out src/State.java src/President.java src/ElectionResult.java src/ElectoralCollege.java src/ElectionGUI.java src/PresidentialSimulator.java
java -cp out PresidentialSimulator
```

On PowerShell, use `New-Item -ItemType Directory -Force out` for the first command; the two Java commands are the same.

Choose numeric menu options when prompted. The current process can remain alive while the results window is open; close the window when finished.

**Verification:** these commands are based on source inspection. The review environment had a Java runtime but no `javac`, so compilation and interactive desktop execution were not verified.

### Troubleshooting

| Symptom | Action |
| --- | --- |
| `javac` is not found | Install a JDK and ensure its `bin` directory is on PATH. A runtime alone is insufficient. |
| Public class filename error | Use the included `src/` names, without the uploaded `(1)` suffix. |
| Main class cannot be found | Compile from the repository root, then run with `-cp out`. |
| Text input crashes the menu | The current prototype expects integers. Input validation is a planned first fix. |
| Headless/Swing error | Run in a graphical desktop session. A genuine console-only mode is planned. |

## Known limitations

- The supplied campaign paths cannot produce a loss: the scoring threshold is below the minimum outcome reachable from the starting attributes. Election balancing is a blocking issue.
- Difficulty is read but never used.
- All state leanings are identical; the opponent has no separate model.
- Several player attributes have no effect on the election.
- Inputs are not robustly validated; attributes have no enforced bounds.
- A tied Electoral College would be presented as a player loss, without a distinct no-majority result.
- Maine and Nebraska are treated as winner-take-all in this simplified implementation.
- Randomness cannot be supplied through a seed for reproducible bug reports.
- There is one campaign decision, no governing loop, no reelection, no saves, no mod support, and no automated test suite.
- The console flow always opens Swing; it is not yet a text-only game.

The electoral allocation matches the National Archives' table for the 2024 and 2028 elections. Maine and Nebraska use district allocation plus statewide electors in the real system; the prototype does not implement that distinction. This is a game abstraction, not an election forecast. [National Archives: allocation](https://www.archives.gov/electoral-college/allocation)

## Game vision

Create a run that tells a story: a candidate with a background and ambitions builds a coalition, faces setbacks, enters office, and encounters the consequences of earlier decisions.

Design principles:

- **Choices have visible tradeoffs.** Explain costs and likely categories of consequences without revealing every event in advance.
- **Consequences persist.** Promises, relationships, and unresolved crises follow the player into later turns.
- **Losing produces a worthwhile ending.** Summarize the run and make another attempt easy.
- **Customization remains understandable.** Start with useful presets, then expose advanced settings.
- **Institutions matter.** Governing involves other actors with their own interests and constraints.
- **The engine works independently of presentation.** Text and graphics should consume the same rules.
- **Fictional gameplay stays distinct from factual reference material.** Use fictional candidates and clearly label alternate-history rules.

The inspirations describe pacing and interface goals. The project should use original writing, art, branding, and interface assets.

## First complete text release

Proposed scope: one self-contained campaign, one presidential term, and a reelection or departure ending. Aim for a playtest session of roughly 30–60 minutes; adjust pacing from observation.

1. Choose a candidate name, background preset, party affiliation within the scenario, and difficulty.
2. Choose a running mate and campaign priorities.
3. Play a finite campaign across twelve turns, allocating limited time and campaign funds.
4. Encounter branching events, debates, staff problems, and competing commitments.
5. Resolve election night, including a distinct no-majority branch.
6. If elected, govern for sixteen quarterly turns with policy choices, institutional responses, and crises.
7. Face reelection, retirement, or an early ending determined by the run.
8. Receive a narrative legacy summary and an option to replay.

A reelection victory ends the first release with an epilogue; playing a second term is a later feature. This keeps the first release finite while establishing the full campaign-to-office experience.

MVP content target: approximately twenty reusable event definitions across campaign and governing phases. These are development targets, not shipped content. Each event needs eligibility conditions, choices, effects, follow-up text, and a testable consequence.

Required supporting features: robust input, help, turn summaries, seeded runs, local save/load, clear endings, and no requirement to open a window.

Deferred from this first release: complete primaries, detailed legislative simulation, playable authoritarian consolidation, comprehensive custom scenarios, multiplayer, mobile packaging, accounts, and advertising.

## Future customization and governing

| System | Early version | Later expansion |
| --- | --- | --- |
| Candidate | Name and background presets | Biography, traits, strengths, drawbacks, portraits, starting relationships |
| Campaign | Limited actions and running-mate choice | Primaries, regional operations, staff roles, debate preparation, platform editor |
| Opponents | Fictional opponent archetypes | Distinct priorities, campaign responses, rivalries, multiple candidates |
| Run setup | Difficulty and seed | Starting resources, scenario rules, event frequency, institutional conditions |
| Governing | Quarterly decisions and consequences | Legislative negotiations, appointments, budget cycles, diplomacy, emergencies |
| Institutions | Basic response rules | Courts, legislature, civil service, media, regional governments, civic organizations |
| Political trajectories | Election, reelection, retirement, early exit | Fictional democratic reform, institutional erosion, authoritarian consolidation, restoration |
| Endings | Victory/loss and legacy narrative | Multiple-term legacies, succession, resignation, removal, regime transition |
| Content | Authored event pool | Branching event chains, scenario packs, localization, data-only community mods |
| Presentation | Console | Interactive map, state inspector, event cards, timelines, accessible charts |

A dictatorship route should be a developed fictional game system with institutional resistance, social consequences, and unstable outcomes. It should not be a single button or automatically the best ending. Build ordinary governing first so alternate political trajectories have meaningful systems to interact with.

In a U.S.-based scenario, distinguish the constitutional ruleset from fictional rule changes. A no-majority election is not simply an opponent victory: the constitutional process involves Congress. [National Archives: Electoral College FAQ](https://www.archives.gov/electoral-college/faq)

## Architecture

Keep Java for the text prototype. Extract rules from `main` before building a new GUI. See [architecture and design](docs/ARCHITECTURE.md).

Proposed engine contract:

```text
apply(GameState, PlayerAction, RandomSource) -> TurnResult
```

`TurnResult` returns the next state, narrative entries, and domain events. It does not read a scanner, open a window, call an ad provider, or write directly to a save file.

| Layer | Responsibility |
| --- | --- |
| Domain | Candidate, campaign, institutions, election outcomes, run configuration |
| Engine | Validate actions, apply costs/effects, advance time, resolve events and endings |
| Content | Versioned scenario and event data |
| Persistence | Save/load, schema migration, deterministic replay metadata |
| Console | Menus, input validation, text formatting |
| GUI | Map, dashboards, event cards, controls |
| Delivery | Optional web API, deployment, ads, and operational concerns |

The existing Swing screen is useful as an election-results prototype. A future browser GUI can call a Java engine through an API, but that introduces hosting and operations work. An offline desktop GUI can share the engine directly. Choose the long-term delivery platform after the text loop has been playtested; see the explicit decision gate in the roadmap.

### Package contents

- `src/`: six original source files with normalized filenames.
- `README.md`: project overview, current instructions, limitations, and goals.
- `docs/CODE_REVIEW.md`: concrete findings from the uploaded prototype.
- `docs/ROADMAP.md`: phased milestones, acceptance criteria, and first backlog.
- `docs/ARCHITECTURE.md`: engine boundaries and evolution toward graphics.
- `docs/LICENSING_AND_REVENUE.md`: proposed licensing, monetization, and launch decisions.
- `docs/GITHUB_SETUP.md`: repository configuration and feedback workflow.
- `CONTRIBUTING.md`: contribution process.
- `.github/ISSUE_TEMPLATE/`: bug, feature, and playtest templates.
- `.github/pull_request_template.md`: change-review prompts.

## Roadmap

| Milestone | Outcome | Completion gate |
| --- | --- | --- |
| Foundation | Reliable console prototype | Reachable win/loss, input recovery, explicit tie, reproducible randomness |
| Text alpha | Campaign → office → ending | Complete playable run, save/load, meaningful choices, clear consequences |
| Text beta | Replayable customization | Expanded candidate setup, event chains, tested scenario data |
| Governing expansion | Deeper institutions and alternate paths | Multiple viable fictional trajectories with understandable consequences |
| GUI alpha | Map and dashboards over the engine | Equivalent rules and outcomes across UI clients |
| Public 1.0 | Supported release | Onboarding, accessibility, packaging, documentation, feedback triage |
| Sustainable release | Optional revenue | Stable hosted experience, provider eligibility, acceptable ad experience |

Detailed tasks and exit criteria are in [ROADMAP.md](docs/ROADMAP.md). Milestones are a proposed sequence, not promised release dates.

## Feedback and contributions

Once the repository is created, use **Issues → New issue** for bugs, feature requests, and structured playtest feedback. Use **Discussions** for open-ended ideas, questions, and run stories after Discussions is enabled.

Useful reports include the release or commit, operating system, Java version, seed and scenario when available, reproduction steps, and expected versus observed behavior. The current prototype has no seed to report; state that explicitly.

Please search for duplicates and avoid uploading private information in logs or saves. Technical contributors can start with [CONTRIBUTING.md](CONTRIBUTING.md). Writers, playtesters, artists, and accessibility reviewers can also contribute.

Issue templates are included locally; GitHub features are not configured until the repository is created and these files are pushed. [GitHub issue templates](https://docs.github.com/en/communities/using-templates-to-encourage-useful-issues-and-pull-requests/about-issue-and-pull-request-templates) · [GitHub Discussions](https://docs.github.com/en/discussions/quickstart)

## Licensing and revenue

**Proposed default: MIT for source code**, if the maintainer wants simple reuse and is comfortable with commercial or closed-source forks. **Alternative: GPLv3** if distributed derivatives should preserve source availability under the same license. Both permit commercial use, so advertising does not require making the game proprietary. [MIT](https://choosealicense.com/licenses/mit/) · [GPLv3](https://choosealicense.com/licenses/gpl-3.0/)

No `LICENSE` has been applied in this planning package. Before an open-source release, establish who owns the supplied work, choose the license, and add its complete text with appropriate notices. Asset rights and third-party notices must be tracked separately. See [LICENSING_AND_REVENUE.md](docs/LICENSING_AND_REVENUE.md).

Initial plan: release a free, ad-free text game. Later, evaluate a hosted edition with unobtrusive ads outside active decision controls and an optional sponsorship route. Advertising approval and revenue are not guaranteed. The game must remain playable if ads fail to load.

Source hosting on GitHub and commercial game hosting are separate decisions. Review hosting restrictions before placing a revenue-generating application on GitHub Pages. [GitHub Pages limits](https://docs.github.com/en/pages/getting-started-with-github-pages/github-pages-limits)
