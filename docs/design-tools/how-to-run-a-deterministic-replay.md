# How to run a deterministic battle replay

## When to use this

Use this when you want two runs of the same battle to produce the exact same sequence of decisions and events, so you can compare UI changes, debug a specific CPU move, or record and replay a battle.

## Background

The CPU makes random choices in two places:

- `PlayTurn` picks the ability and the cast target at random.
- `ApplyEffect` generates a random ID for deployed battle units.

Both use cases accept a `kotlin.random.Random` through their constructors, and `BattleScene` accepts a `seed` that is shared with the whole API graph. When no seed is given, the game behaves exactly as before (non-deterministic).

## Steps

### Replay in the running game

1. Pass a seed when creating the scene:

   ```kotlin
   BattleScene(seed = 42)
   ```

2. Play the same sequence of human actions in the same order. Every CPU turn will make the same decisions and generate the same deployed unit IDs as any other run with the same seed and the same human inputs.

### Replay in a test

1. Build the API graph twice with the same seed, wrapping the event bus in a `RecordingEventBus`:

   ```kotlin
   val random = Random(seed = 42)
   val eventBus = RecordingEventBus(InMemoryEventBus())
   val terrainApi = TerrainApi(eventBus)
   val unitApi = UnitApi(eventBus)
   val playerApi = PlayerApi(eventBus)
   val battlefieldApi = BattlefieldApi(terrainApi, eventBus)
   val effectApi = EffectApi(eventBus)
   val abilityApi = AbilityApi(effectApi, eventBus)
   val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus, random)
   val battleApi = BattleApi(eventBus, battleUnitApi)
   CpuBrainApi(unitApi, playerApi, battleUnitApi, battlefieldApi, terrainApi, effectApi, eventBus, random)
   val battleSetupApi = BattleSetupApi(eventBus)
   ```

2. Set up the default battle and clear the events recorded during setup:

   ```kotlin
   runBlocking { terrainApi.init() }
   battleSetupApi.setupBattle()
   eventBus.dispatch()
   eventBus.clear()
   ```

3. Play CPU rounds. Each round is the human player finishing their turn, which hands control to the CPU and lets it play a full turn:

   ```kotlin
   repeat(4) {
       battleApi.finishPlayerTurn()
       eventBus.dispatch()
   }
   ```

4. Read the recorded sequence with `eventBus.events` and compare it with the events of another run.

## Verification

`DeterministicBattleAcceptanceTest` proves the contract: two harnesses built with the same seed and the same default battle produce identical event sequences after playing the same number of CPU rounds.

Run it with:

```sh
./gradlew jvmTest --tests "com.mkz.rpg.shared.usecases.acceptance.DeterministicBattleAcceptanceTest"
```

## Notes

- The seed only makes the CPU deterministic. The human player's decisions still have to be repeated manually (or scripted, see the UI interaction tooling).
- Different seeds produce different CPU behavior. Use one fixed seed per scenario you want to reproduce.
