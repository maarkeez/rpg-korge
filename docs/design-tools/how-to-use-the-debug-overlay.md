# How to use the in-game debug overlay

## When to use this

Use this while running the game locally (`runJvm` / `runJvmAutoreload`) to see what the game is doing: the current FPS, the active seed, the round and whose turn it is, which unit's range is on screen, the event queue depth, and the last few events. It also lets you restart the battle with the same or the next seed without relaunching.

## Background

The overlay is a dev-only tool. It is compiled into the JVM target and left out of the JS target via an `expect`/`actual` pair: the JVM actual installs the overlay, the JS actual is a no-op. Whether it is active in a given scene is controlled by the `BattleScene.debugEnabled` flag, which `Main.kt` sets to `true` for local dev runs. It is `false` by default, so tests and the web build never see it.

The overlay reads from the same `*Api` graph the game uses (`BattleApi`, `PlayerApi`, `BattlefieldHudRepository`) and from the event bus, so it reflects real game state rather than a copy.

## Steps

### Start the game with the overlay available

1. Run the local dev target:

   ```sh
   ./gradlew runJvm
   ```

   Add `-Pscenario=<name>` to start a specific battle (see [How to run a battle scenario](how-to-run-a-battle-scenario.md)). `runJvmAutoreload` behaves the same but relaunches on source changes.

2. The overlay is off by default. It only exists in the scene because `Main.kt` passes `debugEnabled = true`.

### Show and hide the overlay

1. Press `F3` to toggle the overlay on and off. The overlay is a small semi-transparent panel in the top-left corner.
2. While it is visible it updates every frame.

### Read the overlay

The panel shows, top to bottom:

- `FPS <n> | <n>ms` — smoothed frames-per-second and frame time.
- `seed <n>` — the effective seed driving the battle's random number generator.
- `round <n> | <player>` — the current round and the name of the player whose turn it is.
- `unit <id>` — the id of the battle unit whose movement/ability range is currently displayed, or `-`.
- `queue <n>` — how many events are waiting to be dispatched on the event bus.
- Up to six most-recently dispatched event names, one per line.

### Restart the battle from the overlay

These shortcuts work only while the overlay is visible:

- Press `R` to restart the battle with the **same** seed. The scene is rebuilt with the current effective seed, so the battle starts again from round one with the same CPU decisions.
- Press `N` to restart with the **next** seed (current seed + 1), giving you a fresh battle to compare against.

## Verification

`DebugOverlayTest` (`src/jvmTest/kotlin/com/mkz/rpg/screen/DebugOverlayTest.kt`) drives the real scene with simulated key presses and asserts the overlay appears, hides, reflects round/player, and that `R`/`N` rebuild the scene with the expected seed.

Run it with:

```sh
./gradlew jvmTest --tests "com.mkz.rpg.screen.DebugOverlayTest"
```

## Notes

- The overlay is invisible until you press `F3`; the per-frame update is skipped entirely while it is hidden, so it costs nothing when off.
- `R`/`N` are dev conveniences. In a normal play session the seed comes from the scenario file; these keys just re-roll it.
- Because the JS `actual` is empty, the web build never contains the overlay, the shortcut handling, or the restart path.
