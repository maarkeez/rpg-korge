# Plan: Rename the code of the cpuBattleBrain subdomain to the words of the glossary

> **Status:** done.
>
> **Summary:** The code of the cpuBattleBrain subdomain uses words the [glossary](../../GLOSSARY.md) avoids or gives
> another meaning. E.g.: when a rat two tiles away from a knight walks next to it, `ManageCpuBattleUnit` asks
> `WhereShouldMove`, `requestMove` awaits an `AwaitedAction.Move` counted in `actionsTaken`, and once the rat has nothing
> left to do it is "managed" (`CpuBattleUnitManaged`), so "manage" means both deciding and being done. The walk is no
> Action (a step or a cast), as the rat's whole walk counts once towards `MAX_ACTIONS_PER_TURN`. The rat's sword is scored
> as `CastKind.ATTACK`, the CPU starts the battle with `CombatEngaged`, and after each round it "updates the engagement".
> Reading the code and talking about the game take a translation. Instead, the code says Decision, Done, Walk, player
> turn, Engage, Reach, Join and Leave, Option, Tiles apart and Return, as the glossary does. The game plays the same, and
> players read the same texts. Builds on [CPU battle brain](../architecture/cpu-battle-brain.md),
> [Exploration](../architecture/exploration.md), the [battle renames](battle-renames.md) and the
> [battleUnit renames](battleUnit-renames.md).

## Decisions

| Topic         | Decision                                                                                                                                                                                                                                                                    |
|---------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope         | The renames below, in the code, tests, comments and docs of the cpuBattleBrain subdomain, and where the battle and cpuWanderBrain subdomains publish its requests or mention its words.                                                                                     |
| Decision      | A cast or a walk the CPU battle brain chooses and awaits is a Decision, as a whole walk counts once while the glossary's Action is a step or a cast. Chosen over Order, and over keeping Action with two meanings.                                                          |
| Done          | A battle unit with nothing left to decide is Done, and making the next decision is `MakeNextDecision`, so "manage" no longer means both. Chosen over keeping Manage.                                                                                                        |
| Player turn   | Bare "turn" says "player turn", as the battle section avoids "turn": `PlayPlayerTurn`, `CpuPlayerTurnStarted`, `CpuPlayerTurnFinished`, `PLAYER_TURN_DISCOUNT`.                                                                                                             |
| Walk          | Every "move" of this subdomain is a walk, as it requests `RequestWalkBattleUnitInBattle`: `WhereShouldWalk`, `requestWalk`, `ResolveCpuBattleUnitWalk`. Chosen over keeping Move, which also covers swaps and teleports.                                                    |
| Engage, Reach | Engage is starting a battle, so `CombatEngaged` is `EnemiesEngaged`, as "combat" is avoided. `ENGAGEMENT_DISTANCE` is `REACH`.                                                                                                                                              |
| Join, Leave   | The CPU battle units joining and leaving a battle use the battle section's Join and Leave: `JoinAndLeaveBattle`, `CpuBattleUnitsJoinedAndLeft` and `JoinDisengagedBattle`. Chosen over defining Engagement update and Re-engage, as "engaged" is avoided for in the battle. |
| Cast kind     | `DAMAGE`, `HEAL`, `SUMMON` and `OTHER`, as "attack" is avoided and an ability summons a summon.                                                                                                                                                                             |
| Option        | The casts and tiles scored are options, as the CPU battle brain doc already says "every option is scored". Chosen over Candidate.                                                                                                                                           |
| Tiles apart   | The count of tiles along rows and columns, whatever stands between them, is `tilesApart`. Chosen over Grid distance and Tile distance, which read as siblings of the walking distance.                                                                                      |
| Return        | The local `isHeadingHome` of `WhereShouldWander` is `returns`, as the glossary's Return covers both leaving the territory and returning after it.                                                                                                                           |
| Words kept    | `CastDecision`, `SearchEnemiesWithinReach`, `EngageEnemies`, `HealingNeed`, `MINIMUM_UTILITY_GAIN`, `IsOutsideTerritory`, `SearchDistancesFromHome`, `WANDER_DISTANCE`, `REST_CHANCE` and `pendingBattleUnits` already fit.                                                 |
| Save format   | Not reached: the `CpuBattleBrain` lives in `InMemoryCpuBattleBrainRepository` and is never saved, and `WhereShouldWander`'s local is not saved either, so `SAVE_VERSION` does not change.                                                                                   |
| Player text   | None changes, and no translation key or catalog changes: players read no word of this subdomain.                                                                                                                                                                            |
| Tests         | No new tests: a rename changes no behaviour. The tests of the subdomain are renamed with the code.                                                                                                                                                                          |
| Out of scope  | The wandering code's home, the missing subscribers and the possible bugs, and the words of the cpuWanderBrain, player, terrain and unit subdomains. See [Not in this plan](#not-in-this-plan).                                                                              |

