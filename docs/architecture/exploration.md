# Exploration

A new game starts exploring the battlefield, out of the battles: there are no turns. The human player walks their party with the virtual pad, and the game switches to a turn-based battle once a CPU battle unit engages it. Exploring lives in the [`party`](../../src/commonMain/kotlin/com/mkz/rpg/party) subdomain, and the screen uses it through `PartyApi`.

## The party

- Once the battle units are deployed, [`StartExploration`](../../src/commonMain/kotlin/com/mkz/rpg/party/usecases/commands/StartExploration.kt) puts every battle unit of the human player in the party, in their deployment order. The first one leads, until the player chooses another one (see [Choosing the leader](#choosing-the-leader)), and the rest follow it in a line.
- The CPU battle units wander around the tile they were deployed on while the party explores (see [CPU battle brain](cpu-battle-brain.md#wandering)).
- A defeated CPU battle unit respawns on the tile it was deployed on once the party has walked 50 steps since its defeat (see [CPU battle brain](cpu-battle-brain.md#respawning)).
- A defeated battle unit leaves the party ([`LeaveParty`](../../src/commonMain/kotlin/com/mkz/rpg/party/usecases/commands/LeaveParty.kt)). When it was leading, the next one leads.

## The virtual pad

[`VirtualPadView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/VirtualPadView.kt) is laid out as a Game Boy Color: the north, east, south and west arrows on the left, the round B and A buttons on the right, A higher and further right than B, and the Start pill centred at the bottom, between both. The arrows are grey squares as big as A and B, of the same grey as Start. The pad is laid out from its bottom, lower than the battle HUD it takes the place of: Start lines up with the bottom of Finish turn, so the lowest button is on the same line while exploring and battling.

- The arrows walk the party (see [Walking](#walking)).
- A talks to an npc next to the leader (see [Talking to an npc](#talking-to-an-npc)), recruits it when it is recruitable (see [Recruiting an npc](#recruiting-an-npc)), and trades with it when it is a merchant (see [Shopping at a merchant](#shopping-at-a-merchant)).
- B opens the party formation, where the player chooses who leads (see [Choosing the leader](#choosing-the-leader)).
- Start pauses the game, to save it or quit to the menu (see [Pausing](#pausing)).

## Walking

The party walks one tile at a time, towards the arrow held on the virtual pad (north, east, south or west). An npc blocks its tile as a battle unit does, so the party walks around it.

1. [`WalkParty`](../../src/commonMain/kotlin/com/mkz/rpg/party/usecases/commands/WalkParty.kt) plans a step for each battle unit of the party, in line order (see [Swapping with a party member](#swapping-with-a-party-member)): the leader walks one tile towards the direction, and each follower walks to the tile the battle unit ahead of it left. Nothing happens when the leader can not occupy that tile (e.g.: void, an npc, a CPU battle unit, out of the battlefield), or while the party is still walking or battling. A party member on that tile swaps with the leader instead.
2. The steps are walked one after another, with `RequestWalkBattleUnit`. A walk spends no turn action, unlike a move in a battle, and publishes the same `BattleUnitMoved`, so the battlefield occupancy, the loot found on the way and the animation work as in a battle.
3. Each step is awaited until its battle unit occupies its tile (`BattlefieldTileOccupied`, see [`ResolvePartyStep`](../../src/commonMain/kotlin/com/mkz/rpg/party/usecases/commands/ResolvePartyStep.kt)). Only then is the tile left by the battle unit ahead vacant, so the next battle unit can step on it.
4. A follower that can not reach its tile (e.g.: the battle unit ahead could not leave it) skips its step, so the party always finishes walking. A follower that is not next to the battle unit ahead (e.g.: before the party is lined up) walks the shortest path to its tile.
5. Once every step has been walked or skipped, the party has walked (`PartyWalked`).

### Swapping with a party member

A party member never blocks the leader, so the party can always turn around, e.g.: at the end of a dead-end corridor.

1. When any party member stands on the leader's next tile, the leader swaps tiles with it, as its single step: the other followers stay where they are. [`Party.swapLeader`](../../src/commonMain/kotlin/com/mkz/rpg/party/domain/Party.kt) awaits the leader's step onto the member's tile, and `WalkParty` requests the swap (`RequestSwapBattleUnits`).
2. [`SwapBattleUnits`](../../src/commonMain/kotlin/com/mkz/rpg/battleUnit/usecases/commands/SwapBattleUnits.kt) walks both battle units onto each other's tile: each publishes its `BattleUnitMoved`, naming the battle unit it swapped with (`swappedWithBattleUnitId`), so both count as moved and find the loot on their tiles, as after any step. It requests the battlefield to swap both occupants at once (`RequestSwapOccupants`, see [`SwapOccupants`](../../src/commonMain/kotlin/com/mkz/rpg/battlefield/usecases/commands/SwapOccupants.kt)), so a tile never holds two of them, and the battlefield announces both occupied tiles.
3. The leader's occupied tile resolves its step, so the party has walked (`PartyWalked`), and the CPU battle units may engage it.
4. The screen animates the swap ([`BattlefieldView.swapBattleUnits`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/BattlefieldView.kt)): both battle units glide to each other's tile at once, for `SWAP_DURATION` (150ms), while the camera trails the leader. The leader hops over the member, drawn over it, and the member slides straight underneath. The presenter names the party leader, whichever of the two `BattleUnitMoved` came first.

The followers walk in **line order**, which is kept apart from the party order (the next leader, the shop's buyer). It starts in party order, and is rebuilt after each swap: from the leader, the closest remaining member, in rows plus columns, follows next, ties broken by party order. So each follower follows the member closest to it, and none is trapped behind another one in a corridor. The line goes back to the party order when the exploration starts or resumes, a recruited member joins the end of the line, and a defeated one leaves it.

In a battle, an ally still blocks its tile: only the exploring party swaps.

Holding an arrow keeps walking: [`VirtualPadView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/VirtualPadView.kt) asks to walk on every frame the arrow is held, and [`ExplorationPresenter`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/ExplorationPresenter.kt) only walks once the [presentation timeline](presentation-timeline.md) is idle, so the party can not walk ahead of its animation. The battle units glide from tile to tile. The steps of one walk glide at the same time, the followers together with the leader (`BattleSceneTimeline.playTogether`), so the party walks a tile in `EXPLORING_STEP_DURATION` (250ms) whatever its size, and a held arrow walks it smoothly.

A tap is never lost while the timeline plays (e.g.: the party still gliding, or a recruit flashing):

- The pad reports each press once, before holding it. The presenter keeps the last arrow or A pressed, a newer press replacing it, so tapping several times while the timeline plays walks one step, towards the last arrow.
- On the first frame the timeline is idle, the presenter acts on it: it walks the party, or talks to the npc next to the leader. It is acted on once, even when nothing happens (e.g.: a blocked tile), and a held arrow walking towards it acts on it.
- It is forgotten when the pad is hidden meanwhile (e.g.: a battle starts, or a battle unit of the party is selected).

The camera trails the leader, as in A Link to the Past, not the followers ([`BattlefieldPresenter`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/BattlefieldPresenter.kt)):

- It moves on every frame, outside the presentation timeline, so the party never waits for it.
- The leader moves freely in the 2x2 tiles at the center of the viewport (the deadzone). Once it walks past them, the camera moves with it, pixel for pixel, so it sees 3 tiles ahead. Clamped to the battlefield, so walking along its border does not scroll it.
- When the camera is more than a tile away from the deadzone (the exploration starts or resumes, a teleport), it eases there, in about 250ms.
- Dragging the battlefield pauses it, to look around. The leader's next step resumes it.
- A flash, a loot pick-up and the shop's buyer do not scroll the battlefield meanwhile: they are party members, next to the leader.

When a battle starts (`ExplorationPaused`), the camera stops trailing, and the battlefield centers each battle unit instead, waiting for the scroll before it moves.

## Talking to an npc

An npc ([`npc`](../../src/commonMain/kotlin/com/mkz/rpg/npc) subdomain, see [Game data](game-data.md)) stands on its tile. Pressing A talks to it when it stands next to the leader.

1. [`TalkToAdjacentNpc`](../../src/commonMain/kotlin/com/mkz/rpg/npc/usecases/commands/TalkToAdjacentNpc.kt) looks for an npc on the tiles north, east, south and west of the leader, in that order, with no diagonals and whichever way the leader faces ([`SearchNpcNextTo`](../../src/commonMain/kotlin/com/mkz/rpg/npc/usecases/queries/SearchNpcNextTo.kt)), and requests the party to talk to the first one (`RequestStartTalk`). The party talks to it ([`StartTalk`](../../src/commonMain/kotlin/com/mkz/rpg/party/usecases/commands/StartTalk.kt), `NpcTalkStarted`). Nothing happens when there is none, or when the party can not walk (e.g.: it is still walking, or battling).
2. While talking, the party is in its `TALKING` mode: `isExploring` is `false`, so time stops. The party does not walk, and no second starts, so nothing wanders, no terrain effect applies, and nobody engages.
3. [`NpcDialogView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/NpcDialogView.kt) takes the place of the virtual pad: the portrait of the npc on the left (`npc/<npc id>_portrait.png`, at 3x), its name on top and its line below it, wrapping when long. Tapping the battlefield does nothing.
4. Tapping anywhere on the dialog finishes the talk ([`FinishTalk`](../../src/commonMain/kotlin/com/mkz/rpg/party/usecases/commands/FinishTalk.kt), `NpcTalkFinished`): the virtual pad is displayed again, and time goes on. A merchant's dialog is only left with its Leave choice instead (see [Shopping at a merchant](#shopping-at-a-merchant)).

A battle starting while talking (`PauseExploration`) abandons the talk, and the battle HUD takes the place of the dialog.

## Recruiting an npc

An npc with a unit in its game data is recruitable (e.g.: the viking, see [Game data](game-data.md)): it joins the party once the talk with it is finished. Until then, it is an npc as any other, displayed with the images of its unit.

1. Finishing the talk (`NpcTalkFinished`, with the npc talked to) recruits it ([`RecruitNpc`](../../src/commonMain/kotlin/com/mkz/rpg/npc/usecases/commands/RecruitNpc.kt)). Nothing happens for an npc that is not recruitable, e.g.: the elder.
2. The npc leaves its tile (`NpcRecruited`): the battlefield vacates it, and the screen removes the npc.
3. The party recruits a battle unit of its unit on that tile (`RequestRecruitMember`, see [`RecruitMember`](../../src/commonMain/kotlin/com/mkz/rpg/party/usecases/commands/RecruitMember.kt)): it is deployed for the party player (`RequestDeployBattleUnit`), with full health and mana. It takes the id of the npc, which is deleted, so an npc is only recruited once, and pressing A next to it no longer talks to it.
4. The battle unit joins the party, last (`RequestJoinParty`, `PartyJoined`, see [`JoinParty`](../../src/commonMain/kotlin/com/mkz/rpg/party/usecases/commands/JoinParty.kt)), and the screen flashes it. It follows the battle unit ahead of it, walking the shortest path to it when it is not next to it.

The events are dispatched in the order they are published, so the tile is vacated before the battle unit is deployed on it, and the battle unit is deployed before it joins the party, as the party publishes both requests together. Once recruited, it is a party member as any other: it has a bag, and leaves the party once defeated.

## Shopping at a merchant

An npc with a stock in its game data is a merchant (e.g.: the blacksmith, see [Game data](game-data.md)). Its dialog is displayed as any other, with Buy, Sell and Leave under its line, and the party keeps talking to it, so time stays stopped, until Leave. Tapping the dialog elsewhere does nothing. The shop is screen state only: the party has no mode nor event for it.

1. Buy or Sell opens the merchant's shop in place of the dialog, to buy or to sell ([`ShopPresenter`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/ShopPresenter.kt)). [`ShopView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/ShopView.kt) is one panel: the buyer on top, 12 slots as the inventory does, and a footer with every button the player taps.
2. The buyer is one party member at a time, the leader when the shop opens. The header displays its name, gold and bag (e.g.: `< Knight · 20 G · Bag 11/12 >`, or `Bag full`).
3. The arrows, or swiping the shop left or right (at least 40 px), move the buyer to the previous or next party member, in party order, and are disabled at the ends. Every tile is dimmed but the buyer's and the merchant's, and the battlefield scrolls to the buyer.
4. Buying displays the items the merchant sells, and the footer `Tap an item to buy`. Tapping one frames it, and the footer displays its name and price with Buy (e.g.: `Hammer ........ 50 G  Buy`). When the buyer can not afford it or its bag is full, Buy is disabled and reads why: `Not enough gold` or `Bag full`. After buying, the item stays selected, to buy another one.
5. Selling displays the buyer's items, the equipped ones framed in gold, and the footer `Tap an item to sell`, or `Nothing to sell`. Tapping one displays its sell price with Sell (e.g.: `Hammer ........ 25 G  Sell`). Sell is disabled and reads why for the only copy of an equipped item (`Equipped`), and when the buyer's gold would go above 99,999 G (`Gold full`). After selling, the selection is cleared.
6. The items the buyer can not buy (or sell) are dimmed, so switching the buyer shows at once what it can trade.
7. Switching the buyer, and tapping the selected item again or an empty slot, clear the selection, and the footer displays the hint again.
8. Back, on the left of the footer, closes the shop, the tiles are no longer dimmed, and the merchant's dialog is displayed again, still talking, to buy, sell, or Leave. Leave finishes the talk (`NpcTalkFinished`): the virtual pad is displayed again, and time goes on.

A battle starting while shopping abandons the shop, as it abandons the dialog. See [Buying and selling](inventory.md#buying-and-selling) for the rules of each trade.

## Choosing the leader

The leader walks in front, so it is usually the first battle unit of the party within reach of a CPU battle unit, and
the first one hit, as the CPU plays first once it engages. The CPU battle
brain does not target the leader as such. While exploring, B opens the party formation, where the player sets the party
order, and so who leads:

1. B is handled as A: once the timeline is idle, and while the pad is displayed. [`OpenFormation`](../../src/commonMain/kotlin/com/mkz/rpg/party/usecases/commands/OpenFormation.kt)
   only opens it when the party can walk (`FormationOpened`). Time stops meanwhile, as while talking: the party is in
   formation, so it does not walk, no second starts, the CPU battle units do not wander, no terrain effect is applied,
   and tapping the battlefield does nothing.
2. [`FormationPresenter`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/FormationPresenter.kt) displays
   [`FormationView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/FormationView.kt) in place of the pad:
   a row per party member, in party order, with the crown on the leader, its portrait, its name, and its health and mana
   bars. Every tile but the party's is dimmed.
3. Tapping a member selects it, framing its row and its tile. Tapping another one swaps both, and tapping the selected
   one again clears the selection.
4. Close applies the order displayed ([`CloseFormation`](../../src/commonMain/kotlin/com/mkz/rpg/party/usecases/commands/CloseFormation.kt),
   `FormationClosed`), and the pad is displayed again. Its first member leads.

Nobody moves when the order changes: the line is rebuilt from the new leader, as after a swap, unless the order is the
same. On its next walk, the leader reaches the front by swapping with the members in its way. The camera eases to the
new leader, and the CPU battle units in sight, the npc A talks to and the battle unit selected when a human turn starts
follow it. The party order also decides who leads next when the leader is defeated, and the order of the shop's and the
inventory's arrows. A recruited member still joins last.

## Pausing

While exploring, Start opens the pause screen in place of the pad:

1. Start is handled as B: once the timeline is idle, and while the pad is displayed. [`OpenPause`](../../src/commonMain/kotlin/com/mkz/rpg/party/usecases/commands/OpenPause.kt)
   only pauses when the party can walk (`PauseOpened`). Time stops meanwhile, as in the formation: the party is
   `PAUSED`, so it does not walk, no second starts, the CPU battle units do not wander, no terrain effect is applied,
   and tapping the battlefield does nothing.
2. [`PausePresenter`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/PausePresenter.kt) displays
   [`PauseView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/PauseView.kt) in place of the pad:
   Resume, Save and Quit to menu.
3. Resume explores again ([`ClosePause`](../../src/commonMain/kotlin/com/mkz/rpg/party/usecases/commands/ClosePause.kt),
   `PauseClosed`), and the pad is displayed again.
4. Save lists the save slots, and saves the game into one (see [Save and load](save-and-load.md#saving)).
5. Quit to menu asks to confirm, in [`ConfirmDialogView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/ConfirmDialogView.kt),
   as the progress since the last save is lost, and goes back to the menu.

## Inventory

There are no turns while exploring, so the inventory of the party can always be used:

- Tapping a battle unit of the party selects it: the battle HUD displays its info and its bag in place of the virtual pad, and the call to action below it the item actions once an item is tapped. There is no finish turn, as there are no turns: the call to action is empty otherwise. It has no movement range, so tapping a tile does not move it, and its abilities can not be cast.
- The selection stays as it is while the CPU battle units wander, so the inventory stays open until the player closes it: a tile occupied or vacated only displays the selected battle unit again when its own tile changes ([`RefreshMovementRange`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/battlefieldHud/usecases/commands/RefreshMovementRange.kt)).
- Tapping an empty tile, a CPU battle unit or the selected battle unit again clears the selection, and the virtual pad is displayed again. Tapping another battle unit of the party selects it instead.
- The inventory's title line names the party member between two arrows, its gold before the next one: `< Knight ........ 20 G >`. The arrows, or swiping the inventory left or right (at least 40 px), switch it to the previous or next party member, in party order, and are disabled at the ends. The member switched to is selected on the battlefield, as if its tile was tapped, so Close displays its info. The selected item is cleared, and switching waits while giving an item or choosing what to do with a found one.
- The item actions also offer Give (see [Giving](inventory.md#giving)). Give dims every tile but the ones of the party members that can receive the item, and the call to action reads `Tap a party member to give the Hammer` with Cancel ([`InventoryPresenter`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/InventoryPresenter.kt), which takes the tapped tiles before the battlefield HUD meanwhile). Tapping a receiver's tile gives the item: the giver's bag stays open with nothing selected, and the receiver flashes. Any other tap is ignored (dimmed tiles, the giver's tile, the bag's slots and Close), and Cancel displays the item's actions again. Give is disabled for the only copy of an equipped item, and reads why when no one can receive it: `No one` or `Bags full`. Time goes on meanwhile, and a battle starting abandons the giving.
- When an item found on the way does not fit in the inventory (`InventoryFull`), the battle HUD and the call to action are displayed straight away, so the player can choose what to do with it. Tapping a tile once chosen displays the virtual pad again.
- The party does not walk while the virtual pad is hidden, even when an arrow is still held.

## Terrain effects

Every second while exploring, the battle units that stood still during that second receive the effect of their terrain (e.g.: lava hurts, water heals), the CPU battle units resting included:

- [`ExplorationPresenter`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/ExplorationPresenter.kt) counts the time elapsed on every frame, and calls [`ApplyStandingTerrainEffects`](../../src/commonMain/kotlin/com/mkz/rpg/party/usecases/commands/ApplyStandingTerrainEffects.kt) every second (`STANDING_TERRAIN_EFFECT_INTERVAL`).
- The party remembers the battle units that moved during the current second (`BattleUnitMoved`, see [`MarkBattleUnitMoved`](../../src/commonMain/kotlin/com/mkz/rpg/party/usecases/commands/MarkBattleUnitMoved.kt)), so walking over a tile does not apply its effect. A battle unit that walks onto lava is hurt once it has stood on it for a whole second.
- The effects are requested to the battlefield (`BattlefieldEvent.RequestEffectApplicationToOccupants`) for the battle units standing still, as they are for the battle units in the battle when a round starts. The battlefield announces each effect of a terrain (`TerrainEffectTriggered`, see [`TriggerTerrainEffects`](../../src/commonMain/kotlin/com/mkz/rpg/battlefield/usecases/commands/TriggerTerrainEffects.kt)), and the battle units apply it.
- Nothing is applied while battling: the battle applies the terrain effects when its rounds start instead.
- A battle unit receiving an effect flashes, and when its health changes, its health bar is displayed over its tile meanwhile, so the player sees the lava hurting and the water healing (see [Applying effects](presentation-timeline.md#applying-effects)).
- The battle units flash alongside the timeline, so the party walks meanwhile, whatever the number of battle units flashing. A flash stops once its battle unit walks away.
- Then, the next second starts (`ExplorationSecondStarted`), and the CPU battle units wander (see [CPU battle brain](cpu-battle-brain.md#wandering)). They wander after the effects, so a battle unit that steps counts as moved during the new second.

A battle unit defeated by its terrain while exploring leaves the party. A player whose last battle units are defeated while exploring loses the game, as in a battle (`PlayerVictory` for their enemy, see [`DefeatPlayer`](../../src/commonMain/kotlin/com/mkz/rpg/battle/usecases/commands/DefeatPlayer.kt)). Once the game is over, the virtual pad is hidden and the seconds are no longer counted.

## Engaging

After the party starts exploring, every time it has walked and every time the CPU battle units have wandered, the CPU battle brain [`EngageEnemies`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBattleBrain/usecases/commands/EngageEnemies.kt) looks for the CPU battle units that have a battle unit of the party within reach, with the same rule as in a battle (see [CPU battle brain](cpu-battle-brain.md#engaging)). When there is any, the combat is engaged (`CombatEngaged`), and the battle is requested to start (`RequestStartBattle`):

- The battle starts between the CPU battle units that engaged and the whole party. The CPU plays first, as it engaged. The other CPU battle units stay out of the battle: they do not play, and only wander once per round (see [CPU battle brain](cpu-battle-brain.md#wandering)).
- When a battle round finishes and the CPU battle units out of the battle have wandered, the CPU battle brain [`UpdateEngagement`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBattleBrain/usecases/commands/UpdateEngagement.kt) updates the engagement (`EngagementUpdated`), and requests the next round (`RequestStartNextRound`): the CPU battle units out of the battle that now have a battle unit of the party within reach join it, and the CPU battle units in the battle without any leave it (e.g.: left behind). The next round only starts then, so the joining ones play from it.
- Only the battle units in the battle can be selected: tapping one out of it is as tapping an empty tile.
- The exploration pauses (`ExplorationPaused`), so the party no longer walks.
- The battle HUD and the player call to action, with the finish turn, take the place of the virtual pad as soon as the leader lands on its last tile (`BattleStarted`, played with `BattleSceneTimeline.playNext`), and the battle info announces the battle. Hiding the pad after the announcements (about 950ms later) made the party look frozen.

While exploring, the battle info displays "Exploring".

## Back to exploring

The battle finishes without a winner (`BattleFinished`) once only one player is left with battle units in it, e.g.: every CPU battle unit in it was defeated or left behind. It is checked as soon as it happens, without waiting for the round to finish:

- When a battle unit leaves the battle during a round (e.g.: the last CPU battle unit engaged is defeated, during any turn), [`LeaveBattle`](../../src/commonMain/kotlin/com/mkz/rpg/battle/usecases/commands/LeaveBattle.kt) disengages the battle (`BattleDisengaged`). The CPU battle brain [`ReengageEnemies`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBattleBrain/usecases/commands/ReengageEnemies.kt) has the CPU battle units out of the battle with an enemy within reach join it, without wandering first, so the battle goes on, and they play from the next round. [`FinishDisengagedBattle`](../../src/commonMain/kotlin/com/mkz/rpg/battle/usecases/commands/FinishDisengagedBattle.kt) finishes it when none joins. A CPU summon left in the battle keeps it going, until it is defeated or vanishes.
- When the engagement is updated after a round, and the CPU battle units in it left it (e.g.: left behind), [`StartNextRound`](../../src/commonMain/kotlin/com/mkz/rpg/battle/usecases/commands/StartNextRound.kt) finishes it instead of starting the next round.

Once the battle is finished, a turn finish requested afterwards (e.g.: by the rest of the CPU turn) does nothing. Then:

- The exploration resumes (`ExplorationResumed`, see [`ResumeExploration`](../../src/commonMain/kotlin/com/mkz/rpg/party/usecases/commands/ResumeExploration.kt)), with the battle units of the party that were not defeated.
- The summons left (e.g.: the bees a knight summoned) vanish, whatever turns they had left, as they never explore ([`VanishSummonedBattleUnits`](../../src/commonMain/kotlin/com/mkz/rpg/battleUnit/usecases/commands/VanishSummonedBattleUnits.kt)). They flash and leave the battlefield, without being defeated: they drop no loot. A player whose last battle units were summons is defeated then, so the human player loses the game when only bees were left.
- The battle info announces that the battle is over and displays "Exploring" again. The selection is cleared, the finish turn is removed from the player call to action, and the virtual pad takes the place of the battle HUD and the call to action.
- The next engagement starts a new battle, which replaces the finished one.

So defeating the last CPU battle unit engaged during the human turn explores again straight away, without finishing the turn. A player with no battle units left on the battlefield still loses the game (`PlayerVictory`), and the game does not go back to exploring then: the player is defeated before the disengaged battle is checked, so only the victory is displayed.
