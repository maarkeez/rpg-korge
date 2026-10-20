# CPU battle brain

The CPU player decides what its battle units do following the **utility AI** pattern: every option is scored between 0 and 1 from a few considerations, and the best one is chosen when it is worth more than doing nothing.

## Playing a turn

A CPU turn is played one action at a time by the [`CpuBattleBrain`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBattleBrain/domain/CpuBattleBrain.kt), so every decision is based on the state left by the previous action. This allows, for example, a unit to move next to an enemy and attack it in the same turn.

1. [`PlayTurn`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBattleBrain/usecases/commands/PlayTurn.kt) starts the CPU battle brain with the battle units to manage, in order (`CpuBattleBrainInitialized`): those of the CPU player in the battle, which `PlayerTurnStarted` carries. A battle unit summoned during the turn (e.g.: a bat a goat summons) is added after them by [`JoinCpuTurn`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBattleBrain/usecases/commands/JoinCpuTurn.kt), as its deploy (`BattleUnitDeployed`) is handled before the cast summoning it is resolved, so it plays the turn too.
2. [`ManageCpuBattleUnit`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBattleBrain/usecases/commands/ManageCpuBattleUnit.kt) takes one action with the next battle unit:
   - It casts when [`WhatShouldCast`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBattleBrain/usecases/queries/WhatShouldCast.kt) decides to cast.
   - Otherwise, it moves when [`WhereShouldMove`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBattleBrain/usecases/queries/WhereShouldMove.kt) returns a tile.
   - Otherwise, the battle unit is managed (`CpuBattleUnitManaged`), and the next battle unit follows.
   - Once every battle unit has been managed, the turn is finished (`CpuTurnFinished`, then `RequestFinishPlayerTurn`).
3. After a cast or a move, the CPU battle brain waits for the event that ends that action, then manages the same battle unit again (`CpuBattleUnitActionResolved`):

| Action | Ends with                         | Published when                                             |
|--------|-----------------------------------|------------------------------------------------------------|
| Move   | `BattlefieldTileOccupied`          | The battlefield occupancy has been updated                 |
| Cast   | `AbilityCastResolved`              | Every effect of the cast has been applied to its targets   |

The CPU battle brain only resolves the exact action it awaits (the same battle unit and tile, or the same battle unit and ability), so events of other battle units or actions are ignored.

The whole CPU turn is resolved instantly. The battle scene then replays it action by action, e.g.: each battle unit walking its path tile by tile, see [Presentation timeline](presentation-timeline.md).

Every action spends casts or steps, so a turn always ends. As a safeguard, a turn ends after `CpuBattleBrain.MAX_ACTIONS_PER_TURN` (20) actions.

Known limitations:

- Decisions are greedy: a unit takes the best action available now, e.g.: it attacks an adjacent healthy enemy instead of moving next to a weak one and finishing it off. Scoring a move and a cast together is a possible next step.
- The consequences of an applied effect (e.g.: removing a defeated unit from the battlefield) may still be pending when the next decision is made. A defeated unit is already ignored, but its tile may still look occupied.

An option must beat doing nothing by more than `MINIMUM_UTILITY_GAIN` (0.05), defined in [`Utility.kt`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBattleBrain/usecases/services/Utility.kt).

## Engaging

A battle unit only engages the enemies **within reach**: the ones it can walk to in `ENGAGEMENT_DISTANCE` (5) tiles or fewer, counting their tile, found by [`SearchEnemiesWithinReach`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBattleBrain/usecases/queries/SearchEnemiesWithinReach.kt). The other enemies are ignored, so the CPU battle units wait until the human battle units come close.

- The walk only steps on tiles the battle unit could stand on: other battle units (allies included) and terrains that can not be occupied (e.g.: void) block it, while terrains that hurt (e.g.: lava) do not. It is counted by the battlefield [`SearchWalkingDistance`](../../src/commonMain/kotlin/com/mkz/rpg/battlefield/usecases/queries/SearchWalkingDistance.kt).
- It is checked at every decision, with the battlefield as it is then, and nothing is remembered: an enemy walking out of reach is no longer followed.
- Without an enemy within reach, the battle unit does not approach, attack, deploy or cast its other abilities (e.g.: teleport). It still heals, and still falls back towards its allies and healing terrain when hurt.

For example, next to a knight, a rat two tiles away moves next to it and attacks it, while a rat six tiles away waits. A rat whose way is blocked by an ally may also wait, until the way is free or another way is short enough.

