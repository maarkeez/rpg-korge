# Plan: A camera trailing the leader, as in A Link to the Past

> **Status:** done.
>
> **Summary:** While exploring, the camera blocks the party and jumps too far. Before each step of the leader at an edge
> of the viewport, the battlefield scrolls 6 tiles in 300ms, and the timeline waits for it, so the pad is ignored
> meanwhile. The battle units hop from tile to tile, and the followers step one after another, so a held arrow stutters.
> Instead, while exploring, the battle units glide from tile to tile, the whole party at once, and the camera trails the
> leader on every frame, outside the timeline: the leader moves freely in the 2x2 tiles at the center of the viewport,
> and pushes the camera along once it walks past them. A battle still centers the tile and waits for it, as today.

## Why it feels wrong today

- `followTile` runs before each step of the leader, and awaits a 300ms scroll (`SCROLL_DURATION`) inside the timeline
  step, so the party can not walk until it is done.
- Looking ahead, the camera does not move for 5 steps, then scrolls 6 tiles (288px) in 300ms.
- `moveBattleUnit` removes the battle unit from a tile and draws it on the next one, every 100ms, so nothing moves pixel
  by pixel.
- Each party step is its own timeline step, so a party of 4 walks a tile in 400ms, the leader waiting for its 3
  followers before walking again.

## Decisions

