# Plan: Run jvmTest and jsTest in 2 minutes or less

> **Status:** to do.
>
> **Summary:** `./gradlew allTargetTests` takes about 12 minutes of test execution on a warm build: 5m47s for
> jvmTest and 6m57s for jsTest. Almost all of it goes to the end-to-end tests, and most of that to one stale wait: since
> the main quest starts at the village entrance (commit `698efb24`), a new game shows no quest announcement. But
> `startNewGame` still waits for one, so every end-to-end test that plays a new game sits out the full 10s of
> `waitUntil`, which then carries on silently. E.g.: each of the 3 `MoveKnightEndToEndTest` tests takes 11.3s, and each
> of the 4 `PauseEndToEndTest` tests takes 10.0s. Instead, `startNewGame` waits for what a new game does show,
> `waitUntil` fails when its time runs out, and the JVM test classes run in 5 forks. If `allTargetTests` is still over 2
> minutes after that, jvmTest and jsTest run at the same time, and then the battle tests start from a save instead of
> walking to the bat.

## What was measured

The last local runs on the 10-core Mac, read from `build/test-results`, with compilation not counted:

| Task      | Test execution | End-to-end tests | Rest of the tests       |
|-----------|----------------|------------------|-------------------------|
| jvmTest   | 5m47s (347s)   | ~329s            | ~18s, over ~560 classes |
| jsTest    | 6m57s (417s)   | ~348s            | ~69s                    |

- On the JVM, the end-to-end tests that call `startNewGame` take 10.0–11.9s each, and the ones that don't (e.g.
  `WorldBuilderEndToEndTest`, `StorybookEndToEndTest`, `LanguageEndToEndTest`) take under 0.2s. The 10s is
  `WAIT_TIMEOUT` in `EndToEndExtension.kt`.
- The JS reports have no per-test times and only whole-second timestamps, so the JS end-to-end time is read from the
  gaps between suites. It is very likely the same stale wait.
- jvmTest runs in a single fork, one class after another.

### After M2

`./gradlew jvmTest --rerun jsBrowserTest --rerun` takes 2m14s on the same Mac, with every test green:

| Task      | Test execution | Before M1 | End-to-end tests |
|-----------|----------------|-----------|------------------|
| jvmTest   | 29s, 5 forks   | 347s      | ~19s of work     |
| jsTest    | 124s           | 417s      | ~89s             |

- Gradle already runs the two tasks at the same time: jvmTest ran from 18:29:14 to 18:29:43, inside the jsTest run from
  18:29:22 to 18:31:26. jsTest alone keeps the total over 2 minutes.
- The 5 slowest JVM tests left: `FinishPlayerTurnEndToEndTest` `should disable finish turn while the cpu turn is
  displayed and enable it again when the human turn is announced` (4.0s), `PackageStructureArchitectureTest` `should
  reside in a layer package of a subdomain when is a production file` (3.8s), `CastAbilityEndToEndTest` `should explore
  again when the human player defeats the bat, the only cpu battle unit in the battle, with the sword` (3.8s),
  `LocalizationArchitectureTest` `should only be imported by presentation packages when it is the localization
  subdomain` (3.6s) and `PresentationResourcesArchitectureTest` `should not read resources when is a presentation file
  other than the resources loader` (3.4s).
- The 5 slowest JS suites left, read from the gaps between suites: `QuestEndToEndTest` (25s), `ExploreEndToEndTest`
  (15s), `MoveKnightEndToEndTest` (11s), `BattlefieldViewTest` (9s) and `StorybookEndToEndTest` (7s).

## Decisions

