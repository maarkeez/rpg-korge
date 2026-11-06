# Plan: Rename the code of the building subdomain to the words of the glossary

> **Status:** done.
>
> **Summary:** The code of the building subdomain says the buildings are initialised, while the
> [glossary](../../GLOSSARY.md) says they are placed. E.g.: setting up a game publishes `RequestInitialiseBuildings`, and
> `InitialiseBuildings` then publishes `BuildingPlaced` for the fortress. And when a tile of the fortress is void, the game
> fails with `BuildingTileCanNotBeOccupied`, while the glossary avoids "can be occupied" and the check asks whether the tile
> is vacant. Reading the code and talking about the game take a translation. Instead, the use case, its request, its
> listener, the error and the docs use the glossary's words. The game plays the same, and players read the same texts.
> Builds on [Game data](../architecture/game-data.md) and the [battlefield renames](battlefield-renames.md).

## Decisions

| Topic        | Decision                                                                                                                                                                                                       |
|--------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope        | The renames below, in the code, tests, comments and docs of the building subdomain, and where other subdomains publish its request or call its Api.                                                            |
| Place        | Placing every building is `PlaceBuildings`, as the glossary avoids "initialise" for it. Chosen over keeping `InitialiseBuildings` like `InitialiseNpcs` and `InitialiseQuests`, which wait for their sections. |
| Error        | `BuildingTileNotVacant`, as `PlaceBuildings` asks `IsTileVacant`: void, out of the battlefield or occupied. Chosen over `BuildingTileNotOccupiable`, which says less than the check does.                      |
| Words kept   | The private `Area` of `Building`, as the glossary needs no word for the tiles of a building. `BuildingSizeNotPositive`, `BuildingPlaced` and `Building.place` already fit.                                    |
| Save format  | Not reached: the buildings are read from the resources again, and never saved, so `SAVE_VERSION` does not change.                                                                                              |
| Player text  | None changes, and no translation key or catalog changes: players read no word of this subdomain.                                                                                                              |
| Tests        | No new tests: a rename changes no behaviour. The existing tests are renamed with the code.                                                                                                                     |
| Out of scope | `searchAll`, and the words of the npc, quest, lootDrop and screen subdomains, which wait for their sections. See [Not in this plan](#not-in-this-plan).                                                        |

## Renames

| Today                                      | Glossary term | New name                              |
|--------------------------------------------|---------------|---------------------------------------|
| `InitialiseBuildings`                      | Place         | `PlaceBuildings`                      |
| `BuildingApi.initialiseBuildings`          | Place         | `BuildingApi.placeBuildings`          |
| `BuildingEvent.RequestInitialiseBuildings` | Place         | `BuildingEvent.RequestPlaceBuildings` |
| `OnRequestInitialiseBuildings`             | Place         | `OnRequestPlaceBuildings`             |
| `BuildingApi.onRequestInitialiseBuildings` | Place         | `BuildingApi.onRequestPlaceBuildings` |
| `BuildingTileCanNotBeOccupied`             | Vacant        | `BuildingTileNotVacant`               |
| `InitialiseBuildingsTest`                  | Place         | `PlaceBuildingsTest`                  |
| `OnRequestInitialiseBuildingsTest`         | Place         | `OnRequestPlaceBuildingsTest`         |

## building

### Domain

- `BuildingError.BuildingTileNotVacant` says "A tile of the building is not vacant".
- `BuildingEvent.RequestPlaceBuildings` is the request to place every building.

### Use cases

- `PlaceBuildings` places every loaded building on its tiles, failing with `BuildingTileNotVacant` when any of them is not
  vacant. Its comment says "not vacant (e.g.: void, out of the battlefield)". It moves to `usecases/commands/PlaceBuildings.kt`.

### Adapters

- `OnRequestPlaceBuildings` subscribes to `RequestPlaceBuildings` and calls `placeBuildings`. It moves to
  `adapters/events/OnRequestPlaceBuildings.kt`.
- `BuildingApi` builds `placeBuildings` and `onRequestPlaceBuildings`.

## Other callers

- `SetUpGame` publishes `BuildingEvent.RequestPlaceBuildings`, and `SetUpGameTest` expects it, in both its tests.
- `SubdomainApis` and `BuildingAcceptanceTest` call `buildingApi.placeBuildings()`.

## Docs

- [Game data](../architecture/game-data.md): row 10 of the setup table is `RequestPlaceBuildings`. The building's failures
  say "when any of its tiles is not vacant (`BuildingTileNotVacant`), e.g.: void or out of the battlefield, or is taken by
  another building (`TileIsOccupied`)".
- `docs/architecture/dependencies.html` is regenerated by the pre-commit hook.

## Tests

- `PlaceBuildingsTest`, with its helper `placeBuildings`
    - `should place every loaded building on its tiles, requesting the battlefield to occupy them, when the buildings are
      placed` (replaces `... when the buildings are initialised`)
    - `should not place a building again when it is already placed`
    - `should fail when any tile of the building is void`, expecting `BuildingTileNotVacant`
    - `should fail when any tile of the building is out of the battlefield`, expecting `BuildingTileNotVacant`
- `OnRequestPlaceBuildingsTest`
    - `should place the buildings when the request is dispatched` (replaces `should initialise the buildings when the
      request is dispatched`)

## Not in this plan

- `BuildingRepository.searchAll` is only called by `InMemoryBuildingRepositoryTest`. Whether it should stay is still to be
  decided.

### Waiting for other sections

- `npc`: whether `Npc.place` and `NpcPlaced` are the same Place as a building's. `InitialiseNpcs` and `RequestInitialiseNpcs`,
  while "initialise" is avoided for placing. `NpcTileCanNotBeOccupied` may follow `BuildingTileNotVacant`, as
  `InitialiseNpcs` also asks `IsTileVacant`.
- `quest`: `InitialiseQuests` and `RequestInitialiseQuests`, which no longer follow the buildings' pattern.
  `QuestTileCanNotBeOccupied` may follow `BuildingTileNotVacant`, as `InitialiseQuests` also asks `IsTileVacant`.
- `lootDrop`: whether `PlaceDroppedItem` and `LootDrop.place` are the same Place as a building's.
- `screen`: the piece of a building's image drawn on each of its tiles, in `BattlefieldView.displayBuilding` and
  [Assets](../architecture/assets.md), is still to be defined.

## Changes while building

- [Game data](../architecture/game-data.md) also says "Each `Initialise<Subdomain>` command, and `PlaceBuildings`, creates all
  its data synchronously", as placing the buildings no longer follows the `Initialise<Subdomain>` pattern.
