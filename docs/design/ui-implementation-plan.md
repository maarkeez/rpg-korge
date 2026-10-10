# UI Implementation Plan — Tactical Battle UI

> Status: **planned, not started**. Written 2026-10-10 against branch `task/design-tools` (commit `d53cf40`).
> This plan is self-contained. You do not need the conversation that produced it.

---

## 0. How to read this plan (for low-memory agents)

You do not need to load the whole file for every session:

1. On first read, read sections 1–5 and section 11 completely.
2. For each work session, read section 4 (gap analysis), section 5 (principles), **only the milestone you are implementing** in section 6, section 7 (testing) and section 11.
3. Open only the files listed under that milestone's *Repository touchpoints*. Don't re-explore the repository.

All paths are repository-relative. `src/commonMain/kotlin/com/mkz/rpg/` is abbreviated to `rpg/`, and `src/jvmTest/kotlin/com/mkz/rpg/` is abbreviated to `test/`.

---

## 1. Purpose and scope

This plan turns the prototype battle screen into the battlefield-first, touch-driven, portrait UI described in the design brief. The work is split into small milestones, and you can verify each one before starting the next.

**In scope:** battle-scene layout, pixel-perfect rendering rules, the Famicube UI palette, unit selection and contextual info, movement range, the ability bar and cooldown states, target highlighting, side-effect-free consequence previews, status, propagation and conditional-outcome cues, pixel-art combat feedback, and regression coverage for all of these.

**Out of scope:** see section 10. In short: no new gameplay rules, no combat log, no exploration mode, no new platforms and no final artwork.

---

## 2. Design references

| Document | Role |
| --- | --- |
| [`docs/design/design-brief.md`](design-brief.md) | **Source of truth for product intent.** It is short (72 lines), so read all of it. |
| [`docs/design-tools/`](../design-tools/) | How-to and reference docs for the existing UI tooling: scenarios, deterministic replay, scripted UI tests, snapshots, debug overlay and palette checks. |
| [`docs/testing/how-to-add-a-new-test.md`](../testing/how-to-add-a-new-test.md) | Mandatory test conventions. |
| [`docs/testing/how-to-add-an-effect-outcome.md`](../testing/how-to-add-an-effect-outcome.md) | How effect outcomes flow through the domain. Read it before touching effects. |
| [`docs/plans/design-tools-plan.md`](../plans/design-tools-plan.md) | History of the tooling milestones (T1–T…), already done. |
| `resources/famicube-palette.png` | **The only authoritative palette** (64×1 px, 64 colors). Every other asset may be replaced. |

### 2.1 Requirements extracted from the brief

| # | Category | Requirements (brief section) |
| --- | --- | --- |
| R1 | Visual style | ALttP-style top-down pixel art and Famicube palette only (§2). Hybrid approach: pixel-art world and effects, with higher-resolution UI allowed for bars, icons and text (§2). Authored pixel animation instead of lighting, bloom or VFX (§2). |
| R2 | Layout | Portrait, iPhone 14 class (390×844 pt). The scrolling grid fills most of the screen. Minimal permanent UI, with contextual UI on selection (§3). |
| R3 | Touch flow | Tap an ally to show its movement range, actions and ability bar. Tap a reachable tile to move, and selection stays active. Then choose an ability, choose a target and preview, then confirm (§4). Interactions should be reversible and discoverable. No tiny targets and no hover, keyboard or controller dependence (§4). |
| R4 | Unit info | HP and important statuses compact and visible *on units*. Details appear on selection or inspection, not in permanent panels (§5). |
| R5 | Movement range | Shown when an ally is selected (§4.1). |
| R6 | Ability bar | Up to 6 abilities in a compact bar. Ready and cooldown states must be immediately distinguishable (§4.3). |
| R7 | Targeting | Highlight valid cast locations. Invalid positions must be distinguishable **without relying on color alone** (§4.4, §6). |
| R8 | Preview | Show affected targets and predictable effects before confirming. Clearly separate uncertain, conditional and situational effects (§4.5, §5). |
| R9 | Propagation and synergy | Visual hints for synergies. Make spread-to-nearby and on-death triggers understandable through cues on the battlefield (§5). |
| R10 | Feedback | Pixel animation, target highlights and visible state changes are the primary feedback. Text is concise and contextual. Major events may have distinctive animations (§5). |
| R11 | Scaling | Pixel-perfect scaling, with no blur and no mixed pixel sizes (§7). |
| R12 | Reuse | Reusable tiles, sprite parts, UI components, icons and effects. Document a small set of conventions (§7). Keep world presentation, combat info and interaction logic separate (§7). |
| R13 | Exclusions | No combat log, panel clutter, crowded status icons, tiny touch targets, heavy lighting or effects that hide tactical info. No exploration or combat mode transition and no RTS design (§8). |

**Brief priority rule used to resolve conflicts:** tactical clarity comes first, then the battlefield as focus, then retro aesthetic, then spectacle. The brief's guiding rule applies: every UI element must clarify a decision, communicate important state or improve touch interaction.

### 2.2 Open questions and conservative defaults

Do not invent answers to these. Use the default unless the developer says otherwise. If a milestone depends on one of these, mention it in your report.

| # | Question | Conservative default used by this plan |
| --- | --- | --- |
| Q1 | Pixel scale: 3× (current, 48 pt tiles, about 8 columns visible) or 2× (32 pt tiles, about 12 columns, closer to the SNES field of view)? | **Keep 3×.** 48 pt tiles meet the 44 pt minimum touch target, and 32 pt tiles do not. Expose the scale as a single constant so a later change is one line. |
| Q2 | Should enemies be inspectable, and should their movement range be shown? | Tapping an enemy shows its info and its movement range in a **distinct "inspect" style**. It never shows an ability bar. No attack-threat overlay (not requested). |
| Q3 | What does a tap on an invalid tile do while an ability is selected? (Today it resets to Idle and loses the unit selection.) | It returns to the **unit-selected** state, with the ability deselected and the movement range shown. This is less destructive and still reversible. |
| Q4 | Should terrain hazards (lava `lava-damage`, applied at *round start*, see `rpg/battlefield/usecases/commands/RequestEffectApplicationToOccupants.kt`) be shown in the movement preview? | Show a small, static hazard glyph on reachable tiles whose terrain has an `effectId`. No numbers. |
| Q5 | Summons (`bee` ability, `DEPLOY_BATTLE_UNIT`) can push allies past the brief's "up to four". Is there a cap? | Not a UI concern. Leave gameplay unchanged and make sure the UI does not assume 4 units. |
| Q6 | Portraits `unit/knight_portrait.png` and `unit/rat_portrait.png` are not Famicube (they are excluded from the palette check). Are they allowed under "hybrid UI"? | Keep using them as placeholders. Do not add them to the palette check. Flag them in reports. |
| Q7 | Abilities have no description text in the domain (`Ability.Dto` has `name`, `cost`, `cooldown`, `targeting` and `effectSpecs` only). | Generate a one-line summary from the effect specs in the presenter, for example `10 dmg · Venom 3/turn ×5`. Don't add a description field to the domain. |
| Q8 | Final art: status icons, numerals and hit sparks. | Draw highlights, bars and pips **procedurally** with palette colors. Anything that needs a sprite goes on the asset request list (section 9.3), and you use a procedural placeholder until the art exists. |
| Q9 | Mobile targets: only `targetJvm()` and `targetJs()` are enabled in `build.gradle.kts`. | Verify "iPhone 14" with the 390×844 JVM window plus the JS build in a browser at 390×844 with touch emulation. Don't enable iOS or Android. |

---

## 3. Project architecture findings (verified)

### 3.1 Stack and build

- Korge **6.0.0** (`gradle/libs.versions.toml`) and Kotlin **2.2.0**, forced in `build.gradle.kts`. Kotlin Multiplatform with **JVM and JS targets only**.
- Tests use JUnit 5, AssertJ, mockito-kotlin, ArchUnit and Konsist (`build.gradle.kts`). CI runs `./gradlew jvmTest` (`.github/workflows/gradle.yml`).
- The pre-commit hook (`.githooks/pre-commit`) runs `./gradlew formatKotlin` and `./gradlew lintKotlin`.
- Window: `Korge(windowSize = Size(390, 844), backgroundColor = #2b2b2b)` in `rpg/Main.kt`. No `virtualSize` or scale mode is set.
- Useful Gradle tasks: `jvmTest`, `updateSnapshots`, `generateContactSheet` and `runJvm -Pscenario=<name>`.

### 3.2 Architecture rules (enforced by tests, so read them before adding classes)

`test/shared/adapters/architecture/`:

- `CleanArchitectureTest`: `..domain..` may depend only on domain and the Kotlin/Java stdlib. `..usecases..` may depend only on usecases and domain. `..adapters..` may depend on anything.
- `CommandUseCaseArchitectureTest`: classes in `usecases.commands` expose exactly one public `operator fun invoke` returning `Unit`, and **must not depend on other commands** (they communicate through events).
- `QueryUseCaseArchitectureTest`: classes in `usecases.queries` expose one public `operator fun invoke` returning a **non-Unit** value. Nested result data classes are fine (see `WhereCanCast.CastGroup`).
- `DomainArchitectureTest`: top-level declarations in a `domain` package must be named `<Sub>`, `<Sub>Event`, `<Sub>Repository` or `<Sub>Error`. Aggregate roots have a private constructor, `@ConsistentCopyVisibility` and exactly one public nested `Dto`.
- `NoRunBlockingArchitectureTest`: production code must not reference `kotlinx.coroutines.BuildersKt` (`launch`, `async`, `runBlocking`).
- The view classes in `rpg/screen/*.kt` are in a plain `screen` package and aren't constrained by layer rules. The `rpg/screen/battlefieldHud/` subpackage **is** layered (domain, usecases and adapters).

### 3.3 Battle composition and event flow

