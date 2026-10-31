# Save and load

The player saves the game from the pause screen, while exploring, into one of 3 save slots, and loads it back from the
menu's Load game. The saves are JSON, kept in KorGE's `views.storage`, so it works on every target KorGE runs on with no
platform-specific code. Saving and loading live in the [`save`](../../src/commonMain/kotlin/com/mkz/rpg/save) subdomain,
which depends on no other subdomain, and the screen uses it through `SaveApi`.

## The save slots

There are 3 fixed slots (`Save.SLOTS`). Each one is either:

| State          | Shows                                                         | Save         | Load            |
|----------------|---------------------------------------------------------------|--------------|-----------------|
| `EMPTY`        | `Empty`                                                       | At once      | Can not         |
| `SAVED`        | When it was saved, in local time, the leader and party size, e.g.: `2026-10-30 18:42 · Knight · party of 3` | Overwrite, once confirmed | Loads it, or Delete once confirmed |
| `INCOMPATIBLE` | `Incompatible save`                                           | Overwrite, once confirmed | Delete once confirmed |

A save is incompatible when it was written with another `SAVE_VERSION`, can not be decoded, or names what the game no
longer has (e.g.: a removed unit). Listing the slots ([`SearchSaveSlots`](../../src/commonMain/kotlin/com/mkz/rpg/save/usecases/queries/SearchSaveSlots.kt))
only reads their summaries, so a save naming a removed id shows as saved until it is loaded: loading it fails, and
Load game is displayed again, that slot as incompatible.

## Saving

