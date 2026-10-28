# How to add a story

> **Summary:** Show a view of the game in the [Storybook](../architecture/storybook.md), in each of its states, e.g.:
> after adding a view, or to take it out of the views allowed to have no stories. A view gets one `<View>Stories`
> binding, with its controls and stories, registered in `StorybookApi`.

## Prerequisites

- The view to show, in an `adapters.presentation` package, e.g.: `ManaBarView` in `screen.adapters.presentation`.
- The states worth showing: the ones an artist or a designer would check, e.g.: full, half and empty.

## Steps

### 1. Write the binding

Create `src/commonMain/kotlin/com/mkz/rpg/storybook/adapters/presentation/<View>Stories.kt`, an `object` named after
the view, implementing `StoryBinding`. Start from
[`HealthBarViewStories`](../../src/commonMain/kotlin/com/mkz/rpg/storybook/adapters/presentation/HealthBarViewStories.kt)
for a view without callbacks, or
[`ShopViewStories`](../../src/commonMain/kotlin/com/mkz/rpg/storybook/adapters/presentation/ShopViewStories.kt) for
one with a `Delegate`:

```kotlin
/** The stories of [ManaBarView], at the size the battle unit info displays it. */
object ManaBarViewStories : StoryBinding {
    private val SIZE = Size(281.5, 16)
    private const val REMAINING = "remaining"
    private const val MAXIMUM = "maximum"

    private val full = StoryDto(id = "screen-mana-bar--full", name = "Full", description = "A battle unit that has cast no ability.", args = emptyMap())
    private val empty = StoryDto(id = "screen-mana-bar--empty", name = "Empty", description = "A battle unit with no mana left.", args = mapOf(REMAINING to ArgDto.int(0)))

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-mana-bar",
            group = "Screen",
            title = "Mana bar",
            description = "The mana a battle unit has left, out of its maximum, in the info of the selected battle unit.",
            controls = listOf(ControlDto.int(REMAINING, minimum = 0, maximum = 999), ControlDto.int(MAXIMUM, minimum = 1, maximum = 999)),
            args = mapOf(REMAINING to ArgDto.int(20), MAXIMUM to ArgDto.int(20)),
            stories = listOf(full, empty),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View = ManaBarView(SIZE).apply { display(remaining = args.int(REMAINING), maximum = args.int(MAXIMUM)) }
}
```

- **The component**: its `group` is the subdomain of the view, capitalised (e.g.: `Screen`), and its `id` the group and
  the title in kebab case (e.g.: `screen-mana-bar`). The description says what the view is and where the game shows it,
  for someone who has not read the code.
- **The stories**: each one with an `id` of `<component id>--<name in kebab case>`, a `name`, a description of the
  state, and only the `args` it changes from the component's defaults. The first story is the one shown first.
- **The ids are for good**: links shared hold them, so keep an id when renaming its component or story.
- **The size**: build the view at the size the game displays it. The phone shows it in the middle of a 390x844
  screen, the game window, and crops what does not fit.

### 2. Choose the controls

One control per value the view is displayed with, named as the view names it, with a default arg each in `args`:

| Control                                    | For                                                         | Read with                 |
|--------------------------------------------|-------------------------------------------------------------|---------------------------|
| `ControlDto.boolean(name)`                 | A flag, e.g.: `equipped`.                                   | `args.boolean(name)`      |
| `ControlDto.int(name, minimum, maximum)`   | A number, e.g.: `gold`, within what the game allows.        | `args.int(name)`          |
| `ControlDto.enum(name, options)`           | A few fixed options, e.g.: `ShopView.Mode.entries`, a button each. | `args.text(name)`  |
| `ControlDto.pick(name, options)`           | Many options, gone round with `<` and `>`, e.g.: the item ids. | `args.text(name)`      |
| `ControlDto.text(name)`                    | A free text, e.g.: a name.                                  | `args.text(name)`         |
| `ControlDto.color(name)`                   | A color, as its hex without `#`, e.g.: `FF375F`.            | `args.color(name)`        |