## Renames

| Today                                             | Glossary term | New name                                            |
|---------------------------------------------------|---------------|-----------------------------------------------------|
| `PlayTurn`                                        | Player turn   | `PlayPlayerTurn`                                    |
| `CpuBattleBrainApi.playTurn`                      | Player turn   | `CpuBattleBrainApi.playPlayerTurn`                  |
| `CpuBattleBrain.startTurn`                        | Player turn   | `CpuBattleBrain.startPlayerTurn`                    |
| `CpuBattleBrain.finishTurn`                       | Player turn   | `CpuBattleBrain.finishPlayerTurn`                   |
| `JoinCpuTurn`                                     | Player turn   | `JoinCpuPlayerTurn`                                 |
| `CpuBattleBrainApi.joinCpuTurn`                   | Player turn   | `CpuBattleBrainApi.joinCpuPlayerTurn`               |
| `CpuBattleBrainEvent.CpuBattleBrainInitialized`   | Player turn   | `CpuBattleBrainEvent.CpuPlayerTurnStarted`          |
| `OnCpuBattleBrainInitialized`                     | Player turn   | `OnCpuPlayerTurnStarted`                            |
| `CpuBattleBrainEvent.CpuTurnFinished`             | Player turn   | `CpuBattleBrainEvent.CpuPlayerTurnFinished`         |
| `TURN_DISCOUNT`                                   | Player turn   | `PLAYER_TURN_DISCOUNT`                              |
| `turnsToAttack`                                   | Player turn   | `playerTurnsToDamage`                               |
| `CpuBattleBrain.MAX_ACTIONS_PER_TURN`             | Decision      | `CpuBattleBrain.MAX_DECISIONS_PER_PLAYER_TURN`      |
| `CpuBattleBrain.hasReachedActionLimit`            | Decision      | `CpuBattleBrain.hasReachedDecisionLimit`            |
| `CpuBattleBrain.AwaitedAction`                    | Decision      | `CpuBattleBrain.AwaitedDecision`                    |
| `awaitedAction`                                   | Decision      | `awaitedDecision`                                   |
| `ActionsTaken`, `actionsTaken`                    | Decision      | `DecisionsTaken`, `decisionsTaken`                  |
| `CpuBattleBrain.Dto.isAwaitingAction`             | Decision      | `CpuBattleBrain.Dto.isAwaitingDecision`             |
| `requestAction`, `resolveAction`                  | Decision      | `requestDecision`, `resolveDecision`                |
| `CpuBattleBrainEvent.CpuBattleUnitActionResolved` | Decision      | `CpuBattleBrainEvent.CpuBattleUnitDecisionResolved` |
| `OnCpuBattleUnitActionResolved`                   | Decision      | `OnCpuBattleUnitDecisionResolved`                   |
| `CpuBattleBrainError.AlreadyAwaitingAnAction`     | Decision      | `CpuBattleBrainError.AlreadyAwaitingADecision`      |
| `ManageCpuBattleUnit`                             | Decision      | `MakeNextDecision`                                  |
| `CpuBattleBrainApi.manageCpuBattleUnit`           | Decision      | `CpuBattleBrainApi.makeNextDecision`                |
| `CpuBattleBrain.manageBattleUnit`                 | Done          | `CpuBattleBrain.markDone`                           |
| `CpuBattleBrainEvent.CpuBattleUnitManaged`        | Done          | `CpuBattleBrainEvent.CpuBattleUnitDone`             |
| `OnCpuBattleUnitManaged`                          | Done          | `OnCpuBattleUnitDone`                               |
| `CpuBattleBrainError.NoBattleUnitsToManage`       | Done          | `CpuBattleBrainError.NoBattleUnitsToDecideFor`      |
| `WhereShouldMove`                                 | Walk          | `WhereShouldWalk`                                   |
| `CpuBattleBrainApi.whereShouldMove`               | Walk          | `CpuBattleBrainApi.whereShouldWalk`                 |
| `CpuBattleBrain.requestMove`, `resolveMove`       | Walk          | `CpuBattleBrain.requestWalk`, `resolveWalk`         |
| `AwaitedAction.Move`                              | Walk          | `AwaitedDecision.Walk`                              |
| `ResolveCpuBattleUnitMove`                        | Walk          | `ResolveCpuBattleUnitWalk`                          |
| `CpuBattleBrainApi.resolveCpuBattleUnitMove`      | Walk          | `CpuBattleBrainApi.resolveCpuBattleUnitWalk`        |
| `CpuBattleBrainEvent.CombatEngaged`               | Engage        | `CpuBattleBrainEvent.EnemiesEngaged`                |
| `ENGAGEMENT_DISTANCE`                             | Reach         | `REACH`                                             |
| `UpdateEngagement`                                | Join, Leave   | `JoinAndLeaveBattle`                                |
| `CpuBattleBrainApi.updateEngagement`              | Join, Leave   | `CpuBattleBrainApi.joinAndLeaveBattle`              |
| `CpuBattleBrainEvent.RequestUpdateEngagement`     | Join, Leave   | `CpuBattleBrainEvent.RequestJoinAndLeaveBattle`     |
| `OnRequestUpdateEngagement`                       | Join, Leave   | `OnRequestJoinAndLeaveBattle`                       |
| `CpuBattleBrainEvent.EngagementUpdated`           | Join, Leave   | `CpuBattleBrainEvent.CpuBattleUnitsJoinedAndLeft`   |
| `ReengageEnemies`                                 | Join          | `JoinDisengagedBattle`                              |
| `CpuBattleBrainApi.reengageEnemies`               | Join          | `CpuBattleBrainApi.joinDisengagedBattle`            |
| `CastKind.ATTACK`, `CastKind.DEPLOY`              | Cast kind     | `CastKind.DAMAGE`, `CastKind.SUMMON`                |
| `attackUtility`, `deployUtility`                  | Cast kind     | `damageUtility`, `summonUtility`                    |
| `candidates`, `candidatePositions`                | Option        | `options`, `optionTiles`                            |
| `UtilityEvaluationData.candidateUtility`          | Option        | `UtilityEvaluationData.optionUtility`               |
| `manhattanDistance`                               | Tiles apart   | `tilesApart`                                        |
| `isHeadingHome`                                   | Return        | `returns`                                           |

