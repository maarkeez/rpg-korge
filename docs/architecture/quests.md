# Quests

A quest is a line of tasks, completed one after the other, and completing the last one pays a reward to the party leader. It lives in the [`quest`](../../src/commonMain/kotlin/com/mkz/rpg/quest) subdomain, and the screen shows the main quest (see [What the player sees](#what-the-player-sees)).

## Terms

| Term        | Meaning                                            | In the code                                                                                 |
|-------------|----------------------------------------------------|---------------------------------------------------------------------------------------------|
| Quest       | A line of tasks ending in a reward                 | `Quest`, `resources/quest/<quest id>.toml`                                                  |
| Main quest  | The one principal quest of the game                | `QuestTypeDto.MAIN`, `type = "MAIN"`                                                        |
| Side quest  | Any of the other quests                            | `QuestTypeDto.SIDE`, `type = "SIDE"`                                                        |
| Task        | One stage of a quest                               | `DEFEAT_UNITS` (`unitId`, `count`), `TALK_TO_NPC` (`npcId`), `REACH_TILES` (`tiles`)        |
| Reward      | The gold and lootable items a completed quest pays | `[reward]`, `gold`, `lootableItemIds`                                                       |
| Leader      | Who receives the reward, already a term of `Party` | `leaderBattleUnitId`                                                                        |
| Quest giver | The npc who starts a side quest                    | `giverNpcId`                                                                                |
| Repeatable  | A side quest that can run again once completed     | `repeatable`, `false` by default                                                            |
| Status      | Where a quest is in its life                       | `NOT_STARTED`, `ACTIVE`, `COMPLETED`                                                        |

## Starting

The game has exactly one main quest, and fails to start without it (`MainQuestNotUnique`). `SetupBattle` requests initialising the quests after the buildings, in a new game and in a loaded one, and starts the main quest last, only in a new game. It runs once, and can not be abandoned, repeated nor failed. Completing it pays its reward and publishes its events, and exploring goes on. See [Game data](game-data.md#how-the-battle-is-set-up).

## Side quests

The game can hold any number of side quests, and ships none yet, so only the tests play them. A side quest starts once the party finishes talking to its quest giver, with no choice to accept or decline, and every eligible side quest of that giver starts on the same talk. It can not fail.

A side quest with `repeatable = true` starts again on a later talk to its giver, from its first task, and pays its full reward each time.

[`ResolveNpcTalk`](../../src/commonMain/kotlin/com/mkz/rpg/quest/usecases/commands/ResolveNpcTalk.kt) lists the quests that npc can start, then counts the talk on every quest, and finally starts the listed ones. So the talk that starts a quest does not complete its first task, and the talk that completes a quest does not restart it.

## Abandoning

`QuestApi.abandonQuest`, with no view yet, abandons an active side quest: it goes back to `NOT_STARTED` with its progress reset, announcing `QuestAbandoned`, so its giver can start it again even when it is not repeatable. It fails with `MainQuestCanNotBeAbandoned` on the main quest, and with `QuestNotActive` on a quest that is not active.

## Tasks

Each task maps to a fact the game already publishes. It only counts them from when it becomes the current task, so a rat defeated earlier does not count. One fact progresses every active quest, at most one task each.

| Task           | Fact                 | Done when                                                                                                                                                                             |
|----------------|----------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `DEFEAT_UNITS` | `BattleUnitDefeated` | `count` battle units of `unitId`, of a player other than the party's, were defeated by anything, respawned and CPU-summoned ones included. `BattleUnitVanished` does not count.        |
| `TALK_TO_NPC`  | `NpcTalkFinished`    | The party finished talking to `npcId`.                                                                                                                                                 |
| `REACH_TILES`  | `BattleUnitMoved`    | A party member touched any of `tiles`, while exploring or battling. A walk touches every tile of its path, so nobody crosses a line of tiles without touching it. A teleport only touches its destination. A summon is no party member. |

| Publishes        | When                                                         |
|------------------|--------------------------------------------------------------|
| `QuestStarted`   | The quest starts.                                            |
| `TaskProgressed` | A defeat counts, each time, with its progress and its count. |
| `TaskCompleted`  | The current task is done.                                    |
| `QuestCompleted` | The last task is done.                                       |
| `QuestAbandoned` | A side quest is abandoned.                                   |

For example, a new game with the only quest of the game, defeating the goat wizard for 150 gold, while the hero leads carrying its 30 gold:

| When                                     | Published                                                                                            | Result                                             |
|------------------------------------------|------------------------------------------------------------------------------------------------------|----------------------------------------------------|
| The new game is set up                   | `QuestStarted`                                                                                       | `ACTIVE`, first task, progress 0                   |
| A rat is defeated                        | Nothing                                                                                              | It is not the task's unit                          |
| The goat wizard is defeated              | `TaskProgressed` (1 of 1), `TaskCompleted`, `QuestCompleted`, then `RequestGrantReward` for the hero | `COMPLETED`                                        |
| `inventory` handles the request          | `RewardGranted(hero, gold = 150)`                                                                    | The hero carries 180 gold                          |
| The goat wizard respawns and is defeated | Nothing                                                                                              | The quest is completed, and a main quest runs once |

## The reward

[`RequestQuestReward`](../../src/commonMain/kotlin/com/mkz/rpg/quest/usecases/commands/RequestQuestReward.kt) runs on `QuestCompleted`, and publishes `RequestGrantReward` for whoever leads, on the tile it stands on. As the bus is FIFO, it is handled after the requests already waiting: a leader that just moved onto a tile of a `REACH_TILES` task is on it by then, as `BattleUnitMoved` is followed by `RequestMoveOccupant`, and a leader defeated by the same chain of events has already left the party. So the next member receives the reward when the leader was defeated, and nobody does once the whole party is gone.

A reward is some gold and a list of lootable items, as the game has no experience points. `inventory` pays it, capping the gold and dropping what does not fit on the leader's tile. See [Rewards](inventory.md#rewards).

## What the player sees

The screen only follows the main quest, in [`QuestPresenter`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/QuestPresenter.kt): it reads `QuestApi.searchQuests`, and ignores an event naming any other quest. [`QuestMapper`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/QuestMapper.kt) turns the quest into `QuestInfo`, the plain data the views draw, already translated, so they import no domain. The tasks after the current one are never mapped, so they can not be revealed.

### The tracker

[`QuestTrackerView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/QuestTrackerView.kt) is a capsule on the right of the top line, beside Exploring, naming the current task, with its count when it counts above 1, e.g.: `Defeat 5 Rats 3/5`. A talk, a reach or a single defeat has nothing to count.

| Event                                     | The tracker                                                                                                     |
|-------------------------------------------|-----------------------------------------------------------------------------------------------------------------|
| `QuestStarted`, `GameRestored`            | Is drawn, without popping, so a loaded game draws it too, ticked when completed.                                |
| `TaskProgressed`, `TaskCompleted`         | Pops alongside the timeline when what it reads differs from what it displays, so a defeat pops it once, even when it publishes both. Nothing when the quest is completed, as `QuestCompleted` follows. |
| `QuestCompleted`                          | Reads Quest completed for good, with a tick.                                                                    |
| `ExplorationPaused`, `ExplorationResumed` | Stays in the same place, read only in a battle: no chevron, and tapping it does nothing.                        |
| `PlayerVictory`                           | Is hidden, as the winner is drawn centred across the whole top line.                                            |

## Saving

`quest` is saved: each quest's status, current task and its progress. The rest is read from the resources again. See [Save and load](save-and-load.md#snapshots).

## Dependencies

`quest` depends on `shared`, `battlefield`, `battleUnit`, `party` and `inventory`, and nothing depends on it but `battlesetup` and `screen`. The game does not check the units, npcs and lootable items a quest names when it starts: `QuestResourcesTest` does, in CI. See [Package structure](package-structure.md).

## Known limitations

- A side quest is played and paid with nothing on the screen.
- A long task beside a long turn name may touch while the turn pops to 1.3 on the left of the top line. The tracker draws on top, and the pop lasts 400ms.
- The pop of the tracker, 1.15 for 400ms, was not seen in the prototypes, whose stories draw the settled state: it is a first guess, to check by playing.
- Side quests, abandoning, repeating and dropping a reward can not happen in the game yet, as no side quest ships, no view abandons, and the main quest pays no items. Only the tests play them.
- A battle unit already standing on a tile of a `REACH_TILES` task when it becomes current does not complete it: it has to move onto one.
- A defeat task can be farmed by waiting for respawns, as `BattleUnitDefeated` counts them all.
- A leader defeated by an event still waiting on the bus when the reward is requested receives the reward. It needs a defeat of the task's unit and of the leader in the same chain of events, which should be rare.
