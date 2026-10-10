# Assets

The battle scene images live in the [`resources`](../../resources) folder. They are loaded by [`BattleSceneResourcesLoader`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/BattleSceneResourcesLoader.kt).

## How assets are loaded

- `BattleScene` creates a single `BattleSceneResourcesLoader` and calls `load()` once, when the scene is initialised.
- `load()` walks every folder under `resources` and decodes every `.png`, `.jpg` and `.jpeg` file just once. Hidden files and non-image files (e.g.: terrain `.toml` definitions) are ignored.
- Views receive the loader through their constructor and request the images they need when displaying something:
  - `bitmap(path)` returns an image or fails when it does not exist.
  - `bitmapOrNull(path)` returns an image or `null` when it does not exist.
  - `bitmaps(directory)` returns the images directly inside a folder, keyed by file name.
- Paths are relative to `resources`, e.g.: `unit/knight.png`.
- Views MUST NOT read resources themselves. This is enforced by [`PresentationResourcesArchitectureTest`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/architecture/PresentationResourcesArchitectureTest.kt).

## Adding an asset

Drop the image in `resources` following the naming conventions below. Neither the loader nor the views need to change.

| Asset                            | Path                                  | Example                          |
|----------------------------------|---------------------------------------|----------------------------------|
| Battle unit on the battlefield   | `unit/<unit id>.png`                  | `unit/knight.png`                |
| Battle unit portrait             | `unit/<unit id>_portrait.png`         | `unit/knight_portrait.png`       |
| Ability icon                     | `ability/<ability id>.png`            | `ability/poisoned-sword.png`     |
| Effect icon                      | `effect/<effect id>.png`              | `effect/venom-damage.png`        |
| Terrain transition strip         | `terrain/transitions/<from>_to_<to>.png` | `terrain/transitions/sand_to_lava.png` |

- Units and abilities without an image are not displayed.
- Effects without an image fail when displayed.

## Colour palette

- Assets MUST only use the 64 colours of the FamiCube palette, defined by [`resources/famicube-palette.png`](../../resources/famicube-palette.png).
- Pixels MUST be fully transparent or fully opaque. Semi-transparent pixels are not allowed.
- Both rules are enforced for every image under `resources` by [`AssetPaletteArchitectureTest`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/architecture/AssetPaletteArchitectureTest.kt). When an asset fails, the test lists each off-palette colour with its closest palette colour.
