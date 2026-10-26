# Plan: Display the health bar of a battle unit whose health changes while exploring

> **Status:** done.
>
> **Summary:** While exploring, a battle unit standing on lava or water only blinks in white every second, so the player
> can not tell what is happening to it. Instead, every time the health of a battle unit changes while exploring, the
> health bar of the attack preview (in thirds of its maximum health) is displayed over its tile, with the health it has
> left, for as long as it blinks.

## Decisions

| Topic       | Decision                                                                                                                                                                                                                                   |
|-------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope       | Every health change while exploring, whatever its source: a terrain (lava, water), an item used from the inventory. It follows `BattleUnitDamaged` and `BattleUnitHealed`, so the effects that do not change the health (e.g.: mana, teleport, equipped items) display nothing. |
| Battle      | Unchanged: while battling, the health changes only flash, and the attack preview displays the health a cast would leave beforehand.                                                                                                         |
| Bar         | The thirds bar of the attack preview (`BattlefieldHealthBarPreviewView`), at the top of the tile, with the health the battle unit has left after the change. The bar tells the health level, not whether it went up or down: the terrain does (lava hurts, water heals). |
| Full health | A heal at full health still displays the bar, full, so the player sees the water would heal.                                                                                                                                               |
| Duration    | As long as a flash: `FLASH_COUNT` × 2 × `FLASH_DURATION` (480ms). Standing on lava, the bar shows up once per second with the flash. It is removed as soon as the battle unit leaves its tile (a step, a swap, defeated), like the flash. |
| Timing      | Played alongside the timeline (`playAlongside`), as the flash while exploring, so it never holds the party.                                                                                                                              |
| Who         | Every battle unit, the CPU battle units resting included, as every battle unit that receives the effect flashes.                                                                                                                         |
| Selection   | Its own layer (`HEALTH_FLASH`), apart from the attack preview (`HEALTH_PREVIEW`): it keeps the selection of the tile (e.g.: the inventory open), and resetting the tiles does not remove it.                                              |
| Domain      | Unchanged: the presenter reads the health the battle unit has left when the event arrives, as the presenters do.                                                                                                                          |

For example, the knight, with 30 health points, stands still on lava, which deals 5 damage every second:

| Time    | Health | Displayed over the knight's tile                          |
|---------|--------|-----------------------------------------------------------|
| 0ms     | 25     | It blinks, and the bar shows its last third blinking.     |
| 480ms   | 25     | Nothing.                                                  |
| 1000ms  | 20     | It blinks, and the bar shows its second third blinking.   |
| 1200ms  | 20     | The knight steps on sand: the blink and the bar are gone. |

## BattlefieldPresenter

- `BattleUnitDamaged` and `BattleUnitHealed`, while exploring (`!isBattling()`), read the battle unit and its tile, and
  play `battlefieldView.flashHealth` alongside the timeline with `PreviewOption.of(remaining, maximum)`. A battle unit
  that is not on the battlefield displays nothing.

## BattlefieldView

- `flashHealth(row, column, previewOption)` displays a `BattlefieldHealthBarPreviewView` on the `HEALTH_FLASH` layer of
  the tile, as `displayHealthPreview` does, but keeps the selection. It replaces the bar already on the tile, and removes
  it once a flash has blinked.
- `removeBattleUnit` removes it, stopping its blinks, as it stops the flashes.
- `HEALTH_FLASH` is an animated layer, like `FLASH`: the tile is kept on the screen while it is displayed.

## Tests

- `BattlefieldPresenterTest`
  - `FlashHealth should flash the health left of the battle unit alongside the timeline when it is damaged while exploring`
  - `FlashHealth should flash the full health of the battle unit when it is healed at full health while exploring`
  - `FlashHealth should not flash the health of the battle unit when it is damaged in a battle`
- `BattlefieldViewTest`
  - `FlashHealth should display the health over the tile, keeping its selection, and remove it once a flash has blinked`
  - `FlashHealth should remove the health when the battle unit leaves its tile`
  - `FlashHealth should replace the health already displayed on the tile when it is flashed again`
