---
name: storybook
description: Opens a story of the game's Storybook (the views of the game shown in isolation, e.g. the shop or the health bar, in each of their states) in a headless browser with Playwright, optionally clicks and types on it, and saves a screenshot to look at. Use it to see a view after changing it, to check a story added or changed, to reproduce what a Storybook link shows, or whenever a screenshot of a view of the game is wanted without playing up to it.
---

# Storybook

The Storybook is the game itself, started on another page: `/storybook/` instead of `/`. It shows one view of the game
at a time, a story, in a phone frame, with its controls and actions on the right. A link opens exactly one state, e.g.:
`/storybook/?story=screen-shop--sell&args=gold:0;selectedSlot:1&globals=language:es`. See
[Storybook](../../../docs/architecture/storybook.md) for what it is, and
[How to add a story](../../../docs/testing/how-to-add-a-story.md) to add one.

To look at a story: build the web bundle, add the Storybook page to it, serve it, and run
[`tools/storybook/screenshot.cjs`](../../../tools/storybook/screenshot.cjs), which waits until the story has rendered
and saves a PNG you then read. Every command runs from the repository root.

## 1. Find the story id

Every component and story id is listed in
[`StorySmokeTest`](../../../src/commonTest/kotlin/com/mkz/rpg/storybook/adapters/presentation/StorySmokeTest.kt):

```shell
grep -oE '"[a-z]+(-[a-z0-9]+)*--[a-z0-9-]+"' src/commonTest/kotlin/com/mkz/rpg/storybook/adapters/presentation/StorySmokeTest.kt
```

A story id is `<component id>--<story>`, e.g.: `screen-shop--sell`. The arg names are the control names of the
component, in its `<View>Stories` binding under `src/commonMain/kotlin/com/mkz/rpg/storybook/adapters/presentation/`
(e.g.: `gold`, `selectedSlot` in `ShopViewStories.kt`). The only global is `language` (`en`, `es`, ...).

## 2. Build and serve it

```shell
./gradlew browserReleaseWebpack          # about a minute; again after every change of the code or the resources
bash tools/storybook/add-storybook.sh    # copies build/www into build/www/storybook, as the Deploy JS workflow does
python3 -m http.server 8765 -d build/www # in the background (run_in_background), and leave it running
```

The server serves the files as they are on disk, so after a new build only `add-storybook.sh` has to run again, not the
server.

## 3. Have Playwright available

- **Cloud session**: Chromium and Playwright are already installed (`node -e "require('playwright')"` works). Do not
  install anything: the script finds them, also in the global `node_modules`.
- **Local machine** (e.g.: macOS), when `require('playwright')` fails: install `playwright-core` once, which drives the
  installed Google Chrome without downloading a browser. Its `node_modules` is ignored by git:

  ```shell
  (cd tools/storybook && npm install)
  ```

  Set `CHROME` to the Chrome executable when it is not in its default place.

The script always starts Chrome headless with `--use-angle=swiftshader --enable-unsafe-swiftshader`, which draws WebGL
in software, so it renders without a GPU. The text of the Storybook then shows small dots and ticks around some letters:
it is the software renderer, not a bug of the view.

## 4. Take the screenshot

```shell
node tools/storybook/screenshot.cjs 'story=screen-shop--sell&globals=language:es'
```

- The argument is the query of a link, as the Storybook writes it: `story=<story id>`, then optionally
  `&args=<name>:<value>;<name>:<value>` and `&globals=language:<code>`. Quote it, as it holds `&` and `;`.
- It waits for the readiness signal: once the story has rendered, the Storybook titles the page
  `Storybook – <story id>`. Loading the bundle takes about 9 seconds; it gives up after 60.
- It saves `build/storybook/<story id>.png` (or `--out <png>`, e.g.: under the scratchpad), and prints the story
  rendered and the final URL. **Read the PNG** to look at it.
- An unknown story id, arg or value is ignored, as a link is: the first story, or the story's own value, is shown, and
  the script warns when the story rendered is not the one asked for. Check the values in the Controls of the
  screenshot: the URL keeps what was asked for until a control changes.
