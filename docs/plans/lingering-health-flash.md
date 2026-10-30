# Plan: Keep the health bar of a battle unit on the screen a bit longer while exploring

> **Status:** done.
>
> **Summary:** While exploring, the health bar displayed over a battle unit whose health changes (see
> [the health bar while exploring](health-bar-on-effects-while-exploring.md)) lasts as long as a flash, 480ms, and is
> removed as soon as the battle unit steps away, 250ms later when it walks. Walking over lava or water, the bar only
> flickers, which makes the screen noisy. Instead, the bar stays 1.5s, blinking only while the battle unit flashes and
> still afterwards, follows the battle unit when it moves, and fades out.

## Decisions

| Topic        | Decision                                                                                                                                                                                                                         |
|--------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Duration     | 1.5s in all (`HEALTH_FLASH_DURATION`), its 200ms fade out (`HEALTH_FLASH_FADE_DURATION`) included. Every health change displays it again for 1.5s, so standing on lava, which hurts every second, the bar never goes away.     |
| Blink        | The third that blinks only blinks as long as a flash (`FLASH_COUNT` × 2 × `FLASH_DURATION`, 480ms), then stays still, at the health left, until it fades out.                                                                    |
| End          | It fades out in its last 200ms, instead of disappearing at once.                                                                                                                                                                 |
| Moving       | It follows the battle unit: it glides with it, step by step, and stays over its destination tile. Also in a swap, each bar with its battle unit, and when a CPU battle unit wanders. A teleport takes it along.                  |
| Arriving     | When the destination tile already displays a health bar of its own (e.g.: the lava of the destination hurt the battle unit while it glided), the newer one stays and the carried one is dropped.                               |
| Removed      | At once when the battle unit is removed from its tile without moving (e.g.: defeated), as today, and when a battle starts (`ExplorationPaused`), so it never mixes with the attack preview of the battle.                         |
| Battle       | Unchanged: while battling, the health changes only flash.                                                                                                                                                                        |
| Domain       | Unchanged.                                                                                                                                                                                                                       |
| Storybook    | A `Health flash` story of the battlefield: the battle unit flashes with the health bar over it once, when the story is displayed. `healthPreview` picks the health left, and changing `replay` plays it again.                   |

For example, the knight, with 30 health points, walks over two tiles of lava, which deals 5 damage on each:

| Time    | Health | Displayed over the knight                                                      |
|---------|--------|--------------------------------------------------------------------------------|
| 0ms     | 25     | It blinks on the first lava, and the bar shows its last third blinking.         |
| 250ms   | 25     | It glides to the second lava, the bar blinking with it.                         |
| 250ms   | 20     | It blinks on the second lava, and its bar replaces the one it carried.          |
| 730ms   | 20     | It stops blinking: the bar shows its two thirds still.                          |
| 1000ms  | 20     | It glides to the sand, the bar with it.                                         |
| 1550ms  | 20     | The bar starts fading out.                                                      |
| 1750ms  | 20     | The bar is gone.                                                                |

## BattlefieldHealthBarPreviewView

- `hold()` stops the blinks, leaving each blinking third as it ends up after the change: drawn when it is health left
  (e.g.: `TWO_OUT_OF_THREE` draws its second third), not drawn when it is empty (`EMPTY`).

## BattlefieldView

- `flashHealth(row, column, previewOption)` displays the bar on the `HEALTH_FLASH` layer of the tile, as today, holds
  it once a flash has blinked, fades it out at the end of `HEALTH_FLASH_DURATION`, and removes it. It keeps track of
  the bars displayed, so a bar replaced or removed meanwhile is neither faded nor removed again, wherever it is.
- `moveBattleUnit` takes the bar off the origin tile before removing the battle unit, glides it with the battle unit,
  and puts it on the destination tile, unless the bar is gone meanwhile or the destination tile displays one of its own.
- `swapBattleUnits` does the same with both battle units.
- `removeBattleUnit` keeps removing the bar, as the battle unit leaves without moving (e.g.: defeated).
- `removeHealthFlashes()` removes every bar displayed, e.g.: when a battle starts.

## BattlefieldPresenter

- `ExplorationPaused`, which stops trailing the leader when a battle starts, also removes the health flashes.

## Storybook

- `BattlefieldViewStories`: the `Health flash` story (`screen-battlefield--health-flash`), a new `HEALTH_FLASH`
  layer, and the `replay` control (`int`, 0 to 9).
- `StorySmokeTest` lists the new story.

## Docs

- [Presentation timeline](../architecture/presentation-timeline.md): how long the bar lasts, that it follows its battle
  unit, and that a battle removes it.

## Tests

- `BattlefieldHealthBarPreviewViewTest`
  - `Hold should stop the blinks and draw the third left when the health is held`
  - `Hold should stop the blinks and not draw the third when the empty health is held`
- `BattlefieldViewTest`
  - `FlashHealth should display the health over the tile, keeping its selection, and remove it once its time is over` (replaces the one removing it once a flash has blinked)
  - `FlashHealth should stop blinking once a flash has blinked and keep displaying the health`
  - `FlashHealth should fade out the health before removing it`
  - `FlashHealth should display the health again for its whole time when it is flashed again`
  - `FlashHealth should glide the health with the battle unit and keep it on its destination tile when it moves`
  - `FlashHealth should keep the health of the destination tile when the battle unit arrives carrying its own`
  - `FlashHealth should swap the health of the battle units with them when they swap`
  - `RemoveHealthFlashes should remove the health displayed on every tile`
- `BattlefieldPresenterTest`
  - `ExplorationPaused should remove the health flashes`
