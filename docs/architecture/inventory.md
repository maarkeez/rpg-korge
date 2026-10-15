# Inventory

The inventory stores the lootable items a battle unit has looted. It lives in the [`inventory`](../../src/commonMain/kotlin/com/mkz/rpg/inventory) subdomain, and the screen uses it through `InventoryApi`.

## Who has an inventory

- Each battle unit of the human player has one. CPU battle units have none for now: storing an item for them does nothing, so the item stays on its tile.
- An inventory is created empty when its battle unit stores its first item, so it has no game data. Until then, `searchInventoryByBattleUnitId` returns `null`, which means the inventory is empty.
- Each inventory has 12 storage spaces. Each item takes one space, so two health potions take two.

## Using the inventory

| Command           | What it does                                                                                                                                       |
|-------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|
| `storeLootedItem` | Stores an item lying on a tile, or announces `InventoryFull` without storing it when there is no space left.                                       |
| `consumeItem`     | Removes a consumable and requests the application of its effects, with its battle unit as source and target. E.g.: the health potion heals 10. |
| `discardItem`     | Removes an item, freeing its space. The item leaves the game.                                                                                      |

| Query                           | What it answers                                         |
|---------------------------------|---------------------------------------------------------|
| `searchInventoryByBattleUnitId` | The stored items, to display them.                      |
| `canStoreItem`                  | Whether there is space left for one more item.          |

- Consuming and discarding do not cost any turn action, but are only allowed during the turn of the battle unit's player.
- Storing is not limited to the player's turn, as it happens while a battle unit moves.

## Looting from the battlefield

A dropped item lies on the tile where its battle unit was defeated until an inventory stores it. A tile can hold several items, kept in the order they were dropped.

1. A defeated battle unit drops an item (`LootDropped`), and the battlefield places it on its tile (`placeDroppedItem`, announcing `DroppedItemPlaced`).
2. A battle unit moves or teleports. The battlefield finds the items on every tile of its path, destination included (`findLoot`), and announces one `LootFound` per tile with items, in walking order. A teleport's path is only its destination. Deploying a battle unit does not look for loot.
3. The inventory stores each item found (`storeLootedItem`), and announces `ItemStored` with the tile the item was lying on.
4. The battlefield picks the stored item up from its tile (`pickUpDroppedItem`, announcing `DroppedItemPickedUp`).

An item that is not stored stays on its tile: when a CPU battle unit steps on it, or when the inventory is full. The battlefield exposes the items lying on each tile with `searchDroppedItems`.

## When the inventory is full

Storing an item in a full inventory does not store it: it announces `InventoryFull` with the battle unit, the item and its tile, and the player chooses:

1. To keep the new item: the screen discards one of the stored items with `discardItem`, then stores the new one with `storeLootedItem` and its tile.
2. To leave the new item behind: nothing is called, and the item stays on its tile, to be looted later.

## Not yet supported

- **Equipment:** equipping, unequipping and swapping equippable items, one per equipment zone, once there is an equippable item and an effect that lasts while it is worn.
- **The screen:** displaying the inventory, the choice when it is full, consuming items and drawing the dropped items on the battlefield.
