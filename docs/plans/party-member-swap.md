# Plan: Party member swap

> **Status:** done.
>
> **Summary:** While exploring, a party member standing on the leader's next tile no longer blocks the party: the leader
> and that member swap tiles. The party keeps a line order, who follows whom while walking, apart from the party order,
> and rebuilds it after a swap, so the followers always line up behind the leader again, e.g.: in a dead-end corridor.

## Decisions

| Topic                | Decision                                                                                                                                                           |
|----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope                | Only a party member blocking the leader. Npcs, CPU battle units, void tiles and the battlefield edge still block the leader.                                         |
| Rule                 | When the leader's next tile holds a party member, any of them (not only the one behind), the leader and that member swap tiles.                                      |
| The step             | A swap is the whole step: only the leader and that member move, the other followers stay where they are.                                                           |
| Battlefield          | The battlefield swaps both occupants at once (`RequestSwapOccupants`), so a tile never holds two occupants. It announces both occupied tiles.                        |
| Battle units         | `RequestSwapBattleUnits` walks both battle units to each other's tile: each publishes its `BattleUnitMoved`, which names the battle unit it swapped with.           |
| Counts as a walk     | Both count as moved during the second (no standing terrain effect), the loot on their tiles is found, and the party has walked (`PartyWalked`), as after any step. |
| Line order           | The party keeps its line order apart from its party order. The followers walk in line order. The party order (the next leader, the shop dots) never changes.       |
| Rebuilding the line  | Only after a swap: from the leader, the closest remaining member by tile distance (rows + columns) follows next, ties broken by party order.                        |
| Line resets          | Back to the party order when the exploration starts or resumes. A recruited member joins the end of the line, a defeated one leaves it.                            |
| Unreachable steps    | Unchanged: a follower that can not reach its tile skips its step, and walks the shortest path to it on a later walk.                                                |
| Battles              | Only while exploring: in a battle, an ally still blocks its tile.                                                                                                   |
| Animation            | Both battle units are displayed on their new tiles at once, in a single exploring step (`EXPLORING_STEP_DURATION`, 75ms).                                          |

For example, the party walks into a dead-end corridor, the leader `K` first, then `L` and `M`, `#` being void:

| The player     | The party                                                                                                  |
|----------------|------------------------------------------------------------------------------------------------------------|
| Walks east     | `K` reaches the end of the corridor: `# M L K #`.                                                          |
| Walks west     | `K` swaps with `L`: `# M K L #`. The line stays `K`, `L`, `M`: both are as close, so the party order wins. |
| Walks west     | `K` swaps with `M`: `# K M L #`. The line becomes `K`, `M`, `L`, as `M` is now the closest to `K`.         |
| Walks west     | `K` leaves the corridor, `M` and `L` follow it in line.                                                    |

## party

- `Party` keeps a `Line`: `walkParty` steps follow it, and the `Dto` exposes it (`lineBattleUnitIds`), the leader first.
- `swapLeader` awaits the leader's step onto the member's tile, and rebuilds the line from the positions after the swap.
- `WalkParty` swaps with the party member on the leader's next tile (`SearchOccupant`), and requests `RequestSwapBattleUnits`.

## battleUnit

- `RequestSwapBattleUnits`, `OnRequestSwapBattleUnits` and `SwapBattleUnits`. `BattleUnit.swap` publishes `BattleUnitMoved`, with `swappedWithBattleUnitId`.
- `SwapBattleUnits` requests `RequestSwapOccupants` once, instead of a `RequestMoveOccupant` per battle unit.

## battlefield

- `RequestSwapOccupants`, `OnRequestSwapOccupants` and `SwapOccupants`. `Battlefield.swapOccupants` publishes both `BattlefieldTileOccupied`.

## screen

- `BattlefieldPresenter` plays both `BattleUnitMoved` of a swap in a single step, with `BattlefieldView.swapBattleUnits`.

## Docs

- `exploration.md` describes the swap and the line order.
