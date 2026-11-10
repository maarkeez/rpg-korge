# Plan: One defeat when stacked venoms finish off a battle unit

> **Status:** done.
>
> **Summary:** A battle unit carrying two or more venoms can be defeated several times in one player turn. E.g.: the
> knight hits the Goat Wizard twice with poisoned-sword, so it carries two `venom-damage` stacks. Its player turn starts
> with 3 health left, and each stack publishes its own `BattleUnitDefeated`. Its loot drop is rolled once per defeat, and
> the `goat-wizard` loot table always rolls bee-essence, so two bee-essence lie on its tile. The same defeat is also
> counted twice by a defeat task and awaited twice by the CPU wander brain. Instead, the stacks trigger one after
> another, each on its own, on the battle unit as the previous one left it. They stop once it is defeated, and a defeat
> happens once until the battle unit respawns. So the Goat Wizard publishes one `BattleUnitDefeated` and drops one
> bee-essence.

## Cause

Three faults combine, all in the `battleUnit` subdomain:

1. `TriggerOnPlayerTurnStartedEffects` reads the battle unit once, then, for each effect id in
   `ongoingEffects.onPlayerTurnStarted`, calls `triggerOnPlayerTurnStartedEffect` on that same stale copy. Each venom
   takes its 3 damage off the starting health, and the last write wins. So two venoms deal 3 per player turn, not 6.
2. `BattleUnit.applyDecreaseHealthEffect` publishes `BattleUnitDefeated` (and `RequestRemoveOccupant`) whenever the
   health ends at 0 or below, not only when it goes from some health left to none.
3. `OngoingEffects.triggerOnPlayerTurnStartedEffect(effectId)` finds the stack by its effect id and replaces every stack
   with that id by the first one's triggered copy. So two venoms received on different player turns end up sharing one
   remaining duration.

For example, the Goat Wizard has 3 health and two venoms (3 damage each):

| Step            | Today                                         | Once built                                      |
|-----------------|-----------------------------------------------|-------------------------------------------------|
| First venom     | 3 → 0, `BattleUnitDefeated`, rolls bee-essence | 3 → 0, `BattleUnitDefeated`, rolls bee-essence |
| Second venom    | 3 → 0 again, `BattleUnitDefeated`, rolls again | Does not trigger: the battle unit is defeated  |
| Bee-essence     | 2                                             | 1                                               |

## Decisions

| Topic          | Decision                                                                                                                                                                                                                         |
|----------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope          | The defeat, the triggering of ongoing effects timed on player turn started, and the stacks of an effect, in `battleUnit`. Venom is the only such effect today (`venom-damage`: 3 damage, 5 player turns).                       |
| Defeat         | A [defeat](../../GLOSSARY.md) is a battle unit's health going from some left to none, once until it respawns: a defeated battle unit takes no more damage. Chosen over clamping later damage at 0 and only publishing on the change, as no damage to a defeated battle unit means anything. |
| Stack          | Each venom received is its own ongoing effect with its own remaining duration, so two venoms deal 6 per player turn. Chosen over refreshing the one venom's remaining duration, as stacking is what the ongoing effects already store. |
| Sequence       | The stacks trigger in the order they were received, each on the battle unit as the previous one left it, and the rest stop once one defeats it. The stacks not triggered stay, and are cleared by `respawn` as today.            |
| Loot drop      | Unchanged: `RollLootDrop` still rolls once per `BattleUnitDefeated`. Chosen over a guard in the loot drop against repeated defeats, as the quest and the CPU wander brain also trust a single defeat, and a guard would hide the fault from them. |
| Architecture   | The sequence lives in `battleUnit.domain`: `BattleUnit.triggerOnPlayerTurnStartedEffects` replaces the per-id `triggerOnPlayerTurnStartedEffect`. `TriggerOnPlayerTurnStartedEffects` in `battleUnit.usecases` only looks the effects up, updates the battle unit once and publishes once. Chosen over looping in the use case, as only the domain can tell the stacks apart. |
| Tests          | Unit tests in `BattleUnitTest`, and a use case test in `TriggerOnPlayerTurnStartedEffectsTest` with two venoms and 3 health publishing one defeat. No end-to-end test.                                                          |
| Out of scope   | The items already lying in saves stay where they are, with no save migration, as an extra item can't be told from a rolled one. No ADR.                                                                                         |

