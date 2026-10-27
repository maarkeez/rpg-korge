# Plan: Switch the inventory between party members while exploring

> **Status:** done.
>
> **Summary:** While exploring, opening another party member's inventory means closing the bag, tapping that member's
> tile, and tapping its bag again. Reorganizing the bags of the party (e.g.: giving items, then checking the receiver's
> bag) takes three taps per member. Instead, the inventory's title line names the member between two arrows, as the
> shop's header names the buyer: the arrows, or swiping the inventory left or right, switch it to the previous or next
> party member, which is selected on the battlefield meanwhile.

## Decisions

| Topic          | Decision                                                                                                                                                                                                        |
|----------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| When           | Only while exploring. In a battle, the selected battle unit is the one acting, so the title line reads `Inventory`, without arrows, as before.                                                                  |
| Title line     | `< Knight ........ 20 G >  Close`: the member's name instead of `Inventory`, its gold before the next arrow, and Close kept on the right. No bag count, as the slots show it.                                      |
| Switching      | The arrows, or swiping the inventory at least 40 px left or right (`SWIPE_THRESHOLD`), as the shop does. Party order, from the leader, stopping at the ends: the arrows are disabled there, and shown for a party of one. |
| Selection      | The member switched to is selected on the battlefield, as if its tile was tapped (`BattlefieldPresenter.select`): its tile is framed, and the others dimmed. Close then displays its info, and tapping its tile clears the selection. |
| Scrolling      | None: the members are next to the leader, which the camera trails, as for the shop's buyer.                                                                                                                    |
| Item selection | Cleared, as switching the shop's buyer does: the call to action is dismissed.                                                                                                                                   |
| Blocked        | Ignored while the timeline plays, while giving an item (only Cancel leaves give mode), and while choosing what to do with an item found while the bag is full.                                                |
| Tapping a tile | Unchanged: tapping another member's tile displays its info, with its bag.                                                                                                                                      |
| Domain         | Unchanged: the presenter reads the party order and the member's name, as the shop does.                                                                                                                        |

For example, the knight leads the party, followed by the viking and the mage:

| The player                     | The game                                                                                              |
|--------------------------------|-------------------------------------------------------------------------------------------------------|
| Taps the knight, then its bag  | `< Knight ........ 20 G >`, `<` disabled. The knight's tile is framed.                                |
| Gives the hammer to the viking | The hammer leaves the knight's bag, and the viking flashes.                                           |
| Taps `>`                       | `< Viking ........ 30 G >`, the hammer last in its bag. The viking's tile is framed instead.          |
| Swipes right                   | `< Mage ........ 0 G >`, `>` disabled.                                                                |
| Taps Close                     | The mage's info, with its bag.                                                                        |

## battlefieldHud

- `SelectBattleUnit` gives no movement range out of the battles (`searchBattle`), as tapping a tile does while exploring.

## screen

- `InventoryView.display(slots, gold, owner)`: an `Owner(name, partyIndex, partySize)` names the member between the
  arrows, disabled at the ends of the party. Without one, the title reads `Inventory` and the arrows are hidden.
- `InventoryView.Delegate.inventoryOwnerStepped(step)`: -1 or 1, from the arrows, or a swipe of the whole inventory.
- `InventoryPresenter` passes the owner while exploring, when opening and refreshing the inventory. Stepping selects the
  member through the selector set with `setBattleUnitSelector`, and remembers it until its `SelectedBattleUnit`
  arrives, which then opens its inventory, after the battlefield HUD displays its info. Every other action waits
  meanwhile.
- `BattlefieldPresenter.select(battleUnitId)` selects the battle unit, as when a human turn starts. `BattleScene` sets
  it as the inventory's selector.

## Tests

- `SelectBattleUnitTest`
  - `should select the battle unit with no movement range out of the battles`
- `InventoryViewTest`
  - `Display should name the owner between both arrows enabled, the gold before the next one`
  - `Display should disable the arrows at the ends of the party`
  - `Display should hide the arrows and display the inventory title again when there is no owner`
  - `ArrowClick should notify the delegate of the step when an arrow is clicked`
  - `Swipe should notify the delegate of the direction, without selecting a slot`
  - `Swipe should not notify the delegate when the drag is too short`
- `InventoryPresenterTest`
  - `OpenInventory should display the inventory title, without the arrows, in a battle`
- `InventoryPresenterSwitchTest`
  - `OpenInventory should name the party member and its position in the party while exploring`
  - `InventoryOwnerStepped should select the next party member on the battlefield and display its inventory`
  - `InventoryOwnerStepped should select the previous party member`
  - `InventoryOwnerStepped should stop at the ends of the party`
  - `InventoryOwnerStepped should do nothing while the scene plays the previous steps`
  - `InventoryOwnerStepped should do nothing while giving an item`
  - `SelectedBattleUnit should display the info of a party member selected on its tile once the inventory has switched`

## Not in this plan

- Switching in battles.
- Dragging an item from one member's bag onto another's.
