# Plan: open world exploration

> **Status:** implemented, the 6 milestones are done.
>
> **Summary:** A new game starts exploring the battlefield instead of battling. There are no turns. The human player
> walks one battle unit with a virtual pad (north, east, south and west arrows, as on the SNES) and their other battle
> units follow it in a line. When a CPU battle unit engages, the game switches to a turn-based battle between the
> engaged battle units. Once nobody is engaged any more, exploring resumes.

## Decisions

| Topic             | Decision                                                                                                                                                                  |
|-------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Walking           | Tile by tile, on the battlefield grid. Holding an arrow keeps walking, one tile at a time.                                                                                |
| Leader            | The first human battle unit. When it is defeated, the next one leads.                                                                                                     |
| CPU exploring     | The CPU battle units stand still.                                                                                                                                         |
| Engaging          | A CPU battle unit engages when a human battle unit is within reach (`ENGAGEMENT_DISTANCE`).                                                                               |
| Who battles       | Only the engaged CPU battle units, and the whole human party, as it walks together. The rest of the CPU battle units stay out of the battle: they neither play nor change. |
| Joining, leaving  | When a round finishes, the CPU battle units that got an enemy within reach join, and those without any leave (e.g.: left behind).                                         |
| Back to exploring | When no CPU battle unit is left in the battle (defeated or left behind).                                                                                                  |
| First to play     | The CPU, as it engaged.                                                                                                                                                   |
| Terrain effects   | While exploring, a battle unit receives the effect of its terrain (e.g.: lava, water) every second it stands on it. Walking over a tile does not apply it.               |
| Controls          | The virtual pad only, no keyboard for now.                                                                                                                                |

## exploration (new)

Owns the exploring mode: whether the game is exploring or battling, which battle unit leads, the order the other
battle units follow it in, and which battle units stood still during the last second.

### Domain

- Add `Exploration` because the party and the mode need an owner. It holds:
  - the human player id;
  - the leading battle unit id;
  - the following battle unit ids, in order;
  - the mode, `EXPLORING` or `BATTLING`;
  - the walk in progress: the steps left, and the step awaited until its battle unit occupies its tile;
  - the battle units that moved since the last second.

  It walks the party one tile at a time: the leader steps towards the direction, and each follower steps to the tile
  the battle unit ahead of it left. The steps are walked one after another, as a battle unit can only step on the tile
  the one ahead left once the battlefield has it vacant.
- Add the `ExplorationEvent`s because the other subdomains and the screen react to them:
  - `RequestStartExploration`, `ExplorationStarted`, `PartyWalked`, `ExplorationPaused` and `ExplorationResumed`.

### Use cases

#### Commands

- `StartExploration`: once the battle units are deployed, the first human battle unit leads and the rest follow in
  the loaded order.
- `WalkParty(direction)`: only while exploring, and only when the leader can occupy the next tile (e.g.: not void,
  not another battle unit). It requests the walk of the first step (`BattleUnitEvent.RequestWalkBattleUnit`).
- `ResolvePartyStep`: on `BattlefieldTileOccupied`, once the awaited battle unit occupies its tile, it requests the
  walk of the next step. Once the last step is walked, it publishes `PartyWalked`.
- A service, `PartyWalking`, requests the steps for both, and skips the steps whose battle unit can not reach its
  tile, so the party always finishes walking.
- `ApplyStandingTerrainEffects`: every second while exploring, it requests the terrain effects for the battle units
  that did not move during that second, the CPU battle units included, as they stand still. It requests them to the
  battlefield with `BattlefieldEvent.RequestEffectApplicationToOccupants`, and starts the next second.
- `MarkBattleUnitMoved`: on `BattleUnitMoved`, it remembers the battle unit moved during the current second, only while
  exploring.
- `PauseExploration`: on `BattleStarted`, so the pad no longer walks the party and the seconds no longer apply terrain
  effects.
- `ResumeExploration`: on `BattleFinished`, the party walks again.
- `LeaveParty`: on `BattleUnitDefeated`, the defeated battle unit leaves the party, and the next one leads when the
  leader was defeated. It is a command of its own, rather than part of `ResumeExploration`, as a battle unit can also
  be defeated while exploring (e.g.: standing on lava, from milestone 5).

#### Queries

- `SearchExploration`: the mode and the party, for the screen and the inventory.

### Adapters

#### Presentation