## battleUnit

### Domain

- `BattleUnit.applyDecreaseHealthEffect` does nothing when the battle unit is already defeated: no damage, no
  `BattleUnitDamaged`, no `BattleUnitDefeated`, no `RequestRemoveOccupant`. It publishes `BattleUnitDefeated` only when
  the damage takes the health from above 0 to 0. So `triggerImmediateEffect` on a defeated battle unit leaves it as it
  is too.
- `BattleUnit.triggerOnPlayerTurnStartedEffects(effects: List<Effect.Dto>, currentRow: Int, currentColumn: Int)`
  replaces `triggerOnPlayerTurnStartedEffect`. It triggers each ongoing effect timed on player turn started, in the
  order received, on the battle unit as the previous one left it, and stops once it is defeated. `effects` holds the
  definition of each effect id. Each stack publishes its own `OnPlayerTurnStartedEffectTriggered` before its damage.
  E.g.: a knight with 10 health and two venoms ends on 4 health, with both venoms at 4 player turns left.
- `OngoingEffects` triggers a stack by its place in the list instead of by its effect id. `triggerOnPlayerTurnStarted`
  takes the index, lowers that stack's remaining duration only, and removes it once it reaches 0. Two venoms with 5 and
  2 player turns left become 4 and 1.

### Use cases

- `TriggerOnPlayerTurnStartedEffects` looks up, with `searchEffectById`, each distinct effect id of the battle unit's
  ongoing effects timed on player turn started. It calls `triggerOnPlayerTurnStartedEffects` once, updates the
  battle unit once and publishes its events once.

## Tests

- `BattleUnitTest`
  - `TriggerImmediateEffect should neither damage nor publish the defeated event when the battle unit is already defeated`
  - `TriggerOnPlayerTurnStartedEffects should deal the damage of every stack when the battle unit has two of the same effect`
  - `TriggerOnPlayerTurnStartedEffects should publish one defeated event and stop triggering when a stack defeats the battle unit`
  - `TriggerOnPlayerTurnStartedEffects should lower the remaining duration of each stack on its own when the stacks were received on different player turns`
  - The existing `TriggerOnPlayerTurnStartedEffect should …` tests move to `triggerOnPlayerTurnStartedEffects` with the
    same names under `TriggerOnPlayerTurnStartedEffects`.
- `TriggerOnPlayerTurnStartedEffectsTest`
  - `should publish one defeated event when two venoms finish off a battle unit with 3 health`
  - `should deal the damage of both venoms when the battle unit has two of them`

## Known limitations

- A battle unit defeated by an immediate effect still receives the other effects of the same cast, such as the venom of
  venom-strike. It takes no damage from them, and `respawn` clears them.
- Extra bee-essence already dropped in a save stays on its tile.

## Changes while building

- The events of a battle unit are a `Set`, so two stacks of the same effect publish their `OnPlayerTurnStartedEffectTriggered`
  and `BattleUnitDamaged` once, not once per stack. The health still drops by every stack, so
  `TriggerOnPlayerTurnStartedEffects should deal the damage of every stack when the battle unit has two of the same effect`
  checks each event once.
- `TriggerOnPlayerTurnStartedEffect should only update the matching on player turn started effect when the battle unit has
  several effects` becomes `TriggerOnPlayerTurnStartedEffects should trigger every on player turn started effect when the
  battle unit has several effects`, as every ongoing effect timed on player turn started now triggers in one call.
- `applyDecreaseHealthEffect` on a defeated battle unit still takes the triggered immediate effect off its ongoing effects,
  so it does not linger until the respawn.
