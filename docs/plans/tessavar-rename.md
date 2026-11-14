# Plan: Rename the game to Tessavar, from its packages to its URLs

> **Status:** M1 done and M2 built on `main`, the screenshots of M2 still to be approved; M3 waits for the GitHub and Cloudflare renames.
>
> **Summary:** The game has no name. The repository is `maarkeez/rpg-korge` and the Gradle project is still the
> template's `hello-world`, which builds `hello-world.js`. The packages are `com.mkz.rpg.*`, and KorGE's id is the
> template's `com.sample.demo`. The browser tab says "unnamed", the desktop window says "Game", and the main menu shows
> only its six buttons. A player handed `maarkeez.github.io/rpg-korge/` has nothing to remember the game by, and nothing
> to search for. Instead, the game is **Tessavar** everywhere:
>
> - the browser tab, the desktop window and a gold title above the main menu's buttons;
> - the packages `com.mkz.tessavar.*`;
> - the repository `maarkeez/tessavar`, played at `maarkeez.github.io/tessavar/`;
> - the telemetry collector at `telemetry-collector.tessavar.workers.dev`.
>
> It builds on the [telemetry collector](telemetry-collector.md) and [Wishlist](wishlist-button.md) plans.

## Decisions

| Topic           | Decision |
|-----------------|----------|
| Scope           | One name, **Tessavar**, for the game, the code, the repository and every URL. The local folder stays `hello-world`. |
| Name            | An invented word from *tessera*, a mosaic tile, as the battlefield is made of tiles. No exact-match web search found it. It's never translated, so it's the same in English and in Spanish. Chosen over Velmora, which is taken on Steam, and over names that are already words or games. |
| Subtitle        | None for now. A release can add one later, the way Zelda has *A Link to the Past*, while the slug stays the name alone. |
| In the world    | Tessavar names the game, not a land in it. The glossary's **World** stays what the world builder builds, until story text needs a name for the land. |
| Shown to player | The browser tab and the desktop window say "Tessavar". The main menu shows "Tessavar" above its buttons, in `HudStyle.COIN_MIDDLE` gold like Wishlist, at 40 points. It stays visible while the languages are listed. Chosen over white text and over a drawn logo, which needs art before sharing. |
| Slug            | `tessavar`, chosen over `tessavar-game` as shorter in every link. It names the repository, the Pages path, the Gradle project and its bundle `tessavar.js`, and the PO headers. |
| Code            | The packages are renamed: `com.mkz.rpg` → `com.mkz.tessavar` in `src`, `buildSrc` and the docs. The `rpg.*` system properties become `tessavar.*`. The KorGE id becomes `com.mkz.tessavar`. Chosen over keeping `com.mkz.rpg` for full consistency, despite a diff touching every file. Saves are unaffected: neither the saves nor the storage keys hold a package name, and KorGE picks where the desktop version stores saves from its own code, not from the Gradle `id`. |
| Old URL         | `maarkeez.github.io/rpg-korge/` stops working: GitHub redirects the repository's links and git, not Pages. Nobody has been sent the game yet. Saves and the consent choice survive, as the browser keeps them for the whole `maarkeez.github.io` site, not per path. |
| Privacy notice  | Its last line becomes "Questions: open an issue at github.com/maarkeez/tessavar/issues.", in English and in Spanish. `PRIVACY_NOTICE_VERSION` stays 2, as only the contact link changes and the old link redirects. Chosen over version 3, which would ask every player for consent again. |
| Collector       | The Cloudflare account's `workers.dev` subdomain changes from `rpg-korge` to `tessavar`, and the Worker from `rpg-telemetry-collector` to `telemetry-collector`. The D1 database `rpg-telemetry` and its data stay, bound to the new Worker. Once it's live, you delete the old Worker. Its allowed origin stays `https://maarkeez.github.io`. |
| Brevo           | You rename the signup form and its sender to Tessavar in Brevo, and change the form's contact link to `maarkeez/tessavar` issues. ([ADR 0019](../adr/0019-mailing-list-kept-by-brevo-not-the-telemetry-collector.md)) |
| Docs            | The README, the docs, the skills and [ADR 0001](../adr/0001-layered-subdomain-packages.md) use the new names, as they describe the code today. Done plans and the devlog keep the old names, as records of their time. |
| Architecture    | No subdomain changes. The title is part of `screen.adapters.presentation.MenuView`. `GAME_TITLE` sits in `Main.kt` next to `GAME_WINDOW_SIZE`. |
| Storybook       | **Screen / Menu › Options** shows the title. |
| Tests           | View tests for the title in `MenuViewTest`. The existing tests, the architecture tests and the save fixture tests keep passing under the new packages, which proves the rename is complete. |
| Out of scope    | A pixel-art logo, a subtitle, naming the land, and renaming the local folder or the D1 database. |
| Delivery        | 3 milestones, each green on CI. See [Milestones](#milestones). |

## Milestones

Each milestone is a few commits straight on `main`, green on CI.

| #  | Milestone          | Done when                                                                                                                                                  | Status | Done in |
|----|--------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|--------|---------|
| M1 | Code names         | No `com.mkz.rpg`, `rpg.` property, `hello-world` or `com.sample.demo` is left outside the README, the done plans and the devlog, `jvmTest` and `jsTest` pass, and the Storybook loads `tessavar.js`. | ✅      | 93190c78 |
| M2 | The name shown     | The browser tab and the desktop window say "Tessavar", and **Screen / Menu › Options** shows the gold title above the buttons, in English and in Spanish.        | ✅      | 3a658aed |
| M3 | Repository and URLs | `maarkeez.github.io/tessavar/` plays the game, and a product event sent from it is in D1 through `telemetry-collector.tessavar.workers.dev`.                 | ⬜      |         |

### M1 commits

1. Rename the packages: `git mv` the five `com/mkz/rpg` source folders (`commonMain`, `commonTest`, `jvmMain`, `jvmTest`,
   `jsMain`) to `com/mkz/tessavar`, and replace `com.mkz.rpg` with `com.mkz.tessavar` in every `.kt` file. Also in
   `build.gradle.kts` (`jvmMainClassName`, the `entrypoint`, `packageName` of `telemetryCollectorUrl`), and in
   `buildSrc`'s tests (`KotlinSourceFileMother.BASE_PACKAGE`, `DependencyParserTest`). Also in the paths of
   `telemetry-collector.yml`'s triggers, in the docs under `docs/testing/`, ADR 0001, the `storybook` skill and the
   grill skill's `GLOSSARY-FORMAT.md`, whose `# RPG` example becomes `# Tessavar`. The pre-commit hook regenerates
   `docs/architecture/dependencies.html`.
2. Rename the build: `rootProject.name = "tessavar"`, which builds `tessavar.js`, and `add-storybook.sh` checks for
   `tessavar.js`. The KorGE `id` becomes `com.mkz.tessavar`. Each `rpg.*` property becomes `tessavar.*`:
   `updateSaveFixtures`, `updateTelemetryFixtures`, `telemetryCollectorUrl` and `entry`. That covers `build.gradle.kts`,
   `ENTRY_PROPERTY`, `SaveFixtureTest`, `ProductEventBatchFixtureTest`, `TelemetryCollectorUrlTask`, `deploy-js.yml`,
   which still passes the old collector URL until M3, and the docs that mention them.

### M2 commits

1. Show the name: add `GAME_TITLE` and pass it as `Korge(title = GAME_TITLE)`, set `korge { name = "Tessavar" }` for the
   page's `<title>`, add the title to `MenuView`, and update the story, its description and the tests.

### M3 commits

Before these, you do two things by hand:

- rename the repository on GitHub (Settings › General › Repository name: `tessavar`);
- change the Cloudflare account's subdomain (Workers & Pages › Subdomain: `tessavar`).

From the subdomain change until the deploy, the live game can't reach the collector: its product events wait in the
browser's queue, and are sent once the new bundle loads.

1. Rename the collector: `"name": "telemetry-collector"` in `wrangler.jsonc`, `package.json` and `package-lock.json`.
   Its README is updated, along with the example URLs in `collector.test.ts` and `RemoteTelemetryRepositoryTest`, the
   `-Ptessavar.telemetryCollectorUrl=https://telemetry-collector.tessavar.workers.dev` in `deploy-js.yml`, and the
   comment in `build.gradle.kts`. The Telemetry collector workflow deploys the new Worker, bound to `rpg-telemetry`.
2. Point the text at the new repository:
   - the privacy notice's last line in `messages.pot` and `es.po`;
   - `Project-Id-Version: tessavar` in both catalogs;
   - the Storybook URL in `CommentArchitectureTest`;
   - the repository link in the `devlog` skill;
   - the README, rewritten: the KorGE template's text goes, and it opens with "# Tessavar", what the game is, and links
     to `maarkeez.github.io/tessavar/` and its Storybook.

After them, you do these by hand:

- delete the `rpg-telemetry-collector` Worker;
- rename the Brevo form and sender, and change the form's contact link;
- run `git remote set-url origin https://github.com/maarkeez/tessavar.git` locally.

## Reference

### MenuView

- `GAME_TITLE = "Tessavar"`, in `Main.kt`, names the game for `Korge(title = ...)` and the menu.
- `MenuView.title` is a text of `GAME_TITLE`, 40 points, `HudStyle.COIN_MIDDLE`, centred over `BUTTON_WIDTH`. It sits
  `TITLE_GAP` (30) above `options` and `languages`, which move down by `TITLE_HEIGHT + TITLE_GAP`. The view's height
  grows by as much, so `MenuScene` still centres it in `GAME_WINDOW_SIZE`.
- `selectLanguage` leaves the title as it is: it's never translated.

### Storybook

- `MenuViewStories`: the **Screen / Menu › Options** story's description starts with "Tessavar, in gold, above
  ...". The story id `screen-menu--options` is unchanged.

### Tests

- `MenuViewTest`
  - `Menu view should show Tessavar in gold above the buttons when it opens`
  - `Menu view should keep showing Tessavar when the languages are listed`
  - `Menu view should keep Tessavar untranslated when Spanish is selected`
- Every existing test runs under `com.mkz.tessavar`: the architecture tests read the base package, so a missed file
  fails them.

## Known limitations

- The old Pages address `maarkeez.github.io/rpg-korge/` and the old collector address stop working. Links to
  `github.com/maarkeez/rpg-korge` keep working through GitHub's redirect, as long as no new repository takes that name.
- Claude Code cloud sessions are allowed onto `maarkeez/rpg-korge` by name, so they may need to be allowed onto
  `maarkeez/tessavar` again after the rename.
- The D1 database keeps its name, `rpg-telemetry`, as D1 can't rename one, and moving its data isn't worth the risk.
- On the desktop, the window title says Tessavar, but the saves stay in KorGE's default settings folder.
