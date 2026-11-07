# Plan: Rename the code of the npc subdomain to the words of the glossary

> **Status:** to do.
>
> **Summary:** The code of the npc subdomain says the npcs are initialised, while the [glossary](../../GLOSSARY.md) says
> they are placed, as the buildings are. E.g.: setting up a game publishes `RequestInitialiseNpcs`, and `InitialiseNpcs`
> then publishes `NpcPlaced` for the elder. When the tile of the elder is void, the game fails with
> `NpcTileCanNotBeOccupied`, while the glossary avoids "can be occupied" and the check asks whether the tile is vacant.
> And pressing A asks `SearchNpcNextTo`, while the glossary says adjacent, as `TalkToAdjacentNpc` already does. Reading the
> code and talking about the game take a translation. Instead, the use cases, the request, the listener, the error and
> the docs use the glossary's words. The game plays the same, and players read the same texts. Builds on
> [Game data](../architecture/game-data.md), [Exploration](../architecture/exploration.md), the
> [building renames](building-renames.md) and the [battlefield renames](battlefield-renames.md).

## Decisions

| Topic        | Decision                                                                                                                                                                                 |
|--------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope        | The renames below, in the code, tests, comments and docs of the npc subdomain, and where other subdomains publish its request or call its Api.                                          |
| Place        | Placing every npc is `PlaceNpcs`, the same Place as a building's, as `Npc.place` and `NpcPlaced` already say. Chosen over keeping `InitialiseNpcs`, as the glossary avoids "initialise". |
| Error        | `NpcTileNotVacant`, as `PlaceNpcs` asks `IsTileVacant`, following `BuildingTileNotVacant`.                                                                                              |
| Adjacent     | The npc next to a tile is `SearchAdjacentNpc`, as the glossary avoids "next to", and `TalkToAdjacentNpc` already says adjacent.                                                         |
| Words kept   | `TalkToAdjacentNpc`, `RecruitNpc`, `NpcRecruited`, `isRecruitable`, `isMerchant`, `SearchIsMerchant`, `NpcPlaced`, `NpcNotRecruitable` and the `NpcStock*` errors already fit.            |
| Save format  | Not reached: no field of `NpcSnapshot` changes, so `SAVE_VERSION` does not change.                                                                                                       |
| Player text  | None changes, and no translation key or catalog changes: players read the names and lines of the npcs, which keep their `npc.<npc id>.name` and `npc.<npc id>.line` keys.                |
| Tests        | No new tests: a rename changes no behaviour. The existing tests are renamed with the code.                                                                                               |
| Out of scope | The words of the party, screen, quest and unit subdomains, which wait for their sections. See [Not in this plan](#not-in-this-plan).                                                     |

## Renames

| Today                             | Glossary term | New name                            |
|-----------------------------------|---------------|-------------------------------------|
| `InitialiseNpcs`                  | Place         | `PlaceNpcs`                         |
| `NpcApi.initialiseNpcs`           | Place         | `NpcApi.placeNpcs`                  |
| `NpcEvent.RequestInitialiseNpcs`  | Place         | `NpcEvent.RequestPlaceNpcs`         |
| `OnRequestInitialiseNpcs`         | Place         | `OnRequestPlaceNpcs`                |
| `NpcApi.onRequestInitialiseNpcs`  | Place         | `NpcApi.onRequestPlaceNpcs`         |
| `NpcTileCanNotBeOccupied`         | Vacant        | `NpcTileNotVacant`                  |
| `SearchNpcNextTo`                 | Adjacent      | `SearchAdjacentNpc`                 |
| `NpcApi.searchNpcNextTo`          | Adjacent      | `NpcApi.searchAdjacentNpc`          |
| `InMemoryQueries.searchNpcNextTo` | Adjacent      | `InMemoryQueries.searchAdjacentNpc` |
| `InitialiseNpcsTest`              | Place         | `PlaceNpcsTest`                     |
| `OnRequestInitialiseNpcsTest`     | Place         | `OnRequestPlaceNpcsTest`            |
| `SearchNpcNextToTest`             | Adjacent      | `SearchAdjacentNpcTest`             |

## npc

### Domain

- `NpcError.NpcTileNotVacant` says "The npc tile is not vacant".
- `NpcEvent.RequestPlaceNpcs` is the request to place every npc.

### Use cases

- `PlaceNpcs` places every loaded npc on its tile, failing with `NpcTileNotVacant` when the tile is not vacant (e.g.:
  void, out of the battlefield), or with `NpcStockItemNotFound` when an item sold by a merchant does not exist. It moves to
  `usecases/commands/PlaceNpcs.kt`.
- `SearchAdjacentNpc` returns the first npc adjacent to the tile, looking north, east, south and west, in that order. Its
  comments say "adjacent" for "next to". It moves to `usecases/queries/SearchAdjacentNpc.kt`.
- `TalkToAdjacentNpc` asks `SearchAdjacentNpc`, and its comment says "the first npc adjacent to its leader" and "no npc
  adjacent to the leader".

### Adapters

- `OnRequestPlaceNpcs` subscribes to `RequestPlaceNpcs` and calls `placeNpcs`. It moves to
  `adapters/events/OnRequestPlaceNpcs.kt`.
- `NpcApi` builds `placeNpcs`, `onRequestPlaceNpcs` and `searchAdjacentNpc`.

## Other callers

- `SetUpGame` publishes `NpcEvent.RequestPlaceNpcs`, and `SetUpGameTest` expects it.
- `SubdomainApis`, `NpcAcceptanceTest` and `TradeAcceptanceTest` call `npcApi.placeNpcs()`.
- `InMemoryQueries` builds `searchAdjacentNpc`, which `TalkToAdjacentNpcTest` passes to `TalkToAdjacentNpc`.

## Docs

- [Game data](../architecture/game-data.md): row 13 of the setup table is `RequestPlaceNpcs`. The sentence on
  `Initialise<Subdomain>` commands names `PlaceNpcs` beside `PlaceBuildings`.
- [Exploration](../architecture/exploration.md): `TalkToAdjacentNpc` asks `SearchAdjacentNpc`. "An npc next to the leader"
  and "when it stands next to the leader" say adjacent, in the npc parts only.
- `docs/architecture/dependencies.html` is regenerated by the pre-commit hook.

## Tests

- `PlaceNpcsTest`, with its helper `placeNpcs`
    - `should place every loaded npc on its tile, requesting the battlefield to occupy it, when the npcs are placed`
      (replaces `... when the npcs are initialised`)
    - `should fail when the tile of the npc is void`, expecting `NpcTileNotVacant`
    - `should fail when the tile of the npc is out of the battlefield`, expecting `NpcTileNotVacant`
- `OnRequestPlaceNpcsTest`
    - `should place the npcs when the request is dispatched` (replaces `should initialise the npcs when the request is
      dispatched`)
- `SearchAdjacentNpcTest`
    - `should return the npc standing adjacent to the tile` (replaces `should return the npc standing next to the tile`)
    - `should return the first npc adjacent to the tile, north, east, south and west, in that order` (replaces `should
      return the first npc next to the tile, ...`)
- `TalkToAdjacentNpcTest`
    - `should request to talk to the npc adjacent to the leader` (replaces `... the npc next to the leader`)
    - `should request to talk to the first npc adjacent to the leader, north, east, south and west, in that order`
      (replaces `... the first npc next to the leader, ...`)

## Not in this plan

### Waiting for other sections

- `party`: Talk is still to be defined, with the talking mode: `StartTalk`, `FinishTalk`, `RequestStartTalk`,
  `NpcTalkStarted`, `NpcTalkFinished`, `TALKING` and `talkingNpcId`. Whether `RecruitMember` and `RequestRecruitMember`
  are the same Recruit as an npc's. The `PartyEvent` comment on `RequestStartTalk` says "next to the leader".
- `screen`: `NpcDialog`, `NpcDialogView` and the keys `ui.npcDialog.buy`, `ui.npcDialog.sell` and `ui.npcDialog.leave`,
  while "dialog" is avoided for a line. The shop (`ShopPresenter`, `ShopView`, `ui.shop.*`) and the npc portrait are still
  to be defined.
- `quest`: the quest giver (`giverNpcId`) and `TALK_TO_NPC`.
- `unit`: the unit a recruitable npc is recruited as.
