# Plan: Rename the code of the quest subdomain to the words of the glossary

> **Status:** built on `main` in `09f6d153`, with its screenshots of the abandon question and the visit tasks still to be
> approved.
>
> **Summary:** The code of the quest subdomain uses words the [glossary](../../GLOSSARY.md) gives to other concepts,
> and one word for two things. E.g.: Bats in the Meadow is a `DEFEAT_UNITS` task with a `count` of 3, while a unit is
> what a battle unit is deployed from and Count is the number a plural form is picked for. Its farmer is its
> `giverNpcId`, while Giver is the party member who gives an item or gold. After two bats, `currentTaskProgress` is 2,
> while the quest's `ProgressDto` is how far the whole quest went, and the player reads "Its progress is lost" for the
> whole quest. The Elder's Errand asks to "Reach the southern fields", while Reach is how far a CPU battle unit walks to
> engage. Comments say the player "follows" the pinned quest, as a follower follows the leader, that a quest "pays" its
> reward, which the inventory grants, and "the party's player" for the human player. `QuestTileCanNotBeOccupied` says
> "can be occupied", which the glossary avoids, and `InitialiseQuests` says "initialise", which buildings and npcs no
> longer do. Instead, the code, the data, the tests and the docs say quest giver, defeat task, talk task, visit task,
> required defeats, defeats counted, pinned, grant and human player, and players read "quest giver" and "Visit the
> southern fields". The game plays the same. Builds on the [party renames](party-renames.md), the
> [player renames](player-renames.md) and [Quests](../architecture/quests.md).

## Decisions

