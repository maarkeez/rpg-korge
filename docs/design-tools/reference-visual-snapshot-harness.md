# Reference: visual snapshot harness

Lookup reference for the pieces that make up the visual regression tooling. For the workflow (accepting or investigating snapshot failures), see [How to update the visual snapshot goldens](how-to-update-visual-snapshots.md).

## Gradle tasks

### `updateSnapshots`

Regenerates the golden PNGs by running `BattleSceneSnapshotUpdateTest` only.

```sh
./gradlew updateSnapshots
```

- Reuses the `jvmTest` classpath and compiled test classes.
- Does not run the rest of the test suite.
- Overwrites every PNG in `src/jvmTest/resources/snapshots/` for the states defined in the update test.

## Test classes

### `BattleSceneSnapshotTest` (`src/jvmTest/kotlin/com/mkz/rpg/screen/BattleSceneSnapshotTest.kt`)

One test per snapshot state. Each test:

1. Opens a headless 390×844 Korge window via `korgeScreenshotTest`.
2. Creates `BattleScene(seed = 42)` in a scene container.
3. Waits for the state's observable condition.
4. Captures the frame with `simulateRenderFrame()`.
5. Fails via `assertMatchesGolden` on any pixel difference.

Runs as part of `./gradlew jvmTest`.

### `BattleSceneSnapshotUpdateTest` (`src/jvmTest/kotlin/com/mkz/rpg/screen/BattleSceneSnapshotUpdateTest.kt`)

Tagged `@Tag("update-snapshots")`. Same steps as the assert test, but writes the captured frame to the golden file instead of comparing. Only executed by the `updateSnapshots` task.

## Harness (`src/jvmTest/kotlin/com/mkz/rpg/screen/BattleSceneSnapshotHarness.kt`)

Top-level `internal` declarations shared by both test classes.

### Constants

| Name | Value | Meaning |
| --- | --- | --- |
| `SNAPSHOT_SEED` | `42L` | Seed shared with the deterministic battle harness |
| `SNAPSHOT_WINDOW_SIZE` | `Size(390.0, 844.0)` | Window and virtual size, matching the real game window |
| `SNAPSHOT_GOLDEN_DIR` | `src/jvmTest/resources/snapshots` | Where golden PNGs live (committed to the repo) |
| `SNAPSHOT_REPORT_DIR` | `build/reports/snapshots` | Where `.actual.png` / `.golden.png` diff images are written on failure |
| `INITIAL_LAYOUT` | `battle-initial-layout` | Snapshot state name |
| `UNIT_SELECTED` | `battle-unit-selected` | Snapshot state name |
| `ABILITY_SELECTED` | `battle-ability-selected` | Snapshot state name |
| `ENEMY_INSPECTED`, `MOVEMENT_RANGE`, `ABILITY_COOLDOWNS`, `CAST_TARGETS`, `CAST_PREVIEW`, `PROPAGATION_PREVIEW`, `AFTER_CPU_PLAYBACK` | `battle-enemy-inspected`, `battle-movement-range`, `battle-ability-cooldowns`, `battle-cast-targets`, `battle-cast-preview`, `battle-propagation-preview`, `battle-after-cpu-playback` | Snapshot state names. Together with the three above they form the nine-state matrix |
| `SHOWCASE_SCENARIO` | `scenarios/ui-showcase.json` | Scenario used by every state except the first three and `battle-ability-cooldowns` |

### Scene lifecycle

