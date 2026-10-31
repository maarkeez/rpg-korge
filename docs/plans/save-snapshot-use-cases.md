# Plan: Take and restore the snapshots through use cases

> **Status:** in progress.
>
> **Summary:** the `save` subdomain depends on no other subdomain, and only `screen` uses it, through `SaveApi`, but
> nothing enforces either. Each `<Subdomain>Snapshottable` in `adapters.save` reads and writes its repository, checks
> the ids it restores against the other subdomains (e.g.: `BattleUnitSnapshottable.requireKnownIds`), and wipes and
> recreates the aggregates: business logic in an adapter. Instead, two architecture tests lock `save` in both
> directions, each saved subdomain takes its snapshot through a query and restores it through a command, and its
> `Snapshottable` only maps them to JSON. A third architecture test lets only the use cases call a repository. The saved
> JSON does not change. It builds on [Package structure](../architecture/package-structure.md) and
> [Save and load](../architecture/save-and-load.md).

## How a snapshot is taken and restored today

| Subdomain        | Takes it from                                   | Checks on restore                                   | Restores it by                                         |
|------------------|-------------------------------------------------|-----------------------------------------------------|--------------------------------------------------------|
| `battleUnit`     | `toSnapshot()` of every battle unit             | Its unit, player, abilities and effects exist       | Deleting every battle unit, creating each from `restore` |
| `battlefield`    | `occupants()` of the battlefield                | The battlefield is created (and, in the domain, each tile exists) | Updating it with `restoreOccupants`           |
| `npc`            | `toDto()` of every npc                          | The lootable items of each stock exist              | Deleting every npc, saving each from `restore`         |
| `lootDrop`       | `toDto()` of every loot drop                    | Each unit still drops loot                          | Updating each with `restoreDroppedItems`               |
| `inventory`      | `toDto()` of every inventory                    | The stored lootable items exist                     | `deleteAll`, creating each from `restore`              |
| `party`          | `toSnapshot()` of the party, if any             | Each member is a battle unit                        | Saving it from `restore`                               |
| `cpuWanderBrain` | `toSnapshot()` of the brain, if any             | -                                                   | Saving it from `restore`                               |

All of it is in `<subdomain>.adapters.save.<Subdomain>Snapshottable`, with the `@Serializable` mirror of the snapshot.

## Decisions

