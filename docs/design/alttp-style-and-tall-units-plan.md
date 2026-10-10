# A Link to the Past style and tall units plan

Follow-up to [`asset-redesign-plan.md`](asset-redesign-plan.md) (M0–M8). It answers two owner requests:

1. Make the art read much closer to *The Legend of Zelda: A Link to the Past* (ALttP).
2. Let units be taller (and wider) than one tile, so there is room to draw them properly.

It is written for an implementation agent that has not seen the planning conversation. Read it in full, plus [`art-direction.md`](art-direction.md) and [`design-brief.md`](design-brief.md), before changing anything.

Status legend: **[verified]** checked in the repository while writing this plan; **[proposal]** needs owner approval at a gate. Owner decisions are recorded in §9.

The reference sheets the owner shared are ripped ALttP sprites. They guide style only. Never copy, trace, recolor or ship any of them (plan §14 non-goal still applies).

---

## 1. What makes the references look like ALttP

Observed in the shared sheets (overworld props and terrain, Link, the Hyrule soldiers, Death Mountain and water tile sets).

| # | Trait | ALttP | Ours today (after M8) | Gap |
| --- | --- | --- | --- | --- |
| S1 | Character size | Humanoids ≈ 16 px wide × 22–28 px tall (Link ≈ 16×24, soldiers with plumes ≈ 16×28), drawn over a 16×16 ground tile, so the head rises above the tile | Everything fits a 16×16 frame | Units must overhang the tile (§3) |
| S2 | Proportions | Chibi: head ≈ 40–50 % of the height, big eyes, short legs, readable hands and weapons | Head ≈ 45 %, but body crammed into 16 px, weapon squeezed beside it | More vertical room; a 2–2.5 heads tall body |
| S3 | Shading | 4–6 values per material, clusters (no pillow shading), light from the top-left, **hue-shifted** ramps (shadows cooler and more saturated, highlights warmer) | 3 values, mostly neutral ramps, some regions shaded by script rules | New ramps (§4.1), hand-placed clusters |
| S4 | Outlines | Dark 1 px outline on sprites and props; selective outline (dark tone of the material) on inner edges and on the light side | Black outline, selective lines only on the knight | Selective outlining everywhere |
| S5 | Ground shadow | Separate soft dark oval under every actor; flying actors keep the shadow on the ground | Grey band in the frame, bee shadow detached | Oval shadow in a fixed spot of the frame (§3.2) |
| S6 | Animation | 2–8 frames per action, 4 facing directions, idle bob, walk cycles like 1-2-3-4-5-6-3-4 | 2 frames, one facing | More frames, facing as a later phase (§6, L7) |
| S7 | Terrain texture | Dense, low-contrast micro texture (grass blades, dirt pebbles) built from 8×8 pieces; the 16 px grid never shows | Sparse marks, three variants per terrain | Denser texture, more variants |
| S8 | Terrain edges | Raised areas have outer **and** inner corners, a darker lip and a cast shadow. Cliffs show a vertical face several pixels tall. Water has a light foam line and a dark bank | Edge-only Wang (no outer corners), 1–2 px rim | Corner-aware visual transitions (§5) and cliff faces |
| S9 | Props | Bushes, rocks, pots, signs, fences, statues: outlined, shaded, readable as objects | None | Optional, after a props design note (unchanged from M7) |
| S10 | FX | Small, chunky, 3–6 frames (sparkles, poofs, slashes) | 2–3 frames, 16×16 | Mostly fine; more frames where it helps |

Not adopted on purpose: ALttP's HUD (hearts, magic meter), its exact palettes and any of its sprites, a real-time camera, exploration. UI stays hybrid (R7).

## 2. Constraints and how they interact

