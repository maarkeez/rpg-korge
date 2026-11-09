# Plan: Rename the code of the screen subdomain to the words of the glossary

> **Status:** built on `main` in `f40b048e`, with its screenshots of the inventory full message still to be approved.
>
> **Summary:** The screen names what players see with words the [glossary](../../GLOSSARY.md) gives to other things, or
> rejects. E.g.: `BattleScene` holds the whole game, exploring included. `BattlefieldHud` is not the HUD but what the
> player has selected. The tiles the knight can walk to with its 3 steps are its "movement range" (`tilesWhereCanBeMoved`),
> while Movement range is avoided. The forecast of the mage's Mushroom, a heal, is an `AttackPreviewView` whose rows are
> `Receiver`s, while Receiver is the party member a give goes to. The screen's buttons are "actions" (`ActionRowView`,
> `ItemActionsView`), while an Action is a step or a cast. And the chest that opens the inventory is a `BagButtonView`,
> while Bag is avoided. Players also read "Tap an item to swap it for: Hammer (5 G)" when an item found does not fit, while
> Swap is two occupants exchanging their tiles. So reading the code and talking about the game take a translation.
> Instead, the classes, functions, translation keys, comments and docs say the glossary's words, and players read "Tap an
> item to discard for". The game plays the same. Builds on [Presentation timeline](../architecture/presentation-timeline.md),
> [Exploration](../architecture/exploration.md) and [Buttons](../architecture/buttons.md), and takes what the
> [ability](ability-renames.md), [battle](battle-renames.md), [battlefield](battlefield-renames.md),
> [battle setup](battlesetup-renames.md), [battle unit](battleUnit-renames.md), [building](building-renames.md),
> [CPU wander brain](cpuWanderBrain-renames.md), [effect](effect-renames.md), [inventory](inventory-renames.md),
> [localization](localization-renames.md), [loot drop](lootDrop-renames.md), [world builder](mapBuilder-renames.md),
> [npc](npc-renames.md), [party](party-renames.md), [quest](quest-renames.md) and [save](save-renames.md) renames left
> to this section.

## Decisions

| Topic         | Decision                                                                                                                                                                                                                                                                      |
|---------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope         | The renames below, in the code, tests, comments, translation keys and docs of `screen`, and where `cpuWanderBrain`, `storybook` and `Main.kt` call these names.                                                                                                               |
| Game screen   | `BattleScene` is `GameScene`, as it holds the whole game, from a new game or a loaded save until quitting.                                                                                                                                                                   |
| HUD           | One HUD, in battles and while exploring: `BattleHudView` is `HudView`, and `HudStyle` says the HUD, for "the battle HUD".                                                                                                                                                     |
| Selection     | The nested `battlefieldHud` subdomain is the Selection, as it holds what the player has selected, not the HUD. Its states and events are named after what is selected, not what is displayed.                                                                               |
| Walking range | The tiles the selected battle unit can walk to are its walking range, as only a walk counts, while a Move is also a swap or a teleport, and Movement range is avoided.                                                                                                       |
| Cast preview  | The forecast of a cast is the cast preview, as it previews heals too, and its rows are recipients, the effect section's word, as Receiver is the party member a give goes to.                                                                                               |
| Banner        | `BattleInfoView` is the banner, as it also says Exploring, and Header is avoided for a summary and taken by the battle unit info's header.                                                                                                                                  |
| Player turn   | The code says player turn, while players keep reading the short "Finish turn", "{player} turn" and "turns".                                                                                                                                                                  |
| Victory       | The code says victory and finished battle, while players keep reading "{player} wins!" and "Battle over!".                                                                                                                                                                   |
| Camera        | The camera is what part of the battlefield is visible, and the viewport where the game screen draws it, as the code already says. `VIEW_TILES` of `cpuWanderBrain` is `VISIBLE_TILES`, as it mirrors the camera.                                                            |
| Talk panel    | `NpcDialogView` is the talk panel, as Dialog is avoided for a line and the party talks to an npc.                                                                                                                                                                            |
| Shop          | `ShopView` keeps its name: the shop is the panel where a party member trades with a merchant.                                                                                                                                                                                |
| Poison        | The glossary has no Poison: the ability info's effects are named after their timing and outcome, while players keep reading "Poisons".                                                                                                                                      |
| Buttons       | The screen's buttons are no actions: the button row and the item buttons. The call to action keeps its name, as the place under the HUD, an idiom of its own.                                                                                                              |
| Player        | `BattleUnitInfo.isAlly` means the human player's, so it is `isHumanPlayers`, and "team" is player in comments, as Ally is another battle unit of the same player.                                                                                                           |
| Player text   | Only "Tap an item to swap it for" changes, to "Tap an item to discard for", as Swap is two occupants exchanging their tiles. HP, MP, MOVE, CAST and KO, "An empty tile next to you", "next to {neighbour}", "Poisons" and "Heals you 10 HP" stay, as short captions and plain words. |
| Save format   | Not reached: the screen has no snapshot, and events are not saved, so `SAVE_VERSION` stays 9.                                                                                                                                                                                |
| Tests         | No new tests: a rename changes no behaviour. The existing tests are renamed with the code.                                                                                                                                                                                   |
| Out of scope  | The storybook's own names, titles and story ids, which wait for its section. See [Not in this plan](#not-in-this-plan). Devlog entries and done plans stay as written.                                                                                                      |

