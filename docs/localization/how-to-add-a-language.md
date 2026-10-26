# How to add a language

This guide translates the game into a new language, or updates a translation after the English text changed. The
translations are gettext PO files, so any PO tool works: [Poedit](https://poedit.net/), [Weblate](https://weblate.org/),
or the gettext command line tools (`msginit`, `msgmerge`).

The game only draws some Latin characters for now: the basic Latin letters, `ç ñ å æ ø ÿ ß`, and the vowels with an
acute, grave, circumflex or diaeresis accent (e.g.: `á à â ä`). Spanish, Catalan, Italian and German fit; Portuguese
(`ã õ`), Polish (`ł ą`) or any other script do not yet.

## Add a language

1. Create the catalog from the template, [`resources/localization/messages.pot`](../../resources/localization/messages.pot),
   named after the language code: `resources/localization/<language code>.po`, e.g.: `it.po`.
   - With Poedit: *File* › *New from POT/PO file…*, pick `messages.pot` and the language, and save it as `it.po`.
   - With the command line:

     ```shell
     msginit --input=resources/localization/messages.pot --locale=it --output-file=resources/localization/it.po --no-translator
     ```

2. Check that its header has the `Plural-Forms` of the language, e.g.: `"Plural-Forms: nplurals=2; plural=(n != 1);\n"`.
   Poedit and `msginit` fill it in. The game fails to start without it.
3. Translate `language.name` into the name of the language, in that language, e.g.: `Italiano`. It names the
   language in the game's language list.
4. Translate the other entries. The English text is the `msgid`, and the `#.` comments tell where it is displayed. An
   entry left untranslated is displayed in English, so the translation can be finished in several steps.
5. Run the tests, then play the game and pick the language from *Language* in the main menu:

   ```shell
   ./gradlew jvmTest
   ./gradlew runJvm
   ```

## Update a language

When the English text changes, merge `messages.pot` into the translation:

- With Poedit: *Translation* › *Update from POT file…*, and pick `messages.pot`.
- With the command line:

  ```shell
  msgmerge --update --backup=none resources/localization/es.po resources/localization/messages.pot
  ```

New entries come in untranslated. Entries whose English text changed are marked fuzzy (`#, fuzzy`): the game displays
them in English until the translation is reviewed and the flag removed. Entries removed from `messages.pot` become
obsolete (`#~`), which the game ignores: they can be deleted.

## Rules

The tests fail when a translation breaks one of these rules:

- **Keep every placeholder**, with the same name: `{turns}`, `{player}`, ... They can be moved anywhere in the sentence,
  e.g.: `{player} turn` is `Turno de {player}`. Do not add others, and do not use `{` and `}` for anything else.
- **Translate every plural form**: a plural entry (`msgid_plural`) has one `msgstr[n]` per plural of the language, as its
  `Plural-Forms` says.
- **Only translate entries of `messages.pot`**: do not add entries, nor change a `msgctxt` or a `msgid`. Merge the
  template again instead.
- **Only use characters the game draws**: the ones listed above, and the common punctuation (e.g.: `¿ ¡ « » ·`), but
  not `…` nor typographic quotes (`“ ” ’`).

See [Localization](../architecture/localization.md) for how the game reads the catalogs.
