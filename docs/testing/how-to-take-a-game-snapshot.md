# How to take a snapshot of the game during an end-to-end test

> **Summary:** Save a PNG image of what the player sees at any step of an end-to-end test, e.g.: to check a layout or a visual change. The snapshot test is a local tool: do not commit it.

## Prerequisites

- A machine with OpenGL, e.g.: a macOS or desktop development machine. The headless CI runners are not supported.
- An end-to-end test flow to start from, see [End-to-end test](how-to-add-a-new-test.md#test-example---end-to-end-test).

## Steps

### 1. Create a test class that renders with OpenGL

Create the test class under `src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/e2e`, as the OpenGL rendering is only available on the JVM. Override `createAg()` so the frames are rendered with an offscreen OpenGL context, and release it after each test:

```kotlin
class KnightSelectedSnapshotTest : ViewsForTesting(windowSize = GAME_WINDOW_SIZE) {
    private lateinit var glContext: KmlGlContext

    // Renders the frames with an offscreen OpenGL context, instead of only logging the draw calls.
    override fun createAg(): AG {
        glContext = createKmlGlContext(GAME_WINDOW_SIZE.width.toInt(), GAME_WINDOW_SIZE.height.toInt())
        glContext.set()
        return AGOpenglAWT(context = glContext)
    }

    @AfterTest
    fun releaseGlContext() {
        if (!::glContext.isInitialized) return
        glContext.unset()
        glContext.close()
    }
}
```

Imports:

```kotlin
import korlibs.graphics.AG
import korlibs.kgl.KmlGlContext
import korlibs.korge.testing.createKmlGlContext
import korlibs.render.awt.AGOpenglAWT
import kotlin.test.AfterTest
```

### 2. Add the snapshot helper

Add this function to the test class. It renders the stage with the game background and writes it to `build/snapshots/<name>.png`:

```kotlin
private suspend fun Stage.takeSnapshot(name: String) {
    // The game background, see Main.kt.
    views.clearColor = Colors["#2b2b2b"]
    val snapshot = renderToBitmap(views, includeBackground = true)
    val file = localCurrentDirVfs["build/snapshots/$name.png"]
    file.parent.mkdirs()
    snapshot.writeTo(file, PNG)
}
```

Imports:

```kotlin
import korlibs.image.color.Colors
import korlibs.image.format.PNG
import korlibs.image.format.writeTo
import korlibs.io.file.std.localCurrentDirVfs
import korlibs.korge.view.Stage
import korlibs.korge.view.renderToBitmap
```

### 3. Write the test and take the snapshot

Play the game as in any end-to-end test, and call `takeSnapshot` at the step to capture:

```kotlin
@Test
fun `should take a snapshot of the battle scene when the knight is selected`() =
    viewsTest {
        startGame()
        waitUntil { battlefieldView()?.hasBattleUnitAt(6, 6) == true && canHumanPlayerAct() }
        val battlefieldView = requireNotNull(battlefieldView())
        clickTile(battlefieldView, 6, 6)
        waitUntil { battlefieldView.hasSelectionAt(6, 5) }
        takeSnapshot("knight-selected")
    }
```

Call `takeSnapshot` with a different name at each step to capture several snapshots in the same test.

### 4. Run the test

```shell
./gradlew jvmTest --tests '*KnightSelectedSnapshotTest*' --rerun
```

### 5. Delete the test

Delete the test class once you have the snapshots. The snapshots stay in `build/snapshots` until the next `./gradlew clean`.

## Verify

Open `build/snapshots/knight-selected.png`. It is a 390x844 image of the battle scene, as in the game: the dark background, the battle info, the battlefield with the knight selected and its movement range, and the knight info.

## Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| The snapshot is blank | The test class does not override `createAg()`, so the frames are only logged | Override `createAg()` as in step 1 |
| The background is white and the white texts are missing | `views.clearColor` is not set, and the tests do not use the game background | Set `views.clearColor` before rendering, as in step 2 |
| The snapshot does not show the last action | The snapshot is taken before the scene has displayed it | Wait for what the player sees with `waitUntil` before calling `takeSnapshot` |
| Creating the OpenGL context fails | The machine has no OpenGL, e.g.: a headless CI runner | Run the test on a development machine |
