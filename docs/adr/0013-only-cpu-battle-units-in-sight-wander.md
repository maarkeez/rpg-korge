# Only the CPU battle units in sight wander

While the party explores, only the CPU battle units the player may see wander: those on the tiles the camera can show
around the leader, and 1 tile around them (`SIGHT_MARGIN`), so the ones about to come into view are wandering already.
The others stay put, though they still recover and still engage the party when it walks within their reach. We chose
this because deciding where each one steps takes time: wandering all of them every second (13 in the demo) froze the
web build for about half a second.

## Consequences

- `cpuWanderBrain` keeps its own copy of the 6 tiles the camera shows (`VISIBLE_TILES`), as the domain does not depend
  on the screen, so it must change together with `BattlefieldView.VISIBLE_TILES`.
