# Reference: deterministic battle harness

Reference for the classes and parameters that make a battle deterministic, and for the `RecordingEventBus` used to capture the event sequence of a run.

## BattleScene.seed

| Parameter | Type | Default | Description |
| --- | --- | --- | --- |
| `seed` | `Long?` | `null` | Seed for the `kotlin.random.Random` shared by the CPU brain and the battle unit subdomain. `null` means non-deterministic (seeded with `System.nanoTime()`). |

```kotlin
class BattleScene(val seed: Long? = null) : Scene()
```

The same `Random` instance is passed to both `BattleUnitApi` and `CpuBrainApi`, so every random decision in a battle consumes from a single sequence.

## Random injection points

| Class | Constructor parameter | Used for |
| --- | --- | --- |
| `PlayTurn` | `random: Random = Random.Default` | Picking the ability and the cast target for each CPU battle unit |
| `ApplyEffect` | `random: Random = Random.Default` | Generating the ID of deployed battle units (`deployed-unit-<n>`) |
| `BattleUnitApi` | `random: Random = Random.Default` | Forwarded to `ApplyEffect` |
| `CpuBrainApi` | `random: Random = Random.Default` | Forwarded to `PlayTurn` |

## RecordingEventBus

A decorator over any `EventBus` that records every published event, in publish order, while delegating all behavior to the wrapped bus.

```kotlin
class RecordingEventBus(private val delegate: EventBus) : EventBus {
    val events: List<DomainEvent>   // snapshot of the recorded events
    fun clear()                      // removes all recorded events
}
```

| Member | Description |
| --- | --- |
| `publish(event)`, `publish(events)`, `dispatch()`, `subscribe(...)` | Delegated to the wrapped `EventBus` after recording the event(s) |
| `events` | Returns a copy of the recorded events. Reading it multiple times returns independent snapshots |
| `clear()` | Removes all recorded events without touching the delegate |

## DeterministicBattleAcceptanceTest

| Test | What it asserts |
| --- | --- |
| `should produce identical event sequences when the same seed is used` | Two full API graphs built with `seed = 42`, the default battle, and 4 CPU rounds each produce equal `events` lists |
| `should include cpu decisions in the recorded events when playing cpu rounds` | The recorded sequence is not empty after playing CPU rounds |

## One CPU round

From a state where it is the human player's turn:

1. `battleApi.finishPlayerTurn()` publishes `RequestFinishPlayerTurn`.
2. `eventBus.dispatch()` processes the chain: `FinishPlayerTurn` starts the CPU's turn (`PlayerTurnStarted`), the CPU brain plays its turn (publishing cast/move requests and `RequestFinishPlayerTurn`), and the chain ends back on the human player's turn.

Repeating these two calls plays consecutive CPU rounds.
