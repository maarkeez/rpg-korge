# Plan: Rename the code of the battlesetup subdomain to the words of the glossary

> **Status:** done.
>
> **Summary:** The code says a battle is set up, while the [glossary](../../GLOSSARY.md) says a game is. E.g.: picking "Play
> new game" runs `SetupBattle`, and the Knight then explores with no battle at all, until the CPU engages. And `startGame`
> only displays the main menu, where no game has started yet. Reading the code and talking about the game take a
> translation. Instead, the package, its use case and its docs say game setup, and `startApp` says what booting does. The game
> plays the same, and players read the same texts. Builds on [Game data](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/game-data.md).

## Decisions

| Topic        | Decision                                                                                                                                                                 |
|--------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope        | The renames below, in the code, tests, comments and docs. The glossary's `### Game setup` heading already matches the new package name.                                  |
| Verb         | `SetUpGame`, as commands are verbs (`StartBattle`) and the verb is "set up". The package and the Api keep the noun. Chosen over `SetupGame`, as that names a noun.       |
| Package name | `gameSetup` is camelCase, like `battleUnit` and `lootTable`, and unlike today's lowercase `battlesetup`.                                                                 |
| Save format  | Not reached: none of these names is in a save adapter or a fixture under `src/jvmTest/fixtures/save`, so `SAVE_VERSION` does not change.                                 |
| Player text  | None changes, and no translation key or catalog changes. Players keep reading "Play new game" and "Load game".                                                           |
| Words kept   | "Game data", "initialise" and "resources" stay as they are, as the glossary leaves them out. "Start game" in `BattleScene` stays: a new or a saved game does start there. |
| Tests        | No new tests: a rename changes no behaviour. The existing tests are renamed with the code.                                                                               |
| Out of scope | `BattleScene` and `isLoadingASavedGame`, which wait for their sections. See [Not in this plan](#not-in-this-plan). Devlog entries stay as written.                       |

## Renames

| Today                       | Glossary term | New name                  |
|-----------------------------|---------------|---------------------------|
| package `battlesetup`       | Game setup    | `gameSetup`               |
| `BattleSetupApi`            | Game setup    | `GameSetupApi`            |
| `battleSetupApi`            | Game setup    | `gameSetupApi`            |
| `SetupBattle`               | Game setup    | `SetUpGame`               |
| `setupBattle`               | Game setup    | `setUpGame`               |
| `SetupBattleTest`           | Game setup    | `SetUpGameTest`           |
| `SetupBattleAcceptanceTest` | Game setup    | `SetUpGameAcceptanceTest` |
| `Stage.startGame`           | Game          | `Stage.startApp`          |

## gameSetup

- Moves `src/commonMain/kotlin/com/mkz/rpg/battlesetup` and `src/commonTest/kotlin/com/mkz/rpg/battlesetup` to `gameSetup`.
- `SetUpGameTest`: `should request each subdomain initialisation after the subdomains it refers to, then exploring, then the
  main quest, when the battle is set up` says `when the game is set up`.

## Main

- `startApp` boots the app up to the main menu. `main` and the end-to-end tests call it (`EndToEndExtension`,
  `LanguageEndToEndTest` and `SaveAndLoadEndToEndTest`).
- The comments mean the app: "The same bundle starts the app, or the Storybook when it is asked for" and "so both boot the
  app the same way". "a new game" and "where the games are saved" stay, as they are games.

## Other callers and comments

- `BattleScene`: `gameSetupApi.setUpGame()`, and "each subdomain loads its data before the game is set up".
- The acceptance tests that call it use `gameSetupApi.setUpGame()`: `ConsumeHealthPotionAcceptanceTest`,
  `ConsumeManaPotionAcceptanceTest`, `DropLootAcceptanceTest`, `EquipHammerAcceptanceTest`, `LearnTeleportAcceptanceTest`,
  `LootDroppedItemsAcceptanceTest`, `QuestAcceptanceTest`, `SimpleBattleAcceptanceTest` and `TerrainTransitionsAcceptanceTest`.
- "when the battle is set up" says "when the game is set up":
    - The test names of `SetUpGameAcceptanceTest`, e.g.: `should store the hammer when the game is set up`, and of
      `TerrainTransitionsAcceptanceTest`.
    - The comments of `OnExplorationStarted`, `SubdomainApis` and `BuildingAcceptanceTest`.
- `UseCaseInstantiationArchitectureTest`: the comment naming `battlesetup` goes, and `file.name.equals(apiName, ignoreCase =
  true)` is `file.name == apiName`, as no package differs from its Api by case any more.

## Docs

- [Game data](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/game-data.md): "The game is defined by TOML files", the heading "How the game is set up", "before
  the game is set up", `SetUpGame` and its link.
- [Save and load](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/save-and-load.md): the link is `game-data.md#how-the-game-is-set-up`.
- [Quests](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/quests.md): `SetUpGame` and `gameSetup`.
- [Inventory](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/inventory.md): "when the game is set up".
- [Localization](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/localization.md), [How to add a new test](../testing/how-to-add-a-new-test.md) and
  [How to take a game snapshot](../testing/how-to-take-a-game-snapshot.md): `startApp`. "They MUST boot the app using
  `com.mkz.rpg.startApp()`".
- [Dependencies](../architecture/dependencies.html) is regenerated by the pre-commit hook.

## Not in this plan

### Waiting for other sections

- `save`: Saved game and Load are still to be defined, including `isLoadingASavedGame` of `SetUpGame`.
- `screen`: `BattleScene` holds the whole game, not a battle. New game is still to be defined, with `onNewGameSelected` and
  `ui.menu.playNewGame`.

## Changes while building

- More comments than the plan named say "game setup" for "battle setup", as the glossary avoids it: `EndToEndExtension`,
  `ExploreEndToEndTest`, `FinishTurnEndToEndTest`, `FormationEndToEndTest` and `SaveAndLoadEndToEndTest`.
