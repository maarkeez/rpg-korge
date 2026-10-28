# Plan: Storybook

> **Status:** in progress, M1 and M2 done. Track the progress in [Milestones](#milestones).
>
> **Summary:** A Storybook for the game's views, like [Storybook](https://storybook.js.org) for web components: a live
> KorGE app that shows each view in isolation, in each of its states, without playing the game up to it. A sidebar lists
> the components and their stories, the view is shown at its real size in a phone frame, the Controls edit the values it
> is displayed with, and the Actions log what the player tapped. Every story has its own URL, so a link can be shared
> with an artist or a designer, and Claude can open it with Playwright and look at a screenshot. It is a new `storybook`
> subdomain of the game, following the same clean architecture and architecture tests, and it is published next to the
> game, at `/storybook/`.

## Decisions

| Topic            | Decision                                                                                                                                                                                                                                          |
|------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Purpose          | Build and tweak a view in isolation, browse a catalogue of every view in each of its states, and let Claude see a view after changing it. Visual regression is left for later.                                                                    |
| Audience         | The author and Claude first, then collaborators such as artists and designers: it is hosted and self-explanatory, each component and story shows a description.                                                                                 |
| Form             | A live KorGE app, running the real views with the real renderer and input, on the JVM (with autoreload) and on JS. Not a static gallery of images.                                                                                                |
| Granularity      | Views and composite views (e.g.: `HealthBarView`, `InventorySlotView`, `ShopView`, `BattleHudView`). Not whole scenes: they need the full domain state, which the end-to-end tests and the [game snapshots](../testing/how-to-take-a-game-snapshot.md) already cover. |
| Location         | A new `storybook` subdomain in the game module, `com.mkz.rpg.storybook`, so every existing architecture test applies to it. It ships in the same JS bundle as the game.                                                                          |
| Hosting          | Published by the [`Deploy JS`](../../.github/workflows/deploy-js.yml) workflow at `/storybook/`, as the [dependency report](../architecture/dependencies.md) is at `/dependencies/`: https://maarkeez.github.io/rpg-korge/storybook/.             |
| Entry            | One bundle, a startup flag choosing the game or the Storybook. On JS, the page being in the `/storybook/` folder sets it. On the JVM, a `runJvmStorybook` task and a `.run/` configuration with autoreload set it. The game menu has no Storybook button.             |
| Ubiquitous language | Storybook's own terms, known to the author and to collaborators: **Component**, **Story**, **Args**, **Controls**, **Actions**, **Globals**. See [Glossary](#glossary).                                                                       |
| Stories          | Fixed, named states of a component, with a stable id. Their args can be edited live with the Controls.                                                                                                                                           |
| Controls         | Boolean, int (with a minimum and a maximum), enum, text, pick from a list (e.g.: a lootable item id) and color.                                                                                                                                   |
| Actions          | Every callback of the view (its `Delegate`, or its lambdas) is logged in the Actions panel. A story can also map an action to an arg change (e.g.: `shopSlotSelected(3)` sets `selectedSlot` to 3), as Storybook's `useArgs`.                     |
| Globals          | Only the language in v1, from the real localization catalogs. A viewport, a background and an animation pause are left for later.                                                                                                                |
| Deep links       | The URL holds the story, the args changed from its defaults and the globals, e.g.: `?story=screen-shop--sell&args=mode:SELL;buyerGold:0&globals=language:es`. Opening it shows exactly the same thing, also after a reload.                      |
| Assets           | The real resources bundle (`BattleSceneResourcesLoader`) and the real localization catalogs. No placeholders.                                                                                                                                     |
| Layout           | Drawn all in KorGE, so the JVM and JS look the same: a landscape window, the sidebar on the left, the view in a 390x844 phone frame (`GAME_WINDOW_SIZE`) in the middle, the Controls and the Actions on the right, the Globals in a toolbar.      |
| Storybook texts  | The labels, titles and descriptions of the Storybook itself are in English only, out of `messages.pot`: translators only translate what the players see. The views on show follow the language Global.                                           |
| Architecture     | The story metadata (components, stories, args, controls) is plain data in `storybook.domain`, with no KorGE. The binding from the args to a real view lives in `storybook.adapters.presentation`, one file per component (e.g.: `ShopViewStories`), registered in an explicit list held by `StorybookApi`, as JS has no reflection. |
| Dependencies     | `storybook` only depends on the `adapters.presentation` packages of other subdomains, plus `shared` and `localization`: stories feed the views plain data, never domain objects nor use cases. Enforced by an architecture test. Nothing depends on `storybook`. |
| Coverage         | An architecture test requires every view of an `adapters.presentation` package to have a story, with an allowlist of the views that have none yet. As the restricted subdomains, the allowlist can only shrink. The Storybook's own views are left out. |
| Testing          | Unit tests for the domain and the use cases, view tests with page objects (`…Extension`) for the Storybook's own views, and a smoke test rendering every story with its default args, on the JVM and JS.                                      |
| Claude           | A `.claude/skills/storybook` skill: build and serve the JS bundle, open a story URL with Playwright, and save a screenshot to look at. It works on a development machine, in cloud sessions and on CI.                                            |
| Docs             | `docs/architecture/storybook.md` (explanation) and `docs/testing/how-to-add-a-story.md` (how-to), in the style of the existing docs, written in M4.                                                                                                  |
| Pilot components | `HealthBarView` (a leaf view, no delegate), `InventorySlotView` (a real sprite and a selection) and `ShopView` (a composite view with a `Delegate`). Together they use every control type and the actions.                                       |
| Delivery         | Four milestones, each one green on CI and deployable. See [Milestones](#milestones).                                                                                                                                                              |
| Out of scope     | Visual regression, whole scenes, the viewport and background Globals, pausing or stepping animations, a search in the sidebar, interaction tests run from a story (Storybook's `play` functions), documentation pages beyond the descriptions. |

## Glossary

| Term      | Meaning                                                                                                                                                       |
|-----------|---------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Component | A view of the game shown in the Storybook, e.g.: `ShopView`. Grouped by its subdomain in the sidebar, e.g.: **Screen / Shop**. Has a description and default args. |
| Story     | A named state of a component, e.g.: **Shop / Sell, inventory full**. Its args override the component's default args.                                          |
| Args      | The values a story displays its view with, e.g.: `mode = SELL`, `buyerGold = 0`. One arg per control.                                                         |
| Control   | The typed editor of an arg in the Controls panel, e.g.: an enum select for `mode`. It also defines which values are valid.                                     |
| Action    | A callback the view made, e.g.: `shopSlotSelected(3)`, listed in the Actions panel. A story may map it to an arg change.                                        |
| Global    | A setting shared by every story, chosen in the toolbar, e.g.: the language.                                                                                   |

## Milestones

Update the **Status** column as the work progresses: ⬜ to do, 🟡 in progress, ✅ done. Add the commit or pull request
that completes each milestone to **Done in**.

| #  | Milestone                     | Scope                                                                                                                                                                                                                         | Done when                                                                                                                                                       | Status | Done in |
|----|-------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|--------|---------|
| M1 | Walking skeleton              | The `storybook` subdomain (catalogue, `SelectStory`), the sidebar, the phone frame, the `?story=` deep link, the startup flag, `runJvmStorybook` and its `.run/` configuration, the `/storybook/` deploy, the smoke test, the coverage rule with a full allowlist, the dependency rule, and `HealthBarView` with fixed stories (full, half, empty). | `runJvmStorybook` and https://maarkeez.github.io/rpg-korge/storybook/ show the `HealthBarView` stories, a story URL opens that story, and `jvmTest` and `jsTest` pass. | ✅      | ae1d361, c5a7073, 338bd4e, 4b29dae |
| M2 | Args and Controls             | The `StorySession` args and globals (`ChangeArg`, `ResetArgs`, `ChangeGlobal`), the six control types, the args and globals in the URL, the language Global, the Controls panel and the toolbar, `HealthBarView` with controls and `InventorySlotView`. | Editing a control re-renders the view and updates the URL, opening that URL restores the same args and language, and `InventorySlotView` leaves the allowlist. | ✅      | c1fe60f, 1fa994a |
| M3 | Actions                       | `RecordAction` and `ClearActions`, the Actions panel, the action-to-arg mapping, and `ShopView`.                                                                                                                              | Tapping the shop logs its callbacks, tapping a slot selects it through the mapping, and `ShopView` leaves the allowlist.                                       | ⬜      |         |
| M4 | Claude and collaborators      | The `storybook` skill, the readiness signal it waits for, `docs/architecture/storybook.md`, `docs/testing/how-to-add-a-story.md`, the links from the existing docs, and a review of the descriptions shown in the app.        | Claude, following the skill alone, screenshots a story in a cloud session, and a collaborator can add a story following the how-to alone.                       | ⬜      |         |

Each milestone also updates the docs it changes (e.g.: [Package structure](../architecture/package-structure.md) gets
the dependency rule in M1) and regenerates the [dependency report](../architecture/dependencies.md).

### M1 commits

1. **The `storybook` domain and use cases.** `Component`, `Story`, the catalogue and its ids, `StorySession` with the
   selected story, `SelectStory`, and their unit tests. No view yet.
2. **The Storybook scene.** The startup flag, the Storybook window, the sidebar, the phone frame, `StorybookApi` with
   its registry, `HealthBarViewStories`, the smoke test, and the view tests of the sidebar.
3. **Running and hosting it.** `runJvmStorybook`, the `.run/` configuration, the `jsMain` entry flag, the deep link,
   and the `/storybook/` step of `Deploy JS`.
4. **The architecture rules.** The dependency rule and the coverage rule with its allowlist, and the docs of both.

### M2 commits

1. **Args, Controls and the language Global.** The controls, default args, story args and globals in `Storybook`, with
   `ChangeArg`, `ResetArgs`, `ChangeGlobal`, `SearchArgs` and `SearchGlobals`; `StoryLocation` reading and writing the
   story, args and globals, through `LocationQuery`; `StoryBinding` rendering from `Args`; the `ControlsView` and the
   `ToolbarView`; `HealthBarViewStories` with controls, and `InventorySlotViewStories`, out of the allowlist. With the
   unit, view, presenter, smoke and end-to-end tests, and the dependency report.
2. **The plan.** These commits, and the Reference updated to what was built.

## Reference

Detailed technical notes for whoever implements the plan. The names are proposals: keep them unless an architecture test
or a better name found while building asks otherwise, and update this section when they change.

### Package layout

Following [Package structure](../architecture/package-structure.md), with the leaf packages only. Built in M1 unless
marked with the milestone that adds it:

```
com.mkz.rpg.storybook
├── domain                  Storybook (the aggregate root: the catalogue, the story on show, its args and the globals;
│                           the actions log in M3), StorybookRepository, StorybookEvent, StorybookError
├── usecases
│   ├── commands            InitialiseCatalogue, SelectStory; ChangeArg, ResetArgs, ChangeGlobal (M2);
│   │                       RecordAction, ClearActions (M3)
│   ├── queries             SearchCatalogue, SearchSelectedStory; SearchArgs, SearchGlobals (M2)
│   └── services            StoryLocation (the port to the URL)
└── adapters
    ├── presentation        StorybookApi, StorybookScene, StorybookPresenter, the Storybook's own views (SidebarView,
    │                       PhoneFrameView, StoryInfoView, StorybookButton; ToolbarView and ControlsView in M2; the
    │                       Actions in M3), StoryBinding, StoryContext, Args, and one <View>Stories object per component
    ├── storage             InMemoryStorybookRepository
    └── location            isStorybookRequested() and platformStoryLocation() (expect, in commonMain),
                            NoStoryLocation (commonMain, used on the JVM), LocationQuery (commonMain, M2),
                            BrowserStoryLocation (jsMain)
```

- The domain package only allows the aggregate root, its repository, events and errors, named after the subdomain
  (`DomainArchitectureTest`), so there is a single aggregate, `Storybook`, instead of a catalogue and a session: the
  components and stories are its `Dto.ComponentDto` and `Dto.StoryDto`, and its private nested classes validate them.
  The args, globals and actions of M2 and M3 go into it too.
- `StorybookApi` is the only place where the `storybook` use cases are instantiated
  (`UseCaseInstantiationArchitectureTest`). It also holds `STORY_BINDINGS`, the list of every `<View>Stories`.
- The `<View>Stories` files live in `adapters.presentation`, not in a package of their own: they build the views'
  `Texts`, and only `adapters.presentation` packages may import `localization` (`LocalizationArchitectureTest`).
- The Dtos follow the existing rules: data classes and enum classes only, no methods, no default values (see
  [Dto of data and enums](dto-of-data-and-enums.md), [Dto without methods](dto-without-methods.md) and
  [Dto without default values](dto-without-default-values.md)). An arg value in M2 is then an enum type with optional
  fields (e.g.: `ArgDto(type, boolean, int, text)`) built by companion factories, never a sealed class. Built in M2:
  `ControlDto(name, type, minimum, maximum, options)` with `boolean`, `int`, `enum`, `text`, `pick` and `color`
  factories, `ArgDto` with `boolean`, `int` and `text` factories (an enum, pick or color value is a text), and
  `LocationDto(storyId, args, globals)`, what a link keeps, as text. Inside the aggregate, private sealed `Control`s and
  `Value`s validate and parse them.

### Domain model

- **Catalogue.** The components, each with an id, a group (its subdomain, e.g.: `Screen`), a title, a description and
  its stories, each with an id, a name and a description. Built once by `InitialiseCatalogue` from `STORY_BINDINGS`.
  Rules: there is at least one component; every component has at least one story; the ids are unique. In M2 each
  component also gets its controls and default args, and each story its args, valid for the controls: the control
  names are unique in a component (`RepeatedControl`), an int control has a minimum and a maximum, in order, and an
  enum or pick control at least one option, each once (`InvalidControl`), every control has a default arg
  (`MissingValue`), every arg has a control (`ControlNotFound`) and its control accepts it (`InvalidValue`). The
  globals follow the same rules: `InitialiseCatalogue` gets the global controls and their defaults besides the
  components.
- **Values.** A boolean; an int within the minimum and the maximum of its control; an option of an enum or a pick
  control; any text; and a color as its hex in upper case, without `#` (e.g.: `FF375F`). Written as text in a link,
  the domain parses them by their control, so a link may write a color in lower case.
- **Ids.** Kebab case, as Storybook: the component id is its group and title (e.g.: `screen-health-bar`), and the
  story id `<component id>--<story name>` (e.g.: `screen-health-bar--full`). They are written in the binding, not
  derived from the title and name, so renaming a component or a story keeps the links shared. The domain checks that a
  component id is in kebab case and that a story id is its component id, two dashes, and a name in kebab case.
  `StorySmokeTest` lists every id, so changing one is deliberate.
- **The story on show.** Opened on the story the location asks for, or on the first story when none is asked for or
  it is not in the catalogue (e.g.: a link to a renamed story): it is ignored, never a failure. Only a story of the
  catalogue can be selected; selecting the story on show changes nothing. In M2, selecting another story resets the
  args to its own, keeping the globals, and an arg or a global can only be changed to a value its control accepts;
  changing it to the value it has, or resetting args none of which were changed, changes nothing. In M3, the log keeps the last 50
  actions, the newest first.
- **Events** (`StorybookEvent`): `CatalogueInitialised` and `StorySelected` in M1; `ArgChanged(storyId, argName)`,
  `ArgsReset(storyId)`, `GlobalChanged(globalName)` (M2), `ActionRecorded`, `ActionsCleared` (M3). The presenter subscribes to them to re-render, as
  `BattlefieldPresenter` does.

### Binding a story to a view

A `StoryBinding` per component, in `adapters.presentation`, builds the real view. In M1 the stories are fixed, so it
renders a story by its id, e.g.:

```kotlin
object HealthBarViewStories : StoryBinding {
    override val component = ComponentDto(id = "screen-health-bar", group = "Screen", title = "Health bar", description = "...", stories = listOf(full, half, empty))

    override fun render(storyId: String, context: StoryContext): View =
        HealthBarView(SIZE).apply { display(remaining = remainingHealth.getValue(storyId), maximum = MAXIMUM_HEALTH) }
}
```

From M2 it renders the current args instead, `fun render(args: Args, context: StoryContext): View`, where `Args`
reads them by name (`args.int("remaining")`, `boolean`, `text`, `color`), and the stories become their args. The
component is a function of the context, `fun component(context: StoryContext): ComponentDto`, as the options of a pick
control may come from the images loaded (see `InventorySlotViewStories`):

```kotlin
object ShopViewStories : StoryBinding {
    override val component = /* id, group, title, description, controls, default args, stories */

    override fun render(args: Args, context: StoryContext): View =
        ShopView(context.resourcesLoader, context.texts).apply {
            setDelegate(object : ShopView.Delegate {
                override fun shopSlotSelected(index: Int) = context.action("shopSlotSelected", index, setArg = "selectedSlot" to index)
                // ...
            })
            display(/* from args */)
            selectSlot(args.int("selectedSlot"))
        }
}
```

- `StoryContext` gives the binding the shared `BattleSceneResourcesLoader` and the `Texts` (in English in M1, in the
  selected language from M2: the presenter selects the language global on the `Texts` before every render), and from M3 `action(...)`, which runs `RecordAction` and, when it maps the action to an
  arg, `ChangeArg`.
- The view is rebuilt from scratch whenever the story changes (and from M2 when the args or the globals change):
  simpler and deterministic. Its animations restart, which is acceptable in v1.
- `PhoneFrameView` shows it at its real size, in the middle of a 390x844 screen with the game background, which crops
  what does not fit. A full-screen view placed at the bottom (e.g.: `BattleHudView`) may need a placement in its
  binding.
- A view taking lambdas instead of a `Delegate` (e.g.: `MenuView`) passes lambdas calling `context.action(...)`.
- The options of a pick control come from presentation sources only, e.g.: `resourcesLoader.bitmaps("lootableItem")`
  for the item ids, so the dependency rule holds. A pick that may be empty adds an option of its own for it, e.g.:
  `none` for an empty inventory slot. If a story ever needs a Dto of another subdomain, revisit the rule
  first.
- The language Global is a pick control of the language codes of `Texts.languages()` (`LocalizationApi.searchLanguages`),
  English first, its default, named `StorybookApi.LANGUAGE_GLOBAL`, and applies with `Texts.selectLanguage`. The
  toolbar names each language in its own language, e.g.: `Español`.
- `StorybookApi.init(context)` builds the components of its story bindings, every one unless a test passes fewer
  (`storyBindings`), and `bindingOf(componentId)` finds the binding of the component on show.
- The Controls panel (`ControlsView`) lists one row per control, with a `Reset` button: a boolean is a switch, an int
  `-` and `+` around it (stopping at its minimum and maximum), an enum a button per option, a pick `<` and `>` going
  round its options, a text an input, and a color a swatch and an input of its hex (undone when it is no hex). A text
  or a color is changed when it is entered or left. The views only ask for values their control accepts. Their buttons
  are `StorybookButton`s, which paint their own selection, as `UIButton`'s hover tween overwrites it.
- No story of M2 uses the text and color controls: `HealthBarView` has two ints, and `InventorySlotView` a pick and
  three booleans. They are covered by `StorybookTest` and `ControlsViewTest`, and only seen in the app once a story
  uses them (`ShopView` in M3 brings an enum).

### Startup flag and hosting

- `Main.kt` keeps a single `main`: `isStorybookRequested()` chooses between `startGame()` in the game window and
  `startStorybook()` in `STORYBOOK_WINDOW_SIZE` (1280x900, scaled to fit as the game is). `startStorybook(storyLocation)`
  is shared with the end-to-end tests, as `startGame` is. `isStorybookRequested()` and `platformStoryLocation()` are
  `expect` functions of `storybook.adapters.location`, with an `actual` in `src/jsMain` (the page is in a
  `/storybook/` folder) and in `src/jvmMain` (the system property `-Drpg.entry=storybook`). The Konsist tests scan
  the new source sets with no change, as they are production source sets.
- The layout: the sidebar (260 wide) on the left, the 414x868 phone frame (the 390x844 screen and its bezel) 16 from
  the top, and `StoryInfoView` on the right, with the component and the story, each with its description. The Controls
  and the Actions go under it in M2 and M3, and the toolbar with the Globals above it. Built in M2: the toolbar 32
  from the top, the story info under it, and the Controls under the story info (from y = 344).
- `runJvmStorybook` and `runJvmStorybookAutoreload`: KorGE makes both for an extra entry point,
  `entrypoint("storybook", "com.mkz.rpg.MainKt")` in `build.gradle.kts`, which also sets the system property on both.
  `.run/runJvmStorybookAutoreload.run.xml` runs the second one, as `.run/runJvmAutoreload.run.xml` does the game.
- `/storybook/`: the `Add Storybook` step of `Deploy JS`, after the bundle is built, copies the files of `build/www`
  (the page, the JS and `resources.zip`) into `build/www/storybook`, guarded by a `test` and a `grep`. A copy of the
  page with `<base href="../">` does not work: KorGE's JS loader reads the resources relative to
  `document.location.href`, ignoring the base, so `resources.zip` is not found.

### URL

- `?story=<story id>` in M1. From M2: `&args=<name>:<value>;<name>:<value>&globals=<name>:<value>`, as Storybook. Only
  the args changed from the story's own, in the order of their controls, and the globals changed from their defaults
  are written. The names and values are URL-encoded (`URL.encodeComponent`, so a `:` or `;` in a text is kept), a color
  as its hex without `#`. The other parameters of the page are kept.
- `StoryLocation` (a port in `usecases.services`) has `search(): LocationDto` and `replace(location: LocationDto)`. It is
  read by `InitialiseCatalogue` at startup, and written with `Storybook.toLocation()` by `SelectStory`, `ChangeArg`,
  `ResetArgs` and `ChangeGlobal`, with `history.replaceState`, so the back button does not step through every change.
  `LocationQuery` (commonMain, so it is tested on both platforms) encodes and decodes the query; `BrowserStoryLocation`
  writes the page path, which is absolute, with the query and the hash. `NoStoryLocation`, on the JVM, reads nothing
  and writes nothing.
- An unknown story in the URL is ignored, never a crash: the Storybook opens the first story, and the args of the URL
  are ignored too, as they were for another story. From M2, an unknown or invalid arg or global, or a malformed escape,
  is ignored the same way, one by one: it keeps its own value, and the others still apply. They are not logged.

### Architecture tests

- **Dependency rule**: `StorybookDependencyArchitectureTest`. The files of `com.mkz.rpg.storybook` only import other
  subdomains from a declaration of their `adapters.presentation` package (not from a package nested in it, e.g.:
  `screen.adapters.presentation.battlefieldHud`), except `shared` and `localization`, which they may import entirely.
  No other subdomain imports `storybook`; only `Main.kt` does. `storybook` is also in the table of
  [Package structure](../architecture/package-structure.md#dependencies-between-subdomains).
- **Coverage rule**: `StoryCoverageArchitectureTest`. Every top-level class of an `adapters.presentation` package that
  extends a KorGE view, directly or through another class of the project (e.g.: `HealthBarView` → `BarView` →
  `UIContainer`), has a `<View>Stories` binding, unless it is in the allowlist. Konsist 0.13 only gives the direct
  parents, so the test walks them through the classes of the project up to a list of KorGE view classes. Presenters,
  scenes and the views of `storybook` itself are left out. An allowlisted view that has a story fails the test, and so
  does an allowlisted name that is no view, so the allowlist can only shrink.
- **Registry rule**, in the same test: every `<View>Stories` object is named after a view of the game, and listed in
  `StorybookApi.kt`, so none is forgotten by the smoke test nor the app.
- Mutation testing: PIT already leaves out the classes declared directly in `<subdomain>.adapters.presentation`, so the
  `storybook` domain and use cases are mutation tested, and its presentation is not.

### Tests

- Unit tests of the domain and the use cases, with fakes (`FakeStoryLocation`), as any subdomain. See
  [How to add a new test](../testing/how-to-add-a-new-test.md).
- View tests of the Storybook's own views, with their page objects (`SidebarViewExtension`, `PhoneFrameViewExtension`,
  `StoryInfoViewExtension`), as `ViewTestPageObjectArchitectureTest` requires, and `StorybookPresenterTest` with fake
  views.
- The smoke test, `StorySmokeTest` in `commonTest`, so it runs on the JVM and JS: for every story of the registry, it
  renders it in a `ViewsForTesting`, with `BattleSceneResourcesLoaderMother` and `TestCatalogs`, and fails on any
  exception (with its default args from M2). It also lists every component and story id.
- `StorybookEndToEndTest`: starts the Storybook on a story id, and taps another story in the sidebar. In M2: starts it
  on a link with args and a language, taps a control, and taps a language in the toolbar. In M3: tap the view, check
  the Actions.
- In M2, `ControlsViewTest` and `ToolbarViewTest` (with `ControlsViewExtension` and `ToolbarViewExtension`),
  `LocationQueryTest`, and the smoke test also opens the catalogue of every binding, so a default arg its control does
  not accept (e.g.: an item with no image) fails there.

### Claude skill

- `.claude/skills/storybook/SKILL.md`: build the bundle (`./gradlew browserReleaseWebpack`, as `Deploy JS`), add the
  `/storybook/` page the same way, serve `build/www` (e.g.: `python3 -m http.server -d build/www`), open
  `http://localhost:8000/storybook/?story=…` with Playwright, wait for the readiness signal, and save a screenshot under
  the scratchpad, or `build/storybook`, to read it.
- **Readiness signal**: once the selected story has rendered, the Storybook sets a value Playwright can wait for, e.g.:
  `document.title = "Storybook – <story id>"` or a `window.storybookRenderedStory` set through the `jsMain` adapter.
  Without it, a screenshot may be taken before the resources bundle is loaded: in M1, waiting about 8 seconds was
  enough locally.
- In a cloud session, Chromium and Playwright are already installed: do not install them. Headless Chromium renders
  WebGL in software, so check in M4 that KorGE renders there, and that it also does on the CI runners, as visual
  regression would need it. Locally, `playwright-core` driving the installed Chrome with
  `--use-angle=swiftshader --enable-unsafe-swiftshader` rendered the Storybook and its clicks (a click needs the mouse
  moved over the row first, and a short press).

### Technical risks checked in M1

| Risk                                                                    | Outcome                                                                                                          |
|-------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------|
| KorGE's JS loader does not follow `<base href="../">`                   | It does not: `resourcesVfs` is relative to `document.location.href`. The fallback copies `build/www`.            |
| Adding `jsMain` and `jvmMain` changes what the Konsist tests scan       | They are production source sets, so the tests scan them with no change.                                          |
| `runJvmAutoreload` can not pass a system property                       | It can: KorGE makes `runJvmStorybookAutoreload` for the extra entry point, and `build.gradle.kts` sets the property on it as on `runJvmStorybook`, which was run. |
| Konsist does not resolve indirect parents of a view                     | It does not (0.13): the coverage test walks the project parents up to a list of KorGE view classes.              |
| The smoke test is slow, rendering every story on JS                     | Not with three stories: it renders every story on both. Time `jsTest` again as stories are added.                 |
