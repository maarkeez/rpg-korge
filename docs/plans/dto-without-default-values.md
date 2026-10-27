# Plan: Dto without default values

> **Status:** planned.
>
> **Summary:** A new architecture rule makes every field of the `Dto` of each aggregate root, and of every data class
> nested in it, a required constructor parameter: no default values. The defaults that make building a Dto short move to
> its companion object factories, whose parameters may keep defaults. A Dto that is a choice between shapes gets a factory
> per shape; a plain Dto gets no factory, and is built with every argument. 19 defaults in 10 Dtos break the rule today.

## Decisions

| Topic              | Decision                                                                                                                                                     |
|--------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope              | The primary constructor of the `Dto` of every aggregate root, and of every class nested in it at any depth.                                                   |
| Constructor        | No parameter has a default value: every caller of the constructor passes every field.                                                                        |
| Factories          | Companion object factory parameters may have default values, as `ApplicationSourceDto.battleUnit(battleUnitId, abilityId = null)` today.                     |
| Choices            | A Dto that is an enum `type` with optional fields gets a factory per type, as `ApplicationSourceDto` and `LootTypeDto`: `EffectOutcomeDto` gets one per outcome. |
| Plain Dtos         | No factory: a factory that only adds back the same defaults would hide the rule. `Terrain`, `Npc`, `Unit`, `LootDrop`, `Ability`, `BattleUnit`, `Battlefield.Dto.TileDto` and `EffectApplicationDto` are built with every argument. |
| Tests              | Tests doing `Terrain.create(Terrain.Dto(...))` switch to `TerrainMother.occupiableTerrain` and `nonOccupiableTerrain`. Other tests pass the missing arguments, e.g. `destination = null` in the expected `EffectApplicationDto`s. |

## Violations today

| Dto                                     | Defaults                                                                                   | Built without them                    |
|-----------------------------------------|--------------------------------------------------------------------------------------------|---------------------------------------|
| `BattleUnit.Dto`                        | `remainingTurns = null`                                                                    | Its `toDto`.                          |
| `Ability.Dto`                           | `turnsDuration = null`                                                                     | `ResourcesAbilityLoader`, `AbilityMother`. |
| `Battlefield.Dto.TileDto`               | `terrainTransition = null`                                                                 | `Battlefield.toDto`.                  |
| `LootDrop.Dto`                          | `droppedItems = emptyMap()`                                                                | `ResourcesLootDropLoader`, `LootDropMother`. |
| `Unit.Dto`                              | `startingGold = 0`                                                                         | `ResourcesUnitLoader`, `UnitMother`, `UnitTest`, acceptance tests. |
| `Npc.Dto`                               | `unitId = null`, `stock = emptyList()`                                                     | `ResourcesNpcLoader`, `NpcMother`, 11 tests. |
| `Terrain.Dto`                           | `allowedTransitionTo = emptySet()`, `effectId = null`                                      | `ResourcesTerrainLoader`, 35 tests.   |
| `Effect.Dto.EffectApplicationDto`       | `destination = null`                                                                       | 3 commands, about 30 tests.           |
| `Effect.Dto.EffectApplicationDto.ApplicationSourceDto` | `battleUnitId`, `terrainId`, `lootableItemId`, `abilityId` `= null`         | Its own 3 factories.                  |
| `Effect.Dto.EffectOutcomeDto`           | `deployBattleUnit`, `increaseMana`, `increaseMaximumHealth`, `increaseMaximumMana`, `learnAbility` `= null` | 12 outcomes in `Effect.kt`, `ResourcesEffectLoader`, `EffectMother`. |

## effect

### Domain

- Remove the defaults of `EffectApplicationDto`, `ApplicationSourceDto` and `EffectOutcomeDto`.
- `ApplicationSourceDto.battleUnit`, `terrain` and `item` pass every field, keeping `abilityId = null` as a default of
  `battleUnit`.
- Add a factory per outcome type to `EffectOutcomeDto`, taking the values of its outcome and passing `null` to the
  others: `decreaseHealth(damage)`, `increaseHealth(healing)`, `negateIncreaseHealth()`, `teleport()`,
  `applyEffectOnNearbyAllies(effectId)`, `deployBattleUnit(unitId)`, `increaseMana(mana)`,
  `increaseMaximumHealth(healthPoints)`, `increaseMaximumMana(manaPoints)`, `learnAbility(abilityId)`.
- Modify the outcomes of `Effect` to build their Dto with those factories.

### Adapters

- Modify `ResourcesEffectLoader` to build the outcome with a `when` on its type, calling its factory with the required
  values, instead of an `if` per optional field.

## Plain Dtos

- Remove the defaults of `BattleUnit.Dto`, `Ability.Dto`, `Battlefield.Dto.TileDto`, `LootDrop.Dto`, `Unit.Dto`,
  `Npc.Dto` and `Terrain.Dto`.
- Pass the missing arguments in their `toDto`, their resources loaders, and `TriggerTerrainEffects`,
  `ApplyOnDefeatedEffectsToNearbyAllies` and `ApplyMemberEffect` (`destination = null`).

## Architecture test

In `DomainArchitectureTest`, in `should follow domain encapsulation rules when is the aggregate root`:

- For the `Dto` and every class nested in it, assert no parameter of its primary constructor has a default value, e.g.
  `Aggregate root 'Terrain.Dto' must not declare default values: [allowedTransitionTo, effectId]`.
- Check that it fails before the changes above, on the 10 Dtos listed, and only on them.

## Tests

- Switch the tests building `Terrain.create(Terrain.Dto(...))` to `TerrainMother.occupiableTerrain` and
  `nonOccupiableTerrain`. `TerrainTest` and `ResourcesTerrainLoaderTest`, which test the Dto itself, pass every argument.
- Pass the missing arguments in the other tests and mothers: `EffectMother`, `NpcMother`, `UnitMother`,
  `LootDropMother`, `AbilityMother`, the expected `EffectApplicationDto`s, and the `Npc.Dto` and `Unit.Dto` tests.
- `EffectTest` and `ResourcesEffectLoaderTest` stay green unchanged in their expectations: the factories build the same
  Dtos as today.
