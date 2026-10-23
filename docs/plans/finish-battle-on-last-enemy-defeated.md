# Plan: Finish the battle once the last engaged CPU battle unit is defeated

> **Status:** done.
>
> **Summary:** The battle only finishes when a round does. Defeating the last engaged CPU battle unit during the human
> turn still leaves the player in the battle, with nothing to fight, until they tap Finish turn. Instead, the battle is
> checked as soon as a battle unit leaves it. When only one player is left with battle units in it, the CPU battle units
> out of the battle that have the party within reach join it, and the battle goes on. Otherwise, the battle finishes
> without a winner straight away, and the party explores again.

## Decisions

| Topic          | Decision                                                                                                                                                                                                                       |
|----------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| When           | Any time a battle unit leaves the battle during a round (`LeaveBattle`, e.g.: defeated or vanished): the human turn, the CPU turn (e.g.: a venom tick at its start, venom on death) or the round start (e.g.: lava).          |
| Nearby CPU     | The engagement is checked right away, as at a round end but without wandering: the CPU battle units out of the battle with an enemy within reach join it, and the battle goes on. It finishes only when nobody joins.         |
| Joining        | The joining battle units play from the next round, as when they join at a round end. A CPU turn in progress is not extended with them.                                                                                       |
| CPU summons    | Counted, as today: a CPU summon left in the battle keeps it going. It finishes once the summon is defeated or vanishes, as both leave the battle.                                                                             |
| CPU wiped      | Victory only: when the battle unit leaving was the last one of its player, the player is defeated (`PlayerVictory`), and the battle never goes back to exploring. The same goes for the human player.                       |
| Rest of a turn | A CPU turn in progress, or a turn finish requested afterwards, does nothing once the battle is finished: `FinishPlayerTurn` ignores a finished battle.                                                                        |
| Presentation   | Unchanged: the defeat flash, the loot drop and then "Battle over" are played in timeline order, and the virtual pad takes the place of the battle HUD. The joining battle units are not announced, as at a round end.         |
| Round end      | Unchanged: `UpdateEngagement` and `StartNextRound` still have the battle units without enemies within reach leave it, and finish it when that disengages it.                                                                |

For example, the knight defeats the corridor bat with its sword, during its first turn:

| Step | What happens                                                                                                                          |
|------|---------------------------------------------------------------------------------------------------------------------------------------|
| 1    | The sword cast is resolved (`AbilityCastResolved`): the knight is displayed selected again.                                           |
| 2    | The bat is defeated: it flashes, leaves its tile, and drops its loot. It leaves the battle, which is disengaged (`BattleDisengaged`). |
| 3    | No other CPU battle unit has the knight within reach, so nobody joins, and the battle finishes without a winner (`BattleFinished`).  |
| 4    | "Battle over" is announced, the selection is cleared, and the virtual pad is displayed: the party explores again.                     |

## Battle

- `Battle.leaveDuringRound(battleUnitId)`: removes the battle unit, as `leave` does, and records `BattleDisengaged`
  (with the battle units left in it) when the battle was engaged and is disengaged once it has left. Nothing is
  recorded for a finished battle, or for a battle unit that was not in it (e.g.: a CPU battle unit out of the battle
  hit by a cleave). `leave` is unchanged, as `StartNextRound` checks the disengagement on its own.
- `LeaveBattle` uses it and publishes its events.
- `FinishDisengagedBattle(joiningPlayerId, joiningBattleUnitIds)`, requested with `RequestFinishDisengagedBattle`: joins
  the battle units, then finishes the battle without a winner when it is still disengaged. Nothing happens once the
  battle is finished, including when it only has a single player left, as the player who left is being defeated.
- `FinishPlayerTurn` ignores a finished battle.

Ordering: `RequestBattleLeave` publishes `RequestLeaveBattle`, then `RequestDefeatPlayer` when the player has no battle
units left. `InMemoryEventBus` queues the events, so the player is defeated before `BattleDisengaged` is handled, and
`FinishDisengagedBattle` finds a battle with a single player left: the victory is the only outcome displayed.

## CPU battle brain

- `ReengageEnemies(battleUnitIds)`, on `BattleDisengaged`: the CPU battle units out of the battle with an enemy within
  reach join it. It publishes `EngagementUpdated` (nobody leaves), then `RequestFinishDisengagedBattle` with them.

## Tests

- `BattleTest`: `leaveDuringRound` records `BattleDisengaged` when the last battle unit of a player leaves, and nothing
  when other battle units of the player are left, when the battle unit was not in the battle, or when the battle is
  finished.
- `LeaveBattleTest`: publishes `BattleDisengaged` when the last battle unit of a player leaves the battle, and nothing
  otherwise.
- `FinishDisengagedBattleTest` (new): finishes the battle when nobody joins, goes on with the joining battle units, and
  does nothing when the battle is finished or a player has been defeated.
- `FinishPlayerTurnTest`: does nothing when the battle is finished.
- `ReengageEnemiesTest` (new): requests the battle to finish with the CPU battle units out of the battle that have an
  enemy within reach, and with nobody when none has.
- Adapter tests for `OnBattleDisengaged` and `OnRequestFinishDisengagedBattle`.
- `EngagementAcceptanceTest`: defeating the rat engaged explores again without finishing the turn; a rat within reach
  joins the battle instead, which goes on; and defeating the last rat of the CPU, once the other one was defeated out of
  the battle, only has the human player win.
- `SummonsAtBattleEndAcceptanceTest`: the battle ends, and the bees vanish, as soon as the rat is defeated, without
  finishing the turn.
- `CastAbilityEndToEndTest`: defeating the corridor bat, the only CPU battle unit engaged, now explores again. The test
  checks the battle is over and the virtual pad is displayed, instead of the knight movement range behind the bat.
