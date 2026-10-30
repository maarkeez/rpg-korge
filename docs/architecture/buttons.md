# Buttons

The buttons of the game's views are [`GameButton`](../../src/commonMain/kotlin/com/mkz/rpg/shared/adapters/presentation/GameButton.kt)s,
in `shared.adapters.presentation`, added with `gameButton(label, size)`: a KorGE `UIButton` with the look of the game,
written once instead of in every view.

## The look

| State    | Background                                     | Label                  | Border                   |
|----------|------------------------------------------------|------------------------|--------------------------|
| Enabled  | Grey, `DIMGRAY` (`#696969`), or another color  | White                  | Light grey (`LIGHTGRAY`) |
| Over     | Darker grey (`#555555`), or another color      | White                  | Light grey               |
| Disabled | Dark grey (`#3A3A3A`), whatever its color      | Grey (`#8A8A8A`)       | Dim grey (`#555555`)     |

A disabled button is still displayed, but faded, and tapping it does nothing: the player tells at a glance which
buttons can be tapped, e.g.: the arrows that switch the party member when the party has only one, or Give reading
`No one`. A button of another color fades the same way: Finish turn, blue while it can be tapped, turns dark grey.

KorGE's own disabled look is not enough: it only changes the background, to `#777777`, as close to the game's grey as
to be told apart, and keeps the label white. `GameButton` sets the disabled background and fades the label and the
border with it, whenever `enabled` changes.

## Where it is used

- [`InventoryView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/InventoryView.kt): the arrows
  that switch the party member, disabled at the ends of the party, and Close.
- [`ShopView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/ShopView.kt): the arrows that switch
  the buyer, Back, and Buy or Sell, disabled with the reason it can not, e.g.: `Not enough gold`.
- [`ItemActionsView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/ItemActionsView.kt): the
  actions on the selected item, e.g.: Give, disabled as `No one` or `Bags full`.
- [`MapBuilderHudView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/MapBuilderHudView.kt): the
  steps of the rows and the columns, disabled out of the allowed sizes.
- [`FinishTurnView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/FinishTurnView.kt): Finish
  turn, blue, disabled while the turn can not be ended.

A new button of a view is a `gameButton`, so a disabled one looks disabled. The buttons drawn with images (e.g.: the
abilities, the bag, the virtual pad, the terrains of the map builder) and the Storybook's own buttons are not.

## Seeing and testing it

- The **Shared / Game button** stories of the [Storybook](storybook.md) show it enabled, disabled and in another color.
  The stories of each view above show its disabled buttons, e.g.: **Screen / Inventory › Exploring, only member**.
- `GameButtonTest` checks it fades when disabled, and not once enabled again. The page object `isFaded()`, in
  `GameButtonExtension`, lets the tests of each view check its disabled buttons are displayed faded, besides disabled.
