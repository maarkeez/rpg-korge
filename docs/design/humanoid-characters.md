# Design brief: humanoid characters

> **Status:** in use. The game displays 16x16 and 32x32 units and npcs side by side, each filling its 32px tile (see
> [Assets](../architecture/assets.md)). The sprites of this brief are drafted in `art-drafts/`, then moved to `art/` once
> reviewed: the princess, the knight and the hero are in the game.
>
> **Summary:** Humanoids (humans, orcs, and later elves, dwarves or goblins) are drawn in the style of *The Legend of
> Zelda: A Link to the Past* (SNES): big heads, a dark outline, three tone materials lit from the top left, and the role
> told by headgear, clothes and held gear. Each character has its own silhouette and pose, on a 32x32 canvas it does not
> have to fill, and a 32x32 portrait.

Read the [`pixel-art` skill](../../.claude/skills/pixel-art/SKILL.md) first: this brief adds to its rules, and wins where
they differ (the size and the folder).

## Scope

| In                                                                         | Out                                                               |
|----------------------------------------------------------------------------|-------------------------------------------------------------------|
| Humanoids: party units, npcs and enemies, their sprite and portrait        | Beasts and creatures (the bat, the bee, the rat, the goat)        |
| The gear a character holds, drawn as part of its sprite                    | Terrain, buildings, item, weapon and ability icons                |
|                                                                            | Animations: every sprite is a single standing frame               |
|                                                                            | Changing the game to display 32x32 sprites                        |

## What we take from A Link to the Past

The reference sheets (soldiers, Link, the townsfolk, the maidens) hold every animation frame and palette of each
character. Only the standing frames and the way they are drawn matter here:

- Big heads on short bodies. Large, simple shapes over detail.
- Black on the outer silhouette only. Inside, shapes are separated by the darkest shade of their own colour.
- Light from the top left, three tones per material, no dithering.
- One drawing, many colours: the green, blue and red soldiers are the same sprite in another palette.
- The role is told at a glance by the silhouette (headgear, hair, build, gear), then by the main colour.

Not taken: its 16 colour palettes (we only have the 64 FamiCube colours), its separate shadow sprites (no shadow is
drawn), its back views, and its animations.

## Format

| Asset    | Size  | File                                          | Framing                                                       |
|----------|-------|-----------------------------------------------|---------------------------------------------------------------|
| Sprite   | 32x32 | `art-drafts/humanoid/<id>.aseprite`           | The whole character and its gear, the feet on the bottom row   |
| Portrait | 32x32 | `art-drafts/humanoid/<id>_portrait.aseprite`  | Head and shoulders, cut by the bottom edge                     |

- `<id>` is the unit or npc id of the cast below, e.g. `knight`. A character that is both an npc and a unit (the
  herbalist) has one sprite.
- **The canvas is a limit, not a size to fill.** Draw the character as big as it is: a child is small, a villager about
  24 rows, an orc almost the whole canvas. Leave the rest transparent.
- **Anchor:** the feet stand on row 31, centred on columns 15-16. The whole canvas is the tile the character stands
  on, drawn at 1x where 16x16 art is drawn at 2x: nothing overlaps the neighbouring tiles. Gear may use the whole
  canvas.
- Sources outside `art/` are not exported. Export the PNG next to the source after every change:
  `tools/pixel-art/pixel export art-drafts/humanoid/<id>.aseprite --out art-drafts/humanoid/<id>.png`.