## Renames

| Today                                                                                             | Glossary term       | New name                                                                                       |
|---------------------------------------------------------------------------------------------------|---------------------|------------------------------------------------------------------------------------------------|
| `BattleScene`, `BattleSceneTimeline`, `BattleSceneResourcesLoader`                                | Game screen         | `GameScene`, `GameSceneTimeline`, `GameSceneResourcesLoader`                                   |
| `BattleHudView`                                                                                   | HUD                 | `HudView`                                                                                      |
| Package `screen.adapters.presentation.battlefieldHud`                                             | Selection           | `screen.adapters.presentation.selection`                                                       |
| `BattlefieldHud`, `BattlefieldHudApi`, `BattlefieldHudRepository`, `InMemoryBattlefieldHudRepository` | Selection       | `Selection`, `SelectionApi`, `SelectionRepository`, `InMemorySelectionRepository`               |
| `BattlefieldHudError`, `BattlefieldHudNotFound`, `InvalidBattlefieldHudState`                     | Selection           | `SelectionError`, `SelectionNotFound`, `InvalidSelectionState`                                 |
| `BattlefieldHudEvent`                                                                             | Selection           | `SelectionEvent`                                                                               |
| `InitializeBattlefieldHud`, `initializeBattlefieldHud`                                            | Selection           | `InitializeSelection`, `initializeSelection`                                                   |
| States `Idle`, `DisplayMovementRange`, `DisplayAbilityCastRange`, `DisplayAbilityInfo`, `DisplayAbilityCastPreview` | Selection | `NothingSelected`, `BattleUnitSelected`, `AbilitySelected`, `AbilityInfoDisplayed`, `CastPreviewed` |
| Events `Idle`, `SelectedBattleUnit`, `SelectedBattleUnitAbility`                                  | Selection           | `Deselected`, `BattleUnitSelected`, `AbilitySelected`                                          |
| Events `SelfAbilityCastPreviewed`, `EnemyAbilityCastPreviewed`                                    | Cast preview        | `CastOnCasterOrVacantTilePreviewed`, `CastOnBattleUnitsPreviewed`                              |
| `previewSelfAbilityCast`, `previewEnemyAbilityCast`                                               | Cast preview        | `previewCastOnCasterOrVacantTile`, `previewCastOnBattleUnits`                                  |
| `enemyBattleUnitIds`                                                                              | Recipient           | `recipientBattleUnitIds`                                                                       |
| `tilesWhereCanBeMoved`, `MovementService.tilesWhereCanMove`, `movementService`                    | Walking range       | `walkingRange`, `WalkingRangeService.walkingRange`, `walkingRangeService`                      |
| `RefreshMovementRange`, `refreshMovementRange`                                                    | Walking range       | `RefreshWalkingRange`, `refreshWalkingRange`                                                   |
| `AttackPreviewView`, `ReceiverPreviewView`, `AttackPreviewView.Receiver`                          | Cast preview        | `CastPreviewView`, `RecipientPreviewView`, `CastPreviewView.Recipient`                         |
| `BattleInfoView`, `BattleInfoPresenter`                                                           | Banner              | `BannerView`, `BannerPresenter`                                                                |
| `announceBattleEnd`, `displayPlayerWin`                                                           | Finished, Victory   | `announceBattleFinished`, `displayVictory`                                                     |
| `FinishTurnView`, `FinishTurnPresenter`, `announceTurn`, `isHumanTurnDisplayed`                   | Player turn         | `FinishPlayerTurnView`, `FinishPlayerTurnPresenter`, `announcePlayerTurn`, `isHumanPlayerTurnDisplayed` |
| `VIEW_TILES` (`cpuWanderBrain`)                                                                   | Camera              | `VISIBLE_TILES`                                                                                |
| `NpcDialog`, `NpcDialogView`                                                                      | Talk panel          | `TalkInfo`, `TalkView`                                                                         |
| `AbilityInfo.Effect.DamageEachTurn`, `OnDefeatedPoisonAllies`, `HealSelf`, `isPoison`             | Ability info        | `DamageOnPlayerTurnStarted`, `OnDefeatedDamageAdjacentAllies`, `HealCaster`, `isDamageOnPlayerTurnStarted` |
| `ActionRowView`, `displayActions`, `hideActions`                                                  | Button row          | `ButtonRowView`, `displayButtons`, `hideButtons`                                               |
| `ItemActionsView`                                                                                 | Item buttons        | `ItemButtonsView`                                                                              |
| `PlayerCallToActionView`                                                                          | Call to action      | `CallToActionView`                                                                             |
| `BattleUnitInfo.isAlly`                                                                           | Human player        | `isHumanPlayers`                                                                               |
| `BagButtonView`, `onBagSelected`, `hideBag`, `displayBagAgain`, `BAG_SIZE`                        | Inventory button    | `InventoryButtonView`, `onInventoryButtonSelected`, `hideInventoryButton`, `displayInventoryButtonAgain`, `MAXIMUM_STORAGE_SPACE` |
| `BattlefieldPresenter.dropLoot`, `pickUpLoot`, `displayedLoot`                                    | Dropped item, Loot  | `dropItems`, `lootDroppedItem`, `displayedDroppedItems`                                        |
| `BattlefieldView.displayLoot`, `pickUpLoot`, layers `LOOT`, `PICKED_UP_LOOT`                      | Dropped item, Loot  | `displayDroppedItems`, `lootDroppedItem`, `DROPPED_ITEMS`, `LOOTED_ITEM`                       |
| `BattlefieldView.isOneOfTheNeighbours`, `mapWidth`, `mapHeight`                                   | Adjacent            | `isAdjacent`, `battlefieldWidth`, `battlefieldHeight`                                          |
| `BattleUnitInfo.remainingHealthPoints`, `maximumHealthPoints`, `remainingManaPoints`, `maximumManaPoints` | Health, Mana | `remainingHealth`, `maximumHealth`, `remainingMana`, `maximumMana`                              |
| `cooldownTurnsLeft`                                                                               | Remaining cooldown  | `remainingCooldown`                                                                            |
| `RemainingTurnsView`                                                                              | Remaining summon duration | `RemainingSummonDurationView`                                                            |
| `QuestInfo.Task.progress`, `count`, `QuestInfo.Kind.REACH`                                        | Defeats counted, Required defeats, Visit task | `defeatsCounted`, `requiredDefeats`, `VISIT`                         |
| `UiText`, `Texts`, `texts`                                                                        | Message             | `UiMessage`, `Messages`, `messages`                                                            |
| `SaveSlotsView.Delegate.saveSlotDeleteSelected`                                                   | Save                | `deleteSaveSelected`                                                                           |