- Add `ExplorationApi`, which wires the subdomain as the other `*Api` classes do.

#### Storage

- Add `InMemoryExplorationRepository`.

#### Events

- `OnRequestStartExploration`, `OnBattleStarted` and `OnBattleFinished`.
- `OnBattlefieldTileOccupied`, which resolves the awaited step.
- `OnBattleUnitMoved`, which marks the battle units that moved during the current second (`MarkBattleUnitMoved`).
- `OnBattleUnitDefeated`, which removes the battle unit from the party (`LeaveParty`).

## battle

### Domain

- Modify `Battle` because it no longer starts with the game, nor involves every battle unit:
  - it starts on every engagement, so a game holds several battles one after another;
  - it holds the battle units in the battle. Battle units join and leave it when a round finishes, and leave it when
    they are defeated;
  - the player order comes from the engagement: the CPU first, then the human player;
  - it finishes without a winner (`BattleFinished`) when only one player is left with battle units in it, e.g.: no CPU
    battle unit is left in it (defeated or left behind). It is checked when a round finishes, so defeating the last CPU
    battle unit in it during the human turn still needs the turn to be finished;
  - `PlayerVictory` still ends the game when a player has no battle units left on the battlefield, also while exploring
    (e.g.: defeated by the lava): `DefeatPlayer` then stores a battle with only the winner left
    (`Battle.defeatWhileExploring`), which finishes as any battle with a single player left. `BattleApi` uses
    `SearchEnemyPlayer` for it.
- Modify `BattleEvent.PlayerTurnStarted`, `BattleEvent.BattleRoundStarted` and `BattleEvent.BattleRoundFinished` because the other subdomains must leave
  out the battle units outside the battle. They carry the ids of the battle units in the battle, so the other
  subdomains do not depend on `battle`.
- Add `BattleEvent.BattleFinished` because exploring resumes when a battle finishes without a winner.
- Delete `BattleEvent.RequestInitialiseBattle` because the battle no longer starts with the game.

### Use cases

#### Commands

- Add `StartBattle`: on `CombatEngaged`, the engaged CPU battle units and the human battle units battle, the CPU
  first. It only waits for the battle in progress: a finished battle is replaced. The engaging player plays first with the battle units that engaged, and the engaged player second with all
  their battle units.
- Modify `StartNextRound`: on `EngagementUpdated`, the battle units join and leave, then the next round starts, or the
  battle finishes when only one player is left with battle units in it (`SearchBattleUnitById` gives the player of
  each battle unit). It replaces starting the next round straight on
  `BattleRoundFinished`, so the joining battle units play from the next round. A command of its own
  (`UpdateBattleUnits`) is not needed.
- `DefeatPlayer` and `FinishBattle` already check every battle unit of the player on the battlefield, not only the
  ones in the battle: a player without battle units on the battlefield still loses the game.
- Delete `InitialiseBattle` because `StartBattle` replaces it.
- Delete `StartFirstRound` because `StartBattle` replaces it, and the game does not use it today: only `BattleApi` and
  its test do.

#### Queries

- Modify `SearchBattle` so it also returns the battle units in the battle, e.g.: for the screen to only let the player
  select those.

### Adapters

#### Storage

- `InMemoryBattleRepository` already replaces the stored battle when a new one is created, so a new battle replaces the
  finished one without changes.

#### Events

- Add `OnCombatEngaged` and `OnEngagementUpdated`.
- Delete `OnRequestInitialiseBattle` and `OnRoundFinished`, which the two above replace.

#### Resources

- Delete `resources/battle/battle.toml`, `BattleLoader` and `ResourcesBattleLoader` because the player order now comes
  from the engagement.

## battleUnit

### Domain

- Add `BattleUnit.walk` because walking while exploring spends no turn action, unlike `move`. It publishes the same
  `BattleUnitMoved`, so the battlefield occupancy, the loot found on the way and the animation keep working as they do
  today.
- Add `BattleUnitEvent.RequestWalkBattleUnit` because the exploration requests each walk, as the CPU brain requests
  each move with `RequestMoveBattleUnit`.

### Use cases

#### Commands

- Add `WalkBattleUnit`: on `RequestWalkBattleUnit`. A follower further than one tile walks the shortest path
  (`SearchShortestPath`), e.g.: when the party is not lined up yet.
- Modify `ResetBattleUnitActionsAndReduceCooldowns`, `ApplyOnTurnStartedEffects` and `ReplenishMana` so they only
  change the battle units in the battle.

