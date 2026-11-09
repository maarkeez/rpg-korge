# Plan: Rename the code of the localization subdomain to the words of the glossary

> **Status:** done.
>
> **Summary:** The code and the docs of the localization subdomain use words the [glossary](../../GLOSSARY.md) rejects.
> E.g.: "translation" is a whole catalog in `Localization.translations` and in the docs ("`es.po`: a translation"), but
> one message in another language in `PoFile.Entry.translations` and "a message without a translation". "Plural forms" is
> a language's rule (`nplurals=2; plural=(n != 1);`) in `CatalogDto.pluralForms` and `InvalidPluralForms`, but a
> message's forms in `TooManyPluralForms`, and "count" is both the 3 of "Vanishes in 3 turns" and how many plural forms
> a language has (`PluralRule.count`). `messages.pot` is "the template", and the language is "picked" in the docs while
> the code selects it. Reading the code and talking about the game take a translation. Instead, the classes, fields,
> constants, comments and docs use the glossary's words. The game plays the same. Builds on
> [Localization](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/localization.md) and [How to add a language](../localization/how-to-add-a-language.md).

## Decisions

| Topic        | Decision                                                                                                                                                                                                                    |
|--------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope        | The renames below, in the code, tests and docs of `localization`, in `Main.kt`'s comment, and in `LocalizationResourcesTest`, which reads the catalogs.                                                                     |
| Save format  | Not reached: the localization is not saved, as the game starts in English every time, so `SAVE_VERSION` stays as it is.                                                                                                     |
| Player text  | None changes, and no translation key either. Only the header comment of `es.po` changes, which players never read.                                                                                                          |
| Translation  | A translation is one message in a language other than English, and a `.po` file is the catalog of that language. Chosen over calling the whole catalog a translation, as the PO format already calls each `msgstr` one.     |
| Plural rule  | The plural rule is how a language picks the plural form for a count, and the plural forms are a message's. The PO header keeps gettext's name, `Plural-Forms`, which Poedit and `msginit` fill in.                           |
| PoFile       | `PoFile` keeps gettext's own names (`context`, `id`, `idPlural`, `fuzzy`, `isTranslated`), as it mirrors the PO format translators know, though the glossary says key for `context`.                                          |
| Tests        | No new tests: a rename changes no behaviour. The existing tests are renamed with the code.                                                                                                                                  |
| Out of scope | The screen's `UiText` and `Texts`, which wait for their section, and the open questions. See [Not in this plan](#not-in-this-plan). Devlog entries and done plans stay as written.                                           |

## Renames

| Today                                                                               | Glossary term                  | New name                                    |
|-------------------------------------------------------------------------------------|--------------------------------|---------------------------------------------|
| `Localization.source`, `translations`, and the same fields of `Localization.Dto`    | Source catalog, Translation    | `sourceCatalog`, `translationCatalogs`      |
| `Localization.Dto.CatalogDto.pluralForms`                                           | Plural rule                    | `pluralRule`                                |
| `PluralRule.pluralForms`, the text of the rule                                      | Plural rule                    | `definition`                                |
| `PluralRule.count`, the `count` of `TooManyPluralForms`                             | Plural form, Count             | `formCount`                                 |
| `LocalizationError.InvalidPluralForms`, and its `pluralForms`                       | Plural rule                    | `InvalidPluralRule`, `pluralRule`           |
| `ResourcesLocalizationLoader.SOURCE_PLURAL_FORMS`                                   | Plural rule                    | `SOURCE_PLURAL_RULE`                        |
| `ResourcesLocalizationLoader.TRANSLATION_EXTENSION`                                 | Catalog, Translation           | `TRANSLATION_CATALOG_EXTENSION`             |
| `LocalizationMother.ENGLISH_PLURAL_FORMS`                                           | Plural rule                    | `ENGLISH_PLURAL_RULE`                       |

## localization

### Domain

- `Localization` follows the table. Its comment says "the source catalog, in English, and the catalog of every other
  language", and that a plural message has one plural form per plural of its language, chosen by the plural rule of its
  catalog, e.g.: `nplurals=2; plural=(n != 1);`. `PluralRule`'s comment says it is a catalog's plural rule: how many
  plural forms its messages have, and the C expression choosing one for a count `n`.
