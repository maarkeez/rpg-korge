# Plan: Rename the code of the cpuWanderBrain subdomain to the words of the glossary

> **Status:** to do.
>
> **Summary:** The code of the cpuWanderBrain subdomain gives Respawn a second meaning. In the
> [glossary](../../GLOSSARY.md), a defeated battle unit respawns when it comes back on the battlefield. E.g.: when the
> knight defeats a rat, `CpuWanderBrain.defeat` adds it to `Respawns` with 0 party steps, `toDto` lists a `RespawnDto`
> for it, and the save keeps it under `respawns`, though nothing has respawned: the rat is awaiting its respawn for 50
> party steps. Its events also say `BattleUnitsWanderedWhileExploring` while the cpuBattleBrain events say
> `CpuBattleUnitsJoinedAndLeft`, and the comments say the battle units recover "every time they wander", while a rat out
> of sight stays put and still recovers. Instead, the code says Awaiting respawn, CPU battle units and Wandering, as the
> glossary does. The game plays the same, and players read the same texts, but the saves of version 5 can no longer be
> loaded. Builds on [CPU battle brain](../architecture/cpu-battle-brain.md), [Save and load](../architecture/save-and-load.md)
> and the [cpuBattleBrain renames](cpuBattleBrain-renames.md).

## Decisions

