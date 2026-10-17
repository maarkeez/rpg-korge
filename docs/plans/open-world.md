# Plan: open world exploration

> **Status:** draft overview, not implemented.
>
> **Summary:** A new game starts exploring the battlefield instead of battling. There are no turns. The human player
> walks one battle unit with a virtual pad (north, east, south and west arrows, as on the SNES) and their other battle
> units follow it in a line. When a CPU battle unit engages, the game switches to a turn-based battle between the
> engaged battle units. Once nobody is engaged any more, exploring resumes.

## Decisions

| Topic            | Decision                                                                                                                                                                  |
|------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Walking          | Tile by tile, on the battlefield grid. Holding an arrow keeps walking, one tile at a time.                                                                                |
| Leader           | The first human battle unit. When it is defeated, the next one leads.                                                                                                     |
| CPU exploring    | The CPU battle units stand still.                                                                                                                                         |
| Engaging         | A CPU battle unit engages when a human battle unit is within reach (`ENGAGEMENT_DISTANCE`).                                                                               |
| Who battles      | Only the engaged CPU battle units, and the whole human party, as it walks together. The rest of the CPU battle units stay out of the battle: they neither play nor change. |
| Joining, leaving | When a round finishes, the CPU battle units that got an enemy within reach join, and those without any leave (e.g.: left behind).                                         |
| Back to exploring | When no CPU battle unit is left in the battle (defeated or left behind).                                                                                                  |
| First to play    | The CPU, as it engaged.                                                                                                                                                   |
| Terrain effects  | While exploring, a battle unit receives the effect of its terrain (e.g.: lava, water) every second it stands on it. Walking over a tile does not apply it.               |
| Controls         | The virtual pad only, no keyboard for now.                                                                                                                                |

## Subdomain

- `exploration` (new): owns the exploring mode, that is, whether the game is exploring or battling, which battle unit
  leads, the order the other battle units follow it in, and which battle units stood still during the last second.
- Changed:
  - `battle`: starts on engagement, only between the engaged battle units, and can finish without a winner.
  - `battleUnit`: walks without spending turn actions.
  - `cpuBrain`: decides who engages and who disengages.
  - `battlefield`: applies terrain effects to some battle units only.
  - `inventory`: can be used while exploring.
  - `battlesetup`: starts exploring instead of a battle.
  - `screen`: virtual pad, exploring and battling layouts, the second ticks.

## Domain

- Add `Exploration`, because the party and the mode need an owner. It holds:
  - the human player id;
  - the leading battle unit id;
  - the following battle unit ids, in order;
  - the mode, `EXPLORING` or `BATTLING`;
  - the battle units that moved since the last second.

  It walks the party one tile at a time: the leader steps towards the direction, and each follower steps to the tile
  the battle unit ahead of it left.
- Add the `ExplorationEvent`s, because the other subdomains and the screen react to them:
  - `ExplorationStarted`, `PartyWalked`, `ExplorationPaused` and `ExplorationResumed`;
  - `RequestWalkBattleUnit`, one per battle unit of the party, in walking order.
- Add `BattleUnit.walk`, because walking while exploring spends no turn action, unlike `move`. It publishes the same
  `BattleUnitMoved`, so the battlefield occupancy, the loot found on the way and the animation keep working as they do
  today.
- Add `CpuBrainEvent.CombatEngaged(battleUnitIds)` and `CpuBrainEvent.EngagementUpdated(joiningBattleUnitIds,
  leavingBattleUnitIds)`, because the CPU brain already knows when an enemy is within reach, and engaging uses the same
  rule as battling.
- Modify `Battle`, because it no longer starts with the game, nor involves every battle unit:
  - it starts on every engagement, so a game holds several battles one after another;
  - it holds the battle units in the battle. Battle units join and leave it when a round finishes, and leave it when
    they are defeated;
  - the player order comes from the engagement: the CPU first, then the human player;
  - it finishes without a winner (`BattleFinished`) when no CPU battle unit is left in it;
  - `PlayerVictory` still ends the game when a player has no battle units left on the battlefield.
- Modify `BattleEvent.PlayerTurnStarted` and `BattleEvent.BattleRoundStarted`, so they carry the ids of the battle
  units in the battle. The CPU brain, the turn resets and the round terrain effects then leave out the battle units
  outside it, without depending on the `battle` subdomain.
- Delete `StartFirstRound`, because starting a battle on engagement replaces it, and the game does not use it today:
  only `BattleApi` and its test do.

## Use cases

### Commands

- `exploration`:
  - `StartExploration`: once the battle units are deployed, the first human battle unit leads and the rest follow in
    the loaded order.
  - `WalkParty(direction)`: only while exploring, and only when the leader can occupy the next tile (e.g.: not void,
    not another battle unit). It walks the party and publishes `PartyWalked`.
  - `ApplyStandingTerrainEffects`, every second while exploring: it requests the terrain effects for the battle units
    that did not move during that second, the CPU battle units included, as they stand still.
  - `PauseExploration`, on `BattleStarted`, so the pad no longer walks the party and the seconds no longer apply
    terrain effects.
  - `ResumeExploration`, on `BattleFinished`. Defeated battle units leave the party, and the next one leads when the
    leader was defeated.
