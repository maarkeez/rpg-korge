# Plan: Teleport essence

> **Status:** done.
>
> **Summary:** A new consumable lootable item, the teleport essence, teaches its battle unit the teleport ability for
> good. It is a regular consumable whose effect has a new `LEARN_ABILITY` outcome. The goat always drops it. It can not
> be consumed when its battle unit already knows teleport, or already knows 6 abilities.

## Decisions

| Topic                    | Decision                                                                                                                                                                                   |
|--------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Mechanism                | A regular `CONSUMABLE` lootable item whose effect has a new `LEARN_ABILITY` outcome, with the `abilityId` it teaches. It is applied as any consumable effect: `ConsumeItem` → `RequestApplyEffect` → battle unit. |
| Item                     | Id `teleport-essence`, named `Teleport Essence`, worth 100 G, with the effect `learn-teleport`. It can be sold for half its price, 50 G, as any item.                                       |
| Effect                   | `effect/learn-teleport.toml`: `LEARN_ABILITY` with `abilityId = "teleport"`. The `abilityId` is required, and `LEARN_ABILITY` only accepts the `IMMEDIATELY` application.                  |
| Ability existence        | Not checked when the effects are created, as the effects are initialised before the abilities, the same as `DEPLOY_BATTLE_UNIT`'s `unitId`.                                                 |
| Abilities                | Abilities may use `LEARN_ABILITY` too: nothing restricts it to the lootable items.                                                                                                       |
| Asset                    | The attached icon, with each off-palette colour replaced by its closest FamiCube colour: the outline, the glass and the cork turn into the colours of the potions.                        |
| Obtaining it             | A new `tier-3` loot table holding only the teleport essence, with a weight of 100. The goat drops `tier-3`, so it always drops the teleport essence. The merchant does not sell it.       |
| Permanent                | The learned ability belongs to its battle unit only, not to the other battle units of its unit. It is kept out of the battles and when the battle unit respawns.                           |
| Learned ability          | Added after the battle unit's abilities, at the end of the ability bar, ready to be cast (no cooldown).                                                                                    |
| Turn rules               | As any consumable: consuming it costs no turn action, and is only allowed during the turn of its battle unit player in a battle, always out of the battles.                               |
| Already known            | Consume is disabled when its battle unit already knows teleport (e.g.: the knights): the item stays in the inventory. Consuming it anyway fails (`AbilityCanNotBeLearned`).                |
| Ability bar full         | A battle unit knows 6 abilities at most (`KnownAbilities.MAXIMUM`), the buttons of the ability bar. Consume is disabled when it already knows 6.                                           |
| Disabled Consume         | Greyed out, without a reason, as Discard and Equip.                                                                                                                                        |
| Other consumables        | Only `LEARN_ABILITY` effects can disable Consume: a health potion at full health can still be consumed.                                                                                    |
| Units                    | A unit with more than 6 abilities fails to be created (`AbilitiesAboveLimit`), so the game fails to start.                                                                                 |
| Feedback                 | Nothing new: the battle unit blinks as it receives the effect (`EffectReceived`), the essence leaves the inventory, and teleport is displayed in the ability bar once the inventory is closed. |
| Party                    | The knights keep their abilities: they already know teleport, so only the viking can learn it.                                                                                            |

For example, the viking has picked the teleport essence up from the goat's tile:

| The player                                     | The game                                                                                           |
|------------------------------------------------|----------------------------------------------------------------------------------------------------|
| Opens the viking's bag and taps the essence    | `Teleport Essence` on the left and `100 G` on the right, above Cancel, Discard and Consume.        |
| Taps Consume                                   | The viking blinks, and the essence leaves the inventory.                                           |
| Closes the inventory                           | The viking's ability bar displays sword and teleport, which can be cast straight away.            |
| Opens the knight's bag and taps an essence     | Cancel, Discard and Consume, disabled: the knight already knows teleport.                          |

## effect

### Domain

- Modify `Effect.Dto.EffectOutcomeDto.TypeDto` with `LEARN_ABILITY`, and `EffectOutcomeDto` with
  `learnAbility: LearnAbilityDto? = null`, holding the `abilityId`.
