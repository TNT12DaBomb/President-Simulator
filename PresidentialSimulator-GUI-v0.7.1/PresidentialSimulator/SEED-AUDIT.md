# Seed and consequences — fixed in v0.7.1

Opening support now hashes the selected seed together with each state name. Same seed/settings/history reproduce a campaign; different seeds vary the fictional starting map. Election-day uncertainty remains a separate seeded component. Changing seeds can sometimes yield the same aggregate EV total even when state shares differ.

Public credibility starts at 50 and is clamped to 0–100. Politics answers: correct +2; wrong and confident -4; wrong and hedged -2; withdrawing -.5; silence -2. Recovery modifies that result: doubling down on an error -2; accepting a true correction +1; accepting a false one -1; attacking a true correction -2; disputing a false one +1; requesting verification +.5. Fun mode halves these knowledge-related effects. Leaving a live appearance before its closing costs 3. Pausing and practice do not cost credibility.

Each credibility point away from 50 shifts simulated support by .16 percentage points (maximum +/-8). The same input is used for projections and actual ballots, alongside local campaign work, governing record, events and election-day uncertainty. The effect persists through the statement journal, save/load and subsequent campaigns. Performance is not an automatic win or loss.

After an election, newly earned campaign credibility also changes reputation and trust by half its change and independent approval by .15 of its change. Inherited credibility is excluded from this post-election increment, avoiding repeated awards for old debates. This is a simple authored balance model requiring later playtesting; policy opinions are not scored as factual errors.
