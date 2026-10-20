# Plan: Buy and sell

> **Status:** done.
>
> **Summary:** Each battle unit of the party carries its own gold, in its inventory. At a merchant, the shop has a Buy
> tab, with the items the merchant sells, and a Sell tab, with the items of the buyer. The buyer is one party member at a
> time, switched by swiping the shop or tapping its dots, and the shop shows its name, gold and bag. Buying costs the
> price of the item, and selling pays half of it.

## Decisions

| Topic                | Decision                                                                                                                                                                                         |
|----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Gold                 | Each battle unit has its own gold, never shared. It lives in its inventory, from 0 to 99,999 G.                                                                                                  |
| Starting gold        | `unit/<unit id>.toml` accepts an optional `startingGold`, from 0 to 99,999, 0 by default. The viking starts with 100 G, and the knights with 20 G.                                              |
| Inventory creation   | Each battle unit of the human player, the summoned ones excluded, gets its inventory with its starting gold once deployed (the viking once recruited), instead of on its first loot.            |
| Bag                  | The inventory displays the gold on its title line, aligned to the right: `Inventory ........ 20 G`.                                                                                             |
| Prices               | Buying costs the price of the item. Selling pays half of it, rounded down: the hammer is bought for 50 G and sold for 25 G.                                                                     |
| Merchant             | Unlimited gold and unlimited stock. A sold item leaves the game.                                                                                                                               |
| Trading rule         | Buying and selling are only allowed to a party member while the party talks to a merchant. Buying also requires the merchant to sell the item.                                                  |
| Tabs                 | Buy and Sell buttons on the title line, between the merchant's name and Close. The active one is highlighted. The shop always opens on Buy.                                                      |
| Buy tab              | The merchant's items. Tapping one displays `Health Potion ........ 10 G` with Cancel and Buy. Buy is disabled when the buyer can not afford it or its bag is full.                              |
| Sell tab             | The buyer's stored items, the equipped ones framed in gold. Tapping one displays its sell price with Cancel and Sell. Sell is disabled for the equipped copy, and when the buyer's gold would go above 99,999 G. |
| After trading        | After Buy, the item stays selected, and Buy is checked again, to buy another one. After Sell, the selection is cleared, as the item left. Cancel clears the selection.                           |
| Buyer                | The leader when the shop opens. A line under the grid displays the buyer: `Knight · 20 G · Bag 11/12`, or `Knight · 20 G · Bag full`.                                                          |
| Dots                 | Under the buyer line, one dot per party member in party order, the buyer's one filled.                                                                                                          |
| Switching buyer      | Swiping the shop left or right (at least 40 px) moves the buyer one dot left or right, stopping at the ends. Tapping a dot selects its party member.                                            |
| Switching resets     | Switching the buyer or the tab clears the selection and the call to action.                                                                                                                    |
| Battlefield          | While the shop is open, every tile is dimmed but the buyer's and the merchant's, and the battlefield scrolls to the buyer when it changes. Closing the shop restores the tiles.                |
| Disabled actions     | No message explains a disabled Buy or Sell: the buyer line already displays the gold and the bag.                                                                                               |

For example, the knight leads the party, followed by the other knight and the viking:

| The player                              | The game                                                                                                 |
|-----------------------------------------|----------------------------------------------------------------------------------------------------------|
| Opens the merchant's shop               | Buy tab, `Knight · 20 G · Bag 0/12`, dots `●○○`, every tile dimmed but the knight's and the merchant's.  |
| Taps the hammer                         | `Hammer ........ 50 G`, Cancel, and Buy disabled: the knight has 20 G.                                   |
| Swipes left twice                       | The viking buys: `Viking · 100 G · Bag 0/12`, dots `○○●`, the battlefield scrolls to the viking.        |
| Taps the hammer and Buy                 | `Viking · 50 G · Bag 1/12`. The hammer stays selected, and Buy is enabled again.                        |
| Taps Sell, then the hammer              | `Hammer ........ 25 G`, Cancel and Sell.                                                                 |
| Taps Sell                               | `Viking · 75 G · Bag 0/12`, and the selection is cleared.                                                |
| Taps Close                              | The tiles are no longer dimmed, the virtual pad is displayed again, and time goes on.                    |

