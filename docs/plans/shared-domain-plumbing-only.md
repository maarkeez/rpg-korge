# Plan: Keep only the plumbing in the shared domain

> **Status:** to do.
>
> **Summary:** `shared.domain` holds the plumbing every subdomain needs (`DomainEvent`, `EventBus`, `Subscription`,
> `Snapshottable`), and two game rules: `Gold.MAXIMUM` (99,999) and `KnownAbilities.MAXIMUM` (6). They are there only
> because `unit`, which may only depend on `shared`, checks them when the units load, while `inventory` and `battleUnit`
> check them at runtime. E.g.: the viking's `startingGold = 100` is checked against 99,999 by `Unit` and again by
> `Inventory.create`. So two subdomains share the rule, and any other one can import it. Instead, each rule has a
> single owner: `inventory` keeps the gold cap private, checked in CI against the units' TOMLs by a resources test.
> `unit` owns the abilities cap and hands it to `battleUnit` through a query, and `battleUnit` keeps it, and saves it,
> like `baseMaximumHealthPoints`. An architecture test keeps `shared.domain` to its plumbing. It builds on
> [Package structure](../architecture/package-structure.md), [Inventory](../architecture/inventory.md) and
> [Save and load](../architecture/save-and-load.md).

## How the limits are checked today

| Limit                         | Checked by                                                                 | When                                    |
|-------------------------------|----------------------------------------------------------------------------|-----------------------------------------|
| `Gold.MAXIMUM`, 99,999        | `Unit.StartingGold` (`StartingGoldAboveLimit`)                             | The unit TOMLs load                     |
|                               | `Inventory.Gold` (`GoldAboveLimit`), `Inventory.sellRefusal` (`GOLD_FULL`) | Create, buy, sell, restore from a save  |
| `KnownAbilities.MAXIMUM`, 6   | `Unit.Abilities` (`AbilitiesAboveLimit`)                                   | The unit TOMLs load                     |
|                               | `BattleUnit.canLearnAbility`, `learnAbility` (`AbilitiesAboveLimit`)       | Learning an ability, e.g.: the teleport essence |

`unit`'s gold check was always a copy: `CreateInventory` calls `Inventory.create(gold = startingGold)`, which already
throws `GoldAboveLimit`.

## Decisions