| Topic               | Decision |
|---------------------|----------|
| Save's dependencies | `save` MUST only depend on `shared`: `"save" to setOf("shared")` in `SubdomainDependencyArchitectureTest.allowedDependencies`, and a `save` row in the restricted subdomains table of [Package structure](../architecture/package-structure.md). |
| Save's dependents   | Only `screen` MUST import `save`, enforced by a new `SaveDependencyArchitectureTest`, in the style of `LocalizationArchitectureTest`. Chosen over naming only `screen.adapters.presentation`, as every importer is there today and `screen` has no other layer importing it. |
| Use cases           | Each saved subdomain gets a query, `Search<Aggregate>Snapshot`, returning what is to be saved, and a command, `Restore<Aggregate>`, taking it back: it checks the ids against the other subdomains and replaces the whole state. In `usecases.queries` and `usecases.commands`, following the `Search…` naming of the queries and the `restore` factories of the domain. |
| What they carry     | The existing types of the aggregate: its `Dto.SnapshotDto` where its `Dto` does not hold all it needs to be rebuilt (`battleUnit`, `party`, `cpuWanderBrain`), its `Dto` (or a part of it) elsewhere. Chosen over always the `Dto`, as the read model the presenters use would hold internals only there to save (e.g.: base and boosted maximums), and over always a `SnapshotDto`, as 4 of them would copy their `Dto` field for field. A subdomain gets a `SnapshotDto` once its `Dto` holds something its restore should not have to take. |
| Restore             | `Restore…` publishes no events and takes no `EventBus`, replaces the whole state, and fails with `require` (`IllegalArgumentException`) when the snapshot names an id the game no longer has, as today. `LoadGame` still wraps any failure into `IncompatibleSave`. Chosen over typed domain errors, as the message only helps debugging, and the save only needs to know it failed. |
| Snapshottable       | `<Subdomain>Snapshottable` stays in `<subdomain>.adapters.save`, keeping its `key` and the `@Serializable` mirror. It takes the query and the command, maps them to and from JSON, and does nothing else. The Api builds it from its two use cases, kept private: only `snapshottable` is public. Chosen over the Api implementing `Snapshottable` itself, as the JSON would move into presentation. |
| Repositories        | Only the use cases MUST call a repository. In production, a `<subdomain>.domain.<Name>Repository` is imported only by the domain and the use cases of its subdomain, by the `adapters.storage` class implementing it, and by the Api of its subdomain, which only builds it and hands it to the use cases. Enforced by a new `RepositoryAccessArchitectureTest`. The tests are not checked: they arrange their state through the repositories. |
| Event bus           | The repositories do not register save or load callbacks through the event bus. The bus queues the events and dispatches them later, through handlers returning `Unit`, so it can not hand back a snapshot. The repositories would also dump the aggregates as stored, skipping the `restore` factories and the id checks, and the restore order would be the order they subscribed in. The scene keeps handing `SaveApi` the snapshottables, explicitly and in order. |
| Save                | The JSON of every snapshot is unchanged: `SAVE_VERSION` stays 2, and `SaveFixtureTest` proves it with `v2.json`. |
| Tests               | The checks and the restore move from `<Subdomain>SnapshottableTest` to `Restore<Aggregate>Test` and `Search<Aggregate>SnapshotTest`. `<Subdomain>SnapshottableTest` keeps a JSON round trip and its key. `SnapshotCoverageArchitectureTest` does not change: it still finds a `Snapshottable` per saved subdomain. |
| Delivery            | 4 milestones, each green on CI. See [Milestones](#milestones). |

## Milestones

Each milestone is a few commits straight on `main`, green on CI.

| #  | Milestone                              | Done when                                                                                                                          | Status | Done in |
|----|----------------------------------------|------------------------------------------------------------------------------------------------------------------------------------|--------|---------|
| M1 | `save` locked                          | `save` may only depend on `shared`, and only `screen` imports it.                                                                  | ✅      | `f1fa022` |
| M2 | Repositories called by use cases only  | `RepositoryAccessArchitectureTest` passes, allowing the 6 snapshottables not yet migrated, and `party` takes and restores its snapshot through use cases. | ✅      | `6d347ab` |
| M3 | Every snapshot through use cases       | The other 6 subdomains take and restore their snapshots through use cases, and the allowed snapshottables are gone.                |        |         |
| M4 | Docs                                   | [Package structure](../architecture/package-structure.md) and [Save and load](../architecture/save-and-load.md) describe it.       |        |         |

### M1 commits

1. Add `save` to `SubdomainDependencyArchitectureTest.allowedDependencies`, with its row in
   [Package structure](../architecture/package-structure.md), and add `SaveDependencyArchitectureTest`.

### M2 commits

1. Add `SearchPartySnapshot` and `RestoreParty`, make `PartySnapshottable` take them, and add
   `RepositoryAccessArchitectureTest`, allowing, for now, the snapshottables of `battleUnit`, `battlefield`, `npc`,
   `lootDrop`, `inventory` and `cpuWanderBrain` to call their repository. The list can only shrink.

### M3 commits

1. One commit per subdomain, in the order they are restored: `battleUnit`, `battlefield`, `npc`, `lootDrop`,
   `inventory`, `cpuWanderBrain`, each removing its snapshottable from the allowed ones. The last one removes the list.

### M4 commits

1. State the repository rule in [Package structure](../architecture/package-structure.md), and rewrite the Snapshots
   section of [Save and load](../architecture/save-and-load.md) for the query, the command and the snapshottable, and
   the two locks of `save`.

## Reference

### Use cases

| Subdomain        | Query                                                                   | Command                                                                                      | Checks against                                                          |
|------------------|-------------------------------------------------------------------------|----------------------------------------------------------------------------------------------|-------------------------------------------------------------------------|
| `battleUnit`     | `SearchBattleUnitsSnapshot(): List<BattleUnit.Dto.SnapshotDto>`         | `RestoreBattleUnits(battleUnits: List<BattleUnit.Dto.SnapshotDto>)`                          | `SearchUnitById`, `SearchPlayerById`, `SearchAbilityById`, `SearchEffectById` |
| `battlefield`    | `SearchBattlefieldSnapshot(): List<Battlefield.Dto.OccupantDto>`        | `RestoreBattlefield(occupants: List<Battlefield.Dto.OccupantDto>)`                           | Its own map: the battlefield is created, each tile exists               |
| `npc`            | `SearchNpcsSnapshot(): List<Npc.Dto>`                                   | `RestoreNpcs(npcs: List<Npc.Dto>)`                                                           | `SearchLootableItemById`                                                |
| `lootDrop`       | `SearchLootDropsSnapshot(): List<LootDrop.Dto>`                         | `RestoreLootDrops(droppedItems: Map<String, Map<LootDrop.Dto.PositionDto, List<String>>>)`, by unit id, as the loot tables are read from the resources | Its own loot drops: each unit still drops loot |
| `inventory`      | `SearchInventoriesSnapshot(): List<Inventory.Dto>`                      | `RestoreInventories(inventories: List<Inventory.Dto>)`                                       | `SearchLootableItemById`                                                |
| `party`          | `SearchPartySnapshot(): Party.Dto.SnapshotDto?`                         | `RestoreParty(party: Party.Dto.SnapshotDto?)`, keeping no party when `null`, as today        | `SearchBattleUnitById`                                                  |
| `cpuWanderBrain` | `SearchCpuWanderBrainSnapshot(): CpuWanderBrain.Dto.SnapshotDto?`       | `RestoreCpuWanderBrain(cpuWanderBrain: CpuWanderBrain.Dto.SnapshotDto?)`, keeping none when `null`, as today | -                                                   |

- A command returns `Unit` and a query a value, as `CommandUseCaseArchitectureTest` and `QueryUseCaseArchitectureTest`
  require. A command does not call another command.
- The checks keep their messages, e.g.: `Unknown unit <unitId>`, `Unit <unitId> drops no loot`.

### Snapshottables

- `<Subdomain>Snapshottable(searchSnapshot, restore)` takes only its two use cases, e.g.:
  `PartySnapshottable(searchPartySnapshot: SearchPartySnapshot, restoreParty: RestoreParty)`. `snapshot()` encodes
  what the query returns, and `restore(snapshot)` decodes it and hands it to the command.
- The `@Serializable` mirror, its `key` and the JSON do not change.
- `<Subdomain>Api` builds the two use cases privately, and exposes `val snapshottable: Snapshottable` as today.
  `BattleScene` does not change.

### Architecture tests

- `SubdomainDependencyArchitectureTest.allowedDependencies` gains `"save" to setOf("shared")`.
- `SaveDependencyArchitectureTest`
  - `should only be imported by the screen subdomain when it is the save subdomain`
- `RepositoryAccessArchitectureTest`
  - `should only be imported by the domain, the use cases, the storage and the Api of its subdomain when it is a repository`
  - `should not be called by the Api of its subdomain when it is a repository`, which only builds it and hands it to
    the use cases
  - Its allowed snapshottables, emptied in M3, then removed.

### Docs

- [Package structure](../architecture/package-structure.md): `save` in the restricted subdomains table, only imported
  by `screen`, and the rule that only the use cases call a repository, enforced by `RepositoryAccessArchitectureTest`.
- [Save and load](../architecture/save-and-load.md): each subdomain takes its snapshot through `Search…Snapshot` and
  restores it through `Restore…`, its `Snapshottable` only mapping them to JSON, and the two locks of `save`.

### Tests

For each subdomain, e.g.: `party`:

- `SearchPartySnapshotTest`
  - `should return the members and the line of the party`
  - `should return nothing when there is no party`
- `RestorePartyTest`
  - `should rebuild the same state when it restores the snapshot it took` (moved from `PartySnapshottableTest`)
  - `should fail when a member is not a battle unit` (moved from `PartySnapshottableTest`)
- `PartySnapshottableTest`
  - `Restore should rebuild the same state when it restores the snapshot it took`, through the JSON
  - `Key should be party`
  - `Restore should publish no events when it restores a snapshot` is dropped, as `RestoreParty` takes no `EventBus`.

The other checks move the same way: an occupant on a tile the map no longer has, a lootable item the game no longer
has (stored, or sold by a merchant), a battle unit of a unit the game no longer has, and a unit that no longer drops
loot. `SaveFixtureTest` and `SaveAndLoadEndToEndTest` pass unchanged.

## Known limitations

- A subdomain whose `Dto` is saved (`battlefield`, `npc`, `lootDrop`, `inventory`) restores from its read model: a
  field added to that `Dto` for the presenters also reaches its `Restore…`, until it gets a `SnapshotDto`.
- `RepositoryAccessArchitectureTest` reads the imports and the calls on a `…Repository` name: a repository handed
  around under another name is not caught.
- The scene still lists the snapshottables in order: a new saved subdomain is only caught by
  `SnapshotCoverageArchitectureTest`, not if it is left out of `BattleScene`.
