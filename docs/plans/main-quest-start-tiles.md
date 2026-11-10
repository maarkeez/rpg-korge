# Plan: the main quest starts at the village entrance

> **Status:** done, built on `main` in 698efb24 and approved on screenshots.
>
> **Summary:** Today `SetUpGame` publishes `RequestStartMainQuest` as soon as a new game is set up. Before the player
> has walked a tile, the "NEW QUEST" announcement plays for The Goat Wizard's Lair and the tracker names "Defeat the
> Goat Wizard". It overwhelms the player, and it leaves no room for a story. The Elder already tells the Goat Wizard's
> story in the village, but the quest is announced before the party ever meets the Elder. Instead, a new game drops straight
> into exploring with no quest and no tracker. The main quest starts once a party member walks or teleports onto one of
> its start tiles, the entrance of the village at row 14, columns 9 to 11. Its first task is to talk to the Elder, whose
> line tells the story, and only then to defeat the Goat Wizard. The Elder's Errand moves to the Blacksmith, so the
> Elder's talk is only about the Goat Wizard. Until the main quest starts, the tracker is hidden, or is the empty
> tracker once a side quest is active and none is pinned. It builds on the glossary's **Main quest**, **Start tiles**,
> **Pinned quest** and **Empty tracker**, and on [no save migrations](../adr/0011-no-save-migrations.md).

## Decisions

| Topic             | Decision                                                                                                                                                                                                                                                                                                                                                                                                                         |
|-------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope             | The main quest's start, its tasks, the errand's quest giver, and the tracker until the main quest starts. E.g.: the hero walks from (2,1), defeats the gate bat and talks to the Farmer with no quest shown. Stepping onto (14,10) announces The Goat Wizard's Lair, and the Elder gets a talk task marker.                                                                                                                      |
| Trigger           | The main quest starts the first time a party member walks or teleports onto one of its start tiles. A summon is no party member. Chosen over talking to the Elder, defeating the gate bat, or narration at the new game, so the story is told inside the village.                                                                                                                                                                |
| Start tiles       | Only the entrance line (14,9), (14,10) and (14,11), the only walking path into the village from the north. Chosen over every village tile. Placing them so they cut every walking path is a data rule, with no test checking it.                                                                                                                                                                                                 |
| In a battle       | A move in a battle onto a start tile starts the main quest at once, as visit tasks count moves made in a battle, and the announcement plays over the battle.                                                                                                                                                                                                                                                                     |
| Main quest tasks  | "Talk to the Elder", then "Defeat the Goat Wizard". Its name, description and reward of 150 gold stay. Chosen over a single defeat task, so the Elder's talk carries the story.                                                                                                                                                                                                                                                  |
| Errand            | `the-elders-errand` becomes `the-blacksmiths-errand`, "The Blacksmith's Errand", given by the Blacksmith at (21,5) with the same tasks, the talk back being to the Blacksmith. Chosen over leaving it on the Elder, where one talk would finish the main quest's task and start the errand together.                                                                                                                             |
| Tracker           | The tracker is hidden while no quest is active. It is the empty tracker, reading "No quest pinned" with a chevron and no pin, while a side quest is active, none is pinned and the main quest has not started. Otherwise it names the pinned side quest, or else the main quest once started. Chosen over showing the empty tracker from the new game, as progressive disclosure, and as tapping it would open an empty journal. |
| Empty tracker tap | Tapping the empty tracker opens the journal, which lists the active side quests to pin. It is read only in a battle, like the tracker.                                                                                                                                                                                                                                                                                           |
| Pin on start      | A pinned side quest stays pinned when the main quest starts: the announcement is the only sign. Chosen over the main quest taking the tracker, as the player's pin wins.                                                                                                                                                                                                                                                         |
| Teleport          | A teleport into the village does not start the main quest. A talk with the Elder before the start counts for nothing, and the party talks to the Elder again once the main quest starts. Teleport is only learned from the Alchemist's teleport essence, in the village, or the goats' loot, so this is rare.                                                                                                                    |
| Architecture      | `quest.domain.Quest` holds the start tiles and `canBeStartedOn(tiles)`. `quest.usecases.commands.StartMainQuest` starts it from a move, called by `quest.adapters.events.OnBattleUnitMoved`. The tracker states live in `screen.adapters.presentation`. Chosen over folding the start into `Quest.visited`, which would mix two meanings.                                                                                        |
| Saves             | Renaming the errand's id makes `LoadQuestsSnapshot` fail on older local saves, as [no save migrations](../adr/0011-no-save-migrations.md) accepts. The snapshot shape is unchanged, so `Save.SAVE_VERSION` stays 9.                                                                                                                                                                                                              |
| Storybook         | **Screen / Quest tracker › Empty** is added.                                                                                                                                                                                                                                                                                                                                                                                     |
| Tests             | Unit tests for `Quest`, `CreateQuests`, `StartMainQuest`, `OnBattleUnitMoved`, `PinQuest`, `SetUpGame`, `QuestMapper`, `QuestPresenter` and `ResourcesQuestLoader`. `QuestAcceptanceTest` and `QuestEndToEndTest` play a new game up to the entrance.                                                                                                                                                                            |
| Out of scope      | An intro narration, a test that the start tiles cut every path, and remembering a talk with the Elder made before the start.                                                                                                                                                                                                                                                                                                     |