## cpuBattleBrain

### Domain

- `CpuBattleBrain` plays a CPU player turn one decision at a time: `startPlayerTurn` publishes `CpuPlayerTurnStarted`,
  `requestWalk` and `requestCast` await an `AwaitedDecision` and count it in `decisionsTaken`, `resolveWalk` and
  `resolveCast` publish `CpuBattleUnitDecisionResolved`, `markDone` publishes `CpuBattleUnitDone`, and
  `finishPlayerTurn` publishes `CpuPlayerTurnFinished`. Its comments say "decision" for "action", and "done" for
  "managed". E.g.: "Marks the next battle unit as done, once it has nothing left to decide this player turn".
- `MAX_DECISIONS_PER_PLAYER_TURN` stays 20, and its comment says "every decision spends casts or steps".
- `CpuBattleBrainError.NoBattleUnitsToDecideFor` says "At least one battle unit is required to play a CPU player turn",
  `NotTheNextBattleUnit` says "Only the next battle unit to decide for can act", and `AlreadyAwaitingADecision` says "A new
  decision can not be requested while awaiting the previous one".
- `CpuBattleBrainEvent.EnemiesEngaged`, `CpuBattleUnitsJoinedAndLeft` and `RequestJoinAndLeaveBattle` keep their fields.
  Their comments say "join" and "leave" for "update the engagement".

