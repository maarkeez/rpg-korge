package com.mkz.rpg.battle.domain

import com.mkz.rpg.battle.domain.BattleMother.battle
import com.mkz.rpg.battle.domain.BattleMother.finishedBattle
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.test.Test

class BattleTest {
    private val battleUnitIds = setOf("battle-unit-1", "battle-unit-2")

    @Test
    fun `StartFirstRound should start the first round with the first player when two players join`() {
        // Given
        val players = listOf("player-1", "player-2")
        // When
        val battle = Battle.startFirstRound(players, battleUnitIds)
        // Then
        battle.toDto() shouldBe Battle.Dto(currentPlayerTurn = players.first(), currentRound = 1, isFinished = false, battleUnitIds = battleUnitIds)
    }

    @Test
    fun `StartFirstRound should publish the battle and first player turn events when the battle starts`() {
        // Given
        val players = listOf("player-1", "player-2")
        // When
        val battle = Battle.startFirstRound(players, battleUnitIds)
        // Then
        val (events, _) = battle.pullEvents()
        events.shouldContainExactly(BattleEvent.BattleStarted, BattleEvent.PlayerTurnStarted(players.first(), battleUnitIds))
    }

    @Test
    fun `StartFirstRound should fail when fewer than two players join`() {
        // Given
        val players = listOf("player-1")
        // When
        val result = runCatching { Battle.startFirstRound(players, battleUnitIds) }
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
        result shouldBe Battle.Dto(currentPlayerTurn = players.first(), currentRound = 1, isFinished = false, battleUnitIds = battleUnitIds)
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
        val battle = battle(players, battleUnitIds).defeatPlayer(players[1]).finishBattle()
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
        nextBattle.toDto() shouldBe Battle.Dto(currentPlayerTurn = players.first(), currentRound = 2, isFinished = false, battleUnitIds = battleUnitIds)
        val (events, _) = nextBattle.pullEvents()
        events.shouldContainExactly(BattleEvent.BattleRoundStarted(2, battleUnitIds), BattleEvent.PlayerTurnStarted(players.first(), battleUnitIds))
    }

    @Test
    fun `FinishPlayerTurn should start the next player turn when another player remains in the queue`() {
        // Given
        val players = listOf("player-1", "player-2")
        val battle = battle(players, battleUnitIds)
        // When
        val nextBattle = battle.finishPlayerTurn()
        // Then
        nextBattle.toDto() shouldBe Battle.Dto(currentPlayerTurn = players[1], currentRound = 1, isFinished = false, battleUnitIds = battleUnitIds)
        val (events, _) = nextBattle.pullEvents()
        events.shouldContainExactly(BattleEvent.PlayerTurnStarted(players[1], battleUnitIds))
    }

    @Test
    fun `FinishPlayerTurn should finish the round when the current player is the last one in the queue`() {
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
        nextBattle.toDto() shouldBe Battle.Dto(currentPlayerTurn = players[1], currentRound = 1, isFinished = false, battleUnitIds = battleUnitIds)
        val (events, _) = nextBattle.pullEvents()
        events.shouldContainExactly(BattleEvent.BattleRoundFinished(1, battleUnitIds))
    }

    @Test
    fun `IsBattleFinished should be finished when only one player remains`() {
        // Given
        val players = listOf("player-1", "player-2")
        val battle = battle(players, battleUnitIds).defeatPlayer(players[1])
        // When
        val result = battle.isBattleFinished()
        // Then
        result.shouldBeTrue()
    }

