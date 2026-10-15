# Inventory

The inventory stores the lootable items a battle unit has looted. It lives in the [`inventory`](../../src/commonMain/kotlin/com/mkz/rpg/inventory) subdomain, and the screen uses it through `InventoryApi`.

## Who has an inventory

- Each battle unit of the human player has one. CPU battle units have none for now: storing an item for them does nothing, so the item stays on its tile.
- An inventory is created empty when its battle unit stores its first item, so it has no game data. Until then, `searchInventoryByBattleUnitId` returns `null`, which means the inventory is empty.
- Each inventory has 12 storage spaces. Each item takes one space, so two health potions take two. An equipped item keeps taking its space.

## Using the inventory

| Command           | What it does                                                                                                                                       |
|-------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|
| `storeLootedItem` | Stores an item lying on a tile, or announces `InventoryFull` without storing it when there is no space left.                                       |
| `consumeItem`     | Removes a consumable and requests the application of its effects, with the item as source and its battle unit as target. E.g.: the health potion heals 10. |
| `discardItem`     | Removes an item, freeing its space. The item leaves the game.                                                                                      |
| `equipItem`       | Equips a stored equippable item in its zone, and requests the application of its effects. See [Equipment](#equipment).                              |
| `unequipItem`     | Unequips an item, which stays stored, and requests the removal of its effects. See [Equipment](#equipment).                                         |

| Query                           | What it answers                                         |
|---------------------------------|---------------------------------------------------------|
| `searchInventoryByBattleUnitId` | The stored items and the equipped ones, to display them. |
| `canStoreItem`                  | Whether there is space left for one more item.          |

- Consuming, discarding, equipping and unequipping do not cost any turn action, but are only allowed during the turn of the battle unit's player.
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

## Equipment

Each equipment zone (`HEAD`, `BODY`, `ARMS`, `LEGS`, `WEAPON`) holds one equipped item. Equipped items stay stored, so the inventory exposes them as `equippedItems`, by zone, next to `storedItems`.

1. `equipItem` equips a stored equippable item in its zone, announcing `ItemEquipped`. If the zone held another item, it is a **swap**: the old item is unequipped first, announcing `ItemUnequipped`.
2. The inventory requests the application of each effect of the new item (`RequestApplyEffect`), with the item as source and its battle unit as target. The battle unit records them under the item: they raise its maximum health and mana, without changing the remaining ones. E.g.: the hammer takes the knight from 100/100 health to 100/120.
3. `unequipItem` unequips an item, announcing `ItemUnequipped`, and requests the removal of its effects (`RequestRemoveEquippedEffects`). The battle unit lowers its remaining health and mana to the new maximums when they are above them, announcing `EquippedEffectsRemoved`.

- In a swap, the new item's effects are applied **before** the old item's are removed, so the remaining health and mana are only lowered to the final maximums: swapping an item for another with the same effects loses nothing.
- An item can not be equipped twice. A battle unit can store several copies of the same item, but only one of them is equipped.
- An equipped item can not be discarded: the player unequips it first. When another copy of it is stored, discarding removes that copy instead.
- Healing, restoring mana and the mana replenished each turn never go above the maximums, equipment included. The battle unit exposes them as `maximumHealthPoints` and `maximumManaPoints`, and the health and mana bars and the CPU brain use them.

## Not yet supported

- **The screen:** displaying the inventory, the choice when it is full, consuming and equipping items, and drawing the dropped items on the battlefield.
