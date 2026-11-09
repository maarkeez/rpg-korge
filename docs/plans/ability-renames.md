# Plan: Rename the code of the ability subdomain to the words of the glossary

> **Status:** done.
>
> **Summary:** The code of the ability subdomain uses words the [glossary](../../GLOSSARY.md) rejects. E.g.: the mana
> Mushroom spends is `cost` in `Ability` and its TOML, but `manaCost` in `AbilityInfo`, and Mending heals its
> `NEARBY_ALLIES`, which players read as "adjacent allies". Reading the code and talking about the game take a
> translation. Instead, the classes, functions, TOML keys and values, and translation keys use the glossary's words. The
> game plays the same, apart from one sentence of the ability info. Builds on [Game data](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/game-data.md).

## Decisions

| Topic        | Decision                                                                                                                                                     |
|--------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope        | The renames below, in the code, tests, data, catalogs and docs. Only one player text changes.                                                                |
| Save format  | Not reached: none of these names is in a save adapter or a fixture under `src/jvmTest/fixtures/save`, so `SAVE_VERSION` does not change.                     |
| Player text  | Keeps "empty" and "Yourself", as players read them well. Only "next to any unit" changes, as it hides that the caster does not count.                        |
| Tests        | No new tests: a rename changes no behaviour. The existing tests are renamed with the code.                                                                   |
| Out of scope | `TargetExpression`, which waits for the effect review, and the cooldown question. See [Not in this plan](#not-in-this-plan). Devlog entries stay as written. |

## Renames

| Today                                 | Glossary term                               | New name                                      |
|---------------------------------------|---------------------------------------------|-----------------------------------------------|
| `cost`                                | Mana cost                                   | `manaCost`                                    |
| `EffectSpec`                          | Ability effect                              | `AbilityEffect`                               |
| `turnsDuration`                       | Summon duration                             | `summonDuration`                              |
| `reduceCoolDowns`                     | Cooldown                                    | `reduceCooldowns`                             |
| `SELF`                                | Caster targeting                            | `CASTER`                                      |
| `VACANT_TILE_ADJACENT_TO_SELF`        | Vacant tile adjacent to the caster          | `VACANT_TILE_ADJACENT_TO_CASTER`              |
| `VACANT_TILE_ADJACENT_TO_BATTLE_UNIT` | Vacant tile adjacent to another battle unit | `VACANT_TILE_ADJACENT_TO_ANOTHER_BATTLE_UNIT` |
| `NEARBY_ALLIES`, `NearbyAllies`       | Adjacent allies                             | `ADJACENT_ALLIES`, `AdjacentAllies`           |
| `APPLY_EFFECT_ON_NEARBY_ALLIES`       | Adjacent allies                             | `APPLY_EFFECT_ON_ADJACENT_ALLIES`             |
| `SELECTED_TARGET`, `SelectedTarget`   | Targeted battle unit                        | `TARGETED_BATTLE_UNIT`, `TargetedBattleUnit`  |
| `SELECTED_TILE`, `SelectedTile`       | Targeted tile                               | `TARGETED_TILE`, `TargetedTile`               |

## ability

- `Ability` and `Ability.Dto`: `cost` is `manaCost`, `turnsDuration` is `summonDuration`, `effectSpecs` is `abilityEffects`,
  and `EffectSpecs`, `EffectSpec` and `EffectSpecDto` are `AbilityEffects`, `AbilityEffect` and `AbilityEffectDto`. The
  values of `Targeting`, `TargetingDto` and `TargetExpression` follow the table.
- `AbilityError`: `NegativeAbilityCost` and `AbilityCostAboveLimit` are `NegativeAbilityManaCost` and
  `AbilityManaCostAboveLimit`, and each `AbilityTurnsDuration*` is `AbilitySummonDuration*`, with their messages.
- `AbilityCreation` follows the names above.
- `ResourcesAbilityLoader`: `AbilityDefinition` reads the `manaCost` and `summonDuration` keys, and `EffectSpecDefinition`
  is `AbilityEffectDefinition`. The `effects` and `target` keys stay.

## Resources

- `resources/ability/*.toml`: the `cost` key is `manaCost`, the `turnsDuration` key of `bat` and `bee` is `summonDuration`,
  and the `targeting` and `target` values follow the table.
- `resources/effect/venom-on-death.toml`: `APPLY_EFFECT_ON_ADJACENT_ALLIES`.

## effect

- `Effect`, `ResourcesEffectLoader`: `APPLY_EFFECT_ON_NEARBY_ALLIES`, and the classes and functions named after it, say
  adjacent allies.

## battleUnit

- `BattleUnit`: `castAbility(abilityCost = ...)` is `castAbility(abilityManaCost = ...)`, and `reduceCoolDowns` is
  `reduceCooldowns`, also on its `AbilityCooldowns`.
- `ApplyOnDefeatedEffectsToNearbyAllies` is `ApplyOnDefeatedEffectsToAdjacentAllies`, followed by `OnBattleUnitDefeated` and
  `BattleUnitApi`.
- `AbilityExecution`: `searchNearbyAllies` is `searchAdjacentAllies`, and `WhereCanCast`, `CastAbility`,
  `SearchAbilityAvailabilities` and `ApplyEffect` follow the names above.

## cpuBattleBrain

- `WhatShouldCast` follows the names above.

## screen

- `AbilityInfoMapper`, `AbilityInfoView` and `BattlefieldPresenter` follow the names above. `AbilityInfo.Effect.HealNearbyAllies`
  is `HealAdjacentAllies`.
- `UiText`: the entries and translation keys below change, also as `msgctxt` in `resources/localization/messages.pot` and
  `resources/localization/es.po`.

| Today                                      | New                                                |
|--------------------------------------------|----------------------------------------------------|
| `ABILITY_INFO_TARGETING_SELF`              | `ABILITY_INFO_TARGETING_CASTER`                    |
| `ui.abilityInfo.targeting.self`            | `ui.abilityInfo.targeting.caster`                  |
| `ABILITY_INFO_TARGETING_TILE_NEXT_TO_SELF` | `ABILITY_INFO_TARGETING_TILE_NEXT_TO_CASTER`       |
| `ui.abilityInfo.targeting.tileNextToSelf`  | `ui.abilityInfo.targeting.tileNextToCaster`        |
| `ABILITY_INFO_TARGETING_TILE_NEXT_TO_UNIT` | `ABILITY_INFO_TARGETING_TILE_NEXT_TO_ANOTHER_UNIT` |
| `ui.abilityInfo.targeting.tileNextToUnit`  | `ui.abilityInfo.targeting.tileNextToAnotherUnit`   |
| `ABILITY_INFO_HEAL_NEARBY_ALLIES`          | `ABILITY_INFO_HEAL_ADJACENT_ALLIES`                |
| `ui.abilityInfo.effect.healNearbyAllies`   | `ui.abilityInfo.effect.healAdjacentAllies`         |

- The text of `ui.abilityInfo.targeting.tileNextToAnotherUnit` is "An empty tile next to another unit", and in Spanish
  "Una casilla vacía junto a otra unidad". E.g.: the ability info of Teleport tells it.

## Storybook

- `AbilityInfoViewStories` follows the names above. No story displays the changed sentence, and no story id changes.

## Docs

- [Game data](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/game-data.md): the `ability/<ability id>.toml` and `effect/<effect id>.toml` sections use the new
  keys and values.

## Tests

- The tests that use these names are renamed with the code, with no change in what they assert, including
  `AbilityTest`, `ResourcesAbilityLoaderTest`, `ResourcesEffectLoaderTest`, `EffectTest`, `AbilityInfoMapperTest`,
  `AbilityInfoViewTest`, `WhereCanCastTest`, `WhatShouldCastTest`, `BattleUnitTest`, the acceptance tests and `AbilityMother`.

## Not in this plan

- Cooldown 1 plays the same as cooldown 0: cooldowns go down when a turn of the player starts, and a battle unit has one
  cast per turn, so the ability is ready again on its next turn either way. It applies to Cleave, Poisoned Sword, Teleport
  and Venom Strike. It may be a bug or a balance choice, still to be decided.

### Waiting for other sections

- `effect`: `TargetExpression` should say effect target, but `AbilityExecution` already imports `Effect.Dto.EffectTargetDto`,
  so `Ability.Dto.EffectTargetDto` would collide with it. Settle the word there, then rename it. Its values do follow the table.
- `battleUnit`: Cast, Caster, Known ability, Learned ability and the cooldown turns left are still to be defined.
- `battlefield`: the exact meaning of Vacant and Adjacent is still to be defined.
- `cpuBattleBrain`: `CastKind.ATTACK`, while "attack" is to be avoided.
- `screen`: `AttackPreviewView` also previews heals, while "attack" is to be avoided.

## Changes while building

- The `{cost}` placeholder of `ui.abilityInfo.notEnoughMana` is also `{manaCost}`, in both catalogs, as it is the same word.
  Players read the same sentence.
- [Assets](https://github.com/maarkeez/rpg-korge/blob/8692ff6f0fe556e6e9142583d589630779a6e17e/docs/architecture/assets.md) and [How to add a new test](../testing/how-to-add-a-new-test.md) also used these
  words, so they changed too.
- `WhatShouldCastTest` and `BattlefieldPresenterTest` write `TargetingDto.CASTER`, as importing it would collide with
  `TargetExpressionDto.Type.CASTER`, which they also use.
- `SELECTED_TILE` in `BattlefieldViewStories` and `selectedTile` in `ProcessTileSelected` stay: they are the tile selected
  on the battlefield, not the effect target.
