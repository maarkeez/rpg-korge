# Plan: Exploring without freezes

> **Status:** implemented.
>
> **Summary:** In the browser, while exploring, the whole game freezes for about half a second every 1.4 seconds. Every
> second, each CPU battle unit computes how far every tile it can reach is from its home, and checks each of those tiles'
> terrain by copying the whole battlefield (960 tiles): about 6 million tile copies in a single frame. Instead, a tile's
> terrain is read without copying the battlefield, each CPU battle unit's distances from home are computed once, when it
> claims its home, and only the CPU battle units the player sees, around the party leader, wander. Besides, the virtual
> pad hides as soon as the leader lands when a battle starts, the event bus no longer logs every event, and a script
> measures the frame times of the web build in Chrome, to catch such freezes.

## What was measured

The web build (`jsBrowserProductionWebpack`), played in Chrome (Apple M2 Pro, Metal) by a script holding the virtual pad
arrows, recording every frame (`requestAnimationFrame`) and sampling the CPU profile (Chrome DevTools Protocol):

| Build                                         | Frames over 50ms                     | Longest frame |
|-----------------------------------------------|--------------------------------------|---------------|
| Today                                         | One every ~1.45s while exploring      | 583ms         |
| Prototype: a tile's terrain read in place     | None, on two walks of 14s each        | 16.8ms        |
| Implemented (`tools/frame-times`, 15s walk)   | None (16 on the previous build)       | 16.8ms (550ms) |

In the frozen frames, ~95% of the time is spent under `WanderWhileExploring`:

1. `ExplorationSecondStarted` (every second) → `WanderWhileExploring` → `WanderingSteps.continueWandering`, for each CPU
   battle unit with a home (13 in the demo).
2. `WhereShouldWander` and `IsOutsideTerritory` → `SearchDistancesFromHome`: a breadth-first search from home over every
   tile the battle unit can walk to (~500 in the demo). It can not stop at the territory, as a battle unit far from home
   (e.g.: after chasing the party) needs its way back.
3. For each tile walked, `SearchTerrainHealthEffect` → `SearchBattlefield` → `Battlefield.toDto()`: a copy of the 960
   tiles, to read the terrain of one.

The headless tests did not show it: on the JVM it takes a few milliseconds, and the frames are simulated. The other
suspects were measured and ruled out: the party walk (< 1ms), the per second terrain effects (< 1ms), the camera and the
tile window (< 2ms per frame while walking), the CPU battle units wandering in parallel (`playAlongside`, never waited
for).

## Decisions

| Topic                | Decision                                                                                                                                                                                                                       |
|----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Terrain lookup       | A battlefield query reads the terrain of one tile (`SearchTerrainId`), without copying the battlefield. `SearchTerrainHealthEffect` uses it.                                                                                     |
| Distances from home  | Computed once, when a CPU battle unit claims its home (`ClaimHome`, on deployment), and kept with its home in the `CpuWanderBrain`. The terrain never changes while playing. `WhereShouldWander` and `IsOutsideTerritory` take them as input. |
| Who wanders          | Only the CPU battle units in sight: around the party leader, the tiles the camera can show, and 1 tile around them. The rest stay put, including the ones heading home after a chase. They still engage the party when it walks within their reach, as today. |
| In sight             | The camera shows 8x8 tiles, the leader in its 2x2 centre, so 4 tiles before to 4 after the leader on each axis (9 tiles). Near a border, the camera stops scrolling, so the area shifts to stay inside the battlefield. Plus 1 tile around: 11x11 tiles. |
| When                 | While exploring (every second) and after a battle round, the same rule. With no leader (e.g.: no party), none wanders.                                                                                                          |
| Recovery             | Unchanged: every CPU battle unit with a home recovers 10% every second while exploring, and every one out of the battle after a round, in sight or not.                                                                          |
| Leader               | The wander brain asks the party for its leader (`PartyApi.searchParty`), and the battlefield for its position. `BattleScene` creates the `PartyApi` before the `CpuWanderBrainApi`, which already depends on the party's events. |
| Battle start         | The virtual pad hides as soon as the leader lands on its last tile, before "Battle!" pops. Today it hides about 950ms later, after the battle and the CPU turn are announced, so the party looks frozen.                            |
| Event logs           | The event bus no longer prints every event (`[EVENT] Queued`, `[EVENT] Dispatched`): about 125 lines a second while walking.                                                                                                   |
| Frame-time script    | Committed as a development tool, run by hand after gameplay changes: it builds the web version, plays it in Chrome holding the pad, and reports the frames over 50ms.                                                             |
| Loot pick-up         | Unchanged: the party still waits for the item to rise (400ms).                                                                                                                                                                 |

For example, on a battlefield of 40 rows and 24 columns, the leader on row 10, column 12, the area in sight is rows 5..15 and columns 7..17.
On column 1, near the left border, the camera does not scroll left, so it is columns -1..9 (column -1 being out of the
battlefield).

## Battlefield

- `Battlefield.terrainId(row, column): String?`, from its tiles, without converting them.
- `SearchTerrainId(row, column)`: the terrain of the tile, or `null` out of the battlefield.
- `SearchBattlefieldSize()`: its rows and columns (`Battlefield.Dto.SizeDto`), without converting its tiles.
- `BattlefieldApi` exposes both.

## CPU battle brain

- `SearchTerrainHealthEffect` takes `SearchTerrainId` in place of `SearchBattlefield`.
- `WhereShouldWander(battleUnitId, distancesFromHome, isReturning)` and `IsOutsideTerritory(battleUnitId,
  distancesFromHome)`: the distances from home are given, in place of the home's row and column. Home is the tile at
  distance 0.
- `SearchDistancesFromHome` is unchanged, and only called when a home is claimed.

## CPU wander brain

