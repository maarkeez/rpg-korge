# Inventory

The inventory stores the lootable items a battle unit has looted. It lives in the [`inventory`](../../src/commonMain/kotlin/com/mkz/rpg/inventory) subdomain, and the screen uses it through `InventoryApi`.

## Who has an inventory

- Each battle unit of the human player has one. CPU battle units have none for now: storing an item for them does nothing.
- An inventory is created empty when its battle unit stores its first item, so it has no game data. Until then, `searchInventoryByBattleUnitId` returns `null`, which means the inventory is empty.
- Each inventory has 12 storage spaces. Each item takes one space, so two health potions take two.

## Using the inventory

| Command           | What it does                                                                                                                                       |
|-------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|
| `storeLootedItem` | Stores a looted item, or announces `InventoryFull` without storing it when there is no space left.                                                  |
| `consumeItem`     | Removes a consumable and requests the application of its effects, with its battle unit as source and target. E.g.: the health potion heals 10. |
| `discardItem`     | Removes an item, freeing its space. The item leaves the game.                                                                                      |

| Query                           | What it answers                                         |
|---------------------------------|---------------------------------------------------------|
| `searchInventoryByBattleUnitId` | The stored items, to display them.                      |
| `canStoreItem`                  | Whether there is space left for one more item.          |

- Consuming and discarding do not cost any turn action, but are only allowed during the turn of the battle unit's player.
- Storing is not limited to the player's turn, as it happens while a battle unit moves.

## When the inventory is full

Storing an item in a full inventory does not store it: it announces `InventoryFull` with the battle unit and the item, and the player chooses:

1. To keep the new item: the screen discards one of the stored items with `discardItem`, then stores the new one with `storeLootedItem`.
2. To leave the new item behind: nothing is called, and the item is lost.

## Not yet supported

- **Looting from the battlefield:** dropped loot is only announced (`LootDropped`), not kept on its tile. Once the battlefield keeps it, a battle unit will loot the items on every tile of its move path, on its destination and on the tile it teleports onto.
- **Equipment:** equipping, unequipping and swapping equippable items, one per equipment zone, once there is an equippable item and an effect that lasts while it is worn.
- **The screen:** displaying the inventory, the choice when it is full and consuming items.
