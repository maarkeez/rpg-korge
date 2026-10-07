# Design tools plan — iterating on look & feel and UI

## Purpose

Give us a fast, reliable loop for iterating on the look & feel of the game:
deterministic battles, pixel-exact visual regression, scripted UI interaction,
and in-game debug affordances — without touching game design or gameplay rules.

## Non-goals

- No changes to gameplay rules, abilities, balance, or content.
- No new rendering backends or engine changes; we build on Korge 6.0.0 as-is.
- No mobile/iOS/WASM targets (project builds JVM + JS only).

## Current state

- **Engine**: Korge 6.0.0, Kotlin Multiplatform, targets `jvm` + `js`. Fixed window `Size(390, 844)`.
- **Architecture**: clean architecture per feature (`domain` / `usecases` / `adapters`), event-driven via `shared.domain.EventBus` + `InMemoryEventBus`, presentation adapters named `*Api`. `BattleScene` and the acceptance tests construct the same API graph.
- **ArchUnit/Konsist rules** (`CleanArchitectureTest`): `domain` may only depend on `domain` + external (`kotlin`, `java`, `kotlinx`, `jetbrains`); `usecases` may depend on `domain`/`usecases` + external. New injected dependencies must respect this.
- **Test stack** (122 files, `src/jvmTest`): JUnit 5 + AssertJ + Mockito, object mothers, `should XXX when YYY`, Given/When/Then, `FakeEventBus` for unit tests, `InMemoryEventBus` + `dispatch()` for listener/acceptance tests, acceptance tests under `com.mkz.rpg.shared.usecases.acceptance`. Run with `./gradlew clean jvmTest`. Lint via kotlinter (`./gradlew ktlintCheck`).
- **Korge 6 headless test capabilities (verified in `korge-jvm-6.0.0.jar`)**:
  - `korlibs.korge.tests.ViewsForTesting` — headless view/scene tests: `viewsTest {}`, `sceneTest {}`, `simulateClick(view)`, `mouseMoveTo`, `mouseMoveAndClickTo`, `keyType`, `simulateFrame(n)`, `timeProvider`/`setTime`, `stage`, `ag`, `stats`, `log` (AG render-call log). Already used by `BarViewTest`, `PreviewBarViewTest`.
  - `korlibs.korge.testing.korgeScreenshotTest(windowSize, virtualSize, bgcolor, devicePixelRatio) { ... }` — builds a full `KorgeHeadless` app on an offscreen `AGOpenglAWT` (AwtOffscreenGameWindow, no visible window). Callback receiver is the `OffscreenStage`.
  - `korlibs.graphics.AG.readColor(frameBuffer): Bitmap32` — reads back the rendered framebuffer; on JVM, `Bitmap32.toAwt()` + `ImageIO.write` produces a PNG. This is the pixel-capture primitive for snapshot tests.

### Determinism blockers (verified)

| File | Line | Problem |
|---|---|---|
| `cpuBrain/usecases/commands/PlayTurn.kt` | 50, 54 | unseeded `randomOrNull()` for CPU ability + target choice |
| `battleUnit/usecases/commands/ApplyEffect.kt` | 46 | `Random.nextLong()` for generated unit IDs |

Until these are injected, identical inputs do not produce identical battles, which blocks replay, visual regression, and reproducible UI E2E.

## Progress

| Tool | Status | Notes |
| --- | --- | --- |
| T1 — Deterministic battle harness | **Done** | Seeded `Random` in `PlayTurn`/`ApplyEffect`, `BattleScene(seed)`, `RecordingEventBus`, deterministic acceptance tests |
| T2 — Snapshot testing | **Done** | 3 goldens (initial layout, unit selected, ability selected), `updateSnapshots` gradle task, strict pixel compare |
| T3 — UI end-to-end tests | **Done** | `BattleUiScript` + `BattleUiScriptTest`: real views/presenters on a deterministic API graph, simulated clicks, CPU turn settles in one `dispatch()` |
| T5 — File-based battle scenarios | Not started | |
| T6 — Palette / asset checks | Not started | |
| T4 — Debug overlay + shortcuts | Not started | |
| T7 — VFX sandbox | Not started | |
| T8 — Iteration polish | Not started | |
| T9 — Turn scrubber | Not started | |

