# Plan: Rename the code of the loot drop subdomain to the words of the glossary

> **Status:** done.
>
> **Summary:** The code of the loot drop subdomain says "loot" for the items lying on the battlefield, "pick up" for a
> party member looting them, and "drop" and "place" for what the [glossary](../../GLOSSARY.md) calls a roll and a drop.
> E.g.: when a rat is defeated, `DropLoot` calls `LootDrop.drop`, which only rolls `tier-2` and returns a health potion,
> then `LootDrop.place`, which drops it and publishes `LootDropped`. When the knight walks onto it, `FindLoot` publishes
> `LootFound`, the inventory stores the potion and requests `RequestPickUpDroppedItem`, and `PickUpDroppedItem` publishes
> `DroppedItemPickedUp`. Reading the code and talking about the game take a translation. Instead, the code says roll, drop,
> dropped item, find and loot, as the glossary does. The game plays the same, and players read the same text. Builds on
> [Inventory](../architecture/inventory.md#looting-from-the-battlefield), [Package
> structure](../architecture/package-structure.md) and the [inventory renames](inventory-renames.md).

## Decisions

| Topic         | Decision                                                                                                                                                                                                                         |
|---------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope         | The renames below, in the code, tests, comments and docs of the loot drop subdomain, and the inventory and screen code that names its events and commands.                                                                       |
| Loot drop     | `LootDrop`, the `lootDrop` package, `resources/lootDrop/<unit id>.toml` and `LootDropLoader` keep their names, as Loot drop is the glossary's word.                                                                               |
| Dropped item  | `DroppedItems`, `SearchDroppedItems`, `DroppedItemNotFound` and the saved `droppedItems` already fit Dropped item. "Loot" as a noun goes, so `LootFound`, `FindLoot` and `LootDropped` say dropped items.                         |
| Loot          | Loot is only a verb: a party member storing a dropped item. It takes Pick up's place, chosen over keeping Pick up for the tile's side of the same act, as one word is enough. `StoreLootedItem` already fits.                      |
| Roll and drop | `LootDrop.drop` only rolls, and `LootDrop.place` drops, so they become `roll` and `drop`. Dropping is not a building's Place, which answers the [building renames](building-renames.md).                                          |
| Words kept    | `DropItems` and `RequestDropItems` already fit Drop, which also covers the items of a reward with no storage space left. `LootableItem` and `LootTypeDto` keep their names, as they are named after the verb.                     |
| Save format   | Not reached: no field of `LootDropSnapshot` nor `DroppedItemsSnapshot` changes, and the `lootDrop` key stays, so `SAVE_VERSION` does not change.                                                                                 |
| Player text   | None changes, and no translation key or catalog changes: players never read these names.                                                                                                                                         |
| Tests         | No new tests: a rename changes no behaviour. The existing tests are renamed with the code.                                                                                                                                       |
| Out of scope  | The ownerless loot drop, `LootDropCreated`, and the words of the lootTable, unit, quest, party and screen subdomains, which wait for their sections. See [Not in this plan](#not-in-this-plan).                                    |

## Renames

| Today                                                                      | Glossary term | New name                                                              |
|----------------------------------------------------------------------------|---------------|-----------------------------------------------------------------------|
| `LootDrop.drop`                                                            | Roll          | `roll`                                                                |
| `LootDrop.place`                                                           | Drop          | `drop`                                                                |
| `LootDrop.pickUp`, `DroppedItems.pickUpOne`                                | Loot          | `loot`, `lootOne`                                                     |
| `DropLoot`, `LootDropApi.dropLoot`                                         | Roll          | `RollLootDrop`, `rollLootDrop`                                        |
| `PlaceDroppedItem`, `LootDropApi.placeDroppedItem`                         | Drop          | `DropItemWithoutRolling`, `dropItemWithoutRolling`                    |
| `FindLoot`, `LootDropApi.findLoot`                                         | Find          | `FindDroppedItems`, `findDroppedItems`                                |
| `LootFound`, the local `lootFoundEvents` of `FindLoot`                     | Find          | `DroppedItemsFound`, `droppedItemsFoundEvents`                        |
| `LootDropped`                                                              | Drop          | `ItemDropped`                                                         |
| `PickUpDroppedItem`, `LootDropApi.pickUpDroppedItem`                       | Loot          | `LootDroppedItem`, `lootDroppedItem`                                  |
| `DroppedItemPickedUp`                                                      | Loot          | `DroppedItemLooted`                                                   |
| `RequestPickUpDroppedItem`, `OnRequestPickUpDroppedItem`                   | Loot          | `RequestLootDroppedItem`, `OnRequestLootDroppedItem`                  |
| `OnLootFound` and `InventoryApi.onLootFound`, in inventory                 | Find          | `OnDroppedItemsFound`, `onDroppedItemsFound`                          |

## lootDrop

### Domain

- `LootDrop.roll(lootTables, random)` picks the lootable item, or `null` for nothing, from the entries of the loot tables
  merged together. E.g.: the rat rolls `tier-2`, and a health potion is 10 in 28.
- `LootDrop.drop(lootableItemId, row, column)` drops the item on its tile and publishes `ItemDropped`.
- `LootDrop.loot(lootableItemId, row, column)` takes one copy of the item off its tile, through `DroppedItems.lootOne`, and
  publishes `DroppedItemLooted`. Its comment says "once a party member has looted it".
- `LootDropEvent`: `ItemDropped`, `DroppedItemsFound` ("a battle unit found dropped items on a tile"), `DroppedItemLooted`
  and `RequestLootDroppedItem`, whose comment says it is requested for an item a party member looted.

### Use cases

- `RollLootDrop` rolls the loot drop of the unit of a defeated battle unit and drops the item rolled, unless it rolls
  nothing or the unit has no loot drop.
- `DropItemWithoutRolling` drops an item for a unit without rolling its loot drop, e.g.: to prepare a scenario.
- `FindDroppedItems` publishes one `DroppedItemsFound` per tile with dropped items on the path, in walking order.
- `LootDroppedItem` takes a looted item off its tile, from the first unit that dropped it there. Its comment says "once a
  party member has looted it", for "once an inventory has stored it, and requested its pick-up".
- `DropItems` and `RestoreLootDrops` call `LootDrop.drop` and keep their names.

### Adapters

- `OnBattleUnitDefeated` calls `rollLootDrop`, `OnBattleUnitMoved` calls `findDroppedItems`, and
  `OnRequestLootDroppedItem` subscribes to `RequestLootDroppedItem` and calls `lootDroppedItem`.
- `LootDropApi` exposes `rollLootDrop`, `dropItemWithoutRolling`, `findDroppedItems` and `lootDroppedItem`.

## inventory

- `Inventory.store` records `RequestLootDroppedItem` right after `ItemStored`, and its comment says the item is looted, for
  "pick-up".
- `OnDroppedItemsFound` subscribes to `DroppedItemsFound` and calls `storeLootedItem` for each item. `InventoryApi` holds it
  as `onDroppedItemsFound`.

## screen

- `BattlefieldPresenter` subscribes to `ItemDropped` and `DroppedItemLooted`. Its own `dropLoot`, `pickUpLoot` and
  `displayedLoot` keep their names, as they wait for the `screen` section.

## Docs

- [Inventory](../architecture/inventory.md): Looting from the battlefield says `ItemDropped`, `findDroppedItems`,
  `DroppedItemsFound`, `RequestLootDroppedItem`, `lootDroppedItem` and `DroppedItemLooted`, and "loots" for "picks up".
- [Package structure](../architecture/package-structure.md): the tables say `ItemDropped`, `DroppedItemsFound` and
  `RequestLootDroppedItem`, "Rolls the loot drop of its unit and drops the item rolled" for "Drops the loot of its unit",
  "Announces the dropped items" for "the items", and "Takes it off its tile" for "Picks it up".
- [Presentation timeline](../architecture/presentation-timeline.md): the events of the Loot table are `ItemDropped` and
  `DroppedItemLooted`.
- [Exploration](../architecture/exploration.md): "looting a dropped item" for "a loot pick-up".
- [Game data](../architecture/game-data.md): "How to make a unit drop loot" is "How to give a unit a loot drop", as no link
  points to it, and the loot table's weights are turned into chances "when a loot drop is rolled", for "when the loot is
  dropped".
- [Dependencies](../architecture/dependencies.html) is generated again with `./gradlew dependencyReport`.

## Tests

- `LootDropTest`
    - `Roll should roll the lootable item when the roll falls within its weight` (replaces `Drop should drop the lootable
      item when the roll falls within its weight`), and the same for the nothing weight and the next loot table.
    - `Drop should drop the item on its tile and publish the event when an item is dropped` (replaces `Place should place
      the item on its tile ...`), and `Drop should keep the items already dropped first when the tile has dropped items`.
    - `Loot should remove one copy of the item and publish the event when the item lies on the tile`, `Loot should leave
      out the tile when its last item is looted` and `Loot should fail when the item does not lie on the tile` (replace
      the `PickUp` ones).
- `RollLootDropTest` (replaces `DropLootTest`): `should not drop an item when nothing is rolled` and `should not drop an
  item when the unit has no loot drop`, for "drop loot".
- `DropItemWithoutRollingTest` (replaces `PlaceDroppedItemTest`): `should drop the item on its tile when the unit has a
  loot drop` and `should not drop the item when the unit has no loot drop`.
- `FindDroppedItemsTest` (replaces `FindLootTest`): `should announce the dropped items found and keep them on their tile
  when the path crosses a tile with dropped items` and `should not announce dropped items when the path has none`.
- `LootDroppedItemTest` (replaces `PickUpDroppedItemTest`).
- `OnBattleUnitDefeatedTest`: `should roll the loot drop of its unit where a battle unit is defeated`.
- `OnBattleUnitMovedTest`: `should announce the dropped items found on its path when a battle unit moves`, and on its
  destination when it teleports.
- `OnRequestLootDroppedItemTest` (replaces `OnRequestPickUpDroppedItemTest`): `should take the dropped item off its tile
  when its looting is requested`.
- `DropItemsTest`: `should drop the item on the tile ...` and `should drop every copy ...`, for "place".
- `RollLootDropAcceptanceTest` (replaces `DropLootAcceptanceTest`).
- `OnDroppedItemsFoundTest` (replaces `OnLootFoundTest`): `should store every item found in the inventory when a battle
  unit finds dropped items`.
- `InventoryTest` and `StoreLootedItemTest` expect `RequestLootDroppedItem`, and the acceptance and screen tests call
  `dropItemWithoutRolling`.

## Not in this plan

- The ownerless loot drop (`LootDrop.ownerless`, `OWNERLESS_UNIT_ID`) has no unit and no loot tables: it only holds the
  items of a reward with no storage space left, so it is no loot drop as the glossary defines it. Whether the dropped items
  should live apart from the loot drops is still to be decided.
- `DropItemWithoutRolling` is only called by tests. Whether it should stay is still to be decided.
- Nothing subscribes to `LootDropCreated`: it is published, and only the tests read it. Whether it should stay is still to
  be decided.

### Waiting for other sections

- `lootTable`: Loot table, Entry, Weight and Nothing are still to be defined, as Roll uses them. `LootTable`'s comment
  "the loot drop turns them into chances" already fits Roll.
- `unit`: Unit is still to be defined, as a loot drop belongs to one.
- `quest`: Reward is still to be defined, as Drop mentions it.
- `party`: Party member is still to be defined, as Loot uses it.
- `screen`: `BattlefieldPresenter.dropLoot`, `pickUpLoot` and `displayedLoot`, `BattlefieldView.displayLoot` and
  `pickUpLoot`, `FakeBattlefieldView`, the `LOOT` layer and the Loot heading of
  [Presentation timeline](../architecture/presentation-timeline.md), while Loot is only a verb and Pick up is to be avoided.
  The translator comment of `ui.inventoryFull.leave`, "Leaves the item found on the ground", while a dropped item lies on a
  tile.

## Changes while building

- Names the plan left out follow the table: `DroppedItems.place` is `drop`, the local `droppedEntry` of `LootDrop.roll` is
  `rolledEntry`, `droppedLootableItemId` in `LootDropTest` is `rolledLootableItemId`, the `onRequestPickUpDroppedItem` of
  `LootDropApi` is `onRequestLootDroppedItem`, and the local `pickingUp` of `LootDroppedItemTest` is `looting`.
- More test names than the plan named say the same words, asserting the same: `SearchLootDropsSnapshotTest` says `should
  return nothing when no unit has a loot drop`, and `RestoreLootDropsTest` says `should fail when a unit no longer has a
  loot drop`, for "drops loot".
- More comments than the plan named say "loots" for "an inventory stores": every comment of `LootDropEvent`, `LootDrop`,
  `DropItems` and `FindDroppedItems`, and the comment of `RequestLootDroppedItem` says it is requested for an item a party
  member looted. `StoreLootedItem` says it "requests to take it off its tile, as it is looted", for "requests the
  battlefield to pick it up". [Inventory](../architecture/inventory.md) also says the reward items left are "found, looted
  and drawn", and that the loot drop of the unit is rolled.
- The comments and test names of `BattlefieldView`, `BattlefieldViewTest`, `BattlefieldViewExtension` and
  `BattlefieldPresenterTest` keep "picked up", as they go with `pickUpLoot`, which waits for the `screen` section.
