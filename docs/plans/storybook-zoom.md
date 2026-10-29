# Plan: Storybook zoom

> **Status:** in progress: M1 is done.
>
> **Summary:** Let the person zoom in and out of the [Storybook](../architecture/storybook.md) and move around it, mostly
> on a phone, where the whole 1280x900 Storybook is shrunk to the width of the screen today. The Storybook keeps its
> fixed layout, but is drawn on the whole screen. It zooms with floating buttons that work on every platform, and with a
> pinch where the platform sends one: two fingers on a touch screen, and the trackpad on a Mac. When zoomed in, dragging
> anything that does not drag itself moves around. The zoom is a setting of the device, kept in the presentation only,
> out of the domain and of the link.

## Decisions

| Topic              | Decision                                                                                                                                                                                                                                                                                         |
|--------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| What zooms         | The whole Storybook: the sidebar, the phone and the panels, as one. Its layout stays 1280x900 (`STORYBOOK_WINDOW_SIZE`), unchanged.                                                                                                                                                              |
| Whole screen       | The Storybook is drawn on the whole screen of the device, not in its letterboxed 1280x900 box: `Korge` starts it with `KorgeDisplayMode.CENTER_NO_CLIP`, so the stage still fits 1280x900 in the screen, centred, and draws around it. Without it, zooming in on a portrait phone could not make the phone taller than the strip it fits in today. The game keeps its own 390x844 letterbox. |
| Fit                | Fit is the whole Storybook on the screen, centred, as KorGE shows it today: zoom 1, shown as `100%`. A screenshot at Fit is the same as before the zoom, so the [`storybook` skill](../../.claude/skills/storybook/SKILL.md) and its 1280x900 screenshots do not change.                                  |
| Range              | From Fit to 4× Fit. The Storybook cannot be smaller than Fit.                                                                                                                                                                                                                                    |
| Phone              | The Phone preset zooms so the `PhoneFrameView` fills the screen (the smaller of the two ratios, at most 4×), centred on it. On a 390x844 portrait phone it is about 3×, the phone at its real size; on a 1280x900 window, about 1×.                                                               |
| Buttons            | A floating bar at the bottom right of the screen, `−`, the zoom (`150%`), `+`, `Fit` and `Phone`, drawn above the Storybook and not zoomed with it. `−` and `+` divide and multiply the zoom by 1.25, around the centre of the screen. They work on every platform, and are what the tests tap. |
| Bar size           | The bar keeps its size on the screen, whatever the zoom and the size of the device: it is drawn at the inverse of the scale KorGE draws the Storybook with, so it is as large on a phone as on a 1280x900 window. Its buttons are 40 high, larger than `StorybookButton.HEIGHT`, for a finger. |
| Pinch              | Where the platform sends one, from the shared code: two fingers on a touch screen (the web on a phone or tablet) through KorGE's touch events, and the trackpad of a Mac in the desktop app through `onMagnify`. A pinch zooms around the point between the fingers, or under the pointer. The desktop app on Windows or Linux has no pinch: the buttons cover it. |
| Ctrl and the wheel | A desktop browser sends a trackpad pinch as the mouse wheel with Ctrl held, so the wheel with Ctrl zooms around the pointer, on every platform. Without Ctrl, the wheel keeps scrolling the sidebar.                                                                                           |
| Panning            | When zoomed in, dragging anything that does not drag itself moves the Storybook: the background, the bezel of the phone, the panels. The sidebar list, which scrolls by dragging, and the screen of the phone, where the view on show handles its own taps and drags (e.g.: the battlefield), keep their drags. Moving two fingers during a pinch also pans. |
| Limits             | The Storybook never leaves the screen: when larger than the screen on a side, it is panned at most to its edge on that side; when smaller, it is centred on that side.                                                                                                                         |
| Taps               | A drag that pans is not a tap: releasing it does not tap the button it started on.                                                                                                                                                                                                              |
| Screen size        | When the screen changes size (the phone rotates, the window is resized), Fit and Phone are worked out again. At Fit it stays at Fit; otherwise the zoom and the point at the centre of the screen are kept, within the limits.                                                                    |
| Kept               | The Storybook opens at Fit. The zoom and the position are kept when the story, an arg or a global changes, until the page is reloaded. They are not in the [link](../architecture/storybook.md#links): they depend on the screen of the person, not on what is on show. |
| Architecture       | Presentation only, in `storybook.adapters.presentation`: `Viewport`, plain Kotlin with no KorGE view, holds the zoom and the pan, and their rules (range, steps, presets, zooming around a point, limits, resizing); `ZoomView` wraps the Storybook, applies the `Viewport` to it, and turns the drags, pinches and wheel into it; `ZoomBarView` is the bar. The `Storybook` aggregate, its use cases and its events do not change. |
| Tests              | Unit tests of `Viewport`. View tests of `ZoomView` (a drag pans, a drag on the sidebar or the phone screen does not, a pinch faked through its page object, Ctrl and the wheel) and of `ZoomBarView`. `StorybookEndToEndTest` taps the bar. The real pinch is checked by hand, on a phone and on a Mac trackpad. |
| Skill              | The `storybook` skill takes a screen size, e.g.: a 390x844 portrait phone, and the zoom buttons to tap, so a screenshot can show Fit and Phone on a phone.                                                                                                                                       |
| Delivery           | Four milestones, each a few commits straight on `main`, green on CI. See [Milestones](#milestones).                                                                                                                                                                                              |
| Out of scope       | The game itself, zooming the view inside the phone only, a layout of the Storybook for portrait screens, keeping the zoom after a reload, rotating, and smoothing (inertia, animated zooms).                                                                                                      |

## Milestones

Update the **Status** column as the work progresses: ⬜ to do, 🟡 in progress, ✅ done. Add the commit or pull request
that completes each milestone to **Done in**.

| #  | Milestone                       | Done when                                                                                                                                                                  | Status | Done in |
|----|---------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------|---------|
| M1 | The whole screen and the bar    | The Storybook draws on the whole screen, looks the same at Fit, and the bar zooms in and out, to Fit and to the phone, kept across stories and when the screen changes size. | ✅      | a4d6e39, 90c5fc6 |
| M2 | Panning                         | Dragging the background, the bezel or a panel moves the Storybook within its limits; the sidebar and the phone screen keep their drags, and a pan is never a tap.             | ⬜      |         |
| M3 | Pinch, the trackpad and the wheel | Two fingers zoom and pan on a touch screen, the Mac trackpad zooms in the desktop app, and the wheel with Ctrl zooms everywhere.                                             | ⬜      |         |
| M4 | The docs and the skill          | The Storybook docs describe the zoom, and the `storybook` skill screenshots a story on a phone, at Fit and at Phone.                                                         | ⬜      |         |

Each milestone runs `jvmTest`, `jsTest` and `lintKotlin`, and looks at the Storybook with the
[`storybook` skill](../../.claude/skills/storybook/SKILL.md) at 1280x900, so Fit is checked to be unchanged.

### M1 commits

1. **The viewport.** `Viewport`, with its unit tests: the zoom between Fit and 4×, `zoomIn` and `zoomOut` by 1.25
   around a point, `fit()`, `zoomOnto(rectangle)` for the phone, the limits, and `resize(screen)`.
2. **The whole screen and the bar.** The Storybook starts with `KorgeDisplayMode.CENTER_NO_CLIP`. `StorybookScene` lays
   out the Storybook in a `ZoomView`, which applies the `Viewport` and follows the size of the screen, and puts the
   `ZoomBarView` above it, at the bottom right of the screen. With view tests of both, and `StorybookEndToEndTest`
   tapping the bar.
3. **The plan.** These commits, as built.

### M1 as built

The two commits are as listed, plus these decisions:

- **A frame, not a clip**: drawn on the whole screen, the Storybook showed what the window used to crop, e.g.: the
  stories of the sidebar scrolled above its list, above the Storybook on a portrait phone. A `ClipContainer` did not
  hide them, as KorGE's clip does not crop their rounded rectangles (found in M2 of the
  [story coverage](story-coverage.md#m2-as-built) already). `ZoomView` covers everything out of the 1280x900 Storybook
  with four rectangles of the background, `STORYBOOK_BACKGROUND_COLOR` (in `Main.kt`, next to the window size), zoomed
  and moved with it.
- **The bar's size**: drawn `virtualPixelsPerInch / 96` times its size, at least once: 1 on a 1280x900 window, about 3.3
  on a 390x844 phone, where it is about 340 points wide, its buttons 40 high.
- **`-`, not `−`**: the font of the Storybook has no `−` (U+2212), which drew an empty button.
- **Phone on a desktop window** is about 104%, and the phone is not in its middle: the Storybook can not be moved further
  right than the left of the window. On a 390x844 portrait phone, it is 309%, the phone at its real size, in its middle.
- **KorGE rounds the screen** to whole units, so at Fit the Storybook may be a fraction of a unit from the middle; the
  tests allow half a unit.
- **Taps on a phone are touches**: on a touch device KorGE ignores the mouse, so the bar did nothing when Playwright
  clicked it on an emulated phone, and worked when tapped with touch events (`Input.dispatchTouchEvent`). The skill in
  M4 has to tap with touches on a phone.
- **The end-to-end test** zooms in, changes an arg, and checks the zoom is kept, then taps Phone and Fit. The pre-commit
  hook regenerates the [dependency report](../architecture/dependencies.md), whose file count changed.
- **The bar hides a corner**: at Fit on a 1280x900 window, it covers the bottom right of the Actions, where a long list
  of actions ends; at Phone, the bottom of the phone screen. Noted, not changed: zooming in, or Phone, moves it away from
  what is under it, except the very bottom of the phone.

### M2 commits

1. **Panning.** `ZoomView` pans with a drag that starts outside the sidebar list and the screen of the phone, and keeps
   a drag that panned from tapping. With view tests.
2. **The plan.** These commits, as built.

### M3 commits

1. **Pinch.** Two fingers zoom around their middle and pan as it moves, from KorGE's touch events. `onMagnify` zooms
   around the pointer. With view tests faking the touches.
2. **Ctrl and the wheel.** The wheel with Ctrl zooms around the pointer; without Ctrl it scrolls the sidebar as before.
   With view tests.
3. **The plan.** These commits, as built, and what the pinch looked like on a phone and on a Mac.

### M4 commits

1. **The docs.** [Storybook](../architecture/storybook.md) (what it shows, and how it is built) describes the zoom.
2. **The skill.** The `storybook` skill takes a screen size and the zoom buttons to tap.
3. **The plan.** These commits, and the status set to done.

## Reference

The names are proposals: keep them unless an architecture test or a better name found while building asks otherwise,
and update this section when they change.

### The views and classes

| Name          | Kind                  | What it does                                                                                                                                  |
|---------------|-----------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
| `Viewport`    | Class, no KorGE view  | The zoom (1 at Fit, at most 4) and the position of the Storybook on the screen, and their rules. Knows the size of the screen and of the Storybook, in Storybook units. |
| `ZoomView`    | View                  | Holds the Storybook, scaled and placed as the `Viewport` says. Pans, pinches and zooms with the wheel. Follows the size of the screen.       |
| `ZoomBarView` | View                  | `−`, the zoom, `+`, `Fit` and `Phone`, at the bottom right of the screen, at a fixed size on the screen. Tells a delegate what was tapped.    |

The zoom bar and `ZoomView` are the Storybook's own views, like the sidebar: they are not views of the game, so they
need no stories.

### The screen in Storybook units

With `KorgeDisplayMode.CENTER_NO_CLIP`, KorGE scales the stage so 1280x900 fits the screen and centres it, and draws
around it: `views.actualVirtualBounds` is the screen, in stage units, e.g.: on a 390x844 portrait phone, about 1280
wide and 2770 high, starting about 935 above the stage. `Viewport` works in these units: at Fit the Storybook is at the
origin, at its size; the Phone preset fits the 414x868 `PhoneFrameView` in the screen.
