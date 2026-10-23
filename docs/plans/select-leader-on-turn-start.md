# Plan: Select the leader when the human turn starts

> **Status:** done.
>
> **Summary:** When the first battle starts, a new player is not told what to do. The CPU plays first, "Your turn" pops
> up, and then nothing is selected: the HUD is empty and Finish turn is the only button on the screen. They have to
> find out on their own that tapping the knight shows where it can walk, its abilities and its bag. Instead, when each
> human turn starts, the party leader is selected right after the turn announcement, as if the player had tapped it. The
> battlefield scrolls to it when it is off the screen.

## Decisions

| Topic           | Decision                                                                                                                                                                                             |
|-----------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| When            | Each human turn start, not `BattleStarted`: the engaging player plays first (`StartBattle`), so in the demo the corridor bat's turn comes before the knight's. CPU turns still start with nothing selected. |
| Who             | The current party leader (`PartyApi.searchParty().leaderBattleUnitId`). The party promotes the next member when the leader is defeated, so there is no extra fallback. Nothing is selected when the leader is not in the battle (or there is no party). |
| After acting    | Unchanged: the unit stays selected after it moves or casts, and its range is refreshed as it is today. The game never selects the next party member on its own.                                       |
| Guidance        | Selection only: no hint text and no tutorial. Play the corridor fight with this change first, and plan hints separately if it is still confusing.                                                     |
| Timing          | After the turn announcement and the tint, on the timeline: "Your turn" first, then the leader lights up. Finish turn stays disabled until the selection has been drawn, as the timeline is playing.      |
| Camera          | The battlefield scrolls to the leader's tile (`BattlefieldView.scrollToTile`), only when the tile is out of the viewport or on its edge, before the movement range is drawn.                          |
| Re-tap          | Unchanged: tapping the selected unit deselects it. A player who taps the knight first, as they must today, hides its range; tapping it again brings the range back.                                     |
| Exploring       | Unchanged: nothing is selected when exploring starts or resumes.                                                                                                                                     |
| Domain          | Unchanged: only the battlefield HUD (presentation) and the `BattlefieldPresenter` change.                                                                                                           |

For example, the corridor bat engages the knight:

| Step | The screen                                                                                                       |
|------|------------------------------------------------------------------------------------------------------------------|
| 1    | "Battle!" is announced. The bat's turn is announced, red tint, and the bat acts.                                 |
| 2    | "Your turn" pops up, with a blue tint.                                                                           |
| 3    | The battlefield scrolls to the knight if needed. The knight is selected: its walking range is lit, the other tiles dimmed, and its info, abilities and bag fill the HUD. |
| 4    | Finish turn is enabled. The player taps a lit tile to walk, or an ability to cast.                               |

## Battlefield HUD

- A new command, `SelectBattleUnit(battleUnitId)`, in `battlefieldHud/usecases/commands`. From any HUD state it goes
  idle (like `DeselectBattleUnit`), then selects the battle unit on its tile (`SearchPosition`) with its movement range
  (`MovementService.tilesWhereCanMove`). It publishes `Idle` then `SelectedBattleUnit`. When the battle unit has no
  position, it only goes idle.
- `BattlefieldHud.Idle.selectBattleUnit` is reused, so `DisplayMovementRange` is the same as after a tap: tapping a lit
  tile walks, tapping the knight deselects, and tapping an ability selects it.

## BattlefieldPresenter

- `PlayerTurnStarted`: clears the selection as today. On a human turn, when the party leader is in the battle, it then
  calls `SelectBattleUnit(leaderId)`.
- Ordering: `InMemoryEventBus` queues the events published while it dispatches, so `SelectedBattleUnit` is dispatched
  after every `PlayerTurnStarted` handler. That includes `BattleInfoPresenter.announceTurn`, which is subscribed later.
  The selection step is therefore played after the announcement and the tint, without any extra wiring.
- `displayMovementRange`: in a battle, scrolls to the selected tile (`scrollToTile`) before drawing the range. A tapped
  tile is already on the screen, so this only scrolls when it lies on the viewport edge. That is a small change from
  today, accepted to keep a single path. While exploring there is no scrolling, as the battlefield follows the leader.

## Tests

`SelectBattleUnitTest` (new, next to `DeselectBattleUnitTest`):

- `should select the battle unit with its movement range when the HUD is idle`.
- `should replace the previous selection when a battle unit or an ability is selected`, for each HUD state.
- `should only go idle when the battle unit has no position`.

`BattlefieldPresenterTest`:

- `should select the party leader when a human turn starts`.
- `should not select any battle unit when a CPU turn starts`.
- `should not select the party leader when it is not in the battle`.
- `should select the new party leader when a human turn starts after the leader left the party`.
- `should scroll to the leader when it is selected out of the viewport`.

End-to-end:

- `MoveKnightEndToEndTest` (3), `CastAbilityEndToEndTest` (3) and `InventoryEndToEndTest` (1) tap the knight after
  `startNewBattle()`. That tap now deselects it, so the step is removed: the knight is already selected once the human
  player can act (`canHumanPlayerAct` waits for Finish turn, which waits for the timeline).
- `InventoryEndToEndTest`, `should not display the bag when the human player selects a cpu bat`: the knight's info is
  already displayed, so the test waits for the bat's tile to be selected before checking the bag.
- `MoveKnightEndToEndTest`, `should select the knight and dim the tiles out of its movement range when the human
  player turn starts` replaces the test that tapped the knight: after `startNewBattle()`, its walking range is lit and
  the other tiles dimmed, with no tap.
- `ExploreEndToEndTest` is unchanged, as exploring does not auto-select.
