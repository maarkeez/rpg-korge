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

## Rules

- Presenters MUST read what a step needs when its event arrives, and only draw it inside the step. By the time the step is played, the battle may have moved on, e.g.: a battle unit that walks is defeated later in the same CPU turn, so it can no longer be found.
- Every visual change caused by a battle event MUST go through the timeline, so the scene never displays a later state before an earlier one.
- The player MUST NOT act while the timeline is playing (`isPlaying`): selecting tiles or abilities, cancelling or confirming a cast and finishing the turn are ignored. The scene still displays a past state of the battle meanwhile.

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
