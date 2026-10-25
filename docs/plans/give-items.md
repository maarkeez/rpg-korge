# Plan: Give items

> **Status:** done.
>
> **Summary:** While exploring, only the leader walks where the player says, so the other party members hardly ever
> loot. A stored item gets a Give action while exploring: the player taps the tile of another party member, and the item
> moves from the giver's bag to the receiver's, one item at a time. Battles, shops, equipped items and gold are left
> unchanged.

## Decisions

| Topic             | Decision                                                                                                                                                                                          |
|-------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Mechanic          | A Give action on a stored item moves that one item from the giver's inventory to another party member's. No trade panel, and found loot is not shared out automatically.                             |
| When              | Only while exploring (`isExploring`): not in battles, and not while talking to an npc, the merchants included. Giving does not stop time.                                                       |
| Who               | Both battle units are members of the human player's party, and they are not the same one. The distance between them does not matter.                                                             |
| Item actions      | Give is added while exploring only, before Discard: `Consume, Give, Discard, Cancel` and `Equip, Give, Discard, Cancel`. The battle actions stay as they are.                                     |
| Equipped item     | Give is disabled for the only stored copy of an equipped item (`EQUIPPED`), as Discard and Sell are: the player unequips it first. When another copy is stored, that copy is given.                 |
| Receiver full     | A member with a full bag can not receive. Its tile stays dimmed, and `giveItem` fails with `NoStorageSpaceLeft`.                                                                                |
| No receiver       | Give is disabled and reads why when no member can receive: `No one` when the giver is alone in the party, `Bags full` when every other member's bag is full. Kept short for a 4-button row (~90px each). |
| Gold              | Not giveable: each battle unit keeps its own gold. Giving gold can be a later plan.                                                                                                             |
| Picking receiver  | Give opens give mode: the call to action reads `Tap a party member to give the Hammer` with Cancel, and every tile is dimmed except the tiles of the members that can receive (`dimTilesExcept`). |
| Give mode taps    | Tapping a receiver's tile gives the item. Every other tap does nothing: dimmed tiles, the giver's own tile, and the bag's slots. Only Cancel leaves give mode, back to the item's actions.      |
| After giving      | The giver stays selected with its bag open, and the selection is cleared, as after Sell. The tiles are no longer dimmed, and the receiver flashes on its tile (`flashBattleUnit`).             |
| Receiver's bag    | The item is stored at the end, not equipped, whatever it is.                                                                                                                                    |
| A battle starting | Give mode is abandoned, as an open bag is: the battle HUD takes over.                                                                                                                           |

For example, the knight leads the party, followed by the viking (bag 3/12) and the mage (bag full):

| The player                       | The game                                                                                                    |
|----------------------------------|-------------------------------------------------------------------------------------------------------------|
| Taps the knight, then its bag    | The knight's bag opens: `Inventory ........ 20 G`.                                                          |
| Taps the hammer                  | `Hammer ........ 50 G` with Equip, Give, Discard and Cancel.                                                |
| Taps Give                        | `Tap a party member to give the Hammer` with Cancel. Every tile is dimmed but the viking's.                |
| Taps the mage's tile             | Nothing happens: its bag is full.                                                                           |
| Taps the viking's tile           | The hammer leaves the knight's bag, and the viking flashes. The knight's bag stays open, nothing selected. |
| Taps the viking, then its bag    | The viking's bag shows the hammer last (4/12), ready to be equipped.                                       |

## inventory

- `Inventory.giveRefusal(lootableItemId)`: `NOT_STORED` or `EQUIPPED`, with the same rules as `sellRefusal`. `null` when the item can be given.
- `Inventory.give(lootableItemId, receiverBattleUnitId)` removes a stored copy that is not equipped, and announces `ItemGiven(battleUnitId, receiverBattleUnitId, lootableItemId)`.
- `Inventory.receive(lootableItemId, giverBattleUnitId)` stores it at the end, failing with `NoStorageSpaceLeft` when full, and announces `ItemReceived(battleUnitId, giverBattleUnitId, lootableItemId)`.
- `GiveItem(giverBattleUnitId, receiverBattleUnitId, lootableItemId)` checks `GiveRule`, updates both inventories, saves both, and publishes the giver's events, then the receiver's. It saves nothing when either side fails.
- `GiveRule` (beside `TradeRule`, through `SearchParty`): the party is exploring, and both battle units are distinct members of it. Otherwise it fails with `NotExploringTheParty` or `NotAPartyMember`.
- `SearchGiveRefusal(giverBattleUnitId, lootableItemId)` answers a `GiveRefusalDto` (`NOT_EXPLORING`, `NOT_STORED`, `EQUIPPED`, `NO_ONE`, `BAGS_FULL`) or `null`, so the item actions disable Give and read why.
- `SearchGiveReceivers(giverBattleUnitId)` answers the party members, the giver excluded, that have space left, in party order, so give mode only lights their tiles.
- `InventoryApi` exposes `giveItem`, `searchGiveRefusal` and `searchGiveReceivers`.

## screen

- `PlayerCallToActionView` keeps what an item action displayed instead of dismissing it (`displayGiveItem`, with `GiveItemView`).
- `InventoryPresenter` adds Give to the item actions when the party is exploring, enabled or reading `searchGiveRefusal`.
- Give mode lives in the presenter: it remembers the item being given, displays the call to action with Cancel, and dims every tile but the receivers' ones.
- `BattlefieldPresenter` hands each tapped tile to an interceptor first (`setTileInterceptor`), the presenter while in give mode, instead of selecting or clearing: a receiver's tile calls `giveItem`, any other tile is ignored. Selecting a bag slot is ignored too.
- `ItemGiven` refreshes the giver's inventory, clears the selection and undims the tiles. `ItemReceived` flashes the receiver, and refreshes its inventory when it is the one displayed.
- `ExplorationPaused` (a battle starting) and the giver's defeat (e.g.: by lava) abandon give mode and undim the tiles. A receiver's defeat displays the item's actions again.

## Docs

- `inventory.md` gets a Giving section and the `giveItem`, `searchGiveRefusal` and `searchGiveReceivers` rows. "the party never shares it" stays true for gold.
- `exploration.md`'s Inventory section describes Give and give mode, and `presentation-timeline.md`'s item actions table gets the Give column while exploring.

## Not in this plan

- Giving gold.
- Giving in battles, while shopping, or the found item while the inventory is full.
- Sharing found loot out to the next member with space.