## cpuWanderBrain

- `IsInSight`: `VISIBLE_TILES`, for `VIEW_TILES`, and its comment says the camera shows 6 tiles around the leader.

## screen

### Selection

- The package `selection` follows the table: `Selection`, its states, events, error, repository and Api. Its comments say
  the selection, for "the hud", and the walking range, for "movement range", e.g.: `RefreshWalkingRange` "displays again
  the selected battle unit with its current tile and walking range".
- `ProcessTileSelected` names its private functions after the state: `selectTileWhenNothingSelected`,
  `selectTileWhenBattleUnitSelected` and `selectTileWhenAbilitySelected`.
- `ProcessAbilitySelected`, `SelectBattleUnit`, `DeselectBattleUnit`, `ConfirmCast` and `CancelCast` keep their names,
  and follow the states.

### Presentation

- `GameScene`, `GameSceneTimeline` and `GameSceneResourcesLoader` follow the table, and so does `MenuScene`, which starts
  it. `Main.kt` starts the `GameScene`.
- `HudView`, `HudStyle`: the HUD, for "the battle HUD".
- `BannerView` and `BannerPresenter`: `displayExploring`, `announceBattle`, `announceBattleFinished`,
  `announcePlayerTurn` and `displayVictory`. Their comments say the banner, for "the header" and "the battle info".