- The args of an enum, a pick or a color are texts: `ArgDto.text("SELL")`. The others are `ArgDto.boolean(true)` and
  `ArgDto.int(3)`.
- **Item ids**: use `context.lootableItemIds()`, the items that have an image, for a pick control or the items of a
  view, never a list written by hand. A pick that may be empty adds an option of its own, e.g.: `none` for an empty
  slot.
- **Plain data only**: the binding feeds the view the plain data it displays, never a domain object or a use case of
  another subdomain, which the architecture tests forbid.
- **They must fit**: the Controls panel may take at most `ControlsView.MAXIMUM_HEIGHT` (384), about 8 rows. An enum
  whose options do not fit on one line, e.g.: long names, wraps to two lines and takes two rows. If they do not fit,
  keep the controls that matter to an artist or a designer, and fix the rest in the binding.

### 3. Log the callbacks

Hand every callback of the view to `context.action(name, arguments)`, named as the callback, so it is listed in the
Actions:

```kotlin
setDelegate(
    object : ShopView.Delegate {
        override fun shopSlotSelected(index: Int) =
            context.action("shopSlotSelected", index, setArg = SELECTED_SLOT to ArgDto.text("$index"))

        override fun shopBackSelected() = context.action("shopBackSelected")
    },
)
```

A view that takes lambdas instead of a `Delegate` passes lambdas calling `context.action(...)`.

`setArg` maps the callback to an arg change, as the game would react, e.g.: tapping a slot selects it. Only map to a
value the control accepts: a value outside the options of a pick, or the range of an int, fails. When there is none,
map to nothing (`setArg = null`), e.g.: stepping past the last party member.

### 4. Register it

Add the binding to `StorybookApi.STORY_BINDINGS`, in
[`StorybookApi.kt`](../../src/commonMain/kotlin/com/mkz/rpg/storybook/adapters/presentation/StorybookApi.kt), where the
sidebar should list it.

### 5. Update the tests

- Add the component id and its story ids to the list of `StoryBindings should keep the ids...` in
  [`StorySmokeTest`](../../src/commonTest/kotlin/com/mkz/rpg/storybook/adapters/presentation/StorySmokeTest.kt), in the
  order of the bindings.
- Remove the view from the views allowed to have no stories, in
  [`StoryCoverageArchitectureTest`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/architecture/StoryCoverageArchitectureTest.kt).
  The test fails while it is still there.
- Run them:

  ```shell
  ./gradlew jvmTest jsTest lintKotlin
  ```

  `StorySmokeTest` renders every story with the game resources, opens the catalogue (so a default or story arg its
  control does not accept fails), and checks the controls fit.
- Run `./gradlew dependencyReport` if the binding imports a subdomain the Storybook did not depend on yet.

### 6. Look at it

On the JVM, with every change of the code shown as it is saved:

```shell
./gradlew runJvmStorybookAutoreload
```

Or in the browser, as it is published, by following the [`storybook` skill](../../.claude/skills/storybook/SKILL.md),
which Claude can also follow to take a screenshot of each story. Check each story, its controls, and that tapping the
view lists its actions.

## Troubleshooting

- **`StorySmokeTest` fails on `Init should open the catalogue`**: an arg is not accepted by its control, e.g.: an item
  id with no image, an int outside its range, or an arg without a control. The message names it.
- **`ControlsView should fit the controls`** lists the component: it has too many controls, see step 2.
- **`StoryCoverageArchitectureTest` fails**: the binding is not named `<View>Stories` after a view of the game, it is
  not in `StorybookApi.kt`, or the view is still allowed to have no stories.
- **A tap on the view logs nothing**: the callback is not handed to `context.action(...)`, or the view needs to be
  added to the stage to be tapped, which the phone does.
