# The CPU decides with a greedy utility AI

The CPU plays its battle units following the utility AI pattern: every option (each ability on each target, each tile
to walk to) is scored between 0 and 1 from a few considerations, such as the health of the caster and of its targets,
the enemies within reach and the terrain, and the best one is taken when it beats doing nothing by more than
`MINIMUM_UTILITY_GAIN`. The decisions are greedy, made one at a time, each on the state the previous one left. We chose
it because it blends those considerations into one comparable number, easy to tune and to extend per kind of ability,
and cheap enough for the web build.

The CPU battle units out of a battle wander the same way, one after another, each step awaited until its battle unit
occupies the tile, so two never step on the same tile. Their steps are scored too (roaming away from home, heading back
to it, and the terrain), but one is picked at random, weighted by its utility, rather than the best one, so they reach the
edge of their territory now and then, instead of going back and forth between the same two tiles.

## Considered options

- Behaviour trees: every situation would need a branch written by hand.
- Searching ahead (e.g.: minimax): too costly for the web build.

## Consequences

- A battle unit never plans ahead, e.g.: it damages an adjacent healthy enemy rather than walk next to a weak one and
  finish it off.
