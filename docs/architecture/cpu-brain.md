# CPU brain

The CPU player decides what its battle units do following the **utility AI** pattern: every option is scored between 0 and 1 from a few considerations, and the best one is chosen when it is worth more than doing nothing.

## Playing a turn

A CPU turn is played one action at a time by the [`CpuBrain`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBrain/domain/CpuBrain.kt), so every decision is based on the state left by the previous action. This allows, for example, a unit to move next to an enemy and attack it in the same turn.

1. [`PlayTurn`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBrain/usecases/commands/PlayTurn.kt) starts the CPU brain with the battle units to manage, in order (`CpuBrainInitialized`).
2. [`ManageCpuBattleUnit`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBrain/usecases/commands/ManageCpuBattleUnit.kt) takes one action with the next battle unit:
   - It casts when [`WhatShouldCast`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBrain/usecases/queries/WhatShouldCast.kt) decides to cast.
   - Otherwise, it moves when [`WhereShouldMove`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBrain/usecases/queries/WhereShouldMove.kt) returns a tile.
   - Otherwise, the battle unit is managed (`CpuBattleUnitManaged`), and the next battle unit follows.
   - Once every battle unit has been managed, the turn is finished (`CpuTurnFinished`, then `RequestFinishPlayerTurn`).
3. After a cast or a move, the CPU brain waits for the event that ends that action, then manages the same battle unit again (`CpuBattleUnitActionResolved`):

| Action | Ends with                         | Published when                                             |
|--------|-----------------------------------|------------------------------------------------------------|
| Move   | `BattlefieldTileOccupied`          | The battlefield occupancy has been updated                 |
| Cast   | `AbilityCastResolved`              | Every effect of the cast has been applied to its targets   |

The CPU brain only resolves the exact action it awaits (the same battle unit and tile, or the same battle unit and ability), so events of other battle units or actions are ignored.

The whole CPU turn is resolved instantly. The battle scene then replays it action by action, e.g.: each battle unit walking its path tile by tile, see [Presentation timeline](presentation-timeline.md).

Every action spends casts or steps, so a turn always ends. As a safeguard, a turn ends after `CpuBrain.MAX_ACTIONS_PER_TURN` (20) actions.

Known limitations:

- Decisions are greedy: a unit takes the best action available now, e.g.: it attacks an adjacent healthy enemy instead of moving next to a weak one and finishing it off. Scoring a move and a cast together is a possible next step.
- The consequences of an applied effect (e.g.: removing a defeated unit from the battlefield) may still be pending when the next decision is made. A defeated unit is already ignored, but its tile may still look occupied.

An option must beat doing nothing by more than `MINIMUM_UTILITY_GAIN` (0.05), defined in [`Utility.kt`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBrain/usecases/services/Utility.kt).

## Casting

Every ability the unit can cast (enough mana, no cooldown), with every target `WhereCanCast` allows, is a candidate. Each ability is classified by its effects:

| Kind   | Effects                                         | Examples                          |
|--------|-------------------------------------------------|-----------------------------------|
| Attack | `DECREASE_HEALTH` on the selected target        | sword, skull, poisoned-sword      |
| Heal   | `INCREASE_HEALTH` on the caster                 | heal                              |
| Other  | Anything else                                   | teleport, bee                     |

The **expected amount** of an effect is its full amount when it is applied immediately. When it is applied every turn, it is its total discounted by 0.9 for every turn it waits, e.g.: venom (3 per turn for 5 turns) is worth 3 × (0.9 + 0.81 + 0.73 + 0.66 + 0.59) ≈ 11.1.

| Kind   | Utility                                                                                                   |
|--------|-----------------------------------------------------------------------------------------------------------|
| Attack | `caster health ratio × Σ min(damage, target remaining HP) / target remaining HP`, the sum capped at 1      |
| Heal   | `healing need × min(healing, missing HP) / healing`                                                       |

- Attacks prefer finishing off weak enemies, and badly hurt casters attack less eagerly.
- Heals are worth more the more the caster needs them, and less when most of the healing would be wasted.
- The best attack or heal is cast when its utility is higher than `MINIMUM_UTILITY_GAIN`. Otherwise, one of the other abilities is cast at random, if any.

For example, a rat (20 HP, sword dealing 10 and heal restoring 20) next to a knight:

| Rat HP | Knight HP | Attack           | Heal              | Decision |
|--------|-----------|------------------|-------------------|----------|
| 20/20  | 100/100   | 1.0 × 0.1 = 0.1  | 0                 | Attack   |
| 15/20  | 100/100   | 0.75 × 0.1 = 0.075 | 0.25 × 0.25 = 0.0625 | Attack |
| 10/20  | 100/100   | 0.5 × 0.1 = 0.05 | 0.5 × 0.5 = 0.25  | Heal     |
| 6/20   | 10/100    | 0.3 × 1.0 = 0.3  | 0.7 × 0.7 = 0.49  | Heal     |
| 20/20  | 8/100     | 1.0 × 1.0 = 1.0  | 0                 | Attack   |

## Moving

Every tile the unit can move to, plus its current tile, is a candidate. Its utility adds up three contributions:

- **Enemies**: how close the nearest enemy is (normalised between the candidates), weighted by the unit's health ratio. Healthy units approach enemies.
- **Allies**: how close the nearest ally is (normalised between the candidates), weighted by `1 - health ratio`. Hurt units fall back towards allies.
- **Terrain**: the health effect of the tile terrain relative to the unit max health, weighted by its healing need. Hurt units look for healing terrain (e.g.: water) and avoid harmful terrain (e.g.: lava).

The unit moves to the best tile, unless it is worth no more than `MINIMUM_UTILITY_GAIN` above staying still.
