# Plan: Recruiting the viking

> **Status:** done, the 2 milestones are done.
>
> **Summary:** A viking stands as an NPC at row 12, column 2, two tiles south of The Guy. Talking to it with A displays
> "Viking: For Valhalla!", and once its dialog is closed, it joins the party: it becomes a battle unit of the human
> player on its tile, flashes once, and follows the party last. Any NPC with a unit in its game data is recruitable.

## Decisions

| Topic               | Decision                                                                                                                                                                                                         |
|---------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Before recruiting   | An NPC, as The Guy: it blocks its tile for everyone, nobody can attack it, and it does not count for the victory.                                                                                               |
| Game data           | `npc/viking.toml`: "Viking", "For Valhalla!", row 12, column 2 (counting from 0, on sand), and an optional `unitId = "viking"`. Any NPC with a unit is recruitable. The Guy has none, so he stays an NPC. The game fails to start when the unit does not exist. |
| The unit            | `unit/viking.toml`: "Viking", 150 health, 30 mana, a movement range of 3, and only the `sword` ability.                                                                                                         |
| Recruiting          | Talking to it is as talking to The Guy. Once its dialog is closed, it becomes a battle unit of the human player on its tile, with full health and mana (150/30), and joins the party last.                      |
| Joining             | It follows the battle unit ahead of it, walking the shortest path to catch it up, as any follower not next to it.                                                                                               |
| On the screen       | The NPC becomes the battle unit on its tile, which flashes once. No text.                                                                                                                                        |
| Images              | A recruitable NPC uses the images of its unit, on the battlefield and in its dialog: `unit/viking.png` (16x16) and `unit/viking_portrait.png` (32x32). No copy under `npc/`.                                   |
| Once recruited      | A party member as the knights: it has a bag, with an empty inventory. Once defeated, it leaves the party, and never comes back.                                                                                  |

For example, the knight leads the party next to the viking:

| The player                                  | The game                                                                                     |
|---------------------------------------------|----------------------------------------------------------------------------------------------|
| Walks the knight to (12,3), east of it      | The viking blocks (12,2), as The Guy blocks (10,2).                                          |
| Presses A                                   | The dialog takes the place of the pad: the viking portrait, "Viking" and "For Valhalla!".    |
| Taps the dialog                             | The pad is displayed again. The viking flashes on (12,2): it is a battle unit of the party.  |
| Walks the knight north                      | The other knight follows the leader, and the viking follows the other knight.                |
| Taps the viking                             | Its info and its bag are displayed, as a knight's.                                           |
| Walks back next to (12,2) and presses A     | Nothing happens: the viking is no longer an NPC.                                             |

## The viking's sprite

The sprite uses the same colours as The Guy's, off the FamiCube palette, and two more. Each one is replaced with its
closest palette colour, except the outline, which takes the near black of the other units' outlines, and the sides of
the helmet, whose closest colour is the one of the face:

| Off-palette colour | Palette colour    | Where                                             |
|--------------------|-------------------|---------------------------------------------------|
| `(63, 38, 49)`     | `(21, 21, 21)`    | The outline                                       |
| `(192, 203, 220)`  | `(215, 215, 215)` |                                                   |
| `(189, 108, 74)`   | `(174, 108, 55)`  |                                                   |
| `(139, 155, 180)`  | `(168, 168, 168)` |                                                   |
| `(234, 165, 108)`  | `(246, 143, 55)`  | The sides of the helmet, apart from the face      |
| `(38, 43, 68)`     | `(33, 22, 64)`    |                                                   |
| `(82, 96, 124)`    | `(65, 93, 102)`   |                                                   |
| `(247, 194, 130)`  | `(245, 183, 132)` |                                                   |
| `(225, 154, 101)`  | `(197, 151, 130)` |                                                   |

The portrait is the fixed sprite enlarged twice with no smoothing (each pixel becomes 2x2), so it is 32x32 like the other
unit portraits.

## How an NPC is recruited

The NPC and the battle unit share its tile, one after the other: the NPC leaves it before the battle unit is deployed on
it. The event bus dispatches the events in the order they are published, each one to all its subscribers before the
next one. So `RecruitNpc` publishes, in order:

1. `NpcRecruited(npcId, row, column)`: the battlefield vacates the tile of the NPC (`RemoveOccupant`), and the screen
   removes the NPC.
2. `RequestDeployBattleUnit(battleUnitId, unitId, playerId, row, column)`: the battle unit is deployed on the vacated tile
   (`DeployBattleUnit`), and occupies it once `BattleUnitDeployed` is handled, as any battle unit deployed.
3. `RequestJoinParty(battleUnitId)`: the battle unit joins the party last (`PartyJoined`), and the screen flashes it.

The battle unit takes the id of the NPC (e.g.: `viking`), so an NPC can only be recruited once. The NPC is deleted, so
pressing A next to its tile no longer talks to it.

## npc

### Domain

- Modify `Npc` with an optional `unitId`, in its `Dto` too, and `NpcPlaced` with it, so the screen displays the image of
  its unit.
- Add `Npc.isRecruitable()`, and `Npc.recruit()`, which refuses an NPC with no unit (`NpcNotRecruitable`), and adds
  `NpcRecruited(npcId, row, column)`.
- Add `NpcError.NpcUnitNotFound` and `NpcError.NpcNotRecruitable`.
- Modify `NpcRepository` with `delete(id)`.

### Use cases

#### Commands

