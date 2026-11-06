# CPU battle brain

The CPU player decides what its battle units do following the **utility AI** pattern: every option is scored between 0 and 1 from a few considerations, and the best one is chosen when it is worth more than doing nothing.

## Playing a player turn

A CPU player turn is played one decision at a time by the [`CpuBattleBrain`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBattleBrain/domain/CpuBattleBrain.kt), so every decision is based on the state left by the previous one. This allows, for example, a unit to walk next to an enemy and cast at it in the same player turn.

1. [`PlayPlayerTurn`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBattleBrain/usecases/commands/PlayPlayerTurn.kt) starts the CPU battle brain with its pending battle units, in order (`CpuPlayerTurnStarted`): those of the CPU player in the battle, which `PlayerTurnStarted` carries. A battle unit summoned during the player turn (e.g.: a bat a goat summons) is added after them by [`JoinCpuPlayerTurn`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBattleBrain/usecases/commands/JoinCpuPlayerTurn.kt), as its deploy (`BattleUnitDeployed`) is handled before the cast summoning it is resolved, so it plays the player turn too.
2. [`MakeNextDecision`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBattleBrain/usecases/commands/MakeNextDecision.kt) makes the next decision for the next pending battle unit:
   - It casts when [`WhatShouldCast`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBattleBrain/usecases/queries/WhatShouldCast.kt) decides to cast.
   - Otherwise, it walks when [`WhereShouldWalk`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBattleBrain/usecases/queries/WhereShouldWalk.kt) returns a tile.
   - Otherwise, the battle unit is done (`CpuBattleUnitDone`), and the next battle unit follows.
   - Once every battle unit is done, the player turn is finished (`CpuPlayerTurnFinished`, then `RequestFinishPlayerTurn`).
3. After a cast or a walk, the CPU battle brain waits for the event that ends that decision, then makes the next decision for the same battle unit (`CpuBattleUnitDecisionResolved`):

| Decision | Ends with                         | Published when                                             |
|----------|-----------------------------------|------------------------------------------------------------|
| Walk     | `TileOccupied`                    | The tile has been occupied                                 |
| Cast     | `AbilityCastResolved`              | Every effect of the cast has been applied to its targets   |

The CPU battle brain only resolves the exact decision it awaits (the same battle unit and tile, or the same battle unit and ability), so events of other battle units or decisions are ignored.

The whole CPU player turn is resolved instantly. The battle scene then replays it decision by decision, e.g.: each battle unit walking its path tile by tile, see [Presentation timeline](presentation-timeline.md).

Every decision spends casts or steps, so a player turn always ends. As a safeguard, a player turn ends after `CpuBattleBrain.MAX_DECISIONS_PER_PLAYER_TURN` (20) decisions.

Known limitations:

- Decisions are greedy: a unit takes the best decision available now, e.g.: it damages an adjacent healthy enemy instead of walking next to a weak one and finishing it off. Scoring a walk and a cast together is a possible next step.
- The consequences of an applied effect (e.g.: removing a defeated unit from the battlefield) may still be pending when the next decision is made. A defeated unit is already ignored, but its tile may still look occupied.

An option must beat doing nothing by more than `MINIMUM_UTILITY_GAIN` (0.05), defined in [`Utility.kt`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBattleBrain/usecases/services/Utility.kt).

## Engaging

A battle unit only engages the enemies **within reach**: the ones it can walk to in `REACH` (5) tiles or fewer, counting their tile, found by [`SearchEnemiesWithinReach`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBattleBrain/usecases/queries/SearchEnemiesWithinReach.kt). The other enemies are ignored, so the CPU battle units wait until the human battle units come close.

- The walk only steps on tiles the battle unit could stand on: other battle units (allies included) and tiles that are not occupiable (e.g.: void) block it, while terrains that hurt (e.g.: lava) do not. It is counted by the battlefield [`SearchWalkingDistance`](../../src/commonMain/kotlin/com/mkz/rpg/battlefield/usecases/queries/SearchWalkingDistance.kt).
- It is checked at every decision, with the battlefield as it is then, and nothing is remembered: an enemy walking out of reach is no longer followed.
- Without an enemy within reach, the battle unit does not approach, damage, summon or cast its other abilities (e.g.: teleport). It still heals, and still falls back towards its allies and healing terrain when hurt.

