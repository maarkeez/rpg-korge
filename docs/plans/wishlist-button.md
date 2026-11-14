# Plan: Wishlist on the main menu opens the signup form of the mailing list

> **Status:** built on `main` in `2de9017f`, its screenshots still to be approved.
>
> **Summary:** Today a player who likes the game has no way to hear of its next version: the main menu offers Play new
> game, Load game, Build a world, Language and Privacy, and the game never asks for, nor keeps, anything that reaches
> them later. When a new demo or a new release is out, nobody who played the last one is told. Instead, a gold
> Wishlist button at the bottom of the main menu opens, in the browser, the signup form of the mailing list, kept by
> Brevo: the player leaves their email there, confirms it, and is told of each new version. The game never sees the
> email. See [ADR 0019](../adr/0019-mailing-list-kept-by-brevo-not-the-telemetry-collector.md).

## Decisions

| Topic         | Decision                                                                                                                                                                                                                                                                               |
|---------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope         | A Wishlist button on the main menu, opening the Brevo signup form of the mailing list in the browser. E.g.: a player on GitHub Pages taps Wishlist, a new tab opens Brevo's form, they leave their email and confirm it from the email Brevo sends, while the game stays on the main menu. |
| Words         | The code and the glossary say mailing list; the player sees Wishlist, "Lista de deseados" in Spanish, the words Steam uses, so players know them. Chosen over "Get notified", as players know Wishlist from Steam.                                                                       |
| Where         | The main menu only, last, under Privacy. Chosen over the pause screen too, as every session starts at the main menu, and nobody is sent to a browser tab in the middle of play.                                                                                                       |
| Look          | Filled gold, `HudStyle.COIN_MIDDLE` `#C78A1E`, `#A6731A` when pressed, with dark `HudStyle.PANEL` `#171725` text, about 6:1 contrast. Chosen over white text, at about 3:1, and over `HudStyle.SELECTION` yellow, the selection on the game screen. Play new game stays the blue one at the top. |
| Mailing list  | Kept by Brevo, on its free plan, in the EU: its form, its double opt-in, unsubscribing and the emails later. Chosen over the telemetry collector, as Cloudflare's Email Sending needs the paid plan and a domain of our own: see [ADR 0019](../adr/0019-mailing-list-kept-by-brevo-not-the-telemetry-collector.md). |
| What it is for | Only a new version of the game: a new demo or a new release. Nothing else, no devlog posts.                                                                                                                                                                                           |
| Signup form   | One form, in English, for every language. It says what the mailing list is for, that Brevo keeps it, how to unsubscribe, and the repository's GitHub issues as the contact, without naming the author. The game's privacy notice stays at version 2.                                  |
| Address       | `SIGNUP_FORM_URL`, a constant: the form is public and the same in every build, so Wishlist works on GitHub Pages, on the JVM and in the Storybook. Chosen over a build property like the telemetry collector's, which only the GitHub Pages build gets. The form is made before the build. |
| Telemetry     | Tapping Wishlist records no product event: the privacy notice names every product event, so a new one would ask every player for consent again, and Brevo counts the visits to the form and the signups.                                                                              |
| Architecture  | All in `screen.adapters.presentation`: `Browser` and `GameWindowBrowser` open a page with KorGE's `GameWindow.browse`, the way `Clipboard` and `GameWindowClipboard` copy. No platform code of our own, and no other subdomain.                                                          |
| Storybook     | **Screen / Menu › Options** shows Wishlist, and logs `onWishlistSelected` when tapped. **Screen / Confirm button** gains a gold story.                                                                                                                                                  |
| Tests         | View tests of `MenuView` and `ConfirmButton`, and a test of `MenuScene` with a `FakeBrowser`. Opening the tab on a real phone is checked by hand.                                                                                                                                         |
| Out of scope  | Sending anything to the mailing list, and the sender's postal address Brevo puts in the footer of every email; a Spanish form; Wishlist on the pause screen; an email field in the game.                                                                                               |

## Before building: the Brevo form

Made by hand, on Brevo's free plan, before the first commit, as its address goes in the code:

