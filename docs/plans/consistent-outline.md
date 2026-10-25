# Plan: Consistent outline

> **Status:** done.
>
> **Summary:** The outline of each image is drawn in the image itself, and the images do not agree on its colour. The
> knight, and most battle units and npcs, are outlined with `#151515`. The bat and the merchant, alchemist, blacksmith
> and mystic npcs are outlined with the lighter grey `#343434`, the bee with the navy `#0d2030`, and so are the lootable
> items, the chest and the fortress with `#343434`. The knight and hero portraits are outlined with pure black. Instead,
> every battle unit, npc, portrait, lootable item, chest and building is outlined with the knight's `#151515`, and an
> architecture test keeps it that way.

## Decisions

| Topic              | Decision                                                                                                                                                                                                       |
|--------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Outline colour     | `#151515`, the knight's, already in the FamiCube palette.                                                                                                                                                      |
| Silhouette         | The opaque pixels next to a transparent pixel, or to the edge of the image, diagonals included (the outlines are drawn 8-connected, e.g.: the knight's corners).                                                |
| What is recoloured | Only the dark silhouette pixels (luminance below 60): `#343434`, `#0d2030` and `#000000` become `#151515`. The same colours inside the image (the bat's body, the bee's stripes, the fortress courtyard) stay. |
| Accent trims       | Silhouette pixels in a light or saturated colour stay, e.g.: the goat's pink, the herbalist's brown, the bee's wings.                                                                                         |
| Upscaled portraits | Every portrait but the knight, hero and rat ones is its battlefield image upscaled 2x. They are regenerated from the fixed image, so their 2 pixel outline changes as a whole.                                   |
| Drawn portraits    | The knight and hero portraits are drawn: only their pure black silhouette pixels become `#151515`. The rat portrait has no single outline colour (hair, black, dark green), so it is left as drawn.             |
| Out of scope       | Ability and effect icons, which are framed with the colour of their badge, and terrain.                                                                                                                       |

## Assets

| Images                                                                                        | Before            | Fix                                   |
|-----------------------------------------------------------------------------------------------|-------------------|---------------------------------------|
| `unit/bat.png`, `npc/{merchant,alchemist,blacksmith,mystic}.png`                              | `#343434`         | Recolour the silhouette               |
| `unit/bee.png`                                                                                | `#0d2030`         | Recolour the silhouette               |
| `unit/{bat,bee}_portrait.png`, `npc/{merchant,alchemist,blacksmith,mystic}_portrait.png`      | as their image    | Regenerate as the fixed image 2x      |
| `unit/{knight,hero}_portrait.png`                                                             | `#000000`         | Recolour the silhouette               |
| `lootableItem/*.png`, `battlefield/chest.png`, `building/fortress.png`                        | `#343434`         | Recolour the silhouette               |

The recolouring is a one-off script, not committed. Each fixed image is checked side by side with its original before
committing.

## AssetOutlineArchitectureTest

A new test next to `AssetPaletteArchitectureTest`, over the images under `unit/`, `npc/`, `lootableItem/`, `building/`
and `battlefield/chest.png`:

- `should outline with #151515 when is a game asset`: every silhouette pixel darker than luminance 60 is `#151515`. On a
  failure, it lists each asset with its other dark outline colours and their pixel counts, like the palette test.
- `unit/rat_portrait.png` is excluded by name, with the reason.
- `docs/architecture/assets.md` gains an "Outline" rule under "Colour palette", pointing to the test.

## Tests

- Run `AssetOutlineArchitectureTest` on the current assets first: it fails, listing exactly the assets in the table
  above. Once they are fixed, it passes.
- `AssetPaletteArchitectureTest` still passes: `#151515` is a palette colour, and no semi-transparent pixel is added.
- Snapshot the game (see [How to take a game snapshot](../testing/how-to-take-a-game-snapshot.md)) with the knight next
  to a bat, an npc, a chest and the fortress.
