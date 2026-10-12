# How to add a new test

## Tests location

- All the tests can be found at `src/jvmTest`
- Tests that can run on every target MAY be moved to `src/commonTest`, so they also run on JS (`./gradlew jsTest`). See [Shared tests](#shared-tests).
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
- The test MUST be asserted using [Kotest assertions](https://kotest.io/docs/assertions/assertions.html) (e.g.: `actual shouldBe expected`, `events.shouldContainExactly(...)`, `shouldThrow<Error> { ... }`). They run on every target, so the tests can be shared through `src/commonTest`. AssertJ is not allowed, which is enforced by [`AssertionLibraryArchitectureTest`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/architecture/AssertionLibraryArchitectureTest.kt).
- The test SHOULD explain a failure that is not obvious from the assertion with `withClue("...") { ... }`, and SHOULD report several failures at once with `assertSoftly { ... }`.

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
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import shared.domain.*

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
        storedAbility shouldBe ability
        eventBus.shouldHavePublishedEvents(AbilityEvent.AbilityCreated(ability.id))
    }
}
```

Note: the `EventBus` is provided by a `FakeEventBus` (in `test/jvmTest/kotlin/shared/domain/FakeEventBus.kt`) instead of a mock, so tests can assert on the published events with `eventBus.shouldHavePublishedEvents(...)` and `eventBus.shouldNotHavePublishedEvents()` (in `test/jvmTest/kotlin/shared/domain/EventBusMatchers.kt`).

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
* The test **MUST** interact with the view through its page object, and never through the view internals (child views, `findViewByName`, `children`, KorGE view classes...). This is enforced by [`ViewTestPageObjectArchitectureTest`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/architecture/ViewTestPageObjectArchitectureTest.kt).

### View page objects

Each view has a page object: a `<View>Extension.kt` file next to its test (e.g.: `BattleInfoViewExtension.kt` for `BattleInfoView`). It is the only place that knows how the view is built, so changing the view structure only requires changing its page object, not its tests.

* Queries **MUST** be extension functions on the view, named in terms of what the player sees (e.g.: `fun BattleInfoView.roundText(): String?`, `fun BattlefieldView.hasBattleUnitAt(row, column): Boolean`).
* Actions **MUST** be extension functions on `ViewsForTesting` that receive the view, and **MUST** click with the mouse through `clickOn(view)` (e.g.: `suspend fun ViewsForTesting.clickTile(battlefieldView, row, column)`). The view **MUST** be added to the stage (`addChild(view)`) before clicking it.
* The view children used by the page object **SHOULD** be declared `internal` in the production code, so the page object is typed and fails to compile when the view structure changes.
* Page objects of composite views **SHOULD** reuse the page objects of their child views (e.g.: `BattleUnitInfoView.abilityButtons()` plus `AbilityButtonView.isSelected()`).
* Shared helpers (`clickOn`, `isShown`, `descendants`) live in [`ViewExtension.kt`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/presentation/ViewExtension.kt).

Here is an example of a page object:

```kotlin
// src/jvmTest/kotlin/com/mkz/rpg/screen/adapters/presentation/FinishTurnViewExtension.kt
package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import korlibs.korge.tests.ViewsForTesting

// Page object of [FinishTurnView].

fun FinishTurnView.buttonText(): String = button.text

suspend fun ViewsForTesting.clickFinishTurn(finishTurnView: FinishTurnView) = clickOn(finishTurnView.button)
```

And a view test using it:

```kotlin
class FinishTurnViewTest : ViewsForTesting() {
    @Nested
    inner class Init {
        @Test
        fun `should display finish turn text when is initialized`() =
            viewsTest {
                // Given
                // When
                val finishTurnView = FinishTurnView(onTurnFinished = {})
                // Then
                finishTurnView.buttonText() shouldBe "Finish turn"
            }
    }

    @Nested
    inner class ButtonClick {
        @Test
        fun `should finish turn when button is clicked`() =
            viewsTest {
                // Given
                var turnFinished = false
                val finishTurnView = FinishTurnView(onTurnFinished = { turnFinished = true })
                addChild(finishTurnView)
                // When
                clickFinishTurn(finishTurnView)
                // Then
                turnFinished.shouldBeTrue()
            }
    }
}
```

### Animated views

Some view functions take time and suspend (e.g.: `BattlefieldView.moveBattleUnit`). They wait on frames, so the view **MUST** be added to the stage (`addChild(view)`).

* Start them with `startAnimation { ... }`, so the test can assert what is displayed while they play.
* Advance the frames with `nextFrame()`, `awaitCompletion(animation)` or `sampleUntilCompletion(animation) { ... }`, which records what is displayed on every frame. They call `delayFrame()` followed by `yield()`, because `delayFrame()` alone does not let the animation coroutine run.
* These helpers live in [`ViewExtension.kt`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/presentation/ViewExtension.kt). See `BattlefieldViewTest.MoveBattleUnit`.

### Presenters and the timeline

Presenters draw through the [presentation timeline](../architecture/presentation-timeline.md). Their tests use `BattleSceneTimelineMother.battleSceneTimeline()`, which plays the steps straight away. To check that something waits for the previous steps, start a step that keeps the timeline playing with `timeline.playLongStep()`, and complete it later.

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
- When the scenario needs the state left by an action (e.g.: a battle unit damaged by an ability) before the action under test, that action SHOULD be applied in a `@BeforeEach`, dispatching the events. Effects are applied through a chain of events, and each follow-up event is queued behind the events already published: requested in the `// Given` section, they would be applied after the action under test. See `CpuCastingAcceptanceTest` for an example.

