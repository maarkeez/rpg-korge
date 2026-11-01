# Plan: The side quests on the screen, with a journal, a pinned quest, quest markers and a toast

> **Status:** done.
>
> **Summary:** The [quest system](quest-system.md) plays side quests with nothing on the screen, and the game ships none. `QuestPresenter` ignores any quest but the main one, so a talk with a quest giver would start a quest without a word, a defeated rat would count silently, and 40 gold would land in the leader's bag unannounced. `QuestApi.abandonQuest` has no way to be called. And nothing says which npc has something to give. Instead, the tracker names the quest the player pins, tapping it opens a journal of every quest started, each quest opens in a detail view with Abandon in red, a quest marker over an npc's head says who to talk to, and a toast announces every quest starting and completing, the main quest too, replacing its card. Three real side quests ship with it. Chosen among the Storybook prototypes of the local branch `prototype/side-quest-ui` (`03f55daf`): tracker C, journal B with scrolling instead of pages, detail view B with its buttons swapped, announcement B and marker A. It builds on [Quests](../architecture/quests.md), [Exploration](../architecture/exploration.md), the [Presentation timeline](../architecture/presentation-timeline.md), [Save and load](../architecture/save-and-load.md) and the [main quest on the screen](main-quest-ui.md), three of whose decisions it replaces.

## Domain terms

| Term            | Meaning                                                                     | In the code                                                 |
|-----------------|-----------------------------------------------------------------------------|-------------------------------------------------------------|
| Journal         | The list of every quest started, in place of the pad                        | `QuestJournalView`                                          |
| Pinned quest    | The quest the tracker names: the main quest, unless the player pins another | `Quest.Dto.ProgressDto.isPinned`, `PinQuest`, `QuestPinned` |
| Quest marker    | The sign over an npc's head on the battlefield                              | `QuestMarkerView`, `QuestMarkerView.Sign`                   |
| Quest to start  | The gold sign: the npc can start a side quest                               | `Sign.QUEST_TO_START`                                       |
| Talk task       | The green sign: a current task is to talk to the npc                        | `Sign.TALK_TASK`                                            |
| Repeatable pill | The pill saying that a side quest can run again                             | `QuestViewParts.repeatablePill`                             |
| Announcement    | Still the term for a quest starting or completing, now drawn as a toast     | `QuestAnnouncementView`                                     |
| Danger          | The red of an action that can not be undone                                 | `HudStyle.DANGER`                                           |

Side quest, quest giver, abandon, repeatable, tracker and detail view keep their meaning from [Quests](../architecture/quests.md). The texts on the screen are Journal (Diario), SIDE QUEST (MISIÓN SECUNDARIA), Repeatable (Repetible) and Abandon (Abandonar).

## Decisions

