# How to add a new effect outcome

An effect outcome describes what happens when an [`Effect`](../../src/commonMain/kotlin/com/mkz/rpg/effect/domain/Effect.kt) is applied.

Examples of existing outcomes are:

- `DECREASE_HEALTH`
- `INCREASE_HEALTH`
- `TELEPORT`

Adding a new outcome requires more than adding an enum value. The new outcome must be represented by the effect domain, handled by the code that applies effects, be usable by abilities, and be covered by tests.

This guide walks through the complete workflow.

## 1. Define the new outcome

Start by deciding what the outcome means in domain terms.

For example, suppose we want to add an outcome that removes a status from a battle unit:

```text
REMOVE_STATUS
```

Before implementing it, define:

- what entity or state it changes;
- which effect properties it needs;
- when the outcome is applied;
- whether it can be chained with other effects;
- what should happen when the outcome cannot be applied.

Keep the outcome focused on the domain behavior. Do not make the effect itself responsible for knowing how an ability targets a battle unit.

An `Effect` currently contains common properties such as:

- `id`
- `outcome`
- `application`

The outcome property is therefore the part that identifies the effect outcome.  [Effect](../../src/commonMain/kotlin/com/mkz/rpg/effect/domain/Effect.kt)

## 2. Add the outcome to `Effect`

Add the new outcome to both representations of the effect type in  [Effect](../../src/commonMain/kotlin/com/mkz/rpg/effect/domain/Effect.kt)

The internal domain representation is the private `Outcome` interface:

```kotlin
private sealed interface Outcome {
   // …
}
```

The DTO representation is `Effect.Dto.EffectOutcomeDto`:

```kotlin
data class EffectOutcomeDto {
    // …
}
```

Both are required.

`Effect.create()` converts the DTO type into the domain type, while `Outcome.toDto()` converts it back. 

### Why both exist

`Effect.Outcome` is deliberately private to the domain model. External code works with `Effect.Dto`, which exposes `EffectOutcomeDto`.

This means adding the value only to `EffectOutcomeDto` is not enough: `Effect.create()` will not be able to construct the domain effect correctly.

Likewise, adding it only to `Effect.Outcome` means the outcome cannot be represented by an effect DTO.

## 3. Decide whether effect outcome properties are required

If the new outcome genuinely requires new data, add that data to:

1. `Effect`;
2. `Effect.Dto`;
3. the corresponding domain value object, if appropriate;
4. DTO conversion;
5. validation;
6. the relevant object mothers and tests.

## 4. Implement the outcome where effects are applied

Adding the outcome to `Effect.kt` only makes the outcome representable. It does **not** make the game perform the outcome.

Find the code that interprets an effect and applies its outcome. Search for the existing outcome names, particularly:

```text
DECREASE_HEALTH
INCREASE_HEALTH
TELEPORT
```

The new outcome must be handled in the same effect-application flow as the existing outcomes.

For example, if the existing application logic contains a dispatch such as:

```kotlin
when (effect.outcome.type) {
    DECREASE_HEALTH -> ...
    INCREASE_HEALTH -> ...
    TELEPORT -> ...
}
```

add the new behavior there:

```kotlin
REMOVE_STATUS -> ...
```

The important rule is:

> Do not implement the outcome in the ability domain merely because abilities trigger effects.

An ability contains a list of effect IDs. Its responsibility is to reference effects; the effect/application flow is responsible for determining what those effects do.

The `Ability` domain confirms this separation: its `effects` value is a list of `String` IDs, and ability creation verifies that those IDs correspond to existing effects. [Ability](../../src/commonMain/kotlin/com/mkz/rpg/ability/domain/Ability.kt)

## 5. Make the outcome work with the effect's application rules

An effect is not necessarily applied immediately.

`Effect` currently supports these application modes:

- `IMMEDIATELY`
- `ON_TURN_STARTED`
- `BEFORE_APPLYING_EFFECT`

The application mode is part of the effect itself. [Effect](../../src/commonMain/kotlin/com/mkz/rpg/effect/domain/Effect.kt)

When implementing a new outcome, make sure it behaves correctly in every application mode that the game allows for that outcome.

For example, if `REMOVE_STATUS` is valid only immediately, the application logic should enforce that rule rather than silently producing an unexpected result when the effect is configured for another application mode.

Similarly, respect existing effect behavior around:

- duration;
- effect chaining.

Do not bypass these mechanisms for the new outcome unless the domain explicitly requires different behavior.

## 6. Make the outcome available to abilities

Abilities do not embed effects directly. They contain effect IDs:

```kotlin
effects: List<String>
```

and an ability can contain between one and three effect IDs. [Ability](../../src/commonMain/kotlin/com/mkz/rpg/ability/domain/Ability.kt)

When an ability is created, `RequestAbilityCreation` resolves every referenced effect using `SearchEffectById`. If any effect does not exist, the ability is rejected. [RequestAbilityCreation](../../src/commonMain/kotlin/com/mkz/rpg/ability/usecases/commands/RequestAbilityCreation.kt)

Therefore, no additional ability-type enum is normally required for a new effect outcome.

Instead, make sure the new effect can be created and stored through the existing effect API:

