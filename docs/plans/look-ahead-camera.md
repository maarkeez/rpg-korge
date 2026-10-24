# Plan: A camera looking ahead of the leader

> **Status:** done, superseded by [a camera trailing the leader](trailing-camera.md).
>
> **Summary:** While exploring, the camera moves too often. When the leader reaches an edge of the viewport, the
> battlefield centers it on that axis, so walking straight scrolls every 3 steps, by 3 tiles, and the party waits for
> each scroll. Instead, while exploring, the leader lands one tile past the edge margin behind it, facing the rest of the
> viewport: walking straight scrolls every 6 steps, by 6 tiles. A battle and the other scrolls still center the tile.

## How it scrolls today

The viewport is 8x8 tiles (`VISIBLE_TILES`), and its outer ring (one tile) is the edge margin. `followTile` runs before
each step of the leader: when its next tile is on the margin on an axis, that axis centers it (column 3.5), clamped to
the battlefield. Walking east from column 3.5, it steps on 4.5, 5.5 and 6.5, then 7.5 is on the margin, so it scrolls
back to 3.5.

## Decisions

| Topic           | Decision                                                                                                                                                                                                  |
|-----------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Landing tile    | On the axis the leader walks along, the tile it steps on lands one tile past the edge margin behind it: column 1 walking east, column 6 walking west, row 1 walking south, row 6 walking north. Clamped to the battlefield. |
| Trigger         | Unchanged: an axis scrolls only when the tile is on the edge margin (or out of the viewport) on that axis.                                                                                                |
| Other axis      | Unchanged: it keeps its scroll, or centers the tile when the tile is on its edge margin (e.g.: after dragging the battlefield, or when a battle finishes).                                                |
| Scope           | Only the leader looks ahead, while exploring: its walking steps and its swaps with a party member. A battle move, a flash, a loot pick-up, the shop's buyer and the selected leader on a turn start still center the tile. The followers and the wandering CPU battle units do not move the camera, as today. |
| Turning around  | Nothing specific: right after a scroll, the leader stands on column 1, so walking back steps on the margin at once and scrolls 6 tiles the other way, landing on column 6. Walking back and forth anywhere else in the viewport never scrolls. |
| Animation       | Unchanged: 300ms, ease out (`SCROLL_DURATION`), awaited before the step, whatever the distance.                                                                                                          |
| Direction       | Taken from each step itself (from its previous tile to its tile), so the presenter only says how to follow, not which way.                                                                                |

For example, the leader walks east along row 3 of a 32 columns battlefield, from column 0, not scrolled:

| The leader steps on | Before                                       | After                                                        |
|---------------------|----------------------------------------------|--------------------------------------------------------------|
| Column 7            | Scrolls 3.5 tiles, centering it (`x = -168`) | Scrolls 6 tiles, landing on column 1 (`x = -288`)            |
| Column 10           | Scrolls 3 tiles, centering it (`x = -312`)   | Does not scroll                                              |
| Column 13           | Scrolls 3 tiles, centering it (`x = -456`)   | Scrolls 6 tiles, landing on column 1 (`x = -576`)            |
| Column 16           | Scrolls 3 tiles, centering it (`x = -600`)   | Does not scroll                                              |
| Column 19           | Scrolls 3 tiles, centering it (`x = -744`)   | Scrolls 6 tiles, landing on column 1 (`x = -864`)            |
| Column 18, back     | Does not scroll                              | Scrolls 6 tiles, landing on column 6 (`x = -576`)            |

Any other column does not scroll, before or after.

## BattlefieldView

- A `Following` enum: `NONE` (does not scroll), `CENTER` (today's rule) and `LOOK_AHEAD` (the landing tile above).
- `moveBattleUnit`: `followsTiles: Boolean = true` becomes `following: Following = CENTER`. Each step passes its
  direction (from the previous tile to its tile) to `followTile` when looking ahead.
- `swapBattleUnits`: only the exploring party swaps, so it looks ahead, from the leader's tile to the member's.
- `followTile(row, column, direction)`: per axis, it keeps the scroll while the tile is away from the edge margin, as
  today. Otherwise, the axis the direction walks along lands the tile on `TILE_SIZE` (walking towards the end) or
  `viewport size - 2 * TILE_SIZE` (walking towards the start), and the other axis centers it, both clamped to
  `minScroll..0`. Without a direction (flash, loot, shop, turn start), both axes center it, as today.

## BattlefieldPresenter

- `moveUnit`: passes `CENTER` while battling, `NONE` for a party follower while exploring, else `LOOK_AHEAD`.
- `wanderUnit`: passes `NONE`.

## Tests

`BattlefieldViewTest`, on a 16x16 battlefield, not scrolled, unless told otherwise:

- `MoveBattleUnit should land the battle unit one tile past the edge behind it when it walks east to the viewport edge
  looking ahead`: from (5, 6) to (5, 7): the scroll is `(-288.0, 0.0)`.
- `MoveBattleUnit should land the battle unit one tile past the edge behind it when it walks south to the viewport edge
  looking ahead`: from (6, 5) to (7, 5): the scroll is `(0.0, -288.0)`.
- `MoveBattleUnit should scroll back when the battle unit turns around right after a scroll looking ahead`: from (5, 6)
  to (5, 7), then back to (5, 6): the scroll is `(0.0, 0.0)`. Today it stays `(-168.0, 0.0)`.
- `MoveBattleUnit should center the other axis when the battle unit is on its edge looking ahead`: from (7, 6) to (7, 7):
  the scroll is `(-288.0, -168.0)`.
- `MoveBattleUnit should clamp the landing tile to the battlefield when looking ahead`: a 16 rows, 10 columns
  battlefield, from (5, 6) to (5, 7): the scroll is `(-96.0, 0.0)`.
- `MoveBattleUnit should only scroll the axis whose viewport edge the battle unit reaches when it moves` is kept, for
  `CENTER` (`-168.0`).
- `SwapBattleUnits should scroll to the member's tile before the battle units move when it is at the viewport edge`: the
  scroll becomes `(-288.0, 0.0)`.

`BattlefieldPresenterTest` (`FakeBattlefieldView` records the `Following` of each move, in place of `unfollowedMoves`):

- `MoveUnit should follow the leader of the party looking ahead when it walks while exploring` (was `should follow the
  leader of the party`).
- `MoveUnit should not follow a follower of the party when it walks while exploring`: `NONE`.
- `MoveUnit should center the battle unit when it moves in a battle`: `CENTER`.
- The wandering tests: `NONE`.

## Docs

- [Exploration](../architecture/exploration.md#walking): the camera follows the leader. When it reaches an edge of the
  viewport, the leader lands one tile past the edge behind it on that axis, so it sees 6 tiles ahead.
