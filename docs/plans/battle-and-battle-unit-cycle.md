# Plan: Battle and battle unit dependency cycle

> **Status:** done.
>
> **Summary:** `battle` and `battleUnit` depended on each other. `battle` no longer depends on `battleUnit`: it
> declares `Request…` events (`RequestJoinBattle`, `RequestLeaveBattle`, `RequestDefeatPlayer`), which `battleUnit`
> publishes, and it records the player of each battle unit in the battle itself. Two architecture tests keep two-way
> dependencies between subdomains, and listeners with business logic, from coming back.

## Decisions

| Topic                  | Decision                                                                                                                                                                                                                         |
|------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope                  | Only `battle` ⇄ `battleUnit`. The 9 subdomains cycle and the other 6 two-way dependencies are left for later.                                                                                                                     |
| Direction              | `battle` is upstream: it does not know about `battleUnit`. `battleUnit` may depend on `battle`, so its listeners on `PlayerTurnStarted`, `PlayerTurnFinished` and `BattleFinished` stay.                                          |
| Defeat                 | `battle` exposes `RequestDefeatPlayer(playerId)`. `DefeatPlayer` no longer checks the battle units: it defeats the player, in a battle or while exploring.                                                                         |
| Leave and join         | `battle` exposes `RequestLeaveBattle(battleUnitId)` and `RequestJoinBattle(battleUnitId, playerId)`. Its 3 listeners on `BattleUnitEvent` are deleted.                                                                            |
| Listeners              | A listener holds no business logic, and never publishes events: it only calls command use cases.                                                                                                                                  |
| Publishing             | `battleUnit` listens to its own `BattleUnitDefeated` and `BattleUnitVanished`, and calls `RequestBattleLeave`: it requests the leave, then the defeat once all the player's battle units are defeated. On `BattleUnitDeployed`, it calls `RequestBattleJoin`. |
| Players in the battle  | `Battle` records the player of each battle unit in it (a battle unit id → player id map), so it decides on its own when it is disengaged (fewer than 2 players left with battle units in it).                                     |
| Battle start           | cpuBattleBrain's `CombatEngaged` carries the engaged player's battle units (`engagedBattleUnitIds`, every living one), so `StartBattle` no longer searches them.                                                                   |
| Carrying the player    | `BattleUnitDeployed` carries `playerId`, as `BattleUnitDefeated` and `BattleUnitVanished`. `EngagementUpdated` carries the CPU `playerId`, the player of the joining battle units (null when there is no CPU player).             |
| Guard                  | `SubdomainDependencyArchitectureTest` fails on any new two-way dependency, and on a known one that no longer exists, so its list only shrinks. `EventListenerArchitectureTest` enforces the listeners rule.                       |
| Docs                   | `package-structure.md` has a "Dependencies between subdomains" section. The dependency report is regenerated.                                                                                                                    |
| Delivery               | One commit, with this plan.                                                                                                                                                                                                       |

| What `battle` needed from `battleUnit`                                 | Now                                                                                                    |
|------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------|
| `HasAllBattleUnitsDefeated`, in `DefeatPlayer`                         | `battleUnit` checks it, in `RequestBattleLeave`, and publishes `RequestDefeatPlayer`.                  |
| `SearchBattleUnitById`, in `StartNextRound`, for the player of each battle unit in the battle | `Battle` records it, from `StartBattle`, `RequestJoinBattle` and `EngagementUpdated`. |
| `SearchBattleUnitsByPlayerId`, in `StartBattle`, for the engaged battle units | `CombatEngaged.engagedBattleUnitIds`.                                                           |
| Listening to `BattleUnitDeployed`, `BattleUnitDefeated`, `BattleUnitVanished` | Listening to its own `RequestJoinBattle`, `RequestLeaveBattle`, `RequestDefeatPlayer`.          |

## battle

### Domain

- Add `BattleEvent.RequestJoinBattle(battleUnitId, playerId)`, `RequestLeaveBattle(battleUnitId)` and
  `RequestDefeatPlayer(playerId)`.
- Modify `Battle.BattleUnits` to hold a battle unit id → player id map. `startFirstRound` takes the map, `join` takes the
  player, and `isDisengaged()` reads the players from it. `Battle.Dto.battleUnitIds` and the events keep the ids only.

### Use cases

- Modify `DefeatPlayer` to drop `HasAllBattleUnitsDefeated`.
- Modify `JoinBattle` to take the player.
- Modify `StartBattle` to take the engaged battle units, and drop `SearchBattleUnitsByPlayerId`.
- Modify `StartNextRound` to take the joining player, and drop `SearchBattleUnitById`.

### Adapters

- Add `OnRequestJoinBattle`, `OnRequestLeaveBattle` and `OnRequestDefeatPlayer`.
- Delete `OnBattleUnitDeployed`, `OnBattleUnitDefeated` and `OnBattleUnitVanished`.
- Modify `OnCombatEngaged` and `OnEngagementUpdated` to pass the engaged battle units and the joining player.
- Modify `BattleApi` to drop `BattleUnitApi`.

## battleUnit

- Modify `BattleUnitEvent.BattleUnitDeployed` with `playerId`.
- Add the `RequestBattleJoin` and `RequestBattleLeave` commands.
- Add the `OnBattleUnitDeployed` and `OnBattleUnitVanished` listeners, and modify `OnBattleUnitDefeated` to also call
  `RequestBattleLeave`.

## cpuBattleBrain

- Modify `CombatEngaged` with `engagedBattleUnitIds`, filled by `EngageEnemies`.
- Modify `EngagementUpdated` with the CPU `playerId`, filled by `UpdateEngagement`.

## Architecture

- Add `SubdomainDependencyArchitectureTest`, with the 6 two-way dependencies left: `battle` ⇄ `cpuBattleBrain`,
  `battleUnit` ⇄ `battlefield`, `battlefield` ⇄ `inventory`, `battlefield` ⇄ `npc`, `cpuBattleBrain` ⇄
  `cpuWanderBrain` and `npc` ⇄ `party`.
- Add `EventListenerArchitectureTest`.
- Add the "Dependencies between subdomains" section to `package-structure.md`, and update the event listener test
  example of `how-to-add-a-new-test.md`.

## Tests

- `BattleTest`, `BattleMother`, `DefeatPlayerTest`, `JoinBattleTest`, `StartBattleTest`, `StartNextRoundTest`,
  `OnCombatEngagedTest`, `OnEngagementUpdatedTest`, and the new `OnRequestJoinBattleTest`, `OnRequestLeaveBattleTest`,
  `OnRequestDefeatPlayerTest`. `OnBattleUnitDeployedTest` and `OnBattleUnitDefeatedTest` of `battle` are deleted.
- `RequestBattleJoinTest`, `RequestBattleLeaveTest`, and `battleUnit`'s `OnBattleUnitDeployedTest` and
  `OnBattleUnitVanishedTest`.
- `EngageEnemiesTest` (every living battle unit of the exploring player is engaged), `UpdateEngagementTest`, and the
  tests building `BattleUnitDeployed`, `CombatEngaged` and `EngagementUpdated`.
