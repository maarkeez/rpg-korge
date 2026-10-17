# Presentation timeline

The game resolves its events instantly: a whole CPU turn, with every move and cast, is played within a single frame. The battle scene replays those events in order, at a pace the player can follow, through the [`BattleSceneTimeline`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/BattleSceneTimeline.kt).

## How it works

- `BattleScene` creates a single timeline and passes it to every presenter.
- Presenters never draw an event straight away. They add a **step** to the timeline with `play { ... }`.
- The timeline plays its steps one after another, in the order they are added. A step may take time, e.g.: a battle unit walking tile by tile. The following steps wait for it to finish.
- When the timeline is idle, a new step starts straight away, so instant steps (e.g.: hiding the HUD) are displayed as soon as their event arrives.

For example, when the CPU moves two rats and the turn passes back to the human player:

| Event arrives (same frame)         | Step played                                         |
|------------------------------------|-----------------------------------------------------|
| `BattleUnitMoved` (first rat)      | The first rat walks its path                        |
| `BattleUnitMoved` (second rat)     | Then, the second rat walks its path                 |
| `PlayerTurnStarted` (human player) | Then, the battle info displays the human player turn |

The timeline plays its steps on the scene coroutines, which `BattleScene` runs on the game window event loop, like the frames. Hence, the views are only changed from that loop. KorGE does so when running the game, but not in the headless tests (`ViewsForTesting`), which would otherwise play the steps on `Dispatchers.Default` while the frames are rendered, making the end-to-end tests flaky.

## Rules

- Presenters MUST read what a step needs when its event arrives, and only draw it inside the step. By the time the step is played, the battle may have moved on, e.g.: a battle unit that walks is defeated later in the same CPU turn, so it can no longer be found.
- Every visual change caused by a battle event MUST go through the timeline, so the scene never displays a later state before an earlier one.
- The player MUST NOT act while the timeline is playing (`isPlaying`): selecting tiles or abilities, cancelling or confirming a cast, finishing the turn and walking the party while exploring are ignored. The scene still displays a past state of the battle meanwhile.
- The finish turn button shows it: [`FinishTurnPresenter`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/FinishTurnPresenter.kt) keeps it displayed but disabled (greyed out) unless the scene displays a human player turn and the timeline is idle. It listens to `onPlayingChanged` to enable it again as soon as the timeline stops playing, and disables it as soon as it is clicked.

## Starting a turn

When a player turn starts, two animations play together, in the colour of the player turn: blue (`HUMAN_TURN_COLOR`, `#0084FF`) for the human player and red (`CPU_TURN_COLOR`, `#E03C28`) for the CPU player. Both colours are part of the FamiCube palette.

- `BattleInfoView.announceTurn` displays the turn in the header with the turn colour, and pops it: it grows to `POP_SCALE` (1.3) and shrinks back within `POP_DURATION` (400ms). The round pops as well when it has changed, including the first turn of the battle.
- `BattlefieldView.tint` covers the visible battlefield with the turn colour, fading it up to `TINT_ALPHA` (0.4) and back within `TINT_DURATION` (500ms). The camera does not move.

`BattleInfoPresenter` plays both in a single step and waits for both, so the effects applied at the start of the turn and the CPU actions are displayed afterwards. The player can not act until the turn has been announced.

## Moving battle units

`BattleUnitMoved` carries the `path` walked: the tiles from the origin (excluded) to the destination (included). Each tile costs one step. The path is the shortest one found by [`SearchShortestPath`](../../src/commonMain/kotlin/com/mkz/rpg/battlefield/usecases/queries/SearchShortestPath.kt) through tiles that can be occupied. A teleport jumps straight to its destination, so its path only has the destination.

`BattlefieldView.moveBattleUnit` displays the battle unit on each tile of the path, stopping `MOVE_STEP_DURATION` (150ms) on each one. Before each step, it scrolls the battlefield to center the tile when the tile is out of the viewport or on its edge. A battlefield smaller than the viewport is never scrolled.

## Applying effects

A battle unit blinks in white every time an effect is applied to it:

| Event                        | Published when                                       | Example                              |
|------------------------------|------------------------------------------------------|--------------------------------------|
| `EffectReceived`             | The battle unit receives an effect                   | sword, heal, venom, lava, water      |
| `OnTurnStartedEffectApplied` | An effect applied at the start of the turn ticks     | venom damage at the start of the turn |

`BattlefieldView.flashBattleUnit` first scrolls the battlefield to the battle unit, like a moving battle unit, so effects applied at the start of a turn are seen wherever the battle unit is. Then, a white silhouette of the battle unit blinks `FLASH_COUNT` (3) times, for `FLASH_DURATION` (80ms) each. A battle unit receiving several effects at once (e.g.: skull) blinks once per effect, one after the other.

## Selecting tiles

