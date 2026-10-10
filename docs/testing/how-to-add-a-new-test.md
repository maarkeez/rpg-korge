# How to add a new test

## Tests location

- All the tests can be found at `src/jvmTest`
- The test MUST be in the same package as the class being tested. For example, given the class `src/ability/usecases/commands/RequestAbilityCreation.kt` the test will be in `src/jvmTest/kotlin/ability/usecases/commands/RequestAbilityCreationTest.kt` 

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
// src/jvmTest/kotlin/ability/usecases/commands/RequestAbilityCreationTest.kt
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

Note: the `EventBus` is provided by a `FakeEventBus` (in `test/jvmTest/kotlin/shared/domain/FakeEventBus.kt`) instead of a mock, so tests can assert on the published events with `assertThat(eventBus).hasPublishedEvents(...)`. Since the custom `assertThat(FakeEventBus)` extension shares its name with the AssertJ one, import the fake utilities via `import shared.domain.assertThat` and import the plain AssertJ assertion as `import org.assertj.core.api.Assertions.assertThat`.

## Test example - Event listener

In order to test event subscriptions we need to use the `com.mkz.rpg.shared.adapters.events.InMemoryEventBus` + `eventBus.dispatch()` instead of the FakeEvenBus. You can use this example as reference:
```kotlin
package com.mkz.rpg.battle.adapters.events

import com.mkz.rpg.battle.usecases.commands.DefeatPlayer
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class OnBattleUnitDefeatedTest {
    private val eventBus = InMemoryEventBus()
    private val defeatPlayer: DefeatPlayer = mock()
    private val onBattleUnitDefeated =
        OnBattleUnitDefeated(
            eventBus = eventBus,
            defeatPlayer = defeatPlayer,
        )

    @Test
    fun `should defeat player when a battle unit is defeated`() {
        // Given
        val defeatedPlayer = "player-1"
        val defeatedBattleUnit = "battle-unit-1"
        eventBus.publish(BattleUnitEvent.BattleUnitDefeated(defeatedPlayer, defeatedBattleUnit))
        // When
        eventBus.dispatch()
        // Then
        verify(defeatPlayer).invoke(defeatedPlayer)
    }
}
```

## Test example - Presentation View 

View tests are a bit different from the rest of the tests because they need to rely on KorGE's testing features.

* The test class **MUST** extend `korlibs.korge.tests.ViewsForTesting`.
* Each test function **MUST** start by using `viewsTest` from `korlibs.korge.tests.ViewsForTesting`.
* The production code **SHOULD** be adapted to grant access to `korlibs.korge.view` properties, so that tests can assert whether they are visible and verify their properties, such as `text`, etc.

Here is a complete example of a view test:

```kotlin
import com.mkz.rpg.shared.adapters.presentation.BarView
import korlibs.image.color.Colors
import korlibs.korge.tests.ViewsForTesting
import korlibs.math.geom.Size
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BarViewTest : ViewsForTesting() {

    @Nested
    inner class Display {
        @Test
        fun `should be visible when is displayed`() =
            viewsTest {
                // Given
                val barView = BarView(Size(width = 100.00, height = 50.00), Colors.RED)
                // When
                barView.display(remaining = 50, maximum = 100)
                // Then
                assertThat(barView.isVisibleToUser()).isTrue
                assertThat(barView.progressBar.isVisibleToUser()).isTrue
                assertThat(barView.label.isVisibleToUser()).isTrue
            }

        @Test
        fun `should modify label when is displayed`() =
            viewsTest {
                // Given
                val barView = BarView(Size(width = 100.00, height = 50.00), Colors.RED)
                // When
                barView.display(remaining = 50, maximum = 100)
                // Then
                assertThat(barView.label.text).isEqualTo("50 / 100")
            }

        @Test
        fun `should modify progress bar when is displayed`() =
            viewsTest {
                // Given
                val barView = BarView(Size(width = 100.00, height = 50.00), Colors.RED)
                val remaining = 50
                val maximum = 100
                val percentage = (remaining.toDouble() / maximum.toDouble()) * 100
                // When
                barView.display(remaining = 50, maximum = 100)
                // Then
                assertThat(barView.progressBar.current).isEqualTo(percentage)
            }
    }
}
```

## Test example - Use case acceptance test

