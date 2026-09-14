# Development roadmap

This is a proposed solo-developer plan. Estimates are rough planning ranges in focused development time; coursework, playtest availability, and new tooling will affect calendar duration. Re-estimate after the first milestone. Protect the complete playable loop before expanding the feature count.

## Milestones and gates

| Milestone | Rough effort | Deliverables | Exit criteria |
| --- | --- | --- | --- |
| M0: foundation | 1–2 weeks | Source layout, build, console mode, input validation, injected randomness, election correction | Clean checkout compiles; explicit win/loss/no-majority fixtures; no GUI requirement; invalid input recovers |
| M1: text alpha | 4–8 weeks | Candidate presets, 12-turn campaign, election, 16-quarter term, reelection epilogue, approximately 20 events, saves | Complete run without developer intervention; save continuation matches uninterrupted run; every choice has an explained purpose |
| M2: text beta | 3–6 weeks | Expanded customization, event chains, staff, clearer opponent behavior, tutorial, data validation | Five outside testers can start and finish; confusing choices are revised; no unresolved progress-blocking defects |
| M3: governing expansion | 6–12+ weeks | Legislative and institutional interactions, multiple endings, fictional alternate political paths | Normal governing remains playable; expanded paths have tested prerequisites, consequences, and endings |
| M4: GUI alpha | 6–12+ weeks | Chosen delivery platform, map, state panel, events, history, saves, accessibility | Shared fixtures have identical engine outcomes in console and GUI; keyboard-only full run succeeds |
| M5: public 1.0 | 3–6+ weeks | Packaging, docs, credits, license, release notes, feedback operations | Installation/onboarding works on declared platforms; no known release blockers; reproducible release process |
| M6: revenue experiment | 2–4+ weeks after prerequisites | Hosted edition, eligible ad integration or sponsorship, basic operational monitoring | Game works with blocked/failed ads; placements do not obstruct decisions; measured revenue and costs justify operation |

These are not cumulative release commitments. Pause or reduce scope when a gate fails. A desktop GUI can precede M3 if visual usability becomes the main obstacle; keep the engine boundary intact.

## First ten issues

Each row is ready to turn into a GitHub issue. No issues have been created remotely.

| ID | Issue | Acceptance criteria | Depends on |
| --- | --- | --- | --- |
| P01 | Establish repeatable Java build | Pin toolchain baseline; documented build works from clean checkout; build artifacts ignored | None |
| P02 | Make console mode independent | Console prints all results; no Swing call by default; optional GUI path uses event-dispatch thread | P01 |
| P03 | Validate menu input | Text, blank input, out-of-range input, and EOF have defined behavior | P01 |
| P04 | Inject run configuration and randomness | Seed/configuration are explicit; repeated action sequence reproduces results | P01 |
| P05 | Replace guaranteed-win placeholder | Engine supports explicit winning and losing fixtures; distinct regional scenario conditions; meaningful campaign effects | P04 |
| P06 | Represent no-majority outcomes | Model and both displays distinguish all outcome categories; scenario defines resolution | P05 |
| P07 | Define resource semantics | Every attribute has unit, bounds, owner, and at least one meaningful use or is removed from initial UI | P05 |
| P08 | Add campaign turn loop | Finite turns, constrained actions, summaries, and election transition; invalid actions do not spend resources | P03–P07 |
| P09 | Add versioned save/load | Atomic writes, clear invalid-version error, uninterrupted-versus-restored equivalence | P08 |
| P10 | Implement one governing slice | Election victory enters office; one event and policy decision lead to a testable response and next quarter | P08–P09 |

Then expand event content and governing to the M1 targets. Write one good event chain before writing twenty unrelated events.

## Feature backlog by release

**M1 must have:** playable beginning/middle/end; candidate preset; running mate; campaign resources; coherent election; basic governing; local saves; deterministic reports; no-majority result; replay; a short tutorial.

**M2 should have:** platform choices within fictional scenarios; multiple backgrounds; staff roles; event prerequisites and delayed consequences; import/export run setup; playtest reports; scenario validation; difficulty with documented effects.

**M3 expansion:** legislature and courts as independent actors; appointments; budget cycles; diplomacy and crises; succession; second term; constitutional and alternate-history scenario rules; institutional erosion and restoration storylines; divergent legacies.

**M4 presentation:** original map assets; regional tooltips; event cards; decision previews; trend history; keyboard navigation; color-independent results; audio controls; scalable text; smaller-screen layout.

**After 1.0:** visual scenario editor; data-only mod distribution; localization; challenge scenarios; additional fictional settings; optional hosted saves. Multiplayer, live political data, executable mods, and generated narrative remain separate proposals because each adds substantial complexity.

## Working rhythm

Use a project board with Backlog, Ready, In progress, Review/playtest, and Done. Keep at most one major feature in progress. A feature is done when its acceptance criteria, relevant tests, documentation, and one playthrough pass.

Once a week, triage feedback and choose the next small slice. Label reproducible crashes and lost progress as blockers. Promote repeated player confusion ahead of cosmetic work. Distinguish a bug from a balance opinion and a feature request.

At each milestone, tag a release, include known issues and save compatibility, and ask testers three questions: Where were you confused? Which choice felt meaningful? What would make you play again?

## Decision gates

1. **Before public source release:** establish ownership, choose source license, inventory asset rights, and verify a clean build.
2. **After text alpha:** decide whether the core loop merits expansion from observed player behavior; shorten or revise weak phases.
3. **Before major GUI work:** compare one-turn browser/API and desktop prototypes. Choose based on distribution priority, offline needs, operating cost, and maintenance effort.
4. **Before advertising:** identify a supported hosting/provider combination, review current terms, test failure behavior, and estimate costs from actual usage.
5. **Before extensive modding:** stabilize event/save schemas and validate malformed content safely.

## Release checklist

- [ ] Build and necessary engine tests pass on supported platforms.
- [ ] Start-to-ending smoke playthrough passes.
- [ ] Seed/configuration and save compatibility are documented.
- [ ] Player-facing docs match shipped features.
- [ ] License, copyright notices, credits, and third-party notices are present.
- [ ] Feedback links and templates work in the actual repository.
- [ ] Known issues and migration notes appear in release notes.
- [ ] GUI release supports keyboard navigation and non-color result labels.
- [ ] Hosted release recovers from failed requests and ads.
