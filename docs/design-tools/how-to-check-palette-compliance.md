# How to check palette compliance

## When to use this

Use this when you add or edit pixel art (unit sprites, ability icons, terrain tiles, effect icons) and want to verify it is drawn exclusively with the Famicube palette — or when a `PaletteCheckerTest` failure points at one of your new assets.

## Background

The game targets the 64-color Famicube palette stored in `resources/famicube-palette.png`. `PaletteCheckerTest` (jvmTest) loads the palette and samples every opaque pixel of every game-art asset under `resources/`. Any pixel whose RGB color is not one of the 64 palette colors fails the build, listing the file, each offending color, and its pixel count.

Transparent pixels are ignored. The checker covers the curated game-art set (ability icons, battlefield selection tiles, effect icons, terrain transitions, unit sprites, and the bee portrait). Deliberately excluded: `korge.png` (engine logo), `unit/knight_portrait.png` and `unit/rat_portrait.png` (full-color illustration art), and `unit/goblins/` (reference samples) — see the reference doc for the full list.

## Steps

### Check the current assets

Run the palette test:

```sh
./gradlew jvmTest --tests "com.mkz.rpg.assets.PaletteCheckerTest"
```

The test `should pass when every game art asset uses only Famicube palette colors` fails with a message like:

```
Game art assets with off-palette pixels:
  unit/knight.png: #FF00FF x 2, #00FF00 x 1
```

Each entry is `file: #RRGGBB x count, ...` — the colors are the ones not in the palette, sorted by count.

### Fix an off-palette asset

1. Open the file in a pixel editor that shows the Famicube palette.
2. Remap every off-palette pixel to the nearest palette color (the failing message tells you exactly which colors to look for).
3. Save the file as an 8-bit PNG (RGBA or indexed), keeping the original size.
4. Re-run the palette test until it passes.

### Add a new asset to the checked set

1. Put the PNG under `resources/` (e.g. `resources/unit/fox.png`).
2. Add its path to `GAME_ART_ASSET_PATHS` in `src/jvmTest/kotlin/com/mkz/rpg/assets/FamicubePaletteChecker.kt`.
3. Add the path to `CONTACT_SHEET_ASSET_PATHS` too (same file) if it should appear on the contact sheet.
4. Run the full test suite — the new asset is now palette-checked on every `jvmTest` run.

### Regenerate the asset contact sheet

```sh
./gradlew generateContactSheet
```

Writes `build/contact-sheet.png` — every asset (checked game art plus the excluded portraits and goblin sample) drawn on a single grid. Open it to eyeball all sprites, tiles, and icons at a glance.

## Common mistakes

- **New asset is not checked** — you added the PNG but forgot to list it in `GAME_ART_ASSET_PATHS`. The test only covers the curated list.
- **Checker fails after an editor "save as"** — some editors add anti-aliasing or convert the palette; re-export as a plain 8-bit PNG and re-check the pixel count from the failure message.
- **Portraits look off-palette but the test passes** — `knight_portrait.png` and `rat_portrait.png` are full-color illustration art and are excluded from the check on purpose.
