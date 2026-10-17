# Plan: main menu and map builder

> **Status:** implemented.
>
> **Summary:** The game opens on a menu offering to play a new game or to build a map. The map builder lets the player
> design a rectangular battlefield on a mobile screen, painting its tiles with terrains, and copies it to the clipboard
> as a TOML file with the same format as [`resources/battlefield/battlefield.toml`](../../resources/battlefield/battlefield.toml),
> to paste it there.

## Goals

- A menu screen with two actions: **Play new game** (primary), which opens the battle, and **Build a map** (secondary),
  which opens the map builder.
- A map builder screen laid out like the battle screen, so it is usable on mobiles: a title at the top, then the
  battlefield, then the HUD, then the calls to action.
- The map builder can:
  - Set the battlefield size (rows and columns, from 1 to 128 each). A new map is 16 x 16 sand tiles.
  - Select a terrain (e.g.: sand, grass, lava, water, void) and tap tiles to paint them with it.
  - Export the map as TOML, copied to the clipboard. The battle reads it the same way once it is pasted into
    `resources/battlefield/battlefield.toml`.

## Decisions

| Topic                | Decision                                                                                                                                                                                                                                                           |
|----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Export               | The TOML is copied to the clipboard.                                                                                                                                                                                                                               |
| Forbidden neighbours | Only the transitions in `resources/terrain/transitions` are allowed (today: sand to grass, lava, void and water). A tap that would leave two terrains next to each other without a transition (e.g.: lava next to water) is rejected: the tile does not change, and the subtitle displays why. |
| Temporary messages   | The subtitle under the title displays the map size. Messages (a rejected tap, the map copied) replace it for a while with a pop animation, like the battle round and player turn, and then the size comes back.                                                   |
| Size                 | From 1 to 128 rows and columns. Resizing keeps the painted tiles that still fit and fills the new ones with sand.                                                                                                                                                 |
| Painting             | One tile per tap. Dragging keeps scrolling the battlefield.                                                                                                                                                                                                       |
| Back                 | The **Back** call to action asks for confirmation in the same call to action area, with two buttons: one to leave (the map is lost) and one to stay.                                                                                                              |
| Using a map          | Out of scope: the exported TOML is pasted into `resources/battlefield/battlefield.toml` by hand. Choosing a map from the menu is not planned.                                                                                                                      |
| Titles               | The menu has no title, as the game has no name yet. The map builder title is "Map Builder".                                                                                                                                                                       |
| Battle back button   | Out of scope: the battle has no way back to the menu.                                                                                                                                                                                                              |

## Reusing the battlefield view