- `rpg/screen/BattleScene.kt` builds the whole graph in `sceneMain`. It creates an `InMemoryEventBus`, dispatched once per frame via `addUpdater { eventBus.dispatch() }`, then the `*Api` facades (Terrain, Unit, Player, Battlefield, Effect, Ability, BattleUnit, Battle, CpuBrain, BattleSetup), then the presenters and views in a `uiVerticalStack`: `BattleInfoView` (round and turn text), then `BattlefieldView`, then `BattleHudView` (390×300), then `PlayerCallToActionView`. Optional `seed`, `scenarioPath` and `debugEnabled` constructor parameters exist.
- `rpg/shared/adapters/events/InMemoryEventBus.kt` is a FIFO queue. `dispatch()` drains the queue, **including events published while draining**. A whole CPU turn therefore resolves in one dispatch, in one frame. **There are no animations or pacing today.**
- **Consequence for feedback (M9):** when a subscriber handles an event, the repository may already reflect *later* events. For example, a `BattleUnitDamaged` handler can see HP after a second hit. Presenters must not compute deltas by diffing repository state at handler time.

### 3.4 Domain capabilities relevant to the UI

| Concern | Where | Notes |
| --- | --- | --- |
| Battle unit state | `rpg/battleUnit/domain/BattleUnit.kt` | Immutable aggregate. `toDto()` exposes HP, MP, `remainingTurnActions(remainingSteps, remainingCasts)`, `abilityCooldowns: Map<abilityId, turnsLeft>` (ordered as the unit's ability list) and `ongoingEffects(onTurnStarted: List<id>, onDefeatedEffects: List<id>)`. **Turns left on an ongoing effect are not exposed in the DTO.** `canCastAbility(ability)` checks casts, cooldown **and mana**. |
| Queries (facade) | `rpg/battleUnit/adapters/presentation/BattleUnitApi.kt` | `searchBattleUnitById`, `canMoveTo`, `whereCanMove`, `whereCanCast`, `canCastAbility`, `searchBattleUnitsByPlayerId`, `hasAllBattleUnitsDefeated`, plus the `abilityExecution` service. |
| Movement | `rpg/battleUnit/usecases/queries/CanMoveTo.kt` (BFS shortest path through occupiable tiles), `rpg/screen/battlefieldHud/usecases/services/MovementService.kt` | Movement and casting are independent budgets (1 cast per turn, `movementRange` steps), usable in any order. |
| Targeting | `rpg/battleUnit/usecases/queries/WhereCanCast.kt` | Returns `List<CastGroup(positions)>` per `TargetingDto`: `ADJACENT_ENEMY`, `ALL_ADJACENT_ENEMIES` (one group containing all adjacent enemies), `SELF`, `VACANT_TILE_ADJACENT_TO_BATTLE_UNIT` and `VACANT_TILE_ADJACENT_TO_SELF`. Adjacency uses Manhattan distance ≤ 1. |
| Effect resolution | `rpg/battleUnit/usecases/services/AbilityExecution.kt` | **Read-only.** For one selected tile it validates targeting and returns `List<EffectApplicationDto>`. `OnAbilityCasted` (`rpg/battleUnit/adapters/events/OnAbilityCasted.kt`) calls `RequestEffectApplicationToCastTargets` **once per position in the cast group**. A preview must do the same. |
| Effect application | `rpg/battleUnit/usecases/commands/ApplyEffect.kt` | Handles `IMMEDIATELY` (damage, heal, teleport), `ON_TURN_STARTED` (stored with duration), `ON_DEFEATED` (stored) and tile `DEPLOY_BATTLE_UNIT`. `BEFORE_APPLYING_EFFECT` throws. In `BattleUnit.applyImmediateEffect`, `NEGATE_INCREASE_HEALTH`, `APPLY_EFFECT_ON_NEARBY_ALLIES` and `DEPLOY_BATTLE_UNIT` call `TODO()`. **A preview must never call domain methods for these.** |
| Death propagation | `rpg/battleUnit/usecases/commands/ApplyOnDefeatedEffectsToNearbyAllies.kt` | When a unit is defeated, each `ON_DEFEATED` effect of type `APPLY_EFFECT_ON_NEARBY_ALLIES` applies its inner effect to the defeated unit's same-player, non-defeated neighbours (Manhattan ≤ 1). Ordering detail: `skull` applies `low-physical-damage` and then `venom-on-death`. If the damage is lethal, `venom-on-death` is still received before `BattleUnitDefeated` is handled, so it **does** propagate on that same cast. |
| Terrain effects | `rpg/battlefield/usecases/commands/RequestEffectApplicationToOccupants.kt` | Applied to all occupants at **round start**, not on move. Terrain is configured in `resources/terrain/*.toml` (`canBeOccupied`, optional `effectId`). |
| Content catalog | `rpg/battlesetup/usecases/commands/SetupBattle.kt` | Hard-coded effects, abilities and units. Knight: 100 HP, 30 MP, move 3, abilities in this order: `poisoned-sword`, `mushroom`, `skull`, `teleport`, `bee`, `heal`. Rat: 20 HP, 10 MP, move 3, `sword` and `heal`. Bee is deployed by the `bee` ability. |
| Events | `rpg/battleUnit/domain/BattleUnitEvent.kt` | `BattleUnitDamaged(battleUnitId)` and `BattleUnitHealed(battleUnitId)` **carry no amount**. `BattleUnitMoved` carries from and to. `BattleUnitDefeated` carries its position. `BattleUnitTeleported` is emitted in addition to `BattleUnitMoved`. |
| CPU | `rpg/cpuBrain/usecases/commands/PlayTurn.kt` | Publishes the same `RequestMoveBattleUnit` and `RequestCastAbility` events as the player, so feedback for player and CPU can share one path. |

### 3.5 Current UI implementation

- **HUD state machine:** `rpg/screen/battlefieldHud/domain/BattlefieldHud.kt` has the states `Idle`, `DisplayMovementRange`, `DisplayAbilityCastRange` and `DisplayAbilityCastPreview`. Its events (`BattlefieldHudEvent.kt`) are `Idle`, `SelectedBattleUnit`, `SelectedBattleUnitAbility`, `AbilityDeselected`, `SelfAbilityCastPreviewed` and `EnemyAbilityCastPreviewed`. Use cases are in `rpg/screen/battlefieldHud/usecases/commands/`: `ProcessTileSelected`, `ProcessAbilitySelected`, `ConfirmCast`, `CancelCast`, `UpdateMovementRange` and `InitializeBattlefieldHud`. **This is the right seam for interaction logic.** It is unit-tested in `test/battlefieldHud/...`.
  - In `DisplayAbilityCastPreview`, every tile tap is ignored. You can only use Cancel or Confirm.
  - In `DisplayAbilityCastRange`, an invalid tap moves to `Idle`, which loses the selection (see Q3).
  - `ProcessAbilitySelected` silently returns when `canCastAbility` is false. There's no feedback.
  - Both allied **and enemy** units can be selected and show a movement range in the same style. Movement is only requested for HUMAN-owned units.
- **Presenter:** `rpg/screen/BattlefieldPresenter.kt` (263 lines) subscribes to domain and HUD events and drives every view. Unit sprites are chosen by `unitId` string (`knight`, `rat`, `bee`). The enemy preview shows only `abilityApi.calculateImmediateDamage` (immediate `DECREASE_HEALTH` sum), and there is a `TODO` noting that effect applications aren't previewed. `ALL_ADJACENT_ENEMIES` previews only the tapped enemy.
- **Battlefield view:** `rpg/screen/BattlefieldView.kt`. It is a fixed **384×384** clip viewport (`TILE_SIZE = 48`, `TILE_PIXEL_SIZE = 16`, `VISIBLE_TILES = 8`) holding a `UIGridFill` of transparent `UIButton`s named `row-<r>-column-<c>`. Each button holds images named `TERRAIN`, `BATTLE_UNIT` and `SELECTION`, all `scale = 3.0` with `smoothing = false`. Drag-scrolling uses `onMouseDrag` and an eased `moveTo`, which produces **fractional positions** and so breaks pixel alignment. Highlights are `battlefield/tile_selection_{2,3,4}.png`. There is no on-unit HP or status display.
- **HUD views:** `BattleHudView` swaps between `BattleUnitInfoView` (97.5 pt portrait, name, "Movements left / Remaining casts" text, HP and MP bars, `EffectsView` with up to 10 16 px icons, and 6 `AbilityButtonView`s of 48.75 pt) and `AttackPreviewView` (caster and receiver portrait, bars and effects). `AbilityButtonView` marks unavailable abilities with a darken filter only, which is a color-only cue. `BattleUnitInfoView` decides availability from casts and cooldown and **ignores mana**, which is inconsistent with `BattleUnit.canCastAbility`.
- **Colors:** none of these come from the palette today: `Colors.DIMGREY` (`rpg/shared/adapters/presentation/BarView.kt`), `Colors.WHITE`/`LIGHTSKYBLUE`/`LIGHTGRAY` (`UnitPortraitView.kt`), `RGBA(0,136,255)` (`BattleInfoView.kt`) and `#2b2b2b` (`Main.kt`).
- **Assets (all 16×16 unless noted):** `resources/unit/{knight,rat,bee}.png`, portraits 32×32, `resources/ability/*.png`, `resources/battlefield/tile_selection_{1..4}.png`, `resources/effect/{venom-damage,venom-on-death}.png`, and `resources/terrain/transitions/*.png` (256×16 Wang strips). `EffectView` (`rpg/effect/adapters/presentation/EffectView.kt`) throws for unknown effect ids.

### 3.6 Existing tooling to reuse (don't rebuild it)

| Tool | Where | Use it for |
| --- | --- | --- |
| Deterministic seed | `BattleScene(seed)`, shared `Random` | Reproducible CPU turns and IDs |
| `RecordingEventBus` | `rpg/shared/adapters/events/RecordingEventBus.kt` | Asserting event sequences, and asserting that **no** events are published (preview purity) |
| Scenarios | `resources/scenarios/*.json` with `BattleScenarioLoader` | Known positions. *Discrepancy:* the docs list seeds 7 and 1337 for `chain-showcase` and `terrain-mix`, but all three files contain `"seed": 42`. |
| `BattleUiScript` | `test/screen/BattleUiScript.kt` and `BattleUiScriptTest.kt` | Headless, synchronous input-level tests: `selectUnit`, `tapTile`, `selectAbility`, `confirmCast`, `cancelCast`, `finishTurn` and state queries |
| Snapshot harness | `test/screen/BattleSceneSnapshotHarness.kt`, `BattleSceneSnapshotTest.kt`, `BattleSceneSnapshotUpdateTest.kt`, goldens in `src/jvmTest/resources/snapshots/` | Pixel-exact regression at 390×844. There are 3 states today. It needs an OpenGL context, and `DISABLE_HEADLESS_TEST=true` skips it. |
| Palette checker and contact sheet | `test/assets/FamicubePaletteChecker.kt`, `PaletteCheckerTest.kt`, `AssetContactSheetTest.kt` | Enforcing Famicube on art. `loadFamicubePalette()` is reusable for UI color tokens. |
| Debug overlay | `rpg/screen/DebugSupport.kt`, `src/jvmMain/.../DebugOverlay*.kt` | F3 shows seed, round, selected unit and last events. R and N restart. |
| Object mothers | `test/*/domain/*Mother.kt`, `test/battlefieldHud/domain/BattlefieldHudMother.kt` | Test data |

---

## 4. Current-state gap analysis

Key: **E** = exists, **X** = extend, **M** = missing, **U** = unknown. Priority 1 is highest, following the task's order: correctness, clarity, touch, visuals, testability, cost.

| Req | Item | Status | Evidence / gap | Prio | Milestone |
| --- | --- | --- | --- | --- | --- |
| R3 | Select unit → range + HUD | E | `ProcessTileSelected` and `displayMovementRange` | – | – |
| R3 | Move keeps selection | E | `BattleUnitMoved` → `UpdateMovementRange` | – | – |
| R3 | Invalid-tap behavior while targeting | X | Drops to `Idle` (Q3) | 1 | M6 |
| R3 | Re-target while previewing | M | Taps ignored in `DisplayAbilityCastPreview` | 2 | M7 |
| R3 | Tap vs. drag disambiguation | U | `onMouseDrag` on the viewport and `onClick` on the tile buttons. Whether a drag also fires a tile click is unverified | 1 | M2 |
| R6 | Availability incl. mana | X | The view ignores mana and the use case returns silently | 1 | M5 |
| R6 | Cooldown readable without color | M | Darken filter only, no turns-left number | 2 | M5 |
| R8 | Preview of all effects and targets | M | Immediate damage on one target only (`CalculateImmediateDamage`) | 1 | M7 |
| R8 | Preview is side-effect free | X | `AbilityExecution` is read-only, but there's no preview query or purity test | 1 | M7 |
| R8/R9 | Conditional and over-time outcomes | M | Not modeled for the UI | 2 | M8 |
| R4 | On-unit HP and status | M | Only in the HUD panel | 2 | M3 |
| R4 | Status turns left | X | Not in `BattleUnit.Dto.OngoingEffectsDto` | 2 | M3 |
| R5 | Ally vs. enemy range distinction | X | Same overlay for both | 3 | M4 |
| R7 | Valid vs. invalid without color | X | Shapes of `tile_selection_*` aren't documented, and a "no valid targets" message is missing | 2 | M6 |
| R10 | Animation and pacing | M | Instant updates. The CPU turn resolves in one frame | 2 | M9 |
| R10 | Damage and heal amounts in events | X | Events carry no amount (section 3.4) | 2 | M9 |
| R2 | Battlefield-first layout | X | 384×384 field and a permanent 300 pt HUD with info rows | 2 | M2 |
| R11 | Pixel-perfect scrolling and scale | X | Integer 3× scale, but eased fractional scroll offsets. No `virtualSize` | 3 | M1/M2 |
| R1 | Palette-true UI colors | X | Non-palette `Colors.*` in views | 3 | M1 |
| R12 | Reusable UI tokens and components | X | Repeated fixed sizes and per-id `when` blocks for bitmaps | 4 | M1, ongoing |
| R13 | No combat log | E | None exists. Keep it that way | – | – |
| – | Snapshot coverage of new states | X | 3 states, default scenario only | 3 | M0 + each milestone |

---

## 5. Implementation principles and technical decisions

1. **The domain stays the rule authority.** The UI never re-implements targeting, movement or effect math. It asks queries (`WhereCanCast`, `MovementService`, and the new `PreviewAbilityCast`).
2. **Interaction logic lives in the `battlefieldHud` state machine** (domain plus use cases), and you test it there first. Views stay passive. They expose `display…` and `hide` methods and delegate callbacks, which is the current pattern.
3. **Presentation and resolution are decoupled.** Domain resolution stays instant and synchronous. Visual pacing (M9) is a presentation-side queue that consumes events. With zero durations (the test default) its behavior is identical to today's.
4. **Previews are pure queries.** `PreviewAbilityCast` lives in `rpg/battleUnit/usecases/queries/`. It reads repositories and may call immutable `BattleUnit` methods on **local copies**, but it never calls `repository.update`, never publishes events and never consumes `Random`. Tests prove this (section 7.3).
5. **Additive domain changes only**, and only where the UI can't work without them:
   - `BattleUnit.Dto.OngoingEffectsDto` gains turns-left data (M3).
   - `BattleUnitDamaged` and `BattleUnitHealed` gain `amount` and `remainingHealthPoints` (M9).
   - There are no rule changes.
6. **One pixel scale.** Introduce `PIXEL_SCALE = 3` (pt per art pixel), and derive `TILE_SIZE = TILE_PIXEL_SIZE * PIXEL_SCALE`. All pixel art is drawn with `smoothing = false` at integer scale. All pixel-art view positions are snapped to integer points, and battlefield scroll offsets are snapped to multiples of `PIXEL_SCALE`.
7. **Palette tokens.** All UI colors come from one Kotlin object of named Famicube colors (M1). A test asserts that every token is in `resources/famicube-palette.png`.
8. **Never use color alone.** Every state pairs color with a shape, pattern or glyph: a corner brackets marker for the selection, a dotted fill for movement, a solid border for a valid target, a crosshair for the preview, a number for cooldowns and a lock glyph for unavailable abilities.
9. **Touch targets are at least 44 pt.** Tiles are 48 pt at 3× and ability slots are at least 48 pt. Confirm, Cancel and End Turn are at least 44 pt tall.
10. **Procedural first, sprites later.** Draw highlights, bars, pips and outlines in code with palette colors. Request sprites in section 9.3, and don't block a milestone on art.
11. **No new dependencies.** Korge 6 UI, `korlibs.korge.tween`/`animate` and the existing test libraries are enough.
12. **Don't hard-code unit counts or ids in new code.** Map sprites from `unitId` and `effectId` through one registry (M1) with a safe fallback. Don't add `throw` for unknown ids.

---

## 6. Ordered milestones

| # | Milestone | Depends on | Main risk |
| --- | --- | --- | --- |
| M0 | Baseline, scenario and verification helpers | – | Snapshot env (OpenGL) |
| M1 | Visual foundations: palette tokens, pixel scale, sprite registry | M0 | Snapshot churn |
| M2 | Portrait battlefield-first layout and touch scrolling | M1 | Tap/drag conflicts |
| M3 | Unit selection, on-unit HP and status, contextual unit sheet | M2 | DTO change ripple |
| M4 | Movement range presentation and touch movement | M3 | – |
| M5 | Ability bar, availability and cooldowns | M3 | – |
| M6 | Target and cast-location highlighting and invalid-tap behavior | M5 | HUD state-machine edits |
| M7 | Side-effect-free consequence preview | M6 | Preview/outcome divergence |
| M8 | Status, propagation and conditional-outcome cues | M7 | Clutter |
| M9 | Combat feedback and paced state transitions | M3 (M7 recommended) | Test determinism, input during playback |
| M10 | Consistency, usability and regression pass | all | – |

Snapshots: every milestone that changes visuals **must** regenerate goldens with `./gradlew updateSnapshots`, inspect the PNGs by eye and mention this in its report. Don't regenerate goldens to hide an unexplained diff.

---

### M0 — Baseline, scenario and verification helpers

- **Goal:** start from a known-green baseline and give later milestones the fixtures they need.
- **User-visible result:** none. This milestone is tooling only.
- **Repository touchpoints:** `resources/scenarios/`, `test/screen/BattleSceneSnapshotHarness.kt`, `test/screen/BattleSceneSnapshotTest.kt`, `test/screen/BattleSceneSnapshotUpdateTest.kt`, `test/screen/BattleUiScriptTest.kt`, `test/shared/usecases/acceptance/` and `docs/design-tools/reference-battle-scenarios.md`.
- **Implementation tasks:**
  1. Run `./gradlew jvmTest` once and record the pass/fail counts in your report. If snapshot tests fail only because there's no display, rerun with `DISABLE_HEADLESS_TEST=true` and say so.
  2. Add `resources/scenarios/ui-showcase.json` (16×16 sand and grass, seed 42), with player-one knights **orthogonally adjacent** to player-two rats, and one pair of rats adjacent to each other. Suggested layout: knight at (6,6), knight at (8,6), rat A at (6,7), rat B at (6,8) next to rat A, and a rat C far away at (1,1). Add one lava tile and one water tile within 3 steps of a knight. This scenario makes targeting, preview and death propagation reachable without moving first.
  3. Make the snapshot harness accept an optional scenario. Add `createBattleScene(scenarioPath: String? = null)`, defaulting to the current behavior so the existing goldens don't change.
  4. Add a test helper `BattleStateFingerprint`, test-only, in `test/shared/usecases/acceptance/`. It captures every battle unit's `Dto` (via `searchBattleUnitsByPlayerId` for both players), every occupant position (`searchPosition`), `searchBattle()` and the HUD repository state, as one `data class` that is comparable with `isEqualTo`.
  5. Add `"scenarios/ui-showcase.json"` to the hard-coded scenario lists in `test/battlesetup/adapters/serialization/BattleScenarioLoaderTest.kt` (around line 13) and `test/shared/usecases/acceptance/BattleScenarioAcceptanceTest.kt` (around line 52). They don't discover files automatically. Add a row to `docs/design-tools/reference-battle-scenarios.md`, and fix the seed column there to match the files (all 42) or set the files to the documented seeds. Pick one and mention it in the report.
  6. Create `docs/design/ui-regression-checklist.md` with the manual checklist from section 7.6.
- **Dependencies:** none.
- **Verification:** `./gradlew jvmTest --tests "com.mkz.rpg.battlesetup.*" --tests "com.mkz.rpg.shared.usecases.acceptance.*" --tests "com.mkz.rpg.screen.BattleSceneSnapshotTest"`. Then `./gradlew runJvm -Pscenario=ui-showcase` and look at it.
- **Acceptance criteria:**
  - [ ] The baseline test result is recorded.
  - [ ] `ui-showcase.json` loads, and every unit is deployed at its position (acceptance test).
  - [ ] The existing 3 snapshot goldens still pass unchanged.
  - [ ] The fingerprint helper exists, and one test proves that two fingerprints of an untouched battle are equal.
- **Risks:** headless OpenGL may be unavailable on the agent's machine. Report this. Don't delete snapshot tests.

### M1 — Visual foundations: palette tokens, pixel scale, sprite registry

- **Goal:** one source for colors, scale and sprite lookup, with **no layout change**.
- **User-visible result:** UI colors shift to Famicube equivalents. Everything else looks the same.
- **Repository touchpoints:** `rpg/screen/BattlefieldView.kt` (constants and `addImage`), `rpg/screen/AbilityButtonView.kt`, `rpg/screen/UnitPortraitView.kt`, `rpg/screen/BattleInfoView.kt`, `rpg/shared/adapters/presentation/BarView.kt`, `PreviewBarView.kt`, `rpg/effect/adapters/presentation/EffectView.kt`, `rpg/Main.kt`, `test/assets/FamicubePaletteChecker.kt` and `PaletteCheckerTest.kt`.
- **Implementation tasks:**
  1. Create `rpg/shared/adapters/presentation/UiPalette.kt`: an `object UiPalette` with **semantic** tokens such as `background`, `panel`, `panelBorder`, `textPrimary`, `textMuted`, `hp`, `hpLoss`, `hpGain`, `mana`, `manaCost`, `ally`, `enemy`, `selection`, `move`, `castValid`, `castPreview`, `conditional` and `danger`. Each one is an `RGBA` picked from the Famicube palette. Open `resources/famicube-palette.png` or use `loadFamicubePalette()` in a scratch test to read the exact values.
  2. Create `rpg/shared/adapters/presentation/PixelScale.kt` with `const val PIXEL_SCALE = 3`, plus helpers `snap(v: Double): Double` (round to an integer point) and `snapToArtPixel(v: Double)` (round to a multiple of `PIXEL_SCALE`). Make `BattlefieldView.TILE_SIZE` derive from it.
  3. Create `rpg/screen/SpriteRegistry.kt`: it loads the unit, ability, effect and highlight bitmaps once (move the `loadAssets` bodies here) and has `unit(unitId)`, `ability(abilityId)` and `effect(effectId)` lookups that return a palette-colored placeholder bitmap for unknown ids instead of throwing. Inject it into the views that currently load their own bitmaps. Keep the public view methods unchanged.
  4. Replace every non-palette color listed in section 3.5 with `UiPalette` tokens. `Main.kt`'s background becomes `UiPalette.background`.
  5. Add `UiPaletteTest` in `test/assets/`: *should contain only Famicube colors when UI palette tokens are defined*. Use `loadFamicubePalette()` and reflect over the tokens, or keep an explicit `UiPalette.all` list.
  6. Write the conventions in `docs/design/ui-conventions.md`: scale, snapping, token names, the "never color alone" glyph table from section 5 item 8, and the 44 pt touch minimum. Keep it short; it's the brief's "small set of visual conventions".
  7. Run `./gradlew updateSnapshots`, inspect the goldens, and commit them.
- **Dependencies:** M0.
- **Verification:** `./gradlew jvmTest --tests "com.mkz.rpg.assets.*" --tests "com.mkz.rpg.screen.*"`, then `./gradlew lintKotlin`.
- **Acceptance criteria:**
  - [ ] `grep -rn "Colors\.\|RGBA(" src/commonMain/kotlin/com/mkz/rpg/screen src/commonMain/kotlin/com/mkz/rpg/shared/adapters/presentation` finds only `UiPalette.kt` (and `Colors.TRANSPARENT`, which is allowed).
  - [ ] `UiPaletteTest` passes.
  - [ ] No layout change: element bounds in the existing view tests are unchanged, and the snapshot diffs are color-only (state this after visual inspection).
  - [ ] An unknown `effectId` no longer throws (unit test on the registry).
- **Risks:** Famicube lacks exact greys, so pick the nearest. Text antialiasing produces off-palette pixels, which is acceptable under "hybrid UI". Don't palette-check the rendered snapshots.

### M2 — Portrait battlefield-first layout and touch scrolling

- **Goal:** the battlefield fills the screen. Contextual UI overlays the bottom only when needed. Scrolling is pixel-snapped, and drags never trigger taps.
- **User-visible result:** a full-width map about 13 rows tall. A slim top strip shows the round, whose turn it is and the End Turn button. A bottom sheet appears only while a unit is selected.
- **Repository touchpoints:** `rpg/screen/BattleScene.kt` (layout block), `BattlefieldView.kt` (viewport size, drag handling, new `centerOn(row, column)`), `BattleHudView.kt`, `PlayerCallToActionView.kt`, `FinishTurnView.kt`, `BattleInfoView.kt`, `rpg/Main.kt`, `test/screen/BattlefieldViewTest.kt` and `test/screen/BattleUiScript.kt`.
- **Implementation tasks:**
  1. Define layout constants in one place (`rpg/screen/BattleLayout.kt`): screen 390×844, top strip about 44 pt, bottom sheet height (measure what M3 and M5 need; target ≤ 260 pt) and safe-area padding of 0 for now (documented as a TODO for real devices).
  2. Make `BattlefieldView` take its viewport `Size` as a constructor parameter (default: the current 384×384, so existing tests compile). `BattleScene` passes the full width and the height between the top strip and the screen bottom. The sheet **overlays** the map; it doesn't shrink it.
  3. Change `BattleScene` from the vertical stack to absolute positioning: the battlefield at y = top strip, the HUD sheet anchored to the bottom (hidden in Idle), and the top strip on top. Move End Turn into the top strip, or into a bottom-right floating button when the sheet is hidden. Keep Confirm and Cancel in the sheet.
  4. Make drag scrolling pixel-exact. Replace the eased `moveTo` with a direct position set, snapped to `PIXEL_SCALE`, and clamp it to the map bounds.
  5. Separate taps from drags. Track drag distance, and if it exceeds a threshold (8 pt), suppress the tile `onClick` that ends the gesture. **First write a test** that reproduces the current behavior (simulate drag then up on a tile) so you know whether the bug exists. Record the result.
  6. Add `BattlefieldView.centerOn(row, column)`, snapped and clamped. Call it when the player's turn starts, on the first human unit.
  7. Make sure the sheet consumes touches, so taps on the sheet don't reach the tiles underneath. Test this with `simulateClick` on a sheet area above a tile.
  8. Update `BattleUiScript` if locators changed (End Turn button). Keep its public API stable.
  9. Update the goldens.
- **Dependencies:** M1.
- **Verification:**
  - `BattlefieldViewTest`: viewport size equals the injected size, the scroll offset is a multiple of `PIXEL_SCALE` after a drag, and a drag beyond the threshold doesn't call `delegate.tileSelected`.
  - `BattleUiScriptTest` stays green.
  - Snapshots `battle-initial-layout`, `battle-unit-selected` and `battle-ability-selected` are updated and inspected.
  - Manual: `./gradlew runJvm` and the JS build in a browser at 390×844 with touch emulation. Scroll, then tap.
- **Acceptance criteria:**
  - [ ] In Idle, the battlefield covers ≥ 85% of the 844 pt height.
  - [ ] No permanent panel besides the top strip.
  - [ ] Scroll offsets are always integer multiples of `PIXEL_SCALE` (asserted).
  - [ ] A drag does not select a tile (asserted).
  - [ ] All buttons are ≥ 44 pt in both dimensions (asserted on view bounds).
- **Risks:** Korge `UIButton` click semantics during drag (task 5). `UIGridFill` with 256 buttons is fine, but don't add per-frame work per tile.

### M3 — Unit selection, on-unit HP and status, contextual unit sheet

- **Goal:** the player can see at a glance which unit is selected, every unit's HP, and its key statuses. Details appear only on selection.
- **User-visible result:**
  - A corner bracket marker on the selected unit.
  - A thin pixel HP bar under every unit, with ally and enemy edge colors plus a shape difference: allies get a flat bar and enemies a notched bar.
  - Up to 2 status pips above each unit, plus a "+" when there are more.
  - The bottom sheet shows the portrait, name, HP and MP numbers, move and cast pips (filled or hollow), and the status list with turns left.
- **Repository touchpoints:** `rpg/battleUnit/domain/BattleUnit.kt` (`OngoingEffects.toDto`, `Dto.OngoingEffectsDto`), `test/battleUnit/domain/BattleUnitTest.kt`, `BattleUnitMother.kt`, `rpg/screen/BattlefieldView.kt`, `BattlefieldPresenter.kt`, `BattleUnitInfoView.kt`, `EffectsView.kt` and `rpg/effect/adapters/presentation/EffectView.kt`.
- **Implementation tasks:**
  1. **Domain (additive).** Add `onTurnStartedTurnsLeft: Map<String, Int>` to `OngoingEffectsDto`, keeping the existing lists for compatibility. Fill it from `ApplicationStatus.OnTurnStarted.turnsLeft`. If the same effect id is stacked, record the max and document that choice in a KDoc line. Add `BattleUnitTest` cases.
  2. Create a `UnitOverlayView` (one per deployed unit, a child of the tile button next to the `BATTLE_UNIT` image). It holds a procedural HP bar (width 14 art px, height 2 art px, scaled ×`PIXEL_SCALE`) and a status-pip row (max 2 visible, then "+"). Add an ally/enemy edge marker that doesn't rely on color.
  3. The presenter refreshes a unit's overlay on `BattleUnitDamaged`, `BattleUnitHealed`, `EffectReceived`, `BattleUnitDeployed`, `BattleUnitMoved` (the overlay moves with the unit) and at the start of each turn. It reads the current DTO; exact amounts arrive in M9.
  4. Selection marker: draw a procedural corner bracket on the selected tile in `displayMovementRange`, replacing `tile_selection_4` for the "selected unit" meaning. Ally and enemy selections share the bracket shape with different tokens, and an enemy also shows an "eye" or inspect glyph (Q2).
  5. Restyle `BattleUnitInfoView` as the compact sheet. Replace the "Movements left: … Remaining casts: …" text with pips. Add a status list where each row shows an icon, a short name and turns left. Hide the ability bar for units not owned by the current human player (Q2), and show their abilities as read-only icons instead.
- **Dependencies:** M2.
- **Verification:**
  - `BattleUnitTest`: turns left are exposed and decrease after `applyOnTurnStartedEffect`.
  - `BattlefieldPresenterTest`: after a damage event the overlay HP fraction equals `remaining / max`, and selecting an enemy hides the ability buttons.
  - `BattleUiScriptTest`, new case: *should show inspect info without ability bar when player taps an enemy unit*.
  - Add snapshot state `battle-enemy-inspected` (scenario `ui-showcase`).
- **Acceptance criteria:**
  - [ ] Every unit on the map shows an HP bar. HP is readable in the M2 layout without selecting the unit.
  - [ ] Status pips are capped at 2 plus "+" (asserted with a unit carrying 3 effects).
  - [ ] Turns left are shown for `ON_TURN_STARTED` effects, and `ON_DEFEATED` effects show a distinct "on death" glyph.
  - [ ] Enemy selection never exposes castable ability buttons.
  - [ ] All architecture tests pass.
- **Risks:** changing `BattleUnit.Dto` ripples into tests and mothers. Keep the change additive. Overlay visibility must not hide terrain hazards; keep overlays inside the tile's top and bottom 3 art px.

### M4 — Movement range presentation and touch movement

- **Goal:** reachable tiles are unambiguous, and ally ranges differ from enemy (inspect) ranges.
- **User-visible result:**
  - An ally's reachable tiles get a dotted fill pattern, and an enemy's inspected range gets a hollow outline.
  - Reachable hazardous tiles (terrain with an `effectId`) carry a small hazard glyph (Q4).
  - Tapping a reachable tile moves the unit and keeps it selected. Tapping outside the range deselects.
- **Repository touchpoints:** `rpg/screen/BattlefieldView.kt` (`displayPotentialMovement`), `BattlefieldPresenter.kt` (`displayMovementRange`), `rpg/screen/battlefieldHud/usecases/commands/ProcessTileSelected.kt`, `test/battlefieldHud/usecases/commands/ProcessTileSelectedTest.kt`, `rpg/terrain/adapters/presentation/TerrainApi.kt` (for `searchTerrainById`) and `rpg/battlefield/adapters/presentation/BattlefieldApi.kt`.
- **Implementation tasks:**
  1. Add `displayPotentialMovement(row, column, style: MovementStyle { ALLY, INSPECT }, hazard: Boolean)`. Draw both styles procedurally from `UiPalette`.
  2. In the presenter, choose the style by comparing the unit's `playerId` with the current human player and `battle.currentPlayerTurn`. Compute `hazard` from the terrain lookup (`battlefieldApi.searchBattlefield()` tiles → terrain `effectId != null`).
  3. When an ally has `remainingSteps == 0`, show no range and show an "already moved" pip state.
  4. Keep the existing `ProcessTileSelected` semantics. Add tests for: a tap on a reachable tile publishes `RequestMoveBattleUnit`; a tap on an unreachable empty tile goes to Idle; a tap on another ally switches selection.
- **Dependencies:** M3.
- **Verification:**
  - Unit tests above.
  - `BattleUiScriptTest`: *should highlight exactly the tiles returned by whereCanMove when player selects a knight*. Compare the set of tiles with a `SELECTION` child of style ALLY against `MovementService.tilesWhereCanMove`.
  - Add snapshot `battle-movement-range` on `ui-showcase`, showing a hazard glyph.
- **Acceptance criteria:**
  - [ ] Highlighted tiles equal the domain's reachable set (asserted).
  - [ ] The ally and inspect styles differ in shape, not only color.
  - [ ] A move keeps the unit selected and refreshes the range with the remaining steps (asserted).
- **Risks:** low.

### M5 — Ability bar, availability and cooldowns

- **Goal:** the six ability slots communicate ready, cooldown, no mana, no casts left and selected, without relying on color. Tapping an unavailable ability explains why.
- **User-visible result:**
  - Ready abilities are full-brightness icons.
  - An ability on cooldown is dimmed and shows a large pixel number of turns left.
  - Not enough mana: dimmed, with a mana-drop glyph and the cost in `danger`.
  - No casts left: all slots dimmed, with a lock glyph.
  - The selected ability gets a thick frame.
  - Under the bar, one line shows the selected ability's name, cost and a generated effect summary (Q7).
- **Repository touchpoints:** `rpg/battleUnit/usecases/queries/` (new query), `rpg/battleUnit/adapters/presentation/BattleUnitApi.kt`, `rpg/screen/battlefieldHud/usecases/commands/ProcessAbilitySelected.kt`, `rpg/screen/battlefieldHud/domain/BattlefieldHudEvent.kt` and `BattlefieldHud.kt`, `rpg/screen/AbilityButtonView.kt`, `BattleUnitInfoView.kt`, `BattlefieldPresenter.kt`, `test/battleUnit/usecases/queries/` and `test/battlefieldHud/...`.
- **Implementation tasks:**
  1. Add the query `SearchAbilityAvailability` in `rpg/battleUnit/usecases/queries/`. `invoke(battleUnitId): List<AbilityAvailability>` (nested data class: `abilityId`, `name`, `cost`, `cooldownTurnsLeft`, `status: READY | COOLDOWN | NOT_ENOUGH_MANA | NO_CASTS_LEFT`), in the unit's ability order. Use the precedence NO_CASTS_LEFT, then COOLDOWN, then NOT_ENOUGH_MANA. It must agree with `BattleUnit.canCastAbility`; add a test for each status. Expose it on `BattleUnitApi`.
  2. `BattleUnitInfoView.display` takes `List<AbilityAvailability>` instead of computing availability itself, which fixes the mana inconsistency.
  3. Add `AbilityButtonView.display(availability)`, which draws the status glyph and number procedurally. Keep the 48 pt minimum size.
  4. HUD: when `ProcessAbilitySelected` receives an unavailable ability, publish a new HUD event `AbilityUnavailable(abilityId, reason)` instead of returning silently. The state doesn't change. The presenter shows the reason in the sheet's text line for about 1.5 s, or until the next interaction. Keep it short: "On cooldown (2)", "Needs 10 MP", "Already acted".
  5. Write a summary generator (presenter-side helper) from `Ability.Dto.effectSpecs` and `EffectApi.searchEffectById`: `DECREASE_HEALTH` → `N dmg`, `INCREASE_HEALTH` → `+N HP`, `ON_TURN_STARTED` → `… /turn ×D`, `ON_DEFEATED`/`APPLY_EFFECT_ON_NEARBY_ALLIES` → `On death: spreads <inner effect>`, `TELEPORT` → `Teleport`, `DEPLOY_BATTLE_UNIT` → `Summon <unit>`, and anything else → `?`. Add a unit test for each knight ability.
- **Dependencies:** M3.
- **Verification:**
  - Query tests for all 4 statuses. `ProcessAbilitySelectedTest` checks that an unavailable ability publishes `AbilityUnavailable` and leaves the state unchanged.
  - `BattleUiScriptTest`: after casting `poisoned-sword` (cooldown 1), reselecting the knight shows slot 0 as COOLDOWN with the number 1, and tapping it doesn't enter cast range.
  - Add snapshot `battle-ability-cooldowns`.
- **Acceptance criteria:**
  - [ ] Each of the 4 non-ready statuses has a distinct glyph or number, verified in a view test without inspecting colors.
  - [ ] The view's availability equals `canCastAbility` for every slot (asserted across the knight's 6 abilities in at least 2 states).
  - [ ] Tapping an unavailable ability gives a visible reason and doesn't change the HUD state.
