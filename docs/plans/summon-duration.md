# Plan: summoned battle units last a number of turns

> **Status:** done, the 6 milestones are done.
>
> **Summary:** Every ability that deploys a battle unit (a summon) says how many turns the summoned battle unit lasts:
> `bat` and `bee` last 5. A summon acts from the turn it is summoned, counts its player's turns, and vanishes when its
> player finishes the last one, or when the battle ends. A vanished summon is not defeated: it drops no loot. A CPU
> summon never leaves the battle and chases the nearest enemy, however far. The battle unit info tells how many turns a
> summon has left.

## Decisions

| Topic               | Decision                                                                                                                                                                                                          |
|---------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Where               | `turnsDuration` on the ability, from 1 to 99. Mandatory on the abilities that deploy a battle unit, refused on the others, when the game data is loaded.                                                         |
| Resources           | `bat` and `bee` last 5 turns.                                                                                                                                                                                     |
| What a turn is      | A turn of the summon's player. The turn it is summoned in is its first one, and it can act in it, the CPU summons included.                                                                                      |
| When it vanishes    | When its player finishes its last turn, so it is never on the battlefield with no turns left. Also when the battle ends, whatever turns it has left: summons never explore.                                    |
| Vanishing           | It flashes, as when it receives an effect, and leaves the battlefield and the battle. It is not defeated: no loot, no effects applied on defeat. It is deleted, so nothing counts it anymore.                   |
| Its summoner        | Not linked: a summon outlasts its summoner.                                                                                                                                                                       |
| Last battle unit    | A player whose last battle unit vanishes is defeated, as if it had been defeated: the human loses the game, e.g.: when only a bee is left and the battle ends.                                                   |
| CPU summons         | They never leave the battle at the end of a round, and every enemy is within their reach, so they approach and attack the nearest one, however far.                                                             |
| Display             | Only in the battle unit info: "Vanishes in 3 turns", next to the remaining steps and casts, for the summons only. Nothing on the battlefield nor in the cast preview.                                             |
| Permanent summons   | None: every summon lasts some turns. Human summons joining the party would only make sense for permanent ones (see Follow-ups).                                                                                  |
| Unchanged           | A summon defeated in the battle is defeated as any other battle unit (e.g.: a bat still drops its loot).                                                                                                         |

For example, a goat summons a bat in its player's turn 3:

| CPU player turn | The battle unit info of the bat | What the bat does                                    |
|-----------------|---------------------------------|------------------------------------------------------|
| 3               | Vanishes in 5 turns             | Acts after the units already playing, e.g.: attacks. |
| 4               | Vanishes in 4 turns             | Acts.                                                |
| 5               | Vanishes in 3 turns             | Acts.                                                |
| 6               | Vanishes in 2 turns             | Acts.                                                |
| 7               | Vanishes in 1 turn              | Acts, then vanishes when the turn finishes.          |

## ability

### Domain

- Add `Ability.TurnsDuration` (1..99), optional, and `Dto.turnsDuration: Int? = null`, because the ability decides how
  long what it deploys lasts: the effect only says what it deploys. Same limit as the cooldown.
- Add `AbilityError.AbilityTurnsDurationBelowMinimum` and `AbilityTurnsDurationAboveLimit`, refused by `TurnsDuration`.
- Add `AbilityError.AbilityTurnsDurationMissing` and `AbilityTurnsDurationNotAllowed`, refused by `AbilityCreation`.

### Use cases

#### Services

- Modify `AbilityCreation` so an ability with an effect whose outcome is `DEPLOY_BATTLE_UNIT` must have a turns
  duration, and an ability without one must not. It already searches every effect of the ability to check they exist,
  so the check needs no other subdomain.

### Adapters

#### Resources

- Modify `ResourcesAbilityLoader.AbilityDefinition` with `turnsDuration: Int? = null`.
- Modify `resources/ability/bat.toml` and `resources/ability/bee.toml` with `turnsDuration = 5`.

## effect

### Domain

