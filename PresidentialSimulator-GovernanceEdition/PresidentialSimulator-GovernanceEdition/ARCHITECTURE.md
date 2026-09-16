# Architecture — Governance Edition

There are 28 production Java sources. Renderers submit typed commands and consume immutable views; neither CareerEngine nor Presidency performs console or filesystem I/O.

| Domain | Responsibility |
| --- | --- |
| CareerEngine | Career phases, elections, inauguration, total service, term limits, comeback, carryover, accepted command journal |
| Presidency | One term's monthly action/resource limits and ordered subsystem updates |
| Congress | Chamber seats, majorities, one-bill agreements and midterm seat application |
| MidtermCampaign | Twelve fictional contest objectives, visit eligibility, exact outcome/seat bookkeeping |
| Policy / PolicyCatalog | Bill/promise values and sixteen authored policy options with costs, timing and tradeoffs |
| PublicCorrespondence | Sixteen requests, acknowledgment/delivery state, dated immutable history |
| PresidencyEvent | Existing immutable random-event definitions and immediate/delayed effects |
| CareerCommand / OfficeCommand | Typed career, policy, reply, midterm and settings input |
| CareerView / Presidency.View | Copied collections and immutable records for any future GUI |
| GameEngine and campaign classes | Existing campaign objective board and 36 events |
| CareerSave / CampaignSave | Versioned deterministic replay and campaign import |
| TextUI / PresidentialSimulator / ElectionGUI | Menus, launch, and existing Swing election-results view |

An accepted END_MONTH settles receipts and operating costs, increments the month, applies due event effects, applies due policy implementations, opens or resolves the midterm window, closes annual correspondence, and draws an eligible event. CareerEngine then closes annual records and handles term completion. Month 48 settles existing work without drawing a new event.

Policy implementation is a separate typed queue containing issue, approach, due month and title. Replacing a policy cancels its previous pending implementation before adding the new one. Public requests are updated on funding and delivery, with acknowledgment tracked independently. Each reply is allowed once per request per term. Failed commands validate before spending money, consuming actions, or modifying records.

Midterm task commands encode contest ID and task into the existing bounded choice field: `choice = contestId * 2 + task.ordinal()`. OfficeCommand validates the range, Presidency checks phase/action/event constraints, and MidtermCampaign checks the date window and duplicates. The command journal saves the exact selected objective; display page numbers never enter domain state.

The new rule format is `presidency-governance-v1`. Older monthly or quarterly career saves cannot be faithfully replayed under changed policy costs and midterm rules, so they are explicitly rejected. A future importer must retain the old rules or perform a documented state migration. Campaign-events-v1 import remains supported.

The new systems have no political probability models or real-world policy-effect claims. They exercise explicit gameplay rules and typed extension points. Future appointments and crises can add state/effects without moving UI prompts into the engines. Do not silently change replay behavior under the current format.