| Constraint | Source | Effect on this plan |
| --- | --- | --- |
| Tile = 16 art px = 48 pt, `PIXEL_SCALE = 3` | ui-conventions, touch targets ≥ 44 pt | Stays. Tall units grow over the tile, never change the grid |
| Famicube palette is authoritative (64 colors) | R8, `PaletteCheckerTest` | ALttP uses 15-color sub-palettes from 32 768 colors. Famicube still has usable 4–5 step ramps (§4.1), but some ALttP hues are missing or reserved for UI. Decided: keep Famicube (Q-L1, §9) |
| Reserved UI hues | `UiPalette`, art-direction §3 | Blocks the classic ALttP blue water (`0084FF`, `5BA8FF`, `98DCFF`) and bright yellow/orange (`FFE737`, `FFBB31`, `F68F37`) as dominant colors |
| Unit vs tile value step (Δ luma ≥ 40) | art-direction §4 | Applies to the main body over every terrain it can stand on |
| Taps select tiles | `BattlefieldView` tile buttons | Stays. The overhanging head of a unit belongs to the tile above for input (§3.6) |
| Gameplay terrain rules come from `terrain/transitions/` file names | `ResourcesTerrainLoader` **[verified]** | Any new visual transition format must not live there (§5) |
| Solo developer, no pixel artist in the repo | design brief §7, plan §15 | Script-made art plateaued at "clean but generic" in M2–M8. ALttP quality needs hand-drawn work (§7) |

## 3. Tall units: what the code needs

### 3.1 Why it is not just a bigger PNG [verified]

Today each unit sprite is a child of its tile's `UIButton` (`BattlefieldView.displayUnit` → `addImage` with `centerOn(button)`). Each tile button also holds the terrain image, movement/cast highlights, the dim layer, the selection brackets (`SelectionBracketsView`) and the HP bar and pips (`UnitOverlayView`, bottom 3 art rows of the tile). Buttons draw in row order.

A unit taller than its tile drawn this way would:

- be centered on the tile instead of standing on it (head and feet both overflow by the same amount);
- cover the HP bar, status pips and selection brackets of the unit standing on the tile above, because the lower row's button draws later;
- be cut off at the top of the map by the viewport `clipContainer` for units on row 0 (`clampScroll` allows no margin above row 0);
- have its hit flash, poof, heal/venom FX, walker and damage number positioned for a 16×16 tile, not for the body.

### 3.2 Unit frame format [proposal]

| Item | Value |
| --- | --- |
| Frame size | **32×32** art px for every unit frame and every frame of its strips (`unit/<id>.png`, `unit/<id>_idle.png`, `unit/<id>_walk.png`, …) |
| Anchor | The frame is placed with its bottom edge on the tile's bottom edge and horizontally centered: frame origin = tile origin + (−8, −16) art px |
| Ground line | Feet on frame rows 28–29; shadow oval centered at (16, 30), rows 29–31, inside the tile |
| Body budget | Humanoid body ≤ 16 px wide (weapons, wings, tails may use the 8 px side margins); **soft height 24 px** (overhang 8 px, half a tile); **hard height 32 px** (one full tile) reserved for large creatures |
| Strips | Horizontal strips of 32×32 frames |
| Placeholder | Missing unit ids get a 32×32 magenta frame |

Why 32×32 and not 16×24: one square frame size for every unit keeps strips, slicing and anchoring trivial, leaves room for weapons and wings beside the body, and still lets small units (rat, bee) sit in the bottom of the frame with no code difference. A 16×24 frame would need a second migration as soon as one unit needs a wider pose.

The 16×16 tile, the 48 pt grid, the touch targets, the HP bar position and every non-unit family stay as they are.

### 3.3 Render layers [proposal]

Split the battlefield into layers that scroll together (they all live in the existing `viewport`):

| Order | Layer | Contents | Input |
| --- | --- | --- | --- |
| 1 | Tiles | Tile buttons: terrain image, movement range, cast targets, cast preview, conditional outline, spread connectors | Receives taps (unchanged) |
| 2 | Units | One view per standing unit, positioned by the anchor, **y-sorted** (row, then column) so lower units draw in front | `mouseEnabled = false` |
| 3 | Dim | The "outside valid tiles" dim, so it darkens units too (as today) | `mouseEnabled = false` |
| 4 | Overlays | `UnitOverlayView` (HP bar, pips), `SelectionBracketsView` and the inspect glyph | `mouseEnabled = false` |
| 5 | FX | `FxLayer` (flashes, numbers, strips, walkers) | Already `mouseEnabled = false` |

This keeps every HP bar and selection above every unit, whatever the overlap. The public `BattlefieldView` API keeps its signatures (`displayKnightBattleUnit(row, column)`, `removeBattleUnit(row, column)`, `displayUnitOverlay(...)`, `displayUnitSelection(...)`); internally it keeps a `(row, column) → view` map per layer instead of looking views up inside tile buttons. Names (`BATTLE_UNIT`, `UNIT_OVERLAY`, `SELECTION`) stay so descendant lookups in tests and the snapshot harness keep working.

