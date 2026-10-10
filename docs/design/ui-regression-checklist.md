# UI regression checklist

Manual checklist for the tactical battle UI. Run it on `./gradlew runJvm -Pscenario=ui-showcase`. For M2, M9 and M10 also run it on the JS build in a browser at 390×844 with touch emulation.

1. Can I tell which unit is selected within 1 s?
2. Can I tell where it can move, and do ally and enemy ranges look different?
3. Can I tell which abilities are ready, without color? (Check in grayscale.)
4. Can I tell where the selected ability can be cast, and what happens if I tap elsewhere?
5. Does the preview show every affected unit, the HP after, statuses and conditional spread?
6. Did cancelling a preview leave everything exactly as before?
7. During the CPU turn, could I follow every action?
8. Is anything blurry, mis-scaled or jittering while scrolling?
9. Is any touch target hard to hit with a thumb?
10. Is there any element that doesn't clarify a decision, show important state or help touch? If yes, list it.

## Automated guards

- Touch targets: `BattleUiScriptTest.TouchTargets` (every clickable `UIButton` ≥ 44×44 pt).
- Color tokens: `UiColorTokenAuditTest` and `UiPaletteTest`.
- Visual matrix at 390×844: `initial`, `unit-selected`, `ability-selected`, `enemy-inspected`, `movement-range`, `ability-cooldowns`, `cast-targets`, `cast-preview`, `propagation-preview`, `after-cpu-playback`.

## M10 audit against the guiding rule (brief §8)

Kept, because each one clarifies a decision, shows state or helps touch: top strip (round, turn, "..." during playback), on-unit HP bars and pips, movement range (ally/inspect styles, hazard glyph), ability bar with cooldown, lock and selection frame, cast target highlights with dimming, preview ghost, skull and pending pips, conditional outline and connectors, 4-line consequence sheet, Confirm/Cancel, Finish turn, combat feedback with amount pops.
Removed or unused: `tile_selection_3.png` (movement) and `ability_selection.png` are no longer drawn by the UI but stay in the asset list. No element was removed in M10.
