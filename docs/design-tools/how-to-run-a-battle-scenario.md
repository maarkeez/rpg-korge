# How to run a battle scenario

## When to use this

Use this when you want to start the game with a specific battlefield layout and unit placement — for example, to inspect how a terrain mix looks, to put units adjacent so a chain of effects fires immediately, or to reproduce a battle setup while polishing UI.

## Background

A **battle scenario** is a JSON file under `resources/scenarios/` that describes the battlefield grid and where each battle unit starts. The same file can drive the game's dev run, the deterministic replay tests, and the UI script tests, so a scenario you save is reproducible everywhere.

The game's default battle (two knights vs two rats on the 16×16 demo map) is the built-in fallback: running the game without a scenario, or calling `setupBattle()` without arguments, produces exactly the battle described by `resources/scenarios/default.json`.

Scenarios control the **battlefield** (size + terrain tiles) and the **deployments** (which unit, on which side, on which tile). They do **not** change the unit/ability/effect catalog — units referenced by a deployment must be one of the catalog ids (`knight`, `rat`, `bee`), and tiles must reference catalog terrain ids (`sand`, `grass`, `water`, `lava`, `void`). Players are always `player-one` (human, moves first) and `player-two` (CPU).

## Steps

### Run the game with a scenario

1. Pick one of the shipped scenarios, or pass your own file name:

   ```sh
   ./gradlew runJvm -Pscenario=terrain-mix
   ./gradlew runJvm -Pscenario=chain-showcase.json
   ./gradlew runJvmAutoreload -Pscenario=my-scenario
   ```

   The property accepts a name with or without the `.json` suffix. The file is expected at `resources/scenarios/<name>.json`.

2. The scenario's `seed` value is used for the battle's random number generator (the `seed` constructor argument to `BattleScene`, when given, takes precedence), so the battle is deterministic for the life of the file.

### Write a new scenario

1. Copy an existing file as a starting point:

   ```sh
   cp resources/scenarios/default.json resources/scenarios/my-scenario.json
   ```

2. Edit the file:

   - `seed` — optional integer; the RNG seed for the battle.
   - `battlefield.rows` / `battlefield.columns` — grid size (the default map is 16×16; only the top-left 8×8 area is visible in the 390×844 window).
   - `battlefield.tiles` — `rows` arrays of `columns` terrain ids.
   - `deployments` — one entry per battle unit: `battleUnitId` (unique id, convention `player-<n>-unit-<m>`), `unitId` (`knight`, `rat`, or `bee`), `playerId` (`player-one` or `player-two`), `row`, `column`.

3. Run it and iterate:

   ```sh
   ./gradlew runJvmAutoreload -Pscenario=my-scenario
   ```

### Load a scenario from test code

```kotlin
val scenario = runBlocking { BattleScenarioLoader().load("scenarios/my-scenario.json") }
val dto = scenario.toDto() // seed, battlefield, deployments
battleSetupApi.setupBattle(scenario)
```

See `BattleScenarioAcceptanceTest` for a full example that sets up a scenario and asserts the deployed positions.

## Common mistakes

- **Unit appears missing** — the deployment references a `unitId` that is not in the catalog. Only `knight`, `rat`, and `bee` exist.
- **Tile renders as sand** — the tile references an unknown terrain id; use `sand`, `grass`, `water`, `lava`, or `void`.
- **Battle looks identical to the default** — you ran the game without `-Pscenario`, or the file is not under `resources/scenarios/`.
