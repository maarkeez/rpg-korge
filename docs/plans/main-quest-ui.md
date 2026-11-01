# Plan: The main quest on the screen, with a tracker, a detail view and announcements

> **Status:** to do.
>
> **Summary:** The [quest system](quest-system.md) only exists behind the screen. A new game starts the main quest
> without a word, nothing tells the player to defeat the goat wizard at row 36 of the 40 rows of the battlefield, and
> defeating it pays 150 gold silently, so the player has to open the inventory to notice. Instead, a capsule on the top
> line always names the current task, tapping it opens the quest in full in place of the pad, and a card over the dimmed
> screen announces the quest when it starts and when it completes, with its reward. Chosen among the Storybook prototypes
> of the local branch `prototype/quest-ui` (`e2b2f136`): variant A of the tracker, C of the detail view and C of the
> announcements. It builds on [Quests](../architecture/quests.md), [Exploration](../architecture/exploration.md),
> the [Presentation timeline](../architecture/presentation-timeline.md), [Localization](../architecture/localization.md)
> and the [battle HUD](battle-hud-vector-chrome.md), whose vector chrome it draws with.

## Domain terms

| Term         | Meaning                                                             | In the code                   |
|--------------|---------------------------------------------------------------------|-------------------------------|
| Tracker      | The capsule on the top line naming the current task                 | `QuestTrackerView`            |
| Detail view  | The quest in full, in place of the pad                              | `QuestDetailView`             |
| Announcement | The card over the dimmed screen, when the quest starts or completes | `QuestAnnouncementView`       |
| Quest info   | The plain data the three views display, already translated          | `QuestInfo`                   |
| Target       | Who the current task is about, drawn by its portrait                | `QuestInfo.Task.portraitPath` |
| Reading      | The party's mode while the detail view is open: time stops          | `Party.Mode.READING_QUEST`    |

## Decisions

