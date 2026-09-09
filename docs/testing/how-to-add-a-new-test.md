# How to add a new test

## Tests location

- All the tests can be found at `src/commonTest`
- The test MUST be in the same package as the class being tested. For example, given the class `src/ability/usecases/commands/RequestAbilityCreation.kt` the test will be in `src/commonTest/kotlin/ability/usecases/commands/RequestAbilityCreationTest.kt` 

## Test naming convention

- All the tests MUST be named following this convention `should XXX when YYY`. Where `XXX` is what we expect to happen and `YYY` the precondition. For example: `should deploy battle unit when the battlefield tile can be occupied`
- All test variables SHOULD be named using domain terms, unless we are testing an adapter layer where adapter terms might be required.

## Test structure

- All the tests MUST have the following three comments `// Given`, `// When`, `// Then`
- The `// Given` section MUST contain the preconditions
- The `// When` section MUST contain the function call under test
- The `// Then` section MUST contain the assertions
- The related dependencies such as use cases, repositories, etc. SHOULD be created as class private variables by default. Unless, the test requires a specific setup that colides with the deafult one.

## Test libraries

- The test MUST be annotated with `@org.junit.jupiter.api.Test`
- The test MUST be asserted using `AssertJ` library assertions

## Test fixtures

- The test MUST use object mothers to create the required test data (e.g.: `val effect = EffectMother.effect()`)
- The test MUST set only the relevant values while creating objects with object mothers (e.g.: `val ability = ability(effects = listOf(effect.id)).toDto()`)

## Test nested classes

- When the class under test has multiple public functions, we MUST use junit `@Nested` and create one nested class per public function
- Domain class tests SHOULD use `@Nested`. Often they will grow their available public functions.
- Commands and queries use cases class tests MUST not use `@Nested`. They are expected to have only one single public function.

For example:

```kotlin
// Class under test
class MainClass {

    fun functionOne() {
        // Function one logic goes here
    }

    fun functionTwo() {
        // Function two logic goes here
    }
}

// Test example
import org.junit.jupiter.api.Nested

class MainClassTest {

    @Nested
    class FunctionOne {
        // Function one tests go here
    }

    @Nested
    class FunctionTwo {
        // Function two tests go here
    }
}
```

## Test example

For example, here you are a command test:

```kotlin
// src/commonTest/kotlin/ability/usecases/commands/RequestAbilityCreationTest.kt
package ability.usecases.commands

import ability.adapters.storage.*
import ability.domain.AbilityEvent
import ability.domain.AbilityMother.ability
import effect.domain.*
import effect.usecases.queries.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import shared.domain.*
import shared.domain.assertThat

class RequestAbilityCreationTest {
    private val abilityRepository = InMemoryAbilityRepository()
    private val eventBus = FakeEventBus()
    private val searchEffectById: SearchEffectById = mock()
    private val requestAbilityCreation = RequestAbilityCreation(
        abilityRepository = abilityRepository,
        searchEffectById = searchEffectById,
        eventBus = eventBus
    )

    @Test
    fun `should create ability`() {
        // Given
        val effect = EffectMother.effect().toDto()
        val ability = ability(effects = listOf(effect.id)).toDto()
        whenever(searchEffectById(effect.id)).thenReturn(effect)
        // When
        requestAbilityCreation(abilityDto = ability)
        // Then
        val storedAbility = abilityRepository.searchById(ability.id)?.toDto()
        org.assertj.core.api.Assertions.assertThat(storedAbility).isEqualTo(ability)
        assertThat(eventBus).hasPublishedEvents(AbilityEvent.AbilityCreated(ability.id))
    }
}
```

Note: the `EventBus` is provided by a `FakeEventBus` (in `test/commonTest/kotlin/shared/domain/FakeEventBus.kt`) instead of a mock, so tests can assert on the published events with `assertThat(eventBus).hasPublishedEvents(...)`. Since the custom `assertThat(FakeEventBus)` extension shares its name with the AssertJ one, import the fake utilities via `import shared.domain.assertThat` and import the plain AssertJ assertion as `import org.assertj.core.api.Assertions.assertThat`.

## Run tests

All the project tests can be run using `./gradlew clean jvmTest`
