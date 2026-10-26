# Localization

The game is translated with standard [gettext PO](https://www.gnu.org/software/gettext/manual/html_node/PO-Files.html)
files, so translators can use Weblate, Crowdin, Poedit or any other PO tool. English is the source language. The
language is picked from the main menu, for the session: the game starts in English every time.

To translate the game, see [How to add a language](../localization/how-to-add-a-language.md).

## Catalogs

The catalogs live in [`resources/localization`](../../resources/localization), one per language:

- `messages.pot`: the template, in English. It is maintained by hand: there is no extraction tool.
- `<language code>.po`: a translation, e.g.: `es.po`, `it.po`. The game discovers them: the file name is the language
  code, and the `language.name` entry names the language in that language (e.g.: `Español`), in the language list.

Each message is keyed by its `msgctxt`, and its English text is its `msgid`, so every tool shows translators the English
text, the key never changes, and editing the English text marks its translations as fuzzy when they are merged:

```po
#. Displayed when the buyer can not afford the item.
msgctxt "ui.shop.notEnoughGold"
msgid "Not enough gold"
msgstr "No hay suficiente oro"
```

The keys follow two conventions:

| Text                 | Key                                    | E.g.                                       |
|----------------------|----------------------------------------|--------------------------------------------|
| UI                   | `ui.<view>.<message>`                  | `ui.shop.notEnoughGold`, `ui.menu.playNewGame` |
| Game data            | `<subdomain>.<entity id>.<field>`      | `unit.bat.name`, `npc.elder.line`, `terrain.grass.name` |
| The language's name  | `language.name`                        | `English`, `Español`                       |

The game data text is the name of each ability, lootable item, npc, player, terrain and unit, and the line of each npc.
The [game data files](game-data.md) hold no text, only ids.

### Messages

- **Placeholders** are named, so translators can reorder them: `Vanishes in {turns} turns`, `{player} turn`. `{` and `}`
  are only used for placeholders. A placeholder without an argument is displayed as it is.
- **Plurals** are native PO plurals (`msgid_plural`, `msgstr[n]`). The `Plural-Forms` header of each catalog is
  evaluated at runtime, e.g.: `nplurals=3; plural=(n==1 ? 0 : n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2);`,
  so a new language needs no code change.
- **Whole sentences**: the code never builds a sentence out of pieces, changes the case of a text, or puts an article
  in front of a name. `{terrain} can not be next to {neighbour}`, not `"$terrain can not be next to ${neighbour.lowercase()}"`.

### Fallback

A message without a translation (missing, empty, or fuzzy) is displayed in English. A key missing from `messages.pot`
displays the key itself, which the tests prevent. An invalid catalog fails when the resources are read, with its path in
the message, as an invalid TOML file does.

## Layering

Only the presentation layer translates. The `localization` subdomain only depends on `shared`, and only
`*.adapters.presentation` packages, and the root package that boots the game (`Main.kt`), may depend on it, as
[`LocalizationArchitectureTest`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/architecture/LocalizationArchitectureTest.kt)
enforces. The other subdomains hold ids, never text, and their events and DTOs carry no text either.

- [`localization`](../../src/commonMain/kotlin/com/mkz/rpg/localization): the `Localization` aggregate holds the
  catalogs and the selected language, translates the messages, and evaluates the plural forms.
  [`PoFile`](../../src/commonMain/kotlin/com/mkz/rpg/localization/adapters/resources/PoFile.kt) is a small PO parser,
  as no Kotlin Multiplatform gettext library is mature enough. `LocalizationApi` exposes it.
- [`UiText`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/UiText.kt): the enum of every UI key,
  so a view can not ask for a key that does not exist.
- [`Texts`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/Texts.kt): what the views translate
  with: `texts[UiText.SHOP_NOT_ENOUGH_GOLD]`, `texts[UiText.BATTLE_INFO_ROUND, "round" to 3]`,
  `texts.plural(UiText.REMAINING_TURNS, turns, "turns" to turns)`, and the game data from its id: `texts.unitName(unitId)`,
  `texts.npcLine(npcId)`, ... Views and presenters receive it through their constructor.
- `startGame` creates the `LocalizationApi` once and reads the catalogs before displaying the menu, and every scene
  receives it, so the language picked in the menu applies to every scene opened afterwards.

## Tests

[`LocalizationResourcesTest`](../../src/commonTest/kotlin/com/mkz/rpg/screen/adapters/presentation/LocalizationResourcesTest.kt)
fails the build when:

- a catalog does not parse;
- a `UiText` key, or a game data key (one per resource file), is missing from `messages.pot`, or a plural `UiText` is
  not a plural there;
- `messages.pot` has an entry the screen does not translate;
- a translation has an entry missing from `messages.pot`, other placeholders than its English text, or another number
  of plural forms than its `nplurals`;
- a text uses a character the font does not draw: the game only draws KorGE's `LATIN_ALL` characters, which cover
  Spanish, Catalan, Italian and German, but not Portuguese (`ã õ`) nor Polish (`ł ą`) yet.

Translations may be missing: they fall back to English.

The view and presenter tests translate with the real catalogs, so they keep asserting the English text. As the JS tests
can only read the resources asynchronously, and many tests build their views synchronously, the `testCatalogs` Gradle
task ([`TestCatalogsTask`](../../buildSrc/src/main/kotlin/testcatalogs/TestCatalogsTask.kt)) embeds the catalogs into a
generated `TestCatalogs` test source, which
[`TextsMother`](../../src/commonTest/kotlin/com/mkz/rpg/screen/adapters/presentation/TextsMother.kt) reads.

## How to add a UI text

1. Add its key to [`UiText`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/UiText.kt), as
   `ui.<view>.<message>`.
2. Add its English text to `messages.pot`, with a `#.` comment telling translators where it is displayed when the
   text alone does not.
3. Display it with `texts[UiText.<KEY>]`. The translations get it when they are merged next.
