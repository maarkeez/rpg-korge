# Plan: Give gold between party members, from a panel in place of the bag

> **Status:** to do.
>
> **Summary:** Each battle unit carries its own gold, and the only way to move it is not to: a knight with 20 G can not buy the 50 G hammer while the viking beside it carries 100 G, as [Giving](../architecture/inventory.md#giving) moves items only ("Gold is never given"). Instead, while exploring, the gold on the bag's title line looks like a button. Tapping it opens a panel in place of the bag, with the giver, the receiver, what each would carry and a slider, while Cancel, All and Give sit at the level of the finish turn. Giving moves the gold, goes back to the bag and flashes the receiver. A faded button reads why when no gold can be given. Chosen among the Storybook prototypes of the local branch `prototype/give-gold` (`6b7c817a`): flow C with its buttons moved down, the faded button that reads why, and the flash only. It builds on [Inventory](../architecture/inventory.md), [Exploration](../architecture/exploration.md), the [Presentation timeline](../architecture/presentation-timeline.md), [Localization](../architecture/localization.md) and the [battle HUD](battle-hud-vector-chrome.md), whose vector chrome it draws with.

## Domain terms

| Term        | Meaning                                                                            | In the code            |
|-------------|------------------------------------------------------------------------------------|------------------------|
| Giving gold | Moving an amount of gold from a party member to another, while exploring           | `giveGold`             |
| Giver       | The party member whose bag is open                                                 | `giverBattleUnitId`    |
| Receiver    | The party member who gets the gold, one with room for more                         | `receiverBattleUnitId` |
| Room        | How much more gold a party member can carry, up to 99,999 G                        | `GoldReceiverDto.room` |
| Gold button | The gold on the bag's title line while exploring, looking like a button            | `GoldButtonView`       |
| Give panel  | The giver, the receiver, what each would carry and the slider, in place of the bag | `GiveGoldView`         |
| Refusal     | Why no gold can be given, read when the faded gold button is tapped                | `GiveGoldRefusalDto`   |

## Decisions

| Topic                   | Decision                                                                                                                                                                                                                                                                                                                                                                      |
|-------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope                   | One giver, one receiver and an amount, while exploring, in the game, with its stories and texts in English and Spanish. E.g.: the knight gives 25 of its 120 G to the viking.                                                                                                                                                                                                 |
| Name                    | `giveGold`, with `GoldGiven` and `GoldReceived`, the verb of `giveItem`, in the code and on the button. Chosen over `transferGold`, which sounds like a bank and leaves items on another verb, and `shareGold`, which suggests a shared purse.                                                                                                                                |
| When                    | Only while the party explores, through `GiveRule`, as items: not in a battle, and not while talking to an npc. Chosen over allowing it in the shop, which needs its own rule and a gold control in the shop. So a member short of gold leaves the shop first.                                                                                                                 |
| Scope of a give         | One to one. Chosen over a Split evenly and a Collect all, which need rules for remainders and for what does not fit.                                                                                                                                                                                                                                                          |
| Limit                   | The amount never goes above what the receiver can carry, so no gold is lost: a receiver with 99,990 G takes at most 9 G, and one at 99,999 G can not be chosen. `giveGold` still fails above it (`GoldAboveLimit`). Chosen over the excess being lost, as with a reward, as a give can be refused.                                                                            |
| Flow                    | Variant C, a panel in place of the bag, with everything on one view. Chosen over a stepper, whose 25 G takes 9 taps, chips, which can not give 25 G, and a keypad, the only one of the game.                                                                                                                                                                                  |
| Buttons                 | Cancel, All and Give sit in the call to action, at the level of Finish turn, as tall as it, so every action of the game is on one level and the panel keeps the height of the battle HUD. It replaces the prototype, where they were inside the panel. Give is blue like Finish turn and reads the amount, e.g.: `Give 25 G`.                                                 |
| Panel                   | 378x292, at the x of the quest detail view, in the 300 of the battle HUD. The room freed by the buttons goes to the portraits, at 3× (96) instead of the prototype's 2×. The giver is ringed in `HudStyle.SELECTION` and the receiver in `HudStyle.ALLY`.                                                                                                                     |
| Starting the give       | Tapping the gold button, once the timeline is idle, clearing the selected item. It is ignored while giving an item or choosing what to do with a found one (`whenIdle`). Chosen over a Give gold button in the empty call to action, as the gold is where the player looks for it.                                                                                            |
| Gold button             | Only while exploring: in a battle the gold stays the plain text it is today, as it can not be given. It is drawn with `GameButton`'s colours and a coin, right aligned where the gold is.                                                                                                                                                                                     |
| Can not give            | The button is faded, and tapping it displays why in the call to action, with OK: `No gold to give`, `No one to give gold to` or `Everyone's gold is full`. Chosen over a tap doing nothing, as the player would have to guess.                                                                                                                                                |
| Amount                  | Starts at 1, set with the slider, with - and + (1 G each), or with All, up to the lower of the giver's gold and the receiver's room. Switching to a receiver with less room lowers the amount to fit.                                                                                                                                                                         |
| Receiver                | The first party member with room, in party order. It changes with the arrows of the panel, disabled at the ends as the bag's are and hidden with a single receiver, or by tapping its tile. Members at the limit are skipped.                                                                                                                                                 |
| Tiles                   | Every tile is dimmed but the receivers' ones, as when giving an item, and the chosen one is selected. Chosen over leaving only the chosen one undimmed, as in the prototype, which makes the other tappable receivers look unavailable. Any other tap is ignored.                                                                                                             |
| Time                    | It goes on, as when giving an item: no new `Party` mode. Chosen over stopping it, as items and gold would then differ, and an interruption loses nothing.                                                                                                                                                                                                                     |
| Interruptions           | A battle starting abandons the give with no gold moved. The giver being defeated abandons it too. The receiver being defeated moves to the next one, or goes back to the bag when none is left.                                                                                                                                                                               |
| Gold changing meanwhile | A reward landing while the panel is open (`RewardGranted`, e.g.: a wandering rat dies on lava and completes a task) reads the gold and the room again, lowering the amount to fit. So `giveGold` does not fail on a stale amount.                                                                                                                                             |
| Feedback                | The receiver flashes, alongside the timeline, as for items, and the bag is displayed again with the new gold, so the next give can start straight away. Chosen over a floating `+25 G` and opening the receiver's bag.                                                                                                                                                        |
| Cancel                  | Goes back to the bag with nothing moved, the giver selected again.                                                                                                                                                                                                                                                                                                            |
| Saving                  | `SAVE_VERSION` stays as it is: the gold is already saved, and nothing of the panel is.                                                                                                                                                                                                                                                                                        |
| Pixel art               | Only the portraits. The rest is vector chrome from `HudStyle`, as in the [battle HUD](battle-hud-vector-chrome.md).                                                                                                                                                                                                                                                           |
| Architecture            | The rules, in `inventory`. The views and the flow, in `screen.adapters.presentation`, in `InventoryPresenter`, which already owns giving items, the tile interceptor and `InventoryView.Delegate`. The views take plain data, so they import no domain ([`ViewArchitectureTest`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/architecture/ViewArchitectureTest.kt)). |
| Prototype               | `prototype/give-gold` is a reference and is never merged. The views are rebuilt on `main` with the chosen variants alone: no `GiveGoldStepperView`, `GiveGoldChipsView`, `GiveGoldKeypadView`, `GoldFloatView` nor `GiveGoldScreenView`, and hard-coded texts go through `Texts`.                                                                                             |
| Storybook               | One binding per new view, plus new stories for `InventoryView`, `PlayerCallToActionView` and `BattleHudView`. The layout with the buttons moved down has not been seen yet: it is approved on screenshots at the end of M2, before the game uses it. See [Storybook](#storybook).                                                                                             |
| Tests                   | Domain, use cases, queries, views, `InventoryPresenterGiveGoldTest`, the catalogs in `LocalizationResourcesTest`, and an end-to-end give. See [Tests](#tests).                                                                                                                                                                                                                |
| Out of scope            | Giving gold in the shop or in a battle, Split evenly and Collect all, a floating amount, repeating - and + while held, and sound.                                                                                                                                                                                                                                             |
| Delivery                | 3 milestones, each green on CI. See [Milestones](#milestones).                                                                                                                                                                                                                                                                                                                |

For example, the knight, with 120 G, gives 25 G to the viking, with 100 G, while the herbalist carries 99,999 G:

| The player             | Published                   | Displayed                                                                                                                |
|------------------------|-----------------------------|--------------------------------------------------------------------------------------------------------------------------|
| Opens the knight's bag | Nothing new                 | `< Knight  [ 120 G ] >`, the gold button                                                                                 |
| Taps the gold button   | Nothing                     | The panel with the viking, as the herbalist has no room. Only the viking's tile is undimmed. `Cancel`, `All`, `Give 1 G` |
| Drags the slider to 25 | Nothing                     | `120 G > 95 G` and `100 G > 125 G`, and `Give 25 G`                                                                      |
| Taps Give 25 G         | `GoldGiven`, `GoldReceived` | The bag again, `< Knight  [ 95 G ] >`, the knight selected, and the viking flashes                                       |

## Milestones

Each milestone is a few commits straight on `main`, green on CI.

| #  | Milestone   | Done when                                                                                                                                                            | Status | Done in |
|----|-------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------|---------|
| M1 | The rules   | `inventoryApi.giveGold` moves the gold, and the refusal and receivers can be asked. Nothing on the screen yet.                                                       | ⬜      |         |
| M2 | The views   | The gold button, the panel, its buttons and the refusal have their stories and texts, and the user has approved them on screenshots. The game does not use them yet. | ⬜      |         |
| M3 | In the game | The gold can be given while exploring, from the bag to the flash, with the docs up to date.                                                                          | ⬜      |         |

### M1 commits

1. Add `Inventory.giveGold` and `receiveGold`, `GoldGiven` and `GoldReceived`, and the `GiveGold` use case, in `InventoryApi`.
2. Add `SearchGiveGoldRefusal` and `SearchGoldReceivers`, in `InventoryApi`.

### M2 commits

1. Add `GoldButtonView` with its stories, and display it on `InventoryView`'s title line while exploring, with `goldSelected` on its delegate, ignored for now.
2. Add `GiveGoldView` with its stories, and `BattleHudView.displayGiveGoldView`.
3. Add `GiveGoldActionsView` and `GiveGoldRefusalView` with their stories, displayed by `PlayerCallToActionView`, and the texts in `messages.pot` and `es.po`.
4. Publish screenshots of the stories for the user to approve, in English and Spanish, and apply what comes back.

### M3 commits

1. Give gold from `InventoryPresenter`: the button, the panel, the receivers' tiles, Cancel, All and Give, the refusal and the flash, in `InventoryPresenterGiveGoldTest`.
2. Abandon the give on a battle starting or the giver being defeated, move on when the receiver is defeated, and read the gold again on a reward.
3. Play it in `InventoryEndToEndTest`, and update [Inventory](../architecture/inventory.md), [Exploration](../architecture/exploration.md) and the [Presentation timeline](../architecture/presentation-timeline.md).
4. Mark the plan as done.

## Reference

### inventory

#### Domain

- `Inventory.giveGold(amount, receiverBattleUnitId)` removes the gold, announcing `GoldGiven`. It fails with `InvalidGoldAmount` below 1, and with `NotEnoughGold` above its gold.
- `Inventory.receiveGold(amount, giverBattleUnitId)` adds the gold, announcing `GoldReceived`. It fails with `GoldAboveLimit` above 99,999 G, through `Gold`.
- `Inventory.goldRoom()` answers how much more it can carry, so `MAXIMUM_GOLD` stays private to `Inventory`.
- `GoldGiven(battleUnitId, receiverBattleUnitId, gold)` and `GoldReceived(battleUnitId, giverBattleUnitId, gold)` are new `InventoryEvent`s, as `ItemGiven` and `ItemReceived`.
- `InventoryError.InvalidGoldAmount` is new. `NotEnoughGold` and `NotExploringTheParty` are reworded, as they no longer only speak of buying and of items.
- `Inventory.Dto.GiveGoldRefusalDto` is `NOT_EXPLORING`, `NO_GOLD`, `NO_ONE` or `GOLD_FULL`. `Inventory.Dto.GoldReceiverDto` is `battleUnitId`, `gold` and `room`.

#### Use cases

- `GiveGold(giverBattleUnitId, receiverBattleUnitId, amount)`, as `GiveItem`: `GiveRule.requireGiving`, then both inventories are saved together and their events published, so nothing changes when either side fails.
- `SearchGiveGoldRefusal(giverBattleUnitId)` answers why no gold can be given to anyone, in this order: `NOT_EXPLORING`, `NO_GOLD`, `NO_ONE` when alone, `GOLD_FULL` when every other member is at the limit. `null` otherwise.
- `SearchGoldReceivers(giverBattleUnitId)` answers the other members with room, in party order, as `GoldReceiverDto`.
- `GiveRule` does not change, but its comment, which now speaks of an item or gold.

### screen

#### Views

- `GoldButtonView(onSelected)` draws a rounded button with a coin and the amount, with `GameButton`'s colours, or faded. It can still be tapped when faded.
- `InventoryView.Owner` gains `canGiveGold`. With an owner, `InventoryView.display` draws `GoldButtonView` in place of `goldView`, faded when it can not. Without one, the gold stays plain text. `InventoryView.Delegate` gains `goldSelected()`.
- `GiveGoldView(resourcesLoader, texts)` is the panel, taking plain data through `display(giver, receiver, amount, maximum, canStepToPrevious, canStepToNext)`, with `GiveGoldView.Member(name, unitId, gold)`. Its `Delegate` has `amountChanged(amount)` and `receiverStepped(step)`.
  - It follows the finger on its own, calling `amountChanged` on every whole amount, and only displays again when asked, so a drag is not interrupted.
  - E.g.: `Knight 120 G > 95 G` and `Viking 100 G > 125 G`, with 25 G above the slider.
- `GiveGoldActionsView(texts, onCancelled, onAll, onGiven)` is the row of the call to action: three `gameButton`s of `(390 - 2 × 10) / 3`, as tall as Finish turn (`UIButton.DEFAULT_SIZE.height`). Give is `ConfirmButton.COLOR`. `displayAmount(amount, maximum)` updates `Give 25 G`, and disables All at the maximum.
- `GiveGoldRefusalView(message, okLabel, onDismissed)` is the reason and OK.
- `BattleHudView` takes `giveGoldView`, with `displayGiveGoldView()`, which `FakeBattleHudView` records as `Display.GIVE_GOLD`.
- `PlayerCallToActionView` gains `displayGiveGold(amount, maximum, onCancelled, onAll, onGiven)`, `updateGiveGold(amount, maximum)` and `displayGiveGoldRefusal(message, onDismissed)`.

#### InventoryPresenter

- `goldSelected()` reads `searchGiveGoldRefusal`. With a refusal, the call to action displays why. Otherwise, `startGivingGold` keeps a `GivingGold(giverBattleUnitId, receivers, receiverIndex, amount)`, displays the panel and its buttons, and dims every tile but the receivers'.
- `whenIdle` also waits for `givingGold == null`, so the bag's arrows, Close and slots are ignored, as they are hidden anyway.
- `tileSelected` takes every tap while giving gold: the tile of another receiver chooses it, and any other tile does nothing.
- `amountChanged` stores the amount and updates the buttons, without displaying the panel again. `receiverStepped`, All and a tile do display it.
- Give calls `inventoryApi.giveGold`, if the timeline is idle. `GoldGiven` refreshes the giver's bag and displays it again, with the giver selected as `restoreGiverSelection` does. `GoldReceived` calls `flashReceiver`.
- `ownerOf` fills `canGiveGold` from `searchGiveGoldRefusal`, so the button fades as soon as the giver's bag is displayed with no gold left.
- `ExplorationPaused` and the giver's `BattleUnitDefeated` abandon the give: the bag is displayed again, the call to action dismissed and the tiles reset, and the battle HUD takes over. The receiver's `BattleUnitDefeated` chooses the next receiver, or goes back to the bag. `RewardGranted` of the giver or a receiver reads the gold and the room again.

#### BattleScene

- `BattleScene` builds `GiveGoldView`, hands it to `BattleHudView` and sets `InventoryPresenter` as its delegate.

### Texts

New `UiText` entries, in `messages.pot` and `es.po`, with a comment each:

| Key                    | English                   | Spanish                         |
|------------------------|---------------------------|---------------------------------|
| `ui.giveGold.title`    | `Give gold`               | `Dar oro`                       |
| `ui.giveGold.cancel`   | `Cancel`                  | `Cancelar`                      |
| `ui.giveGold.all`      | `All`                     | `Todo`                          |
| `ui.giveGold.give`     | `Give {amount}`           | `Dar {amount}`                  |
| `ui.giveGold.noGold`   | `No gold to give`         | `No hay oro que dar`            |
| `ui.giveGold.noOne`    | `No one to give gold to`  | `No hay nadie a quien dar oro`  |
| `ui.giveGold.goldFull` | `Everyone's gold is full` | `Todos llevan el máximo de oro` |
| `ui.giveGold.ok`       | `OK`                      | `Vale`                          |

`{amount}` is the amount already written with `Texts.gold`, e.g.: `25 G`.

### Storybook

- `GoldButtonViewStories`: **Screen / Gold button › Default** (`screen-gold-button--default`) and **› Faded** (`screen-gold-button--faded`), with its `gold` (int, 0 to 99,999) and `faded` (boolean) controls.
- `GiveGoldViewStories`: **Screen / Give gold › Default** (`screen-give-gold--default`), **› Several receivers** (`screen-give-gold--several-receivers`), **› Limited by the receiver** (`screen-give-gold--limited`, 9 G at most) and **› Long names** (`screen-give-gold--long-names`). Its `amount`, `giverGold`, `receiverGold` (int, 0 to 99,999) and `receivers` (int, 1 to 3) controls. Dragging the slider maps to `amount`, on its release.
- `GiveGoldActionsViewStories`: **Screen / Give gold actions › Default** (`screen-give-gold-actions--default`) and **› At the maximum** (`screen-give-gold-actions--at-the-maximum`).
- `GiveGoldRefusalViewStories`: **Screen / Give gold refusal › No gold** (`screen-give-gold-refusal--no-gold`), **› No one** (`screen-give-gold-refusal--no-one`) and **› Gold full** (`screen-give-gold-refusal--gold-full`).
- `InventoryViewStories`: its exploring stories display the gold button, and **› Exploring, no gold** (`screen-inventory--exploring-no-gold`) the faded one. It takes no new control, as it has the 8 that fit: the button fades with `gold` at 0 or `partySize` at 1.
- `PlayerCallToActionViewStories`: **› Give gold** (`screen-player-call-to-action--give-gold`) and **› Give gold refusal** (`screen-player-call-to-action--give-gold-refusal`).
- `BattleHudViewStories`: **› Give gold** (`screen-battle-hud--give-gold`), with the panel in place.
- `StorySmokeTest` lists them, and `StorybookApi.STORY_BINDINGS` the new bindings, after `GiveItemViewStories`.

### Docs

- [Inventory](../architecture/inventory.md): `giveGold`, `searchGiveGoldRefusal` and `searchGoldReceivers` in its tables, the [Gold](../architecture/inventory.md#gold) section no longer saying the gold stays with each battle unit for good, and a new Giving gold section after [Giving](../architecture/inventory.md#giving), replacing "Gold is never given".
- [Exploration](../architecture/exploration.md#inventory): the gold button on the title line, the panel, its tiles and its interruptions.
- [Presentation timeline](../architecture/presentation-timeline.md#inventory): the panel in the battle HUD, its buttons in the call to action, and that the bag is redrawn when it gives gold.

### Tests

- `InventoryTest`
  - `GiveGold should remove the gold and announce it when the amount is carried`
  - `GiveGold should fail when the amount is below 1`
  - `GiveGold should fail when the amount is above the gold carried`
  - `ReceiveGold should add the gold and announce it when it fits`
  - `ReceiveGold should fail when it goes above 99,999 G`
  - `GoldRoom should answer how much more gold it can carry`
- `GiveGoldTest`
  - `should move the gold from the giver to the receiver`
  - `should fail, keeping both inventories, when the receiver has no room`
  - `should fail, keeping both inventories, when the giver does not carry the amount`
  - `should fail when the receiver is no party member, or the giver itself`
  - `should fail when the party does not explore, in a battle, or while talking to an npc`
- `SearchGiveGoldRefusalTest`
  - `should answer not exploring when in a battle, or while talking to an npc`
  - `should answer no gold when the giver carries none`
  - `should answer no one when the giver is alone in the party`
  - `should answer gold full when every other member is at the limit`
  - `should answer nothing when another member has room`
- `SearchGoldReceiversTest`
  - `should answer the other members with room, in party order, with their gold and room`
  - `should leave out the members at the limit`
  - `should answer nothing when the party does not explore`
- `InventoryViewTest`
  - `Display should draw the gold as a button when it names an owner`
  - `Display should draw the gold as plain text when it names no owner`
  - `Display should fade the gold button when the owner can not give gold`
  - `GoldButton should call the delegate when it is tapped, also when faded`
- `GiveGoldViewTest`
  - `Display should write what the giver and the receiver would carry`
  - `Display should hide the arrows when there is a single receiver`
  - `Display should disable the arrow at each end of the receivers`
  - `Slider should call the delegate with each whole amount, without going above the maximum, when it is dragged`
  - `Minus and plus should change the amount by 1, stopping at 1 and at the maximum`
- `GiveGoldActionsViewTest`
  - `DisplayAmount should write the amount on Give, and disable All at the maximum`
- `BattleHudViewTest`
  - `DisplayGiveGoldView should display only the panel`
- `InventoryPresenterGiveGoldTest`
  - `GoldSelected should open the panel with the first member with room, starting at 1, and dim every tile but the receivers'`
  - `GoldSelected should clear the selected item when one is selected`
  - `GoldSelected should display why, opening nothing, when the giver carries no gold`
  - `GoldSelected should display why when the giver is alone, or every other member is at the limit`
  - `GoldSelected should be ignored while giving an item`
  - `OK should dismiss the reason`
  - `TileSelected should choose the receiver on the tapped tile, lowering the amount to fit`
  - `TileSelected should take the taps on any other tile without changing anything`
  - `ReceiverStepped should choose the next receiver, lowering the amount to fit`
  - `All should set the lower of the gold and the receiver's room`
  - `Give should move the gold, display the bag again with the new gold, and flash the receiver`
  - `Give should fade the gold button when no gold is left`
  - `Cancel should display the bag again with nothing moved, the giver selected`
  - `Give gold should be abandoned when the exploration pauses, e g a battle starts`
  - `Give gold should be abandoned when the giver is defeated`
  - `Give gold should choose the next receiver when the chosen one is defeated, or go back to the bag when none is left`
  - `Give gold should lower the amount when a reward takes room from the receiver`
- `InventoryEndToEndTest`
  - `should give gold to another party member from the bag, and read it in its bag`
- `StorySmokeTest` lists the new stories, `LocalizationResourcesTest` checks the new texts, and `InventoryPresenterSwitchTest`, `InventoryPresenterTest` and `FakeInventoryView` follow `Owner.canGiveGold`.

### Known limitations

- The slider has only been tried with a mouse in the Storybook, so its feel on a phone is to be checked in M3. The - and + stay as the exact fallback.
- Large amounts take many taps of - and +, as they do not repeat while held: the slider and All cover them.
- A member short of gold at a merchant leaves the shop to receive some, and comes back.
- The Spanish texts are written with the plan, and not yet reviewed by a translator.
