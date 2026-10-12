# How to add a new test

## Tests location

- The tests MUST be in `src/commonTest`, so they run on both the JVM (`./gradlew jvmTest`) and JS (`./gradlew jsTest`). See [Shared tests](#shared-tests).
- Only the tests that can not run on JS MUST be in `src/jvmTest`: the architecture tests (Konsist and ArchUnit are only available on the JVM) and the [end-to-end tests](#test-example---end-to-end-test).
- The test MUST be in the same package as the class being tested. For example, given the class `src/commonMain/kotlin/com/mkz/rpg/ability/usecases/commands/RequestAbilityCreation.kt` the test will be in `src/commonTest/kotlin/com/mkz/rpg/ability/usecases/commands/RequestAbilityCreationTest.kt`

## Test naming convention

- All the tests MUST be named following this convention `should XXX when YYY`. Where `XXX` is what we expect to happen and `YYY` the precondition. For example: `should deploy battle unit when the battlefield tile can be occupied`
- When the class under test has several public functions, the test name MUST start with the function under test, in upper camel case: `<Function> should XXX when YYY`. For example: `StartFirstRound should start the first round with the first player when two players join`. See [Tests of a class with several functions](#tests-of-a-class-with-several-functions).
- All test variables SHOULD be named using domain terms, unless we are testing an adapter layer where adapter terms might be required.

## Test structure

- All the tests MUST have the following three comments `// Given`, `// When`, `// Then`
- The `// Given` section MUST contain the preconditions
- The `// When` section MUST contain the function call under test
- The `// Then` section MUST contain the assertions
- The related dependencies such as use cases, repositories, etc. SHOULD be created as class private variables by default. Unless, the test requires a specific setup that colides with the deafult one.

## Test libraries

- The test MUST be declared with [kotlin.test](https://kotlinlang.org/api/core/kotlin-test/) annotations (e.g.: `@kotlin.test.Test`, `@kotlin.test.BeforeTest`), which run on every target, so the tests can be shared through `src/commonTest`. JUnit is not allowed, which is enforced by [`TestFrameworkArchitectureTest`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/architecture/TestFrameworkArchitectureTest.kt).
- The test MUST be asserted using [Kotest assertions](https://kotest.io/docs/assertions/assertions.html) (e.g.: `actual shouldBe expected`, `events.shouldContainExactly(...)`, `shouldThrow<Error> { ... }`). They run on every target, so the tests can be shared through `src/commonTest`. AssertJ is not allowed, which is enforced by [`AssertionLibraryArchitectureTest`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/architecture/AssertionLibraryArchitectureTest.kt).
- The test SHOULD explain a failure that is not obvious from the assertion with `withClue("...") { ... }`, and SHOULD report several failures at once with `assertSoftly { ... }`.

## Test fixtures

- The test MUST use object mothers to create the required test data (e.g.: `val effect = EffectMother.effect()`)
- The test MUST set only the relevant values while creating objects with object mothers (e.g.: `val ability = ability(effects = listOf(effect.id)).toDto()`)
- The test SHOULD draw the battlefield it needs with `BattlefieldMother.battlefield(map, occupants)`: one line per row and one character per tile, separated by spaces. `.` is sand, `#` is void and a letter is sand occupied by the battle unit that `occupants` maps it to. As in the game, sand in a corner between two void tiles (e.g.: void to the north and to the east) can not be occupied. For example:

```kotlin
val battlefield =
    battlefield(
        """
        K . #
        . . R
        """,
        occupants = mapOf('K' to "knight-1", 'R' to "rat-1"),
    )
```

## Test doubles

- The tests MUST NOT use a mocking library (Mockito, MockK, Mokkery...). This is enforced by [`MockingLibraryArchitectureTest`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/architecture/MockingLibraryArchitectureTest.kt).
- The use case under test SHOULD use its real collaborators (queries, commands, services) built on in-memory repositories (e.g.: `SearchEffectById(InMemoryEffectRepository())`), and the test SHOULD store the data they read in the repositories.
- When the queries under test need many collaborators, the test SHOULD use [`InMemoryQueries`](../../src/commonTest/kotlin/com/mkz/rpg/shared/usecases/InMemoryQueries.kt), which wires the in-memory repositories and the real queries as the subdomain APIs do. For example: `InMemoryQueries().whereShouldMove`.
- The test SHOULD assert outcomes (the stored state, the published events or what is displayed) instead of the calls to the collaborators.
- The `EventBus` is provided by a `FakeEventBus` (in `src/commonTest/kotlin/com/mkz/rpg/shared/domain/FakeEventBus.kt`), so tests can assert on the published events with `eventBus.shouldHavePublishedEvents(...)` and `eventBus.shouldNotHavePublishedEvents()` (in `src/commonTest/kotlin/com/mkz/rpg/shared/domain/EventBusMatchers.kt`). Event listener tests use the `InMemoryEventBus` instead. See [Test example - Event listener](#test-example---event-listener).
- The adapters that read the game resources are replaced by hand-written fakes that load the given data (e.g.: `FakeAbilityLoader(abilities)`), next to the loader interface they implement (e.g.: `ability/usecases/services/FakeAbilityLoader.kt`).
- The views used by presenters are replaced by hand-written fakes that record what they are asked to display (e.g.: `FakeBattlefieldView`), next to the view tests. The views are `open`, with `open` functions, so the fakes can override them. See [Presenters](#presenters).

## Tests of a class with several functions

- The tests MUST NOT use nested test classes (JUnit `@Nested`), which are not available on every target. This is enforced by [`TestFrameworkArchitectureTest`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/architecture/TestFrameworkArchitectureTest.kt).
- When the class under test has several public functions (e.g.: domain classes, views and presenters), the test names MUST start with the function under test, so the tests of the same function are listed together.
- Commands and queries use cases tests MUST NOT prefix their test names. They are expected to have only one single public function.
- A setup only needed by some tests SHOULD be a private function called in their `// Given` section, instead of a `@BeforeTest`, which runs before every test of the class.

For example:

```kotlin
// Class under test
class Battle {
    fun startFirstRound(players: List<String>): Battle { ... }

    fun toDto(): Dto { ... }
}

// Test example
class BattleTest {
    @Test
    fun `StartFirstRound should start the first round with the first player when two players join`() { ... }

    @Test
    fun `ToDto should expose the finished state as true when the battle has finished`() { ... }
}
```

## Test example

For example, here you are a command test:

```kotlin
// src/commonTest/kotlin/com/mkz/rpg/ability/usecases/commands/RequestAbilityCreationTest.kt
package ability.usecases.commands

import ability.adapters.storage.*
import ability.domain.AbilityEvent
import ability.domain.AbilityMother.ability
import effect.domain.*
import effect.usecases.queries.*
import io.kotest.matchers.shouldBe
import shared.domain.*
import kotlin.test.Test

class RequestAbilityCreationTest {
    private val abilityRepository = InMemoryAbilityRepository()
    private val effectRepository = InMemoryEffectRepository()
    private val eventBus = FakeEventBus()
    private val requestAbilityCreation =
        RequestAbilityCreation(
            abilityRepository = abilityRepository,
            searchEffectById = SearchEffectById(effectRepository),
            eventBus = eventBus,
        )

    @Test
    fun `should create ability`() {
        // Given
        val effect = decreaseHealthEffect()
        effectRepository.create(effect)
        val ability = ability(effectSpecs = listOf(effectSpec(effectId = effect.toDto().id))).toDto()
        // When
        requestAbilityCreation(abilityDto = ability)
        // Then
        val storedAbility = abilityRepository.searchById(ability.id)?.toDto()
        storedAbility shouldBe ability
        eventBus.shouldHavePublishedEvents(AbilityEvent.AbilityCreated(ability.id))
    }
}
```

## Test example - Event listener

- Event listener tests MUST use the `com.mkz.rpg.shared.adapters.events.InMemoryEventBus` + `eventBus.dispatch()` instead of the `FakeEventBus`, so the listener receives the published event.
- Each event listener MUST have one single test, which asserts the outcome of the use case it calls (e.g.: the stored state, or the event the use case publishes, recorded with `eventBus.subscribe`). Typed subscriptions are already covered by [`EventBusTest`](../../src/commonTest/kotlin/com/mkz/rpg/shared/domain/EventBusTest.kt), so there is no need to test that other event types are ignored.

You can use this example as reference:

```kotlin
package com.mkz.rpg.battle.adapters.events

// Imports goes here

class OnBattleUnitDefeatedTest {
    private val eventBus = InMemoryEventBus()
    private val battleRepository = InMemoryBattleRepository()
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val onBattleUnitDefeated =
        OnBattleUnitDefeated(
            eventBus = eventBus,
            defeatPlayer = DefeatPlayer(HasAllBattleUnitsDefeated(battleUnitRepository), battleRepository, eventBus),
        )
    private val defeatedPlayers = mutableListOf<PlayerDefeated>().also { defeated -> eventBus.subscribe<PlayerDefeated> { defeated += it } }

    @Test
    fun `should defeat player when a battle unit is defeated`() {
        // Given
        battleRepository.create(battle(players = listOf("player-1", "player-2")))
        battleUnitRepository.create(battleUnit(id = "battle-unit-1", player = player(id = "player-2").toDto(), unit = unit(healthPoints = 0).toDto()))
        eventBus.publish(BattleUnitEvent.BattleUnitDefeated(playerId = "player-2", battleUnitId = "battle-unit-1", defeatedAtRow = 1, defeatedAtColumn = 2))
        // When
        eventBus.dispatch()
        // Then
        defeatedPlayers.shouldContainExactly(PlayerDefeated("player-2"))
    }
}
```

## Test example - Presentation View

View tests are a bit different from the rest of the tests because they need to rely on KorGE's testing features.

* The test class **MUST** extend `korlibs.korge.tests.ViewsForTesting`.
* Each test function **MUST** start by using [`sharedViewsTest`](../../src/commonTest/kotlin/com/mkz/rpg/shared/adapters/presentation/SharedViewsTestExtension.kt), and never KorGE's `viewsTest` directly. KorGE's `viewsTest` skips the test on JS, so the test would pass on JS without asserting anything. `sharedViewsTest` runs it on every target, loading the game resources before the views test starts. This is enforced by [`TestFrameworkArchitectureTest`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/architecture/TestFrameworkArchitectureTest.kt).
* The test **MUST** interact with the view through its page object, and never through the view internals (child views, `findViewByName`, `children`, KorGE view classes...). This is enforced by [`ViewTestPageObjectArchitectureTest`](../../src/jvmTest/kotlin/com/mkz/rpg/shared/adapters/architecture/ViewTestPageObjectArchitectureTest.kt).

### View page objects

Each view has a page object: a `<View>Extension.kt` file next to its test (e.g.: `BattleInfoViewExtension.kt` for `BattleInfoView`). It is the only place that knows how the view is built, so changing the view structure only requires changing its page object, not its tests.

* Queries **MUST** be extension functions on the view, named in terms of what the player sees (e.g.: `fun BattleInfoView.roundText(): String?`, `fun BattlefieldView.hasBattleUnitAt(row, column): Boolean`).
* Actions **MUST** be extension functions on `ViewsForTesting` that receive the view, and **MUST** click with the mouse through `clickOn(view)` (e.g.: `suspend fun ViewsForTesting.clickTile(battlefieldView, row, column)`). The view **MUST** be added to the stage (`addChild(view)`) before clicking it.
* The view children used by the page object **SHOULD** be declared `internal` in the production code, so the page object is typed and fails to compile when the view structure changes.
* Page objects of composite views **SHOULD** reuse the page objects of their child views (e.g.: `BattleUnitInfoView.abilityButtons()` plus `AbilityButtonView.isSelected()`).
* Shared helpers (`clickOn`, `isShown`, `descendants`) live in [`ViewExtension.kt`](../../src/commonTest/kotlin/com/mkz/rpg/shared/adapters/presentation/ViewExtension.kt).

Here is an example of a page object:

```kotlin
// src/commonTest/kotlin/com/mkz/rpg/screen/adapters/presentation/FinishTurnViewExtension.kt
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
    @Test
    fun `Init should display finish turn text when is initialized`() =
        sharedViewsTest {
            // Given
            // When
            val finishTurnView = FinishTurnView(onTurnFinished = {})
            // Then
            finishTurnView.buttonText() shouldBe "Finish turn"
        }

    @Test
    fun `ButtonClick should finish turn when button is clicked`() =
        sharedViewsTest {
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
```

### Animated views

Some view functions take time and suspend (e.g.: `BattlefieldView.moveBattleUnit`). They wait on frames, so the view **MUST** be added to the stage (`addChild(view)`).

* Start them with `startAnimation { ... }`, so the test can assert what is displayed while they play.
* Advance the frames with `nextFrame()`, `awaitCompletion(animation)` or `sampleUntilCompletion(animation) { ... }`, which records what is displayed on every frame. They call `delayFrame()` followed by `yield()`, because `delayFrame()` alone does not let the animation coroutine run.
* These helpers live in [`ViewExtension.kt`](../../src/commonTest/kotlin/com/mkz/rpg/shared/adapters/presentation/ViewExtension.kt). See the `MoveBattleUnit` tests of `BattlefieldViewTest`.

### Presenters

- Presenter tests MUST use the real subdomain APIs through [`SubdomainApis`](../../src/commonTest/kotlin/com/mkz/rpg/shared/adapters/presentation/SubdomainApis.kt), which wires them on one event bus as the game does, with fake loaders that load the given data. Call `initialise()` to store the data, deploy the battle units and start the battle, before creating the presenter.
- Presenter tests MUST use the fake views (e.g.: `FakeBattlefieldView`, `FakeBattleUnitInfoView`), and assert what the fakes recorded (e.g.: `battlefieldView.displayedBattleUnits`). The player actions are simulated by calling the delegate or the callbacks the presenter gave to the fake (e.g.: `playerCallToActionView.onTurnFinished`).

```kotlin
class FinishTurnPresenterTest {
    // The battle has started with the human player turn.
    private val apis =
        SubdomainApis(
            players = listOf(player(id = "human", type = HUMAN).toDto(), player(id = "cpu", type = CPU).toDto()),
            playerOrder = listOf("human", "cpu"),
        ).also(SubdomainApis::initialise)
    private val playerCallToActionView = FakePlayerCallToActionView()
    private val timeline = battleSceneTimeline()

    @Test
    fun `FinishTurn should finish the player turn when the player finishes the turn`() {
        // Given
        FinishTurnPresenter(playerCallToActionView, apis.battleApi, apis.playerApi, timeline, apis.eventBus)
        apis.eventBus.publish(PlayerTurnStarted(playerId = "human"))
        apis.eventBus.dispatch()
        // When
        playerCallToActionView.onTurnFinished.shouldNotBeNull().invoke()
        apis.eventBus.dispatch()
        // Then
        apis.battleApi.searchBattle()?.currentPlayerTurn shouldBe "cpu"
    }
}
```

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
- The common setup for the acceptance test scenario MUST be a `private suspend fun setUp()`, and each test MUST run it with [`scenarioTest(::setUp) { ... }`](../../src/commonTest/kotlin/com/mkz/rpg/shared/usecases/acceptance/ScenarioTest.kt). The setup loads the resources, which suspends, so it can not be a `@BeforeTest`.
- When the scenario needs the state left by an action (e.g.: a battle unit damaged by an ability) before the action under test, that action SHOULD be applied in the `setUp`, dispatching the events. Effects are applied through a chain of events, and each follow-up event is queued behind the events already published: requested in the `// Given` section, they would be applied after the action under test. A scenario that needs a different `setUp` MUST be its own test class. See `CpuCastingBadlyHurtRatAcceptanceTest` for an example.

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
import kotlin.test.Test

class SimpleBattleAcceptanceTest {

    // Subdomain APIs setup goes here

    private suspend fun setUp() {
        // Common setup for the acceptance test scenario goes here (e.g.: `terrainApi.init()` to load the resources)
    }

    @Test
    fun `should occupy a tile when unit was moved`() = scenarioTest(::setUp) {
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
- They MUST find the scene views through the [`BattleScene` page object](../../src/commonTest/kotlin/com/mkz/rpg/screen/adapters/presentation/BattleSceneExtension.kt) (e.g.: `battlefieldView()`), and interact with them and assert what is displayed through their view page objects (e.g.: `clickTile(...)`, `hasBattleUnitAt(...)`). See [View page objects](#view-page-objects).
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

The tests in `src/commonTest` run on both the JVM and JS, so they MUST only depend on common code.

- The test MUST NOT use `java.*` classes, nor JVM only libraries (e.g.: `runBlocking`).
- A test that calls suspend functions MUST run them with `runTest { ... }` (from `kotlinx-coroutines-test`), e.g.: ``fun `should load the abilities`() = runTest { ... }``.
- The test MUST only use test fixtures (e.g.: object mothers, fakes) that are shared too.
- The JS tests read the game resources from `build/processedResources/js/test`, which [`karma.config.d/resources.js`](../../karma.config.d/resources.js) serves to the browser as the game does.

## Run tests

All the project tests can be run using `./gradlew clean` followed by `./gradlew jvmTest jsTest` (cleaning in the same run fails the JS tasks). The shared tests run on JS with `./gradlew jsTest`, which needs Chrome. Both run in the CI.

A single test class can be run on JS with `./gradlew jsBrowserTest --tests '<fully qualified class name>'` (e.g.: `com.mkz.rpg.screen.adapters.presentation.FinishTurnViewTest`). Wildcards (e.g.: `'*FinishTurnViewTest'`) run no test on JS, and the build still succeeds.