### 3.4 Code changes [verified locations]

| File | Change |
| --- | --- |
| `screen/BattlefieldView.kt` | Add the unit, dim and overlay layers. `displayUnit` places the frame with the anchor in the unit layer and re-sorts by row. `removeBattleUnit`, `removeAllBattleUnits`, `displayUnitOverlay`, `displayUnitSelection`, `dimOutside`/`clearDim` use the layers. Add `unitOrigin(row, column)` and use it for `showWalker`, `playHitFlash` (silhouette), `playPoof`, `playHitSlash`, `playHealSparkle`, `playStatusPop` (center on the body, not the tile). `playAmountPop` starts above the unit's visible top (computed once per bitmap from its opaque bounds). Idle playback (`idleImages`) is unchanged. |
| `screen/BattlefieldView.kt` (`clampScroll`, map size) | Add a top margin of one tile (the hard overhang) to the scrollable area so row 0 heads are never clipped; keep `snapToArtPixel`. |
| `screen/SpriteRegistry.kt` | `UNIT_FRAME_SIZE = 32`; slice unit strips by it; 32×32 unit placeholder; silhouettes keep the frame size. Ability, effect and highlight sizes unchanged. |
| `screen/UnitOverlayView.kt`, `SelectionBracketsView.kt` | No geometry change: still tile-relative. They move to the overlay layer. |
| `screen/BattleUnitInfoView.kt`, `UnitPortraitView.kt` | None (portraits stay 32×32 busts). |
| `src/jvmTest/.../assets/AssetTestSupport.kt` | Unit family 32×32; unit strips N×32 × 32. |
| `SpriteRegistryCoverageTest`, `ReadabilitySheetTest` | Frame size 32; the readability sheet draws each unit over a 1×2 tile column (the tile and the tile above), so the overhang is reviewed over every terrain. |
| `BattlefieldViewTest`, `BattlefieldViewFeedbackTest` | Tests that assert `parent == tile(...)` for units or overlays change to the layer lookups; add tests for anchoring, y-sorting, overlays above a unit from the row below, and the top scroll margin. |
| Goldens | All 10 change once art changes. Phase L1 must leave them unchanged (§6). |

### 3.5 Compatibility path

Land the code first with the current art padded into 32×32 frames (bottom-centered: the 16×16 sprite occupies frame columns 8–23, rows 16–31). The battlefield must then render pixel-identical to today, so all goldens stay unchanged. That proves the anchoring and layering before any art changes.

### 3.6 Readability and input rules for tall units [proposal]

1. HP bars, pips and selection always draw above every unit (layer 4).
2. The overhang may cover the lower part of the unit standing above. Lower rows are in front, as in ALttP. Keep the soft height (24 px) for common units so the unit above stays readable.
3. Movement dots and cast outlines are on the tile layer. A head covering the top of a highlighted tile is acceptable; the bottom half of every tile stays visible.
4. Taps still select the tile under the finger. Tapping a unit's head selects the tile above. This matches the tile-based rules and avoids ambiguous hit testing; no sprite hit-testing (Q-L4, §9).
5. A dark ground shadow under every grounded unit; hovering units keep a separate oval on the ground row.

## 4. ALttP style rules (art direction v2) [proposal]

To be written into a new section of `art-direction.md` at gate L2, replacing the parts that conflict.

### 4.1 Ramps from the Famicube palette

Only verified palette entries. Y is luma (0–255). Hue-shifted where the palette allows: shadows move toward red/purple, highlights toward yellow.

