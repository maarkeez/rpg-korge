# Plan: Rename the code of the lootable item subdomain to the words of the glossary

> **Status:** to do.
>
> **Summary:** The code of the lootable item subdomain says an item's effects are "applied while equipped", while the
> [glossary](../../GLOSSARY.md) makes While equipped a timing, and an effect is applied by a source. E.g.: a health potion
> whose `health-potion-heal` were `WHILE_EQUIPPED` fails the game setup with `ConsumableEffectAppliedWhileEquipped`, and a
> hammer whose `maximum-health-20` were `IMMEDIATELY` fails with `EquippableEffectNotAppliedWhileEquipped`, both thrown
> right after `isTimedWhileEquipped`. Reading the code and talking about the game take a translation. Instead, the errors,
> their messages, the comment of `InitialiseLootableItems` and its tests say "timed while equipped". The game plays the
> same, and players read the same text. Builds on [Game data](../architecture/game-data.md) and the
> [effect renames](effect-renames.md).

## Decisions

| Topic         | Decision                                                                                                                                                                                                                                                  |
|---------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope         | The renames below, in the code, tests and comments of the lootable item subdomain.                                                                                                                                                                        |
| Lootable item | `LootableItem`, `lootableItemId` and the `lootableItem` package, resources and translation keys keep their names, as Lootable item is the glossary's word. Item is its short form, so `ItemStored`, `storedItems` and players' "Tap an item to buy" stay. |
| Words kept    | `LootType`, `ConsumableDto`, `EquippableDto`, `EquipmentZone`, `Price` and `sellPrice` already fit Loot type, Consumable, Equippable, Equipment zone, Price and Sell price.                                                                               |
| Apply         | The comments of `PartyEvent` and `BattleUnitEvent` keep "the effects the item applied while equipped", as there the item is the source that applied them.                                                                                                 |
| Save format   | Not reached: the lootable items are read from the resources again, and never saved, so `SAVE_VERSION` does not change.                                                                                                                                    |
| Player text   | None changes, and no translation key or catalog changes: players never read these errors.                                                                                                                                                                 |
| Tests         | No new tests: a rename changes no behaviour. The existing tests are renamed with the code.                                                                                                                                                                |
| Out of scope  | `LootableItemCreated`, and the words of the lootDrop, npc and storybook subdomains, which wait for their sections. See [Not in this plan](#not-in-this-plan).                                                                                             |

## Renames

| Today                                     | Glossary term  | New name                                |
|-------------------------------------------|----------------|-----------------------------------------|
| `ConsumableEffectAppliedWhileEquipped`    | While equipped | `ConsumableEffectTimedWhileEquipped`    |
| `EquippableEffectNotAppliedWhileEquipped` | While equipped | `EquippableEffectNotTimedWhileEquipped` |

## lootableItem

### Domain

- `LootableItemError.ConsumableEffectTimedWhileEquipped` says "A consumable lootable item effect can not be timed while
  equipped".
- `LootableItemError.EquippableEffectNotTimedWhileEquipped` says "An equippable lootable item effect must be timed while
  equipped".

### Use cases

- `InitialiseLootableItems` throws the renamed errors. Its comment says "a consumable's effects can not be timed while
  equipped, and an equippable item's effects must be".

## Tests

- `InitialiseLootableItemsTest`
    - `should fail when a consumable lootable item effect is timed while equipped`, expecting
      `ConsumableEffectTimedWhileEquipped` (replaces `... is applied while equipped`)
    - `should fail when an equippable lootable item effect is not timed while equipped`, expecting
      `EquippableEffectNotTimedWhileEquipped` (replaces `... is not applied while equipped`)

## Not in this plan

- Nothing subscribes to `LootableItemCreated`: it is published, and only the tests read it. Whether it should stay is
  still to be decided.

### Waiting for other sections

- `lootDrop`: Loot is still to be defined, while Lootable item and Loot type are named after it. If it settles on another
  meaning, check those two entries again.
- `npc`: Merchant is still to be defined, as Price and Sell price use it.
- `storybook`: the **Screen / Item actions › Equippable** story says "An item to wear, e.g.: a sword", while the game has
  no sword, only hammers.
