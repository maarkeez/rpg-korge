# Translations are gettext PO catalogs, read by our own parser

The game is translated with standard gettext PO catalogs under `resources/localization`: `messages.pot`, the English
source, maintained by hand, and one `<language code>.po` per language. Each message is keyed by its `msgctxt`, with its
English text as its `msgid`, and plurals are native PO plurals, whose `Plural-Forms` rule is evaluated at runtime. We
chose PO so translators can use Weblate, Crowdin, Poedit or any other PO tool, and the `msgctxt` key so the key never
changes while editing the English text marks its translations as fuzzy when they are merged.

## Considered options

- A Kotlin Multiplatform gettext library: none was mature enough, so `PoFile` is a small PO parser of our own.
