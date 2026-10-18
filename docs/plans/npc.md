# Plan: NPCs and talking to them

> **Status:** planned, none of the 3 milestones is done.
>
> **Summary:** NPCs stand on the battlefield: The Guy stands at row 10, column 2. An NPC is no battle unit: it blocks its
> tile for everyone, and nobody can attack it. The virtual pad gets a Game Boy Color layout, with an A and a B button.
> While exploring, pressing A next to an NPC talks to it: its dialog takes the place of the pad ("The Guy: G'day
> bloke!"), and time stops until the dialog is tapped.

## Decisions

| Topic            | Decision                                                                                                                                                                                                  |
|------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| What an NPC is   | No battle unit: no health, no player, no turns. It blocks its tile for everyone (the party walking, the CPU wandering, the paths). Nobody can attack it, the party included. No terrain effect applies to it. It stands still, also during the battles. |
| Game data        | `npc/<npc id>.toml`: its name, its line, and its tile. The Guy (`the-guy`) says "G'day bloke!", at row 10, column 2 (counting from 0, on grass). The game fails to start when its tile can not be occupied.  |
| Assets           | `npc/<npc id>.png` (16x16) on the battlefield, and `npc/<npc id>_portrait.png` (32x32) in the dialog. The Guy's portrait is its sprite enlarged twice, for now.                                            |
| Pad              | A Game Boy Color layout: the arrows on the left, and the round B and A buttons on the right, A higher and further right than B. Labelled, in palette colours.                                              |
| A                | Talks to an NPC on one of the 4 tiles next to the leader (north, east, south or west, no diagonals, no facing direction), the first one in that order. Nothing happens when there is none, or while the party is still walking. |
| B                | Displayed, but does nothing for now.                                                                                                                                                                      |
| Dialog           | It takes the place of the whole pad: the portrait on the left, at 3x (96px) as the battle unit portraits, the name on top and the line below it, wrapping when long. The whole line at once, no typewriter. |
| Closing          | Tapping anywhere on the dialog closes it, and the pad is displayed again.                                                                                                                                 |
| While talking    | Time stops: the party does not walk, the seconds are not counted, so nothing wanders, no terrain effect applies, and nobody engages. Tapping the battlefield does nothing.                                |
| Tapping the NPC  | As tapping an empty tile: the selection is cleared.                                                                                                                                                       |

For example, the knight leads the party next to The Guy:

| The player                                   | The game                                                                                 |
|----------------------------------------------|------------------------------------------------------------------------------------------|
| Walks the knight to (10,3), east of The Guy  | The Guy blocks (10,2): the knight can not walk west.                                     |
| Presses A                                    | The dialog takes the place of the pad: The Guy's portrait, "The Guy" and "G'day bloke!". |
| Waits 5 seconds                              | Nothing happens: the rats do not wander, the terrains do not hurt nor heal.              |
| Taps the battlefield                         | Nothing happens.                                                                          |
| Taps the dialog                              | The pad is displayed again, and time goes on.                                            |

## The Guy's sprite

The sprite uses 8 colours off the FamiCube palette. Each one is replaced with its closest palette colour, except the
outline, which takes the near black of the other units' outlines (`unit/knight.png`, `unit/rat.png`, `unit/goat.png`):

| Off-palette colour | Palette colour    |
|--------------------|-------------------|
| `(63, 38, 49)`     | `(21, 21, 21)`    |
| `(247, 194, 130)`  | `(245, 183, 132)` |
| `(189, 108, 74)`   | `(174, 108, 55)`  |
| `(118, 59, 54)`    | `(130, 60, 61)`   |
| `(192, 203, 220)`  | `(215, 215, 215)` |
| `(225, 154, 101)`  | `(197, 151, 130)` |
| `(38, 43, 68)`     | `(33, 22, 64)`    |
| `(139, 155, 180)`  | `(168, 168, 168)` |

The portrait is the fixed sprite enlarged twice with no smoothing (each pixel becomes 2x2), so it is 32x32 like the unit
portraits.

## Why an NPC is no battle unit

The battle units belong to a player, and the CPU and the abilities target the battle units of the enemy player. An NPC
as a battle unit would need a third player, which `SearchEnemyPlayer`, the victory and the engagement do not expect, and
every rule would have to leave it out (the turns, the wandering, the terrain effects, the targeting).

So the NPCs live in their own subdomain, and only occupy their tile on the battlefield. The battlefield does not tell
battle units and NPCs apart: an occupant blocks its tile. The ones reading an occupant as a battle unit already skip the
occupants that are not battle units (`WhereCanCast`, `WhatShouldCast`, `ProcessTileSelected`), except the standing
terrain effects, which are changed so.

## npc (new subdomain)

### Domain

- Add `Npc(id, name, line, row, column)`, with `Npc.place(...)`, which refuses an empty name (`NpcNameRequired`) or an
  empty line (`NpcLineRequired`), and adds `NpcPlaced(npcId, row, column)`.
- Add `NpcEvent.RequestInitialiseNpcs` and `NpcEvent.NpcPlaced(npcId, row, column)`.
- Add `NpcRepository` (`save`, `searchById`, `searchAll`).

### Use cases

#### Commands

- Add `InitialiseNpcs()`: on `RequestInitialiseNpcs`, places every NPC of the game data, refusing a tile that can not be
  occupied (`CanBattlefieldTileBeOccupied`, e.g.: void, out of the battlefield). The battle units deployed on the same
  step occupy their tiles once `BattleUnitDeployed` is handled, before `NpcPlaced`: an NPC on the tile of a battle unit
  fails to occupy it (`TileIsNotVacant`), so the game fails to start too.

#### Queries

- Add `SearchNpcById(npcId)` and `SearchNpcAt(row, column)`.

#### Services

- Add `NpcLoader`.

### Adapters

- Add `ResourcesNpcLoader` (resources), `InMemoryNpcRepository` (storage), `OnRequestInitialiseNpcs` (events), and
  `NpcApi` (presentation).

## battlesetup

### Use cases

#### Commands

- Modify `SetupBattle` so it publishes `RequestInitialiseNpcs` after `RequestInitialiseBattleUnits`, before
  `RequestStartExploration`, as the NPCs need the battlefield.

## battlefield

### Adapters

#### Events

- Add `OnNpcPlaced`: its tile is occupied (`UpdateBattlefieldOccupancy`), as on `BattleUnitDeployed`. The
  `BattlefieldTileOccupied` it publishes resolves no step, as no one awaits the NPC.

## exploration

### Domain

- Add the `TALKING` mode, and `Exploration.talk(npcId)`: only when the party can walk (exploring, and done walking), it
  talks to the NPC, and adds `NpcTalkStarted(playerId, npcId)`. While talking, the party can not walk, and no second
  starts.
- Add `Exploration.finishTalk()`: it explores again, and adds `NpcTalkFinished(playerId)`. Nothing changes when it is not
  talking.
- Add `Dto.talkingNpcId: String?`. `isExploring` is `false` while talking.
- Add `ExplorationEvent.NpcTalkStarted(playerId, npcId)` and `NpcTalkFinished(playerId)`.

### Use cases

#### Commands

- Add `TalkToAdjacentNpc()`: talks to the first NPC (`SearchNpcAt`) on the tiles north, east, south and west of the
  leader. Nothing happens when the party can not walk or there is none.
- Add `FinishTalk()`.
- Modify `ApplyStandingTerrainEffects` so only the battle units standing still receive the effect of their terrain
  (`SearchBattleUnitById`), not the NPCs. It already does nothing while not exploring, so while talking.

### Adapters

#### Presentation

- Modify `ExplorationApi` with `npcApi`, `talkToAdjacentNpc` and `finishTalk`.

## screen

### Adapters

#### Presentation

- Modify `VirtualPadView` with a Game Boy Color layout: the arrows on the left, and the round B and A buttons on the
  right, A higher and further right, labelled, in palette colours. Add `setOnAPressed(listener)`. B has no listener yet.
- Add `NpcDialogView`, as big as the pad: the portrait (`npc/<npc id>_portrait.png`, at 3x), the name, and the line,
  wrapping. Tapping anywhere on it calls its listener.
- Modify `BattlefieldView` with `displayNpc(row, column, npcId)` (`npc/<npc id>.png`), on its own layer, kept when its
  tile is off the screen, as a battle unit.
- Modify `BattlefieldPresenter`:
  - On `NpcPlaced`, it displays the NPC on its tile.
  - Tapping a tile does nothing while talking (`explorationApi.searchExploration().talkingNpcId`).
- Modify `ExplorationPresenter`:
  - Pressing A talks to the adjacent NPC, once the timeline is idle and while the pad is displayed, as walking.
  - On `NpcTalkStarted`, the dialog of the NPC (`npcApi.searchNpcById`) takes the place of the pad.
  - Tapping the dialog finishes the talk.
  - On `NpcTalkFinished`, the pad is displayed again.
  - It keeps counting the seconds: `ApplyStandingTerrainEffects` does nothing while talking.
- Modify `BattleScene` so the dialog shares the place of the pad and the battle HUD.

## Resources

- Add `npc/the-guy.toml` (`name = "The Guy"`, `line = "G'day bloke!"`, `row = 10`, `column = 2`).
- Add `npc/the-guy.png` (the fixed sprite) and `npc/the-guy_portrait.png` (enlarged twice).

## Unchanged

- The walking, the wandering, the paths and the engagement distance already go around the occupied tiles, so the NPCs.
- The CPU targets the battle units of the enemy player, and the abilities the battle units on the tiles: an NPC is
  neither.
- `ProcessTileSelected` already treats a tile with no battle unit of the human player as an empty one.
- The pad, so A, is hidden while battling, while a battle unit of the party is selected, and once the game is over.

## Impact

- The tests building `ExplorationApi`, `ExplorationPresenter` and `BattlefieldPresenter` need the new dependencies.
- `SetupBattleTest` checks the new initialisation request.
- `VirtualPadViewTest` and its page object follow the new layout.
- A new acceptance test: the knight next to The Guy talks to it with A, can not walk while talking, no second starts,
  and walks again once the talk is finished. Another one: A does nothing with no NPC next to the leader.
- [Game data](../architecture/game-data.md) documents `npc/<npc id>.toml` and the initialisation order,
  [Assets](../architecture/assets.md) the NPC images, and [Exploration](../architecture/exploration.md) the pad buttons
  and talking.

## Milestones

Each milestone is committed and pushed on its own, with its tests and documentation, and leaves the game playable.

| # | Done | Milestone                    | Changes                                                                                                                                                                                                          | The game after it                                                                     |
|---|------|------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------|
| 1 | ⬜   | NPCs on the battlefield      | The `npc` subdomain, `SetupBattle`, the battlefield `OnNpcPlaced`, `ApplyStandingTerrainEffects`, `BattlefieldView.displayNpc`, `BattlefieldPresenter` on `NpcPlaced`. The Guy's resources. Game data and assets docs. | The Guy stands at (10,2), blocks the way, and nobody can attack it.                   |
| 2 | ⬜   | The Game Boy Color pad       | `VirtualPadView` layout, A and B buttons. Exploration docs.                                                                                                                                                        | The pad has an A and a B button, which do nothing yet.                                |
| 3 | ⬜   | Talking to an NPC            | The `TALKING` mode, `talk`, `finishTalk`, `NpcTalkStarted`, `NpcTalkFinished`, `TalkToAdjacentNpc`, `FinishTalk`, `NpcDialogView`, `ExplorationPresenter`, `BattlefieldPresenter` taps, `BattleScene`. Exploration docs. | Pressing A next to The Guy displays "G'day bloke!" until the dialog is tapped.        |
