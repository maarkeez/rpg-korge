# Plan: An open start room, guarded by a bat on its only gate

> **Status:** done.
>
> **Summary:** A new game starts in a corridor 1 tile high, where the knight can only walk west or east. It forces the
> first fight, against the corridor bat, but it feels restrictive. Instead, the party starts in a room of 4 rows and 11
> columns, which it leaves through a gate: a single sand tile with void north and south. The gate bat lives on it, so
> the first fight is still forced, and the rest of the map is unchanged.

## Decisions

| Topic          | Decision                                                                                                                                                                                                                                       |
|----------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope          | Only the resources, the end-to-end tests and the docs change. No production Kotlin changes.                                                                                                                                                    |
| Start room     | Rows 1 to 4, columns 1 to 11, where the start corridor was, so the battlefield keeps its 24 columns and 40 rows, and nothing else moves. The knight still starts at `(2,1)`.                                                                  |
| Gate           | `(2,12)`, between the start room and the 3x3 room at columns 13 to 15, which leads south to the meadow as before. Void north and south of it, so it is a corridor 1 tile long, ending in a room on each side (see [Map rules](#map-rules)).      |
| Gate bat       | `corridor-bat` is renamed `gate-bat`, and its home moves from `(2,10)` to the gate. It wanders as every bat does: no guard behaviour.                                                                                                           |
| Forced fight   | The bat wanders up to 3 tiles from the gate, and engages within 5, so stepping on the gate is always within its reach, wherever it wandered: the party can not slip past it.                                                                    |
| Safe start     | The start is 11 tiles from the gate, and the bat only wanders once it is in sight, when the leader reaches about column 7, as it engages. So the player walks around the west half of the room freely, and meets the bat waiting on the gate. |
| Room content   | Sand, with grass at `(3,3)` and `(3,8)` for the looks. Nothing heals before the first fight.                                                                                                                                                   |
| Camera         | The battlefield no longer needs the party to start within its first 8 rows and columns: the camera trails the leader from the start of the exploration (see [Trailing camera](trailing-camera.md)). The rule is dropped from Game data.          |

## Map

`K` the knight, `b` the gate bat. Terrain: `.` sand, `#` void, `g` grass.

```text
     012345678901234567890123
  0  ########################
  1  #...........#...########   Start room: rows 1 to 4, columns 1 to 11
  2  #K..........b...########   Gate: (2,12), the gate bat's home
  3  #..g....g...#...########
  4  #...........##.#########
  5  ##############.#########
  6  ########.............###   Meadow, unchanged
```

### Map rules

- The corners of the start room, `(1,1)`, `(1,11)`, `(4,1)` and `(4,11)`, have void on two sides that meet, so they can
  not be occupied: they are drawn as the edge of the void.
- The grass tiles are surrounded by sand, as only sand can be next to another terrain.
- The start room does not reach the west edge of the battlefield: column 0 is void, as row 0 is.

## Tests

The end-to-end tests walk to the first battle, so they follow the room ([`EndToEndExtension`](../../src/commonTest/kotlin/com/mkz/rpg/shared/adapters/e2e/EndToEndExtension.kt)):

- The knight walks east 6 tiles, from `(2,1)` to `(2,7)`, where the gate bat at `(2,12)` has it within reach, and walks
  to `(2,8)`, next to it. By default the CPU battle units always rest, so the bat waits on the gate.
- `MoveKnightEndToEndTest` and `CastAbilityEndToEndTest` count their tiles from the knight: the adjacent `(2,6)`, `(2,5)`
  2 tiles away, and `(2,3)` 4 tiles away, out of its movement range.
- `PartyMemberSwapAcceptanceTest` builds its own corridor, so it is unchanged.

## Known limitations

- Enemies respawn once the party has walked 50 steps, so the gate bat guards the gate again on the way back.
