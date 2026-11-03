# Plan: The sell price in the inventory

> **Status:** built on `main`. The screenshots are still to be approved.
>
> **Summary:** The inventory displays the price of an item, but a merchant pays half of it, so a Hammer reads `50 G` in the bag and `25 G` in the shop's Sell line. Instead, the inventory displays the sell price, with no label, exactly as the shop's Sell line does: on the line of a selected item, and in the message asking what to do with an item found with a full bag, so the two compare directly. The price is then only displayed when buying. The rule does not change, and no subdomain gains a dependency. It builds on [Buying and selling](../architecture/inventory.md#buying-and-selling) and [Game data](../architecture/game-data.md).

## Domain terms

No new term. The two that exist keep their names, and are now defined by who pays whom:

| Term       | Meaning                                                  | In the code                                                                     |
|------------|----------------------------------------------------------|---------------------------------------------------------------------------------|
| Price      | What a merchant charges for an item, from 0 to 999       | `LootableItem.price`, `price` in its TOML file                                  |
| Sell price | What a merchant pays for an item: half its price, rounded down | `LootableItem.sellPrice()`, `SearchSellPrice`                             |

## Decisions

| Topic           | Decision                                                                                                                                                                                                                                                                                                       |
|-----------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Selected item   | Its line displays the sell price alone, unlabelled, e.g.: `Hammer ... 25 G`, identical to the shop's Sell line. Chosen over `Sells for 25 G`, which needs a new translated text, and over both prices, which needs wording to tell them apart and takes more of the line beside the name.                       |
| Full bag        | The message displays the sell price of the found item, e.g.: `Tap an item to swap it for: Hammer (25 G)`. Chosen over keeping the price, as the Hammer would read 50 G here and 25 G once stored, and over removing the number, as the player needs it to choose what to swap.                                 |
| Where           | Every selected stored item, in every context, also during a battle and on an equipped item. The number tells what the item fetches, and whether it can be sold right now stays the shop's concern (`Equipped`, `Gold full`). Chosen over hiding it on equipped items or during battles.                       |
| What stays      | Buy in the shop keeps the price, and Sell the sell price. The Discard question displays no price. The TOML `price`, `LootableItem.Dto` and the rule of half, rounded down, do not change.                                                                                                                      |
| Term            | `sell price` stays. Chosen over `sell value` and `resale price`.                                                                                                                                                                                                                                               |
| Code names      | What now always holds the sell price is renamed: `itemPrice` to `itemSellPrice`, `itemPriceView` to `itemSellPriceView`, `foundItemPrice` to `foundItemSellPrice` and `FoundItem.price` to `FoundItem.sellPrice`. `ShopView.displayItem` keeps `itemPrice`, as it displays either.                             |
| Placeholder     | `{price}` becomes `{sellPrice}` in the message, in `messages.pot` and `es.po`, with the translators' note. It is the first placeholder of two words. Chosen over keeping `{price}`, so the catalogs say what the code says.                                                                                     |
| Definition      | [Game data](../architecture/game-data.md) drops "what it is worth", which could now mean either number. See [Docs](#docs).                                                                                                                                                                                      |
| Merchant item   | The story **Screen / Item actions › Merchant item** goes, with the branch of `ItemActionsView` it is the only one to reach and the test of it. The game never displays that state: the inventory always offers at least Cancel and Discard, and **Screen / Shop › Buy, item selected** covers a merchant's item as the game draws it. |
| Architecture    | `InventoryPresenter` reads the sell price from `lootableItemApi.searchSellPrice`, as `ShopPresenter` does. It already holds `LootableItemApi`, so `screen` to `lootableItem` stays the only dependency involved, and `SubdomainDependencyArchitectureTest` does not change. Chosen over adding `sellPrice` to `LootableItem.Dto`, which is also what gets saved, as a save holds no derived data. |
| Saving          | `SAVE_VERSION` stays as it is: nothing saved changes.                                                                                                                                                                                                                                                          |
| Delivery        | 1 milestone, green on CI and approved on screenshots. See [Milestones](#milestones).                                                                                                                                                                                                                           |

For example, with the prices of today:

| Item            | Price | The inventory, and the shop's Sell line | The shop's Buy line |
|-----------------|-------|-----------------------------------------|---------------------|
| Health Potion   | 10 G  | 5 G                                     | 10 G                |
| Health Elixir   | 25 G  | 12 G                                    | 25 G                |
| Hammer          | 50 G  | 25 G                                    | 50 G                |
| Rune Hammer     | 150 G | 75 G                                    | 150 G               |

## Milestones

Each milestone is a few commits straight on `main`, green on CI.

| #  | Milestone                     | Done when                                                                                                           | Status | Done in |
|----|-------------------------------|---------------------------------------------------------------------------------------------------------------------|--------|---------|
| M1 | The sell price in the inventory | The item line and the full-bag message display the sell price in the game, approved on screenshots, with the docs up to date. | ✅      | `f72f3e7c` |

### M1 commits

1. Remove **Merchant item**, its entry in `StorySmokeTest`, the test of an item with no actions, and the branch of `ItemActionsView`.
2. Display the sell price on the line of a selected item and in the full-bag message, with the renames, the catalogs, the stories and the tests.
3. Publish screenshots of the stories for the user to approve, in English and Spanish, and apply what comes back.
4. Update [Inventory](../architecture/inventory.md), [Game data](../architecture/game-data.md) and the [Presentation timeline](../architecture/presentation-timeline.md), and mark the plan as done.

## Reference

### InventoryPresenter

- `selectItem` reads `lootableItemApi.searchSellPrice(slot.lootableItemId) ?: return`, as it does for `searchLootableItemById`, and passes it as `itemSellPrice`.
- The subscription to `InventoryFull` builds `FoundItem` with `sellPrice`, from `searchSellPrice(event.lootableItemId) ?: return@subscribe`, and `displayFoundItem` passes it as `foundItemSellPrice`.

### Views

- `ItemActionsView`: `itemSellPrice` and `itemSellPriceView`. The buttons are always drawn, with no `isNotEmpty` check. Its comment no longer speaks of an item a merchant sells.
- `PlayerCallToActionView`: `displayItemActions(itemName, itemSellPrice, actions)` and `displayInventoryFull(foundItemName, foundItemSellPrice, foundItemId, onLeft)`. Its comment says sell price.
- `InventoryFullView`: `foundItemSellPrice`, passed to `INVENTORY_FULL_MESSAGE` as `"sellPrice"`.
- `DiscardQuestionView`: its comment says the line of the name and sell price.

### Catalogs

In `messages.pot`, and in `es.po` with `Toca un objeto para cambiarlo por: {item} ({sellPrice})`:

```
#. {item}: the name of the item found. {sellPrice}: its sell price, e.g.: 5 G.
msgctxt "ui.inventoryFull.message"
msgid "Tap an item to swap it for: {item} ({sellPrice})"
```

`LocalizationResourcesTest` fails when a translation has other placeholders than its English text.

## Storybook

- `ItemActionsViewStories`: `merchantItem` and `Kind.MERCHANT_ITEM` go. The arg `itemPrice` becomes `itemSellPrice`. The description of the component says sell price.
- `PRICE_LIMIT` becomes `SELL_PRICE_LIMIT` in these three bindings.
- `PlayerCallToActionViewStories`: the arg `itemPrice` becomes `itemSellPrice`, and **› Item actions** says sell price.
- `InventoryFullViewStories`: the arg `foundItemPrice` becomes `foundItemSellPrice`. **› Expensive** (`screen-inventory-full--expensive`) keeps its id and its 9999, the widest text it tests, though the largest real sell price is 499 G, and says sell price.
- `StorySmokeTest` no longer lists `screen-item-actions--merchant-item`.

## Docs

- [Game data](../architecture/game-data.md): "Its `price` is what a merchant charges for it, from 0 to 999. Its sell price, what a merchant pays, is half of it rounded down, and is what the inventory displays, e.g.: `5 G`."
- [Inventory](../architecture/inventory.md), [On the screen](../architecture/inventory.md#on-the-screen): the sell price on the line of a selected item (e.g.: `Health Potion ........ 5 G`), and in the full-bag message, with its real text (e.g.: `Tap an item to swap it for: Hammer (25 G)`), displayed in every context. The price is only displayed when buying.
- [Presentation timeline](../architecture/presentation-timeline.md): the Discard question takes the line of the name and sell price.
- [Exploration](../architecture/exploration.md) needs no change: it describes the shop, whose lines stay as they are.

## Tests

- `InventoryPresenterTest`
  - `SelectItem should frame the item and display its sell price and actions when a consumable is selected` (the health potion, `itemSellPrice` 5)
  - `SelectItem should display its sell price when the item is equipped` (the hammer, `itemSellPrice` 25)
  - `InventoryFull should open the inventory and ask what to do with the found item when an item is found while the inventory is full` keeps its name, with `foundItemSellPrice` 25
- `PlayerCallToActionViewTest`
  - `DisplayItemActions should display the item name, its sell price and its actions when the item actions are displayed`
  - `DisplayInventoryFull should display the found item with its sell price and leave it when leave it is clicked`
  - `DisplayItemActions should display only the item name and its price when the item has no actions` goes
- `FakePlayerCallToActionView` and `PlayerCallToActionViewExtension` take the new names, e.g.: `itemSellPrice()`.
- `LootableItemTest` and `SearchSellPriceTest` already cover the rule, and do not change.

## Known limitations

- A Hammer bought for 50 G reads 25 G in the bag straight away, with nothing telling that the number is what a merchant pays.
- Old Storybook links setting `itemPrice` or `foundItemPrice` still open, ignoring that arg, and links to **Merchant item** stop working.
- ✅ on the milestone means built, green and pushed: "approved on screenshots" is still open.
- The item line was checked in the real game, through a local snapshot test with items added to a save: the Hammer reads `25 G` and the Health Elixir `12 G`. The full-bag message was not played in the real game: it is checked by `InventoryPresenterTest`, `PlayerCallToActionViewTest` and its stories.
- In the stories of `ItemActionsView`, `InventoryFullView` and `PlayerCallToActionView`, the first letter of the line is cut at the left edge of the phone. It was already so before this plan, this plan does not touch it, and the real game displays the whole line.
