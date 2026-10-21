# Plan: Battlefield and loot drop dependency

> **Status:** done.
>
> **Summary:** `battlefield` depended on `lootDrop` only to place the dropped items on its tiles (`OnLootDropped`), and
> `inventory` depended on `battlefield` only to loot them. Each `LootDrop` now also keeps where the items its unit dropped
> lie, and `lootDrop` owns their whole lifecycle: dropping, finding on a path, picking up and searching them.
> `battlefield` knows nothing about loot: its tiles only hold their terrain and their occupant.

## Decisions

| Topic         | Decision                                                                                                                                                                                                                 |
|---------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Goal          | No edge between `battlefield` and `lootDrop`, in either direction. `battlefield` keeps no loot.                                                                                                                          |
| Scope         | The whole lifecycle moves to `lootDrop`: storing, finding on a path (`LootFound`), picking up (`RequestPickUpDroppedItem`, `DroppedItemPickedUp`) and `searchDroppedItems`. Finding and picking up read the storage.     |
| Model         | Folded into `LootDrop`, one per unit: it keeps the items its unit dropped on each tile, in drop order. A subdomain has a single aggregate (`DomainArchitectureTest`), so there is no separate `DroppedLoot` aggregate.    |
| Order         | The items of a tile are listed by the unit that dropped them, in loot drop creation order, each in the order it dropped them. The order between the items of different units on the same tile is not kept.              |
| Position      | `lootDrop` declares its own `LootDrop.Dto.PositionDto(row, column)`, as `battlefield` does. `BattleUnit` maps its path into it.                                                                                         |
| Path          | The `BattleUnit` aggregate records `RequestFindLoot(battleUnitId, path)` right after `RequestMoveOccupant`, so the loot is found once the occupancy is up to date, as before. `RequestMoveOccupant` has no `path`.      |
| Dropping      | `DropLoot` places the rolled item in the loot drop of the unit and publishes `LootDropped`. `DroppedItemPlaced` is deleted: dropping and placing are the same step.                                                      |
| Finding       | `FindLoot` reads every loot drop, and announces one `LootFound` per tile of the path with items, in walking order.                                                                                                       |
| Picking up    | `PickUpDroppedItem` picks the item up from the first loot drop with it on the tile, and fails with `DroppedItemNotFound` when none has it.                                                                              |
| `inventory`   | Listens to `LootDropEvent.LootFound`, and the `Inventory` aggregate records `LootDropEvent.RequestPickUpDroppedItem`. `inventory` → `battlefield` is cut, `inventory` → `lootDrop` is added.                            |
| `screen`      | `BattlefieldPresenter` draws the items from `LootDropped` and `LootDropEvent.DroppedItemPickedUp`.                                                                                                                       |
| `LootDropApi` | Exposes `placeDroppedItem(unitId, lootableItemId, row, column)` (to prepare scenarios), `findLoot`, `pickUpDroppedItem` and `searchDroppedItems`.                                                                       |
| Docs          | `package-structure.md`, `inventory.md` and `presentation-timeline.md` describe the new owner. The dependency report is regenerated.                                                                                     |
| Delivery      | One pull request, with this plan.                                                                                                                                                                                        |

| What `battlefield` did with loot                       | Now                                                                           |
|--------------------------------------------------------|-------------------------------------------------------------------------------|
| `OnLootDropped`, calling `PlaceDroppedItem`            | `DropLoot` places the item in the loot drop of the unit.                      |
| `FindLoot`, from `OnRequestMoveOccupant`               | `lootDrop`'s `FindLoot`, from `OnRequestFindLoot`, requested by `BattleUnit`. |
| `PickUpDroppedItem`, from `OnRequestPickUpDroppedItem` | `lootDrop`'s `PickUpDroppedItem`, from its own `OnRequestPickUpDroppedItem`.  |
| `SearchDroppedItems`                                   | `lootDrop`'s `SearchDroppedItems`.                                            |

## lootDrop

- Modify `LootDrop` to keep its dropped items (`place`, `droppedItems`, `pickUp`), and `LootDrop.Dto` to hold them.
- Add `update` and `searchAll` to `LootDropRepository`, and `LootDropError.DroppedItemNotFound`.
- Add `RequestFindLoot`, `RequestPickUpDroppedItem`, `LootFound` and `DroppedItemPickedUp` to `LootDropEvent`.
- Add `PlaceDroppedItem`, `FindLoot`, `PickUpDroppedItem`, `SearchDroppedItems`, `OnRequestFindLoot` and `OnRequestPickUpDroppedItem`.
- Modify `DropLoot` to place the item.

## battlefield

- Delete the dropped items of the tiles, `PlaceDroppedItem`, `FindLoot`, `PickUpDroppedItem`, `SearchDroppedItems`, `OnLootDropped`,
  `OnRequestPickUpDroppedItem`, `DroppedItemNotFound`, and the events `DroppedItemPlaced`, `LootFound`, `DroppedItemPickedUp` and
  `RequestPickUpDroppedItem`.
- Remove `path` from `RequestMoveOccupant`, and the loot search from `OnRequestMoveOccupant`.

## battleUnit

- Modify `BattleUnit` to record `RequestFindLoot` after `RequestMoveOccupant`.

## inventory and screen

- `OnLootFound`, `Inventory` and `BattlefieldPresenter` use the `lootDrop` events.

## Tests

- `LootDropTest` and `InMemoryLootDropRepositoryTest` cover the dropped items. `OnRequestFindLootTest`, and the `lootDrop` versions of
  `PlaceDroppedItemTest`, `FindLootTest`, `PickUpDroppedItemTest`, `SearchDroppedItemsTest` and `OnRequestPickUpDroppedItemTest`, replace the
  battlefield ones. `OnLootDroppedTest` is deleted.
- `DropLootTest` expects the item placed.
- `BattleUnitTest`, `MoveBattleUnitTest` and `WalkBattleUnitTest` expect `RequestFindLoot` after `RequestMoveOccupant`.
- `OnLootFoundTest`, `InventoryTest`, `StoreLootedItemTest` and the presenter tests use the `lootDrop` events and API. The presenter tests
  place their items as dropped by a rat that rolls nothing.
- The acceptance tests that loot wire and initialise `LootDropApi`, and place their items as dropped by a unit with a loot drop.
  `DropLootAcceptanceTest` adds a defeated rat's hammer stored by the knight walking onto it.