## quest

### Domain

- `Quest.Dto` gains `startTiles: List<PositionDto>`, empty for a side quest.
- `Quest.create(dto)` fails with the new `MainQuestWithoutStartTiles` when a main quest has no start tiles, and with the
  new `SideQuestWithStartTiles` when a side quest has some. `MainQuestWithQuestGiver` says "The main quest starts on its
  start tiles, so it has no quest giver".
- `Quest.canBeStartedOn(tiles)` is true for a main quest that has not started, when any of the tiles is one of its start
  tiles. E.g.: a path through (13,10) and (14,10) starts it, and the path of a teleport to (15,10) does not.
- `QuestEvent.RequestStartMainQuest` is deleted.
- The `Quest` KDoc says the main quest runs once from its start tiles, not from a new game.

### Use cases

- `StartMainQuest(battleUnitId, path)` starts the main quest when the battle unit is a party member and
  `canBeStartedOn(path)` is true. It is a no-op otherwise, as after the start. It takes `SearchParty` and
  `PublishQuestProgress`, like `ProgressVisitTasks`.
- `CreateQuests` fails with `QuestTileNotVacant` when a start tile is not vacant, as it does for the tiles of a visit
  task.
- `PinQuest` keeps publishing `QuestPinned` with the main quest's id when a pin is cleared. Its KDoc says the tracker
  falls back to the main quest only once it has started.

### Adapters

- `OnBattleUnitMoved` calls `startMainQuest` before `progressVisitTasks`, with the same path. The move that starts the
  main quest can not complete its talk task anyway.
- `OnRequestStartMainQuest` is deleted, and `QuestApi` no longer builds it.
- `ResourcesQuestLoader.QuestDefinition` reads `startTiles` as a list of `[row, column]`, defaulting to empty.

### Resources

- `resources/quest/defeat-the-goat-wizard.toml` gains `startTiles = [[14, 9], [14, 10], [14, 11]]`, and a first task
  `type = "TALK"` with `npcId = "elder"` before the defeat task.
- `resources/quest/the-elders-errand.toml` is renamed `the-blacksmiths-errand.toml`, with `questGiverNpcId =
  "blacksmith"` and its talk task to `"blacksmith"`.
- `messages.pot` and `es.po`:
  - `quest.defeat-the-goat-wizard.task.0` becomes "Talk to the Elder" ("Habla con el Anciano"), and `task.1` is "Defeat
    the Goat Wizard".
  - The `quest.the-elders-errand.*` entries become `quest.the-blacksmiths-errand.*`: "The Blacksmith's Errand", "The
    Blacksmith wants to know whether the path to the southern fields is still open.", "Visit the southern fields" and
    "Talk to the Blacksmith".
  - `ui.questTracker.empty` is "No quest pinned" ("Ninguna misión fijada").

## gameSetup

- `SetUpGame` no longer publishes `QuestEvent.RequestStartMainQuest`: a new game ends with
  `PartyEvent.RequestStartExploration`. Its KDoc says a new game starts exploring, and the main quest starts later on its
  start tiles.

## screen

- `QuestMapper.questInfos` marks the main quest as pinned only once it has started, and while no side quest is pinned.
- `QuestTrackerView.displayEmpty()` draws the capsule with `UiMessage.QUEST_TRACKER_EMPTY`, "No quest pinned", with
  the chevron and no pin. It is as wide as the text, like `display(quest)`.
- `QuestPresenter.displayTracker()` displays the tracked quest when there is one. Otherwise it displays the empty
  tracker when a side quest is active, and hides the tracker when none is. It runs on `QuestStarted`, `QuestCompleted`,
  `QuestPinned`, `QuestAbandoned` and `GameLoaded`. E.g.: starting Bats in the Meadow shows the empty tracker,
  abandoning it hides the tracker, and the main quest starting names "Talk to the Elder".