- **Risks:** the M3 sheet layout may need to shrink portrait or text to fit the summary line. Keep the sheet ≤ 260 pt.

### M6 — Target and cast-location highlighting and invalid-tap behavior

- **Goal:** valid cast locations are obvious, multi-target groups read as one choice, invalid taps are non-destructive and "no valid target" is explained.
- **User-visible result:**
  - Valid tiles get a solid bracket border, plus a target glyph on enemies or an empty-tile glyph on vacant targets.
  - An `ALL_ADJACENT_ENEMIES` group gets a connected outline.
  - The rest of the map is dimmed slightly with a palette-dark overlay pattern, so valid versus invalid doesn't rely on color.
  - With zero valid targets, the sheet says "No valid target in reach".
  - Tapping an invalid tile returns to the unit-selected state (Q3).
- **Repository touchpoints:** `rpg/screen/battlefieldHud/domain/BattlefieldHud.kt` (`DisplayAbilityCastRange`), `rpg/screen/battlefieldHud/usecases/commands/ProcessTileSelected.kt`, `BattlefieldHudEvent.kt`, `rpg/screen/BattlefieldView.kt` (`displayPotentialCast`) and `BattlefieldPresenter.kt` (`displayAbilitySelected`). The tests are the matching files in `test/battlefieldHud/` and `test/screen/battlefieldHud/domain/BattlefieldHudTest.kt`.
- **Implementation tasks:**
  1. HUD domain: in `DisplayAbilityCastRange`, handle a tap outside every cast group by calling `deselectAbility()`, which already exists and returns to `DisplayMovementRange` while emitting `AbilityDeselected` and `SelectedBattleUnit`, instead of `idle()`. A tap on **another allied unit** still switches selection. Update `ProcessTileSelectedTest` accordingly.
  2. Change `displayPotentialCast(row, column, kind: TARGET_UNIT | TARGET_TILE, groupEdges)`, and add `dimOutside(validTiles)` and `clearDim()`.
  3. Presenter: when `castGroupsWhereCanCast` is empty, show the "No valid target in reach" text line and keep the ability selected so the player can deselect it.
  4. Remove the duplicated `AbilityDeselected` subscription in `BattlefieldPresenter` (registered twice).
