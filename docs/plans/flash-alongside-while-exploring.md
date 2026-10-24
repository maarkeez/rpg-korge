# Plan: Flash the effects alongside while exploring, so the party never freezes

> **Status:** done.
>
> **Summary:** While exploring, a party of 3 standing on water never walks again, however often the virtual pad is
> tapped. Every second, each battle unit standing still on water is healed, and flashes on the timeline for 480ms, one
> after another. 3 battle units add 1440ms of flashes every second, so the timeline never goes idle, and
> `ExplorationPresenter` only walks once it is. Instead, while exploring, the flash of a received effect is played
> alongside the timeline, as the CPU battle units wander, so it never holds the party. A flash stops as soon as its
> battle unit leaves its tile.

## Cause

| Step | What happens                                                                                                                                                                         |
|------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1    | `ExplorationPresenter.tick` calls `ApplyStandingTerrainEffects` every second: every battle unit standing still on water is healed (`water-heal`), and publishes `EffectReceived`. |
| 2    | `BattlefieldPresenter.flashUnit` plays each flash in the timeline (`timeline.play`): `FLASH_COUNT` × 2 × `FLASH_DURATION` = 3 × 160ms = 480ms, one after another.                 |
| 3    | `ExplorationPresenter.walkParty` and the pending press do nothing while `timeline.isPlaying`.                                                                                       |
| 4    | 3 battle units: 1440ms of flashes per second. The timeline falls 440ms further behind every second, so it never goes idle. 2 battle units leave a 40ms window per second.        |

The standing battle units include every CPU battle unit on the battlefield, in sight or not, and the heal flashes even
at full health. So a single party member on water next to two bats resting on water out of sight freezes the party too.

## Decisions

| Topic         | Decision                                                                                                                                                                                                                       |
|---------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Timing        | While exploring, the flash of an `EffectReceived` is played alongside the timeline (`playAlongside`): it starts straight away and does not make the timeline playing, so the party walks meanwhile, whatever the number of flashes. |
| Battle        | Unchanged: while battling, the flashes are played in the timeline, one after another, so each effect is seen before the next step.                                                                                            |
| Scope         | Every `EffectReceived` while exploring, whatever its source: a terrain, an equipped item, an item used. The recruit flash (`PartyJoined`) and the vanish flash (`BattleUnitVanished`) stay in the timeline: they are one-off, and must be seen. |
| Moving        | A flash stops as soon as its battle unit leaves its tile (a step, a swap): its silhouette is removed, and it no longer blinks, so it never blinks over an empty tile or the follower stepping on it. The effect itself was already applied. |
| Same tile     | The flashes on one tile still follow one another (e.g.: an item equipped with several effects), so two silhouettes never blink on one tile at once. `BattlefieldView` chains them, as it knows the flashes of each tile.        |
| Scroll        | Unchanged: `followTile` does nothing while trailing, so a flash alongside never scrolls the battlefield.                                                                                                                        |
| Out of scope  | The heal still flashes at full health, and the CPU battle units out of sight still receive their terrain effect and flash. Both are cosmetic once the flashes no longer hold the party.                                       |
| Domain        | Unchanged: only the presentation changes.                                                                                                                                                                                       |

For example, the knight, the archer and the viking stand still on water:

| Time    | Today                                                                       | With this plan                                       |
|---------|-----------------------------------------------------------------------------|------------------------------------------------------|
| 0ms     | The three are healed. The knight flashes; the archer and the viking wait.  | The three are healed, and flash at once, alongside.  |
| 100ms   | The player taps north. It is remembered.                                    | The player taps north. The party walks north.        |
| 1000ms  | The archer is flashing. Three more flashes are queued.                     | The party has left the water: nothing flashes.       |
| 1440ms  | The viking is flashing. The timeline is never idle: the party never walks. | —                                                    |

## BattlefieldPresenter

- `EffectReceived` flashes with `timeline.playAlongside` while exploring (`!isBattling()`), and with `timeline.play`
  while battling, as `wanderUnit` chooses its pace.
- `PartyJoined`, `OnTurnStartedEffectApplied` and `BattleUnitVanished` are unchanged.

## BattlefieldView

- `flashBattleUnit` remembers the flashes of each tile, and blinks after the ones already there. `removeBattleUnit`
  stops them: the silhouette is removed from the tile, and no more blinks are drawn. `moveBattleUnit` and `swapBattleUnits` already remove the battle units from
  their tiles before gliding, so a step or a swap stops the flash.
- `removeBattleUnit` keeps the other layers (`KEPT_LAYERS`) as today.

## Tests

`BattlefieldPresenterTest`:

- `FlashUnit should flash the battle unit alongside the timeline when it receives an effect while exploring`: the flash
  is drawn, and the timeline is not playing.
- `FlashUnit should flash the battle unit in the timeline when it receives an effect while battling`: the timeline is
  playing until the flash completes.

`BattlefieldViewTest`:

- `FlashBattleUnit should stop blinking when the battle unit leaves its tile`: once moved, the tile no longer flashes,
  and the flash completes.
- `FlashBattleUnit should blink after the flash already on the tile when the battle unit is flashed twice at once`.

`ExplorationPresenterTest`:

- `WalkParty should walk the party when an arrow is tapped while its three members flash every second on water`: the
  bug itself. A `BattlefieldPresenter` on the same timeline, whose fake view's flashes do not complete; three party
  members are healed by water every second for 3 seconds; the tapped arrow walks the party.

## Docs

- [Presentation timeline](../architecture/presentation-timeline.md): the flashes of the received effects are played
  alongside while exploring, and stop once their battle unit leaves its tile. In a battle, they are still played one
  after another.
- [Exploration](../architecture/exploration.md#walking): a terrain effect flashing no longer holds a tap, so the
  example of the buffered press is a walk or a recruit, not a flash.
