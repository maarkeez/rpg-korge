# Plan: The ability info while exploring

> **Status:** done.
>
> **Summary:** While exploring, tapping a party member displays its info, its bag and its ability buttons, but tapping
> an ability does nothing: `BattlefieldPresenter.abilitySelected` only hands it to the battlefield HUD in a battle. The
> player can not read what the mage's skull does until a battle starts, too late to choose who leads or what to equip.
> Instead, selecting an ability of a party member while exploring displays its [ability info](ability-info.md), read-only,
> as a blocked ability in a battle. And tapping the bag while an ability is on show, in a battle or exploring, closes it
> first, so the inventory does not open over a dimmed battlefield.

## Decisions

| Topic          | Decision                                                                                                                                                                                                                                                       |
|----------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope          | The abilities of the party members, the only battle units selectable while exploring. E.g.: the mage's skull. The CPU wanderers stay unselectable: reading their abilities before engaging them is a follow-up.                                                 |
| Read-only info | Selecting an ability while exploring enters the `DisplayAbilityInfo` state of the battlefield HUD, as `ProcessAbilitySelected` already does when there is no battle: the panel, the button framed, and the battlefield dimmed except the caster's tile. Nothing is ever cast while exploring. |
| Reasons        | The same as in a battle: an ability in cooldown shows "Ready in 2 turns", one without the mana "Needs 10 mana, has 4". Chosen over no reason while exploring, as the mana missing matters for the next battle.                                                 |
| Closing        | As in a battle: selecting the ability again, or tapping any tile, goes back to the party member's info. The virtual pad stays hidden until the selection is cleared (e.g.: an empty tile tapped after).                                                        |
| The bag        | Tapping the bag while an ability is on show, read-only (`DisplayAbilityInfo`) or castable (`DisplayAbilityCastRange`), selects the battle unit again before opening its inventory, so the panel, the cast range and the dimming are gone. In a battle and exploring alike. Chosen over hiding the bag while an ability is on show, a step more to reach the inventory. |
| Architecture   | No new state nor command. `BattlefieldPresenter` drops its battle-only guard, and `ProcessAbilitySelected` casts only in a battle in progress, as a finished battle keeps the turn it ended on. `InventoryPresenter` reselects the battle unit through the selector it already uses to switch party members, and opens the inventory once it is selected. |
| Tests          | Presenter tests for both, and use case tests for selecting an ability without a battle.                                                                                                                                                                        |
| Out of scope   | Selecting the CPU wanderers while exploring. Casting while exploring.                                                                                                                                                                                          |
| Delivery       | 1 milestone, green on CI. See [Milestones](#milestones).                                                                                                                                                                                                       |

## Milestones

Each milestone is a few commits straight on `main`, green on CI.

| #  | Milestone                          | Done when                                                                                                                                                   | Status | Done in |
|----|------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|--------|---------|
| M1 | The ability info while exploring   | Selecting a party member's ability while exploring shows its panel with its reason, and the bag closes any ability on show before opening the inventory.   | ✅      | `837e2332` |

### M1 commits

1. Display the ability info of a party member while exploring: cast only in a battle in progress in
   `ProcessAbilitySelected`, and drop the guard in `BattlefieldPresenter.abilitySelected`, with their tests.
2. Close the ability on show before opening the inventory from the bag, in `InventoryPresenter`, with its tests.
3. Update [Exploration](../architecture/exploration.md#inventory), [Inventory](../architecture/inventory.md#on-the-screen),
   and the out of scope row of the [ability info plan](ability-info.md). Folded into the second commit by the pre-commit
   hook.

## Reference

### BattlefieldPresenter

- `abilitySelected(abilityId)` hands the ability to `battlefieldHudApi.processAbilitySelected` whenever the scene is
  idle, battling or not. Its comment, "The abilities can only be cast in a battle.", goes.
- `displayAbilityInfo(AbilityInfoDisplayed)` is unchanged: a party member is not the CPU's, so its reason is given.

### battlefieldHud

- `ProcessAbilitySelected.canCast` also requires a battle in progress (`searchBattle()?.isFinished == false`). Without
  it, after a battle the human player won, `currentPlayerTurn` is still theirs (`Battle.finishBattle` keeps it), and a
  castable ability would display its cast range while exploring. With no battle at all, `canCast` was already false.
  Its KDoc names exploring among the otherwise cases.
- `ProcessTileSelected` in `DisplayAbilityInfo` already hides the info whatever the tile, going back to
  `DisplayMovementRange` with no movement range, as `selectTileWhileExploring` left it.
- `ExplorationPresenter` needs nothing: `AbilityInfoDisplayed`, `AbilityDeselected` and the `SelectedBattleUnit` that
  follows never display the pad, only `Idle` does.

### InventoryPresenter

- Tracks whether an ability is on show: set on `SelectedBattleUnitAbility` and `AbilityInfoDisplayed`, cleared on
  `SelectedBattleUnit` and `Idle` (`AbilityDeselected` is always followed by `SelectedBattleUnit`).
- The bag, when an ability is on show, sets `switchingTo` to its own battle unit and calls the battle unit selector,
  as `switchOwner` does. `SelectBattleUnit` goes idle and selects it again, and the `SelectedBattleUnit` handler opens
  its inventory as for a switched-to member. Otherwise, the bag opens the inventory straight away, as today.
- In a battle, selecting the battle unit again gives back its movement range, as `SelectBattleUnit` computes it.

### Docs

- [Exploration](../architecture/exploration.md#inventory): "its abilities can not be cast" becomes "its abilities can
  not be cast, but tapping one displays its info, read-only, as a blocked ability in a battle".
- [Ability info plan](ability-info.md): the out of scope row points to this plan for the abilities while exploring.

### Tests

- `ProcessAbilitySelectedTest`
  - `ProcessAbilitySelected should display the ability info when there is no battle`
  - `ProcessAbilitySelected should display the ability info when the battle is finished on the human player's turn`
- `BattlefieldPresenterTest`
  - `AbilitySelected should display the ability info when exploring`
  - `AbilitySelected should ignore the ability when the timeline is playing`
- `InventoryPresenterSwitchTest`, which sets the battle unit selector
  - `OpenInventory should select the battle unit again before opening the inventory when its ability info is displayed`
  - `OpenInventory should select the battle unit again before opening the inventory when its ability cast range is displayed`
  - `OpenInventory should open the inventory straight away when no ability is displayed`
  - `OpenInventory should open the inventory straight away once the ability on show is closed`

## Known limitations

- The CPU wanderers' abilities can only be read once they engage the party.