### Use cases

- `PlayPlayerTurn` starts the CPU player turn, and `MakeNextDecision` makes the next decision. Its comment says "the battle
  unit is done and the next one follows" for "the battle unit is managed".
- `JoinCpuPlayerTurn` has a battle unit deployed during the CPU player turn play it too.
- `ResolveCpuBattleUnitWalk` lets the CPU battle brain continue once the walk it awaits ends on its tile.
- `JoinAndLeaveBattle` and `JoinDisengagedBattle` publish `CpuBattleUnitsJoinedAndLeft`. Their comments say "join" and
  "leave" for "update the engagement" and "re-engage".
- `WhereShouldWalk` scores `optionTiles`, with `tilesApart` to the nearest enemy and ally.
- `WhatShouldCast` scores options of `CastKind.DAMAGE`, `HEAL`, `SUMMON` and `OTHER`, with `damageUtility` and
  `summonUtility`, discounting by `PLAYER_TURN_DISCOUNT` for every one of the `playerTurnsToDamage`. Its comments say
  "damage" and "summon" for "attack" and "deploy".
- `SearchEnemiesWithinReach` walks at most `REACH` tiles.
- `WhereShouldWander` says `returns` where it heads home.

### Adapters

- `OnPlayerTurnStarted` calls `playPlayerTurn`. `OnCpuPlayerTurnStarted`, `OnCpuBattleUnitDecisionResolved` and
  `OnCpuBattleUnitDone` call `makeNextDecision`. `OnTileOccupied` calls `resolveCpuBattleUnitWalk`. `OnBattleUnitDeployed`
  calls `joinCpuPlayerTurn`. `OnRequestJoinAndLeaveBattle` calls `joinAndLeaveBattle`. `OnBattleDisengaged` calls
  `joinDisengagedBattle`.
- `CpuBattleBrainApi` builds them under their new names.

## cpuWanderBrain

- `CpuWanderBrain.finishWhenDone` publishes `RequestJoinAndLeaveBattle` after a round, and its comment says "having the CPU
  battle units join and leave the battle" for "updating the engagement".
- `WanderAfterRound`'s comment says "before the CPU battle units join and leave the battle".

## battle

- The comments of `OnRequestStartNextRound`, `OnRequestResolveDisengagedBattle` and `ResolveDisengagedBattle` say the CPU
  battle units "joined and left the battle" and "joined the disengaged battle", for "updated the engagement",
  "re-engaged the enemies" and "re-engages it".

## Docs

- [CPU battle brain](../architecture/cpu-battle-brain.md): "Playing a turn" is "Playing a player turn", with
  `PlayPlayerTurn`, `CpuPlayerTurnStarted`, `MakeNextDecision`, `CpuBattleUnitDone`, `CpuPlayerTurnFinished`,
  `CpuBattleUnitDecisionResolved`, `JoinCpuPlayerTurn` and `MAX_DECISIONS_PER_PLAYER_TURN`, and "decision" for "action".
  `ENGAGEMENT_DISTANCE` is `REACH`. The kinds of the casting tables are Damage, Heal, Summon and Other, and the rat's
  table decides Damage. "Candidate" is "option". "Moving" is "Walking", with `WhereShouldWalk`, and "using the Manhattan
  distance" is "counting the tiles apart". `CombatEngaged` is `EnemiesEngaged`, `UpdateEngagement` is
  `JoinAndLeaveBattle`, `EngagementUpdated` is `CpuBattleUnitsJoinedAndLeft`, and `RequestUpdateEngagement` is
  `RequestJoinAndLeaveBattle`.
- [Exploration](../architecture/exploration.md): "Engaging" says the battle units join and leave the battle, with
  `EnemiesEngaged`, `JoinAndLeaveBattle`, `CpuBattleUnitsJoinedAndLeft` and `JoinDisengagedBattle`, for "the engagement is
  updated" and "re-engage". "The next engagement starts a new battle" is "The next time CPU battle units engage, a new
  battle starts".
- [Package structure](../architecture/package-structure.md): `RequestJoinAndLeaveBattle`, `EnemiesEngaged` and
  `CpuBattleUnitsJoinedAndLeft`, with "The CPU battle units engage the party" and "the CPU battle units join and leave the
  battle" for "The combat is engaged" and "the engagement is updated".
