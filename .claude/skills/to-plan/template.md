# Plan: <what the change does, as a sentence or a short name>

> **Status:** to do.
>
> **Summary:** <What the player or the developer sees today, with a concrete example (e.g.: the unit, the screen, the
> number). Why it is a problem. Then, starting with "Instead,", what it does once this plan is built. Link the plans and
> architecture docs it builds on.>

<!-- Optional, for a bug or a measurement: a section before Decisions, e.g. `## The bug`, `## Cause`,
`## What was measured` or `## How it <does it> today`, saying what happens now, with the classes and numbers involved. -->

## Decisions

| Topic        | Decision                                                                                       |
|--------------|------------------------------------------------------------------------------------------------|
| Scope        | <What is in, with examples.>                                                                   |
| <Topic>      | <The decision, then the alternative dropped and why, when there was one.>                      |
| Architecture | <Which subdomain and layer each piece lives in (`<subdomain>.domain`, `.usecases`, `.adapters`).> |
| Storybook    | <The stories added or changed: **Group / Component › Story**, with their controls.>           |
| Tests        | <How it is tested: unit, view, architecture, end-to-end.>                                      |
| Out of scope | <What is deliberately left out.>                                                               |
| Delivery     | <Only when it has milestones: "N milestones, each green on CI. See [Milestones](#milestones).">  |

<!-- Optional: a worked example after the table, e.g. a timeline or a before/after table, with real units and numbers:

For example, the knight, with 30 health points, walks over two tiles of lava, which deal 5 damage each:

| Time  | Health | Displayed over the knight |
|-------|--------|---------------------------|
| 0ms   | 25     | ...                       |
-->

## <subdomain or class, e.g. battlefield or BattlefieldView>

<!-- One section per subdomain or class it changes, in dependency order. A large one splits into
### Domain, ### Use cases, ### Adapters and ### Resources. -->

- `<function or class>(<args>)` <does what, in the present tense, as if it were already built>. E.g.: <a concrete case>.
- `<Event or use case>` <new or changed, and who publishes or handles it>.

## Storybook

- `<ViewStories>`: the `<Story>` story (`<story-id>`), its `<control>` control (`<type>`, <range>).
- `StorySmokeTest` lists the new story.

## Docs

- [<Architecture doc>](../architecture/<doc>.md): <what it gains>.

## Tests

- `<ClassTest>`
  - `<Subject> should <outcome> when <condition>`
  - `<Subject> should <outcome> when <condition>` (replaces `<old test name>`)
- `<OtherClassTest>`
  - `<Subject> should <outcome>`

## Known limitations

<!-- Or ## Not in this plan. What stays broken or unfinished on purpose, and why. -->

- <Limitation.>

<!-- Only when the delivery has several milestones, replacing the per-component sections above with Reference:

## Milestones

Each milestone is a few commits straight on `main`, green on CI.

| #  | Milestone | Done when                                         | Status | Done in |
|----|-----------|---------------------------------------------------|--------|---------|
| M1 | <Name>    | <What can be seen or run once it is done.>        | ⬜      |         |
| M2 | <Name>    | <...>                                             | ⬜      |         |

### M1 commits

1. <What the first commit does.>
2. <...>

### M2 commits

1. <...>

## Reference

### <Package layout, Domain model, Tests, ...>

<The per-component detail the milestones build.>
-->
