# Plan: Rename the code of the battleUnit subdomain to the words of the glossary

> **Status:** to do.
>
> **Summary:** The code of the battle unit says what the [glossary](../../GLOSSARY.md) avoids, and one word often means
> several things. E.g.: `MoveBattleUnit` only walks in a battle, while `BattleUnitMoved` also fires for a walk while exploring,
> a swap and a teleport. A Bat summoned for 2 player turns counts `remainingTurns`, and a Knight's 3 steps are its
> `movementRange` and its `maximumSteps`, in the same class. Reading the code and talking about the game take a translation.
> Instead, the code says move for any change of tile and walk for going along a path, health and mana with no "points", and
> no bare "turn" but where the [effect section](#waiting-for-other-sections) decides it. The game plays the same, and players
> read the same texts. Builds on [Save and load](../architecture/save-and-load.md).

## Decisions

| Topic        | Decision                                                                                                                                                                                                                                             |
|--------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope        | The renames below, in the code, tests, comments and docs of the `battleUnit` package, and wherever other packages read its names. The own names of other packages wait for their sections.                                                           |
| Move, walk   | Move is any change of tile (a walk, a swap or a teleport), and walk is going along a path, in a battle or not. Chosen over move in a battle and walk outside it, which needs a new word for `BattleUnitMoved` and rewords Path and Walking distance. |
| Health, mana | The words are health and mana. Chosen over keeping "points" in the code, as the code already says `PreviewCastRemainingHealth` and `maximumHealthBonus`. `Unit.healthPoints` and `Unit.manaPoints` wait for the `unit` section.                      |
| Adjacency    | `AdjacencyService.isAdjacent` replaces `DistanceService.manhattanDistance`, whose 4 callers only skip anything above 1. Each already skips its own tile, so nothing changes. Chosen over a numeric `tilesApart`, which keeps the comparison.         |
| Bare "turn"  | `remainingTurns`, `finishTurn`, `cooldownTurnsLeft` and `RemainingTurnActions` change here. The on-turn-started names wait for the `effect` section, as they mirror `ApplicationTypeDto.ON_TURN_STARTED` and the word is chosen once.                |
| Save format  | Reached: seven fields of `BattleUnitSnapshot` change, so `SAVE_VERSION` goes from 4 to 5, with a new `v5.json` fixture. See [Save format](#save-format).                                                                                             |
| Player text  | None changes. Players keep HP, MP, MOVE, CAST, KO, "Vanishes in 2 turns" and "An empty tile next to another unit". One translation key changes, see [Translation key](#translation-key).                                                             |
| Words kept   | `BattleUnitMoved`, `RequestMoveOccupant` and `MarkBattleUnitMoved` stay, as they now mean any move. `restore` stays for rebuilding from a save, which is why `BattleUnitManaRestored` changes. Caster stays in the ability section.                  |
| Tests        | No new tests: a rename changes no behaviour. The existing tests are renamed with the code, and `DistanceServiceTest` becomes `AdjacencyServiceTest`, asserting `isAdjacent`.                                                                         |
| Out of scope | The bugs and leftovers in [Not in this plan](#not-in-this-plan), and every name waiting for another section. Devlog entries stay as written.                                                                                                         |

## Renames

| Today                                                | Glossary term             | New name                                             |
|------------------------------------------------------|---------------------------|------------------------------------------------------|
| `MoveBattleUnit`                                     | Walk                      | `WalkBattleUnitInBattle`                             |
| `RequestMoveBattleUnit`                              | Walk                      | `RequestWalkBattleUnitInBattle`                      |
| `OnRequestMoveBattleUnit`                            | Walk                      | `OnRequestWalkBattleUnitInBattle`                    |
| `WalkBattleUnit`                                     | Walk                      | `WalkBattleUnitOutOfBattle`                          |
| `RequestWalkBattleUnit`                              | Walk                      | `RequestWalkBattleUnitOutOfBattle`                   |
| `OnRequestWalkBattleUnit`                            | Walk                      | `OnRequestWalkBattleUnitOutOfBattle`                 |
| `BattleUnit.move`                                    | Walk                      | `walkInBattle`                                       |
| `BattleUnit.walk`                                    | Walk                      | `walkOutOfBattle`                                    |
| `CanMoveTo`, `WhereCanMove`                          | Walk                      | `CanWalkTo`, `WhereCanWalk`                          |
| `moveToRow`, `moveToColumn`                          | Walk                      | `walkToRow`, `walkToColumn`                          |
| `canMoveDistance`                                    | Walking distance          | `canWalkDistance`                                    |
| `MovementDistanceExceedsRemainingSteps`              | Walking distance          | `WalkingDistanceExceedsRemainingSteps`               |
| `MovementDistanceMustBeGreaterThanZero`              | Walking distance          | `WalkingDistanceMustBeGreaterThanZero`               |
| `remainingHealthPoints`, `RemainingHealthPoints`     | Remaining health          | `remainingHealth`, `RemainingHealth`                 |
| `remainingManaPoints`, `RemainingManaPoints`         | Remaining mana            | `remainingMana`, `RemainingMana`                     |
| `maximumHealthPoints`                                | Maximum health            | `maximumHealth`                                      |
| `maximumManaPoints`                                  | Maximum mana              | `maximumMana`                                        |
| `baseMaximumHealthPoints`, `BaseMaximumHealthPoints` | Base maximum health       | `baseMaximumHealth`, `BaseMaximumHealth`             |
| `baseMaximumManaPoints`, `BaseMaximumManaPoints`     | Base maximum mana         | `baseMaximumMana`, `BaseMaximumMana`                 |
| `RemainingManaPointsBelowZero`                       | Remaining mana            | `RemainingManaBelowZero`                             |
| `remainingTurns`, `RemainingTurns`                   | Remaining summon duration | `remainingSummonDuration`, `RemainingSummonDuration` |
| `BattleUnit.finishTurn`                              | Remaining summon duration | `reduceRemainingSummonDuration`                      |
| `FinishSummonedBattleUnitsTurn`                      | Remaining summon duration | `ReduceRemainingSummonDurations`                     |
| `VanishSummonedBattleUnits`                          | Summon                    | `VanishSummons`                                      |
| id `deployed-unit-<random>`                          | Summon                    | `summon-<random>`                                    |
| `RemainingTurnActions`, `RemainingTurnActionsDto`    | Action                    | `RemainingActions`, `RemainingActionsDto`            |
| `remainingTurnActions`                               | Action                    | `remainingActions`                                   |
| `CASTS_PER_TURN`                                     | Action                    | `CASTS_PER_PLAYER_TURN`                              |
| `MovementRange`, `movementRange` in `BattleUnit`     | Maximum steps             | `MaximumSteps`, `maximumSteps`                       |
| `CooldownTurnsLeft`, `cooldownTurnsLeft`             | Remaining cooldown        | `RemainingCooldown`, `remainingCooldown`             |
| `AbilityCooldowns`, `abilityCooldowns`               | Known ability             | `KnownAbilities`, `knownAbilities`                   |
| `HasAllBattleUnitsDefeated`                          | Defeated                  | `AreAllBattleUnitsDefeated`                          |
| `RequestBattleJoin`                                  | Join                      | `RequestJoinBattle`                                  |
| `RequestBattleLeave`                                 | Leave                     | `RequestLeaveBattle`                                 |
| `BattlefieldTileCanNotBeOccupied`                    | Vacant                    | `TileIsNotVacant`                                    |
| `BattleUnitManaRestored`                             | Mana                      | `BattleUnitManaIncreased`                            |
| `AbilityExecution`                                   | Cast                      | `CastResolution`                                     |
| `selectedRow`, `selectedColumn`                      | Targeted tile             | `targetedRow`, `targetedColumn`                      |
| `DistanceService.manhattanDistance`                  | Adjacent                  | `AdjacencyService.isAdjacent`                        |
| local `distance` in `WhereCanCast`                   | Adjacent                  | `tilesAround`                                        |

## battleUnit

- `BattleUnit` and `BattleUnit.Dto`, with `SnapshotDto`, `BattleUnitEvent` and `BattleUnitError`, follow the table. The messages
  of the errors say the same words, e.g.: `TileIsNotVacant` says "Tile is not vacant".
- `BattleUnitDeployment`, `ApplyEffect` and `InitialiseBattleUnits` follow the names above: `remainingTurns` is
  `remainingSummonDuration`, and `BattlefieldTileCanNotBeOccupied` is `TileIsNotVacant`. The id of a summon is `summon-<random>`.
- `AdjacencyService.isAdjacent(fromRow, fromColumn, toRow, toColumn)` tells two tiles that share a side, and is false for a
  tile and itself. `AbilityExecution`, `WhereCanCast` and `ApplyOnDefeatedEffectsToAdjacentAllies` ask it instead of comparing
  a distance with 1, and `searchAdjacentEnemyPositions` calls its local `distance` `tilesAround`.
- `SearchAbilityAvailabilities.AbilityAvailability`: `cooldownTurnsLeft` is `remainingCooldown`.
- `BattleUnitApi` and the `adapters/events` subscribers follow the names above, e.g.: `OnPlayerTurnFinished` calls
  `reduceRemainingSummonDurations`.

## Other subdomains

These only follow the names they read from `battleUnit`. Their own names wait for their sections, see
[Waiting for other sections](#waiting-for-other-sections).

- `cpuBattleBrain`: `ManageCpuBattleUnit`, `WhatShouldCast`, `WhereShouldMove`, `SearchEnemiesWithinReach`, `HealingNeed`,
  `UpdateEngagement`, `WhereShouldWander` and `CpuBattleBrainApi`.
- `cpuWanderBrain`: `ClaimHome` and `WanderingSteps`.
- `party`: `PartyWalking`.
- `screen`: `BattlefieldPresenter`, `BattlefieldView`, `BattleUnitInfoMapper`, `AbilityInfoMapper`, `FormationPresenter`,
  `InventoryPresenter`, and in `battlefieldHud`, `ProcessTileSelected`, `SelectBattleUnit`, `RefreshMovementRange`,
  `MovementService` and `BattlefieldHudApi`.
- `storybook`: the stories that build a `BattleUnit.Dto`, and `StorybookApi`.

## Save format

`BattleUnitSnapshot` in `BattleUnitSnapshottable` reaches the save, so `Save.SAVE_VERSION` goes from 4 to 5, and
`src/jvmTest/fixtures/save/v5.json` is written with the command in
[Save and load](../architecture/save-and-load.md#save_version), keeping the older fixtures.

| Saved field today         | New                       |
|---------------------------|---------------------------|
| `remainingHealthPoints`   | `remainingHealth`         |
| `remainingManaPoints`     | `remainingMana`           |
| `baseMaximumHealthPoints` | `baseMaximumHealth`       |
| `baseMaximumManaPoints`   | `baseMaximumMana`         |
| `movementRange`           | `maximumSteps`            |
| `abilityCooldowns`        | `knownAbilities`          |
| `remainingTurns`          | `remainingSummonDuration` |

No TOML key changes: `resources/battleUnit/*.toml` only holds `unitId`, `playerId`, `row` and `column`.

## Translation key

`UiText.REMAINING_TURNS` is `REMAINING_SUMMON_DURATION`, and its key changes, also as `msgctxt` in
`resources/localization/messages.pot` and `resources/localization/es.po`, and in `ResourcesLocalizationLoaderTest`, `PoFileTest`,
`LocalizationTest`, `LocalizationMother` and `TranslatePluralTest`. Its `msgid` and translations stay.

| Today                        | New                                   |
|------------------------------|---------------------------------------|
| `ui.remainingTurns.vanishes` | `ui.remainingSummonDuration.vanishes` |

## Docs

- [Game data](../architecture/game-data.md), [Exploration](../architecture/exploration.md),
  [CPU battle brain](../architecture/cpu-battle-brain.md), [Package structure](../architecture/package-structure.md),
  [Presentation timeline](../architecture/presentation-timeline.md), [Save and load](../architecture/save-and-load.md) and
  [How to add a new test](../testing/how-to-add-a-new-test.md) use the new names. E.g.: Exploration says "A walk out of a
  battle spends no step, unlike one in a battle" for "A walk spends no turn action, unlike a move in a battle".
- `docs/architecture/dependencies.html` names the classes as they are.

## Tests

- Renamed with their class: `MoveBattleUnitTest`, `WalkBattleUnitTest`, `CanMoveToTest`, `WhereCanMoveTest`,
  `HasAllBattleUnitsDefeatedTest`, `RequestBattleJoinTest`, `RequestBattleLeaveTest`, `FinishSummonedBattleUnitsTurnTest`,
  `VanishSummonedBattleUnitsTest` and `DistanceServiceTest`.
- `AdjacencyServiceTest`
  - `IsAdjacent should be true when the tiles share a side`
  - `IsAdjacent should be false when the tiles are diagonal`
  - `IsAdjacent should be false when it is the same tile`
- `SaveFixtureTest` passes with `v5.json`.

## Not in this plan

- `ApplyOnTurnStartedEffects` looks like a bug: its inner loop starts every effect from the same stale `battleUnit`. A battle
  unit poisoned twice by venom publishes `BattleUnitDamaged` twice but loses the health of one. Whether stacked venom should
  count twice is still to be decided.
- `BattleUnit.applyImmediateEffect` looks like a bug: the `TELEPORT` branch never takes the `ongoingEffects` it computed, so each
  teleport leaves one more immediate effect in the ongoing effects, and in the save, until a respawn.
- A defeated summon is never deleted, as `vanish` and `finishTurn` change nothing once defeated. `ClaimHome` skips it, so it
  never respawns and stays in the repository and the save for good.
- The comment of `SearchBattleUnitsSnapshot` says the defeated ones are left out as they are deleted, while it takes them all
  and nothing deletes them. The defeated CPU battle units have to stay, to respawn.
- `WhereCanCast` lists a vacant tile once per battle unit next to it, so `VACANT_TILE_ADJACENT_TO_ANOTHER_BATTLE_UNIT` can repeat
  a cast group.
- `MoveBattleUnit` stores with `create` instead of `update`. The message of `RemainingManaPointsBelowZero` says "must be greater
  than zero", while 0 is allowed.

### Waiting for other sections

- `effect`: Apply is still to be defined. `OnTurnStartedEffectApplied`, `ApplyOnTurnStartedEffects`, `receiveOnTurnStartedEffect`,
  `hasOnTurnStartedEffects`, `applyOnTurnStartedEffect`, the saved `ON_TURN_STARTED` and `turnsLeft` mirror `OnTurnStarted` and
  `duration`, so they change with them. The saved ones bump `SAVE_VERSION` again.
- `unit`: Unit is still to be defined. `healthPoints`, `manaPoints` and `movementRange`, which are TOML keys, while "points" and
  "movement range" are to be avoided here. Maximum steps may fit `movementRange`.
- `player`: only the enemy player is left to define (`SearchEnemyPlayer`), as Enemy is a battle unit here. The battle section's
  Player defeat says "their enemy" for a player, so check that entry.
- `party`: the party's own Step (`ResolvePartyStep`, "walked 50 steps"), against Step here.
- `cpuBattleBrain`: `WhereShouldMove`, while it picks a tile to walk to.
- `cpuWanderBrain`: Wandering is still to be defined, as Recover mentions it. `WANDERING_RECOVERY_PERCENTAGE` already fits.
- `lootableItem`: Equip and Consume are still to be defined, as Equipped effect and Learned ability mention them.
- `screen`: names of its own that repeat the old words: `RemainingTurnsView`, `RefreshMovementRange`, `MovementService`, and the
  fields of `BattleUnitInfo` and `AbilityButtonView`, such as `remainingHealthPoints` and `cooldownTurnsLeft`.
