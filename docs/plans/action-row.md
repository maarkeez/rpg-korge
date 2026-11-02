# Plan: Every action at the level of Finish turn, in a row below the panels

> **Status:** built on `main`. The screenshots of the three milestones are still to be approved.
>
> **Summary:** Finish turn and give gold's Cancel, All and Give sit in the call to action, below the 300 of the battle HUD, but the other panels keep their buttons inside. The journal's Back is at y 256 of its panel, a 72 wide button in the bottom left, Quit to the menu asks with its buttons at y 190, and the shop's Back and Buy share a line with the item's name, so a thumb has no one place to go. Instead, every one of those buttons sits in one row at the level of Finish turn, as tall as it: a lone button fills the row, two share it in halves and the merchant's three in thirds. The panels stay 300 tall and spread their content evenly over the space the buttons leave, so the journal shows 4 quests instead of 3. Chosen among the Storybook prototypes of the local branch `prototype/action-row` (`e32c4870`): option 1, Spread. It builds on [Give gold](give-gold.md), whose buttons set the level, the [side quests on the screen](side-quest-ui.md), two of whose decisions it replaces, [Buttons](../architecture/buttons.md), [Exploration](../architecture/exploration.md) and [Quests](../architecture/quests.md).

## Domain terms

| Term       | Meaning                                                                       | In the code     |
|------------|-------------------------------------------------------------------------------|-----------------|
| Action row | The buttons of a panel, at the level of Finish turn, sharing its 390 of width | `ActionRowView` |
| Spread     | A panel keeps its 300 and shares what the buttons leave out as even gaps      | No single class |

## Decisions