- `battleUnit`:
  - `WalkBattleUnit`, on `RequestWalkBattleUnit`. A follower further than one tile walks the shortest path
    (`SearchShortestPath`), e.g.: when the party is not lined up yet.
  - Modify `ResetBattleUnitActionsAndReduceCooldowns`, `ApplyOnTurnStartedEffects` and `ReplenishMana`, so they only
    change the battle units in the battle.
- `battlefield`: modify `RequestEffectApplicationToOccupants`, so it can be limited to some battle units (those
  standing while exploring, those in the battle when a round starts).
- `cpuBrain`:
  - `EngageEnemies`, on `PartyWalked`: when any CPU battle unit has an enemy within reach (`SearchEnemiesWithinReach`),
    it publishes `CombatEngaged` with those CPU battle units.
  - `UpdateEngagement`, when a battle round finishes: it publishes `EngagementUpdated` with the CPU battle units that
    joined (an enemy is now within reach) and left (no enemy is within reach any more).
  - Modify `PlayTurn`, so it only manages the CPU battle units in the battle.
- `battle`:
  - `StartBattle`, on `CombatEngaged`: the engaged CPU battle units and the human battle units battle, the CPU first.
    It replaces `InitialiseBattle`.
  - `UpdateBattleUnits`, on `EngagementUpdated`, in place of starting the next round straight on `BattleRoundFinished`:
    the battle units join and leave, then the next round starts, or the battle finishes when no CPU battle unit is left
    in it.
  - Modify `DefeatPlayer` and `FinishBattle`, so a player without battle units on the battlefield still loses the
    game.
- `battlesetup` `SetupBattle` publishes `RequestStartExploration` instead of `RequestInitialiseBattle`.
- `inventory`: modify `PlayerTurnRule`, so the inventory can be used while exploring, as there are no turns then.

### Queries

- `exploration` `SearchExploration`: the mode and the party, for the screen and the inventory.
- `battle` `SearchBattle`: also returns the battle units in the battle, e.g.: for the screen to only let the player
  select those.
- `cpuBrain` `SearchEnemiesWithinReach`, `battlefield` `CanBattlefieldTileBeOccupied` and `SearchShortestPath`, and
  `battleUnit` `SearchBattleUnitsByPlayerId`: reused as they are.

## Adapters

### Presentation

- Add `ExplorationApi`, which wires the `exploration` subdomain as the other `*Api` classes do.
- Add `VirtualPadView`: four arrow buttons, shown in place of the battle HUD while exploring. Holding an arrow keeps
  walking, one tile at a time.
- Add `ExplorationPresenter`:
  - it sends each arrow press to `ExplorationApi.walkParty` once the timeline has played the previous step, so a held
    arrow can not walk ahead of the animation;
  - it swaps the pad and the battle HUD when exploring pauses and resumes.
- Modify `BattleScene`:
  - it creates the exploration API and presenter, and the pad;
  - it ticks `ExplorationApi.applyStandingTerrainEffects` every second, from the frames, as it dispatches the event
    bus today.
- Modify `BattlefieldPresenter`:
  - it ignores tile taps while exploring;
  - it keeps the leader in the viewport (`followTile`);
  - it plays the party steps faster than a battle move;
  - in a battle, it only lets the player select the battle units in it.
- Modify `BattleInfoPresenter`: it shows "Exploring" instead of the round and the player turn, announces the battle
  when combat is engaged, and announces its end.
- Modify `FinishTurnPresenter`, so its call to action is hidden while exploring.

### Events

- `exploration`:
  - `OnRequestStartExploration`, `OnBattleStarted` and `OnBattleFinished`;
  - `OnBattleUnitMoved`, which marks the battle units that moved during the current second;
  - `OnBattleUnitDefeated`, which removes the battle unit from the party.
- `battleUnit` `OnRequestWalkBattleUnit`.
- `cpuBrain` `OnPartyWalked` and `OnBattleRoundFinished`.
- `battle`:
  - `OnCombatEngaged` and `OnEngagementUpdated`;
  - delete `OnRequestInitialiseBattle` and `OnRoundFinished`, which the two above replace.

### Storage

- Add `InMemoryExplorationRepository`.
- Modify `InMemoryBattleRepository`, so a new battle replaces the finished one.

### Resources

- Delete `resources/battle/battle.toml`, `BattleLoader` and `ResourcesBattleLoader`, because the player order now comes
  from the engagement.
- Modify the human deployments (`resources/battleUnit/player-1-*.toml`), so the knights start next to each other in a
  line, rather than diagonally.
- No new resource: the party is read from the human battle units deployed.