| Topic          | Decision                                                                                                                                                                                                                                                                                         |
|----------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Budget         | `./gradlew allTargetTests`, jvmTest and jsTest together, finishes its tests in 2 minutes or less on the local Mac with a warm build. Chosen over each task on its own and over counting compilation. CI on `ubuntu-latest` (4 vCPUs) gets faster too, but is not held to it.                         |
| Stale wait     | `startNewGame` waits for the virtual pad with the knight on its start tile, `KNIGHT_START_ROW` and `KNIGHT_START_COLUMN`, instead of the quest announcement a new game no longer shows.                                                                                                             |
| Wait timeout   | `waitUntil` throws once `WAIT_TIMEOUT` (10s of real time) runs out, so a stale wait breaks the build instead of costing 10s. Chosen over keeping it silent and over logging a warning. The stack trace points at the test line holding the condition, chosen over a description argument at all 144 call sites. |
| Absence        | A test that checks something does not appear first waits for something that comes after it, then asserts the absence once. E.g.: a new game shows no announcement once the virtual pad shows the knight on its start tile. Chosen over a fixed `waitFor` and over a second helper that waits out the timeout without failing. |
| JVM forks      | `maxParallelForks = availableProcessors / 2` for the JVM `Test` tasks: 5 on the Mac, 2 on CI, leaving cores for jsTest and Gradle. Chosen over every core and over 1 fork.                                                                                                                          |
| If still over  | Measure after M2. Only if `allTargetTests` is still over 2 minutes, M3 makes jvmTest and jsTest run at the same time. Only if it is still over after M3, M4 starts the battle tests from a save. If it is still over after M4, stop and report the measured time and the slowest tests left.         |
| Battle save    | A commonTest helper builds the save in code: it sets up a new game through the subdomains' Apis, as the acceptance tests do, with the party one tile west of the bat, and writes it into the `InmemoryStorage` the game starts with. It follows any snapshot change by itself and runs on JVM and JS. Chosen over saving through the pause screen once per class, which couples the tests of a class, and over a committed JSON save, which [every save version bump](../adr/0011-no-save-migrations.md) would replace and JS could not read. |
| Targets        | The end-to-end tests stay in commonTest and run on JVM and JS, as the game is deployed to the browser. Chosen over moving them back to jvmTest and over a single smoke test on JS.                                                                                                                   |
| Architecture   | Test code only, in `com.mkz.rpg.shared.adapters.e2e` under `src/commonTest`, plus `build.gradle.kts`. No game code changes.                                                                                                                                                                        |
| Tests          | A new `EndToEndExtensionTest` covers `waitUntil` with a 100ms timeout. The end-to-end tests keep their names and what they check.                                                                                                                                                                 |
| Out of scope   | Trimming the end-to-end tests to critical journeys, a faster frame loop in `sharedViewsTest` (yielding on the JVM, fewer browser pauses on JS), a written budget or per-test time limit, and the compile time. They stay options if M4 is not enough.                                              |
| Delivery       | 4 milestones, each green on CI; M3 and M4 are only built if still over 2 minutes. See [Milestones](#milestones).                                                                                                                                                                                  |

## Milestones

Each milestone is a few commits straight on `main`, green on CI. Each is measured with
`./gradlew jvmTest --rerun jsTest --rerun` once the tests are compiled.

| #  | Milestone                   | Done when                                                                                                                    | Status | Done in |
|----|-----------------------------|------------------------------------------------------------------------------------------------------------------------------|--------|---------|
| M1 | No stale waits              | `waitUntil` fails on timeout, no end-to-end test hits it, and no end-to-end test that plays a new game takes 10s any more.     | ✅      | f573ed34 |
| M2 | JVM forks and measurement   | jvmTest runs in `availableProcessors / 2` forks, and the time of `allTargetTests` is written in this plan.                     | ✅      | 36cca6ec |
| M3 | jvmTest ∥ jsTest            | Only if M2 is over 2 minutes: `allTargetTests` runs jvmTest and jsTest at the same time, and its new time is written here.     | ⬜      |         |
| M4 | Battles from a save         | Only if M3 is over 2 minutes: the battle tests load a save one tile west of the bat, and the new time is written here.         | ⬜      |         |

### M1 commits

1. `waitUntil` takes a `timeout` (default `WAIT_TIMEOUT`) and throws `IllegalStateException("waitUntil timed out after
   <timeout>")` once it runs out, with `EndToEndExtensionTest`.
2. `startNewGame` waits for the virtual pad with the knight on its start tile instead of the quest announcement. Every
   other wait that now fails is fixed to wait for what the game shows today, and every absence check waits for a later
   sign first. E.g.: `should play a new game with no announcement and no tracker` and `should not display the inventory
   button when the human player selects a cpu bat`.
3. [How to add a new test](../testing/how-to-add-a-new-test.md) says `waitUntil` fails on timeout and how to check an
   absence.

### M2 commits

1. `build.gradle.kts` sets `maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)` in
   `tasks.withType<Test>().configureEach`.
2. The time of `allTargetTests`, jvmTest and jsTest is written in [What was measured](#what-was-measured), with the 5
   slowest tests left on each target.

### M3 commits

1. Measure whether Gradle already runs jvmTest and jsTest at the same time under `allTargetTests`: Gradle 8.8 runs the
   tasks of one project in parallel when the configuration cache is reused, which `org.gradle.configuration-cache=true`
   asks for, but `problems=warn` lets an incompatible task fall back to running one task at a time.
2. If they run one after the other, make them run together, e.g. by fixing the configuration cache problems that stop
   it, or with `org.gradle.parallel=true`. Write the new time here.

### M4 commits

1. `writeBattleSave(storage: IStorage)` builds the battle save in code, as described in [Battle save](#battle-save).
2. `startNewBattle` loads slot 1 from the menu, as `SaveAndLoadEndToEndTest` does, walks the leader east once onto
   (2, 7), and waits until `canHumanPlayerAct`. `ExploreEndToEndTest` `should switch to the battle when the party walks
   within reach of a cpu battle unit` keeps the full walk from a new game.
3. Write the new time here, and if it is still over 2 minutes, the slowest tests left.

## Reference

### EndToEndExtension

- `waitUntil(timeout: Duration = WAIT_TIMEOUT, condition: () -> Boolean)` checks the condition every simulated frame,
  returns as soon as it holds, and throws once `timeout` of real time runs out. Real time, as the game loads its
  resources asynchronously in real time.
- `startNewGame` ends with `waitUntil { stage.virtualPadView()?.isPadShown() == true &&
  stage.battlefieldView()?.hasBattleUnitAt(KNIGHT_START_ROW, KNIGHT_START_COLUMN) == true }`.
- `startNewBattle` keeps its name. In M4 it starts from the battle save instead of `ROUTE_TO_BATTLE`.

### Battle save

Built in M4 only.

- `writeBattleSave(storage: IStorage)` in `com.mkz.rpg.shared.adapters.e2e` wires the subdomains' Apis without views,
  as `ExplorationAcceptanceTest` does, plus a `SaveApi` over `storage` with the 9 `Snapshottable`s the game saves.
- It sets up a new game with `FixedDoublesRandom.cpuBattleUnitsAlwaysResting()`, steps the party east 5 times with
  `StepParty`, from (2, 1) to (2, 6), opens the pause with `OpenPause` so `canSave` holds, and calls `saveGame(1)`.
- The save holds the version `Save.SAVE_VERSION` of the build, so it never needs rewriting when the version is bumped.

### build.gradle.kts

- `tasks.withType<Test>().configureEach` gains `maxParallelForks`. It applies to jvmTest only, as jsTest is a
  `KotlinJsTest`. Each fork is its own JVM, so the end-to-end tests share no state across forks.

## Docs

- [How to add a new test](../testing/how-to-add-a-new-test.md): the end-to-end section says `waitUntil` fails once its
  timeout runs out, and that an absence is checked after a later sign, never by waiting out the timeout.

## Tests

- `EndToEndExtensionTest`
  - `waitUntil should return as soon as the condition holds`
  - `waitUntil should fail when the condition does not hold within the timeout`

## Known limitations

- CI is not held to 2 minutes: `ubuntu-latest` has 4 vCPUs, so jvmTest gets only 2 forks there.
- The JS reports have no per-test times, so the slowest JS tests are read from the gaps between suites.
- Compiling the main and test sources is not part of the budget.

## Changes while building

- M1: the `startNewGame` fix is committed before `waitUntil` starts failing on timeout, the other way round from the
  plan, so each commit is green on its own. No other wait needed fixing: every end-to-end test, including the absence
  checks the plan names, already waited for something the game shows.