- `CpuWanderBrain.claimHome(battleUnitId, row, column, distancesFromHome)`: the home keeps its distances, and
  `CpuWanderBrain.distancesFromHome(battleUnitId)` returns them (none without a home). `HomeDto` is unchanged, so the
  homes compared in the tests do not carry them.
- `ClaimHome` computes them with `SearchDistancesFromHome`, but for a battle unit that already has a home, as a home
  never changes.
- `WanderingSteps` passes the home's distances to `WhereShouldWander` and `IsOutsideTerritory`.
- `IsInSight(battleUnitId)` (a query): whether the battle unit stands in the area in sight around the party leader, as
  above, from `PartyApi.searchParty`, `SearchPosition` and `SearchBattlefieldSize`. The sizes are its own constants
  (`VIEW_TILES = 8`, `SIGHT_MARGIN = 1`), mirroring `BattlefieldView.VISIBLE_TILES`, as the domain does not depend on
  the screen.
- `WanderWhileExploring`: recovers every battle unit with a home, as today, and wanders the ones in sight.
- `WanderAfterRound`: recovers every battle unit out of the battle, as today, and wanders the ones in sight. It is still
  announced when none wanders, as the next round starts on it. `CpuWanderBrain.startAfterRound(battleUnitIdsInBattle,
  battleUnitIds)` takes the battle units to wander, as `startWhileExploring` does.
- `CpuWanderBrainApi` takes the `PartyApi`.

## Screen

- `BattleSceneTimeline.playNext(step)`: plays the step right after the step being played, before the steps waiting.
  When the timeline is idle, it plays as `play`.
- `ExplorationPresenter`, on `BattleStarted`: `timeline.playNext { displayVirtualPad(isDisplayed = false) }`. The leader's
  last step is being played, and "Battle!" is waiting, so the pad hides as soon as the leader lands. On `ExplorationPaused`,
  it is still hidden, which does nothing more.

## Event bus

- `InMemoryEventBus` no longer prints the events.

## Frame-time script

- `tools/frame-times/`: `package.json` (`puppeteer-core`), `measure.mjs` and a static file server, so it needs Node 18 or
  newer (e.g.: the one Gradle downloads for Kotlin/JS) and Google Chrome, but no other install.
- `measure.mjs`: copies the web build (`build/processedResources/js/main` and
  `build/kotlin-webpack/js/productionExecutable`) to a temporary folder, serves it, opens it in Chrome at the game window
  size (390x844), plays a new game, walks the party one tile west and one tile east in turns for 15s, and prints the
  frame times (p50, p95, max) and every frame over 50ms. It exits with 1 when there is one. `CHROME` overrides the
  Chrome path.
- The walk stays on the first tiles of the demo battlefield, away from the CPU battle units, as a battle pauses the
  exploration, and so the wandering. Holding the arrows east and west for 600ms each walked the party within reach of the
  corridor bat now and then. After the walk, it checks the virtual pad is still displayed (the corner of its up arrow is
  blue): otherwise, a battle started, and it exits with 2, to measure again.
- The play button and the arrows are clicked at their position on the game window, so a layout change needs them updated.
- [How to measure the frame times](../testing/how-to-measure-the-frame-times.md): building the web version, running it,
  reading the report, and profiling a long frame (`--profile` writes a CPU profile to open in Chrome DevTools).

## Tests

`SearchTerrainIdTest`, `SearchBattlefieldSizeTest`, `BattlefieldTest`:

- The terrain of a tile, and `null` out of the battlefield.
- The rows and the columns.

`SearchTerrainHealthEffectTest`: unchanged results, with the new dependency.

`WhereShouldWanderTest`, `IsOutsideTerritoryTest`: take the distances from home (e.g.: from `SearchDistancesFromHome` in
the test setup), with unchanged results.

`ClaimHomeTest`, `CpuWanderBrainTest`:

- `ClaimHome should keep the distances from home of the battle unit when it claims its home`.
- `CpuWanderBrainMother` gives the homes the distances of an open field by default, and `withLeaderApart(map)` adds the
  party leader to a small map, walled off by void, so the wandering tests keep their maps with every battle unit in
  sight.

`IsInSightTest`:

- In sight when it stands within 5 tiles of the leader on each axis.
- Not in sight 6 tiles away.
- Near a border, the area shifts inside the battlefield: 7 tiles away from a leader on column 1 is in sight.
- Not in sight without a party leader, or when the leader is not on the battlefield.

`WanderWhileExploringTest`, `WanderAfterRoundTest`:

- `should only wander the battle units in sight of the party leader`.
- `should recover every battle unit with a home, in sight or not` (and, after a round, every one out of the battle).

`BattleSceneTimelineTest`:

- `PlayNext should play the step before the waiting steps once the step being played finishes`.
- `PlayNext should play the step straight away when the timeline is idle`.

`ExplorationPresenterTest`:

- `BattleStarted should hide the virtual pad before the steps waiting in the timeline`.

`InMemoryEventBusTest`: unchanged.

Acceptance: the tests creating the `CpuWanderBrainApi` create the `PartyApi` before it, as `BattleScene` does.

End-to-end: the existing tests run with the CPU battle units resting, so they are unchanged. The frame-time script is run
before and after, expecting no frame over 50ms after.

## Docs

- [CPU battle brain](../architecture/cpu-battle-brain.md#wandering): only the CPU battle units in sight of the party
  leader wander, and their distances from home are computed when they claim it.
- [Exploration](../architecture/exploration.md): the pad hides as soon as the leader lands when a battle starts.
- [Presentation timeline](../architecture/presentation-timeline.md): `playNext`.
- [How to measure the frame times](../testing/how-to-measure-the-frame-times.md), new, linked from
  [How to add a new test](../testing/how-to-add-a-new-test.md).
