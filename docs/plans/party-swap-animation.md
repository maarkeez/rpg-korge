# Plan: Party swap animation

> **Status:** done.
>
> **Summary:** While exploring, the leader and the party member it swaps with trade tiles instantly, which reads as a
> glitch. Instead, both glide to each other's tile at once: the leader hops over the member in a small arc, and the member
> slides straight underneath. Only the swap is animated: the other steps still snap from tile to tile.

## Decisions

| Topic           | Decision                                                                                                                                                                         |
|-----------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Motion          | Both battle units glide to each other's tile at once. The leader rises in an arc, `SWAP_HOP_HEIGHT` (a third of a tile) at its peak half-way, and lands on the member's tile. The member slides straight to the leader's tile. |
| Drawing order   | The leader is drawn over the member while they cross, and both over the terrain and the battle units around them.                                                                |
| Duration        | `SWAP_DURATION`, 150ms, two exploring steps. The walk waits for it, as the party only walks once the presentation timeline is idle.                                               |
| Scope           | Only the swap. The exploring steps, the battle moves and the wandering keep snapping from tile to tile.                                                                          |
| Camera          | Unchanged: the battlefield follows the leader's new tile first (one axis at a time, see [Exploring camera](exploring-camera.md)), and the swap plays once it has scrolled.        |
| The leader      | Named by the presenter (the party leader), not deduced from the order the two `BattleUnitMoved` are published in.                                                                |
| Domain          | Unchanged: the swap is resolved as today, only its display changes.                                                                                                             |

For example, the leader `K` swaps with `M`, on its east:

| Time   | The screen                                                                     |
|--------|--------------------------------------------------------------------------------|
| 0ms    | `K` and `M` stand on their tiles.                                              |
| 75ms   | Both are half-way, `K` a third of a tile higher, drawn over `M`.               |
| 150ms  | `M` stands on `K`'s former tile, and `K` on `M`'s. The party can walk again.   |

## BattlefieldView

- A `movingBattleUnits` container in the viewport, right above `battlefieldGrid`, kept at its scroll. It only holds the
  battle units while they glide, so they are not sorted with the tiles nor drawn under the tiles below them.
- `swapBattleUnits(leaderRow, leaderColumn, leaderUnitId, memberRow, memberColumn, memberUnitId)`:
  1. Follows the member's tile, where the leader lands, and awaits the scroll, as today.
  2. Removes both battle units from their tiles, and adds an image of each to `movingBattleUnits`, on its tile, the
     leader's last, so it is drawn on top.
  3. Animates both, in parallel, for `SWAP_DURATION`: both glide straight to each other's tile (linear), and the
     leader's image, inside its gliding container, rises by `SWAP_HOP_HEIGHT` (ease out) and falls back (ease in), each
     in half the duration. So the arc does not fight the glide over the same coordinate.
  4. Removes both images, and displays each battle unit on its new tile.
- `SWAP_DURATION = 150.milliseconds` and `SWAP_HOP_HEIGHT = TILE_SIZE / 3.0`, next to `EXPLORING_STEP_DURATION`.
- A battlefield displayed again (`displayBattlefield`) clears `movingBattleUnits`, as it cancels the animator.

## BattlefieldPresenter

- `swapUnit`: once both `BattleUnitMoved` of the swap are received, passes the party leader's as the leader, whichever
  came first.

## Tests

`BattlefieldViewTest`:

- `SwapBattleUnits should display each battle unit on the other's tile when the swap finishes`.
- `SwapBattleUnits should hop the leader over the member while they swap`: on every frame, the leader is drawn over the
  member and the member stays on its row; the leader peaks `SWAP_HOP_HEIGHT` above its row.
- `SwapBattleUnits should not display the battle units on their tiles while they swap`.
- `SwapBattleUnits should last the swap duration`: it completes after 15 test frames of 10ms, give or take one.
- `SwapBattleUnits should scroll to the member's tile before the battle units move when it is at the viewport edge`.

`BattlefieldPresenterTest`:

- `MoveUnit should swap the leader over the member whichever battle unit moved first`: `FakeBattlefieldView` records
  the swaps, with the leader and the member.

## Docs

- [Exploration](../architecture/exploration.md#swapping-with-a-party-member), step 4: the leader hops over the member.
- [Party member swap](party-member-swap.md): the animation row links to this plan.
