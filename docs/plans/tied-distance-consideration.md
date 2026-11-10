# Plan: A distance consideration that can't tell the options apart is worth 0

> **Status:** to do.
>
> **Summary:** When every option of a CPU battle unit is the same tiles apart from the nearest enemy, or from the
> nearest ally, `WhereShouldWalk` divides 0 by 0. Every utility becomes NaN, and the battle unit walks to the first
> tile `WhereCanWalk` returns. E.g.: a battle unit standing between two enemies, in the row `[enemy, unit, sand,
> enemy]`, steps onto the sand for nothing, as it is next to an enemy wherever it stands. This happens in an ordinary
> melee. Instead, a consideration that can't tell the options apart is worth 0 for every option, as when no enemy or
> ally is deployed. The other considerations decide, and the battle unit stays when no option gains more than
> `MINIMUM_UTILITY_GAIN`. It fixes the greedy utility AI of [ADR 0012](../adr/0012-greedy-utility-ai-for-cpu-battle-units.md).

## The bug

`WhereShouldWalk` normalises the tiles apart from the nearest enemy of every option between 0 and 1, from the smallest
to the largest. It computes `(maximum − distance) / (maximum − minimum)`, and does the same for the nearest ally. When
every option is the same tiles apart, the largest equals the smallest, and the division is 0 / 0:

1. `nearestEnemyDistanceNormalized` (or `nearestAllyDistanceNormalized`) is NaN for every option, and so is every
   `optionUtility`.
2. `maxBy` compares NaN with NaN as equal, so it keeps the first option: the first tile `WhereCanWalk` returns, as the
   current tile is added last.
3. `abs(NaN − NaN) <= MINIMUM_UTILITY_GAIN` is false, so the check meant to stop a walk with no gain lets it through.

A test with the row `[enemy, unit, sand, enemy]` returned the tile at column 2, instead of no walk. When the current
tile is the only option, `maxBy` keeps it and no walk follows, so that case was harmless.

## Decisions

| Topic        | Decision                                                                                                                                                                                                                                      |
|--------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope        | Both distance considerations of `WhereShouldWalk`: the nearest enemy and the nearest ally. `WhatShouldCast` and `WhereShouldWander` don't normalise this way, so they don't change.                                                           |
| Tied value   | When every option is the same tiles apart from the nearest enemy (or ally), that consideration is worth 0 for every option, as when none is deployed. Chosen over 1 for every option, which ranks the options the same but raises every utility by the preference, and over replacing the max−min normalisation (e.g. dividing by `REACH`), which is a larger change than the bug. |
| Architecture | Only `cpuBattleBrain.usecases.queries.WhereShouldWalk` changes.                                                                                                                                                                              |
| Tests        | Unit tests of `WhereShouldWalk`: one for a tied enemy distance, one for a tied ally distance, and one where the tie leaves the terrain to decide. Each one fails today.                                                                       |
| Out of scope | The max−min normalisation itself, and a general guard against NaN in the utilities.                                                                                                                                                         |

For example, a battle unit at 10 of 100 health, at the centre of a 3 × 3 battlefield, with an enemy on every tile
but the ones north and south of it, and healing terrain (healing 10) to the south:

| Option        | Tiles apart from the nearest enemy | Enemy consideration today | Enemy consideration once built | Terrain contribution | Utility once built |
|---------------|------------------------------------|---------------------------|--------------------------------|----------------------|--------------------|
| North (sand)  | 1                                  | NaN                       | 0                              | 0                    | 0                  |
| South (heal)  | 1                                  | NaN                       | 0                              | 0.9 × 10 / 100       | 0.09               |
| Stay (sand)   | 1                                  | NaN                       | 0                              | 0                    | 0                  |

Today it walks north, the first tile `WhereCanWalk` returns. Once built, it walks south: 0.09 beats staying by more
than `MINIMUM_UTILITY_GAIN` (0.05).

## WhereShouldWalk

- `invoke(battleUnitId)` sets `nearestEnemyDistanceNormalized` to 0.0 for every option when `maximumEnemyDistance`
  equals `minimumEnemyDistance`, instead of dividing. E.g.: the row `[enemy, unit, sand, enemy]` gives both options
  0.0, and the battle unit stays.
- It does the same for `nearestAllyDistanceNormalized` when `maximumAllyDistance` equals `minimumAllyDistance`.

## Tests

Following [How to add a new test](../testing/how-to-add-a-new-test.md): the tests of a query aren't prefixed, the
enemy and ally tests draw their battlefield with `BattlefieldMother.battlefield(map, occupants)`, and the terrain test
needs healing terrain, which the map can't draw. So `WhereShouldWalkTest` gains a private `givenBattlefield(terrainIds,
battleUnitPositions)` for a battlefield of several rows, and `givenBattlefieldRow` calls it.

- `WhereShouldWalkTest`
  - `should not walk when every option is the same tiles apart from the nearest enemy`: a healthy battle unit on the
    map `E K . F`, with the enemies `E` and `F`.
  - `should not walk when every option is the same tiles apart from the nearest ally`: a battle unit at 1 of 10
    health on the map `A K . B`, with the allies `A` and `B` and no enemy.
  - `should walk to the healing terrain when every option is the same tiles apart from the nearest enemy and the
    battle unit has low health`: the 3 × 3 battlefield of the example, with the battle unit at 10 of 100 health, sand
    to the north and healing terrain to the south. It walks to the south tile.

## Known limitations

- The distances are still normalised against the other options, not against a fixed scale: a 1 tile difference
  between two options is worth as much as a 5 tile one.