## The tools

### T1 — Deterministic battle harness (foundation) — done

**Goal**: identical setup + seed + command sequence ⇒ identical battle, every time, on every platform.

**Design**:
- Inject `kotlin.random.Random` into `PlayTurn` and `ApplyEffect` constructors (external `kotlin` type, so arch rules are satisfied), and thread it through `CpuBrainApi`, `BattleUnitApi`, and `BattleScene`.
- `BattleScene` takes an optional `seed: Long?` (default: fresh seed). Deterministic entry point: `BattleScene(seed = 42L)`.
- Add `RecordingEventBus` (decorator over `EventBus`, lives in `shared/adapters/events` alongside `InMemoryEventBus`): delegates all calls to a wrapped bus and appends every published event to a public `List<DomainEvent>`. Provides `events` and `clear()`.
- Replay: a helper (test utility or `commonMain` object, T5 decides final home) that re-publishes a recorded `Request*` command sequence against a fresh API graph + same seed.

**Files to touch**: `PlayTurn.kt`, `ApplyEffect.kt`, `CpuBrainApi.kt`, `BattleUnitApi.kt`, `BattleScene.kt`, new `RecordingEventBus.kt`, new `RecordingEventBusTest.kt`, acceptance test for determinism.

**Tests**:
- `RecordingEventBusTest`: records published events, delegates publish/dispatch/subscribe.
- Acceptance test: run the same battle twice with the same seed → identical event sequence (assert via `RecordingEventBus`).
- Unit tests for `PlayTurn`/`ApplyEffect` with a fixed `Random` (seeded) asserting deterministic choices.

**Acceptance criteria**: two runs with seed N produce byte-identical event logs; a recorded command log replayed against a fresh graph produces the same end state.

### T2 — Snapshot (visual regression) testing — done

**Goal**: pixel-exact golden images for the main UI states, diffed in CI locally; one command to update goldens.

**Design**:
- Golden images live in `src/jvmTest/resources/snapshots/*.png` (committed).
- Capture: `korgeScreenshotTest(windowSize = Size(390, 844)) { <build scene state>; simulateFrame(N); korge.ag.readColor(korge.ag.mainFrameBuffer) }` → `Bitmap32`.
- Compare: `Bitmap32` vs golden `Bitmap32` (load via `Bitmap32(ImageIO.read(...))` or `readBitmap`); assert either exact equality or a small per-pixel tolerance (start strict, add tolerance only if GL non-determinism proves out).
- Two gradle-friendly entry points, implemented as ordinary JVM tests (no new plugins):
  - `SnapshotTest` (jvmTest): asserts equality against goldens.
  - `SnapshotUpdateTest` (jvmTest, annotated `@Tag("update-snapshots")`): overwrites goldens. Run with `./gradlew jvmTest --tests "*SnapshotUpdateTest"` or `-PincludeTags`.
- Scenes to snapshot first: initial battle layout, unit selected (preview), attack preview, HUD states (health/mana bars), turn handoff.

**Tests**: `SnapshotTest` for each named scene state; update path exercised manually.

**Acceptance criteria**: `./gradlew jvmTest` fails with a clear message + diff image path when a scene changes; one gradle invocation regenerates goldens.

### T3 — UI end-to-end tests (simulated input) — done

**Goal**: script real player interactions (tap unit → tap ability → tap target) headlessly to guard the interaction flow, not just state.

**Design**:
- Use `ViewsForTesting`/`sceneTest` + `simulateClick` / `mouseMoveAndClickTo` on the real views (`BattlefieldView`, `PlayerCallToActionView`, `AbilityButtonView`) wired to a deterministic API graph (T1).
- Helper `BattleUiScript` (test source): `selectUnit(id)`, `castAbility(abilityIndex, targetRow, targetCol)`, `finishTurn()` — map logical intents to clicks on the actual views using their screen coordinates.
- Assertions on resulting state via the same `*Api` queries used by acceptance tests.