The map builder displays its battlefield with the same [`BattlefieldView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/BattlefieldView.kt)
as the battle, driven by a new presenter, instead of a view of its own.

Why:

- The view already draws each terrain and the transition tiles between terrains, picked from the neighbouring tiles.
  It is the code a second view would most likely drift on.
- It already scrolls a battlefield larger than its 8 x 8 tiles viewport by dragging, and reports the tapped tiles
  through `BattlefieldView.Delegate.tileSelected`, which the map builder presenter implements to paint.
- It displays a `Battlefield.Dto`, built by the battlefield domain. The map builder builds it with the same domain
  code, so a map displays, and is validated, the same way in the map builder and in the battle.

What it costs:

- The battle features (battle units, loot, flashes, tints, health previews) are not used by the map builder. They
  are harmless.
- The view loads its images with `BattleSceneResourcesLoader`, which also loads the units, effects and items images.
- `displayBattlefield` can only be called once: every call adds another grid and another drag handler. The map
  builder displays the battlefield again when it is resized, and repaints tiles on every tap. So:
  - `displayBattlefield` replaces the previous grid, keeping a single drag handler.
  - A new `updateTerrain(battlefield, positions)` changes the terrain images of the given tiles only: the painted
    tile and its four neighbours, whose transitions may change.

Extracting a shared "terrain grid" view is not worth it yet. It can be done if the map builder ends up needing none of
the battle features and the shared view gets in the way.

## Design

### Screens and navigation

- `startGame()` opens a new `MenuScene` with the two actions, instead of the `BattleScene`.
- `MenuScene` changes to `BattleScene` or to a new `MapBuilderScene`. `MapBuilderScene` changes back to `MenuScene`
  once the player confirms leaving.
- The end-to-end tests start from the menu, so they tap **Play new game** first.

### Battlefield subdomain

The map builder reuses the battlefield rules through `BattlefieldApi`:

| New use case                                           | What it does                                                                                                                                                                                                                                  |
|--------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Query `previewBattlefield(tiles)`                      | Builds the `Battlefield.Dto` of the terrain ids, with the transitions and the occupancy of the battle, without storing it. It throws `TileTerrainTransitionNotAllowed` when two terrains without a transition are next to each other.       |
| Query `exportBattlefield(tiles)`                       | Writes the terrain ids in the format of `resources/battlefield/battlefield.toml`, through a `BattlefieldEncoder` implemented next to `ResourcesBattlefieldLoader` (the header comment, then `tiles = [...]` with one aligned row per line). |

A round trip test checks that the exported TOML is read back by `ResourcesBattlefieldLoader` as the same tiles.

### Terrain subdomain

| New use case            | What it does                                                    |
|-------------------------|-----------------------------------------------------------------|
| Query `searchTerrains()` | All the terrains, in id order, to display them in the palette. |

### Map builder subdomain

A new `mapBuilder` subdomain, `com.mkz.rpg.mapBuilder`, holds the map being designed. It follows the usual package
structure, so it needs no exception in [Package structure](../architecture/package-structure.md).

- Domain: `MapBuilder` (named after its subdomain, as every aggregate root), with the rows, the columns, the terrain id
  of each tile and the selected terrain. It is created as 16 x 16 sand tiles, with sand selected.
- Commands, through `MapBuilderApi`:

| Command                      | Announces                                                                                                                         |
|------------------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| `createMap()`                | `MapCreated`, with the size and the tiles.                                                                                        |
| `resizeMap(rows, columns)`   | `MapResized`, with the new size and tiles. A size out of 1 to 128 is ignored.                                                    |
| `selectTerrain(terrainId)`   | `TerrainSelected`.                                                                                                                |
| `paintTile(row, column)`     | `TilePainted` with the tile, or `TilePaintRejected` with the two terrains that can not be neighbours. Only the painted tile neighbours are checked, with the terrain `isTransitionAllowed` in either direction, as the battlefield does. |

- Query `exportMap()`: the TOML of the map, from `exportBattlefield`.

### Map builder screen

Laid out like the battle screen, within the 390 x 844 window:

1. **Title:** "Map Builder", and the subtitle with the size (e.g.: "16 x 16") or a temporary message.
2. **Battlefield:** the reused `BattlefieldView`.
3. **HUD:**
   - Two steppers, rows and columns, with buttons changing the size by -8, -1, +1 and +8, so a large map takes a few
     taps. The buttons that would leave 1 to 128 are disabled.
   - A terrain palette: one button per terrain, with its tile image, highlighting the selected one.
4. **Calls to action:** **Export** (primary) and **Back** (secondary). Back switches them to **Leave** and **Stay**.

`MapBuilderPresenter` subscribes to the map builder events and drives the views:

- `MapCreated` and `MapResized` display the battlefield from `previewBattlefield`, and the size in the subtitle.
- `TilePainted` updates the terrain of the painted tile and its neighbours.
- `TilePaintRejected` displays, for a while, why in the subtitle, e.g.: "Lava can not be next to water".
- **Export** copies `exportMap()` to the clipboard, through a `Clipboard` port (the game window clipboard in the game,
  a fake in the tests), and displays "Map copied to the clipboard" in the subtitle.

## Importing a map

A hamburger menu at the top right corner of the map builder lists **Import**. It replaces the map with the one in the
clipboard, e.g.: exported earlier, right away: the current map is lost.

| Topic          | Decision                                                                                                                                                 |
|----------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| Format         | The format of `resources/battlefield/battlefield.toml`, read by the battlefield `parseBattlefield` query with the same definition as the battlefield loader. |
| Invalid maps   | The whole import is rejected, keeping the map, and the subtitle displays why: the clipboard is empty or can not be read, has no map, the size is out of 1 to 128, a terrain does not exist, or two neighbours can not be next to each other. |
| Selected terrain | Kept.                                                                                                                                                  |
| Menu           | Choosing an option, the menu button again, or anywhere else on the screen closes it. A tap out of the menu only closes it, e.g.: it does not paint.     |
| Scope          | Only the map builder has the menu.                                                                                                                      |

Browsers ask for permission to read the clipboard (e.g.: Safari on iOS shows a "Paste" bubble to tap). When it is
denied, the import tells the clipboard can not be read.

## Risks

### Large maps

The battlefield view first created one button per tile, and every frame went through all of them, also the ones out of
the viewport. Measured on the JVM, without rendering:

| Size      | Tiles  | Displaying the map | Each frame    | Painting a tile |
|-----------|--------|--------------------|---------------|-----------------|
| 16 x 16   | 256    | 58 ms              | 6 ms          | < 1 ms          |
| 32 x 32   | 1024   | 46 ms              | 12.5 ms       | 1.4 ms          |
| 64 x 64   | 4096   | 148 ms             | 24 ms         | 3 ms            |
| 128 x 128 | 16384  | 1.8 s              | 135 to 600 ms | 12 ms           |

Hiding the tiles out of the viewport only brought a 128 x 128 frame down to 25 ms, as they were still gone through. So
the view now only creates the tiles in the viewport and one more around it, and creates or drops tiles while it is
scrolled. A tile holding more than its terrain (e.g.: a battle unit, loot, a selection) is kept off the screen until it
comes back, and the other tiles are created again from the battlefield. The size of the battlefield no longer matters:

| Size      | Displaying the map | Each frame | Painting a tile |
|-----------|--------------------|------------|-----------------|
| 16 x 16   | 43 ms              | 4.8 ms     | < 1 ms          |
| 64 x 64   | 7 ms               | 3.5 ms     | 4 ms            |
| 128 x 128 | 6 ms               | 1.3 ms     | 10 ms           |
| 256 x 256 | 4.5 ms             | 1.3 ms     | -               |

Painting a tile builds the whole map again with the battlefield rules (27 ms for 256 x 256), which is the limit if
maps grow further.

### Clipboard on the browser

Browsers only let a page write to the clipboard after a user action (a tap) and on secure pages (HTTPS or localhost).
Export is a tap, so it is expected to work. KorGE logs the error in the browser console when the write is refused.

## Implementation steps

Each step is tested (unit, presenter and view tests, and end-to-end tests on JVM and JS) before the next one.

1. Menu scene and navigation, and the end-to-end tests starting from the menu.
2. Battlefield `previewBattlefield` and `exportBattlefield`, with the TOML round trip test, and terrain
   `searchTerrains`.
3. `mapBuilder` subdomain: `MapDraft`, its commands, events and `exportMap`.
4. `BattlefieldView`: `displayBattlefield` replacing the previous grid, and `updateTerrain`.
5. Map builder screen: title and subtitle messages, HUD (steppers and palette), calls to action with the leave
   confirmation, presenter and clipboard.
6. End-to-end tests (menu to battle, menu to map builder, paint, reject, export, leave), snapshots of both new screens,
   the large map measurement, and the documentation of the map builder.