| Material | Outline / deep | Shadow | Base | Light | Highlight | Notes |
| --- | --- | --- | --- | --- | --- | --- |
| ALttP grass (yellow-green) | `172808` (31) | `376D03` (81) | `6AB417` (140) | `8CD612` (170) | `BEEB71` (208) | Replaces the teal grass `20B562`. Today this is the poison ramp; poison moves (below) |
| Dirt / sand path | `5C3C0D` (64) | `AE6C37` (122) | `C59782` (162) | `F5B784` (196) | `FFE9C5` (235) | Base becomes `C59782` with `F5B784` light, closer to ALttP's tan paths; lighter `F5B784` base stays an option |
| Cliff / rock (Death Mountain) | `231712` (26) | `5C3C0D` (64) | `AE6C37` (122) | `C59782` (162) | `E2D7B5` (214) | Void edges become chasm cliffs with a 3–4 px face |
| Water | `00177D` (28) | `005280` (63) | `0A98AC` (112) | `25E2CD` (167) | `FFFFFF` foam | Teal (Q-L2): ALttP blue is reserved for UI |
| Lava | `4F1507` (37) | `AD4E1A` (100) | `CC8F15` (147) | `FFE9C5` (235) | `FFFFFF` | Brighter molten base than today; `E03C28` stays forbidden |
| Steel / armor | `151515` (21) | `415D66` (86) | `7B7B7B` (123) | `A8A8A8` (168) | `D7D7D7`/`FFFFFF` | Cool shadow like ALttP soldiers |
| Skin | `823C3D` (81) | `E18289` (159) | `FFE9C5` (235) | – | `FFFFFF` | Pink-shifted shading (ALttP style); never `F5B784` as a main color |
| Cloth (blue) | `211640` (30) | `3D34A5` (68) | `6264DC` (113) | `9BA0EF` (168) | `E2C9FF` | Knight tunic |
| Cloth (green, soldier) | `172808` | `376D03` | `6AB417` | `8CD612` | `BEEB71` | Same as grass: use only with a dark outline and never on grass-colored units without a value step |
| Poison (moved) | `211640` (30) | `5A1991` (58) | `6A31CA` (83) | `A675FE` (147) | `E2C9FF` (215) | Purple, distinct from the new grass. `D59CFC` (conditional) and `A328B3` (placeholder) stay forbidden |

Every unit/terrain pair must be re-measured against the Δ luma ≥ 40 rule on the readability sheet; the knight's navy tunic (68) on the new grass base (140) gives 72, on lava base (147) 79. Known failure to solve in L5: the bee's gold `CC8F15` (147) on the new grass (140) gives 7, so the bee needs dark stripes as its dominant value or a darker body.

### 4.2 Drawing rules

- Proportions: humanoids 2–2.5 heads tall; head 10–12 px wide; eyes 2 px tall; hands and weapon readable at ×1.
- Shading: 4 values per material plus outline; clusters of at least 2 px; no pillow shading (light is top-left, not "center").
- Outline: dark 1 px on the outer silhouette; inner lines use the material's deep tone; the lit side of a silhouette may use the deep tone instead of `151515` (selective outline).
- Ground shadow: oval 10–12 px wide, 3 px tall, `151515`/`343434`, centered under the feet.
- Terrain: dense micro texture with ≤ 2 values of contrast inside a material, built from 8×8 sub-blocks; every terrain gets 3–4 variants (M7 mechanism).
- Raised edges: deep-tone rim, 1 lit pixel on top/left lips, 1–2 px cast shadow down/right. Cliffs: 3–4 px vertical face with 2 ridge bands, darker toward the bottom.
- Water: 1 px foam on the shore, dark bank under north/west shores, 2–4 frame ripple animation (optional).
- Animation targets: idle 2–4 frames, walk 4–6 frames, attack 3 frames, hit 1 frame.

## 5. Corner-aware terrain transitions [proposal, gated]

ALttP edges have outer and inner corners. Our edge-only Wang index (`Battlefield.kt`, 16 tiles) cannot show an outer corner: a grass patch's corner tile does not know it is a corner. The look in S8 needs corners.

**Keep the gameplay index as it is.** Occupiability next to void uses the edge Wang index (`OCCUPIABLE_TERRAIN_TRANSITION_WANG_INDICES`) and must not change.

**Add a presentation-only "dual grid" pass** in `BattlefieldView`:

- For every tile corner (vertex), look at the 4 tiles around it and build a 4-bit corner mask per non-sand terrain (marching squares). 16 masks per terrain pair, the same count as today.
- Draw those mask tiles offset by half a tile, over a pure sand base. Layer order: void, water, lava, grass, so diagonal meetings resolve predictably.
- Art lives in a new folder, `terrain/dual/sand_to_<terrain>.png` (256×16, mask order), **never in `terrain/transitions/`**.
- The existing `terrain/transitions/*.png` stay as gameplay rule carriers until L4a moves allowed adjacency into `terrain/<id>.toml` (approved, Q-L3); L4a is a gameplay data change with its own domain tests.