For example: Requesting to move a battle unit

- Requesting to move a battle unit is triggered from `battleUnit` subdomain, but has consequences on `battlefield` subdomain too.
- Requesting to move a battle unit to a valid tile is a good fit.
- Requesting to move a battle unit to an invalid tile (e.g.: out of battlefield boundaries) is NOT a good fit.

Here is a test example 

```kotlin
package com.mkz.rpg.shared.usecases.acceptance

// Imports goes here
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
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
        previousTileOccupantId.shouldBeNull()
        val newTileOccupantId = battlefieldApi.searchOccupant(row = 2, column = 1)
        newTileOccupantId shouldBe humanBattleUnitId
    }
}
```

## Test example - End-to-end test

End-to-end tests play the game as a player would: they boot the game headless, click on the screen and assert what is displayed.

- End-to-end tests MUST only cover critical player journeys. They are slower than the rest of the tests, so prefer acceptance tests for anything else.
- They MUST be implemented under `com.mkz.rpg.shared.adapters.e2e` test package
- The test class MUST extend `korlibs.korge.tests.ViewsForTesting(windowSize = GAME_WINDOW_SIZE)` and each test MUST start with `viewsTest`. It runs the game without a window or a GPU, so it runs in the CI.
- They MUST boot the game using `com.mkz.rpg.startGame()`, the same function used by `main`.
- They MUST find the scene views through the [`BattleScene` page object](../../src/jvmTest/kotlin/com/mkz/rpg/screen/adapters/presentation/BattleSceneExtension.kt) (e.g.: `battlefieldView()`), and interact with them and assert what is displayed through their view page objects (e.g.: `clickTile(...)`, `hasBattleUnitAt(...)`). See [View page objects](#view-page-objects).
- They MUST wait until the human player can act (`canHumanPlayerAct()`: the turn has been announced and the finish turn is enabled) before clicking, as the player can not act on the battle scene while it replays the battle. Waiting for what is displayed only is not enough: the last animation may have finished while its timeline step has not. See [Presentation timeline](../architecture/presentation-timeline.md).
- They MUST wait for the game to react using the shared [`waitUntil`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/e2e/EndToEndExtension.kt) helper, which is bounded by real time. The game loads its resources asynchronously in real time, while `delayFrame()` simulates frames much faster than that. A fixed number of frames is therefore not enough, and `yield()` is needed to let the game coroutines run between frames.

To see what the player sees at any step of an end-to-end test, see [How to take a snapshot of the game during an end-to-end test](how-to-take-a-game-snapshot.md).

For example: [`MoveKnightEndToEndTest`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/e2e/MoveKnightEndToEndTest.kt)

```kotlin
class MoveKnightEndToEndTest : ViewsForTesting(windowSize = GAME_WINDOW_SIZE) {
    @Test
    fun `should move the knight one tile when the human player selects the knight and an adjacent tile`() =
        viewsTest {
            // Given
            startGame()
            waitUntil { battlefieldView()?.hasBattleUnitAt(knightRow, knightColumn) == true && canHumanPlayerAct() }
            val battlefieldView = requireNotNull(battlefieldView()) { "The battlefield is not displayed" }
            clickTile(battlefieldView, knightRow, knightColumn)
            waitUntil { battlefieldView.hasSelectionAt(targetRow, targetColumn) }
            // When
            clickTile(battlefieldView, targetRow, targetColumn)
            // Then
            waitUntil { battlefieldView.hasBattleUnitAt(targetRow, targetColumn) }
            battlefieldView.hasBattleUnitAt(targetRow, targetColumn).shouldBeTrue()
            battlefieldView.hasBattleUnitAt(knightRow, knightColumn).shouldBeFalse()
        }
}
```

## Shared tests

A test that only depends on common code can be moved to `src/commonTest`, so it runs on both the JVM and JS. A test can be shared when it does not use Mockito, KorGE views or `java.*` classes.

- The test MUST be annotated with `@kotlin.test.Test` instead of `@org.junit.jupiter.api.Test`.
- The test MUST NOT use `@Nested`, which is not available on JS. Flatten the nested classes and put their context in the test name (e.g.: `should start the first round with the first player when start first round and two players join`).
- The test MUST only use test fixtures (e.g.: object mothers, fakes) that are shared too.

## Run tests

All the project tests can be run using `./gradlew clean jvmTest`. The shared tests also run on JS with `./gradlew jsTest`, which needs Chrome.