| Topic               | Decision                                                                                                                                                                                                                                                                                                                                                         |
|---------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope               | `Gold.kt` and `KnownAbilities.kt` leave `shared.domain`, which then holds only `DomainEvent`, `EventBus`, `Subscription` and `Snapshottable`, and an architecture test keeps it that way.                                                                                                                                                                        |
| Shared domain       | `shared.domain` holds plumbing only: a game rule belongs to the subdomain owning its concept. Chosen over a shared kernel of game rules, as any subdomain could then import a rule it does not own.                                                                                                                                                             |
| Gold cap            | `inventory` owns it, as `private const val MAXIMUM_GOLD = 99_999` in `Inventory.kt`: nothing outside the aggregate reads it (the shop only gets `GOLD_FULL` and shows `Gold full`). Chosen over a public `inventory.domain.Gold`, as nobody needs it, and over `unit.domain`, as the cap guards the bag, not the unit catalogue.                                  |
| Unit's starting gold | `Unit` keeps `NegativeStartingGold` and drops `StartingGoldAboveLimit`. A unit TOML above 99,999 now fails in CI, through `InventoryUnitResourcesTest`, or when its battle unit is deployed, no longer when the units load.                                                                                                                                      |
| Gold resources test | `InventoryUnitResourcesTest` creates an `Inventory` with the `startingGold` of every real unit, expecting none to fail, in the style of `NpcUnitResourcesTest`. Chosen over a test in `unit` with a literal 99,999, as `unit` would know an inventory rule again.                                                                                                 |
| Abilities cap       | `unit` owns it, one value for every unit (6, one per button of the ability bar), as `Unit.maximumAbilities()`, a companion function. `Unit.Abilities` keeps checking it when the units load. Chosen over a per-unit TOML field, as no unit needs another cap yet.                                                                                              |
| Abilities handoff   | `battleUnit` reads it through a new query, `SearchMaximumAbilities()`, returning an `Int`. Chosen over a `maximumAbilities` field of `Unit.Dto`, as `Unit.Dto` is also the input of `Unit.create` and `DomainArchitectureTest` forbids getters and defaults on it; over `Unit.create` taking parameters, as it breaks the `create(dto)` of 7 aggregates; and over `battleUnit` reading the constant directly, as two domains would share it again. No argument, as the cap is the same for every unit: a per-unit cap would change its 1 caller. |
| Battle unit's cap   | `BattleUnit` keeps its own `MaximumAbilities`, given to `deploy`, kept by `respawn` (as its learned abilities are, so the cap never falls below them), and saved in its snapshot, like `baseMaximumHealthPoints`, so an effect raising it later is not lost on load. Chosen over re-reading it from `unit` on restore, which only holds while the cap never changes. |
| Save                | The battle unit snapshot gains `maximumAbilities`, so `SAVE_VERSION` goes from 1 to 2, with a new `v2.json` fixture, keeping `v1.json`.                                                                                                                                                                                                                          |
| Errors              | `InventoryError.GoldAboveLimit` and `UnitError.AbilitiesAboveLimit` no longer hold the number in their message, as the caps are private: e.g.: `The inventory gold is above its limit`.                                                                                                                                                                          |
| Architecture        | No dependency between subdomains changes: `inventory` → `unit` and `battleUnit` → `unit` already exist, and no row of the restricted subdomains table changes. A new `SharedDomainArchitectureTest` pins `shared.domain` to its 4 declarations.                                                                                                                  |
| Tests               | Domain and use case tests, two resources tests loading the real TOMLs, the save fixture, and an architecture test.                                                                                                                                                                                                                                              |
| Out of scope        | A cap per unit, an effect raising the cap, and checking at deploy that a unit's abilities fit the cap (`Unit` already does it when the units load).                                                                                                                                                                                                             |
| Delivery            | 3 milestones, each green on CI. See [Milestones](#milestones).                                                                                                                                                                                                                                                                                                   |

## Milestones

Each milestone is a few commits straight on `main`, green on CI.

| #  | Milestone                     | Done when                                                                                                     | Status | Done in |
|----|-------------------------------|---------------------------------------------------------------------------------------------------------------|--------|---------|
| M1 | Gold owned by `inventory`     | `Gold.kt` is gone, the cap is private to `Inventory.kt`, and `InventoryUnitResourcesTest` checks every unit.   | ✅      | `e16c8f3` |
| M2 | Abilities cap owned by `unit` | `KnownAbilities.kt` is gone, `BattleUnit` gets its cap from `SearchMaximumAbilities` and saves it, `SAVE_VERSION` is 2. | ⬜      |         |
| M3 | Plumbing only in `shared`     | `SharedDomainArchitectureTest` pins `shared.domain`, and `package-structure.md` states the rule.               | ⬜      |         |

### M1 commits

1. Move the gold cap to `inventory`, private, drop `StartingGoldAboveLimit` from `unit`, delete `shared.domain.Gold`,
   add `InventoryUnitResourcesTest`, and update [Inventory](../architecture/inventory.md) and
   [Game data](../architecture/game-data.md).

### M2 commits

1. Move the abilities cap to `Unit.maximumAbilities()`, add `SearchMaximumAbilities`, give `BattleUnit` its
   `MaximumAbilities` from `BattleUnitDeployment`, save it, bump `SAVE_VERSION` to 2 with the `v2.json` fixture,
   delete `shared.domain.KnownAbilities`, add `UnitResourcesTest`, and update [Game data](../architecture/game-data.md)
   and [Save and load](../architecture/save-and-load.md).

### M3 commits

1. Add `SharedDomainArchitectureTest`, and state the rule in [Package structure](../architecture/package-structure.md).

## Reference

### inventory

- `Inventory.kt` declares `private const val MAXIMUM_GOLD = 99_999`, read by its `Gold` value class (create, buy, sell,
  restore) and by `sellRefusal` (`GOLD_FULL`). It no longer imports `shared.domain.Gold.MAXIMUM`.
- `InventoryError.GoldAboveLimit` reads `The inventory gold is above its limit`.

### unit

- `Unit.StartingGold` only refuses a negative amount (`NegativeStartingGold`). `UnitError.StartingGoldAboveLimit` is
  removed.
- `Unit.maximumAbilities()`, in its companion object, returns 6. `Unit.Abilities` refuses more
  (`AbilitiesAboveLimit`, `Unit abilities are above their limit`).
- `SearchMaximumAbilities()`, in `unit.usecases.queries`, returns `Unit.maximumAbilities()`. `UnitApi` builds it as
  `searchMaximumAbilities`.
- `Unit.Dto` does not change.

### battleUnit

- `BattleUnit` holds a private `MaximumAbilities` value class. `canLearnAbility(abilityId)` is true when it does not
  know the ability and knows fewer than its `MaximumAbilities`, and `learnAbility` fails with `AbilitiesAboveLimit`
  otherwise. Their signatures do not change.
- `BattleUnit.deploy(..., maximumAbilities: Int, ...)` takes the cap. `BattleUnitDeployment` takes
  `SearchMaximumAbilities` and passes it, and `BattleUnitApi` wires `unitApi.searchMaximumAbilities`.
- `respawn(unit, row, column)` keeps its `MaximumAbilities`, like its learned abilities.
- `BattleUnit.Dto.SnapshotDto` and `BattleUnitSnapshot` gain `maximumAbilities`, written by `toSnapshot()` and read back
  by `restore(snapshot)`. `BattleUnitSnapshottable` does not change.
- `BattleUnitMother.battleUnit` takes `maximumAbilities: Int = 6`.

### save

- `Save.SAVE_VERSION` is 2. `src/jvmTest/fixtures/save/v2.json` is written with
  `./gradlew jvmTest --tests '*SaveFixtureTest*' -Drpg.updateSaveFixtures=true`, and `v1.json` is kept.

### shared

- `shared.domain` declares only `DomainEvent`, `EventBus`, `Subscription` and `Snapshottable`.

### Docs

- [Package structure](../architecture/package-structure.md): a rule that `shared.domain` holds only the plumbing of
  the event bus and the save, never a game rule, enforced by `SharedDomainArchitectureTest`.
- [Inventory](../architecture/inventory.md): the cap is private to `Inventory`, no longer `Gold.MAXIMUM`, and checked
  against the unit resources by `InventoryUnitResourcesTest`.
- [Game data](../architecture/game-data.md): `startingGold` is checked by `NegativeStartingGold` only, and must fit
  the inventory's 99,999, checked in CI. A unit has at most 6 abilities, `Unit.maximumAbilities()`.
- [Save and load](../architecture/save-and-load.md): the battle unit snapshot holds its maximum abilities.

### Tests

- `UnitTest`
  - `Create should create a unit with its starting gold, from 0` (replaces `Create should create a unit with its
    starting gold, from 0 to 99,999`)
  - `Create should fail when the starting gold is negative` (replaces `Create should fail when the starting gold is
    negative or above 99,999`)
  - `MaximumAbilities should be 6, one per button of the ability bar`
- `SearchMaximumAbilitiesTest`
  - `should return the maximum abilities of a unit`
- `UnitResourcesTest`
  - `should load every unit in the unit resources`
- `InventoryUnitResourcesTest`
  - `should create an inventory with the starting gold of every unit in the unit resources`
- `BattleUnitTest`
  - `CanLearnAbility should be true when the battle unit does not know the ability and knows fewer than its maximum`
    (replaces `CanLearnAbility should be true when the battle unit does not know the ability and knows fewer than 6`)
  - `CanLearnAbility should be false when the battle unit already knows its maximum abilities` (replaces `CanLearnAbility
    should be false when the battle unit already knows 6 abilities`)
  - `ApplyImmediateEffect should fail when the effect teaches an ability and the battle unit already knows its maximum`
    (replaces `ApplyImmediateEffect should fail when the effect teaches an ability and the battle unit already knows 6`)
  - `Respawn should keep the maximum abilities of the battle unit`
- `BattleUnitSnapshottableTest`
  - `Restore should rebuild the same state when it restores the snapshot it took`, with a maximum of abilities other
    than 6
- `SaveFixtureTest`, with `v2.json`
- `SharedDomainArchitectureTest`
  - `should only declare the plumbing of the event bus and the save when it is the shared domain package`

## Known limitations

- A unit TOML with more than 99,999 starting gold no longer fails when the game starts: CI catches it, and otherwise it
  fails when its battle unit is deployed.
- Bumping `SAVE_VERSION` to 2 makes every earlier save incompatible.
- `Unit.maximumAbilities()` is public, so another subdomain could call it instead of `SearchMaximumAbilities`: only the
  convention stops it.
