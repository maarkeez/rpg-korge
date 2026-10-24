# Plan: No finish turn while exploring

> **Status:** done.
>
> **Summary:** While exploring, tapping a battle unit of the party displays the battle HUD and the player call to action
> in place of the virtual pad. The call to action falls back to Finish turn, disabled, as there is no human turn. It is
> confusing: there are no turns while exploring. The same happens once the inventory is opened, and after an item action
> or a found item is chosen. Instead, Finish turn is only displayed during a battle. While exploring, the call to action
> displays nothing, unless the player has a choice to make: the item actions or the inventory full prompt.

## Decisions

| Topic            | Decision                                                                                                                                                                                                  |
|------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| In a battle      | Unchanged: Finish turn is displayed for the whole battle, disabled during the CPU turns and while the timeline plays.                                                                                    |
| Appears          | When the battle starts (`BattleStarted`), with `BattleSceneTimeline.playNext`, so it appears together with the battle HUD, as the virtual pad is hidden, before the battle is announced.                |
| Disappears       | When the battle finishes (`BattleFinished`), in timeline order, after the last steps of the battle. The virtual pad takes the place of the call to action right after (`ExplorationResumed`), as today. |
| While exploring  | The call to action displays nothing. The item actions and the inventory full prompt are displayed in it as today, and once chosen, nothing is displayed again.                                          |
| A new game       | Starts exploring, so nothing is displayed until the first battle starts.                                                                                                                                 |
| Game over        | Unchanged: the call to action is hidden once a player wins (`PlayerVictory`).                                                                                                                            |
| Layout           | The call to action is below the battle HUD, the last view of the scene, so leaving it empty moves nothing.                                                                                               |
| Domain           | Unchanged: only `PlayerCallToActionView` and `FinishTurnPresenter` change, and the presenters calling them.                                                                                              |

## PlayerCallToActionView

The finish turn is what the call to action displays when the player has nothing else to choose. It becomes optional:

- `displayFinishTurn(onTurnFinished)`: unchanged. It displays the finish turn, which is displayed again once the player
  has chosen (e.g.: cancelled a cast, or selected an item action).
- `hideFinishTurn()` (new): forgets the finish turn, and removes it when it is displayed. Any other call to action stays
  displayed.
- `redisplayFinishTurn()` is renamed `dismiss()`: it displays the finish turn when there is one, and nothing otherwise.
  The cancel and confirm, the item actions and `InventoryPresenter` use it once the player has chosen.

## FinishTurnPresenter

- `init` no longer displays the finish turn, as a new game starts exploring. It still disables it, and listens to
  `onPlayingChanged`.
- `BattleStarted`: `timeline.playNext { displayFinishTurn(...) }`, disabled until the human turn is displayed.
- `BattleFinished`: `timeline.play { hideFinishTurn() }`.

## Tests

- `PlayerCallToActionViewTest`: `HideFinishTurn` removes the finish turn, keeps another call to action displayed, and
  `Dismiss` displays nothing afterwards (after the item actions and after the cancel and confirm).
- `FinishTurnPresenterTest`: no finish turn when created, displayed when the battle starts, hidden when it finishes. The
  turn tests start a battle first.
- `ExploreEndToEndTest`: no finish turn when the knight is selected and its bag opened while exploring, and Finish turn
  displayed once the battle starts.
