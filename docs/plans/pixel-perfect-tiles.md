# Plan: Pixel perfect 64px tiles

> **Status:** done.
>
> **Summary:** Since the [princess](princess-npc.md), a tile is 32px and the battlefield shows 12x12 tiles: everything
> on it looks 2/3 of its old size, too small to read on a phone. A tile becomes 64px, the smallest size above 32 where
> the 32x32 art is drawn at a whole scale (48px would draw it at 1.5x). The battlefield keeps its 384x384 area on the
> screen, so it shows 6x6 tiles. So that every art pixel is drawn with the same number of screen pixels, the moving
> views are drawn at whole pixels, and the stage is scaled to the window by a whole number.

## Decisions

| Topic          | Decision                                                                                                                                                                                                                                                         |
|----------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Tile           | `TILE_SIZE` 32 → 64. A 48px tile, as before the princess, would draw the 32x32 art at 1.5x. 32x32 art is drawn at 2x, 16x16 art (terrain, building pieces, the selections, most units and npcs) at 4x, through the scales already derived from `TILE_SIZE`. |
| Viewport       | `VISIBLE_TILES` 12 → 6: the viewport stays 384x384, so the layout of the battle scene is unchanged. Characters are about 1/3 bigger than with the old 48px tiles.                                                                                               |
| How to scale   | `TILE_SIZE` changes, and everything derived from it follows: the image scales (`tileFillingScale`, the terrain), the camera's deadzone (still the 2x2 tiles at the centre) and catch-up speed, the dim, the health preview. The view is not scaled as a whole. |
| Loot           | `LOOT_SCALE` 1 → 2: 32px in a 64px tile, half of it as today. A picked up item still grows by half while it rises (`PICK_UP_SCALE` 1.5 → 3).                                                                                                                    |
| Whole pixels   | The views moving on the battlefield are drawn at whole pixels: the scroll (trailing, catching up, dragging, centering a tile), the gliding battle units, the swap hop and the rising loot. Rounded on every frame after they moved, by the camera's updater.      |
| Camera         | The trailed scroll is rounded, and the catch-up moves by whole pixels (at least one per frame), so the camera reaches the leader at a whole pixel instead of being rounded back on every frame.                                                                 |
| Swap hop       | `SWAP_HOP_HEIGHT` is a whole number of pixels (`TILE_SIZE / 3`, 21px), so the hop lands back where it started.                                                                                                                                                  |
| Stage          | A `WholePixelScale` scale mode: the stage is scaled by the largest whole number that fits the window, in its device pixels (a 2x screen shows the 390x844 stage at 2x in a 390x844 window), centred, with the game background around it. When even 1x does not fit, it is scaled down to fit, as before. On the JVM and the web. |
| Enemy sight    | `IsInSight.VIEW_TILES` 12 → 6, as it follows the camera: enemies wake when they may step into view.                                                                                                                                                             |
| Map builder    | It shares the `BattlefieldView`, so it shows 6x6 tiles too.                                                                                                                                                                                                      |
| Art            | Nothing is redrawn: the 16x16 art (4 screen pixels per art pixel) and the 32x32 art (2) mix until the cast and the terrain are redrawn.                                                                                                                         |
| Portraits      | Unchanged: the dialogs and the HUD draw them at their own scale.                                                                                                                                                                                                 |

## screen

### Adapters

- `BattlefieldView`:
  - `TILE_SIZE = 64`, `VISIBLE_TILES = 6`, `LOOT_SCALE = 2.0`, `PICK_UP_SCALE = 3.0`, `SWAP_HOP_HEIGHT = TILE_SIZE / 3`
    (whole pixels).
  - The camera's updater, after trailing the leader, rounds the scroll, the gliding battle units, the hopping image and
    the rising loot to whole pixels.
  - `trailLeader`: the target scroll is rounded; `catchUp` moves by a whole number of pixels, at least one.

## shared

### Adapters

- `WholePixelScale`: a `ScaleMode` scaling the stage by the largest whole number fitting the window, or down to fit it
  below 1x.
- `Main.kt`: `Korge` takes it as its display mode, centred, clipping the borders.

## cpuWanderBrain

### Use cases

- `IsInSight`: `VIEW_TILES = 6`, still documented as following `BattlefieldView.VISIBLE_TILES`.

## Tests

- `WholePixelScaleTest`: whole scales for windows of 1x, 2x and 3x the stage and in between, the largest fitting one on
  each axis, and the fit below 1x.
- `BattlefieldViewTest`: the drag, scroll and camera cases written in pixels of the 32px tiles move to 64px ones; the
  scroll, the gliding battle units and the rising loot are at whole pixels on every frame; loot is drawn at 2x.
- `IsInSightTest` and `WanderWhileExploringTest`: the sight area is 6 tiles plus the margin; the cases at the edge of
  the 12 tiles move.
- `MoveKnightEndToEndTest` and the other end-to-end tests: re-run, as the battlefield shows fewer tiles.
- A game snapshot (see [How to take a game snapshot](../testing/how-to-take-a-game-snapshot.md)) of the exploring view
  with 6x6 tiles, and of the 32x32 knight next to the 16x16 bat in the first battle.
- Frame times while exploring, see [How to measure the frame times](../testing/how-to-measure-the-frame-times.md).
  Measured: p95 16.8ms, no frame over 50ms.

## Docs

- [Assets](../architecture/assets.md): a tile is 64px; 16x16 art at 4x, 32x32 art at 2x, loot at 2x.
- The [humanoid characters brief](../design/humanoid-characters.md): characters fill their 64px tile, 32x32 art at 2x.
- [The princess npc](princess-npc.md): a note that the tiles became 64px.

## Known limitations

- The picked up item grows from 2x to 3x while it fades out, so its pixels are not whole during those 400ms.
- 16x16 and 32x32 art mix pixel sizes, now 4 and 2 screen pixels, until the cast and the terrain are redrawn.
- A window smaller than the stage (e.g.: a phone browser held in landscape) still scales it down by a fraction.
