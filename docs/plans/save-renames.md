# Plan: Rename the code of the save subdomain to the words of the glossary

> **Status:** to do.
>
> **Summary:** The code of the save subdomain loads a game but announces it restored (`LoadGame` publishes
> `GameRestored`), while the [glossary](../../GLOSSARY.md) gives Restore to a battle unit getting mana back from an
> effect. It deletes a save slot (`DeleteSaveSlot`, `SaveSlotDeleted`), while the slot stays, empty: only its save is
> gone. And `SetUpGame` says "saved game" (`isLoadingASavedGame`) for a save. E.g.: deleting the knight's game in slot 3
> leaves slot 3 showing `Empty`, yet the code says the slot was deleted. So reading the code and talking about the game
> take a translation. Instead, the code, the tests and the docs say load, delete a save and save. The game plays the
> same, and players read the same text. Builds on [Save and load](../architecture/save-and-load.md) and the
> [battle setup renames](battlesetup-renames.md), which left Save and Load to this section.

## Decisions

| Topic        | Decision                                                                                                                                                                          |
|--------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope        | The renames below, in the code, tests, comments and docs of the save subdomain, and where `gameSetup` and `screen` call its Api or say its words.                                 |
| Load         | A save is loaded, and `GameRestored` is `GameLoaded`. Chosen over Restore, as Restore is a battle unit getting mana back, and players read "Load game".                           |
| Delete       | `DeleteSave` deletes the save in a save slot, which `SaveDeleted` announces. Chosen over `DeleteSaveSlot`, as the slot stays, empty.                                              |
| Save         | What a save slot holds is a save, so `isLoadingASavedGame` is `isLoadingASave`, and "a saved game" is "a save" in comments and test names.                                        |
| Overwrite    | Saving into a slot holding a save overwrites it, so the comment of `Save.save` says "overwriting the save there", for "replacing".                                                |
| Words kept   | `Save`, `SaveGame`, `GameSaved`, `LoadGame`, `SearchSaveSlots`, `SAVE_VERSION`, `IncompatibleSave`, `Summary` and `Snapshots` already fit. `SaveFile` stays, as the JSON adapter. |
| Save format  | Not reached: no snapshot field and no summary field is renamed, and events are not saved. `SAVE_VERSION` stays 9.                                                                 |
| Player text  | None: players keep reading "Load game", "Delete" and "Delete the game saved in slot 3?", and no translation key changes.                                                          |
| Tests        | No new tests: a rename changes no behaviour. The existing tests are renamed with the code.                                                                                        |
| Out of scope | The restore of each subdomain's snapshot, and the save slots view. See [Not in this plan](#not-in-this-plan).                                                                     |

## Renames

| Today                            | Glossary term | New name                     |
|----------------------------------|---------------|------------------------------|
| `SaveEvent.GameRestored`         | Load          | `SaveEvent.GameLoaded`       |
| `DeleteSaveSlot`                 | Save          | `DeleteSave`                 |
| `SaveApi.deleteSaveSlot`         | Save          | `SaveApi.deleteSave`         |
| `SaveEvent.SaveSlotDeleted`      | Save          | `SaveEvent.SaveDeleted`      |
| `SetUpGame(isLoadingASavedGame)` | Save          | `SetUpGame(isLoadingASave)`  |
| "the game is restored"           | Load          | "the game is loaded"         |
| "a saved game"                   | Save          | "a save"                     |
| "replacing the game saved there" | Overwrite     | "overwriting the save there" |
| `DeleteSaveSlotTest`             | Save          | `DeleteSaveTest`             |

## save

- `SaveEvent.GameLoaded(slot)` announces the save in the slot is loaded: every subdomain is back as it was saved,
  without announcing it, so the screen draws it all again. `Save.load()` adds it, and the comments of `Save.load()` and
  `LoadGame` say "announces the game is loaded (`GameLoaded`)".
- `DeleteSave(slot)` deletes the save in the slot, even an incompatible one, so the slot is empty, and publishes
  `SaveEvent.SaveDeleted(slot)`. It moves to `usecases/commands/DeleteSave.kt`, and `SaveApi` builds `deleteSave`.
- The comment of `Save.save` says "Saves the game into the slot, overwriting the save there, if any", and the one of
  `SaveFile` says "A save as it is stored".

## Other subdomains

- `gameSetup`: `SetUpGame(isLoadingASave)` stops after the quests when a save is loaded. `BattleScene` calls
  `gameSetupApi.setUpGame(isLoadingASave = true)`.
- `screen`: `BattlefieldPresenter`, `BattleInfoPresenter`, `ExplorationPresenter` and `QuestPresenter` subscribe to
  `GameLoaded`, and their comments say "A save is loaded exploring" and "A save is loaded without announcing what it
  holds". `LoadGamePresenter` calls `saveApi.deleteSave`.

## Docs

- [Save and load](../architecture/save-and-load.md): step 1 of Loading names `DeleteSave` and `SaveDeleted`, and steps
  3 and 4 `GameLoaded`.
- [Quests](../architecture/quests.md): the tracker and the markers are drawn on `GameLoaded`.
- [Game data](../architecture/game-data.md): "To load a save, `SetUpGame` stops after step 11 (`isLoadingASave`)".
- `docs/architecture/dependencies.html` is regenerated by the pre-commit hook.
- The devlog stays as written, as it tells what was built then.

## Tests

The tests are renamed with the code, and their names and comments say the new words:

- `SaveTest`: `Load should hand the snapshots and announce the game is loaded when it was saved with this version`.
- `LoadGameTest`: `LoadGame should restore every snapshot in order and publish GameLoaded when the slot is compatible`.
- `DeleteSaveTest` (replaces `DeleteSaveSlotTest`): `DeleteSave should empty the slot when a game is saved in it` and
  `DeleteSave should empty the slot when its game can not be read`.
- `SetUpGameTest`: `should stop after the quests, which the save is loaded on, when a save is loaded`.
- `BattlefieldPresenterTest`: `GameLoaded should display every battle unit, npc and dropped item when a save is loaded`
  and `GameLoaded should display the npcs left and trail the leader of the party when a save is loaded while exploring`.
- `BattleInfoPresenterTest`: `DisplayExploring should display that the player explores when a save is loaded`.
- `ExplorationPresenterTest`: `GameLoaded should display the virtual pad in place of the battle HUD and the call to
  action when a save is loaded`.
- `QuestPresenterTest`: `QuestPresenter should draw the tracker without announcing when a save is loaded`,
  `QuestPresenter should draw the pinned quest when a save is loaded` and `QuestPresenter should draw the markers when
  a save is loaded`.

## Not in this plan

- The comment of `Save.slotOf` says "What a slot shows, from its summary, or `null` when it is empty", while it never
  returns `null`: an empty slot is a `SlotDto` with `EMPTY`. A slip in the comment, still to be fixed.

### Waiting for other sections

- `shared`: `Snapshottable.restore`, and each subdomain's `Restore…` command and `restore` factory (`RestoreParty`,
  `RestoreBattleUnits`, `BattleUnit.restore`…), say Restore for taking a snapshot back, which the effect section gives to
  a battle unit getting mana back.
- `screen`: `SaveSlotsView.Delegate`'s `saveSlotSelected` and `saveSlotDeleteSelected`, and the `ui.saveSlots.*` keys.
