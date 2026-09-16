# Campaign encounters — Debates Edition

DEBATES replaces the three single-question encounters below with nine questions across three scheduled debates. The remaining campaign content is retained. Every detailed answer costs $50 in abstract staff resources and scores 4; broad answers score 2 and add a temporary $15 fundraising modifier. No answer scores -2. Opponent scores depend on style. These are authored game parameters.

## Household costs | Question 1/3

Prices are straining households. What can your administration realistically do?

- Explain targeted relief and its limits
- Focus on household experiences

## Household costs | Question 2/3

How would you pay for the policy without hiding its tradeoffs?

- Name the funding source and tradeoffs
- Emphasize restraint without a funding breakdown

## Household costs | Question 3/3

A moderator challenges a statistic in your opening answer. How do you respond?

- Correct the statistic and cite its source
- Commit to publishing the source afterward

## Your record | Question 1/3

Which documented result shows that you can deliver your promises?

- Cite a documented result and its limits
- Emphasize your broader priorities

## Your record | Question 2/3

Your opponent points to an unmet commitment. How do you explain it?

- Explain the shortfall and a revised milestone
- Acknowledge the gap and focus on future goals

## Your record | Question 3/3

What would you change if your preferred policy failed to deliver?

- Set a review date and conditions for changing course
- Promise to listen and adapt

## Foreign affairs | Question 1/3

A fictional regional confrontation is escalating. What is your first step?

- Explain consultations and a diplomatic sequence
- Emphasize stability and restraint

## Foreign affairs | Question 2/3

An ally wants a commitment before consultations are complete. How do you respond?

- Condition the commitment on consultations
- Reassure the ally without a firm commitment

## Foreign affairs | Question 3/3

What limit would you place on the response, and how would you explain the risk?

- State the limits and a review trigger
- Commit to explaining the decision afterward

## Prior single-question and retained ordinary catalog

# Campaign event catalog

54 fictional scenarios: 36 inherited and 18 additions, including three mandatory debate encounters. SUPPORT values are basis points (100 = one percentage point). Old rules retain only the original 36.

## Small-donor weekend (`small_donations`)

Your campaign receives an unexpected batch of contributions.

Immediate effects: [Effect[side=PLAYER, kind=FUNDS, amount=180, task=null, duration=0]]

## Campaign bus repair (`bus_repair`)

Your team's bus needs a repair before its next trip.

Immediate effects: [Effect[side=PLAYER, kind=FUNDS, amount=-120, task=null, duration=0]]

## Opponent fundraising dinner (`rival_dinner`)

The opponent's fictional fundraising event brings in extra cash.

Immediate effects: [Effect[side=OPPONENT, kind=FUNDS, amount=180, task=null, duration=0]]

## Opponent printing invoice (`rival_invoice`)

The opponent receives an unplanned printing bill.

Immediate effects: [Effect[side=OPPONENT, kind=FUNDS, amount=-150, task=null, duration=0]]

## Travel surcharge (`travel_surcharge`)

A supplier adds a travel charge to both campaigns' bookings.

Immediate effects: [Effect[side=BOTH, kind=FUNDS, amount=-80, task=null, duration=0]]

## Venue refunds (`venue_refunds`)

Both campaigns receive refunds on deposits.

Immediate effects: [Effect[side=BOTH, kind=FUNDS, amount=90, task=null, duration=0]]

## Merchandise shipment sold (`merchandise`)

Your team sells surplus campaign merchandise.

Immediate effects: [Effect[side=PLAYER, kind=FUNDS, amount=130, task=null, duration=0]]

## Opponent equipment refund (`rival_refund`)

The opponent receives an equipment deposit back.

Immediate effects: [Effect[side=OPPONENT, kind=FUNDS, amount=130, task=null, duration=0]]

## Volunteer hosts step in (`local_hosts`)

Local volunteers organize one of your outstanding town halls.

Immediate effects: [Effect[side=PLAYER, kind=COMPLETE_TASK, amount=0, task=Town hall, duration=0]]

## Office team ready (`office_team`)

A volunteer team sets up one of your required field offices.

Immediate effects: [Effect[side=PLAYER, kind=COMPLETE_TASK, amount=0, task=Field office, duration=0]]

## Outreach team arrives (`outreach_team`)

A volunteer team completes one of your outreach objectives.

Immediate effects: [Effect[side=PLAYER, kind=COMPLETE_TASK, amount=0, task=Outreach event, duration=0]]