- `FinishPlayerTurnView` and `FinishPlayerTurnPresenter`, and `CallToActionView`'s `displayFinishPlayerTurn`,
  `hideFinishPlayerTurn` and `setFinishPlayerTurnEnabled`, say the player turn, for "the turn".
- `CastPreviewView` and `RecipientPreviewView` say the cast preview and its recipients, and `KO` is read from
  `UiMessage.CAST_PREVIEW_DEFEATED`.
- `BattleUnitInfo`, `BattleUnitInfoView`, `BattleUnitInfoMapper`, `AbilityButtonView`, `AbilityInfoMapper` and
  `BattlefieldPresenter` follow the table for health, mana, remaining cooldown and `isHumanPlayers`. Their comments say
  the human player's or the CPU player's, for "its team".
- `AbilityInfoView.AbilityInfo.Effect` follows the table. `Targeting` already says the glossary's words.
- `TalkInfo` and `TalkView`: the talk panel, for "the npc dialog", and `ShopPresenter` returns "to the talk panel".
- `ButtonRowView`, `ItemButtonsView`, `CallToActionView.displayButtons` and `hideButtons`, and the presenters calling
  them: `InventoryPresenter`, `ShopPresenter`, `QuestPresenter`, `PausePresenter`, `FormationPresenter`,
  `LoadGamePresenter` and `MenuView`.
- `InventoryButtonView` and `InventoryPresenter` follow the table: the inventory button, for "the bag".
- `BattlefieldView` and `BattlefieldPresenter` follow the table for the dropped items and looting, and say "looted" for
  "picked up", e.g.: `PICK_UP_RISE` is `LOOT_RISE`.
- `RemainingSummonDurationView` tells how much of its summon duration a summon has left.
- `QuestInfo`, `QuestMapper` and the quest views follow the table, and their comments say the quest announcement, for
  "the toast".
- `FormationPresenter` and `FormationView` say tapping another member reorders both, for "swaps both".
- `SaveSlotsView.Delegate.deleteSaveSelected`, and `SaveSlotsMapper` says each slot shows Empty, Incompatible save, or the
  summary of its save.
- `UiMessage` and `Messages` follow the table, with every `UiMessage` entry renamed with its key below, e.g.:
  `SHOP_BAG_FULL` is `SHOP_INVENTORY_FULL`. `MenuView` says selecting a language translates the game, for "picking one".

## storybook

