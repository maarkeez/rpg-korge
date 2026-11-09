# Plan: Rename the code of the terrain subdomain to the words of the glossary

> **Status:** done, built on `main` in `d52a6d56`.
>
> **Summary:** The code of the terrain subdomain says a terrain "allows" a transition to another, while the
> [glossary](../../GLOSSARY.md) says a terrain transition is the border drawn where two terrains meet, which a terrain
> either has or not. E.g.: `sand_to_void.png` gives sand `allowedTransitionTo = {"sand", "void"}`, and the world builder
> asks `isTransitionAllowed("sand", "void")` before painting void next to sand. The test helpers also call a terrain
> "occupiable" (`TerrainMother.occupiableTerrain`), while the glossary gives Occupiable to a tile, which also depends on
> the terrains around it, and says a terrain can be occupied. Instead, a terrain has a transition to another
> (`transitionsTo`, `hasTransitionTo`, `HasTerrainTransition`), and the test helpers say a terrain can be occupied. The
> game plays the same, and players read the same text. Builds on [Game data](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/game-data.md) and the
> [battlefield](battlefield-renames.md), [cpuBattleBrain](cpuBattleBrain-renames.md) and
> [mapBuilder](mapBuilder-renames.md) renames, which left these words to this section.

## Decisions

| Topic            | Decision                                                                                                                                                                                                                                                                                                                                 |
|------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope            | The renames below, in the code and tests of `terrain`, and of `battlefield` and `worldBuilder` where they use these names.                                                                                                                                                                                                               |
| Save format      | Not reached: terrains are game data, never saved. `SAVE_VERSION` stays.                                                                                                                                                                                                                                                                  |
| Player text      | No change. No translation key changes, in `messages.pot` nor `es.po`.                                                                                                                                                                                                                                                                    |
| Has a transition | A terrain has a terrain transition to another, as the battlefield section defines Terrain transition as the border drawn. Chosen over keeping "allowed transition", as nothing allows it: the strip exists or not.                                                                                                                       |
| Can be occupied  | A terrain's own setting stays `canBeOccupied`, in the TOML files and `Terrain.Dto`, and the battlefield's `terrainCanBeOccupied` stays. Chosen over Void (`void = true`) and Walkable, as an npc or a building stands on a terrain without walking, and Void stays one terrain among the others.                                         |
| Test helpers     | `TerrainMother.occupiableTerrain` and `nonOccupiableTerrain` say "can be occupied", as Occupiable is said of a tile. Chosen over keeping them as test-only names.                                                                                                                                                                        |
| Words kept       | `effectId` (the terrain effect), `InitialiseTerrains`, `TerrainCreated`, `SearchTerrainHealthEffect`, `terrainHealthContribution`, the battlefield's `TileTerrainTransitionNotAllowed`, the "transition asset" of the loader tests, and **Screen / Terrain tile**, its `transition` and `wangIndex` args and its **Sand to void** story. |
| Tests            | No new tests: a rename changes no behaviour. The existing tests are renamed with the code.                                                                                                                                                                                                                                               |
| Out of scope     | The open questions. See [Not in this plan](#not-in-this-plan). Devlog entries and done plans stay as written.                                                                                                                                                                                                                            |

## Renames

| Today                                                                                                | Glossary term      | New name                                                                   |
|------------------------------------------------------------------------------------------------------|--------------------|----------------------------------------------------------------------------|
| `Terrain.Dto.allowedTransitionTo`, the private `AllowedTransitionTo`                                 | Terrain transition | `Terrain.Dto.transitionsTo`, `TransitionsTo`                               |
| `Terrain.canTransitionTo`                                                                            | Terrain transition | `Terrain.hasTransitionTo`                                                  |
| `IsTransitionAllowed`, its `transitionToTerrainId` parameter                                         | Terrain transition | `HasTerrainTransition`, `toTerrainId`                                      |
| `TerrainApi.isTransitionAllowed`, `InMemoryQueries.isTransitionAllowed`                              | Terrain transition | `TerrainApi.hasTerrainTransition`, `InMemoryQueries.hasTerrainTransition`  |
| `isTransitionAllowed` (parameter of `PaintTile` and `ImportWorld`)                                   | Terrain transition | `hasTerrainTransition`                                                     |
| `ResourcesTerrainLoader.computeAllowedTransitions`, the local `transitionToTerrainId`                | Terrain transition | `transitionsTo`, `toTerrainId`                                             |
| `TerrainMother.occupiableTerrain`, `TerrainMother.nonOccupiableTerrain`, their `allowedTransitionTo` | Can be occupied    | `terrainThatCanBeOccupied`, `terrainThatCanNotBeOccupied`, `transitionsTo` |
| `IsTransitionAllowedTest`                                                                            | Terrain transition | `HasTerrainTransitionTest`                                                 |

## terrain

### Domain

- `Terrain.Dto.transitionsTo` holds the terrains it has a terrain transition to, e.g.: `{"sand", "grass", "lava", "void",
  "water"}` for sand, and `Terrain.hasTransitionTo("void")` tells whether sand has one.

### Use cases

- `HasTerrainTransition(terrainId, toTerrainId)` tells whether the terrain has a transition to the other, and is `false`
  when the terrain does not exist. It moves to `usecases/queries/HasTerrainTransition.kt`.
- `SearchTerrains`'s comment says "e.g.: to select the terrain to paint with in the world builder", as the glossary
  avoids "map" and "designing".

### Adapters

- `ResourcesTerrainLoader` gives each terrain its `transitionsTo` from the strips in `terrain/transitions/`, with
  `transitionsTo(terrainId)` and `toTerrainId` for the second terrain of a strip's name.
- `TerrainApi` builds `hasTerrainTransition`.

## Other callers

- `BattlefieldCreation` reads `transitionsTo` for its `TerrainTransitionRuleDto`s.
- `WorldBuilderApi` passes `terrainApi.hasTerrainTransition` to `PaintTile` and `ImportWorld`, which ask it in both
  directions as `canBeAdjacent`.
- `InMemoryQueries` builds `hasTerrainTransition`, and `PaintTileTest` and `ImportWorldTest` pass
  `HasTerrainTransition(terrainRepository)`.
- The 31 test files calling `TerrainMother` say `terrainThatCanBeOccupied`, `terrainThatCanNotBeOccupied` and
  `transitionsTo`, e.g.: `SaveFixtureTest`, `PreviewBattlefieldTest`, `RequestBattlefieldCreationTest`,
  `WhereShouldWanderTest` and `WorldBuilderPresenterTest`. `TerrainEffectAcceptanceTest`, `InitialiseTerrainsTest` and
  `ResourcesTerrainLoaderTest` build `Terrain.Dto` with `transitionsTo`.

## Docs

- `docs/architecture/dependencies.html` is regenerated by the pre-commit hook.

## Tests

- `TerrainTest`
    - `HasTransitionTo should be true when the terrain has the transition` (replaces `CanTransitionTo should be true when
      the terrain allows the transition`)
    - `HasTransitionTo should be false when the terrain does not have the transition` (replaces `CanTransitionTo should
      be false when the terrain does not allow the transition`)
    - `HasTransitionTo should be false when the terrain has no transitions` (replaces `CanTransitionTo should be false
      when the terrain has no allowed transitions`)
- `HasTerrainTransitionTest`
    - `should be true when the terrain has the transition` (replaces `should be true when the terrain allows the
      transition`)
    - `should be false when the terrain does not have the transition` (replaces `should be false when the terrain does
      not allow the transition`)
- `ResourcesTerrainLoaderTest`
    - `LoadTerrains should give a transition only to the target terrain when a transition asset exists` (replaces
      `LoadTerrains should allow the transition only to the target terrain when a transition asset exists`)
    - `LoadTerrains should give a terrain a transition to itself when it is loaded` (replaces `LoadTerrains should allow
      the transition to itself when a terrain is loaded`)
- `TerrainTransitionsAcceptanceTest`
    - `should have only the sand to void transition when the game is set up` (replaces `should allow only the sand to
      void transition when the game is set up`)

## Not in this plan

- `transitionsTo` holds the terrain itself, e.g.: sand has a transition to sand, though no strip is drawn from a terrain
  to itself, and every caller already skips two equal terrains (`BattlefieldCreation` filters them out, `WorldBuilder`
  compares them first). Whether `ResourcesTerrainLoader` should stop adding it, with its test `LoadTerrains should give a
  terrain a transition to itself when it is loaded` and the default resources test asserting it.
- [Game data](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/game-data.md) documents no `terrain/<terrain id>.toml` (`canBeOccupied`, `effectId`), nor
  how the strips in `terrain/transitions/` give the transitions, beyond the battlefield's note on sand.

### Waiting for other sections

None: `unit` has nothing on terrain.
