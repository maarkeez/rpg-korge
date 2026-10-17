package com.mkz.rpg.exploration.domain

import com.mkz.rpg.exploration.domain.Exploration.Dto.StepDto
import com.mkz.rpg.exploration.domain.ExplorationMother.exploration
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.test.Test

class ExplorationTest {
    private val leaderStep = StepDto(battleUnitId = "knight-1", row = 1, column = 0)
    private val followerStep = StepDto(battleUnitId = "knight-2", row = 2, column = 0)

    @Test
    fun `Start should explore with the first battle unit leading when the party starts exploring`() {
        // When
        val exploration = Exploration.start(playerId = "human", battleUnitIds = listOf("knight-1", "knight-2"))
        // Then
        exploration.toDto() shouldBe
            Exploration.Dto(
                playerId = "human",
                leaderBattleUnitId = "knight-1",
                battleUnitIds = listOf("knight-1", "knight-2"),
                isExploring = true,
                isWalking = false,
            )
        exploration.pullEvents().first.shouldContainExactly(
            ExplorationEvent.ExplorationStarted(playerId = "human", battleUnitIds = listOf("knight-1", "knight-2")),
        )
    }

    @Test
    fun `Start should fail when the party has no battle units`() {
        // When
        val result = runCatching { Exploration.start(playerId = "human", battleUnitIds = emptyList()) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<ExplorationError.PartyRequired>()
    }

    @Test
    fun `WalkParty should walk the steps in order when the party is exploring`() {
        // When
        val walkingExploration = exploration().walkParty(listOf(leaderStep, followerStep))
        // Then
        walkingExploration.toDto().isWalking.shouldBeTrue()
        walkingExploration.nextStep() shouldBe leaderStep
    }

    @Test
    fun `WalkParty should fail when the party is still walking`() {
        // Given
        val walkingExploration = exploration().walkParty(listOf(leaderStep))
        // When
        val result = runCatching { walkingExploration.walkParty(listOf(leaderStep)) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<ExplorationError.AlreadyWalking>()
    }

    @Test
    fun `WalkParty should fail when the party is not exploring`() {
        // Given
        val pausedExploration = exploration().pause()
        // When
        val result = runCatching { pausedExploration.walkParty(listOf(leaderStep)) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<ExplorationError.NotExploring>()
    }

    @Test
    fun `WalkParty should fail when there are no steps`() {
        // When
        val result = runCatching { exploration().walkParty(emptyList()) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<ExplorationError.StepsRequired>()
    }

    @Test
    fun `NextStep should be null while the step is awaited`() {
        // Given
        val awaitingExploration = exploration().walkParty(listOf(leaderStep, followerStep)).awaitNextStep()
        // When
        val result = awaitingExploration.nextStep()
        // Then
        result.shouldBeNull()
    }

    @Test
    fun `ResolveStep should continue with the next step when the awaited battle unit occupies its tile`() {
        // Given
        val awaitingExploration = exploration().walkParty(listOf(leaderStep, followerStep)).awaitNextStep()
        // When
        val resolvedExploration = awaitingExploration.resolveStep(battleUnitId = "knight-1", row = 1, column = 0)
        // Then
        resolvedExploration.nextStep() shouldBe followerStep
        resolvedExploration.pullEvents().first.shouldBeEmpty()
    }

    @Test
    fun `ResolveStep should finish walking when the last step is walked`() {
        // Given
        val awaitingExploration = exploration().walkParty(listOf(leaderStep)).awaitNextStep()
        // When
        val resolvedExploration = awaitingExploration.resolveStep(battleUnitId = "knight-1", row = 1, column = 0)
        // Then
        resolvedExploration.canWalk().shouldBeTrue()
        resolvedExploration.pullEvents().first.shouldContainExactly(
            ExplorationEvent.PartyWalked(playerId = "human", battleUnitIds = listOf("knight-1", "knight-2")),
        )
    }

    @Test
    fun `ResolveStep should not change when another tile is occupied`() {
        // Given
        val awaitingExploration = exploration().walkParty(listOf(leaderStep)).awaitNextStep()
        // When
        val resolvedExploration = awaitingExploration.resolveStep(battleUnitId = "knight-1", row = 5, column = 5)
        // Then
        resolvedExploration shouldBe awaitingExploration
    }

    @Test
    fun `SkipNextStep should finish walking when the skipped step is the last one`() {
        // Given
        val awaitingExploration = exploration().walkParty(listOf(leaderStep, followerStep)).awaitNextStep()
        val resolvedExploration = awaitingExploration.resolveStep(battleUnitId = "knight-1", row = 1, column = 0)
        // When
        val skippedExploration = resolvedExploration.skipNextStep()
        // Then
        skippedExploration.canWalk().shouldBeTrue()
        skippedExploration.pullEvents().first.shouldContainExactly(
            ExplorationEvent.PartyWalked(playerId = "human", battleUnitIds = listOf("knight-1", "knight-2")),
        )
    }

    @Test
    fun `Pause should stop exploring and abandon the walk when a battle starts`() {
        // Given
        val walkingExploration = exploration().walkParty(listOf(leaderStep, followerStep)).awaitNextStep()
        // When
        val pausedExploration = walkingExploration.pause()
        // Then
        pausedExploration.toDto().isExploring.shouldBeFalse()
        pausedExploration.toDto().isWalking.shouldBeFalse()
        pausedExploration.canWalk().shouldBeFalse()
        pausedExploration.pullEvents().first.shouldContainExactly(ExplorationEvent.ExplorationPaused(playerId = "human"))
    }
}