- **Dependencies:** M5.
- **Verification:**
  - `ProcessTileSelectedTest`: *should return to unit selection when player taps a tile outside every cast group*.
  - `BattleUiScriptTest`: the tiles highlighted as cast targets equal `castTargets(...)`. With `teleport` selected, the vacant tiles adjacent to other units are highlighted with the TILE kind.
  - Add snapshot `battle-cast-targets` (on `ui-showcase` with `mushroom` selected, showing the group outline).
- **Acceptance criteria:**
  - [ ] Highlighted cast tiles equal the domain's `whereCanCast` positions (asserted).
  - [ ] An invalid tap never loses the unit selection (asserted).
  - [ ] The empty-target message is shown (asserted on the view text).
  - [ ] Valid and invalid tiles are distinguishable in a grayscale render of the snapshot. Check this manually: desaturate the golden and look.
- **Risks:** HUD state-machine regressions. Run all `battlefieldHud` tests and `BattleUiScriptTest`.

### M7 — Side-effect-free consequence preview

- **Goal:** after tapping a valid target, the player sees every affected unit or tile and every predictable change before confirming. Computing the preview never changes game state.
- **User-visible result:**
  - The affected tiles get a crosshair.
  - Each affected unit's on-map HP bar shows a ghost segment for the predicted loss or gain.
  - A "skull" glyph marks predicted lethal hits.
  - Statuses to be applied appear as pips with a "+" badge.
  - The sheet lists: caster MP before and after, the cooldown the ability will set, and per-target lines (`Rat: 20 → 10 HP, +Venom ×5`).
  - Tapping another valid target switches the preview. Confirm and Cancel stay at ≥ 44 pt.