## Opponent hosts step in (`rival_hosts`)

Local hosts complete an outstanding town hall for the opponent.

Immediate effects: [Effect[side=OPPONENT, kind=COMPLETE_TASK, amount=0, task=Town hall, duration=0]]

## Opponent office opens (`rival_office`)

The opponent gains a completed field-office objective.

Immediate effects: [Effect[side=OPPONENT, kind=COMPLETE_TASK, amount=0, task=Field office, duration=0]]

## Opponent outreach team (`rival_outreach`)

A volunteer team completes an opponent outreach objective.

Immediate effects: [Effect[side=OPPONENT, kind=COMPLETE_TASK, amount=0, task=Outreach event, duration=0]]

## Town hall follow-up required (`townhall_followup`)

A completed town-hall objective is reopened because its follow-up session fell through.

Immediate effects: [Effect[side=PLAYER, kind=REMOVE_TASK, amount=0, task=Town hall, duration=0]]

## Field office closes (`office_closure`)

A lease problem reopens one of your completed field-office objectives.

Immediate effects: [Effect[side=PLAYER, kind=REMOVE_TASK, amount=0, task=Field office, duration=0]]

## Outreach follow-up lost (`outreach_rework`)

A coordination failure means one outreach objective must be completed again.

Immediate effects: [Effect[side=PLAYER, kind=REMOVE_TASK, amount=0, task=Outreach event, duration=0]]

## Opponent follow-up cancelled (`rival_townhall`)

The opponent must redo a town-hall objective after its follow-up fell through.

Immediate effects: [Effect[side=OPPONENT, kind=REMOVE_TASK, amount=0, task=Town hall, duration=0]]

## Opponent office closes (`rival_closure`)

A lease problem reopens an opponent field-office objective.

Immediate effects: [Effect[side=OPPONENT, kind=REMOVE_TASK, amount=0, task=Field office, duration=0]]

## Opponent outreach rework (`rival_rework`)

The opponent must redo an outreach objective after a coordination failure.

Immediate effects: [Effect[side=OPPONENT, kind=REMOVE_TASK, amount=0, task=Outreach event, duration=0]]

## Discounted venue bookings (`venue_discount`)

Your town halls cost less for the next two turns.

Immediate effects: [Effect[side=PLAYER, kind=ACTION_COST, amount=-40, task=Town hall, duration=2]]

## Opponent rental discount (`rival_rent`)

Opponent field offices cost less for the next two turns.

Immediate effects: [Effect[side=OPPONENT, kind=ACTION_COST, amount=-50, task=Field office, duration=2]]

## Outreach transport costs (`transport_delay`)

Both campaigns pay extra for outreach during the next two turns.

Immediate effects: [Effect[side=BOTH, kind=ACTION_COST, amount=40, task=Outreach event, duration=2]]

## Fundraising tools donated (`donation_tools`)

Your fundraising actions collect additional cash for two turns.

Immediate effects: [Effect[side=PLAYER, kind=FUNDRAISING, amount=100, task=null, duration=2]]

## Opponent payment delays (`rival_payment_delay`)

Opponent fundraising actions collect less cash for two turns.

Immediate effects: [Effect[side=OPPONENT, kind=FUNDRAISING, amount=-80, task=null, duration=2]]

## Field-office supply shortage (`office_supplies`)

Your field-office actions cost extra for two turns.

Immediate effects: [Effect[side=PLAYER, kind=ACTION_COST, amount=50, task=Field office, duration=2]]

## Opponent venue costs rise (`rival_venue_cost`)

Opponent town halls cost extra for two turns.

Immediate effects: [Effect[side=OPPONENT, kind=ACTION_COST, amount=40, task=Town hall, duration=2]]

## Fundraising platform upgrade (`shared_donation_tools`)

Both campaigns collect additional cash when fundraising for two turns.

Immediate effects: [Effect[side=BOTH, kind=FUNDRAISING, amount=60, task=null, duration=2]]

## A venue opens up (`venue_offer`)

A host offers to complete a town-hall objective if you cover the booking.

Window: 2 turn(s); live seconds: 0.

- **Pay $100; complete this town-hall objective**: [Effect[side=PLAYER, kind=FUNDS, amount=-100, task=null, duration=0], Effect[side=PLAYER, kind=COMPLETE_TASK, amount=0, task=Town hall, duration=0]]
- **Decline; keep current resources**: []

## Ready-to-use office (`office_offer`)

