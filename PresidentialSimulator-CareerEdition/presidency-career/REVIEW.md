# Career revision review

This release moves the supplied campaign-events revision into a continuing lifecycle while preserving the text interface and the UI-independent campaign engine.

| Need | Implemented behavior |
|---|---|
| Run again after winning or losing | Win opens transition and office; loss opens a four-year comeback period before another run |
| Loss affects reputation/support | Loss stays in election history, adds descriptive public/reputation/support text, and withholds $200 from the next campaign until donor meetings |
| Winning method affects presidency | Campaign work in held states supplies one-use briefing, cabinet, and service-review transition resources |
| Presidential leadership affects reelection | Final-year public, cabinet, service, and budget work prepare next-campaign objectives or a fictional fundraising benefit; skipped work leaves clear outstanding effects |
| Traditional eight-year limit | Two elected victories end normal play, including nonconsecutive terms |
| Presidency structure first | Six quarterly action types are deliberately administrative; no scandal, war, policy, or dictator systems were invented prematurely |
| Developer options | Force outcomes, timeline jumps, resource grants, term skip, and reelection-start fixture are visible in the Game Menu and marked in saves |
| Smaller menus | Campaign, briefing, status, presidency work areas, comeback work, election review, and game menu each expose a few commands |
| Stats screen | Career Status shows feedback, reputation, support, treasury, receipts, expenses, year/term progress, election history, and carryover effects |
| Save/menu functionality | Autosave, three manual slots, load/import, save/exit, retirement, help, and exit are available through the 0 menu |

## Deliberate limits

The economy screen tracks a fictional operating treasury, receipts, routine expenses, and reconciliation. It does not claim to calculate GDP or produce meaningful approval polling. Public feedback, reputation, and support remain descriptive until a future balance pass establishes explicit numerical models.

The natural campaign still determines the electoral result. Developer-forced wins/losses intentionally omit fake EV totals. A loss does not make a future run impossible; it changes the comeback timeline and starting resources. A winning campaign does not automatically guarantee a second election: presidential administrative records only prepare visible carryover benefits, and the normal campaign still has to be played.

Two wins are the current normal cap. A future dictator path should become a new explicit career branch with its own content and consequences. It should not quietly change ordinary two-term behavior.

## Further work

1. Playtest the four-year administrative loop and tune costs, benefits, and how much carryover should matter.
2. Add approval/reputation numbers only after deciding which events and governance choices move them and how those values affect reelection.
3. Add annual presidential events after the administrative skeleton is enjoyable; reuse `CampaignEvent` where its effect model fits.
4. Add a real reelection briefing that summarizes the administration before the next campaign starts.
5. Add an explicit candidate/career identity, party alignment, legislature, appointments, and staff capacity as separate systems rather than attributes on `President`.
6. Build the GUI against `CareerView`, `CareerCommand`, and `CareerReport`. The text UI already exercises the same controller contract.
7. Add a dictator branch after ordinary two-term and loss/comeback play is stable. Define how legitimacy, institutions, opposition, and ending conditions work before implementing a bypass.

The source now contains 19 production classes and three test classes. `CareerEngine` owns lifecycle rules; `GameEngine` owns campaign/event rules; `TextUI` owns presentation; `CareerSave` owns replay persistence.
