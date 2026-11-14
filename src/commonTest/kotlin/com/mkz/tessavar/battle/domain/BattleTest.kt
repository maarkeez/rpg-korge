package com.mkz.tessavar.battle.domain

import com.mkz.tessavar.battle.domain.BattleMother.battle
import com.mkz.tessavar.battle.domain.BattleMother.finishedBattle
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.test.Test

class BattleTest {
    private val battleUnitIds = setOf("battle-unit-1", "battle-unit-2")
    private val battleUnits = mapOf("battle-unit-1" to "player-1", "battle-unit-2" to "player-2")

    @Test
    fun `StartFirstRound should start the first round with the first player when two players join`() {
        // Given
        val players = listOf("player-1", "player-2")
        // When
        val battle = Battle.startFirstRound(players, battleUnits)
        // Then
        battle.toDto() shouldBe Battle.Dto(currentPlayerId = players.first(), currentRound = 1, isFinished = false, battleUnitIds = battleUnitIds)
    }

    @Test
    fun `StartFirstRound should publish the battle and first player turn events when the battle starts`() {
        // Given
        val players = listOf("player-1", "player-2")
        // When
        val battle = Battle.startFirstRound(players, battleUnits)
        // Then
        val (events, _) = battle.pullEvents()
        events.shouldContainExactly(BattleEvent.BattleStarted, BattleEvent.PlayerTurnStarted(players.first(), battleUnitIds))
    }

