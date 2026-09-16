# Event catalog

These 36 events are fictional gameplay content. This reference is generated from `EventCatalog.all()` so its IDs and mechanical effects match the packaged code.

| ID | Event | Category | Immediate effect or response options |
|---|---|---|---|
| `small_donations` | Small-donor weekend | funding | player cash +$180 |
| `bus_repair` | Campaign bus repair | funding | player cash -$120 |
| `rival_dinner` | Opponent fundraising dinner | funding | opponent cash +$180 |
| `rival_invoice` | Opponent printing invoice | funding | opponent cash -$150 |
| `travel_surcharge` | Travel surcharge | funding | both cash -$80 |
| `venue_refunds` | Venue refunds | funding | both cash +$90 |
| `merchandise` | Merchandise shipment sold | funding | player cash +$130 |
| `rival_refund` | Opponent equipment refund | funding | opponent cash +$130 |
| `local_hosts` | Volunteer hosts step in | volunteers | player completes Town hall in one eligible state |
| `office_team` | Office team ready | volunteers | player completes Field office in one eligible state |
| `outreach_team` | Outreach team arrives | volunteers | player completes Outreach event in one eligible state |
| `rival_hosts` | Opponent hosts step in | volunteers | opponent completes Town hall in one eligible state |
| `rival_office` | Opponent office opens | volunteers | opponent completes Field office in one eligible state |
| `rival_outreach` | Opponent outreach team | volunteers | opponent completes Outreach event in one eligible state |
| `townhall_followup` | Town hall follow-up required | disruption | player reopens Town hall in one eligible state |
| `office_closure` | Field office closes | disruption | player reopens Field office in one eligible state |
| `outreach_rework` | Outreach follow-up lost | disruption | player reopens Outreach event in one eligible state |
| `rival_townhall` | Opponent follow-up cancelled | disruption | opponent reopens Town hall in one eligible state |
| `rival_closure` | Opponent office closes | disruption | opponent reopens Field office in one eligible state |
| `rival_rework` | Opponent outreach rework | disruption | opponent reopens Outreach event in one eligible state |
| `venue_discount` | Discounted venue bookings | operations | player Town hall cost -$40 for 2 upcoming turns |
| `rival_rent` | Opponent rental discount | operations | opponent Field office cost -$50 for 2 upcoming turns |
| `transport_delay` | Outreach transport costs | operations | both Outreach event cost +$40 for 2 upcoming turns |
| `donation_tools` | Fundraising tools donated | operations | player fundraising proceeds +$100 for 2 upcoming turns |
| `rival_payment_delay` | Opponent payment delays | operations | opponent fundraising proceeds -$80 for 2 upcoming turns |
| `office_supplies` | Field-office supply shortage | operations | player Field office cost +$50 for 2 upcoming turns |
| `rival_venue_cost` | Opponent venue costs rise | operations | opponent Town hall cost +$40 for 2 upcoming turns |
| `shared_donation_tools` | Fundraising platform upgrade | operations | both fundraising proceeds +$60 for 2 upcoming turns |
| `venue_offer` | A venue opens up | opportunity | Pay $100; complete this town-hall objective: player cash -$100; player completes Town hall in one eligible state / Decline; keep current resources: No additional effect |
| `office_offer` | Ready-to-use office | opportunity | Pay $150; complete this field-office objective: player cash -$150; player completes Field office in one eligible state / Decline; keep current resources: No additional effect |
| `outreach_offer` | Weekend outreach opening | opportunity | Pay $100; complete this outreach objective: player cash -$100; player completes Outreach event in one eligible state / Decline; keep current resources: No additional effect |
| `training_offer` | Volunteer training offer | opportunity | Pay $75; outreach costs $50 less for two turns: player cash -$75; player Outreach event cost -$50 for 2 upcoming turns / Decline; keep current resources: No additional effect |
| `fundraising_offer` | Fundraising workshop | opportunity | Pay $75; fundraising earns $100 more for two turns: player cash -$75; player fundraising proceeds +$100 for 2 upcoming turns / Decline; keep current resources: No additional effect |
| `shared_booking` | Shared booking refund | opportunity | Accept: both campaigns receive $125: player cash +$125; opponent cash +$125 / Decline; keep current resources: No additional effect |
| `quiet_week` | A quiet news cycle | quiet | No additional effect |
| `routine_week` | Routine campaign week | quiet | No additional effect |

Mandatory cash losses are capped at available cash. Optional paid responses must be affordable in full. Completed/reopened objectives are restricted to valid target states. Each event can appear at most once in a campaign. Temporary modifiers stack and expire after the stated upcoming game turns; action prices have a $25 floor and fundraising a $50 floor.

## Event text

**Small-donor weekend** — Your campaign receives an unexpected batch of contributions.

**Campaign bus repair** — Your team's bus needs a repair before its next trip.

**Opponent fundraising dinner** — The opponent's fictional fundraising event brings in extra cash.

**Opponent printing invoice** — The opponent receives an unplanned printing bill.

**Travel surcharge** — A supplier adds a travel charge to both campaigns' bookings.

**Venue refunds** — Both campaigns receive refunds on deposits.

**Merchandise shipment sold** — Your team sells surplus campaign merchandise.

**Opponent equipment refund** — The opponent receives an equipment deposit back.

**Volunteer hosts step in** — Local volunteers organize one of your outstanding town halls.

**Office team ready** — A volunteer team sets up one of your required field offices.

**Outreach team arrives** — A volunteer team completes one of your outreach objectives.

**Opponent hosts step in** — Local hosts complete an outstanding town hall for the opponent.

**Opponent office opens** — The opponent gains a completed field-office objective.

**Opponent outreach team** — A volunteer team completes an opponent outreach objective.

**Town hall follow-up required** — A completed town-hall objective is reopened because its follow-up session fell through.

**Field office closes** — A lease problem reopens one of your completed field-office objectives.

**Outreach follow-up lost** — A coordination failure means one outreach objective must be completed again.

**Opponent follow-up cancelled** — The opponent must redo a town-hall objective after its follow-up fell through.

**Opponent office closes** — A lease problem reopens an opponent field-office objective.

**Opponent outreach rework** — The opponent must redo an outreach objective after a coordination failure.

**Discounted venue bookings** — Your town halls cost less for the next two turns.

**Opponent rental discount** — Opponent field offices cost less for the next two turns.

**Outreach transport costs** — Both campaigns pay extra for outreach during the next two turns.

**Fundraising tools donated** — Your fundraising actions collect additional cash for two turns.

**Opponent payment delays** — Opponent fundraising actions collect less cash for two turns.

**Field-office supply shortage** — Your field-office actions cost extra for two turns.

**Opponent venue costs rise** — Opponent town halls cost extra for two turns.

**Fundraising platform upgrade** — Both campaigns collect additional cash when fundraising for two turns.

**A venue opens up** — A host offers to complete a town-hall objective if you cover the booking.

**Ready-to-use office** — A local team can open an office immediately if you fund its setup.

**Weekend outreach opening** — Volunteers can complete an outreach objective with supplies you provide.

**Volunteer training offer** — Training now can lower your outreach costs over the next two turns.

**Fundraising workshop** — A workshop can improve the proceeds of your next two turns' fundraising actions.

**Shared booking refund** — A joint cancellation releases deposits only if both campaigns accept the refund.

**A quiet news cycle** — Both teams continue their planned work. No additional effects.

**Routine campaign week** — Schedules hold and no unexpected costs arise. No additional effects.