- `docs/architecture/dependencies.html` is regenerated by the pre-commit hook.

## Tests

- `CpuBattleBrainTest`
    - `StartPlayerTurn should leave the battle units pending in order when the player turn starts` (replaces `StartTurn
      should manage the battle units in order when the turn starts`)
    - `StartPlayerTurn should fail when there are no battle units to decide for`, expecting `NoBattleUnitsToDecideFor`
    - `RequestWalk should await the walk and count the decision when the next battle unit walks` (replaces `RequestMove
      should await the move and count the action when the next battle unit moves`)
    - `RequestWalk should fail when the battle unit is not the next one to decide for`
    - `RequestWalk should fail when the previous decision is still awaited`, expecting `AlreadyAwaitingADecision`
    - `RequestCast should await the cast and count the decision when the next battle unit casts`
    - `ResolveWalk should continue when the awaited walk reaches the requested tile`
    - `ResolveWalk should keep waiting when another tile is occupied`
    - `ResolveWalk should keep waiting when a cast is awaited`
    - `MarkDone should continue with the next battle unit when the battle unit is done` (replaces `ManageBattleUnit should
      continue with the next battle unit when the battle unit is managed`)
    - `MarkDone should fail when the battle unit decision is still awaited`
    - `HasReachedDecisionLimit should reach the decision limit when the maximum decisions per player turn have been taken`
    - `FinishPlayerTurn should have no battle units left when the player turn is finished`
    - `AddBattleUnit should leave the battle unit pending after the ones already pending when the player turn is being
      played`, `AddBattleUnit should not leave the battle unit pending twice`, and `AddBattleUnit should not leave the
      battle unit pending once the player turn is over`
- `MakeNextDecisionTest` (replaces `ManageCpuBattleUnitTest`)
    - `should cast and await the cast when the next battle unit should cast`
    - `should walk and await the walk when the next battle unit should walk and not cast`
    - `should mark the battle unit done when it has nothing left to do`
    - `should mark the battle unit done without acting when it has been defeated`
    - `should finish the player turn when every battle unit is done`
    - `should finish the player turn when the decision limit has been reached`
    - `should not act when the cpu battle brain is awaiting a decision`
    - `should not act when the cpu battle brain belongs to another player`
- `PlayPlayerTurnTest` (replaces `PlayTurnTest`): `should not play the player turn when the player does not exist`, `should
  not play the player turn when the player is not a cpu`, `should leave the battle units pending in order when the player
  is a cpu`, `should only leave the battle units in the battle pending when the player is a cpu`, and `should finish the
  player turn when the cpu player has no battle units in the battle` and `... has no battle units`.
- `JoinCpuPlayerTurnTest` (replaces `JoinCpuTurnTest`): "cpu turn" is "cpu player turn" in its names.
- `ResolveCpuBattleUnitWalkTest` (replaces `ResolveCpuBattleUnitMoveTest`): `should continue the cpu player turn when the
  awaited walk has ended`, `should ignore the walk when it is not the awaited one`, and `should ignore the walk when there
  is no cpu player turn in progress`. `ResolveCpuBattleUnitCastTest` says "cpu player turn".
- `WhereShouldWalkTest` (replaces `WhereShouldMoveTest`): "move" is "walk" and "candidates" is "options" in its names.
  E.g.: `should walk to the tile closest to the enemy when the battle unit is healthy`.
- `WhatShouldCastTest`: "attack" is "damage" and "deploy" is "summon" in its names. E.g.: `should damage the adjacent enemy
  when the caster is healthy`, and `should summon a battle unit on the tile nearest to the enemy when several tiles are
  vacant`.
- `JoinAndLeaveBattleTest` (replaces `UpdateEngagementTest`): `should have nobody join when no cpu battle unit out of the
  battle has an enemy within reach` and `should have nobody join nor leave when there is no human player`, for "update
  the engagement without anybody ...".
