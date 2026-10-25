# Design brief: humanoid characters

> **Status:** in use, rewritten for the top-down view. The game displays 16x16 and 32x32 units and npcs side by side,
> each filling its 32px tile (see [Assets](../architecture/assets.md)). The sprites of this brief are drafted in
> `art-drafts/`, then moved to `art/` once reviewed. The elf is the first character drawn with this version; the rest
> of the cast is to be redrawn with it, see [Redrawing the cast](#redrawing-the-cast).
>
> **Summary:** Humanoids (humans, orcs, elves, and later dwarves or goblins) are drawn in the style of *The Legend of
> Zelda: A Link to the Past* (SNES): seen from above, big heads whose top is hair or headgear, small faces with big
> eyes, short bodies tucked under the head, a dark outline also used inside to split the big shapes, materials shaded
> as volumes with warm shades, and a shadow on the ground. The role is told by headgear, clothes and held gear. Each
> character has its own silhouette and pose, on a 32x32 canvas it does not have to fill, and a 32x32 portrait.

Read the [`pixel-art` skill](../../.claude/skills/pixel-art/SKILL.md) first: this brief adds to its rules, and wins where
they differ (the size and the folder).

## Scope

| In                                                                         | Out                                                               |
|----------------------------------------------------------------------------|-------------------------------------------------------------------|
| Humanoids: party units, npcs and enemies, their sprite and portrait        | Beasts and creatures (the bat, the bee, the rat, the goat)        |
| The gear a character holds, drawn as part of its sprite                    | Terrain, buildings, item, weapon and ability icons                |
|                                                                            | Animations: every sprite is a single standing frame               |

## Look at the reference first

`art-drafts/reference/` holds standing frames of Link, snapped to the FamiCube colours, on 32x32 canvases anchored as our
sprites are: `link-front` (front) and `link-side` (three-quarter, facing right). They are Nintendo's art, from a sheet
ripped by Mister Man for The Spriters Resource: they are there to study, never to copy into an asset. They are not
under `art/`, so they are never exported to the game.

Before drawing, `pixel preview` them and `pixel grid` them, and read how they are built: the rest of this brief describes
them. A drawing that follows every rule below but does not look like them is wrong; one that looks like them and breaks
a rule is worth discussing.

```text
link-front, columns 8-23, rows 10-30          the template (a villager), columns 8-23, rows 10-31

   0123456789012345                              0123456789012345
10 ......WWWW......   cap: the top of the head   ......WWWW......   hair: the top of the head
11 .....WHHGHWW....                              ....WWBBuuWW....
12 ....WHHGGGGHW...                              ...WBBBuuuuvW...
13 .W.WWBHHGHBWHWW.   ears out at the sides      ...WBBuuuuuvvW..
14 WmWkWWBBBBWWHWmW                              .WWWBuuuuuuuvWW.
15 WmWkkkWWWWkkWWmW   hair under the cap         WmWuuvuuuuvuvWmW   ears out at the sides
16 WumWkkkkkkkkWmuW                              WmWuvuvuuvuvvWuW
17 WuWkWWWWWWWWkWuW   fringe: W on the forehead  WuWvWWWWWWWWvWuW   fringe: W on the forehead
18 .WuWu==uu==uWuW.   eyes: whites ...           .WuWu==uu==uWuW.   eyes: whites ...
19 ..Wuu=WmmW=uuW..   ... and pupils             ..Wuu=WlmW=uuW..   ... and pupils, lit nose
20 ..WWumWmmWmuWW..   lower lids, cheeks         ..WWumWmmWmuWW..
21 .WMNWummmmuWNMW.   chin over the shoulders    .WMLWummmmuWLKW.   chin over the shoulders
22 WpWNNWWuuWWNNWpW                              WMWLLWWuuWWLLWLW
23 WppWNMNWWNMNWppW   arms split by W            WMLWMMLWWLLKWLKW   arms split by W
24 WyWWNNMMMMMNWWyW                              WmWWLMMLLLLKWWmW
25 WyyWNNNMMNNNWyyW   hands at the belt          WmmWLLMLLLLKWmuW   hands at the belt
26 WyWNzzWzzWzzNWyW   belt                       WmWLuuuBuuuvKWuW   belt
27 .WWWNNzzzzNNWWW.                              .WWWLMLLLLLKWWW.
28 ..WrWWNNNNWWrW..   feet peek out              ..WWWLLLLLKWWW..
29 ...WBrWWWWrBW...                              ...WuvWWWWuvW...   feet peek out ...
30 ....WWW..WWW....                              ..WWuvWWWWvvWW..   ... on the shadow
31                                               ...WWWWWWWWWW...
```

## What we take from A Link to the Past

- **The camera looks down** on the characters, at about 45 degrees: the top of the head, of the shoulders and of the
  gear show; the face and the body are foreshortened. See [Camera](#camera).
- **Big heads on short bodies**, the body partly hidden under the head. Large, simple shapes over detail.
- **Expressive faces from few pixels:** eyes with whites and pupils, tucked under the fringe, in a small face. See
  [Face](#face).
- **A dark outline, inside too.** The silhouette is outlined, and the same dark splits the big shapes from each other:
  the hairline on the forehead, the eyes, the chin over the body, the arms from the torso, the belt.
- **Shapes shaded as volumes**, lit from above and the left, with shades warmer than their base, never greyer.
- **A shadow on the ground** under the feet.
- One drawing, many colours: the green, blue and red soldiers are the same sprite in another palette.
- The role is told at a glance by the silhouette (headgear, hair, build, gear), then by the main colour.

Not taken: its 16 colour palettes (we only have the 64 FamiCube colours), its semi-transparent shadows (ours is `W`),
its back views, and its animations.

## Format

| Asset    | Size  | File                                          | Framing                                                       |
|----------|-------|-----------------------------------------------|---------------------------------------------------------------|
| Sprite   | 32x32 | `art-drafts/humanoid/<id>.aseprite`           | The whole character, its gear and its shadow                  |
| Portrait | 32x32 | `art-drafts/humanoid/<id>_portrait.aseprite`  | Head and shoulders, cut by the bottom edge                    |

- `<id>` is the unit or npc id of the cast below, e.g. `knight`. A character that is both an npc and a unit (the
  herbalist) has one sprite.
- **The canvas is a limit, not a size to fill.** Draw the character as big as it is: a child is small, a villager about
  21 rows, an orc almost the whole canvas. Leave the rest transparent.
- **Anchor:** the shadow's bottom row is row 31; the feet stand on row 30, centred on columns 15-16. The whole canvas
  is the tile the character stands on, drawn at 1x where 16x16 art is drawn at 2x: nothing overlaps the neighbouring
  tiles. Gear may use the whole canvas.
- Sources outside `art/` are not exported. Export the PNG next to the source after every change:
  `tools/pixel-art/pixel export art-drafts/humanoid/<id>.aseprite --out art-drafts/humanoid/<id>.png`.
- The architecture tests do not see `art-drafts/`, so check their rules by hand (see [Checklist](#checklist)).
- Once reviewed, a draft moves to `art/unit/` or `art/npc/`, replacing the sprite of its character, and its PNG is
  exported to `resources/` by the tool; the PNGs next to the drafts are removed. The other 16x16 sprites stay until
  they are redrawn.

## Camera

The view from above is what makes a sprite read as A Link to the Past rather than as a front view. On a front facing
character:

- **The head is mostly its top.** Hair or headgear fills the top half of the head or more, as a dome with its light on
  the top left. The face is only the bottom third of the head: about 4 rows.
- **The eyes sit low**, right under the fringe, about two thirds down the head, with 2-3 rows of cheeks and chin under
  them. A face with skin above the eyes, or eyes in the middle of the head, is a front view.
- **The chin overlaps the shoulders**: no neck shows. A `W` line, broken by the collar, marks it.
- **The body is shorter than the head**: about 5 rows from the shoulders to the belt, then 2 rows of hem or legs. The
  shoulders and the top of the arms show from above, a little wider than the torso.
- **The arms hang at the sides**, split from the torso by `W`, the hands at the belt.
- **The feet just peek out** under the hem: 2 rows, standing on the shadow.
- **The shadow** is a flat `W` ellipse under the feet, on its own `shadow` layer: row 31, and row 30 around the feet, 1-2
  columns wider than the feet on each side.
- The ears, when they show, stick out sideways at the eyes' height. Gear held upright shows its top end bigger than its
  bottom end, and its top face (a shield's rim, a hat's crown).

Three-quarter, toward the viewer's right (see `link-side`): the same camera, the face shifted to the right half of the
head, the near eye (the viewer's left one) next to the middle and the far eye at the right edge, the hair or headgear
covering the left side of the head, the near shoulder bigger. A nose may break the outline on the right.

## Silhouette and pose

Each character has its own silhouette: filled in black, it must still be told apart from the rest of the cast. Get it
from the build (tall and narrow, hunched and broad, small), the headgear and hair, and the gear and how it is held.

- **Facing:** front, or three-quarter toward the viewer's right. Never toward the left: the game will flip a sprite
  when it needs one facing left.
- **Pose:** free, as long as it is a standing frame: a staff planted beside the body, an axe held upright, a weapon
  over the shoulder, a wide stance, a hunched back.
- **Mirroring** helps a front facing character, but leaves it stiff: draw its left half, `mirror --from left`, then
  break the symmetry everywhere it is lit or held differently: the right side in shade, the hair parted, the gear, one
  arm bent, the pupils looking aside.
- **Do not start from the last character drawn**: start from the reference and the template, so the cast does not drift
  toward one drawing. Look at the characters already drawn with this version (the elf) only to match their scale and
  finish.

## Proportions

These are a guide to keep the cast in one style, not a frame to fit. Heights do not count the shadow row, widths do
not count long ears (an elf's) nor gear.

| Character            | Height   | Head                              | Body                                                       |
|----------------------|----------|-----------------------------------|------------------------------------------------------------|
| Adult human          | ~21 rows | ~12 rows: ~7 of hair, ~4 of face  | ~9 rows, ~16 columns wide arms and human ears included, 2 of feet |
| Human with headgear  | + the hat or helmet above the hair line |            |                                                            |
| Child                | ~16 rows | ~10 rows                          | ~6 rows                                                    |
| Orc                  | ~28 rows | ~11 rows, small for the body      | ~17 rows, ~23 columns wide, hunched: the shoulders reach the jaw |

`art-drafts/humanoid/template.aseprite` is a villager, front facing, on its `base` layer only, drawn over `link-front`
(see [Look at the reference first](#look-at-the-reference-first)). It shows the proportions and the reading of a human
at this scale; draw over it as a hidden `sketch`, and change the build, the hair and the clothes for every character.

## Face

The face is where a sprite gets its life. On the sprite:

- **Eyes:** 2 wide and 2 tall each, 2 columns apart. The top row is two whites (`=`); the bottom row a white and the
  pupil, the pupils on the inner side (looking at the viewer). A `W` pixel under each pupil is the lower lid.
- **The fringe** is a `W` line across the forehead, right above the eyes, with strands of hair coming down. The hair
  frames the face at its sides down to the cheeks.
- **The face is in the shade of the hair:** its sides and the row under the fringe are the skin's shade, only the
  middle (the nose, the cheeks, the chin) is the base, with one highlight pixel on the nose.
- **Expression:** the pupils move (both to one side looks away, up looks proud), a brow in `W` or the hair's line over
  the top of an eye frowns, half closed eyes (whites on one row only) are tired or smug, a mouth (1-2 pixels of the
  skin's line) opens it or is shown under a beard. Give every character one.
- The skin, the eyes' colour and the features of each species are in [Species](#species).

## Species

| Species | Skin                         | Pupils              | Features                                                                           |
|---------|------------------------------|---------------------|------------------------------------------------------------------------------------|
| Human   | The [skin ramps](#skin)      | `W`                 | Small ears sticking out at the eyes' height. A beard or a moustache may cover the chin |
| Orc     | Orc green: `J` `I` `H` `G`   | `z`, 1 pixel each, no whites, in the shade of a heavy `W` brow | Lower tusks (`+`), a jaw jutting forward, pointed ears, a topknot. Bigger than a human, the head small for the body |
| Elf     | The light [skin ramp](#skin) | `2`                 | Long pointed ears: their base at the sides of the head at the eyes' height, rising diagonally outward, 5-6 pixels long, the tip 4-5 rows above the base (about the hair's middle). A light top edge (`l` on the lit ear, `m` on the other), `u` underneath; in three-quarter view the far ear is `u`. Human sized, slender: 1 pixel sleeves |

Add a species here, with its ramps and features, the first time one is drawn.

## Lines and light

- **Silhouette:** `W` (`#151515`) all around, diagonals included. A light trim on the edge (a white beard, a steel
  edge, a sparkle) may replace it, never a dark colour.
- **`W` inside** splits the big shapes, as in the reference: the fringe on the forehead, the eyes and their lower lids,
  the chin over the body, the arms from the torso, the collar, the belt's edges, a hat's brim, a hand over a staff, a
  pauldron over the arm.
- **Inner lines:** within one shape, folds, seams and hair strands use the darkest colour of that shape's ramp (the
  *line* column below), not `W`.
- **Light from above and the left, shaded as volumes.** Each shape is a volume (a dome of hair, a round shoulder, a
  chest, a leg), not a flat area: its highlight is a patch on its top left, its base the middle, its shade the bottom
  and the right. A highlight column down the left edge and a shade column down the right edge is not shading.
- **Cast shades:** where a shape hangs over another, the one under is in shade: the face under the fringe, the chest
  under the chin, the torso under the arms, the hem under the belt, the inner side of the legs.
- **Warm shades:** a shade is darker *and* more saturated or warmer than its base (skin shades to orange brown, not to
  grey pink). A shade too close to its base reads as no light: one clear value step at least.
- **Tones:** each material uses its ramp's base, shade and highlight, plus its line. No dithering, no anti-aliasing,
  no gradients of more than three tones. Ramps may share colours (the skin's shade and the brown hair's base), as `W`
  or a line splits them.
- **Budget:** at most 20 colours per sprite, `W` and the eyes included. Fewer is better: the template uses 10.

## Colours

Each material takes one ramp. The characters are those of the [skill's palette](../../.claude/skills/pixel-art/SKILL.md#colours).
Ramps marked *tried* were drawn and read well; the others are untried suggestions: preview them before relying on them,
and update this table with what works.

The grass terrain is `N` and `O`: do not make them a character's main colour, it would melt into the grass.

### Skin

| Skin      | Line | Shade | Base | Highlight | Notes                                                |
|-----------|------|-------|------|-----------|------------------------------------------------------|
| Light     | `x`  | `u`   | `m`  | `l`       | Tried, in the template: the shade of the reference. `t` as the shade was grey and flat, `o` looked like blush |
| Medium    | `w`  | `x`   | `u`  | `m`       |                                                      |
| Dark      | `w`  | `q`   | `p`  | `u`       | Tried. `v` as the base turned olive and hid the eyes |
| Orc green | `J`  | `I`   | `H`  | `G`       | Tried                                                |

### Hair

| Hair         | Line | Shade | Base | Highlight | Notes                                 |
|--------------|------|-------|------|-----------|---------------------------------------|
| Brown        | `w`  | `v`   | `u`  | `B`       | Tried, in the template                |
| Black        | `W`  | `W`   | `w`  | `v`       | Tried, as the orc's topknot           |
| Blond        | `x`  | `B`   | `A`  | `z`       | Tried, as the elf's hair. Use `x` strands, `B` alone is too close to `A` |
| Ginger       | `q`  | `x`   | `y`  | `A`       | Tried, as the princess's hair. Reads copper, not red |
| Grey, white  | `Y`  | `Z`   | `+`  | `=`       | Tried, as the mage's hair and beard   |

### Clothes (dyes)

As LttP's soldiers, a design keeps each dye on its own ramp, so another variant is a `pixel replace` of four colours.

| Dye     | Line | Shade | Base | Highlight | Notes                             |
|---------|------|-------|------|-----------|-----------------------------------|
| Green   | `K`  | `K`   | `L`  | `M`       | Tried, in the template; `K` is both line and shade |
| Blue    | `1`  | `1`   | `2`  | `3`       | Tried, as the knight's tabard     |
| Violet  | `a`  | `b`   | `c`  | `d`       | Tried, as the mage's hat and robe |
| Olive   | `J`  | `I`   | `C`  | `D`       |                                   |
| Red     | `q`  | `p`   | `r`  | `o`       | Tried, as the hero's plume and cross |
| Gold    | `x`  | `B`   | `A`  | `z`       |                                   |
| Indigo  | `9`  | `8`   | `7`  | `6`       |                                   |
| Teal    | `V`  | `P`   | `Q`  | `R`       | Close to the grass: trims only    |
| Magenta | `i`  | `h`   | `g`  | `f`       |                                   |
| Brown   | `w`  | `v`   | `u`  | `t`       | Tried, as trousers and boots      |
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

Gear is the strongest role cue, so it is part of the sprite, drawn as big as reads well, but never bigger than the
character in the eye:

- A shaft or a blade 1 pixel wide sits between two `W` columns; a hand gripping it is drawn over it, with `W` around.
- **Which side:** a front facing character holds gear on the viewer's right (its left hand), unless the pose says
  otherwise; a three-quarter character on the far side (also the viewer's right), partly hidden by the body.
- **How big:** long gear (a staff, an axe, a spear, a bow) stands upright beside the body, its top at most 3-4 rows
  above the head and its bottom on the ground or the shadow. A sword or a bow held in the hand is about the body's
  height. Seen from above, its top end is bigger than its bottom end.
- **Weight:** gear is as easy to read as the character but not louder: give it a body (a bow 2 pixels thick at its
  middle, curving in steps, a staff's head 3-4 pixels wide) instead of a long 1 pixel line, and keep it close to the
  body, the hand on it, without bending an arm away from the body just to reach it.
- **Strings and trims** (a bow string, a banner's cord) are 1 pixel of a light colour (`Z`, `+`), outside the gear's
  `W`, without their own `W`: they are light, so the outline rule allows them on the silhouette.
- Gear never covers the eyes, and stays inside the canvas. Long ears and gear on the same side compete for the same
  columns: put the gear on the other side, or lower the hand so the gear passes under the ear.

## Portraits

LttP has no portraits, so this part is ours: the first one, `princess_portrait`, is in the game, still under review.
Review it closely, and update this section with what works.

- The same character as its sprite, closer: same ramps, hair, headgear and clothes, same facing, same expression. Gear
  only when it reaches the shoulders (a staff's top, a shield's rim).
- Seen from the front, not from above: a portrait is a face to talk to.
- Head and shoulders. The head spans about rows 2-23 and 22 columns; the shoulders fill rows 24-31 and are cut by the
  bottom edge.
- The bottom edge counts as silhouette: its dark pixels must be `W`.
- Eyes are 2 wide and 3 tall: a column of whites (`=`) and a column of pupil (the species' colour), the pupil on the
  side the character looks, as on its sprite (looking at the viewer: both pupils on the inner side). No lower lid.
  Brows 2 pixels in the hair's line colour, one row above. The nose is a single shade pixel (or a highlight pixel on
  its top), the mouth 2-3 pixels in the skin's line colour. The elf, looking right:
  ```text
  xx....xx    brows
  =2....=2    eyes: whites, then the pupils, on the right
  =2....=2
  =2....=2
  ```
- **A lit face:** the face is mostly the skin's base. The shade only on the side away from the light (the right), one
  row under the hair's edge, and under the chin and the nose: at most a third of the skin. A face mostly in shade looks
  dirty, not lit.
- The same light, lines and tones as the sprite, within the same colour budget.
- A portrait has no `shadow` layer.

## Layers

The skill's stack, plus the shadow:

| Layer       | Holds                                                                 |
|-------------|-----------------------------------------------------------------------|
| `outline`   | The `W` silhouette, the `W` lines inside and the eyes                 |
| `highlight` | The highlights, glints and sparkles                                   |
| `shade`     | The shades, cast shades and the inner lines (each ramp's line colour) |
| `base`      | The flat base colour of each shape, under the other layers            |
| `shadow`    | The `W` shadow on the ground, under everything                        |
| `sketch`    | Optional, hidden: the template or a reference to draw over            |

Start a sprite with
`tools/pixel-art/pixel new art-drafts/humanoid/<id>.aseprite --size 32x32 --layer outline --layer highlight --layer shade --layer base --layer shadow --layer sketch`,
then `copy --from art-drafts/humanoid/template.aseprite --from-layer base --region 0,0,32x32 --layer sketch --at 0,0`
(or from a reference) and `layer hide sketch`.
Drawing the whole character flat on `base` first, then splitting it, works too: copy `base` onto the other layers
(`copy --from-layer base`), keep only each layer's colours on it (`replace <colour> .`), and turn the shades and
highlights left on `base` into their base colour. A colour shared by two ramps (`u` is the light skin's shade and the
leather's base) belongs to a different layer in each region: clear it by region (`rect ... . --filled`, `set`) where
`replace` would take both. The composite must not change: compare `pixel grid` before and after.

## Review

The rules are easy to pass with a drawing that still does not look like A Link to the Past. Before showing a draft,
review it against the reference:

1. `pixel preview` the draft and `art-drafts/reference/link-front.png` (or `link-side`) at the same scale, and read both.
2. Write down, for each, what differs from the reference: the **camera** (how much of the head is its top, where the
   eyes sit, how short the body is, the shadow), the **face** (whites, pupils, fringe, shade, expression), the
   **light** (volumes or stripes, cast shades, warm shades), the **lines** (`W` splitting the big shapes).
3. Fix every difference that is not a deliberate choice of the character, and repeat.
4. Then look at it at 1x, on the grass, next to the rest of the cast.

## Checklist

What the tests would check:

- [ ] The sprite and the portrait are 32x32.
- [ ] Every dark pixel on the silhouette, diagonals and the image edges included, is `W`.
- [ ] Only opaque pixels, or transparent ones. The tool can not write anything else.
- [ ] The `sketch` layer is hidden, and the PNG next to the source was exported after the last change.

What makes it fit this brief:

- [ ] Seen from above: hair or headgear fills the top half of the head, the eyes sit right under the fringe, the body
  is shorter than the head, the feet peek out on a `W` shadow on rows 30-31.
- [ ] The feet are on row 30, centred on columns 15-16; the shadow's bottom row is 31.
- [ ] It faces front, or three-quarter toward the viewer's right.
- [ ] Eyes with whites and pupils, under a `W` fringe, in a face shaded by the hair, with an expression.
- [ ] `W` splits the big shapes; folds use each ramp's line.
- [ ] Every shape is shaded as a volume, with warm shades a clear step below their base.
- [ ] Filled in black, its silhouette is told apart from the rest of the cast.
- [ ] At most 20 colours (`pixel info` lists them).
- [ ] The [review](#review) against the reference was done, and its differences fixed or chosen.

## Redrawing the cast

Every humanoid of the game is to be drawn with this version, one character at a time, when asked. Only the drawing
changes: each keeps the design of its row in the [Cast](#cast) (look, gear, ramps), its id and its files.

| Character                                   | Drawn now                                           | Source                                        |
|---------------------------------------------|-----------------------------------------------------|-----------------------------------------------|
| `elf`                                       | With this version                                   | `art/npc/elf.aseprite`, `elf_portrait`        |
| `princess`                                  | 32x32, with the first version                       | `art/npc/princess.aseprite`, `princess_portrait` |
| `knight`, `hero`                            | 32x32, with the first version                       | `art/unit/<id>.aseprite`, `<id>_portrait`     |
| `mage`, `orc`                               | Drafts with the first version, not in the game      | `art-drafts/humanoid/<id>.aseprite`           |
| `herbalist`, `viking`, `viking-warrior`     | 16x16, before this brief                            | `art/unit/<id>.aseprite`, `<id>_portrait`     |
| `merchant`, `farmer`, `elder`, `blacksmith`, `alchemist` | 16x16, before this brief               | `art/npc/<id>.aseprite`, `<id>_portrait`      |

For each character:

1. Read the [`pixel-art` skill](../../.claude/skills/pixel-art/SKILL.md) and this whole brief, then
   [look at the reference](#look-at-the-reference-first) and at the elf.
2. Look at the character's current sprite and portrait for its design only (`pixel preview`), never as a starting
   point: start a draft in `art-drafts/humanoid/<id>.aseprite` and `<id>_portrait.aseprite` with the template as its
   hidden `sketch` (see [Layers](#layers)).
3. Draw it, do the [review](#review) for real, and go through the [checklist](#checklist).
4. Replace the game's sources by the drafts (`art/unit/` or `art/npc/`, the same file names), `pixel export` both so
   their PNGs under `resources/` follow, remove the draft PNGs, and run
   `./gradlew jvmTest --tests '*AssetPaletteArchitectureTest' --tests '*AssetOutlineArchitectureTest'`.
5. Update the character's row in the cast and in the table above ("With this version"), and write down in this brief
   what was learnt: a ramp tried, a rule that was unclear or did not hold.

## Cast

The humanoids of the game, each with its reference in the LttP sheets, and two new ones drafted with this brief. Pick
from the suggestions; anything not listed is free.

| Id               | Who                                                                | LttP reference                         | Look                                                                                                    | Gear                         |
|------------------|--------------------------------------------------------------------|----------------------------------------|---------------------------------------------------------------------------------------------------------|------------------------------|
| `hero`           | The player's first unit ("Knight", sword and heal), the commander | None; a closed great helm knight        | Drawn with the first version, with a portrait (`art/unit/`): three-quarter, wide stance, closed steel helmet with an eye slit and breathing bars, red plume streaming back, dark mail on the upper arm, leather glove and belt. Larger than the knight | A sword raised forward with a gold hilt and sparkles, a steel heater shield with a red cross over the torso |
| `knight`         | A soldier of the realm, the second playable unit                   | The blue Hyrule soldiers                | Drawn with the first version (`art/unit/`): front, steel helmet open on the face with cheek guards and a small crest, light skin, steel pauldrons and gauntlets, blue tabard with a leather belt, steel greaves. Broader than a villager. Its portrait, kept from before, shows the helmet closed | None                         |
| `herbalist`      | Healer and poisoner who joins the party ("Herbs to heal, venom to kill") | The maidens' robes, the townsfolk | Olive hooded robe closing the legs, medium skin, a leather satchel at the hip                           | A sprig, or a flask of venom  |
| `viking`         | A northern warrior                                                 | The lumberjacks, for the build          | Horned steel helmet (`+` horns), big ginger beard over the chin, brown fur cloak, broad shoulders         | Sword and a round wooden shield |
| `viking-warrior` | The viking npc who joins the party ("For Valhalla"), sword and cleave | As `viking`                          | The `viking` drawing in the red dye, a steel chest plate                                                | An axe instead of the sword   |
| `merchant`       | The shopkeeper                                                     | Merchants A and B                       | A wide hat or turban, an apron over the gold dye, medium skin                                           | A coin pouch                  |
| `farmer`         | The villager met first, near the meadow                            | The Kakariko townsfolk                  | Straw hat (gold), blue overalls over a white shirt, light skin                                          | A hoe                         |
| `elder`          | The village elder who tells of the Goat Wizard                     | Sahasrahla                              | White hair and a long white beard over the chest, robe closing the legs, bent over                      | A wooden staff                |
| `blacksmith`     | Sells hammers ("Fresh from the forge!")                            | The smiths (draw a human, not a dwarf)  | Bare strong arms, dark skin, black hair under a red bandana, leather apron                              | A hammer                      |
| `alchemist`      | Sells potions and the teleport essence                             | Syrup, the witch brewing                | A tall pointed hat and robe in magenta, light skin                                                      | A potion flask                |
| `mage`           | New, not in the game yet: a classic wizard                         | Sahasrahla, for the beard               | Drafted with the first version: three-quarter, violet pointed hat leaning back with a gold band, long white beard, violet robe widening to the hem | A wooden staff with a magic orb |
| `orc`            | New, not in the game yet: a brute warrior                          | None; a classic fantasy orc             | Drafted with the first version: three-quarter, orc green, hunched, tusks, yellow eyes under a heavy brow, topknot, steel pauldron, leather kilt | A big axe held upright        |
| `elf`            | In the game: a forest archer watching over the village             | None; a classic fantasy elf            | Drawn with this version, with a portrait (`art/npc/`): front, the pupils looking right (watching), long pointed ears, blond hair to the jaw under a circlet of green leaves, green tunic with 1 pixel sleeves and a leather belt, brown trousers and boots. The portrait adds a quiver strap across the chest | A longbow on the viewer's right, in the bent left hand, with a leather grip and a `Z` string. To improve on a redraw: the bow is a 1 pixel line taller than the elf, and the arm bends away to hold it (see [Gear](#gear)) |
| `princess`       | In the game: waits at the end of the first path, in place of the mystic | The maidens, Zelda                      | Drawn with the first version, with a portrait (`art/npc/`): front, light skin, ginger hair down to the waist, silver tiara with a blue gem, white dress with gold trims widening to the hem | None: hands clasped at the waist |

`the-guy` is a test npc and is left out. The elder and the alchemist must not read as the mage: keep the elder bent
and hatless, and the alchemist in magenta with a flask.