```kotlin
effectApi.requestEffectCreation(...)
```

`EffectApi` exposes `RequestEffectCreation` and `SearchEffectById`. [EffectApi](../../src/commonMain/kotlin/com/mkz/rpg/effect/adapters/presentation/EffectApi.kt)

The workflow should therefore be:

```text
new Effect.Dto
      ↓
RequestEffectCreation
      ↓
Effect.create()
      ↓
EffectRepository
      ↓
SearchEffectById
      ↓
Ability references effect ID
      ↓
Ability is used
      ↓
effect application flow
      ↓
new outcome is performed
```

`RequestEffectCreation` creates the effect only if an effect with the same ID does not already exist. [RequestEffectCreation](../../src/commonMain/kotlin/com/mkz/rpg/effect/usecases/commands/RequestEffectCreation.kt)

## 7. Add unit tests for the new behavior

Add tests for the new domain/application behavior following the project's testing conventions.

See:

[How to add a new test](../testing/how-to-add-a-new-test.md)

You MUST, follow the repository's conventions.

The tests should verify the behavior of the new outcome itself.

For example:

```kotlin
@Test
fun `should remove status when effect is applied`() {
    // Given
    // ...
    // When
    // ...
    // Then
    // ...
}
```

Prefer testing the behavior rather than implementation details.

If the outcome has multiple meaningful behaviors, add a test for each behavior. For example, if the new outcome is affected by probability or modifiers, those rules should be covered by the appropriate unit tests.

## 8. Add or update test fixtures

If the new outcome requires a particular effect configuration, update the existing effect object mother rather than constructing large DTOs repeatedly in tests.

For example:

```kotlin
val effect = EffectMother.removeStatusEffect()
```

Follow the existing object-mother conventions and set only the values relevant to the scenario.

This keeps tests focused on the behavior being tested and follows the project's testing guidelines. [How to add a new test](../testing/how-to-add-a-new-test.md).

## 9. Add a use-case acceptance test

A unit test proves that the new effect implementation works in isolation. It does not prove that the complete game flow can use the new outcome through an ability.

Add a **use-case acceptance test** for the new feature.

The project defines use-case acceptance tests as tests for features that integrate multiple subdomains. They should test the happy path, interact with the system through use cases, and assert the resulting state rather than implementation details or events. [How to add a new test](../testing/how-to-add-a-new-test.md).

Put the acceptance test under:

```text
src/jvmTest/kotlin/com/mkz/rpg/shared/usecases/acceptance
```

Use the relevant subdomain APIs, such as:

```kotlin
com.mkz.rpg.effect.adapters.presentation.EffectApi
```

and the corresponding APIs for the subdomains involved in applying the effect. The existing `EffectApi` is specifically intended to expose the effect subdomain's use cases to this kind of integration test. [EffectApi](../../src/commonMain/kotlin/com/mkz/rpg/effect/adapters/presentation/EffectApi.kt)

### What the acceptance test should prove

The acceptance test should exercise the complete path:

```text
create the new effect
        ↓
create an ability referencing that effect
        ↓
use the ability
        ↓
dispatch the resulting events
        ↓
assert the resulting game state
```

For example, if the new outcome is `REMOVE_STATUS`, the acceptance test should create an ability containing a `REMOVE_STATUS` effect, use that ability in an appropriate battle scenario, and assert that the target no longer has the status.

Follow the existing acceptance-test conventions described at [how-to-add-a-new-test.md](../testing/how-to-add-a-new-test.md)

## 11. Run the complete test suite

Once the implementation and tests are complete, run the tests as explained in described at [how-to-add-a-new-test.md](../testing/how-to-add-a-new-test.md)

Do not rely only on the new unit test. 

Running the complete suite helps detect regressions in those existing flows.

## 12. Final checklist

Before opening the pull request, verify:

- [ ] The new outcome has a clear domain meaning.
- [ ] The outcome was added to `Effect.Outcome`.
- [ ] The outcome was added to `Effect.Dto.EffectOutcomeDto`.
- [ ] Any required effect properties were added and validated.
- [ ] The effect-application logic handles the new outcome.
- [ ] Existing effect attributes semantics are respected.
- [ ] The effect can be created through `EffectApi`.
- [ ] An ability can reference the new effect by ID.
- [ ] Unit tests cover the new outcome's behavior.
- [ ] Test data uses the existing object mothers.
- [ ] A use-case acceptance test covers the happy path through an ability.
- [ ] The acceptance test asserts resulting state rather than events.
- [ ] Chaining behavior has been considered and tested where relevant.
- [ ] `./gradlew clean jvmTest` passes.

## Summary

Adding an effect outcome is a cross-cutting change.

The minimum implementation path is:

1. **Add the outcome to `Effect`.**
2. **Implement what the outcome does in the effect-application flow.**
3. **Make sure the effect can be created and referenced by an ability.**
4. **Add focused unit tests.**
5. **Add a use-case acceptance test proving the outcome works through an ability.**
6. **Run the complete test suite.**

The key distinction to keep in mind is that an **effect describes what should happen**, an **ability references effects**, and the **effect application flow performs the outcome**.