- `LocalizationError`: `InvalidPluralRule` says "Language 'pl' has an invalid plural rule '...'". `TooManyPluralForms`
  keeps its name and message, as it means a message's plural forms.

### Adapters

- `ResourcesLocalizationLoader` follows the table, with the locals `sourceCatalog` and `translationCatalogs`. Its comment
  says it loads the source catalog from `localization/messages.pot` and "one catalog per `localization/<language
  code>.po`", and that only the translated messages are kept: the untranslated ones are shown in English.
  `PLURAL_FORMS_HEADER` keeps its name, as it is the header's.
- `LocalizationApi` and `PoFile` need no change.

## Main

- `startApp`'s comment says the texts of every scene are "in the language selected for the session", for "picked".

## Translations

- `resources/localization/es.po`: its header comment says "The Spanish catalog of the game, merged from messages.pot",
  for "Spanish translation". No entry changes, in `es.po` nor in `messages.pot`.

## Docs

- [Localization](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/localization.md): the language is selected from the main menu, for "picked", also in
  Layering. `messages.pot` is "the source catalog, in English", for "the template", and `<language code>.po` is "the
  catalog of a language", for "a translation". Plurals say the plural rule of each catalog, its `Plural-Forms` header,
  and the aggregate "evaluates the plural rules". The heading Fallback is Untranslated messages, and says an
  untranslated message (missing, empty, or fuzzy) is shown in English.
- [How to add a language](../localization/how-to-add-a-language.md): "Create the catalog from the source catalog", for
  "the template", with step 2 checking the plural rule of the language in its `Plural-Forms` header, and "select the
  language from *Language* in the main menu", for "pick". "Merge the source catalog again", for "the template". Poedit's
  own menus (*New from POT/PO file…*, *Update from POT file…*) and "pick `messages.pot`" in a file dialog stay.

## Tests

- The tests that use these names are renamed with the code, with no change in what they assert: `LocalizationTest`,
  `LocalizationMother`, `ResourcesLocalizationLoaderTest`, `FakeLocalizationLoader`, and `LocalizationResourcesTest`,
  whose local `translations` is `translationCatalogs`.
- Test names say the same words, asserting the same:
  - `LocalizationTest`: `Create should fail when the plural rule of a catalog is not valid`, for "the plural forms".
  - `LocalizationTest`: `Create should fail when a message has more plural forms than the plural rule of its catalog`.
  - `LocalizationTest`: `TranslatePlural should choose the form by the plural rule of the selected language`.
  - `LocalizationTest`: `TranslatePlural should evaluate the operators of the plural rule`.
  - `LocalizationTest`: `Languages should return the source catalog first, then the translation catalogs by code, named in
    their language`.

## Not in this plan

- `Localization.languages()` names a catalog whose `language.name` is untranslated with the source catalog's message,
  so a new `it.po` not translated yet shows a second "English" in the language list, and `LocalizationTest` expects `pl`
  to be named "English". It looks like a bug, as naming it by its code (`it`) seems the intent, still to be decided.
- Nothing subscribes to `LocalizationInitialised` nor `LanguageSelected`: they are published, and only the tests read
  them. Whether they should stay is still to be decided.

### Waiting for other sections

- `screen`: `UiText`, `Texts`, and "UI text" and "game data text" in [Localization](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/localization.md)
  (e.g.: How to add a UI text), while Message is the glossary's word. `MenuView`'s comment "Picking one translates",
  while Select is the glossary's word.
- `storybook`: the story descriptions "as translations may be" already fit, as they mean a message's translation.

## Changes while building

- Names the plan left out follow the table: `LocalizationMother.POLISH_PLURAL_RULE`, the `definition` parameter of
  `PluralRule.parse` and its local `formCount`, and the parameters `sourceCatalog` and `translationCatalogs` of
  `LocalizationMother.localization`.
- `ResourcesLocalizationLoaderTest` names say the same words, asserting the same: `should load the source catalog in
  English, and a catalog per po file sorted by code`, and `should fail with the file when a translation catalog has no
  plural rule`. `LocalizationResourcesTest` says `should only have the source entries in the translation catalogs`.
- `Localization`'s comment says a message falls back to English "when it is untranslated there".
- `TextsTest`'s `languages should return English first, then the translations, named in their language` keeps its name,
  as it is the screen's and waits for its section.
