# Plan: Dto without methods

> **Status:** done.
>
> **Summary:** A new architecture rule keeps the `Dto` of each aggregate root as plain data: neither it nor any type nested
> in it declares methods or computed properties, and no extension is declared on them elsewhere. Companion object methods
> stay allowed, as factories. Three places break the rule today: `Party.Dto.PositionDto.distanceTo`,
> `LootableItem.Dto.sellPrice` and `ExplorationPresenter`'s `Npc.Dto.isMerchant()`. The behaviour behind them moves
> to the domain, and is reached through query use cases.

## Decisions

| Topic               | Decision                                                                                                                                       |
|---------------------|------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope               | The `Dto` of every aggregate root, and every class, enum and interface nested in it at any depth (`EntryDto`, `PositionDto`, `EffectTargetDto`...). |
| Methods             | No declared `fun`. The members a data class generates (`copy`, `equals`, `componentN`...) are not declared, so they are fine.                    |
| Computed properties | A property with a custom getter (`val x get() = ...`) counts as a method, and is not allowed. Enum constructor properties (`DirectionDto.rowOffset`) are fine. |
| Companion objects   | Their methods are allowed: they are the factories of the Dto (`EntryDto.lootableItem`, `LootTypeDto.consumable`, `ApplicationSourceDto.battleUnit`...). |
| Extensions          | No extension function or property, anywhere in production, has a receiver that is a `Dto` or a type nested in it.                              |
| Derived values      | A value derived from a Dto is not a Dto field (loaders and tests build Dtos as input, and would have to supply it). It is computed by the domain and reached through a query use case. |

## Violations today

| Where                                                    | What                       | Fix                                                                             |
|----------------------------------------------------------|----------------------------|---------------------------------------------------------------------------------|
| `Party.Dto.PositionDto.distanceTo`                       | Method in a nested Dto     | Moves into `Party`, private. Only `Line.closestFirst` uses it.                   |
| `LootableItem.Dto.sellPrice`                             | Computed property          | `LootableItem.sellPrice()` in the domain, reached through `SearchSellPrice`.     |
| `ExplorationPresenter`'s `Npc.Dto.isMerchant()`          | Extension on a Dto         | The domain already has `Npc.isMerchant()`, reached through `SearchIsMerchant`.   |

## party

### Domain

- Remove `Party.Dto.PositionDto.distanceTo`, and compute the distance in rows plus columns in a private function of
  `Party`, used by `Line.closestFirst`.

## lootableItem

### Domain

- Remove `LootableItem.Dto.sellPrice`.
- Add `LootableItem.sellPrice()`: what a merchant pays for it, half its price rounded down.

### Use cases

- Add the query `SearchSellPrice(lootableItemId): Int?`, `null` when the item does not exist.

### Adapters

- Modify `LootableItemApi` with `searchSellPrice`.

## inventory

### Use cases

- Modify `SellItem` and `SearchSellRefusal` to depend on `SearchSellPrice` instead of reading `lootableItem.sellPrice`.
  An item without a sell price is an item that does not exist: `InventoryLootableItemDoesNotExist`, and
  `TradeRefusalDto.NOT_STORED`, as today.

## npc

### Use cases

- Add the query `SearchIsMerchant(npcId): Boolean`, `false` when the npc does not exist, using `Npc.isMerchant()`.

### Adapters

- Modify `NpcApi` with `searchIsMerchant`.

## screen

- Modify `ShopPresenter.selectItem` to read the sell price from `lootableItemApi.searchSellPrice`, before selecting the
  slot.
- Modify `ExplorationPresenter` to remove its `Npc.Dto.isMerchant()` extension and ask `npcApi.searchIsMerchant` with
  the npc id.

## Architecture test

In `DomainArchitectureTest`:

- In `should follow domain encapsulation rules when is the aggregate root`, for the `Dto` and every declaration nested in
  it, assert it declares no function (leaving companion objects out) and no property with a getter. Each failure names
  the declaration, e.g. `Aggregate root 'Party.Dto.PositionDto' must not declare method 'distanceTo'`.
- Add `should not declare extensions on the Dto of an aggregate root`: collect the fully qualified names of every `Dto`
  and the types nested in it, and assert no extension function or property in `Konsist.scopeFromProduction()` has one of
  them as its receiver type.
- Check that both assertions fail before the fixes above, on the three known violations, and only on them.

## Tests

- `LootableItemTest`: the sell price is half the price, rounded down (e.g.: 51 sells for 25), and 0 for a worthless item.
- `SearchSellPrice` and `SearchIsMerchant`: an existing item or npc, and a missing one, and an npc without a stock.
- `PartyTest`: the line order behind the leader is unchanged (existing tests cover `closestFirst`).
- The existing sell and trade acceptance tests stay green without changes in their expectations.
