# Plan: Rename the code of the inventory subdomain to the words of the glossary

> **Status:** done.
>
> **Summary:** The code and the player text of the inventory subdomain use words the [glossary](../../GLOSSARY.md)
> rejects. E.g.: players read "Inventory" on the title of the panel and "Inventory full" when an item found does not fit,
> but "Bag 11/12" and "Bag full" in the shop and "Bags full" in the item actions, for the same thing. The most gold is a
> "cap" in one comment, a "limit" in `GoldAboveLimit` and "the most it can carry" in another, and bare "give" means giving
> an item (`GiveRefusalDto`, `SearchGiveReceivers`) while giving gold says gold (`GiveGoldRefusalDto`,
> `SearchGoldReceivers`). Reading the code and talking about the game take a translation. Instead, the classes, functions,
> enum values, comments and player text use the glossary's words. The game plays the same. Builds on
> [Inventory](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/inventory.md) and [Exploration](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/exploration.md).

## Decisions

| Topic        | Decision                                                                                                                                                                                                                      |
|--------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope        | The renames below, in the code, tests and docs of `inventory`, and of `screen` and `storybook` where they call these names.                                                                                                   |
| Save format  | Not reached: the saved `maximumStorageSpace`, `storedItems`, `equippedItems` and `gold` keep their names, and events are not saved, so `SAVE_VERSION` stays 7.                                                                |
| Player text  | "Bag" is "Inventory" in the shop and the item actions, in `messages.pot` and `es.po`. Chosen over keeping "Bag" for players, as they would read two words for one thing. The translation keys stay, see the `screen` section. |
| Give         | Give covers items and gold, so every name only about an item says item, as `GiveItem` and `GiveGold` already do. "Receive" stays plain English in `ItemReceived` and `GoldReceived`.                                          |
| Room         | Room is only the gold an inventory can still carry, so `goldRoom` and `GoldReceiverDto.room` keep their names. Items say storage space.                                                                                       |
| Tests        | No new tests: a rename changes no behaviour. The existing tests are renamed with the code.                                                                                                                                    |
| Out of scope | The translation keys, `UiText` and the screen's and storybook's own names, which wait for their sections, and the open question. See [Not in this plan](#not-in-this-plan). Devlog entries and done plans stay as written.    |

## Renames

| Today                                                           | Glossary term  | New name                                         |
|-----------------------------------------------------------------|----------------|--------------------------------------------------|
| `InventoryItemNotFound`                                         | Stored item    | `ItemNotStored`                                  |
| `GoldAboveLimit`                                                | Maximum gold   | `GoldAboveMaximum`                               |
| `TradeRefusalDto.BAG_FULL`                                      | Inventory full | `INVENTORY_FULL`                                 |
| `GiveRefusalDto.BAGS_FULL`                                      | Inventory full | `INVENTORIES_FULL`                               |
| `GiveRefusalDto`                                                | Give, Refusal  | `GiveItemRefusalDto`                             |
| `Inventory.give`, `receive`, `giveRefusal`                      | Give           | `giveItem`, `receiveItem`, `giveItemRefusal`     |
| `SearchGiveRefusal`, `searchGiveRefusal`                        | Give, Refusal  | `SearchGiveItemRefusal`, `searchGiveItemRefusal` |
| `SearchGiveReceivers`, `searchGiveReceivers`                    | Receiver       | `SearchItemReceivers`, `searchItemReceivers`     |
| `ItemSold.price`, `price` of `Inventory.sell` and `sellRefusal` | Sell           | `sellPrice`                                      |

## inventory

### Domain

- `Inventory` follows the table. Its comments say the items an inventory stores, not carries, and the maximum gold, not
  "the most it can carry": `MAXIMUM_GOLD` is "The maximum gold of an inventory", and `sellRefusal` refuses a sell price
  that takes the gold above its maximum gold.
- `Inventory.grantReward` drops the items left "as a grant can not fail", and `receiveGold` fails above the maximum gold
  "as a give, unlike a grant, can fail", for "can not be refused" and "can be refused".
- `Inventory.Dto`: the comment of `GiveGoldRefusalDto.GOLD_FULL` says every other party member is at its maximum gold.
- `InventoryError`: `ItemNotStored` and `GoldAboveMaximum` keep their messages, but "The inventory gold is above its
  maximum gold". `NotTheBattleUnitPlayerTurn` says "The inventory can only be used during the player turn of its battle
  unit's player".
- `InventoryEvent`: `ItemSold.sellPrice`. `RewardGranted` says "the gold below the maximum gold and the items that fit in
  the storage spaces left", for "under the cap" and "in the bag".

### Use cases

- `SearchGiveItemRefusal`, `SearchItemReceivers` and `GiveItem` follow the table.
- `GrantReward` grants a reward, for "pays", drops what does not fit in the inventory, for "its bag", and is not limited
  to the player turn.
- `EquipItem`: equipping in a zone that holds an item unequips it, for "it is a swap", as Swap is two occupants exchanging
  their tiles.
- `PlayerTurnRule`: the inventory is only used during the player turn of the party player, although it costs no action,
  for "the turn" and "turn action".
- `CreateInventory`, `StoreLootedItem` and `SearchInventoryByBattleUnitId` say summons, for "summoned battle units".
- `SearchGiveGoldRefusal` says every other member is at its maximum gold.

### Adapters

- `InventoryApi`: `searchGiveItemRefusal` and `searchItemReceivers`.

## screen

- `InventoryPresenter`, `ShopPresenter` and `ShopView` follow the table: `GiveItemRefusalDto.INVENTORIES_FULL`,
  `TradeRefusalDto.INVENTORY_FULL`, `searchGiveItemRefusal` and `searchItemReceivers`. Their own names stay, see
  [Not in this plan](#not-in-this-plan).

## storybook

- `ShopViewStories` and `ItemActionsViewStories` follow the table. Their own names stay.

## Translations

- `resources/localization/messages.pot` and `es.po`, keeping their keys:
  - `ui.shop.bag`: "Inventory {used}/{maximum}", "Inventario {used}/{maximum}".
  - `ui.shop.bagFull` and `ui.shop.noRoom`: "Inventory full", "Inventario lleno".
  - `ui.itemActions.bagsFull`: "Inventories full", "Inventarios llenos".
  - The comments of `ui.shop.buyer` (e.g.: Inventory 3/12), `ui.shop.bag` (the stored items, and the storage spaces),
    `ui.shop.bagFull`, `ui.shop.noRoom`, `ui.itemActions.bagsFull` and `ui.giveGold.cancel` say inventory, and
    `ui.giveGold.goldFull` says maximum gold.
- **Screen / Shop › Buy, bag full** and **Screen / Item actions › Bags full** are looked at with the `storybook` skill,
  as the buyer line (e.g.: `< Knight · 20 G · Inventory 11/12 >`) and the Give button get longer.

## Docs

- [Inventory](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/inventory.md): the inventory for "the bag", storage spaces, maximum gold for "cap" and
  "limit", grant for "pay", player turn, summons, the renamed names, and equipping over an item unequips it, with no
  swap. The examples say `Inventory full` and `Inventories full`.
- [Exploration](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/exploration.md): 12 storage spaces for "12 slots", the inventory for "its bag" where it
  means the inventory, and the examples say `Inventory 11/12`, `Inventory full` and `Inventories full`.
- `docs/architecture/dependencies.html` is generated again with `./gradlew dependencyReport`, as it names `GiveRefusalDto`.

## Tests

- The tests that use these names are renamed with the code, with no change in what they assert, including
  `InventoryTest`, `SellItemTest`, `EquipItemTest`, `GiveGoldTest`, `DiscardItemTest`, `ConsumeItemTest`,
  `UnequipItemTest`, `SearchBuyRefusalTest` and `InventoryPresenterGiveGoldTest`. `SearchGiveRefusalTest` and
  `SearchGiveReceiversTest` are `SearchGiveItemRefusalTest` and `SearchItemReceiversTest`.
- Test names say the same words, asserting the same:
  - `InventoryTest`: `Inventory should request dropping the items left when the inventory is full`.
  - `GrantRewardTest`: `GrantReward should request dropping the items left when the inventory is full`.
  - `OnRequestGrantRewardTest`: `should grant the reward when it is requested`.
  - `GiveItemTest`: `should move one copy of the item to the end of the receiver's inventory`, and `should fail, keeping
    both inventories, when ...`.
  - `SearchGiveGoldRefusalTest`: `should answer gold full when every other member is at its maximum gold`.
  - `SearchGoldReceiversTest`: `should leave out the members at their maximum gold`.
  - `EquipItemTest`: `should apply the new item effects before removing the unequipped item effects when the zone
    already holds an item`.
  - `EquipItemTest`, `DiscardItemTest` and `ConsumeItemTest`: `should fail when it is not the player turn of the party
    player`.

## Not in this plan

- The comment of `Inventory.create` and [Inventory](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/inventory.md) say an inventory is created when its
  battle unit is deployed (`BattleUnitDeployed`), while `OnExplorationStarted` and `OnPartyJoined` create it, for the
  party members only. The docs look out of date, still to be fixed.

### Waiting for other sections

- `lootableItem`: Item and Lootable item are still to be defined, as the inventory says item for a lootable item
  (`ItemStored`, `storedItems`, `lootableItemId`, `InventoryLootableItemDoesNotExist`). Equipment zone, Equippable,
  Consumable, Price and Sell price, which Equip, Consume, Buy and Sell use.
- `quest`: Reward, which the inventory grants (`RequestGrantReward`, `RewardGranted`).
- `party`: Party member and Exploring, which Inventory, Give and Trade use (`NotExploringTheParty`, `NOT_EXPLORING`,
  `GiveRule`).
- `npc`: Merchant and its stock, which Trade uses (`TradeRule.merchantStock`, `NotTradingWithAMerchant`,
  `ItemNotSoldByTheMerchant`).
- `lootDrop`: Loot, Drop and Pick up, in `StoreLootedItem`, `RequestDropItems` and `RequestPickUpDroppedItem`.
- `screen`: the keys `ui.shop.bag`, `ui.shop.bagFull`, `ui.shop.noRoom` and `ui.itemActions.bagsFull`, the `{bag}`
  placeholder of `ui.shop.buyer`, and `UiText`'s `SHOP_BAG`, `SHOP_BAG_FULL`, `SHOP_NO_ROOM` and
  `ITEM_ACTIONS_BAGS_FULL`, while Bag is avoided and Room only means gold. `BagButtonView`, `onBagSelected`, `hideBag`,
  `displayBagAgain` and `BAG_SIZE`. The choice of what to do with an item found while the inventory is full, which
  players read as "Tap an item to swap it for", while Swap is two occupants exchanging their tiles.
- `storybook`: `BAG_USED`, `BAG_SIZE`, `buyBagFull`, `sellBagFull` and `bagsFull` in `ShopViewStories` and
  `ItemActionsViewStories`, the stories **Buy, bag full**, **Sell, bag full** and **Bags full**, and "no room left in its
  bag".

## Changes while building

- The Give button reads "No space" ("Sin espacio"), not "Inventories full", in `ui.itemActions.bagsFull`: **Screen / Item
  actions › Bags full** showed "Inventories …" and "Inventarios …" cut on the button, a quarter of the row. It says storage
  space, the glossary's word, and fits like "No one" beside it. [Buttons](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/buttons.md),
  [Presentation timeline](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/presentation-timeline.md) and [Exploration](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/exploration.md) say
  `No space`, and its translator comment says the other members have no storage space left.
- The screen tests that read the player text follow it: `ShopPresenterTest`, `ShopViewTest` and `ShopViewExtension` say
  `Inventory 0/12` and `Inventory full`, and `InventoryPresenterGiveTest` says `No space`.
- More test names and comments than the plan named say the same words: `UnequipItemTest` and `PlayerTurnRuleTest` say
  player turn, `StoreLootedItemTest` says the bee is a summon, the comment of `GiveItemTest` says stores and inventory,
  and the domain tests of giving an item are `GiveItem`, `ReceiveItem` and `GiveItemRefusal`.
- [Inventory](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/inventory.md) says the bag button where it means the button that opens the inventory, as
  it waits for the `screen` section with `BagButtonView`.
- `InventoryPresenterGiveGoldTest` keeps "at the limit" and "the bag" in its test names, as it is the screen's and waits
  for its section.

