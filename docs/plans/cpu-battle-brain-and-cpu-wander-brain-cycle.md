# Plan: Cpu battle brain and cpu wander brain dependency cycle

> **Status:** done.
>
> **Summary:** `cpuBattleBrain` and `cpuWanderBrain` depended on each other. `cpuBattleBrain` no longer depends on
> `cpuWanderBrain`: it declares `RequestEngageEnemies` and `RequestUpdateEngagement`, which the `CpuWanderBrain`
> aggregate records right after `BattleUnitsWanderedWhileExploring` and `BattleUnitsWanderedAfterRound`, and listens to
> them instead of the wander events. `cpuWanderBrain` keeps asking `WhereShouldWander` and `IsOutsideTerritory`.

## Decisions

| Topic      | Decision                                                                                                                                                                       |
|------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope      | Only `cpuBattleBrain` ⇄ `cpuWanderBrain`. `battle` ⇄ `cpuBattleBrain` is left for later.                                                                                       |
| Direction  | `cpuBattleBrain` is upstream: it does not know about `cpuWanderBrain`. The wandering queries, and the healing and terrain helpers they share with moving, stay where they are. |
| Publisher  | The `CpuWanderBrain` aggregate records the requests in `finishWhenDone`, as `BattleUnit` records `RequestOccupyTile`, so they are published at the same point as before.       |
| Facts      | `BattleUnitsWanderedWhileExploring` and `BattleUnitsWanderedAfterRound` stay, each followed by its request. The aggregate events widen to `Set<DomainEvent>`.                  |
| Requests   | `RequestEngageEnemies(exploringPlayerId)` and `RequestUpdateEngagement(battleUnitIds)`, the fields `EngageEnemies` and `UpdateEngagement` take.                                |
| Listeners  | `OnRequestEngageEnemies` and `OnRequestUpdateEngagement` replace `OnBattleUnitsWanderedWhileExploring` and `OnBattleUnitsWanderedAfterRound`.                                  |
| Wiring     | Unchanged: `CpuWanderBrainApi` keeps taking `CpuBattleBrainApi`.                                                                                                               |
| Guard test | The pair is removed from `SubdomainDependencyArchitectureTest`.                                                                                                                |
| Docs       | `package-structure.md` has the `CpuWanderBrain` example. `cpu-battle-brain.md` says the wander brain requests the engagement. The dependency report is regenerated.            |
| Delivery   | One commit, with this plan, pushed to `main`.                                                                                                                                  |

| What `cpuBattleBrain` needed from `cpuWanderBrain` | Now                                                                           |
|----------------------------------------------------|-------------------------------------------------------------------------------|
| `BattleUnitsWanderedWhileExploring`, to engage     | Listening to its own `RequestEngageEnemies`, recorded by `CpuWanderBrain`.    |
| `BattleUnitsWanderedAfterRound`, to update         | Listening to its own `RequestUpdateEngagement`, recorded by `CpuWanderBrain`. |

## cpuBattleBrain

- Add `CpuBattleBrainEvent.RequestEngageEnemies` and `RequestUpdateEngagement`.
- Rename `OnBattleUnitsWanderedWhileExploring` to `OnRequestEngageEnemies`, and `OnBattleUnitsWanderedAfterRound` to `OnRequestUpdateEngagement`, listening to the requests.

## cpuWanderBrain

- `CpuWanderBrain.finishWhenDone` records the request right after the wandered event.

## Tests

- The two listener tests are renamed after their listeners and publish the requests.
- `CpuWanderBrainTest`, `WanderWhileExploringTest`, `WanderAfterRoundTest` and `ResolveWanderingStepTest` expect the request after the wandered event.
