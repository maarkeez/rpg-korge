# Exploration

A new game starts exploring the battlefield, out of the battles: there are no turns. The human player walks their party with the virtual pad, and the game switches to a turn-based battle once a CPU battle unit engages it. Exploring lives in the [`exploration`](../../src/commonMain/kotlin/com/mkz/rpg/exploration) subdomain, and the screen uses it through `ExplorationApi`.

## The party

- Once the battle units are deployed, [`StartExploration`](../../src/commonMain/kotlin/com/mkz/rpg/exploration/usecases/commands/StartExploration.kt) puts every battle unit of the human player in the party, in their deployment order. The first one leads, and the rest follow it in a line.
- The CPU battle units stand still while the party explores.

## Walking

The party walks one tile at a time, towards the arrow held on the virtual pad (north, east, south or west).

1. [`WalkParty`](../../src/commonMain/kotlin/com/mkz/rpg/exploration/usecases/commands/WalkParty.kt) plans a step for each battle unit of the party: the leader walks one tile towards the direction, and each follower walks to the tile the battle unit ahead of it left. Nothing happens when the leader can not occupy that tile (e.g.: void, another battle unit, out of the battlefield), or while the party is still walking or battling.
2. The steps are walked one after another, with `RequestWalkBattleUnit`. A walk spends no turn action, unlike a move in a battle, and publishes the same `BattleUnitMoved`, so the battlefield occupancy, the loot found on the way and the animation work as in a battle.
3. Each step is awaited until its battle unit occupies its tile (`BattlefieldTileOccupied`, see [`ResolvePartyStep`](../../src/commonMain/kotlin/com/mkz/rpg/exploration/usecases/commands/ResolvePartyStep.kt)). Only then is the tile left by the battle unit ahead vacant, so the next battle unit can step on it.
4. A follower that can not reach its tile (e.g.: the battle unit ahead could not leave it) skips its step, so the party always finishes walking. A follower that is not next to the battle unit ahead (e.g.: before the party is lined up) walks the shortest path to its tile.
5. Once every step has been walked or skipped, the party has walked (`PartyWalked`).

Holding an arrow keeps walking: [`VirtualPadView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/VirtualPadView.kt) asks to walk on every frame the arrow is held, and [`ExplorationPresenter`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/ExplorationPresenter.kt) only walks once the [presentation timeline](presentation-timeline.md) is idle, so the party can not walk ahead of its animation. The party steps are played quicker than a battle move (`EXPLORING_STEP_DURATION`, 75ms per tile).

## Engaging

After the party starts exploring and every time it has walked, the CPU brain [`EngageEnemies`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBrain/usecases/commands/EngageEnemies.kt) looks for the CPU battle units that have a battle unit of the party within reach, with the same rule as in a battle (see [CPU brain](cpu-brain.md#engaging)). When there is any, the combat is engaged (`CombatEngaged`):

- The battle starts between the CPU battle units that engaged and the whole party. The CPU plays first, as it engaged. The other CPU battle units stay out of the battle: they neither play nor change.
- When a battle round finishes, the CPU brain [`UpdateEngagement`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBrain/usecases/commands/UpdateEngagement.kt) has the CPU battle units out of the battle that now have a battle unit of the party within reach join it (`EngagementUpdated`). The next round only starts then, so they play from it.
- Only the battle units in the battle can be selected: tapping one out of it is as tapping an empty tile.
- The exploration pauses (`ExplorationPaused`), so the party no longer walks.
- The battle info announces the battle, and the battle HUD and the player call to action take the place of the virtual pad.

While exploring, tapping a battle unit does not select it, and the battle info displays "Exploring".
