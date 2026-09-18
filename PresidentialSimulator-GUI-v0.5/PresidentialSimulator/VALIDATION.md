# v0.5 validation

Java 17 compilation with `-Xlint:all` succeeds. The runnable desktop JAR builds successfully.

The full suite passed after the new debate protocol and GUI integration: EngineTests, DebateTests, WorldTests, InterfaceTests, StableScreenTests, DesktopTests, MapDesktopTests, PolishTests and RefinedTests. After the final source-card expansion and wording changes, focused DebateTests (131 checks), RefinedTests (347 checks) and PolishTests (44 checks) passed again.

Coverage includes live timer progression, dialog pauses, stale-click rejection, command replay in a challenge, invalid choices, truthful and false opponent claims, timeout behavior, later quotation of statements, campaign completion, existing office navigation, notification bounds, geographic/callout hover targets and width-aware layout. Tests use fictional fixtures; they are software checks, not validation of political predictions.

Screenshots were rendered headlessly at laptop sizes, including 1024×600. Office, Policy and debate screenshots were visually inspected. Native Windows/macOS rendering, display scaling, assistive technology and installer/runtime packaging still need device testing.

No new numerical political evaluation was added. The live debate protocol records dialogue and factual corrections; it does not currently modify electoral support or reputation ratings. See DEBATE-DESIGN.md and BACKLOG.md for limits.
