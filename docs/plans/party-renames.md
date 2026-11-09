# Plan: Rename the code of the party subdomain to the words of the glossary

> **Status:** built on `main` in `b338a522`, its screenshots of the formation hints still to be approved.
>
> **Summary:** The code of the party subdomain uses words the [glossary](../../GLOSSARY.md) gives to other concepts.
> E.g.: the party keeps its walking order as a `Line`, while a line is what an npc says. A battle starting publishes
> `ExplorationPaused`, while pausing is what the player does from the pause screen (`PauseOpened`). And one press of an
> arrow is `WalkParty` and `PartyWalked`, while a walk is a battle unit going along a path, and the glossary already
> counts the party's moves in party steps (the 50 respawn steps of a defeated bat). Players also read "Tap two members to
> swap them" in the formation, while a swap is two occupants exchanging their tiles, and nobody moves there. So reading
> the code and talking about the game take a translation. Instead, the code, the save, the tests and the docs say
> walking order, interrupt and party step, and players read "reorder" in the formation, in English and Spanish. The game
> plays the same. Builds on [Exploration](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/exploration.md), [Save and load](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/save-and-load.md),
> the [npc renames](npc-renames.md) and the [cpuWanderBrain renames](cpuWanderBrain-renames.md).

## Decisions

| Topic          | Decision                                                                                                                                                                                                                                                                 |
|----------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope          | The renames below, in the code, tests, comments and docs of the party subdomain, and where other subdomains handle its events or call its Api. The two formation hints, in both catalogs.                                                                             |
| Walking order  | `Party.Line` is `WalkingOrder`, as the code comments already say "walking order". Chosen over keeping `Line`, as the glossary gives it to what an npc says, and over "marching order".                                                                                  |
| Interrupt      | A battle starting interrupts the exploration (`InterruptExploration`, `ExplorationInterrupted`), which resumes once it finishes (`ResumeExploration` stays). Pause is only the pause screen. Chosen over "stop", as an interrupted exploration resumes.                 |
| Party step     | One press of an arrow is a party step (`StepParty`, `PartyStepped`), as Respawn steps and `walkPartyStep` already count it. The party's own walking words follow: `canStep`, `isStepping`, `AlreadyStepping`, `PartyStepping`. Chosen over "party walk".              |
| Member's step  | Each member's move within a party step is the battle unit's Step, so `pendingSteps`, `awaitedStep`, `nextStep`, `skipNextStep`, `resolveStep`, `ResolvePartyStep` and `StepsRequired` stay.                                                                            |
| Words kept     | Member (`RecruitMember`, `ApplyMemberEffect`, `CanMemberLearnAbility`, `NotAPartyMember`), `NotAFollower`, `JoinParty`, `PartyJoined`, `LeaveParty`, the talk, formation and pause names, `ExplorationSecondStarted`, `ApplyStandingTerrainEffects` and `MarkBattleUnitMoved` already fit. |
| Exploring      | Exploring keeps the code's meaning: out of the battles while time goes on. So `isExploring`, `Mode.EXPLORING`, `NotExploringTheParty`, `WanderWhileExploring`, `startWhileExploring`, `Purpose.Exploring`, `exploringPlayerId` and `Battle.defeatWhileExploring` stay. |
| Save format    | Reached: the saved `lineBattleUnitIds` of the `party` snapshot is `walkingOrderBattleUnitIds`, so `SAVE_VERSION` goes from 7 to 8, with a new `v8.json` fixture. Chosen over keeping the saved key, so the save reads the glossary's words.                              |
| Player text    | The formation hints say "reorder": `ui.formation.tapToSelect` keeps its key, and `ui.formation.tapToSwap` is `ui.formation.tapToReorder`. Both catalogs change. "Move" and "trade" were left out, as the glossary already gives them to a battle unit and a merchant. |
| Tests          | No new tests: a rename changes no behaviour. The existing tests are renamed with the code, and `SaveFixtureTest` checks the new fixture.                                                                                                                                |
| Out of scope   | The words of the quest, player and screen subdomains, which wait for their sections. See [Not in this plan](#not-in-this-plan).                                                                                                                                         |

## Renames

| Today                                       | Glossary term  | New name                                            |
|---------------------------------------------|----------------|-----------------------------------------------------|
| `Party.Line`                                | Walking order  | `Party.WalkingOrder`                                |
| `Party.line`                                | Walking order  | `Party.walkingOrder`                                |
| `Line.closestFirst`                         | Walking order  | `WalkingOrder.closestFirst`                         |
| `Party.walkingLine()`                       | Walking order  | `Party.leaderFirstWalkingOrder()`                   |
| `Party.Dto.lineBattleUnitIds`               | Walking order  | `Party.Dto.walkingOrderBattleUnitIds`               |
| `Party.Dto.SnapshotDto.lineBattleUnitIds`   | Walking order  | `Party.Dto.SnapshotDto.walkingOrderBattleUnitIds`   |
| `PartySnapshot.lineBattleUnitIds`           | Walking order  | `PartySnapshot.walkingOrderBattleUnitIds`           |
| `PauseExploration`                          | Interrupt      | `InterruptExploration`                              |
| `PartyApi.pauseExploration`                 | Interrupt      | `PartyApi.interruptExploration`                     |
| `Party.pause()`                             | Interrupt      | `Party.interrupt()`                                 |
| `PartyEvent.ExplorationPaused`              | Interrupt      | `PartyEvent.ExplorationInterrupted`                 |
| `WalkParty`                                 | Party step     | `StepParty`                                         |
| `PartyApi.walkParty`                        | Party step     | `PartyApi.stepParty`                                |
| `Party.walkParty`                           | Party step     | `Party.stepParty`                                   |
| `PartyEvent.PartyWalked`                    | Party step     | `PartyEvent.PartyStepped`                           |
| `Party.canWalk()`                           | Party step     | `Party.canStep()`                                   |
| `Party.Dto.isWalking`                       | Party step     | `Party.Dto.isStepping`                              |
| `Party.isWalking()`                         | Party step     | `Party.isStepping()`                                |
| `Party.finishWalkWhenDone()`                | Party step     | `Party.finishStepWhenDone()`                        |
| `PartyError.AlreadyWalking`                 | Party step     | `PartyError.AlreadyStepping`                        |
| `PartyWalking`                              | Party step     | `PartyStepping`                                     |
| `PartyWalking.continueWalking`              | Party step     | `PartyStepping.continueStepping`                    |
| `cpuBattleBrain` `OnPartyWalked`            | Party step     | `OnPartyStepped`                                    |
| `cpuWanderBrain` `OnPartyWalked`            | Party step     | `OnPartyStepped`                                    |
| `UiText.FORMATION_TAP_TO_SWAP`              | Formation      | `UiText.FORMATION_TAP_TO_REORDER`                   |
| `ui.formation.tapToSwap`                    | Formation      | `ui.formation.tapToReorder`                         |
| `PauseExplorationTest`                      | Interrupt      | `InterruptExplorationTest`                          |
| `WalkPartyTest`                             | Party step     | `StepPartyTest`                                     |
| `cpuBattleBrain` `OnPartyWalkedTest`        | Party step     | `OnPartySteppedTest`                                |
| `cpuWanderBrain` `OnPartyWalkedTest`        | Party step     | `OnPartySteppedTest`                                |

## party

### Domain

- `Party` keeps its `walkingOrder`, a `WalkingOrder` rebuilt by `WalkingOrder.closestFirst` after a swap with the leader
  or a new party order. `leaderFirstWalkingOrder()` gives it to the `Dto` with the leader first. Its comments say
  "walking order" for "line" and "line order", and "party step" for the party's walk: e.g.: "The party steps one tile at
  a time" becomes "Each party step is one battle unit's step at a time".
- `Party.stepParty(steps)` plans a party step, and `canStep()` says whether the party is exploring and done stepping.
  `interrupt()` stops exploring when a battle starts, abandoning a party step, a talk, a formation or a pause in
  progress.
- `PartyError.AlreadyStepping` says "The party can not step again until its party step is done", and `NotExploring`
  says "The party can only step while exploring".
- `PartyEvent.PartyStepped` and `ExplorationInterrupted` replace `PartyWalked` and `ExplorationPaused`. The comment on
  `RequestStartTalk` says "for the npc adjacent to the leader", for "next to the leader".

### Use cases

- `StepParty` steps the leader one tile towards the direction, and each follower, in walking order, onto the tile the
  battle unit ahead of it left. It moves to `usecases/commands/StepParty.kt`.
- `InterruptExploration` interrupts the exploration when a battle starts. It moves to
  `usecases/commands/InterruptExploration.kt`.
- `PartyStepping` walks a party step one battle unit's step at a time (`continueStepping`), and keeps `canReach`. It
  moves to `usecases/services/PartyStepping.kt`.
- `ResolvePartyStep`, `CloseFormation`, `OpenFormation`, `OpenPause`, `OpenQuest` and `StartTalk` say "can not step" for
  "can not walk" in their comments, and `SearchPartySnapshot` says "its members and its walking order".

### Adapters

- `PartySnapshottable` saves `walkingOrderBattleUnitIds`, and its comment says "its members and its walking order".
- `OnBattleStarted` calls `interruptExploration`.
- `PartyApi` builds `stepParty`, `interruptExploration` and `PartyStepping`.

## Other subdomains

- `cpuBattleBrain` and `cpuWanderBrain`: `OnPartyStepped` subscribes to `PartyStepped`, and `CpuBattleBrainApi` and
  `CpuWanderBrainApi` build `onPartyStepped`.
- `screen`: `ExplorationPresenter` calls `partyApi.stepParty`, and its private `walkParty` is `stepParty`.
  `ExplorationPresenter`, `BattlefieldPresenter`, `InventoryPresenter` and `QuestPresenter` subscribe to
  `ExplorationInterrupted`. `FormationPresenter` and `FormationViewStories` use `UiText.FORMATION_TAP_TO_REORDER`.

## save

- `Save.SAVE_VERSION` is 8, and `src/jvmTest/fixtures/save/v8.json` is written with
  `./gradlew jvmTest --tests '*SaveFixtureTest*' -Drpg.updateSaveFixtures=true`, keeping `v1.json` to `v7.json`.

## Resources

| Key                         | English (`messages.pot`)          | Spanish (`es.po`)              |
|-----------------------------|-----------------------------------|--------------------------------|
| `ui.formation.tapToSelect`  | Tap two members to reorder them   | Toca dos para reordenarlos     |
| `ui.formation.tapToReorder` | Reorder {name} with...            | Reordena a {name} con...       |

Their comments say "where the player reorders two party members to set the party order", and "until another one is
tapped", as today.

## Docs

- [Exploration](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/exploration.md): `StepParty`, `PartyStepped`, `PartyStepping`, `InterruptExploration`
  and `ExplorationInterrupted`. "Line order" and "the line" say "walking order", and the party's walk says "party step",
  while a member's own walk stays a walk.
- [CPU battle brain](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/cpu-battle-brain.md): `PartyStepped`.
- [Presentation timeline](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/presentation-timeline.md) and [Quests](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/quests.md):
  `ExplorationInterrupted`.
- [Save and load](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/save-and-load.md): row 6 of the snapshot table says "The members and the walking
  order".
- `docs/architecture/dependencies.html` is regenerated by the pre-commit hook.

## Tests

The tests are renamed with the code, and their names and comments say the new words:

- `PartyTest`, `StepPartyTest`, `InterruptExplorationTest`, `ResolvePartyStepTest`, `OnBattleStartedTest`,
  `OnTileOccupiedTest`, `PartySnapshottableTest`, `RestorePartyTest`, `SearchPartySnapshotTest`, `CloseFormationTest`,
  `ClosePauseTest`, `CloseQuestTest`, `OpenFormationTest`, `StartTalkTest`.
- Both `OnPartySteppedTest`, `CpuWanderBrainTest`, `RespawnDefeatedBattleUnitsTest` and `ResolveRespawnTest`.
- `ExplorationPresenterTest`, `BattlefieldPresenterTest`, `InventoryPresenterGiveTest`, `InventoryPresenterGiveGoldTest`
  and `QuestPresenterTest`.
- The acceptance tests calling `partyApi.walkParty`: `BuildingAcceptanceTest`, `CpuRespawnAcceptanceTest`,
  `EngagementAcceptanceTest`, `ExplorationAcceptanceTest`, `ExploringTerrainEffectAcceptanceTest`,
  `NpcAcceptanceTest`, `PartyMemberSwapAcceptanceTest`, `QuestAcceptanceTest` and `SummonsAtBattleEndAcceptanceTest`.
- `SaveFixtureTest` checks `v8.json`.

## Not in this plan

- `PauseExploration` (`InterruptExploration` once built) interrupts the exploration when the party is exploring,
  talking, in the formation or paused, but not while it reads a quest. Time stops while reading, so a battle should not
  start meanwhile, but the check looks inconsistent. Bug or not, still to be decided.
- The comment of `Party.Dto` says Exploring is `false` "while battling, while talking and while in formation", leaving
  out paused and reading a quest. Still to be fixed.
- `Party.swapLeader` reads as changing who leads, while it swaps the leader's tile with a follower's. Its word is still
  to be decided.

### Waiting for other sections

- `quest`: reading a quest (`OpenQuest`, `CloseQuest`, `QuestOpened`, `QuestClosed`, `READING_QUEST`,
  `isReadingQuest`), and whether it is reading the journal.
- `player`: the human player the party belongs to (`playerId`, and "the party player" in the comment of
  `RecruitMember`).
- `screen`: the comments of `FormationPresenter` and `FormationView` (e.g.: "tapping another one swaps both", "to swap
  them"), now that players read "reorder".

## Changes while building

- `StepsRequired` also says "At least one step is required for a party step", for "to walk", and the comment of
  `DirectionDto` says "Where the leader steps to".
- The comments of `CpuWanderBrain.walkPartyStep` and `RespawnDefeatedBattleUnits` say "The party took a party step", and
  the comment in `ExplorationPresenter` says the exploration is interrupted, for paused.
- `CloseFormation`'s comment says the followers "fall in" behind the new leader, as "line up" read as the old Line.
- The **Screen / Formation** story descriptions say "reorder", and `FormationPresenterTest` and `FormationViewTest`
  expect the new hints.
- The test names of the party tests, `ExplorationPresenterTest`, `BattlefieldPresenterTest`, the give tests and the
  acceptance tests say stepping, walking order and interrupted where they meant the party, while a battle unit's own
  walk stays a walk. Local variables such as `pausedParty` in `PartyTest` were left as they are.
- [Quests](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/quests.md): the first column of the tracker table is wider, to fit `ExplorationInterrupted`.