The same rule starts the battles: while the human player explores, a CPU battle unit with a battle unit of the party within reach engages it, and the battle starts. When a round finishes, the CPU battle units out of the battle wander, then those that got an enemy within reach join the battle, and those without any leave it, until none is left and the party explores again (see [Exploration](exploration.md#engaging)).

### Summons

A summoned battle unit (e.g.: the bat a goat summons) lasts only some turns, and spends them attacking: every enemy it can walk to is within its reach, however far, so it approaches and attacks the nearest one. It plays the turn it is summoned in, after the battle units already playing it. It never leaves the battle when a round finishes, even with no enemy it can walk to, and claims no home, so it never wanders: it vanishes once its turns are over.

## Casting

Every ability the unit can cast (enough mana, no cooldown), with every target `WhereCanCast` allows, is a candidate, but only heals when no enemy is within reach. Each ability is classified by its effects:

| Kind   | Effects                                         | Examples                          |
|--------|-------------------------------------------------|-----------------------------------|
| Attack | `DECREASE_HEALTH` on the selected target        | sword, skull, poisoned-sword      |
| Heal   | `INCREASE_HEALTH` on the caster                 | heal                              |
| Deploy | `DEPLOY_BATTLE_UNIT`                            | bee, bat                          |
| Other  | Anything else                                   | teleport                          |

The **expected amount** of an effect is its full amount when it is applied immediately. When it is applied every turn, it is its total discounted by 0.9 for every turn it waits, e.g.: venom (3 per turn for 5 turns) is worth 3 × (0.9 + 0.81 + 0.73 + 0.66 + 0.59) ≈ 11.1.

| Kind   | Utility                                                                                                   |
|--------|-----------------------------------------------------------------------------------------------------------|
| Attack | `caster health ratio × Σ min(damage, target remaining HP) / target remaining HP`, the sum capped at 1      |
| Heal   | `healing need × min(healing, missing HP) / healing`                                                       |
| Deploy | `caster health ratio × 0.9 ^ turns to attack × min(deployed damage, enemy remaining HP) / enemy remaining HP` |

- Attacks prefer finishing off weak enemies, and badly hurt casters attack less eagerly.
- Heals are worth more the more the caster needs them, and less when most of the healing would be wasted.
- Attacks only count the targets within reach.
- Deploys are worth the attack the deployed battle unit will make on the enemy within reach of the caster nearest to its tile:
  - The **deployed damage** is the expected amount of the best attack of the deployed unit.
  - The deployed battle unit acts in the turn it is deployed in, after the battle units already playing it, and moves and attacks in the same turn, so the **turns to attack** are `max(1, ⌈(distance to the enemy - 1) / its movement range⌉)`, using the Manhattan distance. Hence, the tile nearest to the enemy is preferred.
  - A deployed unit without an attack, or without an enemy within reach, is worth 0.
- The best attack, heal or deploy is cast when its utility is higher than `MINIMUM_UTILITY_GAIN`. Otherwise, one of the other abilities is cast at random, if any.

For example, a rat (20 HP, sword dealing 10 and heal restoring 20) next to a knight:

| Rat HP | Knight HP | Attack           | Heal              | Decision |
|--------|-----------|------------------|-------------------|----------|
| 20/20  | 100/100   | 1.0 × 0.1 = 0.1  | 0                 | Attack   |
| 15/20  | 100/100   | 0.75 × 0.1 = 0.075 | 0.25 × 0.25 = 0.0625 | Attack |
| 10/20  | 100/100   | 0.5 × 0.1 = 0.05 | 0.5 × 0.5 = 0.25  | Heal     |
| 6/20   | 10/100    | 0.3 × 1.0 = 0.3  | 0.7 × 0.7 = 0.49  | Heal     |
| 20/20  | 8/100     | 1.0 × 1.0 = 1.0  | 0                 | Attack   |

For example, a goat (30 HP, deploying a bat whose sword deals 10 and walks 5 tiles per turn) within reach of a knight (100 HP). The bat attacks the knight in its first turn from the tile next to the goat:

| Goat HP | Deploy                  | Heal               | Decision |
|---------|-------------------------|--------------------|----------|
| 30/30   | 1.0 × 0.9 × 0.1 = 0.09  | 0                  | Deploy   |
| 15/30   | 0.5 × 0.9 × 0.1 = 0.045 | 0.5 × 0.75 = 0.375 | Heal     |

## Moving

Every tile the unit can move to, plus its current tile, is a candidate. Its utility adds up three contributions:

- **Enemies**: how close the nearest enemy within reach is (normalised between the candidates), weighted by the unit's health ratio. Healthy units approach the enemies within reach, and add nothing without any.
- **Allies**: how close the nearest ally is (normalised between the candidates), weighted by `1 - health ratio`. Hurt units fall back towards allies.
- **Terrain**: the health effect of the tile terrain relative to the battle unit max health (equipment included), weighted by its healing need. Hurt units look for healing terrain (e.g.: water) and avoid harmful terrain (e.g.: lava).

The unit moves to the best tile, unless it is worth no more than `MINIMUM_UTILITY_GAIN` above staying still.

## Wandering

While the human player explores, the CPU battle units wander around their **home**: the tile each one was deployed on. The summons (e.g.: a bat deployed by a goat) have none, as they never wander (see [Summons](#summons)). Their **territory** is the tiles walkable from home in `WANDER_DISTANCE` (3) tiles or fewer, without stepping on harmful terrain (e.g.: lava), as they never step on it while they wander. Only the terrain bounds it (e.g.: void), not the battle units, so it does not change as they walk around.

The [`wandering`](../../src/commonMain/kotlin/com/mkz/rpg/wandering) subdomain remembers the homes, and walks the CPU battle units every second, while the CPU battle brain decides where each one steps:

1. [`ClaimHome`](../../src/commonMain/kotlin/com/mkz/rpg/wandering/usecases/commands/ClaimHome.kt) remembers the tile of every CPU battle unit deployed (`BattleUnitDeployed`), but the summons.
2. Every second while exploring (`ExplorationSecondStarted`), [`WanderWhileExploring`](../../src/commonMain/kotlin/com/mkz/rpg/wandering/usecases/commands/WanderWhileExploring.kt) lets each CPU battle unit rest or take one step, one after another, in their deployment order.
3. [`WhereShouldWander`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBattleBrain/usecases/queries/WhereShouldWander.kt) decides it. Each step is requested with `RequestWalkBattleUnit`, as the party steps, so it spends no turn action.
4. Each step is awaited until its battle unit occupies its tile (`BattlefieldTileOccupied`, see [`ResolveWanderingStep`](../../src/commonMain/kotlin/com/mkz/rpg/wandering/usecases/commands/ResolveWanderingStep.kt)), so the next battle unit decides with the battlefield as it is then: two battle units never step on the same tile.
5. Once every CPU battle unit has wandered (`BattleUnitsWanderedWhileExploring`), [`EngageEnemies`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBattleBrain/usecases/commands/EngageEnemies.kt) engages the party when a CPU battle unit wandered within its reach.

Within its territory, a CPU battle unit rests at random (`REST_CHANCE`, 0.5), but on harmful terrain. Otherwise, every free tile next to it that is not harmful (e.g.: lava) and within its territory, or nearer to home, is a candidate. It steps to one of them at random, each one as likely as its utility, which adds up three contributions:

- **Roam**: how much further from home the tile is (normalised between the candidates and the current tile), weighted by `1 - distance from home / WANDER_DISTANCE`. Near home, the battle unit walks away from it.
- **Home**: how much nearer to home the tile is (normalised the same way), weighted by `distance from home / WANDER_DISTANCE`. Far from home, the battle unit walks back to it.
- **Terrain**: the health effect of the tile terrain relative to the battle unit max health, weighted by its healing need, as when [moving](#moving). Hurt battle units prefer healing terrain (e.g.: water).

The distance from home is the walking distance through the battle units, around void and harmful terrain ([`SearchDistancesFromHome`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBattleBrain/usecases/queries/SearchDistancesFromHome.kt)). As it only walks over the tiles a wandering battle unit can step on, every tile away from home has a tile next to it nearer to home, so a battle unit heading home never stops in front of harmful terrain: it walks around it. The harmful tiles are counted from the nearest walked tile next to them, without walking through them, so a battle unit standing on one (e.g.: after a chase) knows how far it is from home. Picking at random, rather than the best tile, lets the battle unit reach the edge of its territory now and then, instead of going back and forth between the same two tiles. For example, on open sand:

| Distance from home | Steps (utility)                                          |
|--------------------|----------------------------------------------------------|
| 0 (home)           | Every tile next to it (1 each): it walks away in any direction |
| 1                  | Further (0.67), along (0.5), back home (0.33)            |
| 2                  | Back (0.67), along (0.5), further, to the edge (0.33)    |
| 3 (edge)           | Back (1), along the edge (0): it always walks back       |

A CPU battle unit outside its territory (e.g.: after chasing the party in a battle, as an engaged battle unit is not tied to its home) **returns** home ([`IsOutsideTerritory`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBattleBrain/usecases/queries/IsOutsideTerritory.kt)): it never rests, and only steps to the tiles nearer to home, picking one of them at random, all the way to its home tile, through its territory. The wandering remembers the returning battle units, as the CPU battle brain only decides one step at a time, and stops once one stands at home, where it wanders as before. It stays when home can not be walked to, e.g.: walled off by void, or only through harmful terrain, or while another battle unit stands on the only way home.

Every time they wander, whether they rest or step, the CPU battle units recover `WANDERING_RECOVERY_PERCENTAGE` (10%) of their maximum health and mana, the equipment included, rounded up and up to the maximums: a rat (20 health, 10 mana) recovers 2 health and 1 mana every second. The wandering requests it to the battle units (`RequestRecoverBattleUnits`, see [`RecoverBattleUnits`](../../src/commonMain/kotlin/com/mkz/rpg/battleUnit/usecases/commands/RecoverBattleUnits.kt)). A defeated battle unit does not recover, as it stays stored with no health. The CPU battle units out of a battle also recover once per round, when they wander after it, and the ones in the battle do not. The recovery is not displayed.

When a battle round finishes (`BattleRoundFinished`), the CPU battle units out of the battle wander once too, as every second while exploring ([`WanderAfterRound`](../../src/commonMain/kotlin/com/mkz/rpg/wandering/usecases/commands/WanderAfterRound.kt)). Once they have (`BattleUnitsWanderedAfterRound`), [`UpdateEngagement`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBattleBrain/usecases/commands/UpdateEngagement.kt) updates the engagement, so a CPU battle unit that wandered within reach of the party joins the battle from the next round. It is announced also when no CPU battle unit is out of the battle, as the next round starts on it. The CPU battle units in the battle do not wander: they move as in any battle, not tied to their home.

### Respawning

A defeated CPU battle unit with a home comes back to it once the party has walked `RESPAWN_STEPS` (50) steps since its defeat, whether it was defeated in a battle or by its terrain while exploring. The summons have no home, so they never respawn.

1. [`AwaitRespawn`](../../src/commonMain/kotlin/com/mkz/rpg/wandering/usecases/commands/AwaitRespawn.kt) starts counting the party steps of the battle unit when it is defeated (`BattleUnitDefeated`).
2. Every time the party walks a step (`PartyWalked`), [`RespawnDefeatedBattleUnits`](../../src/commonMain/kotlin/com/mkz/rpg/wandering/usecases/commands/RespawnDefeatedBattleUnits.kt) counts it for every defeated battle unit, and requests the respawn of those that have counted enough steps on their home (`RequestRespawnBattleUnit`). There are no steps while battling, so the battle units only count while the party explores.
3. While its home is occupied (e.g.: by a battle unit of the party), the battle unit waits: it is requested again on the next step, and respawns on the first one its home is free.
4. [`RespawnBattleUnit`](../../src/commonMain/kotlin/com/mkz/rpg/battleUnit/usecases/commands/RespawnBattleUnit.kt) brings it back as it was deployed: full health and mana, no cooldowns, no ongoing effects, with the same id. It announces `BattleUnitRespawned`, so the battlefield occupies its home and the screen displays it, and [`ResolveRespawn`](../../src/commonMain/kotlin/com/mkz/rpg/wandering/usecases/commands/ResolveRespawn.kt) stops counting its steps.

A respawned battle unit wanders around its home from the next second, and drops its loot again when it is defeated again. It is not deployed again (`BattleUnitDeployed`), so it never joins a battle on its own: a battle engaged on the step it respawns on is played without it, and it joins it as any other CPU battle unit out of the battle, when the engagement is updated with the party within its reach. A battle unit of the party within its reach when it respawns is engaged on the next check, when the CPU battle units wander or the party walks again.

The game is over once every CPU battle unit is defeated at the same time, before the party walks any step, so none respawns then.
