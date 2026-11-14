# Tessavar

Tessavar is a turn-based RPG where a party explores the battlefield and fights battles on its tiles: recruit members in
the village, take on quests, trade with its merchants, and face the goat wizard. It is built with Kotlin and
[KorGE](https://docs.korge.org/korge/), and plays in the browser, on a phone or a computer.

You can play it at https://maarkeez.github.io/tessavar/

## Storybook

Every view of the game, in each of its states, can be browsed at https://maarkeez.github.io/tessavar/storybook/, and
shared with a link. See [ADR 0008](docs/adr/0008-in-game-storybook.md), and [How to add a story](docs/testing/how-to-add-a-story.md).

## Translations

The game is translated with gettext PO files, so you can translate it with Poedit, Weblate or any other PO tool. See
[How to add a language](docs/localization/how-to-add-a-language.md), and [ADR 0009](docs/adr/0009-gettext-po-catalogs-with-own-parser.md)
for how the game reads the translations.
