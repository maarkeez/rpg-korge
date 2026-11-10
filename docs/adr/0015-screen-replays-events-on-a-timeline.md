# The screen replays the game's events on a timeline

The game resolves its events at once, e.g.: a whole CPU turn within one frame, while drawing some of them takes time,
e.g.: a battle unit walking tile by tile. So each presenter reads what it needs when an event arrives, and adds a step
to `GameSceneTimeline` that draws it once the steps before it have been played. While the timeline plays, the player
can not act on the scene. We chose this over making the game wait for the screen, which would tie the domain to the
animations, and over drawing the state as it is when a step plays, which would show the end of the CPU turn before its
first walk. It is hard to reverse, as every presenter draws through the timeline.

## Consequences

- A step draws the state read when its event arrived, not the current one, so a presenter searches what it needs
  before adding the step, e.g.: the unit of a battle unit to display, which may be defeated by the time it is drawn.
- What should not hold the player plays alongside the timeline, e.g.: a CPU battle unit wandering while the party
  walks, and what plays with the step being played joins it, e.g.: the followers of the party gliding with its leader.