A local team can open an office immediately if you fund its setup.

Window: 2 turn(s); live seconds: 0.

- **Pay $150; complete this field-office objective**: [Effect[side=PLAYER, kind=FUNDS, amount=-150, task=null, duration=0], Effect[side=PLAYER, kind=COMPLETE_TASK, amount=0, task=Field office, duration=0]]
- **Decline; keep current resources**: []

## Weekend outreach opening (`outreach_offer`)

Volunteers can complete an outreach objective with supplies you provide.

Window: 2 turn(s); live seconds: 0.

- **Pay $100; complete this outreach objective**: [Effect[side=PLAYER, kind=FUNDS, amount=-100, task=null, duration=0], Effect[side=PLAYER, kind=COMPLETE_TASK, amount=0, task=Outreach event, duration=0]]
- **Decline; keep current resources**: []

## Volunteer training offer (`training_offer`)

Training now can lower your outreach costs over the next two turns.

Window: 2 turn(s); live seconds: 0.

- **Pay $75; outreach costs $50 less for two turns**: [Effect[side=PLAYER, kind=FUNDS, amount=-75, task=null, duration=0], Effect[side=PLAYER, kind=ACTION_COST, amount=-50, task=Outreach event, duration=2]]
- **Decline; keep current resources**: []

## Fundraising workshop (`fundraising_offer`)

A workshop can improve the proceeds of your next two turns' fundraising actions.

Window: 2 turn(s); live seconds: 0.

- **Pay $75; fundraising earns $100 more for two turns**: [Effect[side=PLAYER, kind=FUNDS, amount=-75, task=null, duration=0], Effect[side=PLAYER, kind=FUNDRAISING, amount=100, task=null, duration=2]]
- **Decline; keep current resources**: []

## Shared booking refund (`shared_booking`)

A joint cancellation releases deposits only if both campaigns accept the refund.

Window: 2 turn(s); live seconds: 0.

- **Accept: both campaigns receive $125**: [Effect[side=PLAYER, kind=FUNDS, amount=125, task=null, duration=0], Effect[side=OPPONENT, kind=FUNDS, amount=125, task=null, duration=0]]
- **Decline; keep current resources**: []

## A quiet news cycle (`quiet_week`)

Both teams continue their planned work. No additional effects.

Immediate effects: []

## Routine campaign week (`routine_week`)

Schedules hold and no unexpected costs arise. No additional effects.

Immediate effects: []

## Debate: household costs (`debate_economy`)

The moderator asks how you would address rising household costs without promising prices you cannot control.

Window: 1 turn(s); live seconds: 60.

- **Use a researched cost-of-living answer**: [Effect[side=PLAYER, kind=FUNDS, amount=-100, task=null, duration=0], Effect[side=PLAYER, kind=SUPPORT, amount=65, task=null, duration=2], Effect[side=OPPONENT, kind=SUPPORT, amount=25, task=null, duration=2]]
- **Focus on a family's experience**: [Effect[side=PLAYER, kind=SUPPORT, amount=25, task=null, duration=2], Effect[side=PLAYER, kind=FUNDRAISING, amount=35, task=null, duration=2], Effect[side=OPPONENT, kind=SUPPORT, amount=25, task=null, duration=2]]

## Debate: your record (`debate_record`)

Your opponent challenges whether your promises are backed by results. The moderator gives you the floor.

Window: 1 turn(s); live seconds: 60.

- **Cite documented results and limits**: [Effect[side=PLAYER, kind=FUNDS, amount=-75, task=null, duration=0], Effect[side=PLAYER, kind=SUPPORT, amount=60, task=null, duration=2], Effect[side=OPPONENT, kind=SUPPORT, amount=20, task=null, duration=2]]
- **Acknowledge gaps and explain priorities**: [Effect[side=PLAYER, kind=SUPPORT, amount=25, task=null, duration=2], Effect[side=OPPONENT, kind=SUPPORT, amount=15, task=null, duration=2]]

## Debate: foreign affairs (`debate_security`)

The moderator presents a confrontation between two fictional states and asks how you would avoid escalation.

Window: 1 turn(s); live seconds: 60.

