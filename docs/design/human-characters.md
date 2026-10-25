# Design brief: human characters

> **Status:** drafts only. The game displays 16x16 units and npcs; the 16x24 sprites of this brief are drawn in
> `art-drafts/`, to validate the brief and the [`pixel-art` skill](../../.claude/skills/pixel-art/SKILL.md) before the
> game is changed to display them.
>
> **Summary:** Humans are drawn in the style of *The Legend of Zelda: A Link to the Past* (SNES): a big head on a short
> body, front facing, a dark outline, three tone materials lit from the top left, and the role told by headgear, clothes
> and held gear. Each human has a 16x24 sprite and a 32x32 portrait.

Read the [`pixel-art` skill](../../.claude/skills/pixel-art/SKILL.md) first: this brief adds to its rules, and wins where
they differ (the size and the folder).

## Scope

| In                                                                    | Out                                                             |
|-----------------------------------------------------------------------|-----------------------------------------------------------------|
| Humans: party units, npcs and human enemies, their sprite and portrait | Creatures, terrain, buildings, item, weapon and ability icons   |
| The gear a human holds, drawn as part of its sprite                   | Animations: every sprite is a single standing frame             |
|                                                                       | Changing the game to display 16x24 sprites                      |

## What we take from A Link to the Past

The reference sheets (soldiers, Link, the townsfolk, the maidens) hold every animation frame and palette of each
character. Only the standing front frame (Link's "down" idle) and the way it is drawn matter here:

- A big head, about half of the height, on a short body. Large, simple shapes over detail.
- Black on the outer silhouette only. Inside, shapes are separated by the darkest shade of their own colour.
- Light from the top left, three tones per material, no dithering.
- One drawing, many colours: the green, blue and red soldiers are the same sprite in another palette.
- The role is told at a glance by headgear (helmet, hood, hat, hair), the main colour, and what the hands hold.

Not taken: its 16 colour palettes (we only have the 64 FamiCube colours), its separate shadow sprites (no shadow is
drawn), its side and back views, and its animations.

## Format

| Asset    | Size  | File                                       | Framing                                                     |
|----------|-------|--------------------------------------------|-------------------------------------------------------------|
| Sprite   | 16x24 | `art-drafts/human/<id>.aseprite`           | The whole body, front facing, the feet on the bottom row     |
| Portrait | 32x32 | `art-drafts/human/<id>_portrait.aseprite`  | Head and shoulders, front facing, cut by the bottom edge     |

- `<id>` is the unit or npc id of the cast below, e.g. `knight`. A human that is both an npc and a unit (the herbalist)
  has one sprite.
- The bottom 16 rows of a sprite stand on its tile; the top 8 rows overlap the tile above. Keep row 0 empty, except for
  tall headgear, a raised weapon or a staff.
- Sources outside `art/` are not exported. Export the PNG next to the source after every change:
  `tools/pixel-art/pixel export art-drafts/human/<id>.aseprite --out art-drafts/human/<id>.png`.
- The architecture tests do not see `art-drafts/`, so check their rules by hand (see [Checklist](#checklist)).
- The current 16x16 sprites and portraits under `art/` stay as they are until the game displays 16x24 sprites; then the
  drafts move to `art/unit/` or `art/npc/`.

## Proportions

Every sprite starts from this template (`art-drafts/human/template.aseprite`), a villager with brown hair, light skin, a
green tunic, brown trousers and boots. It and the knight below were drawn on their `base` layer only, to try the
proportions: draw new sprites on the layers of [Layers](#layers).

```text
   0123456789012345
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

| Part   | Rows  | Columns                                    | Notes                                                       |
|--------|-------|--------------------------------------------|-------------------------------------------------------------|
| Hair   | 1-7   | 1-14                                       | Row 7 is a ragged fringe over the forehead                  |
| Face   | 7-12  | 3-12                                       | Eyes at columns 5 and 10, rows 9-10; ears at columns 1 and 14 |
| Chin   | 13    | 4-11                                       | A `W` line: the head overlaps the body                      |
| Torso  | 13-19 | 5-10, belt on row 17                       | Narrower than the head                                       |
| Arms   | 14-18 | 2-3 and 12-13, hands on row 18             | Split from the torso by the clothing's line colour           |
| Legs   | 20-22 | 4-6 and 9-11                               | Boots on row 22                                              |
| Feet   | 23    |                                            | The bottom outline                                           |

Keep the head at 12 rows and the body at 11 for every adult, so the cast reads as one family; change the silhouette
with headgear, hair, clothes and gear instead. A robe or a dress may close the gap between the legs. A child (none in the
cast yet) keeps the head and drops 3 rows of body.

## Lines and light

- **Silhouette:** `W` (`#151515`) all around, diagonals included. A light trim on the edge (a white beard, a steel
  rim) may replace it, never a dark colour.
- **Overlaps:** `W` where a big shape is drawn in front of another: the chin over the body, a shield over the arm, an arm
  over the torso when it crosses it.
- **Inner lines:** within a shape, folds and seams use the darkest colour of that shape's ramp (the *line* column
  below), not `W`.
- **Eyes:** two pixels tall, one wide, `9` (`#211640`), on every skin tone. No mouth and no nose on a sprite.
- **Light:** from the top left. Highlights go on the top and left of each shape, shades on the right and bottom.
  Draw the left half, `mirror --from left`, then move the light: that breaks the symmetry on purpose.
- **Tones:** each material uses its ramp's base, shade and highlight, plus its line. No dithering, no anti-aliasing,
  no gradients of more than three tones.
- **Budget:** at most 14 colours per sprite, `W` and the eyes included. Fewer is better.

## Colours

Each material takes one ramp. The characters are those of the [skill's palette](../../.claude/skills/pixel-art/SKILL.md#colours).
Ramps marked *tried* were drawn in the template or the knight and read well; the others are untried suggestions:
preview them before relying on them, and update this table with what works.

### Skin

| Skin   | Line | Shade | Base | Highlight | Notes                                               |
|--------|------|-------|------|-----------|-----------------------------------------------------|
| Light  | `p`  | `t`   | `m`  | `l`       | Tried. `o` as the shade looked like blush: avoid it |
| Medium | `w`  | `v`   | `u`  | `t`       | Tried                                               |
| Dark   | `w`  | `q`   | `p`  | `u`       | Tried. `v` as the base turned olive and hid the eyes |

### Hair

| Hair         | Line | Shade | Base | Highlight | Notes   |
|--------------|------|-------|------|-----------|---------|
| Brown        | `w`  | `v`   | `u`  | `B`       | Tried   |
| Black        | `W`  | `W`   | `w`  | `v`       |         |
| Blond        | `x`  | `B`   | `A`  | `z`       |         |
| Ginger       | `q`  | `x`   | `y`  | `A`       |         |
| Grey, white  | `X`  | `Y`   | `Z`  | `+`       | The elder's hair and beard |

### Clothes (dyes)

As LttP's soldiers, a design keeps each dye on its own ramp, so another variant is a `pixel replace` of four colours.

| Dye     | Line | Shade | Base | Highlight | Notes                             |
|---------|------|-------|------|-----------|-----------------------------------|
| Green   | `K`  | `K`   | `L`  | `M`       | Tried; `K` is both line and shade |
| Blue    | `1`  | `1`   | `2`  | `3`       | Tried                             |
| Olive   | `J`  | `I`   | `C`  | `D`       |                                   |
| Red     | `q`  | `p`   | `r`  | `o`       |                                   |
| Gold    | `x`  | `B`   | `A`  | `z`       |                                   |
| Violet  | `a`  | `b`   | `c`  | `d`       |                                   |
| Indigo  | `9`  | `8`   | `7`  | `6`       |                                   |
| Teal    | `V`  | `P`   | `Q`  | `R`       |                                   |
| Magenta | `i`  | `h`   | `g`  | `f`       |                                   |
| Brown   | `w`  | `v`   | `u`  | `t`       | Tried, as trousers                |
| White   | `Y`  | `Z`   | `+`  | `=`       |                                   |
| Black   | `W`  | `W`   | `X`  | `Y`       |                                   |

### Materials

| Material | Line | Shade | Base | Highlight | Notes                                          |
|----------|------|-------|------|-----------|------------------------------------------------|
| Steel    | `X`  | `Y`   | `Z`  | `+`       | Tried. `=` for a single glint, e.g. a blade's tip |
| Leather  | `w`  | `v`   | `u`  | `B`       | Tried, as belts and boots                      |
| Wood     | `w`  | `v`   | `u`  | `t`       | Staffs, shafts, a round shield                 |
| Gold     | `x`  | `B`   | `A`  | `z`       | Tried, as an emblem. Trims, crowns, coins      |

## Gear

Gear in the hands is the strongest role cue at this size, so it is part of the sprite. This knight shows how a sword
and a shield fit (`art-drafts/human/knight.aseprite`):

```text
   0123456789012345
 0 ......WWWW......
 1 ....WWZ++ZWW....
 2 ...WZ+ZZZZZYW...
 3 ..WZ+ZZZZZZZYW..
 4 .WWZZZZZZZZZYW..
 5 W=WZZZZZZZZZYW..
 6 W+WYYYYYYYYYXW..
 7 W+WXWWWWWWWWXW..
 8 W+WYWmmmmmmWXW..
 9 W+WYWm9mm9tWXW..
10 W+WYWm9mm9tWXW..
11 W+WXWmmmmttWXW..
12 YZYWXWWWWWWXWW..
13 WmmWY322221WWWW.
14 WmmWY32222WZZZZW
15 .WWYY32222WZ22ZW
16 ..WYX32222WZAAZW
17 ..WWvvvvvvWZAAZW
18 ...W322221WZ22ZW
19 ...W322211WZ22ZW
20 ...WWWWWWW.WZZW.
21 ...WYYWWYXW.WW..
22 ...WXXWWXXW.....
23 ...WWWWWWWW.....
```

- **Main hand** on the viewer's left. A one-handed weapon is held upright beside the head: a 1 pixel blade or shaft
  between two `W` columns, a 3 pixel crossguard. Headgear may narrow the head to make room (the helmet spans columns
  2-13).
- **Off hand** on the viewer's right. A shield replaces the arm: about 6x9, with a rim and an emblem in a material
  ramp.
- **Two-handed and long gear** (staff, axe, hammer, hoe) stands beside the body and may reach row 0.
- Gear never covers the eyes, and stays inside the 16x24 frame.
- A face under a helmet or a hood narrows to 6 columns (5-10), its eyes at columns 6 and 9.

## Portraits

LttP has no portraits, so this part is ours and is not tried yet: review the first one closely, and update this
section with what works.

- The same character as its sprite, closer: same ramps, hair, headgear and clothes. Gear only when it reaches the
  shoulders (a staff's top, a shield's rim).
- Head and shoulders, front facing. The head spans about rows 2-23 and 22 columns; the shoulders fill rows 24-31 and
  are cut by the bottom edge.
- The bottom edge counts as silhouette: its dark pixels must be `W`.
- Eyes are 2 wide and 3 tall, with a light pixel in the top left. Brows in the hair's line colour. The nose is a single
  shade pixel, the mouth 2-3 pixels in the skin's line colour.
- The same light, lines and tones as the sprite, with at most 20 colours.

## Layers

The skill's stack, refined:

| Layer       | Holds                                                                 |
|-------------|-----------------------------------------------------------------------|
| `outline`   | The `W` silhouette, the `W` overlaps and the eyes                     |
| `highlight` | The highlights                                                        |
| `shade`     | The shades and the inner lines (each ramp's line colour)              |
| `base`      | The flat base colour of each shape                                    |
| `sketch`    | Optional, hidden: the template or a reference, painted to start from |

Start a sprite with
`tools/pixel-art/pixel new art-drafts/human/<id>.aseprite --size 16x24 --layer outline --layer highlight --layer shade --layer base --layer sketch`,
paint the template into `sketch`, and draw over it.

## Checklist

Before showing a draft, check what the tests would:

- [ ] The sprite is 16x24, the portrait 32x32.
- [ ] Every dark pixel on the silhouette, diagonals and the image edges included, is `W`.
- [ ] Only opaque pixels, or transparent ones. The tool can not write anything else.
- [ ] The `sketch` layer is hidden, and the PNG next to the source was exported after the last change.

And what makes it fit this brief:

- [ ] The head is 12 rows and the body 11; the feet are on row 23.
- [ ] The eyes are `9`, two pixels tall; no `W` inside a shape except an overlap.
- [ ] Light from the top left on every shape; no shape is flat or lit from both sides.
- [ ] At most 14 colours (`pixel info` lists them).
- [ ] The role reads at 1x from the silhouette and the main colour. Put the draft next to the others of the cast with
      `pixel preview`, and compare.

## Cast

The humans of the game, each with its reference in the LttP sheets. Pick from the suggestions; anything not listed is
free.

| Id               | Who                                                                | LttP reference                         | Look                                                                                                    | Gear                         |
|------------------|--------------------------------------------------------------------|----------------------------------------|---------------------------------------------------------------------------------------------------------|------------------------------|
| `hero`           | The player's first unit ("Knight", sword and heal)                 | Link, Link's uncle                      | Bare head, brown hair, light skin, green tunic, leather belt and boots. Not a copy of Link: no pointed cap, no pointed ears | A short sword                 |
| `knight`         | A soldier of the realm, the second playable unit                   | The blue Hyrule soldiers                | Steel helmet open on the face, blue tabard, steel greaves. The gear example above                        | Sword and shield              |
| `herbalist`      | Healer and poisoner who joins the party ("Herbs to heal, venom to kill") | The maidens' robes, the townsfolk | Olive hooded robe closing the legs, medium skin, a leather satchel at the hip                           | A sprig, or a flask of venom  |
| `viking`         | A northern warrior                                                 | The lumberjacks, for the build          | Horned steel helmet (`+` horns), big ginger beard over the chin, brown fur cloak, broad shoulders         | Sword and a round wooden shield |
| `viking-warrior` | The viking npc who joins the party ("For Valhalla"), sword and cleave | As `viking`                          | The `viking` drawing in the red dye, a steel chest plate                                                | An axe instead of the sword   |
| `merchant`       | The shopkeeper                                                     | Merchants A and B                       | A wide hat or turban, an apron over the gold dye, medium skin                                           | A coin pouch                  |
| `farmer`         | The villager met first, near the meadow                            | The Kakariko townsfolk                  | Straw hat (gold), blue overalls over a white shirt, light skin                                          | A hoe                         |
| `elder`          | The village elder who tells of the Goat Wizard                     | Sahasrahla                              | White hair and a long white beard over the chest, violet robe closing the legs                          | A wooden staff                |
| `blacksmith`     | Sells hammers ("Fresh from the forge!")                            | The smiths (draw a human, not a dwarf)  | Bare strong arms, dark skin, black hair under a red bandana, leather apron                              | A hammer                      |
| `alchemist`      | Sells potions and the teleport essence                             | Syrup, the witch brewing                | A tall pointed hat and robe in magenta, light skin                                                      | A potion flask                |
| `mystic`         | Waits at the end of the first path                                 | The fortune teller                      | Indigo hood and robe, a veil over the lower face, only the eyes showing                                 | A glowing teal orb            |

`the-guy` is a test npc and is left out.
