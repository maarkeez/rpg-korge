# Plan: CPU battle units wander around their home

> **Status:** implemented, the 3 milestones are done.
>
> **Summary:** A CPU battle unit that is not engaged in a battle wanders around the tile it was deployed on, its
> **home**, up to 3 walkable tiles away. Near home, it tends to walk away from it. Far from it, it tends to walk back.
> It picks its steps with the utility AI, as it picks its moves in a battle. A battle unit left far from home (e.g.:
> after chasing a party that ran away) walks back home first.

## Decisions

| Topic                | Decision                                                                                                                                                                                                                                  |
|----------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Home                 | The tile where the CPU battle unit was deployed, also during a battle (e.g.: a bat deployed by a goat). It never changes.                                                                                                                  |
| Territory            | The tiles walkable from home in `WANDER_DISTANCE` (3) tiles or fewer. Only the terrain counts (e.g.: void blocks the way), not the battle units, so the territory does not change as they walk around.                                       |
| Who wanders          | The CPU battle units out of a battle: all of them while exploring, and those out of the battle when a round finishes.                                                                                                                       |
| Pace while exploring | Every second, each CPU battle unit takes one step or rests. Inside its territory, it rests at random (`REST_CHANCE`, 0.5). Outside its territory, it never rests: it heads home.                                                            |
| Pace in a battle     | When a round finishes, each CPU battle unit out of the battle takes one step or rests, as every second while exploring. The engagement is updated afterwards, so a CPU battle unit can wander into reach of the party and join the battle. |
| Where it steps       | One tile, to a free tile next to it, within its territory. Outside its territory, only to a tile nearer to home.                                                                                                                          |
| Harmful terrain      | Never stepped on while wandering (e.g.: lava). Healing terrain (e.g.: water) is preferred when hurt, as in a battle.                                                                                                                       |
| Engaged              | Unchanged: an engaged CPU battle unit chases the party as today, not tied to its home. Once it leaves the battle and the party explores again, it heads home.                                                                             |
| Engaging             | A CPU battle unit that wanders into reach of the party engages it, as when the party walks into its reach.                                                                                                                                 |
| Animation            | While exploring, a wandering step plays alongside the timeline, so the party keeps walking smoothly, and it does not scroll the battlefield. When a round finishes, the steps are played in the timeline, before the next round starts.     |

## Utility

Every second (or round), a CPU battle unit that does not rest scores the tiles it can step to, and steps to one of them
at random, weighted by their utility. The distances are normalised between those tiles and its current tile. It does
not rest at random on harmful terrain, which it may stand on when it was deployed on it. The weighted pick lets it reach the edge of its territory
now and then, instead of going back and forth between the same two tiles. Its utility adds up three contributions:

- **Roam**: how much further from home the tile is (normalised between the candidates), weighted by
  `1 - distance from home / WANDER_DISTANCE`. Near home, the battle unit walks away from it.
- **Home**: how much nearer to home the tile is (normalised between the candidates), weighted by
  `distance from home / WANDER_DISTANCE`, capped at 1. Far from home, the battle unit walks back to it.
- **Terrain**: the health effect of the tile, weighted by its healing need, as `WhereShouldMove` does it.

The distance from home is the walking distance through the terrain. For example, at home, every tile next to it is
equally worth it, so it walks away in any direction. Two tiles away, it walks back more often than it walks away.
Three tiles away, it can only walk back or along the edge.

## wandering (new)

Owns the homes of the CPU battle units and walks them while they wander. It is a subdomain of its own, rather than part
of the CPU brain as first planned, as a subdomain holds a single aggregate, and `CpuBrain` only lives for a CPU turn.

### Domain

- Add `Wandering` because the homes must be remembered for the whole game, and the steps decided one after another: a
  step is only decided once the previous one occupies its tile, so two battle units never pick the same tile. It holds
  the home of each CPU battle unit, the CPU battle units left to wander, the awaited step, as `CpuBrain` and
  `Exploration` do, and why they wander: while exploring, or after a round (from milestone 3), with the battle units in
  the battle, so the engagement can be updated with them afterwards.