- **Explain a prepared diplomatic sequence**: [Effect[side=PLAYER, kind=FUNDS, amount=-100, task=null, duration=0], Effect[side=PLAYER, kind=SUPPORT, amount=60, task=null, duration=2], Effect[side=OPPONENT, kind=SUPPORT, amount=20, task=null, duration=2]]
- **Give a short statement of principles**: [Effect[side=PLAYER, kind=SUPPORT, amount=20, task=null, duration=2], Effect[side=PLAYER, kind=FUNDRAISING, amount=30, task=null, duration=2], Effect[side=OPPONENT, kind=SUPPORT, amount=20, task=null, duration=2]]

## Live interview follow-up (`live_interview`)

A presenter asks for a concrete answer after your campaign's general statement. The broadcast is live.

Window: 1 turn(s); live seconds: 45.

- **Give the prepared explanation**: [Effect[side=PLAYER, kind=FUNDS, amount=-50, task=null, duration=0], Effect[side=PLAYER, kind=SUPPORT, amount=40, task=null, duration=2]]
- **State what you do not yet know**: [Effect[side=PLAYER, kind=SUPPORT, amount=15, task=null, duration=2]]

## Debate source request (`fact_check`)

A newsroom requests the documents behind your debate answer before publishing its follow-up.

Window: 2 turn(s); live seconds: 0.

- **Release sources with explanatory notes**: [Effect[side=PLAYER, kind=FUNDS, amount=-50, task=null, duration=0], Effect[side=PLAYER, kind=SUPPORT, amount=35, task=null, duration=2]]
- **Send the existing campaign summary**: [Effect[side=PLAYER, kind=SUPPORT, amount=-15, task=null, duration=2]]

## Voter-information hotline (`ballot_help`)

County volunteers report confusion about polling locations. They want verified information rather than campaign speculation.

Window: 2 turn(s); live seconds: 0.

- **Staff a hotline using official information**: [Effect[side=PLAYER, kind=FUNDS, amount=-90, task=null, duration=0], Effect[side=PLAYER, kind=SUPPORT, amount=30, task=null, duration=2]]
- **Share links to election offices**: [Effect[side=PLAYER, kind=SUPPORT, amount=10, task=null, duration=2]]

## Volunteer shifts unfilled (`volunteer_retention`)

Local organizers warn that long shifts are exhausting volunteers before the final push.

Window: 2 turn(s); live seconds: 0.

- **Pay for transport and shorter shifts**: [Effect[side=PLAYER, kind=FUNDS, amount=-80, task=null, duration=0], Effect[side=PLAYER, kind=SUPPORT, amount=35, task=null, duration=2]]
- **Reduce the schedule to available staff**: [Effect[side=PLAYER, kind=FUNDRAISING, amount=-20, task=null, duration=2]]

## Venue accessibility complaint (`accessible_venue`)

A town-hall venue's accessible entrance is unavailable. Organizers ask whether to relocate.

Window: 2 turn(s); live seconds: 0.

- **Move to an accessible venue**: [Effect[side=PLAYER, kind=FUNDS, amount=-100, task=null, duration=0], Effect[side=PLAYER, kind=SUPPORT, amount=30, task=null, duration=2]]
- **Switch to a remote question session**: [Effect[side=PLAYER, kind=SUPPORT, amount=10, task=null, duration=2], Effect[side=PLAYER, kind=FUNDRAISING, amount=-15, task=null, duration=2]]

## Advertising reservation (`ad_reservation`)

A broadcaster offers a short booking window. Buying now means less cash for field operations.

Window: 2 turn(s); live seconds: 0.

- **Buy the available airtime**: [Effect[side=PLAYER, kind=FUNDS, amount=-140, task=null, duration=0], Effect[side=PLAYER, kind=SUPPORT, amount=45, task=null, duration=2]]
- **Keep the money for local organizing**: []

## Donor asks for privileged access (`donor_conditions`)

A donor asks for a private policy briefing unavailable to other supporters. Your team can set boundaries.

Window: 2 turn(s); live seconds: 0.

- **Offer the same public briefing to everyone**: [Effect[side=PLAYER, kind=SUPPORT, amount=20, task=null, duration=2], Effect[side=PLAYER, kind=FUNDRAISING, amount=-20, task=null, duration=2]]
- **Decline the meeting and move on**: [Effect[side=PLAYER, kind=SUPPORT, amount=10, task=null, duration=2]]

## Local officials offer a meeting (`endorsement_meeting`)

A group of fictional local officials wants a substantive discussion before deciding whether to support your campaign.

Window: 2 turn(s); live seconds: 0.

- **Send a prepared policy team**: [Effect[side=PLAYER, kind=FUNDS, amount=-80, task=null, duration=0], Effect[side=PLAYER, kind=SUPPORT, amount=40, task=null, duration=2]]
- **Keep the existing campaign schedule**: []

