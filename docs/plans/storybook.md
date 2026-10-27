# Plan: Storybook

> **Status:** planned. Track the progress in [Milestones](#milestones).
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
| Entry            | One bundle, a startup flag choosing the game or the Storybook. On JS, `/storybook/index.html` sets it. On the JVM, a `runJvmStorybook` task and a `.run/` configuration with autoreload set it. The game menu has no Storybook button.             |
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
| M1 | Walking skeleton              | The `storybook` subdomain (catalogue, `SelectStory`), the sidebar, the phone frame, the `?story=` deep link, the startup flag, `runJvmStorybook` and its `.run/` configuration, the `/storybook/` deploy, the smoke test, the coverage rule with a full allowlist, the dependency rule, and `HealthBarView` with fixed stories (full, half, empty). | `runJvmStorybook` and https://maarkeez.github.io/rpg-korge/storybook/ show the `HealthBarView` stories, a story URL opens that story, and `jvmTest` and `jsTest` pass. | ⬜      |         |
| M2 | Args and Controls             | The `StorySession` args and globals (`ChangeArg`, `ResetArgs`, `ChangeGlobal`), the six control types, the args and globals in the URL, the language Global, the Controls panel and the toolbar, `HealthBarView` with controls and `InventorySlotView`. | Editing a control re-renders the view and updates the URL, opening that URL restores the same args and language, and `InventorySlotView` leaves the allowlist. | ⬜      |         |
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

## Reference

Detailed technical notes for whoever implements the plan. The names are proposals: keep them unless an architecture test
or a better name found while building asks otherwise, and update this section when they change.

### Package layout

Following [Package structure](../architecture/package-structure.md), with the leaf packages only:

```
com.mkz.rpg.storybook
├── domain                  Component, Story, Control, ArgValue, Args, Globals, Action, StorySession,
│                           StorySessionRepository, StorybookEvent, StorybookError
├── usecases
│   ├── commands            InitialiseCatalogue, SelectStory, ChangeArg, ResetArgs, ChangeGlobal,
│   │                       RecordAction, ClearActions
│   ├── queries             SearchCatalogue, SearchSelectedStory
│   └── services            StoryLocation (the port to the URL)
└── adapters
    ├── presentation        StorybookApi, StorybookScene, the Storybook's own views and presenters
    │                       (sidebar, phone frame, toolbar, Controls, Actions), StoryBinding,
    │                       StoryContext, and one <View>Stories file per component
    ├── storage             InMemoryStorySessionRepository, InMemoryCatalogueRepository
    └── location            BrowserStoryLocation (jsMain), NoStoryLocation (JVM)
```

- `StorybookApi` is the only place where the `storybook` use cases are instantiated
  (`UseCaseInstantiationArchitectureTest`).
- The `<View>Stories` files live in `adapters.presentation`, not in a package of their own: they build the views'
  `Texts`, and only `adapters.presentation` packages may import `localization` (`LocalizationArchitectureTest`).
- The Dtos follow the existing rules: data classes and enum classes only, no methods, no default values (see
  [Dto of data and enums](dto-of-data-and-enums.md), [Dto without methods](dto-without-methods.md) and
  [Dto without default values](dto-without-default-values.md)).

### Domain model

- **Catalogue.** The components, each with an id, a group (its subdomain, e.g.: `Screen`), a title, a description, its
  controls, its default args, and its stories. Built once by `InitialiseCatalogue` from the registry of
  `StorybookApi`. Rules: the ids are unique; every story's args name a control of its component and hold a valid value
  for it.
- **Ids.** Kebab case, as Storybook: the component id is `<group>-<title>` (e.g.: `screen-shop`), and the story id
  `<component id>--<story name>` (e.g.: `screen-shop--sell-inventory-full`). They are stable: renaming one breaks the
  links shared, so a test lists them.
- **Control** (sealed): `BooleanControl`, `IntControl(min, max)`, `EnumControl(options)`, `TextControl`,
  `PickControl(options)` and `ColorControl`. Each validates an `ArgValue` (sealed: boolean, int, text, color as
  `RRGGBB` or `RRGGBBAA`). An enum and a pick hold their option as text.
- **StorySession** (the aggregate root): the selected story, its current args (the story's args over the component's
  default args, then the changes), the globals and the actions log. Rules: only a story of the catalogue can be
  selected; an arg can only be changed to a value its control accepts; selecting another story resets the args to its
  own; the log keeps the last 50 actions, the newest first.
- **Events** (`StorybookEvent`): `StorySelected`, `ArgChanged`, `ArgsReset`, `GlobalChanged`, `ActionRecorded`,
  `ActionsCleared`. The presenters subscribe to them to re-render, as `BattlefieldPresenter` does.

### Binding a story to a view

A `StoryBinding` per component, in `adapters.presentation`, builds the real view from the args, e.g.:

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

- `StoryContext` gives the binding the shared `BattleSceneResourcesLoader`, the `Texts` in the selected language, and
  `action(...)`, which runs `RecordAction` and, when it maps the action to an arg, `ChangeArg`.
- The view is rebuilt from scratch when the args or the globals change: simpler and deterministic. Its animations
  restart, which is acceptable in v1.
- A view taking lambdas instead of a `Delegate` (e.g.: `MenuView`) passes lambdas calling `context.action(...)`.
- The options of a `PickControl` come from presentation sources only, e.g.: `resourcesLoader.bitmaps("lootableItem")`
  for the item ids, so the dependency rule holds. If a story ever needs a Dto of another subdomain, revisit the rule
  first.
- The language Global lists `LocalizationApi.searchLanguages` and applies with `LocalizationApi.selectLanguage`.

### Startup flag and hosting

- `Main.kt` keeps a single `main`: it reads the flag and starts either the game (`startGame`) or the Storybook in its
  own window size. Reading the flag is platform specific: an `expect` in `commonMain` with an `actual` in a new
  `jsMain` source set (e.g.: the page's path ending in `/storybook/`, or a value set by its `index.html`) and in a new
  `jvmMain` source set (a system property, e.g.: `-Drpg.entry=storybook`). Check that `PackageStructureArchitectureTest`
  and the other Konsist tests also scan the new source sets.
- The window: about 1280x900, scaled to fit as the game is. The 390x844 phone frame plus a toolbar set the height.
- `runJvmStorybook`: a `JavaExec` copying the KorGE `runJvm` task with the system property. The autoreload
  configuration mirrors `.run/runJvmAutoreload.run.xml`; check how the KorGE plugin defines `runJvmAutoreload` to pass
  the property through it.
- `/storybook/`: a step of `Deploy JS`, after the bundle is built, adds `build/www/storybook/index.html`. The preferred
  way is a copy of `build/www/index.html` with `<base href="../">`, so the JS and `resources.zip` (read relative to the
  page by `ResourcesBundle`) are loaded from the game's files instead of copied. The URL updates must then use
  absolute paths, as the base changes how relative URLs resolve. Fall back to copying `build/www` into
  `build/www/storybook` if KorGE's JS loader does not follow the base. Guard the step with a `grep`, as the dependency
  report step does.

### URL

- `?story=<story id>&args=<name>:<value>;<name>:<value>&globals=<name>:<value>`, as Storybook. Only the args changed
  from the story's own are written. The values are URL-encoded, a color as its hex without `#`.
- `StoryLocation` (a port in `usecases.services`) reads the URL at startup and replaces it with
  `history.replaceState` on every `StorybookEvent` that changes it, so the back button does not step through every
  arg change. On the JVM it reads nothing and writes nothing (or reads the program arguments, if useful for
  `runJvmStorybook --args`).
- An unknown story or an invalid arg in the URL is ignored and logged, never a crash: the Storybook opens the first
  story, or the story with its default args.

### Architecture tests

- **Dependency rule**, e.g.: `StorybookDependencyArchitectureTest`: the files of `com.mkz.rpg.storybook` only import
  other subdomains from their `adapters.presentation` packages, except `shared` and `localization`. Also add
  `storybook` to the table of [Package structure](../architecture/package-structure.md#dependencies-between-subdomains).
- **Coverage rule**, e.g.: `StoryCoverageArchitectureTest`: every class of an `adapters.presentation` package that
  extends a KorGE `View`, directly or through another view (e.g.: `HealthBarView` → `BarView` → `Container`), has a
  `<View>Stories` binding, unless it is in the allowlist. Scenes, presenters and the views of `storybook` itself are
  left out. An allowlisted view that has a story fails the test, so the allowlist can only shrink. Check that Konsist
  resolves the indirect parents; otherwise list the view base classes in the test.
- **Registry rule**: every `<View>Stories` object is registered in the list of `StorybookApi`, so none is forgotten by
  the smoke test nor the app.
- Mutation testing: PIT already leaves out the classes declared directly in `<subdomain>.adapters.presentation`, so the
  `storybook` domain and use cases are mutation tested, and its presentation is not.

### Tests

- Unit tests of the domain and the use cases, with fakes, as any subdomain. See
  [How to add a new test](../testing/how-to-add-a-new-test.md).
- View tests of the Storybook's own views, with their page objects (`…Extension`), as `ViewTestPageObjectArchitectureTest`
  requires.
- The smoke test, in `commonTest` so it runs on the JVM and JS: for every story of the registry, render it with its
  default args in a `ViewsForTesting`, with `BattleSceneResourcesLoaderMother` and `TestCatalogs`, and fail on any
  exception. Also check that the story ids are unique and stable.
- An end-to-end test of the Storybook: start it on a story id, change an arg, tap the view, check the Actions.

### Claude skill

- `.claude/skills/storybook/SKILL.md`: build the bundle (`./gradlew browserReleaseWebpack`, as `Deploy JS`), add the
  `/storybook/` page the same way, serve `build/www` (e.g.: `python3 -m http.server -d build/www`), open
  `http://localhost:8000/storybook/?story=…` with Playwright, wait for the readiness signal, and save a screenshot under
  the scratchpad, or `build/storybook`, to read it.
- **Readiness signal**: once the selected story has rendered, the Storybook sets a value Playwright can wait for, e.g.:
  `document.title = "Storybook – <story id>"` or a `window.storybookRenderedStory` set through the `jsMain` adapter.
  Without it, a screenshot may be taken before the resources bundle is loaded.
- In a cloud session, Chromium and Playwright are already installed: do not install them. Headless Chromium renders
  WebGL in software, so check in M4 that KorGE renders there, and that it also does on the CI runners, as visual
  regression would need it.

### Technical risks to check early in M1

| Risk                                                                    | How to check it                                                              | Fallback                                                         |
|-------------------------------------------------------------------------|------------------------------------------------------------------------------|------------------------------------------------------------------|
| KorGE's JS loader does not follow `<base href="../">`                   | Deploy to a local `build/www/storybook/` and open it                         | Copy `build/www` into `build/www/storybook`                      |
| Adding `jsMain` and `jvmMain` changes what the Konsist tests scan       | Run the architecture tests after adding them                                 | Adjust the scopes of the tests                                   |
| `runJvmAutoreload` can not pass a system property                       | Read the KorGE plugin's task, try `-D` on the `.run/` configuration           | An environment variable, read by the `jvmMain` actual            |
| Konsist does not resolve indirect parents of a view                     | Write the coverage test against `HealthBarView` (`BarView` → `Container`)    | List the view base classes in the test                           |
| The smoke test is slow, rendering every story on JS                     | Time `jsTest` before and after                                               | Render every story on the JVM and a sample on JS                 |