    @Test
    fun `IsBattleFinished should not be finished when more than one player remains`() {
        // Given
        val players = listOf("player-1", "player-2")
        val battle = battle(players, battleUnitIds)
        // When
        val result = battle.isBattleFinished()
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `IsDisengaged should be disengaged when only one player has battle units in the battle`() {
        // Given
        val players = listOf("player-1", "player-2")
        val battle = battle(players, battleUnitIds)
        // When
        val result = battle.isDisengaged(playersInBattle = setOf("player-2"))
        // Then
        result.shouldBeTrue()
    }

    @Test
    fun `IsDisengaged should not be disengaged when every player has battle units in the battle`() {
        // Given
        val players = listOf("player-1", "player-2")
        val battle = battle(players, battleUnitIds)
        // When
        val result = battle.isDisengaged(playersInBattle = setOf("player-1", "player-2"))
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `FinishWithoutWinner should finish the battle and publish that it finished when it is disengaged`() {
        // Given
        val players = listOf("player-1", "player-2")
        val battle = battle(players, battleUnitIds)
        // When
        val finishedBattle = battle.finishWithoutWinner()
        // Then
        finishedBattle.toDto().isFinished.shouldBeTrue()
        val (events, _) = finishedBattle.pullEvents()
        events.shouldContainExactly(BattleEvent.BattleFinished)
    }

    @Test
    fun `FinishBattle should publish the player victory when a single player remains`() {
        // Given
        val players = listOf("player-1", "player-2")
        val winner = players[1]
        val battle = finishedBattle(players)
        // When
        val finishedBattle = battle.finishBattle()
        // Then
        val (events, _) = finishedBattle.pullEvents()
        events.shouldContainExactly(BattleEvent.PlayerVictory(winner))
    }

    @Test
    fun `DefeatPlayer should remove the player and publish the defeat when the defeated player is not the current player`() {
        // Given
        val players = listOf("player-1", "player-2")
        val defeatedPlayer = players[1]
        val battle = battle(players, battleUnitIds)
        // When
        val nextBattle = battle.defeatPlayer(defeatedPlayer)
        // Then
        nextBattle.toDto() shouldBe Battle.Dto(currentPlayerTurn = players.first(), currentRound = 1, isFinished = false, battleUnitIds = battleUnitIds)
        val (events, _) = nextBattle.pullEvents()
        events.shouldContainExactly(BattleEvent.PlayerDefeated(defeatedPlayer))
    }

    @Test
    fun `DefeatPlayer should start the next player turn when the defeated player is the current player`() {
        // Given
        val players = listOf("player-1", "player-2")
        val defeatedPlayer = players.first()
        val battle = battle(players, battleUnitIds)
        // When
        val nextBattle = battle.defeatPlayer(defeatedPlayer)
        // Then
        nextBattle.toDto() shouldBe Battle.Dto(currentPlayerTurn = players[1], currentRound = 1, isFinished = false, battleUnitIds = battleUnitIds)
        val (events, _) = nextBattle.pullEvents()
        events.shouldContainExactly(BattleEvent.PlayerDefeated(defeatedPlayer))
    }

    @Test
    fun `PullEvents should pull the pending events and clear them when the battle has events`() {
        // Given
        val players = listOf("player-1", "player-2")
        val battle = Battle.startFirstRound(players, battleUnitIds)
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
        val joinedBattle = battle.join("battle-unit-3")
        // Then
        joinedBattle.toDto().battleUnitIds shouldBe setOf("battle-unit-1", "battle-unit-2", "battle-unit-3")
    }

    @Test
    fun `Join should keep the battle units when the battle unit is already in the battle`() {
        // Given
        val battle = battle(listOf("player-1", "player-2"), battleUnitIds)
        // When
        val joinedBattle = battle.join("battle-unit-1")
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
    fun `FinishPlayerTurn should start the next player turn with the battle units in the battle when one has left`() {
        // Given
        val players = listOf("player-1", "player-2")
        val battle = battle(players, battleUnitIds).leave("battle-unit-2")
        // When
        val nextBattle = battle.finishPlayerTurn()
        // Then
        val (events, _) = nextBattle.pullEvents()
        events.shouldContainExactly(BattleEvent.PlayerTurnStarted(players[1], setOf("battle-unit-1")))
    }

    @Test
    fun `StartNextRound should start the round with the battle units in the battle when one has joined`() {
        // Given
        val players = listOf("player-1", "player-2")
        val battle = battle(players, battleUnitIds).join("battle-unit-3")
        // When
        val nextBattle = battle.startNextRound()
        // Then
        val joinedBattleUnitIds = setOf("battle-unit-1", "battle-unit-2", "battle-unit-3")
        val (events, _) = nextBattle.pullEvents()
        events.shouldContainExactly(
            BattleEvent.BattleRoundStarted(2, joinedBattleUnitIds),
            BattleEvent.PlayerTurnStarted(players.first(), joinedBattleUnitIds),
        )
    }

    @Test
    fun `DefeatWhileExploring should defeat the player and leave the winner as the only player when the game finishes out of the battles`() {
        // When
        val battle = Battle.defeatWhileExploring(defeatedPlayerId = "player-1", winnerPlayerId = "player-2")
        // Then
        battle.isBattleFinished().shouldBeTrue()
        battle.toDto().isFinished.shouldBeFalse()
        battle.toDto().battleUnitIds.shouldBeEmpty()
        val (events, defeatedBattle) = battle.pullEvents()
        events.shouldContainExactly(BattleEvent.PlayerDefeated("player-1"))
        val (victoryEvents, _) = defeatedBattle.finishBattle().pullEvents()
        victoryEvents.shouldContainExactly(BattleEvent.PlayerVictory("player-2"))
    }
}