- Use case acceptance tests MUST test new features that integrates multiple subdomains.
- They MUST test the happy path.
- They MUST NOT test corner cases.
- They MUST NOT test integrations with the presentation layer.
- They MUST request the actions through use cases.
- They MUST assert state.
- They MUST NOT assert that events are being triggered.
- They MUST be implemented under `com.mkz.rpg.shared.usecases.acceptance` test package
- They SHOULD use each subdomain API declared under `com.mkz.rpg.<subdomain>.adapters.presentation.<subdomain>Api`. E.g.: use `com.mkz.rpg.effect.adapters.presentation.EffectApi` to interact with the effect subdomain use cases.
- They SHOULD use `require` to validate preconditions about the common setup for the acceptance test scenario. E.g.: before moving a battle unit, require that the initial position is the one needed by the test.
- They MUST only invoke `eventBus.dispatch()` in the `// When` section

For example: Requesting to move a battle unit

- Requesting to move a battle unit is triggered from `battleUnit` subdomain, but has consequences on `battlefield` subdomain too.
- Requesting to move a battle unit to a valid tile is a good fit.
- Requesting to move a battle unit to an invalid tile (e.g.: out of battlefield boundaries) is NOT a good fit.

Here is a test example 

```kotlin
package com.mkz.rpg.shared.usecases.acceptance

// Imports goes here
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SimpleBattleAcceptanceTest {

    // Subdomain APIs setup goes here

    @BeforeEach
    fun setup() {
        // Common setup for the acceptance test scenario goes here
    }

    @Test
    fun `should occupy a tile when unit was moved`() {
        // Given
        require(battlefieldApi.searchOccupant(row = 1, column = 1) == humanBattleUnitId)
        battleUnitApi.moveBattleUnit(battleUnitId = humanBattleUnitId, moveToRow = 2, moveToColumn = 1)
        // When
        eventBus.dispatch()
        // Then
        val previousTileOccupantId = battlefieldApi.searchOccupant(row = 1, column = 1)
        assertThat(previousTileOccupantId).isNull()
        val newTileOccupantId = battlefieldApi.searchOccupant(row = 2, column = 1)
        assertThat(newTileOccupantId).isEqualTo(humanBattleUnitId)
    }
}
```

## Test example - End-to-end test

End-to-end tests play the game as a player would: they boot the game headless, click on the screen and assert what is displayed.

- End-to-end tests MUST only cover critical player journeys. They are slower than the rest of the tests, so prefer acceptance tests for anything else.
- They MUST be implemented under `com.mkz.rpg.shared.adapters.e2e` test package
- The test class MUST extend `korlibs.korge.tests.ViewsForTesting(windowSize = GAME_WINDOW_SIZE)` and each test MUST start with `viewsTest`. It runs the game without a window or a GPU, so it runs in the CI.
- They MUST boot the game using `com.mkz.rpg.startGame()`, the same function used by `main`.
- They MUST interact through mouse events (e.g.: `mouseMoveAndClickTo(x, y)`) using the global coordinates of the views.
- They MUST assert what is displayed. Views can be found by name (e.g.: battlefield tiles are named `row-<row>-column-<column>`).
- They MUST wait for the game to react using a `waitUntil` loop bounded by real time. The game loads its resources asynchronously in real time, while `delayFrame()` simulates frames much faster than that. A fixed number of frames is therefore not enough, and `yield()` is needed to let the game coroutines run between frames.

For example: [`MoveKnightEndToEndTest`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/e2e/MoveKnightEndToEndTest.kt)

```kotlin
class MoveKnightEndToEndTest : ViewsForTesting(windowSize = GAME_WINDOW_SIZE) {
    @Test
    fun `should move the knight one tile when the human player selects the knight and an adjacent tile`() =
        viewsTest {
            // Given
            startGame()
            waitUntil { hasBattleUnit(knightRow, knightColumn) }
            clickOnTile(knightRow, knightColumn)
            waitUntil { hasSelection(targetRow, targetColumn) }
            // When
            clickOnTile(targetRow, targetColumn)
            // Then
            waitUntil { hasBattleUnit(targetRow, targetColumn) }
            assertThat(hasBattleUnit(targetRow, targetColumn)).isTrue
            assertThat(hasBattleUnit(knightRow, knightColumn)).isFalse
        }

    private suspend fun waitUntil(condition: () -> Boolean) {
        val startMillis = System.currentTimeMillis()
        while (!condition() && System.currentTimeMillis() - startMillis < WAIT_TIMEOUT_MILLIS) {
            delayFrame()
            yield()
        }
    }
}
```

## Run tests

All the project tests can be run using `./gradlew clean jvmTest`
