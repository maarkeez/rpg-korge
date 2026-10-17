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

    @Test
    fun `Resume should explore again with the party when a battle finishes`() {
        // Given
        val pausedExploration = exploration().pause().pullEvents().second
        // When
        val resumedExploration = pausedExploration.resume()
        // Then
        resumedExploration.toDto().isExploring.shouldBeTrue()
        resumedExploration.canWalk().shouldBeTrue()
        resumedExploration.pullEvents().first.shouldContainExactly(
            ExplorationEvent.ExplorationResumed(playerId = "human", battleUnitIds = listOf("knight-1", "knight-2")),
        )
    }

    @Test
    fun `Leave should have the next battle unit lead when the leader leaves the party`() {
        // When
        val exploration = exploration(battleUnitIds = listOf("knight-1", "knight-2", "knight-3")).leave("knight-1")
        // Then
        exploration.toDto().leaderBattleUnitId shouldBe "knight-2"
        exploration.toDto().battleUnitIds.shouldContainExactly("knight-2", "knight-3")
    }

    @Test
    fun `Leave should keep the leader when a follower leaves the party`() {
        // When
        val exploration = exploration(battleUnitIds = listOf("knight-1", "knight-2", "knight-3")).leave("knight-2")
        // Then
        exploration.toDto().leaderBattleUnitId shouldBe "knight-1"
        exploration.toDto().battleUnitIds.shouldContainExactly("knight-1", "knight-3")
    }

    @Test
    fun `Leave should have no leader when the last battle unit leaves the party`() {
        // When
        val exploration = exploration(battleUnitIds = listOf("knight-1")).leave("knight-1")
        // Then
        exploration.toDto().leaderBattleUnitId.shouldBeNull()
        exploration.toDto().battleUnitIds.shouldBeEmpty()
    }

    @Test
    fun `StandingBattleUnitIds should leave out the battle units that moved during the second when they are marked as moved`() {
        // When
        val exploration = exploration().markMoved("knight-1").markMoved("rat-1")
        // Then
        exploration.standingBattleUnitIds(setOf("knight-1", "knight-2", "rat-1", "rat-2")) shouldBe setOf("knight-2", "rat-2")
    }

    @Test
    fun `StartNextSecond should have every battle unit standing when the next second starts`() {
        // Given
        val exploration = exploration().markMoved("knight-1")
        // When
        val nextSecondExploration = exploration.startNextSecond()
        // Then
        nextSecondExploration.standingBattleUnitIds(setOf("knight-1", "knight-2")) shouldBe setOf("knight-1", "knight-2")
    }

    @Test
    fun `MarkMoved should not remember the battle units moving while battling`() {
        // When
        val exploration = exploration().pause().markMoved("knight-1")
        // Then
        exploration.standingBattleUnitIds(setOf("knight-1")) shouldBe setOf("knight-1")
    }

    @Test
    fun `Pause should forget the battle units that moved while exploring when a battle starts`() {
        // When
        val exploration = exploration().markMoved("knight-1").pause().resume()
        // Then
        exploration.standingBattleUnitIds(setOf("knight-1")) shouldBe setOf("knight-1")
    }
}