| Function | Description |
| --- | --- |
| `OffscreenStage.createBattleScene(): BattleScene` | Creates a scene container on the offscreen stage and `changeTo` a `BattleScene(seed = SNAPSHOT_SEED)`. Returns once `sceneInit` (asset loading) completes. |
| `awaitBattleReady(scene)` | Polls until the battlefield view contains 4 `BATTLE_UNIT` images (all units deployed). 10 second timeout. |
| `OffscreenStage.selectHumanKnight(scene)` | Simulates a click on the knight tile (`row-6-column-6`) and polls until the HUD shows the unit info view. |
| `OffscreenStage.selectFirstAbility(scene)` | Simulates a click on the first visible ability button in the unit info view and polls until the ability selection highlight is shown. |
| `OffscreenStage.createBattleScene(scenarioPath, feedbackTiming)` | Same, with an optional scenario file and feedback timing (default `FeedbackTiming.Instant`; `Standard` only for the CPU playback state). |
| `OffscreenStage.selectEnemyRat(scene)`, `selectKnightAbility(scene, index)`, `selectKnightAfterHealing(scene)` | Reach the enemy-inspected, cast-targets and ability-cooldowns states. |
| `OffscreenStage.previewKnightSkullOnRat(scene)` | Selects the knight's skull and taps rat A: cast preview with HP ghost and sheet lines. |
| `OffscreenStage.previewPropagation(scene)` | Same, then waits for the conditional outline on rat B (faint, because the skull does not defeat rat A). A lethal spread is covered by `BattleUiScriptTest`, since the harness can't set up the prior hit. |
| `OffscreenStage.finishTurnAndAwaitPlayback(scene)` | Passes the turn with real timing and waits until the CPU turn and every effect have ended. |
| `OffscreenStage.capture(): Bitmap32` | Settles for 200 ms, then renders the whole stage to a `Bitmap32` with `simulateRenderFrame()`. |

### Golden file operations

| Function | Description |
| --- | --- |
| `loadGolden(name): Bitmap32?` | Decodes `src/jvmTest/resources/snapshots/<name>.png` as a straight (non-premultiplied) bitmap. Returns `null` if the file does not exist. |
| `writeGolden(name, bitmap)` | Encodes the bitmap as PNG and writes it to the golden directory, creating the directory if needed. |
| `writeReport(name, actual, golden)` | Writes `<name>.actual.png` (and `<name>.golden.png` when a golden is given) to `build/reports/snapshots/`. |
| `assertMatchesGolden(name, actual)` | Loads the golden, compares with `BitmapComparer.compare`, and throws an `AssertionError` with the diff count, max pixel distance, PSNR, and the report directory path when they differ. When the golden is missing, the error tells you to run `./gradlew updateSnapshots`. |

## Korge APIs used

| API | Package | Role |
| --- | --- | --- |
| `korgeScreenshotTest(windowSize, virtualSize, callback)` | `korlibs.korge.testing` | Opens a headless Korge app with a real offscreen OpenGL context. The callback runs as a suspend function inside the running app, so `delay`-based polling ticks real frames. Skipped when `DISABLE_HEADLESS_TEST=true`. |
| `OffscreenStage.simulateRenderFrame(view, posterize, includeBackground, useTexture)` | `korlibs.korge.testing` | Renders a view (default: the whole stage) to a `Bitmap32` without going through the window. |
| `BitmapComparer.compare(left, right): CompareResult` | `korlibs.korge.testing` | Pixel comparison. `CompareResult(pixelDiffCount, pixelTotalDistance, pixelMaxDistance, psnr, error)`. |
| `View.descendantsWith { predicate }` | `korlibs.korge.view` | Collects named/typed descendant views. Used for the observable ready-conditions. |
| `UIButton.simulateClick(views)` | `korlibs.korge.ui` | Dispatches a synthetic touch tap and mouse click on the button, which triggers its `onClick` handler without a real window. |
| `SceneContainer.changeTo { ... }` | `korlibs.korge.scene` | Enters the scene. Suspends until `sceneInit` completes; `sceneMain` keeps running as a separate job. |

## Scene readiness model

`changeTo` returns as soon as `sceneInit` (asset loading) finishes, but `sceneMain` — which builds the API graph and starts the battle setup — runs as a separate unscoped job. Events published by the setup are only applied to the views when the scene's frame-loop updater dispatches them. The harness therefore never renders immediately after `changeTo`; every state waits on an observable view condition (`awaitUntil`) that can only become true once the relevant events have been dispatched and applied.

## Failure output

On mismatch, `assertMatchesGolden` writes:

- `build/reports/snapshots/<name>.actual.png` — the current render
- `build/reports/snapshots/<name>.golden.png` — the stored baseline

and fails with a message like:

```
Snapshot 'battle-unit-selected' differs from the golden image: 1234 pixels differ (max pixel distance 255, psnr 28.4). Diff images were written to /path/to/build/reports/snapshots. If the visual change is intentional, run './gradlew updateSnapshots' to accept it.
```
