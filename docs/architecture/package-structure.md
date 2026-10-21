# Package structure

The production code is split into subdomains. Each subdomain lives in its own package, `com.mkz.rpg.<subdomain>`, and is organised in clean architecture layers.

This structure is enforced by [`PackageStructureArchitectureTest`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/architecture/PackageStructureArchitectureTest.kt).

The dependencies between the subdomains are shown in the [Subdomain dependencies](dependencies.md) report.

## Allowed packages

A subdomain MUST only contain the following packages:

```
<subdomain>
├── adapters
│   └── <any name>      e.g.: events, presentation, storage, resources
├── domain
└── usecases
    ├── commands
    ├── queries
    └── services
```

- Classes, interfaces, objects and top-level functions MUST be declared in one of the leaf packages above.
- Nothing MUST be declared directly in `<subdomain>`, `<subdomain>.adapters` or `<subdomain>.usecases`.
- The leaf packages MUST NOT contain further sub-packages.

For example, given a subdomain called `tomatoes`:

| Declaration                                            | Allowed |
|--------------------------------------------------------|---------|
| `tomatoes.domain.Tomatoes`                             | ✅       |
| `tomatoes.usecases.commands.PlantTomatoes`             | ✅       |
| `tomatoes.adapters.storage.InMemoryTomatoesRepository` | ✅       |
| `tomatoes.Tomatoes`                                    | ❌       |
| `tomatoes.usecases.PlantTomatoes`                      | ❌       |
| `tomatoes.domain.model.Tomatoes`                       | ❌       |

## Exceptions

- `com.mkz.rpg.Main.kt` is the application entry point and lives in the root package.
- `com.mkz.rpg.screen.adapters.presentation.battlefieldHud` is a subdomain that only exists to support the presentation layer of the `screen` subdomain. It is nested in that presentation adapter and follows the same allowed packages as any other subdomain.

## Dependencies between subdomains

- Subdomains MUST NOT depend on each other, even indirectly (e.g.: `a` → `b` → `c` → `a`). This is enforced by [`SubdomainDependencyArchitectureTest`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/architecture/SubdomainDependencyArchitectureTest.kt), which fails on the same cycles the [dependency report](dependencies.md) highlights.
- The restricted subdomains MUST only depend on the subdomains allowed for them, so their dependencies do not grow. Each one is allowed exactly the subdomains it depends on, so the list can only shrink: an allowed subdomain that is no longer depended on must be removed from it. This is enforced by [`SubdomainDependencyArchitectureTest`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/architecture/SubdomainDependencyArchitectureTest.kt). The other subdomains are only checked for cycles.

  | Restricted subdomain                  | May only depend on                                                       |
  |---------------------------------------|--------------------------------------------------------------------------|
  | `shared`                              | nothing                                                                  |
  | `effect`, `player`, `terrain`, `unit` | `shared`                                                                 |
  | `ability`, `lootableItem`             | `shared`, `effect`                                                       |
  | `battle`                              | `shared`, `player`                                                       |
  | `lootTable`                           | `shared`, `lootableItem`                                                 |
  | `battlefield`                         | `shared`, `effect`, `terrain`, `battle` (to remove next, see below)      |
  | `party`                               | `shared`, `effect`, `player`, `battle`, `battlefield`, `battleUnit`      |
  | `battleUnit`                          | `shared`, `effect`, `player`, `unit`, `ability`, `battle`, `battlefield` |

  `battlefield` still depends on `battle`: only `OnBattleRoundStarted` listens to `BattleEvent`. It is the next dependency to remove, by a `Request…` event of `battlefield` that `battle` publishes.