- A list, the mailing list, with double opt-in on: Brevo emails a confirmation link, and only a confirmed email joins.
- A form, in English, whose hosted page is `SIGNUP_FORM_URL`, saying:

  > **Wishlist**
  >
  > Leave your email to be told when a new demo or a new release of the game is out. Nothing else.
  >
  > Brevo keeps it for us, in the European Union, until you unsubscribe: every email has a link to do it.
  >
  > Questions: open an issue at github.com/maarkeez/rpg-korge/issues.

## screen

### Adapters

- `Browser`, an interface with `suspend fun open(url: String)`, and `GameWindowBrowser(gameWindow)`, which calls
  `gameWindow.browse(URL(url))`: a new tab on the web, the default browser on the JVM.
- `SIGNUP_FORM_URL`, the address of the Brevo form, a constant next to `MenuScene`.
- `ConfirmButton(label, buttonWidth, color, pressedColor, textColor, onConfirmed)` gains `textColor`, white by default,
  so every button today stays as it is.
- `HudStyle.COIN_PRESSED` `#A6731A`, the gold of a pressed Wishlist.
- `MenuView(messages, onNewGameSelected, onLoadGameSelected, onWorldBuilderSelected, onPrivacySelected,
  onWishlistSelected)` gains `wishlistButton`, a `ConfirmButton` in `COIN_MIDDLE`, `COIN_PRESSED` and `PANEL` text,
  last in `options`, which grows from 5 to 6 buttons, 267 high. Like Privacy, Wishlist keeps the options shown.
  `selectLanguage` relabels it.
- `MenuScene(localizationApi, telemetryApi, storage, random, incompatibleSlot, browser)` gains `browser: Browser?`,
  `GameWindowBrowser(views.gameWindow)` when null, and `onWishlistSelected` calls `browser.open(SIGNUP_FORM_URL)` with
  `launchImmediately`, so on the web the tab opens within the tap, which a browser allows, and doesn't block as a popup.

### Resources

- `UiMessage.MENU_WISHLIST`, `ui.menu.wishlist`: "Wishlist" in `messages.pot`, "Lista de deseados" in `es.po`.

## Storybook

- `MenuViewStories`: the `Options` story (`screen-menu--options`) shows Wishlist, logs `onWishlistSelected`, and its
  description and its component's name Wishlist.
- `ConfirmButtonStories`: a `Gold` story (`screen-confirm-button--gold`), labelled Wishlist, in `COIN_MIDDLE` with
  `PANEL` text.
- `StorySmokeTest` lists the new story.

## Docs

- [GLOSSARY.md](../../GLOSSARY.md) already has the mailing list and the main menu joining it.

## Tests

- `MenuViewTest`
  - `MenuView should display playing a new game, building a world and the language when it is displayed` also checks
    Wishlist
  - `Wishlist should ask to open the signup form when it is clicked`
  - `Wishlist should keep the options shown when it is clicked`
  - `PickLanguage should translate the game and display the options relabelled when a language is picked` also checks
    "Lista de deseados"
- `ConfirmButtonTest`
  - `ConfirmButton should write its label in the text color when it is given one`
- `MenuSceneTest`, with a `FakeBrowser` recording what it opens
  - `MenuScene should open the signup form of the mailing list when Wishlist is clicked`

## Known limitations

- Whether the browser opens the tab, or blocks it as a popup, is only known on a real browser: it is checked by hand in
  the game on GitHub Pages, on a phone and on a computer.
- A Spanish player reads the form in English.
- The mailing list sends nothing yet: the first announcement needs the sender's postal address in its footer, decided
  then.

## Changes while building

- **Screen / Confirm button › Gold** is drawn from a new `gold` control of the component, a boolean, false in its
  other stories: the stories of a component share one `render`, so the colors follow an arg.
- `GameWindowBrowser` calls KorGE's `browse` through `korlibs.render.browse`, an extension of the window's dialogs,
  not a function of `GameWindow` itself.
- The Brevo form is live at `SIGNUP_FORM_URL`, but still shows Brevo's default text ("Newsletter", "Subscribe to our
  newsletter and stay updated.") rather than the text above, and no confirmation email has arrived yet in a signup
  test: both are fixed in Brevo, not in the game.
