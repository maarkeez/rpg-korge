# Plan: Lootable item prices

> **Status:** done.
>
> **Summary:** The currency system starts with a price in gold on each lootable item. The price is displayed next to the
> item name when an inventory item is selected, and in the message asking what to do with an item found while the
> inventory is full. Merchant npcs, a wallet, earning gold, buying and selling are left for later.

## Decisions

| Topic                | Decision                                                                                                                                                                       |
|----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope                | Only a price on each lootable item, displayed in the inventory. No wallet, no gold earned, no buying nor selling, and nothing else has a price.                                 |
| Currency             | Gold, displayed as `G` after the amount, e.g.: `10 G`.                                                                                                                          |
| Game data            | `lootableItem/<lootable item id>.toml` requires a whole `price`, from 0 to 999. A single price per item: merchants will derive their buying and selling prices from it.          |
| Prices               | The health potion and the mana potion are worth 10 G, and the hammer 50 G.                                                                                                       |
| Domain               | A private `Price` value class inside `LootableItem`, as its `Name`. It moves to a shared gold type once the merchants need one.                                                   |
| Item actions         | The price is displayed on the line of the item name, aligned to the right: `Health Potion ........ 10 G`.                                                                       |
| Inventory full       | The price is displayed in brackets at the end of the message: `Tap an item to swap it for the Hammer (50 G)`.                                                                   |
| Worthless items      | A price of 0 is displayed as `0 G`, as any other price.                                                                                                                         |
| Formatting           | The views receive the price as a number, and a single presentation helper writes it as gold (`goldText(50)` is `50 G`), so the `G` is only written once.                        |
| Elsewhere            | The dropped items on the battlefield do not display their price.                                                                                                                |

For example, the knight has looted a health potion and a hammer:

| The player                                    | The game                                                                                   |
|-----------------------------------------------|--------------------------------------------------------------------------------------------|
| Opens the knight's bag and taps the potion    | `Health Potion` on the left and `10 G` on the right, above Cancel, Discard and Consume.     |
| Taps the hammer                               | `Hammer` on the left and `50 G` on the right, above Cancel, Discard and Equip.             |
| Finds a hammer while the inventory is full    | `Inventory full`, and `Tap an item to swap it for the Hammer (50 G)`.                      |

## lootableItem

### Domain

- Modify `LootableItem` with a private `Price` value class, and its `Dto` with `price: Int`.
- Add `LootableItemError.NegativePrice` and `LootableItemError.PriceAboveLimit`, thrown by `Price` below 0 and above 999.

### Adapters

- Modify `ResourcesLootableItemLoader.LootableItemDefinition` with a required `price`. A file without it fails when the
  resources are read, with its path in the message.

### Resources

- Add `price = 10` to `health-potion.toml` and `mana-potion.toml`, and `price = 50` to `hammer.toml`.

## screen

- Add `goldText(price)`, which writes a price as gold, e.g.: `50 G`.
- Modify `ItemActionsView` with the `itemPrice`, displayed by an `itemPriceView` aligned to the right, on the line of the
  `itemNameView`.
- Modify `InventoryFullView` with the `foundItemPrice`, at the end of its message.
- Modify `PlayerCallToActionView.displayItemActions` with the `itemPrice`, and `displayInventoryFull` with the
  `foundItemPrice`, passing them to their views.
- Modify `InventoryPresenter` to pass the price of the selected item and of the found item.

## Tests

- `LootableItemTest`: a valid price, a negative price, and a price above 999.
- `ResourcesLootableItemLoaderTest`: the price is read, and a file without a price fails with its path.
- `LootableItemMother`: a random price from 0 to 999 by default.
- `PlayerCallToActionViewTest`: the item actions display the price on the right, and the inventory full message ends
  with it.
- `InventoryPresenterTest`: the item actions and the inventory full receive the price of their item.
- `GoldTextTest`: a price is written as gold, 0 included.

## Docs

- `game-data.md`: the `price` of the `lootableItem` TOML file, and its range.
- `inventory.md`: the price on the screen.