| Topic            | Decision                                                                                                                                                                                                                                     |
|------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope            | The renames below, in the code, tests, comments and docs of the cpuWanderBrain subdomain, and the wandering part of [CPU battle brain](../architecture/cpu-battle-brain.md), which still uses words earlier sections avoid.                  |
| Awaiting respawn | The party steps a defeated CPU battle unit counts are an awaited respawn: `AwaitedRespawns`, `AwaitedRespawnDto`, `awaitRespawn()`, matching `AwaitRespawn`. Chosen over keeping `respawns`, which reads as the battle units that came back. |
| Wandering        | A wandering is one pass of the CPU battle units out of a battle, in which those in sight wander. They recover "on every wandering", in sight or not, for "every time they wander".                                                           |
| CPU battle units | The wandered events say `CpuBattleUnits`, as `CpuBattleUnitsJoinedAndLeft` does.                                                                                                                                                             |
| Words kept       | `ClaimHome`, `IsInSight`, `VIEW_TILES`, `SIGHT_MARGIN`, `RESPAWN_STEPS`, `dueRespawns`, `leaveTerritory`, `arriveHome`, `isReturning`, `pendingBattleUnits` and `WANDERING_RECOVERY_PERCENTAGE` already fit.                                 |
| Save format      | Reached: the saved `respawns` of the `cpuWanderBrain` snapshot is `awaitedRespawns`, so `SAVE_VERSION` goes from 5 to 6, with a new `v6.json` fixture. Chosen over keeping the saved key, so the save reads as the code.                     |
| Player text      | None changes, and no translation key or catalog changes: players read no word of this subdomain.                                                                                                                                             |
| Tests            | No new tests: a rename changes no behaviour. The tests of the subdomain are renamed with the code, and `SaveFixtureTest` gets the `v6.json` fixture.                                                                                         |
| Out of scope     | The missing subscribers, and the words of the party, player and screen subdomains. See [Not in this plan](#not-in-this-plan).                                                                                                                |

## Renames

| Today                                                   | Glossary term    | New name                                                   |
|---------------------------------------------------------|------------------|------------------------------------------------------------|
| `CpuWanderBrain.Respawns`, `respawns`                   | Awaiting respawn | `CpuWanderBrain.AwaitedRespawns`, `awaitedRespawns`        |
| `CpuWanderBrain.defeat`                                 | Awaiting respawn | `CpuWanderBrain.awaitRespawn`                              |
| `CpuWanderBrain.Dto.respawns`                           | Awaiting respawn | `CpuWanderBrain.Dto.awaitedRespawns`                       |
| `CpuWanderBrain.Dto.RespawnDto`                         | Awaiting respawn | `CpuWanderBrain.Dto.AwaitedRespawnDto`                     |
| `CpuWanderBrain.Dto.SnapshotDto.respawns`               | Awaiting respawn | `CpuWanderBrain.Dto.SnapshotDto.awaitedRespawns`           |
| `CpuWanderBrainSnapshot.respawns` (saved)               | Awaiting respawn | `CpuWanderBrainSnapshot.awaitedRespawns` (saved)           |
| `CpuWanderBrainEvent.BattleUnitsWanderedWhileExploring` | CPU battle unit  | `CpuWanderBrainEvent.CpuBattleUnitsWanderedWhileExploring` |
| `CpuWanderBrainEvent.BattleUnitsWanderedAfterRound`     | CPU battle unit  | `CpuWanderBrainEvent.CpuBattleUnitsWanderedAfterRound`     |

## cpuWanderBrain

### Domain

- `CpuWanderBrain.awaitRespawn` starts counting the party steps of a defeated battle unit with a home, in its
  `AwaitedRespawns`. `walkPartyStep`, `dueRespawns` and `respawn` read and clear them. Its comments say "awaited
  respawns" for "respawns", e.g.: "What is kept when the game is saved: the homes, the battle units returning home and the
  awaited respawns".
- `CpuWanderBrain.finishWhenDone` publishes `CpuBattleUnitsWanderedWhileExploring` and `CpuBattleUnitsWanderedAfterRound`,
  which keep their fields.

### Use cases

- `AwaitRespawn` calls `awaitRespawn`.
- `WanderWhileExploring` and `WanderAfterRound` say the CPU battle units recover on every wandering, in sight or not.
  `WANDERING_RECOVERY_PERCENTAGE`'s comment says "on every wandering" for "every time they wander".
- `ClaimHome`'s comment says the summons "vanish once their summon duration is over", for "once their turns are over".
- `SearchCpuWanderBrainSnapshot`'s comment says "the awaited respawns".

### Adapters

- `CpuWanderBrainSnapshottable` saves `awaitedRespawns`, the party steps counted by each battle unit awaiting its respawn,
  and restores them into `AwaitedRespawnDto`. Its comments say so.

## save

- `Save.SAVE_VERSION` is 6, and `src/jvmTest/fixtures/save/v6.json` is written with
  `./gradlew jvmTest --tests '*SaveFixtureTest*' -Drpg.updateSaveFixtures=true`, keeping `v1.json` to `v5.json`.

## cpuBattleBrain

- `WhereShouldWander`'s comment says "the vacant adjacent tiles" for "the free tiles next to it", as the battlefield
  section avoids "free" and "next to".

## Docs

- [CPU battle brain](../architecture/cpu-battle-brain.md):
  - `CpuBattleUnitsWanderedWhileExploring` and `CpuBattleUnitsWanderedAfterRound`.
  - "On every wandering, whether they rest, step or are out of sight, the CPU battle units recover", for "Every time they
    wander".
  - "every vacant adjacent tile" for "every free tile next to it", "spends no action" for "spends no turn action", and
    "the battle unit maximum health" for "max health", in the wandering part and in Walking.
  - "A summoned battle unit lasts its summon duration" for "lasts only some turns", and "once its summon duration is over"
    for "once its turns are over", in Summons.
- [Save and load](../architecture/save-and-load.md): the `cpuWanderBrain` row keeps "The homes, the battle units returning
  home, and the awaited respawns".
- `docs/architecture/dependencies.html` is regenerated by the pre-commit hook.

## Tests

- `CpuWanderBrainTest`
    - `AwaitRespawn should start counting the party steps of a battle unit with a home` (replaces `Defeat should start
      counting the party steps of a battle unit with a home`)
    - `AwaitRespawn should ignore a battle unit without a home` (replaces `Defeat should ignore a battle unit without a
      home`)
    - The tests on the wandered events expect `CpuBattleUnitsWanderedWhileExploring` and `CpuBattleUnitsWanderedAfterRound`,
      and the ones on the awaited respawns read `toDto().awaitedRespawns`.
- `AwaitRespawnTest`, `OnBattleUnitDefeatedTest`, `OnPartyWalkedTest`, `RespawnDefeatedBattleUnitsTest`,
  `SearchCpuWanderBrainSnapshotTest`, `RestoreCpuWanderBrainTest` and `CpuWanderBrainSnapshottableTest` read
  `awaitedRespawns` and `AwaitedRespawnDto`.
- `OnTileOccupiedTest`, `OnRoundFinishedTest`, `ResolveWanderingStepTest`, `WanderWhileExploringTest` and
  `WanderAfterRoundTest` subscribe to `CpuBattleUnitsWanderedWhileExploring` and `CpuBattleUnitsWanderedAfterRound`.
- `CpuWanderBrainMother` builds `AwaitedRespawnDto`.
- `SaveFixtureTest` loads `v6.json`.

## Not in this plan

- Nothing subscribes to `BattleUnitsWanderedWhileExploring` (`CpuBattleUnitsWanderedWhileExploring`) nor
  `BattleUnitsWanderedAfterRound` (`CpuBattleUnitsWanderedAfterRound`): they are published, and only the tests read them,
  as the requests published right after them (`RequestEngageEnemies` and `RequestJoinAndLeaveBattle`) do the work. Whether
  they should stay is still to be decided.

### Waiting for other sections

- `party`: the party steps a defeated CPU battle unit counts (`walkPartyStep`, `partySteps`, `RESPAWN_STEPS`), against the
  battle unit's Step. The party leader In sight is measured around. Exploring, in `WanderWhileExploring`,
  `startWhileExploring`, `Purpose.Exploring` and `exploringPlayerId`.
- `player`: CPU player and CPU battle unit are still to be defined, as every term of this section uses them.
- `screen`: `VIEW_TILES` mirrors `BattlefieldView.VISIBLE_TILES`. The camera and what it shows are still to be defined.