Each battlefield tile draws its content as named layers, from bottom to top: `TERRAIN`, `LOOT` (the dropped items, see [Loot](#loot)), `BATTLE_UNIT`, `SELECTION` (the tile frames) and `DIM`.

When the player selects a battle unit or an ability, the tiles that can be selected keep their frames, and every other tile is darkened with a black mask (`DIM`, at `DIM_ALPHA` 0.4 opacity) by `BattlefieldView.dimTilesExcept`, including the battle units on them:

| Selection   | Tiles that are not darkened                              |
|-------------|----------------------------------------------------------|
| Battle unit | Its movement range and the selected battle unit tile      |
| Ability     | Every tile where it can be cast and the caster tile       |

`BattlefieldView.resetTiles` removes the frames and the mask, e.g.: when the selection is cleared or a battle unit moves.

The movement range of the selected battle unit is refreshed (`RefreshMovementRange`) whenever it may change: when a tile is occupied (`BattlefieldTileOccupied`, e.g.: a battle unit moved or deployed) or vacated (`OccupantRemoved`, e.g.: a battle unit defeated), and when a cast has been resolved. A cast resolves before its consequences reach the battlefield (e.g.: the deployed bee, or the defeated enemy leaving its tile), so refreshing on the occupancy changes is what keeps the range right.

When a turn starts, the selection is cleared (`DeselectBattleUnit`), so a selection from a previous turn can not be acted on.

## Casting an ability

The selected battle unit stays selected while the player casts an ability with it:

- Confirming the cast keeps the preview displayed while the cast is played, e.g.: the target blinking. Once the cast has been resolved (`AbilityCastResolved`), the caster is displayed selected again, with its movement range read after the cast (e.g.: its mana spent, or its new tile when teleported).
- Cancelling the cast goes back to choosing where to cast the same ability.

## Remaining turn actions

The selected battle unit info displays its remaining turn actions without text, with a [`PointsView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/PointsView.kt) per action: one circle per point, filled while it remains and hollow once it has been spent.

| Points | Colour (FamiCube palette) | Maximum                          |
|--------|---------------------------|----------------------------------|
| Steps  | White `#FFFFFF`           | The unit movement range          |
| Casts  | Gold `#FFBB31`            | The casts per turn (currently 1) |

For example, a knight that walked 1 of its 3 steps and has not cast yet displays ●●○ ●. Both maximums come from the battle unit (`RemainingTurnActionsDto`).

## Loot

The items lying on a tile are drawn on its `LOOT` layer by `BattlefieldView.displayLoot`, at 2x (32px) centred on the tile, so the terrain shows around them and a battle unit standing on the tile is drawn over them:

| Items on the tile | Drawn as                              |
|-------------------|---------------------------------------|
| None              | Nothing                               |
| One               | Its icon, `lootableItem/<id>.png`     |
| Several           | The chest, `battlefield/chest.png`    |

`BattlefieldPresenter` keeps the items it has drawn on each tile, and changes them in timeline order:

| Event                 | Step played                                                                                      |
|-----------------------|--------------------------------------------------------------------------------------------------|
| `DroppedItemPlaced`   | The item appears, once the defeated battle unit has been removed from its tile                   |
| `DroppedItemPickedUp` | The item disappears, or the chest becomes the item left, once the battle unit has walked its path |

## Inventory

The selected battle unit info displays a bag (the chest icon) next to its abilities, only for the battle units of the human player. Clicking it opens the inventory of the battle unit in the HUD, instead of its info, through [`InventoryPresenter`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/InventoryPresenter.kt):

- [`InventoryView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/InventoryView.kt) displays one slot per storage space (12, in 2 rows of 6), with the stored items in order and the equipped copy of each equipped item framed in gold (`#FFBB31`).
- Clicking a stored item frames it in white, and the call to action displays its name and actions instead of the finish turn:

| Item                            | Actions                                    |
|---------------------------------|--------------------------------------------|
| Consumable                      | Consume, Discard, Cancel                   |
| Equippable, not equipped        | Equip, Discard, Cancel                     |
| Equippable, equipped            | Unequip, Discard (disabled), Cancel        |
| Another copy is equipped        | Equip (disabled), Discard, Cancel          |

- Equipping an item in a zone already used is the swap. Once an action is selected, the finish turn is displayed again.
- The actions are only offered during the turn of the battle unit player, and ignored while the timeline is playing. Closing the inventory displays the battle unit info again, read again, as the items may have changed it (e.g.: healed it, or raised its maximum health).
- The inventory is redrawn, in timeline order, when the battle unit stores, consumes, discards, equips or unequips an item. Selecting a battle unit or clearing the selection displays the battle unit info again.

### Inventory full

When an item is found while the inventory is full (`InventoryFull`), a step opens the inventory and the call to action displays "Inventory full", the found item icon and a "Leave it" button. **The step waits for the player to choose**, so the timeline keeps playing and the player can not move, cast, close the inventory or finish the turn meanwhile:

- Clicking a stored item discards it and stores the found item from its tile. An equipped item can not be discarded, so clicking it does nothing.
- Clicking "Leave it" leaves the found item on its tile.

Several items found in the same walk are asked one after another, in walking order. Each step reads the inventory when it starts, as the previous choice may have changed it.
