# Debate protocol — v0.5

Current careers schedule appearances at campaign turns 4, 8 and 12. `GameEngine` hosts a `PressureEvent`; `LiveDebate` exposes a current event rather than printing or reading input. The controller converts elapsed time into ordinary journaled commands. Every stage/preparation revision changes the decision identity, preventing a queued click from answering a later stage.

Sequence: preparation → factual answer → confidence → opponent claim → recovery → sourced feedback. Repeat three factual rounds, then a policy tradeoff, record follow-up and debrief. Preparation/feedback/debrief are untimed. Answer windows are 12 seconds, rapid fire 8, confidence 10, opponent delivery 3, recovery 6, policy/record 25. These are game timings, not real debate rules. Accessibility settings disable the clock.

Six briefing slots permit three two-slot topics: economy, Constitution and own record. Notes appear for prepared factual subjects; record preparation recalls the latest statement. Answers shuffle once per question, not per repaint. Question selection prefers topics absent from the retained career statement ledger. Opponent behavior is scripted: a Prosecutor uses the correct source statement, a Brawler can present a false correction, and a Statesman may use either. These are fictional scene behaviors, not numerical candidate assessments.

Knowledge and confidence are separate. Withdrawal, silence and recovery choices are recorded explicitly. The moderator's correction separates the player's original claim from the opponent's claim and cites the source. Policy choices preserve the exact statement without assigning a correct ideology. Later debate questions quote the earlier commitment. Subsequent campaigns inherit structured statements; the career journal retains the transcript through presidency and losses. Revisions append entries rather than erasing the earlier words.

Scope: the protocol changes subsequent dialogue and records. It does not currently change electoral support, candidate ratings or finances; old authored debate scoring is not used by current careers. Media propagation, numerical reputation mechanics, automatic promise-to-law matching and general crisis adapters are not implemented. `PressureEvent` provides the common host contract; the broader data-driven delayed-event scheduler remains in BACKLOG.md.

Factual sources:
- National Archives, Constitution transcript, Articles I and II: https://www.archives.gov/founding-docs/constitution-transcript
- Bureau of Labor Statistics, CPI questions and answers: https://www.bls.gov/cpi/questions-and-answers.htm

Reviewed September 18, 2026. Constitutional prompts identify the specific institution/procedure. CPI prompts distinguish consumer prices, household experience, investments and positive-but-slower price changes. No transient budget percentages are invented.
