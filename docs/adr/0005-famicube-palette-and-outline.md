# Assets use the FamiCube palette, opaque pixels and one outline colour

Every image under `resources` only uses the 64 colours of the FamiCube palette (`resources/famicube-palette.png`), with
every pixel fully transparent or fully opaque, and the battle units, npcs, their portraits, lootable items, buildings and
the chest are outlined with `#151515`, the knight's outline, on their dark silhouette pixels. We chose a small fixed
palette so the art of every contributor, coding agents included, looks like one game, and because it can be checked:
`AssetPaletteArchitectureTest` and `AssetOutlineArchitectureTest` fail on any colour off the palette, semi-transparent
pixel or other outline, where a looser style could only be reviewed by eye.
