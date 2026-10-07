# How to update the visual snapshot goldens

## When to use this

Use this when a `BattleSceneSnapshotTest` failure is caused by a visual change you made on purpose — a new sprite, a reworked HUD, a layout tweak — and you want to accept the new look as the new baseline.

## Background

`BattleSceneSnapshotTest` renders `BattleScene` in a headless Korge window (390×844, the same size as the real game window) and compares the pixels against golden PNGs stored in `src/jvmTest/resources/snapshots/`. The comparison is strict: a single differing pixel fails the test.

Each test drives the scene to a specific state before capturing:

| Snapshot | State captured |
| --- | --- |
| `battle-initial-layout.png` | The default battle right after setup (terrain, 4 deployed units, HUD idle, finish-turn bar) |
| `battle-unit-selected.png` | The human knight selected (movement range highlighted, unit info HUD with health/mana bars and ability buttons) |
| `battle-ability-selected.png` | The knight's first ability selected (cast range highlighted, ability button highlighted) |

All snapshots use the seeded battle from the deterministic replay tooling (`seed = 42`), so CPU behavior is stable across runs.

## Steps

### Accept a visual change

1. Run the snapshot tests and confirm the failure is the visual change you intended:

   ```sh
   ./gradlew jvmTest --tests "com.mkz.rpg.screen.BattleSceneSnapshotTest"
   ```

2. Open the diff images written next to the failure to double-check what changed:

   - `build/reports/snapshots/<name>.actual.png` — what the scene renders now
   - `build/reports/snapshots/<name>.golden.png` — the stored baseline

3. Regenerate the goldens:

   ```sh
   ./gradlew updateSnapshots
   ```

   This runs `BattleSceneSnapshotUpdateTest`, which renders the same states and overwrites the PNGs in `src/jvmTest/resources/snapshots/`.

4. Run the full suite to confirm everything is green:

   ```sh
   ./gradlew jvmTest
   ```

5. Commit the updated PNGs together with the code change that caused them. The goldens are part of the contract — a commit that changes the look without updating the goldens is a broken commit.

### Investigate an unexpected failure

1. Read the assertion message. It states how many pixels differ, the max per-pixel distance, and the PSNR, and points at `build/reports/snapshots/` for the images.
2. Open the `.actual.png` and `.golden.png` side by side.
3. If the difference is a regression, fix the code. If it is intentional, follow the "Accept a visual change" steps.

### Add a new snapshot state

1. Add a state name constant to `BattleSceneSnapshotHarness.kt` (e.g. `internal const val TURN_HANDOFF: String = "battle-turn-handoff"`).
2. Add the interaction steps that drive the scene to that state as a function in the same file (the existing `selectHumanKnight` and `selectFirstAbility` show the pattern: perform the interaction, then `awaitUntil` on an observable view condition).
3. Add one test in `BattleSceneSnapshotTest` that drives the state and calls `assertMatchesGolden`.
4. Add the matching test in `BattleSceneSnapshotUpdateTest` that drives the same state and calls `writeGolden`.
5. Run `./gradlew updateSnapshots` to generate the golden, review the image, then run `./gradlew jvmTest`.

## Verification

```sh
./gradlew jvmTest --tests "com.mkz.rpg.screen.BattleSceneSnapshotTest"
```

All three tests pass when the rendered scenes match the committed goldens exactly.

## Notes

- The comparison is machine-specific. Golden images generated on one machine may not match pixel-for-pixel on another (different GPU drivers or font rendering). If you move the project to a new machine, regenerate the goldens once with `./gradlew updateSnapshots`.
- Headless rendering uses a real (offscreen) OpenGL context, so a display server is required. To skip the snapshot tests in a headless CI environment, set `DISABLE_HEADLESS_TEST=true` — Korge's `korgeScreenshotTest` skips the test when that environment variable is set.
- The update task (`./gradlew updateSnapshots`) only runs the update tests, not the full suite. Always follow it with a full `./gradlew jvmTest`.