- To have the upstream subdomain react to the downstream one without depending on it, the upstream subdomain declares a `Request…` event in its own events (e.g.: `BattleEvent.RequestDefeatPlayer`), and a downstream command use case publishes it (e.g.: `battleUnit`'s `RequestBattleLeave`). The upstream subdomain listens to its own request, and the data it needs travels in the event (e.g.: the player of the battle unit joining the battle), instead of being queried from the downstream subdomain.
- The bus dispatches the events in the order they are published (FIFO), so a request published by a listener on a fact is handled after every event already waiting. When the upstream subdomain must be up to date before those (e.g.: the battlefield occupancy before the next CPU decision), the downstream aggregate records the request right after the fact instead, so both are published together (e.g.: `BattleUnit` records `BattlefieldEvent.RequestOccupyTile` after `BattleUnitDeployed`). A downstream command use case without an aggregate publishes the fact and the request together instead (e.g.: `EngageEnemies` publishes `CombatEngaged` and `BattleEvent.RequestStartBattle`).
- An event listener (`adapters/events/On…`) holds no business logic: it only takes command use cases and the `EventBus`, and never publishes an event itself. A command use case decides and publishes. This is enforced by [`EventListenerArchitectureTest`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/architecture/EventListenerArchitectureTest.kt).

For example, `battle` does not depend on `battleUnit`, while `battleUnit` depends on `battle` and publishes its requests:

| When                                       | `battleUnit` publishes                      | `battle`                                            |
|--------------------------------------------|---------------------------------------------|-----------------------------------------------------|
| A battle unit is deployed                  | `RequestJoinBattle(battleUnitId, playerId)` | Adds it to the battle, when there is one.           |
| A battle unit is defeated, or vanishes     | `RequestLeaveBattle(battleUnitId)`          | Removes it from the battle, when there is one.      |
| ...and all their battle units are defeated | `RequestDefeatPlayer(playerId)`             | Defeats the player, in a battle or while exploring. |

Likewise, `battlefield` does not depend on `battleUnit`. The `BattleUnit` aggregate records its requests right after its own events:

| When                                    | `BattleUnit` records                               | `battlefield`             |
|-----------------------------------------|----------------------------------------------------|---------------------------|
| A battle unit is deployed, or respawns  | `RequestOccupyTile(occupantId, row, column)`       | Occupies the tile.        |
| A battle unit moves, walks or teleports | `RequestMoveOccupant(occupantId, toRow, toColumn)` | Occupies the destination. |
| A battle unit is defeated, or vanishes  | `RequestRemoveOccupant(occupantId)`                | Vacates its tile.         |

The terrain effects go the other way: `battlefield` announces `TerrainEffectTriggered(application)` for each battle unit standing on a terrain with an effect, and `battleUnit` applies it.

`battlefield` does not depend on `npc` either. It names whoever stands on a tile its occupant (`occupantId`), a battle unit or an npc, and the `Npc` aggregate records the same requests right after its own events:

| When                     | `Npc` records                                | `battlefield`      |
|--------------------------|----------------------------------------------|--------------------|
| An npc is placed         | `RequestOccupyTile(occupantId, row, column)` | Occupies the tile. |
| An npc is recruited      | `RequestRemoveOccupant(occupantId)`          | Vacates its tile.  |

So the tile of a recruited npc is vacated before the battle unit it joins the party as is deployed on it.

`battleUnit` does not depend on `lootDrop`: `lootDrop` listens to the facts of `battleUnit`, and `battlefield` knows nothing about loot. Each `LootDrop` keeps the items its unit dropped on each tile, with its own position type, into which it maps the path walked. `LootDropped` is published while handling `BattleUnitDefeated`, so it is dispatched after `RequestRemoveOccupant`, once the tile is vacated. `LootFound` is published while handling `BattleUnitMoved`, so it is dispatched before the events of the occupancy update (e.g.: `BattlefieldTileOccupied`):

| When `battleUnit` publishes                                 | `lootDrop`                                                                            |
|-------------------------------------------------------------|---------------------------------------------------------------------------------------|
| `BattleUnitDefeated` (a battle unit is defeated)            | Drops the loot of its unit on the tile where it was defeated (`LootDropped`), if any. |
| `BattleUnitMoved` (a battle unit moves, walks or teleports) | Announces the items on each tile of its path (`LootFound`), in walking order.         |

`lootDrop` does not depend on `inventory` either. `inventory` listens to `LootFound`, and the `Inventory` aggregate records the pick-up right after its own event:

| When              | `Inventory` records                                     | `lootDrop`                 |
|-------------------|---------------------------------------------------------|----------------------------|
| An item is stored | `RequestPickUpDroppedItem(lootableItemId, row, column)` | Picks it up from its tile. |

`inventory` does not depend on `battleUnit` nor `player` either: only the party members have an inventory, so it relies on `party` for them. It creates the inventory of each party member when the exploration starts (`ExplorationStarted`) or a battle unit joins the party (`PartyJoined`), which carry the unit of each battle unit for its starting gold. It checks the turn of the party player, and asks `party` whether a member can learn the ability a consumable teaches (`CanMemberLearnAbility`). The effects of the items reach `battleUnit` through the requests of `party`, which relays them to its members:

| When                              | `inventory` publishes                                              | `party`                                                                 |
|-----------------------------------|--------------------------------------------------------------------|-------------------------------------------------------------------------|
| An item is consumed, or equipped  | `RequestApplyMemberEffect(battleUnitId, lootableItemId, effectId)` | Requests `RequestApplyEffect` from `battleUnit` for a member.           |
| An item is unequipped, or swapped | `RequestRemoveMemberEquippedEffects(battleUnitId, lootableItemId)` | Requests `RequestRemoveEquippedEffects` from `battleUnit` for a member. |

`party` does not depend on `npc` either. `npc` listens to `NpcTalkFinished` to recruit the npc, and publishes the requests of `party`:

| When                                         | `npc` publishes                                           | `party`                                                    |
|----------------------------------------------|-----------------------------------------------------------|------------------------------------------------------------|
| A is pressed, with an npc next to the leader | `RequestStartTalk(npcId)`                                 | Talks to it, only when the party can walk.                 |
| A recruitable npc is recruited               | `RequestRecruitMember(battleUnitId, unitId, row, column)` | Recruits a battle unit of the unit on the tile, see below. |

`npc` does not depend on `battleUnit` nor `unit` either. `party` deploys the recruited battle unit for its player, publishing both requests together, so it is deployed before it joins:

| When `npc` requests    | `party` publishes                                                      | Then                                 |
|------------------------|------------------------------------------------------------------------|--------------------------------------|
| `RequestRecruitMember` | `RequestDeployBattleUnit(battleUnitId, unitId, playerId, row, column)` | `battleUnit` deploys it on the tile. |
|                        | `RequestJoinParty(battleUnitId)`                                       | `party` adds it to the party, last.  |

The unit of a recruitable npc is not checked when the game starts: [`NpcUnitResourcesTest`](../../src/commonTest/kotlin/com/mkz/rpg/npc/adapters/resources/NpcUnitResourcesTest.kt) checks that every one in the resources exists.

`cpuBattleBrain` does not depend on `cpuWanderBrain` either. `cpuWanderBrain` asks it where each battle unit wanders (`WhereShouldWander`, `IsOutsideTerritory`), and the `CpuWanderBrain` aggregate records its requests right after its own events:

| When                                                                | `CpuWanderBrain` records                  | `cpuBattleBrain`                                      |
|---------------------------------------------------------------------|-------------------------------------------|-------------------------------------------------------|
| Every CPU battle unit has wandered, while exploring                 | `RequestEngageEnemies(exploringPlayerId)` | Engages the party when a battle unit is within reach. |
| Every CPU battle unit out of the battle has wandered, after a round | `RequestUpdateEngagement(battleUnitIds)`  | Updates the engagement of the battle.                 |

`battle` does not depend on `cpuBattleBrain` either. `cpuBattleBrain` listens to `PlayerTurnStarted` to play the CPU turn, and publishes `RequestFinishPlayerTurn` once it is played. Its command use cases have no aggregate, so they publish their requests together with their own events:

| When                                                                  | `cpuBattleBrain` publishes                                                                           | `battle`                                                                                     |
|-----------------------------------------------------------------------|------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------|
| The combat is engaged (`CombatEngaged`)                               | `RequestStartBattle(engagingPlayerId, engagedPlayerId, engagingBattleUnitIds, engagedBattleUnitIds)` | Starts the first round.                                                                      |
| A round finishes, and the engagement is updated (`EngagementUpdated`) | `RequestStartNextRound(joiningPlayerId, joiningBattleUnitIds, leavingBattleUnitIds)`                 | Starts the next round, with those joining and without those leaving, or finishes the battle. |
