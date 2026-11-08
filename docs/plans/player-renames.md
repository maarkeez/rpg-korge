# Plan: Rename the code of the player subdomain to the words of the glossary

> **Status:** to do.
>
> **Summary:** The code of the player subdomain calls the other player the enemy (`SearchEnemyPlayer`), while the
> [glossary](../../GLOSSARY.md) gives Enemy to a battle unit of another player. E.g.: when the human player's last
> battle unit falls to lava while exploring, `DefeatPlayer` asks `SearchEnemyPlayer` for the winner, and its comment
> says "their enemy is left as the only player", as if the CPU player were a battle unit. The party, the inventory and
> their tests also call the human player "the party player". So reading the code and talking about the game take a
> translation. Instead, the code, the tests and the docs say opponent and human player. The game plays the same, and
> players read the same text. Builds on the [battle renames](battle-renames.md), the
> [battleUnit renames](battleUnit-renames.md) and the [party renames](party-renames.md).

## Decisions

| Topic        | Decision                                                                                                                                                                                        |
|--------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope        | The renames below, in the code, tests, comments and docs of the player subdomain, and where other subdomains call its Api or say its words.                                                    |
| Opponent     | The other player is the opponent (`SearchOpponent`, `searchOpponent`, `opponent`). Chosen over "enemy player", as Enemy is a battle unit of another player.                                  |
| Human player | "The party player" is the human player, in comments, test names and docs. The party's `playerId` stays, as it already names the player the party belongs to, the human player.               |
| Words kept   | `Player.Dto.PlayerTypeDto`, `HUMAN` and `CPU`, `SearchHumanPlayer`, `SearchPlayerById`, `InitialisePlayers` and `RequestPlayerCreation` already fit. "Player type" is a programmer's word, left out of the glossary. |
| Save format  | Not reached: players are read from the resources again when a game is loaded, and no snapshot field is renamed. `SAVE_VERSION` stays.                                                          |
| Player text  | None: players keep reading "Human turn" and "CPU wins!", and no translation key changes.                                                                                                       |
| Tests        | No new tests: a rename changes no behaviour. The existing tests are renamed with the code.                                                                                                     |
| Out of scope | `InMemoryPlayerRepository.searchEnemy` throwing when there is no other player. See [Not in this plan](#not-in-this-plan).                                                                     |

## Renames

| Today                                      | Glossary term | New name                                   |
|--------------------------------------------|---------------|--------------------------------------------|
| `SearchEnemyPlayer`                        | Opponent      | `SearchOpponent`                           |
| `PlayerApi.searchEnemyPlayer`              | Opponent      | `PlayerApi.searchOpponent`                 |
| `PlayerRepository.searchEnemy`             | Opponent      | `PlayerRepository.searchOpponent`          |
| `InMemoryPlayerRepository.searchEnemy`     | Opponent      | `InMemoryPlayerRepository.searchOpponent`  |
| `searchEnemyPlayer` parameters             | Opponent      | `searchOpponent`                           |
| `enemyPlayer` locals                       | Opponent      | `opponent`                                 |
| `enemyPlayerId` test fields                | Opponent      | `opponentId`                               |
| "their enemy", "an enemy player"           | Opponent      | "their opponent", "an opponent"            |
| "the party player"                         | Human player  | "the human player"                         |
| `SearchEnemyPlayerTest`                    | Opponent      | `SearchOpponentTest`                       |

## player

- `SearchOpponent` returns the opponent of the given player. It moves to `usecases/queries/SearchOpponent.kt`.
- `PlayerRepository.searchOpponent(playerId)` returns the other player stored, in `InMemoryPlayerRepository` too.
- `PlayerApi` builds `searchOpponent`.

## Other subdomains

- `battle`: `DefeatPlayer` takes `searchOpponent`, and its comment says "Out of the battles, their opponent is left as
  the only player, so it wins". `BattleApi` passes `playerApi.searchOpponent`.
- `cpuBattleBrain`: `EngageEnemies`, `JoinAndLeaveBattle`, `JoinDisengagedBattle` and `SearchEnemiesWithinReach` take
  `searchOpponent`, and the `enemyPlayer` local of `SearchEnemiesWithinReach` is `opponent`. `CpuBattleBrainApi` passes
  `playerApi.searchOpponent`.
- `party`: the comment of `RecruitMember` says "for the human player".
- `inventory`: the comment of `PlayerTurnRule` says "only during the player turn of the human player".

## Docs

- [Exploration](../architecture/exploration.md): step 3 of recruiting says the battle unit is deployed "for the human
  player".
- [Package structure](../architecture/package-structure.md): `inventory` "checks the turn of the human player".
- `docs/architecture/dependencies.html` is regenerated by the pre-commit hook.

## Tests

The tests are renamed with the code, and their names and comments say the new words:

- `SearchOpponentTest`: `should return the opponent when another player exists` (replaces `should return enemy player
  when another player exists`).
- `InMemoryPlayerRepositoryTest`: `SearchOpponent should return the other player when there are two players stored`.
- `DefeatPlayerTest`: `should defeat the player and leave their opponent as the only player while exploring`.
- `OnRequestDefeatPlayerTest` and `InMemoryQueries` build `SearchOpponent`, and the cpuBattleBrain tests use
  `queries.searchOpponent`: `OnBattleDisengagedTest`, `OnExplorationStartedTest`, `OnPartySteppedTest`,
  `OnRequestEngageEnemiesTest`, `OnRequestJoinAndLeaveBattleTest`, `EngageEnemiesTest`, `JoinAndLeaveBattleTest`,
  `JoinDisengagedBattleTest` and `WhereShouldWalkTest`, whose comment says "The game always has an opponent".
- The `enemyPlayer` locals are `opponent` in `RequestEffectApplicationToCastTargetsTest`, `WhereCanCastTest`,
  `PreviewCastOngoingEffectsTest`, `PreviewCastRemainingHealthTest` and `ProcessTileSelectedTest`, and the
  `enemyPlayerId` fields are `opponentId` in `SummonVanishingAcceptanceTest` and `SummonLootingAcceptanceTest`.
- "the party player" is "the human player" in the test names of `RecruitMemberTest`, `OnRequestRecruitMemberTest`,
  `EquipItemTest`, `DiscardItemTest`, `UnequipItemTest`, `ConsumeItemTest` and `PlayerTurnRuleTest`.

## Not in this plan

- `InMemoryPlayerRepository.searchEnemy` takes the `first` other player, so it throws when there is none, while
  `SearchEnemyPlayer` returns a `Player.Dto?` and its callers expect `null` (`?: return`). Nothing shows it today, as
  the data always has `player-one` and `player-two`. Bug or not, still to be decided.

### Waiting for other sections

Nothing.
