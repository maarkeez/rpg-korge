# Plan: the game has no victory, only a game over

> **Status:** built on `main` in 2777a984, its screenshots still to be approved.
>
> **Summary:** Today a player whose battle units are all defeated is defeated, and the other player gets the victory,
> which ends the game: the banner says "Human wins!" or "CPU wins!" and the screen locks. The human player only wins
> once every CPU battle unit of the battlefield is defeated at the same time, the far ones and the ones waiting to
> respawn included, which the open world makes all but impossible. The CPU player wins when the party is wiped out, and
> the screen locks with the virtual pad hidden, so the player can't even quit to the main menu. A party member's summon
> also holds the CPU's victory off until it vanishes. Instead, the CPU player is never defeated, and the game has no
> winning end. The only end is the game over, when the last party member leaves the party, in a battle or while
> exploring: the banner says "Game over!", the battle in progress finishes quietly, and the call to action offers Quit
> to menu. It builds on the glossary's **Game over**, **Finished battle** and **Quit**, and on
> [the event bus handling events in publication order](../adr/0014-event-bus-handles-events-in-publication-order.md).

## Decisions

| Topic            | Decision                                                                                                                                                                                                                                                                                                         |
|------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope            | Taking the player defeat, the victory and the winner out of the game, and adding the game over, with its banner and its Quit to menu. E.g.: the bat, the only CPU battle unit of a test battlefield, is defeated, and the party explores again.                                                                  |
| No victory       | No player is ever defeated, and the game has no winning end, as there are always more CPU battle units, far away or respawning. Finishing the main quest gives its reward and play goes on. Chosen over the main quest ending the game, which waits for the story.                                               |
| CPU player       | The CPU player is never defeated: its battle units are defeated one by one and respawn. When every one of them is defeated at once, the battle disengages and finishes as any other, with nothing more.                                                                                                          |
| Game over        | The end of the game, when the last party member leaves the party, in a battle or while exploring (e.g.: lava, or a venom). Chosen over the party respawning somewhere safe, a bigger feature left for later, and over keeping "CPU wins!" as the losing end.                                                      |
| Summons          | Only party members count: a summon of the party left in the battle doesn't hold the game over off, and vanishes as the battle finishes. Chosen over waiting for the last summon to go, as today.                                                                                                                |
| In a battle      | The game over replaces the end of the battle: the battle finishes, its summons vanish, but the banner doesn't announce "Battle over!" and the party doesn't explore again. Chosen over "Battle over!" followed by "Game over!".                                                                                   |
| Finishing it     | `LeaveParty` publishes `GameOver` followed by `RequestFinishBattle`, and `battle` finishes the battle in progress, if any. Chosen over finishing only once the battle disengages, which leaves a summon and the CPU's turns running under the locked screen, and over the screen asking for it, a game rule in the screen. |
| Party state      | A party whose last member left is over: `Party` has a new `Mode.OVER`, which no command leaves, so it never explores, steps, talks, opens the formation, the pause or the quest again. Chosen over guarding only `ResumeExploration` on an empty party.                                                          |
| After it         | The banner says "Game over!" in the middle of the line, and the call to action shows a button row with Quit to menu, which goes back to the main menu with no confirmation, as the game is already over. Chosen over a game over panel to load a save from, and over today's locked screen.                     |
| Texts            | `ui.banner.gameOver`, "Game over!" / "¡Fin de la partida!", replaces `ui.banner.victory`. The button reuses `ui.pause.quit`, "Quit to menu" / "Salir al menú".                                                                                                                                                   |
| Renames          | `BattleFinishedWithoutWinner` becomes `BattleFinished`, and `Battle.finishWithoutWinner` becomes `Battle.finish`, as every battle finishes without a winner.                                                                                                                                                     |
| Architecture     | The game over lives in `party.domain` and `party.usecases`. `battle` gains `FinishBattle` and loses `DefeatPlayer` and `FinishBattleWithWinner`. `battleUnit` loses `AreAllBattleUnitsDefeated`. The subdomain dependencies don't change, as `party` already depends on `battle`.                                |
| Saves            | Nothing is saved: a game over can't be saved, as the pause screen can't be opened. `Save.SAVE_VERSION` stays 9, and older saves load.                                                                                                                                                                            |
| Storybook        | **Screen / Banner › Winner** becomes **Screen / Banner › Game over**. The Quit to menu row is a `ButtonRowView`, which its stories already cover.                                                                                                                                                                |
| Tests            | A new `GameOverAcceptanceTest`, the acceptance tests of the victory rewritten to show the game going on, unit tests for `Party`, `LeaveParty`, `Battle` and `FinishBattle`, and presenter and view tests for the banner, the exploration, the quests and finish turn. No end-to-end test.                         |
| Out of scope     | The party respawning, a game over panel to load a save from, a new end of the game, and the devlogs and done plans, which tell the victory as it was.                                                                                                                                                           |

