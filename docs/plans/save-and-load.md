# Plan: Save and load the game

> **Status:** in progress: M1 and M2 done.
>
> **Summary:** Today a game only lives in memory. E.g.: the player walks the knight's party across the battlefield,
> recruits the elder, buys a health potion and wins two battles; closing the window, or going back to the menu, loses
> all of it, and Play new game starts again from the deployment in the resources. Instead, a Start button on the
> virtual pad opens a pause screen, which saves the game into one of 3 slots, and the menu's Load game lists the slots
> and loads one, back to the tile the party stood on, with its health, items, gold and recruits. It works on every
> target KorGE runs on with no platform-specific code: the saves are JSON, kept in KorGE's `views.storage`. It builds
> on [Game data](../architecture/game-data.md) and [Exploration](../architecture/exploration.md).

## Decisions

| Topic           | Decision                                                                                                                                                                                                                                                                                                                                       |
|-----------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope           | Saving while exploring, into 3 fixed slots, loading and deleting them from the menu, and a pause screen opened with a new Start button. Saving in a battle, while talking to an npc or in the formation is not possible: Start only shows while exploring.                                                                                      |
| Saving          | Manual only: Start → Save → pick a slot → confirm when it is taken → "Saved" → back to the pause screen. Chosen over an autosave after each battle won, which the player did not want.                                                                                                                                                          |
| Pause screen    | Resume, Save and Quit to menu, Quit asking to confirm as the progress since the last save is lost. Pausing stops time as the formation does: a new `PAUSED` mode of `Party`, so the party does not walk, no exploration second starts and the CPU battle units stop wandering.                                                                   |
| Slots           | 3 fixed slots, each showing its save date and time, the party leader and the party size, or "Empty". A slot can be deleted from Load game, after confirming. Chosen over a single slot and over unlimited named saves.                                                                                                                         |
| Capture         | Each subdomain holding runtime state takes and restores its own snapshot, through a shared `Snapshottable` port. Chosen over replaying the recorded events, which breaks every save on any rule or TOML change and grows with playtime, and over a single game state aggregate, which would couple every subdomain.                               |
| What is saved   | Only runtime state: battle units, battlefield occupants, inventories, npcs, dropped loot, the party and the CPU wander brain's homes and respawns. Static content (terrains, effects, abilities, units, lootable items, loot tables, players, the map and its buildings) is read from TOML again. Short-lived state is reset (e.g.: where a CPU battle unit is wandering to, the party's pending steps). |
| Battle          | The battle and the CPU battle brain are not saved, as there is no battle while exploring: they create their empty state on `GameRestored`, as they do on `ExplorationStarted`. The random rolls are not saved either, so a reload rolls anew.                                                                                                  |
| Loading         | `BattleScene` runs the first 10 steps of `SetupBattle` (terrains to buildings) as a new game does, then restores the snapshots silently, in initialisation order, without creation events, and publishes one `GameRestored`, on which the presenters draw the whole state from the queries. Chosen over replaying the creation events, which would recreate empty inventories and new wander brains, over a `Restored` event per subdomain, and over one scene-wide restorer that would duplicate the presenters' drawing. |
| Storage         | KorGE's `views.storage` (`NativeStorage`): a `game.storage` file in the app settings folder on the JVM, `localStorage` in the browser. Chosen over a VFS file per slot, which the browser can not write without `expect`/`actual` code.                                                                                                         |
| Format          | JSON with kotlinx.serialization, already enabled (`serializationJson()`). TOML stays for the files written by hand. Chosen over TOML, as ktoml's encoder handles neither sealed classes nor `null`, and the snapshots are deeply nested.                                                                                                        |
| Versioning      | `SAVE_VERSION`, a constant written in each save. A save of another version, one that can not be decoded, or one naming an id the resources no longer have (e.g.: a removed unit) is an "Incompatible save": it can be deleted, not loaded. No migrations.                                                                                      |
| Architecture    | A new `save` subdomain, depending on no other subdomain. `Snapshottable` in `shared.domain`; each snapshot class in its subdomain's `adapters.save`, never on the domain classes. `BattleScene` hands `SaveApi` the snapshottables in initialisation order.                                                                                    |
| Storybook       | **Screen / Virtual pad › Pad** gains Start. New **Screen / Pause**, **Screen / Save slots** and **Screen / Confirm dialog** components, and **Screen / Menu › Options** gains Load game.                                                                                                                                                        |
| Tests           | Unit tests of each snapshot and restore, presenter and view tests, a Konsist test that every subdomain holding runtime state is snapshottable, three golden fixture tests (round-trip, restore, refusal) and an end-to-end test saving then loading.                                                                                           |
| Out of scope    | Autosave, more or named slots, exporting or importing a save, migrating old saves, saving in a battle, and keeping the language across sessions.                                                                                                                                                                                              |
| Delivery        | 4 milestones, each green on CI. See [Milestones](#milestones).                                                                                                                                                                                                                                                                                 |

For example, the player saves in slot 2 with the knight leading, 3 party members, the knight at 18 of 30 health points,
a health potion in its bag, 40 gold, and a goblin defeated 12 steps ago:

| Step                           | What happens                                                                                                     |
|--------------------------------|------------------------------------------------------------------------------------------------------------------|
| Start, Save, slot 2            | Slot 2 is empty, so it saves at once: "Saved", then the pause screen again.                                      |
| Quit to menu, confirm          | The menu.                                                                                                        |
| Load game                      | Slot 1 "Empty", slot 2 "30 Oct 2026 18:42 · Knight · 3", slot 3 "Empty".                                        |
| Slot 2                         | Setup steps 1–10, then the snapshots are restored and `GameRestored` draws the knight at 18/30, the party behind it. |
| Walk 38 steps                  | The goblin respawns on its home tile, 50 steps after its defeat, as the respawn counter was saved.               |

## Milestones

Each milestone is a few commits straight on `main`, green on CI.

| #  | Milestone        | Done when                                                                                                       | Status | Done in |
|----|------------------|-----------------------------------------------------------------------------------------------------------------|--------|---------|
| M1 | Pause screen     | Start, shown only while exploring, opens the pause screen, which stops time; Resume and Quit to menu work.      | ✅      | `e4544e3`, `d0162ea`, `181c98f` |
| M2 | Saving           | Save writes the game into one of 3 slots, confirming before overwriting, and the slots survive a restart.       | ✅      | `d6e2e90`, `cd3ae5f`, `3dcdf3a` |
| M3 | Loading          | Load game in the menu lists the slots, loads one back to where it was saved, and deletes one.                  | ⬜      |         |
| M4 | Fixtures and docs| The golden fixture tests guard `SAVE_VERSION`, and the architecture docs describe saving and loading.           | ⬜      |         |

### M1 commits

1. `Party` gains the `PAUSED` mode, `openPause()` and `closePause()`, with `PauseOpened` and `PauseClosed`, and
   `PartyApi` exposes them.
2. `VirtualPadView` gains the Start button, shown only while exploring, and its story.
3. `PauseView` with Resume, Save (not wired yet) and Quit to menu, `ConfirmDialogView` for the quit confirmation, the
   `PausePresenter`, their stories and texts.

### M2 commits

1. `Snapshottable` in `shared.domain`, and the `save` subdomain: `SaveSlot`, `SaveSummary`, `SaveSlotRepository`,
   `InMemorySaveSlotRepository` and `NativeStorageSaveSlotRepository`.
2. The snapshots of `battleUnit`, `battlefield`, `inventory`, `npc`, `lootDrop`, `party` and `cpuWanderBrain`, each
   with its `restore` factory and tests.
3. `SaveGame` and `SearchSaveSlots`, `SaveApi`, and the Konsist `SnapshotCoverageArchitectureTest`.
4. `SaveSlotsView` in its save mode, the overwrite confirmation and "Saved" in the pause screen, wired by
   `PausePresenter`, with stories and texts.

### M3 commits

1. `LoadGame`, `GameRestored` and `DeleteSaveSlot`; `battle` and `cpuBattleBrain` create their empty state on
   `GameRestored`.
2. `BattleScene` loads a slot instead of setting up a new game, and the presenters draw the whole state on
   `GameRestored`.
3. `MenuView` gains Load game, `SaveSlotsView` its load mode with delete, and `LoadGamePresenter`, with stories and
   texts.
4. `SaveAndLoadEndToEndTest`.

### M4 commits

1. The golden fixtures `save/v1.json` and `save/v0.json`, and `SaveFixtureTest`, regenerating the fixture with
   `-Drpg.updateSaveFixtures=true`.
2. The [Save and load](../architecture/save-and-load.md) doc, and the links to it.

## Reference

### shared

- `Snapshottable` (`shared.domain`): `key` (e.g.: `"battleUnit"`), `snapshot(): String` and `restore(snapshot: String)`.
  Each subdomain's adapter writes and reads its own JSON, so the `save` subdomain never knows their shapes.

### save

#### Domain

- `SaveSlot`: its `SlotNumber` (1 to 3, `SaveError.SlotOutOfRange` otherwise) and its state: `Empty`,
  `Saved(summary)` or `Incompatible`.
- `SaveSummary`: `savedAt` (korlibs-time `DateTime`, shown in local time, e.g.: "30 Oct 2026 18:42"), `leaderUnitId`
  (its name is translated, e.g.: Knight) and `partySize`.
- `SaveEvent`: `GameSaved(slot)`, `GameRestored(slot)` and `SaveSlotDeleted(slot)`.
- `SaveError`: `SlotOutOfRange` and `IncompatibleSave(slot)`.
- `SaveSlotRepository`: `search(slot)`, `save(slot, file)` and `delete(slot)`.

#### Use cases

- `SaveGame(slot)` fails when the party is not exploring. It takes each snapshot in order, and writes the save file:
  `version` (`SAVE_VERSION`), `savedAt`, `summary` and `snapshots`, keyed by each `Snapshottable.key`. Publishes
  `GameSaved`.
- `LoadGame(slot)` refuses an incompatible slot. It restores each snapshot in order, and publishes `GameRestored`. Any
  failure while restoring (e.g.: `"goblin-archer"` is no longer a unit) is reported as `IncompatibleSave`, and the scene
  goes back to Load game.
- `DeleteSaveSlot(slot)` empties it and publishes `SaveSlotDeleted`.
- `SearchSaveSlots` lists the 3 slots, reading only the summaries, so listing does not decode the snapshots.

#### Adapters

- `SaveFile` (`adapters.json`): the `@Serializable` save file. Decoded with `ignoreUnknownKeys = false`.
- `NativeStorageSaveSlotRepository` (`adapters.storage`): `views.storage`, the save under `save.slot<N>` and its
  summary under `save.slot<N>.summary`. `InMemorySaveSlotRepository` for the tests.
- `SaveApi` (`adapters.presentation`): built with the repository and the snapshottables. `MenuScene` builds it without
  snapshottables, to list and delete the slots.

### Snapshots

Each subdomain holding runtime state gains `adapters.save.<Subdomain>Snapshot` (`@Serializable`) and
`<Subdomain>Snapshottable`, and its aggregate a `restore(dto)` factory that publishes no events. In restore order:

| Subdomain        | Saved                                                                                    | Reset on load                                              |
|------------------|------------------------------------------------------------------------------------------|------------------------------------------------------------|
| `battleUnit`     | Every field of `BattleUnit`: health, mana, equipped effects, cooldowns, learned abilities, ongoing effects, remaining turns | -                                          |
| `battlefield`    | The occupant of each tile                                                                | -                                                          |
| `npc`            | The npcs left (a recruited npc is gone), their tiles and stocks                         | -                                                          |
| `lootDrop`       | The dropped items on each tile                                                           | -                                                          |
| `inventory`      | Stored and equipped items, and gold                                                      | -                                                          |
| `party`          | The members and the line                                                                 | Its mode (`EXPLORING`), pending and awaited steps, the battle units moved this second |
| `cpuWanderBrain` | Its homes and respawns                                                                   | The returning and pending battle units, the awaited step, the purpose |

`battle` and `cpuBattleBrain` are not snapshottable, and create their empty state on `GameRestored`.

### party

- `Party.openPause()` pauses while exploring (`PAUSED`, `PauseOpened`); `closePause()` explores again (`PauseClosed`).
  Nothing happens in any other mode, as with `openFormation()`.
- `Party.restore(dto)` explores, with the saved members and line.

### screen

- `VirtualPadView` gains `startButton`, a "START" pill centred at the bottom of the pad, between the arrows and B and A,
  and `setOnStartPressed`. `ExplorationPresenter` shows it only while exploring.
- `PauseView`: Resume, Save and Quit to menu, and "Saved" under them for 1.5s after saving.
- `SaveSlotsView`: the 3 slots, each "Empty", its summary, or "Incompatible save". In save mode, tapping a slot saves;
  in load mode, it loads, and each slot that is not empty has Delete.
- `ConfirmDialogView`: a message over `CancelAndConfirmView`, for overwriting a slot, deleting one and quitting.
- `PausePresenter`: Start opens the pause; Resume closes it; Save opens `SaveSlotsView`, confirms an overwrite, calls
  `SaveApi`, shows "Saved"; Quit confirms and goes back to `MenuScene`.
- `MenuView` gains Load game, under Play new game. `LoadGamePresenter` lists the slots, confirms a delete, and starts
  `BattleScene` with the slot.
- `BattleScene(localizationApi, random, slot = null)`: with a slot, `SetupBattle` stops after
  `RequestInitialiseBuildings`, and `SaveApi.load(slot)` restores the rest.
- On `GameRestored`, `BattlefieldPresenter` places every battle unit, npc and dropped item, and `ExplorationPresenter`
  shows the pad and centres the camera on the leader.

### Resources

- `messages.pot`: `pause.resume`, `pause.save`, `pause.quit`, `pause.saved`, `pause.quitConfirmation`,
  `saveSlots.empty`, `saveSlots.incompatible`, `saveSlots.delete`, `saveSlots.overwriteConfirmation`,
  `saveSlots.deleteConfirmation` and `menu.loadGame`.

### Storybook

- `VirtualPadViewStories`: the Pad story (`screen-virtual-pad--pad`) shows Start.
- `PauseViewStories`: the Options (`screen-pause--options`) and Saved (`screen-pause--saved`) stories.
- `SaveSlotsViewStories`: the Save (`screen-save-slots--save`) and Load (`screen-save-slots--load`) stories, with a
  control per slot (`select`: empty, saved, incompatible).
- `ConfirmDialogViewStories`: the Overwrite, Delete and Quit stories (`screen-confirm-dialog--overwrite`, `--delete`,
  `--quit`).
- `MenuViewStories`: the Options story shows Load game.
- `StorySmokeTest` lists the new stories.

### Docs

- New [Save and load](../architecture/save-and-load.md): the slots, the save file, `Snapshottable`, the restore order,
  `GameRestored`, `SAVE_VERSION` and how to regenerate the fixtures.
- [Game data](../architecture/game-data.md): loading runs steps 1–10 of the setup, then restores.
- [Exploration](../architecture/exploration.md): the Start button and the pause.
- [Package structure](../architecture/package-structure.md): the `adapters.save` package.

### Tests

- `PartyTest`
  - `OpenPause should pause when the party is exploring`
  - `OpenPause should do nothing when the party is battling`
  - `ClosePause should explore when the party is paused`
  - `Restore should explore with the saved members and line`
- `<Subdomain>SnapshottableTest`, for each subdomain in the table
  - `Restore should rebuild the same state when it restores the snapshot it took`
  - `Restore should publish no events when it restores a snapshot`
- `SaveGameTest`
  - `SaveGame should write every snapshot in order when the party is exploring`
  - `SaveGame should fail when the party is not exploring`
- `LoadGameTest`
  - `LoadGame should restore every snapshot in order and publish GameRestored when the slot is compatible`
  - `LoadGame should refuse when the save is of another version`
  - `LoadGame should refuse when a snapshot names an id the resources no longer have`
- `SearchSaveSlotsTest`
  - `SearchSaveSlots should list the 3 slots, empty, saved or incompatible, when some are saved`
- `VirtualPadViewTest`
  - `ClickStart should call the Start listener once when Start is tapped`
- `PausePresenterTest`
  - `PausePresenter should ask to confirm when saving into a taken slot`
  - `PausePresenter should show Saved and the pause screen when the game is saved`
- `LoadGamePresenterTest`
  - `LoadGamePresenter should not load when the slot is incompatible`
  - `LoadGamePresenter should empty the slot when its deletion is confirmed`
- `SnapshotCoverageArchitectureTest`
  - `should be snapshottable when its subdomain holds runtime state`
- `SaveFixtureTest`
  - `SaveFile should encode the fixture as it was when it decodes the fixture of SAVE_VERSION`
  - `LoadGame should restore the fixture's leader tile, items and gold when it loads the fixture of SAVE_VERSION`
  - `LoadGame should refuse when it loads the fixture of an older version`
- `SaveAndLoadEndToEndTest`
  - `Game should be back where it was saved when the slot is loaded after quitting to the menu`

## Known limitations

- A save naming a removed id shows as saved in Load game until it is loaded, as listing only reads the summaries.
- Clearing the browser's site data deletes the saves, and there is no way to back them up.
- Any change to a snapshot's shape bumps `SAVE_VERSION`, so every earlier save becomes incompatible.
