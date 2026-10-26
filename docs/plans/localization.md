# Plan: Localization

> **Status:** done. See [Localization](../architecture/localization.md) and [How to add a language](../localization/how-to-add-a-language.md).
>
> **Summary:** The game is translated with standard gettext PO files, so contributors can use Weblate, Crowdin, Poedit
> or any other PO tool. English is the source language and Spanish the first translation. Only the presentation layer
> knows about translations: the other subdomains hold ids, never text. The language is picked from the main menu for
> the session.

## Decisions

| Topic             | Decision                                                                                                                                                                                                  |
|-------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Format            | gettext PO. `resources/localization/messages.pot` is the hand-maintained English source, and each translation is a `<language code>.po` next to it (e.g.: `es.po`, `it.po`). One catalog per language, UI and game data together. |
| Identifiers       | The key goes in `msgctxt`, the English text in `msgid`: every tool shows translators the English text, the key never changes, and editing the English marks the translations as fuzzy.                     |
| Keys              | Game data: `<subdomain>.<entity id>.<field>` (`unit.bat.name`, `npc.alchemist.line`, `terrain.grass.name`). UI: `ui.<view>.<message>` (`ui.shop.notEnoughGold`, `ui.menu.playNewGame`). `language.name` names each catalog's language in that language. |
| Scope             | The UI text (menus, buttons, messages, the map builder) and the game data text (the names of units, abilities, lootable items, npcs, players and terrains, and the npc lines). Log and exception messages stay in English. |
| Placeholders      | Named, e.g.: `Vanishes in {turns} turns`, so translators can reorder them. `{` and `}` are only used for placeholders.                                                                                     |
| Plurals           | Native PO plurals (`msgid_plural`, `msgstr[n]`), each catalog's `Plural-Forms` header evaluated at runtime, so a new language needs no Kotlin change.                                                       |
| Sentences         | Whole sentences with placeholders: no case changes in code (no `lowercase()`), and no article in front of an inserted name (`Tap a party member to give: {item}`).                                         |
| Layering          | A new `localization` subdomain, only depending on `shared`. Only `*.adapters.presentation` packages, and the root package booting the game (`Main.kt`), may depend on it, enforced by an architecture test. The other subdomains drop their `name` and `line`: they keep ids, and the screen translates them. |
| UI keys           | Typed: the `UiText` enum in `screen.adapters.presentation` lists every `ui.*` key, in one place.                                                                                                           |
| Parser            | Our own small PO parser in `commonMain`: `msgctxt`, `msgid`, `msgid_plural`, `msgstr`, `msgstr[n]`, comments, flags, multi-line strings and escapes. No Kotlin Multiplatform gettext library is mature enough. |
| Picking           | A third main menu button opens the list of languages, each named in its own language (`English`, `Español`). The languages are discovered from the `*.po` files, the code is the file name.                 |
| Default           | English, every session: the choice is not saved.                                                                                                                                                         |
| Switching         | Picking a language relabels the menu right away and applies to every screen opened after it. Nothing else is on the screen, so nothing else re-renders.                                                    |
| Fallback          | A string without a translation (missing, empty or fuzzy) falls back to its English text. A key missing from `messages.pot` displays the key itself, which the tests prevent.                               |
| Invalid catalogs  | Fail when the resources are read, with the file path in the message, as an invalid TOML file does.                                                                                                       |
| Scripts           | Latin only. The font (`DefaultTtfFontAsBitmap`, KorGE's `LATIN_ALL` characters) already draws Spanish (`ñ á é í ó ú ü ¿ ¡`). A test fails on a character the font does not draw.                          |
| Syncing           | Translators merge `messages.pot` into their catalog with the standard tools (`msgmerge`, Weblate, Poedit). No custom tool for now.                                                                        |
| Loading           | The catalogs are read before the menu is displayed, so the menu waits for the resources bundle (under 1 MB) instead of only prefetching it.                                                               |
| Delivery          | Three commits on `main`: the localization subdomain with the UI text, then the game data text, then Spanish, the picker and the docs. The game stays in English and working after each one.              |

For example:

| The player                       | The game                                                                                 |
|----------------------------------|------------------------------------------------------------------------------------------|
| Opens the game                   | The menu in English: `Play new game`, `Build a map`, `Language`.                         |
| Taps `Language`                  | The list of languages: `English`, `Español`.                                              |
| Taps `Español`                   | The menu in Spanish: `Jugar partida nueva`, `Construir un mapa`, `Idioma`.               |
| Taps `Jugar partida nueva`       | The battle in Spanish, e.g.: `Explorando`, the bat is a `Murciélago`.                    |
| Restarts the game                | The menu in English again.                                                               |

## localization

### Domain

- `Localization`: the aggregate, holding the source catalog (English, from `messages.pot`), the translation catalogs and
  the selected language code (`en` for the source).
  - `translate(key, arguments)` and `translatePlural(key, count, arguments)`: the message of the selected language,
    falling back to the source, then to the key. Placeholders are replaced by their argument, and a placeholder
    without one is left as it is.
  - `selectLanguage(code)`: records `LanguageSelected`, failing with `LanguageNotFound` for an unknown code.
  - `languages()`: the source language first, then the translations in code order, each with its `language.name`.
  - A private `PluralRule` parses and evaluates the `Plural-Forms` expression (`n`, integers, `== != < > <= >=`,
    `&& || !`, `+ - * / %`, `? :` and parentheses). A catalog whose message has more forms than `nplurals` is invalid.
- `LocalizationError`: `EmptyLanguageCode`, `InvalidPluralForms`, `TooManyPluralForms`, `LanguageNotFound`, `LocalizationNotFound`.
- `LocalizationEvent`: `LocalizationInitialised`, `LanguageSelected`.
- `LocalizationRepository`: saves and searches the single localization.

### Use cases

- Commands: `InitialiseLocalization` (from the loaded catalogs, English selected) and `SelectLanguage(code)`.
- Queries: `Translate(key, arguments)`, `TranslatePlural(key, count, arguments)` and `SearchLanguages()`.
- Services: `LocalizationLoader`, with `initResources()` and `loadLocalization()`.

### Adapters

- `resources`: `PoFile` parses a PO file into its header and entries, failing with the line number. The
  `ResourcesLocalizationLoader` reads `localization/messages.pot` and every `localization/*.po`, keeps the translated
  entries that are not fuzzy, and fails with the file path when one is invalid or has no `Plural-Forms`.
- `storage`: `InMemoryLocalizationRepository`.
- `presentation`: `LocalizationApi`.

## screen

- `UiText`: the enum of every `ui.*` key.
- `Texts`: wraps the `LocalizationApi` for the views: `texts[UiText.SHOP_NOT_ENOUGH_GOLD]`,
  `texts.plural(UiText.REMAINING_TURNS, count)`, and the game data names: `unitName(unitId)`, `npcLine(npcId)`, ...
- Every view and presenter displaying text receives `Texts` through its constructor, as views receive the
  `BattleSceneResourcesLoader`.
- `startGame` creates the `LocalizationApi`, reads the catalogs before the menu, and passes it to every scene.
- `MenuView` gets a `Language` button, opening the list of languages in its place. Picking one selects it and relabels
  the menu.
- The sentences built in code are rewritten as whole sentences: `{terrain} can not be next to {neighbour}`,
  `Tap a party member to give: {item}`, `Tap an item to swap it for: {item} ({price})`, `{player} turn`,
  `{player} wins!`, `Round {round}`, `{gold} G`, `Bag {used}/{maximum}`, and the plural `Vanishes in {turns} turns`.

## Game data

- `ability`, `lootableItem`, `npc`, `player` and `unit` drop `name` (and `npc` drops `line`) from their TOML files,
  aggregates, DTOs and events. Their English text moves to `messages.pot`.
- `terrain` gets `terrain.<id>.name` keys, instead of the map builder capitalising the id.
- The screen translates them from their ids: `unitName(unitId)`, `abilityName(abilityId)`, `lootableItemName(itemId)`,
  `npcName(npcId)`, `npcLine(npcId)`, `playerName(playerId)`, `terrainName(terrainId)`.

## Tests

- `PoFileTest`: entries, context, plurals, comments, flags, multi-line strings, escapes, and errors with the line.
- `LocalizationTest`: translating, falling back to the source then to the key, placeholders, plurals with several
  `Plural-Forms`, selecting a language, and the languages.
- Use case tests for the commands and queries, with a `LocalizationMother`.
- `LocalizationResourcesTest`, failing the build when:
  - a catalog does not parse;
  - a `UiText` key or a game data key (each unit, ability, lootable item, npc, player and terrain in `resources`) is
    missing from `messages.pot`;
  - a `messages.pot` entry is not a `UiText` key, a game data key, nor `language.name`;
  - a `.po` has an entry missing from `messages.pot`, other placeholders than its English text, or another number of
    plural forms than its `nplurals`;
  - a translation uses a character the font does not draw.
- `LocalizationArchitectureTest`: only `*.adapters.presentation` packages depend on `localization`.
- The view tests receive `Texts` from a `TextsMother`, loaded from the real catalogs, so they keep asserting the English
  text. The JS tests can only read the resources asynchronously, while many tests build their views synchronously, so
  the `testCatalogs` Gradle task (in `buildSrc`) embeds the catalogs into a generated `TestCatalogs` test source.
  `MenuViewTest` covers the language list and relabelling.

## Docs

- `docs/architecture/localization.md`: the design, the key convention and the layering.
- `docs/localization/how-to-add-a-language.md`: for translators, with Weblate, Poedit and `msgmerge`.
- The README links both, and `game-data.md` drops `name` and `line` from the TOML examples.
