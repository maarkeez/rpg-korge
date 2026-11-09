# Plan: Rename the loading of a snapshot to the words of the glossary

> **Status:** done, built on `main` in `3e1f4eb8`.
>
> **Summary:** Every subdomain holding runtime state takes its snapshot back into play with a word the
> [glossary](../../GLOSSARY.md) rejects: `Snapshottable.restore(snapshot)`, the `Restore…` commands (`RestoreParty`,
> `RestoreBattleUnits`…) and the `restore` factories (`BattleUnit.restore`, `Party.restore`…). The glossary gives
> Restore to a battle unit getting mana back from an effect, and the Save section says Load, avoiding Restore. E.g.:
> loading the knight's save in slot 1 calls `LoadGame`, which calls `restore` on each snapshottable, which calls
> `RestoreBattleUnits`, which calls `BattleUnit.restore`, while a Mana potion restores 10 mana to the same knight. So
> reading the code and talking about the game take a translation. Instead, a snapshot is loaded: `Snapshottable.load`,
> the `Load…Snapshot` commands, mirroring the `Search…Snapshot` queries, and the `load` factories. The game plays the
> same, and players read the same text. Builds on [Save and load](../architecture/save-and-load.md) and the
> [save renames](save-renames.md), which left these words to this section.

## Decisions

