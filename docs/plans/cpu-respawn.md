# Plan: defeated CPU battle units respawn

> **Status:** done, the 2 milestones are done.
>
> **Summary:** A defeated CPU battle unit respawns on its home once the party has walked 50 steps since its defeat. It
> is the same battle unit, back as it was deployed: full health and mana, no cooldowns, no ongoing effects. It waits
> while its home is occupied, and it drops its loot again when it is defeated again. Defeating every CPU battle unit at
> the same time still wins the game.

## Decisions

| Topic         | Decision                                                                                                                                                                                                   |
|---------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Who           | Every CPU battle unit with a home: the ones deployed with the map. The summons have none, so they never respawn.                                                                                         |
| A step        | A party walk (`PartyWalked`): the leader walks one tile. A walk that does not happen (e.g.: the leader is blocked) is no step, and there are no steps while battling.                                   |
| When          | Once the party has walked `RESPAWN_STEPS` (50) steps since its defeat, a constant for every unit. Each battle unit counts its own steps, from its own defeat, whether in a battle or by the terrain.    |
| Where         | On its home. While its home is occupied (e.g.: by a battle unit of the party, or a CPU battle unit wandering by), it waits, and respawns on the first step its home is free.                             |
| Party nearby  | It respawns anyway. A battle unit of the party within its reach is engaged on the next check: when the CPU battle units wander (every second), or when the party walks again.                           |
| State         | The same battle unit (same id, same home), back as it was deployed: full health and mana, no cooldowns, no ongoing effects. It wanders from the next second, around its home.                         |
| Loot          | It drops loot again when it is defeated again. An item lying on its home stays there: it lies under it, as a CPU battle unit loots nothing.                                                             |
| Victory       | Unchanged: once every CPU battle unit is defeated at the same time, the human player wins, and the game is over before any step is walked, so none respawns.                                            |
| Display       | It is displayed on its home as when the game starts. No countdown.                                                                                                                                        |

For example, the knight defeats a rat in a battle, and the party explores again:

| Party steps since the rat was defeated | The rat                                                                       |
|----------------------------------------|-------------------------------------------------------------------------------|
| 0 to 49                                | Defeated: not on the battlefield.                                             |
| 50, the knight stands on its home      | Waits.                                                                        |
| 51, its home is free                   | Respawns on its home, with full health and mana, and wanders from then on.   |

## Why a respawn event, not `BattleUnitDeployed` again

In the interview, the respawn was to publish `BattleUnitDeployed` again, so the battlefield and the screen would treat
it as a deploy. Two subscribers of `BattleUnitDeployed` make it wrong:

- `JoinBattle` (battle) adds the deployed battle unit to the battle, and `JoinCpuTurn` (cpuBrain) to the CPU turn
  playing, because a summon joins the battle it is summoned in.
- The respawn is requested on `PartyWalked`, as `EngageEnemies` engages the party. When the same step engages a battle,
  the respawn is handled once it has started, with the CPU playing first: the respawned battle unit would join a battle
  it did not engage, and play in it.

So the respawn publishes its own `BattleUnitRespawned`: the battlefield occupies the tile and the screen displays the
battle unit, as for a deploy, and the battle and the CPU brain ignore it. A respawned battle unit is out of a battle
started on that step: it wanders after the rounds, and joins it if the engagement is updated with the party within
its reach, as any other CPU battle unit out of the battle.

## battleUnit

### Domain

- Add `BattleUnit.respawn(unit, row, column)` because respawning brings the battle unit back as it was deployed: full
  health and mana of its unit, no equipped effects, its movement range, no cooldowns, no ongoing effects. It keeps its
  id and its player. Nothing changes when it is not defeated.
- Add `BattleUnitEvent.BattleUnitRespawned(playerId, battleUnitId, row, column)`.
- Add `BattleUnitEvent.RequestRespawnBattleUnit(battleUnitId, row, column)` because the wandering requests it, as it
  requests `RequestWalkBattleUnit` and `RequestRecoverBattleUnits`.

### Use cases

#### Commands

- Add `RespawnBattleUnit(battleUnitId, row, column)`: on `RequestRespawnBattleUnit`, the defeated battle unit respawns
  on the tile, searching its unit with `SearchUnitById`. Nothing happens when it is not defeated or the tile can not be
  occupied (`CanBattlefieldTileBeOccupied`), so a request is never harmful: the wandering requests it again on the next
  step.

