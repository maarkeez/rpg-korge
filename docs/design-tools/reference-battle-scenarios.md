# Battle scenarios

Scenario files that parameterize the battlefield layout and unit deployments for dev runs and tests. See [how to run a battle scenario](how-to-run-a-battle-scenario.md).

## File location

`resources/scenarios/<name>.json` — loaded from the classpath via Korge's `resourcesVfs`.

Shipped scenarios:

| File | Seed | Battlefield | Purpose |
| --- | --- | --- | --- |
| `default.json` | 42 | 16×16, same terrain as the hardcoded demo | The built-in default battle, as a file |
| `chain-showcase.json` | 7 | 16×16, demo terrain | Units deployed in adjacent pairs so effect chains trigger immediately |
| `terrain-mix.json` | 1337 | 16×16, all five terrain types | Visual check of every terrain tile type |

## JSON schema

```jsonc
{
  "seed": 42,                          // optional, Long; RNG seed for the battle
  "battlefield": {
    "rows": 16,                        // Int, > 0
    "columns": 16,                     // Int, > 0
    "tiles": [                          // rows arrays of columns terrain ids
      ["sand", "sand", ...],
      ...
    ]
  },
  "deployments": [                     // one entry per battle unit
    {
      "battleUnitId": "player-1-unit-1", // unique within the scenario
      "unitId": "knight",               // catalog unit id: knight | rat | bee
      "playerId": "player-one",         // player-one (human) | player-two (CPU)
      "row": 6,                         // 0-based, < rows
      "column": 6                       // 0-based, < columns
    }
  ]
}
```

Constraints:

- `tiles` must have exactly `rows` entries of exactly `columns` ids; unknown terrain ids render as `sand`.
- `deployments` must have unique `battleUnitId` values and positions within the grid; deployment tiles may overlap (the battlefield does not reserve them).
- `unitId` must be a catalog unit id; `playerId` must be `player-one` or `player-two`.
- The scenario does not change the unit/ability/effect catalog or the players.

## Code

### `BattleScenarioLoader` — `src/commonMain/kotlin/com/mkz/rpg/battlesetup/adapters/serialization/BattleScenarioLoader.kt`

| Member | Signature | Description |
| --- | --- | --- |
| `load` | `suspend fun load(path: String): Battlesetup` | Reads `path` from `resourcesVfs`, decodes the JSON, returns a `Battlesetup` aggregate |

`BattleScenarioJson` (in the same file) is the `@Serializable` mirror of the schema: `seed: Long?`, `battlefield: BattlefieldJson(rows, columns, tiles)`, `deployments: List<DeploymentJson(battleUnitId, unitId, playerId, row, column)>`.

### `Battlesetup` — `src/commonMain/kotlin/com/mkz/rpg/battlesetup/domain/Battlesetup.kt`

Aggregate root for a scenario. Constructed only via `Battlesetup.create(dto)`; fields are private, read via `toDto()`.

| Member | Signature | Description |
| --- | --- | --- |
| `create` | `fun create(battlesetupDto: Dto): Battlesetup` (companion) | Builds the aggregate from its DTO |
| `toDto` | `fun toDto(): Dto` | Exposes `seed`, `battlefield`, `deployments` |
| `Dto` | `data class Dto(seed: Long?, battlefield: BattlefieldDto, deployments: List<DeploymentDto>)` | Public DTO; `BattlefieldDto(rows: Int, columns: Int, tiles: List<List<String>>)`, `DeploymentDto(battleUnitId, unitId, playerId, row, column)` |

### `SetupBattle` — `src/commonMain/kotlin/com/mkz/rpg/battlesetup/usecases/commands/SetupBattle.kt`

| Member | Signature | Description |
| --- | --- | --- |
| `invoke` | `operator fun invoke(scenario: Battlesetup? = null)` | Sets up a battle; with a scenario it uses the scenario's battlefield + deployments, otherwise the hardcoded default (identical to `default.json`) |

### `BattleScene` — `src/commonMain/kotlin/com/mkz/rpg/screen/BattleScene.kt`

| Member | Description |
| --- | --- |
| `scenarioPath: String? = null` constructor parameter | When non-null, `sceneMain` loads `resources/scenarios/<scenarioPath>` and passes it to `setupBattle`; the scenario's `seed` is used for the RNG unless the `seed` parameter is given |

### Gradle

`build.gradle.kts` forwards a `scenario` project property to every `runJvm*` task:

```sh
./gradlew runJvm -Pscenario=chain-showcase        # appends .json
./gradlew runJvm -Pscenario=chain-showcase.json   # accepted as-is
```

The value is passed as the single JVM argument `scenarios/<name>.json`, which `main` in `Main.kt` extracts (first argument ending in `.json`).

## Tests

| Test | Location | Covers |
| --- | --- | --- |
| `BattleScenarioLoaderTest` (3) | `src/jvmTest/kotlin/com/mkz/rpg/battlesetup/adapters/serialization/` | `default.json` decodes to the expected battle; every shipped scenario is internally consistent (grid shape, in-bounds deployments); JSON encode/decode round-trip |
| `BattleScenarioAcceptanceTest` (2) | `src/jvmTest/kotlin/com/mkz/rpg/shared/usecases/acceptance/` | Every shipped scenario sets up a battle with all units deployed at their scenario positions; `default.json` reproduces the hardcoded default battle exactly |