#### Queries

- `SearchBattleUnitsByPlayerId`: reused as it is.

### Adapters

#### Events

- Add `OnRequestWalkBattleUnit`.
- Modify `OnPlayerTurnStarted` so it passes the battle units in the battle.

#### Resources

- Modify the human deployments (`resources/battleUnit/player-1-*.toml`) so the knights start next to each other in a
  line, rather than diagonally.

## battlefield

### Domain

- Nothing changes.

### Use cases

#### Commands

- Modify `RequestEffectApplicationToOccupants` so it can be limited to some battle units: those standing while
  exploring, and those in the battle when a round starts. The exploration requests it with
  `BattlefieldEvent.RequestEffectApplicationToOccupants`.

#### Queries

- `CanBattlefieldTileBeOccupied` and `SearchShortestPath`: reused as they are.

### Adapters

#### Events

- Modify `OnBattleRoundStarted` so it passes the battle units in the battle.
- Add `OnRequestEffectApplicationToOccupants`, for the battle units standing still while exploring.

## cpuBrain

### Domain

- Add `CpuBrainEvent.CombatEngaged(playerId, engagedPlayerId, battleUnitIds)` and
  `CpuBrainEvent.EngagementUpdated(joiningBattleUnitIds, leavingBattleUnitIds)` because the CPU brain already knows
  when an enemy is within reach, and engaging uses the same rule as battling.

### Use cases

#### Commands

- Add `EngageEnemies`: on `ExplorationStarted` and `PartyWalked`, when any CPU battle unit has an enemy within reach,
  it publishes `CombatEngaged` with those CPU battle units.
- Add `UpdateEngagement`: when a battle round finishes, it publishes `EngagementUpdated` with the CPU battle units
  that joined (an enemy is now within reach) and left (no enemy is within reach any more). It always publishes it,
  also when nobody joins nor leaves, as the next round starts on it.
- Modify `PlayTurn` so it only manages the CPU battle units in the battle.

#### Queries

- `SearchEnemiesWithinReach`: reused as it is.

### Adapters

#### Events

- Add `OnExplorationStarted`, `OnPartyWalked` and `OnBattleRoundFinished`.
- Modify `OnPlayerTurnStarted` so it passes the battle units in the battle.

## player

### Domain

- Modify `PlayerRepository`, so it can search every player.

### Use cases

#### Queries

- Add `SearchHumanPlayer` because the exploration starts with the battle units of the human player.

### Adapters

- Nothing changes.

## inventory

### Domain

- Nothing changes.

### Use cases

#### Commands

- Modify `PlayerTurnRule` so the inventory can be used while exploring, as there are no turns then: it is only
  limited to the turn of the battle unit player while a battle is in progress.

#### Queries

- Nothing changes.

### Adapters

- Nothing changes.

## battlesetup

### Domain

- Nothing changes.

### Use cases

#### Commands

- Modify `SetupBattle` so it publishes `RequestStartExploration` instead of `RequestInitialiseBattle`.

### Adapters

- Nothing changes.

## screen

### Domain

- Nothing changes.

### Use cases

- Nothing changes.

### Adapters

#### Presentation

- Add `VirtualPadView`: four arrow buttons, shown in place of the battle HUD while exploring. Holding an arrow keeps
  walking, one tile at a time.
- Add `ExplorationPresenter`:
  - it sends each arrow press to `ExplorationApi.walkParty` once the timeline has played the previous step, so a held
    arrow can not walk ahead of the animation;
  - it swaps the pad and the battle HUD when exploring pauses and resumes.
- Modify `BattleScene`:
  - it creates the exploration API and presenter, and the pad;
  - it passes the time elapsed on every frame to the `ExplorationPresenter`, which calls
    `ExplorationApi.applyStandingTerrainEffects` every second. The presenter counts the seconds, so it can be tested,
    and stops counting them once the game is over, when it also hides the pad.
- Modify `BattlefieldPresenter`:
  - it ignores tile taps while exploring;
  - it keeps the leader in the viewport (`followTile`);
  - it plays the party steps faster than a battle move;
  - in a battle, it only lets the player select the battle units in it;
  - it clears the selection when the battle finishes, as no turn starts then.
- Modify `BattleInfoPresenter`: it shows "Exploring" instead of the round and the player turn, announces the battle
  when combat is engaged, and announces its end ("Battle over!").
