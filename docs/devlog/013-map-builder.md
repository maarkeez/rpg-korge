---
date: 17th October 2026
commit: 192cce69a8b4331f27ae592e4b02cf60fb6ccb56
---

# 013 — Map Builder: Shortening the Feedback Loop

## Hook (1 minute)

Designing a battlefield should be as simple as painting one.

Before the map builder, creating a new battlefield meant editing a TOML resource file by hand. That works, but it slows down experimentation: change some tiles, update the file, run the game, inspect the result, and repeat.

I wanted a faster feedback loop. A visual editor would let me paint terrain, see the result immediately, and export the finished map in the format the game already understands.

The result is a new map builder, accessible from the main menu, that lets me create battlefields without manually writing TOML. Building it also brought two engineering challenges into focus: keeping large maps performant and handling file operations consistently across platforms.

## Context

The game already had a battlefield view that rendered terrain and the transitions between neighbouring tiles. It also supported scrolling across maps larger than the visible eight-by-eight tile area.

Rather than build a separate editor from scratch, I wanted to reuse that existing infrastructure. The map builder would use the same battlefield domain rules as a battle, ensuring that terrain is displayed and validated consistently in both contexts.

The scope was deliberately practical. The player can create a rectangular map, select terrain, paint individual tiles, resize the battlefield, and export it as TOML. The exported text can then be pasted into `resources/battlefield/battlefield.toml`, which remains the game's battlefield resource.

## Main Story (3 minutes)

### Reusing the battlefield instead of duplicating it

The first decision was to reuse `BattlefieldView` and give it a new presenter for the map builder.

This avoided duplicating terrain rendering, transition selection, and scrolling behavior. It also meant that the editor could use the same `Battlefield` domain to build and validate its displayed battlefield.

The tradeoff was that `BattlefieldView` had originally been designed for combat. It also handled battle units, loot, selection effects, and other features the editor doesn't need. Its resource loader loads images for units, effects, and items as well as terrain.

I accepted those costs rather than extract a separate terrain-grid component prematurely.

However, reuse exposed an important limitation: calling `displayBattlefield` repeatedly created additional grids and drag handlers. The editor needs to redraw the battlefield after resizing and update tiles whenever terrain changes. I changed the method to replace the existing grid and keep a single drag handler, then added `updateTerrain` to update only the painted tile and its four neighbours.

That last detail matters because changing one tile can also change the terrain transitions displayed around it.

### Making large maps practical

The editor supports maps from 1 × 1 to 128 × 128 tiles. Resizing preserves tiles that still fit and fills new positions with sand.

At the upper limit, however, the original battlefield view was too expensive. It created a button for every tile and processed all of them every frame, including those outside the visible area.

The initial measurements on the JVM showed that displaying a 128 × 128 map took 1.8 seconds. Frame processing took between 135 and 600 milliseconds. Simply hiding off-screen tiles brought frame processing down to around 25 milliseconds, but the view still iterated over every tile.

The solution was to create only the tiles inside the viewport and a one-tile margin around it. As the player scrolls, the view creates and drops tiles as needed. Terrain is reconstructed from the battlefield when a tile returns to view, while tiles containing additional state, such as a unit or loot, are retained off-screen.

After this change, displaying a 128 × 128 map took 6 milliseconds, and frame processing fell to 1.3 milliseconds.

The improvement was substantial, and the measurements also showed that larger maps no longer necessarily meant more work every frame. There is still a cost to painting: the battlefield is rebuilt using the domain rules, taking 27 milliseconds for a 256 × 256 map. That is a potential limit if maps grow further.

### Making import work across platforms

Exporting a map copies its TOML representation to the clipboard. Importing initially followed the same idea in reverse, but browser clipboard restrictions made that approach unreliable, particularly on Safari for iOS.

I considered alternatives, including a custom paste box on the browser page, a text input, and the browser's prompt dialog. Each introduced drawbacks, from platform-specific UI to clipboard dependencies or lost line breaks.

Instead, I chose a file picker. KorGE provides `GameWindow.openFileDialog`, allowing the editor to open a platform-native file picker on the JVM and a browser file picker on the web. Import reads the selected file using the same battlefield format as the resource loader.

The import is validated before replacing the current map. Invalid files are rejected, leaving the existing map intact and displaying an explanation. Cancelling the picker does nothing.

This keeps the workflow consistent: export a TOML file, save it, and import it later. File picking was tested successfully on iOS.

## Key Takeaways

- **Improve the feedback loop.** A small visual tool can remove repeated manual work and make experimentation easier.
- **Reuse existing rules.** Sharing the battlefield view and domain avoids having separate implementations drift apart.
- **Measure before optimizing.** Hiding off-screen tiles was insufficient because they were still processed. Reducing the number of active tiles addressed the actual bottleneck.
- **Design for real platforms.** A common API doesn't guarantee identical browser behavior. File-based import proved more suitable than clipboard-based import.
- **Validate at the domain boundary.** Terrain transitions and imported maps must obey the same rules as battlefields used in combat.

## What's Next

The map builder is implemented, with terrain painting, resizing, import, and export integrated into the game.

The next challenge is to continue improving the game without introducing unnecessary abstractions. The current measurements provide a baseline for future performance work, while the shared battlefield implementation gives both combat and map editing a common foundation.