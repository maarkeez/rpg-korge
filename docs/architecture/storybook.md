# Storybook

The Storybook shows the views of the game in isolation, each in its states, without playing the game up to them, as
[Storybook](https://storybook.js.org) does for web components. It is the game itself, started on another page, and
running the real views, with the real images, texts and input. The version on `main` is published alongside the game,
at https://maarkeez.github.io/rpg-korge/storybook/, by the [`Deploy JS`](../../.github/workflows/deploy-js.yml)
workflow.

It serves three purposes:

- **Building a view**: change a view and see it at once, in each of its states, with `runJvmStorybookAutoreload`.
- **A catalogue**: an artist or a designer browses every view in every state, and shares a link to one of them.
- **Claude seeing a view**: the [`storybook` skill](../../.claude/skills/storybook/SKILL.md) opens a story with
  Playwright and saves a screenshot to look at.

To add a story, see [How to add a story](../testing/how-to-add-a-story.md).

## What it shows

- **The sidebar**, on the left, lists the components by their group, and the stories of each one. Tapping a story
  shows it. The list scrolls under the title and the search, with the mouse wheel or by dragging it, and scrolls to the
  story on show when it is out of sight, e.g.: when opened from a link.
- **The search**, under the title of the sidebar, lists only the stories that match what is typed, as it is typed:
  every word, ignoring case and in any order, must be in the group, the title of the component or the name of the
  story, e.g.: `shop sell` lists the **Sell** stories of **Screen / Shop**, and `shop` all of its stories. The `×`
  empties it, listing every story again, scrolled to the story on show. It never changes the story on show, is kept
  while stories are tapped, and is not in the [link](#links).
- **The phone**, in the middle, shows the story: the real view, at its real size, in a 390x844 screen, the game window,
  with the game background. What does not fit the screen is cropped, as in the game.
- **The toolbar**, on the top right, holds the globals: the language of the views on show.
- **The story info** names the component and the story, each with its description.
- **The Controls** edit the args the view is displayed with, one row per arg. `Reset` brings back the story's own.
- **The Actions** list the callbacks the view made when it was tapped, the newest on top. `Clear` empties them.
- **The zoom bar**, floating at the bottom right of the screen, zooms the whole Storybook: `-` and `+` by 1.25, `Fit`
  back to the whole Storybook (`100%`), and `Phone`, which fills the screen with the phone, at its real size on a
  portrait phone. It goes from Fit to 4×, and is as large on a phone as on the window.

The Storybook is drawn on the whole screen of the device: at Fit it looks as on the 1280x900 window, in the middle of
the screen, e.g.: a strip across a portrait phone, and zoomed in it fills it. Besides the bar, it zooms with two fingers
on a touch screen, with the trackpad of a Mac in the desktop app, and with the mouse wheel and Ctrl, which is also how a
desktop browser sends a pinch of its trackpad; each around the fingers or the pointer. Zoomed in, dragging anything but
the sidebar and the screen of the phone, which keep their own drags, moves it, never past its edges. The zoom is kept
when the story, an arg or a global changes, until the page is reloaded, and is not in the [link](#links): it depends on
the screen, not on what is on show.

The labels and descriptions of the Storybook itself are in English only: they are not in the translation catalogs, as
the translators only translate what the players see. The views on show follow the language global.

## Ubiquitous language

The Storybook uses Storybook's own terms, in the code, the docs and the app:

| Term      | Meaning                                                                                                                      |
|-----------|------------------------------------------------------------------------------------------------------------------------------|
| Component | A view of the game shown in the Storybook, e.g.: `ShopView`, titled **Screen / Shop**: its group, the subdomain of the view, and its title. It has a description, its controls, its default args and its stories. |
| Story     | A named state of a component, e.g.: **Sell, bag full**. It has a description, and the args it changes from the component's defaults. |
| Args      | The values a story displays its view with, e.g.: `mode = SELL`, `gold = 120`. One arg per control.                            |
| Control   | The typed editor of an arg, which also defines the values it accepts: a boolean, an int (between a minimum and a maximum), an enum (a button per option), a pick (going round a list of options, e.g.: the item ids), a text, or a color (its hex). |
| Action    | A callback the view made, e.g.: `shopSlotSelected(3)`. A story may map it to an arg change, as the game would react: tapping a slot of the shop sets `selectedSlot`. |
| Global    | A setting shared by every story, chosen in the toolbar. Only the language, for now.                                          |

Every component and story has an id, in kebab case, as in Storybook: the component id is its group and title (e.g.:
`screen-shop`), and the story id is the component id, two dashes and its name (e.g.: `screen-shop--sell-bag-full`).
They are written by hand, not derived from the title and name, so renaming a component or a story keeps the links
shared working.

## Links

Every state the Storybook shows has its own link, which shows exactly the same thing when opened, also after a reload:

```
https://maarkeez.github.io/rpg-korge/storybook/?story=screen-shop--sell&args=gold:0;selectedSlot:1&globals=language:es
```

- `story`: the story id. Without it, or when it is not a story (e.g.: a link to a story since renamed), the first
  story is shown.
- `args`: the args changed from the story's own, `<name>:<value>` separated by `;`, in the order of the controls.
- `globals`: the globals changed from their defaults, the same way.

The names and values are URL-encoded, so a `:` or `;` in a text is kept. A color is its hex without `#`, e.g.:
`FF375F`. An arg or a global that is unknown, or a value its control does not accept, is ignored, one by one: it keeps
its own value, and the others still apply. A link never crashes the Storybook.

The Storybook keeps the URL up to date as the story, an arg or a global changes, replacing it instead of adding to the
history, so the back button leaves the Storybook instead of stepping through every change. The actions are not kept.

## How it is built

The Storybook is the `storybook` subdomain, `com.mkz.rpg.storybook`, following the same clean architecture and
architecture tests as the rest of the game (see [Package structure](package-structure.md#storybook)). It ships in the
same bundle as the game.

### Domain

`Storybook` is the aggregate root: the catalogue (its components and their stories, controls and default args), the
story on show, its args, the globals, and the actions logged. It holds the rules:

- The catalogue has at least one component, each with at least one story, and the ids are unique and in kebab case.
- The control names are unique in a component, an int control has a minimum and a maximum, in order, and an enum or a
  pick control has options, each once. Every control has a default arg, every arg has a control, and its control
  accepts it. The globals follow the same rules.
- Only a story of the catalogue can be selected. Selecting another story shows its own args, keeping the globals, and
  empties the actions.
- An arg or a global can only change to a value its control accepts.
- The actions keep the 50 newest (`Storybook.MAXIMUM_ACTIONS`). Changing an arg keeps them.

The components and stories are its `Dto.ComponentDto` and `Dto.StoryDto`, plain data with no KorGE, so the domain is
unit and mutation tested as any other subdomain.

### Use cases

| Use case                                             | What it does                                                                                     |
|------------------------------------------------------|--------------------------------------------------------------------------------------------------|
| `InitialiseCatalogue`                                | Opens the catalogue and the globals, on the story, args and globals the link asks for.            |
| `SelectStory`                                        | Shows a story of the catalogue, with its own args.                                               |
| `ChangeArg`, `ResetArgs`                             | Changes an arg of the story on show, or brings back its own.                                     |
| `ChangeGlobal`                                       | Changes a global, e.g.: the language.                                                            |
| `RecordAction`, `ClearActions`                       | Logs a callback of the view on show, or empties the log.                                         |
| `SearchCatalogue`, `SearchSelectedStory`, `SearchArgs`, `SearchGlobals`, `SearchActions` | Read what the presenter shows.                                |

The commands that change what is on show keep it in the `StoryLocation` port (`search` and `replace`), and publish a
`StorybookEvent` (`StorySelected`, `ArgChanged`, ...) that the presenter listens to.

### Bindings

A story binding ties a component to the real view it shows: one `<View>Stories` object per view, e.g.:
[`ShopViewStories`](../../src/commonMain/kotlin/com/mkz/rpg/storybook/adapters/presentation/ShopViewStories.kt) for
`ShopView`, in `storybook.adapters.presentation`. It has two functions:

- `component(context)`: the component, with its controls, default args and stories, as plain data. The options of a
  control may come from the context, e.g.: the lootable item ids from the images loaded.
- `render(args, context)`: a new view of the game, displayed with the args, read by name (`args.int("gold")`). Its
  callbacks are handed to `context.action(...)`, which logs them and may change an arg.

The bindings are listed by hand in `StorybookApi.STORY_BINDINGS`, in the order the sidebar shows them, as JS has no
reflection to find them. They live in `adapters.presentation` because they build the views' texts, and only the
presentation may use `localization`. They feed the views plain data, never domain objects nor use cases of other
subdomains.

The view on show is rebuilt from scratch whenever the story, an arg or a global changes: it is simple and
deterministic, though its animations restart.

### Presentation

`StorybookScene` lays out the window, 1280x900, landscape: the `SidebarView`, the `PhoneFrameView`, and on its right
the `ToolbarView`, the `StoryInfoView`, the `ControlsView` and the `ActionsView`. The Controls may take at most
`ControlsView.MAXIMUM_HEIGHT`, so the Actions always fit under them. `StorybookPresenter` shows the catalogue and the
story on show, and turns what is tapped into the use cases, as any presenter of the game. The search is the sidebar's
alone: it keeps the catalogue the presenter displays, and lists what `CatalogueSearch`, plain Kotlin, finds in it as the
text changes, so neither the presenter nor the domain know of it. `StoryContext` gives the
bindings the images, loaded once, and the texts, in the language global.

The zoom is the presentation's alone, out of the domain and of the link, as a setting of the screen:

- `Viewport`, plain Kotlin, holds the zoom and where the Storybook is on the screen, and their rules: from Fit to 4×,
  zooming around a point, onto an area (the phone), panning within the edges, and following a new size of the screen.
- `ZoomView` holds the views the scene lays out, scaled and moved as its `Viewport` says, and the `ZoomBarView` above
  them, not zoomed. It turns the drags, the fingers, the trackpad and the wheel into the `Viewport`, and follows the
  size of the screen. Around the Storybook, it draws a frame of the background: KorGE's clip does not crop the rounded
  rectangles of the sidebar scrolled above its list, which the window used to crop.
- `Main.kt` starts the Storybook with `KorgeDisplayMode.CENTER_NO_CLIP`, so the stage fits 1280x900 in the middle of
  the screen and draws around it; `actualVirtualBounds` is the screen. The game keeps its letterbox.
- A view of the game that drags reads the drag in its own units (`localDXY`), so it follows the finger when zoomed,
  e.g.: the battlefield.

### Platforms and startup

The same bundle, and the same `main`, start the game or the Storybook. `isStorybookRequested()` chooses:

- **JS**: the page is in a `/storybook/` folder. The deploy copies the web build there, as KorGE reads the resources
  relative to the page URL, so the folder needs its own copy. The [`add-storybook.sh`](../../tools/storybook/add-storybook.sh)
  script does it, for the workflow and the skill alike.
- **JVM**: the `rpg.entry=storybook` system property, which `./gradlew runJvmStorybook` and
  `./gradlew runJvmStorybookAutoreload` set (and the `runJvmStorybookAutoreload` run configuration in `.run/`). The JVM
  has no URL, so it opens on the first story, and keeps nothing.

The platform specific code lives in two adapter packages, declared in `src/commonMain` and implemented in `src/jsMain`
and `src/jvmMain`:

- `storybook.adapters.location`: where what is on show is kept. `BrowserStoryLocation` reads and writes the page URL,
  through `LocationQuery`, which encodes and decodes the query and is tested on both platforms. `NoStoryLocation`, on
  the JVM, keeps nothing.
- `storybook.adapters.page`: the readiness signal. Once the story on show has rendered, `BrowserStorybookPage` titles
  the page `Storybook – <story id>`, e.g.: `Storybook – screen-shop--sell`, which a script waits for before taking a
  screenshot. Loading the bundle and its resources takes several seconds, with nothing else to tell it is over.
  `NoStorybookPage`, on the JVM, tells no one.

### Architecture rules

Besides the rules of every subdomain, three architecture tests keep the Storybook in place:

- **Dependencies**: `storybook` only depends on `shared`, `localization`, and the `adapters.presentation` package of
  the other subdomains, and nothing depends on it but `Main.kt`
  ([`StorybookDependencyArchitectureTest`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/architecture/StorybookDependencyArchitectureTest.kt)).
- **Coverage**: every view of an `adapters.presentation` package has a `<View>Stories` binding, listed in
  `StorybookApi`, with no exception
  ([`StoryCoverageArchitectureTest`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/architecture/StoryCoverageArchitectureTest.kt)).
- **Views**: so every view can have its stories, which feed it plain data, no view of the game imports the domain nor
  the use cases of a subdomain (`com.mkz.rpg.<subdomain>.domain` or `.usecases`). The presenters map the domain to the
  view data the view draws
  ([`ViewArchitectureTest`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/architecture/ViewArchitectureTest.kt)).

## Tests

- **Unit tests** of the `Storybook` aggregate and of every use case, with `FakeStoryLocation`, and of the `Viewport`
  and the `CatalogueSearch`.
- **View tests** of the Storybook's own views, through their page objects (e.g.: `ControlsViewExtension`), and
  `StorybookPresenterTest`, with fake views and `FakeStorybookPage`. The zoom's fake the fingers, the trackpad and the
  wheel (`pinch`, `magnify`, `scrollZoom` in `ZoomViewExtension`).
- **The smoke test**, [`StorySmokeTest`](../../src/commonTest/kotlin/com/mkz/rpg/storybook/adapters/presentation/StorySmokeTest.kt),
  on the JVM and JS: it renders every story of every binding with the game resources and texts, opens the catalogue of
  every binding (so an invalid default arg fails), checks the controls of every component fit
  `ControlsView.MAXIMUM_HEIGHT`, and lists every component and story id, so changing one is deliberate.
- **The end-to-end test**, `StorybookEndToEndTest`: starts the Storybook on a link, and taps a story, a control, a
  language, the shop and the zoom bar, and types a search, as a person would.
- **The page**, with the [`storybook` skill](../../.claude/skills/storybook/SKILL.md): the JS bundle in a headless
  browser, which no automated test covers yet, also on a phone (`--screen 390x844 --touch`) and zoomed (`--zoom`).
  Visual regression is left for later. A real pinch, on a phone or the trackpad of a Mac, is only checked by hand.