For example, the party of knight-1 and knight-2 fights rat-1, and knight-1 has summoned a bee:

| Step                    | Today                                                      | Once built                                                                   |
|-------------------------|------------------------------------------------------------|------------------------------------------------------------------------------|
| rat-1 defeats knight-1  | knight-1 leaves the party                                  | knight-1 leaves the party                                                    |
| rat-1 defeats knight-2  | The battle goes on, with the bee left                      | `GameOver`: "Game over!", Quit to menu. The battle finishes, the bee vanishes |
| The bee defeats rat-1   | "Battle over!", the bee vanishes, then "CPU wins!"         | Doesn't happen: the battle is over                                           |

## party

### Domain

- `PartyEvent.GameOver(playerId)` is the game over.
- `Party.Mode.OVER` is the mode of a party whose last member left. `Party.Dto.isOver` tells it.
- `Party.leave(battleUnitId)` switches the party to `OVER` and publishes `GameOver` when the last member leaves, in
  any mode. E.g.: knight-2, the last member, defeated in a battle, or by lava while exploring.
- `resume()`, `interrupt()`, `stepParty(...)`, `talk(...)`, `openFormation()`, `openPause()` and `openQuest()` return
  the party as it is when it is over, and `canStep()` is false.

### Use cases

- `LeaveParty` takes the `EventBus`, and publishes the party's events followed by `BattleEvent.RequestFinishBattle`
  when the party is over, so the screen knows of the game over before the battle finishes.
- `ResumeExploration` does nothing for a party that is over, as `resume()` returns it as it is.

### Adapters

- `OnBattleFinishedWithoutWinner` becomes `OnBattleFinished`, on `BattleEvent.BattleFinished`.
- `PartyApi` builds `LeaveParty` with the `EventBus`.

## battle

### Domain

- `BattleEvent.RequestFinishBattle` asks for the battle in progress to finish.
- `BattleEvent.BattleFinishedWithoutWinner` becomes `BattleFinished`.
- `Battle.finishWithoutWinner()` becomes `finish()`.
- `BattleEvent.RequestDefeatPlayer`, `PlayerDefeated` and `PlayerVictory` go, with `Battle.defeatPlayer`,
  `Battle.defeatWhileExploring`, `Battle.hasWinner` and `Battle.finishWithWinner`.

### Use cases

- `FinishBattle` finishes the battle in progress and publishes `BattleFinished`. It does nothing when there is no
  battle, or when it is already finished. E.g.: the game over by lava while exploring, after a battle that finished
  earlier.
- `StartNextRound` and `ResolveDisengagedBattle` no longer check `hasWinner()`, only `isFinished`. E.g.: the CPU battle
  units joining the disengaged battle of the game over find it finished, as they would have found today's victory.
- `DefeatPlayer` and `FinishBattleWithWinner` go.

### Adapters

- `OnRequestFinishBattle` runs `FinishBattle` on `RequestFinishBattle`.
- `OnRequestDefeatPlayer` and `OnPlayerDefeated` go, and `BattleApi` loses `defeatPlayer`, `finishBattleWithWinner`
  and `onPlayerDefeated`, and gains `finishBattle`.