- Add the `WanderingEvent`s `BattleUnitsWanderedWhileExploring(exploringPlayerId)` and
  `BattleUnitsWanderedAfterRound(battleUnitIds)`, because engaging and updating the engagement follow them.

### Use cases

#### Commands

- Add `ClaimHome`: on `BattleUnitDeployed`, it remembers the tile of the CPU battle units as their home.
- Add `WanderWhileExploring`: every second while exploring, it lets every CPU battle unit with a home rest or step, one
  after another. A wandering in progress is abandoned.
- Add `WanderAfterRound` (milestone 3): when a round finishes, the same with the CPU battle units out of the battle.
- Add `ResolveWanderingStep`: on `BattlefieldTileOccupied`, it resolves the awaited step, so the next battle unit
  wanders.

#### Services

- Add `WanderingSteps`, which both share, as the exploration shares `PartyWalking`: the CPU brain decides whether the
  next battle unit rests or where it steps (`WhereShouldWander`), the resting ones are done at once, and the first step
  is requested with `BattleUnitEvent.RequestWalkBattleUnit`, as the party does. Once every battle unit has wandered, the
  aggregate publishes its event.

### Adapters

#### Presentation

- Add `WanderingApi`, which wires the subdomain as the other `*Api` classes do.

#### Storage

- Add `InMemoryWanderingRepository`.

#### Events

- Add `OnBattleUnitDeployed` (`ClaimHome`), `OnExplorationSecondStarted` (`WanderWhileExploring`) and
  `OnBattlefieldTileOccupied` (`ResolveWanderingStep`).
- Add `OnBattleRoundFinished` (`WanderAfterRound`, milestone 3).

## cpuBrain

### Domain

- Nothing changes.

### Use cases

#### Commands

- Modify `EngageEnemies` so it also engages on `BattleUnitsWanderedWhileExploring`.
- Modify `UpdateEngagement` (milestone 3) so it updates the engagement on `BattleUnitsWanderedAfterRound`, rather than
  straight on `BattleRoundFinished`, so the battle units that wandered into reach join the battle.

#### Queries

- Add `WhereShouldWander(battleUnitId, homeRow, homeColumn)`: the tile to step to, or `null` to rest, with the utility
  above. The home is passed in, so the CPU brain does not depend on the wandering. It takes a `Random`, as
  `WhatShouldCast` does, so the tests choose the outcome.
- Add `SearchTerrainHealthEffect(row, column)`, the health effect of the terrain of a tile, which `WhereShouldMove` and
  `WhereShouldWander` share.

#### Services

- Add `WANDER_DISTANCE` (3) and `REST_CHANCE` (0.5) to `Utility.kt`, next to `ENGAGEMENT_DISTANCE`, and
  `terrainHealthContribution`, which `WhereShouldMove` and `WhereShouldWander` share.

### Adapters

#### Presentation

- Modify `CpuBrainApi` so it wires the new queries, and takes the `Random`, so the end-to-end tests can make the CPU
  battle units rest. `startGame`, `MenuScene` and `BattleScene` pass it on.

#### Events

- Add `OnBattleUnitsWanderedWhileExploring` (`EngageEnemies`).
- Replace `OnBattleRoundFinished` with `OnBattleUnitsWanderedAfterRound` (`UpdateEngagement`, milestone 3).

## exploration

### Domain

- Add `ExplorationEvent.ExplorationSecondStarted(playerId)` because the CPU battle units wander every second while
  exploring, and the exploration already counts the seconds for the terrain effects.

### Use cases

#### Commands

- Modify `ApplyStandingTerrainEffects` so it publishes `ExplorationSecondStarted` when it starts the next second, after
  requesting the terrain effects. The battle units that wander then count as moved during the new second, so a
  wandering battle unit only receives the effect of its terrain once it rests on it for a whole second.

#### Queries

- Nothing changes.

### Adapters

- Nothing changes: `ExplorationPresenter` already calls `applyStandingTerrainEffects` every second.

## battlefield

### Domain

- Nothing changes.

### Use cases

#### Queries

- Modify `SearchWalkingDistance` so it can walk through the battle units, as the territory only depends on the
  terrain. Without it, a battle unit would block the walk from home to the tiles behind it, itself included.