| Topic        | Decision                                                                                                                                                                                                    |
|--------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope        | The renames below, in the code, tests, comments and docs of every subdomain holding a snapshot, and of `shared`, `save`, `gameSetup` and `screen` where they say them.                                      |
| Load         | A snapshot is loaded, as a save is. Chosen over keeping Restore as a programmer's word, as Restore is a battle unit getting mana back.                                                                       |
| Commands     | Each `Restore…` command is `Load…Snapshot`, mirroring its `Search…Snapshot` query, e.g.: `SearchPartySnapshot` and `LoadPartySnapshot`. Chosen over `LoadParty`, which reads as loading the party's game data. |
| Loaders      | The `<Subdomain>Loader`s and `Resources<Subdomain>Loader`s keep their names: they read game data from the resources, never a snapshot.                                                                       |
| Words kept   | Restore keeps its meaning for mana: `RestoreMana`, `RestoreManaDto`, `RESTORE_MANA`, `restoreManaEffect` and the **Screen / Mana bar preview › Restored** story.                                              |
| Game data    | Game data, Definition, Bar, Preview bar, Blink and Disabled already fit the code: `TomlResources`, the `*Definition` classes, `BarView`, `PreviewBarView`, `blink` and `GameButton` keep their names.        |
| Save format  | Not reached: no snapshot field and no snapshot `key` is renamed, so `SAVE_VERSION` does not change.                                                                                                         |
| Player text  | None: no translation key or catalog changes.                                                                                                                                                                |
| Tests        | No new tests: a rename changes no behaviour. The existing tests are renamed with the code.                                                                                                                  |
| Out of scope | The other meanings of restore, and the unused colors of `ColorPalette`. See [Not in this plan](#not-in-this-plan). Devlog entries stay as written.                                                           |

## Renames

| Today                                     | Glossary term | New name                             |
|-------------------------------------------|---------------|--------------------------------------|
| `Snapshottable.restore(snapshot)`         | Load          | `Snapshottable.load(snapshot)`       |
| `RestoreBattleUnits`                      | Load          | `LoadBattleUnitsSnapshot`            |
| `RestoreBattlefield`                      | Load          | `LoadBattlefieldSnapshot`            |
| `RestoreNpcs`                             | Load          | `LoadNpcsSnapshot`                   |
| `RestoreLootDrops`                        | Load          | `LoadLootDropsSnapshot`              |
| `RestoreInventories`                      | Load          | `LoadInventoriesSnapshot`            |
| `RestoreParty`                            | Load          | `LoadPartySnapshot`                  |
| `RestoreCpuWanderBrain`                   | Load          | `LoadCpuWanderBrainSnapshot`         |
| `RestoreQuests`                           | Load          | `LoadQuestsSnapshot`                 |
| `BattleUnit.restore`, and its nested ones | Load          | `BattleUnit.load`                    |
| `Party.restore`                           | Load          | `Party.load`                         |
| `Inventory.restore`                       | Load          | `Inventory.load`                     |
| `Npc.restore`                             | Load          | `Npc.load`                           |
| `CpuWanderBrain.restore`                  | Load          | `CpuWanderBrain.load`                |
| `Battlefield.restoreOccupants`            | Load          | `Battlefield.loadOccupants`          |
| `LootDrop.restoreDroppedItems`            | Load          | `LootDrop.loadDroppedItems`          |
| `Quest.restoreProgress`                   | Load          | `Quest.loadProgress`                 |
| "restores", "restored" a snapshot         | Load          | "loads", "loaded"                    |

## shared

- `Snapshottable.load(snapshot)` replaces the whole state of the subdomain with its snapshot, publishing no events, and
  fails when the snapshot can not be loaded, e.g.: it names a unit the game no longer has. Its comment says "taken to
  save the game and loaded to load it" and "Loading replaces the whole state".

## Subdomains holding a snapshot

- `battleUnit`: `LoadBattleUnitsSnapshot` loads every battle unit through `BattleUnit.load(snapshot)`, which loads its
  equipped effects, remaining cooldowns and ongoing effects through the nested `load`s of `BattleUnit`. `BattleUnitApi`
  builds `loadBattleUnitsSnapshot`, and `BattleUnitSnapshottable.load` calls it.
- `battlefield`: `LoadBattlefieldSnapshot` loads the occupant of each tile through `Battlefield.loadOccupants` and
  `Tiles.loadOccupants`.
- `npc`: `LoadNpcsSnapshot` loads the npcs left through `Npc.load(dto)`.
- `lootDrop`: `LoadLootDropsSnapshot` loads the dropped items on each tile through `LootDrop.loadDroppedItems`.
- `inventory`: `LoadInventoriesSnapshot` loads the stored and equipped items and the gold through `Inventory.load(dto)`.
- `party`: `LoadPartySnapshot` loads the members and the walking order through `Party.load(snapshot)`.
- `cpuWanderBrain`: `LoadCpuWanderBrainSnapshot` loads the homes, the battle units returning home and the awaited
  respawns through `CpuWanderBrain.load(snapshot)`.
- `quest`: `LoadQuestsSnapshot` loads each quest's status, current task, progress and pin through `Quest.loadProgress`.
- Each `<Subdomain>Snapshottable` overrides `load`, each Api builds its `load…Snapshot` command, and each command moves
  to `usecases/commands/Load…Snapshot.kt`.

## Other subdomains

- `save`: `LoadGame` and `Save.load` load each snapshot in order, and their comments say so. `FakeSnapshottable`
  overrides `load`.
- `gameSetup`: the comment of `SetUpGame` says "To load a save, … the save loads the rest".
- `screen`: the comments of `GameScene` say the subdomains are loaded in the order they are initialised, and the saved
  game is loaded once the game data is set.

## Docs

- [Save and load](../architecture/save-and-load.md): Snapshots names `load(snapshot)`, the `Load…Snapshot` commands
  (e.g.: `LoadPartySnapshot`) and the `load` factory (e.g.: `BattleUnit.load(snapshot)`), and Loading says `LoadGame`
  loads each snapshot.
- [Package structure](../architecture/package-structure.md): Saving names `SearchPartySnapshot` and `LoadPartySnapshot`.
- [Game data](../architecture/game-data.md): the battle units, the npcs, the party and the quests are loaded from the
  save.
- [Quests](../architecture/quests.md): `LoadQuestsSnapshot` publishes nothing.
- `RepositoryAccessArchitectureTest`: its comment says "a snapshottable loading a save".
- `docs/architecture/dependencies.html` is regenerated by the pre-commit hook.

## Tests

The tests are renamed with the code, and their names and comments say the new words:

- `LoadBattleUnitsSnapshotTest`, `LoadBattlefieldSnapshotTest`, `LoadNpcsSnapshotTest`, `LoadLootDropsSnapshotTest`,
  `LoadInventoriesSnapshotTest`, `LoadPartySnapshotTest`, `LoadCpuWanderBrainSnapshotTest` and
  `LoadQuestsSnapshotTest` replace the `Restore…Test`s, e.g.: `LoadQuestsSnapshot should put back the progress of each
  quest, publishing no events` and `LoadQuestsSnapshot should fail when the task no longer exists`.
- Each `<Subdomain>SnapshottableTest`: `Load should rebuild the same state when it loads the snapshot it took`, and
  `QuestSnapshottableTest`: `Load should pin the quest when it was saved pinned`.
- `LoadGameTest`: `LoadGame should load every snapshot in order and publish GameLoaded when the slot is compatible`.
- `SaveFixtureTest`: `LoadGame should load the fixture's leader tile, items and gold when it loads the fixture of
  SAVE_VERSION`.
- `BattlefieldPresenterTest`, `SaveFixtureTest` and `QuestMapperTest` call `snapshottable.load` and `loadProgress`, and
  the `restored` and `restoredRepository` variables are `loaded` and `loadedRepository`.

## Not in this plan

- Restore keeps three other meanings, neither a snapshot nor mana: `InventoryPresenter.restoreGiverSelection` selects
  the giver again, `ShopPresenter.close` and `FormationPresenter.close` "restore the tiles", and `BattleUnitTest` says
  "ResetActions should restore the remaining steps and casts". Whether each gets its own word is still to be decided.
- A comment of `HudStyle` says "the health a heal would restore", while the glossary gives Restore to mana only.
- `ColorPalette.TRANSPARENT` and `ColorPalette.SystemGray3` are never used, and `TRANSPARENT` is `RGBA(0, 0, 0, 1)`,
  with an alpha of 1, not 0. Whether to delete them is still to be decided.

## Changes while building

- `BattlefieldPresenter.displayRestoredGame` is `displayLoadedGame`, as it draws the battlefield once a save is loaded.
- `QuestEndToEndTest` says `should keep the pinned quest and the markers when a game is loaded`, for "should restore",
  as "should load … when a game is loaded" would say the same word twice.
- The comment of `GameScene` says the saved game is loaded "once the game data read from the resources is set up", for
  "the content", following the Game data of the glossary.
