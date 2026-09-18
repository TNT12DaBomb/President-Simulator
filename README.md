# Presidential Simulator v0.10.3 — Physical workspaces

Extract the ZIP. Java 17+ is required. Windows: double-click `run-windows.bat`. macOS/Linux: `sh run.sh`.

## Navigation

The campaign headquarters has a wall board above a working desk, with seven objects: phone, map, fundraising ledger, planner, policy binder, newspaper and reports. The presidential desk has six: secure phone, legislation folio, cabinet book, planner, newspaper and reports. Objects use individual normalized positions, sizes, notification keys and renderer hooks; they are no longer arranged as a uniform grid. No laptop or sidebar remains.

Opening a document replaces the desk. The aligned bottom **Return** control returns to it. The small bottom **Menu** opens saves, Settings, How to play and exit; Escape opens/resumes the menu. Headlines appear above neutral stat tiles. Team-color explanations have been removed from the dock; color distinctions remain on the map.

Campaign debate settings live on a policy-binder tab. Reports contain statistics and links to the career archive and activity records. The cabinet book contains appointments, public requests and midterm sections. The planner contains presidential actions and upcoming deliveries. Legislation and promises share the folio.

Common form choices now use direct selection buttons: policy, administration, debate setup, campaign color and save actions. The state picker is now a searchable paper index. Developer actions also use direct selections. Long forms and records can still scroll.

## Monthly progression and saves

After the second presidential action, the month advances automatically. A pending decision must be resolved before that rollover. Reading documents and changing settings do not spend actions. The legacy terminal still permits explicitly ending a month early.

**This version requires a new career.** Saves use `presidency-auto-month-v6`; older manual-month command histories are intentionally rejected because replaying them with automatic progression could change their timelines. Save files live in `~/PresidentialSimulator/saves`. Activity reports remain until dismissed.

## Display

Settings includes a Fullscreen checkbox when the native display supports it. Switching back restores the previous window bounds/state. It preserves the same frame and listeners. Display mode is session-only. Native fullscreen and high-DPI behavior require testing on your machine; they cannot be verified by headless rendering.

Developer password: **devtools**. Seed overrides remain developer-only; normal new careers use random seeds.

## Development

Build: `sh build.sh`. UI checks: `sh test-hubs.sh`. Calendar/save checks: `sh test-month-flow.sh`.

All 13 main desk destinations, pause/resume, return navigation and three window sizes pass headless checks. The calendar test checks action counts, month rollover and save replay only. Existing gameplay calculations remain unchanged; the behavior change is turn scheduling.

Future work: native display/accessibility testing, optional artwork packs, selectable cosmetic themes, searchable state directory, and migration of older tests that assume separate manual month endings.

## v0.10.3 interface refinements

Wall pins, a corkboard, varied paper sizes and a desk ledge distinguish headquarters from the executive desk. The map preview uses the existing state outlines; the interactive map has paper borders and a fold line. Geometry and interaction logic are unchanged.

The bottom control is aligned with stats and changes between Menu, Resume and Return. Default button focus painting is replaced with a keyboard focus accent. Pre-game menus hide career-only links and the dock hides news/stat previews.

Activity displays one retained report at a time. History has paged career/campaign records. Statistics uses phase-specific tabs. Newspaper pages include full current briefings, selected recent dispatches and scheduling context from existing records, with background flavor kept separate. It does not invent additional reporting when the game has no supporting detail.

Automatic monthly progression and the v6 save format are unchanged from v0.10.0.

## v0.10.3 consistency pass

Live and practice debates share warm paper and green controls. Changed stat tiles fade through muted brass back to their original dock color. The remaining developer and legacy view dropdowns now use direct selections. The Menu control has additional width and a larger gap from the stats.

The start screen has no default action button, redundant footer title or settings-location instruction. Wall pins are painted within their paper assets; wall papers use shallower shadows, while folders remain entirely below the desk ledge. No gameplay or save-format changes.

## v0.10.3 pending decisions and presentation

Pending decisions no longer disable ordinary desktop actions. Live debates use a nonmodal window; returning to the workspace retains the normal deadline. Spending a campaign turn can reach a decision's in-game deadline, so multitasking is not an indefinite pause. The phone shows a decision badge and caption until the pending response resolves. Existing action budgets and phase boundaries still apply.

The map uses the same paper palette as its surrounding workspace, includes a current-news clipping with a Newspaper link, and retains a searchable state index (no dropdown). Live and practice debates share a leather border and paper background. Stat highlights now expire correctly instead of replaying on later refreshes.

Save format remains v6.