- Modify `Effect.Dto.EffectApplicationDto.ApplicationSourceDto` with `abilityId: String? = null`, and
  `battleUnit(battleUnitId, abilityId = null)`, because applying a deploy effect needs the ability it was cast with to
  know how long the summon lasts. The effects applied on defeat to the nearby allies keep no ability.

## battleUnit

### Domain

- Add `BattleUnit.RemainingTurns`, optional, and `Dto.remainingTurns: Int?`: the turns a summon has left, counting the
  current one. `null` for the battle units that never vanish (the ones deployed when the game starts).
- Modify `BattleUnit.deploy` with `remainingTurns: Int? = null`.
- Add `BattleUnit.finishTurn(row, column)` because a summon counts its player's turns: it has one turn fewer, and when
  none is left it vanishes. Nothing changes for the other battle units, nor once defeated.
- Add `BattleUnit.vanish(row, column)`, for a summon when the battle ends.
- Add `BattleUnitEvent.BattleUnitVanished(playerId, battleUnitId, unitId, row, column)`. It carries the unit and the
  tile, as the battle unit is deleted before the screen flashes it.
- Add `BattleUnitRepository.delete(battleUnitId)` because a vanished battle unit is gone: deleted, nothing counts it, so
  `HasAllBattleUnitsDefeated` and `SearchBattleUnitsByPlayerId` need no change. A defeated one is still stored.

### Use cases

#### Commands

- Modify `ApplyEffect.applyToTile` so the summon is deployed with the turns duration of the ability in the application
  source, searched with `SearchAbilityById`. A deploy effect with no ability in its source is refused
  (`FailedToReceiveAbilityEffects`).
- Add `FinishSummonedBattleUnitsTurn(playerId, battleUnitIds)`: on `PlayerTurnFinished`, the summons of the player in the
  battle finish their turn. The vanished ones are deleted, and `BattleUnitVanished` is published.
- Add `VanishSummonedBattleUnits()`: on `BattleFinished`, every summon left vanishes and is deleted.

#### Services

- Modify `AbilityExecution` so the applications of a cast carry the ability in their source.
- Modify `BattleUnitDeployment.deploy` with `remainingTurns: Int? = null`.

### Adapters

#### Events

- Add `OnPlayerTurnFinished` and `OnBattleFinished`, wired in `BattleUnitApi`.

#### Storage

- Modify `InMemoryBattleUnitRepository` with `delete`.

## battle

### Domain

- Add `BattleEvent.PlayerTurnFinished(playerId, battleUnitIds)`, published by `Battle.finishPlayerTurn` before the next
  `PlayerTurnStarted` or `BattleRoundFinished`, because nothing tells today which player has just finished its turn, and
  the summons count it.

### Use cases

#### Commands

- Modify `StartNextRound` so it does nothing once the battle is finished, because a summon vanishing at the end of a round
  may defeat the last player in it (game over) before the next round starts.

### Adapters

#### Events

- Add `OnBattleUnitVanished`: the battle unit leaves the battle (`LeaveBattle`), and its player is defeated if it has no
  battle unit left (`DefeatPlayer`), as on `BattleUnitDefeated`.

## battlefield

### Adapters

#### Events

- Add `OnBattleUnitVanished`: its tile is vacated (`RemoveOccupant`), which publishes `OccupantRemoved`, so the screen
  removes it as a defeated one.

## cpuBrain

### Domain

- Add `CpuBrain.addBattleUnit(battleUnitId)` because a summon plays the turn it is summoned in: it is managed after the
  battle units already pending.

### Use cases

#### Commands

- Add `JoinCpuTurn(battleUnitId)`: on `BattleUnitDeployed`, a battle unit of the CPU player playing its turn (a summon)
  is added to its pending battle units. Nothing happens otherwise (e.g.: the battle units deployed when the game starts,
  or out of the CPU turns). `BattleUnitDeployed` is handled before the cast is resolved, so before the next decision.
- Modify `UpdateEngagement` so the CPU summons never leave the battle.

#### Queries

- Modify `SearchEnemiesWithinReach` so every enemy the summon can walk to is within its reach, however far, because a
  summon lasts only some turns, and spends them attacking. `WhatShouldCast` and `WhereShouldMove` use it, so a summon
  approaches and attacks the nearest enemy.