- `JoinDisengagedBattleTest` (replaces `ReengageEnemiesTest`), asserting the same.
- Listener tests: `OnCpuPlayerTurnStartedTest`, `OnCpuBattleUnitDecisionResolvedTest`, `OnCpuBattleUnitDoneTest`,
  `OnRequestJoinAndLeaveBattleTest`, and `OnPlayerTurnStartedTest`, `OnTileOccupiedTest`, `OnBattleDisengagedTest`,
  `OnExplorationStartedTest`, `OnPartyWalkedTest` and `OnRequestEngageEnemiesTest`, which expect the renamed events. E.g.:
  `should make the next decision when the cpu battle unit is done` (replaces `should manage the next cpu battle unit when
  a cpu battle unit is managed is dispatched`), and `should have the cpu battle units join the disengaged battle when it
  is disengaged` (replaces `should re-engage the enemies when the battle is disengaged`).
- `CpuBattleBrainMother`, `InMemoryQueries`, `CpuWanderBrainTest` and `WanderAfterRoundTest` use the new names. The
  acceptance tests keep their names, which describe the game: their words wait for the sections of the subdomains they
  play.

## Not in this plan

- Nothing subscribes to `CombatEngaged` (`EnemiesEngaged`), `EngagementUpdated` (`CpuBattleUnitsJoinedAndLeft`) nor
  `CpuTurnFinished` (`CpuPlayerTurnFinished`): they are published, and only the tests read them. Whether they should stay
  is still to be decided.
- `WhereShouldMove` looks like it has a bug: it normalises the distances to the nearest enemy and ally by dividing by the
  furthest minus the nearest, which is 0 when every candidate tile is as far from the nearest enemy (or ally), e.g.: two
  knights on either side. The utilities are then NaN, `maxBy` picks the first candidate, and the battle unit walks to the
  first tile `WhereCanWalk` returns instead of staying.
- `WhereShouldMove` and the deploy scoring of `WhatShouldCast` count the tiles apart, not the walking distance, so a rat
  with a knight behind void walks towards the void. Whether they should use the walking distance, as
  `SearchEnemiesWithinReach` does, is still to be decided.
- The wandering decisions live in cpuBattleBrain while only cpuWanderBrain calls them: `WhereShouldWander`,
  `IsOutsideTerritory`, `SearchDistancesFromHome`, `WANDER_DISTANCE`, `REST_CHANCE` and `terrainHealthContribution`,
  which `WhereShouldMove` also uses. Whether they should move is still to be decided.

### Waiting for other sections

- `cpuWanderBrain`: Wander and Home are still to be defined, as Territory, Distance from home, Rest and Return use them.
  `ClaimHome`, and its `Purpose.AfterRound`, which requests the CPU battle units to join and leave the battle.
- `player`: CPU, CPU player and CPU battle unit are still to be defined, with `SearchEnemyPlayer` and `SearchHumanPlayer`.
- `terrain`: harmful and healing terrain are still to be defined, with `SearchTerrainHealthEffect` and
  `terrainHealthContribution`.
- `unit`: `movementRange` in `WhatShouldCast.deployUtility`, which may become Maximum steps, as the battleUnit section
  already waits for.

## Changes while building

- Names the plan left out follow the table: `CpuBattleBrain.isPlayingTurn` is `isPlayingPlayerTurn`, the local
  `managedBattleUnitIds` of `PlayPlayerTurn` is `pendingBattleUnitIds`, the `action` parameter of `requestDecision` is
  `decision`, and the local `managing` of `CpuBattleBrainTest` is `markingDone`.
- `WhatShouldCast`'s comment on the summon scoring no longer says the summon "acts from the next turn", which contradicted
  [CPU battle brain](../architecture/cpu-battle-brain.md): it acts in the player turn it is summoned in. Its locals
  `deployedUnitId` and `deployedUnit` stay, as that unit is deployed.
- The wandering part of [CPU battle brain](../architecture/cpu-battle-brain.md) also says "option" for "candidate", and
  `WhereShouldWander`'s comments say it "returns" for "heads home", as the glossary avoids both.
- `InMemoryCpuBattleBrainRepository`'s comment also says "player turn".
- `EngagementAcceptanceTest` and the `engagements` locals of the listener tests keep their names: the acceptance tests keep
  theirs, as planned, and the locals collect the times the CPU battle units engage.
