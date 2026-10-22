# Plan: Shop redesign

> **Status:** in progress (slices 1 and 2 done).
>
> **Summary:** The shop asked too much at once: two tabs and Close on top, the slots and buyer dots in the middle, and
> Cancel and Buy in the call to action under it. The merchant's dialog now offers Buy, Sell and Leave, so the shop opens
> in a single mode, and Back returns to the dialog. The shop becomes one panel: a header with the buyer, the slots, and
> a footer with every button the player taps.

## Decisions

| Topic               | Decision                                                                                                                                                       |
|---------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Buy or Sell         | Chosen in the merchant's dialog, with Buy, Sell and Leave buttons under its line. The shop has no tabs: it opens in the chosen mode.                          |
| Merchant's dialog   | Tapping it no longer opens the shop: only its buttons do something. Leave finishes the talk. Any other npc's dialog is still closed by tapping it.            |
| Leaving the shop    | Back returns to the merchant's dialog, still talking, to switch between buying and selling, or Leave.                                                          |
| Header              | `< Knight · 20 G · Bag 11/12 >`. The arrows and the swipe switch the buyer, stopping at the ends. No merchant's name, as the dialog displays it, and no dots. |
| Footer              | Back on the left. `Tap an item to buy` (or `to sell`) until an item is selected, then its name, price, and Buy (or Sell) on the right.                        |
| Selection           | Tapping the selected item again, or an empty slot, clears the selection. No Cancel. The call to action stays hidden while shopping.                           |
| Disabled actions    | Buy or Sell reads why it is disabled: `Not enough gold`, `Bag full`, `Equipped`, or `Gold full`.                                                               |
| Untradeable items   | The items the buyer can not buy (or sell) are dimmed in the grid.                                                                                              |
| Nothing to sell     | The footer displays `Nothing to sell` when the buyer stores no item.                                                                                           |
| Kept                | The leader buys first, every tile is dimmed but the buyer's and the merchant's, and a bought item stays selected to buy another one.                         |

## Slices

1. **Dialog choices** (done). `NpcDialogView` displays choice buttons under the line. `ExplorationPresenter` gives the
   merchant's dialog Buy, Sell and Leave, and opens the shop in the chosen mode. `ShopView` loses its tabs, and Close
   becomes Back, which returns to the dialog (`ShopPresenter.setOnBack`).
2. **Shop layout** (done). The header with the arrows replaces the merchant's name, the buyer line and the dots. The footer
   replaces the call to action: Back, the hint, or the item with Buy or Sell. No Cancel, and tapping the selected item
   again clears the selection. `ShopPresenter` no longer uses the call to action, which no longer needs to keep its item
   actions once one is selected.
3. **Trade reasons.** `canBuyItem` and `canSellItem` answer why an item can not be traded, instead of `false`. The button
   reads the reason, and the grid dims the items the buyer can not trade.