- The architecture tests do not see `art-drafts/`, so check their rules by hand (see [Checklist](#checklist)).
- Once reviewed, a draft moves to `art/unit/` or `art/npc/`, replacing the 16x16 sprite of its character, and its PNG is
  exported to `resources/` by the tool; the PNGs next to the drafts are removed. The other 16x16 sprites stay until
  they are redrawn.

## Silhouette and pose

Each character has its own silhouette: filled in black, it must still be told apart from the rest of the cast. Get it
from the build (tall and narrow, hunched and broad, small), the headgear and hair, and the gear and how it is held.

- **Facing:** front, or three-quarter toward the viewer's right. Never toward the left: the game will flip a sprite
  when it needs one facing left.
- **Three-quarter:** the face shifts toward the right edge of the head, the far eye sits next to that edge, and a nose,
  a snout or a beard may break the outline on the right. The near side of the body (the viewer's left) shows more.
- **Pose:** free, as long as it is a standing frame: a staff planted beside the body, an axe held upright, a weapon
  over the shoulder, a wide stance, a hunched back.
- **Mirroring:** only helps a front facing character. Draw its left half, `mirror --from left`, then break the
  symmetry (the light, the gear).

## Proportions

These are a guide to keep the cast in one style, not a frame to fit:

| Character            | Height   | Head      | Body                                                     | Draft                         |
|----------------------|----------|-----------|----------------------------------------------------------|-------------------------------|
| Adult human          | ~23 rows | ~12 rows  | ~11 rows, ~14 columns wide arms included                 | `template.aseprite`           |
| Human with headgear  | + the hat or helmet | | The mage: ~20 rows of head and body under a 10 row hat   | `mage.aseprite`               |
| Orc                  | ~30 rows | ~12 rows  | ~19 rows, ~23 columns wide, hunched: the shoulders reach the jaw | `orc.aseprite`        |

The template is a villager, front facing, on its `base` layer only. It shows the proportions and the reading of a
human at this scale; start from it only when it helps:

```text
   0123456789012345    (columns 8-23 and rows 8-31 of the 32x32 canvas)
 0 ................
 1 .....WWWWWW.....
 2 ...WWBBBuuuWW...
 3 ..WBBuuuuuuuuW..
 4 .WBuuuuuuuuuuvW.
 5 .WuuuuuuuuuuuvW.
 6 .WuuvuuuvuuuvvW.
 7 .WuvmmvmmmvmmvW.
 8 WmvmmmmmmmmmtvmW
 9 Wmvmm9mmmm9mtvtW
10 .Wvmm9mmmm9mtvW.
11 .WWmmmmmmmmmtWW.
12 ..WWttmmmmttWW..
13 ..WLWWWWWWWWKW..
14 .WMLLMLLLLLLKKW.
15 .WMLKLLLLLLKLKW.
16 .WMLKLLLLLLKLKW.
17 .WLLKvvvvvvKLKW.
18 .WmmWLLLLLLWmtW.
19 .WWWWLLLLLKWWWW.
20 ...WuuuWWuuvW...
21 ...WuuvWWuuvW...
22 ...WvvvWWvvvW...
23 ...WWWW..WWWW...
```

- Hair rows 1-7, the face rows 7-12, the eyes 2 pixels tall at columns 5 and 10, the ears poking out at the sides.
- A `W` chin line where the head overlaps the body; the torso narrower than the head; the arms split from the torso by
  the clothing's line colour; the legs apart, boots on the last row of colour.

## Species

| Species | Skin                         | Eyes                | Features                                                                           |
|---------|------------------------------|---------------------|------------------------------------------------------------------------------------|
| Human   | The [skin ramps](#skin)      | `9`, 2 pixels tall   | No mouth nor nose on the sprite; a beard or a moustache may cover the chin           |
| Orc     | Orc green: `J` `I` `H` `G`   | `z`, 1 pixel, in the shade of a heavy brow | Lower tusks (`+`), a jaw jutting forward, pointed ears, a topknot. Bigger than a human, the head small for the body |

Add a species here, with its ramps and features, the first time one is drawn.

## Lines and light

- **Silhouette:** `W` (`#151515`) all around, diagonals included. A light trim on the edge (a white beard, a steel
  edge, a sparkle) may replace it, never a dark colour.
- **Overlaps:** `W` where a big shape is drawn in front of another: the chin over the body, a hat's brim over the
  face, a hand over a staff, a pauldron over the arm.
- **Inner lines:** within a shape, folds and seams use the darkest colour of that shape's ramp (the *line* column
  below), not `W`.
- **Light:** from the top left. Highlights go on the top and left of each shape, shades on the right and bottom.
- **Tones:** each material uses its ramp's base, shade and highlight, plus its line. No dithering, no anti-aliasing,
  no gradients of more than three tones.
- **Budget:** at most 20 colours per sprite, `W` and the eyes included. Fewer is better: the mage uses 20, the orc 15.

## Colours

Each material takes one ramp. The characters are those of the [skill's palette](../../.claude/skills/pixel-art/SKILL.md#colours).
Ramps marked *tried* were drawn in a draft and read well; the others are untried suggestions: preview them before
relying on them, and update this table with what works.

The grass terrain is `N` and `O`: do not make them a character's main colour, it would melt into the grass.

### Skin

| Skin      | Line | Shade | Base | Highlight | Notes                                                |
|-----------|------|-------|------|-----------|------------------------------------------------------|
| Light     | `p`  | `t`   | `m`  | `l`       | Tried. `o` as the shade looked like blush: avoid it  |
| Medium    | `w`  | `v`   | `u`  | `t`       | Tried                                                |
| Dark      | `w`  | `q`   | `p`  | `u`       | Tried. `v` as the base turned olive and hid the eyes |
| Orc green | `J`  | `I`   | `H`  | `G`       | Tried                                                |

### Hair

| Hair         | Line | Shade | Base | Highlight | Notes                                 |
|--------------|------|-------|------|-----------|---------------------------------------|
| Brown        | `w`  | `v`   | `u`  | `B`       | Tried                                 |
| Black        | `W`  | `W`   | `w`  | `v`       | Tried, as the orc's topknot           |
| Blond        | `x`  | `B`   | `A`  | `z`       |                                       |
| Ginger       | `q`  | `x`   | `y`  | `A`       | Tried, as the princess's hair. Reads copper, not red |
| Grey, white  | `Y`  | `Z`   | `+`  | `=`       | Tried, as the mage's hair and beard   |

### Clothes (dyes)

As LttP's soldiers, a design keeps each dye on its own ramp, so another variant is a `pixel replace` of four colours.

| Dye     | Line | Shade | Base | Highlight | Notes                             |
|---------|------|-------|------|-----------|-----------------------------------|
| Green   | `K`  | `K`   | `L`  | `M`       | Tried; `K` is both line and shade |
| Blue    | `1`  | `1`   | `2`  | `3`       | Tried, as the knight's tabard     |
| Violet  | `a`  | `b`   | `c`  | `d`       | Tried, as the mage's hat and robe |
| Olive   | `J`  | `I`   | `C`  | `D`       |                                   |
| Red     | `q`  | `p`   | `r`  | `o`       | Tried, as the hero's plume and cross |
| Gold    | `x`  | `B`   | `A`  | `z`       |                                   |
| Indigo  | `9`  | `8`   | `7`  | `6`       |                                   |
| Teal    | `V`  | `P`   | `Q`  | `R`       | Close to the grass: trims only    |
| Magenta | `i`  | `h`   | `g`  | `f`       |                                   |
| Brown   | `w`  | `v`   | `u`  | `t`       | Tried, as trousers                |
| White   | `Y`  | `Z`   | `+`  | `=`       | Tried, as the princess's dress    |
| Black   | `W`  | `W`   | `X`  | `Y`       |                                   |

### Materials

| Material | Line | Shade | Base | Highlight | Notes                                                     |
|----------|------|-------|------|-----------|-----------------------------------------------------------|
| Steel    | `X`  | `Y`   | `Z`  | `+`       | Tried, as the orc's axe and the knight's armour. `=` for a glint |
| Leather  | `w`  | `v`   | `u`  | `t`       | Tried, as the orc's belt and kilt                          |
| Wood     | `w`  | `v`   | `u`  | `t`       | Tried, as a 1 pixel shaft (`u` only)                       |
| Gold     | `x`  | `B`   | `A`  | `z`       | Tried, as the mage's hat band and the princess's trims. Not on ginger hair, whose highlight is `A` |
| Magic    | `V`  | `P`   | `Q`  | `R`, `S`  | Tried, as the mage's orb; loose `S` pixels around it glow   |

## Gear

Gear is the strongest role cue, so it is part of the sprite, drawn as big as reads well:

- A shaft or a blade 1 pixel wide sits between two `W` columns; a hand gripping it is drawn over it, with `W` around.
- Long gear (a staff, an axe, a spear) stands upright beside the body and may span the whole height of the canvas.
- Gear never covers the eyes, and stays inside the canvas.

The mage (a staff planted in front, an orb on top) and the orc (an axe held upright on the far side, its blade at the
edge of the canvas) show two ways; `pixel grid` them to read their pixels.

## Portraits

LttP has no portraits, so this part is ours: the first one, `princess_portrait`, is in the game, still under review. Review it closely, and update this
section with what works.

- The same character as its sprite, closer: same ramps, hair, headgear and clothes, same facing. Gear only when it
  reaches the shoulders (a staff's top, a shield's rim).
- Head and shoulders. The head spans about rows 2-23 and 22 columns; the shoulders fill rows 24-31 and are cut by the
  bottom edge.
- The bottom edge counts as silhouette: its dark pixels must be `W`.
- Eyes are 2 wide and 3 tall, with a light pixel in the top left. Brows in the hair's line colour. The nose is a single
  shade pixel, the mouth 2-3 pixels in the skin's line colour.
- The same light, lines and tones as the sprite, within the same colour budget.

## Layers

The skill's stack:

| Layer       | Holds                                                                 |
|-------------|-----------------------------------------------------------------------|
| `outline`   | The `W` silhouette, the `W` overlaps and the eyes                     |
| `highlight` | The highlights, glints and sparkles                                   |
| `shade`     | The shades and the inner lines (each ramp's line colour)              |
| `base`      | The flat base colour of each shape, under the other layers           |
| `sketch`    | Optional, hidden: the template or a reference to draw over           |

Start a sprite with
`tools/pixel-art/pixel new art-drafts/humanoid/<id>.aseprite --size 32x32 --layer outline --layer highlight --layer shade --layer base`.
Drawing the whole character flat on `base` first, then splitting it, works too: copy `base` onto the other layers
(`copy --from-layer base`), keep only each layer's colours on it (`replace <colour> .`), and turn the shades and
highlights left on `base` into their base colour. The composite must not change: compare `pixel grid` before and
after. The mage and the orc were split this way.

## Checklist

Before showing a draft, check what the tests would:

- [ ] The sprite and the portrait are 32x32.
- [ ] Every dark pixel on the silhouette, diagonals and the image edges included, is `W`.
- [ ] Only opaque pixels, or transparent ones. The tool can not write anything else.
- [ ] The `sketch` layer is hidden, and the PNG next to the source was exported after the last change.

And what makes it fit this brief:

- [ ] The feet are on row 31, centred on columns 15-16.
- [ ] It faces front, or three-quarter toward the viewer's right.
- [ ] Filled in black, its silhouette is told apart from the other drafts.
- [ ] No `W` inside a shape except an overlap; light from the top left on every shape.
- [ ] At most 20 colours (`pixel info` lists them).
- [ ] It reads at 1x and on the grass (`N`, `O`). Put it next to the other drafts with `pixel preview` and compare.

## Cast

The humanoids of the game, each with its reference in the LttP sheets, and two new ones drafted with this brief. Pick
from the suggestions; anything not listed is free.

| Id               | Who                                                                | LttP reference                         | Look                                                                                                    | Gear                         |
|------------------|--------------------------------------------------------------------|----------------------------------------|---------------------------------------------------------------------------------------------------------|------------------------------|
| `hero`           | The player's first unit ("Knight", sword and heal), the commander | None; a closed great helm knight        | Drawn with a portrait (`art/unit/`): three-quarter, wide stance, closed steel helmet with an eye slit and breathing bars, red plume streaming back, dark mail on the upper arm, leather glove and belt. Larger than the knight | A sword raised forward with a gold hilt and sparkles, a steel heater shield with a red cross over the torso |
| `knight`         | A soldier of the realm, the second playable unit                   | The blue Hyrule soldiers                | Drawn (`art/unit/`): front, steel helmet open on the face with cheek guards and a small crest, light skin, steel pauldrons and gauntlets, blue tabard with a leather belt, steel greaves. Broader than a villager. Its portrait, kept from before, shows the helmet closed | None                         |
| `herbalist`      | Healer and poisoner who joins the party ("Herbs to heal, venom to kill") | The maidens' robes, the townsfolk | Olive hooded robe closing the legs, medium skin, a leather satchel at the hip                           | A sprig, or a flask of venom  |
| `viking`         | A northern warrior                                                 | The lumberjacks, for the build          | Horned steel helmet (`+` horns), big ginger beard over the chin, brown fur cloak, broad shoulders         | Sword and a round wooden shield |
| `viking-warrior` | The viking npc who joins the party ("For Valhalla"), sword and cleave | As `viking`                          | The `viking` drawing in the red dye, a steel chest plate                                                | An axe instead of the sword   |
| `merchant`       | The shopkeeper                                                     | Merchants A and B                       | A wide hat or turban, an apron over the gold dye, medium skin                                           | A coin pouch                  |
| `farmer`         | The villager met first, near the meadow                            | The Kakariko townsfolk                  | Straw hat (gold), blue overalls over a white shirt, light skin                                          | A hoe                         |
| `elder`          | The village elder who tells of the Goat Wizard                     | Sahasrahla                              | White hair and a long white beard over the chest, robe closing the legs, bent over                      | A wooden staff                |
| `blacksmith`     | Sells hammers ("Fresh from the forge!")                            | The smiths (draw a human, not a dwarf)  | Bare strong arms, dark skin, black hair under a red bandana, leather apron                              | A hammer                      |
| `alchemist`      | Sells potions and the teleport essence                             | Syrup, the witch brewing                | A tall pointed hat and robe in magenta, light skin                                                      | A potion flask                |
| `mage`           | New, not in the game yet: a classic wizard                         | Sahasrahla, for the beard               | Drafted: three-quarter, violet pointed hat leaning back with a gold band, long white beard, violet robe widening to the hem | A wooden staff with a magic orb |
| `orc`            | New, not in the game yet: a brute warrior                          | None; a classic fantasy orc             | Drafted: three-quarter, orc green, hunched, tusks, yellow eyes under a heavy brow, topknot, steel pauldron, leather kilt | A big axe held upright        |
| `princess`       | In the game: waits at the end of the first path, in place of the mystic | The maidens, Zelda                      | Drawn with a portrait (`art/npc/`): front, light skin, ginger hair down to the waist, silver tiara with a blue gem, white dress with gold trims widening to the hem | None: hands clasped at the waist |

`the-guy` is a test npc and is left out. The elder and the alchemist must not read as the mage: keep the elder bent
and hatless, and the alchemist in magenta with a flask.