- Modify `Effect.Outcome` with a private `LearnAbility(abilityId)` value class.
- Modify `Effect.create` to fail with `InvalidEffectApplication` when a `LEARN_ABILITY` outcome is not applied
  `IMMEDIATELY`.

### Adapters

- Modify `ResourcesEffectLoader.OutcomeDefinition` with an optional `abilityId`, required for `LEARN_ABILITY`.

## unit

- Add `KnownAbilities.MAXIMUM` (6) to `shared/domain`, as `Gold.MAXIMUM`.
- Modify `Unit.Abilities` to fail with `UnitError.AbilitiesAboveLimit` above `KnownAbilities.MAXIMUM`.

## battleUnit

### Domain

- Modify `BattleUnit` with the private `LearnedAbilities` it learned, empty once deployed.
- Add `fun canLearnAbility(abilityId: String): Boolean`: it does not know the ability yet, and knows fewer than
  `KnownAbilities.MAXIMUM`.
- Modify `applyImmediateEffect` with `LEARN_ABILITY`: it learns the ability, adding it to its learned abilities and at
  the end of its ability cooldowns with no cooldown, and publishes `AbilityLearned`. It fails with
  `BattleUnitError.AbilityAlreadyKnown` or `BattleUnitError.AbilitiesAboveLimit` when it can not learn it.
- Modify `respawn` to keep the learned abilities, after the abilities of its unit.
- Add `BattleUnitEvent.AbilityLearned(battleUnitId, abilityId)`.

### Use cases

- Add the query `CanLearnAbility(battleUnitId, abilityId)`, false when the battle unit does not exist.

### Adapters

- Modify `BattleUnitApi` with `canLearnAbility`.

## inventory

- Add `Inventory.canConsume(lootableItemId)`: whether the item is stored.
- Add the service `ConsumptionRule`: a consumable can be consumed when its battle unit can learn the ability of each of its
  `LEARN_ABILITY` effects. The other effects never prevent it.
- Add `InventoryError.AbilityCanNotBeLearned`.
- Modify `ConsumeItem` with the `ConsumptionRule`, failing with `AbilityCanNotBeLearned` before consuming the item.
- Add the query `CanConsumeItem(battleUnitId, lootableItemId)`: the item is stored, consumable, and the
  `ConsumptionRule` allows it.
- Modify `InventoryApi` with the `EffectApi`, and `canConsumeItem`.

## screen

- Modify `InventoryPresenter` to enable Consume only when `canConsumeItem`.
- Modify `BattleScene` to pass the `EffectApi` to the `InventoryApi`.

## Resources

- Add `effect/learn-teleport.toml`.
- Add `lootableItem/teleport-essence.toml` and `lootableItem/teleport-essence.png`.
- Add `lootTable/tier-3.toml`, and `lootDrop/goat.toml` with `lootTableIds = ["tier-3"]`.

## Tests

- `EffectTest`: a `LEARN_ABILITY` effect applied immediately, failing when applied otherwise or without its details, and its
  `abilityId` exposed.
- `EffectMother`: `learnAbilityEffect`.
- `ResourcesEffectLoaderTest`: a `LEARN_ABILITY` effect is loaded with its `abilityId`.
- `UnitTest`: a unit with 6 abilities, and more than 6.
- `BattleUnitTest`: learning an ability at the end of the ability cooldowns, failing when already known or when 6 are known,
  `CanLearnAbility`, and respawning with the learned abilities.
- `CanLearnAbilityTest`.
- `InventoryTest`: `CanConsume`.
- `ConsumeItemTest`: failing to consume an item whose ability is already known, keeping it stored.
- `CanConsumeItemTest`: a potion, an essence whose ability can be learned, already known, 6 abilities known, an item that is not
  stored, and an item that is not consumable.
- `InventoryPresenterTest`: Consume enabled for an essence whose ability can be learned, and disabled when already known.
- `LearnTeleportAcceptanceTest`: the viking consumes a looted teleport essence while exploring, and knows teleport.
- `AssetPaletteArchitectureTest` already checks the new image.

## Docs

- `game-data.md`: the `LEARN_ABILITY` outcome and its rules, and the maximum abilities of a unit.
- `inventory.md`: consuming an item that teaches an ability.
- `presentation-timeline.md`: Consume disabled when the ability can not be learned.