**Tests**: e.g. `should move a unit when player taps the destination tile`, `should cast an ability when player taps ability button and target`, `should pass turn when finish button tapped`.

**Acceptance criteria**: a full CPU-vs-CPU turn cycle executes purely through simulated input and ends in the expected state.

### T4 — In-game debug overlay + shortcuts (gated)

**Goal**: while running `runJvmAutoreload`, see what the game sees.

**Design**:
- A `DebugOverlay` view (presentation layer, `screen/`) toggled by a key (e.g. `F3`), disabled by default and disabled when a `debugEnabled = false` flag is set (flag sourced from a constructor param on `BattleScene`, defaulting to `true` in `Main.kt` for JVM dev runs, `false` in tests).
- Overlay contents: FPS/frame time (Korge `stats`), current turn/phase, selected unit id, event queue depth (via a lightweight read on the bus wrapper), last N events.
- Extra shortcuts (dev only): `R` = restart battle with same seed, `N` = next battle (seed++), `1..9` = jump to turn N (relies on T9 when available, else skip).
- Must not leak into `js` production: gated so the web build omits it (compile-time `expect`/`actual` or a simple flag read at `Main.kt`).

**Tests**: overlay view test (visible when toggled, hidden otherwise); arch test already covers layering.

**Acceptance criteria**: pressing `F3` in the dev run shows the overlay; it never appears in the JS build.

### T5 — File-based battle scenarios

**Goal**: describe a battle (units, positions, abilities, seed) in a data file and load it from the game or tests — the same file drives T1 replay, T2 snapshots, and T3 UI E2E.

**Design**:
- Scenario format: JSON (kotlinx-serialization is already a Korge dependency) under `src/commonMain/resources/scenarios/*.json` (JVM) + mirrored for tests.
- DTO + loader `loadScenario(path): BattleScenario` in a small `battlesetup` extension (presentation/adapter layer), mapping onto the existing `SetupBattle` inputs (which already support custom unit placement — see default 16x16 setup).
- `Main.kt`/`BattleScene` accept an optional scenario path (JVM dev run only).
- Ship 3 starter scenarios: default demo, "chain showcase" (units arranged to trigger chain effects), "terrain mix" (lava/water).

**Tests**: loader round-trips; each starter scenario loads and reaches a stable state deterministically (T1).

**Acceptance criteria**: `./gradlew` dev run with `-Pscenario=chain.json` starts that battle; snapshot tests can target any scenario.

### T6 — Palette checker + asset contact sheet

**Goal**: enforce the Famicube palette for new pixel art and see all assets at a glance.

**Design**:
- `PaletteCheckerTest` (jvmTest): loads `famicube-palette.png`, samples every pixel of every sprite/terrain/ability-icon PNG under `resources/`, fails listing file + out-of-palette colors + count.
- Contact sheet: a jvmTest task (`@Tag("contact-sheet")`) that draws all sprites + terrain tiles + icons into one large `Bitmap32` grid and writes `build/contact-sheet.png`.
- Both read assets via `resourcesVfs`/`VfsFile` like production does.

**Acceptance criteria**: introducing one off-palette pixel fails the build with the file name; contact sheet regenerates with one command.

### T7 — VFX / chain-effect sandbox scene

**Goal**: a throwaway scene to preview chain-effect timing, particles, and sprite animations without playing a battle.

**Design**:
- `VfxSandboxScene` (presentation, dev-only): deterministic scripted timeline that fires a fixed chain of effects (reuse `EffectApi`/`BattleUnitApi` on a synthetic setup) with adjustable playback speed (`timeProvider`/`setTime` in tests; a speed slider or key in the dev run).
- Launched from `Main.kt` behind a flag (`-Pscene=sandbox`) or a key press in dev.
- Reuses T1 seed + T5 scenario inputs so a sandbox run is reproducible.