- Add `CanBattlefieldTerrainBeOccupied`, for it: whether the terrain of a tile can be occupied, whoever stands on it.
- `CanBattlefieldTileBeOccupied`: reused as it is, for the tiles a battle unit can step to.

### Adapters

- Nothing changes.

## battleUnit

### Domain

- Nothing changes: `BattleUnitDeployed` already carries the tile, and `walk` already spends no turn action.

### Use cases

- Nothing changes: `WalkBattleUnit` walks the wandering steps as it walks the party steps.

### Adapters

- Nothing changes.

## screen

### Domain

- Nothing changes.

### Use cases

- Nothing changes.

### Adapters

#### Presentation

- Modify `BattleSceneTimeline` with `playAlongside(step)`: the step starts straight away, in parallel with the playing
  steps, and does not make the timeline playing, so the player can still act.
- Modify `BattlefieldPresenter` so it plays the CPU battle unit moves while exploring alongside the timeline, at the
  exploring pace (`EXPLORING_STEP_DURATION`). The moves after a round stay in the timeline.
- Modify `BattlefieldView.moveBattleUnit` so it can move a battle unit without following its tiles, as a wandering
  battle unit must not scroll the battlefield away from the party.

## Impact

- The tests that rely on the CPU battle units standing still while exploring make them rest with a `FixedRandom` in
  `SubdomainApis`, or with an end-to-end `CpuBrainApi`, e.g.: the rat standing on lava in
  `ExploringTerrainEffectAcceptanceTest`, and the rats the party walks to in `EngagementAcceptanceTest` and the
  end-to-end tests.
- The tests updating the engagement when a round finishes also go through the wandering.
- [CPU brain](../architecture/cpu-brain.md) explains wandering, and [Exploration](../architecture/exploration.md) no
  longer says that the CPU battle units stand still. [Presentation timeline](../architecture/presentation-timeline.md)
  explains the steps played alongside.

## Milestones

Each milestone is committed and pushed on its own, with its tests and documentation, and leaves the game playable.

| # | Done | Milestone                    | Changes                                                                                                                                                                                                                                                                                                                                                                   | The game after it                                                                                                     |
|---|------|------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------|
| 1 | ✅    | Wandering while exploring    | `wandering` subdomain: `Wandering`, `ClaimHome`, `WanderWhileExploring`, `ResolveWanderingStep`, `WanderingSteps`. `SearchWalkingDistance` through the battle units. `WhereShouldWander`, inside the territory only. `ExplorationSecondStarted`. `EngageEnemies` after wandering. `playAlongside`, and the moves without following the tiles. The end-to-end tests make the CPU battle units rest. | While exploring, the CPU battle units wander around their home, and engage the party when they wander into its reach. |
| 2 | ✅    | Back home                    | `WhereShouldWander` outside the territory: only towards home, never resting.                                                                                                                                                                                                                                                                                              | The CPU battle units left behind by a party that ran away walk back home once the party explores again.               |
| 3 | ✅    | Wandering when a round ends  | `WanderAfterRound` on `BattleRoundFinished` with the CPU battle units out of the battle, played in the timeline. `UpdateEngagement` on `BattleUnitsWanderedAfterRound`.                                                                                                                                                                                            | During a battle, the CPU battle units out of it wander once per round, and may wander into reach and join the battle.  |

Until milestone 2, a CPU battle unit outside its territory (e.g.: after a battle) had nowhere to step to, so it stood
still. From milestone 3, the acceptance tests that play rounds also wire the `WanderingApi`, as the engagement is only
updated once the CPU battle units out of the battle have wandered, and make them rest.

## Follow-ups

- **Around harmful terrain**: the distance from home first walked over harmful terrain, which a wandering battle unit
  never steps on. Outside its territory, a battle unit whose only tile nearer to home was harmful stopped in front of
  it for good (e.g.: below the lava strip of the battlefield). The distance from home now only walks over the tiles a
  wandering battle unit can step on (`SearchDistancesFromHome`), so it walks around the harmful terrain, and the
  territory leaves it out. `SearchWalkingDistance` no longer walks through the battle units, as nothing else needed it.