## shared

### Domain

- Add `Gold.MAXIMUM`, 99,999: the most gold a battle unit can carry.

## unit

### Domain

- Modify `Unit` with a private `StartingGold` value class, and its `Dto` with `startingGold: Int = 0`.
- Add `UnitError.NegativeStartingGold` and `UnitError.StartingGoldAboveLimit`.

### Adapters and resources

- Modify `ResourcesUnitLoader.UnitDefinition` with an optional `startingGold`.
- Add `startingGold = 20` to `knight.toml`, and `startingGold = 100` to `viking.toml`.

## lootableItem

- Modify `LootableItem.Dto` with `sellPrice`: half its price, rounded down.

## inventory

### Domain

- Modify `Inventory` with a private `Gold` value class, `create(battleUnitId, gold)`, and its `Dto` with `gold`.
- Add `canBuy(price)`, `buy(lootableItemId, price)`, `canSell(lootableItemId, price)` and `sell(lootableItemId, price)`,
  publishing `ItemBought` and `ItemSold`.
- Add `InventoryError.NegativeGold`, `GoldAboveLimit`, `NotEnoughGold`, `NoStorageSpaceLeft`,
  `EquippedItemCanNotBeSold`, `NotTradingWithAMerchant` and `ItemNotSoldByTheMerchant`.

### Use cases

- Add `CreateInventory`: the inventory of a deployed battle unit of the human player, the summoned ones excluded, with
  the starting gold of its unit. Nothing happens when it already has one.
- Modify `StoreLootedItem` to leave the item on its tile when the battle unit has no inventory, instead of creating it.
- Modify `CanStoreItem` to answer `false` when the battle unit has no inventory.
- Add `TradeRule`: the battle unit is a party member and the party talks to a merchant, who sells the item to buy.
- Add `BuyItem` and `SellItem`, and the `CanBuyItem` and `CanSellItem` queries, which answer `false` instead of failing.

### Adapters

- Add `OnBattleUnitDeployed`, which creates the inventory.
- Modify `InventoryApi` with the `UnitApi`, the `PartyApi` and the `NpcApi`.

## screen

- Modify `BattlefieldView` with `scrollToTile`, which scrolls the battlefield so the tile is visible.
- Modify `InventoryView.display` with the gold, displayed on the title line.
- Modify `PlayerCallToActionView.displayItemActions` with `redisplaysFinishTurn`, so the shop keeps its actions after Buy.
- Modify `ShopView` with the tabs, the buyer line, the dots and the swipe. The whole shop can be swiped.
- Add `ShopPresenter`, with the shop moved out of `ExplorationPresenter`: the tabs, the buyer, the dimmed tiles, the
  call to action, and the trades, refreshed once `ItemBought` or `ItemSold` is published.
- Modify `ExplorationPresenter` to open the shop with the `ShopPresenter`, and close it when the pad or the battle HUD
  is displayed.
- Modify `InventoryPresenter` to display the gold, and refresh the bag on `ItemBought` and `ItemSold`.

## Tests

- `UnitTest`, `ResourcesUnitLoaderTest`: the starting gold, its default and its range.
- `InventoryTest`: gold, buying and selling, and their failures.
- `CreateInventoryTest`, `OnBattleUnitDeployedTest`, `StoreLootedItemTest`, `BuyItemTest`, `SellItemTest`,
  `CanBuyItemTest`, `CanSellItemTest`.
- `ShopViewTest`, `ShopPresenterTest`, `ExplorationPresenterTest`, `InventoryViewTest`, `InventoryPresenterTest`.
- `TradeAcceptanceTest`: buying and selling at the merchant with the resources.

## Docs

- `game-data.md`: the `startingGold` of the unit TOML file.
- `inventory.md`: gold, the inventory created on deployment, buying and selling.
- `exploration.md`: the shop tabs, the buyer and the dimmed battlefield.
