# Plan: Battlefield and inventory dependency cycle

> **Status:** done.
>
> **Summary:** `battlefield` and `inventory` depended on each other. `battlefield` no longer depends on `inventory`: it
> declares `RequestPickUpDroppedItem`, which the `Inventory` aggregate records right after `ItemStored`, so the stored
> item leaves its tile as early as before. `inventory` keeps listening to `LootFound`.

## Decisions

| Topic        | Decision                                                                                                                                                                                                             |
|--------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope        | Only `battlefield` ⇄ `inventory`. `npc` ⇄ `party`, `battle` ⇄ `cpuBattleBrain` and `cpuBattleBrain` ⇄ `cpuWanderBrain` are left for later.                                                                           |
| Direction    | `battlefield` is upstream: it does not know about `inventory`. `inventory` keeps its `OnLootFound` listener, calling `StoreLootedItem`.                                                                              |
| Request      | `battlefield` declares `RequestPickUpDroppedItem(lootableItemId, row, column)`, listened by `OnRequestPickUpDroppedItem`, calling `PickUpDroppedItem`. `OnItemStored` is deleted. No `occupantId`: it is not needed. |
| Publishing   | The `Inventory` aggregate records the request right after `ItemStored`, in `store`, never after `InventoryFull`. Its events are `DomainEvent`s.                                                                      |
| Event order  | The request is queued right behind `ItemStored`, and no listener of `ItemStored` publishes, so `DroppedItemPickedUp` comes out where it did. It holds for several items on a tile, and for the full-inventory swap.  |
| `ItemStored` | Keeps `row` and `column`: where the item was looted from. Its KDoc no longer says it makes the item leave its tile.                                                                                                  |
| Guard        | The pair is removed from `SubdomainDependencyArchitectureTest`.                                                                                                                                                      |
| Docs         | `package-structure.md` has the inventory example, next to the npc one. `inventory.md` says the inventory requests the pick-up. The dependency report is regenerated.                                                 |
| Delivery     | One commit, with this plan, pushed to `main`.                                                                                                                                                                        |

| What `battlefield` needed from `inventory` | Now                                                                                          |
|--------------------------------------------|----------------------------------------------------------------------------------------------|
| Listening to `ItemStored`                  | Listening to its own `RequestPickUpDroppedItem`, recorded by `Inventory` after `ItemStored`. |

## battlefield

- Add `BattlefieldEvent.RequestPickUpDroppedItem` and `OnRequestPickUpDroppedItem`. Delete `OnItemStored`.
- Modify the KDoc of `PickUpDroppedItem`.

## inventory

- Modify `Inventory` to record `RequestPickUpDroppedItem` after `ItemStored`.
- Modify the KDocs of `Inventory.store`, `StoreLootedItem` and `ItemStored`.

## Tests

- `OnRequestPickUpDroppedItemTest` replaces `OnItemStoredTest`.
- `InventoryTest` and `StoreLootedItemTest` expect the request after `ItemStored`, and none after `InventoryFull`.
