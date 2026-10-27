# Plan: Party formation

> **Status:** done.
>
> **Summary:** The party leader is always the first battle unit deployed, so the player can not choose who walks in
> front. Choosing it is a strategy: the CPU battle brain does not target the leader as such, but the leader walks first
> into the reach of the CPU battle units, and the CPU plays first once it engages, so the leader is usually the first
> one hit. While exploring, B opens a formation panel in place of the pad, listing the party in order, the leader
> first, with a crown. Tapping two members swaps them, and Close applies the new order: its first member leads.

## Decisions

| Topic          | Decision                                                                                                                                                                                                 |
|----------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| When           | Only while exploring and idle: B is handled as A is, once the timeline is idle and while the pad is displayed. Never in battles.                                                                         |
| Opening        | B opens the formation panel, also for a party of one, which has nothing to swap.                                                                                                                       |
| Time           | Stops while the panel is open, as while talking: the party is in a new `FORMATION` mode, so it does not walk, no second starts, the CPU battle units do not wander and no terrain effect is applied.     |
| Layout         | A panel in place of the pad, as the shop. A row per party member, in party order: the crown (leader only), the portrait, the name, and the health and mana bars. Under the rows, a hint and Close.       |
| Swapping       | Tapping a member selects its row, tapping another one swaps both in the panel and clears the selection. Tapping the selected row again clears it.                                                        |
| Battlefield    | Every tile but the party's is dimmed while the panel is open, and the selected member's tile is framed. Tapping the battlefield does nothing meanwhile.                                                  |
| Applying       | Close applies the order shown, once (`CloseFormation`), and the party explores again. B does not close it, as the panel hides the pad.                                                                  |
| Party order    | The whole order is the player's: it decides the leader, who leads next when the leader is defeated, the order of the shop and inventory arrows, and the line once the exploration resumes. A recruit still joins last. |
| Moving         | Nobody moves. The line is rebuilt from the new leader, as after a swap (`Line.closestFirst`, ties broken by the new order). On its next walk, the leader reaches the front by swapping with the members in its way. Nothing changes when the order is the same. |
| Leader effects | The camera eases to the new leader. The CPU battle units in sight, the npc A talks to and the battle unit selected when a human turn starts follow it, as they read the party leader.                  |
| Crown          | A new 16x16 icon, `battlefield/crown.png`, drawn after the player's reference: a golden band with two jewels on its points. Only in the panel, not on the battlefield.                                 |
| Texts          | In `messages.pot`, with their Spanish translation.                                                                                                                                                      |

For example, the knight leads the party, followed by the viking and the mage, walking east in a corridor:

| The player        | The game                                                                                                                 |
|-------------------|--------------------------------------------------------------------------------------------------------------------------|
| Presses B         | The panel replaces the pad: `Knight` (crowned), `Viking`, `Mage`. Every tile but the party's is dimmed.                  |
| Taps the mage     | Its row and its tile are framed. The hint reads `Swap Mage with...`.                                                       |
| Taps the knight   | `Mage` (crowned), `Viking`, `Knight`. Nothing is selected.                                                               |
| Taps Close        | The pad is back, and the camera eases to the mage, at the back of the line. Nobody has moved.                            |
| Walks east        | The mage swaps with the viking, then with the knight on the next walk: it walks in front, the knight right behind it.    |

## party

- `Party.Mode.FORMATION`. `Dto.isInFormation`, and `isExploring` is `false` meanwhile, as while talking.
- `Party.openFormation()`: only when the party can walk (exploring, and done walking), publishing `FormationOpened`.
  Otherwise, nothing changes.
- `Party.closeFormation(battleUnitIds, positions)`: the new party order, every member once, or `NotTheParty`. It
  explores again, rebuilds the line from the new leader when the order changed, and publishes `FormationClosed`, with
  the party, the leader first. Nothing happens when it is not in formation.
- `OpenFormation` and `CloseFormation(battleUnitIds)` commands, through `PartyApi`. `CloseFormation` reads the positions
  with `SearchPosition`, as `WalkParty` does.
- `PauseExploration` also pauses the party in formation, although no battle can start meanwhile.

## screen

- `VirtualPadView.setOnBPressed(listener)`, and `pressB()`.
- `ExplorationPresenter`: B is a pending press, as A is, so a press while the scene plays is not lost, and opens the
  formation (`partyApi.openFormation()`). `FormationOpened` hides the pad and opens the panel, `FormationClosed` closes
  it and displays the pad, in the timeline. Displaying the pad closes it too, as it does with the shop.
- `FormationView`: the rows (`Row(unitId, name, health, maximumHealth, mana, maximumMana, isLeader)`), `selectRow`,
  `displayHint`, and a delegate with `formationRowSelected(index)` and `formationCloseSelected()`. The crown is drawn at
  2x, the portrait at 1x.
- `FormationPresenter`: `open()` reads the party order, displays the rows, dims every tile but the party's. It keeps the
  order shown, swaps it on the second tap, and frames the selected member's tile. Close calls
  `partyApi.closeFormation(order)`. `close()` hides the panel and resets the tiles.
- `BattlefieldPresenter`: ignores the tapped tiles while in formation, as while talking, and trails the leader again on
  `FormationClosed`, so the camera eases to it.
- `UnitPortraitView` takes the scale of the portrait, 3x by default.
- `BattleScene` adds the panel next to the shop.

## art

- `art/battlefield/crown.aseprite`, exported to `resources/battlefield/crown.png`, with the `pixel-art` skill.

## Tests

- `PartyTest`
  - `should open the formation only when the party can walk`
  - `should stop the time while in formation`
  - `should reorder the party and rebuild the line from the new leader when the formation is closed`
  - `should keep the line when the formation is closed with the same order`
  - `should fail to close the formation when the order is not the party`
- `OpenFormationTest`, `CloseFormationTest`
- `FormationViewTest`: the rows, the crown on the leader only, the selection, the hint, Close.
- `FormationPresenterTest`: opening, dimming, selecting, swapping, clearing the selection, Close.
- `ExplorationPresenterTest`: B opens the formation, the pad is hidden then displayed again once it is closed.
- `BattlefieldPresenterTest`: the tapped tiles are ignored while in formation, the camera trails the new leader.
- End-to-end: `FormationEndToEndTest`, opening the formation with the hero alone in the party, crowned, and closing it.

## Docs

- `exploration.md` describes the formation.
- `assets.md` lists the crown.

## Not in this plan

- Changing the order in battles.
- A crown on the battlefield.
- Dragging the rows.
