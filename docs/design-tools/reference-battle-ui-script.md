# BattleUiScript reference

`BattleUiScript` (`src/jvmTest/kotlin/com/mkz/rpg/screen/BattleUiScript.kt`) is the test-only helper that maps logical player intents to simulated clicks on the real battle views, then dispatches the event bus so the full cascade (presenter updates, use cases, CPU turn) settles before the next call.

## Construction

```kotlin
BattleUiScript(
    click: suspend (View) -> Unit,        // provided by the test: { view -> view.simulateClick() }
    battlefieldView: BattlefieldView,
    battleUnitInfoView: BattleUnitInfoView,
    playerCallToActionView: PlayerCallToActionView,
    battlefieldApi: BattlefieldApi,
    battleUnitApi: BattleUnitApi,
    battleApi: BattleApi,
    eventBus: EventBus,
)
```

Built inside `viewsTest { }` via the `setupBattleUiScript()` helper in `BattleUiScriptTest`, which loads view assets, constructs `BattlefieldPresenter` + `FinishTurnPresenter`, attaches the views to the stage, and runs `terrainApi.init()` + `battleSetupApi.setupBattle()` + one `dispatch()`.

## Scripted intents (input)

All are `suspend` and end with `eventBus.dispatch()`.

| Method | Clicks | Resulting settled state |
| --- | --- | --- |
| `selectUnit(battleUnitId)` | the tile the unit stands on | Unit selected: movement range highlighted, unit info HUD shown |
| `tapTile(row, column)` | the tile at `row`/`column` | Context-dependent: selects the unit on the tile, moves the selected unit there, or previews an ability cast on it |
| `selectAbility(abilityIndex)` | the ability button at `abilityIndex` in the unit info HUD | Ability selected: cast range highlighted |
| `confirmCast()` | the **Confirm** button (inner `UIButton` of `ConfirmButton`) | `RequestCastAbility` published and applied; HUD back to finish-turn state |
| `cancelCast()` | the **Cancel** button (inner `UIButton` of `CancelButton`) | Cast discarded; HUD back to finish-turn state |
| `finishTurn()` | the **Finish turn** button | Turn passed; if the next player is a CPU it plays its entire turn in the same dispatch |

## State queries (assertions)

| Method | Returns | Source |
| --- | --- | --- |
| `positionOf(battleUnitId)` | `PositionDto` (non-null) | `battlefieldApi.searchPosition` |
| `occupantAt(row, column)` | `String?` battle unit id | `battlefieldApi.searchOccupant` |
| `battleUnit(battleUnitId)` | `BattleUnit.Dto` (non-null) | `battleUnitApi.searchBattleUnitById` |
| `currentPlayerTurn()` | `String` player id | `battleApi.searchBattle().currentPlayerTurn` |
| `castTargets(battleUnitId, abilityId)` | `List<PositionDto>` — all tiles the unit can cast the ability on | `battleUnitApi.whereCanCast` (use to *pick* a valid target tile for `tapTile`) |
| `isConfirmAndCancelDisplayed()` | `Boolean` — confirm/cancel bar is currently shown | view tree of the call-to-action view |

## Default battle layout (seed 42, `setupBattle()`)

| Unit id | Unit | Player | Position |
| --- | --- | --- | --- |
| `player-1-unit-1` | knight (100 hp, 30 mp, move 3) | `player-one` (human, moves first) | row 6, col 6 |
| `player-1-unit-2` | knight | `player-one` | row 7, col 7 |
| `player-2-unit-1` | rat (20 hp, 10 mp, move 3) | `player-two` (CPU) | row 0, col 0 |
| `player-2-unit-2` | rat | `player-two` | row 1, col 1 |

Knight ability button order (index in `selectAbility`): `0` poisoned-sword, `1` mushroom, `2` skull, `3` teleport, `4` bee, `5` heal.

## View-tree locators used internally

- Tile button: named `row-<r>-column-<c>` under `BattlefieldView`'s grid.
- Ability buttons: the six `AbilityButtonView` children of `BattleUnitInfoView`, in `abilityCooldowns` order.
- Call-to-action: child of `PlayerCallToActionView` named `call-to-action`; action buttons are the `UIButton` descendants matched by `text` (`"Confirm"`, `"Cancel"`, `"Finish turn"`).
