# Plan: The ability info panel

> **Status:** to do.
>
> **Summary:** In a battle, the selected battle unit shows up to 6 ability buttons, icons only. The player has to cast
> an ability to learn what it does: nothing says that the skull deals 10 damage and, when its target is defeated,
> poisons the target's adjacent allies with 3 damage each turn for 5 turns, nor that mending costs 10 mana and has a
> cooldown of 2 turns. Tapping an ability in cooldown or without the mana does nothing, and the button only tells why
> by its darkened or blue tint.
> Instead, selecting an ability shows the `AbilityInfoView` docked under the ability buttons: the ability's name, its
> mana cost and cooldown with an icon each, its targeting as a sentence and a 3×3 diagram, and one sentence per effect,
> generated from the [game data](../architecture/game-data.md) and [translated](../architecture/localization.md).
> Blocked abilities and the abilities of enemy battle units can be selected too, to read them, never to cast them.

## Decisions

| Topic           | Decision                                                                                                                                                                                                                                                                                                      |
|-----------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope           | Every ability of every battle unit selected in a battle: the human player's, castable or blocked, and the CPU's, read-only. E.g.: the mage's mending in cooldown, the rat's sword.                                                                                                                             |
| Trigger         | Selecting an ability shows its info, as it shows its cast range today, and selecting it again hides it. Chosen over a hover tooltip, which touch screens lack, and over a long press or an info button, a second gesture to learn.                                                                            |
| Layout          | A panel docked under the ability buttons, in the empty part of the 390×300 battle HUD, always in the same place. Chosen among four Storybook prototypes over a bubble on the button (it covers the health and mana), swapping the stats area (too small for three effects), and a battlefield corner (it covers the map). |
| Content         | The name, the mana cost, the cooldown, the targeting, each effect with its numbers and timing, and a summon's unit, turns and health. E.g.: "Summons Bat for 5 turns (10 HP)." A nested effect is expanded one level: "When defeated: poisons its adjacent allies (3 damage each turn for 5 turns)."            |
| Stats           | The mana cost and the cooldown each have an icon and a number. The icons are a blue diamond and an orange hourglass, pixel art like the prototype's shapes. A cooldown of 0 shows `-`. Chosen over text labels ("10 mana · no cooldown"), which are longer and need translating.                                 |
| Targeting       | A sentence (e.g.: "All adjacent enemies") and a 3×3 diagram: the caster in the middle, and its four orthogonal neighbours marked by who the ability reaches there: an enemy (red), an ally (green), or an empty tile (white outline). E.g.: mending marks the caster and its four neighbours green.             |
| Text            | Generated from the ability, effect and unit data, with whole-sentence templates in `messages.pot` and `es.po`, one per effect kind the abilities use today. Chosen over a hand-written description per ability, which can drift from the TOML files.                                                            |
| Blocked reasons | An ability in cooldown shows "Ready in 2 turns" in orange, and one without the mana "Needs 10 mana, has 4" in blue, under its name. The cooldown wins, as on the button. No casts left shows no reason, as the cast points already say it. Enemy abilities show no reason.                                     |
| Attack preview  | The panel is hidden while the attack preview is displayed, as the preview takes the place of the battle unit info in the HUD. Cancelling the cast shows it again. Chosen over keeping it visible, which left it no room.                                                                                     |
| Read-only info  | Selecting a blocked ability, or any ability of a CPU battle unit, enters a new `DisplayAbilityInfo` state of the battlefield HUD: the panel, the button framed, and the battlefield dimmed except the caster's tile. A tile tap only closes it. Chosen over reusing `DisplayAbilityCastRange` with no cast groups, to keep reading and casting apart. |
| Architecture    | `AbilityInfoMapper` in `screen.adapters.presentation` maps `AbilityApi`, `EffectApi` and `UnitApi` to `AbilityInfoView.AbilityInfo`, plain view data with numbers and ids. `AbilityInfoView` translates it with `Texts`. The `DisplayAbilityInfo` state lives in `screen.adapters.presentation.battlefieldHud`. Chosen over a `SearchAbilityInfo` query in `battleUnit`, as only this view needs it. |
| Storybook       | **Screen / Ability info** with its own stories, and an ability-selected story in **Screen / Battle unit info**. The four prototypes, under **Prototype**, are deleted.                                                                                                                                       |
| Tests           | View tests for `AbilityInfoView`, mapper tests for `AbilityInfoMapper`, including one that maps every ability of the game resources, so an ability with an effect no template covers fails CI. Domain and use case tests for `DisplayAbilityInfo`, and presenter tests.                                       |
| Out of scope    | The info of an item, and the enemy abilities' cast range. A summon's own abilities and stats beyond its health. Hover, long press and tooltips outside the battle.                                                                                                                                             |
| Delivery        | 3 milestones, each green on CI. See [Milestones](#milestones).                                                                                                                                                                                                                                                |

For example, the mage selects its four abilities in turn, with 4 of its 20 mana points left and mending used last turn:

| Ability | Reason               | Stats    | Targeting                 | Effects                                                                                       |
|---------|----------------------|----------|---------------------------|-----------------------------------------------------------------------------------------------|
| Sword   | -                    | ◆ 0  ⧗ - | One adjacent enemy        | Deals 10 damage.                                                                              |
| Skull   | Needs 10 mana, has 4 | ◆ 10 ⧗ - | One adjacent enemy        | Deals 10 damage. When defeated: poisons its adjacent allies (3 damage each turn for 5 turns). |
| Bat     | Needs 10 mana, has 4 | ◆ 10 ⧗ - | An empty tile next to you | Summons Bat for 5 turns (10 HP).                                                              |
| Mending | Ready in 1 turn      | ◆ 10 ⧗ 2 | Yourself                  | Heals you 15 HP. Heals adjacent allies 15 HP.                                                 |

## Milestones

Each milestone is a few commits straight on `main`, green on CI.

| #  | Milestone                     | Done when                                                                                                          | Status | Done in |
|----|-------------------------------|--------------------------------------------------------------------------------------------------------------------|--------|---------|
| M1 | The ability info view         | **Screen / Ability info** shows every story in English and Spanish, with the new icons, and the prototypes are gone. | ⬜      |         |
| M2 | The panel for castable abilities | Selecting a castable ability of the human player in a battle shows its panel under the buttons, hidden during the attack preview. | ⬜      |         |
| M3 | Reading blocked and enemy abilities | Selecting a blocked ability, or one of a CPU battle unit, shows its panel with its reason and no cast range.        | ⬜      |         |

### M1 commits

1. Draw the mana diamond and the cooldown hourglass, 8×8, with the pixel-art skill: `art/ability/info/mana.aseprite`
   and `cooldown.aseprite`, exported to `resources/ability/info/mana.png` and `cooldown.png`.
2. Add the `ui.abilityInfo.*` texts to `UiText`, `messages.pot` and `es.po`, and `Texts.abilityName`.
3. Add `AbilityInfoView`, with `AbilityInfoViewStories` and its tests.
4. Delete `AbilityDetailsPrototypes.kt` and its ids from `StorySmokeTest`.

### M2 commits

1. Add `AbilityInfoMapper`, with its tests, including the one over every ability of the game resources.
2. Add the `AbilityInfoView` to `BattleUnitInfoView`, under the ability buttons, with `displayAbilityInfo` and
   `hideAbilityInfo`, and the **Ability selected** story.
3. Show it from `BattlefieldPresenter` on `SelectedBattleUnitAbility`, giving it `EffectApi` and `UnitApi`.
4. Update [Game data](../architecture/game-data.md).

### M3 commits

1. Add the `DisplayAbilityInfo` state and the `AbilityInfoDisplayed` event to `BattlefieldHud`, with their tests.
2. Enter it from `ProcessAbilitySelected` for blocked and CPU abilities, and handle it in the other commands.
3. Display it from `BattlefieldPresenter`.

## Reference

### Resources

- `resources/ability/info/mana.png`: a blue diamond, the `ManaBarView` blue (`RGBA(0, 145, 255)`).
- `resources/ability/info/cooldown.png`: an orange hourglass, the cast points orange
  (`BattleUnitInfoView.CAST_POINTS_COLOR`, `RGBA(0xFF, 0xBB, 0x31)`).
- `StoryContext.abilityIds()` only lists the images directly under `ability/`, so the info icons are no ability.

### Texts

Whole sentences, with named placeholders, as [Localization](../architecture/localization.md#messages) asks. The ones
with turns are plurals.

| `UiText`                                     | Key                                             | English                                                                       |
|----------------------------------------------|-------------------------------------------------|-------------------------------------------------------------------------------|
| `ABILITY_INFO_READY_IN`                      | `ui.abilityInfo.readyIn`                        | Ready in {turns} turn / turns                                                 |
| `ABILITY_INFO_NOT_ENOUGH_MANA`               | `ui.abilityInfo.notEnoughMana`                  | Needs {cost} mana, has {mana}                                                 |
| `ABILITY_INFO_TARGETING_SELF`                | `ui.abilityInfo.targeting.self`                 | Yourself                                                                      |
| `ABILITY_INFO_TARGETING_ADJACENT_ENEMY`      | `ui.abilityInfo.targeting.adjacentEnemy`        | One adjacent enemy                                                            |
| `ABILITY_INFO_TARGETING_ALL_ADJACENT_ENEMIES` | `ui.abilityInfo.targeting.allAdjacentEnemies`  | All adjacent enemies                                                          |
| `ABILITY_INFO_TARGETING_TILE_NEXT_TO_SELF`   | `ui.abilityInfo.targeting.tileNextToSelf`       | An empty tile next to you                                                     |
| `ABILITY_INFO_TARGETING_TILE_NEXT_TO_UNIT`   | `ui.abilityInfo.targeting.tileNextToUnit`       | An empty tile next to any unit                                                |
| `ABILITY_INFO_DAMAGE`                        | `ui.abilityInfo.effect.damage`                  | Deals {damage} damage.                                                        |
| `ABILITY_INFO_DAMAGE_EACH_TURN`              | `ui.abilityInfo.effect.damageEachTurn`          | Poisons: {damage} damage each turn for {turns} turn / turns.                  |
| `ABILITY_INFO_HEAL_SELF`                     | `ui.abilityInfo.effect.healSelf`                | Heals you {healing} HP.                                                       |
| `ABILITY_INFO_HEAL_NEARBY_ALLIES`            | `ui.abilityInfo.effect.healNearbyAllies`        | Heals adjacent allies {healing} HP.                                           |
| `ABILITY_INFO_SUMMON`                        | `ui.abilityInfo.effect.summon`                  | Summons {unit} for {turns} turn / turns ({health} HP).                         |
| `ABILITY_INFO_TELEPORT`                      | `ui.abilityInfo.effect.teleport`                | Teleports you there.                                                          |
| `ABILITY_INFO_ON_DEFEATED_POISON_ALLIES`     | `ui.abilityInfo.effect.onDefeatedPoisonAllies`  | When defeated: poisons its adjacent allies ({damage} damage each turn for {turns} turn / turns). |

### AbilityInfoView

- `AbilityInfoView(width, resourcesLoader, texts)`, 380 wide, in `screen.adapters.presentation`.
- `AbilityInfoView.AbilityInfo(abilityId, manaCost, cooldown, targeting, effects, reason)`: the plain data it displays.
  `targeting` is an `AbilityInfo.Targeting` (`SELF`, `ADJACENT_ENEMY`, `ALL_ADJACENT_ENEMIES`,
  `VACANT_TILE_ADJACENT_TO_SELF`, `VACANT_TILE_ADJACENT_TO_BATTLE_UNIT`). `effects` are `AbilityInfo.Effect`s:
  `Damage(damage)`, `DamageEachTurn(damage, turns)`, `HealSelf(healing)`, `HealNearbyAllies(healing)`,
  `Summon(unitId, turns, healthPoints)`, `Teleport` and `OnDefeatedPoisonAllies(damage, turns)`. `reason` is `null`,
  `Cooldown(turnsLeft)` or `NotEnoughMana(cost, remainingMana)`.
- `display(abilityInfo)` draws, top to bottom: the ability icon at scale 1.5 and its name (`Texts.abilityName`), the
  reason, the mana and cooldown icons with their numbers, the targeting sentence, and one line per effect, wrapped to
  the width. The 3×3 diagram sits at the top right, 9-pixel cells with a 1-pixel gap.
- The panel has a 1-pixel border `#9C9CB4` over `#141422`, text 12 high. With three effects it fits in the 135 pixels
  left under the ability buttons in the 300-high HUD.
- `hide()` hides it.

### AbilityInfoMapper

- `AbilityInfoMapper.abilityInfo(ability, effectsById, unitsById, cooldownTurnsLeft, remainingMana)` maps an
  `Ability.Dto`, its `Effect.Dto`s and the summoned `Unit.Dto` to an `AbilityInfo`. E.g.: `skull`'s
  `low-physical-damage` (`DECREASE_HEALTH`, `IMMEDIATELY`) is `Damage(10)`, and its `venom-on-death`
  (`APPLY_EFFECT_ON_NEARBY_ALLIES` of `venom-damage`, `ON_DEFEATED`) is `OnDefeatedPoisonAllies(3, 5)`.
- `INCREASE_HEALTH` is `HealSelf` on the `CASTER` target and `HealNearbyAllies` on `NEARBY_ALLIES`. `DEPLOY_BATTLE_UNIT`
  takes the turns from the ability's `turnsDuration` and the health from the unit's `healthPoints`.
- An effect of no kind above (e.g.: `INCREASE_MAXIMUM_MANA`, `WHILE_EQUIPPED`) fails with an error naming the ability
  and the effect, which the resources test catches.
- The reason is `Cooldown` when `cooldownTurnsLeft > 0`, else `NotEnoughMana` when `remainingMana < cost`, else `null`.
  `BattlefieldPresenter` passes no reason for a CPU battle unit.

### BattleUnitInfoView

- Holds an `AbilityInfoView` under the ability buttons, hidden by `display(...)`.
- `displayAbilityInfo(abilityInfo)` shows it, and `hideAbilityInfo()` hides it.
- The attack preview hides it with the rest of the info, as `BattleHudView.displayAttackPreviewView` swaps the info
  out.

### battlefieldHud

- `BattlefieldHud.DisplayAbilityInfo(casterTile, battleUnitId, abilityId, tilesWhereCanBeMoved)`: a selected ability
  that is only read. It publishes `BattlefieldHudEvent.AbilityInfoDisplayed(casterTile, battleUnitId, abilityId)`.
- `DisplayMovementRange.displayAbilityInfo(abilityId)` and `DisplayAbilityCastRange.displayAbilityInfo(abilityId)`
  enter it. From it, `displayAbilityInfo(otherAbilityId)` reads another ability, `selectAbility(...)` goes to
  `DisplayAbilityCastRange`, and `hideAbilityInfo()` goes back to `DisplayMovementRange`, publishing
  `AbilityDeselected` and `SelectedBattleUnit`, as `deselectAbility()` does.
- `ProcessAbilitySelected` casts only when the battle unit belongs to the human player whose turn it is and
  `CanCastAbility` says so. Otherwise it displays the info. Selecting the ability on show again hides it. E.g.: the rat's
  sword, castable on the CPU's turn, is only read.
- `ProcessTileSelected` in `DisplayAbilityInfo` hides the info, whatever the tile.
- `SelectBattleUnit` and `DeselectBattleUnit` go idle from it, `RefreshMovementRange` ignores it, and `ConfirmCast` and
  `CancelCast` fail with `InvalidBattlefieldHudState`, as from the other states they do not apply to.

### BattlefieldPresenter

- Takes `EffectApi` and `UnitApi` besides `AbilityApi`, from `BattleScene`.
- On `SelectedBattleUnitAbility`, it also displays the ability info, without a reason (the ability is castable).
- On `AbilityInfoDisplayed`, it resets the tiles, frames the button (`displayAbilitySelected`), displays the info with
  its reason, and dims every tile but the caster's.

### Storybook

- `AbilityInfoViewStories` (**Screen / Ability info**, `screen-ability-info`): its `ability` control (`pick`, sword,
  skull, bat, mending, teleport, cleave), `cooldownTurnsLeft` (`int`, 0 to 9) and `remainingMana` (`int`, 0 to 20). The
  story feeds plain `AbilityInfo` data, as it can not read the game data. Stories:
  - **Sword** (`screen-ability-info--sword`): one effect, free, no cooldown.
  - **Skull** (`screen-ability-info--skull`): a nested effect.
  - **Bat** (`screen-ability-info--bat`): a summon.
  - **Mending** (`screen-ability-info--mending`): a heal of the caster and its allies.
  - **In cooldown** (`screen-ability-info--in-cooldown`): mending with 2 turns left.
  - **Not enough mana** (`screen-ability-info--not-enough-mana`): bat with 4 mana.
- `BattleUnitInfoViewStories` gains the **Ability selected** story (`screen-battle-unit-info--ability-selected`) and its
  `selectedAbility` control (`int`, 0 for none to 6).
- `StorySmokeTest` lists them, and drops the `prototype-ability-details-*` ids.

### Docs

- [Game data](../architecture/game-data.md): a new kind of effect on an ability (an outcome, application and target not
  in `AbilityInfoMapper`) needs its sentence and a case in the mapper, or CI fails. "No Kotlin change is needed" is only
  true for the kinds it covers.

### Tests

- `AbilityInfoViewTest`
  - `AbilityInfoView should display the name, the mana cost and the cooldown when it displays an ability`
  - `AbilityInfoView should display a dash when the ability has no cooldown`
  - `AbilityInfoView should display one sentence per effect when the ability has several effects`
  - `AbilityInfoView should display the turns left when the ability is in cooldown`
  - `AbilityInfoView should display the mana missing when the ability costs more mana than is left`
  - `AbilityInfoView should mark the four adjacent tiles as enemies when the ability targets all adjacent enemies`
  - `AbilityInfoView should fit under the ability buttons when the ability has three effects`
- `AbilityInfoMapperTest`
  - `AbilityInfoMapper should map an immediate decrease of health to a damage`
  - `AbilityInfoMapper should map a decrease of health on each turn to a damage each turn`
  - `AbilityInfoMapper should map a deployed battle unit to a summon with its turns and health`
  - `AbilityInfoMapper should expand the effect applied to the nearby allies when it is applied on defeat`
  - `AbilityInfoMapper should give the cooldown as the reason when the ability is in cooldown and lacks mana`
  - `AbilityInfoMapper should fail naming the ability when an effect has no sentence`
- `AbilityInfoResourcesTest`
  - `AbilityInfoMapper should map every ability when it reads the game resources`
- `BattleUnitInfoViewTest`
  - `BattleUnitInfoView should display the ability info under the ability buttons when an ability is selected`
  - `BattleUnitInfoView should hide the ability info when it displays a battle unit`
- `BattlefieldHudTest`
  - `DisplayMovementRange should display the ability info when it displays the info of an ability`
  - `DisplayAbilityInfo should go back to the movement range when it hides the ability info`
  - `DisplayAbilityInfo should display the cast range when it selects a castable ability`
- `ProcessAbilitySelectedTest`
  - `ProcessAbilitySelected should display the ability info when the ability is in cooldown`
  - `ProcessAbilitySelected should display the ability info when the battle unit has not the mana`
  - `ProcessAbilitySelected should display the ability info when the battle unit belongs to the CPU`
  - `ProcessAbilitySelected should hide the ability info when the ability on show is selected again`
- `ProcessTileSelectedTest`
  - `ProcessTileSelected should hide the ability info when a tile is selected while it is displayed`
- `BattlefieldPresenterTest`
  - `BattlefieldPresenter should display the ability info when an ability is selected`
  - `BattlefieldPresenter should display the reason and dim the battlefield when the ability info is displayed`
  - `BattlefieldPresenter should hide the ability info when the attack preview is displayed`

## Known limitations

- The sentences cover only the effect kinds the abilities use today. A new kind fails CI until it gets its sentence.
- A summon's info gives its unit and health, not its abilities.
- The enemy abilities no longer show their cast range, which they did when castable.
