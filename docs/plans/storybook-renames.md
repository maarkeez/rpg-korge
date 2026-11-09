# Plan: Rename the Storybook's words, and its stories', to the words of the glossary

> **Status:** done, built on `main` in `7fea011f`.
>
> **Summary:** The Storybook still says the words the earlier sections of the [glossary](../../GLOSSARY.md) left
> behind, as their plans renamed the views but not their stories. E.g.: `CastPreviewViewStories` shows **Screen /
> Attack preview › One enemy** (`screen-attack-preview--one-enemy`), with a `receiver` arg, and **Screen / Item actions ›
> Bags full** says "Give reads Bags full", while the button reads No space. Its own words differ between the code and
> the docs too: the code initialises the catalogue (`InitialiseCatalogue`) that the docs open, the docs show "the story
> on show" that the code selects (`selectedStoryId`), and a global's value is an `ArgDto`. Instead, the titles, story
> names, ids, args and descriptions say the glossary's words (**Screen / Cast preview › One enemy**,
> `screen-cast-preview--one-enemy`, `recipient`), the catalogue is opened (`OpenCatalogue`), the docs say the selected
> story, and a control holds a `ValueDto`. The white silhouette of a battle unit, `TileFlashView`, blinks, as Blink now
> covers it. The game plays the same, and players read the same text. Builds on [Storybook](../architecture/storybook.md)
> and the [screen](screen-renames.md), [inventory](inventory-renames.md), [quest](quest-renames.md) and
> [lootable item](lootableItem-renames.md) renames, which left these words to this section.

## Decisions

