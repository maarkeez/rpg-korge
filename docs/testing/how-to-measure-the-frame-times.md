# How to measure the frame times of the web build

> **Summary:** Play the web build in Chrome with a script that walks the party, and list the frames over 50ms, e.g.: after a gameplay change, to check exploring does not stutter. The headless tests do not catch it: they simulate the frames, and the JVM runs the game logic much faster than the browser.

## Prerequisites

- Node 18 or newer. The one Gradle downloads for Kotlin/JS works, e.g.: `~/.gradle/nodejs/node-v20.12.1-darwin-arm64/bin`.
- Google Chrome. The script looks for it where it is installed by default on macOS, Windows and Linux. Set `CHROME` to its path otherwise.

## Steps

### 1. Install the script

Once, from the repository root:

```shell
cd tools/frame-times
npm install
```

It installs [`puppeteer-core`](https://pptr.dev), which drives the installed Chrome, without downloading one.

### 2. Build the web version

From the repository root:

```shell
./gradlew jsBrowserProductionWebpack
```

The script plays this build: the resources in `build/processedResources/js/main`, and the bundle in `build/kotlin-webpack/js/productionExecutable`. Build it again after every change.

### 3. Measure

From `tools/frame-times`:

```shell
node measure.mjs
```

It takes about 30 seconds. It serves the build, opens it in Chrome at the game window size (390x844), plays a new game, and walks the party for 15 seconds: one tile west, then one tile east, in turns, on its first tiles of the demo battlefield. Chrome draws with the GPU, as when playing, without a window.

### 4. Read the report

```
Renderer: ANGLE (Apple, ANGLE Metal Renderer: Apple M2 Pro, Unspecified Version)
Frames: 946, p50 16.7ms, p95 16.7ms, max 16.8ms
No frame over 50ms
```

- **Renderer**: what draws the frames. A software renderer (e.g.: `SwiftShader`) means Chrome could not use the GPU, so the frames are slower than when playing.
- **Frames**: how many frames were drawn while walking, and how long they took: the median (p50), the 95th percentile (p95) and the longest. At 60 frames per second, a frame takes 16.7ms.
- **Long frame at 1.43s: 533ms**: every frame over 50ms, and when, since the party started walking. A freeze the player notices.

It exits with:

| Exit code | Meaning                                                                                                                                   |
|-----------|-------------------------------------------------------------------------------------------------------------------------------------------|
| 0         | No frame over 50ms.                                                                                                                       |
| 1         | Some frames over 50ms.                                                                                                                    |
| 2         | Nothing measured: the web build is missing, the game did not start exploring, or a battle started while walking. Measure again after a battle, as the CPU battle units do not wander meanwhile, so the frames are not those of exploring. |

### 5. Find what takes long

```shell
node measure.mjs --profile
```

It also samples where the time goes while walking, and writes it to `frame-times.cpuprofile`. Open it in Chrome DevTools: the Performance panel, then Load profile. The long frames are the wide blocks of the flame chart. The production bundle is minified, so the function names are short. The source map next to the bundle, `hello-world.js.map`, maps them back to the Kotlin sources, e.g.: with the [`source-map`](https://www.npmjs.com/package/source-map) package.

## Troubleshooting

- **The game did not start exploring**: the script clicks the play button and the arrows at their position on the game window, and checks that the up arrow is displayed (blue). After a layout change, update `PLAY_BUTTON`, `ARROWS` and `PAD_CORNER` in [`measure.mjs`](../../tools/frame-times/measure.mjs).
- **A battle started while walking**: a CPU battle unit wandered within reach of the party. It is random, so measure again. After a change of the demo battlefield, check that the walk still keeps the party away from the CPU battle units.

See [the plan](../plans/exploring-without-freezes.md) for the freezes it was written for.