- The stories follow the table where they call the screen's names, e.g.: `CastPreviewView.Recipient`, `remainingHealth`,
  `isHumanPlayers` and `TalkInfo`. Their own names stay, see [Not in this plan](#not-in-this-plan).
- `InventoryFullViewStories` and `PlayerCallToActionViewStories` quote "Tap an item to discard for", as the game reads.

## Translations

- `resources/localization/messages.pot` and `es.po`, keeping the text unless said:
  - `ui.battleInfo.*` are `ui.banner.*`, with `ui.banner.victory` for `playerWins` and `ui.banner.battleFinished` for
    `battleOver`. Their comments say the banner, for "the header".
  - `ui.finishTurn.finishTurn` is `ui.finishPlayerTurn.finishPlayerTurn`.
  - `ui.attackPreview.*` are `ui.castPreview.*`, with `ui.castPreview.defeated` for `ko`.
  - `ui.npcDialog.*` are `ui.talk.*`, and the comment of `ui.talk.leave` says it closes the talk panel.
  - `ui.itemActions.*` are `ui.itemButtons.*`, with `ui.itemButtons.noSpace` for `bagsFull`.
  - `ui.shop.bag`, `ui.shop.bagFull` and `ui.shop.noRoom` are `ui.shop.inventory`, `ui.shop.inventoryFull` and
    `ui.shop.noStorageSpace`, and the `{bag}` placeholder of `ui.shop.buyer` is `{inventory}`.
  - `ui.abilityInfo.targeting.tileNextToCaster` and `tileNextToAnotherUnit` are `vacantTileAdjacentToCaster` and
    `vacantTileAdjacentToAnotherBattleUnit`.
  - `ui.abilityInfo.effect.damageEachTurn`, `healSelf` and `onDefeatedPoisonAllies` are `damageOnPlayerTurnStarted`,
    `healCaster` and `onDefeatedDamageAdjacentAllies`, and the `{healing}` placeholder is `{heal}`.
  - The `{neighbour}` placeholder of `ui.worldBuilder.forbiddenAdjacentTerrains`, and of the comments that quote it, is
    `{adjacentTerrain}`.
  - `ui.saveSlots.saved` is `ui.saveSlots.summary`.
  - `ui.inventoryFull.message` reads "Tap an item to discard for: {item} ({sellPrice})", "Toca un objeto para descartarlo
    por: {item} ({sellPrice})", and the comment of `ui.inventoryFull.leave` says it leaves the item found on its tile.
- **Screen / Inventory full** is looked at with the `storybook` skill, in English and Spanish, as its message changes.

## Docs

- [Presentation timeline](../architecture/presentation-timeline.md): the game screen, the banner, the selection, the
  walking range, the cast preview and its recipients, the dropped items and the `DROPPED_ITEMS` layer for the Loot
  heading, the quest announcement for "toast", and the renamed names.
- [Exploration](../architecture/exploration.md), [Buttons](../architecture/buttons.md),
  [Quests](../architecture/quests.md), [Inventory](../architecture/inventory.md) and
  [Save and load](../architecture/save-and-load.md): the button row, the item buttons, the call to action, the talk panel,
  the inventory button for "the bag button", the quest announcement and the renamed names.
- [Localization](../architecture/localization.md): `UiMessage` and `Messages`, and a UI message for "UI text", e.g.:
  How to add a UI message.
- [Package structure](../architecture/package-structure.md), [Assets](../architecture/assets.md),
  [Game data](../architecture/game-data.md), [CPU battle brain](../architecture/cpu-battle-brain.md) and
  [Dependencies](../architecture/dependencies.md): `GameScene`, the `selection` package and `VISIBLE_TILES`.
- `docs/architecture/dependencies.html` is generated again with `./gradlew dependencyReport`.

## Tests

- The tests that use these names are renamed with the code, with no change in what they assert, including the tests of
  the selection (`ProcessTileSelectedTest`, `ProcessAbilitySelectedTest`, `RefreshMovementRangeTest` as
  `RefreshWalkingRangeTest`, `MovementServiceTest` as `WalkingRangeServiceTest`), `BattleInfoPresenterTest` as
  `BannerPresenterTest`, `FinishTurnPresenterTest` as `FinishPlayerTurnPresenterTest`, `AttackPreviewViewTest` as
  `CastPreviewViewTest`, `BattlefieldPresenterTest`, `BattlefieldViewTest`, `InventoryPresenterTest`,
  `QuestMapperTest`, `TextsTest` as `MessagesTest`, and the end-to-end tests starting the `GameScene`.
- Test names say the same words, asserting the same:
  - `QuestMapperTest`: `questInfo should count the defeats of a defeat task and 1 for a talk or a visit`.
  - `BattlefieldViewTest`, `BattlefieldPresenterTest` and `BattlefieldViewExtension`: "looted" for "picked up".
  - `InventoryPresenterGiveGoldTest`: "at its maximum gold" and "the inventory" for "at the limit" and "the bag".
  - `MessagesTest`: `languages should return English first, then the other languages, named in their language`.
- The tests reading "Tap an item to swap it for" read "Tap an item to discard for".

## Not in this plan

### Waiting for other sections

- `storybook`: the stories' own names, titles and ids, e.g.: `AttackPreviewViewStories`, `ReceiverPreviewViewStories`,
  `BattleHudViewStories` and `NpcDialogViewStories`, the components **Screen / Attack preview**, **Screen / Npc dialog**
  and **Screen / Item actions**, `BAG_USED`, `BAG_SIZE`, `buyBagFull`, `sellBagFull` and `bagsFull`, the stories **Buy,
  bag full**, **Sell, bag full** and **Bags full**, `QuestSample.REACH` and the `reach` stories, and the **Equippable**
  story's "e.g.: a sword", while the game has only hammers.

## Changes while building

- The story bindings follow their views (e.g.: `CastPreviewViewStories`, `HudViewStories`, `TalkViewStories`), as
  `StoryCoverageArchitectureTest` asks every view for a `<View>Stories` binding. Their story ids, titles, story names and
  args stay, waiting for the `storybook` section, and so do `FINISH_TURN` and `ITEM_ACTIONS` of the call to action
  stories, as `FINISH_PLAYER_TURN` made its controls cover the actions.
- The Spanish reads "Toca un objeto a descartar por: Health potion (25 G)", not "para descartarlo por", which cut
  "(25 G)" on **Screen / Inventory full › Default**. It is shorter than "para cambiarlo por".
- `AbilityInfoView.isOneOfTheNeighbours` is `isOneOfTheAdjacentTiles`, not `isAdjacent`: it lives in the ability info's
  diagram, not in `BattlefieldView`, and says the ability reaches one of the adjacent tiles marked, not all of them.
- `BAG_SIZE` of `BattleUnitInfoView` is `INVENTORY_BUTTON_SIZE`, as it is the size of the button, not a storage space.
  `InventoryButtonView.BAG`, the name of its chest, is `CHEST`.
- `LOOT_SCALE` is `DROPPED_ITEM_SCALE`, so the `PICK_UP_*` constants are `LOOTED_ITEM_RISE`, `LOOTED_ITEM_SCALE` and
  `LOOTED_ITEM_DURATION`, and the Loot heading of [Presentation timeline](../architecture/presentation-timeline.md) is
  Dropped items.
- More names follow the selection: the states' `idle()` is `deselect()`, `walkingRange(tile, walkingRange)` is
  `refreshWalkingRange`, the events built in `Selection` are written `SelectionEvent.<Event>`, as a state and an event
  share a name, and `SelectionMother` builds `nothingSelected()`, `battleUnitSelected()`, `abilitySelected()`,
  `abilityInfoDisplayed()` and `castPreviewed()`.
- More names follow the buttons: `CallToActionView.displayItemButtons`, `ItemButtonsView.Button`, and the call to action
  in `GameSceneExtension` and its tests. `isBattleHudShown` is `isHudShown`, the quest announcement's `TOAST_*`,
  `playToasts` and `fadeToast` say announcement, and `InventoryPresenter` discards a stored item for a found one
  (`askToDiscardFor`, `discardFor`), for swap.
- `QuestMapper.countOf` is `requiredDefeatsOf`, and `QuestInfoMother.task` takes `defeatsCounted` and `requiredDefeats`.
- [CPU battle brain](../architecture/cpu-battle-brain.md) divides by the maximum steps, which it called "movement range"
  as the battle unit section had avoided it, not by the walking range.
- The comments say the game screen for "the battle scene" and the player's colour for "the team's".
