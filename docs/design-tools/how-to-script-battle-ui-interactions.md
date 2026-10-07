# How to write a scripted battle UI interaction test

## When to use this

Use this when you change how the player interacts with the battle UI — tile taps, ability buttons, the confirm/cancel flow, the finish-turn button — and want a test that drives those interactions headlessly and asserts on the resulting battle state.

## Background

`BattleUiScriptTest` (`src/jvmTest/kotlin/com/mkz/rpg/screen/BattleUiScriptTest.kt`) builds the **real views and presenters** (`BattlefieldView`, `BattleUnitInfoView`, `AttackPreviewView`, `BattleHudView`, `PlayerCallToActionView`, `BattlefieldPresenter`, `FinishTurnPresenter`) on top of a **deterministic API graph** — the same `*Api` graph the acceptance tests use, with a seeded `Random` (`seed = 42`), exactly like the deterministic replay tooling (T1).

It does not run `BattleScene`: the scene's glue (view layout, `sceneInit`/`sceneMain`) is thin, and building the graph in the test gives direct access to the `*Api` queries for assertions and to the `EventBus` for manual dispatch.

Input is simulated with Korge's `ViewsForTesting`:

- `View.simulateClick()` fires the view's real mouse-click signal — the same signal a finger tap on the button produces, so `onClick` handlers, delegates, and use cases all run for real.
- After each scripted click the test calls `eventBus.dispatch()`. `InMemoryEventBus.dispatch()` drains the whole queue, including events published while dispatching — so one dispatch settles the full cascade: the presenter's view updates **and** any CPU turn triggered by `PlayerTurnStarted`.

Because everything is synchronous and seeded, no frame polling or waiting is needed.

## Steps

### Add a new interaction test

1. Open `BattleUiScriptTest` and add the test inside the matching `@Nested` group (`UnitSelection`, `AbilityCast`, `TurnFlow`) — or create a new group. Follow the `should XXX when YYY` naming and Given/When/Then structure:

   ```kotlin
   @Test
   fun `should do X when player taps Y`() =
       viewsTest {
           // Given
           val script = setupBattleUiScript()
           script.selectUnit(playerOneKnightId)
           // When
           script.tapTile(row = 5, column = 6)
           // Then
           assertThat(script.occupantAt(row = 5, column = 6)).isEqualTo(playerOneKnightId)
       }
   ```

2. Drive the interaction through `BattleUiScript` methods only — never call `*Api` commands (`moveBattleUnit`, `finishPlayerTurn`, …) to make the interaction happen. It is fine to use `*Api` **queries** to *pick* input (e.g. `castTargets(...)` to find a valid cast tile) or to assert.

3. Run just your test:

   ```sh
   ./gradlew jvmTest --tests "com.mkz.rpg.screen.BattleUiScriptTest"
   ```

### Add a new scripted intent

When the test needs an interaction `BattleUiScript` does not expose yet (e.g. dragging, hotkeys), add a method to `BattleUiScript` (`src/jvmTest/kotlin/com/mkz/rpg/screen/BattleUiScript.kt`) that maps the intent to one or more clicks and ends with `eventBus.dispatch()`. Keep the class free of assertions — it is input, not verification.

Notes for mapping new intents:

- Tile views are named `row-<r>-column-<c>` inside `BattlefieldView`.
- Ability buttons appear in the order of `battleUnit.abilityCooldowns.keys` (for the default knight: `poisoned-sword`, `mushroom`, `skull`, `teleport`, `bee`, `heal`).
- `ConfirmButton`/`CancelButton`/`FinishTurnView` are containers whose click handler lives on an **inner** `UIButton` — click the inner button (the script's `actionButton(text)` helper already does this).
- After a cast preview is shown, the call-to-action view shows the Confirm/Cancel buttons; after confirming or cancelling it goes back to the Finish-turn button.

### Keep the suite green

```sh
./gradlew formatKotlin
./gradlew lintKotlin
./gradlew jvmTest
```

## Troubleshooting

| Symptom | Cause / fix |
| --- | --- |
| Click has no effect (state unchanged, no `[EVENT] Queued` in output) | You clicked a container instead of the `UIButton` that owns the `onClick` handler. Find the inner button (by class or by `text`) and click that. |
| `ConfirmCast`/`CancelCast` throws `InvalidBattlefieldHudState` | The HUD is not in the cast-preview state — a required step (unit select → ability select → target tile) was skipped, or a previous `dispatch()` already consumed the state. |
| CPU did not play after `finishTurn()` | The CPU only plays when `PlayerTurnStarted` reaches its `OnPlayerTurnStarted` listener inside a `dispatch()` — make sure the turn was actually finished and `dispatch()` ran. |
| Test is flaky or order-dependent | Each JUnit test gets a fresh class instance, so the event bus and views are fresh per test — do not share mutable state across tests via `companion` or singletons. |
