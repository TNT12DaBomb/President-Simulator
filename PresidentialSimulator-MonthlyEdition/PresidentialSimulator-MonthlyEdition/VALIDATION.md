# Validation — Monthly Edition

Validated in the supplied Linux environment with Java 17.

- All 25 production sources compile with `-Xlint:all` and no warnings.
- Existing `EngineTests`: **673,373 checks**, **320 complete campaigns**, all **36 campaign events** exercised; normal wins and losses remain reachable.
- New `MonthlyTests`: **388 checks** covering inauguration, exactly two actions/month, rejected-command immutability, annual refund limits and reset, month-24 midterms and both authored branches, 48-month terms, 96-month retirement, no third run, bill negotiation/signature, pledge contradiction and carryover, event prerequisites, pending-event time blocking, paid delayed effects, term-end settlement, loss-related withholding, developer scenarios, and exact save replay.
- Save replay compared full immutable career snapshots and accepted command journals. Tested pending and delayed events, term review, retirement, and developer scenarios.
- Supplied campaign save format imports with an identical campaign snapshot. Old quarterly career format is rejected with an explicit compatibility message.
- Scripted text UI reaches inauguration and the monthly desk; end-of-input writes a loadable save. The included Linux launcher reads the rebuilt JAR and prints help successfully.
- Release archive checked for all production sources, tests, launchers, documentation, and runnable JAR. Compiled test classes and temporary saves are excluded from the download.

Limits: Windows launcher and Swing windows were not executed visually in this Linux environment. This is functional regression coverage, not an assessment of game balance or realism. Congressional seat branches and policy effects are authored placeholders. No numerical approval or national economic simulation is asserted by these tests.

Reproduce from the extracted project folder:

```sh
java -m jdk.compiler/com.sun.tools.javac.Main -Xlint:all -d test-build ./*.java tests/*.java
java -cp test-build EngineTests
java -cp test-build MonthlyTests
sh build.sh
sh run.sh --help
```
