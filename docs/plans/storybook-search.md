# Plan: Storybook search

> **Status:** to do.
>
> **Summary:** Let the person find a story of the [Storybook](../architecture/storybook.md) by typing, instead of
> scrolling the whole sidebar and reading every name: about 46 components and 178 stories today. A search box under the
> title of the sidebar filters the list as it is typed, by the group, the title of the component and the name of the
> story. The search is the presentation's alone, as the zoom: out of the domain and of the link.

## Decisions

| Topic           | Decision                                                                                                                                                                                                                                  |
|-----------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| What it matches | The group, the title of the component and the name of the story, together: a story matches when its `<group> <title> <name>` does. So a component whose title matches lists all its stories, e.g.: `shop` lists every story of **Screen / Shop**. |
| How it matches  | Every word typed, ignoring case, must be in it somewhere, in any order. No fuzziness, no ranking: `shop sell` finds **Screen / Shop › Sell**, `sell shop` too, `shp` nothing. Spaces around the words are ignored.                          |
| The list        | Only the stories that match are listed, under their group and component, in the order of the catalogue. A group or a component with no story that matches is not listed. An empty search lists them all.                               |
| When            | On every change of the text, as it is typed: there is no key to search with.                                                                                                                                                              |
| The box         | Under the title of the sidebar, fixed: the list scrolls under it. It shows `Search stories`, in grey, while it is empty.                                                                                                                  |
| Clearing        | A `×` button at the right of the box, shown only while there is text, empties it. No keyboard shortcuts: no `/` to focus, no Enter, no Escape.                                                                                             |
| No results      | The list says `No stories match`.                                                                                                                                                                                                         |
| Highlight       | The letters that match are not highlighted: a `Text` has one color.                                                                                                                                                                       |
| Scroll          | The list goes back to the top whenever the text changes. Emptying the box scrolls the whole list back to the story on show, when it is out of sight.                                                                                    |
| On show         | Searching never changes the story on show: it stays in the phone even when it does not match, and is highlighted again once listed.                                                                                                     |
| Kept            | The text is kept while stories are tapped, so the matches can be gone through one by one, until it is emptied or the page reloads. It is not in the [link](../architecture/storybook.md#links).                                            |
| Architecture    | Presentation only, in `storybook.adapters.presentation`: `CatalogueSearch`, plain Kotlin with no KorGE view, holds the matching rule. `SidebarView` keeps the components it is given, searches them as the text changes and lists what matches. `StorybookPresenter`, the `Storybook` aggregate, its use cases and its events do not change. |
| Labels          | `Search stories` and `No stories match` are in English only, as the Storybook's own labels.                                                                                                                                              |
| Phone           | Typing on a phone relies on KorGE's text input, which focuses a hidden HTML input on JS so the keyboard opens. It is checked by hand; if the keyboard does not open, it is a follow-up and does not hold back the search.                   |
| Tests           | Unit tests of `CatalogueSearch`. View tests of `SidebarView` through its page object: typing, the `×`, no results, and the story on show filtered out. `StorybookEndToEndTest` types a search and taps a story listed.                     |
| Delivery        | Two milestones, each a few commits straight on `main`, green on CI. See [Milestones](#milestones).                                                                                                                                         |
| Out of scope    | Keyboard shortcuts and navigating the list with the arrows, fuzzy matching, highlighting the matches, searching the descriptions, and keeping the search in the link or after a reload.                                                   |

## Milestones

Update the **Status** column as the work progresses: ⬜ to do, 🟡 in progress, ✅ done. Add the commit or pull request
that completes each milestone to **Done in**.

| #  | Milestone              | Done when                                                                                                                                                                                       | Status | Done in |
|----|------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------|---------|
| M1 | The search             | Typing in the box under the title lists only the stories that match, `×` empties it, `No stories match` shows when none does, and the story on show is kept. Unit, view and end-to-end tested. | ⬜      |         |
| M2 | The docs and the check | The Storybook docs describe the search, and the page is checked with the `storybook` skill, typing a search.                                                                                    | ⬜      |         |

Each milestone runs `jvmTest`, `jsTest` and `lintKotlin`, and looks at the Storybook with the
[`storybook` skill](../../.claude/skills/storybook/SKILL.md).

### M1 commits

1. **The search.** `CatalogueSearch`, with its unit tests: an empty search lists everything, every word must match,
   in any order and ignoring case, a component's title matches all its stories, the order of the catalogue is kept, and
   a component with no story that matches is dropped.
2. **The box.** `SidebarView` lays out the box and its `×` under the title, and the list under them, and lists what
   `CatalogueSearch` finds as the text changes, or `No stories match`. With view tests through `SidebarViewExtension`,
   and `StorybookEndToEndTest` typing a search and tapping a story listed.
3. **The plan.** These commits, as built.

### M2 commits

1. **The docs.** [Storybook](../architecture/storybook.md) (what it shows, and how it is built) describes the search.
2. **The plan.** The check with the `storybook` skill, and the status set to done.

## Reference

The names are proposals: keep them unless an architecture test or a better name found while building asks otherwise,
and update this section when they change.

| Name              | Kind                 | What it does                                                                                                                       |
|-------------------|----------------------|------------------------------------------------------------------------------------------------------------------------------------|
| `CatalogueSearch` | Object, no KorGE view | `search(components, text)`: the components with a story that matches the text, each with only the stories that match, in their order. |
| `SidebarView`     | View                 | Adds the search box, its `×` and the `No stories match` line, and keeps the components displayed to search them again.             |

The search box is a `UITextInput`, as the text controls of the `ControlsView`. Its `onTextUpdated` fires on every
change, also when a test sets its text, so the page object types by setting it.