| Topic          | Decision                                                                                                                                                                                                                                                                                                                |
|----------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope          | The renames below, in `storybook`, its tests and [Storybook](../architecture/storybook.md), and the blink of `screen`, with its tests and docs.                                                                                                                                                                         |
| Ids            | The component and story ids follow the new titles and names, this once: `screen-attack-preview` is `screen-cast-preview`. Chosen over keeping them, as the glossary's words matter more than the few links shared. A later rename keeps its id, as today.                                                               |
| Open           | The catalogue is opened, as the docs say: `OpenCatalogue`, `CatalogueOpened`. Chosen over Initialise, which the glossary avoids for placing buildings and npcs.                                                                                                                                                         |
| Value          | What a control holds is a value: an arg, of a component's control, or a global. `ArgDto` is `ValueDto`. `Args` and `SearchArgs` keep their names, as they only hold args.                                                                                                                                               |
| Selected story | The comments and docs say the selected story, as the code does, for "the story on show".                                                                                                                                                                                                                                |
| Record         | The comments and docs say an action is recorded, as the code does, for "logged" and "the log of actions".                                                                                                                                                                                                               |
| Location       | Location stays, in the code and the docs, for what the page URL keeps. The docs say location where they mean it (**Links** is **Location**), and link only for the URL a person shares.                                                                                                                                 |
| Words kept     | Action, as Storybook's own word for a callback. Viewport, meaning where and at what size the Storybook is drawn, as the game screen's does. **Screen / Gold button › Faded**, as that button can still be tapped to read why, so it is not Disabled. **Quest tracker** and **Quest journal**, which follow their views. |
| Blink          | The white silhouette blinking over a battle unit that receives something (`TileFlashView`) is a blink, as the glossary's Blink now says, so the flash names of `screen` say blink.                                                                                                                                      |
| Save format    | Not reached: no snapshot is renamed, so `SAVE_VERSION` does not change.                                                                                                                                                                                                                                                 |
| Player text    | None: the Storybook's texts are in English only, outside the catalogs, and no translation key changes.                                                                                                                                                                                                                  |
| Tests          | No new tests: a rename changes no behaviour. The existing tests are renamed with the code, and `StorySmokeTest` lists the new ids.                                                                                                                                                                                      |
| Out of scope   | The stories of units and terrains, waiting for their sections. See [Not in this plan](#not-in-this-plan). Devlog entries stay as written.                                                                                                                                                                               |

## storybook

### Domain and use cases

| Today                                            | Glossary term  | New name                                     |
|--------------------------------------------------|----------------|----------------------------------------------|
| `InitialiseCatalogue`, `InitialiseCatalogueTest` | Open           | `OpenCatalogue`, `OpenCatalogueTest`         |
| `StorybookEvent.CatalogueInitialised`            | Open           | `CatalogueOpened`                            |
| `StorybookApi.initialiseCatalogue`               | Open           | `openCatalogue`                              |
| "while the catalogue is not initialised"         | Open           | "while the catalogue is not open"            |
| `Storybook.Dto.ArgDto`, `ArgDto.TypeDto`         | Value          | `ValueDto`, `ValueDto.TypeDto`               |
| "the story on show", "on show"                   | Selected story | "the selected story", "selected"             |
| "logs a callback", "the log of actions"          | Record         | "records a callback", "the actions recorded" |

- `ValueDto` is the value of an arg or a global: `Storybook.create(defaultGlobals: Map<String, ValueDto>)`,
  `changeArg(name, value: ValueDto)`, `changeGlobal(name, value: ValueDto)`, `ChangeArg`, `ChangeGlobal`,
  `SearchArgs`, `SearchGlobals`, `StoryContext.action(setArg = "selectedSlot" to ValueDto.text("3"))`, and every story
  binding, e.g.: `args = mapOf(GOLD to ValueDto.int(120))`.
- The comments of `Storybook`, its use cases, `StoryLocation`, `StorybookPage`, `StorybookScene`, `StorybookPresenter`,
  `SidebarView`, `ControlsView`, `ActionsView`, `ToolbarView`, `StoryInfoView`, `PhoneFrameView` and `StoryContext` say
  the selected story and record.
- The comment of `Storybook` and the `StorySmokeTest` comment "Renaming an id breaks the links shared: keep it" stay.

### Stories

| Binding                                             | Today                                                                   | New                                                                        |
|-----------------------------------------------------|-------------------------------------------------------------------------|----------------------------------------------------------------------------|
| `CastPreviewViewStories`                            | **Screen / Attack preview**, `screen-attack-preview`                    | **Screen / Cast preview**, `screen-cast-preview`                           |
| `RecipientPreviewViewStories`                       | **Screen / Receiver preview**, `screen-receiver-preview`                | **Screen / Recipient preview**, `screen-recipient-preview`                 |
| `HudViewStories`                                    | **Screen / Battle HUD**, `screen-battle-hud`                            | **Screen / HUD**, `screen-hud`                                             |
| `HudViewStories`                                    | **Attack preview**, `--attack-preview`                                  | **Cast preview**, `--cast-preview`                                         |
| `TalkViewStories`                                   | **Screen / NPC dialog**, `screen-npc-dialog`                            | **Screen / Talk panel**, `screen-talk-panel`                               |
| `ItemButtonsViewStories`                            | **Screen / Item actions**, `screen-item-actions`                        | **Screen / Item buttons**, `screen-item-buttons`                           |
| `ItemButtonsViewStories`                            | **Bags full**, `--bags-full`                                            | **No space**, `--no-space`                                                 |
| `ButtonRowViewStories`                              | **Screen / Action row**, `screen-action-row`                            | **Screen / Button row**, `screen-button-row`                               |
| `ButtonRowViewStories`                              | **One action**, **Two actions**, **Three actions**, **Disabled action** | **One button**, **Two buttons**, **Three buttons**, **Disabled button**    |
| `InventoryButtonViewStories`                        | **Screen / Bag button**, `screen-bag-button`                            | **Screen / Inventory button**, `screen-inventory-button`                   |
| `BannerViewStories`                                 | **Screen / Battle info**, `screen-battle-info`                          | **Screen / Banner**, `screen-banner`                                       |
| `RemainingSummonDurationViewStories`                | **Screen / Remaining turns**, `screen-remaining-turns`                  | **Screen / Remaining summon duration**, `screen-remaining-summon-duration` |
| `CallToActionViewStories`                           | **Screen / Player call to action**, `screen-player-call-to-action`      | **Screen / Call to action**, `screen-call-to-action`                       |
| `CallToActionViewStories`                           | **Item actions**, **Actions**                                           | **Item buttons**, **Button row**                                           |
| `BattlefieldViewStories`                            | **Move range**, **Health flash**                                        | **Walking range**, **Health blink**                                        |
| `TileHighlightViewStories`                          | **Move range**                                                          | **Walking range**                                                          |
| `TileFlashViewStories`                              | **Screen / Tile flash**, `screen-tile-flash`                            | `TileBlinkViewStories`: **Screen / Tile blink**, `screen-tile-blink`       |
| `TileBlinkViewStories`                              | **Flashing**, **Not flashing**                                          | **Blinking**, **Not blinking**                                             |
| `ShopViewStories`                                   | **Buy, bag full**, **Sell, bag full**                                   | **Buy, inventory full**, **Sell, inventory full**                          |
| `GiveGoldRefusalViewStories`                        | **Gold full**                                                           | **Maximum gold**                                                           |
| `PillViewStories`                                   | **KO**                                                                  | **Defeated**                                                               |
| `QuestTrackerViewStories`, `QuestDetailViewStories` | **Reach**, **Counted**                                                  | **Visit**, **Defeats counted**                                             |

Each story id follows its component id and name, e.g.: `screen-shop--buy-inventory-full`, `screen-pill--defeated`,
`screen-quest-tracker--visit`, `screen-cast-preview--one-enemy`.

- `CastPreviewViewStories`: the args `receiver`, `receivers` and `receiverHealthAfter` are `recipient`, `recipients`
  and `recipientHealthAfter`, with `RECIPIENT`, `RECIPIENTS`, `RECIPIENT_HEALTH_AFTER`, `DEFAULT_RECIPIENT`,
  `RECIPIENT_MANA` and `RECIPIENTS_LIMIT`. The `receiver` of the give gold stories stays: a receiver is given gold.
- `ShopViewStories`: `BAG_USED` and its arg `bagUsed` are `INVENTORY_USED` and `inventoryUsed`, `BAG_SIZE` is
  `INVENTORY_SIZE`, and `buyBagFull` and `sellBagFull` are `buyInventoryFull` and `sellInventoryFull`.
- `ItemButtonsViewStories`: `bagsFull` is `noSpace`, and `ACTIONS` and its arg `actions` are `KIND` and `kind`, the kind
  of item.
- `CallToActionViewStories`: `CallToAction.FINISH_TURN`, `ITEM_ACTIONS` and `ACTIONS` are `FINISH_PLAYER_TURN`,
  `ITEM_BUTTONS` and `BUTTON_ROW`, and `FINISH_TURN_ENABLED` and its arg `finishTurnEnabled` are
  `FINISH_PLAYER_TURN_ENABLED` and `finishPlayerTurnEnabled`.
- `BattlefieldViewStories`: `MOVE_RANGE` is `WALKING_RANGE`, the layer `HEALTH_FLASH` is `HEALTH_BLINK`,
  `flashHealthOnScreen` is `blinkHealthOnScreen`, `isFlashed` is `isBlinked`, and `LOOT`, its arg `loot`,
  `DEFAULT_LOOT` and `LOOT_TILE` are `DROPPED_ITEM`, `droppedItem`, `DEFAULT_DROPPED_ITEM` and `DROPPED_ITEM_TILE`.
- `TileBlinkViewStories`: `FLASHING`, its arg `flashing` and `notFlashing` are `BLINKING`, `blinking` and `notBlinking`.
- `BattleUnitInfoViewStories`: the arg `remainingTurns` is `remainingSummonDuration`.
- `QuestSample.REACH` and `COUNTED` are `VISIT` and `DEFEATS_COUNTED`, titled **Visit** and **Defeats counted**, the
  latter described as "A defeat task under way: 3 of its 5 required defeats."
- The descriptions say the glossary's words, read once each for bag, a cast's receiver, loot, flash, move range and KO.
  E.g.: **Equippable** says "An item to wear, e.g.: a hammer", **No space** says "Give reads No space, faded", **Buy,
  inventory full** says "A buyer with no storage space left in its inventory", **Defeated** says "A recipient a cast
  would defeat: a solid pill reading KO", and **Battle unit info** says "its inventory button" for "its bag".
- `StorybookApi.STORY_BINDINGS` lists `TileBlinkViewStories`.

## screen

| Today                                                                         | Glossary term | New name                                                                    |
|-------------------------------------------------------------------------------|---------------|-----------------------------------------------------------------------------|
| `TileFlashView`                                                               | Blink         | `TileBlinkView`                                                             |
| `BattlefieldView.flashBattleUnit`, `flashHealth`                              | Blink         | `blinkBattleUnit`, `blinkHealth`                                            |
| `BattlefieldView.FLASH`, `FLASH_COUNT`, `FLASH_DURATION`                      | Blink         | `BLINK`, `BLINK_COUNT`, `BLINK_DURATION`                                    |
| `HEALTH_FLASH`, `HEALTH_FLASH_DURATION`, `HEALTH_FLASH_FADE_DURATION`         | Blink         | `HEALTH_BLINK`, `HEALTH_BLINK_DURATION`, `HEALTH_BLINK_FADE_DURATION`       |
| `healthFlashes`, `putHealthFlash`, `takeHealthFlash`, `removeHealthFlash(es)` | Blink         | `healthBlinks`, `putHealthBlink`, `takeHealthBlink`, `removeHealthBlink(s)` |
| `leaderHealthFlash`, `memberHealthFlash`, `tileFlashes`, `previousFlashes`    | Blink         | `leaderHealthBlink`, `memberHealthBlink`, `tileBlinks`, `previousBlinks`    |
| `BattlefieldPresenter.flashUnit`, `flashHealth`, `removeHealthFlashes`        | Blink         | `blinkUnit`, `blinkHealth`, `removeHealthBlinks`                            |
| `InventoryPresenter.flashReceiver`                                            | Blink         | `blinkReceiver`                                                             |
| "flash", "flashes", "flashed", "flashing"                                     | Blink         | "blink", "blinks", "blinked", "blinking"                                    |

- The comments of `BattlefieldView`, `BattlefieldPresenter`, `InventoryPresenter`, `ExplorationPresenter` and
  `BattlefieldHealthBarPreviewView` say blink.

## Docs

- [Storybook](../architecture/storybook.md): says the selected story, record, open and `ValueDto`. Its **Links**
  section is **Location**, and its Ubiquitous language table points to the glossary's Storybook section. Its
  examples use the new ids, e.g.: `screen-shop--sell-inventory-full`.
- [How to add a story](../testing/how-to-add-a-story.md): `ValueDto`.
- [Exploration](../architecture/exploration.md) and [Presentation timeline](../architecture/presentation-timeline.md):
  blink, `blinkBattleUnit`, `blinkHealth`, `BLINK_COUNT`, `BLINK_DURATION`, `HEALTH_BLINK` and
  `HEALTH_BLINK_DURATION`.
- [Dependencies](../architecture/dependencies.html): `TileBlinkView`, `TileBlinkViewStories` and `OpenCatalogue`.
- The `to-plan` skill: its examples say `HEALTH_BLINK_DURATION` and `lingering-health-blink.md`.

## Tests

- `StorySmokeTest` lists the new ids.
- `StorybookEndToEndTest`, `StorybookTest`, `StorybookMother`, `StorybookPresenterTest` and the use case tests say
  `ValueDto`, `OpenCatalogue` and `CatalogueOpened`, and the new ids where they open a story.
- `BattlefieldPresenterTest`: `flashedBattleUnits`, `flashedHealth`, `FlashHealth`, `FlashUnit` and
  `removeHealthFlashesCount` are `blinkedBattleUnits`, `blinkedHealth`, `BlinkHealth`, `BlinkUnit` and
  `removeHealthBlinksCount`, and the test names say blink.

## Not in this plan

- The screenshots still to be approved (the visit tasks, the formation hints, the world builder, the action row and
  the sell price) link the old ids, e.g.: `screen-quest-tracker--reach`. Once renamed, those links open the first
  story.

### Waiting for other sections

- `unit`: **Screen / Unit portrait**, **Unit name** and **Unit sprite**, and the `unit` args of the stories, while the
  glossary avoids Unit for a battle unit.
- `terrain`: **Screen / Terrain tile**, its `transition` and `wangIndex` args, and its **Sand to void** story.

## Changes while building

- The `callToAction` control of **Screen / Call to action** and the `refusal` control of **Screen / Shop** are picks, not
  enums: `FINISH_PLAYER_TURN`, `BUTTON_ROW` and `INVENTORY_FULL` made their buttons wrap over the Actions, which
  `StorySmokeTest` caught. A pick goes round its options on one row. This is why the screen renames had kept
  `FINISH_TURN` and `ITEM_ACTIONS`.
- More story enums say the glossary's words: `Refusal.BAG_FULL` of `ShopViewStories` is `INVENTORY_FULL`, and
  `Kind.BAGS_FULL` of `ItemButtonsViewStories` is `NO_SPACE`, with its `actions` as `buttons` and `PRIMARY_ACTIONS` as
  `PRIMARY_BUTTONS`.
- `TileHighlightView.Kind.MOVE_RANGE` is `WALKING_RANGE`, which the screen renames had missed, as the **Walking range**
  story of **Screen / Tile highlight** displays it.
- More descriptions say the glossary's words than the plan named: those of **Health bar preview**, **Mana bar
  preview**, **Preview bar**, **Recipient preview**, **HUD**, **Talk panel**, **Unit name**, **Virtual pad**, **Game
  button**, **Gold button** and **Give gold**. **Cast preview › Defeat** says the recipient reads KO.
- The `remainingTurns` parameter that `BattleUnitInfoViewStories` hands to the screen's `BattleUnitInfo` keeps its
  name: it is the screen's, outside this plan.
- [Storybook](../architecture/storybook.md) says that only these renames changed the ids, once, and the test names say
  open, selected story and record.