- **Repository touchpoints:** a new `rpg/battleUnit/usecases/queries/PreviewAbilityCast.kt`, `rpg/battleUnit/usecases/services/AbilityExecution.kt` (read, don't change behavior), `rpg/battleUnit/domain/BattleUnit.kt` (read), `rpg/battleUnit/adapters/presentation/BattleUnitApi.kt`, `rpg/screen/battlefieldHud/domain/BattlefieldHud.kt` and `BattlefieldHudEvent.kt`, `ProcessTileSelected.kt`, `rpg/screen/BattlefieldPresenter.kt`, `AttackPreviewView.kt` (replace or slim down) and `rpg/ability/usecases/queries/CalculateImmediateDamage.kt` (it becomes unused by the presenter; leave it in place).
- **Implementation tasks:**
  1. **Query `PreviewAbilityCast`** (in `usecases/queries`, depending only on usecases and domain). `invoke(casterId, abilityId, castGroup: List<PositionDto>): AbilityCastPreview`, where the nested data classes are:
     - `AbilityCastPreview(casterId, manaBefore, manaAfter, cooldownAfter, targets: List<TargetPreview>, tiles: List<TilePreview>, unsupported: List<String>)`
     - `TargetPreview(battleUnitId, hpBefore, hpAfter, maxHp, isLethal, appliedEffects: List<AppliedEffectPreview>, teleportTo: PositionDto?)`
     - `AppliedEffectPreview(effectId, timing: IMMEDIATE | OVER_TIME | ON_DEATH, perTurn: Int?, turns: Int?)`
     - `TilePreview(row, column, deploysUnitId: String?)`

     Algorithm:
     1. For **each** position in `castGroup`, call `AbilityExecution(...)`, mirroring `OnAbilityCasted`.
     2. For each `EffectApplicationDto`, look up the effect. For unit targets, keep a **local** `MutableMap<id, BattleUnit>` of working copies, loaded from the repository with `searchById`.
     3. `IMMEDIATELY` with `DECREASE_HEALTH`, `INCREASE_HEALTH` or `TELEPORT`: call `copy.receiveImmediateEffect(id).applyImmediateEffect(effect, unit, row, col)` on the copy and store the result **only in the local map**.
     4. `ON_TURN_STARTED`: record OVER_TIME with `perTurn` (from `decreaseHealth`) and `turns = duration`. Don't simulate future turns.
     5. `ON_DEFEATED`: record ON_DEATH.
     6. Tile targets with `DEPLOY_BATTLE_UNIT`: record a `TilePreview`.
     7. Any other combination: add to `unsupported` and **don't call the domain** (avoids `TODO()`).
     8. Compute `isLethal` from the working copy's `isDefeated()`, and `hpAfter` from the copy's `toDto()`.
     9. Mana and cooldown come from `Ability.Dto.cost` and `cooldown`.

     The query must not take `EventBus`, `Random` or any command as a dependency.
  2. Expose the query as `BattleUnitApi.previewAbilityCast`.
  3. **HUD:**
     - Merge `SelfAbilityCastPreviewed` and `EnemyAbilityCastPreviewed` into `AbilityCastPreviewed(casterBattleUnitId, abilityId, castGroup)`. Keep `enemyBattleUnitId` out; the preview query determines targets.
     - In `DisplayAbilityCastPreview`, a tap on a tile inside another valid cast group switches the preview (new transition `previewAbilityCast(castGroup)`). A tap on the same group does nothing. Any other tap does nothing (Confirm and Cancel remain explicit).
     - Update `ProcessTileSelected`, `BattlefieldHud` and their tests. `ConfirmCast` is unchanged.
  4. **Presenter and views:** on `AbilityCastPreviewed`, call the query and render the tile crosshairs, HP ghost segments on the `UnitOverlayView`s, pending-status pips and the sheet lines. Clear all preview marks on `Idle`, `AbilityDeselected`, a new preview, or a cast. Show `unsupported` entries as "?" lines; never hide them.
- **Dependencies:** M6 (cast groups), M3 (overlays).
- **Verification:**
  - `PreviewAbilityCastTest` (unit, with in-memory repositories and mothers): sword → −10; skull → −10 plus ON_DEATH venom-on-death; poisoned-sword → OVER_TIME 3×5; heal capped at max HP; teleport → `teleportTo`; bee → `TilePreview`; mushroom with 2 adjacent enemies → 2 targets; an unsupported effect type is listed and doesn't throw.
  - **Purity test** (acceptance, `test/shared/usecases/acceptance/AbilityPreviewPurityAcceptanceTest.kt`): on `ui-showcase`, for every knight ability and every cast group from `whereCanCast`, take `BattleStateFingerprint` before, call the preview, then assert the fingerprint is unchanged **and** that `RecordingEventBus.events` is empty after `dispatch()`.
  - **Parity test** (acceptance, `AbilityPreviewParityAcceptanceTest.kt`): for each knight ability and its first cast group, preview, then publish `RequestCastAbility` and `dispatch()`. Assert each target's actual HP equals `hpAfter`, that `isLethal` matches defeat, and that OVER_TIME and ON_DEATH effects appear in the target's `ongoingEffects`. Build a fresh graph per ability for independence.
  - `BattleUiScriptTest`: preview, then tap another target, and the preview switches. Cancel leaves the fingerprint identical to before selection.
  - Add snapshot `battle-cast-preview` (`ui-showcase`, skull on rat A).
- **Acceptance criteria:**
  - [ ] Purity test passes for all abilities and cast groups.
  - [ ] Parity test passes for all six knight abilities.
  - [ ] Multi-target preview lists every target.
  - [ ] No preview code path calls `repository.update` or `eventBus.publish`. A test asserts this, and a review of `PreviewAbilityCast`'s constructor shows no `EventBus` or `Random`.
  - [ ] Architecture tests pass.
- **Risks:** preview and real resolution can diverge as rules evolve; the parity test is the guard. Don't skip or weaken it. Stacked same-id effects have unclear semantics (see M3 task 1).

### M8 — Status, propagation and conditional-outcome cues

- **Goal:** the player understands *what will spread, to whom, and under which condition*, and certain outcomes look different from conditional ones.
- **User-visible result:**
  - Units carrying an ON_DEATH spreading effect show the "on death" pip on the map (from M3).
  - In a preview, if a target is predicted lethal and has, or receives in this cast, an `APPLY_EFFECT_ON_NEARBY_ALLIES` effect, its neighbouring allies get a **dashed** "conditional" outline and a pending-status pip. A short connector or arrow is drawn from the target to each neighbour, so the battlefield itself shows the spread.
  - If the target is not predicted lethal, the same neighbours get a fainter dashed outline, and the sheet line reads "If defeated: spreads Venom to 1 ally".
  - Certain effects use solid styles and conditional effects use dashed styles. Over-time effects show "×N" on their pip.
  - Synergy hint: while an ability is selected, enemies on which the ability would be lethal right now get a small skull glyph on their cast highlight (computed by calling `PreviewAbilityCast` per cast group, which is read-only and cheap at this scale).
- **Repository touchpoints:** `PreviewAbilityCast.kt` (extend), `rpg/battleUnit/usecases/commands/ApplyOnDefeatedEffectsToNearbyAllies.kt` (read, to mirror the neighbour rule), `BattlefieldPresenter.kt`, `BattlefieldView.kt`, `UnitOverlayView` and the sheet view.
- **Implementation tasks:**
  1. Extend `AbilityCastPreview` with `triggered: List<TriggeredPreview(sourceBattleUnitId, condition: IF_DEFEATED | ON_LETHAL_HIT, affectedBattleUnitIds, effectId)>`. Compute neighbours with the **same rule** as `ApplyOnDefeatedEffectsToNearbyAllies`: same player as the defeated unit, not defeated, Manhattan ≤ 1, excluding self. Consider the target's existing `onDefeatedEffects` plus ON_DEATH effects received in this preview (this is the skull ordering in section 3.4). Model one level only, with no recursive chains; mark "chain may continue" when an affected neighbour is itself predicted lethal (which is impossible for venom over time, but keep the flag generic).
  2. Render conditional versus certain styles (section 5 item 8). Keep at most one connector per pair. Don't animate the connectors here; M9 handles motion.
  3. Add lethal hints on the cast highlight (M6 `displayPotentialCast` gains `lethal: Boolean`).
  4. Make sure the sheet text stays at ≤ 4 lines. If there are more targets, show "+N more", which is tappable to expand. Anti-clutter rule from R13.
- **Dependencies:** M7.
- **Verification:**
  - `PreviewAbilityCastTest`: skull on a 10 HP rat next to another rat gives `triggered`, with the neighbour listed and condition ON_LETHAL_HIT. Skull on a 20 HP rat gives IF_DEFEATED. An enemy neighbour of a different player is excluded.
  - Parity extension: after confirming the lethal skull, the neighbour's `ongoingEffects.onTurnStarted` contains `venom-damage`. Set the precondition by publishing `RequestApplyEffect(low-physical-damage)` to rat A before the test.
  - Add snapshot `battle-propagation-preview` on `ui-showcase`.
- **Acceptance criteria:**
  - [ ] Triggered predictions match the real outcome in the parity test.
  - [ ] Conditional and certain cues differ by line style or pattern, not only color.
  - [ ] Sheet text never exceeds 4 lines (view test with a 3-target mushroom preview).
- **Risks:** clutter. Follow the brief's guiding rule. If a cue doesn't clarify a decision, drop it and say so in the report.

### M9 — Combat feedback and paced state transitions

- **Goal:** state changes are *seen*, for the player's and the CPU's actions alike, through short authored pixel animations, without slowing tests or changing rules.
- **User-visible result:**
  - Units slide tile by tile when moving, and teleports use a blink.
  - Hits flash the unit sprite (palette white, 2 frames) and pop a pixel damage or heal number.
  - Statuses arrive with a pip pop.
  - Defeated units play a poof and are then removed.
  - Death spread animates a travelling spark along the M8 connector.
  - The CPU turn plays back step by step, and the camera follows the acting unit.
  - Input is ignored while playback runs, shown by a subtle "CPU" or "…" indicator in the top strip.
- **Repository touchpoints:** `rpg/battleUnit/domain/BattleUnitEvent.kt` and `BattleUnit.kt` (amounts), `test/battleUnit/domain/BattleUnitTest.kt`, all subscribers of `BattleUnitDamaged` and `BattleUnitHealed` (`grep -rn "BattleUnitDamaged\|BattleUnitHealed" src`), `rpg/screen/BattlefieldPresenter.kt`, `BattlefieldView.kt`, `BattleScene.kt`, and a new `rpg/screen/feedback/` package.
- **Implementation tasks:**
  1. **Domain (additive):** add `amount: Int` and `remainingHealthPoints: Int` to `BattleUnitDamaged` and `BattleUnitHealed`, filled in `BattleUnit.applyDecreaseHealthEffect` and the `INCREASE_HEALTH` branch (amount = the actual delta after clamping). Update tests and mothers. No behavior change.
  2. Create `rpg/screen/feedback/FeedbackQueue.kt`: an ordered queue of `FeedbackBeat` items (sealed: `Move(from, to)`, `Teleport`, `Hit(unitId, amount, remaining)`, `Heal`, `StatusApplied`, `Defeated`, `Spread(from, to)`, `Deployed`, `CameraFocus`). It takes an injectable `FeedbackTiming` (durations). `FeedbackTiming.Instant` is all zeros and is the **default in tests and in `BattleUiScript`**.
  3. **Step A (no visual change):** route every *view-mutating* domain-event handler in `BattlefieldPresenter` (deploy, move, remove, overlay refresh) through the queue with `Instant` timing. All existing tests and goldens must stay green and unchanged. Commit this step separately.
  4. **Step B:** implement the animations with Korge `animate`/tween on the battlefield views, keeping positions snapped (section 5 item 6). Real timing goes into `BattleScene` only. Keep each beat at ≤ 300 ms and each CPU turn at ≤ about 3 s total, by merging simultaneous beats.
  5. **Input gating:** while the queue is non-empty, `BattlefieldView` and `AbilityButtonView` delegates ignore taps (with a single gate check in the presenter). End Turn is hidden. Add tests.
  6. Don't use `launch`, `async` or `runBlocking`. Drive playback from the frame updater (`addUpdater`) or Korge's animator, as `DebugOverlaySupport` does.
- **Dependencies:** M3 (overlays). M8 is recommended so `Spread` has connectors.
- **Verification:**
  - `BattleUnitTest`: amounts are correct, including heal clamping.
  - `FeedbackQueueTest`: beats are enqueued in event order for a scripted CPU turn (`RecordingEventBus` plus seed 42), and the queue drains instantly with `Instant`.
  - `BattleUiScriptTest` is fully green with no changes to its assertions.
  - The input-gating test passes.
  - Snapshot goldens are unchanged after Step A. After Step B, add a snapshot captured **after** playback settles. Don't snapshot mid-animation.
  - Manual: watch a full CPU turn on `ui-showcase` and `chain-showcase`. Record whether every CPU action was visible.
- **Acceptance criteria:**
  - [ ] Step A is merged with zero golden diffs.
  - [ ] All the beats listed under *User-visible result* are implemented and observable.
  - [ ] Tests run with instant timing, and the `jvmTest` duration is not noticeably increased (report before and after).
  - [ ] No taps are processed during playback (asserted).
  - [ ] `NoRunBlockingArchitectureTest` passes.
- **Risks:**
  - View state drifting from domain state if a beat is dropped. Add a "resync" at the end of each queue drain that redraws every unit from DTOs.
  - Long CPU turns. Merge beats.

### M10 — Consistency, usability and regression pass

- **Goal:** make the UI coherent, audit it against the brief and lock it with tests.
- **User-visible result:** polish only.
- **Repository touchpoints:** all views, `docs/design/ui-conventions.md`, `docs/design/ui-regression-checklist.md`, the snapshot tests and the asset checker.
- **Implementation tasks:**
  1. Audit every element against the guiding rule (brief §8). List removed or kept elements in the report.
  2. Add a touch-target audit test: walk the stage after selecting a unit, an ability and a preview, and assert every clickable `UIButton` is ≥ 44×44 pt.
  3. Add a color-token audit: the grep from M1 still returns only `UiPalette.kt`.
  4. Make sure every new sprite asset is listed in `GAME_ART_ASSET_PATHS` and passes the palette check. Regenerate the contact sheet (`./gradlew generateContactSheet`) and inspect it.
  5. Snapshot matrix: `initial`, `unit-selected`, `enemy-inspected`, `movement-range`, `ability-cooldowns`, `cast-targets`, `cast-preview`, `propagation-preview` and `post-cpu-turn`.
  6. Update `docs/design-tools/reference-visual-snapshot-harness.md` and `reference-battle-ui-script.md` for any new states or helpers.
- **Dependencies:** M0–M9.
- **Verification:** `./gradlew clean jvmTest`, `./gradlew lintKotlin`, the manual checklist in section 7.6 on JVM and on JS at 390×844.
- **Acceptance criteria:**
  - [ ] Full suite green.
  - [ ] Touch and color audits pass.
  - [ ] The checklist is fully ticked, with notes.
  - [ ] Docs are updated.
- **Risks:** scope creep. Stop at audit fixes; new features need a new plan entry.

---

## 7. Testing and visual-verification strategy

### 7.1 Test pyramid for this work

| Level | Tool | Used for |
| --- | --- | --- |
| Domain unit | JUnit and AssertJ, mothers, `FakeEventBus` | DTO additions (M3, M9), `BattlefieldHud` transitions |
| Use-case unit | Same | `SearchAbilityAvailability`, `PreviewAbilityCast`, `ProcessTileSelected`, `ProcessAbilitySelected` |
| Acceptance | Real `*Api` graph, `InMemoryEventBus` or `RecordingEventBus`, seed 42, scenarios | Preview purity and parity, propagation, movement and target sets |
| Input-level | `BattleUiScript` in `ViewsForTesting` (`simulateClick`) | Touch flows end to end, without a renderer |
| Visual | `BattleSceneSnapshotTest` goldens at 390×844 | Layout and pixel regressions |
| Asset | `PaletteCheckerTest`, `UiPaletteTest`, contact sheet | Palette fidelity |
| Manual | `runJvm -Pscenario=…`, JS in a browser at 390×844 with touch emulation, F3 debug overlay | Feel, readability, animation |

### 7.2 Determinism

Always pass `seed = 42`, or use a scenario. Never call `Random.Default` in new code; if randomness is needed, inject the shared `Random` as `BattleUnitApi` does. Feedback timing in tests is `FeedbackTiming.Instant`.

### 7.3 Preview must not mutate state (mandatory from M7)

Use `BattleStateFingerprint` (M0) and `RecordingEventBus`:

```kotlin
// Given
val before = fingerprint()
recordingEventBus.clear()
// When
battleUnitApi.previewAbilityCast(casterId, abilityId, castGroup)
recordingEventBus.dispatch()
// Then
assertThat(fingerprint()).isEqualTo(before)
assertThat(recordingEventBus.events).isEmpty()
```

### 7.4 Rules coverage the UI depends on

- Movement range: highlighted set equals `MovementService.tilesWhereCanMove` (M4).
- Target validity: highlighted set equals `whereCanCast` (M6).
- Status propagation and death triggers: parity tests (M7, M8) build on the existing `SkullAbilityAcceptanceTest` and `MushroomAbilityAcceptanceTest` in `test/shared/usecases/acceptance/`. Read them for setup patterns.

### 7.5 Snapshot discipline

- Add one snapshot state per milestone, named `battle-<state>`. Add the state to **both** `BattleSceneSnapshotTest` and `BattleSceneSnapshotUpdateTest`, and drive it with harness helpers that wait on observable view conditions (`awaitUntil`), never on fixed delays alone.
- After `./gradlew updateSnapshots`, open every changed PNG in `src/jvmTest/resources/snapshots/` and describe the change in your report. On a failure, the diff images are in `build/reports/snapshots/`.

### 7.6 Manual checklist (copy into `docs/design/ui-regression-checklist.md` in M0)

Run on `./gradlew runJvm -Pscenario=ui-showcase`, and for M2, M9 and M10 also on the JS build in a browser at 390×844 with touch emulation:

1. Can I tell which unit is selected within 1 s?
2. Can I tell where it can move, and do ally and enemy ranges look different?
3. Can I tell which abilities are ready, without color? (Check in grayscale.)
4. Can I tell where the selected ability can be cast, and what happens if I tap elsewhere?
5. Does the preview show every affected unit, the HP after, statuses and conditional spread?
6. Did cancelling a preview leave everything exactly as before?
7. During the CPU turn, could I follow every action?
8. Is anything blurry, mis-scaled or jittering while scrolling?
9. Is any touch target hard to hit with a thumb?
10. Is there any element that doesn't clarify a decision, show important state or help touch? If yes, list it.

### 7.7 Focused commands

```sh
./gradlew jvmTest --tests "com.mkz.rpg.battlefieldHud.*"          # HUD state machine
./gradlew jvmTest --tests "com.mkz.rpg.screen.battlefieldHud.*"   # HUD domain/storage
./gradlew jvmTest --tests "com.mkz.rpg.battleUnit.*"              # unit rules, queries
./gradlew jvmTest --tests "com.mkz.rpg.screen.BattleUiScriptTest"
./gradlew jvmTest --tests "com.mkz.rpg.screen.BattleSceneSnapshotTest"
./gradlew jvmTest --tests "com.mkz.rpg.shared.usecases.acceptance.*"
./gradlew jvmTest --tests "com.mkz.rpg.shared.adapters.architecture.*"
./gradlew jvmTest --tests "com.mkz.rpg.assets.*"
./gradlew updateSnapshots
./gradlew lintKotlin
./gradlew jvmTest            # full suite, before marking a milestone done
```

Note: HUD use-case tests live in package `com.mkz.rpg.battlefieldHud` (`test/battlefieldHud/`), while HUD domain and storage tests live in `com.mkz.rpg.screen.battlefieldHud` (`test/screen/battlefieldHud/`). Follow the existing location of the test you're extending.

---

## 8. Dependencies and critical risks

```
M0 → M1 → M2 → M3 ─┬→ M4
                   ├→ M5 → M6 → M7 → M8
                   └──────────────────→ M9 (M8 recommended first)
                                        all → M10
```

| Risk | Impact | Mitigation |
| --- | --- | --- |
| Preview diverges from real resolution | Players are misled, which breaks tactical clarity | Parity acceptance tests (M7, M8). Preview reuses `AbilityExecution` and immutable `BattleUnit` methods |
| Preview mutates state | Corrupts the game | Pure query (no `EventBus`, `Random` or commands). Purity test |
| `TODO()` branches in `BattleUnit.applyImmediateEffect` | Crash during preview | Preview only simulates `DECREASE_HEALTH`, `INCREASE_HEALTH` and `TELEPORT`, and lists the rest as unsupported |
| Animation makes tests flaky or slow | Lost confidence | `FeedbackTiming.Instant` in tests. Step A of M9 is behavior-identical |
| View/domain drift during playback | Wrong board shown | Resync at the end of each queue drain |
| Tap/drag conflicts on touch | Accidental moves | Threshold plus test (M2) |
| Snapshot environment without OpenGL | Can't verify visuals | `DISABLE_HEADLESS_TEST=true` for logic. Report the visual check as **not done** rather than claiming it |
| `BattleUnit.Dto` and event changes ripple | Compile errors across tests | Additive fields only. Update mothers once |
| Clutter from cues | Violates the brief | Caps (2 pips, 4 lines) and M10 audit |
| Placeholder art mistaken for final | Visual inconsistency | Procedural placeholders listed in section 9.3. The developer replaces them |

---

## 9. Supporting decisions

### 9.1 Where new code goes

| Kind | Location | Rule |
| --- | --- | --- |
| Rule-derived read models (availability, preview) | `rpg/battleUnit/usecases/queries/` | Single `operator fun invoke` returning non-Unit. No `EventBus` |
| Interaction state | `rpg/screen/battlefieldHud/domain/` and `usecases/commands/` | Commands return Unit and publish HUD events. No command-to-command dependencies |
| Views and presenters | `rpg/screen/` (or `rpg/screen/feedback/`) | Passive views and delegate callbacks |
| Shared UI primitives | `rpg/shared/adapters/presentation/` | `UiPalette`, `PixelScale`, bars |
| Test helpers | `test/screen/` and `test/shared/usecases/acceptance/` | Follow `docs/testing/how-to-add-a-new-test.md` |

### 9.2 Existing behavior to preserve

- The HUD repository and debug overlay contract: `BattlefieldHudRepository.search()` is read by `DebugOverlaySupport`. If you rename HUD states, update the overlay and `DebugOverlayTest`.
- `BattleUiScript`'s public method names (documented in `docs/design-tools/reference-battle-ui-script.md`).
- Tile button names `row-<r>-column-<c>` (used by the harness and the script).

### 9.3 Asset requests (developer-authored; use procedural placeholders until then)

All assets use the Famicube palette, are drawn at 1× art pixels and are added to `GAME_ART_ASSET_PATHS`:

1. Status icons, 8×8: venom, on-death spread, and an overflow "+".
2. Pixel numerals 0–9 plus "+" and "−", 5×7, for damage, heal and cooldown numbers.
3. Selection bracket, target crosshair and inspect "eye", 16×16.
4. Hit spark (3 frames), defeat poof (4 frames), heal sparkle (3 frames), teleport blink (3 frames), all 16×16.
5. Ability-slot frame states (ready, selected, locked), 16×16.
6. Optional: replacement Famicube portraits for knight and rat, 32×32 (Q6).

---

## 10. Out-of-scope work

- Gameplay, balance, ability, effect or AI rule changes. That includes implementing `BEFORE_APPLYING_EFFECT` or `NEGATE_INCREASE_HEALTH`, and capping summons.
- A combat log, turn history or event feed of any kind.
- Exploration mode, real-time systems, mode transitions, or RTS concepts.
- New platforms (iOS, Android, WASM), safe-area handling for real devices, or a responsive landscape layout.
- Final artwork, audio, or new fonts beyond the asset requests above.
- New dependencies or engine upgrades.
- Rewriting the event bus or the clean-architecture structure.

---

## 11. Instructions for the implementation agent

1. **Read this whole plan and `docs/design/design-brief.md` before editing code.** In later sessions, re-read section 0's minimal set plus your milestone.
2. **Implement exactly one milestone per session, in the order M0 → M10.** Don't start the next milestone. If the developer asks for a different order, check section 8's dependencies first and report any conflict.
3. Open only the files in your milestone's *Repository touchpoints* (plus their tests). Use `grep -n` and small excerpts. Don't dump directories or read build outputs.
4. Follow `docs/testing/how-to-add-a-new-test.md`: same package as the class under test, `should X when Y` names, Given/When/Then comments, AssertJ, mothers, and `@Nested` only for multi-method classes.
5. Respect section 3.2's architecture rules, and run `./gradlew jvmTest --tests "com.mkz.rpg.shared.adapters.architecture.*"` after adding classes.
6. **Write or adjust tests first** for domain and HUD changes, then the implementation, then views.
7. Run focused tests first (section 7.7), then the full `./gradlew jvmTest` and `./gradlew lintKotlin` before declaring the milestone done.
8. Inspect visual output whenever views change: update goldens, open the PNGs, and describe what changed. If you can't render (no OpenGL), say that the visual check was **not performed**.
9. **Never mark an acceptance criterion as met unless you verified it.** If one fails, leave it unchecked, explain why, and stop. Don't weaken, skip, `@Disabled`, or delete a test or golden to make progress.
10. Don't add dependencies, don't rename or delete existing files unless the milestone says so, and don't change gameplay rules.
11. At the end, report:
    - The milestone ID.
    - Changed and added files.
    - Test commands with their pass/fail counts.
    - Snapshot changes and what you saw.
    - Unchecked criteria with reasons.
    - Deviations from this plan with justification.
    - Open questions (section 2.2) you relied on.
12. Update section 12 below **only after verification**: tick the box and add the date and a one-line note. Then **stop and wait** for the next instruction.

---

## 12. Milestone completion checklist

| Milestone | Done | Date | Verified by (tests / snapshots / manual) | Notes |
| --- | --- | --- | --- | --- |
| M0 Baseline, scenario and verification helpers | [x] | 2026-10-10 | jvmTest baseline 586 tests, 0 failures. Focused M0 tests and `lintKotlin` pass; existing 3 goldens unchanged. `runJvm` visual check not performed (no display) | Seed column in `reference-battle-scenarios.md` fixed to 42 to match the files. Grass near lava/water was removed in `ui-showcase` because terrain transitions are validated |
| M1 Visual foundations | [ ] | | | |
| M2 Portrait battlefield-first layout | [ ] | | | |
| M3 Unit selection and on-unit info | [ ] | | | |
| M4 Movement range | [ ] | | | |
| M5 Ability bar and cooldowns | [ ] | | | |
| M6 Target highlighting | [ ] | | | |
| M7 Safe consequence preview | [ ] | | | |
| M8 Propagation and conditional cues | [ ] | | | |
| M9 Combat feedback and pacing | [ ] | | | |
| M10 Consistency and regression pass | [ ] | | | |
