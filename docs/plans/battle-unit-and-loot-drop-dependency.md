# Plan: Battle unit and loot drop dependency

> **Status:** done.
>
> **Summary:** `battleUnit` depended on `lootDrop` only to record `RequestDropLoot` and `RequestFindLoot`. Since
> `battlefield` no longer depends on `lootDrop`, the edge can be inverted without a cycle: `lootDrop` now listens to
> `BattleUnitDefeated` and `BattleUnitMoved`, and `battleUnit` imports nothing from `lootDrop`. Supersedes the direction
> chosen in [Battle unit and loot drop dependency cycle](battle-unit-and-loot-drop-cycle.md).

## Decisions

| Topic            | Decision                                                                                                                                                                             |
|------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Edge             | `lootDrop` → `battleUnit`. `battleUnit` no longer imports `lootDrop`, in production nor in its tests. No cycle: `battleUnit` reaches no subdomain that reaches `lootDrop`.           |
| Mechanism        | `lootDrop` listens to the facts of `battleUnit` (`OnBattleUnitDefeated`, `OnBattleUnitMoved`). `RequestDropLoot`, `RequestFindLoot` and their listeners are deleted.                 |
| Unit of the drop | `BattleUnitDefeated` carries the `unitId`. `DropLoot(unitId, row, column)` is unchanged, and `LootDropApi` does not take `BattleUnitApi`.                                            |
| Position         | `OnBattleUnitMoved` maps the path into `LootDrop.Dto.PositionDto`, so `FindLoot` keeps its own position type.                                                                        |
| Event order      | On a defeat, `LootDropped` is still dispatched after `RequestRemoveOccupant`. On a move, `LootFound` is now dispatched before the events of the occupancy update, which is accepted. |
| Guard            | `SubdomainDependencyArchitectureTest` is enough: importing `lootDrop` from `battleUnit` again would be a cycle.                                                                      |
| Docs             | `package-structure.md` and `inventory.md` describe the listeners. The dependency report is regenerated.                                                                              |
| Delivery         | One commit, with this plan, pushed to `main`.                                                                                                                                        |

## battleUnit

- Add `unitId` to `BattleUnitDefeated`.
- Modify `BattleUnit` to no longer record `RequestDropLoot` nor `RequestFindLoot`.

## lootDrop

- Add `OnBattleUnitDefeated` and `OnBattleUnitMoved`. Delete `OnRequestDropLoot`, `OnRequestFindLoot`, `RequestDropLoot` and `RequestFindLoot`.
- Modify `LootDropApi` to subscribe the new listeners.

## Tests

- `OnBattleUnitDefeatedTest` and `OnBattleUnitMovedTest` replace `OnRequestDropLootTest` and `OnRequestFindLootTest`, the latter also covering a teleport.
- `BattleUnitTest`, `MoveBattleUnitTest` and `WalkBattleUnitTest` no longer expect the requests, and `BattleUnitDefeated` carries the `unitId`.
- The loot acceptance tests are unchanged.
