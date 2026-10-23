# Plan: A responsive virtual pad, and a slower party

> **Status:** done.
>
> **Summary:** While exploring, a quick tap on an arrow sometimes does nothing, and the player has to tap again before
> the leader moves. The pad only asks to walk while the arrow is down, and the presenter ignores it while the timeline is
> playing. A tap that starts and ends while the timeline is busy is therefore lost, e.g.: while a battle unit standing on
> lava or water flashes with its terrain effect, about 480ms every second. Instead, the presenter remembers the last
> press and acts on it as soon as the timeline is idle. A is buffered the same way. Separately, the party walks too fast,
> so its steps are played at 3/4 of the current speed.

## Decisions

| Topic           | Decision                                                                                                                                                                                                                                         |
|-----------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Cause           | `ExplorationPresenter.walkParty` returns while `timeline.isPlaying`, and the pad only calls it while the arrow is held. The terrain flashes (`EffectReceived`, `flashUnit`, 3 × 160ms) are the usual culprit when standing still, and the party's own steps after a walk. |
| Buffer          | One pending press: the last arrow or A pressed. A newer press replaces it, so mashing never queues several steps. Tapping three times during a walk walks once more, not three times.                                                            |
| When it acts    | On the first frame the timeline is idle and the pad is displayed. Waiting is accepted: a tap during a flash walks up to about 480ms later. The flashes stay on the timeline.                                                                      |
| Consumed        | The press is consumed when it is acted on, even if nothing happens (e.g.: a blocked tile, or no npc next to the leader), so it never fires later.                                                                                                |
| Dropped         | When the pad is hidden (a battle starts, a dialog, the inventory, a found item that does not fit, the game over). It does not time out otherwise.                                                                                                 |
| Holding         | Unchanged: holding an arrow keeps walking on every idle frame. The press of a held arrow is consumed by its first walk, so the pending press never adds a step to it.                                                                              |
| A               | Buffered as the arrows. An arrow pressed after A replaces it, and the other way around.                                                                                                                                                          |
| Where           | `ExplorationPresenter`, which knows the timeline. The view stays unaware of it.                                                                                                                                                                  |
| Party speed     | `EXPLORING_STEP_DURATION` from 75ms to 100ms, 3/4 of the speed. Each member still steps one after another, so a larger party still walks slower per tile, as today (100ms per member).                                                           |
| Wandering       | Unchanged: the CPU battle units wandering while exploring keep stepping in 75ms, under a new `WANDERING_STEP_DURATION`.                                                                                                                           |
| Swap            | Unchanged: `SWAP_DURATION` stays 150ms.                                                                                                                                                                                                          |
| Battle          | Unchanged: `MOVE_STEP_DURATION` stays 150ms, for the party and for the CPU battle units wandering when a round finishes.                                                                                                                         |
| Domain          | Unchanged: only the presentation changes.                                                                                                                                                                                                        |

For example, the knight stands still next to a rat resting on water:

| Time   | Today                                                         | With this plan                                        |
|--------|---------------------------------------------------------------|-------------------------------------------------------|
| 0ms    | The rat starts flashing (water heals it). The timeline plays. | Same.                                                 |
| 100ms  | The player taps north. Ignored, and released at 180ms.        | The player taps north. It is remembered.              |
| 480ms  | The flash ends. Nothing happens.                              | The flash ends. The knight walks north, in 100ms.     |
| 700ms  | The player taps north again. The knight walks north.          | The knight has already walked.                        |

## VirtualPadView

- A new listener, `setOnDirectionPressed`, is called once when an arrow is pressed (`onDown`), before its first hold.
  `setOnDirectionHeld` is still called on every frame while it is held, including the frame it is pressed on.
- A keeps `setOnAPressed`. Its press is reported once, as today.

## ExplorationPresenter

- A `pendingPress`: a direction, A, or nothing. It is set by `setOnDirectionPressed` and `setOnAPressed`, and replaces the
  previous one.
- `tick` (already called on every frame) acts on it when the timeline is idle and the pad is displayed: it walks the
  party towards the direction, or talks to the adjacent npc, and clears it. When the pad is hidden, it is cleared without
  acting.
- `setOnDirectionHeld` walks as today, when the timeline is idle and the pad is displayed. A walk consumes the pending
  press of the same direction, so the press and the hold of the same frame walk once.
- `tick` returns once the game is over, as today, so a pending press is never acted on afterwards.

## BattlefieldView and BattlefieldPresenter

- `EXPLORING_STEP_DURATION` = 100ms. Its comment says it is the pace of the party walking while exploring.
- `WANDERING_STEP_DURATION` = 75ms (new). `BattlefieldPresenter.wanderUnit` uses it out of the battles, and
  `MOVE_STEP_DURATION` while battling, as today.

## Tests

`ExplorationPresenterTest`:

- `WalkParty should not walk the party while the scene plays the previous steps` becomes `WalkParty should walk the party
  once the scene is idle when an arrow was tapped while it played`: the arrow is pressed and released during a long step,
  the knight has not moved yet, and walks one tile once the step finishes and a frame ticks.
- `WalkParty should walk only towards the last arrow tapped while the scene played`: north then east, it walks east, once.
- `WalkParty should forget the arrow tapped while the scene played when the pad is hidden`, e.g.: a battle unit is
  selected before the step finishes.
- `TalkToAdjacentNpc should not talk while the scene plays the previous steps` becomes `TalkToAdjacentNpc should talk
  once the scene is idle when A was pressed while it played`.

`VirtualPadViewTest`:

- `HoldArrow should report the press of the arrow once, then hold it on every frame`.

`BattlefieldPresenterTest`: the party walks with `EXPLORING_STEP_DURATION`, and the CPU battle units wandering while
exploring with `WANDERING_STEP_DURATION`.

## Docs

- [Exploration](../architecture/exploration.md#walking): the buffered press, the dropped press, and the party pace (100ms
  per tile and member).
- [Presentation timeline](../architecture/presentation-timeline.md#rules): the player still does not act while the
  timeline is playing, but a press is acted on once it is idle, rather than ignored. The wandering pace.
