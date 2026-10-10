# UI conventions

Short set of visual conventions for the battle UI. Source of truth in code: `UiPalette.kt`, `PixelScale.kt`, `SpriteRegistry.kt`.

## Scale and snapping

- `PIXEL_SCALE = 3`: points drawn per art pixel. A 16 px tile is 48 pt (`BattlefieldView.TILE_SIZE`).
- Pixel art is always drawn with `smoothing = false` at integer scale.
- Pixel-art view positions are snapped to integer points (`snap`). Battlefield scroll offsets are snapped to multiples of `PIXEL_SCALE` (`snapToArtPixel`).

## Colors

- Every UI color comes from `UiPalette` (`rpg/shared/adapters/presentation/UiPalette.kt`). Don't use `Colors.*` or `RGBA(...)` in views. `Colors.TRANSPARENT` is allowed.
- Tokens are semantic: `background`, `panel`, `panelBorder`, `textPrimary`, `textMuted`, `hp`, `hpLoss`, `hpGain`, `mana`, `manaCost`, `ally`, `enemy`, `selection`, `move`, `castValid`, `castPreview`, `conditional`, `danger` and the button/portrait tokens.
- Each token is a color from `resources/famicube-palette.png`. `UiPaletteTest` enforces it. Add new tokens to `UiPalette.all`.
- Text antialiasing creates off-palette pixels. That's accepted.

## Sprites

- Look up unit, ability, effect and highlight bitmaps through `SpriteRegistry`. Unknown ids return a `UiPalette.placeholder` bitmap and never throw.
- Art rules (sizes, palette roles, light, outlines) live in [`art-direction.md`](art-direction.md).
- Unit frames are 32×32 and stand on their tile (tile = frame rows 16–31, columns 8–23), so units may be taller and wider than a tile. `BattlefieldView` draws terrain, then units sorted by row (lower rows in front), then the tile buttons (highlights, dim, HP bars, selection, taps), then effects; an overhanging head never hides the HP bar of the unit above. The map can scroll past row 0 by the tallest unit's overhang.
- Unit animation: `unit/<id>_idle.png` and `unit/<id>_walk.png` are horizontal 32×32 strips, read with `SpriteRegistry.unitFrames`, which falls back to the single sprite. Idle frames follow `FeedbackTiming.idleFrameMs` (0 keeps frame 0, as in tests and snapshots); walkers show the next walk frame on each hop. Hit flashes use frame 0.
- Combat FX: `effect/fx_<name>.png` strips (`fx_defeat`, `fx_hit`, `fx_heal`, `fx_venom`, `fx_spread`) drawn at ×3 by `BattlefieldView` within the existing `FxViews` durations. A missing strip skips that effect.
- Status icons are drawn at ×2 (32 pt) in `StatusListView` (Q5); everything else in the battlefield is ×3.
- Terrain variants: `terrain/variants/<terrain>.png` holds alternate base tiles, picked by tile position. Never put them in `terrain/transitions/`, where every file is sliced as a transition strip.
- Terrain edges are drawn with corner overlays (dual grid): `terrain/dual/<from>_to_<to>.png` holds 16 tiles indexed by the corner mask (north-west 1, north-east 2, south-east 4, south-west 8). Every tile shows its plain terrain and `BattlefieldView` draws, centered on each tile corner, the overlay of each terrain touching it (void, water, lava, then grass). Pairs without corner art fall back to the edge transition strip. Gameplay still uses the edge Wang index.
- Terrain adjacency rules live in `transitionsTo` in `terrain/<id>.toml` (one side is enough); every allowed pair needs a `terrain/transitions/<from>_to_<to>.png` strip.

## Never color alone

| State | Color | Shape / glyph |
| --- | --- | --- |
| Selected unit | `selection` | corner brackets marker |
| Movement range | `move` | dotted fill |
| Valid target | `castValid` | solid border |
| Preview target | `castPreview` | reticle (corner brackets and side ticks, `CastPreviewTileView`) |
| Predicted HP change | `hpLoss` / `hpGain` | checkered ghost segment on the unit's HP bar |
| Predicted lethal hit | `textPrimary` | skull glyph |
| Status to be applied | `conditional` / `danger` | pip preceded by a "+" badge |
| Cooldown | `textMuted` | turns-left number |
| Unavailable ability | `textMuted` | lock glyph |

## Touch targets

- At least 44 pt. Tiles are 48 pt, ability slots at least 48 pt, and Confirm, Cancel and End Turn at least 44 pt tall.
