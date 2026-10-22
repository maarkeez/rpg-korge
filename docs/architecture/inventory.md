# Inventory

The inventory stores the lootable items a battle unit carries, and its gold. It lives in the [`inventory`](../../src/commonMain/kotlin/com/mkz/rpg/inventory) subdomain, and the screen uses it through `InventoryApi`.

## Who has an inventory

- Each battle unit of the human player has one, except the summoned ones (e.g.: a bee), which vanish after some turns. CPU battle units and summoned battle units have none: storing an item for them does nothing, so the item stays on its tile, and their bag is not displayed.
- An inventory is created empty, with the starting gold of its unit, when its battle unit is deployed ([`CreateInventory`](../../src/commonMain/kotlin/com/mkz/rpg/inventory/usecases/commands/CreateInventory.kt), on `BattleUnitDeployed`), e.g.: when the battle is set up, or an npc is recruited. `searchInventoryByBattleUnitId` returns `null` for the battle units without inventory.
- Each inventory has 12 storage spaces. Each item takes one space, so two health potions take two. An equipped item keeps taking its space.

## Using the inventory

| Command           | What it does                                                                                                                                       |
|-------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|
| `storeLootedItem` | Stores an item lying on a tile, or announces `InventoryFull` without storing it when there is no space left.                                       |
| `consumeItem`     | Removes a consumable and requests the application of its effects, with the item as source and its battle unit as target. E.g.: the health potion heals 10. Fails, keeping it, when its battle unit can not learn the ability it teaches. See [Learning an ability](#learning-an-ability). |
| `discardItem`     | Removes an item, freeing its space. The item leaves the game.                                                                                      |
| `equipItem`       | Equips a stored equippable item in its zone, and requests the application of its effects. See [Equipment](#equipment).                              |
| `unequipItem`     | Unequips an item, which stays stored, and requests the removal of its effects. See [Equipment](#equipment).                                         |
| `buyItem`         | Pays the price of an item the merchant sells, and stores it. See [Buying and selling](#buying-and-selling).                                        |
| `sellItem`        | Removes an item that is not equipped, and receives half its price. See [Buying and selling](#buying-and-selling).                                  |

| Query                           | What it answers                                         |
|---------------------------------|---------------------------------------------------------|
| `searchInventoryByBattleUnitId` | The stored items and the equipped ones, to display them. |
| `canStoreItem`                  | Whether there is space left for one more item.          |
| `canConsumeItem`                | Whether the battle unit can consume the item now.       |
| `searchBuyRefusal`              | Why the battle unit can not buy the item now, if so.    |
| `searchSellRefusal`             | Why the battle unit can not sell the item now, if so.   |

- Consuming, discarding, equipping and unequipping do not cost any turn action, but in a battle they are only allowed during the turn of the battle unit's player. Out of the battles (e.g.: while exploring), there are no turns, so they are always allowed (see `PlayerTurnRule`).
- Storing is not limited to the player's turn, as it happens while a battle unit moves.

## Looting from the battlefield

A dropped item lies on the tile where its battle unit was defeated until an inventory stores it. A tile can hold several items, listed by the unit that dropped them, each in the order it dropped them.

1. A defeated battle unit drops an item: the loot drop of its unit keeps it on its tile, and announces `LootDropped`.
2. A battle unit moves or teleports (`BattleUnitMoved`). `lootDrop` finds the items on every tile of the path, destination included (`findLoot`), and announces one `LootFound` per tile with items, in walking order. A teleport's path is only its destination. Deploying a battle unit does not look for loot.
3. The inventory stores each item found (`storeLootedItem`), and announces `ItemStored` with the tile the item was lying on.
4. The inventory requests the pick-up right after (`RequestPickUpDroppedItem`), and `lootDrop` picks the stored item up from its tile (`pickUpDroppedItem`, announcing `DroppedItemPickedUp`).

An item that is not stored stays on its tile: when a CPU battle unit or a summoned battle unit steps on it, or when the inventory is full. `lootDrop` exposes the items lying on each tile with `searchDroppedItems`. The battlefield knows nothing about loot: its tiles only hold their terrain and their occupant.

## When the inventory is full

Storing an item in a full inventory does not store it: it announces `InventoryFull` with the battle unit, the item and its tile, and the player chooses:

1. To keep the new item: the screen discards one of the stored items with `discardItem`, then stores the new one with `storeLootedItem` and its tile.
2. To leave the new item behind: nothing is called, and the item stays on its tile, to be looted later.

## Equipment

Each equipment zone (`HEAD`, `BODY`, `ARMS`, `LEGS`, `WEAPON`) holds one equipped item. Equipped items stay stored, so the inventory exposes them as `equippedItems`, by zone, next to `storedItems`.

1. `equipItem` equips a stored equippable item in its zone, announcing `ItemEquipped`. If the zone held another item, it is a **swap**: the old item is unequipped first, announcing `ItemUnequipped`.
2. The inventory requests the application of each effect of the new item (`RequestApplyEffect`), with the item as source and its battle unit as target. The battle unit records them under the item: they raise its maximum health and mana, without changing the remaining ones. E.g.: the hammer takes the knight from 100/100 health to 100/120.
3. `unequipItem` unequips an item, announcing `ItemUnequipped`, and requests the removal of its effects (`RequestRemoveEquippedEffects`). The battle unit lowers its remaining health and mana to the new maximums when they are above them, announcing `EquippedEffectsRemoved`.

- In a swap, the new item's effects are applied **before** the old item's are removed, so the remaining health and mana are only lowered to the final maximums: swapping an item for another with the same effects loses nothing.
- An item can not be equipped twice. A battle unit can store several copies of the same item, but only one of them is equipped.
- An equipped item can not be discarded: the player unequips it first. When another copy of it is stored, discarding removes that copy instead.
- Healing, restoring mana and the mana replenished each turn never go above the maximums, equipment included. The battle unit exposes them as `maximumHealthPoints` and `maximumManaPoints`, and the health and mana bars and the CPU battle brain use them.

## On the screen

The battle scene draws the dropped items on their tiles, and opens the inventory of the selected battle unit behind its bag, where the player consumes, equips, unequips, swaps and discards items, and chooses what to do with an item found while the inventory is full. See [Presentation timeline](presentation-timeline.md#loot) and [Inventory on the screen](presentation-timeline.md#inventory).

The price of an item is displayed in gold: on the line of its name, aligned to the right, when it is selected (e.g.: `Health Potion ........ 10 G`), and at the end of the message asking what to do with it when it is found while the inventory is full (e.g.: `Tap an item to swap it for the Hammer (50 G)`).

## Gold

Each battle unit carries its own gold in its inventory: the party never shares it. It starts with the `startingGold` of its unit (e.g.: the knights 20 G, and the viking 100 G, see [Game data](game-data.md)), and goes from 0 to 99,999 G (`Gold.MAXIMUM`). The bag displays it on its title line, e.g.: `Inventory ........ 20 G`.

## Buying and selling

A battle unit trades as a party member, while the party talks to a merchant ([`TradeRule`](../../src/commonMain/kotlin/com/mkz/rpg/inventory/usecases/services/TradeRule.kt)): otherwise, buying and selling fail (`NotTradingWithAMerchant`). The merchant has unlimited gold and stock.

| Trade  | Price                             | Fails when                                                                                                                                  |
|--------|-----------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| Buy    | The price of the item, e.g.: 50 G | The merchant does not sell it (`ItemNotSoldByTheMerchant`), the battle unit can not afford it (`NotEnoughGold`), or has no space left (`NoStorageSpaceLeft`). |
| Sell   | Half of it, rounded down: 25 G    | The item is not stored, its only stored copy is equipped (`EquippedItemCanNotBeSold`), or the gold would go above 99,999 G (`GoldAboveLimit`). |

- Buying announces `ItemBought`, and selling `ItemSold`, with the price paid. A sold item leaves the game, so it does not join the merchant's stock.
- Any item can be sold, also the ones the merchant does not sell.
- `searchBuyRefusal` and `searchSellRefusal` answer why instead of failing (a `TradeRefusalDto`, e.g.: `NOT_ENOUGH_GOLD`, `BAG_FULL`, `EQUIPPED` or `GOLD_FULL`), or `null` when the trade is allowed, so the shop dims the items and disables Buy and Sell with the reason. The same rules make `buyItem` and `sellItem` fail. See [Shopping at a merchant](exploration.md#shopping-at-a-merchant).

## Learning an ability

A consumable can teach its battle unit an ability for good, with a `LEARN_ABILITY` effect (see [Game data](game-data.md)). E.g.: the teleport essence (100 G) teaches teleport, and the goat always drops it.

- The ability is added at the end of the battle unit's abilities, ready to be cast. It belongs to that battle unit only, and it is kept out of the battles and when the battle unit respawns.
- The item can not be consumed when its battle unit already knows the ability (e.g.: the knights know teleport), or already knows 6 abilities: `consumeItem` fails with `AbilityCanNotBeLearned`, and the item stays stored. `canConsumeItem` answers `false` instead, so the inventory disables Consume ([`ConsumptionRule`](../../src/commonMain/kotlin/com/mkz/rpg/inventory/usecases/services/ConsumptionRule.kt)).
- The other effects never prevent consuming an item, e.g.: a health potion can be consumed at full health.

## Not yet supported

- **Looking inside a chest:** a tile with several items is drawn as a chest, and its items are only shown once they are looted.