For example, next to a knight, a rat two tiles away walks next to it and damages it, while a rat six tiles away waits. A rat whose way is blocked by an ally may also wait, until the way is free or another way is short enough.

The same rule starts the battles: while the human player explores, a CPU battle unit with a battle unit of the party within reach engages it, and the battle starts. When a round finishes, the CPU battle units out of the battle wander, then those that got an enemy within reach join the battle, and those without any leave it, until none is left and the party explores again (see [Exploration](exploration.md#engaging)). When the last CPU battle unit in the battle is defeated during a round, the CPU battle units out of it with the party within reach join it straight away, and the party explores again when none has (see [Back to exploring](exploration.md#back-to-exploring)).

### Summons

A summoned battle unit (e.g.: the bat a goat summons) lasts its summon duration, and spends it attacking: every enemy it can walk to is within its reach, however far, so it approaches and attacks the nearest one. It plays the player turn it is summoned in, after the battle units already playing it. It never leaves the battle when a round finishes, even with no enemy it can walk to, and claims no home, so it never wanders: it vanishes once its summon duration is over.

## Casting

Every ability the unit can cast (enough mana, no cooldown), with every target `WhereCanCast` allows, is an option, but only heals when no enemy is within reach. Each ability is classified by its effects:

| Kind   | Effects                                         | Examples                          |
|--------|-------------------------------------------------|-----------------------------------|
| Damage | `DECREASE_HEALTH` on the selected target        | sword, skull, poisoned-sword      |
| Heal   | `INCREASE_HEALTH` on the caster                 | heal                              |
| Summon | `DEPLOY_BATTLE_UNIT`                            | bee, bat                          |
| Other  | Anything else                                   | teleport                          |

The **expected amount** of an effect is its full amount when it is applied immediately. When it is applied every turn, it is its total discounted by 0.9 for every turn it waits, e.g.: venom (3 per turn for 5 turns) is worth 3 × (0.9 + 0.81 + 0.73 + 0.66 + 0.59) ≈ 11.1.

| Kind   | Utility                                                                                                   |
|--------|-----------------------------------------------------------------------------------------------------------|
| Damage | `caster health ratio × Σ min(damage, target remaining HP) / target remaining HP`, the sum capped at 1      |
| Heal   | `healing need × min(healing, missing HP) / healing`                                                       |
| Summon | `caster health ratio × 0.9 ^ player turns to damage × min(summon damage, enemy remaining HP) / enemy remaining HP` |

- Damage prefers finishing off weak enemies, and badly hurt casters damage less eagerly.
- Heals are worth more the more the caster needs them, and less when most of the healing would be wasted.
- Damage only counts the targets within reach.
- Summons are worth the damage the summon will deal to the enemy within reach of the caster nearest to its tile:
  - The **summon damage** is the expected amount of the best damaging ability of the summon's unit.
  - The summon acts in the player turn it is summoned in, after the battle units already playing it, and walks and casts in the same player turn, so the **player turns to damage** are `max(1, ⌈(distance to the enemy - 1) / its movement range⌉)`, counting the tiles apart. Hence, the tile nearest to the enemy is preferred.
  - A summon without a damaging ability, or without an enemy within reach, is worth 0.
- The best damage, heal or summon is cast when its utility is higher than `MINIMUM_UTILITY_GAIN`. Otherwise, one of the other abilities is cast at random, if any.

For example, a rat (20 HP, sword dealing 10 and heal restoring 20) next to a knight:

| Rat HP | Knight HP | Damage           | Heal              | Decision |
|--------|-----------|------------------|-------------------|----------|
| 20/20  | 100/100   | 1.0 × 0.1 = 0.1  | 0                 | Damage   |
| 15/20  | 100/100   | 0.75 × 0.1 = 0.075 | 0.25 × 0.25 = 0.0625 | Damage |
| 10/20  | 100/100   | 0.5 × 0.1 = 0.05 | 0.5 × 0.5 = 0.25  | Heal     |
| 6/20   | 10/100    | 0.3 × 1.0 = 0.3  | 0.7 × 0.7 = 0.49  | Heal     |
| 20/20  | 8/100     | 1.0 × 1.0 = 1.0  | 0                 | Damage   |

For example, a goat (30 HP, summoning a bat whose sword deals 10 and walks 5 tiles per player turn) within reach of a knight (100 HP). The bat damages the knight in its first player turn from the tile next to the goat:

| Goat HP | Summon                  | Heal               | Decision |
|---------|-------------------------|--------------------|----------|
| 30/30   | 1.0 × 0.9 × 0.1 = 0.09  | 0                  | Summon   |
| 15/30   | 0.5 × 0.9 × 0.1 = 0.045 | 0.5 × 0.75 = 0.375 | Heal     |

## Walking

Every tile the unit can walk to, plus its current tile, is an option. Its utility adds up three contributions:

- **Enemies**: how close the nearest enemy within reach is (normalised between the options), weighted by the unit's health ratio. Healthy units approach the enemies within reach, and add nothing without any.
- **Allies**: how close the nearest ally is (normalised between the options), weighted by `1 - health ratio`. Hurt units fall back towards allies.
- **Terrain**: the health effect of the tile terrain relative to the battle unit maximum health (equipment included), weighted by its healing need. Hurt units look for healing terrain (e.g.: water) and avoid harmful terrain (e.g.: lava).

The unit walks to the best tile, unless it is worth no more than `MINIMUM_UTILITY_GAIN` above staying still.

## Wandering

While the human player explores, the CPU battle units wander around their **home**: the tile each one was deployed on. The summons (e.g.: a bat deployed by a goat) have none, as they never wander (see [Summons](#summons)). Their **territory** is the occupiable tiles walked to from home in `WANDER_DISTANCE` (3) tiles or fewer, without stepping on harmful terrain (e.g.: lava), as they never step on it while they wander. Only the terrain bounds it (e.g.: void), not the battle units, so it does not change as they walk around.

The [`cpuWanderBrain`](../../src/commonMain/kotlin/com/mkz/rpg/cpuWanderBrain) subdomain remembers the homes, and walks the CPU battle units every second, while the CPU battle brain decides where each one steps:

1. [`ClaimHome`](../../src/commonMain/kotlin/com/mkz/rpg/cpuWanderBrain/usecases/commands/ClaimHome.kt) remembers the tile of every CPU battle unit deployed (`BattleUnitDeployed`), but the summons, with how far each tile is from it ([`SearchDistancesFromHome`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBattleBrain/usecases/queries/SearchDistancesFromHome.kt)). They are computed once, as the terrain never changes while playing.
2. Every second while exploring (`ExplorationSecondStarted`), [`WanderWhileExploring`](../../src/commonMain/kotlin/com/mkz/rpg/cpuWanderBrain/usecases/commands/WanderWhileExploring.kt) lets each CPU battle unit [in sight](#in-sight) rest or take one step, one after another, in their deployment order.
3. [`WhereShouldWander`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBattleBrain/usecases/queries/WhereShouldWander.kt) decides it, from the distances from home the CPU wander brain remembers. Each step is requested with `RequestWalkBattleUnitOutOfBattle`, as the party steps, so it spends no action.
4. Each step is awaited until its battle unit occupies its tile (`TileOccupied`, see [`ResolveWanderingStep`](../../src/commonMain/kotlin/com/mkz/rpg/cpuWanderBrain/usecases/commands/ResolveWanderingStep.kt)), so the next battle unit decides with the battlefield as it is then: two battle units never step on the same tile.
5. Once every CPU battle unit has wandered (`CpuBattleUnitsWanderedWhileExploring`), the CPU wander brain requests them to engage (`RequestEngageEnemies`), and [`EngageEnemies`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBattleBrain/usecases/commands/EngageEnemies.kt) engages the party when a CPU battle unit wandered within its reach (`EnemiesEngaged`), and requests the battle start (`RequestStartBattle`).

Within its territory, a CPU battle unit rests at random (`REST_CHANCE`, 0.5), but on harmful terrain. Otherwise, every vacant adjacent tile that is not harmful (e.g.: lava) and within its territory, or nearer to home, is an option. It steps to one of them at random, each one as likely as its utility, which adds up three contributions:

- **Roam**: how much further from home the tile is (normalised between the options and the current tile), weighted by `1 - distance from home / WANDER_DISTANCE`. Near home, the battle unit walks away from it.
- **Home**: how much nearer to home the tile is (normalised the same way), weighted by `distance from home / WANDER_DISTANCE`. Far from home, the battle unit walks back to it.
- **Terrain**: the health effect of the tile terrain relative to the battle unit maximum health, weighted by its healing need, as when [walking](#walking). Hurt battle units prefer healing terrain (e.g.: water).

The distance from home is the walking distance through the battle units, around void and harmful terrain ([`SearchDistancesFromHome`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBattleBrain/usecases/queries/SearchDistancesFromHome.kt)). As it only walks over the tiles a wandering battle unit can step on, every tile away from home has a tile next to it nearer to home, so a battle unit heading home never stops in front of harmful terrain: it walks around it. The harmful tiles are counted from the nearest walked tile next to them, without walking through them, so a battle unit standing on one (e.g.: after a chase) knows how far it is from home. Picking at random, rather than the best tile, lets the battle unit reach the edge of its territory now and then, instead of going back and forth between the same two tiles. For example, on open sand:

| Distance from home | Steps (utility)                                          |
|--------------------|----------------------------------------------------------|
| 0 (home)           | Every tile next to it (1 each): it walks away in any direction |
| 1                  | Further (0.67), along (0.5), back home (0.33)            |
| 2                  | Back (0.67), along (0.5), further, to the edge (0.33)    |
| 3 (edge)           | Back (1), along the edge (0): it always walks back       |

A CPU battle unit outside its territory (e.g.: after chasing the party in a battle, as a battle unit in the battle is not tied to its home) **returns** home ([`IsOutsideTerritory`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBattleBrain/usecases/queries/IsOutsideTerritory.kt)): it never rests, and only steps to the tiles nearer to home, picking one of them at random, all the way to its home tile, through its territory. The CPU wander brain remembers the returning battle units, as the CPU battle brain only decides one step at a time, and stops once one stands at home, where it wanders as before. It stays when home can not be walked to, e.g.: walled off by void, or only through harmful terrain, or while another battle unit stands on the only way home.

On every wandering, whether they rest, step or are out of sight, the CPU battle units recover `WANDERING_RECOVERY_PERCENTAGE` (10%) of their maximum health and mana, the equipment included, rounded up and up to the maximums: a rat (20 health, 10 mana) recovers 2 health and 1 mana every second. The CPU wander brain requests it to the battle units (`RequestRecoverBattleUnits`, see [`RecoverBattleUnits`](../../src/commonMain/kotlin/com/mkz/rpg/battleUnit/usecases/commands/RecoverBattleUnits.kt)). A defeated battle unit does not recover, as it stays stored with no health. The CPU battle units out of a battle also recover once per round, on the wandering after it, and the ones in the battle do not. The recovery is not displayed.

When a round finishes (`RoundFinished`), the CPU battle units out of the battle in sight wander once too, as every second while exploring ([`WanderAfterRound`](../../src/commonMain/kotlin/com/mkz/rpg/cpuWanderBrain/usecases/commands/WanderAfterRound.kt)). Once they have (`CpuBattleUnitsWanderedAfterRound`), the CPU wander brain requests it (`RequestJoinAndLeaveBattle`), and [`JoinAndLeaveBattle`](../../src/commonMain/kotlin/com/mkz/rpg/cpuBattleBrain/usecases/commands/JoinAndLeaveBattle.kt) has the CPU battle units join and leave the battle (`CpuBattleUnitsJoinedAndLeft`), and requests the next round (`RequestStartNextRound`), so a CPU battle unit that wandered within reach of the party joins the battle from the next round. It is announced also when no CPU battle unit is out of the battle, as the next round starts on it. The CPU battle units in the battle do not wander: they walk as in any battle, not tied to their home.

### In sight

Only the CPU battle units the player may see wander: those around the party leader, on the tiles the camera can show, and 1 tile around them (`SIGHT_MARGIN`), so the ones about to come into view are wandering already ([`IsInSight`](../../src/commonMain/kotlin/com/mkz/rpg/cpuWanderBrain/usecases/queries/IsInSight.kt)). The camera shows 6x6 tiles (`VIEW_TILES`, as `BattlefieldView.VISIBLE_TILES`, since the domain does not depend on the screen), and keeps the leader in its 2x2 centre, so on each axis it can show from 3 tiles before to 3 tiles after the leader: 7 tiles, 9 with the margin. Near a border, the camera stops scrolling, so the area shifts to stay inside the battlefield. For example, on a battlefield of 40 rows and 24 columns, with the leader on row 10 and column 12, the rows 6 to 14 and the columns 8 to 16 are in sight. With the leader on column 1, the columns -1 to 7 are, as the camera does not scroll left of the first column.

The others stay put, including the ones returning home after a chase, as nobody sees them, and deciding where each one steps takes time: on the web build, wandering all of them every second (13 in the demo) froze the game for about half a second. They still recover, and still engage the party when it walks within their reach. Without a party leader on the battlefield, none wanders.

### Respawning

A defeated CPU battle unit with a home comes back to it once the party has walked `RESPAWN_STEPS` (50) steps since its defeat, whether it was defeated in a battle or by its terrain while exploring. The summons have no home, so they never respawn.

1. [`AwaitRespawn`](../../src/commonMain/kotlin/com/mkz/rpg/cpuWanderBrain/usecases/commands/AwaitRespawn.kt) starts counting the party steps of the battle unit when it is defeated (`BattleUnitDefeated`).
2. Every time the party walks a step (`PartyWalked`), [`RespawnDefeatedBattleUnits`](../../src/commonMain/kotlin/com/mkz/rpg/cpuWanderBrain/usecases/commands/RespawnDefeatedBattleUnits.kt) counts it for every defeated battle unit, and requests the respawn of those that have counted enough steps on their home (`RequestRespawnBattleUnit`). There are no steps while battling, so the battle units only count while the party explores.
3. While its home is occupied (e.g.: by a battle unit of the party), the battle unit waits: it is requested again on the next step, and respawns on the first one its home is free.
4. [`RespawnBattleUnit`](../../src/commonMain/kotlin/com/mkz/rpg/battleUnit/usecases/commands/RespawnBattleUnit.kt) brings it back as it was deployed: full health and mana, no cooldowns, no ongoing effects, with the same id. It announces `BattleUnitRespawned`, followed by `RequestOccupyTile`, so the battlefield occupies its home and the screen displays it, and [`ResolveRespawn`](../../src/commonMain/kotlin/com/mkz/rpg/cpuWanderBrain/usecases/commands/ResolveRespawn.kt) stops counting its steps.

A respawned battle unit wanders around its home from the next second, and drops its loot again when it is defeated again. It is not deployed again (`BattleUnitDeployed`), so it never joins a battle on its own: a battle engaged on the step it respawns on is played without it, and it joins it as any other CPU battle unit out of the battle, when the CPU battle units join and leave the battle with the party within its reach. A battle unit of the party within its reach when it respawns is engaged on the next check, when the CPU battle units wander or the party walks again.

The game is over once every CPU battle unit is defeated at the same time, before the party walks any step, so none respawns then.
