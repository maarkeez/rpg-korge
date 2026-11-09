# Quests

A quest is a line of tasks, completed one after the other, and completing the last one grants a reward to the party leader. It lives in the [`quest`](../../src/commonMain/kotlin/com/mkz/rpg/quest) subdomain, and the screen shows the main quest (see [What the player sees](#what-the-player-sees)).

## Terms

| Term            | Meaning                                                                     | In the code                                                                 |
|-----------------|-----------------------------------------------------------------------------|-----------------------------------------------------------------------------|
| Quest           | A line of tasks ending in a reward                                          | `Quest`, `resources/quest/<quest id>.toml`                                  |
| Main quest      | The one principal quest of the game                                         | `QuestTypeDto.MAIN`, `type = "MAIN"`                                        |
| Side quest      | Any of the other quests                                                     | `QuestTypeDto.SIDE`, `type = "SIDE"`                                        |
| Task            | One stage of a quest                                                        | `DEFEAT` (`unitId`, `requiredDefeats`), `TALK` (`npcId`), `VISIT` (`tiles`) |
| Reward          | The gold and lootable items a completed quest grants                        | `[reward]`, `gold`, `lootableItemIds`                                       |
| Leader          | Who receives the reward, already a term of `Party`                          | `leaderBattleUnitId`                                                        |
| Quest giver     | The npc who starts a side quest                                             | `questGiverNpcId`                                                           |
| Repeatable      | A side quest that can run again once completed                              | `repeatable`, `false` by default                                            |
| Journal         | The list of every quest started, in place of the pad                        | `QuestJournalView`                                                          |
| Pinned quest    | The quest the tracker names: the main quest, unless the player pins another | `ProgressDto.isPinned`, `PinQuest`, `QuestPinned`                           |
| Quest marker    | The sign over an npc's head on the battlefield                              | `QuestMarkerView`, `QuestMarkerView.Sign`                                   |
| Quest to start  | The gold sign: the npc can start a side quest                               | `Sign.QUEST_TO_START`                                                       |
| Talk task       | The green sign: a current task is to talk to the npc                        | `Sign.TALK_TASK`                                                            |
| Repeatable pill | The pill saying that a side quest can run again                             | `QuestViewParts.repeatablePill`                                             |
| Announcement    | A quest starting or completing, drawn as a toast                            | `QuestAnnouncementView`                                                     |
| Status          | Where a quest is in its life                                                | `NOT_STARTED`, `ACTIVE`, `COMPLETED`                                        |

## Starting

The game has exactly one main quest, and fails to start without it (`MainQuestNotUnique`). `SetUpGame` requests creating the quests (`RequestCreateQuests`) after the buildings, in a new game and in a loaded one, and starts the main quest last, only in a new game. It runs once, and can not be abandoned, repeated nor failed. Completing it pays its reward and publishes its events, and exploring goes on. See [Game data](game-data.md#how-the-game-is-set-up).

## Side quests

The game can hold any number of side quests, and ships none yet, so only the tests play them. A side quest starts once the party finishes talking to its quest giver, with no choice to accept or decline, and every eligible side quest of that giver starts on the same talk. It can not fail.

A side quest with `repeatable = true` starts again on a later talk to its quest giver, from its first task, and grants its full reward each time.

[`ResolveNpcTalk`](../../src/commonMain/kotlin/com/mkz/rpg/quest/usecases/commands/ResolveNpcTalk.kt) lists the quests that npc can start, then counts the talk on every quest, and finally starts the listed ones. So the talk that starts a quest does not complete its first task, and the talk that completes a quest does not restart it.

## Abandoning

`QuestApi.abandonQuest` abandons an active side quest (see [Abandoning](#abandoning)): it goes back to `NOT_STARTED` with its progress reset, announcing `QuestAbandoned`, so its quest giver can start it again even when it is not repeatable. It fails with `MainQuestCanNotBeAbandoned` on the main quest, and with `QuestNotActive` on a quest that is not active.

## Tasks

Each task maps to a fact the game already publishes. It only counts them from when it becomes the current task, so a rat defeated earlier does not count. One fact progresses every active quest, at most one task each.

| Task     | Fact                 | Done when                                                                                                                                                                                                                                                     |
|----------|----------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `DEFEAT` | `BattleUnitDefeated` | `requiredDefeats` battle units of `unitId`, of the CPU player, were defeated by anything, respawned and CPU-summoned ones included. `BattleUnitVanished` does not count.                                                                                      |
| `TALK`   | `NpcTalkFinished`    | The party finished talking to `npcId`.                                                                                                                                                                                                                        |
| `VISIT`  | `BattleUnitMoved`    | A party member walked or teleported onto any of `tiles`, while exploring or battling. A walk goes onto every tile of its path, so nobody crosses a line of tiles without visiting it. A teleport only goes onto its destination. A summon is no party member. |

| Publishes        | When                                                                           |
|------------------|--------------------------------------------------------------------------------|
| `QuestStarted`   | The quest starts.                                                              |
| `DefeatCounted`  | A defeat counts, each time, with its defeats counted and its required defeats. |
| `TaskCompleted`  | The current task is done.                                                      |
| `QuestCompleted` | The last task is done.                                                         |
| `QuestAbandoned` | A side quest is abandoned.                                                     |

For example, a new game with the only quest of the game, defeating the goat wizard for 150 gold, while the hero leads carrying its 30 gold:

| When                                     | Published                                                                                           | Result                                             |
|------------------------------------------|-----------------------------------------------------------------------------------------------------|----------------------------------------------------|
| The new game is set up                   | `QuestStarted`                                                                                      | `ACTIVE`, first task, progress 0                   |
| A rat is defeated                        | Nothing                                                                                             | It is not the task's unit                          |
| The goat wizard is defeated              | `DefeatCounted` (1 of 1), `TaskCompleted`, `QuestCompleted`, then `RequestGrantReward` for the hero | `COMPLETED`                                        |
| `inventory` handles the request          | `RewardGranted(hero, gold = 150)`                                                                   | The hero carries 180 gold                          |
| The goat wizard respawns and is defeated | Nothing                                                                                             | The quest is completed, and a main quest runs once |

## The side quests shipped

| Quest id             | Giver  | Tasks                                     | Reward                   | Repeatable |
|----------------------|--------|-------------------------------------------|--------------------------|------------|
| `bats-in-the-meadow` | Farmer | `DEFEAT` bat, 3. `TALK` farmer            | 40 gold                  | No         |
| `the-elders-errand`  | Elder  | `VISIT` (25, 3) and (25, 4). `TALK` elder | 25 gold, `health-potion` | No         |
| `rats-in-the-fields` | Elf    | `DEFEAT` rat, 2                           | 20 gold                  | Yes        |

Between them they use each kind of task, required defeats, a task done, gold and an item, and a repeatable quest. Rows 24 and 25 only have columns 3 and 4, so nobody gets to the southern fields without visiting one of those two tiles. Rats respawn, so the repeatable quest stays playable. See [Respawning](cpu-battle-brain.md#respawning).

## Pinning

The pinned quest is the one the tracker names. Exactly one quest is followed: the main quest by default, which holds no flag, or the side quest with `ProgressDto.isPinned`. [`PinQuest`](../../src/commonMain/kotlin/com/mkz/rpg/quest/usecases/commands/PinQuest.kt) unpins every other quest, then pins an active one with `Quest.pin`, publishing `QuestPinned`, and fails with `QuestNotActive` otherwise. Pinning the main quest, or a quest already pinned, clears every pin and publishes `QuestPinned` with the main quest's id. Completing or abandoning a pinned quest clears its pin too, returning to the main quest, and a quest starting does not take the pin.

## The reward

[`RequestQuestReward`](../../src/commonMain/kotlin/com/mkz/rpg/quest/usecases/commands/RequestQuestReward.kt) runs on `QuestCompleted`, and publishes `RequestGrantReward` for whoever leads, on the tile it stands on. As the bus is FIFO, it is handled after the requests already waiting: a leader that just moved onto a tile of a `REACH_TILES` task is on it by then, as `BattleUnitMoved` is followed by `RequestMoveOccupant`, and a leader defeated by the same chain of events has already left the party. So the next member receives the reward when the leader was defeated, and nobody does once the whole party is gone.

A reward is some gold and a list of lootable items, as the game has no experience points. `inventory` grants it, capping the gold and dropping what does not fit on the leader's tile. See [Rewards](inventory.md#rewards).

## What the player sees

The screen follows the quests in [`QuestPresenter`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/QuestPresenter.kt), reading `QuestApi.searchQuests`: the tracker follows the main quest, and a toast announces any quest. [`QuestMapper`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/QuestMapper.kt) turns the quest into `QuestInfo`, the plain data the views draw, already translated, so they import no domain. The tasks after the current one are never mapped, so they can not be revealed.

### The tracker

[`QuestTrackerView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/QuestTrackerView.kt) is a capsule on the right of the top line, beside Exploring, naming the current task of the [pinned quest](#pinning), or else of the main quest, with a pin while a side quest is pinned, and its count when it counts above 1, e.g.: `Defeat 5 Rats 3/5`. A talk, a reach or a single defeat has nothing to count.

| Event                                            | The tracker                                                                                                                                                                                                                                                            |
|--------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `QuestStarted` of the main quest, `GameLoaded`   | Is drawn, without popping, so a loaded game draws it too, ticked when completed.                                                                                                                                                                                       |
| `DefeatCounted`, `TaskCompleted`                 | Pops alongside the timeline when what it reads differs from what it displays, so a defeat pops it once, even when it publishes both. Nothing when the quest is completed, as `QuestCompleted` follows. Of a quest it does not name, it only pops, with no text change. |
| `QuestPinned`, `QuestAbandoned`                  | Is drawn, without popping, naming the quest now pinned, or the main quest.                                                                                                                                                                                             |
| `QuestCompleted`                                 | Is drawn, without popping: a pinned quest returns to the main quest, and the main quest reads Quest completed for good, with a tick.                                                                                                                                   |
| `ExplorationInterrupted`, `ExplorationResumed`   | Stays in the same place, read only in a battle: no chevron, and tapping it does nothing.                                                                                                                                                                               |
| `PlayerVictory`                                  | Is hidden, as the winner is drawn centred across the whole top line.                                                                                                                                                                                                   |

### The detail view

[`QuestDetailView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/QuestDetailView.kt) displays a quest in place of the pad, with time stopped (see [Reading the quest](exploration.md#reading-the-quest)). It is led by the portrait of who the current task is about, or the task's glyph for a place, with its caption (MAIN QUEST, SIDE QUEST or QUEST COMPLETED), the Repeatable pill when it runs again, the name and the description beside it. Under them, the current task in a banner, with its count and bar only when it counts above 1, the tasks done as a count, and the reward, which is always shown, evenly apart down the 300 of the panel, the banner lower when no tasks done are under it. Its buttons are in the [action row](buttons.md#the-action-row), at the level of the finish turn, with Back first. Once completed, it still opens, with a ticked target, so the story can be read again.

### The journal

Tapping the tracker while exploring opens [`QuestJournalView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/QuestJournalView.kt) once a side quest is listed, and otherwise the detail view of the main quest, so there is no list of one row. It takes the place of the pad, in the 300 of its slot, so the battlefield stays in sight.

- It lists the main quest first while active, then the active side quests, then the completed quests ticked, each group in the order the quests were created. Side quests not started are hidden, so nothing is spoiled.
- Each row names its section (MAIN QUEST, SIDE QUEST, COMPLETED), and holds the target's portrait, the name, the current task, its count and bar only when it counts above 1, the Repeatable pill, and on an active quest its pin, gold when pinned.
- Tapping a pin makes the tracker name that quest, and tapping the gold pin returns it to the main quest. See [Pinning](#pinning). The pin takes its own taps in the 32 around it, so the row does not open.
- It holds any number of quests, dragged to scroll. A drag above 4 points does not select the row under it, and the list keeps its place when coming back from a detail view.
- Tapping a row opens its detail view, and Back, filling the action row, steps out one level at a time.
- 4 rows of 63 fit its list of 261. While dragging, the list clips the rows crossing its edges: they draw their rounded shapes as bitmaps (`GraphicsRenderer.SYSTEM`), as KorGE draws them straight to the GPU otherwise, past the clip.

### Abandoning

The detail view of an active side quest has Abandon beside Back in the action row, in `HudStyle.DANGER`, the red of every action that can not be undone. It asks first, in [`ConfirmDialogView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/ConfirmDialogView.kt): Abandon Bats in the Meadow? Its progress is lost, and its giver can start it again. Back and Abandon stay in the row, under the thumb, so tapping Abandon twice abandons the quest without the question being read. Back returns to the detail view. Once abandoned, the quest leaves the journal, and the player returns to it when a side quest is still listed, and otherwise to the pad. No toast announces it.

### The quest markers

[`QuestMarkerView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/QuestMarkerView.kt) is the sign over an npc's head, on the battlefield: gold with an exclamation mark for a quest to start, and green with a speech bubble for a talk task. [`SearchQuestMarkers`](../../src/commonMain/kotlin/com/mkz/rpg/quest/usecases/queries/SearchQuestMarkers.kt) lists one sign per npc, the talk task winning over a quest to start, as the talk does both.

- `QuestPresenter` reads them on `QuestStarted`, `TaskCompleted`, `QuestCompleted`, `QuestAbandoned`, `NpcRecruited`, `GameLoaded` and `ExplorationResumed`, and draws each on the tile of its npc, removing those gone. An npc no longer on the battlefield, e.g.: once recruited, has none.
- They only show while exploring, as an npc can only be talked to then: `ExplorationInterrupted` removes them all.
- A repeatable quest giver stays marked whenever its quest can start.

### The announcements

[`QuestAnnouncementView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/QuestAnnouncementView.kt) is a toast over the top of the battlefield, announcing every quest starting, with its goal, and completing, with the reward the quest defines, also when a repeatable quest runs again. It is held for 4 seconds and fades in 0.5 seconds. A count or a task done only pops the tracker.

- It takes no taps, so whatever is under it can be pressed.
- It holds nothing: time runs, and the player can act. Toasts play one after the other, so a talk starting two quests takes 9 seconds. See [Presentation timeline](presentation-timeline.md#announcing-a-quest).
- A loaded game announces nothing, as `SetUpGame` only starts the main quest in a new game and `RestoreQuests` publishes nothing.

For example, a new game, then defeating the goat wizard in a battle:

| When                        | Published                                          | Displayed                                                                                 | Time     |
|-----------------------------|----------------------------------------------------|-------------------------------------------------------------------------------------------|----------|
| The new game is set up      | `QuestStarted`                                     | The tracker reads Defeat the Goat Wizard, and a toast announces the quest for 4.5 seconds | Runs     |
| The tracker is tapped       | `QuestOpened`                                      | The detail view takes the place of the pad, as no side quest is listed                    | Stopped  |
| Back is tapped              | `QuestClosed`                                      | The pad is displayed again                                                                | Runs     |
| A battle starts             | `ExplorationInterrupted`                           | The tracker stays, without its chevron                                                    | In turns |
| The goat wizard is defeated | `DefeatCounted`, `TaskCompleted`, `QuestCompleted` | Once it has fallen, the tracker reads Quest completed, and a toast announces 150 G        | In turns |
| The battle ends             | `ExplorationResumed`                               | The ticked tracker has its chevron back, and opens the completed detail                   | Runs     |

## Saving

`quest` is saved: each quest's status, current task, its progress and whether it is pinned. The rest is read from the resources again. See [Save and load](save-and-load.md#snapshots).

## Dependencies

`quest` depends on `shared`, `battlefield`, `battleUnit`, `party` and `inventory`, and nothing depends on it but `gameSetup` and `screen`. The game does not check the units, npcs and lootable items a quest names when it starts: `QuestResourcesTest` does, in CI. See [Package structure](package-structure.md).

## Known limitations

- The toast shows the reward the quest defines, so a leader near the cap of 99,999 gold reads 150 G and receives less, and an item that did not fit the bag drops on its tile without a word. Only a leader close to the cap, or with a full bag on The Elder's Errand, would notice.
- A long description is cut with an ellipsis beside the target, at 4 lines with a name of one line and 3 with two, as the detail view has no scrolling. The shipped descriptions take 2 or 3.
- A long task beside a long turn name may touch while the turn pops to 1.3 on the left of the top line. The tracker draws on top, and the pop lasts 400ms.
- The pop of the tracker, 1.15 for 400ms, was not seen in the prototypes, whose stories draw the settled state: it is a first guess, to check by playing.
- A quest giver's dialog says nothing about its quest: the toast is what tells the player. E.g.: the Farmer still only thanks the party for dealing with that bat.
- The toast can be missed, as nothing brings it back: the journal holds the same story.
- A marker only shows on an npc in view, with nothing pointing at one further away.
- The drag of the journal was only tried with a mouse, so its feel on a phone is still to be checked.
- A battle unit already standing on a tile of a `VISIT` task when it becomes current does not complete it: it has to move onto one.
- A defeat task can be farmed by waiting for respawns, as `BattleUnitDefeated` counts them all.
- A leader defeated by an event still waiting on the bus when the reward is requested receives the reward. It needs a defeat of the task's unit and of the leader in the same chain of events, which should be rare.