| Topic                  | Decision                                                                                                                                                                                                                                                                                                                                                                              |
|------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope                  | The tracker, journal, detail view, abandoning, announcements and quest markers, for side quests and the main quest, in the game, with their stories and texts, and three real side quests. E.g.: talking to the Farmer starts Bats in the Meadow, announced by a toast, and it can be pinned and abandoned from the journal.                                                          |
| Tracker                | Variant C: it names the pinned quest. Chosen over the main quest with a badge counting the side quests, which never shows a side quest's task, and over the quest that progressed last, which changes by itself mid-fight. It stays in the same place, read only in a battle.                                                                                                         |
| Pinning                | Exactly one quest is pinned: the main quest by default. Tapping the pin of another active quest moves it there, and tapping the gold pin returns it to the main quest. So is completing or abandoning the pinned quest. Chosen over a pin that can be empty, and over one that stays on a completed quest, which then shows nothing useful.                                           |
| New side quests        | A quest starting does not take the pin, so a repeatable one can not keep taking over the tracker. The toast announces it.                                                                                                                                                                                                                                                             |
| Main quest completed   | The tracker stays ticked as Quest completed while no side quest is pinned, as [today](main-quest-ui.md).                                                                                                                                                                                                                                                                              |
| Progress of the others | A count or a task done of a quest that is not pinned only pops the tracker, with no text change and no toast, the rule the main quest already follows. Chosen over a toast per count, which adds noise over a battle.                                                                                                                                                                 |
| Journal                | Variant B: rows 66 tall with the target's portrait, in place of the pad, in the 300 of its slot, so the battlefield stays in sight. Chosen over slim rows without the portrait, and over a panel filling the screen, which hides the battlefield.                                                                                                                                     |
| Journal overflow       | Any number of quests, dragged to scroll, as the list of the Storybook. It replaces the pages of the prototype: a drag does not select a row, and it keeps its place when coming back from a detail view.                                                                                                                                                                              |
| Journal content        | The main quest first while active, then the active side quests, then the completed quests ticked, each group in the order the quests were created, so no save has to hold when each started. Side quests not started are hidden, so nothing is spoiled, and an abandoned one leaves the list. Each row names its section (MAIN QUEST, SIDE QUEST, COMPLETED): there are no headings.  |
| Opening                | Only by tapping the tracker, as today. It opens the journal once a side quest is listed, and otherwise the detail view of the main quest, so there is no list of one row. Back steps out one level at a time. Chosen over an entry in the pause menu, which needs a second way in.                                                                                                    |
| Time                   | It stops while the journal, the detail view or the question to abandon is open: all of them are the existing `READING_QUEST`, opened by `QuestOpened` and closed by `QuestClosed`.                                                                                                                                                                                                    |
| Detail view            | Variant B, for every quest, the main quest too, so it replaces its shipped layout: the reward moves up under the task, and the bottom row only holds the buttons. Chosen over the layout of today, where a reward of gold and four items slides under Abandon.                                                                                                                        |
| Buttons                | Back on the left and Abandon on the right, in the detail view, and Back on the left in the journal too, so it does not jump when stepping out. Abandon is only on an active side quest.                                                                                                                                                                                               |
| Abandoning             | Abandon asks first, in `ConfirmDialogView`, as every action that can not be undone: Abandon Bats in the Meadow? Its progress is lost, and its giver can start it again. Back returns to the detail view. Once abandoned, the player returns to the journal when a side quest is still listed, and otherwise to the pad. No toast.                                                     |
| The red                | `HudStyle.DANGER` is `#D32F2F`, 5.0:1 with white text, on Abandon in the detail view and on the action of every `ConfirmDialogView`, so red means it can not be undone: Abandon, Quit to the menu, overwriting a save and deleting a save. Chosen over `#FF375F`, the red of health and enemies, 3.5:1, which already means something else, and `#9B1C2E`, 8.1:1, the least alarming. |
| Repeatable pill        | On the journal row and in the detail view, as a repeatable giver stays marked after the quest completes, and something has to say why.                                                                                                                                                                                                                                                |
| Announcements          | Variant B, for every quest: a toast over the top of the battlefield, which fades by itself and takes no taps. Chosen over the dimmed card, which a repeatable quest would bring up again and again, and a banner held until tapped. It announces every start and completion, also of a repeat.                                                                                        |
| Toast timing           | Held 4 seconds, then fading in 0.5 seconds, enough to read a name and a task. Toasts play one after the other, e.g.: a talk starting two quests takes 9 seconds. It does not hold the timeline.                                                                                                                                                                                       |
| Main quest's card      | Removed. This replaces three decisions of the [main quest plan](main-quest-ui.md): time no longer stops for the announcement, the timeline no longer waits for Continue, and the start no longer stops the player to tell the goal. The tracker still names the task. A loaded game still announces nothing.                                                                          |
| Quest marker           | Variant A: an icon above the head, gold with an exclamation mark for a quest to start and green with a speech bubble for a talk task. Chosen over a ring, which the sprites hide as they fill the tile, and a badge on the sprite's corner.                                                                                                                                           |
| Marker rules           | One marker per npc, the talk task winning over a quest to start, as the talk does both. Only while exploring, as an npc can only be talked to then. A repeatable giver stays marked whenever its quest can start. No arrow to an npc off the screen, as the [main quest plan](main-quest-ui.md) decided.                                                                              |
| Starting               | Still automatic as the talk ends, with no choice to accept or decline, as Abandon undoes it.                                                                                                                                                                                                                                                                                          |
| Saving                 | `isPinned` is saved with each quest, so `SAVE_VERSION` goes to 4 with its golden fixture, and earlier saves become incompatible. The journal's scroll and what is open are not saved, as saving is only possible while `PAUSED`.                                                                                                                                                      |
| Side quests shipped    | Bats in the Meadow, The Elder's Errand and Rats in the Fields. See [Resources](#resources). Between them they use each kind of task, a count, a task done, gold and an item, and a repeatable quest, so each part of the screen is reachable in play.                                                                                                                                 |
| Inherited              | Later tasks stay hidden, a count and bar only appear above 1, the tracker is read only in a battle and hidden once a player wins, and pixel art is only for portraits and items, the rest being vector chrome from `HudStyle`.                                                                                                                                                        |
| Architecture           | Pinning and the markers in `quest`, as `Quest.pin`, `PinQuest`, `QuestPinned` and `SearchQuestMarkers`. All the rest in `screen.adapters.presentation`. The views take `QuestInfo`, which `QuestMapper` builds from `Quest.Dto`, so they import no domain. `party` does not change.                                                                                                   |
| Prototype              | `prototype/side-quest-ui` is a reference and is never merged. The views are rebuilt on `main` with the chosen variants alone: no `SideQuestVariant`, `AbandonRed` nor `RepeatablePill`, hard-coded texts go through `Texts`, the sample quests stay in stories, and `SideQuestScreenView`, which only composed the stories, is left out.                                              |
| Storybook              | Bindings for the two new views, and new stories for the others. See [Storybook](#storybook).                                                                                                                                                                                                                                                                                          |
| Tests                  | Domain, use case, view, presenter and save tests, the catalogs in `LocalizationResourcesTest`, `QuestResourcesTest` over the new quests, and `QuestEndToEndTest` playing a side quest. See [Tests](#tests).                                                                                                                                                                           |
| Out of scope           | Accepting or declining a quest, a line of dialog from the giver about its quest, an arrow to a quest, sorting or filtering the journal, the note that a reward did not fit the bag, and sound.                                                                                                                                                                                        |
| Delivery               | 6 milestones, each green on CI, the real side quests last so players never meet a half-finished screen. See [Milestones](#milestones).                                                                                                                                                                                                                                                |

For example, a new game, then Bats in the Meadow. Every toast appears once the timeline reaches its event, as the [timeline](../architecture/presentation-timeline.md#rules) requires:

| When                           | Published                                          | Displayed                                                                                                     | Time     |
|--------------------------------|----------------------------------------------------|---------------------------------------------------------------------------------------------------------------|----------|
| The new game is set up         | `QuestStarted`                                     | The tracker reads Defeat the Goat Wizard, a toast announces the quest for 4.5 seconds, and the markers appear | Runs     |
| The talk with the Farmer ends  | `NpcTalkFinished`, `QuestStarted`                  | A toast announces Bats in the Meadow, and the Farmer's gold marker goes                                       | Runs     |
| The tracker is tapped          | `QuestOpened`                                      | The journal takes the place of the pad, with two rows                                                         | Stopped  |
| The pin of the quest is tapped | `QuestPinned`                                      | Its pin turns gold, and the tracker reads Defeat 3 Bats 0/3 with a pin                                        | Stopped  |
| Back is tapped                 | `QuestClosed`                                      | The pad is displayed again                                                                                    | Runs     |
| A bat is defeated              | `TaskProgressed`                                   | The tracker reads Defeat 3 Bats 1/3, and pops                                                                 | In turns |
| The third bat is defeated      | `TaskProgressed`, `TaskCompleted`                  | The tracker reads Talk to the Farmer, and pops once. The Farmer gets a green marker when the battle ends      | In turns |
| The talk with the Farmer ends  | `TaskCompleted`, `QuestCompleted`, `RewardGranted` | A toast announces 40 G, the marker goes, and the tracker returns to the main quest                            | Runs     |

## Milestones

Each milestone is a few commits straight on `main`, green on CI.

| #  | Milestone         | Done when                                                                                                                                    | Status | Done in |
|----|-------------------|----------------------------------------------------------------------------------------------------------------------------------------------|--------|---------|
| M1 | The toast         | The main quest is announced by a toast that fades with time running, and the card is gone. Any quest starting or completing gets one.        | ✅      | `c749d674` |
| M2 | The journal       | Once a side quest is listed, tapping the tracker opens the journal, dragged to scroll, each row opening the new detail view. Back steps out. | ✅      | `6af72e0f` |
| M3 | Abandoning        | Abandon in red asks first, and takes the side quest back to not started, returning to the journal or the pad.                                | ✅      | `51694f3f` |
| M4 | The pinned quest  | Pinning a quest in the journal makes the tracker name it, also in a loaded game, with `SAVE_VERSION` 4.                                      | ✅      | `566a5cf3` |
| M5 | The quest markers | An npc with a quest to start or a talk task has its sign while exploring, also in a loaded game.                                             | ✅      | `19020c2d` |
| M6 | The side quests   | The three quests can be played from a new game, proved end to end. The plan is marked as done.                                               | ✅      | `2c887c46` |

### M1 commits

1. Turn `QuestAnnouncementView` into the toast, with its stories, dropping `QuestAnnouncementView.Delegate`, `UiText.QUEST_ANNOUNCEMENT_REWARD` and `QUEST_ANNOUNCEMENT_CONTINUE`.
2. Announce every quest: `QuestPresenter` plays the toasts one after the other alongside the timeline, drops `isAnnouncing` and `pendingContinue`, `ExplorationPresenter.tick` no longer checks it, `startNewGame` no longer taps Continue, and describe it in Quests and the Presentation timeline.

### M2 commits

1. Name what the views need: `QuestInfo.isMain`, `isRepeatable` and `isPinned`, built by `QuestMapper`, `QuestViewParts.repeatablePill`, and the texts `QUEST_DETAIL_SIDE_QUEST`, `QUEST_REPEATABLE`, `QUEST_JOURNAL_TITLE`, `QUEST_JOURNAL_ACTIVE` and `QUEST_JOURNAL_COMPLETED`.
2. Lay out `QuestDetailView` as variant B for every quest, with its caption and pill, Back on the left, with its stories.
3. Add `QuestJournalView` with its stories, opened by the tracker through `QuestPresenter.openQuest` once a side quest is listed, with Back stepping out, and describe it in Quests and Exploration.

### M3 commits

1. Add `HudStyle.DANGER` and `DANGER_PRESSED`, the colours of `ConfirmButton`, and draw the action of `ConfirmDialogView` in them, for Quit to the menu and the saves too, with its story.
2. Abandon a side quest from its detail view: the button, the question through `QuestPresenter`, and where it returns to, with `QUEST_ABANDON` and `QUEST_ABANDON_QUESTION`, and describe it in Quests.

### M4 commits

1. Pin a quest: `Quest.pin`, `unpin`, `ProgressDto.isPinned`, `PinQuest`, `QuestPinned`, unpinning as it completes or is abandoned, and `isPinned` in `QuestSnapshot`, taking `SAVE_VERSION` to 4 with its golden fixture, and describe it in Quests and Save and load.
2. Name the pinned quest: `QuestViewParts.pinGlyph`, the pin of `QuestJournalView` and of `QuestTrackerView`, and `QuestPresenter` following `QuestPinned`, popping for the quests it does not name.

### M5 commits

1. List the markers: `Quest.markerFor`, `SearchQuestMarkers` and `QuestMarkerDto`.
2. Add `QuestMarkerView` with its stories, `BattlefieldView.displayQuestMarker` and `hideQuestMarker`, and `QuestPresenter` keeping them up to date while exploring, and describe them in Quests and Exploration.

### M6 commits

1. Ship Bats in the Meadow, The Elder's Errand and Rats in the Fields, with their texts in English and Spanish, and play one in `QuestEndToEndTest`. Remove the known limitation from Quests, and mark the plan as done.

## Reference

### quest

- `ProgressDto.isPinned`: whether the player pinned the quest. False by default, so the main quest needs no flag to be the one named.
- `Quest.pin()` sets it on an active quest, publishing `QuestPinned(questId)`. It fails with `QuestNotActive` otherwise. `Quest.unpin()` clears it, and publishes nothing.
- `Quest.abandon()` and completing the last task also clear it.
- `PinQuest(questId)` unpins every other quest, then pins this one. Pinning the main quest, or a quest already pinned, clears every pin and publishes `QuestPinned` with the main quest's id. E.g.: tapping the gold pin of Bats in the Meadow returns the tracker to the main quest.
- `Quest.markerFor(npcId)`: `TALK_TASK` when it is active and its current task is to talk to that npc, `QUEST_TO_START` when `canBeStartedBy(npcId)`, or nothing.
- `SearchQuestMarkers()` returns a `QuestMarkerDto(npcId, sign)` per npc any quest names, the talk task winning. `QuestApi` exposes it and `pinQuest`.
- `QuestSnapshot` gains `isPinned`, and `Save.SAVE_VERSION` goes to 4, with `src/jvmTest/fixtures/save/v4.json`.

### Resources

Each is a `resources/quest/<quest id>.toml`, with the texts of `quest.<quest id>.*` in `messages.pot` and `es.po`.

| Quest id             | Giver  | Tasks                                                  | Reward                   | Repeatable |
|----------------------|--------|--------------------------------------------------------|--------------------------|------------|
| `bats-in-the-meadow` | Farmer | `DEFEAT_UNITS` bat, 3. `TALK_TO_NPC` farmer            | 40 gold                  | No         |
| `the-elders-errand`  | Elder  | `REACH_TILES` (25, 3) and (25, 4). `TALK_TO_NPC` elder | 25 gold, `health-potion` | No         |
| `rats-in-the-fields` | Elf    | `DEFEAT_UNITS` rat, 2                                  | 20 gold                  | Yes        |

- Rows 24 and 25 only have columns 3 and 4, so nobody reaches the southern fields without touching one of those two tiles.
- Rats respawn after `RESPAWN_STEPS` (50), so the repeatable quest stays playable. See [Respawning](../architecture/cpu-battle-brain.md#respawning).

| Key                                    | English                                                                                            | Spanish                                                                                                      |
|----------------------------------------|----------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------|
| `quest.bats-in-the-meadow.name`        | Bats in the Meadow                                                                                 | Murciélagos en el prado                                                                                      |
| `quest.bats-in-the-meadow.description` | Bats nest in the meadow near the farm, and the Farmer wants them gone.                             | Los murciélagos anidan en el prado cerca de la granja, y el Granjero quiere que se vayan.                    |
| `quest.bats-in-the-meadow.task.0`      | Defeat 3 Bats                                                                                      | Derrota 3 Murciélagos                                                                                        |
| `quest.bats-in-the-meadow.task.1`      | Talk to the Farmer                                                                                 | Habla con el Granjero                                                                                        |
| `quest.the-elders-errand.name`         | The Elder's Errand                                                                                 | El encargo del Anciano                                                                                       |
| `quest.the-elders-errand.description`  | The Elder wants to know whether the path to the southern fields is still open.                     | El Anciano quiere saber si el paso a los campos del sur sigue abierto.                                       |
| `quest.the-elders-errand.task.0`       | Reach the southern fields                                                                          | Llega a los campos del sur                                                                                   |
| `quest.the-elders-errand.task.1`       | Talk to the Elder                                                                                  | Habla con el Anciano                                                                                         |
| `quest.rats-in-the-fields.name`        | Rats in the Fields                                                                                 | Ratas en los campos                                                                                          |
| `quest.rats-in-the-fields.description` | Rats keep coming back to the southern fields. The Elf pays to thin them out, as often as it takes. | Las ratas siempre vuelven a los campos del sur. La Elfa paga por ahuyentarlas, tantas veces como haga falta. |
| `quest.rats-in-the-fields.task.0`      | Defeat 2 Rats                                                                                      | Derrota 2 Ratas                                                                                              |

### Texts

| `UiText`                  | Key                         | English                                                                  | Spanish                                                                             |
|---------------------------|-----------------------------|--------------------------------------------------------------------------|-------------------------------------------------------------------------------------|
| `QUEST_DETAIL_SIDE_QUEST` | `ui.questDetail.sideQuest`  | SIDE QUEST                                                               | MISIÓN SECUNDARIA                                                                   |
| `QUEST_REPEATABLE`        | `ui.quest.repeatable`       | Repeatable                                                               | Repetible                                                                           |
| `QUEST_JOURNAL_TITLE`     | `ui.questJournal.title`     | Journal                                                                  | Diario                                                                              |
| `QUEST_JOURNAL_ACTIVE`    | `ui.questJournal.active`    | {quests} active, plural                                                  | {quests} activa, {quests} activas                                                   |
| `QUEST_JOURNAL_COMPLETED` | `ui.questJournal.completed` | COMPLETED                                                                | COMPLETADA                                                                          |
| `QUEST_ABANDON`           | `ui.quest.abandon`          | Abandon                                                                  | Abandonar                                                                           |
| `QUEST_ABANDON_QUESTION`  | `ui.quest.abandonQuestion`  | Abandon {quest}? Its progress is lost, and its giver can start it again. | ¿Abandonar {quest}? Se pierde su progreso, y quien la dio puede empezarla de nuevo. |

`QUEST_ANNOUNCEMENT_REWARD` and `QUEST_ANNOUNCEMENT_CONTINUE` are removed from `UiText` and both catalogs.

### QuestInfo and QuestMapper

- `QuestInfo` gains `id`, `isMain`, `isRepeatable` and `isPinned`.
- `QuestMapper.questInfo` fills them from `Quest.Dto`. The main quest is pinned when no other quest is, so its pin is gold by default.

### QuestViewParts and HudStyle

- `repeatablePill(x, y)`: a `PillView` in `HudStyle.MANA`, text size 10, returning its width.
- `pinGlyph(x, y, side, color)`: a head, a bar and a point as three rectangles, as a circle blurs at 13.
- `exclamationGlyph(x, y, side, color)`: a bar and a dot.
- `HudStyle.DANGER` is `RGBA(211, 47, 47)`, and `DANGER_PRESSED` `RGBA(183, 35, 35)`.
- `ConfirmButton` takes `color` and `pressedColor`, by default its blue. `CancelAndConfirmView` takes them too. `ConfirmDialogView` always draws its action in `DANGER`, as it is only used before what can not be undone: `PausePresenter` for Quit to the menu and overwriting a save, `LoadGamePresenter` for deleting a save, and `QuestPresenter` for Abandon. Every other `ConfirmButton` stays blue.

### QuestTrackerView

- `display(quest: QuestInfo)` draws the quest it is given: `QuestPresenter` chooses the pinned one.
- `pinGlyph` of 13 in `SELECTION`, 10 from the left of the capsule and 3 before the task's glyph, only while a side quest is pinned. The capsule is as wide as what it holds, with its right edge at x 384 as today.
- `pop()` also plays with what is on show, for a quest it does not name.

### QuestJournalView

`QuestJournalView : Container`, 378x300, at `QUEST_DETAIL_X` of the slot of the pad, on top of the views there. A `HudStyle.panel` as its background.

| Part      | Size and place                                                                           | Colour                                    |
|-----------|------------------------------------------------------------------------------------------|-------------------------------------------|
| Title     | Journal, size 16, at (12, 9)                                                             | White                                     |
| Active    | N active, size 12, right-aligned to x 366, y 12                                          | `FAINT`                                   |
| List      | From y 34, 214 tall, dragged to scroll                                                   |                                           |
| Row       | 354x66 at x 12, 4 apart, panel with `SLOT_RADIUS`                                        | `PANEL_RAISED`                            |
| Target    | `targetSlot` of 50 at (8, 8) of the row, portrait at 1×, a reach's flag at half the side | Ring from `ringOf`, `ALLY` once completed |
| Tick      | `tickDisc` of 18, over the bottom right corner of the target, only once completed        | `ALLY`                                    |
| Section   | Size 10, at (68, 6) of the row: MAIN QUEST, SIDE QUEST or COMPLETED                      | `FAINT`                                   |
| Name      | Size 14, at (68, 19) of the row, `fitted` up to what ends the row                        | White, `MUTED` once completed             |
| Task      | Size 12, at (68, 36) of the row, `fitted`: the current one, or Quest completed           | `MUTED`                                   |
| Bar       | `progressBar` 5 tall at (68, 53) of the row, only when it counts above 1                 | `SELECTION`                               |
| Row's end | From the right, 8 apart: chevron of 10, count size 13, Repeatable pill, pin of 14        | `MUTED`, `SELECTION`, `MANA`              |
| Pin       | Only on an active quest. It takes its own taps in the 32 at the end of its place         | `SELECTION` when pinned, or else `FAINT`  |
| Scrollbar | 3 wide at x 370, as long as the part in sight, only when it does not all fit             | `STROKE`                                  |
| Divider   | 354x1 at (12, 248)                                                                       | `STROKE`                                  |
| Back      | `CancelButton` 72 wide at (12, 256)                                                      | As every `CancelButton`                   |

- `display(quests: List<QuestInfo>)` draws them in the order given, keeping the scroll, up to what still fits.
- It scrolls as `SidebarView` does, with `onMouseDrag` and `localDXY`. A drag above 4 points does not select the row under it.
- KorGE's `clipContainer` does not clip rounded rectangles, so the rows out of the list are hidden, and the header and the footer are drawn over the others, covering only the width of the rows.
- `QuestJournalView.Delegate`: `questJournalSelected(questId)`, `questJournalPinSelected(questId)` and `questJournalBackSelected()`.

### QuestDetailView

The layout of variant B, for every quest.

| Part       | Size and place                                                           | Colour                            |
|------------|--------------------------------------------------------------------------|-----------------------------------|
| Caption    | Size 11, at (128, 10): MAIN QUEST, SIDE QUEST or QUEST COMPLETED         | `FAINT`, or `ALLY` once completed |
| Pill       | `repeatablePill` 8 after the caption, at y 7, only on a repeatable quest | `MANA`                            |
| Task       | Banner 354x52 at (12, 124). A counted task's bar at y 158                | `PANEL_RAISED`                    |
| Tasks done | `tickDisc` of 12 at (14, 183) and N tasks done, size 14 at x 32          | `ALLY`, `MUTED`                   |
| Reward     | REWARD, size 11 at (12, 215), then `rewardRow` 8 after it, at y 208      | `FAINT`                           |
| Divider    | 354x1 at (12, 248)                                                       | `STROKE`                          |
| Back       | `CancelButton` 72 wide at (12, 256)                                      | As every `CancelButton`           |
| Abandon    | `ConfirmButton` 110 wide at (256, 256), only on an active side quest     | `DANGER`, `DANGER_PRESSED`        |

The target, name and description stay as they are. `QuestDetailView.Delegate` gains `questDetailAbandonSelected()`.

### QuestAnnouncementView

`QuestAnnouncementView : UIContainer(Size(390, 844))`, added last in `BattleScene`, invisible until it announces. It takes no taps, so whatever is under it can be pressed. `Moment` stays.

| Part   | Size and place                                                                         | Colour                                             |
|--------|----------------------------------------------------------------------------------------|----------------------------------------------------|
| Toast  | 366x76 at (12, 54), radius 12                                                          | `PANEL`, stroke `SELECTION`, `ALLY` once completed |
| Target | `targetSlot` of 56 at (10, 10) of the toast, portrait at 1×, tick of 18 once completed | Ring from `ringOf`, `ALLY` once completed          |
| Title  | Size 11, at (76, 8): NEW QUEST or QUEST COMPLETED                                      | `SELECTION`, `ALLY`                                |
| Name   | Size 15, at (76, 22), `fitted` to 280                                                  | White                                              |
| Goal   | When it starts: `taskGlyph` of 14 at (76, 49), the task, size 13 at x 96, `fitted`     | `SELECTION`, `MUTED`                               |
| Reward | When it completes: `rewardRow` at (76, 42)                                             |                                                    |

`suspend fun announce(moment, quest)` draws it, holds `HOLD_DURATION` (4s), fades in `FADE_DURATION` (500ms) and hides it.

### QuestMarkerView

`QuestMarkerView(sign) : Container`, in the units of a tile of 64, its sign drawn over the sprite and over the tile above.

| Part    | Size and place                                     | Colour                                 |
|---------|----------------------------------------------------|----------------------------------------|
| Pointer | 10x10, rotated 45°, under the disc, at x 32        | `SELECTION`, or `ALLY` for a talk task |
| Disc    | 24 across, at x 20, y -14, with a 1.5 black stroke | `SELECTION`, or `ALLY` for a talk task |
| Sign    | Exclamation mark or `taskGlyph` to talk, 14 across | Black                                  |

- `BattlefieldView.displayQuestMarker(row, column, sign)` adds it to the tile as a new layer, `QUEST_MARKER`, in `KEPT_LAYERS`, replacing any other on the tile. `hideQuestMarker(row, column)` removes it.

### QuestPresenter

It takes `QuestJournalView`, `ConfirmDialogView`, `BattlefieldView` and `NpcApi` too.

- **Announcing**: `QuestStarted` and `QuestCompleted` of any quest play a step adding a toast to a queue, played one at a time with `timeline.playAlongside`, so they never hold the timeline. A quest is read as its event arrives. `isAnnouncing`, `pendingContinue` and `questAnnouncementContinued` are gone.
- **The tracker**: it displays the pinned quest, or else the main quest. `TaskProgressed` and `TaskCompleted` of that quest draw and pop when what it reads differs. Of another quest, they only pop when that quest's info differs from the last seen, so a defeat completing a task pops once. `QuestPinned`, `QuestCompleted` and `QuestAbandoned` draw it with no pop.
- **Opening**: `openQuest()`, replacing `openDetail()`, opens `QuestJournalView` when a side quest is listed, and otherwise the detail view of the main quest. `closeQuest()`, replacing `closeDetail()`, hides whichever is open, and `isDetailOpen` becomes `isQuestOpen`.
- **Stepping out**: Back in a detail view opened from the journal displays the journal again, as it was, publishing nothing. Back in a detail view opened directly, or in the journal, calls `partyApi.closeQuest()`.
- **Abandoning**: Abandon displays `ConfirmDialogView`. Back displays the detail view again. Abandon calls `questApi.abandonQuest`, then displays the journal when a side quest is still listed, and otherwise calls `partyApi.closeQuest()`.
- **The markers**: `SearchQuestMarkers` is read on `QuestStarted`, `TaskCompleted`, `QuestCompleted`, `QuestAbandoned`, `GameRestored` and `ExplorationResumed`, and each marker is drawn on the tile of its npc, from `npcApi.searchNpcById`, removing those gone. An npc no longer on the battlefield, e.g.: once recruited, has none. `ExplorationPaused` removes them all. A loaded game in a battle draws none until it resumes.

`ExplorationPresenter` opens and closes through `openQuest` and `closeQuest`, and `tick` no longer checks `isAnnouncing`. `BattleScene` adds `questJournalView` beside `questDetailView`, and hands the views over.

## Storybook

- `QuestJournalViewStories`: `screen-quest-journal`, with `screen-quest-journal--two-quests`, `--six-quests`, `--twelve-quests`, `--twelve-quests-scrolled` and `--long-text`, its `count` (int, 2 to 12) and `scroll` (int, 0 to 1000) controls. Sample quests in `QuestSample`.
- `QuestMarkerViewStories`: `screen-quest-marker`, with `screen-quest-marker--quest-to-start` and `--talk-task`, on the tiny map, its `sign` control (enum).
- `QuestDetailViewStories`: **Screen / Quest detail › Side quest**, **Repeatable**, **Completed, repeatable** and **Large reward**, added to the stories it has, whose ids stay.
- `QuestTrackerViewStories`: **Screen / Quest tracker › Pinned**, and **Long text** with the pin.
- `QuestAnnouncementViewStories`: the five stories keep their ids, and draw the toast, held so it can be looked at. **Screen / Quest announcement › Large reward** is added.
- `ConfirmDialogViewStories`: **Screen / Confirm dialog › Abandon** is added, and its stories draw the action in `DANGER`.
- `StorySmokeTest` lists the new stories.

## Docs

- [Quests](../architecture/quests.md): the journal, the pinned quest, abandoning, the toast and the quest markers under What the player sees, the shipped side quests, its terms, and the known limitation removed.
- [Exploration](../architecture/exploration.md): tapping the tracker opens the journal or the detail view, and the markers while exploring.
- [Presentation timeline](../architecture/presentation-timeline.md): Announcing the main quest becomes announcing a quest, alongside the timeline.
- [Save and load](../architecture/save-and-load.md): `isPinned`, and `SAVE_VERSION` 4.
- [Game data](../architecture/game-data.md): the shipped side quests as examples.

## Tests

- `QuestTest`
  - `Pin should mark the quest and publish QuestPinned when it is active`
  - `Pin should fail with QuestNotActive when it is not active`
  - `Abandon should unpin the quest when it is pinned`
  - `Defeated should unpin the quest when its last task is completed`
  - `MarkerFor should be a talk task when the current task is to talk to the npc`
  - `MarkerFor should be a quest to start when the npc can start it`
  - `MarkerFor should be a quest to start when a repeatable quest is completed`
- `PinQuestTest`
  - `PinQuest should unpin the other quest when another one is pinned`
  - `PinQuest should clear every pin when it is the pinned quest`
  - `PinQuest should clear every pin when it is the main quest`
- `SearchQuestMarkersTest`
  - `SearchQuestMarkers should prefer a talk task when an npc has both`
  - `SearchQuestMarkers should list no npc when nothing asks for it`
- `QuestSnapshottableTest`
  - `Restore should pin the quest when it was saved pinned`
- `SaveFixtureTest`: `v4.json` encodes and loads back.
- `QuestResourcesTest`: it covers the new quests with the tests it has.
- `QuestMapperTest`
  - `QuestInfo should pin the main quest when no other quest is pinned`
  - `QuestInfo should not pin the main quest when a side quest is pinned`
- `QuestJournalViewTest`
  - `Display should order the quests as given, each naming its section`
  - `Display should show no pin when the quest is completed`
  - `Display should show the pill when the quest is repeatable`
  - `Drag should scroll the list and not select a row when it moves above the threshold`
  - `Display should hide the rows out of the list when they do not all fit`
  - `Display should keep the scroll when it draws again`
- `QuestDetailViewTest`
  - `Display should show Abandon when it is an active side quest`
  - `Display should hide Abandon when it is the main quest or a completed one`
  - `Display should draw the reward above the buttons when it is a large reward`
  - `Display should show the pill when it is repeatable`
- `QuestTrackerViewTest`
  - `Display should draw the pin when a side quest is pinned`
- `QuestAnnouncementViewTest`
  - `Announce should hide the toast when it has been held and faded`
  - `Announce should take no taps when it is displayed` (replaces its tests of the dim and Continue)
- `QuestMarkerViewTest`
  - `QuestMarkerView should draw the sign of a talk task in green`
- `BattlefieldViewTest`
  - `DisplayQuestMarker should replace the marker when the tile has one`
  - `DisplayQuestMarker should keep the marker when its tile leaves the window`
- `ConfirmDialogViewTest`
  - `Display should draw the action in danger`
- `QuestPresenterTest`
  - `QuestPresenter should announce a side quest when it starts and when it completes`
  - `QuestPresenter should play the toasts one after the other when a talk starts two quests`
  - `QuestPresenter should not hold the timeline when it announces` (replaces `QuestPresenter should be announcing until Continue is tapped`)
  - `QuestPresenter should only pop the tracker when a quest it does not name progresses` (replaces `QuestPresenter should ignore an event when it names a side quest`)
  - `QuestPresenter should display the main quest when the pinned quest completes`
  - `OpenQuest should open the journal when a side quest is listed`
  - `OpenQuest should open the detail view when only the main quest is listed`
  - `QuestDetailBackSelected should display the journal again when it was opened from it`
  - `QuestAbandon should return to the journal when a side quest is still listed`
  - `QuestAbandon should close the quest when no side quest is listed`
  - `QuestPresenter should draw the markers when the party explores, and remove them in a battle`
- `ExplorationPresenterTest`: `ExplorationPresenter should start no second while announcing` is removed.
- `LocalizationResourcesTest`: it covers the new quests and texts with the tests it has.
- `QuestEndToEndTest`
  - `should announce the main quest with a toast and let the player walk when a new game starts` (replaces `should announce the main quest and display the tracker when a new game starts`)
  - `should play a side quest when it is started, pinned, progressed and completed`
  - `should take a side quest back to not started when it is abandoned`
  - `should restore the pinned quest and the markers when a game is loaded`

## Known limitations

- A quest giver's dialog says nothing about its quest: the toast is what tells the player. E.g.: the Farmer still only thanks the party for dealing with that bat.
- The toast can be missed, as nothing brings it back: the journal holds the same story.
- A marker only shows on an npc in view, with nothing pointing at one further away.
- The drag was only tried with a mouse in the Storybook, in the prototype, so its feel on a phone is to be checked in M2.