| Topic           | Decision                                                                                                                                                                                                                                                                                                                                                                                  |
|-----------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope           | The quest journal and detail view, every `ConfirmDialogView` (Abandon, Quit to the menu, overwriting and deleting a save), the save slots, the shop, the formation and the merchant dialog, in the game and in the main menu. E.g.: the journal's Back fills the row at y 763. Chosen over the quest panels alone, so every action of the game is on one level.                           |
| What stays      | Pause and the main menu keep their options, as moving them leaves the panels empty. The bag's Close stays on its title line, as the row holds the item actions by then. The shop's buyer arrows and each save slot's Delete stay beside what they act on. `GiveGoldView` and `GiveGoldActionsView` do not change.                                                                         |
| Row layout      | The buttons share the 390 equally with a spacing of 10: Back alone is 390, Back and Abandon are 190 each, and Buy, Sell and Leave are 123.3 each. Chosen over a Back of fixed width, which leaves part of the row empty, and over thirds as in give gold. Back's left edge never moves, so it still does not jump when stepping out, as the [side quests plan](side-quest-ui.md) asks.    |
| Colours         | Every button keeps its colour of today: grey, but red for Abandon and the action of a question. Buy and Sell stay grey. Chosen over making them blue like Give and Finish turn, as only their place changes.                                                                                                                                                                              |
| Freed space     | Spread: the panels stay 300 tall, 15 above the row as the battle HUD is, and even out their gaps. Chosen over a longer description, which leaves a blank band on most quests, shrinking to 248, which leaves 67 empty above the row, and growing to 310, which only adds 10 and leaves the slot of the pad.                                                                               |
| Journal         | 4 rows of 63 with a spacing of 3 instead of 3 rows of 66 with 4, in a list of 261, so the freed space shows another quest instead of a blank band.                                                                                                                                                                                                                                        |
| Journal drag    | Rows have only 5 below them, so they can not slide under the footer as they did. The list clips them at its edges: KorGE draws rounded shapes straight to the GPU, past the clip, so the rows draw theirs as bitmaps (`GraphicsRenderer.SYSTEM`), and a row crossing an edge is cut there. **Screen / Quest journal › Twelve quests, dragged** shows it.                                  |
| Detail view     | The banner moves to y 160 without tasks done and y 141 with them, the tasks done to y 217 and the reward row to y 256. The divider goes. Nothing is added: the description stays beside the portrait.                                                                                                                                                                                     |
| Questions       | `ConfirmDialogView` only holds the message, centred at y 95, and the row its two choices. Chosen over leaving its buttons in place, so all the actions are on one level. It replaces [Abandoning](side-quest-ui.md#decisions) only in where its buttons are.                                                                                                                              |
| Double tap      | No guard: Abandon stays under the thumb when its question opens, so tapping it twice abandons the quest without the question being read. Chosen over ignoring taps for a moment, and over swapping the sides of the question, which breaks Back on the left. The quest's giver can start it again.                                                                                        |
| Shop            | 300 tall, with gaps of 27 around the header, the slots and the name and price line, its slots the same size. The name and price, or the hint, stay in the panel on a line of their own, so the row stays at the level of Finish turn. Back fills the row until an item is selected, and then Back and Buy or Sell share it. Chosen over keeping 220, which leaves 95 empty above the row. |
| Formation       | Close fills the row. The hint stays where it is, using the width Close leaves. Nothing else moves, as its 5 rows already fill the panel.                                                                                                                                                                                                                                                  |
| Save slots      | Back fills the row. The rows spread to y 72, 152 and 232, a spacing of 32 instead of 10.                                                                                                                                                                                                                                                                                                  |
| Merchant dialog | Buy, Sell and Leave fill the row in thirds. The line of the dialog grows to 236 tall, for every npc. An npc with no choices displays no row.                                                                                                                                                                                                                                              |
| Main menu       | Its save slots and its question display their row directly below the panel, at y 587, with the same 15 as in a game. The panel stays centred at y 272. Chosen over y 763, as in a game, which moves the row away from the panel.                                                                                                                                                          |
| Leaving a panel | `ExplorationPresenter.displayVirtualPad` and its `PlayerVictory` step call `hideActions()`, so a battle starting never leaves an old Back on the screen: Finish turn is displayed again when the battle has one. `hideActions()` removes only an action row, as `hideFinishTurn()` removes only Finish turn, so an item found with a full bag stays.                                      |
| Architecture    | All of it in `screen.adapters.presentation`. `PlayerCallToActionView` displays `ActionRowView`, and the presenters take it and own the actions, so the panels lose their buttons and the delegates their callbacks.                                                                                                                                                                       |
| Prototype       | `prototype/action-row` is a reference and is never merged. The views are rebuilt on `main` with Spread alone: no `QuestPanelLayout` nor `ActionRowScreenView`.                                                                                                                                                                                                                            |
| Saving          | `SAVE_VERSION` stays as it is: nothing is saved.                                                                                                                                                                                                                                                                                                                                          |
| Storybook       | **Screen / Action row** and new stories of `PlayerCallToActionView`. The stories of the panels keep their ids and no longer display the buttons. See [Storybook](#storybook).                                                                                                                                                                                                             |
| Tests           | Views, presenters with `FakePlayerCallToActionView`, and the end-to-end tests playing through the row. See [Tests](#tests).                                                                                                                                                                                                                                                               |
| Out of scope    | Moving the options of pause and the menu or the bag's Close, a guard on double taps, new colours, new texts, and giving gold's views.                                                                                                                                                                                                                                                     |
| Delivery        | 3 milestones, each green on CI and approved on screenshots. See [Milestones](#milestones).                                                                                                                                                                                                                                                                                                |

For example, the party reads Rats in the Fields, an active side quest, from the journal, and abandons it:

| The player       | The panel, at y 448                     | The row, at y 763 |
|------------------|-----------------------------------------|-------------------|
| Taps the tracker | The journal, 4 quests in sight          | `Back`            |
| Taps the rats    | The detail view, with the reward at 256 | `Back`, `Abandon` |
| Taps Abandon     | `Abandon Rats in the Fields? ...`       | `Back`, `Abandon` |
| Taps Back        | The detail view again                   | `Back`, `Abandon` |
| Taps Back        | The journal, keeping its scroll         | `Back`            |
| Taps Back        | The pad                                 | Nothing           |

## Milestones

Each milestone is a few commits straight on `main`, green on CI.

| #  | Milestone                                | Done when                                                                                                                                              | Status | Done in |
|----|------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------|--------|---------|
| M1 | The row and the quests                   | Back and Abandon of the journal and the detail view are in the row, and both panels spread, in the game, approved on screenshots. The drag is decided. | ✅      | `26e872f7` |
| M2 | The questions and the saves              | Every question and the save slots have their buttons in the row, in the game and in the main menu, approved on screenshots.                            | ✅      | `845e2240` |
| M3 | The shop, the formation and the merchant | Their buttons are in the row and the shop spreads, approved on screenshots, with the docs up to date.                                                  | ✅      | `b273255b` |

### M1 commits

1. Add `ActionRowView` with its stories, and `PlayerCallToActionView.displayActions` and `hideActions`, called by `ExplorationPresenter.displayVirtualPad` and its `PlayerVictory` step.
2. Move the buttons of `QuestJournalView` and `QuestDetailView` to the row, displayed by `QuestPresenter`, while the question to abandon hides it and keeps its own buttons for now.
3. Spread `QuestDetailView` and `QuestJournalView`, and try to clip the rows of the journal while dragging, with the outcome in Known limitations when it can not be done.
4. Publish screenshots of the stories for the user to approve, in English and Spanish, and apply what comes back.
5. Update [Quests](../architecture/quests.md), [Exploration](../architecture/exploration.md) and [Buttons](../architecture/buttons.md).

### M2 commits

1. Add `PlayerCallToActionView.displayConfirmation`, and move the buttons of `ConfirmDialogView` to the row, in `QuestPresenter`, `PausePresenter` and `LoadGamePresenter`, with a `PlayerCallToActionView` in `MenuScene`.
2. Move Back of `SaveSlotsView` to the row, in `PausePresenter` and `LoadGamePresenter`, and spread its rows.
3. Publish screenshots for the user to approve, in English and Spanish, and apply what comes back.
4. Update [Save and load](../architecture/save-and-load.md), [Exploration](../architecture/exploration.md) and [Quests](../architecture/quests.md).

### M3 commits

1. Move Close of `FormationView` to the row, in `FormationPresenter`.
2. Move Back and the action of `ShopView` to the row, in `ShopPresenter`, and spread the shop to 300.
3. Move the choices of `NpcDialogView` to the row, in `ExplorationPresenter`, and let its line use the room.
4. Publish screenshots for the user to approve, in English and Spanish, and apply what comes back.
5. Update [Exploration](../architecture/exploration.md), [Buttons](../architecture/buttons.md) and the [Presentation timeline](../architecture/presentation-timeline.md), and mark the plan as done.

## Reference

### PlayerCallToActionView and ActionRowView

- `ActionRowView(actions)` draws a `gameButton` per action, 390 wide in all, with a spacing of 10, and as tall as Finish turn (`UIButton.DEFAULT_SIZE.height`). `ActionRowView.Action` is `label`, `color` and `pressedColor`, by default `GameButton.GREY` and `GREY_OVER`, `isEnabled`, by default `true`, and `onSelected`.
- `PlayerCallToActionView.displayActions(actions)` displays the row in place of whatever is displayed, and makes the view visible, as the pad hides it while exploring. Unlike `displayItemActions`, a tap does not dismiss it: the presenter displays what follows.
- `displayConfirmation(cancelLabel, confirmLabel, onCancelled, onConfirmed)` displays both choices, the second in `HudStyle.DANGER` and `DANGER_PRESSED`.
- `hideActions()` calls `dismiss()` only when an `ActionRowView` is displayed, so Finish turn returns when the battle has one, and nothing else is removed.

### Quests

- `QuestJournalView`: `ROW_HEIGHT` is 63, `ROW_SPACING` 3 and `LIST_HEIGHT` 261, so the list ends at y 295. `TARGET_AT` stays 8 across, and its slot is centred down the row, at 6.5. `FOOTER_HEIGHT` is 5. `BACK_Y`, `BACK_WIDTH`, `backButton`, the divider and `Delegate.questJournalBackSelected` go.
- `QuestDetailView`: `BANNER_Y` is 141, or 160 without tasks done, moving `COUNTED_TASK_Y`, `SINGLE_TASK_Y` and `BAR_Y` with it. `TASKS_DONE_Y` is 217, `REWARD_ROW_Y` 256 and `REWARD_CAPTION_Y` 263. `drawButtons`, `backButton`, `abandonButton`, `DIVIDER_Y`, the `BACK_` and `ABANDON_` constants and its `Delegate.questDetailBackSelected` and `questDetailAbandonSelected` go. The header comment no longer speaks of a bottom row.
- `QuestPresenter` takes `playerCallToActionView`. `journalActions()` is Back, with `QUEST_DETAIL_BACK`. `detailActions(quest)` is Back, and Abandon in `HudStyle.DANGER` only on an active side quest. Every tap keeps its `canAct()`.
- `questJournalBackSelected`, `questDetailBackSelected` and `questDetailAbandonSelected` become private functions of `QuestPresenter`. Abandon asks with `displayConfirmation`, and `keepQuest` displays the detail view's row again.

### ConfirmDialogView

- `ConfirmDialogView.display(message)` only writes the message, with `MESSAGE_Y` at 95, centring its 110 in the 300. `ACTIONS_Y` and `cancelAndConfirmView` go. `CancelAndConfirmView` stays, for `displayCancelAndConfirm`.
- `PausePresenter` and `LoadGamePresenter` take `playerCallToActionView`, and ask with `displayConfirmation`, with the labels they have today. E.g.: `CALL_TO_ACTION_CANCEL` and `PAUSE_QUIT` in red.
- `PausePresenter.displayPause` calls `hideActions()`, as the pause screen has no row.

### SaveSlotsView and MenuScene

- `SaveSlotsView`: `ROW_SPACING` is 32 and `rowY` starts at 72, so the last row ends at y 280. `backButton`, `BACK_WIDTH`, `BAR_HEIGHT` and `Delegate.saveSlotsBackSelected` go. `saveSlotDeleteSelected` stays.
- `PausePresenter.displaySaveSlots` and `LoadGamePresenter.displaySlots` display Back, with `SAVE_SLOTS_BACK`. `LoadGamePresenter`'s Back also calls `hideActions()`, as the menu's options have no row.
- `MenuScene` adds a `PlayerCallToActionView` at y 587, its `ROW_Y`, which is `(GAME_WINDOW_SIZE.height - PANEL_HEIGHT) / 2 + PANEL_HEIGHT + 15`. Starting on an incompatible slot displays its row straight away.

### FormationView

- `closeButton`, `CLOSE_WIDTH` and `Delegate.formationCloseSelected` go. `hintText` is `WIDTH - 2 * PADDING` wide, at the same y.
- `FormationPresenter` takes `playerCallToActionView`, and displays Close, with `FORMATION_CLOSE`, in `open()`.

### ShopView

- `HEIGHT` is 300. The header starts at y 27, and the vertical gaps are 27 where they are `PADDING` today, its slots spaced by `SLOT_SPACING` as today, so the name and price line ends at y 271.
- `backButton`, `actionButton`, `BACK_WIDTH`, `ACTION_WIDTH` and `Delegate.shopBackSelected` and `shopActionSelected` go. `itemNameText` and `itemPriceText` are `WIDTH` wide from x 0.
- `displayItem(itemName, itemPrice)` and `displayHint(hint)` only write the line.
- `ShopPresenter` takes `playerCallToActionView`. `clearSelection` displays Back alone, with `SHOP_BACK`. `selectItem` displays Back and the action, with its label or why it can not be done, disabled, as `isActionEnabled` does today. E.g.: `Back`, `Not enough gold`, faded. Both keep `whenIdle`.

### NpcDialogView and ExplorationPresenter

- `NpcDialogView.display(npc)` loses `choices`, `Choice`, `choiceButtons`, `CHOICE_HEIGHT` and `choicesY`. `lineText` is `size.height - PADDING * 3 - NAME_HEIGHT` tall, 236.
- `ExplorationPresenter.displayNpcDialog` displays Buy, Sell and Leave in the row for a merchant. Tapping the dialog of a merchant is still ignored, as it has its Leave.

## Storybook

- `ActionRowViewStories`: **Screen / Action row › One action** (`screen-action-row--one-action`), **› Two actions** (`screen-action-row--two-actions`), **› Danger** (`screen-action-row--danger`), **› Three actions** (`screen-action-row--three-actions`) and **› Disabled action** (`screen-action-row--disabled-action`). Controls: `first`, `second` and `third` (text, an empty one drawing no button). Tapping logs `onSelected`.
- `PlayerCallToActionViewStories`: **› Actions** (`screen-player-call-to-action--actions`) and **› Confirmation** (`screen-player-call-to-action--confirmation`).
- `QuestJournalViewStories`, `QuestDetailViewStories`, `ConfirmDialogViewStories`, `SaveSlotsViewStories`, `FormationViewStories`, `ShopViewStories` and `NpcDialogViewStories` keep their stories, no longer log the buttons, and describe the spread layout. The stories of `QuestJournalView` cover dragging the list.
- `StorySmokeTest` lists the new stories.

## Docs

- [Buttons](../architecture/buttons.md): `ActionRowView` in Where it is used, and `ShopView` without Back and Buy.
- [Quests](../architecture/quests.md): the journal, the detail view and Abandoning, with their buttons in the row.
- [Exploration](../architecture/exploration.md): the steps of the shop, the formation, the merchant and the quests, and the row while the pad is hidden.
- [Save and load](../architecture/save-and-load.md): the save slots and the question to delete, in the menu.
- [Presentation timeline](../architecture/presentation-timeline.md): the row on the panels, and that the pad clears it.

## Tests

- `PlayerCallToActionViewTest`
  - `DisplayActions should display every action, sharing the width of Finish turn, when they are displayed`
  - `DisplayActions should keep the row displayed when an action is clicked`
  - `DisplayActions should make the view visible when it is hidden`
  - `DisplayConfirmation should draw the confirm action in danger`
  - `HideActions should display the finish turn again when the battle has one`
  - `HideActions should keep the inventory full displayed when no row is displayed`
- `ActionRowViewTest`
  - `ActionRowView should fill the row when it has one action`
  - `ActionRowView should keep the first button on the left when it has several actions`
  - `ActionRowView should display a disabled action faded, not calling it when it is tapped`
- `QuestJournalViewTest`
  - `display should show 4 rows when it lists more than fit` (replaces `Tapping Back should call its delegate when it is displayed`)
- `QuestDetailViewTest`
  - `display should keep the reward above the panel's bottom when it has gold and four items` (replaces `QuestDetailView should call its delegate when Back or Abandon is tapped`)
- `QuestPresenterTest`
  - `openQuest should display Back when the journal is listed`
  - `questJournalSelected should display Back and Abandon when it is an active side quest`
  - `questJournalSelected should display Back alone when it is the main quest or a completed one`
  - `Abandon should display the question's actions, and Back the detail view's again`
- `ConfirmDialogViewTest`
  - `ConfirmDialogView should display the message when it asks` (replaces `ConfirmDialogView should display the message over both choices when it asks`, `Display should draw the action in danger` and `ClickChoice should call the action of the choice tapped`)
- `PausePresenterTest`
  - `pauseQuitSelected should display the question's actions, and Cancel hide them again`
  - `Save should display Back, and Back hide it again`
- `LoadGamePresenterTest`
  - `saveSlotDeleteSelected should display the question's actions, and Cancel display Back again`
  - `Back should hide the row when the menu is displayed again`
- `SaveSlotsViewTest`
  - `display should spread the rows when they are 3`
- `FormationViewTest`
  - `DisplayHint should use the whole width` (replaces `DisplayHint should display the hint next to Close` and `ClickClose should notify the delegate`)
- `FormationPresenterTest`
  - `open should display Close, which closes the formation`
- `ShopViewTest`
  - `DisplayItem should display the item's name and price, and DisplayHint the hint` (replaces `DisplayItem should display the item's name, price and action, and DisplayHint the hint without action` and `Click should notify the delegate of the selected slot, the arrows, the action and Back`)
- `ShopPresenterTest`
  - `open should display Back alone when nothing is selected`
  - `Selecting an item should display Back and the action when it can be traded`
  - `Selecting an item should display the action disabled with why when it can not be traded`
- `NpcDialogViewTest`
  - `Display should give the line the room of the choices` (replaces `Display should display the choices under the line, in their order`, `Display should replace the choices displayed before` and `ClickChoice should call the selected choice`)
- `ExplorationPresenterTest`
  - `ExplorationPresenter should display Buy, Sell and Leave when a merchant talks`
  - `ExplorationPresenter should display no row when an npc with no choices talks`
  - `ExplorationPresenter should hide the row and display the finish turn when a battle starts with a panel open`
  - `ExplorationPresenter should hide the row when the player wins`
- `QuestEndToEndTest`, `PauseEndToEndTest`, `FormationEndToEndTest`, `SaveAndLoadEndToEndTest` and `InventoryEndToEndTest` play through the row, with their page objects in `QuestJournalViewExtension`, `QuestDetailViewExtension`, `SaveSlotsViewExtension`, `FormationViewExtension`, `ShopViewExtension`, `NpcDialogViewExtension` and `ConfirmDialogViewExtension` moved to `PlayerCallToActionViewExtension`.

## Known limitations

- Tapping Abandon twice abandons the quest without its question being read, as decided in [Double tap](#decisions).
- ✅ on a milestone means built, green and pushed: "approved on screenshots" is still open for all three.
- The rows of the quests, the formation, the save slots, the question and the main menu were played in the real game in a browser. The shop and the merchant's row were not, as the merchants stand far from where a new game starts: they are checked by `ExplorationPresenterTest`, `ShopPresenterTest` and their stories.
- The line through the buttons in the screenshots of `prototype/action-row` comes from the software renderer. The same kind of mark is on Resume and Quit to menu of the pause screen, which this plan does not touch, and none of them has been checked on a phone, which the screenshots do not settle.
