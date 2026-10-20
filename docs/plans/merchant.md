# Plan: Merchant

> **Status:** done.
>
> **Summary:** A merchant npc stands on the battlefield. Talking to it shows its line, "How can I help you?", and tapping
> its dialog opens its shop: up to 12 items for sale, displayed as the inventory does. Tapping an item shows its name and
> price. Buying, selling, a wallet and stock quantities are left for later.

## Decisions

| Topic              | Decision                                                                                                                                                                                  |
|--------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope              | Only browsing the items for sale. No buying, no selling, no wallet.                                                                                                                       |
| Domain             | A merchant is an npc with a stock, in the `npc` subdomain, as a recruitable npc is an npc with a unit. No new subdomain until buying and selling need one.                                |
| Game data          | `npc/<npc id>.toml` accepts an optional `stock`: the ids of the lootable items for sale, in the order they are displayed.                                                                  |
| Stock rules        | The game fails to start when an item of the stock does not exist, when the stock has more than 12 items, or when an item is listed twice.                                                |
| Supply             | Each item for sale is in unlimited supply: no quantities, and each one takes a single slot.                                                                                              |
| The merchant       | Id `merchant`, named `Merchant`, line `How can I help you?`, on row 14, column 2, selling the health potion, the mana potion and the hammer.                                              |
| Assets             | The attached sprite, with each off-palette colour replaced by its closest FamiCube colour (the blue-grey armour turns neutral grey), previewed before committing.                          |
| Portrait           | The palette-fixed sprite scaled 2x to 32x32, nearest neighbour.                                                                                                                           |
| Flow               | Two steps: the merchant's dialog first, as any npc's. Tapping it opens the shop in its place, instead of finishing the talk.                                                               |
| Shop               | Takes the place of the dialog: the merchant's name as title, a Close button, and 12 slots in two rows of 6, reusing `InventorySlotView`. The items fill the first slots, the rest are empty. |
| Selecting an item  | The slot is framed, and the call to action displays `Health Potion ........ 10 G`, as the inventory does, without actions. Tapping an empty slot unselects it and clears the call to action. |
| Closing            | Close finishes the talk (`NpcTalkFinished`): the virtual pad is displayed again, and time goes on.                                                                                        |
| Time               | The party stays `TALKING` while the shop is open, so time stays stopped. The shop is screen state only: no new party mode nor event.                                                      |
| Battle             | A battle starting while the shop is open abandons it, as it abandons the dialog.                                                                                                          |
| Other npcs         | Unchanged: tapping their dialog finishes the talk.                                                                                                                                        |

For example, the party walks next to the merchant:

| The player                          | The game                                                                                              |
|-------------------------------------|-------------------------------------------------------------------------------------------------------|
| Presses A                           | The dialog: the merchant's portrait, `Merchant`, and `How can I help you?`. Time stops.               |
| Taps the dialog                     | The shop: `Merchant`, Close, the health potion, the mana potion and the hammer, then 9 empty slots.   |
| Taps the hammer                     | The hammer is framed, and the call to action displays `Hammer` on the left and `50 G` on the right.   |
| Taps an empty slot                  | No slot is framed, and the call to action is cleared.                                                 |
| Taps Close                          | The virtual pad is displayed again, and time goes on.                                                 |

## npc

### Domain

- Modify `Npc` with a private `Stock` value class holding the lootable item ids, and its `Dto` with
  `stock: List<String> = emptyList()`.
- Add `fun isMerchant(): Boolean`: whether it has a stock.
- Add `NpcError.NpcStockTooLarge` (more than 12 items) and `NpcError.NpcStockItemRepeated`, thrown by `Stock`.
- Add `NpcError.NpcStockItemNotFound`.

### Use cases

- Modify `InitialiseNpcs` with `SearchLootableItemById`, failing with `NpcStockItemNotFound` when an item of the stock
  does not exist. The lootable items are initialised before the npcs (see `game-data.md`), so no order changes.

### Adapters

- Modify `ResourcesNpcLoader.NpcDefinition` with an optional `stock`.
- Modify `NpcApi` with the `LootableItemApi`, and `BattleScene` to pass it.

### Resources

- Add `npc/merchant.toml`, `npc/merchant.png` and `npc/merchant_portrait.png`.

## screen

- Add `ShopView`, as `InventoryView`: a title, a Close button and 12 `InventorySlotView`. It displays the items for sale,
  frames the selected slot, and tells its delegate when a slot is selected or the shop is closed.
- Modify `PlayerCallToActionView.displayItemActions` to accept no actions, displaying only the name and the price.
- Modify `ExplorationPresenter`:
  - Tapping the dialog of a merchant opens the shop in its place, with its items, instead of finishing the talk.
  - Selecting a stocked slot frames it and displays its name and price in the call to action, which is shown while the
    shop is open. Selecting an empty slot unselects it and clears the call to action.
  - Close finishes the talk. Displaying the virtual pad or the battle HUD hides the shop, as it hides the dialog.
- Modify `BattleScene` to add the `ShopView` next to the dialog, and pass it and the `LootableItemApi` to the
  `ExplorationPresenter`.

## Tests

- `NpcTest`: a merchant with a stock, an npc without one, a stock of 12 items, more than 12, and a repeated item.
- `NpcMother`: no stock by default.
- `InitialiseNpcsTest`: a merchant is placed with its stock, and fails when an item of its stock does not exist.
- `ResourcesNpcLoaderTest`: the stock is read in order, and an npc without one has an empty stock.
- `ShopViewTest`: the items fill the first slots, the rest are empty, the selected slot is framed, and the delegate is
  told when a slot is selected or the shop is closed.
- `PlayerCallToActionViewTest`: the item actions without actions display only the name and the price.
- `ExplorationPresenterTest`: tapping a merchant's dialog opens the shop, tapping another npc's still finishes the talk,
  selecting a stocked slot displays its name and price, selecting an empty one clears them, Close finishes the talk, and
  a battle starting hides the shop.
- `NpcAcceptanceTest`: the merchant is placed with its stock.
- `AssetPaletteArchitectureTest` already checks the new images.

## Docs

- `game-data.md`: the `stock` of the `npc` TOML file, its rules, and the merchant.
- `exploration.md`: talking to a merchant and its shop.
