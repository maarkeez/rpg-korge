# Asset redesign plan

Plan for auditing, improving and replacing the game's art library. It is written for an implementation agent that has not seen the planning conversation. Read it in full before touching any asset.

Follow-up: the ALttP style pass and taller-than-tile units are planned in [`alttp-style-and-tall-units-plan.md`](alttp-style-and-tall-units-plan.md).

Status legend used throughout: **[verified]** = checked in the repository during planning; **[assumption]** = not verified, must be confirmed before acting on it; **[proposal]** = a recommendation that needs the owner's approval at a decision gate.

---

## 1. Purpose and scope

Give the game a coherent, polished pixel-art identity (SNES-era, *A Link to the Past* sensibility, *Super Mario Bros. 3* color energy, Famicube palette) while:

- keeping the battlefield readable on a portrait iPhone 14-class screen (390×844 pt);
- keeping every existing resource reference working unless a milestone explicitly changes it;
- keeping production small enough for one developer.

In scope: terrain tiles, unit sprites, unit portraits, ability icons, status/effect icons, battlefield highlight sprites, authored combat-feedback animations, and the tooling that validates them.

Out of scope: see [§14 Non-goals](#14-explicit-non-goals).

## 2. Source documents

| Document | What it contributes |
| --- | --- |
| `docs/design/design-brief.md` | Product intent: visual direction, portrait mobile, touch, feedback without combat log, accessibility, solo-dev constraints, non-goals |
| `docs/design/ui-conventions.md` | `PIXEL_SCALE = 3`, 16 px tile = 48 pt, `smoothing = false`, snapping, `UiPalette` tokens, "never color alone" table, 44 pt touch targets |
| `docs/design/ui-regression-checklist.md` | Manual UI checklist and the automated guards (touch targets, color tokens, 10-state snapshot matrix) |
| `docs/design-tools/reference-asset-checks.md`, `how-to-check-palette-compliance.md` | Existing palette checker and contact sheet |
| `docs/design-tools/reference-visual-snapshot-harness.md`, `how-to-update-visual-snapshots.md` | Golden-image snapshot tests at 390×844 |
| `docs/design-tools/how-to-run-a-battle-scenario.md`, `reference-battle-scenarios.md` | `./gradlew runJvm -Pscenario=<name>` and the scenario files |
| `docs/design/task-game-asset-redesign.md` | The brief this plan answers |

### 2.1 Requirements extracted from the design documents [verified]

| # | Topic | Requirement | Source |
| --- | --- | --- | --- |
| R1 | World art | Top-down fantasy pixel art inspired by ALttP; world must feel continuous, not like a separate battle screen | brief §1, §2, §3 |
| R2 | Characters | Proportions and apparent pixel size close to the SNES reference | brief §2 |
| R3 | Animation | Authored pixel-art animations/effects preferred over lighting VFX, bloom | brief §2, §5 |
| R4 | Tiles | Reusable tiles; battlefield is the primary interface and must stay dominant | brief §1, §7 |
| R5 | Ability/status | Visual hints for synergies; status propagation and on-death effects must be understandable from the battlefield | brief §5 |
| R6 | Feedback | Pixel-art animation, target highlighting and visible state changes are the primary feedback; no combat log | brief §5 |
| R7 | UI art | Hybrid: higher-resolution health bars, icons and text allowed where they improve readability | brief §2 |
| R8 | Palette | Famicube is authoritative for world, characters and effects; UI tokens are also Famicube colors (`UiPaletteTest`) | brief §2, ui-conventions |
| R9 | Scaling | Pixel-perfect integer scaling, no blur, consistent pixel sizes; `PIXEL_SCALE = 3` | brief §7, ui-conventions |
| R10 | Mobile | Portrait first, touch targets ≥ 44 pt, tiles 48 pt | brief §3, §4, ui-conventions |
| R11 | Solo dev | Reusable tiles, sprite parts, icons, effect animations; a small documented set of conventions | brief §7 |
| R12 | Accessibility | Never rely on color alone; consistent iconography | brief §4, §6, ui-conventions |
| R13 | Non-goals | No combat log, no crowded status icons, no excessive lighting, no effects that conceal tactical info, no exploration/combat mode transition, no RTS design | brief §8 |

### 2.2 Conflicts and how this plan resolves them

| Conflict | Resolution |
| --- | --- |
| "Apparent pixel size close to ALttP" vs `PIXEL_SCALE = 3` and 48 pt tiles. ALttP shows ~16 tiles across 256 px; this game shows 8 tiles across 384 pt, so each art pixel is roughly twice as large on screen as ALttP played full-width on the same phone. | Touch-target rule (R10, explicit and enforced by tests) wins. Keep `PIXEL_SCALE = 3` and 16 px tiles. Match ALttP in *proportions and density relative to the tile*, not in on-screen pixel size. Recorded as open question Q1. |
| ALttP characters are taller than one tile (roughly 16 px wide, up to ~24 px tall). The code assumes 16×16 unit sprites centered in the tile. | **[proposal]** Keep a 16×16 frame for this redesign (zero integration risk, tiles never obscured). Taller sprites are an explicit decision gate (Q2) with the required code changes listed in M4. |
| Text antialiasing creates off-palette pixels; palette is otherwise a hard constraint. | Already accepted in `ui-conventions.md`. Applies only to text rendered by the engine, never to bitmap assets. |

## 3. Existing asset directory map [verified]

Resources live in the top-level `resources/` folder and are read through KorGE's `resourcesVfs`. 28 tracked files in total (plus 10 golden snapshots).

```
resources/
  famicube-palette.png          64×1, one pixel per Famicube color (authoritative palette)
  korge.png                     512×512 engine logo (not game art)
  ability/                      8 × 16×16 ability icons (7 abilities + ability_selection)
  battlefield/                  4 × 16×16 tile highlight sprites (tile_selection_1..4)
  effect/                       2 × 16×16 status/effect icons (venom-damage, venom-on-death)
  terrain/
    grass|lava|sand|void|water.toml   terrain gameplay definitions (no images)
    transitions/                4 × 256×16 Wang strips (sand_to_grass|lava|void|water)
  unit/                         3 × 16×16 unit sprites + 3 × 32×32 portraits
    goblins/                    reference samples (goblin.png 32×32, card_sample.jpeg 1582×1420)
  scenarios/                    4 battle scenario JSON files
src/jvmTest/resources/snapshots/  10 golden PNGs (390×844)
```

## 4. Current asset pipeline [verified]

### 4.1 Loading

| Consumer | File | What it loads | How ids map to files |
| --- | --- | --- | --- |
| `SpriteRegistry` | `src/commonMain/kotlin/com/mkz/rpg/screen/SpriteRegistry.kt` | units, portraits, ability icons, effect icons, highlights, ability selection frame | Hard-coded id lists: `UNIT_IDS = knight, rat, bee` → `unit/<id>.png` and `unit/<id>_portrait.png`; `ABILITY_IDS = heal, sword, poisoned-sword, mushroom, skull, teleport, bee` → `ability/<id with '-'→'_'>.png`; `EFFECT_IDS = venom-damage, venom-on-death` → `effect/<id>.png`; `Highlight` enum → `battlefield/tile_selection_N.png`. Unknown ids return a 16×16 (32×32 for portraits) solid `UiPalette.placeholder` (#A328B3) bitmap and never throw. |
| `BattlefieldView.loadAssets()` | `src/commonMain/kotlin/com/mkz/rpg/screen/BattlefieldView.kt` | every PNG in `terrain/transitions/` | Lists the folder, crops each strip into `width / 16` tiles. **Base terrain bitmaps are not separate files:** tile 0 of each strip becomes the `from` terrain (sand) and the last tile becomes the `to` terrain. Sand's base tile is whichever strip is listed last. |
| `ResourcesTerrainLoader` | `src/commonMain/kotlin/com/mkz/rpg/terrain/adapters/resources/ResourcesTerrainLoader.kt` | `terrain/*.toml` | *(Superseded by ALttP plan L4a: allowed adjacency now comes from `transitionsTo` in `terrain/<id>.toml`; transition PNGs are art only.)* Originally: `<from>_to_<to>.png` file names defined which terrain transitions were allowed. |
| `BattleScenarioLoader` | `src/commonMain/kotlin/com/mkz/rpg/battlesetup/adapters/serialization/BattleScenarioLoader.kt` | `scenarios/*.json` | Not art. |

There is no texture atlas, no sprite-sheet metadata file, no animation data, no font asset, and no asset manifest. **[verified]**

### 4.2 Terrain transitions [verified]

`Battlefield.kt` computes a 4-bit edge Wang index per tile: N = 1, E = 2, S = 4, W = 8 (bit set when that neighbour is the target terrain). Strips therefore hold 16 tiles in index order 0..15. Only edge neighbours are considered (no corner tiles). Occupiability next to non-occupiable terrain (void) uses the same index: indices `{0, 1, 2, 4, 5, 8, 10}` remain occupiable (`OCCUPIABLE_TERRAIN_TRANSITION_WANG_INDICES`). Transitions only exist from sand; grass↔water, grass↔lava etc. have no art and therefore no allowed direct adjacency.

### 4.3 Rendering

- All pixel art is drawn with `scale = PIXEL_SCALE (3)` and `smoothing = false` (terrain, units, highlights, ability icons, portraits, walkers). **[verified]**
- Units and terrain are added to each tile `UIButton` with `centerOn(button)` (`BattlefieldView.addImage`). A sprite taller or wider than 16 px would be centered, not foot-anchored. **[verified]**
- `EffectView` draws effect icons at `scale = 1.0` into a 16×16 pt box, and `StatusListView` draws them at 16×16 pt (`ICON_SIZE = 16`). Status icons are therefore drawn at one-third of the world pixel density. **[verified]** Whether this is intentional (hi-res UI) or an inconsistency is open question Q5.
- Scroll offsets snap to multiples of `PIXEL_SCALE` (`snapToArtPixel`). **[verified]**

### 4.4 Animation and combat feedback [verified]

- Units have exactly one frame. Movement is a sliding "walker" container (`showWalker`, `WalkPath.kt`); no walk cycle, no idle, no attack pose.
- Combat feedback is drawn procedurally from `SolidRect` art pixels in `FxViews.kt`: hit flash (white silhouette, 132 ms), damage/heal numbers (`PixelGlyphs` 3×5 font, 480 ms), defeat "poof" ring (3 frames, 300 ms), status "pop" plus (240 ms), spread "spark" (2×2). Beats and durations live in `FeedbackBeat.kt`, `FeedbackTiming.kt` (`Instant` for tests, `Standard` for play).
- Ability-specific animations (sword slash, heal, poison, teleport, bee) do not exist.

### 4.5 UI

UI chrome (panels, bars, buttons, brackets, reticles, cooldown numbers, lock glyph) is drawn in code with `UiPalette` tokens and KorGE's default UI text. `PixelGlyphs.kt` is the only pixel font. No bitmap UI frames exist.

### 4.6 Existing validation [verified]

| Guard | Location | Run |
| --- | --- | --- |
| Palette compliance for 22 curated assets | `src/jvmTest/kotlin/com/mkz/rpg/assets/FamicubePaletteChecker.kt` (`GAME_ART_ASSET_PATHS`), `PaletteCheckerTest` | `./gradlew jvmTest --tests "com.mkz.rpg.assets.*"` |
| Contact sheet of all assets | `AssetContactSheetTest` | `./gradlew generateContactSheet` → `build/contact-sheet.png` |
| 10-state golden snapshots at 390×844 | `BattleSceneSnapshotTest` | `./gradlew jvmTest`; accept with `./gradlew updateSnapshots`; diffs in `build/reports/snapshots/` |
| UI tokens on palette | `UiPaletteTest`, `UiColorTokenAuditTest` | `./gradlew jvmTest` |
| Touch targets | `BattleUiScriptTest.TouchTargets` | `./gradlew jvmTest` |
| Deterministic scenarios | `resources/scenarios/*.json` | `./gradlew runJvm -Pscenario=ui-showcase` (also `terrain-mix`, `chain-showcase`, `default`) |

Missing: dimension/format rules per family, registry coverage (an id silently falling back to the placeholder), transition-strip consistency, readability previews.

### 4.7 Replacement compatibility summary

| Change | Code change needed? |
| --- | --- |
| Redraw any existing PNG at the same size and path | No. Goldens will change (expected). |
| Redraw a transition strip keeping 256×16, 16 tiles, Wang order | No. |
| Add a new transition `X_to_Y.png` | **Yes, gameplay changes**: new allowed adjacency. Needs scenario/terrain review and owner approval. |
| Unit sprite larger than 16×16 | Yes: foot anchoring in `BattlefieldView` (`addImage`, `showWalker`), `UnitOverlayView`/selection brackets positioning, placeholder size. |
| Animated units (multiple frames) | Yes: strip convention in `SpriteRegistry`, frame playback in `BattlefieldView`, frozen frame under `FeedbackTiming.Instant`. |
| Authored FX bitmaps replacing `FxViews` procedural effects | Yes: `FxViews`/`SpriteRegistry`. Durations stay in `FxViews`/`FeedbackTiming`. |
| New status icon ids (e.g. `lava-damage`) | Yes: add to `EFFECT_IDS`. |
| Base terrain variants (multiple grass tiles) | Yes: `BattlefieldView` picks a variant deterministically. |

Logical-vs-physical assets: `SpriteRegistry` already is the logical layer (ids → bitmaps, placeholder fallback). **[proposal]** Do not add a new asset pipeline; extend `SpriteRegistry` only where a milestone needs it.

## 5. Asset inventory [verified]

Produced during planning with a small dependency-free Python PNG decoder (zlib + struct, run with `python3 -I`, not committed) plus `sips` for the JPEG. Columns: color type (3 = indexed, 6 = RGBA), share of fully opaque pixels, pixels with partial alpha, distinct visible colors, pixels whose RGB is not in `famicube-palette.png`. To reproduce in-repo, use the M0 tests instead of the script.

| Path | Size | Type | Opaque % | Partial α | Colors | Off-palette px | Referenced by | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `ability/bee.png` | 16×16 | RGBA | 100 | 0 | 5 | 0 | `SpriteRegistry` | full-square icon |
| `ability/heal.png` | 16×16 | RGBA | 100 | 0 | 5 | 0 | `SpriteRegistry` | |
| `ability/mushroom.png` | 16×16 | RGBA | 100 | 0 | 4 | 0 | `SpriteRegistry` | |
| `ability/poisoned_sword.png` | 16×16 | RGBA | 100 | 0 | 8 | 0 | `SpriteRegistry` | |
| `ability/skull.png` | 16×16 | RGBA | 100 | 0 | 4 | 0 | `SpriteRegistry` | |
| `ability/sword.png` | 16×16 | RGBA | 100 | 0 | 5 | 0 | `SpriteRegistry` | |
| `ability/teleport.png` | 16×16 | RGBA | 100 | 0 | 4 | 0 | `SpriteRegistry` | |
| `ability/ability_selection.png` | 16×16 | RGBA | 25 | 0 | 1 | 0 | loaded by `SpriteRegistry`; `abilitySelection()` has no caller | `AbilityButtonView.select()` draws its frame in code |
| `battlefield/tile_selection_1.png` | 16×16 | 1-bit | – | – | – | – | `Highlight.ALTERNATIVE`, never drawn | not decoded by planning script (1-bit); palette test covers it |
| `battlefield/tile_selection_2.png` | 16×16 | 1-bit | – | – | – | – | `Highlight.CAST`, never drawn | |
| `battlefield/tile_selection_3.png` | 16×16 | 1-bit | – | – | – | – | `Highlight.MOVEMENT`, never drawn | `MovementRangeTileView` draws in code |
| `battlefield/tile_selection_4.png` | 16×16 | indexed | 23 | 0 | 1 | 0 | `Highlight.SELECTION` (`BattlefieldView` line ~324) | |
| `effect/venom-damage.png` | 16×16 | RGBA | 50 | 0 | 4 | 0 | `SpriteRegistry`, `EffectView`, `StatusListView` | drawn at 1 pt per art pixel |
| `effect/venom-on-death.png` | 16×16 | RGBA | 54 | 0 | 5 | 0 | same | |
| `terrain/transitions/sand_to_grass.png` | 256×16 | RGBA | 100 | 0 | 3 | 0 | `BattlefieldView`, `ResourcesTerrainLoader` (file name = rule) | colors F5B784, 20B562, 00604B |
| `terrain/transitions/sand_to_lava.png` | 256×16 | RGBA | 100 | 0 | 3 | 0 | same | F5B784, E03C28, 4F1507 |
| `terrain/transitions/sand_to_void.png` | 256×16 | RGBA | 100 | 0 | 2 | 0 | same | F5B784, 151515 |
| `terrain/transitions/sand_to_water.png` | 256×16 | RGBA | 100 | 0 | 3 | 0 | same | F5B784, 5BA8FF, 6264DC |
| `unit/knight.png` | 16×16 | indexed | 76 | 0 | 8 | 0 | `SpriteRegistry` | uses F5B784 (= sand) for skin |
| `unit/rat.png` | 16×16 | indexed | 75 | 0 | 4 | 0 | `SpriteRegistry` | 30 px of F5B784 (= sand) |
| `unit/bee.png` | 16×16 | RGBA | 24 | 0 | 4 | 0 | `SpriteRegistry` (deployed by `deploy-bee`) | very small silhouette |
| `unit/knight_portrait.png` | 32×32 | RGBA | 55 | 0 | 26 | 564 | `SpriteRegistry` → `UnitPortraitView` | excluded from palette check as "illustration" |
| `unit/rat_portrait.png` | 32×32 | RGBA | 62 | 0 | 28 | 642 | same | excluded from palette check |
| `unit/bee_portrait.png` | 32×32 | RGBA | 24 | 0 | 4 | 0 | same | appears to be the 16×16 sprite placed on a 32×32 canvas |
| `unit/goblins/goblin.png` | 32×32 | RGBA | 14 | 0 | 3 | 146 | test contact sheet only | reference sample, not a game unit |
| `unit/goblins/card_sample.jpeg` | 1582×1420 | JPEG | – | – | – | – | test contact sheet only | reference sample |
| `famicube-palette.png` | 64×1 | RGBA | 100 | 0 | 64 | 0 | palette tests | authoritative palette |
| `korge.png` | 512×512 | indexed | 82 | 1856 | 65 | 110638 | no code reference found | **[assumption]** may be used implicitly by the KorGE Gradle plugin as the app icon. Do not delete. |

No asset uses partial alpha except `korge.png`. No asset has animation frames.

### 5.1 Inventory after the redesign (M8)

All game art below is on the Famicube palette (`PaletteCheckerTest`), has no partial alpha and matches its family size (`AssetSpecTest`).

| Path | Size | Status |
| --- | --- | --- |
| `ability/{sword, poisoned_sword, mushroom, skull, teleport, bee, heal}.png` | 16×16 | Redrawn (M6a): family-coloured plate plus one symbol |
| `battlefield/tile_selection_4.png` | 16×16 | Retained (selection highlight, readable over every terrain) |
| `battlefield/tile_selection_1..3.png`, `ability/ability_selection.png` | – | Removed (M8): never drawn; registry entries removed |
| `effect/venom-damage.png`, `effect/venom-on-death.png` | 16×16, drawn at ×2 | Redrawn (M2, M6b) |
| `effect/fx_defeat.png`, `fx_hit.png`, `fx_heal.png`, `fx_venom.png` | 48×16 | Created (M2, M6c) |
| `effect/fx_spread.png` | 32×16 | Created (M6c) |
| `terrain/transitions/sand_to_{grass, lava, void, water}.png` | 256×16 | Redrawn (M2, M3); same names and Wang order |
| `terrain/variants/{sand, grass, water}.png` | 48×16 | Created (M7) |
| `unit/{knight, rat, bee}.png` | 16×16 | Redrawn (M2, M4) |
| `unit/{knight, rat, bee}_{idle, walk}.png` | 32×16 | Created (M5) |
| `unit/{knight, rat, bee}_portrait.png` | 32×32 | Redrawn on-palette (M4) |
| `unit/goblins/*`, `korge.png` | – | Untouched (Q7, A13) |

The procedural poof, status pop and spark in `FxViews` were removed in M8; the authored strips replace them.

## 6. Art direction and production standards

### 6.1 Densities and frame sizes per family

| Family | Art size | Drawn at | Rules |
| --- | --- | --- | --- |
| Terrain tile | 16×16, strips 256×16 (16 tiles, Wang order) | ×3 = 48 pt | Fully opaque. Tile 0 = pure `from` terrain; tile 15 = pure `to` terrain. Tile 0 identical across every `sand_to_*` strip (it is the sand base tile). |
| Unit sprite | 16×16 (frame); **[proposal]** visible body ≈ 12–15 px tall, feet on rows 13–15 | ×3 | Transparent background, no partial alpha. Frame 0 lives at the existing `unit/<id>.png`. |
| Unit animation strip | N × 16×16 horizontal, `unit/<id>_<anim>.png` | ×3 | Frame 0 of `idle` must equal `unit/<id>.png`. |
| Portrait | 32×32 | ×3 = 96 pt | On-palette pixel art (not illustration). Bust framing, same light direction as sprite. |
| Ability icon | 16×16, full square | ×3 = 48 pt (matches 48 pt slot) | Opaque background plate allowed (current style). One clear symbol, readable in grayscale. |
| Status/effect icon | 16×16 | currently 1 pt/px (Q5) | Transparent background, 1 px dark outline, readable at 16 pt. |
| Battlefield highlight | 16×16 | ×3 | Transparent, outline-only; must not hide the unit. |
| FX animation strip | N × 16×16 (one tile), `effect/fx_<name>.png` | ×3 | Must stay inside the target tile unless the effect is explicitly an area/spread effect. |

Do not invent other sizes. A family that needs a different size must be added to this table first.

### 6.2 Pixel-art rules [proposal]

- **Light:** from the top-left. Highlights on top/left surfaces, shadow on bottom/right.
- **Outlines:** characters, props and icons use a 1 px outline. Default outline `151515`; **selective outlining** (outline pixel takes the darkest ramp color of the adjacent material) allowed on large shapes. Terrain tiles have **no** outlines; only terrain edges in transitions get a 1 px darker rim on the lower/raised side.
- **Shading:** max 3 values per material (shadow, base, highlight) plus outline. No dithering on characters; checkered 2-color dithering allowed only on large terrain areas and sparingly.
- **Silhouette first:** every unit must be identifiable as a flat black silhouette at 16×16.
- **Ground contact:** each unit gets a 1–2 px dark ground shadow under the feet, inside the 16×16 frame, so it separates from any terrain.
- **No anti-aliasing, no partial alpha, no orphan pixels** (single pixels of a color that touch no other pixel of the same color) except intentional highlights.
- **Tiles:** seamless when repeated in any direction; low-contrast texture (≤ 2 values of difference inside a tile) so the grid of units and highlights reads above it. Avoid strong repeating features that create a visible 16 px grid.
- **Animation:** keep feet anchored; idle uses 1 px vertical bob at most; frames reuse pixels between frames wherever possible.
- **Scaling:** always integer `PIXEL_SCALE`, `smoothing = false`, positions snapped. Never scale pixel art by `size = …` to a non-multiple of its art size.
- **Hi-res UI separation:** engine text, bars and panel shapes are UI and may be drawn at point resolution. Bitmap assets under `resources/` are pixel art and follow these rules.

### 6.3 Palette [verified palette, proposed roles]

`resources/famicube-palette.png` is the authoritative definition (64 colors, enforced by `PaletteCheckerTest`). Its colors, in file order:

```
000000 00177D 024ACA 0084FF 5BA8FF 98DCFF 9BA0EF 6264DC 3D34A5 211640 5A1991 6A31CA A675FE E2C9FF FEC9ED D59CFC
CC69E4 A328B3 871646 CF3C71 FF82CE FFE9C5 F5B784 E18289 DA655E 823C3D 4F1507 E03C28 E2D7B5 C59782 AE6C37 5C3C0D
231712 AD4E1A F68F37 FFE737 FFBB31 CC8F15 939717 B6C121 EEFFA9 BEEB71 8CD612 6AB417 376D03 172808 004E00 139D08
58D332 20B562 00604B 005280 0A98AC 25E2CD BDFFCA 71A6A1 415D66 0D2030 151515 343434 7B7B7B A8A8A8 D7D7D7 FFFFFF
```

M1 must turn this into a short role table (`docs/design/art-direction.md`). The following constraints come from verified collisions and are mandatory inputs:

- **Reserved UI hues.** `UiPalette` uses `E03C28` for `enemy`, `danger` and `hpLoss`; `0084FF` for `ally` and `mana`; `FFE737` for `selection`; `98DCFF` for `move`; `FFBB31`/`F68F37` for cast states; `D59CFC` for `conditional`; `A328B3` for the missing-asset placeholder. Terrain must not use these as its dominant base color. Today `lava` is `E03C28` (same as enemy/danger) and `water` is `5BA8FF` (same as `manaCost`, next to `ally`/`move`).
- **Skin vs ground.** `F5B784` is both the sand base and the knight/rat skin/fur color. Units must not share their main body color with any base terrain color.
- **Value before hue.** Units sit one clear value step away from every base terrain (verify in grayscale, M0 readability sheet). Team identity stays on the existing team plate (`UnitOverlayView`) and is never encoded by sprite color alone.
- **Effects by family color + shape:** poison/venom = green ramp + droplet/bubble shape; fire/lava = orange-yellow ramp + flame shape; heal = light green/white + plus/sparkle shape; freeze (future) = cyan/white + crystal shape; physical damage = white flash + slash shape. Each must stay distinguishable in grayscale by shape.
- **Placeholder color** `A328B3` must not be used as a dominant color in any authored asset, so a magenta square always means "missing asset".

### 6.4 Tactical readability rules [proposal]

1. Units: silhouette + ground shadow + 1 px outline; at least one value step from the tile they stand on.
2. Terrain: each terrain readable by value and texture, not hue alone (grayscale test). Movement-blocking terrain (void) must read as clearly impassable (darkest values, hard rim).
3. Highlights and overlays are drawn above terrain and must remain visible over every terrain (check on the readability sheet).
4. Effects stay inside the affected tile(s) and finish within the existing beat durations in `FxViews`/`FeedbackTiming`; they never cover the HP bar or status pips of another unit for longer than one beat.
5. Status icons: one 16×16 icon per status family; never more than the existing pip layout already shows on a unit.
6. Decoration (future props) uses lower contrast than units and interactive objects.

### 6.5 Solo-developer production rules [proposal]

- One template file per family (16×16 unit frame with ground-line guide, 256×16 Wang strip with index labels in a guide layer that is not exported).
- Transition strips: draw the 16 tiles from 4 edge pieces + 4 corner pieces and recombine; only redraw the pieces for each new terrain pair.
- Reuse animation templates: all units share frame counts and timings per animation type.
- Effect families share shapes and differ by palette ramp (poison drop vs. fire drop).
- Prefer redrawing in place over adding files; add a new file only when a code path consumes it.
- Automate checks (M0) instead of manual inspection for size, alpha, palette and strip consistency.
- Tool: any pixel editor that exports 8-bit PNG with the Famicube palette loaded (e.g. Aseprite, LibreSprite, Pixelorama). **[assumption]** No editor is in the repository; the implementation agent cannot draw art itself (see §15).

## 7. Palette and pixel-perfect scaling requirements (summary)

- Every PNG under `resources/` except `korge.png` and `unit/goblins/` must pass `PaletteCheckerTest` and be listed in `GAME_ART_ASSET_PATHS`. The portrait exclusion is removed in M4 once portraits are redrawn on-palette.
- Exported as 8-bit RGBA or indexed PNG, no partial alpha, no color profile tricks.
- Drawn only at `PIXEL_SCALE` (3) with `smoothing = false`. Any `scale = 1.0` / `size = …` on pixel art must be justified in Q5 or fixed.
- Positions snapped with `snap` / `snapToArtPixel` (already enforced in code).

## 8. Asset-family audit and priorities

Priority: P1 = always on screen / tactical meaning; P2 = frequent; P3 = occasional; P4 = decorative.

| # | Family / path | Class | Evidence | Issue and impact | Action | Risk | Pri |
| --- | --- | --- | --- | --- | --- | --- | --- |
| A1 | Terrain strips `terrain/transitions/*.png` | **Redesign** | 2–3 flat colors per strip, no texture, diagonal blocky transitions (visual inspection + color counts) | Dominates every frame; reads as placeholder; lava = enemy red; sand = unit skin color; no sense of a continuous world | Redraw all 4 strips at same size/order; new color roles | Low (same paths); goldens change | P1 |
| A2 | Unit sprites `unit/knight.png`, `rat.png`, `bee.png` | **Redesign** | 16×16 single frame; knight/rat share sand color; bee silhouette ~24 % of frame | Low separation from sand; bee hard to see/tap | Redraw at 16×16 with ground shadow, outline, value contrast | Low (same paths) | P1 |
| A3 | Unit animation | **Create** | No frames exist; walker slides one frame | World feels static | Idle (2 frames) and walk (2 frames) strips + minimal code | Medium (code + snapshot determinism) | P2 |
| A4 | Portraits `unit/*_portrait.png` | **Replace** (knight, rat), **Redesign** (bee) | knight/rat 26–28 colors, 564/642 off-palette px; bee portrait reuses sprite | Breaks palette rule and style consistency in HUD | Redraw all three as on-palette 32×32 busts; add to palette check | Low | P2 |
| A5 | Ability icons `ability/*.png` (7) | **Polish** | On-palette, 4–8 colors, consistent 16×16 full-square format | Style consistency to be checked against new art direction; grayscale readability unknown | Review against rules; redraw only failing icons | Low | P2 |
| A6 | Status icons `effect/venom-*.png` | **Polish** + **Investigate** (scale) | On-palette; drawn at 1 pt per art pixel in `EffectView`/`StatusListView` | Inconsistent density vs world (Q5) | Decide scale; polish to family rules | Low–medium | P2 |
| A7 | Terrain effect icons `lava-damage`, `water-heal` | **Investigate** → **Create** | Effects exist in `SetupBattle.kt`; ids absent from `EFFECT_IDS`; no PNGs | If ever shown as a status, the magenta placeholder appears | Confirm whether they surface in `StatusListView`/`EffectsView`; create icons if so | Low | P2 |
| A8 | Combat FX (`FxViews` procedural flash/poof/pop/spark) | **Polish** (keep code), **Create** (authored strips for defeat, hit, heal, venom spread) | Procedural 1-color rings and dots | Feedback is functional but generic; no per-ability identity | Authored FX strips replacing poof first (prototype), then others | Medium (code) | P2 |
| A9 | Highlight `battlefield/tile_selection_4.png` | **Keep** / light **Polish** | Used for selection; on-palette | Check visibility over new terrain | Re-verify on readability sheet | Low | P1 |
| A10 | Highlights `tile_selection_1..3.png`, `ability/ability_selection.png` | **Investigate** (likely obsolete) | Loaded by `SpriteRegistry` but no render caller found; views draw these states in code | Dead weight, confusing for maintenance | Verify no dynamic use, then retire in M8 with registry entries | Low | P4 |
| A11 | UI chrome (bars, panels, buttons, text) | **Keep** | Drawn in code with `UiPalette`; guarded by tests | Not an asset problem; default KorGE font is hi-res UI (allowed) | No asset work; pixel UI font is open question Q6 | – | P3 |
| A12 | `unit/goblins/*` | **Investigate** | Reference samples; goblin 146 off-palette px; JPEG 1582×1420 | Ships in resources; not used by game | Keep; owner decides whether to move out of `resources/` (Q7) | Low | P4 |
| A13 | `korge.png` | **Keep** | No code reference; possibly plugin default icon | Unknown consumer | Do not touch | – | – |
| A14 | Base terrain variants / props | **Create** (later) | Only one tile per terrain | Visible tiling; world feels like a board | Optional M7 after core work | Medium (code) | P4 |
| A15 | Missing transitions (grass↔water etc.) | **Investigate** | Only `sand_to_*` exist | Adding files changes gameplay adjacency | Out of scope unless owner approves (Q4) | High (gameplay) | – |

## 9. Ordered milestones

| ID | Milestone | Depends on | Gate |
| --- | --- | --- | --- |
| M0 | Asset validation tooling and baseline | – | – |
| M1 | Art direction and palette roles document | M0 | Owner approves Q1–Q3, Q5 |
| M2 | Representative prototype (knight, sand_to_grass, venom-damage icon, defeat FX) | M1 | **Owner visual approval before any scale-up** |
| M3 | Terrain: remaining transition strips | M2 | – |
| M4 | Units and portraits | M2 (M3 for contrast checks) | – |
| M5 | Unit idle and walk animation | M4 | – |
| M6 | Ability icons, status icons and authored combat FX | M2 | – |
| M7 | Optional: terrain variants and props | M3 | Owner opts in |
| M8 | Cleanup, integration and final consistency review | M3–M6 | – |

## 10. Milestone details

Every milestone ends with: focused tests → `./gradlew jvmTest` → snapshot review → checklist update (§16).

### M0 — Asset validation tooling and baseline

- **Goal:** make asset problems visible automatically before any art changes.
- **Scope:** `src/jvmTest/kotlin/com/mkz/rpg/assets/`, `build.gradle.kts` (only if a new task is needed), `docs/design-tools/reference-asset-checks.md`.
- **Expected output:** new jvmTest checks, a readability sheet, updated reference doc. No asset or production code changes.
- **Technical requirements:**
  1. `AssetSpecTest` (new, next to `PaletteCheckerTest`): for every path in `GAME_ART_ASSET_PATHS`, assert decodable PNG, size per §6.1 family table (derive family from folder), no partial alpha (alpha ∈ {0, 255}).
  2. Transition checks: every file in `terrain/transitions/` matches `<a>_to_<b>.png`, is 256×16, both terrains have a `.toml`; tile 0 of all `sand_to_*` strips is pixel-identical; tile 0 and tile 15 are fully opaque.
  3. Registry coverage: every id in `SpriteRegistry` lists resolves to an existing file (test reads the same paths; expose the id lists to tests via `internal` only if necessary — that is a production code touch, keep it minimal and documented). Unit strip files, when they exist, have width multiple of 16 and frame 0 equal to `unit/<id>.png`.
  4. Readability sheet: extend `AssetContactSheetTest` (or add a tagged test run by `generateContactSheet`) that renders every unit sprite and `tile_selection_4` on top of every base terrain tile (tile 0 and 15 of each strip) at ×3, plus a grayscale copy. Output `build/readability-sheet.png`.
  5. A check that lists, as an informational test output (not failure), which `GAME_ART_ASSET_PATHS` entries contain a color reserved for UI per §6.3 as a dominant (> 40 % of opaque pixels) color.
- **Implementation steps:** write tests one at a time; run `./gradlew jvmTest --tests "com.mkz.rpg.assets.*"` after each; known current failures (e.g. reserved-color report) must be informational, not red.
- **Validation:** all asset tests green on the current, unchanged assets; `./gradlew generateContactSheet` produces both sheets.
- **Acceptance:** tests merged and green; `reference-asset-checks.md` documents each new test; baseline sheets attached to the milestone report; zero changes under `resources/`.
- **Rollback:** tests only; revert the commit.

### M1 — Art direction and palette roles

- **Goal:** fix the standards before drawing.
- **Scope:** new `docs/design/art-direction.md` (do not edit `design-brief.md`).
- **Expected output:** one short document (target ≤ 2 pages) containing: §6.1 family table (final), pixel rules (§6.2, final), a palette role table built only from the 64 verified hex values (outline, skin ramp, metal ramp, cloth ramps, each terrain ramp, effect ramps, reserved UI hues), readability rules (§6.4), and answers to Q1, Q2, Q3, Q5.
- **Visual requirements:** each terrain ramp checked in grayscale against each unit ramp (value difference visible).
- **Steps:** draft roles → render a 64-swatch strip grouped by role (optional, `build/` only) → owner review → record decisions.
- **Validation / acceptance:** owner has approved the document and the open-question answers are written in it. No asset changes.
- **Rollback:** documentation only.

### M2 — Representative prototype

- **Goal:** validate the art direction on one asset of each kind before scaling up.
- **Scope:** `resources/unit/knight.png`, `resources/terrain/transitions/sand_to_grass.png`, `resources/effect/venom-damage.png`, new `resources/effect/fx_defeat.png`, `FxViews.kt`/`SpriteRegistry.kt` for the defeat FX only.
- **Expected output:** 3 redrawn files, 1 new FX strip (3 frames × 16×16 = 48×16, matching `POOF_FRAMES = 3`), minimal code to draw the strip in place of `poofFrame`, updated goldens, `GAME_ART_ASSET_PATHS` updated.
- **Visual requirements:** §6.2–6.4. Knight reads in grayscale on sand and grass; sand no longer shares the knight's skin color; grass/sand edge has a readable rim; defeat FX stays within the tile and reads within 300 ms.
- **Technical requirements:** same paths and sizes; strip tile 0 must still match the other `sand_to_*` strips' tile 0 → **because sand changes, update tile 0 of `sand_to_lava`, `sand_to_void`, `sand_to_water` to the new sand tile in the same commit** (M0 test enforces it). Under `FeedbackTiming.Instant` the defeat FX must remain deterministic.
- **Steps:** (1) draw sand base + grass base + 16 transition tiles; (2) copy new sand tile 0 into the other three strips; (3) knight; (4) venom icon; (5) defeat strip + code; (6) run asset tests; (7) `./gradlew runJvm -Pscenario=terrain-mix` and `-Pscenario=ui-showcase`, screenshot; (8) run `jvmTest`, inspect `build/reports/snapshots/*.actual.png`, then `./gradlew updateSnapshots`.
- **Validation:** asset tests; readability sheet; snapshot diff review (only expected regions changed: terrain, knight, venom icon); manual checklist items 1, 2, 8.
- **Acceptance:** owner approves the prototype screenshots in writing. Without this approval, do not start M3–M6.
- **Rollback:** `git revert` of the milestone commit restores files and goldens; the procedural `poofFrame` code is kept (not deleted) until M8 so the FX can be switched back.

### M3 — Terrain

- **Goal:** consistent terrain set.
- **Scope:** `sand_to_lava.png`, `sand_to_water.png`, `sand_to_void.png` (all 256×16, same names). No new transition files.
- **Visual requirements:** lava base no longer dominated by `E03C28`; water not dominated by `5BA8FF`/`0084FF`; void reads as impassable (darkest, hard rim); edges consistent with M2 grass; low texture contrast; no visible 16 px grid when tiled.
- **Technical:** Wang order 0..15 (N=1, E=2, S=4, W=8); tile 0 = M2 sand tile; occupiable indices next to void (`{0,1,2,4,5,8,10}`) must still look standable (no rim that suggests a wall on standable tiles).
- **Steps:** one strip per commit (lava, water, void); after each: asset tests, `runJvm -Pscenario=terrain-mix` screenshot, readability sheet.
- **Validation / acceptance:** asset tests green; movement-range, cast-target and selection highlights visible on every terrain (readability sheet + `battle-movement-range` snapshot); grayscale check of `terrain-mix`; goldens updated after review.
- **Rollback:** per-strip commit revert.

### M4 — Units and portraits

- **Goal:** consistent cast.
- **Scope:** `unit/rat.png`, `unit/bee.png`, `unit/knight_portrait.png`, `unit/rat_portrait.png`, `unit/bee_portrait.png`; `FamicubePaletteChecker.kt` (remove portrait exclusions, add paths).
- **Visual requirements:** silhouettes distinct among the three units; bee silhouette enlarged within 16×16 (target ≥ 40 % opaque coverage **[proposal]**) for readability and tap confidence; portraits share light direction and outline style with sprites.
- **Technical:** 16×16 and 32×32, same paths. If Q2 approved taller sprites, this milestone also includes: foot-anchoring in `BattlefieldView.addImage`/`showWalker` for units, `SelectionBracketsView`/`UnitOverlayView` position checks, `SpriteRegistry.ART_SIZE` placeholder per family, and new acceptance criteria that the overhang never hides the HP bar of the unit above. **[proposal: do not approve for this redesign.]**
- **Steps:** rat → bee → portraits (one commit each).
- **Validation:** palette test now covers portraits; readability sheet; snapshots `battle-initial-layout`, `battle-enemy-inspected`, `battle-unit-selected`.
- **Acceptance:** all three units identifiable in grayscale on every terrain; portraits pass palette test.
- **Rollback:** per-file commit revert.

### M5 — Unit idle and walk animation

- **Goal:** life on the battlefield without hurting readability.
- **Scope:** new `unit/<id>_idle.png` (2 frames, 32×16) and `unit/<id>_walk.png` (2 frames, 32×16) for knight, rat, bee; `SpriteRegistry.kt` (load optional strips, fall back to the single frame), `BattlefieldView.kt` (play idle on standing units, walk on walkers).
- **Technical:** frame 0 of idle = `unit/<id>.png`; idle ~500 ms/frame, walk ~150 ms/frame **[proposal]**; under `FeedbackTiming.Instant` (tests) animations stay on frame 0 so goldens are deterministic; single facing only (facing directions = Q3). Hit flash silhouettes must be built per frame or from frame 0 (document which).
- **Steps:** registry support + tests (strip slicing, fallback) → knight strips → wire playback → rat, bee strips.
- **Validation:** unit tests for slicing/fallback; snapshots unchanged except where intended; manual check while scrolling (checklist item 8: no jitter, feet stay planted).
- **Acceptance:** all snapshot tests green without re-baselining caused by animation timing; manual playthrough of `ui-showcase` shows idle/walk.
- **Rollback:** delete the strip files → registry falls back to single frame; revert code commit.

### M6 — Ability icons, status icons and combat FX

Split into batches; each batch is its own commit and review.

- **M6a — Ability icons** (`ability/*.png`, 7 files): grayscale review; redraw only icons that fail §6.2/6.4. Snapshots: `battle-ability-selected`, `battle-ability-cooldowns`, `battle-cast-targets`. Cooldown numbers and lock glyph must remain readable over the redrawn icons.
- **M6b — Status icons** (`effect/venom-on-death.png`, plus `lava-damage`/`water-heal` if A7 confirms they display): apply Q5 decision (if icons move to ×3 or ×2, change `EffectView`/`StatusListView` scale in the same commit and verify layout widths). Add new ids to `EFFECT_IDS` and `GAME_ART_ASSET_PATHS`.
- **M6c — Combat FX:** authored strips, all N×16×16, reusing the M2 pattern: `fx_hit` (physical, replaces flash accent, ≤ 132 ms total), `fx_heal`, `fx_venom` (status applied, replaces pop), `fx_spread` (replaces spark along the spread path). Durations stay those in `FxViews`/`FeedbackTiming`; no new beats.
- **Validation:** asset tests; `-Pscenario=chain-showcase` for spread/on-death; `battle-propagation-preview`, `battle-after-cpu-playback` snapshots; checklist item 7 (CPU turn followable).
- **Acceptance:** each effect distinguishable in grayscale by shape; nothing covers a neighbouring unit's HP bar beyond one beat; no new placeholder appearances.
- **Rollback:** per batch revert; procedural FX code kept until M8.

### M7 — Optional: terrain variants and props

Only if the owner opts in after M3–M6.

- **Goal:** reduce visible tiling and make the world feel continuous.
- **Scope:** **[proposal]** `terrain/variants/<terrain>.png` strips (N×16) chosen deterministically by `(row, column)` hash in `BattlefieldView`; a variants folder outside `transitions/` so it cannot create gameplay rules. Props are decoration-only overlays, never affecting occupiability; a prop system is likely a separate design and should not be started without a design note.
- **Acceptance:** deterministic snapshots; highlights still readable; no gameplay test changes.

### M8 — Cleanup, integration and final review

- **Goal:** remove dead assets safely and confirm consistency.
- **Scope:** A10 assets, procedural FX functions replaced by authored strips, palette-checker lists, docs.
- **Steps:** (1) prove each candidate unused (grep for path, enum entry, id, and run the full suite with the file temporarily moved out); (2) remove registry entries and files in one commit per family; (3) regenerate contact and readability sheets; (4) run full manual checklist (`ui-regression-checklist.md`) on `ui-showcase`, `terrain-mix`, `chain-showcase`; (5) update `reference-asset-checks.md` and `ui-conventions.md` "Sprites" section.
- **Acceptance:** all tests green; contact sheet shows one consistent style; checklist answered with no open "no" items; inventory in §5 updated.
- **Rollback:** per-family revert.

## 11. Integration and compatibility strategy

- **Keep paths and sizes.** Redrawing at the same path and size needs no code change. This covers A1, A2, A4, A5, A6, A9.
- **Never put non-strip files in `resources/terrain/transitions/`.** Every PNG there is sliced as a strip. No backups, drafts or variants in that folder. (Since ALttP plan L4a, adjacency rules live in `terrain/<id>.toml`, not in these file names.)
- **Keep previous versions in git, not in `resources/`.** Rollback is `git revert`. For side-by-side comparison, export the old version with `git show <rev>:resources/<path> > build/compare/<name>.old.png` (in `build/`, not committed).
- **Code changes travel with their assets.** If a milestone needs a code change (M2 FX, M5, M6b/c, M7), the asset, code, tests and goldens go in the same commit.
- **Identify broken references:** M0 registry-coverage test; a magenta (`A328B3`) square on screen means a missing id; KorGE throws on a missing transition file at load time (no placeholder there).
- **Expected change vs integration failure:** expected = only the redrawn regions differ in `build/reports/snapshots/*.actual.png`, layout and positions identical. Integration failure = placeholder magenta, blur, shifted sprites, missing tiles, wrong tile in a transition (Wang order), changed movement range or HP numbers (gameplay, never expected from an asset change), or a failing non-snapshot test.
- **Gameplay guard:** any change in a non-visual test (domain, battle harness, deterministic replay) after an asset-only commit is a stop condition.

## 12. Testing and visual QA

### Automated (run in this order, cheapest first)

1. `./gradlew jvmTest --tests "com.mkz.rpg.assets.*"` — palette, spec, transition, registry checks.
2. `./gradlew generateContactSheet` — contact sheet + readability sheet in `build/`.
3. `./gradlew jvmTest --tests "com.mkz.rpg.screen.BattleSceneSnapshotTest"` — snapshot diffs.
4. `./gradlew jvmTest` — full suite (includes touch targets, UI tokens, gameplay).
5. After visual review only: `./gradlew updateSnapshots`, then re-run step 3.

Do not run `pitest` or `allTargetTests` for asset milestones; they are expensive and unrelated.

### Visual validation scenes

| Scene | How |
| --- | --- |
| Terrain-heavy view | `./gradlew runJvm -Pscenario=terrain-mix`, scroll whole map |
| Friendly and enemy units | `battle-initial-layout`, `battle-enemy-inspected` snapshots |
| Characters on contrasting terrain | `build/readability-sheet.png` (color + grayscale) |
| Movement range overlay | `battle-movement-range` snapshot |
| Multi-target ability / cast preview | `battle-cast-targets`, `battle-cast-preview` |
| Status propagation / death trigger | `battle-propagation-preview`; `runJvm -Pscenario=chain-showcase` |
| Most demanding FX | `battle-after-cpu-playback` + manual `chain-showcase` at `Standard` timing |
| Portrait mobile UI | Any snapshot (390×844); view at 100 % on a phone-sized window |

### Manual artistic review (per milestone)

- Grayscale check (desaturate a screenshot): units, terrains, effect families still distinguishable.
- Thumb check: units and icons recognisable at real size on a phone-sized window.
- Consistency: new assets next to old on the contact sheet share outline, light direction and shading depth.
- Relevant items of `docs/design/ui-regression-checklist.md`.

## 13. Risks, dependencies and open questions

### Risks

| Risk | Mitigation |
| --- | --- |
| Transition file names are gameplay data | Rule in §11; M0 transition test; never add/rename without owner approval |
| Sand base tile comes from whichever strip loads last | M0 test that tile 0 is identical across `sand_to_*`; update all strips together (M2) |
| Every art change breaks 10 goldens | Expected; review `.actual.png` before `updateSnapshots`; one family per commit keeps diffs reviewable |
| Animations make snapshots nondeterministic | Freeze frame 0 under `FeedbackTiming.Instant` (M5) |
| Taller sprites would obscure tiles and need code changes | Default to 16×16 (Q2) |
| The implementation agent cannot draw | §15: report the limitation; only use an approved workflow |
| Limited RAM | Run focused tests first; avoid `pitest`/`allTargetTests`; never print images as text |

### Open questions (owner decisions)

- **Q1** Accept that on-screen pixel size is larger than ALttP (because of 48 pt tiles), matching it only in proportion? *Recommended: yes.*
- **Q2** Unit frame 16×16 (recommended) or taller (16×24) with foot anchoring and overhang?
- **Q3** Units face one direction only for now, or add left/right (mirrored) facing? *Recommended: one direction in M5; mirroring later is cheap.*
- **Q4** Should new terrain pairs (e.g. grass↔water) ever be added? It changes gameplay adjacency and is outside this plan unless approved.
- **Q5** Status icons: keep at 1 pt per art pixel (treated as hi-res UI icons drawn at 16×16 pt) or draw at an integer art scale (×2 = 32 pt or ×3 = 48 pt) like the rest of the pixel art? Affects `EffectView` and `StatusListView` layout.
- **Q6** Should UI text move to a pixel/bitmap font? Requires a licensed font asset; currently out of scope.
- **Q7** Move `unit/goblins/` reference samples out of `resources/` so they don't ship?
- **Q8** Which pixel editor and export settings are the approved workflow (§15)?

### Assumptions to verify

- `korge.png` may be consumed by the KorGE Gradle plugin as the app icon (not verified).
- `lava-damage` / `water-heal` may never be displayed as statuses (A7, not verified).
- `tile_selection_1..3` and `ability_selection` are unused at render time (no caller found by text search; verify per M8 step 1).
- The horizontal striping visible on the battlefield in `battle-propagation-preview.png` is a dimming overlay drawn in code, not an asset. Not investigated.

## 14. Explicit non-goals

- No combat log, no permanent panels, no crowded status icons, no lighting/bloom/post-processing as part of the identity (brief §8).
- No exploration mode, no real-time/RTS systems, no mode-transition art.
- No new asset pipeline, atlas packer or engine change; no changes to `PIXEL_SCALE` or tile size.
- No new terrain types or transition pairs (gameplay) unless Q4 is approved.
- No copying, tracing or redistribution of ALttP, SMB3 or other copyrighted assets; references guide style only.
- No mass speculative asset production; only assets consumed by code paths in this plan.
- No gameplay or balance changes.

## 15. Instructions for the implementation agent

1. Read this plan and `docs/design/design-brief.md` completely before changing anything. After M1, also read `docs/design/art-direction.md`.
2. Work only on the milestone you were asked to do, in the order of §9. Inspect only files listed in that milestone's scope (plus their tests).
3. Start with M2's prototype before producing assets at scale. Do not start M3–M6 without written owner approval of M2.
4. **You cannot create pixel art yourself unless an approved tool is available.** If no approved workflow exists (Q8), do not fabricate, trace, generate procedurally, or claim to have produced artwork. Prepare everything else (tests, templates, code, checklists), then report what art is needed (path, size, frames, palette roles) and stop.
5. Validate each batch in the game (`./gradlew runJvm -Pscenario=…`) or via the snapshot/contact-sheet tools before moving on.
6. Keep existing paths, sizes and ids. Never add, rename or remove files in `resources/terrain/transitions/` without owner approval.
7. Do not redraw assets that already meet the art direction.
8. Run focused checks first (§12 order), then the full `jvmTest`. Do not run `pitest`.
9. Update goldens only after inspecting `build/reports/snapshots/` and confirming every difference is an intended art change.
10. In the milestone report, list: assets modified, created, retained, replaced/removed; tests run and results; screenshots/sheets reviewed; visual limitations; unresolved issues.
11. Tick items in §16 only when every acceptance criterion of that milestone passes. Never mark partial work as done; never skip a criterion silently.
12. Stop after the requested milestone and wait for the next instruction.

## 16. Milestone completion checklist

- [x] **M0** Asset spec, transition, registry tests and readability sheet added; all green on unchanged assets; docs updated.
- [x] **M1** `docs/design/art-direction.md` written; palette roles from verified hex only; Q1, Q2, Q3, Q5 answered by owner.
- [x] **M2** Prototype (knight, sand_to_grass + sand tile 0 in all strips, venom-damage, defeat FX) merged; goldens reviewed and updated; **owner approved**.
- [x] **M3** Lava, water, void strips redrawn; readability sheet and terrain-mix reviewed.
- [x] **M4** Rat, bee, three portraits redrawn; portraits palette-checked.
- [x] **M5** Idle and walk strips with registry fallback; snapshots deterministic.
- [x] **M6a** Ability icons reviewed / polished.
- [x] **M6b** Status icons polished; Q5 applied; terrain effect icons resolved (A7).
- [x] **M6c** Authored hit, heal, venom, spread FX.
- [x] **M7** (optional) Terrain variants / props.
- [ ] **M8** Unused assets retired after verification; docs and inventory updated; full manual checklist passed. *(Retirement, docs and inventory done; the manual `ui-regression-checklist.md` pass on `ui-showcase`, `terrain-mix` and `chain-showcase` is still to be run by the owner.)*