### Adapters

#### Events

- Add `OnRequestRespawnBattleUnit`, wired in `BattleUnitApi`.

## wandering

### Domain

- Add `RESPAWN_STEPS` (50), in `Wandering.kt`, as the wandering decides when its battle units come back home.
- Add `Wandering.Respawns`: the party steps walked since each defeated battle unit with a home was defeated, and
  `Dto.respawns: List<RespawnDto(battleUnitId, partySteps)>`.
- Add `Wandering.defeat(battleUnitId)`: a battle unit with a home starts counting the party steps. The ones without a
  home (the battle units of the party, the summons) are ignored.
- Add `Wandering.walkPartyStep()`: every defeated battle unit counts one step more.
- Add `Wandering.dueRespawns(): List<HomeDto>`: the defeated battle units that have counted `RESPAWN_STEPS` steps, with
  their home, in the order they were deployed.
- Add `Wandering.respawn(battleUnitId)`: it no longer counts. It also no longer returns, as it stands at home.

### Use cases

#### Commands

- Add `AwaitRespawn(battleUnitId)`: on `BattleUnitDefeated`, the defeated battle unit starts counting.
- Add `RespawnDefeatedBattleUnits()`: on `PartyWalked`, every defeated battle unit counts the step, and those due whose
  home is free (`CanBattlefieldTileBeOccupied`) are requested to respawn there (`RequestRespawnBattleUnit`). The ones
  whose home is occupied keep waiting.
- Add `ResolveRespawn(battleUnitId)`: on `BattleUnitRespawned`, the battle unit no longer counts.

### Adapters

#### Events

- Add `OnBattleUnitDefeated`, `OnPartyWalked` and `OnBattleUnitRespawned`, wired in `WanderingApi`.

## battlefield

### Adapters

#### Events

- Add `OnBattleUnitRespawned`: its tile is occupied (`UpdateBattlefieldOccupancy`), as on `BattleUnitDeployed`.

## screen

### Adapters

#### Presentation

- Modify `BattlefieldPresenter` so, on `BattleUnitRespawned`, it displays the battle unit on its tile (`displayUnit`),
  as on `BattleUnitDeployed`.

## Unchanged

- `DropLoot` drops loot on every defeat, so a respawned battle unit drops loot again.
- `DefeatPlayer` defeats the CPU player once every one of its battle units is defeated: the game is over then, and the
  party no longer walks.
- The wandering already lets a defeated battle unit rest (it stands nowhere), and the recovery skips it.
- The engagement already finds the CPU battle units with a battle unit of the party within reach, the respawned ones
  included, when they wander and when the party walks.

## Impact

- The acceptance tests that wire the `WanderingApi` and walk the party after defeating a CPU battle unit would see it
  respawn after 50 steps. None walks that far.
- A new acceptance test, with the exploration: a rat defeated is not back after 49 steps, is back on its home with full
  health after 50, and waits while a battle unit of the party stands on its home.
- [CPU brain](../architecture/cpu-brain.md#wandering) explains the respawn, and
  [Exploration](../architecture/exploration.md) mentions it.

## Milestones

Each milestone is committed and pushed on its own, with its tests and documentation, and leaves the game playable.

| # | Done | Milestone                           | Changes                                                                                                                                                              | The game after it                                                                                       |
|---|------|-------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------|
| 1 | ✅   | Respawning a battle unit            | `BattleUnit.respawn`, `BattleUnitRespawned`, `RequestRespawnBattleUnit`, `RespawnBattleUnit`, `OnRequestRespawnBattleUnit`, the battlefield `OnBattleUnitRespawned`, `BattlefieldPresenter`. | As before: a battle unit can respawn, but nothing requests it yet.                                       |
| 2 | ✅   | Respawning after 50 party steps     | `RESPAWN_STEPS`, `Wandering.Respawns` and its methods, `AwaitRespawn`, `RespawnDefeatedBattleUnits`, `ResolveRespawn`, the wandering event listeners. CPU brain and exploration docs. | A defeated CPU battle unit respawns on its home once the party has walked 50 steps, when its home is free. |
