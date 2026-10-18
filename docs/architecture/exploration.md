# Exploration

A new game starts exploring the battlefield, out of the battles: there are no turns. The human player walks their party with the virtual pad, and the game switches to a turn-based battle once a CPU battle unit engages it. Exploring lives in the [`exploration`](../../src/commonMain/kotlin/com/mkz/rpg/exploration) subdomain, and the screen uses it through `ExplorationApi`.

## The party

- Once the battle units are deployed, [`StartExploration`](../../src/commonMain/kotlin/com/mkz/rpg/exploration/usecases/commands/StartExploration.kt) puts every battle unit of the human player in the party, in their deployment order. The first one leads, and the rest follow it in a line.
- The CPU battle units wander around the tile they were deployed on while the party explores (see [CPU brain](cpu-brain.md#wandering)).
- A defeated battle unit leaves the party ([`LeaveParty`](../../src/commonMain/kotlin/com/mkz/rpg/exploration/usecases/commands/LeaveParty.kt)). When it was leading, the next one leads.

## Walking

The party walks one tile at a time, towards the arrow held on the virtual pad (north, east, south or west).

1. [`WalkParty`](../../src/commonMain/kotlin/com/mkz/rpg/exploration/usecases/commands/WalkParty.kt) plans a step for each battle unit of the party: the leader walks one tile towards the direction, and each follower walks to the tile the battle unit ahead of it left. Nothing happens when the leader can not occupy that tile (e.g.: void, another battle unit, out of the battlefield), or while the party is still walking or battling.
2. The steps are walked one after another, with `RequestWalkBattleUnit`. A walk spends no turn action, unlike a move in a battle, and publishes the same `BattleUnitMoved`, so the battlefield occupancy, the loot found on the way and the animation work as in a battle.
3. Each step is awaited until its battle unit occupies its tile (`BattlefieldTileOccupied`, see [`ResolvePartyStep`](../../src/commonMain/kotlin/com/mkz/rpg/exploration/usecases/commands/ResolvePartyStep.kt)). Only then is the tile left by the battle unit ahead vacant, so the next battle unit can step on it.
4. A follower that can not reach its tile (e.g.: the battle unit ahead could not leave it) skips its step, so the party always finishes walking. A follower that is not next to the battle unit ahead (e.g.: before the party is lined up) walks the shortest path to its tile.
5. Once every step has been walked or skipped, the party has walked (`PartyWalked`).

Holding an arrow keeps walking: [`VirtualPadView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/VirtualPadView.kt) asks to walk on every frame the arrow is held, and [`ExplorationPresenter`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/ExplorationPresenter.kt) only walks once the [presentation timeline](presentation-timeline.md) is idle, so the party can not walk ahead of its animation. The party steps are played quicker than a battle move (`EXPLORING_STEP_DURATION`, 75ms per tile).

## Inventory

There are no turns while exploring, so the inventory of the party can always be used:

- Tapping a battle unit of the party selects it: the battle HUD displays its info and its bag, and the call to action its item actions, in place of the virtual pad. It has no movement range, so tapping a tile does not move it, and its abilities can not be cast.
- Tapping an empty tile, a CPU battle unit or the selected battle unit again clears the selection, and the virtual pad is displayed again. Tapping another battle unit of the party selects it instead.
- When an item found on the way does not fit in the inventory (`InventoryFull`), the battle HUD and the call to action are displayed straight away, so the player can choose what to do with it. Tapping a tile once chosen displays the virtual pad again.
- The party does not walk while the virtual pad is hidden, even when an arrow is still held.

## Terrain effects

Every second while exploring, the battle units that stood still during that second receive the effect of their terrain (e.g.: lava hurts, water heals), the CPU battle units resting included:

- [`ExplorationPresenter`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/ExplorationPresenter.kt) counts the time elapsed on every frame, and calls [`ApplyStandingTerrainEffects`](../../src/commonMain/kotlin/com/mkz/rpg/exploration/usecases/commands/ApplyStandingTerrainEffects.kt) every second (`STANDING_TERRAIN_EFFECT_INTERVAL`).
- The exploration remembers the battle units that moved during the current second (`BattleUnitMoved`, see [`MarkBattleUnitMoved`](../../src/commonMain/kotlin/com/mkz/rpg/exploration/usecases/commands/MarkBattleUnitMoved.kt)), so walking over a tile does not apply its effect. A battle unit that walks onto lava is hurt once it has stood on it for a whole second.
- The effects are requested to the battlefield (`BattlefieldEvent.RequestEffectApplicationToOccupants`) for the battle units standing still, as they are for the battle units in the battle when a round starts.
- Nothing is applied while battling: the battle applies the terrain effects when its rounds start instead.
- Then, the next second starts (`ExplorationSecondStarted`), and the CPU battle units wander (see [CPU brain](cpu-brain.md#wandering)). They wander after the effects, so a battle unit that steps counts as moved during the new second.

A battle unit defeated by its terrain while exploring leaves the party. A player whose last battle units are defeated while exploring loses the game, as in a battle (`PlayerVictory` for their enemy, see [`DefeatPlayer`](../../src/commonMain/kotlin/com/mkz/rpg/battle/usecases/commands/DefeatPlayer.kt)). Once the game is over, the virtual pad is hidden and the seconds are no longer counted.

## Engaging

After the party starts exploring, every time it has walked and every time the CPU battle units have wandered, the CPU brain [`EngageEnemies`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBrain/usecases/commands/EngageEnemies.kt) looks for the CPU battle units that have a battle unit of the party within reach, with the same rule as in a battle (see [CPU brain](cpu-brain.md#engaging)). When there is any, the combat is engaged (`CombatEngaged`):

- The battle starts between the CPU battle units that engaged and the whole party. The CPU plays first, as it engaged. The other CPU battle units stay out of the battle: they do not play, and only wander once per round (see [CPU brain](cpu-brain.md#wandering)).
- When a battle round finishes and the CPU battle units out of the battle have wandered, the CPU brain [`UpdateEngagement`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBrain/usecases/commands/UpdateEngagement.kt) updates the engagement (`EngagementUpdated`): the CPU battle units out of the battle that now have a battle unit of the party within reach join it, and the CPU battle units in the battle without any leave it (e.g.: left behind). The next round only starts then, so the joining ones play from it.
- Only the battle units in the battle can be selected: tapping one out of it is as tapping an empty tile.
- The exploration pauses (`ExplorationPaused`), so the party no longer walks.
- The battle info announces the battle, and the battle HUD and the player call to action take the place of the virtual pad.

While exploring, the battle info displays "Exploring".

## Back to exploring

When the engagement is updated and only one player is left with battle units in the battle (e.g.: every CPU battle unit in it was defeated or left behind), [`StartNextRound`](../../src/commonMain/kotlin/com/mkz/rpg/battle/usecases/commands/StartNextRound.kt) finishes the battle without a winner (`BattleFinished`) instead of starting the next round:

- The exploration resumes (`ExplorationResumed`, see [`ResumeExploration`](../../src/commonMain/kotlin/com/mkz/rpg/exploration/usecases/commands/ResumeExploration.kt)), with the battle units of the party that were not defeated.
- The summons left (e.g.: the bees a knight summoned) vanish, whatever turns they had left, as they never explore ([`VanishSummonedBattleUnits`](../../src/commonMain/kotlin/com/mkz/rpg/battleUnit/usecases/commands/VanishSummonedBattleUnits.kt)). They flash and leave the battlefield, without being defeated: they drop no loot. A player whose last battle units were summons is defeated then, so the human player loses the game when only bees were left.
- The battle info announces that the battle is over and displays "Exploring" again. The selection is cleared, and the virtual pad takes the place of the battle HUD and the player call to action.
- The next engagement starts a new battle, which replaces the finished one.

So the battle only finishes when a round does: defeating the last CPU battle unit in it during the human turn still needs the turn to be finished. A player with no battle units left on the battlefield still loses the game (`PlayerVictory`), and the game does not go back to exploring then.
