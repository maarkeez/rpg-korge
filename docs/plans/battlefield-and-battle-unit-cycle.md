# Plan: Battlefield and battle unit dependency cycle

> **Status:** done.
>
> **Summary:** `battlefield` and `battleUnit` depended on each other. `battlefield` no longer depends on `battleUnit`: it
> declares `Request…` events (`RequestOccupyTile`, `RequestMoveOccupant`, `RequestRemoveOccupant`), which the `BattleUnit`
> aggregate records right after its own events, so the battlefield is updated as early as before. The terrain effects
> are announced by `battlefield` (`TerrainEffectTriggered`) and applied by `battleUnit`.

## Decisions

| Topic               | Decision                                                                                                                                                                                                                                       |
|---------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope               | Only `battleUnit` ⇄ `battlefield`. The other 5 two-way dependencies are left for later.                                                                                                                                                        |
| Direction           | `battlefield` is upstream: it does not know about `battleUnit`. `battleUnit` keeps its queries on `battlefield` (positions, paths, occupants).                                                                                                 |
| Occupancy           | `battlefield` declares `RequestOccupyTile(battleUnitId, row, column)` and `RequestRemoveOccupant(battleUnitId)`, listened by `OnRequestOccupyTile` and `OnRequestRemoveOccupant`. Its 5 `OnBattleUnit…` listeners are deleted.                  |
| Moving and loot     | One `RequestMoveOccupant(battleUnitId, toRow, toColumn, path)`, whose listener occupies the destination, then finds the loot on the path, as `OnBattleUnitMoved` did.                                                                          |
| Publishing          | The `BattleUnit` aggregate records each request right after its fact (deployed, moved, walked, teleported, respawned, vanished, defeated), so both are published together. Its events are `DomainEvent`s.                                       |
| Why not listeners   | The bus is FIFO: a request published by a listener on the fact lands behind the events already waiting. After a summon, a push or a defeat inside a CPU cast, the CPU would make its next decision on a stale battlefield.                   |
| Terrain effects     | `RequestEffectApplicationToOccupants` (the command) is renamed `TriggerTerrainEffects`, and publishes `TerrainEffectTriggered(application)`. `battleUnit`'s `OnTerrainEffectTriggered` calls `ApplyEffect` directly, with as many hops as before. |
| Request event name  | `BattlefieldEvent.RequestEffectApplicationToOccupants`, published by party and on `BattleRoundStarted`, keeps its name.                                                                                                                       |
| Guard               | The pair is removed from `SubdomainDependencyArchitectureTest`.                                                                                                                                                                                |
| Docs                | `package-structure.md` explains when the aggregate records the request, with the battlefield example. The dependency report is regenerated.                                                                                                    |
| Delivery            | One commit, with this plan.                                                                                                                                                                                                                    |

| What `battlefield` needed from `battleUnit`                                              | Now                                                                               |
|------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------|
| Listening to `BattleUnitDeployed` and `BattleUnitRespawned`                              | Listening to its own `RequestOccupyTile`.                                         |
| Listening to `BattleUnitMoved`                                                           | Listening to its own `RequestMoveOccupant`.                                       |
| Listening to `BattleUnitDefeated` and `BattleUnitVanished`                               | Listening to its own `RequestRemoveOccupant`.                                     |
| Publishing `BattleUnitEvent.RequestApplyEffect`, in `RequestEffectApplicationToOccupants` | Publishing its own `TerrainEffectTriggered`, in `TriggerTerrainEffects`.          |

## battlefield

- Add `BattlefieldEvent.RequestOccupyTile`, `RequestMoveOccupant`, `RequestRemoveOccupant` and `TerrainEffectTriggered`.
- Add `OnRequestOccupyTile`, `OnRequestMoveOccupant` and `OnRequestRemoveOccupant`. Delete `OnBattleUnitDeployed`,
  `OnBattleUnitMoved`, `OnBattleUnitRespawned`, `OnBattleUnitDefeated` and `OnBattleUnitVanished`.
- Rename the command `RequestEffectApplicationToOccupants` to `TriggerTerrainEffects`.

## battleUnit

- Modify `BattleUnit` to record the battlefield requests after `BattleUnitDeployed`, `BattleUnitMoved` (move, walk and
  teleport), `BattleUnitRespawned`, `BattleUnitVanished` and `BattleUnitDefeated`.
- Add `OnTerrainEffectTriggered`, calling `ApplyEffect`.

## Tests

- `OnRequestOccupyTileTest`, `OnRequestMoveOccupantTest` and `OnRequestRemoveOccupantTest` replace the battlefield
  `OnBattleUnit…Test`s. `TriggerTerrainEffectsTest` and `OnRequestEffectApplicationToOccupantsTest` expect
  `TerrainEffectTriggered`.
- `BattleUnitTest`, `DeployBattleUnitTest`, `InitialiseBattleUnitsTest`, `MoveBattleUnitTest`, `WalkBattleUnitTest`,
  `RespawnBattleUnitTest`, `VanishSummonedBattleUnitsTest` and `FinishSummonedBattleUnitsTurnTest` expect the requests
  after the facts.
