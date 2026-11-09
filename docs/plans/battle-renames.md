# Plan: Rename the code of the battle subdomain to the words of the glossary

> **Status:** done.
>
> **Summary:** The code of the battle subdomain uses words the [glossary](../../GLOSSARY.md) rejects, and some names say
> less or more than they do. E.g.: the Knight defeats the only Rat in a battle and `BattleFinished` is published, but
> defeating the last CPU battle unit on the map publishes `PlayerVictory` instead, although that battle finishes too. And
> `isBattleFinished()` is already true before that battle is over. Reading the code and talking about the game take a
> translation. Instead, the classes, functions, events and docs use the glossary's words, and "finished" has one meaning.
> The game plays the same, and players read the same texts. Builds on [Exploration](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/exploration.md) and
> [CPU battle brain](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/cpu-battle-brain.md).

## Decisions

| Topic        | Decision                                                                                                                                                                                             |
|--------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope        | The renames below, in the code, tests, comments and docs of what the battle subdomain holds, and the listeners other subdomains name after its events.                                               |
| Save format  | Not reached: a save is written while exploring, and none of these names is in a save adapter or a fixture under `src/jvmTest/fixtures/save`, so `SAVE_VERSION` does not change.                      |
| Player text  | None changes, and no translation key changes. Players keep reading "Finish turn" and "{player} wins!", as they read them well: "Finish player turn" would read badly on a button.                    |
| Fields kept  | `engagingBattleUnitIds` and `engagedBattleUnitIds` of `RequestStartBattle` stay: they name the battle units that engaged or were engaged when the battle started, not being in the battle.           |
| Tests        | No new tests: a rename changes no behaviour. The existing tests are renamed with the code.                                                                                                           |
| Out of scope | The bare "turn" of other subdomains, which each section decides, and the two open questions. See [Not in this plan](#not-in-this-plan). Devlog entries stay as written.                              |

## Renames

| Today                             | Glossary term     | New name                           |
|-----------------------------------|-------------------|------------------------------------|
| `BattleRoundStarted`              | Round             | `RoundStarted`                     |
| `BattleRoundFinished`             | Round             | `RoundFinished`                    |
| `OnBattleRoundStarted`            | Round             | `OnRoundStarted`                   |
| `OnBattleRoundFinished`           | Round             | `OnRoundFinished`                  |
| `TurnQueue`, `turnQueue`          | Turn order        | `TurnOrder`, `turnOrder`           |
| `CurrentPlayerTurn`               | Current player    | `CurrentPlayerId`                  |
| `currentPlayerTurn`               | Current player    | `currentPlayerId`                  |
| `BattleFinished`                  | Finished battle   | `BattleFinishedWithoutWinner`      |
| `OnBattleFinished`                | Finished battle   | `OnBattleFinishedWithoutWinner`    |
| `isBattleFinished()`              | Winner            | `hasWinner()`                      |
| `finishBattle()`                  | Finished battle   | `finishWithWinner()`               |
| `FinishBattle`                    | Finished battle   | `FinishBattleWithWinner`           |
| `FinishDisengagedBattle`          | Disengaged        | `ResolveDisengagedBattle`          |
| `RequestFinishDisengagedBattle`   | Disengaged        | `RequestResolveDisengagedBattle`   |
| `OnRequestFinishDisengagedBattle` | Disengaged        | `OnRequestResolveDisengagedBattle` |

## battle

- `Battle`: the names above. The `currentPlayerTurnIndex`, `nextPlayerTurnIndex` and `nextPlayerTurn` locals are
  `currentPlayerIndex`, `nextPlayerIndex` and `nextPlayerId`, and `Battle.Dto.currentPlayerTurn` is `currentPlayerId`.
- `Battle.finishPlayerTurn`: the `turnFinished` local is `playerTurnFinished`.
- `Battle.leaveDuringRound`: the `isEngaged` local is `canDisengage`, as it says whether the battle unit leaving can
  disengage the battle.
- `BattleEvent`: the names above. The comment of `BattleFinishedWithoutWinner` keeps saying why it has no winner.
- `FinishBattleWithWinner`, `ResolveDisengagedBattle`, `StartNextRound`, `BattleApi` and the listeners under `adapters/events`
  follow the names above. `finishWithoutWinner()`, `isFinished`, `winnerPlayerId` and `OnPlayerDefeated` stay.
- Comments: "CPU battle unit engaged" is "CPU battle unit in the battle", e.g.: in `Battle`, `BattleEvent`, `LeaveBattle`,
  `FinishPlayerTurn` and `ResolveDisengagedBattle`. Bare "turn" is "player turn", e.g.: in `PlayerTurnFinished`.

## battlefield

- `OnBattleRoundStarted` and `BattlefieldApi` follow the names above.

## battleUnit

- `OnBattleFinished` and `BattleUnitApi` follow the names above.

## cpuBattleBrain

- `ReengageEnemies` publishes `RequestResolveDisengagedBattle`. Its comment, and the one of `UpdateEngagement`, say "CPU
  battle unit in the battle" and "round".

## cpuWanderBrain

- `OnBattleRoundFinished` and `CpuWanderBrainApi` follow the names above. The comments of `CpuWanderBrain` and
  `WanderAfterRound` say "round".

## inventory

- `PlayerTurnRule` reads `currentPlayerId`.

## party

- `OnBattleFinished` and `PartyApi` follow the names above.

## screen

- `BattleInfoPresenter`, `BattlefieldPresenter`, `FinishTurnPresenter`, `InventoryPresenter`, `ProcessAbilitySelected` and
  `ProcessTileSelected` follow the names above.
- `resources/localization/messages.pot` and `resources/localization/es.po`: the translator comment of `ui.battleInfo.round`
  says "the number of the round". Its `msgctxt`, `msgid` and `msgstr` stay.

## Storybook

- No story uses these names, and no story id changes.

## Docs

- [Exploration](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/exploration.md), [CPU battle brain](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/cpu-battle-brain.md),
  [Package structure](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/package-structure.md) and
  [Presentation timeline](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/presentation-timeline.md): the names above, "round" for "battle round", and "in
  the battle" for "CPU battle unit engaged" and "engaged battle unit".
- [How to add a new test](../testing/how-to-add-a-new-test.md): `currentPlayerId`.
- `docs/architecture/dependencies.html` is regenerated by the pre-commit hook.

## Tests

- The tests that use these names are renamed with the code, with no change in what they assert.
- Test classes: `FinishBattleTest` is `FinishBattleWithWinnerTest`, `FinishDisengagedBattleTest` is
  `ResolveDisengagedBattleTest`, `OnRequestFinishDisengagedBattleTest` is `OnRequestResolveDisengagedBattleTest`,
  `OnBattleFinishedTest` is `OnBattleFinishedWithoutWinnerTest` and `OnBattleRoundFinishedTest` is `OnRoundFinishedTest`.
- Test names and bodies: `BattleTest`, `DefeatPlayerTest`, `FinishPlayerTurnTest`, `StartBattleTest`, `StartNextRoundTest`,
  `OnRequestStartBattleTest`, `OnPlayerDefeatedTest`, `OnBattleDisengagedTest`, `ReengageEnemiesTest`,
  `BattlefieldPresenterTest`, `BattleInfoPresenterTest`, `FinishTurnPresenterTest`, `ProcessAbilitySelectedTest` and the
  acceptance tests, e.g.: `EngagementAcceptanceTest`, `SummonsAtBattleEndAcceptanceTest` and `SimpleBattleAcceptanceTest`.

## Not in this plan

- `Battle.defeatWhileExploring` builds a `Battle` with round 0 and no battle units, only so that a player defeated while
  exploring (e.g.: the last Knight dies on lava) gives a victory. That is no battle, as the glossary defines it. It may
  deserve another home, still to be decided.
- `Battle.defeatPlayer` looks like a bug: when the current player is defeated, it copies from `this.events`, so it drops
  the `PlayerTurnFinished` and `PlayerTurnStarted` that `finishPlayerTurn()` added. `BattleTest` expects only
  `PlayerDefeated`, although its name says "should start the next player turn". Nothing shows it today: with two players,
  the battle finishes straight away.

### Waiting for other sections

- `battleUnit`: `RequestBattleLeave` says the same as `RequestLeaveBattle` in another order. Defeated is still to be defined
  for a battle unit. Bare "turn", while "Turn" is to be avoided: `remainingTurns`, `finishTurn`, `onTurnStarted`,
  `cooldownTurnsLeft` and `RemainingTurnActions`. These are in the save fixtures, so renaming them bumps `SAVE_VERSION`.
- `cpuBattleBrain`: `CombatEngaged`, while "combat" is to be avoided. Engage, Engagement, Re-engage, Within reach and Left
  behind are still to be defined. Bare "turn": `PlayTurn`, `finishTurn`, `startTurn`, `JoinCpuTurn` and `CpuTurnFinished`.
- `cpuWanderBrain`: bare "turn" in `remainingTurns`.
- `effect`: bare "turn" in `OnTurnStarted` and `toTurnStartedDto`.
- `party`: Exploring, the counterpart of a battle, is still to be defined.
- `player`: Player, Enemy, Human and CPU are still to be defined.
- `screen`: `announceBattleEnd`, while the word is "finished". `displayPlayerWin` and `ui.battleInfo.playerWins`, while "win"
  is to be avoided, though players keep reading "wins!". Bare "turn": `FinishTurnView`, `announceTurn` and
  `isHumanTurnDisplayed`.

## Changes while building

- `BattleTest`: `IsBattleFinished should be finished when only one player remains` and its opposite are `HasWinner should be
  true when only one player remains` and `HasWinner should be false when more than one player remains`, as "be finished"
  no longer reads. They assert the same.
- The `newTurnQueue` local of `Battle.defeatPlayer` is also `newTurnOrder`.
- The docs keep bare "turn", e.g.: "during the human turn" in [Exploration](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/exploration.md), as only the
  battle package changes it. `BattleMother` keeps "in turns", which is plain English there.
- The tests' comments on the player turn of the CPU and of the human also say "player turn", in `FinishPlayerTurnTest` and
  `DefeatPlayerTest`.