- `Console error:` and `Page error:` lines are the page's own errors: a story that fails to render shows them.
- `--port <port>` when it is served elsewhere than 8765.
- `--screen <width>x<height>` opens it on another screen, e.g.: `--screen 390x844`, a portrait phone; add `--touch` for
  a touch screen, as a phone's (three pixels a point), where every `--click` is a tap: KorGE ignores the mouse on a
  touch screen. By default it is the 1280x900 window, with a mouse. The Storybook is drawn on the whole screen, at Fit:
  on a portrait phone, a strip across its middle.

## 5. Click and type

Steps run in order after the story has rendered, each one waiting briefly for the story to render again (it prints
`rendered again` or `nothing rendered again`, e.g.: an action that only logs itself):

| Step              | Does                                                                                       |
|-------------------|--------------------------------------------------------------------------------------------|
| `--click <x>,<y>` | Moves the mouse there, then presses and releases it after 80ms: KorGE misses a click without the move first. |
| `--type <text>`   | Types the text after the text of the input clicked before, and presses Enter, which applies it. |
| `--replace <text>`| Deletes the text of the input clicked before (KorGE's inputs have no select all), types the text, and presses Enter. |
| `--press <key>`   | Presses a key, e.g.: `Backspace`, `ArrowLeft`.                                              |
| `--wait <ms>`     | Waits, e.g.: for an animation to finish.                                                    |
| `--shot <png>`    | Saves a screenshot now, besides the last one.                                              |
| `--zoom <button>` | Taps a button of the zoom bar, at the bottom right of the screen: `out` (`-`), `in` (`+`), `fit` or `phone`, which fills the screen with the phone. |

For example, selecting the second slot of the shop, renaming the buyer, and clearing the actions:

```shell
node tools/storybook/screenshot.cjs 'story=screen-shop--sell' \
  --click 400,407 --shot build/storybook/slot-selected.png \
  --click 1037,440 --replace 'Sir Mage' \
  --click 1180,757 --out build/storybook/renamed.png
```

### Where things are

The page is 1280x900, the Storybook window, so a coordinate of the page is one of the window:

| Area                                 | Coordinates                                                                                                 |
|--------------------------------------|-------------------------------------------------------------------------------------------------------------|
| Sidebar                              | x 0 to 260. The search at (114, 70), its `×` at (229, 70). The stories are rows 28 high under their component, their names from x ≈ 37, e.g.: `Shop / Sell` at (48, 789) with it on show. Searching puts the first story listed at (48, 159), e.g.: `--click 114,70 --type 'shop sell' --click 48,159`. |
| Phone screen, the view on show       | From (304, 28), 390x844, the game window: a point (x, y) of the game is at (304 + x, 28 + y).              |
| Toolbar, the language                | `Language` at y ≈ 46; the languages from x ≈ 835, e.g.: `English` (880, 46), `Español` (979, 46).          |
| Story info                           | From (738, 90): the component and its description, then the story and its description.                     |
| Controls                             | Title and `Reset` (1180, 357), then a row every 38 from y ≈ 401: names from x 738, editors from x 888. An int has `-` at x 900 and `+` at x 993, a pick `<` at x 900 and `>` at x 1129, a text input from x 888 to 1188. |
| Actions                              | Title at y ≈ 754, `Clear` (1180, 757), the actions from y ≈ 791, the newest on top.                          |

The rows depend on the component: take a first screenshot and read the coordinates off it before clicking. The table
is the window at Fit: once zoomed, or on another `--screen`, read them off a screenshot too, e.g.: on a 390x844 phone
zoomed to `phone`, the phone fills its width: a point (x, y) of the game is at about (11 + 0.94 x, 24 + 0.94 y).

For example, the shop on a phone, at Fit and with the phone filling the screen:

```shell
node tools/storybook/screenshot.cjs 'story=screen-shop--sell' --screen 390x844 --touch \
  --shot build/storybook/shop-phone-fit.png --zoom phone --out build/storybook/shop-phone.png
```

## When it does not work

- **`No story rendered in 60 seconds`**: the page is not the Storybook (check the server serves `build/www`, and that
  `add-storybook.sh` ran after the last build), or the bundle was built before the readiness signal existed. Look at the
  screenshot it saved anyway.
- **`... did not open`**: the server is not running, or on another port.
- **A black or empty phone**: WebGL did not start. Check the `Console error:` lines; the SwiftShader flags are needed
  on a machine without a GPU.
- **A click does nothing**: check the coordinates on a screenshot, and that the view is not still animating
  (`--wait 500` first).
