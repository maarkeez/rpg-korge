# Art direction and palette roles

Standards for the pixel-art asset redesign. Derived from [`asset-redesign-plan.md`](asset-redesign-plan.md) §6 and [`design-brief.md`](design-brief.md). Read it before drawing or changing any file under `resources/`.

**Status: APPROVED by owner (M1 gate).** Unit frames stay 16×16 for now (Q2 as originally proposed).

Every color is one of the 64 entries of `resources/famicube-palette.png` (enforced by `PaletteCheckerTest`). Luma `Y` is `0.299 R + 0.587 G + 0.114 B` on a 0–255 scale, used for the grayscale check in §4.

---

## 1. Frame sizes per family

Final for M1. Do not invent other sizes; add a family here first.

| Family | Art size | Drawn at | Rules |
| --- | --- | --- | --- |
| Terrain tile | 16×16; strips 256×16 (16 tiles, Wang order) | ×3 = 48 pt | Fully opaque. Tile 0 = pure `from` terrain; tile 15 = pure `to` terrain. Tile 0 identical across all `sand_to_*` strips. |
| Unit sprite | 32×32 frame standing on its tile: the tile covers frame rows 16–31 and columns 8–23, so a unit may rise up to one tile above it and half a tile to each side | ×3 | Transparent background, no partial alpha. Frame 0 is `unit/<id>.png`. Body heights: see the ALttP plan (soft 24 px, hard 32 px). |
| Unit animation strip | N × 32×32, `unit/<id>_<anim>.png` | ×3 | Frame 0 of `idle` equals `unit/<id>.png`. |
| Portrait | 32×32 | ×3 = 96 pt | On-palette pixel bust, same light direction as the sprite. |
| Ability icon | 16×16 full square | ×3 = 48 pt | Opaque plate allowed. One clear symbol, readable in grayscale. |
| Status / effect icon | 16×16 | Q5 (see §5) | Transparent background, 1 px dark outline, readable at 16 pt. |
| Battlefield highlight | 16×16 | ×3 | Transparent, outline-only; must not hide the unit. |
| FX animation strip | N × 16×16 (one tile), `effect/fx_<name>.png` | ×3 | Stays inside the target tile unless explicitly an area effect. |

## 2. Pixel rules

- **Light** from the top-left: highlights on top/left, shadow on bottom/right.
- **Outlines**: 1 px. Default `151515`. Terrain tiles have no outline; only transition edges get a 1 px darker rim on the lower/raised side.
- **Shading**: at most 3 values per material (shadow, base, highlight) plus outline. No dithering on units; 2-color checker dithering only on large terrain areas, sparingly.
- **Silhouette first**: every unit reads as a flat black silhouette at 16×16.
- **Ground contact**: 1–2 px dark shadow under the feet, inside the 16×16 frame.
- **No anti-aliasing, no partial alpha, no orphan pixels** except deliberate highlights.
- **Tiles** are seamless in every direction, with ≤ 2 values of difference inside a tile, and no strong feature that forms a visible 16 px grid.
- **Animation**: feet anchored; idle bob ≤ 1 px; reuse pixels between frames.
- **Scaling**: integer `PIXEL_SCALE` (3) only, `smoothing = false`, positions snapped.

## 3. Palette roles

Only verified hex values. Units and terrain never use a reserved UI hue as their dominant color (see the reserved list at the end of this section).

| Role | Hex | Y | Use |
| --- | --- | --- | --- |
| Outline / void base | `151515` | 21 | 1 px outlines, ground shadow |
| Steel light / steel mid / steel shade | `A8A8A8` / `7B7B7B` / `343434` | 168 / 123 / 52 | Knight armor |
| Light neutral | `D7D7D7`, `FFFFFF` | 215, 255 | Metal glints, sparkle |
| Skin light / skin mid / skin dark | `FFE9C5` / `C59782` / `5C3C0D` | 235 / 162 / 64 | Unit skin (**not** `F5B784`, the sand base) |
| Leather / fur | `AE6C37` | 122 | Knight belt, rat fur highlight |
| Rat fur dark / base / shade | `AE6C37` / `5C3C0D` / `231712` | 122 / 64 / 27 | Rat body: dark base keeps a value step from sand and grass |
| Pink (ears, nose, tail) | `FEC9ED` / `E18289` / `CF3C71` | 216 / 159 / 112 | Rat details; `CF3C71` also the rat eye |
| Bee gold / shade / stripe | `CC8F15` / `AE6C37` / `343434` | 147 / 122 / 52 | Bee body (replaces reserved `FFE737`, `F68F37`) |
| Wings | `FFFFFF` / `D7D7D7` | 255 / 215 | Bee wings (replaces reserved `98DCFF`) |
| Knight tunic light / base / shade | `6264DC` / `3D34A5` / `211640` | 112 / 67 / 27 | Knight tunic and plume |
| Sand base | `F5B784` | 196 | Sand terrain only |
| Grass dark / base / light | `00604B` / `20B562` / `BDFFCA` | 65 / 127 / 229 | Grass terrain |
| Lava dark / base / light | `4F1507` / `AD4E1A` / `DA655E` | 37 / 100 / 135 | Lava terrain (no `E03C28`) |
| Water deep / base / light | `005280` / `0A98AC` / `25E2CD` | 63 / 112 / 167 | Water terrain (no `5BA8FF`) |
| Void base / rim | `0D2030` / `415D66` | 28 / 86 | Void terrain: darkest values, hard rim |
| Poison ramp | `376D03` / `6AB417` / `8CD612` / `BEEB71` | 81 / 140 / 170 / 208 | Venom effects and icons |
| Fire ramp | `AD4E1A` / `CC8F15` / `FFE9C5` | 100 / 147 / 235 | Fire / lava effects (core = light skin) |
| Heal ramp | `58D332` / `BDFFCA` / `FFFFFF` | 156 / 229 / 255 | Heal effects |
| Physical hit | `FFFFFF` / `D7D7D7` | 255 / 215 | White flash plus slash shape |