**Acceptance criteria**: sandbox plays the same chain identically on each run; useful for tuning animation durations/offsets.

### T8 — Iteration-loop polish

**Goal**: make the daily loop one keystroke.

**Design**:
- Wire everything above into a few documented commands (in `docs/design-tools/README.md`): run dev game, run all tests, update snapshots, regenerate contact sheet, run scenario X.
- Ensure `runJvmAutoreload` picks up the debug flag and scenario flag.
- Keep goldens + scenarios + contact sheet out of the JS bundle (resources filtering check).

**Acceptance criteria**: a new developer follows the README and runs every tool without reading source.

### T9 — Turn-history scrubber (optional)

**Goal**: during a dev run, scrub back/forward through played turns to inspect any frame.

**Design** (depends on T1):
- Record per-turn snapshots (state DTOs) in dev mode; `][` keys step through, re-deriving the view state from the stored snapshot via the existing `*Api` read models.
- If the state model turns out too large to snapshot cheaply, degrade to "replay commands up to turn N" (T1 replay) and document the tradeoff.

**Acceptance criteria**: in a dev run, stepping to turn N renders the exact board/HP of turn N.

## Documentation strategy (Diataxis)

Each tool ships with docs under `docs/design-tools/`:

- **How-to** (task-oriented, imperative): e.g. `how-to-run-a-deterministic-replay.md`, `how-to-add-a-snapshot-test.md`, `how-to-write-a-ui-e2e-test.md`, `how-to-update-snapshot-goldens.md`, `how-to-add-a-battle-scenario.md`, `how-to-check-palette-compliance.md`.
- **Reference** (descriptive lookup): e.g. `reference-deterministic-battle-harness.md` (APIs: seed plumbing, `RecordingEventBus`, replay helper), `reference-snapshot-testing.md`, `reference-debug-overlay.md` (keys + flags), `reference-battle-scenarios.md` (JSON schema).
- `docs/design-tools/README.md`: index + the T8 command cheat-sheet.
- Follow existing `docs/testing/` conventions: imperative voice, concrete code samples, "Run tests" section.

## Per-tool workflow

For each tool T1..T9:
1. Implement + tests (conventions in `docs/testing/how-to-add-a-new-test.md`).
2. `./gradlew clean jvmTest` green + `./gradlew ktlintCheck` green.
3. Add the Diataxis docs (how-to + reference) under `docs/design-tools/`.
4. Commit (message: `Add <tool> ...`), push to `origin/task/design-tools`.

## Implementation order and dependencies

1. **T1** — everything else depends on determinism.
2. **T2** — needs T1 (stable pixels) + the verified `korgeScreenshotTest`/`readColor` path.
3. **T3** — needs T1 (deterministic outcomes to assert).
4. **T5** — needs T1; unifies inputs for T2/T3/T7.
5. **T6** — independent, quick win, can land in parallel after T1.
6. **T4** — needs T1 (seed display/restart); independent of T2/T3.
7. **T7** — needs T1 + T5.
8. **T8** — after T1..T7 exist.
9. **T9** — optional, needs T1; last.

## Risks and mitigations

- **GL non-determinism in headless snapshots** (macOS offscreen AWT/GL can vary between runs): mitigate by capturing after `simulateFrame` settles, comparing with a tight tolerance, and if flaky, pin the comparison to a software path (`checkGl = false`) — the `korgeScreenshotTest`/`suspendTestWithOffscreenAG` knobs exist for this.
- **`kotlin.random.Random` injection ripples through constructors**: keep it additive with default `Random.Default` at API boundaries so existing call sites (tests) compile unchanged; only the new deterministic entry points pass a seeded instance.
- **Scenario/flag leakage into the JS bundle**: gate all dev-only entry points in `Main.kt` behind the JVM source set or a compile constant; verify with a JS build.
- **Snapshot golden churn**: commit goldens per tool milestone; update them deliberately via the update task only.