## Campaign handout contains an error (`press_correction`)

Staff find an incorrect statistic in a distributed handout. Reporters have asked for clarification.

Window: 2 turn(s); live seconds: 0.

- **Correct the handout and explain the error**: [Effect[side=PLAYER, kind=FUNDS, amount=-40, task=null, duration=0], Effect[side=PLAYER, kind=SUPPORT, amount=20, task=null, duration=2]]
- **Remove it pending a full review**: [Effect[side=PLAYER, kind=SUPPORT, amount=-10, task=null, duration=2]]

## Airport disruption changes the route (`route_change`)

Cancelled flights threaten two appearances. Staff present a costly reroute and a remote alternative.

Window: 2 turn(s); live seconds: 0.

- **Reroute the team**: [Effect[side=PLAYER, kind=FUNDS, amount=-110, task=null, duration=0], Effect[side=PLAYER, kind=SUPPORT, amount=25, task=null, duration=2]]
- **Hold remote appearances**: [Effect[side=PLAYER, kind=SUPPORT, amount=5, task=null, duration=2], Effect[side=PLAYER, kind=FUNDRAISING, amount=-10, task=null, duration=2]]

## Regional radio invitation (`rural_radio`)

A regional program offers a long-form interview focused on local services and agricultural communities.

Window: 2 turn(s); live seconds: 0.

- **Prepare with local organizers**: [Effect[side=PLAYER, kind=FUNDS, amount=-60, task=null, duration=0], Effect[side=PLAYER, kind=SUPPORT, amount=30, task=null, duration=2]]
- **Send a written response**: [Effect[side=PLAYER, kind=SUPPORT, amount=5, task=null, duration=2]]

## Student civic forum (`student_forum`)

A nonpartisan student forum offers equal time to both campaigns. Preparation competes with fundraising.

Window: 2 turn(s); live seconds: 0.

- **Attend with a prepared Q&A**: [Effect[side=PLAYER, kind=FUNDS, amount=-50, task=null, duration=0], Effect[side=PLAYER, kind=SUPPORT, amount=25, task=null, duration=2], Effect[side=OPPONENT, kind=SUPPORT, amount=15, task=null, duration=2]]
- **Send written answers**: [Effect[side=PLAYER, kind=SUPPORT, amount=10, task=null, duration=2], Effect[side=OPPONENT, kind=SUPPORT, amount=15, task=null, duration=2]]

## Suspicious campaign login (`staff_security`)

Staff detect a suspicious account login. A security review will interrupt normal operations.

Window: 2 turn(s); live seconds: 0.

- **Reset access and commission a review**: [Effect[side=PLAYER, kind=FUNDS, amount=-85, task=null, duration=0], Effect[side=PLAYER, kind=FUNDRAISING, amount=-10, task=null, duration=2]]
- **Restrict affected accounts for now**: [Effect[side=PLAYER, kind=FUNDRAISING, amount=-35, task=null, duration=2]]

## Opponent releases a comparison ad (`opponent_ad`)

The opposing campaign buys airtime contrasting your records. Your team can answer substantively or save resources.

Window: 2 turn(s); live seconds: 0.

- **Publish a sourced response**: [Effect[side=PLAYER, kind=FUNDS, amount=-70, task=null, duration=0], Effect[side=PLAYER, kind=SUPPORT, amount=25, task=null, duration=2], Effect[side=OPPONENT, kind=SUPPORT, amount=20, task=null, duration=2]]
- **Stay on the existing message**: [Effect[side=OPPONENT, kind=SUPPORT, amount=35, task=null, duration=2]]

## Closing-address opportunity (`closing_address`)

A civic broadcaster offers both candidates time for a closing statement. There is no guarantee that a speech changes minds.

Window: 2 turn(s); live seconds: 0.

- **Prepare a concise record-and-priorities address**: [Effect[side=PLAYER, kind=FUNDS, amount=-60, task=null, duration=0], Effect[side=PLAYER, kind=SUPPORT, amount=30, task=null, duration=2], Effect[side=OPPONENT, kind=SUPPORT, amount=20, task=null, duration=2]]
- **Use the standard campaign statement**: [Effect[side=PLAYER, kind=SUPPORT, amount=10, task=null, duration=2], Effect[side=OPPONENT, kind=SUPPORT, amount=20, task=null, duration=2]]