- Modify `FinishTurnPresenter` so its call to action is hidden while exploring.
- To use the inventory while exploring, which the plan missed, as it is only reachable from the selected battle unit:
  - `ProcessTileSelected` selects a battle unit of the party, without any movement range, when no battle is in
    progress. Anything else clears the selection;
  - `BattlefieldPresenter` no longer ignores the tile taps while exploring, and ignores the abilities instead;
  - `ExplorationPresenter` displays the battle HUD and the call to action in place of the pad while a battle unit is
    selected, and straight away when a found item does not fit in the inventory (`InventoryFull`), which waited
    behind the hidden HUD otherwise. It does not walk the party while the pad is hidden;
  - `InventoryPresenter` offers the item actions out of the battles.

## Milestones

Each milestone is committed and pushed on its own, with its tests and documentation, and leaves the game playable.

| # | Done | Milestone                       | Changes                                                                                                                                                                                                                                                                                                                                                                                                                                     | The game after it                                                                                                                                                |
|---|------|---------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1 | ✅   | Battle units in the battle      | `Battle` holds the battle units in the battle: every battle unit, for now. The battle units deployed during it join it (e.g.: a bat deployed by a goat), and the defeated ones leave it. `PlayerTurnStarted` and `BattleRoundStarted` carry them, and the turn resets, the round terrain effects and `PlayTurn` only change those. `SearchBattle` returns them. Delete `StartFirstRound`.                                                                                                                                                                | Unchanged: the battle starts with the game, with every battle unit.                                                                                              |
| 2 | ✅   | Exploring until a CPU engages   | `exploration` subdomain: `StartExploration`, `WalkParty`, `ResolvePartyStep`, `PauseExploration`, `ExplorationApi`. `SearchHumanPlayer`. `BattleUnit.walk` and `WalkBattleUnit`. `EngageEnemies` and `CombatEngaged`. `SetupBattle` starts exploring. `CombatEngaged` starts the battle as today: every battle unit, the loaded player order. `VirtualPadView`, `ExplorationPresenter`, the "Exploring" info, the battle announcement, tile taps ignored while exploring. The knights deployed in a line. | The party explores with the pad. The first CPU battle unit within reach starts the turn-based battle of today, played until a player is defeated.                |
| 3 | ✅   | Only the engaged battle units   | `StartBattle` with the engaged CPU battle units and the human party, the CPU first. `UpdateEngagement`, `EngagementUpdated`, and `StartNextRound` with the joining battle units: the CPU battle units getting an enemy within reach join when a round finishes. Only the battle units in the battle can be selected. `DefeatPlayer` and `FinishBattle` against the battle units on the battlefield, as they already are. Delete `battle.toml`, `BattleLoader`, `ResourcesBattleLoader` and `InitialiseBattle`. | The battle involves the engaged CPU battle units only, the CPU playing first. The rest join when the party walks within their reach, until a player is defeated. |
| 4 | ✅   | Back to exploring               | The CPU battle units without an enemy within reach leave the battle when a round finishes. `BattleFinished` when only one player is left with battle units in the battle. `ResumeExploration`, and `LeaveParty` (defeated battle units leave the party, the next one leads). `StartBattle` replaces the finished battle. The battle end announcement, and the selection cleared.                                                   | Exploring resumes once no CPU battle unit is left in the battle, and the next engagement starts a new battle.                                                     |
| 5 | ✅   | Terrain effects while exploring | `ApplyStandingTerrainEffects`, called every second by the `ExplorationPresenter` from the `BattleScene` frames. `OnBattleUnitMoved` and `MarkBattleUnitMoved` mark the battle units that moved. `RequestEffectApplicationToOccupants` limited to some battle units, requested with `BattlefieldEvent.RequestEffectApplicationToOccupants`. The game over while exploring.                                                     | Standing on lava hurts and standing on water heals while exploring, every second.                                                                                |
| 6 | ✅   | Inventory while exploring       | `PlayerTurnRule` lets the inventory be used while exploring. Tapping a battle unit of the party displays its info and bag in place of the pad, and the found item that does not fit in the inventory is asked while exploring too.                                                                                                                                                                                             | Items can be consumed, equipped and discarded while exploring.                                                                                                   |

The tests that expect the battle to start with the game change in milestone 2: they either walk the party within reach
of a CPU battle unit first, or deploy them within reach of each other.
