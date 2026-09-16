# Validation — Governance Edition

Validated in the supplied Linux environment with Java 17.

| Suite | Result | Coverage |
| --- | --- | --- |
| Production compile | 28 sources, no `-Xlint:all` warnings | All domain classes, text UI, launcher and existing Swing result adapter |
| EngineTests | 673,373 checks; 320 complete campaigns | All 36 campaign events, reachable normal wins/losses, campaign invariants |
| MonthlyTests | 408 checks | Inauguration, 48/96-month service, two-action limit, annual refund reset, midterms, reelection, retirement, event timing, saved replay, developer scenarios |
| GovernanceTests | 233 checks | Replies vs delivery, distinct public requests, duplicate rejection, costs, insufficient funds, cancellation on policy reversal, exact delivery timing, partial/all midterm objectives, campaign window, seat accounting, term-end archive and replay |
| GovernanceUITests | 11 checks | Menu-to-command selection for policies, replies and contests; costs/tradeoffs displayed; paging/back free; EOF saves reload correctly |

Full career snapshots are compared after replay at important states, including pending events, delayed work, public correspondence, partial contest work, resolved midterms, and retirement. Tests explicitly verify rejected commands do not mutate the snapshot or append to the journal.

Older monthly and quarterly career formats are rejected with an edition-specific compatibility message. Campaign-events-v1 imports retain the exact campaign snapshot. New save and slot filenames isolate older default saves.

`build.sh` rebuilt the release JAR; `run.sh --help` executed it successfully. The final ZIP was checked for integrity and required files; temporary saves and compiled test/build directories are excluded. The JAR contains production classes only.

Windows launchers and Swing windows were not visually executed on this Linux host. These tests verify functional behavior, not realism or balance. The policy catalog and midterm board use authored fictional game rules. No claim of national economic or numerical approval simulation is made.

To reproduce, from the extracted project folder:

```sh
java -m jdk.compiler/com.sun.tools.javac.Main -Xlint:all -d test-build ./*.java tests/*.java
java -cp test-build EngineTests
java -cp test-build MonthlyTests
java -cp test-build GovernanceTests
java -cp test-build GovernanceUITests
sh build.sh
sh run.sh --help
```
