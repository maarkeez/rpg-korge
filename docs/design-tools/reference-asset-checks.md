# Asset checks: palette checker and contact sheet

Test tooling that enforces the Famicube palette on game art and renders every asset on one grid. See [how to check palette compliance](how-to-check-palette-compliance.md).

## Palette file

`resources/famicube-palette.png` — 64×1 RGBA PNG, one pixel per Famicube color. The checker loads it the same way production loads art (via `resourcesVfs`), so what the test sees is what the game sees.

## Checked assets

`GAME_ART_ASSET_PATHS` in `FamicubePaletteChecker.kt` lists the game-art PNGs that must be fully on-palette (22 files):

| Folder | Files |
| --- | --- |
| `resources/ability/` | `ability_selection`, `bee`, `heal`, `mushroom`, `poisoned_sword`, `skull`, `sword`, `teleport` |
| `resources/battlefield/` | `tile_selection_1` … `tile_selection_4` |
| `resources/effect/` | `venom-damage`, `venom-on-death` |
| `resources/terrain/transitions/` | `sand_to_grass`, `sand_to_lava`, `sand_to_void`, `sand_to_water` |
| `resources/unit/` | `bee`, `bee_portrait`, `knight`, `rat` |

Deliberately excluded from the palette check:

| Path | Reason |
| --- | --- |
| `resources/korge.png` | Korge engine logo, not game art |
| `resources/unit/knight_portrait.png`, `resources/unit/rat_portrait.png` | Full-color illustration art, not Famicube art |
| `resources/unit/goblins/` | Reference samples, not used by the game |

The contact sheet (`CONTACT_SHEET_ASSET_PATHS`) includes the checked set plus the excluded portraits and the goblin sample, so everything ships on one grid; only `korge.png` is left off the sheet.

## Code

### `FamicubePaletteChecker.kt` — `src/jvmTest/kotlin/com/mkz/rpg/assets/`

| Member | Signature | Description |
| --- | --- | --- |
| `GAME_ART_ASSET_PATHS` | `val: List<String>` | Curated game-art asset paths checked against the palette |
| `CONTACT_SHEET_ASSET_PATHS` | `val: List<String>` | Asset paths drawn on the contact sheet |
| `FAMICUBE_PALETTE_PATH` | `const: String` | `"famicube-palette.png"` |
| `CONTACT_SHEET_PATH` | `const: String` | `"build/contact-sheet.png"` |
| `PaletteViolation` | `data class PaletteViolation(color: Int, count: Int)` | One off-palette color and how many pixels use it (`color` is the 0xRRGGBB value) |
| `loadFamicubePalette` | `suspend fun(): Set<Int>` | Loads the palette PNG and returns its opaque colors as a set of 0xRRGGBB ints |
| `Bitmap32.paletteColors` | `fun(): Set<Int>` | Distinct opaque colors of a bitmap |
| `checkAsset` | `suspend fun(path: String, palette: Set<Int>): List<PaletteViolation>` | Loads an asset from `resourcesVfs` and returns its off-palette colors |
| `Bitmap32.checkPalette` | `fun(palette: Set<Int>): List<PaletteViolation>` | Off-palette colors of a bitmap, sorted by count descending then color |
| `describeViolations` | `fun(path: String, violations: List<PaletteViolation>): String` | Formats `path: #RRGGBB x count, ...` for failure messages |
| `colorHex` | `fun(color: Int): String` | `0xFF00FF` → `"#FF00FF"` |

### `PaletteCheckerTest` — 4 tests

| Test | Covers |
| --- | --- |
| `should load the Famicube palette with 64 distinct colors` | Palette file integrity (64 colors) |
| `should report off-palette pixels with color and count when a bitmap deviates from the palette` | Violation detection, counts, and message format on a synthetic bitmap |
| `should ignore transparent pixels when checking a bitmap against the palette` | Transparency is not a violation |
| `should pass when every game art asset uses only Famicube palette colors` | Every checked asset is fully on-palette; failure lists file + colors + counts |

### `AssetContactSheetTest` — 1 test, `@Tag("contact-sheet")`

| Test | Covers |
| --- | --- |
| `should draw every asset into a single contact sheet image` | Every sheet asset decodes and is drawn into `build/contact-sheet.png` (512px wide, shelf layout, dark background) |

### Gradle

```sh
./gradlew generateContactSheet   # runs AssetContactSheetTest, writes build/contact-sheet.png
```

Registered in `build.gradle.kts` next to `updateSnapshots`; both reuse the `jvmTest` classes and classpath.

## Run tests

```sh
./gradlew jvmTest --tests "com.mkz.rpg.assets.*"
```