| Topic            | Decision                                                                                                                                                                                                                                                                                                      |
|------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope            | The renames below, in the code, data, tests, comments and docs of the quest subdomain, and where `gameSetup` and `screen` use its words.                                                                                                                                                                      |
| Quest giver      | `giverNpcId` is `questGiverNpcId`, in the TOML too. Chosen over keeping "giver", as Giver is the party member who gives.                                                                                                                                                                                      |
| Task kinds       | `DEFEAT_UNITS`, `TALK_TO_NPC` and `REACH_TILES` are `DEFEAT`, `TALK` and `VISIT`, with their Dtos. Chosen over renaming only `DEFEAT_UNITS`, so each type reads as its glossary term.                                                                                                                         |
| Visit            | The task to get onto a tile is a visit task. Chosen over Reach, which is the CPU battle brain's, and over touch.                                                                                                                                                                                              |
| Required defeats | A defeat task's `count` is `requiredDefeats`. Chosen over count, which is localization's.                                                                                                                                                                                                                     |
| Defeats counted  | `currentTaskProgress` is `currentTaskDefeats`, and `TaskProgressed` is `DefeatCounted`, so Progress only means how far a whole quest went (`ProgressDto`, `PublishQuestProgress`).                                                                                                                            |
| Create           | `InitialiseQuests` is `CreateQuests`, as it creates each quest with `Quest.create`, with no glossary term: only the code uses it. Chosen over Place, as quests are not on a tile.                                                                                                                             |
| Not vacant       | `QuestTileCanNotBeOccupied` is `QuestTileNotVacant`, like `BuildingTileNotVacant` and `NpcTileNotVacant`, as `CreateQuests` asks `IsTileVacant`.                                                                                                                                                              |
| Words kept       | `QuestTypeDto` `MAIN` and `SIDE`, `StatusDto` `NOT_STARTED`, `ACTIVE` and `COMPLETED`, `AbandonQuest`, `PinQuest`, `QuestPinned`, `QuestMarkerDto` with its `QUEST_TO_START` and `TALK_TASK` signs, and `RequestQuestReward`.                                                                                 |
| Save format      | Reached: the saved `currentTaskProgress` of the `quest` snapshot is `currentTaskDefeats`, so `SAVE_VERSION` goes from 8 to 9, with a new `v9.json` fixture. Chosen over keeping the saved key, so the save reads the glossary's words. The TOML renames do not reach the save, as only the progress is saved. |
| Player text      | `ui.quest.abandonQuestion` and `quest.the-elders-errand.task.0` change, in `messages.pot` and `es.po`, with the same keys.                                                                                                                                                                                    |
| Tests            | No new tests: a rename changes no behaviour. The existing tests are renamed with the code, and `SaveFixtureTest` checks the new fixture.                                                                                                                                                                      |
| Out of scope     | The error message of `QuestNotActive`, and the side quests `quests.md` says the game does not ship. See [Not in this plan](#not-in-this-plan).                                                                                                                                                                |

## Renames

| Today                                                              | Glossary term    | New name                                                      |
|--------------------------------------------------------------------|------------------|---------------------------------------------------------------|
| `giverNpcId` (TOML key, `Quest.Dto`, `QuestDefinition`)            | Quest giver      | `questGiverNpcId`                                             |
| `Quest.GiverNpcId`                                                 | Quest giver      | `Quest.QuestGiverNpcId`                                       |
| `MainQuestWithGiver`, `SideQuestWithoutGiver`                      | Quest giver      | `MainQuestWithQuestGiver`, `SideQuestWithoutQuestGiver`       |
| `TaskDto.TypeDto.DEFEAT_UNITS`, `type = "DEFEAT_UNITS"`            | Defeat task      | `DEFEAT`, `type = "DEFEAT"`                                   |
| `TaskDto.TypeDto.TALK_TO_NPC`, `type = "TALK_TO_NPC"`              | Talk task        | `TALK`, `type = "TALK"`                                       |
| `TaskDto.TypeDto.REACH_TILES`, `type = "REACH_TILES"`              | Visit task       | `VISIT`, `type = "VISIT"`                                     |
| `Quest.Task.DefeatUnits`, `TalkToNpc`, `ReachTiles`                | Task kinds       | `Quest.Task.Defeat`, `Talk`, `Visit`                          |
| `DefeatUnitsDto`, `TalkToNpcDto`, `ReachTilesDto`                  | Task kinds       | `DefeatDto`, `TalkDto`, `VisitDto`                            |
| `TaskDto.defeatUnits`, `talkToNpc`, `reachTiles`                   | Task kinds       | `TaskDto.defeat`, `talk`, `visit`                             |
| `count` (TOML key, `DefeatUnitsDto`, `Task.DefeatUnits`)           | Required defeats | `requiredDefeats`                                             |
| `TaskCountBelowOne`                                                | Required defeats | `RequiredDefeatsBelowOne`                                     |
| `currentTaskProgress` (`Progress`, `ProgressDto`, `QuestSnapshot`) | Defeats counted  | `currentTaskDefeats`                                          |
| `TaskProgressed(questId, taskIndex, progress, count)`              | Defeats counted  | `DefeatCounted(questId, taskIndex, defeats, requiredDefeats)` |
| `ProgressReachTasks`, `QuestApi.progressReachTasks`                | Visit task       | `ProgressVisitTasks`, `QuestApi.progressVisitTasks`           |
| `Quest.reached`                                                    | Visit task       | `Quest.visited`                                               |
| `EmptyTaskTiles`                                                   | Visit task       | `EmptyVisitTaskTiles`                                         |
| `InitialiseQuests`, `QuestApi.initialiseQuests`                    | (none)           | `CreateQuests`, `QuestApi.createQuests`                       |
| `RequestInitialiseQuests`, `OnRequestInitialiseQuests`             | (none)           | `RequestCreateQuests`, `OnRequestCreateQuests`                |
| `QuestTileCanNotBeOccupied`                                        | Vacant           | `QuestTileNotVacant`                                          |
| "the player follows", "followed"                                   | Pinned quest     | "pinned", "the tracker shows"                                 |
| "pays", "pay its reward"                                           | Grant            | "grants", "grant its reward"                                  |
| "touch", "touches"                                                 | Visit task       | "walk or teleport onto"                                       |
| "the party's player", "any player but the party's"                 | Human player     | "the human player", "the CPU player"                          |

## quest

### Domain

- `Quest` holds `questGiverNpcId`, and its tasks are `Task.Defeat(unitId, requiredDefeats)`, `Task.Talk(npcId)` and
  `Task.Visit(tiles)`. Its comment says the reward is "granted to the party leader", and `create` "fails when it could
  never run: e.g.: it has no tasks, or grants nothing".
- `Quest.defeated(unitId)` counts `currentTaskDefeats`, publishing `DefeatCounted`. E.g.: the second bat of Bats in the
  Meadow publishes `DefeatCounted(defeats = 2, requiredDefeats = 3)`.
- `Quest.visited(tiles)` completes a current visit task. `pin` says "Makes it the pinned quest, the one the tracker
  shows", and `unpin` "It is no longer the pinned quest, e.g.: as another quest is pinned".
- `DefeatDto` says "Defeat `requiredDefeats` battle units of the unit, of the CPU player", and `VisitDto` "Walk or
  teleport onto any of the tiles with any party member".
- `QuestError`: `MainQuestWithQuestGiver` ("The main quest starts with a new game, so it has no quest giver"),
  `SideQuestWithoutQuestGiver` ("A side quest needs a quest giver"), `RequiredDefeatsBelowOne` ("A defeat task needs at
  least 1 required defeat"), `EmptyVisitTaskTiles` ("A visit task needs at least one tile") and `QuestTileNotVacant`
  ("A tile of a visit task is not vacant").
- `QuestEvent`: `RequestCreateQuests` and `DefeatCounted`. `QuestPinned` says "The quest is the pinned quest: the main
  quest, when no side quest is pinned", and `QuestCompleted` "the reward is requested for whoever leads the party".

### Use cases

- `CreateQuests` creates every loaded quest, failing with `QuestTileNotVacant` when a tile of a visit task is not vacant.
- `ProgressVisitTasks` completes the current task of every quest asking for a tile of the path, when a party member
  walked or teleported onto it.
- `ProgressDefeatTasks` says "unless it is of the human player".
- `PinQuest` says "Makes the quest the pinned quest, unpinning any other, so the tracker shows exactly one. The main
  quest is the pinned quest when no side quest is pinned, so it holds no pin".

### Adapters

- `ResourcesQuestLoader` reads `questGiverNpcId`, `type = "DEFEAT"`, `"TALK"` or `"VISIT"`, and `requiredDefeats`, and
  its errors name `tasks.requiredDefeats`.
- `QuestSnapshottable` saves `currentTaskDefeats`.
- `OnRequestCreateQuests` handles `RequestCreateQuests`, and `OnBattleUnitMoved` calls `progressVisitTasks`.
- `QuestApi` builds `createQuests` and `progressVisitTasks`.

### Resources

- `bats-in-the-meadow.toml`: `questGiverNpcId = "farmer"`, `type = "DEFEAT"` with `requiredDefeats = 3`, then
  `type = "TALK"`.
- `defeat-the-goat-wizard.toml`: `type = "DEFEAT"` with `requiredDefeats = 1`.
- `rats-in-the-fields.toml`: `questGiverNpcId = "elf"`, `type = "DEFEAT"` with `requiredDefeats = 2`.
- `the-elders-errand.toml`: `questGiverNpcId = "elder"`, `type = "VISIT"`, then `type = "TALK"`.

## Other subdomains

- `gameSetup`: `SetUpGame` publishes `QuestEvent.RequestCreateQuests`.
- `save`: `Save.SAVE_VERSION` is 9, and `src/jvmTest/fixtures/save/v9.json` is written with
  `./gradlew jvmTest --tests '*SaveFixtureTest*' -Drpg.updateSaveFixtures=true`, keeping `v1.json` to `v8.json`.
- `screen`: `QuestMapper` reads `TaskDto.TypeDto.DEFEAT`, `TALK` and `VISIT`, `task.defeat?.requiredDefeats` and
  `progress.currentTaskDefeats`. `QuestPresenter` handles `DefeatCounted`. Its own `QuestInfo` `progress` and `count`
  wait for the screen section.

## Player text

| Key                              | English today                                                            | English                                                                        | Spanish                                                                                 |
|----------------------------------|--------------------------------------------------------------------------|--------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------|
| `ui.quest.abandonQuestion`       | Abandon {quest}? Its progress is lost, and its giver can start it again. | Abandon {quest}? Its progress is lost, and its quest giver can start it again. | ¿Abandonar {quest}? Se pierde su progreso, y quien la encargó puede empezarla de nuevo. |
| `quest.the-elders-errand.task.0` | Reach the southern fields                                                | Visit the southern fields                                                      | Visita los campos del sur                                                               |

Both change in `resources/localization/messages.pot` and `resources/localization/es.po`.

## Docs

- [Quests](../architecture/quests.md): the terms table and the text say quest giver, `DEFEAT`, `TALK` and `VISIT`,
  `requiredDefeats`, `currentTaskDefeats`, `DefeatCounted`, `CreateQuests`, "grants" for "pays", "walks or teleports
  onto" for "touched", "the human player" for "the party's", and "Visit the southern fields".
- [Game data](../architecture/game-data.md): the quest TOML keys, types and example.
- [Presentation timeline](../architecture/presentation-timeline.md): `DefeatCounted` for `TaskProgressed`.
- `docs/architecture/dependencies.html` is regenerated by the pre-commit hook.

## Tests

The tests are renamed with the code, and their names and comments say the new words:

- `QuestTest`: `Quest should fail when a defeat task has fewer than one required defeat` (replaces `Quest should fail
  when a task has a count below one`), `Quest should fail when a side quest has no quest giver`, `Quest should fail when
  a main quest has a quest giver`, `Quest should complete only once the defeats counted reach the required defeats`, and
  `Quest should be startable by its quest giver when …` in its four startable tests.
- `ProgressVisitTasksTest` (replaces `ProgressReachTasksTest`): `ProgressVisitTasks should complete the task when a
  follower walks onto any tile of the group`, and the rest renamed to `ProgressVisitTasks`.
- `ProgressDefeatTasksTest`: `ProgressDefeatTasks should not count a defeat when the unit is of the human player`, and
  it expects `DefeatCounted`.
- `CreateQuestsTest` (replaces `InitialiseQuestsTest`): `CreateQuests should create every loaded quest`, and `CreateQuests
  should fail when a tile of a visit task is not vacant` (replaces `InitialiseQuests should fail when a tile can not be
  occupied`).
- `OnRequestCreateQuestsTest` (replaces `OnRequestInitialiseQuestsTest`): `should create the quests when the request is
  dispatched`. `StartMainQuestTest`: `StartMainQuest should fail when the quests are not created`.
- `OnBattleUnitMovedTest`: `should progress the visit tasks with the tiles walked when a battle unit moves`.
- `ResourcesQuestLoaderTest`: `should fail with the file when a defeat task has no required defeats`.
- `QuestResourcesTest`: `should find the npc of every talk task and quest giver, and none recruitable, in the npc
  resources`.
- `ResolveNpcTalkTest`: `ResolveNpcTalk should start every eligible side quest of the quest giver`. `AbandonQuestTest`:
  `AbandonQuest should allow its quest giver to start it again when it is not repeatable`.
- `QuestAcceptanceTest`: `A side quest should run through its tasks and grant its reward when it is started by talking
  to its quest giver`, and `A reward item should lie on the tile the leader walked onto when visiting it completes the
  quest`.
- `QuestMother`, `QuestMapperTest`, `QuestPresenterTest`, `RestoreQuestsTest`, `SearchQuestsSnapshotTest`,
  `SearchQuestMarkersTest`, `OnBattleUnitDefeatedTest`, `OnNpcTalkFinishedTest` and `QuestEndToEndTest` use the new
  names.
- `SaveFixtureTest` checks `v9.json`.

## Not in this plan

- `QuestNotActive` says "Only an active quest can be abandoned", but `Quest.pin` throws it too, for a quest that is not
  active. Its message, or a second error, is still to be decided.
- [Quests](../architecture/quests.md) says the game "ships none yet" of the side quests, while it ships three (see its
  The side quests shipped). Still to be fixed.

### Waiting for other sections

- `screen`: the tracker, the journal, the quest detail, the announcement and the repeatable pill. Whether opening the
  journal is reading a quest, as `OpenQuest`, `QuestOpened` and `READING_QUEST` live in `party`, whose glossary already
  says the party "reads a quest". The `progress` and `count` of `QuestInfo.Task`, which may follow Defeats counted and
  Required defeats.
  `QuestInfo.Kind.REACH`, which `QuestMapper` maps a visit task to, and the `questInfo should count the defeats of a
  defeat task and 1 for a talk or a reach` test of `QuestMapperTest`.
- `storybook`: `QuestSample.REACH` and the `reach` stories of the quest tracker and the quest detail
  (`screen-quest-tracker--reach`, `screen-quest-detail--reach`).

## Changes while building

- The storybook's sample quests read like the game: "Visit the southern fields", "Visit the old bridge" and "Visit the
  lair down south", for "Reach", and the description of the `REACH` sample says "A visit task: it has no portrait". The
  names of the samples and stories wait for the `storybook` section.
- `ResourcesQuestLoaderTest` loads its malformed visit task as `visit-a-tile`, for `reach-a-tile`.
- [Game data](../architecture/game-data.md) also lists `RequestCreateQuests` in the order of the game setup, and
  `CreateQuests` beside `PlaceBuildings` and `PlaceNpcs` as creating its data synchronously.
- The tables of [Quests](../architecture/quests.md) are aligned again, as the new names changed their widths.
