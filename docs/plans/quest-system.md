# Plan: Quests of linear tasks, paying the party leader a reward

> **Status:** to do.
>
> **Summary:** Exploring has no goal today. The hero walks from row 2 of the 40 rows of the battlefield, and defeating
> the goat wizard at row 36 only drops its bee essence: nothing in the game asks for it, and nothing keeps count of it.
> Instead, a new `quest` subdomain holds one main quest and any number of side quests. Each quest is a line of tasks,
> completed one after the other, and completing the last one pays its reward to the party leader. The main quest starts
> with a new game, and a side quest starts once the party finishes talking to its quest giver. This plan builds the
> backend only: the game ships one main quest, defeating the goat wizard for 150 gold, and no view shows it yet. It
> builds on [Inventory](../architecture/inventory.md), [Exploration](../architecture/exploration.md),
> [Game data](../architecture/game-data.md) and [Save and load](../architecture/save-and-load.md).

## Domain terms

| Term        | Meaning                                            | In the code                                                                                            |
|-------------|----------------------------------------------------|--------------------------------------------------------------------------------------------------------|
| Quest       | A line of tasks ending in a reward                 | `Quest`, `resources/quest/<quest id>.toml`                                                             |
| Main quest  | The one principal quest of the game                | `QuestTypeDto.MAIN`, `type = "MAIN"`                                                                   |
| Side quest  | Any of the other quests                            | `QuestTypeDto.SIDE`, `type = "SIDE"`                                                                   |
| Task        | One stage of a quest                               | `DEFEAT_UNITS` (`unitId`, `count`), `TALK_TO_NPC` (`npcId`), `REACH_TILES` (`tiles`)                   |
| Reward      | The gold and lootable items a completed quest pays | `[reward]`, `gold`, `lootableItemIds`                                                                  |
| Leader      | Who receives the reward, already a term of `Party` | `leaderBattleUnitId`                                                                                   |
| Quest giver | The npc who starts a side quest                    | `giverNpcId`                                                                                           |
| Repeatable  | A side quest that can run again once completed     | `repeatable`, `false` by default                                                                       |
| Status      | Where a quest is in its life                       | `NOT_STARTED`, `ACTIVE`, `COMPLETED`                                                                   |
| Events      | What the quest system publishes                    | `QuestStarted`, `TaskProgressed`, `TaskCompleted`, `QuestCompleted`, `QuestAbandoned`, `RewardGranted` |

## Decisions