## battleUnit

- `RequestLeaveBattle` only publishes `BattleEvent.RequestLeaveBattle`, and loses `AreAllBattleUnitsDefeated`, which
  goes.
- `OnBattleFinishedWithoutWinner` becomes `OnBattleFinished`, which still vanishes the summons left.

## screen

- `BannerView.displayVictory(playerId)` becomes `displayGameOver()`: it takes off the round and the player turn, and
  shows "Game over!" in the middle of the line. The `winner` text becomes `gameOver`.
- `BannerPresenter` displays the game over on `GameOver`, and no longer announces the end of a battle that finishes
  after it.
- `ExplorationPresenter` locks the screen on `GameOver`, as it did on `PlayerVictory`, and shows a button row in the
  call to action with Quit to menu, which runs `onQuit` with no confirmation.
- `GameScene` hands the same `onQuit`, which changes to the `MenuScene`, to `PausePresenter` and
  `ExplorationPresenter`.
- `QuestPresenter` hides the tracker and closes the quest on `GameOver`, as it did on `PlayerVictory`.
- `FinishPlayerTurnPresenter` loses its `PlayerVictory` subscription: `BattleFinished` already hides finish turn, and
  hiding the whole call to action would hide Quit to menu.
- `BattlefieldPresenter`, `BannerPresenter` and `FinishPlayerTurnPresenter` subscribe to `BattleFinished`.
- `UiMessage.BANNER_VICTORY` becomes `BANNER_GAME_OVER`, on `ui.banner.gameOver`.

## localization

- `messages.pot` and `es.po` replace `ui.banner.victory` with `ui.banner.gameOver`:

  ```po
  #. The banner when the last party member leaves the party.
  msgctxt "ui.banner.gameOver"
  msgid "Game over!"
  msgstr "¡Fin de la partida!"
  ```

## Storybook

- `BannerViewStories`: the `Winner` story (`screen-banner--winner`) becomes `Game over` (`screen-banner--game-over`),
  "The game over, in the middle of the line.", and the `STATE` control's `WINNER` becomes `GAME_OVER`. The component's
  description ends with "or the game over".
- `StorySmokeTest` lists `screen-banner--game-over` in place of `screen-banner--winner`.

## Tests

