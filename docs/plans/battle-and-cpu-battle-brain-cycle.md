# Plan: Battle and cpu battle brain dependency cycle

> **Status:** done.
>
> **Summary:** `battle` and `cpuBattleBrain` depended on each other. `battle` no longer depends on `cpuBattleBrain`: it
> declares `RequestStartBattle` and `RequestStartNextRound`, which `EngageEnemies` and `UpdateEngagement` publish together
> with `CombatEngaged` and `EngagementUpdated`, and listens to them instead of the CPU battle brain events.
> `cpuBattleBrain` keeps listening to `PlayerTurnStarted` and publishing `RequestFinishPlayerTurn`. This was the last
> two-way dependency between subdomains.

## Decisions

| Topic      | Decision                                                                                                                                                                       |
|------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope      | Only `battle` ⇄ `cpuBattleBrain`. The indirect cycle `battleUnit` → `battlefield` → `lootDrop` → `battleUnit` (`battlefield`'s `OnLootDropped`) is left for later.            |
| Direction  | `battle` is upstream: it does not know about `cpuBattleBrain`, nor that a CPU player exists. `PlayerTurnStarted` and `RequestFinishPlayerTurn` stay as they are.                |
| Requests   | `RequestStartBattle(engagingPlayerId, engagedPlayerId, engagingBattleUnitIds, engagedBattleUnitIds)` and `RequestStartNextRound(joiningPlayerId?, joiningBattleUnitIds, leavingBattleUnitIds)`, the parameters `StartBattle` and `StartNextRound` take. |
| Listeners  | `OnRequestStartBattle` and `OnRequestStartNextRound` replace `OnCombatEngaged` and `OnEngagementUpdated`.                                                                      |
| Facts      | `CombatEngaged` and `EngagementUpdated` stay, each followed by its request.                                                                                                    |
| Publisher  | `EngageEnemies` and `UpdateEngagement` have no aggregate, so they publish the fact and its request together, and the battle starts at the same point as before.               |
| Guard test | The known two-way dependencies, and the test that they still exist, are removed from `SubdomainDependencyArchitectureTest`: no two subdomains depend on each other.           |
| Docs       | `package-structure.md` has the `cpuBattleBrain` example, and says a command without an aggregate publishes the request with its fact. `cpu-battle-brain.md` and `exploration.md` mention the requests. The dependency report is regenerated. |
| Delivery   | One commit, with this plan, pushed to `main`.                                                                                                                                  |

| What `battle` needed from `cpuBattleBrain` | Now                                                                       |
|--------------------------------------------|---------------------------------------------------------------------------|
| `CombatEngaged`, to start the battle       | Listening to its own `RequestStartBattle`, published by `EngageEnemies`.   |
| `EngagementUpdated`, to start next round   | Listening to its own `RequestStartNextRound`, published by `UpdateEngagement`. |

## battle

- Add `BattleEvent.RequestStartBattle` and `RequestStartNextRound`.
- Rename `OnCombatEngaged` to `OnRequestStartBattle`, and `OnEngagementUpdated` to `OnRequestStartNextRound`, listening to the requests.

## cpuBattleBrain

- `EngageEnemies` publishes `CombatEngaged` and `RequestStartBattle` together.
- `UpdateEngagement` publishes `EngagementUpdated` and `RequestStartNextRound` together.

## Tests

- The two listener tests are renamed after their listeners and publish the requests.
- `EngageEnemiesTest` and `UpdateEngagementTest` expect the request after the fact.
- `SubdomainDependencyArchitectureTest` keeps only `should not depend on each other when they are two subdomains`.