### Adapters

#### Events

- Add `OnBattleUnitDeployed`, wired in `CpuBrainApi`.

## wandering

### Use cases

#### Commands

- Modify `ClaimHome` so the summons claim no home, because they never wander: they never leave the battle, and they vanish
  when it ends.

## screen

### Adapters

#### Presentation

- Modify `BattlefieldPresenter` so, on `BattleUnitVanished`, it flashes the battle unit on its tile (`flashBattleUnit`).
  `OccupantRemoved` then removes it, after the flash, as both are played on the timeline.
- Add `RemainingTurnsView` (text, as `UnitNameView`), and modify `BattleUnitInfoView.display` so it shows "Vanishes in N
  turns" ("Vanishes in 1 turn" for the last one) after the cast points, only for the summons. It goes on the same line as
  the remaining steps and casts, so the health and mana bars do not move.

## Impact

- `DeployBattleUnitEffectAcceptanceTest`, `CpuDeployingAcceptanceTest` and `CastAbilityEndToEndTest` deploy bats and bees,
  so their test abilities need a turns duration. The tests that build `EffectApplicationDto` for a deploy need the ability.
- [Game data](../architecture/game-data.md) documents `turnsDuration`.
- [CPU brain](../architecture/cpu-brain.md) explains the CPU summons: they play the turn they are summoned in, chase any
  enemy, and never leave the battle nor wander.
- [Exploration](../architecture/exploration.md) explains that the summons vanish when the battle ends.

## Follow-ups

- Permanent summons (an ability with no turns duration), whose human ones would join the party and walk with it. Left out
  as no ability deploys one.
- `Battle.defeatPlayer` seems to drop the events of the turn it finishes. Left as it is.

## Milestones

Each milestone is committed and pushed on its own, with its tests and documentation, and leaves the game playable.

| # | Done | Milestone                               | Changes                                                                                                                                                                                                                                                   | The game after it                                                                                                                                           |
|---|------|-----------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1 | ✅   | Turns duration of the summon abilities  | `Ability.TurnsDuration` and its errors, `AbilityCreation` checks, `ResourcesAbilityLoader`, `bat.toml` and `bee.toml` last 5 turns. Game data docs.                                                                                                       | As before: the duration is loaded and checked, but not used yet.                                                                                            |
| 2 | ✅   | Summons vanish after their turns        | `ApplicationSourceDto.abilityId`, `AbilityExecution`, `ApplyEffect`, `BattleUnit.RemainingTurns`, `finishTurn`, `BattleUnitVanished`, `BattleUnitRepository.delete`, `PlayerTurnFinished`, `FinishSummonedBattleUnitsTurn`, the battle and battlefield `OnBattleUnitVanished`, `StartNextRound` guard, `ClaimHome`, the flash. | Bats and bees flash and vanish when their player finishes their 5th turn, defeating their player if they were its last battle units. They no longer wander. |
| 3 | ✅   | Turns left in the battle unit info      | `RemainingTurnsView`, `BattleUnitInfoView`.                                                                                                                                                                                                               | Selecting a bat or a bee tells how many turns it has left.                                                                                                  |
| 4 | ✅   | CPU summons chase the enemies           | `UpdateEngagement`, `SearchEnemiesWithinReach`. CPU brain docs.                                                                                                                                                                                           | A bat never leaves the battle, and approaches and attacks the nearest enemy, however far.                                                                   |
| 5 | ✅   | Summons vanish when the battle ends     | `BattleUnit.vanish`, `VanishSummonedBattleUnits`, `OnBattleFinished`. Exploration docs.                                                                                                                                                                   | Bees left when the battle ends vanish before exploring, which loses the game if only bees were left.                                                        |
| 6 | ✅   | CPU summons play the turn they arrive   | `CpuBrain.addBattleUnit`, `JoinCpuTurn`, the CPU brain `OnBattleUnitDeployed`. CPU brain docs.                                                                                                                                                             | A bat acts in the turn the goat summons it, as a bee already does.                                                                                          |