- `QuestPresenter.listedQuests()` lists the main quest only once it has started, so `openQuest()` opens the journal from
  the empty tracker, and does nothing on a new game.
- The `QuestPresenter` and `ExplorationPresenter` KDocs describe the empty tracker and the hidden tracker.

## Storybook

- `QuestTrackerViewStories`: the `Empty` story (`screen-quest-tracker--empty`), drawn with `displayEmpty()`, with no
  quest control.
- `StorySmokeTest` lists the new story.

## Tests

- `QuestTest`
  - `Quest should fail when a main quest has no start tiles`
  - `Quest should fail when a side quest has start tiles`
  - `CanBeStartedOn should be true when a tile is a start tile of a main quest not started`
  - `CanBeStartedOn should be false when no tile is a start tile`
  - `CanBeStartedOn should be false when the main quest is active or completed`
- `CreateQuestsTest`
  - `CreateQuests should fail when a start tile is not vacant`
- `StartMainQuestTest`
  - `StartMainQuest should start the main quest when a party member walks onto a start tile` (replaces `StartMainQuest
    should start the main quest and no side quest`)
  - `StartMainQuest should start the main quest when a party member teleports onto a start tile`
  - `StartMainQuest should not start the main quest when the battle unit is no party member`
  - `StartMainQuest should not start the main quest again when it is active` (replaces `StartMainQuest should fail when
    the quests are not created`)
- `OnBattleUnitMovedTest`
  - `OnBattleUnitMoved should start the main quest when a party member moves onto a start tile`
- `OnRequestStartMainQuestTest` is deleted.
- `SetUpGameTest`
  - `should request each subdomain initialisation after the subdomains it refers to, then exploring, when the game is
    set up` (replaces the one ending in `then the main quest`)
- `ResourcesQuestLoaderTest`
  - `ResourcesQuestLoader should read the start tiles of the main quest`
- `QuestMapperTest`
  - `questInfos should not pin the main quest when it has not started`
- `QuestPresenterTest`
  - `QuestPresenter should keep the tracker hidden when a new game is set up`
  - `QuestPresenter should display the empty tracker when a side quest starts before the main quest`
  - `QuestPresenter should hide the tracker when the last active side quest is abandoned before the main quest starts`
  - `QuestPresenter should keep the pinned side quest on the tracker when the main quest starts`
  - `OpenQuest should open the journal when the empty tracker is tapped`
- `QuestAcceptanceTest`
  - `The main quest should not be started when a new game is set up` (replaces `The main quest should be active when a
    new game is set up`)
  - `The main quest should start when the leader walks onto the village entrance`
- `QuestEndToEndTest`
  - `should play a new game with no announcement and no tracker` (replaces `should announce the main quest with an
    announcement and let the player walk when a new game starts`)
  - `should announce the main quest and name Talk to the Elder when the leader walks onto the village entrance`
  - The tests that need the main quest active start it from the entrance first.

## Known limitations

- If the Goat Wizard is defeated before the main quest starts, the main quest can never be completed. Only the start
  tiles cutting every walking path to the lair guards against it, and no test checks that.
- A talk with the Elder before the main quest starts, after a teleport into the village, counts for nothing.
- Older local saves holding `the-elders-errand` no longer load.
- In a save whose main quest was active on "Defeat the Goat Wizard", the main quest is now on "Talk to the Elder", as
  saves are not migrated.

## Changes while building

- The start tiles are (14,10), (14,11) and (14,12), not (14,9) to (14,11): the plan misread the map by one column, and
  (14,9) is void, so `CreateQuests` failed with `QuestTileNotVacant`.
- `Quest.Dto.startTiles` has no default value, as `DomainArchitectureTest` forbids defaults in an aggregate root's Dto:
  every Dto names it, empty for a side quest.
- `QuestTrackerView.clear()` is added for the hidden tracker, as `hide()` hides it for good once a player wins, and a
  later `display` must draw it again.
- `QuestEndToEndTest` starts the main quest, or moves the knight north of the entrance, by editing the save, as it
  already does for the farmer and the gate bat, rather than walking a whole game to the village.
- `ResourcesQuestLoaderTest` reads the start tiles in its existing main quest test, renamed, rather than in a new one.
- Three `ExplorationPresenterTest` tests that tap the tracker, and three `QuestPresenterTest` tests that open the quest,
  start the main quest first, as no quest is listed before it starts.
