# Plan: The princess replaces the mystic, the first 32x32 character

> **Status:** done.
>
> **Summary:** The mystic at the end of the first path becomes a princess, drawn with the
> [humanoid characters brief](../design/humanoid-characters.md). She is the first 32x32 character on the battlefield.
> So that 16x16 and 32x32 art live together without overflowing their tiles, a tile becomes 32px instead of 48px:
> 16x16 art is drawn at 2x and 32x32 art at 1x, both at whole scales. The battlefield keeps its 384x384 area on the
> screen, so it shows 12x12 tiles instead of 8x8, and the enemies' sight follows the camera.

## Decisions

| Topic          | Decision                                                                                                                                                                                                                                      |
|----------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Identity       | The npc `mystic` is renamed `princess`: `resources/npc/princess.toml`, name "Princess", same tile (row 38, column 11). The mystic's files are removed.                                                                                     |
| Line           | "You defeated the Goat Wizard! The realm thanks you, brave travellers. This was only the first of many paths: keep exploring this world."                                                                                               |
| Art            | The drafts `art-drafts/humanoid/princess.aseprite` and `princess_portrait.aseprite` move to `art/npc/`, so they export to `resources/npc/princess.png` and `princess_portrait.png`. The drafts' PNGs are removed.                           |
| Tile           | `TILE_SIZE` 48 → 32. Terrain, terrain transitions and building pieces (16x16 per tile) are drawn at 2x.                                                                                                                                      |
| Characters     | A battle unit or npc image fills its tile: its scale is `TILE_SIZE / its width`, 2x for 16x16 and 1x for 32x32. The size is read from the image, not configured, so swapping a 16x16 image for a 32x32 one moves a character to the new art. |
| No overflow    | Every image stays inside its tile, so the draw order, the clicks, the dim and the selection are unchanged.                                                                                                                                  |
| Pixel density  | During the transition, a 32x32 character has pixels half the size of the 16x16 ones: finer detail at about the same height (the princess is 26px tall, a 16x16 unit about 28px). Accepted: it fades as the cast is redrawn.                 |
| Viewport       | `VISIBLE_TILES` 8 → 12: the viewport stays 384x384 and shows more of the map. Everything on the battlefield looks 2/3 of its size today.                                                                                                     |
| Camera         | Derived from `TILE_SIZE` and `VISIBLE_TILES` already: the leader stays in the 2x2 centre (`DEADZONE_START`, `DEADZONE_END`), the trail and the catch-up speed scale with the tile.                                                           |
| Enemy sight    | `IsInSight.VIEW_TILES` 8 → 12, as it follows the camera: enemies start chasing from further away, and no sleeping enemy shows on screen.                                                                                                    |
| Loot           | `LOOT_SCALE` 2 → 1: 16px in a 32px tile, still smaller than the characters. A picked up item still grows by half while it rises (`PICK_UP_SCALE` 3 → 1.5).                                                                                  |
| Portraits      | Unchanged: npc and unit portraits are 32x32 already, drawn by the dialogs and the HUD at their own scale.                                                                                                                                     |
| Map builder    | It shares the `BattlefieldView`, so it shows 12x12 tiles too.                                                                                                                                                                                 |

## screen

### Adapters

- `BattlefieldView`:
  - `TILE_SIZE = 32`, `VISIBLE_TILES = 12`; the terrain scale becomes `TILE_SIZE / TILE_PIXEL_SIZE` instead of the
    `3.0` default of `addImage`.
  - `addImage`: a battle unit or npc is scaled to fill the tile (`TILE_SIZE / bitmap.width`); `displayBattleUnit`,
    `displayNpc` and the moving battle units (`movingBattleUnit`, whose `scale = 3.0` is hard coded) use it.
  - `LOOT_SCALE = 1.0`, `PICK_UP_SCALE = 1.5`.
  - The constants already derived from `TILE_SIZE` (the pick up rise, the swap hop, the deadzone, the catch-up speed,
    the health preview's size, the dim) need no change.

## cpuWanderBrain

### Use cases

- `IsInSight`: `VIEW_TILES = 12`, still documented as following `BattlefieldView.VISIBLE_TILES`.

## Resources

- `resources/npc/mystic.toml` → `resources/npc/princess.toml`, with the new name and line.
- `art/npc/mystic.aseprite`, `art/npc/mystic_portrait.aseprite` and their PNGs removed.
- `art/npc/princess.aseprite` and `art/npc/princess_portrait.aseprite` moved from `art-drafts/humanoid/`, exported.

## Tests

- `BattlefieldViewTest`: a 16x16 and a 32x32 npc both fill their tile (32px), a 16x16 battle unit too, the terrain
  fills its tile, loot is drawn at 1x. The existing tests derived from `TILE_SIZE` keep passing.
- `IsInSightTest` and `WanderWhileExploringTest`: the sight area is 12 tiles plus the margin; the cases at the edge of
  the old 8 tiles move.
- `MoveKnightEndToEndTest` and the camera tests: derived from `TILE_SIZE`, re-run.
- `AssetPaletteArchitectureTest` and `AssetOutlineArchitectureTest` now see the princess; she passes them already.
- Frame times, as 12x12 tiles (196 with the window's margin, from 100) are on the screen at once: measured while
  exploring, see [How to measure the frame times](../testing/how-to-measure-the-frame-times.md). Measured: p95 16.7ms,
  no frame over 50ms.
- A game snapshot of the lair (see [How to take a game snapshot](../testing/how-to-take-a-game-snapshot.md)): the
  princess next to 16x16 units, and the exploring view with 12x12 tiles.

## Docs

- [Assets](../architecture/assets.md): units and npcs are 16x16 or 32x32 and fill their 32px tile; loot is drawn at 1x.
- The [`pixel-art` skill](../../.claude/skills/pixel-art/SKILL.md): npcs may be 32x32, following the humanoid brief.
- The [humanoid characters brief](../design/humanoid-characters.md): the status says npcs are displayed at 32x32 and
  that a finished character moves from `art-drafts/` to `art/npc/`; the anchor section says a 32x32 character fills its
  tile (no overlap with its neighbours); the `mystic` row of the cast is removed, and the `princess` row says she is in
  the game.
- [First playable demo](first-playable-demo.md): a note that the mystic became the princess.

## Known limitations

- Tiles are 2/3 of their size today, so they are harder to tap on a phone: 32 points, under the 44 points usually
  advised for a touch target. Movement has the virtual pad; selecting an attack or ability target has only the tap.
- 16x16 and 32x32 art mix pixel sizes until the cast is redrawn.
- No character is taller than one tile: the brief's orc, meant to tower over humans, stands no taller than a 16x16
  unit at 2x.
