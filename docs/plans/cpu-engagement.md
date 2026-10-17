# Plan: CPU battle units engage only nearby enemies

> **Status:** implemented. The reach distance was raised from 3 to 5 tiles afterwards.
>
> **Summary:** A CPU battle unit approaches and casts at the human battle units only when one of them can be walked to
> in 3 tiles or fewer. Otherwise, it ignores them: it does not walk towards them, attack them, deploy battle units
> against them or cast its other abilities (e.g.: teleport).

## Goals

Today, the CPU battle units head for the human battle units from anywhere on the battlefield, as soon as the battle
starts. Instead, a CPU battle unit engages a human battle unit only when it is **within reach**: the human battle unit
can be walked to in 3 tiles or fewer.

## Decisions

| Topic            | Decision                                                                                                                                                                                                          |
|------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Within reach     | A human battle unit is within reach when the CPU battle unit walks at most 3 tiles to its tile, counting its tile: next to it is 1, two tiles away with a free tile between them is 2.                         |
| Walkable         | The walk only steps on tiles the CPU battle unit could stand on: other battle units and terrains that can not be occupied (e.g.: void) block it. Terrains that can be occupied count, also when they hurt (e.g.: lava). |
| Reach distance   | 3, in a constant of the CPU brain, `ENGAGEMENT_DISTANCE`, so it can be tuned.                                                                                                                                     |
| When it is known | At every decision, from the battlefield as it is then, as every other consideration of the CPU brain.                                                                                                            |
| Memory           | None: a human battle unit walking out of reach is no longer followed.                                                                                                                                            |
| Moving           | Only the enemies within reach attract the battle unit. The rest stays as it is: a hurt battle unit still falls back towards its allies and looks for healing terrain.                                          |
| Casting          | Attacks, deploys and the other abilities (e.g.: teleport) are only cast when an enemy is within reach, and attacks and deploys only consider the enemies within reach.                                          |
| Healing          | Unchanged: a hurt battle unit heals itself whether enemies are within reach or not, as it is about its own health.                                                                                             |

## Design

### Battlefield subdomain

A new query, `SearchWalkingDistance(fromRow, fromColumn, toRow, toColumn, maximumDistance)`, returns how many tiles are
walked from one tile to another, or `null` when it is further than `maximumDistance` or can not be reached.

- It steps only on tiles that can be occupied, as `SearchShortestPath`, except the destination, which may be occupied
  (e.g.: by the enemy). `SearchShortestPath` can not be reused, as it only reaches tiles that can be occupied.
- It stops searching beyond `maximumDistance`, so it stays cheap on large battlefields.

### CPU brain subdomain

A new query, `SearchEnemiesWithinReach(battleUnitId)`, returns the enemy battle units within `ENGAGEMENT_DISTANCE` of
the battle unit, with `SearchWalkingDistance`.

- `WhereShouldMove` measures the distance to the nearest enemy among the enemies within reach only. Without any, the
  enemies add nothing, so a healthy battle unit stays where it is.
- `WhatShouldCast`:
  - Without an enemy within reach, only heals are candidates.
  - Attacks only count the targets within reach. As every attack reaches adjacent enemies only, nothing changes today,
    but it would hold for an attack with a longer range.
  - Deploys are worth the attack of the deployed battle unit on the nearest enemy within reach of the caster.

## Impact

- On the default battlefield, the CPU battle units start about 10 tiles away from the knights, so they wait until the
  knights come within 3 tiles.
- The tests placing an enemy further than 3 tiles away expecting the CPU battle unit to approach it, deploy or teleport
  are updated, to place it within reach. Tests out of reach are added.
- [CPU brain](../architecture/cpu-brain.md) explains engaging.

## Implementation steps

Each step is tested before the next one.

1. Battlefield `SearchWalkingDistance`: next to the destination, through free tiles, around a blocked tile, blocked by
   battle units and void, through lava, further than the maximum distance, and unreachable.
2. CPU brain `SearchEnemiesWithinReach`: an enemy at 3 tiles is within reach, at 4 tiles or behind a wall is not.
3. `WhereShouldMove`: approaches an enemy within reach, stays when the only enemy is out of reach, still falls back
   towards allies and healing terrain when hurt.
4. `WhatShouldCast`: does not deploy or teleport without an enemy within reach, still heals, deploys against the
   enemy within reach.
5. The existing CPU brain and end-to-end tests, and the documentation.
