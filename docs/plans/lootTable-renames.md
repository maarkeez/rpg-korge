# Plan: Rename the code of the loot table subdomain to the words of the glossary

> **Status:** to do.
>
> **Summary:** The code and docs of the loot table subdomain call a loot table "the weighted entries a defeated unit may
> drop", while a loot table belongs to no unit: `tier-1` is shared by every loot drop that lists it, and the
> [glossary](../../GLOSSARY.md) keeps Drop for an item left on a tile. The comment of an entry says it is "dropped with
> the given weight", for what the glossary calls a roll. And `InvalidLootTableEntryType` is thrown when a `LOOTABLE_ITEM`
> entry has no `lootableItemId`, whose type is not what is wrong. E.g.: an entry of `tier-2` with `type =
> "LOOTABLE_ITEM"`, `weight = 3` and no `lootableItemId` fails with "Invalid loot table entry type". Instead, the code
> and docs say a loot table is what loot drops roll on, an entry is rolled with its weight, and the error names the
> missing lootable item. The game plays the same, and players read the same text. Builds on
> [Game data](../architecture/game-data.md) and the [loot drop renames](lootDrop-renames.md).

## Decisions

| Topic        | Decision                                                                                                                                                                           |
|--------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope        | The error, the comments and the doc below, in the loot table subdomain.                                                                                                            |
| Words kept   | `LootTable`, `Entry`, `EntryDto`, `Weight`, `NOTHING`, the `lootTable` package, `resources/lootTable/<loot table id>.toml` and its `[[entries]]` already say the glossary's words. |
| Tier         | `tier-1`, `tier-2` and `tier-3` stay as ids. Tier is only how some ids are named, not a term: `goat-wizard` is no tier, and the code knows none.                                   |
| Error        | `InvalidLootTableEntryType` becomes `LootTableEntryWithoutLootableItem`, as its test already says: "when a lootable item entry has no lootable item".                              |
| Save format  | Not reached: loot tables are game data, never saved, so `SAVE_VERSION` does not change.                                                                                            |
| Player text  | None changes, and no translation key or catalog changes: players never read loot tables.                                                                                           |
| Tests        | No new tests: a rename changes no behaviour. The existing test expects the renamed error.                                                                                          |
| Out of scope | The devlog, which tells what was built at the time. See [Not in this plan](#not-in-this-plan).                                                                                     |

## Renames

| Today                                        | Glossary term | New name                                                         |
|----------------------------------------------|---------------|------------------------------------------------------------------|
| `LootTableError.InvalidLootTableEntryType`   | Entry         | `LootTableEntryWithoutLootableItem`                              |
| Its message, "Invalid loot table entry type" | Entry         | "A lootable item entry of a loot table requires a lootable item" |

## lootTable

### Domain

- `LootTable`'s comment says "The weighted entries loot drops roll on, shared by every loot drop that lists it. It only
  holds the weights: a loot drop's roll turns them into chances.", for "The weighted entries a defeated unit may drop. It
  only holds the weights: the loot drop turns them into chances.".
- `Entry`'s comment says "A lootable item, or nothing when [lootableItemId] is `null`, rolled with the given [weight].",
  for "dropped with the given [weight]".
- `Entry(dto)` throws `LootTableEntryWithoutLootableItem` when a `LOOTABLE_ITEM` entry has no lootable item, and
  `LootTable.kt` imports it.

## Docs

- [Game data](../architecture/game-data.md): the `lootTable/<loot table id>.toml` section starts with "The weighted
  entries loot drops roll on, shared by every loot drop that lists it.", for "The weighted entries a defeated unit may
  drop.".

## Tests

- `LootTableTest`
  - `Create should fail when a lootable item entry has no lootable item` keeps its name and expects
    `LootTableEntryWithoutLootableItem`.

## Not in this plan

- [Add loot mechanic](../devlog/012-add-loot-mechanic.md) keeps its words, as a devlog tells what was built at the time.