**Reserved UI hues** (`UiPalette`, `src/commonMain/.../UiPalette.kt`). Do not use as a terrain base or as a dominant color of any authored asset:

| Hex | UI meaning |
| --- | --- |
| `E03C28` | enemy, danger, hpLoss |
| `0084FF` | ally, mana, primary button |
| `5BA8FF` | manaCost |
| `FFE737` | selection |
| `98DCFF` | move, portrait hover |
| `FFBB31`, `F68F37` | castValid, castPreview |
| `D59CFC` | conditional |
| `58D332` | hpGain (heal ramp is the only allowed use, as a mid-tone) |
| `A328B3` | missing-asset placeholder: a magenta square must always mean "missing" |

**Collisions in the current art** that this table resolves:

- `F5B784` is both sand base and the knight/rat skin. Units move to the skin ramp above.
- Lava uses `E03C28` (enemy red). Lava moves to the lava ramp above.
- Water uses `5BA8FF` (manaCost). Water moves to the water ramp above.

## 4. Readability rules and the grayscale check

Rules (from plan §6.4):

1. A unit has a silhouette, a ground shadow and a 1 px outline, and sits at least one clear value step from the tile it stands on.
2. Each terrain is readable by value and texture, not hue alone. Void is the darkest value with a hard rim.
3. Highlights and overlays stay visible over every terrain.
4. Effects stay inside the affected tile(s) and finish within the existing beat durations in `FxViews` / `FeedbackTiming`.
5. One 16×16 status icon per status family.
6. Decoration uses lower contrast than units and interactive objects.

**Proposed threshold:** Δ luma ≥ 40 between a unit's main body and the tile it stands on. Verify on the readability sheet (M0 `generateContactSheet`).

Current art measured against the threshold (Δ luma):

| Pair | Δ Y | Result |
| --- | --- | --- |
| Knight steel `7B7B7B` vs grass base `20B562` | 4 | **Fails** — indistinguishable in grayscale |
| Knight outline `151515` vs void `151515` | 0 | **Fails** — knight disappears on void |
| Knight light `A8A8A8` vs water `5BA8FF` | 13 | **Fails** |
| Knight steel `7B7B7B` vs lava `E03C28` | 16 | **Fails** |
| Bee yellow `FFE737` vs sand `F5B784` | 22 | **Fails** |
| Knight steel `7B7B7B` vs sand `F5B784` | 73 | Pass |
| Rat fur `AE6C37` vs sand `F5B784` | 74 | Pass |
| Bee body `F68F37` vs lava `E03C28` | 57 | Pass |

The failures are the reason M3 and M4 exist. They are recorded here so they are not re-measured by hand.

## 5. Open questions (owner decisions)

Q1, Q2, Q3 and Q5 are approved as proposed. Units keep the 16×16 frame for now, so no code or asset size changes come from M1.

| Q | Question | Proposed answer | Owner decision |
| --- | --- | --- | --- |
| Q1 | Accept on-screen size larger than ALttP (48 pt tiles), matching only in proportion? | **Yes.** Keep 48 pt tiles; match proportion, not pixel size. | Approved |
| Q2 | Unit frame 16×16 or taller (16×24) with foot anchoring? | **16×16.** Taller frames would need code changes to the tile layout (§13 risk); revisit only if needed later. | Approved |
| Q3 | One facing direction or mirrored left/right? | **One direction in M5.** Mirroring later is cheap. | Approved |
| Q5 | Status icons at 1 pt per art pixel, or integer scale? | **×2 = 32 pt.** Keeps the integer-scale rule and avoids the crowded status icons the design brief (§8) rules out. Applied in M6b: `StatusListView` shows up to three statuses side by side (icon with name and turns beside it) so the 34 pt row still fits the sheet. | Approved |

Q4 (new terrain pairs), Q6 (pixel UI font), Q7 (`unit/goblins/` location) and Q8 (approved editor) are not decided here. They stay as listed in the plan.

## 6. Approval record

- [x] Owner has approved this document.
- [x] Q1, Q2, Q3, Q5 answers are written into §5 above.

M2 may start.
