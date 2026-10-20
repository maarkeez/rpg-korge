# Exploration

A new game starts exploring the battlefield, out of the battles: there are no turns. The human player walks their party with the virtual pad, and the game switches to a turn-based battle once a CPU battle unit engages it. Exploring lives in the [`party`](../../src/commonMain/kotlin/com/mkz/rpg/party) subdomain, and the screen uses it through `PartyApi`.

## The party

- Once the battle units are deployed, [`StartExploration`](../../src/commonMain/kotlin/com/mkz/rpg/party/usecases/commands/StartExploration.kt) puts every battle unit of the human player in the party, in their deployment order. The first one leads, and the rest follow it in a line.
- The CPU battle units wander around the tile they were deployed on while the party explores (see [CPU battle brain](cpu-battle-brain.md#wandering)).
- A defeated CPU battle unit respawns on the tile it was deployed on once the party has walked 50 steps since its defeat (see [CPU battle brain](cpu-battle-brain.md#respawning)).
- A defeated battle unit leaves the party ([`LeaveParty`](../../src/commonMain/kotlin/com/mkz/rpg/party/usecases/commands/LeaveParty.kt)). When it was leading, the next one leads.

## The virtual pad

[`VirtualPadView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/VirtualPadView.kt) is laid out as a Game Boy Color: the north, east, south and west arrows on the left, and the round B and A buttons on the right, A higher and further right than B.

- The arrows walk the party (see [Walking](#walking)).
- A talks to an npc next to the leader (see [Talking to an npc](#talking-to-an-npc)), recruits it when it is recruitable (see [Recruiting an npc](#recruiting-an-npc)), and shows the items it sells when it is a merchant (see [Shopping at a merchant](#shopping-at-a-merchant)).
- B is displayed, but does nothing for now.

## Walking

The party walks one tile at a time, towards the arrow held on the virtual pad (north, east, south or west). An npc blocks its tile as a battle unit does, so the party walks around it.

1. [`WalkParty`](../../src/commonMain/kotlin/com/mkz/rpg/party/usecases/commands/WalkParty.kt) plans a step for each battle unit of the party: the leader walks one tile towards the direction, and each follower walks to the tile the battle unit ahead of it left. Nothing happens when the leader can not occupy that tile (e.g.: void, another battle unit, out of the battlefield), or while the party is still walking or battling.
2. The steps are walked one after another, with `RequestWalkBattleUnit`. A walk spends no turn action, unlike a move in a battle, and publishes the same `BattleUnitMoved`, so the battlefield occupancy, the loot found on the way and the animation work as in a battle.
3. Each step is awaited until its battle unit occupies its tile (`BattlefieldTileOccupied`, see [`ResolvePartyStep`](../../src/commonMain/kotlin/com/mkz/rpg/party/usecases/commands/ResolvePartyStep.kt)). Only then is the tile left by the battle unit ahead vacant, so the next battle unit can step on it.
4. A follower that can not reach its tile (e.g.: the battle unit ahead could not leave it) skips its step, so the party always finishes walking. A follower that is not next to the battle unit ahead (e.g.: before the party is lined up) walks the shortest path to its tile.
5. Once every step has been walked or skipped, the party has walked (`PartyWalked`).

Holding an arrow keeps walking: [`VirtualPadView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/VirtualPadView.kt) asks to walk on every frame the arrow is held, and [`ExplorationPresenter`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/ExplorationPresenter.kt) only walks once the [presentation timeline](presentation-timeline.md) is idle, so the party can not walk ahead of its animation. The party steps are played quicker than a battle move (`EXPLORING_STEP_DURATION`, 75ms per tile).

## Talking to an npc

An npc ([`npc`](../../src/commonMain/kotlin/com/mkz/rpg/npc) subdomain, see [Game data](game-data.md)) stands on its tile. Pressing A talks to it when it stands next to the leader.

1. [`TalkToAdjacentNpc`](../../src/commonMain/kotlin/com/mkz/rpg/party/usecases/commands/TalkToAdjacentNpc.kt) looks for an npc on the tiles north, east, south and west of the leader, in that order, with no diagonals and whichever way the leader faces, and talks to the first one (`NpcTalkStarted`). Nothing happens when there is none, or when the party can not walk (e.g.: it is still walking, or battling).
2. While talking, the party is in its `TALKING` mode: `isExploring` is `false`, so time stops. The party does not walk, and no second starts, so nothing wanders, no terrain effect applies, and nobody engages.
3. [`NpcDialogView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/NpcDialogView.kt) takes the place of the virtual pad: the portrait of the npc on the left (`npc/<npc id>_portrait.png`, at 3x), its name on top and its line below it, wrapping when long. Tapping the battlefield does nothing.
4. Tapping anywhere on the dialog finishes the talk ([`FinishTalk`](../../src/commonMain/kotlin/com/mkz/rpg/party/usecases/commands/FinishTalk.kt), `NpcTalkFinished`): the virtual pad is displayed again, and time goes on. A merchant opens its shop instead (see [Shopping at a merchant](#shopping-at-a-merchant)).

A battle starting while talking (`PauseExploration`) abandons the talk, and the battle HUD takes the place of the dialog.

## Recruiting an npc

An npc with a unit in its game data is recruitable (e.g.: the viking, see [Game data](game-data.md)): it joins the party once the talk with it is finished. Until then, it is an npc as any other, displayed with the images of its unit.

1. Finishing the talk (`NpcTalkFinished`, with the npc talked to) recruits it ([`RecruitNpc`](../../src/commonMain/kotlin/com/mkz/rpg/npc/usecases/commands/RecruitNpc.kt)). Nothing happens for an npc that is not recruitable, e.g.: The Guy.
2. The npc leaves its tile (`NpcRecruited`): the battlefield vacates it, and the screen removes the npc.
3. A battle unit of its unit is deployed for the human player on that tile (`RequestDeployBattleUnit`), with full health and mana. It takes the id of the npc, which is deleted, so an npc is only recruited once, and pressing A next to it no longer talks to it.
4. The battle unit joins the party, last (`RequestJoinParty`, `PartyJoined`, see [`JoinParty`](../../src/commonMain/kotlin/com/mkz/rpg/party/usecases/commands/JoinParty.kt)), and the screen flashes it. It follows the battle unit ahead of it, walking the shortest path to it when it is not next to it.

The events are dispatched in the order they are published, so the tile is vacated before the battle unit is deployed on it, and the battle unit is deployed before it joins the party. Once recruited, it is a party member as any other: it has a bag, and leaves the party once defeated.

## Shopping at a merchant

An npc with a stock in its game data is a merchant (e.g.: the merchant, see [Game data](game-data.md)). Its dialog is displayed as any other, and the party keeps talking to it, so time stays stopped, until its shop is closed. The shop is screen state only: the party has no mode nor event for it.

1. Tapping the merchant's dialog opens its shop in place of the dialog, instead of finishing the talk ([`ShopPresenter`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/ShopPresenter.kt)). [`ShopView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/ShopView.kt) displays the merchant's name, the Buy and Sell tabs, Close, 12 slots as the inventory does, and the buyer.
2. The buyer is one party member at a time, the leader when the shop opens. A line under the slots displays its name, gold and bag (e.g.: `Knight · 20 G · Bag 11/12`, or `Bag full`), and one dot per party member, in party order, the buyer's one filled.
3. Swiping the shop left or right (at least 40 px) moves the buyer one dot left or right, stopping at the ends. Tapping a dot selects its party member. Every tile is dimmed but the buyer's and the merchant's, and the battlefield scrolls to the buyer.
4. The shop opens on the Buy tab, with the items the merchant sells. Tapping one frames it, and the call to action displays its name and price with Cancel and Buy (e.g.: `Hammer ........ 50 G`). Buy is disabled when the buyer can not afford it or its bag is full. After buying, the item stays selected, to buy another one.
5. The Sell tab displays the buyer's items, the equipped ones framed in gold. Tapping one displays its sell price with Cancel and Sell (e.g.: `Hammer ........ 25 G`). Sell is disabled for the only copy of an equipped item, and when the buyer's gold would go above 99,999 G. After selling, the selection is cleared.
6. Switching the buyer or the tab, Cancel, and tapping an empty slot clear the selection and hide the call to action.
7. Close finishes the talk (`NpcTalkFinished`): the shop is closed, the tiles are no longer dimmed, the virtual pad is displayed again, and time goes on.

A battle starting while shopping abandons the shop, as it abandons the dialog. See [Buying and selling](inventory.md#buying-and-selling) for the rules of each trade.

## Inventory

There are no turns while exploring, so the inventory of the party can always be used:

- Tapping a battle unit of the party selects it: the battle HUD displays its info and its bag, and the call to action its item actions, in place of the virtual pad. It has no movement range, so tapping a tile does not move it, and its abilities can not be cast.
- The selection stays as it is while the CPU battle units wander, so the inventory stays open until the player closes it: a tile occupied or vacated only displays the selected battle unit again when its own tile changes ([`RefreshMovementRange`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/battlefieldHud/usecases/commands/RefreshMovementRange.kt)).
- Tapping an empty tile, a CPU battle unit or the selected battle unit again clears the selection, and the virtual pad is displayed again. Tapping another battle unit of the party selects it instead.
- When an item found on the way does not fit in the inventory (`InventoryFull`), the battle HUD and the call to action are displayed straight away, so the player can choose what to do with it. Tapping a tile once chosen displays the virtual pad again.
- The party does not walk while the virtual pad is hidden, even when an arrow is still held.

## Terrain effects

Every second while exploring, the battle units that stood still during that second receive the effect of their terrain (e.g.: lava hurts, water heals), the CPU battle units resting included:

- [`ExplorationPresenter`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/ExplorationPresenter.kt) counts the time elapsed on every frame, and calls [`ApplyStandingTerrainEffects`](../../src/commonMain/kotlin/com/mkz/rpg/party/usecases/commands/ApplyStandingTerrainEffects.kt) every second (`STANDING_TERRAIN_EFFECT_INTERVAL`).
- The party remembers the battle units that moved during the current second (`BattleUnitMoved`, see [`MarkBattleUnitMoved`](../../src/commonMain/kotlin/com/mkz/rpg/party/usecases/commands/MarkBattleUnitMoved.kt)), so walking over a tile does not apply its effect. A battle unit that walks onto lava is hurt once it has stood on it for a whole second.
- The effects are requested to the battlefield (`BattlefieldEvent.RequestEffectApplicationToOccupants`) for the battle units standing still, as they are for the battle units in the battle when a round starts.
- Nothing is applied while battling: the battle applies the terrain effects when its rounds start instead.
- Then, the next second starts (`ExplorationSecondStarted`), and the CPU battle units wander (see [CPU battle brain](cpu-battle-brain.md#wandering)). They wander after the effects, so a battle unit that steps counts as moved during the new second.

A battle unit defeated by its terrain while exploring leaves the party. A player whose last battle units are defeated while exploring loses the game, as in a battle (`PlayerVictory` for their enemy, see [`DefeatPlayer`](../../src/commonMain/kotlin/com/mkz/rpg/battle/usecases/commands/DefeatPlayer.kt)). Once the game is over, the virtual pad is hidden and the seconds are no longer counted.

## Engaging

After the party starts exploring, every time it has walked and every time the CPU battle units have wandered, the CPU battle brain [`EngageEnemies`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBattleBrain/usecases/commands/EngageEnemies.kt) looks for the CPU battle units that have a battle unit of the party within reach, with the same rule as in a battle (see [CPU battle brain](cpu-battle-brain.md#engaging)). When there is any, the combat is engaged (`CombatEngaged`):

- The battle starts between the CPU battle units that engaged and the whole party. The CPU plays first, as it engaged. The other CPU battle units stay out of the battle: they do not play, and only wander once per round (see [CPU battle brain](cpu-battle-brain.md#wandering)).
- When a battle round finishes and the CPU battle units out of the battle have wandered, the CPU battle brain [`UpdateEngagement`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBattleBrain/usecases/commands/UpdateEngagement.kt) updates the engagement (`EngagementUpdated`): the CPU battle units out of the battle that now have a battle unit of the party within reach join it, and the CPU battle units in the battle without any leave it (e.g.: left behind). The next round only starts then, so the joining ones play from it.
- Only the battle units in the battle can be selected: tapping one out of it is as tapping an empty tile.
- The exploration pauses (`ExplorationPaused`), so the party no longer walks.
- The battle info announces the battle, and the battle HUD and the player call to action take the place of the virtual pad.

While exploring, the battle info displays "Exploring".

## Back to exploring

When the engagement is updated and only one player is left with battle units in the battle (e.g.: every CPU battle unit in it was defeated or left behind), [`StartNextRound`](../../src/commonMain/kotlin/com/mkz/rpg/battle/usecases/commands/StartNextRound.kt) finishes the battle without a winner (`BattleFinished`) instead of starting the next round:

- The exploration resumes (`ExplorationResumed`, see [`ResumeExploration`](../../src/commonMain/kotlin/com/mkz/rpg/party/usecases/commands/ResumeExploration.kt)), with the battle units of the party that were not defeated.
- The summons left (e.g.: the bees a knight summoned) vanish, whatever turns they had left, as they never explore ([`VanishSummonedBattleUnits`](../../src/commonMain/kotlin/com/mkz/rpg/battleUnit/usecases/commands/VanishSummonedBattleUnits.kt)). They flash and leave the battlefield, without being defeated: they drop no loot. A player whose last battle units were summons is defeated then, so the human player loses the game when only bees were left.
- The battle info announces that the battle is over and displays "Exploring" again. The selection is cleared, and the virtual pad takes the place of the battle HUD and the player call to action.
- The next engagement starts a new battle, which replaces the finished one.

So the battle only finishes when a round does: defeating the last CPU battle unit in it during the human turn still needs the turn to be finished. A player with no battle units left on the battlefield still loses the game (`PlayerVictory`), and the game does not go back to exploring then.
