# Plan: Rename the code of the battlefield subdomain to the words of the glossary

> **Status:** to do.
>
> **Summary:** The code of the battlefield subdomain uses words the [glossary](../../GLOSSARY.md) rejects, and "vacant" says
> something else than it does in the glossary. E.g.: `Tile.isVacant()` is true for an empty void tile, while Teleport can't
> land on it, as its "Vacant tile adjacent to another battle unit" targeting asks `CanBattlefieldTileBeOccupied`. And
> `RequestEffectApplicationToOccupants` says nothing of the lava that burns the Knight. Reading the code and talking about
> the game take a translation. Instead, the classes, functions, events and docs use the glossary's words, a void tile is
> never vacant, and "distance" only means a walking distance. The game plays the same, and players read the same texts.
> Builds on [Game data](../architecture/game-data.md) and [Exploration](../architecture/exploration.md).

## Decisions

| Topic        | Decision                                                                                                                                                                                                |
|--------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope        | The renames below, in the code, tests, comments and docs of what the battlefield subdomain holds, and where other subdomains call it or name their listeners after its events.                          |
| Vacant       | A vacant tile is occupiable and has no occupant, so void is never vacant. Chosen over "no occupant" plus another word, such as Free, as that would also rename the ability targetings and their TOML.   |
| Map          | The word is Battlefield, also for one being designed. Comments of this subdomain that say "map" say "battlefield". The code and player texts of the map builder wait for its section.                   |
| Save format  | Not reached: the save only holds `occupantId`, `row` and `column` under the `battlefield` key, and none of them changes, so `SAVE_VERSION` does not change.                                             |
| Player text  | None changes, and no translation key changes. Players keep reading "An empty tile next to you", as the ability review settled.                                                                          |
| Names kept   | `wangIndex`, a drawing detail. `isInBoundaries`, `ExportBattlefield` and `ParseBattlefield`. `RequestMoveOccupant` and `RequestOccupyTile`, which both occupy a tile but say why.                       |
| Tests        | No new tests: a rename changes no behaviour. The existing tests are renamed with the code.                                                                                                              |
| Out of scope | What the terrain package defines, such as its `canBeOccupied`, and the two open questions. See [Not in this plan](#not-in-this-plan). Devlog entries stay as written.                                   |

## Renames

| Today                                   | Glossary term  | New name                     |
|-----------------------------------------|----------------|------------------------------|
| `BattlefieldTileOccupied`               | Tile           | `TileOccupied`               |
| `OnBattlefieldTileOccupied`             | Tile           | `OnTileOccupied`             |
| `UpdateBattlefieldOccupancy`            | Occupy         | `OccupyTile`                 |
| `Tiles.isDeployed`                      | Occupant       | `Tiles.hasOccupant`          |
| `CanBattlefieldTileBeOccupied`          | Vacant         | `IsTileVacant`               |
| `Battlefield.canBeOccupied`             | Vacant         | `Battlefield.isVacant`       |
| `SearchTilesThatCanBeOccupied`          | Vacant         | `SearchVacantTiles`          |
| `Tiles.isVacant`, `Tile.isVacant`       | Vacant         | `hasNoOccupant`              |
| `TileIsNotVacant`                       | Occupied       | `TileIsOccupied`             |
| `CanBattlefieldTerrainBeOccupied`       | Occupiable     | `IsTileOccupiable`           |
| `canTerrainBeOccupied`                  | Occupiable     | `isOccupiable`               |
| `isTerrainOccupiable`                   | Occupiable     | `isOccupiable`               |
| `TerrainCanBeOccupied`                  | Occupiable     | `Occupiable`                 |
| `RequestEffectApplicationToOccupants`   | Terrain effect | `RequestTerrainEffects`      |
| `OnRequestEffectApplicationToOccupants` | Terrain effect | `OnRequestTerrainEffects`    |
| `effectApplicationsToOccupants`         | Terrain effect | `terrainEffectsOnOccupants`  |
| `EffectApplicationToOccupantDto`        | Terrain effect | `TerrainEffectOnOccupantDto` |

## battlefield

- `Battlefield`, `BattlefieldError`, `BattlefieldEvent`, `BattlefieldApi` and the listeners under `adapters/events` follow the
  names above. `TileIsOccupied` says "Tile is occupied".
- `Battlefield.create` keeps its `terrainCanBeOccupied` map, which is each terrain's own setting, while the value a `Tile`
  holds is `occupiable`, as it also depends on the terrains around it: a sand tile in a corner of void is not occupiable.
- `SearchVacantTiles`: the `distance` parameter is `tilesAround`, as it is a square around the occupant, not a walking
  distance. E.g.: `tilesAround = 3` includes a tile 3 rows and 3 columns away, which is 6 tiles walked.
- `SearchShortestPath` and `SearchWalkingDistance` take `IsTileVacant`, and their comments say "vacant tiles".
- Comments: "map" is "battlefield" in `Battlefield`, `BattlefieldCreation`, `ExportBattlefield`, `ParseBattlefield`,
  `PreviewBattlefield`, `RestoreBattlefield`, `SearchBattlefieldSnapshot` and `TomlBattlefieldDecoder`. E.g.: "to display a
  battlefield being designed". The comment of `TileOccupied` says an occupant is a battle unit, an npc or a building.

## battleUnit

- `BattleUnitApi`, `WhereCanCast`, `WhereCanMove`, `RespawnBattleUnit` and `BattleUnitDeployment` follow the names above.

## building

- `BuildingApi` and `InitialiseBuildings` follow the names above. The private `canTilesBeOccupied` is `areTilesVacant`.

## cpuBattleBrain

- `CpuBattleBrainApi`, `OnTileOccupied`, `WhereShouldWander` and `SearchDistancesFromHome` follow the names above.

## cpuWanderBrain

- `CpuWanderBrainApi`, `OnTileOccupied` and `RespawnDefeatedBattleUnits` follow the names above.

## npc

- `NpcApi` and `InitialiseNpcs` follow the names above. The comment of `RecruitNpc` says the occupant is removed, not that the
  tile is vacated.

## party

- `PartyApi` and `OnTileOccupied` follow the names above. `ApplyStandingTerrainEffects` publishes `RequestTerrainEffects`.

## quest

- `QuestApi` and `InitialiseQuests` follow the names above.

## screen

- `BattlefieldHudApi`, `MovementService` and `BattlefieldPresenter` follow the names above.
- The comments of `BattlefieldPresenter` and `RefreshMovementRange` say the occupant is removed, not that the tile is vacated.

## Storybook

- No story uses these names, and no story id changes.

## Docs

- [Exploration](../architecture/exploration.md), [Package structure](../architecture/package-structure.md) and
  [Presentation timeline](../architecture/presentation-timeline.md): the names above, and "remove" for "vacate". E.g.: "The
  npc leaves its tile (`NpcRecruited`): the battlefield removes it, and the screen removes the npc."
- [CPU battle brain](../architecture/cpu-battle-brain.md): the names above, and "occupiable" for "walkable".
- [Game data](../architecture/game-data.md): `TileIsOccupied`, and "occupiable" for "walkable". "Tiles that can be occupied"
  is "vacant tiles". `BuildingTileCanNotBeOccupied` and the other errors of its kind keep their names.
- [Save and load](../architecture/save-and-load.md): "the battlefield and its buildings" for "the map and its buildings".
- `docs/architecture/dependencies.html` is regenerated by the pre-commit hook.

## Tests

- The tests that use these names are renamed with the code, with no change in what they assert, including `BattlefieldTest`,
  `OccupyTilesTest`, `MovementServiceTest`, `ProcessTileSelectedTest`, `RefreshMovementRangeTest`, `SelectBattleUnitTest`,
  `ApplyStandingTerrainEffectsTest`, `ExplorationPresenterTest`, `InMemoryQueries` and the acceptance tests.
- Test classes: `OnBattlefieldTileOccupiedTest` is `OnTileOccupiedTest`, in `party`, `cpuBattleBrain` and `cpuWanderBrain`.
  `UpdateBattlefieldOccupancyTest` is `OccupyTileTest`, `CanBattlefieldTileBeOccupiedTest` is `IsTileVacantTest`,
  `CanBattlefieldTerrainBeOccupiedTest` is `IsTileOccupiableTest`, `SearchTilesThatCanBeOccupiedTest` is
  `SearchVacantTilesTest` and `OnRequestEffectApplicationToOccupantsTest` is `OnRequestTerrainEffectsTest`.

## Not in this plan

- `Battlefield.occupy` and `occupyTiles` only check that a tile has no occupant, not that it is occupiable, so
  `RequestOccupyTile` could put a battle unit on void, an occupied tile that isn't occupiable. Their callers check first
  today. It looks like a bug, still to be decided.
- `RequestBattlefieldCreation` is only called by tests. The game creates the battlefield with `InitialiseBattlefield`. Whether
  it should stay is still to be decided.

### Waiting for other sections

- `terrain`: Terrain and Void are still to be defined, and a terrain's own `canBeOccupied`, `allowedTransitionTo` and `effectId`,
  with `terrainCanBeOccupied` in `Battlefield.create`.
- `mapBuilder`: "Map" is to be avoided, while `MapBuilder`, `CreateMap`, `ResizeMap`, `ImportMap`, `ExportMap`, `SearchMap` and
  players' "Map Builder" and "Build a map" use it. `canBeNeighbours` and `neighbourTerrainIds`, while "neighbour" is to be
  avoided. `ParseBattlefield`, while players read "import".
- `battleUnit`: Walk, Step, Move and Deployed are still to be defined. `DistanceService.manhattanDistance`, only a programmer's
  word, for what `WhereCanCast` uses to tell adjacent tiles. `BattlefieldTileCanNotBeOccupied`, while "can be occupied" is to be
  avoided.
- `cpuBattleBrain`: `manhattanDistance` in `WhereShouldMove`, while the glossary only has a walking distance.
- `building`: `BuildingTileCanNotBeOccupied`, while "can be occupied" is to be avoided.
- `effect`: Trigger and Apply, as the terrain effect is "triggered" and carries an `EffectApplicationDto`.
- `npc`: `SearchNpcNextTo`, while "next to" is to be avoided. `NpcTileCanNotBeOccupied`, while "can be occupied" is to be avoided.
- `quest`: `QuestTileCanNotBeOccupied`, while "can be occupied" is to be avoided.
- `screen`: `battlefieldGrid` in `BattlefieldView`. `UiText` and `ui.mapBuilder.forbiddenNeighbours`, while "neighbour" is to be
  avoided.