    @Test
    fun `StartFirstRound should fail when fewer than two players join`() {
        // Given
        val players = listOf("player-1")
        // When
        val result = runCatching { Battle.startFirstRound(players, battleUnits) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<BattleError.MinimumTwoPlayersRequired>()
    }

    @Test
    fun `ToDto should expose the current player turn and round when converted to dto`() {
        // Given
        val players = listOf("player-1", "player-2")
        val battle = battle(players, battleUnitIds)
        // When
        val result = battle.toDto()
        // Then
        result shouldBe Battle.Dto(currentPlayerId = players.first(), currentRound = 1, isFinished = false, battleUnitIds = battleUnitIds)
    }

    @Test
    fun `ToDto should expose the finished state as false when the battle has not finished`() {
        // Given
        val battle = battle()
        // When
        val result = battle.toDto().isFinished
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `ToDto should expose the finished state as true when the battle has finished`() {
        // Given
        val players = listOf("player-1", "player-2")
        val battle = battle(players, battleUnitIds).finish()
        // When
        val result = battle.toDto().isFinished
        // Then
        result.shouldBeTrue()
    }

    @Test
    fun `StartNextRound should increment the round and start the first player turn when a battle exists`() {
        // Given
        val players = listOf("player-1", "player-2")
        val battle = battle(players, battleUnitIds)
        // When
        val nextBattle = battle.startNextRound()
        // Then
        nextBattle.toDto() shouldBe Battle.Dto(currentPlayerId = players.first(), currentRound = 2, isFinished = false, battleUnitIds = battleUnitIds)
        val (events, _) = nextBattle.pullEvents()
        events.shouldContainExactly(BattleEvent.RoundStarted(2, battleUnitIds), BattleEvent.PlayerTurnStarted(players.first(), battleUnitIds))
    }

    @Test
    fun `FinishPlayerTurn should finish the player turn, then start the next one when another player remains in the queue`() {
        // Given
        val players = listOf("player-1", "player-2")
        val battle = battle(players, battleUnitIds)
        // When
        val nextBattle = battle.finishPlayerTurn()
        // Then
        nextBattle.toDto() shouldBe Battle.Dto(currentPlayerId = players[1], currentRound = 1, isFinished = false, battleUnitIds = battleUnitIds)
        val (events, _) = nextBattle.pullEvents()
        events.shouldContainExactly(BattleEvent.PlayerTurnFinished(players[0], battleUnitIds), BattleEvent.PlayerTurnStarted(players[1], battleUnitIds))
    }

    @Test
    fun `FinishPlayerTurn should finish the player turn, then the round when the current player is the last one in the queue`() {
        // Given
        val players = listOf("player-1", "player-2")
        val battle =
            battle(players, battleUnitIds)
                .finishPlayerTurn()
                .pullEvents()
                .second
        // When
        val nextBattle = battle.finishPlayerTurn()
        // Then
        nextBattle.toDto() shouldBe Battle.Dto(currentPlayerId = players[1], currentRound = 1, isFinished = false, battleUnitIds = battleUnitIds)
        val (events, _) = nextBattle.pullEvents()
        events.shouldContainExactly(BattleEvent.PlayerTurnFinished(players[1], battleUnitIds), BattleEvent.RoundFinished(1, battleUnitIds))
    }

    @Test
    fun `IsDisengaged should be disengaged when only one player has battle units in the battle`() {
        // Given
        val players = listOf("player-1", "player-2")
        val battle = battle(players, battleUnits = battleUnits).leave("battle-unit-1")
        // When
        val result = battle.isDisengaged()
        // Then
        result.shouldBeTrue()
    }

    @Test
    fun `IsDisengaged should not be disengaged when every player has battle units in the battle`() {
        // Given
        val players = listOf("player-1", "player-2")
        val battle = battle(players, battleUnits = battleUnits)
        // When
        val result = battle.isDisengaged()
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `Finish should finish the battle and publish that it finished when it is disengaged`() {
        // Given
        val players = listOf("player-1", "player-2")
        val battle = battle(players, battleUnitIds)
        // When
        val finishedBattle = battle.finish()
        // Then
        finishedBattle.toDto().isFinished.shouldBeTrue()
        val (events, _) = finishedBattle.pullEvents()
        events.shouldContainExactly(BattleEvent.BattleFinished)
    }

    @Test
    fun `PullEvents should pull the pending events and clear them when the battle has events`() {
        // Given
        val players = listOf("player-1", "player-2")
        val battle = Battle.startFirstRound(players, battleUnits)
        // When
        val (events, clearedBattle) = battle.pullEvents()
        // Then
        events.shouldContainExactly(BattleEvent.BattleStarted, BattleEvent.PlayerTurnStarted(players.first(), battleUnitIds))
        clearedBattle.pullEvents().first.shouldBeEmpty()
    }

    @Test
    fun `PullEvents should not pull events when there are no pending events`() {
        // Given
        val battle = battle()
        // When
        val (events, _) = battle.pullEvents()
        // Then
        events.shouldBeEmpty()
    }

    @Test
    fun `Join should add the battle unit to the battle when it joins`() {
        // Given
        val battle = battle(listOf("player-1", "player-2"), battleUnitIds)
        // When
        val joinedBattle = battle.join("battle-unit-3", "player-1")
        // Then
        joinedBattle.toDto().battleUnitIds shouldBe setOf("battle-unit-1", "battle-unit-2", "battle-unit-3")
    }

    @Test
    fun `Join should keep the battle engaged when the battle unit of a player whose last battle unit left joins`() {
        // Given
        val battle = battle(listOf("player-1", "player-2"), battleUnits = battleUnits).leave("battle-unit-1")
        // When
        val joinedBattle = battle.join("battle-unit-3", "player-1")
        // Then
        joinedBattle.isDisengaged().shouldBeFalse()
    }

    @Test
    fun `Join should keep the battle units when the battle unit is already in the battle`() {
        // Given
        val battle = battle(listOf("player-1", "player-2"), battleUnitIds)
        // When
        val joinedBattle = battle.join("battle-unit-1", "player-1")
        // Then
        joinedBattle.toDto().battleUnitIds shouldBe battleUnitIds
    }

    @Test
    fun `Leave should remove the battle unit from the battle when it leaves`() {
        // Given
        val battle = battle(listOf("player-1", "player-2"), battleUnitIds)
        // When
        val leftBattle = battle.leave("battle-unit-1")
        // Then
        leftBattle.toDto().battleUnitIds shouldBe setOf("battle-unit-2")
    }

    @Test
    fun `LeaveDuringRound should disengage the battle with the battle units left when the last battle unit of a player leaves`() {
        // Given
        val battle = battle(listOf("player-1", "player-2"), battleUnitIds)
        // When
        val (events, leftBattle) = battle.leaveDuringRound("battle-unit-1").pullEvents()
        // Then
        leftBattle.toDto().battleUnitIds shouldBe setOf("battle-unit-2")
        events.shouldContainExactly(BattleEvent.BattleDisengaged(setOf("battle-unit-2")))
    }

    @Test
    fun `LeaveDuringRound should not disengage the battle when other battle units of the player are left in it`() {
        // Given
        val battle = battle(listOf("player-1", "player-2"), battleUnits = battleUnits + ("battle-unit-3" to "player-1"))
        // When
        val (events, leftBattle) = battle.leaveDuringRound("battle-unit-1").pullEvents()
        // Then
        leftBattle.toDto().battleUnitIds shouldBe setOf("battle-unit-2", "battle-unit-3")
        events.shouldBeEmpty()
    }

    @Test
    fun `LeaveDuringRound should not disengage the battle again when a battle unit out of it leaves`() {
        // Given
        val battle = battle(listOf("player-1", "player-2"), battleUnitIds).leave("battle-unit-1")
        // When
        val (events, _) = battle.leaveDuringRound("battle-unit-3").pullEvents()
        // Then
        events.shouldBeEmpty()
    }

    @Test
    fun `LeaveDuringRound should not disengage the battle when it is finished`() {
        // Given
        val battle =
            battle(listOf("player-1", "player-2"), battleUnits = mapOf("battle-unit-2" to "player-2"))
                .finish()
                .join("battle-unit-1", "player-1")
        // When
        val (events, _) =
            battle
                .pullEvents()
                .second
                .leaveDuringRound("battle-unit-1")
                .pullEvents()
        // Then
        events.shouldBeEmpty()
    }

    @Test
    fun `FinishPlayerTurn should start the next player turn with the battle units in the battle when one has left`() {
        // Given
        val players = listOf("player-1", "player-2")
        val battle = battle(players, battleUnitIds).leave("battle-unit-2")
        // When
        val nextBattle = battle.finishPlayerTurn()
        // Then
        val (events, _) = nextBattle.pullEvents()
        events.shouldContainExactly(BattleEvent.PlayerTurnFinished(players[0], setOf("battle-unit-1")), BattleEvent.PlayerTurnStarted(players[1], setOf("battle-unit-1")))
    }

    @Test
    fun `StartNextRound should start the round with the battle units in the battle when one has joined`() {
        // Given
        val players = listOf("player-1", "player-2")
        val battle = battle(players, battleUnitIds).join("battle-unit-3", "player-1")
        // When
        val nextBattle = battle.startNextRound()
        // Then
        val joinedBattleUnitIds = setOf("battle-unit-1", "battle-unit-2", "battle-unit-3")
        val (events, _) = nextBattle.pullEvents()
        events.shouldContainExactly(
            BattleEvent.RoundStarted(2, joinedBattleUnitIds),
            BattleEvent.PlayerTurnStarted(players.first(), joinedBattleUnitIds),
        )
    }
}