- Modify `InitialiseNpcs` so it refuses an NPC whose unit does not exist (`SearchUnitById`, `NpcUnitNotFound`): the game
  fails to start.
- Add `RecruitNpc(npcId, playerId)`: nothing happens when the NPC does not exist or has no unit. Otherwise, it deletes the
  NPC, and publishes `NpcRecruited`, `RequestDeployBattleUnit` and `RequestJoinParty`, in that order.

### Adapters

- `ResourcesNpcLoader` reads the optional `unitId`.
- Add `OnNpcTalkFinished` (events), which recruits the NPC talked to.
- Modify `NpcApi` with `unitApi` and `recruitNpc`.

## battleUnit

### Domain

- Add `BattleUnitEvent.RequestDeployBattleUnit(battleUnitId, unitId, playerId, row, column)`.

### Adapters

- Add `OnRequestDeployBattleUnit` (events), which deploys the battle unit (`DeployBattleUnit`), with no remaining turns,
  as it never vanishes.

## battlefield

### Adapters

- Add `OnNpcRecruited` (events): the tile of the NPC is vacated (`RemoveOccupant`). The `OccupantRemoved` it publishes is
  handled after the battle unit is deployed, as it is published later.

## exploration

### Domain

- Modify `NpcTalkFinished` with the `npcId` talked to, so the NPC is recruited once its dialog is closed.
- Add `Exploration.join(battleUnitId)`: the battle unit joins the party last, and adds `PartyJoined(playerId,
  battleUnitId)`. Nothing changes when it is already in the party.
- Add `ExplorationEvent.RequestJoinParty(battleUnitId)` and `ExplorationEvent.PartyJoined(playerId, battleUnitId)`.

### Use cases

#### Commands

- Add `JoinParty(battleUnitId)`.

### Adapters

- Add `OnRequestJoinParty` (events).

## screen

### Adapters

#### Presentation

- Modify `BattlefieldView`:
  - `displayNpc(row, column, npcId, unitId)` displays `unit/<unit id>.png` for a recruitable NPC, and
    `npc/<npc id>.png` otherwise.
  - Add `removeNpc(row, column)`.
- Modify `NpcDialogView` so a recruitable NPC displays the portrait of its unit, `unit/<unit id>_portrait.png`.
- Modify `BattlefieldPresenter`:
  - On `NpcRecruited`, it removes the NPC from its tile.
  - On `PartyJoined`, it flashes the battle unit that joined.
- Modify `BattleScene` so `NpcApi` gets `unitApi`.

## Resources

- Add `unit/viking.toml` (`name = "Viking"`, `healthPoints = 150`, `manaPoints = 30`, `movementRange = 3`,
  `abilities = ["sword"]`).
- Add `unit/viking.png` (the fixed sprite) and `unit/viking_portrait.png` (enlarged twice).
- Add `npc/viking.toml` (`name = "Viking"`, `line = "For Valhalla!"`, `row = 12`, `column = 2`, `unitId = "viking"`).

## Unchanged

- Talking to the viking: `TalkToAdjacentNpc`, the dialog, and time stopping while talking.
- The battle units deployed while exploring: the battlefield occupancy, the screen displaying them, and the wandering,
  which only claims a home for the CPU battle units.
- The party: the followers walk the shortest path to the battle unit ahead when they are not next to it, the battles
  start with the whole party, and a defeated battle unit leaves it.
- The inventory: the battle units of the human player have a bag, and an inventory with nothing stored yet is empty.

## Impact

- The tests building `Npc.Dto`, `NpcPlaced`, `NpcTalkFinished`, `NpcApi` and `BattlefieldView.displayNpc` need the new
  fields and dependencies.
- A new acceptance test: the knight next to the viking talks to it, and once the talk is finished, the viking is a battle
  unit of the human player on (12,2), last in the party, and follows it when it walks. Pressing A next to it again does
  nothing. Another one: finishing a talk with The Guy recruits no one.
- [Game data](../architecture/game-data.md) documents the optional `unitId` and the viking unit,
  [Assets](../architecture/assets.md) the images of a recruitable NPC, and [Exploration](../architecture/exploration.md)
  recruiting.

## Milestones

Each milestone is committed and pushed on its own, with its tests and documentation, and leaves the game playable.

| # | Done | Milestone             | Changes                                                                                                                                                                                                                                                                                 | The game after it                                                                    |
|---|------|-----------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------|
| 1 | ✅   | The viking as an NPC  | The viking unit, sprite and portrait. `npc/viking.toml`. `Npc.unitId`, `NpcPlaced.unitId`, `InitialiseNpcs` checking the unit, `ResourcesNpcLoader`, `NpcApi` with `unitApi`. `BattlefieldView.displayNpc` and `NpcDialogView` with the unit images. Game data and assets docs.          | The viking stands at (12,2), and pressing A next to it displays "For Valhalla!".     |
| 2 | ✅   | Recruiting the viking | `NpcTalkFinished.npcId`, `Npc.recruit`, `NpcRecruited`, `NpcRepository.delete`, `RecruitNpc`, `OnNpcTalkFinished`, `RequestDeployBattleUnit`, `OnRequestDeployBattleUnit`, the battlefield `OnNpcRecruited`, `Exploration.join`, `JoinParty`, `PartyJoined`, `BattlefieldView.removeNpc`, `BattlefieldPresenter`. Exploration docs. | Closing the viking's dialog recruits it: it flashes, and follows the party last.      |
