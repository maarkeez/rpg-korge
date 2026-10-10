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

## Never color alone

| State | Color | Shape / glyph |
| --- | --- | --- |
| Selected unit | `selection` | corner brackets marker |
| Movement range | `move` | dotted fill |
| Valid target | `castValid` | solid border |
| Preview target | `castPreview` | crosshair |
| Cooldown | `textMuted` | turns-left number |
| Unavailable ability | `textMuted` | lock glyph |

## Touch targets

- At least 44 pt. Tiles are 48 pt, ability slots at least 48 pt, and Confirm, Cancel and End Turn at least 44 pt tall.