| Topic           | Decision                                                                                                                                                                                                                                                                    |
|-----------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Camera          | While exploring, the camera trails the leader on every frame (an updater), never in the timeline, so it never blocks the party.                                                                                                                                              |
| Deadzone        | The 2x2 tiles at the center of the viewport: the leader's top left corner stays within `3 * TILE_SIZE..4 * TILE_SIZE` (144..192px) of the viewport, on each axis. Inside it, the camera does not move. Past it, the camera keeps the leader on its edge. Clamped to the battlefield, so the leader reaches its borders. |
| Followed point  | The leader's pixel position: its gliding image while it moves, its tile while it stands. Walking east, the camera starts moving once the leader is on column 4, so it always sees 3 tiles ahead. Turning around takes one free step before the camera moves back.            |
| Locked          | While the leader walks past the deadzone, the camera moves with it, pixel for pixel, at its speed (48px per 250ms).                                                                                                                                                           |
| Catching up     | When the camera is more than one tile away from where it should be (the exploration starts or resumes, after a drag, a teleport), it eases there: each frame closes `1 - e^(-dt / CATCH_UP_TIME)` of the gap (`CATCH_UP_TIME` 80ms), and at least `CATCH_UP_MIN_SPEED` (384px/s, twice the party walking, so it reaches a walking leader), then locks once it is there (6 tiles in ~260ms). Not awaited: the party can walk meanwhile. |
| Gliding         | Every move glides: the battle unit's image slides along its path in the `movingBattleUnits` layer (as a swap does), linearly, `stepDuration` per tile, and lands on its destination tile. A teleport (a step to a tile that is not next to the previous one) still jumps, waiting `stepDuration`. |
| Party pace      | While exploring, the steps of one walk (the leader and its followers) glide at the same time, so a walk takes one step whatever the party size. The domain is unchanged: `WalkParty` still resolves each step in line order.                                                |
| Walking speed   | `EXPLORING_STEP_DURATION`: 100ms → 250ms per tile (4 tiles/s, close to Link's pace). `MOVE_STEP_DURATION` (150ms, battle), `WANDERING_STEP_DURATION` (75ms) and `SWAP_DURATION` (150ms) are unchanged.                                                                  |
| Drag            | Dragging the battlefield while exploring pauses the trailing, so the player can look around. The leader's next step resumes it, catching up.                                                                                                                                |
| Other scrolls   | While trailing, the camera belongs to the leader: a flash, a loot pick-up and the shop's buyer do not scroll (they are party members, next to the leader).                                                                                                                  |
| Battles         | Unchanged: when the exploration pauses (a CPU battle unit engages the party), the trailing stops, and each move, flash, loot pick-up and turn start centers its tile and waits for it, as today. Battle moves glide too.                                                    |

For example, the leader walks east along row 3 of a 32 columns battlefield, from column 3, not scrolled:

| The leader glides onto | Today (look ahead)                               | Trailing                                                       |
|------------------------|--------------------------------------------------|----------------------------------------------------------------|
| Column 4               | Does not scroll                                  | Does not scroll: it reaches the deadzone edge (192px)          |
| Column 5               | Does not scroll                                  | Scrolls 48px with it, during its glide (`x = -48`)             |
| Column 6               | Does not scroll                                  | Scrolls 48px with it (`x = -96`)                               |
| Column 7               | Waits for a 6 tiles scroll, then steps (`x = -288`) | Scrolls 48px with it (`x = -144`)                           |
| Column 6, back         | Does not scroll                                  | Does not scroll: it moves back across the deadzone (144..192px) |
| Column 5, back         | Does not scroll                                  | Scrolls 48px back with it (`x = -96`)                          |

## BattleSceneTimeline

- `playTogether(step)`: plays the step straight away, together with the step being played, and the next steps wait for
  both, so the timeline keeps playing until both are done. When the timeline is idle, it plays as `play`.
- The events of a walk arrive in the same frame, so a follower's step joins its leader's: the leader moved, its loot
  picked up (queued), the follower moved (joins the leader's step), its loot picked up (queued). The pick-ups play once
  the party has glided, in order.

## BattlefieldView

- `Following`: `LOOK_AHEAD` is replaced with `TRAIL`, the leader exploring: the camera trails the moving image, and then
  the destination tile. `NONE` and `CENTER` are unchanged.
- `trail(row, column)`: starts trailing the battle unit standing on the tile, catching up. `stopTrailing()`: stops it,
  leaving the scroll where it is.
- An updater on a `camera` view, added to the viewport after the tiles and the gliding battle units, so it reads where
  the leader's image is drawn on this frame, while trailing and not paused by a drag: computes the scroll that keeps the
  followed point inside the deadzone, clamped to `minScroll..0`, then sets it straight away (locked) or eases to it
  (catching up). It then moves the gliding battle units with the scroll and refreshes the tiles on the screen, which
  `movingBattleUnits` and `battlefieldGrid` did in their own updaters before.
- `moveBattleUnit`: glides one image along the whole path, then displays the battle unit on its destination tile.
  `CENTER` still awaits `followTile` before each step, so in a battle the image waits for the scroll.
- `swapBattleUnits`: trails the leader's gliding image, instead of `followTile` (only the exploring party swaps).
- `followTile` (flash, loot pick-up, shop's buyer, `CENTER`): does nothing while trailing.
- The look-ahead landing (`walkedFrom`) is removed from `followedScroll`.
- The drag pauses the trailing on its start. `moveBattleUnit` and `swapBattleUnits` with `TRAIL` resume it.

## BattlefieldPresenter

- `moveUnit`, while exploring: the leader plays with `TRAIL`. A follower plays with `NONE` through `timeline.playTogether`.
  Both at `EXPLORING_STEP_DURATION`.
- `ExplorationStarted` and `ExplorationResumed`: `timeline.play { battlefieldView.trail(leader's row, column) }`, read
  when the event arrives, so the camera catches up with the leader straight away (e.g.: after a battle). Nothing is
  trailed when the party is no longer exploring by then.
- `ExplorationPaused`: `timeline.play { battlefieldView.stopTrailing() }`, before the battle's steps.
- `wanderUnit`: unchanged (`NONE`, alongside the timeline), now gliding.

## Tests

`BattleSceneTimelineTest`:

- `PlayTogether should play the step straight away when another step is still playing`.
- `PlayTogether should play the next steps once both steps are done`.
- `PlayTogether should play the step as a step of the timeline when the timeline is idle`.

`BattlefieldViewTest`, on a 16x16 battlefield, not scrolled, unless told otherwise:

- `MoveBattleUnit should glide the battle unit image along its path, off the tiles, when it moves` and `should display
  the battle unit on its destination tile once it has glided along its path` replace the tile by tile tests.
- `MoveBattleUnit should glide each tile of the path for a step when the battle unit moves`.
- `MoveBattleUnit should jump to the destination when the battle unit teleports`.
- `MoveBattleUnit should not scroll the battlefield when the battle unit moves inside the deadzone trailing`: from (3, 3)
  to (3, 4): the scroll stays `(0.0, 0.0)`.
- `MoveBattleUnit should scroll the battlefield with the battle unit when it walks past the deadzone trailing`: from
  (3, 4) to (3, 5): on every frame, the image is on the deadzone edge (192px), and the scroll ends at `(-48.0, 0.0)`.
- `MoveBattleUnit should not wait for the battlefield to scroll trailing`: the step lasts `stepDuration`.
- `MoveBattleUnit should not scroll back until the battle unit crosses the deadzone when it turns around trailing`.
- `MoveBattleUnit should clamp the trailing scroll to the battlefield when the battle unit walks near its border`.
- `Trail should ease the battlefield to the battle unit when it is far from the deadzone`: to a battle unit on (10, 10),
  the scroll gets closer on every frame, and reaches `(-288.0, -288.0)`.
- `StopTrailing should leave the scroll where it is`.
- `DisplayBattlefield should pause trailing when the battlefield is dragged`, and `MoveBattleUnit should resume trailing
  when the battle unit moves after a drag trailing`: the battle unit ends inside the deadzone.
- `SwapBattleUnits should trail the leader while it hops over the member` replaces the look ahead swap test.
- `FlashBattleUnit should not scroll the battlefield trailing`.
- The `CENTER` tests are kept. The look-ahead tests are removed.

`BattlefieldPresenterTest` (`FakeBattlefieldView` records the trailed tiles and the stops):

- `MoveUnit should trail the leader of the party when it walks while exploring` (was `looking ahead`).
- `MoveUnit should glide a follower of the party together with the step being played when it walks while exploring`.
- `MoveUnit should wait for the step being played to move a battle unit in a battle`.
- `ExplorationStarted should trail the leader of the party`, `ExplorationResumed should trail the leader of the party`,
  `ExplorationPaused should stop trailing the leader of the party`.

End-to-end:

- `walkLeader` taps the arrow once and waits for the leader on its tile: clicking on every frame walked it further, as
  the leader is off the tiles while it glides, and a tap is kept until the scene is idle.
- `MoveKnightEndToEndTest`: `should glide the knight tile by tile when the human player moves it two tiles away` checks
  the knight glides past the tile it walks through, instead of standing on it.

## Docs

- [Exploration](../architecture/exploration.md#walking): the camera trails the leader on every frame, inside a 2x2
  tiles deadzone, without blocking the party. The party glides at once, 250ms per tile.
- [Presentation timeline](../architecture/presentation-timeline.md#moving-battle-units): the battle units glide, and
  `playTogether`.
- [Look ahead camera](look-ahead-camera.md): status superseded by this plan.
