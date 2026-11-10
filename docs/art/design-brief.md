# Design brief

## 1. Vision

Create a clean, polished, thoughtful tactical game whose battles feel like part of exploring an open fantasy setting,
rather than a separate, menu-driven battle screen. Combine the visual charm of *The Legend of Zelda: A Link to the
Past* (SNES) with the vibrant colours of *Super Mario All-Stars: Super Mario Bros. 3*, using the FamiCube palette.

The battlefield is the primary interface. The game screen should help the player understand tactical possibilities
and consequences without overwhelming the battlefield or interrupting the flow of play.

**Design pillars:** Tactical clarity · Immersion in the setting · Pixel-art authenticity · Minimalism · Solo-developer
sustainability.

## 2. Visual direction

- **Battlefield, battle units and npcs:** Top-down fantasy pixel art inspired by *A Link to the Past*. Keep the
  proportions of battle units and npcs, and their apparent pixel size, close to the SNES reference.
- **Palette:** FamiCube is the authoritative palette of every asset (see
  [ADR 0005](../adr/0005-famicube-palette-and-outline.md)). Build a consistent visual language from its colours rather
  than introducing arbitrary shades.
- **Pixel art and vector chrome:** Pixel art for what the artist draws: the battlefield's terrains and buildings, battle
  units, npcs, lootable items, portraits, and ability and effect icons. Vector chrome around it, where it improves
  readability: the panels, bars and buttons of the HUD and the call to action, and their text (see
  [ADR 0007](../adr/0007-vector-hud-chrome-around-pixel-art.md)).
- **Presentation:** Modern information hierarchy without losing the retro aesthetic. Use clean spacing, restrained
  framing, and clear contrast.
- **Animations:** Prefer authored pixel-art animations over lighting-heavy animations, bloom, or anything else that
  obscures the battlefield.

## 3. Battlefield and game screen

- Design first for **portrait mobile play** and touch, on a 390x844 screen (iPhone 14), as the Storybook's phone.
- The battlefield is a top-down grid of tiles that occupies most of the game screen, the camera scrolling over it.
- Keep what is always on the game screen to a minimum. Show the HUD and the call to action according to the
  selection: a battle unit, one of its abilities, or a cast to preview.
- Each player has several battle units; the human player's explore as the party. During their player turn, the human
  player can act with each of their battle units before the CPU player's player turn.
- A battle starts and finishes on the battlefield the party explores: the game screen stays, the banner announces the
  battle, and there is no separate battle screen.

## 4. Core touch interaction

The interaction should be discoverable and reversible where practical:

1. **Select a battle unit:** Tap one of the human player's battle units to make it the selected battle unit, revealing
   its walking range, and its remaining steps and casts and its abilities in the battle unit info.
2. **Walk or cast:** The selected battle unit can walk, cast an ability, or do neither. Tapping a tile in its walking
   range walks it there; it stays the selected battle unit and its remaining steps and casts update.
3. **Choose an ability:** Show its abilities, up to the maximum abilities of six, in a compact, touch-friendly row.
   Make an ability with a remaining cooldown immediately distinguishable from one that can be cast.
4. **Choose where to cast:** Highlight the cast range of the selected ability. Make the tiles outside it clearly
   distinguishable without relying on colour alone.
5. **Preview the cast:** When a tile in the cast range is tapped, show the cast preview, with its recipients and the
   predictable outcomes on each, before the player confirms the cast.

Avoid requiring precise taps on tiny tiles or buttons, or relying only on hover, keyboard, or controller conventions.

## 5. Tactical information and feedback

The key challenge is helping the player understand how abilities, effects, terrains, units, and adjacent battle units
interact.

- Keep the health and the important ongoing effects of battle units compact and visible on them.
- Reveal the battle unit info and what each effect does through the selection, rather than permanent panels.
- Use visual hints on battle units and abilities to communicate synergies and possible interactions.
- Show the predictable outcomes and the recipients in the cast preview, before the cast. Clearly distinguish uncertain,
  conditional, or situation-dependent outcomes.
- Communicate effects that spread, such as an effect on defeated that applies an effect to its recipient's adjacent
  allies, through understandable visual cues and the battlefield itself.
- Use pixel-art animations, highlighted tiles, and visible changes on battle units as the primary battle feedback. Keep
  supporting text concise and contextual.
- Do not use a battle log. Important outcomes must be understandable without reading a running history of events.

Clarity takes priority over spectacle, but major events can still have distinctive, satisfying animations.

## 6. Accessibility and learning

Keep the game screen simple for a broad audience. Introduce mechanics gradually as the player meets them. Use
consistent icons and visual rules, with an explanation in context, such as the ability info, when the player selects an
ability or meets a complex interaction. Do not make colour the only way to distinguish important states.

## 7. Production constraints

The art and the game screen must be achievable by a solo developer. These rules keep them so:

- Every asset uses only the FamiCube palette, fully opaque or fully transparent pixels, and one outline colour, which
  the architecture tests check (see [ADR 0005](../adr/0005-famicube-palette-and-outline.md)).
- Every asset is an Aseprite source drawn with the pixel tool (see
  [ADR 0006](../adr/0006-aseprite-sources-drawn-with-the-pixel-tool.md)), following the
  [`pixel-art` skill](../../.claude/skills/pixel-art/SKILL.md).
- Pixel art is drawn at a whole scale, never blurred: on 64px tiles, 16x16 art at 4x and 32x32 art at 2x.
- Tiles, sprites, views, icons, and animations are reusable, and every view of the game screen is shown in its states
  in the Storybook (see [ADR 0008](../adr/0008-in-game-storybook.md)).
- The battlefield, the battle information, and the interaction stay separate enough to evolve without a complete
  redesign of the game screen.

## 8. Non-goals and success criteria

**Avoid:** Permanent panel clutter, crowded effect icons, tiny tiles or buttons to tap, a battle log, lighting-heavy
animations, and animations that conceal tactical information. Do not introduce a separate battle screen to go to from
exploring.

**The game screen succeeds when:** The player can quickly identify which battle unit is selected, its walking range,
which of its abilities can be cast, the cast range of the selected ability, and what its cast preview shows, while the
battlefield remains the focus and the game still feels like exploring a coherent fantasy setting.

**Guiding rule:** Every element of the game screen should either clarify a meaningful decision, communicate an
important state, or improve touch interaction. If it does none of these, it probably should not be on screen.