1. Start, on the virtual pad, pauses the game (see [Exploration](exploration.md#pausing)). The game is only saved from
   there, so always while exploring, done walking: never in a battle, while talking or in the formation.
2. Save lists the slots in place of the pause screen ([`PausePresenter`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/PausePresenter.kt)).
   An empty slot saves at once; one holding a game asks to overwrite it first.
3. [`SaveGame`](../../src/commonMain/kotlin/com/mkz/rpg/save/usecases/commands/SaveGame.kt) takes the snapshot of each
   subdomain, in order, and writes the save file with its summary (`GameSaved`). It fails when the game is not paused.
4. The pause screen is displayed again, with `Saved` under its options for 1.5s.

## The save file

[`NativeStorageSaveRepository`](../../src/commonMain/kotlin/com/mkz/rpg/save/adapters/storage/NativeStorageSaveRepository.kt)
keeps each slot under two keys of the storage: the save under `save.slot<N>`, and its summary alone under
`save.slot<N>.summary`, so listing the slots does not read the snapshots. The game hands it `views.storage`: a
`game.storage` file in the settings folder on the JVM, and the `localStorage` of the browser. `Main.kt` hands it down
from scene to scene, so the tests save in an `InmemoryStorage` instead.

The save is a [`SaveFile`](../../src/commonMain/kotlin/com/mkz/rpg/save/adapters/json/SaveFile.kt), written with
kotlinx.serialization. A key it does not know fails to decode, so a save of another shape is incompatible rather than
half loaded:

```json
{
  "summary": { "version": 1, "savedAt": 1.79338572E12, "leaderUnitId": "knight", "partySize": 1 },
  "snapshots": {
    "battleUnit": [ { "id": "knight-1", "remainingHealthPoints": 30, "...": "..." } ],
    "battlefield": [ { "occupantId": "knight-1", "row": 1, "column": 1 } ],
    "...": "..."
  }
}
```

## Snapshots

Each subdomain holding runtime state takes and restores its own snapshot, through the
[`Snapshottable`](../../src/commonMain/kotlin/com/mkz/rpg/shared/domain/Snapshottable.kt) port of `shared.domain`: its
`key` (e.g.: `battleUnit`), `snapshot()` and `restore(snapshot)`. Each one writes its own JSON, so the `save` subdomain
never knows their shapes:

- `<subdomain>.adapters.save.<Subdomain>Snapshottable` holds the `@Serializable` classes, and maps them to the plain
  snapshot of the aggregate, never annotating the domain.
- The aggregate takes it (`toSnapshot()`, or `toDto()` when it already holds everything) and is rebuilt from it by a
  `restore` factory that publishes no events, e.g.: `BattleUnit.restore(snapshot)`.
- Restoring replaces the whole state of the subdomain, and fails when the snapshot names an id the game no longer has
  (e.g.: a unit, a lootable item, a tile).

Only runtime state is saved. What never changes while playing (terrains, effects, abilities, units, lootable items, loot
tables, players, the map and its buildings) is read from the resources again. Short-lived state is reset.

| #  | Subdomain        | Saved                                                                 | Reset on load                                  |
|----|------------------|-----------------------------------------------------------------------|------------------------------------------------|
| 1  | `battleUnit`     | Every battle unit as it is: health, mana, equipped effects, cooldowns, learned abilities, maximum abilities, ongoing effects, remaining turns | -                                              |
| 2  | `battlefield`    | The occupant of each tile                                             | -                                              |
| 3  | `npc`            | The npcs left (a recruited npc is gone), their tiles and stocks      | -                                              |
| 4  | `lootDrop`       | The dropped items on each tile                                        | -                                              |
| 5  | `inventory`      | Stored and equipped items, and gold                                   | -                                              |
| 6  | `party`          | The members and the line                                              | It explores again: no pending step, no battle unit moved this second |
| 7  | `cpuWanderBrain` | The homes, the battle units returning home, and the respawns          | The wandering in progress                      |

The battle and the CPU battle brain hold no state while exploring, so they are not saved: the CPU battle brain starts
its state with a CPU turn. The random rolls are not saved either, so a reload rolls anew.

`BattleScene` hands `SaveApi` the snapshottables in this order, the order the subdomains are initialised in, so each is
restored after the ones it refers to (e.g.: the party after its battle units).
[`SnapshotCoverageArchitectureTest`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/architecture/SnapshotCoverageArchitectureTest.kt)
fails when a subdomain with a repository has no `Snapshottable` and is not listed as not saved, with why.

## Loading

1. Load game, in the menu, lists the slots in place of its options ([`LoadGamePresenter`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/LoadGamePresenter.kt)).
   Only a saved slot can be chosen; Delete, on any slot not empty, deletes its game once confirmed
   ([`DeleteSaveSlot`](../../src/commonMain/kotlin/com/mkz/rpg/save/usecases/commands/DeleteSaveSlot.kt), `SaveSlotDeleted`).
2. Choosing a slot starts `BattleScene` with it. It runs the first 10 steps of the [setup](game-data.md#how-the-battle-is-set-up),
   terrains to buildings, as a new game does, and dispatches them, so the battlefield and its buildings are drawn.
3. [`LoadGame`](../../src/commonMain/kotlin/com/mkz/rpg/save/usecases/commands/LoadGame.kt) restores each snapshot in
   order, silently, without creation events, as replaying them would create empty inventories and new homes. Then it
   publishes one `GameRestored`.
4. On `GameRestored`, the presenters draw the whole state from the queries: `BattlefieldPresenter` places every battle
   unit, npc and dropped item, and trails the leader, `ExplorationPresenter` displays the pad, and
   `BattleInfoPresenter` displays `Exploring`.

A save that fails to load (`IncompatibleSave`) goes back to the menu, which lists the slots again, that one as
incompatible.

## `SAVE_VERSION`

Every save holds `Save.SAVE_VERSION`. There are no migrations: a save of another version is incompatible, and can only
be deleted. Any change to the shape of a snapshot (e.g.: a field renamed, added or removed) MUST bump `SAVE_VERSION`.

[`SaveFixtureTest`](../../src/jvmTest/kotlin/com/mkz/rpg/save/adapters/json/SaveFixtureTest.kt) guards it with golden
saves under [`src/jvmTest/fixtures/save`](../../src/jvmTest/fixtures/save), one per version:

- A game played with nothing random must still be saved as `v<SAVE_VERSION>.json`, so a change to a snapshot fails it.
- `v<SAVE_VERSION>.json` encodes as it was, and loads back: the knight on its tile, with its health potion and 40 gold.
- An older save (`v0.json`) is refused.

When a snapshot changes on purpose, bump `SAVE_VERSION`, then write the new fixture, keeping the older ones:

```shell
./gradlew jvmTest --tests '*SaveFixtureTest*' -Drpg.updateSaveFixtures=true
```

## Known limitations

- A save naming a removed id shows as saved in Load game until it is loaded, as listing only reads the summaries.
- Clearing the browser's site data deletes the saves, and there is no way to back them up.
- Any change to a snapshot's shape bumps `SAVE_VERSION`, so every earlier save becomes incompatible.