This is a code change of moderate size (one pure function computing masks, tested in isolation, plus drawing). It is optional: the ALttP texture and ramps (L4) can ship on the current strips if the owner declines it.

## 6. Milestones

Every milestone: focused tests → `./gradlew jvmTest` → `./gradlew lintKotlin` → snapshot review → checklist. Commit messages start with the milestone id (`L1: …`). Note: the first `jvmTest` run right after `updateSnapshots` often fails with a Mockito `MockMaker` initialization error; re-run once before investigating.

| ID | Milestone | Depends on | Gate |
| --- | --- | --- | --- |
| L1 | Render layers and 32×32 unit frames, current art padded | – | Goldens unchanged |
| L2 | Art direction v2 (ramps, proportions, rules, templates) | – | Q-L1…Q-L6 answered (§9); owner reviews the document |
| L3 | Prototype: knight at the new size, ALttP grass and dirt, one cliff edge | L1, L2 | **Owner visual approval** |
| L4 | Terrain v2: all strips, variants for every terrain | L3 | – |
| L4a | Move terrain adjacency rules into `terrain/<id>.toml` (Q-L3) | – | Domain tests green; gameplay unchanged |
| L4b | Optional: corner-aware dual-grid transitions (§5) | L3, L4a | Owner opts in |
| L5 | Units v2: rat, bee; idle 2–4 and walk 4–6 frames for all units | L3 | – |
| L6 | Portraits, ability/status icons and FX refreshed to the v2 ramps (poison moves to purple) | L2 | – |
| L7 | Optional: facing (left/right mirror, then up/down) | L5 | Owner opts in |
| L8 | Cleanup, docs, full manual checklist | L4–L6 | Owner runs the manual checklist |

### L1 — Render layers and tall frames

- Scope: §3.3, §3.4 code; pad `unit/*.png`, `unit/*_idle.png`, `unit/*_walk.png` to 32×32 frames (bottom-centered).
- Acceptance: all goldens unchanged; new tests for anchor, y-sort, overlays above a lower unit's head (draw a temporary 32-tall test bitmap), top scroll margin, walker/flash positions; readability sheet shows the 1×2 tile column.
- Rollback: revert the commit (assets and code travel together).
- Done notes: tile buttons now sit above terrain and units, so KorGE's default button drop shadow had to be switched off. It used to draw a faint darker band on the right and bottom of every tile (a visible 16 px grid). Removing it is the only golden change: every changed pixel got lighter, nothing moved. Body-centered FX use the unit's measured overhang (`SpriteRegistry.unitOverhang`), which is 0 for the padded art, so effects are unchanged.

### L2 — Art direction v2

- Scope: `art-direction.md` (new "v2" sections, old ones marked superseded), a palette file for editors (`docs/design/famicube.gpl`), and templates: a 32×32 unit frame guide with the anchor, ground line and soft/hard height marked; a 16×16 terrain tile with 8×8 sub-grid guides. Templates live under `docs/design/templates/`, never under `resources/`.
- Acceptance: §9 decisions copied into `art-direction.md`; owner has reviewed the document.

### L3 — Prototype

- Scope: `unit/knight.png` (+ idle/walk) at ≈ 16×24 body in the 32×32 frame; sand/dirt and grass with v2 ramps and texture; one cliff-style edge on void; readability sheet; `terrain-mix` and `ui-showcase` screenshots.
- Acceptance: owner approves in writing. Do not start L4–L6 before that.

### L4 / L4b / L5 / L6 / L7 / L8

As in the table; each follows the M-series pattern (one commit per milestone, goldens reviewed before `updateSnapshots`, palette and spec tests extended for any new family). L6 must update the venom icons and FX together with the poison ramp so a status never changes hue between map and HUD.

## 7. Who draws the art (decided: the agent, Q-L5)

The M2–M8 art was generated by scripts (shapes, auto-outline, rule-based shading). That approach reliably produces clean, on-palette, consistent assets, but it stops short of ALttP's hand-placed clusters, expressive faces and fluid animation. Options:

