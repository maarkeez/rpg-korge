# Plan: Buildings, starting with a fortress

> **Status:** done.
>
> **Summary:** Buildings stand on the battlefield, as npcs do, but over several tiles. The first one is a fortress of 4x4
> tiles in the village. It is an obstacle: nobody can stand on its tiles nor walk through them, and nothing else
> happens. Entering it, its gate and any interaction are left for later.

## Decisions

| Topic         | Decision                                                                                                                                                                                                                         |
|---------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope         | A building only blocks its tiles. No gate, no interaction, no name displayed.                                                                                                                                                    |
| Domain        | A new `building` subdomain, shaped like `npc`. It is restricted: it may only depend on `shared` and `battlefield`.                                                                                                                |
| Game data     | One file per building, `building/<building id>.toml`, as for npcs: **the file name is the building id**. It holds `row` and `column`, its top-left tile, and `rows` and `columns`, its size in tiles. No `name` until something displays it. |
| Assets        | `building/<building id>.png`, of `columns` x 16 by `rows` x 16 pixels. The size is declared in the TOML, so the game logic does not depend on the images: `BuildingImageResourcesTest` checks the image matches it.              |
| Occupancy     | Battlefield occupants can cover several tiles. The building records `BattlefieldEvent.RequestOccupyTiles(occupantId, row, column, rows, columns)`, and battlefield checks every tile is vacant before occupying them all, publishing one `BattlefieldTileOccupied` per tile, row by row. |
| Checks        | The game fails to start when a tile of a building is out of the battlefield or can not be occupied (`BuildingTileCanNotBeOccupied`), or is taken (`TileIsNotVacant`).                                                            |
| Order         | Buildings are initialised after the battlefield and before the battle units and the npcs, so a battle unit or an npc on a tile of a building fails to occupy it (`TileIsNotVacant`).                                              |
| Drawing       | `BuildingPlaced(buildingId, row, column)` is displayed by `BattlefieldPresenter`. `BattlefieldView.displayBuilding` cuts the image into its tiles, each one drawn on the `BUILDING` layer of its tile, right above the terrain: below the loot, the occupants and the selection. A building without an image is not displayed. |
| The fortress  | Id `fortress`, top-left tile `(17,13)`, 4 rows and 4 columns: rows 17 to 20, columns 13 to 16, in the village. Its gate faces the open ground at row 21.                                                                         |
| Palette       | The attached image, with each off-palette colour replaced as below: the blue-grey stone becomes a neutral grey ramp.                                                                                                             |
| Map builder   | Out of scope: it ignores buildings, which are added by hand.                                                                                                                                                                     |

### Palette

| Original          | FamiCube          | Where                   |
|-------------------|-------------------|-------------------------|
| `(192, 203, 220)` | `(215, 215, 215)` | Stone, light            |
| `(139, 155, 180)` | `(168, 168, 168)` | Stone                   |
| `( 82,  96, 124)` | `(123, 123, 123)` | Stone, shade            |
| `( 63,  38,  49)` | `( 52,  52,  52)` | Outlines                |
| `( 38,  43,  68)` | `( 21,  21,  21)` | The open gate           |
| `(118,  59,  54)` | `(130,  60,  61)` | Courtyard               |
| `(189, 108,  74)` | `(174, 108,  55)` | Trim and gate doors     |

## Map

`F` the fortress. Terrain: `.` sand, `#` void, `~` water, `g` grass; `N` an npc.

```text
     012345678901234567890123
 15  ##....................##
 16  ##..g...N......N...g..##
 17  ##.........~~FFFF.....##   Fortress: rows 17 to 20, columns 13 to 16
 18  ##.........~~FFFF.....##
 19  ##...........FFFF.....##
 20  ##..g........FFFF..g..##
 21  ##...N............N...##   Its gate faces row 21
```

## building

### Domain

- Add `Building`, with `place(dto)` recording `BuildingPlaced(buildingId, row, column)`, then
  `BattlefieldEvent.RequestOccupyTiles(occupantId, row, column, rows, columns)`, published together as `Npc` does.
  Its `Dto` holds `id`, `row`, `column`, `rows` and `columns`.
- `rows` and `columns` must be at least 1 (`BuildingSizeNotPositive`).
- Add `BuildingError.BuildingTileCanNotBeOccupied`, `BuildingEvent.RequestInitialiseBuildings` and `BuildingRepository`.

### Use cases

- Add `BuildingLoader`, as `NpcLoader`.
- Add `InitialiseBuildings`: places every loaded building, unless it is already placed, failing with
  `BuildingTileCanNotBeOccupied` when any of its tiles can not be occupied (`CanBattlefieldTileBeOccupied`).

### Adapters

- Add `ResourcesBuildingLoader`, reading `building/*.toml`, `InMemoryBuildingRepository`,
  `OnRequestInitialiseBuildings` and `BuildingApi`.

### Resources

- Add `building/fortress.toml` and `building/fortress.png`.

## battlefield

- Add `BattlefieldEvent.RequestOccupyTiles`.
- Add `Battlefield.occupyTiles(row, column, rows, columns, occupantId)`: fails with `TileNotFound` or `TileIsNotVacant`
  when any tile is, before occupying any. `position(occupantId)` is then its top-left tile.
- Add `OccupyTiles` and `OnRequestOccupyTiles`, as `UpdateBattlefieldOccupancy` and `OnRequestOccupyTile`.

## battlesetup

- `SetupBattle` publishes `BuildingEvent.RequestInitialiseBuildings` right after `RequestInitialiseBattlefield`.

## screen

- `BattlefieldView.displayBuilding(row, column, buildingId)` adds a `BUILDING` layer to each tile of the building. The
  layer is kept with the tile when it leaves the screen.
- `BattlefieldPresenter` displays `BuildingPlaced` on the timeline, as `NpcPlaced`.
- `BattleScene` creates the `BuildingApi` and initialises its resources.

## Docs

- `assets.md`: the building image row.
- `game-data.md`: the initialisation order, `building/<building id>.toml`, and how to add a building.
- `package-structure.md`: `building` in the restricted subdomains, and its requests to `battlefield`.

## Tests

- `BuildingTest`, `InitialiseBuildingsTest`, `ResourcesBuildingLoaderTest`, `OnRequestInitialiseBuildingsTest`,
  `InMemoryBuildingRepositoryTest`.
- `BuildingImageResourcesTest`: every building has its image, of its size.
- `BattlefieldTest`, `OccupyTilesTest`, `OnRequestOccupyTilesTest`: every tile is occupied, one event per tile, and
  nothing is occupied when one tile is taken or out of the battlefield.
- `SetupBattleTest`: the new request, after the battlefield.
- `BattlefieldViewTest` and `BattlefieldPresenterTest`: the building is drawn on each of its tiles.
- `SubdomainDependencyArchitectureTest`: `building` may only depend on `shared` and `battlefield`.

## Known limitations

- A building can not be moved, removed nor swapped: nothing requests it, so battlefield does not support it for
  occupants over several tiles.
- Building ids share the occupant ids with the battle units and the npcs, which are not checked against each other.
