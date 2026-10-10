# Asset checks: palette, spec, transitions, registry and sheets

Test tooling that validates game art and renders assets for review. Used by the asset redesign plan ([`docs/design/asset-redesign-plan.md`](../design/asset-redesign-plan.md)). See [how to check palette compliance](how-to-check-palette-compliance.md).

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

## Spec and transition checks (M0)

### `AssetSpecTest` — 3 tests

Checks every `GAME_ART_ASSET_PATHS` entry against the family table in §6.1 of the plan.

| Test | Covers |
| --- | --- |
| `should decode every game art asset when the asset file exists` | File exists and is a decodable PNG |
| `should match the family size when every game art asset is listed` | Exact size per family: `terrain/transitions/` 256×16, `unit/*_portrait` 32×32, `unit/`, `ability/`, `battlefield/`, `effect/` 16×16. A folder with no family fails, so a new family must be added to the plan first |
| `should use only fully opaque or fully transparent pixels when every game art asset is checked` | Alpha is 0 or 255 only |

`expectedArtSize(path)` in `AssetTestSupport.kt` holds the family table.

### `TransitionStripTest` — 5 tests

Checks everything in `resources/terrain/transitions/`. Every file there is a gameplay rule (see plan §11), so stray files are reported.

| Test | Covers |
| --- | --- |
| `should name every transition file as from_to_to when the transitions folder is listed` | Name is `<from>_to_<to>.png`; any other file (draft, backup, variant) fails |
| `should have a terrain definition for both sides when a transition file is listed` | `terrain/<from>.toml` and `terrain/<to>.toml` exist |
| `should be 256 by 16 pixels when every transition strip is listed` | 16 tiles of 16×16 in Wang order |
| `should be fully opaque at tile 0 and tile 15 when every transition strip is checked` | Base tiles are opaque |
| `should have an identical tile 0 in every sand strip when the sand base tile is compared` | All `sand_to_*` strips share one sand base tile. Sand is the `from` terrain of every strip, and `BattlefieldView` takes it from whichever strip loads last |

### `SpriteRegistryCoverageTest` — 2 tests

| Test | Covers |
| --- | --- |
| `should find a file for every registered id when the registry id lists are checked` | Every unit, portrait, ability, effect and highlight id in `SpriteRegistry` resolves to a file. A missing file renders the magenta placeholder silently |
| `should keep frame 0 equal to the unit sprite and width a multiple of 16 when a unit animation strip exists` | For `unit/<id>_idle.png` and `unit/<id>_walk.png` (M5), when present: height 16, width a multiple of 16, frame 0 equal to `unit/<id>.png`. No strips exist yet, so this passes vacuously until M5 |

Production change for this check: `SpriteRegistry`'s id lists and path builders are now `internal` (in its companion object), and `load()` uses the same path builders the tests use. No behaviour changed.

### `ReadabilitySheetTest` — 1 test, `@Tag("contact-sheet")`

| Test | Covers |
| --- | --- |
| `should draw every unit and highlight over every base terrain tile when the readability sheet is written` | Writes `build/readability-sheet.png`: one row per base terrain tile (tile 0 and tile 15 of each transition strip, 8 rows), columns = knight, rat, bee, `tile_selection_4`, each at ×3. Left panel is color, right panel is the same sheet in grayscale to check value contrast |

### `ReservedUiColorReportTest` — 1 test (informational)

This test never fails on color. It writes `build/reports/asset-reserved-color-report.txt`:

- **Dominant colour per asset (baseline):** the most common opaque color of each asset, plus the base tiles of each transition strip. Use it as the before/after record for the redesign.
- **Reserved UI colours above 40%:** assets or base tiles where a color from `UiPalette`'s reserved hues (plan §6.3) covers more than 40 % of opaque pixels.

Gotcha: in this korlibs version `RGBA.rgb` is BGR-ordered (`0xBBGGRR`). The report builds the key from `r`, `g`, `b`. `PaletteCheckerTest` also uses `rgb`, which is consistent with itself, but `colorHex` then prints violation colors in reversed byte order (for example `#283CE0` for `#E03C28`). Fix that before relying on palette failure messages.

## Gradle

```sh
./gradlew generateContactSheet   # runs AssetContactSheetTest and ReadabilitySheetTest, writes build/contact-sheet.png and build/readability-sheet.png
```

Registered in `build.gradle.kts` next to `updateSnapshots`; both reuse the `jvmTest` classes and classpath.

## Run tests

```sh
./gradlew jvmTest --tests "com.mkz.rpg.assets.*"
```

The reserved-color report is written by `ReservedUiColorReportTest` in the same run.