| Option | Quality | Cost | Agent role |
| --- | --- | --- | --- |
| A. Hand-drawn in Aseprite (or LibreSprite/Pixelorama) by the owner or a pixel artist, using the L2 templates and palette | Closest to ALttP | Artist time | Templates, palette, validation, integration, previews, goldens |
| B. Agent drafts by script, artist polishes | Good | Less artist time | Drafts plus everything in A |
| C. Agent only (as M2–M8) | Clean but generic | Lowest | Everything |

**Decision (Q-L5): option C, the agent draws all art.** To push past the M2–M8 ceiling, the agent works per asset in hand-authored pixel grids (not shape generators), applies §4.2 cluster and selective-outline rules by hand, reviews every asset enlarged and in grayscale, and compares it with the ALttP traits in §1 before integrating.

## 8. Testing and validation

- Automated, cheapest first: `./gradlew jvmTest --tests "com.mkz.rpg.assets.*"` → `./gradlew generateContactSheet` → `./gradlew jvmTest --tests "com.mkz.rpg.screen.BattleSceneSnapshotTest"` → `./gradlew jvmTest` → `./gradlew lintKotlin`.
- New checks: unit frames 32×32; shadow pixels present in rows 29–31 of grounded units; body height ≤ 32 and, for units listed as "soft", ≤ 24 (opaque bounds above the shadow); overlay layer above unit layer.
- Visual: readability sheet (color and grayscale, 1×2 columns), `terrain-mix`, `ui-showcase`, `chain-showcase`. Check a unit standing directly below another: its head must never hide the upper unit's HP bar.
- Manual: `docs/design/ui-regression-checklist.md`, items 1, 2, 7, 8, 9 in particular (selection, ranges, CPU turn, scrolling with tall sprites, thumb reach).

## 9. Owner decisions

Answered by the owner after this plan was written. L2 still records the final art rules, but these answers are no longer open.

| Q | Question | Decision |
| --- | --- | --- |
| Q-L1 | Palette | **Keep Famicube** as the only palette. §4.1 ramps apply; `PaletteCheckerTest` unchanged |
| Q-L2 | Water hue | **Keep teal** (`005280` / `0A98AC` / `25E2CD`, deep `00177D`, foam `FFFFFF`). Blue water would force new ally, mana and move UI hues and re-validating every UI state; teal already reads as water and keeps the knight (68) at Δ 44 on the water base (112). Chosen by the agent at the owner's request |
| Q-L3 | Terrain adjacency rules | **Approved:** move allowed adjacency from `terrain/transitions/` file names into `terrain/<id>.toml`, so terrain art can change format. Gameplay data change: done in its own commit with domain tests, before L4b |
| Q-L4 | Taps on an overhanging head | **Approved:** select the tile under the finger (no sprite hit-testing) |
| Q-L5 | Who draws the v2 art | **The agent draws all art** (option C in §7) |
| Q-L6 | Unit height | **Approved:** soft 24 px for common units, hard 32 px for large creatures |

## 10. Risks

| Risk | Mitigation |
| --- | --- |
| Heads hide information of the unit above | Overlay layer above units; soft height 24 px; reviewed on the readability sheet and in snapshots |
| Layer refactor breaks view tests and the snapshot harness | Keep public API and view names; L1 must leave goldens unchanged |
| Poison and grass share the green ramp today | Poison moves to purple in L6; grass ships in L3/L4 only after L2 records the swap |
| Agent-drawn art stays generic (as in M2–M8) | Q-L5 picked agent-drawn art: hand-authored grids per asset, §4.2 rules, enlarged and grayscale review, and the L3 owner gate before scaling up |
| Corner-aware transitions touch terrain loading | Presentation-only dual grid in a new folder; gameplay index untouched; optional (L4b) |
| Copyright | References guide style only; no copying, tracing or recoloring ALttP assets |

## 11. Checklist

- [x] **L1** Layers and 32×32 frames; goldens unchanged except the removed tile-button drop shadow (see L1 notes).
- [ ] **L2** Art direction v2 and templates (decisions in §9).
- [ ] **L3** Prototype approved by the owner.
- [ ] **L4** Terrain v2.
- [ ] **L4a** Terrain adjacency rules in `terrain/<id>.toml`.
- [ ] **L4b** (optional) Dual-grid transitions.
- [ ] **L5** Units v2 with animation.
- [ ] **L6** Portraits, icons, FX v2.
- [ ] **L7** (optional) Facing.
- [ ] **L8** Cleanup, docs, manual checklist.
