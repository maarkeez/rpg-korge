# Plan: A pixel art tool for coding agents

> **Status:** done.
>
> **Summary:** A command line tool, `pixel`, for Claude agents to draw the game's low resolution art (16x16, 32x32, ...)
> following the design brief. It reads and writes Aseprite files, so the art keeps its layers and can still be opened
> in Aseprite, but it does not depend on Aseprite. It reads and writes single pixels and regions of a layer, shows
> what all the visible layers display together, and imports from and exports to PNG.

## Decisions

| Topic          | Decision                                                                                                                                                                                                                     |
|----------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Location       | A standalone Gradle build in `tools/pixel-art`, with its own `settings.gradle.kts` and plain `kotlin("jvm")`. The game build does not include it, so neither the KorGE plugins nor the JS targets slow it down.                |
| Dependencies   | The tool depends on Clikt, for its commands and `--help`, and on the JDK: `java.util.zip` for the cels, `javax.imageio` for PNG and `java.awt` for the previews. KorGE is only a test dependency (see Testing).            |
| Source of truth | Each asset has a layered `.aseprite` source under `art/`, mirroring `resources`: `art/unit/knight.aseprite` is exported to `resources/unit/knight.png`. Sources stay out of `resources`, so they are not bundled with the game. |
| Export         | Every command that saves a source under `art/` also exports its PNG to `resources`, so the PNG can not fall behind its source. There is no separate check. `--no-export` skips it, and `pixel export` exports explicitly.   |
| Colour mode    | Indexed, with the 64 FamiCube colours. Index 0 is transparent, and indexes 1 to 64 are the colours of `resources/famicube-palette.png` in order. An off-palette or semi-transparent pixel can not be written.               |
| Characters     | Every colour has a fixed character, the same in every file: `0-9`, `a-z`, `A-Z`, `+` and `=` in palette order. `.` is transparent. When painting, `-` leaves a pixel as it is. `pixel palette` lists them.                 |
| Pixel view     | A character grid, with the x and y coordinates around it and a legend of the colours it uses. `--plain` prints the grid alone, as `paint` reads it.                                                                          |
| Aseprite subset | One frame, image layers without groups, Normal blend mode, cels of type raw or compressed image with full opacity and no z-index. Anything else (groups, tilemaps, more frames, tags, slices, linked cels, user data) is rejected with a message naming it, instead of being lost on save. |
| Composite      | What all the visible layers display together, from the bottom layer to the top one: the topmost opaque pixel wins. A layer with opacity 0 displays nothing; any other opacity displays its pixels fully opaque.             |
| Layers         | Free-form names. The skill suggests a stack (e.g.: `outline`, `highlight`, `shade`, `base`) that the design brief can refine. Layers are listed from the top one, as Aseprite does.                                         |
| Edit model     | Each command reads the file, changes it and writes it back. `pixel apply` runs a script of commands on one file and saves only when all of them succeed. git is the history.                                               |
| PNG import     | Off-palette colours fail, listing each one with its closest FamiCube colour (in CIELAB, as `AssetPaletteArchitectureTest`). `--snap` replaces them with their closest colour. Alpha below `--alpha-threshold` (128) is transparent with `--snap`. |
| Feedback       | Commands that change a layer print it as a grid (`--quiet` skips it). `pixel preview` writes an upscaled PNG, with a pixel grid and a checkerboard for the transparent pixels, for the agent to look at. `pixel diff` shows what changed since a git revision. |
| Agent docs     | The `.claude/skills/pixel-art/SKILL.md` skill, with the workflow, the characters and examples, and a precise `--help` for every command.                                                                                       |
| Migration      | Every PNG under `resources`, except `famicube-palette.png`, is imported up front into a single `base` layer.                                                                                                               |
| Testing        | Unit tests in the tool. The files the tool writes are also decoded with KorGE's own Aseprite reader (`korlibs.image.format.ASE`), an independent implementation of the format, since there is no Aseprite binary to check them with. |
| CI             | A separate GitHub Actions workflow runs the tool's tests and lint when `tools/pixel-art` changes.                                                                                                                             |
| Out of scope   | Animation frames, auto-outline, palette and outline lint (the architecture tests keep doing it), side-by-side previews of several sprites.                                                                                   |

## Commands

Coordinates are `x,y` from the top-left pixel, sizes `WxH`, regions `x,y,WxH`. A colour is its character, its hex
(e.g.: `#151515`), or `.` for transparent.

| Command                                                         | Does                                                                                     |
|-----------------------------------------------------------------|------------------------------------------------------------------------------------------|
| `pixel palette`                                                 | Lists the characters of the colours.                                                     |
| `pixel new FILE --size 16x16 [--layer NAME]...`                 | Creates a sprite. Its layers are given from the top one (default: `base`).                |
| `pixel info FILE`                                               | Prints the size, the layers and the colours used.                                        |
| `pixel grid FILE [--layer NAME] [--region R] [--plain]`         | Prints a layer, or the composite, as a grid.                                             |
| `pixel get FILE X,Y [--layer NAME]`                             | Prints one pixel.                                                                        |
| `pixel layer add\|remove\|rename\|move\|hide\|show FILE ...`     | Manages the layers.                                                                      |
| `pixel import PNG FILE [--layer NAME] [--snap]`                 | Imports a PNG into a layer, creating the sprite when it does not exist.                  |
| `pixel export FILE [--out PNG] [--layer NAME]`                  | Exports the composite, or a layer, to PNG.                                               |
| `pixel set FILE --layer NAME X,Y=C...`                          | Sets single pixels.                                                                      |
| `pixel paint FILE --layer NAME --at X,Y [GRID]`                 | Paints a grid read from a file or stdin. `-` leaves a pixel as it is.                    |
| `pixel fill\|rect\|line\|replace FILE --layer NAME ...`         | Flood fills, draws a rectangle or a line, replaces a colour.                             |
| `pixel flip\|mirror\|shift\|copy FILE --layer NAME ...`         | Flips, mirrors a half onto the other one, shifts, copies a region between layers or files. |
| `pixel apply FILE [SCRIPT]`                                     | Runs a script of the drawing commands above, saving once.                                |
| `pixel preview FILE [--out PNG] [--scale N] [--layer NAME]`     | Writes an upscaled preview.                                                              |
| `pixel diff FILE [--against REV] [--layer NAME]`                | Shows the pixels changed since a git revision (default: `HEAD`).                         |

## Commits

1. **Read, write, import and export.** The Gradle build, the Aseprite reader and writer, the palette and the grid,
   `palette`, `new`, `info`, `grid`, `get`, `layer`, `import` and `export`, the export on save, the `pixel` launcher and
   the CI workflow.
2. **Drawing.** `set`, `paint`, `fill`, `rect`, `line`, `replace`, `flip`, `mirror`, `shift`, `copy` and `apply`.
3. **Feedback.** The grid printed after a change, `preview` and `diff`.
4. **Agents and migration.** The `pixel-art` skill, the docs, and every asset imported into `art/`.
