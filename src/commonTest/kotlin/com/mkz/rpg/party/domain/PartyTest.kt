package com.mkz.rpg.party.domain

import com.mkz.rpg.party.domain.Party.Dto.StepDto
import com.mkz.rpg.party.domain.PartyMother.party
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.test.Test

class PartyTest {
    private val leaderStep = StepDto(battleUnitId = "knight-1", row = 1, column = 0)
    private val followerStep = StepDto(battleUnitId = "knight-2", row = 2, column = 0)

    @Test
    fun `Start should explore with the first battle unit leading when the party starts exploring`() {
        // When
        val party = Party.start(playerId = "human", battleUnitIds = listOf("knight-1", "knight-2"), unitIds = listOf("knight", "knight"))
        // Then
        party.toDto() shouldBe
            Party.Dto(
                playerId = "human",
                leaderBattleUnitId = "knight-1",
                battleUnitIds = listOf("knight-1", "knight-2"),
                isExploring = true,
                isWalking = false,
                talkingNpcId = null,
            )
        party.pullEvents().first.shouldContainExactly(
            PartyEvent.ExplorationStarted(playerId = "human", battleUnitIds = listOf("knight-1", "knight-2"), unitIds = listOf("knight", "knight")),
        )
    }

    @Test
    fun `Start should fail when the party has no battle units`() {
        // When
        val result = runCatching { Party.start(playerId = "human", battleUnitIds = emptyList(), unitIds = emptyList()) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<PartyError.PartyRequired>()
    }

    @Test
    fun `WalkParty should walk the steps in order when the party is exploring`() {
        // When
        val walkingParty = party().walkParty(listOf(leaderStep, followerStep))
        // Then
        walkingParty.toDto().isWalking.shouldBeTrue()
        walkingParty.nextStep() shouldBe leaderStep
    }

    @Test
    fun `WalkParty should fail when the party is still walking`() {
        // Given
        val walkingParty = party().walkParty(listOf(leaderStep))
        // When
        val result = runCatching { walkingParty.walkParty(listOf(leaderStep)) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<PartyError.AlreadyWalking>()
    }

    @Test
    fun `WalkParty should fail when the party is not exploring`() {
        // Given
        val pausedParty = party().pause()
        // When
        val result = runCatching { pausedParty.walkParty(listOf(leaderStep)) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<PartyError.NotExploring>()
    }

    @Test
    fun `WalkParty should fail when there are no steps`() {
        // When
        val result = runCatching { party().walkParty(emptyList()) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<PartyError.StepsRequired>()
    }

    @Test
    fun `NextStep should be null while the step is awaited`() {
        // Given
        val awaitingParty = party().walkParty(listOf(leaderStep, followerStep)).awaitNextStep()
        // When
        val result = awaitingParty.nextStep()
        // Then
        result.shouldBeNull()
    }

    @Test
    fun `ResolveStep should continue with the next step when the awaited battle unit occupies its tile`() {
        // Given
        val awaitingParty = party().walkParty(listOf(leaderStep, followerStep)).awaitNextStep()
        // When
        val resolvedParty = awaitingParty.resolveStep(battleUnitId = "knight-1", row = 1, column = 0)
        // Then
        resolvedParty.nextStep() shouldBe followerStep
        resolvedParty.pullEvents().first.shouldBeEmpty()
    }

    @Test
    fun `ResolveStep should finish walking when the last step is walked`() {
        // Given
        val awaitingParty = party().walkParty(listOf(leaderStep)).awaitNextStep()
        // When
        val resolvedParty = awaitingParty.resolveStep(battleUnitId = "knight-1", row = 1, column = 0)
        // Then
        resolvedParty.canWalk().shouldBeTrue()
        resolvedParty.pullEvents().first.shouldContainExactly(
            PartyEvent.PartyWalked(playerId = "human", battleUnitIds = listOf("knight-1", "knight-2")),
        )
    }

    @Test
    fun `ResolveStep should not change when another tile is occupied`() {
        // Given
        val awaitingParty = party().walkParty(listOf(leaderStep)).awaitNextStep()
        // When
        val resolvedParty = awaitingParty.resolveStep(battleUnitId = "knight-1", row = 5, column = 5)
        // Then
        resolvedParty shouldBe awaitingParty
    }

    @Test
    fun `SkipNextStep should finish walking when the skipped step is the last one`() {
        // Given
        val awaitingParty = party().walkParty(listOf(leaderStep, followerStep)).awaitNextStep()
        val resolvedParty = awaitingParty.resolveStep(battleUnitId = "knight-1", row = 1, column = 0)
        // When
        val skippedParty = resolvedParty.skipNextStep()
        // Then
        skippedParty.canWalk().shouldBeTrue()
        skippedParty.pullEvents().first.shouldContainExactly(
            PartyEvent.PartyWalked(playerId = "human", battleUnitIds = listOf("knight-1", "knight-2")),
        )
    }

    @Test
    fun `Pause should stop exploring and abandon the walk when a battle starts`() {
        // Given
        val walkingParty = party().walkParty(listOf(leaderStep, followerStep)).awaitNextStep()
        // When
        val pausedParty = walkingParty.pause()
        // Then
        pausedParty.toDto().isExploring.shouldBeFalse()
        pausedParty.toDto().isWalking.shouldBeFalse()
        pausedParty.canWalk().shouldBeFalse()
        pausedParty.pullEvents().first.shouldContainExactly(PartyEvent.ExplorationPaused(playerId = "human"))
    }

    @Test
    fun `Resume should explore again with the party when a battle finishes`() {
        // Given
        val pausedParty = party().pause().pullEvents().second
        // When
        val resumedParty = pausedParty.resume()
        // Then
        resumedParty.toDto().isExploring.shouldBeTrue()
        resumedParty.canWalk().shouldBeTrue()
        resumedParty.pullEvents().first.shouldContainExactly(
            PartyEvent.ExplorationResumed(playerId = "human", battleUnitIds = listOf("knight-1", "knight-2")),
        )
    }

    @Test
    fun `Leave should have the next battle unit lead when the leader leaves the party`() {
        // When
        val party = party(battleUnitIds = listOf("knight-1", "knight-2", "knight-3")).leave("knight-1")
        // Then
        party.toDto().leaderBattleUnitId shouldBe "knight-2"
        party.toDto().battleUnitIds.shouldContainExactly("knight-2", "knight-3")
    }

    @Test
    fun `Join should add the battle unit to the party, last, when it is not in it`() {
        // When
        val party = party(battleUnitIds = listOf("knight-1", "knight-2")).join("viking-1", "viking")
        // Then
        party.toDto().leaderBattleUnitId shouldBe "knight-1"
        party.toDto().battleUnitIds.shouldContainExactly("knight-1", "knight-2", "viking-1")
        party.pullEvents().first.shouldContainExactly(PartyEvent.PartyJoined(playerId = "human", battleUnitId = "viking-1", unitId = "viking"))
    }

    @Test
    fun `Join should not change when the battle unit is already in the party`() {
        // Given
        val party = party(battleUnitIds = listOf("knight-1", "knight-2"))
        // When
        val joinedParty = party.join("knight-2", "knight")
        // Then
        joinedParty shouldBe party
    }

    @Test
    fun `Leave should keep the leader when a follower leaves the party`() {
        // When
        val party = party(battleUnitIds = listOf("knight-1", "knight-2", "knight-3")).leave("knight-2")
        // Then
        party.toDto().leaderBattleUnitId shouldBe "knight-1"
        party.toDto().battleUnitIds.shouldContainExactly("knight-1", "knight-3")
    }

    @Test
    fun `Leave should have no leader when the last battle unit leaves the party`() {
        // When
        val party = party(battleUnitIds = listOf("knight-1")).leave("knight-1")
        // Then
        party.toDto().leaderBattleUnitId.shouldBeNull()
        party.toDto().battleUnitIds.shouldBeEmpty()
    }

    @Test
    fun `StandingBattleUnitIds should leave out the battle units that moved during the second when they are marked as moved`() {
        // When
        val party = party().markMoved("knight-1").markMoved("rat-1")
        // Then
        party.standingBattleUnitIds(setOf("knight-1", "knight-2", "rat-1", "rat-2")) shouldBe setOf("knight-2", "rat-2")
    }

    @Test
    fun `StartNextSecond should have every battle unit standing when the next second starts`() {
        // Given
        val party = party().markMoved("knight-1")
        // When
        val nextSecondParty = party.startNextSecond()
        // Then
        nextSecondParty.standingBattleUnitIds(setOf("knight-1", "knight-2")) shouldBe setOf("knight-1", "knight-2")
    }

    @Test
    fun `StartNextSecond should announce the next second`() {
        // When
        val (events, _) = party().startNextSecond().pullEvents()
        // Then
        events shouldBe setOf(PartyEvent.ExplorationSecondStarted(playerId = "human"))
    }

    @Test
    fun `MarkMoved should not remember the battle units moving while battling`() {
        // When
        val party = party().pause().markMoved("knight-1")
        // Then
        party.standingBattleUnitIds(setOf("knight-1")) shouldBe setOf("knight-1")
    }

    @Test
    fun `Pause should forget the battle units that moved while exploring when a battle starts`() {
        // When
        val party = party().markMoved("knight-1").pause().resume()
        // Then
        party.standingBattleUnitIds(setOf("knight-1")) shouldBe setOf("knight-1")
    }

    @Test
    fun `Talk should talk to the npc and stop exploring when the party can walk`() {
        // When
        val talkingParty = party().talk("the-guy")
        // Then
        talkingParty.toDto().talkingNpcId shouldBe "the-guy"
        talkingParty.toDto().isExploring.shouldBeFalse()
        talkingParty.canWalk().shouldBeFalse()
        talkingParty.pullEvents().first.shouldContainExactly(PartyEvent.NpcTalkStarted(playerId = "human", npcId = "the-guy"))
    }

    @Test
    fun `Talk should not talk while the party is walking`() {
        // Given
        val walkingParty = party().walkParty(listOf(leaderStep, followerStep))
        // When
        val party = walkingParty.talk("the-guy")
        // Then
        party shouldBe walkingParty
    }

    @Test
    fun `Talk should not talk while battling`() {
        // Given
        val pausedParty = party().pause().pullEvents().second
        // When
        val party = pausedParty.talk("the-guy")
        // Then
        party shouldBe pausedParty
    }

    @Test
    fun `Talk should not talk to another npc while talking`() {
        // Given
        val talkingParty = party().talk("the-guy").pullEvents().second
        // When
        val party = talkingParty.talk("the-girl")
        // Then
        party shouldBe talkingParty
    }

    @Test
    fun `MarkMoved should not remember the battle units moving while talking`() {
        // When
        val party = party().talk("the-guy").markMoved("knight-1").finishTalk()
        // Then
        party.standingBattleUnitIds(setOf("knight-1")) shouldBe setOf("knight-1")
    }

    @Test
    fun `FinishTalk should explore again when the party is talking`() {
        // Given
        val talkingParty = party().talk("the-guy").pullEvents().second
        // When
        val party = talkingParty.finishTalk()
        // Then
        party.toDto().talkingNpcId.shouldBeNull()
        party.toDto().isExploring.shouldBeTrue()
        party.canWalk().shouldBeTrue()
        party.pullEvents().first.shouldContainExactly(PartyEvent.NpcTalkFinished(playerId = "human", npcId = "the-guy"))
    }

    @Test
    fun `FinishTalk should not change when the party is not talking`() {
        // Given
        val exploringParty = party()
        // When
        val party = exploringParty.finishTalk()
        // Then
        party shouldBe exploringParty
    }

    @Test
    fun `Pause should abandon the talk when a battle starts`() {
        // Given
        val talkingParty = party().talk("the-guy").pullEvents().second
        // When
        val pausedParty = talkingParty.pause().resume()
        // Then
        pausedParty.toDto().talkingNpcId.shouldBeNull()
        pausedParty.canWalk().shouldBeTrue()
    }
}
