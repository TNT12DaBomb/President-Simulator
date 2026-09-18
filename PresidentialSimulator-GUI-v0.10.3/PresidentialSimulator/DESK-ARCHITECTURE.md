# Desk and document architecture

DeskDefinition.Item owns asset ID, normalized position, destination, theme, visibility condition, notification key, animation hook and cosmetic slot. Each DeskHub object has independent focus/hover/click state. Assets are injected via DeskHub.Assets; no gameplay changes are needed to replace artwork.

Seven campaign and six presidency entries use asymmetric placements. Secondary sections use tabs or document links. DesktopPanel is the controller-facing router; DeskMenuFrame replaces the whole main scene and owns a small return control. PaperChoice provides direct selection controls for forms; DisplayMode manages native full-screen state without replacing the frame.

The bottom dock is fixed-height. News precedes statistics; party colors are omitted from its identity line. Calendar rollover belongs to CareerEngine, not the renderer, so save replay and other clients share the same progression. Pending decisions block automatic rollover.

External asset-pack/mod discovery and selectable cosmetic themes are future features. Animation and cosmetic identifiers remain declarative extension points.

## Headquarters view

Campaign definitions supply independent normalized wall/desk positions. The hub paints the board/desk environment; object controls retain separate hit areas, states and destinations. Wall-mounted objects have static pins. The map preview shares the existing geometry through a presentation-only outline renderer.

DocumentPages owns reusable paged reading controls, StateDirectory owns search and keyboard selection, and DeskStyle owns keyboard focus treatment. DesktopPanel continues to route typed actions and snapshots; visual changes do not alter simulation calculations.