| Topic               | Decision                                                                                                                                                                                                                                                                                                                                                         |
|---------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope               | The backend of quests: the `quest` subdomain, its game data, its save, and the reward through `inventory` and `lootDrop`. E.g.: a test can start a side quest, complete it and read the hero's gold, but the player sees no quest on screen.                                                                                                                     |
| Main quest          | Exactly one, starting with a new game. It runs once, and can not be abandoned, repeated nor failed. Completing it only pays its reward and publishes its events: no ending, and exploring goes on.                                                                                                                                                               |
| Side quests         | Any number, each started when the party finishes talking to its quest giver, with no choice to accept or decline, as that needs a view. Every eligible side quest of that giver starts on the same talk. They can not fail.                                                                                                                                      |
| Repeating           | A side quest with `repeatable = true` starts again on a later talk to its giver, from its first task, and pays its full reward each time. The talk completing it does not restart it. Chosen over restarting at once, as nothing would pace it.                                                                                                                  |
| Abandoning          | A side quest can be abandoned, through a command with no view yet. It goes back to `NOT_STARTED` with its progress reset, so its giver can start it again even when it is not repeatable. Chosen over keeping the progress, or over losing the quest for good.                                                                                                   |
| Tasks               | Defeating units, talking to an npc and reaching a group of tiles. Each maps to an event the game already publishes (`BattleUnitDefeated`, `NpcTalkFinished`, `BattleUnitMoved`). Collecting items is left out, as they can be given, sold, consumed or discarded once picked up.                                                                                 |
| Task progress       | A task only counts events from when it becomes the current one, so a rat defeated earlier does not count. Chosen over counting from the start of the game, which needs totals of every possible task. One event progresses every active quest, at most one task each.                                                                                            |
| Defeating           | Any `BattleUnitDefeated` of the task's unit, from a player other than the party's, counts: whatever defeated it, including respawned and CPU-summoned battle units. `BattleUnitVanished` does not. The event names no killer, and tasks can be farmed by waiting for a respawn.                                                                                  |
| Reaching            | `REACH_TILES` completes when the move of any party member touches any tile of its group, while exploring or battling. `BattleUnitMoved.path` holds every tile walked, so nobody crosses a line of tiles without touching it, and a teleport only counts its destination.                                                                                         |
| Reward              | Some gold and a list of lootable items, as the game has no experience points. It goes to whoever leads when the last task completes: the next member when the leader was already defeated, and nobody once the whole party is gone. Chosen over the leader at the quest's start, which needs a fallback anyway.                                                  |
| Reward over the cap | Gold stops at 99,999 and the excess is lost, as a reward can not be refused the way selling is. Chosen over passing it to the next member, as it is very unlikely.                                                                                                                                                                                               |
| Full bag            | The items that do not fit drop on the leader's tile as loot, so none is lost and no new view is needed: walking back onto the tile stores them, or announces the existing `InventoryFull`. Chosen over the next member with space, a pending claim, or losing them.                                                                                              |
| Dropped rewards     | `lootDrop` keeps them in one ownerless `LootDrop`, with no unit nor loot tables, created with the others. Chosen over an empty `LootDrop` for the leader's unit (e.g.: the hero has no `lootDrop/hero.toml`), which needs the unit carried through `inventory`, and over taking the dropped items out of `LootDrop`, a much larger change.                       |
| Aggregate           | One `Quest` holds its definition and its progress. Chosen over a `Quest` and a `QuestProgress`, as the split of `Unit` and `BattleUnit` exists because a unit has many battle units, and a quest only has one running copy.                                                                                                                                      |
| Validation          | The game fails to start without exactly one main quest, on a quest with no tasks, an empty reward, negative gold, an empty tile group, a tile that can not be occupied or a `count` below 1, on a side quest with no giver, and on a main quest with a giver or `repeatable`.                                                                                    |
| References          | The unit, npc and lootable item ids are not checked when the game starts, so `quest` does not depend on their subdomains: `QuestResourcesTest` checks them in CI, as `NpcUnitResourcesTest` does. It also fails on a recruitable npc as a giver or a talk task, as recruiting deletes the npc, so the task could never complete and the quest could not restart. |
| Save                | `quest` is saved: each quest's status, current task and its progress. The rest is read from the resources again. `SAVE_VERSION` goes to 3, so every earlier save is incompatible, as [Save and load](../architecture/save-and-load.md#save_version) requires.                                                                                                    |
| Content             | One main quest, `resources/quest/defeat-the-goat-wizard.toml`: defeat 1 `goat-wizard`, for 150 gold. No side quest ships, so only the tests play them.                                                                                                                                                                                                           |
| Architecture        | A new `quest` subdomain that nothing depends on but `battlesetup` and `screen`. It listens to the facts of `battleUnit` and `party`, reads through `party` and `battlefield`, and publishes `InventoryEvent.RequestGrantReward`, which `inventory` declares and knows nothing of quests for.                                                                     |
| Texts               | None yet: the names and descriptions of quests are added with the view that shows them. `searchQuests` on `QuestApi` is there for it.                                                                                                                                                                                                                            |
| Tests               | Unit tests of the aggregate and use cases, event listener tests, the resources tests, an acceptance test of a whole quest, and the golden save fixture `v3.json`. No end-to-end test, as there is nothing to see.                                                                                                                                                |
| Out of scope        | Any view, story or text, collecting items, tasks in parallel, failing a quest, choosing whether to accept a quest, quest chains and an ending for the main quest.                                                                                                                                                                                                |
| Delivery            | 4 milestones, each green on CI. See [Milestones](#milestones).                                                                                                                                                                                                                                                                                                   |

For example, a new game: the hero, carrying its starting 30 gold, leads the party.

| When                                     | Published                                                                                            | Result                                             |
|------------------------------------------|------------------------------------------------------------------------------------------------------|----------------------------------------------------|
| The new game is set up                   | `QuestStarted("defeat-the-goat-wizard")`                                                             | `ACTIVE`, first task, progress 0                   |
| A rat is defeated                        | Nothing                                                                                              | It is not the task's unit                          |
| The goat wizard is defeated              | `TaskProgressed` (1 of 1), `TaskCompleted`, `QuestCompleted`, then `RequestGrantReward` for the hero | `COMPLETED`                                        |
| `inventory` handles the request          | `RewardGranted(hero, gold = 150)`                                                                    | The hero carries 180 gold                          |
| The goat wizard respawns and is defeated | Nothing                                                                                              | The quest is completed, and a main quest runs once |

And a side quest with `lootableItemIds = ["hammer", "health-potion"]`, completed by a leader with one space left in its
bag: the hammer is stored, the health potion drops on the leader's tile (`RequestDropItem`, `LootDropped`), and walking
back onto that tile stores it, or announces `InventoryFull` if the bag is still full.

## Milestones

Each milestone is a few commits straight on `main`, green on CI.

| #  | Milestone        | Done when                                                                                                                              | Status | Done in |
|----|------------------|----------------------------------------------------------------------------------------------------------------------------------------|--------|---------|
| M1 | The reward       | `inventory` grants a requested reward, capping the gold and dropping what does not fit, and the dropped items can be looted and saved. | ⬜      |         |
| M2 | Quests and saves | A new game starts the main quest, and saving and loading keep its status. `SAVE_VERSION` is 3.                                         | ⬜      |         |
| M3 | Task progress    | Defeating the goat wizard completes the main quest, and the leader carries 150 more gold. The three kinds of task work.                | ⬜      |         |
| M4 | Side quests      | Talking to a giver starts its side quests, which can be repeated and abandoned, played by the tests only.                              | ⬜      |         |

### M1 commits

1. Keep the dropped items no unit dropped: `LootDrop.ownerless()`, created by `InitialiseLootDrops`, `RequestDropItem`, `DropItem` and `OnRequestDropItem`, and the ownerless loot drop left out of `SearchLootDropsSnapshot` while it holds nothing, so a save's shape does not change.
2. Grant a reward: `Inventory.grantReward`, `GrantReward`, `OnRequestGrantReward`, `RequestGrantReward` and `RewardGranted`, and describe it in Inventory.

### M2 commits

1. Add the `quest` subdomain: `Quest` with its tasks, reward, status and validation, `QuestRepository`, `QuestLoader`, `ResourcesQuestLoader`, `InitialiseQuests`, `SearchQuests` and `QuestApi`, and `quest` in the restricted subdomains.
2. Start the main quest with a new game: `RequestInitialiseQuests` and `RequestStartMainQuest` from `SetupBattle`, `StartMainQuest`, `BattleScene`'s wiring, and `resources/quest/defeat-the-goat-wizard.toml`, with `QuestResourcesTest`.
3. Save the quests: `QuestSnapshottable`, `SearchQuestsSnapshot`, `RestoreQuests`, `SAVE_VERSION` 3 and the `v3.json` fixture, and describe it in Save and load.

### M3 commits

1. Progress the tasks: `Quest.defeated`, `Quest.reached` and `Quest.talkedTo`, `PublishQuestProgress`, `ProgressDefeatTasks`, `ProgressReachTasks`, and their listeners, with the reward requested as the last task completes.
2. Talk tasks: `ResolveNpcTalk` and `OnNpcTalkFinished`, with `QuestAcceptanceTest`, and describe quests in the new Quests doc and Game data.

### M4 commits

1. Start the side quests from their giver in `ResolveNpcTalk`, and repeat them once completed.
2. Abandon a side quest: `AbandonQuest`, `QuestAbandoned`.

## Reference

### lootDrop

- `LootDrop.ownerless()` builds the loot drop of the items no unit dropped, with `OWNERLESS_UNIT_ID` as its `unitId` and no loot tables. `create` keeps failing with `EmptyLootDropLootTables` on any other loot drop, a check moved from `LootTableIds` to `create`. It records no `LootDropCreated`, and nothing rolls it, as no battle unit has that unit.
- `InitialiseLootDrops` creates it along with the loaded ones, in the same step (7), so `RestoreLootDrops` finds it when a save names it.
- `LootDropEvent.RequestDropItem(lootableItemId, row, column)`: requested by `inventory`. `OnRequestDropItem` calls the new `DropItem`, which places the item in the ownerless loot drop (`LootDrop.place`, `LootDropped`).
- `FindLoot`, `PickUpDroppedItem` and `SearchDroppedItems` read `searchAll`, so a dropped reward is found, picked up and drawn as any other dropped item, with no change.
- `SearchLootDropsSnapshot` leaves out the ownerless loot drop while it holds nothing, so M1 changes no save and needs no `SAVE_VERSION` of its own.

### inventory

- `InventoryEvent.RequestGrantReward(battleUnitId, gold, lootableItemIds, row, column)`: `row` and `column` are the tile of the battle unit, where what does not fit drops, as `inventory` does not know it.
- `Inventory.grantReward(gold, lootableItemIds, row, column)` adds the gold, up to `MAXIMUM_GOLD`, stores each item in order while `canStore()`, and records `RewardGranted(battleUnitId, gold, lootableItemIds)` with what it really received, then one `RequestDropItem` per item left. E.g.: a leader with 99,950 gold granted 150 receives 49, and `RewardGranted` says 49.
- `GrantReward` fails with `InventoryLootableItemDoesNotExist` on an item the game does not have, and does nothing for a battle unit with no inventory. As `StoreLootedItem`, it is not limited to the player's turn, since a task can complete in a battle.
- `OnRequestGrantReward` calls it, and `InventoryApi` builds both.

### quest

#### Domain

- `Quest.Dto(id, type, giverNpcId, repeatable, tasks, reward, progress)`, with `QuestTypeDto` (`MAIN`, `SIDE`), `TaskDto` (`DefeatUnitsDto(unitId, count)`, `TalkToNpcDto(npcId)`, `ReachTilesDto(tiles)`), `RewardDto(gold, lootableItemIds)` and `ProgressDto(questId, status, currentTaskIndex, currentTaskProgress)`, with `StatusDto` (`NOT_STARTED`, `ACTIVE`, `COMPLETED`).
- `Quest.create(dto)` fails with `QuestWithoutTasks`, `EmptyQuestReward`, `NegativeQuestRewardGold`, `EmptyTaskTiles`, `TaskCountBelowOne`, `SideQuestWithoutGiver`, `MainQuestWithGiver` or `MainQuestRepeatable`. It starts `NOT_STARTED`, on its first task, with progress 0.
- `start()` makes it `ACTIVE`, on its first task with progress 0, recording `QuestStarted(questId)`.
- `canBeStartedBy(npcId)`: it is a side quest of that giver, `NOT_STARTED`, or `COMPLETED` and repeatable.
- `defeated(unitId)`, `reached(tiles)` and `talkedTo(npcId)` do nothing unless it is `ACTIVE` and the current task asks for it. A defeat records `TaskProgressed(questId, taskIndex, progress, count)`, each time. Once a task is done it records `TaskCompleted(questId, taskIndex)` and moves on to the next one, or, after the last one, becomes `COMPLETED` and records `QuestCompleted(questId)`.
- `abandon()` goes back to `NOT_STARTED` with its progress reset, recording `QuestAbandoned(questId)`. It fails with `MainQuestCanNotBeAbandoned`, or `QuestNotActive` when it is not `ACTIVE`.
- `restoreProgress(progress)` puts back what a save took, publishing no events, and fails with `require` on a task the quest does not have.
- `QuestEvent` also declares `RequestInitialiseQuests` and `RequestStartMainQuest`. `QuestRepository`: `save`, `searchById`, `searchAll`.

#### Use cases

- `InitialiseQuests` creates every loaded quest, unless it already exists, failing with `MainQuestNotUnique` unless exactly one is `MAIN`, and with `QuestTileCanNotBeOccupied` through `CanBattlefieldTileBeOccupied`.
- `StartMainQuest` starts the main quest.
- `PublishQuestProgress` (a service) saves the quests an event changed and publishes their events. After each `QuestCompleted`, it reads `SearchParty` and `SearchPosition`, and publishes `RequestGrantReward` for the leader on its tile. With no leader, nobody receives it.
- `ProgressDefeatTasks(playerId, unitId)` calls `defeated` on every quest, unless `playerId` is the party's. Without a party, nothing counts.
- `ProgressReachTasks(battleUnitId, path)` calls `reached` on every quest, when the battle unit is a party member. A summon is none.
- `ResolveNpcTalk(npcId)` first lists the quests `canBeStartedBy` that npc, then calls `talkedTo` on every quest, and finally starts the listed ones. So the talk that starts a quest does not complete its first task, and the talk that completes a quest does not restart it.
- `AbandonQuest(questId)`, `SearchQuests` and `SearchQuestsSnapshot`, and `RestoreQuests`, which fails with `require` on a quest the game no longer has.

#### Adapters

- `ResourcesQuestLoader`: `QuestLoader`, one quest per `quest/<quest id>.toml`, with `QuestDefinition`, built on `readTomlFiles`, `fromResource`, `enumOf` and `required`.
- `OnRequestInitialiseQuests`, `OnRequestStartMainQuest`, `OnBattleUnitDefeated`, `OnBattleUnitMoved` and `OnNpcTalkFinished` only call their commands.
- `QuestSnapshottable`, with `key = "quest"`, writes `questId`, `status`, `currentTaskIndex` and `currentTaskProgress`.
- `QuestApi(battlefieldApi, partyApi, eventBus, questLoader)`, as every Api takes its collaborators from its caller, with `searchQuests`, `abandonQuest` and `snapshottable`.

#### Resources

```toml
# resources/quest/defeat-the-goat-wizard.toml
type = "MAIN"

[[tasks]]
type = "DEFEAT_UNITS"
unitId = "goat-wizard"
count = 1

[reward]
gold = 150
```

A side quest, with the other two kinds of task. `tiles` is a list of `[row, column]`, as `battlefield.toml` nests its
lists, for a line of tiles across a corridor. `battlefield.toml` only nests them at the top level of its file, so
`ResourcesQuestLoaderTest` first proves ktoml 0.7.1 reads them inside `[[tasks]]`, and `tiles` becomes a list of
`"row,column"` strings if it does not:

```toml
type = "SIDE"
giverNpcId = "farmer"
repeatable = true

[[tasks]]
type = "REACH_TILES"
tiles = [[17, 11], [18, 11]]

[[tasks]]
type = "TALK_TO_NPC"
npcId = "farmer"

[reward]
gold = 20
lootableItemIds = ["health-potion"]
```

### battlesetup and screen

- `SetupBattle` publishes `RequestInitialiseQuests` after `RequestInitialiseBuildings`, as step 11, in a new game and in a loaded one, since a save only restores the progress. It publishes `RequestStartMainQuest` last, after `RequestStartExploration`, only in a new game.
- `BattleScene` builds `QuestApi` with `ResourcesQuestLoader()`, calls `questApi.init()`, and hands `questApi.snapshottable` to `SaveApi`, last.
- `SubdomainApis` gains a `quests` parameter and its `QuestApi` with a `FakeQuestLoader`, and only initialises them when there are any, so the tests with no quests do not fail with `MainQuestNotUnique`.

### Dependencies

`SubdomainDependencyArchitectureTest` restricts `quest` to `shared`, `battlefield`, `battleUnit`, `party` and `inventory`. `SnapshotCoverageArchitectureTest` passes through `QuestSnapshottable`. The new dependencies, with the requests the rules ask for:

| When                               | `quest` publishes                                                      | `inventory`                                                      |
|------------------------------------|------------------------------------------------------------------------|------------------------------------------------------------------|
| The last task of a quest completes | `RequestGrantReward(battleUnitId, gold, lootableItemIds, row, column)` | Adds the gold, stores the items, and requests dropping the rest. |

| When                       | `Inventory` records                            | `lootDrop`                                        |
|----------------------------|------------------------------------------------|---------------------------------------------------|
| A reward item does not fit | `RequestDropItem(lootableItemId, row, column)` | Keeps it on the tile, in the ownerless loot drop. |

### Docs

- A new [Quests](../architecture/quests.md) doc: the terms, starting, the tasks, the reward, repeating and abandoning.
- [Game data](../architecture/game-data.md): `quest/<quest id>.toml`, the setup table with its two new steps, and How to add a quest.
- [Inventory](../architecture/inventory.md): `GrantReward`, the cap and the drop.
- [Save and load](../architecture/save-and-load.md): row 8 of the snapshots, `quest`, and loading stopping after step 11.
- [Package structure](../architecture/package-structure.md): `quest` in the restricted subdomains, with the two tables above.

### Tests

- `LootDropTest`
  - `LootDrop should hold no loot tables when it is ownerless`
  - `LootDrop should fail when it is created with no loot tables and is not ownerless` (kept, through `create`)
- `DropItemTest`
  - `DropItem should place the item on the tile and announce it when requested`
- `InitialiseLootDropsTest`
  - `InitialiseLootDrops should create the ownerless loot drop when the loot drops are initialised`
- `SearchLootDropsSnapshotTest`
  - `SearchLootDropsSnapshot should leave out the ownerless loot drop when it holds nothing`
  - `SearchLootDropsSnapshot should hold the ownerless loot drop when an item lies in it`
- `InventoryTest`
  - `Inventory should add the gold and store the items when a reward is granted`
  - `Inventory should stop at the maximum gold when a reward takes it above`
  - `Inventory should request dropping the items left when the bag fills up`
- `GrantRewardTest`
  - `GrantReward should fail when a lootable item does not exist`
  - `GrantReward should do nothing when the battle unit has no inventory`
- `QuestTest`
  - `Quest should fail when it has no tasks`, and one per other validation error
  - `Quest should move on to the next task when the current one is done`
  - `Quest should not count an event when its task is not the current one`
  - `Quest should complete only once the defeats reach the count`
  - `Quest should complete when the last task is done`
  - `Quest should be startable by its giver when it is completed and repeatable`
  - `Quest should not be startable by its giver when it is completed and not repeatable`
  - `Quest should reset its progress when it is abandoned`
  - `Quest should fail when a main quest is abandoned`
  - `Quest should fail when it is abandoned and not active`
- `InitialiseQuestsTest`
  - `InitialiseQuests should fail when there is no main quest`
  - `InitialiseQuests should fail when there are two main quests`
  - `InitialiseQuests should fail when a tile can not be occupied`
- `StartMainQuestTest`
  - `StartMainQuest should start the main quest and no side quest`
- `PublishQuestProgressTest`
  - `PublishQuestProgress should request the reward for the leader on its tile when a quest completes`
  - `PublishQuestProgress should request the reward for the next member when the leader was defeated`
  - `PublishQuestProgress should request no reward when the whole party is gone`
- `ProgressDefeatTasksTest`
  - `ProgressDefeatTasks should count a defeat when the unit is of another player`
  - `ProgressDefeatTasks should not count a defeat when the unit is of the party's player`
  - `ProgressDefeatTasks should progress every active quest when they ask for the same unit`
- `ProgressReachTasksTest`
  - `ProgressReachTasks should complete the task when a follower touches any tile of the group`
  - `ProgressReachTasks should complete the task when a walk crosses a tile on its way`
  - `ProgressReachTasks should not count a battle unit out of the party`
  - `ProgressReachTasks should complete at most one task per event when the next task asks for the same tile`
- `ResolveNpcTalkTest`
  - `ResolveNpcTalk should start every eligible side quest of the giver`
  - `ResolveNpcTalk should not complete the first task of a quest when the same talk starts it`
  - `ResolveNpcTalk should not restart a repeatable quest when the same talk completes it`
  - `ResolveNpcTalk should restart a repeatable quest when a later talk finishes`
- `AbandonQuestTest`
  - `AbandonQuest should allow its giver to start it again when it is not repeatable`
- `RestoreQuestsTest`
  - `RestoreQuests should put back the progress of each quest, publishing no events`
  - `RestoreQuests should fail when the quest no longer exists`
- `QuestSnapshottableTest`, `ResourcesQuestLoaderTest`, `SearchQuestsTest`, `InMemoryQuestRepositoryTest`, and one test per listener.
- `QuestResourcesTest`, as `NpcUnitResourcesTest`
  - `should find the unit of every defeat task in the unit resources`
  - `should find the npc of every talk task and giver, and none recruitable, in the npc resources`
  - `should find every lootable item of a reward in the lootable item resources`
  - `should hold exactly one main quest`
- `QuestAcceptanceTest`
  - `The leader should carry the reward gold when the main quest's unit is defeated`
  - `A reward item should lie on the leader's tile and be stored when it walks back when the bag is full`
  - `A side quest should run through its tasks and pay its reward when it is started by talking to its giver`
- `SetupBattleTest`
  - `should request each subdomain initialisation after the subdomains it refers to, then exploring, then the main quest, when the battle is set up` (replaces the current name)
  - `should stop after the quests, which the saved game is restored on, when a saved game is loaded` (replaces the current name)
- `SaveFixtureTest`: its game gains a main quest, and `v3.json` holds its status.

## Known limitations

- Nothing shows a quest: the only one in the game pays its gold silently, and the player has to open the inventory to notice.
- Side quests, abandoning, repeating and dropping a reward can not happen in the game yet, as no side quest ships, no view abandons, and the main quest pays no items. Only the tests play them.
- A battle unit already standing on a tile of a `REACH_TILES` task when it becomes current does not complete it: it has to move onto one.
- A defeat task can be farmed by waiting for respawns, as `BattleUnitDefeated` counts them all.
- A leader defeated by an event still waiting on the bus when the last task completes receives the reward, as the reward is requested for whoever leads at that moment. It needs a defeat of the task's unit and of the leader in the same chain of events, which should be rare.
- Every earlier save becomes incompatible with `SAVE_VERSION` 3.
