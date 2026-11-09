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

## The button row

[`ButtonRowView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/ButtonRowView.kt) holds the
buttons of a panel at the level of Finish turn and as tall as it, so a thumb has one place to go. They share the 390 of
the row with a spacing of 10: Back alone is 390, Back and Abandon are 190 each, and three buttons are 123.3 each. The
first one always starts on the left, so Back does not jump when stepping out. Each is grey, but for an action that can
not be undone, in `HudStyle.DANGER`, and Buy, blue as Finish turn, in `ConfirmButton.COLOR`. A disabled one fades as any
other.

The presenter of the panel owns the buttons and displays them with `CallToActionView.displayButtons`. A tap does
not dismiss them, unlike the item buttons: the presenter displays what follows. `hideButtons` removes the row and
nothing else, so an item found with a full inventory stays, and Finish turn returns when the battle has one.

The row sits 15 below the 300 of the panels, as it does below the HUD, also in the main menu, where it is at y 587.

## Where it is used

- [`InventoryView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/InventoryView.kt): the arrows
  that switch the party member, disabled at the ends of the party, and Close.
- [`ShopView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/ShopView.kt): the arrows that switch
  the buyer. Back and Buy or Sell are in the button row.
- [`ItemButtonsView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/ItemButtonsView.kt): the
  actions on the selected item, e.g.: Give, disabled as `No one` or `No space`, with Consume, Equip and Unequip in blue
  (`ConfirmButton.COLOR`).
- [`DiscardQuestionView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/DiscardQuestionView.kt):
  Back and Discard, in red, of the question asked before discarding an item, in a `ButtonRowView` of its own.
- [`WorldBuilderHudView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/WorldBuilderHudView.kt): the
  steps of the rows and the columns, disabled out of the allowed sizes.
- [`FinishPlayerTurnView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/FinishPlayerTurnView.kt): Finish
  turn, blue, disabled while the turn can not be ended.
- [`ButtonRowView`](../../src/commonMain/kotlin/com/mkz/rpg/screen/adapters/presentation/ButtonRowView.kt): Back and
  Abandon of the quest journal and the detail view, Back of the save slots, both choices of every question, Close of the
  formation, Back and Buy or Sell of the shop, disabled with the reason it can not, e.g.: `Not enough gold`, and Leave, Sell
  and Buy of a merchant.

A new button of a view is a `gameButton`, so a disabled one looks disabled. The buttons drawn with images (e.g.: the
abilities, the inventory button, the virtual pad, the terrains of the world builder) and the Storybook's own buttons are not.

## Seeing and testing it

- The **Shared / Game button** stories of the [Storybook](storybook.md) show it enabled, disabled and in another color,
  and **Screen / Action row** the row, from a lone button to three.
  The stories of each view above show its disabled buttons, e.g.: **Screen / Inventory › Exploring, only member**.
- `GameButtonTest` checks it fades when disabled, and not once enabled again. The page object `isFaded()`, in
  `GameButtonExtension`, lets the tests of each view check its disabled buttons are displayed faded, besides disabled.
