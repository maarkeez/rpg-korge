# UI Design Brief — Tactical Fantasy RPG

## 1. Vision

Create a clean, polished, thoughtful tactical combat experience that feels like controlling characters in an open fantasy world, rather than entering a separate, menu-driven battle screen. Combine the visual charm of *The Legend of Zelda: A Link to the Past* (SNES) with the vibrant colors of *Super Mario All-Stars: Super Mario Bros. 3*, using the Famicube palette.

The battlefield is the primary interface. The UI should help players understand tactical possibilities and consequences without overwhelming the world or interrupting the flow of play.

**Design pillars:** Tactical clarity · World immersion · Pixel-art authenticity · Minimalism · Solo-developer sustainability.

## 2. Visual Direction

- **World and characters:** Top-down fantasy pixel art inspired by *A Link to the Past*. Keep character proportions and apparent pixel size close to the SNES reference.
- **Palette:** Use Famicube as the authoritative color palette. Build a consistent visual language from its colors rather than introducing arbitrary shades.
- **UI:** A hybrid approach: pixel-art world, characters, and combat effects combined with selected higher-resolution UI elements where they improve readability, such as health bars, icons, and text.
- **Presentation:** Modern information hierarchy without losing the retro aesthetic. Use clean spacing, restrained framing, and clear contrast.
- **Effects:** Prefer authored pixel-art animations and effects over lighting-heavy VFX, bloom, or other effects that obscure the battlefield.

## 3. Battlefield and Layout

- Design first for **portrait mobile play**, touch input, and an iPhone 14-class screen.
- Use a top-down, scrolling grid battlefield that occupies most of the screen.
- Keep permanent UI to a minimum. Show contextual controls and information when a unit or ability is selected.
- Each side can contain multiple units; the player controls up to four allied units. During the player's turn, they can manage all their units before the CPU takes its turn.
- Avoid a visually abrupt transition into a separate battle interface. Keep the combat presentation compatible with a future world experience that may combine real-time exploration and turn-based combat, without designing that future system now.

## 4. Core Touch Interaction

The interaction should be discoverable and reversible where practical:

1. **Select a unit:** Tap an allied unit to reveal its movement range, available actions, and ability bar.
2. **Move or act:** The player can move, cast an ability, or choose neither. Tapping a reachable tile moves the unit there; the selection remains active and the available actions update.
3. **Choose an ability:** Show up to six abilities in a compact, touch-friendly bar. Make cooldowns immediately distinguishable from abilities that are ready.
4. **Choose a cast location:** Highlight valid cast locations or targets. Make invalid positions clearly distinguishable without relying on color alone.
5. **Preview consequences:** When a valid cast location is selected, show the affected targets and the predictable effects before the player confirms the cast.

Avoid requiring precise taps on tiny targets or relying exclusively on hover, keyboard, or controller conventions.

## 5. Tactical Information and Feedback

The key challenge is helping players understand how abilities, status effects, terrain, enemy types, and nearby units interact.

- Keep health and important status indicators compact and visible on units.
- Reveal detailed stats and status descriptions through selection or inspection, rather than permanent panels.
- Use visual hints on units and abilities to communicate synergies and possible interactions.
- Preview predictable outcomes and affected targets before casting. Clearly distinguish uncertain, conditional, or situation-dependent effects.
- Communicate status propagation—including spread to nearby enemies and effects triggered by death—through understandable visual cues and the battlefield itself.
- Use pixel-art animation, target highlighting, and visible state changes as the primary combat feedback. Keep supporting text concise and contextual.
- Do not use a combat log. Important outcomes must be understandable without reading a running history of events.

Clarity takes priority over spectacle, but major events can still have distinctive, satisfying animations.

## 6. Accessibility and Learning

Keep the interface simple for a broad audience. Introduce mechanics gradually as players encounter them. Use consistent iconography and visual rules, with contextual explanations when a player selects an ability or encounters a complex interaction. Do not make color the only way to distinguish important states.

## 7. Production Constraints

The UI and art system must be achievable by a solo developer.

- Establish consistent pixel-art and UI scaling rules.
- Use pixel-perfect scaling for pixel-art assets; avoid unintended blur and inconsistent pixel sizes.
- Define reusable tiles, sprite parts, UI components, icons, and effect animations.
- Keep the Famicube palette and a small set of visual conventions documented.
- Separate world presentation, combat information, and interaction logic sufficiently to allow future evolution without requiring a complete redesign of the interface.

## 8. Non-Goals and Success Criteria

**Avoid:** Permanent panel clutter, crowded status icons, tiny touch targets, combat logs, excessive lighting effects, and effects that conceal tactical information. Do not introduce a separate exploration/combat mode transition or design the future RTS concept into the current combat system.

**The UI succeeds when:** A player can quickly identify which unit is selected, where it can move, which abilities are ready, where an ability can be cast, and what its consequences are—while the battlefield remains the focus and the experience still feels like exploring a coherent fantasy world.

**Guiding rule:** Every UI element should either clarify a meaningful decision, communicate an important state, or improve touch interaction. If it does none of these, it probably should not be on screen.