- `GameOverAcceptanceTest`
  - `should be game over, with the battle finished and no exploring again, when the last party member is defeated in a battle`
  - `should be game over at once, with the summon left vanished, when the last party member is defeated in a battle` (replaces `SummonsAtBattleEndAcceptanceTest`'s `should lose the game when only summons are left when the battle ends`)
  - `should be game over when the last party member is defeated by the lava while exploring`
  - `should not be game over when a party member is defeated and another is left`
- `EngagementAcceptanceTest`
  - `should explore again when the last cpu battle unit of the battlefield is defeated` (replaces `should only have the human player win, without exploring again, when the last cpu battle unit is defeated`)
- `ExploringTerrainEffectAcceptanceTest`
  - `should keep exploring when the last cpu battle unit is defeated by the lava while exploring` (replaces `should have the human player win when the last cpu battle unit is defeated by the lava while exploring`)
- `SummonsAtBattleEndAcceptanceTest`
  - `should vanish the summons left when the battle ends, before exploring again` no longer checks the winners.
- `PartyTest`
  - `Leave should switch the party over and publish the game over when the last member leaves the party` (replaces `Leave should have no leader when the last battle unit leaves the party`)
  - `Leave should keep exploring and publish no game over when a member is left`
  - `Resume should keep the party over when the game is over`
  - `OpenPause should not open the pause when the game is over`
  - `CanStep should be false when the game is over`
- `LeavePartyTest`
  - `should publish the game over and request the battle to finish when the last member leaves the party`
- `ResumeExplorationTest`
  - `should not explore again when the game is over`
- `OnBattleFinishedTest` (party, renamed from `OnBattleFinishedWithoutWinnerTest`)
  - `should explore again when a battle finishes`
- `BattleTest`
  - `Finish should finish the battle and publish that it finished when it is disengaged` (replaces `FinishWithoutWinner should ...`)
  - The `HasWinner`, `FinishWithWinner`, `DefeatPlayer` and `DefeatWhileExploring` tests go.
- `FinishBattleTest` (replaces `FinishBattleWithWinnerTest`)
  - `should finish the battle in progress and publish that it finished`
  - `should do nothing when the battle is already finished`
  - `should do nothing when there is no battle`
- `OnRequestFinishBattleTest` (replaces `OnRequestDefeatPlayerTest` and `OnPlayerDefeatedTest`)
  - `should finish the battle when it is requested to finish`
- `StartNextRoundTest` and `ResolveDisengagedBattleTest`: their `... without a winner ...` tests drop "without a winner".
- `RequestLeaveBattleTest`
  - `should request the battle unit to leave the battle` (replaces both tests)
- `OnBattleUnitVanishedTest`
  - `should request the summon to leave the battle when it vanishes` (replaces `should request the summon to leave the battle, and its player to be defeated, when its last battle unit vanishes`)
- `DefeatPlayerTest` and `AreAllBattleUnitsDefeatedTest` go.
- `BannerViewTest`
  - `DisplayGameOver should display the game over without the round and the player turn` (replaces `DisplayPlayerWin should display player name when player wins`)
- `BannerPresenterTest`
  - `DisplayGameOver should display the game over when the game is over` (replaces `DisplayPlayerWin should display player win when player victory is published`)
  - `AnnounceBattleEnd should not announce the end of the battle when it finishes after the game over`
- `ExplorationPresenterTest`
  - `Tick should no longer apply the terrain effects and the virtual pad should be hidden when the game is over`, now on `GameOver`.
  - `ExplorationPresenter should display Quit to menu when the game is over` (replaces `ExplorationPresenter should hide the row when the player wins`)
  - `QuitToMenu should go back to the main menu, without confirming, when Quit to menu is tapped after the game over`
- `QuestPresenterTest`
  - `QuestPresenter should hide the tracker when the game is over` (replaces `... when a player wins`)
  - `QuestPresenter should close the detail view when the game is over` (replaces `... when a player wins`)
- `FinishPlayerTurnPresenterTest`
  - `PlayerWins should hide the call to action when a player wins` goes.
- The other tests that build `BattleFinishedWithoutWinner` or call `finishWithoutWinner()` (e.g.:
  `BattlefieldPresenterTest`, `PlayerTurnRuleTest`, `InMemoryBattleRepositoryTest`) follow the renames.

## Known limitations

- The game over is final: the player loads a save from the main menu, with what was played since lost.
- The CPU battle units within reach still join the disengaged battle of a game over, and find it finished, as they do
  today with the victory.

## Changes while building

- `BattleApi` no longer takes a `PlayerApi`: `DefeatPlayer` was the only use `battle` made of `player`, so
  `SubdomainDependencyArchitectureTest` lets `battle` depend on `shared` only.
- Quit to menu runs `PausePresenter.quit()`, which runs the `onQuit` `GameScene` already gives it, in place of
  `GameScene` handing the same `onQuit` to `ExplorationPresenter` too, so quitting stays in one place.
- `Party.stepParty` and `Party.swapLeader` still fail with `NotExploring` once the party is over, as they do in every
  mode but exploring, in place of returning the party as it is. Nothing calls them then, as `canStep()` is false.
- `ResolveDisengagedBattleTest`'s `should not finish the battle when the player who left it is defeated` goes, as no
  player is defeated any more, and `BattleMother.finishedBattle` finishes the battle in place of defeating a player.
- `ExploringTerrainEffectAcceptanceTest`'s new test checks that there is no battle at all, as a defeat while exploring
  no longer creates one.
