# The Storybook is part of the game

To show each view of the game in isolation, in each of its states, we built our own Storybook as the `storybook`
subdomain, in the same bundle and started by the same `main` (from the `/storybook/` page, or with
`rpg.entry=storybook` on the JVM), so it runs the real KorGE views with the real images, texts and input. For every view
to have its stories, a view of the game never imports a domain nor a use case, and takes the plain view data its
presenter maps from the domain (`ViewArchitectureTest`), every view has its `<View>Stories` binding
(`StoryCoverageArchitectureTest`), and `storybook` only depends on `shared`, `localization` and the presentation of the
other subdomains, with only `Main.kt` depending on it (`StorybookDependencyArchitectureTest`).

## Considered options

- [Storybook](https://storybook.js.org) itself: it shows web components, while the views are KorGE views drawn by the
  game, so it could only show screenshots of them, not the views themselves.
