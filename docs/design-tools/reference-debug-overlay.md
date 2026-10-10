# Reference: in-game debug overlay

Lookup reference for the debug overlay's keys, flags, and the classes that back it. For the workflow (toggling, reading, restarting), see [How to use the in-game debug overlay](how-to-use-the-debug-overlay.md).

## Files

| File | Source set | Role |
| --- | --- | --- |
| `src/commonMain/kotlin/com/mkz/rpg/screen/DebugSupport.kt` | commonMain | `expect fun BattleScene.installDebugSupport(...)` |
| `src/jvmMain/kotlin/com/mkz/rpg/screen/DebugOverlay.kt` | jvmMain | The overlay view |
| `src/jvmMain/kotlin/com/mkz/rpg/screen/DebugOverlaySupport.kt` | jvmMain | `actual` — key handling, per-frame update, restart |
| `src/jsMain/kotlin/com/mkz/rpg/screen/DebugOverlaySupport.kt` | jsMain | No-op `actual` (keeps the overlay out of the web build) |
| `src/jvmTest/kotlin/com/mkz/rpg/screen/DebugOverlayTest.kt` | jvmTest | Behavior tests |

## Keys and flags

| Key | Action | Available |
| --- | --- | --- |
| `F3` | Toggle the overlay on/off | overlay installed (any scene with `debugEnabled = true`) |
| `R` | Restart the battle with the same seed | overlay visible |
| `N` | Restart the battle with seed + 1 | overlay visible |

| Flag | Where | Default | Description |
| --- | --- | --- | --- |
| `debugEnabled` | `BattleScene` constructor | `false` | When `false`, `installDebugSupport` returns immediately and no overlay is created. `Main.kt` passes `true` for local dev runs. |
| `effectiveSeed` | passed into `installDebugSupport` from `sceneMain` | — | The seed actually driving the battle (`seed` constructor arg, or the scenario's seed). Shown in the panel and used by `R`/`N`. |

## installDebugSupport

```kotlin
internal expect fun BattleScene.installDebugSupport(
    battleApi: BattleApi,
    playerApi: PlayerApi,
    battlefieldHudRepository: BattlefieldHudRepository,
    eventBus: EventBus,
    effectiveSeed: Long?,
)
```

Called from `BattleScene.sceneMain` after the battle is set up. The JVM `actual`:

- Returns early when `debugEnabled` is `false`.
- Adds a `DebugOverlay` to `sceneView`.
- Registers a per-frame `addUpdater` on `sceneView` that computes a smoothed FPS, handles `F3`/`R`/`N`, and refreshes the panel.

## DebugOverlay

```kotlin
internal class DebugOverlay(size: Size = Size(220, 150)) : UIContainer(size)
```

A `UIContainer` holding a rounded-rectangle background and a single `Text` label. Hidden by default.

| Member | Description |
| --- | --- |
| `toggle()` | Flips `visible` |
| `update(fps, frameMs, seed, round, currentPlayer, selectedUnitId, queueDepth, lastEvents)` | Rebuilds the label text. `round`/`currentPlayer`/`selectedUnitId` render as `-` when null; `lastEvents` is rendered one name per line |

Panel fields are sourced per frame from `BattleApi.searchBattle()`, `PlayerApi.searchPlayerById(...)`, `BattlefieldHudRepository.search()` (the unit id of an active range/preview HUD state), and the event bus.

## Restart (`R`/`N`)

Restarting rebuilds the `BattleScene`. Because the key handler runs from the game-loop updater (not a coroutine), it cannot call the suspend `sceneContainer.changeTo { ... }` directly, and the project's `NoRunBlockingArchitectureTest` forbids production code from depending on `kotlinx.coroutines.BuildersKt` (`launch`/`async`/`runBlocking`). So `restartBattle` uses Korge's non-suspend `changeToAsync` together with an explicit injector factory:

```kotlin
private fun BattleScene.restartBattle(nextSeed: Long?) {
    injector.root.mapPrototype(BattleScene::class) {
        BattleScene(
            seed = getOrNull<Long>(),
            scenarioPath = getOrNull<String>(),
            debugEnabled = getOrNull<Boolean>() ?: false,
        )
    }
    val injects = (listOfNotNull(nextSeed, scenarioPath) + debugEnabled).toTypedArray()
    sceneContainer.changeToAsync(BattleScene::class, *injects)
}
```

Notes on this mechanism:

- `changeToAsync(KClass, vararg injects)` is the non-suspend overload. It is **not** `inline`, so the `async` it calls internally stays in Korge's bytecode and the caller keeps no `BuildersKt` dependency (the reified `changeToAsync<T>` overload *is* inline and would have inlined `async` back into this file).
- The `injects` are registered in the scene's child injector by runtime class (`Long`, `String`, `Boolean`), then `changeToAsync` builds the new scene through the `mapPrototype` factory, which reads them back with `getOrNull`. This is used because Korge's default fallback does not inject into `BattleScene`'s nullable constructor parameters.
- `nextSeed` is the current `effectiveSeed` (`R`) or `effectiveSeed + 1` (`N`); `scenarioPath` and `debugEnabled` are carried over unchanged.

## Event bus additions

The overlay's queue-depth and recent-events rows rely on two read-only members added to `InMemoryEventBus` (`src/commonMain/.../shared/adapters/events/InMemoryEventBus.kt`):

| Member | Description |
| --- | --- |
| `queueDepth: Int` | Number of events queued and not yet dispatched |
| `lastEvents: List<DomainEvent>` | The most recently dispatched events, kept in a ring buffer capped at `MAX_LAST_EVENTS = 32` |

The panel shows `takeLast(6)` of `lastEvents` as simple class names.

## DebugOverlayTest

| Test | What it asserts |
| --- | --- |
| `should show the debug overlay when F3 is pressed and debug is enabled` | Overlay becomes visible and its label contains the seed |
| `should hide the debug overlay when F3 is pressed again` | A second `F3` hides it again |
| `should not install the debug overlay when debug is disabled` | No `DebugOverlay` in `sceneView` when `debugEnabled = false` |
| `should display the round and current player when the overlay is shown` | Label reflects the round and the current player name |
| `should restart the battle with the same seed when R is pressed` | `R` produces a new `BattleScene` with the same seed |
| `should restart the battle with the next seed when N is pressed` | `N` produces a new `BattleScene` with seed + 1 |
