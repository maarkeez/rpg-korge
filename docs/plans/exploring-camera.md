# Plan: Exploring camera

> **Status:** done.
>
> **Summary:** While exploring along a border of the battlefield, the camera bounces up and down on every step, even
> though no battle unit is offscreen. Only the leader moves the camera while exploring, and the camera scrolls only the
> axis whose viewport edge the battle unit reaches.

## The bug

`BattlefieldView.followTile` runs on each step of every party member: `BattlefieldPresenter.moveUnit` moves them with the
default `followsTiles = true`. When the tile is in the outer ring of the viewport on either axis, it centers the tile on
**both** axes.

On a border of the battlefield (e.g.: column 0), the scroll is already clamped, so the tile stays in that outer ring and
every step scrolls. As the vertical axis is centered too, the camera centers on the leader's row, then on the next party
member's row, and so on: it bounces up and down. Near another border (e.g.: row 12 of a 16 rows battlefield, two tiles
left of the viking), the centering is clamped on some rows only, so the bounces are not even.

## Decisions

| Topic           | Decision                                                                                                                                                                             |
|-----------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Camera owner    | While exploring, only the leader's steps move the camera. The followers walk with `followsTiles = false`, as the wandering CPU battle units do. In a battle, any moving battle unit still moves it. |
| Scroll rule     | `followTile` decides each axis on its own: an axis whose edge the tile is away from keeps its scroll, the other one centers the tile, clamped to the battlefield. Nothing scrolls when neither axis changes. |
| Borders         | Nothing specific: on a border, the centered scroll is clamped back to the current one, so that axis does not scroll.                                                                  |
| Other callers   | The rule applies to every caller of `followTile`: moving, flashing, picking up loot and the shop's buyer.                                                                             |
| Camera style    | Unchanged: the same edge margin (one tile) and scroll animation (300ms, ease out) while exploring and in a battle.                                                                    |

For example, a party of two walks south along column 0 of a 16x16 battlefield, not scrolled:

| The party                         | Before                                                     | After                       |
|-----------------------------------|------------------------------------------------------------|-----------------------------|
| The leader steps on row 4         | Scrolls down, centering row 4                              | Does not scroll             |
| The follower steps on row 3       | Scrolls up, centering row 3                                | Does not scroll             |
| The leader steps on row 7         | Scrolls down, centering row 7                              | Scrolls down, centering row 7, as it reaches the bottom edge |

## BattlefieldView

- `followTile`: computes the target scroll per axis, keeping the current one when `isAwayFromViewportEdges` on that axis,
  else the centered one clamped to `minScroll..0`. Returns when the target is the current scroll, otherwise scrolls and
  awaits it as today.

## BattlefieldPresenter

- `moveUnit`: passes `followsTiles = false` when exploring (`!isBattling()`) and the battle unit is not the party leader
  (`partyApi.searchParty()?.leaderBattleUnitId`).

## Tests

`BattlefieldViewTest`:

- `MoveBattleUnit should not scroll the battlefield when the battle unit moves along a border of the battlefield`:
  16x16, not scrolled, from (3, 0) to (4, 0): the scroll stays `(0.0, 0.0)`. Fails today, centering row 4 (`y = -24`).
- `MoveBattleUnit should only scroll the axis whose viewport edge the battle unit reaches`: 16x16, not scrolled, from
  (5, 6) to (5, 7): the scroll is `(-168.0, 0.0)`. Fails today, centering row 5 too (`y = -72`).

`BattlefieldPresenterTest`:

- `BattleUnitMoved should not scroll the battlefield when a party follower walks while exploring`.
- `BattleUnitMoved should scroll the battlefield when the party leader walks to the viewport edge while exploring`.

## Docs

- [Exploration](../architecture/exploration.md#walking): the camera follows the leader, scrolling when it reaches an edge
  of the viewport.
