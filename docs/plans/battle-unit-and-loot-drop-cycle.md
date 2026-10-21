# Plan: Battle unit and loot drop dependency cycle

> **Status:** done, superseded by [Battle unit and loot drop dependency](battle-unit-and-loot-drop-dependency.md):
> `lootDrop` now listens to `battleUnit` instead.
>
> **Summary:** `battleUnit` → `battlefield` → `lootDrop` → `battleUnit` was the last cycle between subdomains. `lootDrop`
> no longer depends on `battleUnit`: it declares `RequestDropLoot`, which the `BattleUnit` aggregate records right after
> `BattleUnitDefeated`, carrying the unit the loot drop belongs to. `SubdomainDependencyArchitectureTest` now fails on
> any cycle, not only on two subdomains importing each other.

## Decisions

| Topic                | Decision                                                                                                                                                                                   |
|----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Edge                 | `lootDrop` → `battleUnit` is cut. `battleUnit` depends on `lootDrop` instead. `battlefield` keeps listening to `LootDropped`.                                                              |
| Request              | `lootDrop` declares `RequestDropLoot(unitId, row, column)`, listened by `OnRequestDropLoot`, calling `DropLoot`. `OnBattleUnitDefeated` is deleted. No `battleUnitId`: it is not needed.   |
| Publishing           | The `BattleUnit` aggregate records the request right after `BattleUnitDefeated`, before `RequestRemoveOccupant`.                                                                           |
| Event order          | `LootDropped` is still queued after `RequestRemoveOccupant` and the events of the other `BattleUnitDefeated` listeners, so the tile is vacated before the item lands on it, as before.     |
| `DropLoot`           | Takes the `unitId`, `row` and `column`, and no longer searches the battle unit. `LootDropApi` no longer takes `BattleUnitApi`.                                                             |
| `BattleUnitDefeated` | Unchanged: no `unitId`, as no listener needs it.                                                                                                                                           |
| Guard                | `SubdomainDependencyArchitectureTest` replaces the two-way check with a single cycle check: the groups of subdomains that reach each other, even indirectly, as highlighted by the report. |
| Docs                 | `package-structure.md` states the guard catches indirect cycles, and has the loot drop example. The dependency report is regenerated, with no cycle left.                                  |
| Delivery             | One commit, with this plan, pushed to `main`.                                                                                                                                              |

| What `lootDrop` needed from `battleUnit` | Now                                                                                |
|------------------------------------------|------------------------------------------------------------------------------------|
| Listening to `BattleUnitDefeated`        | Listening to its own `RequestDropLoot`, recorded by `BattleUnit` after the defeat. |
| `SearchBattleUnitById`, for its `unitId` | The `unitId` travels in `RequestDropLoot`.                                         |

## lootDrop

- Add `LootDropEvent.RequestDropLoot` and `OnRequestDropLoot`. Delete `OnBattleUnitDefeated`.
- Modify `DropLoot` and its KDoc, and `LootDropApi`.

## battleUnit

- Modify `BattleUnit` to record `RequestDropLoot` after `BattleUnitDefeated`.

## screen

- Modify `BattleScene` to build `LootDropApi` without `BattleUnitApi`.

## Tests

- `OnRequestDropLootTest` replaces `OnBattleUnitDefeatedTest`.
- `DropLootTest` drops the loot of a unit, without battle units.
- `BattleUnitTest` expects the request after `BattleUnitDefeated`.
- `SubdomainApis`, `DropLootAcceptanceTest` and `SetupBattleAcceptanceTest` build `LootDropApi` without `BattleUnitApi`.
- `SubdomainDependencyArchitectureTest` fails on any cycle.