| Topic               | Decision                                                                                                                                                                                                                                                                                                                                                                                                      |
|---------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope               | The main quest only: its tracker, detail view and announcements, in the game, with their stories and texts. E.g.: a new game announces The Goat Wizard's Lair, and defeating the goat wizard announces its 150 gold.                                                                                                                                                                                          |
| Tracker             | Variant A: a capsule on the right of the top line, beside Exploring. Chosen over a floating card, which hides tiles, and a strip over the pad, which has no place in a battle. It covers nothing, in the only line with room to spare.                                                                                                                                                                        |
| Tracker in a battle | Displayed in the same place, so it does not jump when a battle starts, and read only: no chevron, and tapping it does nothing. It comes for free with variant A, the condition for showing it in a battle at all.                                                                                                                                                                                             |
| Tracker at the end  | Hidden on `PlayerVictory`, as the winner is drawn centred across the whole top line, 30 points tall.                                                                                                                                                                                                                                                                                                          |
| Completed quest     | The tracker stays for good as a ticked Quest completed, and tapping it opens the detail view in its completed state, so the story can be read again. Chosen over removing it, which leaves no way into the detail view.                                                                                                                                                                                       |
| Detail view         | Variant C, in place of the pad, in the 300 of its slot, led by the target's portrait. Chosen over a card and a timeline, which leave a large gap on a quest of one task. The battlefield stays in sight above it.                                                                                                                                                                                             |
| Opening the detail  | Only by tapping the tracker, once the timeline is idle and while the pad is displayed. A tap at any other moment is ignored, not kept as a press of the pad is. Chosen over an entry in the pause menu, as A, B and Start are taken and the tracker is already in sight. Back closes it, as in the shop.                                                                                                      |
| Time in the detail  | It stops, as in the formation and the pause: a new `Party` mode, `READING_QUEST`. So the CPU battle units do not wander, lava does not hurt, and tapping the battlefield does nothing. Chosen over reusing `PAUSED`, which allows saving and opens the pause screen, and over a flag in the presenter, which leaves the battlefield tappable under the panel.                                                 |
| Tasks to come       | The detail view shows the tasks done, as a count, and the current one. The later ones are never mapped, so they can not be revealed. The reward is always shown.                                                                                                                                                                                                                                              |
| Count and bar       | Only when a task counts above 1, e.g.: Defeat 5 Rats 3/5. A talk, a reach or a single defeat has nothing to count: `0/1` with an empty bar was tried in the prototypes, and read as noise.                                                                                                                                                                                                                    |
| Announcements       | Variant C: a card over the dimmed screen, until Continue is tapped. Chosen over a toast and a banner, as each moment happens once per game, and the start card is the only place that tells the goal without a tap. Only the start and the completion: a count or a task done only pops the tracker.                                                                                                          |
| Time in a card      | It stops while exploring: `ExplorationPresenter.tick` starts no second while `QuestPresenter.isAnnouncing`, so nothing wanders and no terrain effect applies. The timeline waits for Continue, as it does for a found item (`InventoryPresenter.askWhatToDoWith`), so the player can not act.                                                                                                                 |
| Loading a save      | No card, as `SetupBattle` only starts the main quest in a new game and `RestoreQuests` publishes nothing. `GameRestored` draws the tracker from `searchQuests`, ticked when completed. `SAVE_VERSION` stays 3: the mode is not saved, and saving is only possible while `PAUSED`.                                                                                                                             |
| Reward in the card  | The reward the quest defines, read on `QuestCompleted`. Chosen over waiting for `RewardGranted`, which names no quest, and never comes when nobody is left to receive it.                                                                                                                                                                                                                                     |
| Directions          | Only the description says where to go (down south). No arrow nor marker, as a defeat task names a unit, not a tile, and any battle unit of that unit counts.                                                                                                                                                                                                                                                  |
| Texts               | The name, description and each task of a quest are game data, keyed as `quest.<quest id>.name`, `.description` and `.task.<index>`, in English and Spanish. Each task is a whole sentence written by hand, e.g.: `Defeat the Goat Wizard`. Chosen over building it from the unit's name, which [Localization](../architecture/localization.md#messages) forbids, as it would lose the article and the plural. |
| Pixel art           | Only the portraits (3× in the detail view, 2× in the card) and the item icons (1×). The rest is vector chrome from `HudStyle`, as in the [battle HUD](battle-hud-vector-chrome.md).                                                                                                                                                                                                                           |
| Architecture        | All in `screen.adapters.presentation`, but the new mode, in `party`. `party` names the mode and imports nothing of `quest`. The views take `QuestInfo`, which `QuestMapper` builds from `Quest.Dto`, so they import no domain ([`ViewArchitectureTest`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/architecture/ViewArchitectureTest.kt)).                                                          |
| Prototype           | `prototype/quest-ui` is a reference and is never merged. The views are rebuilt on `main` with the chosen variants alone: no `QuestVariant`, `QuestMoment` becomes `QuestAnnouncementView.Moment`, hard-coded texts go through `Texts`, and `QuestScreenView`, which only composed the stories, is left out.                                                                                                   |
| Storybook           | One binding per view, with the real quest and the cases the backend allows. See [Storybook](#storybook).                                                                                                                                                                                                                                                                                                      |
| Tests               | View and presenter tests, the party's new mode, the catalogs in `LocalizationResourcesTest`, and a new `QuestEndToEndTest`. See [Tests](#tests).                                                                                                                                                                                                                                                              |
| Out of scope        | Side quests and abandoning on the screen, an arrow or marker, a journal listing several quests, the note that a reward did not fit the bag, and sound.                                                                                                                                                                                                                                                        |
| Delivery            | 3 milestones, each green on CI. See [Milestones](#milestones).                                                                                                                                                                                                                                                                                                                                                |

For example, a new game, then defeating the goat wizard in a battle. Every event of one line arrives in the same chain of
events, and each is read when it arrives, as the [timeline](../architecture/presentation-timeline.md#rules) requires:

| When                        | Published                                           | Displayed                                                                                               | Time     |
|-----------------------------|-----------------------------------------------------|---------------------------------------------------------------------------------------------------------|----------|
| The new game is set up      | `QuestStarted`                                      | The tracker reads Defeat the Goat Wizard, and the start card dims over it                               | Stopped  |
| Continue is tapped          | Nothing                                             | The card goes, and the pad can be used                                                                  | Runs     |
| The tracker is tapped       | `QuestOpened`                                       | The detail view takes the place of the pad                                                              | Stopped  |
| Back is tapped              | `QuestClosed`                                       | The pad is displayed again                                                                              | Runs     |
| A battle starts             | `ExplorationPaused`                                 | The tracker stays, without its chevron                                                                  | In turns |
| The goat wizard is defeated | `TaskProgressed`, `TaskCompleted`, `QuestCompleted` | Once it has fallen, the tracker reads Quest completed, and the completion card dims over it, with 150 G | Waits    |
| The battle ends             | `ExplorationResumed`                                | The ticked tracker has its chevron back, and opens the completed detail                                 | Runs     |

And a quest of a line of tasks, as the backend allows: a defeat task going from 2/5 to 3/5 pops the tracker once. The
fifth defeat publishes `TaskProgressed` and `TaskCompleted`, both reading the next task as current, so the tracker draws
and pops once, for the first of them, as the second finds nothing new.

## Milestones

Each milestone is a few commits straight on `main`, green on CI.

| #  | Milestone         | Done when                                                                                                                                          | Status | Done in |
|----|-------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|--------|---------|
| M1 | The tracker       | Playing shows the current task on the top line, exploring and battling, ticked once completed, also in a loaded game. Tapping it does nothing yet. | ⬜     |         |
| M2 | The detail view   | Tapping the tracker while exploring opens the quest in full with time stopped, and Back closes it, also once completed.                            | ⬜     |         |
| M3 | The announcements | A new game announces the quest and defeating the goat wizard announces its reward, each until Continue. The plan is marked as done.                | ⬜     |         |

### M1 commits

1. Name the quests: the `quest.defeat-the-goat-wizard.*` entries in `messages.pot` and `es.po`, `Texts.questName`, `questDescription` and `questTask`, `quest` in `LocalizationResourcesTest`, and describe them in Localization and Game data.
2. Display a quest: `QuestInfo` and `QuestMapper`, with `HudStyle.COIN_MIDDLE`, `PillView.Glyph.GOLD` and its story, and `QuestViewParts`.
3. Add `QuestTrackerView` with its stories, and `QuestPresenter` wired in `BattleScene`, keeping the tracker up to date, and describe it in Quests.

### M2 commits

1. Read a quest with time stopped: `Party.openQuest` and `closeQuest`, `READING_QUEST`, `isReadingQuest`, `OpenQuest`, `CloseQuest`, `QuestOpened` and `QuestClosed`, and `BattlefieldPresenter.isTimeStopped`.
2. Add `QuestDetailView` with its stories, opened by the tracker through `ExplorationPresenter`, with `QuestEndToEndTest`, and describe it in Exploration.

### M3 commits

1. Add `QuestAnnouncementView` with its stories.
2. Announce the quest: `QuestPresenter` waits for Continue in the timeline and stops the seconds of `ExplorationPresenter`, `startNewGame` taps Continue, and describe it in Quests and the Presentation timeline. Mark the plan as done.

## Reference

Sizes are in points of the 390x844 game window. Colours are `HudStyle` tokens: `PANEL`, `PANEL_RAISED`, `STROKE`, `TRACK`,
`MUTED`, `FAINT`, `SELECTION` (gold), `ALLY` (green) and `ENEMY` (red).

### party

- `Party.Mode.READING_QUEST`, with `Dto.isReadingQuest`.
- `Party.openQuest()` reads, only when `canWalk()`, recording `QuestOpened(playerId)`. Otherwise, nothing changes, e.g.: still walking, talking, in formation, paused or battling. `closeQuest()` explores again, recording `QuestClosed(playerId)`, and does nothing unless reading.
- `OpenQuest` and `CloseQuest`, as `OpenPause` and `ClosePause`, in `PartyApi` as `openQuest` and `closeQuest`.
- `canSave` in `BattleScene` keeps asking for `isPaused`, so a quest can not be saved mid-reading, and `Party.Dto.SnapshotDto` does not change.

### Texts

In `messages.pot`, with a comment for the translators as the others have, and in `es.po`:

| Key                                        | English                                                              | Spanish                                                                     |
|--------------------------------------------|----------------------------------------------------------------------|-----------------------------------------------------------------------------|
| `quest.defeat-the-goat-wizard.name`        | The Goat Wizard's Lair                                               | La guarida del Mago Cabra                                                   |
| `quest.defeat-the-goat-wizard.description` | A goat swallowed a wizard's magic and now rules the lair down south. | Una cabra se tragó la magia de un mago y ahora gobierna la guarida del sur. |
| `quest.defeat-the-goat-wizard.task.0`      | Defeat the Goat Wizard                                               | Derrota al Mago Cabra                                                       |
| `ui.questTracker.completed`                | Quest completed                                                      | Misión completada                                                           |
| `ui.questDetail.mainQuest`                 | MAIN QUEST                                                           | MISIÓN PRINCIPAL                                                            |
| `ui.questDetail.completed`                 | QUEST COMPLETED                                                      | MISIÓN COMPLETADA                                                           |
| `ui.questDetail.reward`                    | REWARD                                                               | RECOMPENSA                                                                  |
| `ui.questDetail.tasksDone` (plural)        | {tasks} task done / {tasks} tasks done                               | {tasks} tarea hecha / {tasks} tareas hechas                                 |
| `ui.questDetail.back`                      | Back                                                                 | Volver                                                                      |
| `ui.questAnnouncement.started`             | NEW QUEST                                                            | NUEVA MISIÓN                                                                |
| `ui.questAnnouncement.completed`           | QUEST COMPLETED                                                      | MISIÓN COMPLETADA                                                           |
| `ui.questAnnouncement.reward`              | REWARD                                                               | RECOMPENSA                                                                  |
| `ui.questAnnouncement.continue`            | Continue                                                             | Continuar                                                                   |

- `UiText` gains `QUEST_TRACKER_COMPLETED`, `QUEST_DETAIL_MAIN_QUEST`, `QUEST_DETAIL_COMPLETED`, `QUEST_DETAIL_REWARD`, `QUEST_DETAIL_TASKS_DONE` (plural), `QUEST_DETAIL_BACK`, `QUEST_ANNOUNCEMENT_STARTED`, `QUEST_ANNOUNCEMENT_COMPLETED`, `QUEST_ANNOUNCEMENT_REWARD` and `QUEST_ANNOUNCEMENT_CONTINUE`.
- `Texts.questName(questId)`, `questDescription(questId)` and `questTask(questId, taskIndex)`.
- The capitals are in the texts, as the code never changes the case of a text.

### QuestInfo and QuestMapper

- `QuestInfo(name, description, tasks, gold, items, isCompleted)`, with `currentTask` and `doneTasks`. `Task(kind, text, progress, count, portraitPath, isDone)`, `Kind` (`DEFEAT`, `TALK`, `REACH`) and `RewardItem(id, name)`.
- `QuestMapper.questInfo(quest: Quest.Dto, texts: Texts): QuestInfo`, as `AbilityInfoMapper` does. Tasks: every task before `currentTaskIndex` as done, then the current one with `currentTaskProgress`, and none after it. A `COMPLETED` quest maps every task as done.
- `count` is `DefeatUnitsDto.count`, and 1 for a talk or a reach.
- `portraitPath` is `unit/<unitId>_portrait.png` for a defeat, `npc/<npcId>_portrait.png` for a talk, and null for a reach. A recruitable npc needs no other path, as `QuestResourcesTest` fails on one in a talk task.

### QuestViewParts

An `internal object`, as the prototype's, drawing for the three views. `QuestViewParts.kt` holds no view, so it needs no stories.

| Part               | Draws                                                                                                                                                             |
|--------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `fitted`           | The text cut with `…` to fit a width.                                                                                                                             |
| `wrappedText`      | The text wrapped to a width, one `Text` per line, 3 between lines, optionally centred, cut with `…` past `maximumLines`. Returns its height.                      |
| `taskGlyph`        | In a square of a side. Defeat: a crosshair, four 0.3-long ticks and a dot, as a sword read as a tick when small. Talk: a speech bubble. Reach: a flag.            |
| `tick`, `tickDisc` | A tick of two rotated rectangles, and the white tick on a filled `ALLY` disc.                                                                                     |
| `chevron`          | A `>` of two rotated rectangles, as KorGE draws thin shape strokes faint.                                                                                         |
| `targetSlot`       | A rounded slot, its ring a filled shape 2 wide with the slot inset over it. The portrait at a whole scale, or the task's glyph at half the side when it has none. |
| `ringOf`           | `ENEMY` for a defeat, `ALLY` for a talk, `SELECTION` for a reach.                                                                                                 |
| `progressBar`      | Rounded, on `TRACK`, in `SELECTION`, cut into one segment per unit up to 10, 2 apart.                                                                             |
| `rewardRow`        | 28 tall: the gold as a `PillView` with `Glyph.GOLD`, then each item in a slot of 28, its icon at 1×, 5 apart. Returns its width.                                  |

`PillView.Glyph.GOLD` is a circle in `SELECTION` with a middle of half its radius in `HudStyle.COIN_MIDDLE` (`#C78A1E`).

### QuestTrackerView

`QuestTrackerView : UIContainer(Size(390, 33))`, sharing the top line with `BattleInfoView` in a `fixedSizeContainer` of the same size, so the vertical stack of `BattleScene` does not move. Only the capsule takes taps, so the left of the line stays free.

| Part    | Size and place                                               | Colour                    |
|---------|--------------------------------------------------------------|---------------------------|
| Capsule | 30 tall, radius 15, 1 under the top, its right edge at x 384 | `PANEL`, 1-pixel `STROKE` |
| Glyph   | `taskGlyph` of 14, 10 from its left, vertically centred      | `SELECTION`               |
| Task    | Size 13, 6 after the glyph, at most 200 wide with `fitted`   | White                     |
| Count   | Size 13, 6 after the task, only when `count` is above 1      | `SELECTION`               |
| Chevron | 10, 4 after the text, 8 from its right, only when tappable   | `MUTED`                   |

- The capsule is as wide as what it holds: 10 + glyph + 6 + text + count + 4 + chevron + 8. E.g.: Defeat the Goat Wizard is about 175 wide, and the longest task with no count 252.
- Completed: `tickDisc` of 14 in place of the glyph, and Quest completed (`QUEST_TRACKER_COMPLETED`) in `MUTED`, with no count.
- `display(quest: QuestInfo)` draws it. `setTappable(isTappable)` shows or hides the chevron, re-fitting the capsule to the right edge. `pop()` scales the capsule to `POP_SCALE` (1.15) and back within `POP_DURATION` (400ms), growing from its right edge so it stays on the screen. `hide()`.
- `QuestTrackerView.Delegate.questTrackerSelected()`.

### QuestDetailView

`QuestDetailView : Container`, 378x300 (`WIDTH`, `HEIGHT`), at x 6 of the slot of the pad, on top of the views already there. A `HudStyle.panel` as its background.

| Part          | Size and place                                                                                       | Colour                                    |
|---------------|------------------------------------------------------------------------------------------------------|-------------------------------------------|
| Target        | `targetSlot` of 104 at (12, 12), portrait at 3×, a reach's flag at half the side                     | Ring from `ringOf`, `ALLY` once completed |
| Target's tick | `tickDisc` of 26, over the bottom right corner of the target, only once completed                    | `ALLY`                                    |
| Caption       | Size 11, at (128, 10): MAIN QUEST, or QUEST COMPLETED                                                | `FAINT`, or `ALLY` once completed         |
| Name          | Size 17, from (128, 26), 238 wide, at most 2 lines                                                   | White                                     |
| Description   | Size 13, 5 under the name, 238 wide, as many lines as fit down to the bottom of the target (y 116)   | `MUTED`                                   |
| Task banner   | 354x56 at (12, 126), panel radius 10                                                                 | `PANEL_RAISED`                            |
| Single task   | `taskGlyph` of 16 at (24, 146), text size 16 at x 48, `fitted` to 306, both centred in the banner    | `SELECTION`, white                        |
| Counted task  | Glyph and text at y 136, count right-aligned to x 354, `progressBar` 330x7 at (24, 162)              | `SELECTION`, white, `SELECTION`           |
| Completed     | `tickDisc` of 16 at (24, 146) and the task, size 16 at x 48                                          | `ALLY`, `MUTED`                           |
| Tasks done    | `tickDisc` of 12 at (14, 189) and N tasks done, size 14 at x 32, only when some are, until completed | `ALLY`, `MUTED`                           |
| Divider       | 354x1 at (12, 248)                                                                                   | `STROKE`                                  |
| Reward        | REWARD, size 11 at (12, 265), then `rewardRow` 8 after it, at y 258                                  | `FAINT`                                   |
| Back          | `CancelButton` 72 wide at (294, 256)                                                                 | As every `CancelButton`                   |

- `display(quest: QuestInfo)` draws it from scratch, and `hide()`.
- `QuestDetailView.Delegate.questDetailBackSelected()`.
- E.g.: the goat wizard's portrait ringed in red, the name and description beside it, Defeat the Goat Wizard alone in the banner, an empty row, then REWARD 150 G and Back.

### QuestAnnouncementView

`QuestAnnouncementView : UIContainer(Size(390, 844))`, added last in `BattleScene`, over everything, and invisible until it announces. With `enum class Moment { STARTED, COMPLETED }`.

| Part          | Size and place                                                                  | Colour                                    |
|---------------|---------------------------------------------------------------------------------|-------------------------------------------|
| Dim           | The whole 390x844, taking every tap, so the pad under it is not pressed         | Black at `DIM_ALPHA` (0.62)               |
| Card          | 330 wide, radius 14, padding 16, as tall as what it holds, centred on both axes | `PANEL`, 1-pixel `STROKE`                 |
| Target        | `targetSlot` of 72, centred, portrait at 2×                                     | Ring from `ringOf`, `ALLY` when completed |
| Target's tick | `tickDisc` of 22, over its bottom right corner, only when completed             | `ALLY`                                    |
| Title         | Size 12, centred, 12 under the target: NEW QUEST, or QUEST COMPLETED            | `SELECTION`, or `ALLY`                    |
| Name          | Size 18, centred and wrapped, 6 under the title                                 | White                                     |
| Continue      | `ConfirmButton` 298 wide, 32 tall, 16 from the bottom of the card               | As every `ConfirmButton`                  |

Between the name and Continue, 10 under the name:

| Moment      | Holds                                                                                                                                                                                                                   |
|-------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `STARTED`   | The description, size 12 in `MUTED`, centred and wrapped. 12 under it, a `PANEL_RAISED` row of 298x34, radius 8, with `taskGlyph` of 14 at (10, 10) and the task, size 13 in white at (32, 9), `fitted`. Then 14.       |
| `COMPLETED` | A `STROKE` divider 298 wide. 10 under it, REWARD, size 11 in `FAINT`. 18 under it, the gold as `rewardRow`. Then each item in a row 4 under the last: a slot of 28, and its name, size 13 in white, 8 after it. Then 8. |

- `display(moment, quest: QuestInfo)` draws it and shows it, and `hide()`.
- `QuestAnnouncementView.Delegate.questAnnouncementContinued()`.

### QuestPresenter

`QuestPresenter(questTrackerView, questDetailView, questAnnouncementView, questApi, partyApi, texts, timeline, eventBus)`, created in `BattleScene` before `ExplorationPresenter`, which takes it, as it takes `PausePresenter`.

It only follows the main quest: the one of `QuestTypeDto.MAIN` from `questApi.searchQuests`. An event naming any other quest is ignored. It maps the quest with `QuestMapper` when the event arrives, and draws it inside the step.

| Event                                     | Step, with `timeline.play`                                                                                                                                                                                |
|-------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `QuestStarted`                            | Draws the tracker, then announces `STARTED` and waits for Continue.                                                                                                                                       |
| `TaskProgressed`, `TaskCompleted`         | Nothing when the quest is completed, as `QuestCompleted` follows. When what it reads differs from what the tracker displays, draws it, and plays `pop` with `playAlongside`, so it never holds the party. |
| `QuestCompleted`                          | Draws the ticked tracker, then announces `COMPLETED` and waits for Continue.                                                                                                                              |
| `GameRestored`                            | Draws the tracker, without popping.                                                                                                                                                                       |
| `ExplorationPaused`, `ExplorationResumed` | `setTappable(false)`, and `setTappable(true)`.                                                                                                                                                            |
| `PlayerVictory`                           | Hides the tracker, and closes the detail view.                                                                                                                                                            |

- `isAnnouncing` is true from the moment a card is shown until Continue. The wait is a `CompletableDeferred`, as `InventoryPresenter.choice`, completed by `questAnnouncementContinued`. The step hides the card and clears the flag once it resumes.
- `openDetail()` displays the quest in full, read at that moment. `closeDetail()` hides it. `isDetailOpen`.
- `questDetailBackSelected` calls `partyApi.closeQuest`, once the timeline is idle, as `PausePresenter.pauseResumeSelected` does (`whenIdle`).
- `setOnTrackerSelected(listener)` hands the tap to `ExplorationPresenter`, as `ShopPresenter.setOnBack` does.

### ExplorationPresenter

- `openQuest()`: as `openPause`, it does nothing while `timeline.isPlaying` or the pad is hidden, and calls `partyApi.openQuest`. It clears no pending press, as a tap on the tracker is no press of the pad. So a tap does nothing while a dialog, the shop, the formation, the pause screen or the battle HUD has the place of the pad.
- `QuestOpened`: a step hides the pad and calls `questPresenter.openDetail()`. `QuestClosed`: a step calls `displayVirtualPad(isDisplayed = true)`.
- `displayVirtualPad` and the `PlayerVictory` step also call `questPresenter.closeDetail()`.
- `tick` returns early while `questPresenter.isAnnouncing`, starting no second. The time it skips is not counted.

### BattlefieldPresenter

`isTimeStopped()` also holds `party.isReadingQuest`, so tapping the battlefield does nothing while the detail view is open.

### BattleScene

- The top line of the vertical stack becomes `fixedSizeContainer(Size(390, 33))`, holding `battleInfoView` and `questTrackerView`.
- `questDetailView` joins the views of the slot of the pad, positioned at x 6, after `confirmDialogView`.
- `questAnnouncementView` is added after the vertical stack, so it draws over everything. It takes no room in the stack.

## Storybook

- `QuestSample`: the quests the stories display, as in the prototype. Goat wizard reads the real texts through `context.texts`, following the language global. Counted, Talk, Reach, Several tasks, Item rewards and Completed are built on it. Long text holds a long Spanish name, description and task, and 99999 gold.
- `QuestTrackerViewStories`: **Screen / Quest tracker › Goat wizard** (`screen-quest-tracker--goat-wizard`), **› Counted**, **› Talk**, **› Reach**, **› Long text**, **› Completed** and **› Read only** (`--read-only`, as in a battle). Controls: `quest` (pick of `QuestSample`) and `tappable` (boolean). `pop` is not shown, as a story draws the settled state. Tapping logs `questTrackerSelected`.
- `QuestDetailViewStories`: **Screen / Quest detail › Goat wizard** (`screen-quest-detail--goat-wizard`), **› Counted**, **› Talk**, **› Reach**, **› Several tasks**, **› Item rewards**, **› Long text** and **› Completed**. Control: `quest`. Back logs `questDetailBackSelected`.
- `QuestAnnouncementViewStories`: **Screen / Quest announcement › Started** (`screen-quest-announcement--started`), **› Completed**, **› Completed with items**, **› Started with long text** and **› Completed with long text**. Controls: `moment` (enum) and `quest`. Continue logs `questAnnouncementContinued`.
- `PillViewStories`: **Screen / Pill › Gold** (`screen-pill--gold`).
- All in `StorybookApi.STORY_BINDINGS`, after `BattleInfoViewStories`, with their ids in `StorySmokeTest`.

## Docs

- [Quests](../architecture/quests.md): a new section, What the player sees, with the tracker, the detail view and the announcements. Its first known limitation goes, and it gains the ones below.
- [Exploration](../architecture/exploration.md): a new section, Reading the quest, beside Pausing, with `READING_QUEST` and what stops. The list of what takes the place of the pad.
- [Presentation timeline](../architecture/presentation-timeline.md): the announcements as steps waiting for Continue, as a found item does, and the tracker's pop alongside.
- [Localization](../architecture/localization.md) and [Game data](../architecture/game-data.md): the names, descriptions and tasks of the quests among the game data texts.
- [Storybook](../architecture/storybook.md) lists nothing per view, so it does not change. `docs/architecture/dependencies.html` is regenerated with `./gradlew dependencyReport` if a dependency changes.

## Tests

- `PartyTest`
  - `openQuest should read the quest when the party can walk`
  - `openQuest should change nothing when the party is walking, talking, in formation, paused or battling`
  - `closeQuest should explore again when reading the quest`
  - `closeQuest should change nothing when not reading the quest`
- `OpenQuestTest`, `CloseQuestTest`
  - `should publish QuestOpened when the party can walk`
  - `should publish nothing when the party can not walk`
  - `should publish QuestClosed when reading the quest`
- `ApplyStandingTerrainEffectsTest`
  - `should start no second when the party is reading the quest`
- `QuestMapperTest`
  - `questInfo should translate the name, the description and each task when given a quest`
  - `questInfo should map the tasks before the current one as done, and none after it, when a task is under way`
  - `questInfo should map every task as done when the quest is completed`
  - `questInfo should count the defeats of a defeat task and 1 for a talk or a reach`
  - `questInfo should point to the portrait of the unit, of the npc, or to none, by the kind of task`
  - `questInfo should name the gold and each item when the reward has them`
- `QuestTrackerViewTest`
  - `display should draw the task when the quest is active`
  - `display should cut the task with an ellipsis when it is longer than 200`
  - `display should draw the count only when the task counts above 1`
  - `display should draw Quest completed and a tick when the quest is completed`
  - `setTappable should hide the chevron and keep the right edge when it is not tappable`
  - `QuestTrackerView should call its delegate when the capsule is tapped`
- `QuestDetailViewTest`
  - `display should draw the count and the bar only when the task counts above 1`
  - `display should cut the description to the lines beside the target when it is long`
  - `display should draw the tasks done only while the quest is active`
  - `display should draw QUEST COMPLETED and a ticked target when the quest is completed`
  - `QuestDetailView should call its delegate when Back is tapped`
- `QuestAnnouncementViewTest`
  - `display should draw the description and the task when the quest started`
  - `display should draw the gold and each item with its name when the quest completed`
  - `display should centre the card when it is taller with long texts`
  - `QuestAnnouncementView should call its delegate when Continue is tapped`
- `QuestPresenterTest`
  - `QuestPresenter should draw the tracker and wait for Continue when the main quest starts`
  - `QuestPresenter should ignore an event when it names a side quest`
  - `QuestPresenter should pop the tracker once when a defeat completes a task with another one after it`
  - `QuestPresenter should announce the completion once when the last defeat publishes three events`
  - `QuestPresenter should draw the tracker without announcing when a game is restored`
  - `QuestPresenter should be announcing until Continue is tapped`
  - `QuestPresenter should make the tracker read only when a battle starts, and tappable again when it finishes`
  - `QuestPresenter should hide the tracker when a player wins`
- `ExplorationPresenterTest`
  - `ExplorationPresenter should open the quest when the tracker is tapped while the pad is displayed`
  - `ExplorationPresenter should not open the quest when the tracker is tapped while the timeline is playing, or the pad is hidden`
  - `ExplorationPresenter should display the pad when the quest is closed`
  - `ExplorationPresenter should start no second while announcing`
- `BattlefieldPresenterTest`
  - `BattlefieldPresenter should ignore a tile selected when the party is reading the quest`
- `LocalizationResourcesTest`
  - `quest` joins `gameDataFields` with `name` and `description`, so its existing tests cover them.
  - `should have a text in the source catalog for every task of every quest`
- `QuestEndToEndTest`
  - `should announce the main quest and display the tracker when a new game starts`
  - `should not walk the party when an arrow is pressed under the card`
  - `should open the detail view in place of the pad when the tracker is tapped, and display the pad again with Back`
  - `should announce the reward and tick the tracker when the goat wizard is defeated`
  - `should not announce and should display the tracker when a game is loaded`
- `EndToEndExtension.startNewGame` taps Continue on the start card, so the end-to-end tests already written go on as they do. `BattleSceneExtension` gains `questTrackerView`, `questDetailView` and `questAnnouncementView`, and each view its `<View>Extension`.

## Known limitations

- The card shows the reward the quest defines, so a leader near the cap of 99,999 gold reads 150 G and receives less, and an item that did not fit the bag drops on its tile without a word. The shipped quest pays gold, and only a leader past 99,849 gold would notice.
- The pop of the tracker was not seen in the prototypes, whose stories draw the settled state, so its 1.15 and 400ms are a first guess, to check by playing.
- A long task beside a long turn name may touch while the turn pops to 1.3 on the left of the line. E.g.: the 252 of the longest tracker leaves 132 for it. The tracker draws on top, and the pop lasts 400ms.
- A long description is cut with an ellipsis beside the target, at 4 lines with a name of one line and 3 with two. The detail view has no scrolling, and the shipped description takes 2.
- A side quest is played and paid with nothing on the screen, as before.
