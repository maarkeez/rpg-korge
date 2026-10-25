# Assets

The battle scene images live in the [`resources`](../../resources) folder. They are loaded by [`BattleSceneResourcesLoader`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/BattleSceneResourcesLoader.kt).

## How resources are bundled

Contributors add, edit and remove the individual files under `resources`. The game never reads them one by one: the build bundles them into a single file, so a new game loads every resource with one request instead of one per file (each request is a round trip in the browser).

- The `resourcesBundle` Gradle task ([`ResourcesBundleTask`](../../buildSrc/src/main/kotlin/resourcesbundle/ResourcesBundleTask.kt)) zips every file under `resources`, except hidden ones, into `build/generated/resourcesBundle/resources.zip`. The entries are stored uncompressed. The bundle is never committed.
- Only the bundle is packaged, on every target (e.g.: the JVM game and the web game on GitHub Pages). The individual files and the `$catalog.json` files KorGE lists them with are left out, so a file read one by one fails instead of being slow.
- [`ResourcesBundle`](../../src/commonMain/kotlin/com/mkz/rpg/shared/adapters/resources/ResourcesBundle.kt) reads the bundle once per run. `ResourcesBundle.root` reads its files as `resourcesVfs` would, e.g.: `ResourcesBundle.root["unit/knight.toml"]`. The game starts downloading it as soon as it starts, while the menu is displayed.
- Only `ResourcesBundle` may use `resourcesVfs`. This is enforced by [`ResourcesBundleArchitectureTest`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/architecture/ResourcesBundleArchitectureTest.kt).

## How assets are loaded

- `BattleScene` creates a single `BattleSceneResourcesLoader` and calls `load()` once, when the scene is initialised.
- `load()` walks every folder of the resources bundle and decodes every `.png`, `.jpg` and `.jpeg` file just once. Hidden files and non-image files (e.g.: terrain `.toml` definitions) are ignored.
- Views receive the loader through their constructor and request the images they need when displaying something:
  - `bitmap(path)` returns an image or fails when it does not exist.
  - `bitmapOrNull(path)` returns an image or `null` when it does not exist.
  - `bitmaps(directory)` returns the images directly inside a folder, keyed by file name.
- Paths are relative to `resources`, e.g.: `unit/knight.png`.
- Views MUST NOT read resources themselves. This is enforced by [`PresentationResourcesArchitectureTest`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/architecture/PresentationResourcesArchitectureTest.kt).

## Sources

Every image has a layered Aseprite source under [`art`](../../art), at the same path: `art/unit/knight.aseprite` is the
source of `resources/unit/knight.png`. Sources are kept out of `resources`, so they are not bundled.

- They are drawn with the [`pixel` tool](../../tools/pixel-art/README.md), which coding agents use through the
  [`pixel-art` skill](../../.claude/skills/pixel-art/SKILL.md). They can also be opened in Aseprite, as long as they
  keep a single frame, without layer groups, blend modes, tags nor slices.
- Every time the tool saves a source, it exports what its visible layers display to its PNG, so the PNG follows its
  source. A PNG whose pixels did not change is left as it is.
- Sources are indexed with the colours of the palette below, so they can only hold valid pixels.
- `famicube-palette.png` has no source: it is the palette itself.

## Adding an asset

Draw its source under `art` with the `pixel` tool, or drop the image in `resources` and import it
(`tools/pixel-art/pixel import resources/<path>.png art/<path>.aseprite`), following the naming conventions below. Neither the bundle, the loader nor the views need to change. A new unit, building, ability or effect also needs its data file, see [Game data](game-data.md).

| Asset                            | Path                                  | Example                          |
|----------------------------------|---------------------------------------|----------------------------------|
| Battle unit on the battlefield   | `unit/<unit id>.png`                  | `unit/knight.png`                |
| Battle unit portrait             | `unit/<unit id>_portrait.png`         | `unit/knight_portrait.png`       |
| Npc on the battlefield           | `npc/<npc id>.png`                    | `npc/the-guy.png`                |
| Npc portrait (in its dialog)     | `npc/<npc id>_portrait.png`           | `npc/the-guy_portrait.png`       |
| Recruitable npc (as its unit)    | `unit/<unit id>.png` and `unit/<unit id>_portrait.png` | `unit/viking.png`  |
| Building on the battlefield      | `building/<building id>.png`, 16 pixels per tile | `building/fortress.png` |
| Ability icon                     | `ability/<ability id>.png`            | `ability/poisoned-sword.png`     |
| Effect icon                      | `effect/<effect id>.png`              | `effect/venom-damage.png`        |
| Lootable item icon               | `lootableItem/<lootable item id>.png` | `lootableItem/health-potion.png` |
| Chest (several items on a tile, and the bag) | `battlefield/chest.png`   | `battlefield/chest.png`          |
| Terrain transition strip         | `terrain/transitions/<from>_to_<to>.png` | `terrain/transitions/sand_to_lava.png` |

- Units, npcs, buildings and abilities without an image are not displayed.
- A battlefield tile is 32px. Battle units and npcs are 16x16 or 32x32 (the [humanoid characters](../design/humanoid-characters.md)),
  both scaled to fill their tile: 16x16 at 2x, 32x32 at 1x. The size is read from the image, so replacing a 16x16 image
  by a 32x32 one is enough. Terrain and building pieces are drawn at 2x, loot at 1x.
- A building image spans its tiles: it is cut into one 16x16 piece per tile, drawn right above the terrain of that tile.
- Effects without an image fail when displayed.

## Colour palette

- Assets MUST only use the 64 colours of the FamiCube palette, defined by [`resources/famicube-palette.png`](../../resources/famicube-palette.png).
- Pixels MUST be fully transparent or fully opaque. Semi-transparent pixels are not allowed.
- Both rules are enforced for every image under `resources` by [`AssetPaletteArchitectureTest`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/architecture/AssetPaletteArchitectureTest.kt). When an asset fails, the test lists each off-palette colour with its closest palette colour.

## Outline

- Battle units, npcs, their portraits, lootable items, buildings and the chest MUST be outlined with `#151515`, the knight's outline. The outline is their silhouette: the opaque pixels next to a transparent pixel or to the edge of the image, diagonals included.
- Only the dark silhouette pixels (luminance below 60) must be `#151515`. Light or saturated trims on the silhouette are allowed, e.g.: the goat's pink.
- The rule is enforced by [`AssetOutlineArchitectureTest`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/architecture/AssetOutlineArchitectureTest.kt). The rat portrait, drawn without a single outline colour, is excluded.
