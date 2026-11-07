# Presentation timeline

The game resolves its events instantly: a whole CPU turn, with every move and cast, is played within a single frame. The battle scene replays those events in order, at a pace the player can follow, through the [`BattleSceneTimeline`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/BattleSceneTimeline.kt).

## How it works

- `BattleScene` creates a single timeline and passes it to every presenter.
- Presenters never draw an event straight away. They add a **step** to the timeline with `play { ... }`.
- The timeline plays its steps one after another, in the order they are added. A step may take time, e.g.: a battle unit walking tile by tile. The following steps wait for it to finish.
- When the timeline is idle, a new step starts straight away, so instant steps (e.g.: hiding the HUD) are displayed as soon as their event arrives.
- A step added with `playNext { ... }` is played right after the step being played, before the steps waiting, whatever the order the presenters receive their events in. E.g.: when a battle starts, the virtual pad hides as soon as the leader lands on its last tile, before the battle is announced (see [Exploration](exploration.md#engaging)).

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
- The player MUST NOT act while the timeline is playing (`isPlaying`): selecting tiles or abilities, cancelling or confirming a cast, finishing the turn and walking the party while exploring are ignored. The scene still displays a past state of the battle meanwhile. The virtual pad is the exception: its last press is kept, and acted on once the timeline is idle (see [Exploration](exploration.md#walking)).
- The finish turn button shows it: [`FinishTurnPresenter`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/FinishTurnPresenter.kt) displays it during the battles only, from `BattleStarted` to `BattleFinishedWithoutWinner`, as there are no turns while exploring. It keeps it disabled (faded, see [Buttons](buttons.md)) unless the scene displays a human player turn and the timeline is idle. It listens to `onPlayingChanged` to enable it again as soon as the timeline stops playing, and disables it as soon as it is clicked.

## Announcing a quest

`QuestPresenter` turns `QuestStarted` and `QuestCompleted` of any quest into a step of the timeline, which draws the tracker for the main quest and adds a [toast](quests.md#the-announcements) to a queue. The queue plays with `playAlongside`, one toast at a time, so **it never holds the timeline**: the party walks and the battle ends under it. A toast still appears once the timeline reaches its event, so it never announces a later state before an earlier one is displayed.

The tracker's pop when a task changes does the same: the step draws it and plays `pop` with `playAlongside`. A last defeat publishes `TaskProgressed`, `TaskCompleted` and `QuestCompleted` in one chain of events, each read when it arrives: the first two find the quest completed and draw nothing, as the third one announces it.

## Starting a turn

When a player turn starts, two animations play together, in the colour of the player turn: blue (`HUMAN_TURN_COLOR`, `#0084FF`) for the human player and red (`CPU_TURN_COLOR`, `#E03C28`) for the CPU player. Both colours are part of the FamiCube palette.

- `BattleInfoView.announceTurn` displays the turn in the header with the turn colour, and pops it: it grows to `POP_SCALE` (1.3) and shrinks back within `POP_DURATION` (400ms). The round pops as well when it has changed, including the first turn of the battle.
- `BattlefieldView.tint` covers the visible battlefield with the turn colour, fading it up to `TINT_ALPHA` (0.4) and back within `TINT_DURATION` (500ms). The camera does not move.

`BattleInfoPresenter` plays both in a single step and waits for both, so the effects applied at the start of the turn and the CPU actions are displayed afterwards. The player can not act until the turn has been announced.

## Moving battle units

`BattleUnitMoved` carries the `path` walked: the tiles from the origin (excluded) to the destination (included). Each tile costs one step. The path is the shortest one found by [`SearchShortestPath`](../../src/commonMain/kotlin/com/mkz/rpg/battlefield/usecases/queries/SearchShortestPath.kt) through vacant tiles. A teleport jumps straight to its destination, so its path only has the destination.

`BattlefieldView.moveBattleUnit` glides the image of the battle unit along the path, over the tiles, `MOVE_STEP_DURATION` (150ms) per tile, then displays it on its destination tile. A step to a tile that is not next to the previous one (a teleport) jumps to it. In a battle, before each step, it scrolls the battlefield to center the tile when the tile is out of the viewport or on its edge, and waits for the scroll. A battlefield smaller than the viewport is never scrolled.

While exploring, the camera trails the leader on every frame instead, outside the timeline (see [Exploration](exploration.md#walking)). The followers' steps are played with `playTogether`: they start straight away, together with the step being played (the leader's), and the next steps wait for both, so the whole party glides at once.

While exploring, the CPU battle units [wander](cpu-battle-brain.md#wandering) every second. Their steps are played alongside the timeline (`playAlongside`), rather than in it, at a quicker pace than the party walks (`WANDERING_STEP_DURATION`, 75ms per tile), so the party keeps walking smoothly: the step starts straight away, in parallel with the playing steps, and does not make the timeline playing. They do not scroll the battlefield, so the party stays in view, nor reset the tiles, so the selection is kept (e.g.: to use the inventory). When a round finishes, the CPU battle units out of the battle wander once, and their steps are played in the timeline at the battle pace, so they are seen before the next round starts, also without scrolling the battlefield.

## Applying effects

A battle unit blinks in white every time an effect is applied to it:

| Event                                | Published when                            | Example                               |
|--------------------------------------|-------------------------------------------|---------------------------------------|
| `EffectReceived`                     | The battle unit receives an effect        | sword, heal, venom, lava, water       |
| `OnPlayerTurnStartedEffectTriggered` | An on player turn started effect triggers | venom damage at the start of the turn |

`BattlefieldView.flashBattleUnit` first scrolls the battlefield to the battle unit, like a moving battle unit, so effects triggered at the start of a player turn are seen wherever the battle unit is. Then, a white silhouette of the battle unit blinks `FLASH_COUNT` (3) times, for `FLASH_DURATION` (80ms) each. A battle unit receiving several effects at once (e.g.: skull) blinks once per effect, one after the other: a flash waits for the flashes already blinking on its tile.

While exploring, the flash of an `EffectReceived` is played alongside the timeline (`playAlongside`), as the CPU battle units wander, so it never holds the party. Played in the timeline, the terrain effects of three battle units standing on water flashed for 1440ms every second, so the timeline was never idle, and the party never walked again. A flash stops as soon as its battle unit leaves its tile (`removeBattleUnit`, e.g.: a step or a swap), so it never blinks over an empty tile, or over the follower stepping on it. In a battle, every flash is still played in the timeline.

While exploring, a battle unit whose health changes (`BattleUnitDamaged`, `BattleUnitHealed`, e.g.: standing on lava or water, or drinking a potion) also displays its health over its tile, alongside the timeline, so the player sees what the effect does. `BattlefieldView.flashHealth` draws the health bar of the attack preview (`BattlefieldHealthBarPreviewView`, in thirds of its maximum health) with the health left, on its own `HEALTH_FLASH` layer, so the selection of the tile is kept. It lasts 1.5s (`HEALTH_FLASH_DURATION`): the third left blinks as long as the battle unit flashes, then stays still, and the bar fades out in its last 200ms. The next health change on the tile replaces it, for 1.5s again, so standing on lava it never goes away. It follows its battle unit when it moves (a step, a swap, a wander), gliding with it, unless its destination displays a newer one, and is removed when the battle unit is removed from its tile (e.g.: defeated), and when a battle starts (`ExplorationPaused`). A heal at full health displays the bar full. In a battle, the health changes only flash.

## Selecting tiles

Each battlefield tile draws its content as named layers, from bottom to top: `TERRAIN`, `LOOT` (the dropped items, see [Loot](#loot)), `BATTLE_UNIT`, `SELECTION` (the tile frames) and `DIM`.

When the player selects a battle unit or an ability, the tiles that can be selected keep their frames, and every other tile is darkened with a black mask (`DIM`, at `DIM_ALPHA` 0.4 opacity) by `BattlefieldView.dimTilesExcept`, including the battle units on them:

| Selection   | Tiles that are not darkened                              |
|-------------|----------------------------------------------------------|
| Battle unit | Its movement range and the selected battle unit tile      |
| Ability     | Every tile where it can be cast and the caster tile       |

`BattlefieldView.resetTiles` removes the frames and the mask, e.g.: when the selection is cleared or a battle unit moves.

The movement range of the selected battle unit is refreshed (`RefreshMovementRange`) whenever it may change: when a tile is occupied (`TileOccupied`, e.g.: a battle unit moved or deployed) or an occupant removed (`OccupantRemoved`, e.g.: a battle unit defeated), and when a cast has been resolved. A cast resolves before its consequences reach the battlefield (e.g.: the deployed bee, or the defeated enemy leaving its tile), so refreshing on the occupancy changes is what keeps the range right.

When a turn starts, the selection is cleared (`DeselectBattleUnit`), so a selection from a previous turn can not be acted on.

## Casting an ability

The selected battle unit stays selected while the player casts an ability with it:

- Selecting an ability collapses the header of the battle unit info to a strip of what a cast spends (the portrait,
  health, mana, steps and casts), and the ability buttons and the ability info slide up
  under it in 150ms (`BattleUnitInfoView.FOCUS_DURATION`), outside the timeline, as a view's own animation.
- Choosing where to cast previews it as a forecast ([`AttackPreviewView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/AttackPreviewView.kt)):
  the ability and its caster on a line, with the mana it spends (e.g.: `Mushroom`, `by Mage`, `-10 mana`), then a row
  per battle unit the cast changes: the caster first, only when the cast changes its health or effects (e.g.: a heal),
  then the receivers. Each row tells the change of health, large, red for a loss (`-12`) and green for a gain (`+5`),
  the health from before to after (`30 → 18`), and `KO` when it would be left with no health. A cast reaches at most
  the caster and its 4 adjacent tiles, so the rows are all in sight, with no scroll.

- Confirming the cast keeps the preview displayed while the cast is played, e.g.: the target blinking. Once the cast has been resolved (`AbilityCastResolved`), the caster is displayed selected again, with its movement range read after the cast (e.g.: its mana spent, or its new tile when teleported).
- Cancelling the cast goes back to choosing where to cast the same ability.

## Remaining actions

The selected battle unit info displays its remaining actions without text, with a [`PointsView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/PointsView.kt) per action: one rounded pip per point, 14x7, filled while it remains and tinted with an outline once it has been spent.

| Points | Colour (FamiCube palette) | Maximum                          |
|--------|---------------------------|----------------------------------|
| Steps  | White `#FFFFFF`           | The unit movement range          |
| Casts  | Gold `#FFBB31`            | The casts per turn (currently 1) |

For example, a knight that walked 1 of its 3 steps and has not cast yet displays ▬▬▭ ▬. Both maximums come from the battle unit (`RemainingActionsDto`).

## Loot

The items lying on a tile are drawn on its `LOOT` layer by `BattlefieldView.displayLoot`, at 2x (32px) centred on the tile, so the terrain shows around them and a battle unit standing on the tile is drawn over them:

| Items on the tile | Drawn as                              |
|-------------------|---------------------------------------|
| None              | Nothing                               |
| One               | Its icon, `lootableItem/<id>.png`     |
| Several           | The chest, `battlefield/chest.png`    |

`BattlefieldPresenter` keeps the items it has drawn on each tile, and changes them in timeline order:

| Event               | Step played                                                                                       |
|---------------------|---------------------------------------------------------------------------------------------------|
| `ItemDropped`       | The item appears, once the defeated battle unit has been removed from its tile                    |
| `DroppedItemLooted` | The item disappears, or the chest becomes the item left, once the battle unit has walked its path |

## Inventory

The selected battle unit info displays a bag (the chest icon) next to its abilities, only for the battle units of the human player. Clicking it opens the inventory of the battle unit in the HUD, instead of its info, through [`InventoryPresenter`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/InventoryPresenter.kt):

- [`InventoryView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/InventoryView.kt) displays one slot per storage space (12, in 2 rows of 6), with the stored items in order and the equipped copy of each equipped item framed in gold (`#FFBB31`).
- Its title line reads `Inventory` and the gold in a battle. While exploring, it names the party member between the arrows that switch to the previous or next member instead (see [Inventory while exploring](exploration.md#inventory)): the presenter selects that member on the battlefield, and opens its inventory once its selection has displayed its info.
- Clicking a stored item frames it in white, and the call to action displays its name and actions instead of the finish turn (nothing while exploring):

| Item                            | Actions                                    |
|---------------------------------|--------------------------------------------|
| Consumable                      | Cancel, Discard, Consume                   |
| Can not learn its ability       | Cancel, Discard, Consume (disabled)        |
| Equippable, not equipped        | Cancel, Discard, Equip                     |
| Equippable, equipped            | Cancel, Discard (disabled), Unequip        |
| Another copy is equipped        | Cancel, Discard, Equip (disabled)          |

- Consume, Equip and Unequip are blue, and the others grey.
- Discard asks first, in place of the actions: `Discard {item}? It can't be undone.` takes the line of the name and sell price, with Back and Discard, in red, below it, and the item stays framed. Back displays the item's actions again. Tapping another item, the arrows or Close drop the question, discarding nothing.
- While exploring, Give is added after Discard (e.g.: Cancel, Discard, Give, Consume), disabled for the only copy of an equipped item, and reading `No one` or `No space` when no other party member can receive it. See [Inventory while exploring](exploration.md#inventory).
- Equipping an item in a zone already used is the swap. Once an action is selected, the finish turn is displayed again in a battle, and nothing while exploring.
- In a battle, the actions are only offered during the turn of the battle unit player. Out of the battles (e.g.: while exploring), they are always offered. They are ignored while the timeline is playing. Closing the inventory displays the battle unit info again, read again, as the items may have changed it (e.g.: healed it, or raised its maximum health).
- The inventory is redrawn, in timeline order, when the battle unit stores, consumes, discards, equips, unequips, gives or receives an item. Selecting a battle unit or clearing the selection displays the battle unit info again.

### Giving gold

While exploring, the gold button opens [`GiveGoldView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/GiveGoldView.kt) in the HUD instead of the inventory, and the call to action displays Cancel, All and Give (`displayGiveGold`), as tall as the finish turn, so the panel keeps the height of the HUD.

- The slider follows the finger on its own, and only `updateGiveGold` writes the amount on Give, as displaying the panel again would end the drag. Anything else that changes the panel (All, a receiver chosen, a reward) displays it again.
- Give only acts while the timeline is idle, as Cancel does. `GoldGiven` displays the bag again in timeline order, redrawn with the new gold, and `GoldReceived` flashes the receiver alongside the timeline, so the next give can start straight away.
- Taking the bag off the stage ends no swipe, so the inventory ignores a swipe that started before it was last displayed: the tap on the gold button would otherwise end as a swipe on the tap on Give.

### The panels and their row

The panels that take the place of the pad (the quests, the save slots, a question, the formation, the shop and the merchant's dialog) keep their buttons in the [action row](buttons.md#the-action-row) of the call to action. Their presenters display it while the panel is displayed, in timeline order, and each tap only acts while the timeline is idle, as the panel's own taps do.

`ExplorationPresenter` clears it: displaying or hiding the pad calls `hideActions()`, as does `PlayerVictory`, so a battle starting never leaves an old Back on the screen, and Finish turn is displayed again when the battle has one. `hideActions()` removes only the row, so the step waiting for the player to choose with an inventory full is not disturbed.

### Inventory full

When an item is found while the inventory is full (`InventoryFull`), a step opens the inventory and the call to action displays "Inventory full", the found item icon and a "Leave it" button. **The step waits for the player to choose**, so the timeline keeps playing and the player can not move, cast, close the inventory or finish the turn meanwhile:

- Clicking a stored item frames it and asks the same question as Discard. Discard then discards it and stores the found item from its tile, Back displays "Inventory full" again with no item framed, and clicking another stored item moves the question to it. An equipped item can not be discarded, so clicking it does nothing.
- Clicking "Leave it" leaves the found item on its tile.

Several items found in the same walk are asked one after another, in walking order. Each step reads the inventory when it starts, as the previous choice may have changed it.
