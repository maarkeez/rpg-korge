# Plan: Rename the map builder to the world builder, in the code and in what players read

> **Status:** built on `main` in `cd254b3b`, with its screenshots still to be approved.
>
> **Summary:** The [glossary](../../GLOSSARY.md) avoids "map", while the map builder says it everywhere: `MapBuilder`,
> `CreateMap`, `ImportMap`, `MapImportRejected`, and players read "Build a map", "Map Builder" and "Map copied to the
> clipboard". It only builds a battlefield today, but it is to build more of what a game is played in, so neither
> "battlefield" nor "game", which is one whole play that is saved and loaded, fits. And `canBeNeighbours` says
> "neighbour", where the glossary says adjacent. E.g.: importing a file of 200 × 16 tiles publishes `MapImportRejected`
> with `SIZE_OUT_OF_RANGE`, and players read "The map must be 1 to 128 tiles wide and tall". Instead, the code, its
> tests, stories and docs say world and world builder, players read "world", and the code says adjacent. Painting lava
> next to water still reads "Lava can not be next to Water". Builds on the [battlefield renames](battlefield-renames.md)
> and [Localization](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/localization.md).

## Decisions

| Topic        | Decision                                                                                                                                                                                                                                                                                                                                                      |
|--------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope        | The renames below, in the `mapBuilder` package, and everything named after it in `screen`, `storybook`, `shared` tests, the translation catalogs and the docs.                                                                                                                                                                                                |
| World        | What is built is a world, and the tool the world builder. Chosen over Battlefield builder, as a world is to hold more than a battlefield, over Game builder, as a game is one whole play, and over Scenario.                                                                                                                                                  |
| Size         | The size is the world's: `ResizeWorld` and `WorldResized`. Chosen over `ResizeBattlefield`, as players read "The world must be 1 to 128 tiles wide and tall".                                                                                                                                                                                                 |
| Adjacent     | The code says adjacent, as the glossary does. Players keep reading "{terrain} can not be next to {neighbour}", as they keep "An empty tile next to you": only its key changes.                                                                                                                                                                                |
| Package name | `worldBuilder`, camelCase like `gameSetup`. The glossary's `### World builder` heading already matches it.                                                                                                                                                                                                                                                    |
| Names kept   | `ParseBattlefield` and `ExportBattlefield` in the battlefield package, as the battlefield plan kept them: import is the player's action on a world, parsing how the battlefield reads its text. `PaintTile`, `SelectTerrain`, `TilePainted`, `TilePaintRejected`, `TerrainSelected`, `MINIMUM_SIZE`, `MAXIMUM_SIZE`, `DEFAULT_SIZE` and `DEFAULT_TERRAIN_ID`. |
| Save format  | Not reached: the world builder only keeps its world in memory, in `InMemoryWorldBuilderRepository`, and no save adapter or fixture under `src/jvmTest/fixtures/save` names it, so `SAVE_VERSION` does not change.                                                                                                                                             |
| Player text  | Every "map" players read says "world", in English and Spanish, and the keys of the world builder say `worldBuilder`. See [Player texts](#player-texts).                                                                                                                                                                                                       |
| Tests        | No new tests: a rename changes no behaviour. The existing tests are renamed with the code, and the ones that read the texts expect the new ones.                                                                                                                                                                                                              |
| Out of scope | `BattlefieldView`'s own "map" and "neighbour" words, which wait for the screen section. See [Not in this plan](#not-in-this-plan). Devlog entries stay as written.                                                                                                                                                                                            |

## Renames

| Today                                                                                                   | Glossary term | New name                                                                                                            |
|---------------------------------------------------------------------------------------------------------|---------------|---------------------------------------------------------------------------------------------------------------------|
| package `mapBuilder`                                                                                    | World builder | `worldBuilder`                                                                                                      |
| `MapBuilder`, `MapBuilderTest`                                                                          | World builder | `WorldBuilder`, `WorldBuilderTest`                                                                                  |
| `MapBuilderApi`, `mapBuilderApi`                                                                        | World builder | `WorldBuilderApi`, `worldBuilderApi`                                                                                |
| `MapBuilderRepository`, `InMemoryMapBuilderRepository`, `mapBuilderRepository`                          | World builder | `WorldBuilderRepository`, `InMemoryWorldBuilderRepository`, `worldBuilderRepository`                                |
| `MapBuilderEvent`, `MapBuilderError`                                                                    | World builder | `WorldBuilderEvent`, `WorldBuilderError`                                                                            |
| `CreateMap`, `ResizeMap`, `ImportMap`, `ExportMap`, `SearchMap`, and their tests                        | World         | `CreateWorld`, `ResizeWorld`, `ImportWorld`, `ExportWorld`, `SearchWorld`, and their tests                          |
| `createMap`, `resizeMap`, `importMap`, `exportMap`, `searchMap`                                         | World         | `createWorld`, `resizeWorld`, `importWorld`, `exportWorld`, `searchWorld`                                           |
| `MapCreated`, `MapResized`, `MapImported`, `MapImportRejected`                                          | World         | `WorldCreated`, `WorldResized`, `WorldImported`, `WorldImportRejected`                                              |
| `MapNotCreated`, "The map has not been created"                                                         | World         | `WorldNotCreated`, "The world has not been created"                                                                 |
| `TileNotFound`'s "The tile is out of the map"                                                           | World         | "The tile is out of the world"                                                                                      |
| `Reason.NOT_A_MAP`                                                                                      | World         | `NOT_A_WORLD`                                                                                                       |
| `Reason.FORBIDDEN_NEIGHBOURS`                                                                           | Adjacent      | `FORBIDDEN_ADJACENT_TERRAINS`                                                                                       |
| `canBeNeighbours`                                                                                       | Adjacent      | `canBeAdjacent`                                                                                                     |
| `neighbourTerrainIds`, `neighbourTerrainId`, `forbiddenNeighbourTerrainId`, `forbiddenNeighbours`       | Adjacent      | `adjacentTerrainIds`, `adjacentTerrainId`, `forbiddenAdjacentTerrainId`, `forbiddenAdjacentTerrains`                |
| `MapBuilderScene`, `MapBuilderPresenter`                                                                | World builder | `WorldBuilderScene`, `WorldBuilderPresenter`                                                                        |
| `MapBuilderHudView`, `MapBuilderInfoView`, `MapBuilderMenuView`, `MapBuilderCallToActionView`           | World builder | `WorldBuilderHudView`, `WorldBuilderInfoView`, `WorldBuilderMenuView`, `WorldBuilderCallToActionView`               |
| `MapFilePicker`, `GameWindowMapFilePicker`, `FakeMapFilePicker`, `mapFilePicker`, `mapFiles`, `pickMap` | World         | `WorldFilePicker`, `GameWindowWorldFilePicker`, `FakeWorldFilePicker`, `worldFilePicker`, `worldFiles`, `pickWorld` |
| `onMapBuilderSelected`, `buildMapButton`, `buildMapLabel`, `clickBuildMap`, `startMapBuilder`           | World builder | `onWorldBuilderSelected`, `buildWorldButton`, `buildWorldLabel`, `clickBuildWorld`, `startWorldBuilder`             |
| `displayMap`, `forbiddenNeighboursMessage`, `givenMapCreated`                                           | World         | `displayWorld`, `forbiddenAdjacentTerrainsMessage`, `givenWorldCreated`                                             |
| `MapBuilderEndToEndTest`                                                                                | World builder | `WorldBuilderEndToEndTest`                                                                                          |
| `MENU_BUILD_MAP`, `MAP_BUILDER_*`, `MAP_FILE_PICKER_MAPS`                                               | World         | `MENU_BUILD_WORLD`, `WORLD_BUILDER_*`, `WORLD_FILE_PICKER_WORLDS`                                                   |

The locals named after the map builder follow: `mapBuilder`, `paintedMapBuilder`, `resizedMapBuilder`,
`importedMapBuilder` and `updatedMapBuilder` say `worldBuilder`, and the fakes, extensions and tests of each view are
renamed with it, e.g.: `FakeMapBuilderHudView` and `MapBuilderHudViewExtension`.

## worldBuilder

### Domain

- `WorldBuilder`'s comment says "The world the player is building: a rectangular battlefield with the terrain id of each
  tile, and the terrain the player paints the tiles with.", for "The map the player is designing".
- `WorldBuilder.paint` rejects a tile whose selected terrain can not be adjacent to an adjacent tile's terrain, and
  publishes `TilePaintRejected` with its `adjacentTerrainId`. E.g.: painting lava next to water.
- `WorldBuilder.import` rejects a world whose terrains `canBeAdjacent` tells can not be adjacent, with
  `FORBIDDEN_ADJACENT_TERRAINS`, and one whose text is not a world, with `NOT_A_WORLD`.
- `WorldBuilderRepository`'s comment says "there is only one world being built".

### Use cases

- `CreateWorld` starts building a new world, replacing the previous one.
- `ImportWorld` replaces the world with the one written in the text. Two different terrains can only be adjacent when
  there is a transition between them, in either direction, as in `PaintTile`.
- `ExportWorld` writes the world in the format the battlefield is loaded from, with `ExportBattlefield`.
- `SearchWorld` returns the world being built, or `null` before it is created.

## screen

- `MenuView` offers "Build a world", and `MenuScene` opens the `WorldBuilderScene` with `onWorldBuilderSelected`.
- `WorldBuilderPresenter` subscribes to `WorldCreated`, `WorldResized`, `WorldImported` and `WorldImportRejected`, and
  displays `NOT_A_WORLD` as "The file has no world".
- `WorldFilePicker` shows the `*.toml` files as "Worlds".

## storybook

- `MapBuilderCallToActionViewStories`, `MapBuilderInfoViewStories`, `MapBuilderMenuViewStories` and
  `MapBuilderHudViewStories` are `WorldBuilder*ViewStories`, with `StorybookApi` listing them.
- Their ids say `screen-world-builder-…` for `screen-map-builder-…`, e.g.: `screen-world-builder-info--large-world` for
  `screen-map-builder-info--large-map`, with the story **Screen / World builder info › Large world**.
- Their titles say "World builder call to action", "World builder info", "World builder menu" and "World builder hud",
  and their descriptions and sample texts say "world", e.g.: "World copied to the clipboard".
- `MenuViewStories` and `CancelAndConfirmViewStories` say "world builder" where they say "map builder".

## Player texts

The keys and texts change in `resources/localization/messages.pot` and `resources/localization/es.po`, with their
translator comments saying "world" for "map", e.g.: "Leaves the world builder, losing the world.".

| Key today                           | New key                                     | English                                                      | Spanish                                                                  |
|-------------------------------------|---------------------------------------------|--------------------------------------------------------------|--------------------------------------------------------------------------|
| `ui.menu.buildMap`                  | `ui.menu.buildWorld`                        | Build a world                                                | Construir un mundo                                                       |
| `ui.mapBuilderInfo.title`           | `ui.worldBuilderInfo.title`                 | World Builder                                                | Constructor de mundos                                                    |
| `ui.mapBuilder.mapCopied`           | `ui.worldBuilder.worldCopied`               | World copied to the clipboard                                | Mundo copiado al portapapeles                                            |
| `ui.mapBuilder.mapLostWhenLeaving`  | `ui.worldBuilder.worldLostWhenLeaving`      | The world is lost when leaving                               | El mundo se pierde al salir                                              |
| `ui.mapBuilder.mapImported`         | `ui.worldBuilder.worldImported`             | World imported                                               | Mundo importado                                                          |
| `ui.mapBuilder.notAMap`             | `ui.worldBuilder.notAWorld`                 | The file has no world                                        | El archivo no tiene ningún mundo                                         |
| `ui.mapBuilder.sizeOutOfRange`      | `ui.worldBuilder.sizeOutOfRange`            | The world must be {minimum} to {maximum} tiles wide and tall | El mundo debe tener de {minimum} a {maximum} casillas de ancho y de alto |
| `ui.mapBuilder.forbiddenNeighbours` | `ui.worldBuilder.forbiddenAdjacentTerrains` | {terrain} can not be next to {neighbour}, as today           | {terrain} no puede estar junto a {neighbour}, as today                   |
| `ui.mapFilePicker.maps`             | `ui.worldFilePicker.worlds`                 | Worlds                                                       | Mundos                                                                   |

The other keys move from `ui.mapBuilder…` to `ui.worldBuilder…` with the same texts: `ui.worldBuilderInfo.size`,
`ui.worldBuilderHud.rows`, `.columns` and `.terrain`, `ui.worldBuilderCallToAction.back`, `.export`, `.stay` and `.leave`,
`ui.worldBuilderMenu.import`, `ui.worldBuilder.fileNotRead` and `ui.worldBuilder.unknownTerrain`. The translator comments
of `terrain.<id>.name` say "Displayed in the world builder".

## Docs

- [Buttons](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/buttons.md) names `WorldBuilderHudView` and "the terrains of the world builder".
- `SnapshotCoverageArchitectureTest` skips `worldBuilder`, as "the world builder exports its worlds instead".
- [Dependencies](../architecture/dependencies.html) is regenerated by the pre-commit hook.

## Tests

The tests are renamed with their classes, and their names say "world" for "map". E.g.:

- `CreateWorldTest`
  - `should store a new world and announce it when a world is created` (replaces `should store a new map and announce it
    when a map is created`)
- `WorldBuilderPresenterTest`
  - `Import should display the terrains when two adjacent tiles of the world in the file can not be next to each other`
    (replaces `Import should display the terrains when two neighbours of the map in the file can not be next to each other`)
  - `TilePainted should only draw again the adjacent tiles inside the world when a corner tile is selected` (replaces
    `TilePainted should only draw again the neighbours inside the map when a corner tile is selected`)
- `MenuViewTest`
  - `BuildWorld should open the world builder when it is clicked`, expecting "Build a world" and "Construir un mundo"
- `WorldBuilderEndToEndTest`
  - `should display a 16 x 16 world when the world builder is opened from the menu`

## Not in this plan

- [Map builder](../devlog/013-map-builder.md) and the other devlog entries keep their words, as a devlog tells what was
  built at the time.

### Waiting for other sections

- `terrain`: whether two terrains can be adjacent, which the world builder checks with `isTransitionAllowed` in either
  direction.
- `screen`: `isOneOfTheNeighbours`, `mapWidth` and `mapHeight` in `BattlefieldView`, which are the battlefield view's own
  words, not the world builder's.

## Changes while building

- A few more words follow the plan than it named: the `map.toml` file of the file picker tests is `world.toml`, the
  text that is not a world reads "Hello, world builder!", every test name saying "neighbour" says "adjacent tile", and
  the comments saying a world is "designed" say it is "built".
