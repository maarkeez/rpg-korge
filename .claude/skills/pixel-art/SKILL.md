---
name: pixel-art
description: Draws and edits the game's pixel art (units, npcs, portraits, items, abilities, effects, buildings, terrain) with the `pixel` command line tool, in the layered Aseprite sources under art/. Use it whenever an asset under resources/ is to be drawn, redrawn, recoloured, fixed or reviewed, or a design brief is to be followed.
---

# Pixel art

Every image of the game has a layered Aseprite source under `art/`, mirroring `resources/`:
`art/unit/knight.aseprite` is exported to `resources/unit/knight.png`. Draw on the sources with
`tools/pixel-art/pixel`, never on the PNGs: every command that saves a source also exports its PNG.

Run `tools/pixel-art/pixel --help`, and `--help` on any command, for the exact options.

## The rules every asset follows

These are enforced by the architecture tests (see [assets](../../../docs/architecture/assets.md)):

- Only the 64 FamiCube colours, fully opaque, or fully transparent. The tool can not write anything else.
- Battle units, npcs, their portraits, lootable items, buildings and the chest are outlined with `W` (`#151515`): the
  dark pixels of the silhouette, diagonals included, must be `W`. Light or saturated trims on the silhouette are fine.
- Sizes: units and npcs are 16x16, or 32x32 when drawn with the [humanoid brief](../../../docs/design/humanoid-characters.md);
  items, abilities, effects and the chest are 16x16; portraits are 32x32 (`<id>_portrait`);
  buildings are 16 pixels per tile; terrain transition strips are 256x16.

## Colours

Every colour has a fixed character, the same in every file; `.` is transparent. When painting, `-` keeps a pixel as it
is. The ramps, from dark to light:

| Ramp | Colours |
|------|---------|
| Greys | `0` #000000 `W` #151515 `X` #343434 `Y` #7b7b7b `Z` #a8a8a8 `+` #d7d7d7 `=` #ffffff |
| Blues | `1` #00177d `2` #024aca `3` #0084ff `4` #5ba8ff `5` #98dcff |
| Indigos | `9` #211640 `8` #3d34a5 `7` #6264dc `6` #9ba0ef |
| Violets | `a` #5a1991 `b` #6a31ca `c` #a675fe `d` #e2c9ff |
| Magentas | `i` #871646 `h` #a328b3 `g` #cc69e4 `f` #d59cfc `e` #fec9ed |
| Pinks | `j` #cf3c71 `k` #ff82ce |
| Skins and reds | `q` #4f1507 `p` #823c3d `r` #e03c28 `o` #da655e `n` #e18289 `m` #f5b784 `l` #ffe9c5 |
| Browns | `w` #231712 `v` #5c3c0d `u` #ae6c37 `t` #c59782 `s` #e2d7b5 |
| Oranges and yellows | `x` #ad4e1a `B` #cc8f15 `y` #f68f37 `A` #ffbb31 `z` #ffe737 |
| Yellow greens | `J` #172808 `I` #376d03 `C` #939717 `H` #6ab417 `D` #b6c121 `G` #8cd612 `F` #beeb71 `E` #eeffa9 |
| Greens | `K` #004e00 `O` #00604b `L` #139d08 `N` #20b562 `M` #58d332 |
| Teals and blue greys | `V` #0d2030 `P` #005280 `U` #415d66 `Q` #0a98ac `T` #71a6a1 `R` #25e2cd `S` #bdffca |

Commands take a colour as its character, its hex (e.g. `#151515`) or `.`.

## Layers

Layer names are free. Unless the design brief says otherwise, keep from the top:

| Layer       | Holds                                                         |
|-------------|---------------------------------------------------------------|
| `outline`   | The `W` silhouette, and the inner lines.                      |
| `highlight` | The lit side of each shape.                                   |
| `shade`     | The side away from the light, and the cast shadows.           |
| `base`      | The flat colour of each shape.                                |
| `sketch`    | A hidden guide, e.g. an imported reference. Hide it when done. |

Separate layers let a shade be redrawn or recoloured (`pixel replace`) without touching the rest. Hidden layers are not
exported.

## Workflow

1. **Read the design brief** for the asset, under `docs/design/` (e.g. [humanoid characters](../../../docs/design/humanoid-characters.md)),
   and look at the assets it must match: `pixel preview` them (e.g. for a humanoid, the A Link to the Past references
   under `art-drafts/reference/`) and read the PNG it prints.
2. **Start the source**: `pixel new art/unit/goblin.aseprite --size 16x16 --layer outline --layer shade --layer base`,
   or `pixel layer add` on an existing one. Its PNG is exported right away, so the game can display it.
3. **Draw** layer by layer, most of it in one `pixel apply` script, which saves once and only when every line
   succeeds:

   ```shell
   tools/pixel-art/pixel apply art/unit/goblin.aseprite <<'SCRIPT'
   # Body: the left half, then mirrored
   rect --layer base 5,6,6x7 H --filled
   paint --layer outline --at 4,2
   --WWWW
   -WHHHH
   WHH0HH
   end
   mirror --layer outline --from left
   SCRIPT
   ```

   - `paint` is the main tool: draw shapes as grids, `-` keeps what is under them, so a grid only changes its shape.
   - Draw a front facing character's left half, then `mirror --from left`. Break the symmetry afterwards, with `set`.
   - `fill`, `rect`, `line`, `replace`, `flip`, `shift` and `copy` (also from another sprite, with `--from`) save
     redrawing by hand.
4. **Look at it**: every change prints the layer drawn on. `pixel grid FILE` prints the composite, and
   `pixel preview FILE` writes an upscaled PNG: read it to judge the shapes, the readability at 1x, the contrast with
   the terrain and the consistency with the other assets. Repeat 3 and 4 until it reads well.
5. **Check it**: `pixel diff FILE` shows what changed since `HEAD`. Then run the asset rules:
   `./gradlew jvmTest --tests '*AssetPaletteArchitectureTest' --tests '*AssetOutlineArchitectureTest'`.
6. **Commit** the source under `art/` together with its PNG under `resources/`.

## Importing

`pixel import some.png art/x.aseprite --layer sketch` imports a PNG into a layer (creating the sprite when needed). A
PNG of a free asset pack fails when it uses other colours, listing each one with its closest FamiCube colour:
`--snap` replaces them. Prefer choosing the replacements of the main colours yourself (as the fortress did, see
[buildings](../../../docs/plans/buildings.md)), then `pixel replace` the rest.

## What the tool does not support

Single frame sprites only, without layer groups, blend modes, tags or slices. Opening a source in Aseprite is fine, but
a file saved with any of those can not be read until they are removed.
