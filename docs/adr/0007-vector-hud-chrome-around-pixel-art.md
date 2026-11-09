# The HUD chrome is drawn as vectors around pixel art

The HUD keeps pixel art only for what the artist draws (the portraits, the ability and effect icons, the chest), always
at a whole scale, and draws the chrome around it as vectors: the panels, slots, bars, points, pills, rings and overlays,
styled by `HudStyle`. We chose this so the chrome needs no asset per size or state, at the cost of a chrome that is not
pixel art: the FamiCube palette (see [ADR 0005](0005-famicube-palette-and-outline.md)) binds only the assets, and
`HudStyle` holds the colours of the chrome.
