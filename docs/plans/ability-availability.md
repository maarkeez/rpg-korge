# Plan: Tell the cooldowns apart

> **Status:** done.
>
> **Summary:** The battle unit info darkens an ability when the battle unit has no casts left, or when the ability is in
> cooldown. Once a battle unit has cast, it has no casts left, so all its abilities are darkened, and it is not clear
> which ones are in cooldown. It happens to the CPU battle units after their turn, and to the battle units of the player
> after they cast. Instead, the ability buttons tell apart why an ability can not be cast: a cooldown darkens it and
> displays the turns left in a corner badge, and a lack of mana tints it blue. No casts left no longer darkens anything,
> as the cast points already display it.

## Decisions

| Topic            | Decision                                                                                                                                                                                                                   |
|------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| In cooldown      | Darkened, as today, with the cooldown turns left in a badge in the bottom right corner (e.g.: `2`).                                                                                                                        |
| Turns left       | The cooldown as stored: the turns of its own player left until it can be cast (e.g.: `1` is castable on its next turn). No badge once it is `0`.                                                                           |
| Not enough mana  | In shades of blue, as the mana bar, from the icon luminance, with no badge. The cost is compared with the remaining mana, as `BattleUnit.canCastAbility` does, not with the mana replenished on its next turn.                                       |
| Both             | The cooldown wins: darkened with the badge.                                                                                                                                                                                |
| No casts left    | No longer darkens the abilities: they stay bright, meaning castable on its next turn. The cast points (e.g.: `0/1`) display that no casts are left. A tap still does nothing, as `ProcessAbilitySelected` checks it.     |
| Who              | Every battle unit the info displays: the ones of the player and of the CPU, in a battle and while exploring.                                                                                                              |
| Selection        | `ProcessAbilitySelected` checks `canCastAbility` too when switching from a selected ability to another one, so an ability that can not be cast is never selected.                                                        |
| Domain           | `BattleUnit` and `Ability` are unchanged. A new query reads the availabilities from them.                                                                                                                                 |

## SearchAbilityAvailabilities

A new query in `battleUnit/usecases/queries`, exposed by `BattleUnitApi`, next to `CanCastAbility`, as it needs the
abilities (their costs) too:

- `invoke(battleUnitId): List<AbilityAvailabilityDto>`, in the order of `abilityCooldowns`, so the indexes match
  `displayAbilitySelected`.
- `AbilityAvailabilityDto(abilityId, cooldownTurnsLeft: Int, hasEnoughMana: Boolean)`. The remaining casts are not part
  of it.

## BattleUnitInfoView

- `display(battleUnit, unit, abilityAvailabilities)`: each ability button is displayed from its availability, instead
  of reading `abilityCooldowns` and `remainingCasts`.
- `BattlefieldPresenter.displayMovementRange` and `InventoryPresenter.inventoryClosed` read the availabilities with the
  battle unit, before the timeline step.

## AbilityButtonView

- `display(abilityId, cooldownTurnsLeft, hasEnoughMana)` replaces `display(abilityId, canCast)`:
  - `cooldownTurnsLeft > 0`: the dark filter, and the badge with the turns left.
  - else `!hasEnoughMana`: a filter turning the icon into shades of blue.
  - else: no filter.
- The badge, named `COOLDOWN`, is the turns left in white on a translucent black square, in the bottom right corner,
  removed when displayed again (as `ABILITY`).

## Tests

- `SearchAbilityAvailabilitiesTest`: the turns left after a cast, `0` once reduced, the mana compared with the cost, and
  unaffected by the remaining casts.
- `AbilityButtonViewTest`: the dark filter and the badge in cooldown, the blue tint without enough mana, the cooldown
  winning over the mana, and no filter nor badge otherwise.
- `BattleUnitInfoViewTest`: after a cast, the abilities not in cooldown are displayed bright.
- `ProcessAbilitySelectedTest`: switching to an ability in cooldown, or without enough mana, keeps the selected one.
- `BattlefieldPresenterTest`: the selected battle unit is displayed with its availabilities, in the order of its
  abilities.
