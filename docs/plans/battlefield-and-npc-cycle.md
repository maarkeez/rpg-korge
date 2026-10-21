# Plan: Battlefield and npc dependency cycle

> **Status:** done.
>
> **Summary:** `battlefield` and `npc` depended on each other. `battlefield` no longer depends on `npc`: the `Npc`
> aggregate records the existing `RequestOccupyTile` and `RequestRemoveOccupant` right after `NpcPlaced` and
> `NpcRecruited`, so the tile of a recruited npc is still vacated before its battle unit is deployed on it. As an npc
> occupies a tile too, `battlefield` no longer says battle unit: it names whoever stands on a tile its occupant
> (`occupantId`).

## Decisions

| Topic         | Decision                                                                                                                                                                                                 |
|---------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope         | Only `battlefield` ⇄ `npc`. `battlefield` ⇄ `inventory`, `npc` ⇄ `party`, `battle` ⇄ `cpuBattleBrain` and `cpuBattleBrain` ⇄ `cpuWanderBrain` are left for later.                                         |
| Direction     | `battlefield` is upstream: it does not know about `npc`. `npc` keeps its `CanBattlefieldTileBeOccupied` query, in `InitialiseNpcs`.                                                                       |
| Requests      | `npc` reuses `RequestOccupyTile` and `RequestRemoveOccupant`, already listened by `OnRequestOccupyTile` and `OnRequestRemoveOccupant`. `OnNpcPlaced` and `OnNpcRecruited` are deleted.                    |
| Publishing    | The `Npc` aggregate records each request right after its fact: `RequestOccupyTile` after `NpcPlaced`, `RequestRemoveOccupant` after `NpcRecruited`. Its events are `DomainEvent`s.                         |
| Event order   | `RecruitNpc` publishes the npc events before `RequestDeployBattleUnit`, which checks that the tile can be occupied, so the tile is vacated first, as before. Placing npcs keeps its order too.             |
| Occupant      | `battlefield` never says battle unit: its requests, events, aggregate, commands and queries use `occupantId` (`occupantIds`), e.g.: `BattlefieldTileOccupied.occupantId`, `LootFound.occupantId`, `TileDto.occupantId`. Its listeners map it to their battle unit. |
| Guard         | The pair is removed from `SubdomainDependencyArchitectureTest`.                                                                                                                                           |
| Docs          | `package-structure.md` has the npc example, next to the battle unit one. The dependency report is regenerated.                                                                                            |
| Delivery      | Two commits: the `occupantId` rename first, then the cycle break with this plan.                                                                                                                          |

| What `battlefield` needed from `npc` | Now                                                                           |
|--------------------------------------|-------------------------------------------------------------------------------|
| Listening to `NpcPlaced`             | Listening to its own `RequestOccupyTile`, recorded by `Npc` after `NpcPlaced`. |
| Listening to `NpcRecruited`          | Listening to its own `RequestRemoveOccupant`, recorded by `Npc` after `NpcRecruited`. |

## Rename (first commit)

- `battlefield`: `battleUnitId`, `battlefieldUnitId` and `occupantBattleUnitId` become `occupantId`, and `battleUnitIds`
  becomes `occupantIds`, in `BattlefieldEvent`, `Battlefield` (its `OccupyingBattleUnitId` becomes `OccupantId`),
  `UpdateBattlefieldOccupancy`, `RemoveOccupant`, `FindLoot`, `TriggerTerrainEffects`, `SearchPosition` and
  `SearchTilesThatCanBeOccupied`.
- Downstream: `battleUnit`, `cpuBattleBrain`, `cpuWanderBrain`, `party`, `inventory` and `screen` use the new names.

## npc

- Modify `Npc` to record `RequestOccupyTile` after `NpcPlaced`, and `RequestRemoveOccupant` after `NpcRecruited`.
- Modify the KDocs of `InitialiseNpcs` and `RecruitNpc`.

## battlefield

- Delete `OnNpcPlaced` and `OnNpcRecruited`, and their wiring in `BattlefieldApi`.

## Tests

- `OnNpcPlacedTest` and `OnNpcRecruitedTest` are deleted.
- `NpcTest`, `InitialiseNpcsTest` and `RecruitNpcTest` expect the requests after the facts.
