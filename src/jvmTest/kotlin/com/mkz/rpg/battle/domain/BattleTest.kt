package com.mkz.rpg.battle.domain

import com.mkz.rpg.battle.domain.BattleMother.battle
import com.mkz.rpg.battle.domain.BattleMother.finishedBattle
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BattleTest {
    @Nested
    inner class StartFirstRound {
        @Test
        fun `should start the first round with the first player when two players join`() {
            // Given
            val players = listOf("player-1", "player-2")
            // When
            val battle = Battle.startFirstRound(players)
            // Then
            assertThat(battle.toDto()).isEqualTo(Battle.Dto(currentPlayerTurn = players.first(), currentRound = 1))
        }

        @Test
        fun `should publish the battle and first player turn events when the battle starts`() {
            // Given
            val players = listOf("player-1", "player-2")
            // When
            val battle = Battle.startFirstRound(players)
            // Then
            val (events, _) = battle.pullEvents()
            assertThat(events).containsExactly(BattleEvent.BattleStarted, BattleEvent.PlayerTurnStarted(players.first()))
        }

        @Test
        fun `should fail when fewer than two players join`() {
            // Given
            val players = listOf("player-1")
            // When
            val result = runCatching { Battle.startFirstRound(players) }
            // Then
            assertThat(result.exceptionOrNull()).isExactlyInstanceOf(BattleError.MinimumTwoPlayersRequired::class.java)
        }
    }

    @Nested
    inner class ToDto {
        @Test
        fun `should expose the current player turn and round when converted to dto`() {
            // Given
            val players = listOf("player-1", "player-2")
            val battle = battle(players)
            // When
            val result = battle.toDto()
            // Then
            assertThat(result).isEqualTo(Battle.Dto(currentPlayerTurn = players.first(), currentRound = 1))
        }
    }

    @Nested
    inner class StartNextRound {
        @Test
        fun `should increment the round and start the first player turn when a battle exists`() {
            // Given
            val players = listOf("player-1", "player-2")
            val battle = battle(players)
            // When
            val nextBattle = battle.startNextRound()
            // Then
            assertThat(nextBattle.toDto()).isEqualTo(Battle.Dto(currentPlayerTurn = players.first(), currentRound = 2))
            val (events, _) = nextBattle.pullEvents()
            assertThat(events).containsExactly(BattleEvent.BattleRoundStarted(2), BattleEvent.PlayerTurnStarted(players.first()))
        }
    }

    @Nested
    inner class FinishPlayerTurn {
        @Test
        fun `should start the next player turn when another player remains in the queue`() {
            // Given
            val players = listOf("player-1", "player-2")
            val battle = battle(players)
            // When
            val nextBattle = battle.finishPlayerTurn()
            // Then
            assertThat(nextBattle.toDto()).isEqualTo(Battle.Dto(currentPlayerTurn = players[1], currentRound = 1))
            val (events, _) = nextBattle.pullEvents()
            assertThat(events).containsExactly(BattleEvent.PlayerTurnStarted(players[1]))
        }

        @Test
        fun `should finish the round when the current player is the last one in the queue`() {
            // Given
            val players = listOf("player-1", "player-2")
            val battle =
                battle(players)
                    .finishPlayerTurn()
                    .pullEvents()
                    .second
            // When
            val nextBattle = battle.finishPlayerTurn()
            // Then
            assertThat(nextBattle.toDto()).isEqualTo(Battle.Dto(currentPlayerTurn = players[1], currentRound = 1))
            val (events, _) = nextBattle.pullEvents()
            assertThat(events).containsExactly(BattleEvent.BattleRoundFinished(1))
        }
    }

    @Nested
    inner class IsBattleFinished {
        @Test
        fun `should be finished when only one player remains`() {
            // Given
            val players = listOf("player-1", "player-2")
            val battle = battle(players).defeatPlayer(players[1])
            // When
            val result = battle.isBattleFinished()
            // Then
            assertThat(result).isTrue
        }

        @Test
        fun `should not be finished when more than one player remains`() {
            // Given
            val players = listOf("player-1", "player-2")
            val battle = battle(players)
            // When
            val result = battle.isBattleFinished()
            // Then
            assertThat(result).isFalse
        }
    }

    @Nested
    inner class FinishBattle {
        @Test
        fun `should publish the player victory when a single player remains`() {
            // Given
            val players = listOf("player-1", "player-2")
            val winner = players[1]
            val battle = finishedBattle(players)
            // When
            val finishedBattle = battle.finishBattle()
            // Then
            val (events, _) = finishedBattle.pullEvents()
            assertThat(events).containsExactly(BattleEvent.PlayerVictory(winner))
        }
    }

    @Nested
    inner class DefeatPlayer {
        @Test
        fun `should remove the player and publish the defeat when the defeated player is not the current player`() {
            // Given
            val players = listOf("player-1", "player-2")
            val defeatedPlayer = players[1]
            val battle = battle(players)
            // When
            val nextBattle = battle.defeatPlayer(defeatedPlayer)
            // Then
            assertThat(nextBattle.toDto()).isEqualTo(Battle.Dto(currentPlayerTurn = players.first(), currentRound = 1))
            val (events, _) = nextBattle.pullEvents()
            assertThat(events).containsExactly(BattleEvent.PlayerDefeated(defeatedPlayer))
        }

        @Test
        fun `should start the next player turn when the defeated player is the current player`() {
            // Given
            val players = listOf("player-1", "player-2")
            val defeatedPlayer = players.first()
            val battle = battle(players)
            // When
            val nextBattle = battle.defeatPlayer(defeatedPlayer)
            // Then
            assertThat(nextBattle.toDto()).isEqualTo(Battle.Dto(currentPlayerTurn = players[1], currentRound = 1))
            val (events, _) = nextBattle.pullEvents()
            assertThat(events).containsExactly(BattleEvent.PlayerDefeated(defeatedPlayer))
        }
    }

    @Nested
    inner class PullEvents {
        @Test
        fun `should pull the pending events and clear them when the battle has events`() {
            // Given
            val players = listOf("player-1", "player-2")
            val battle = Battle.startFirstRound(players)
            // When
            val (events, clearedBattle) = battle.pullEvents()
            // Then
            assertThat(events).containsExactly(BattleEvent.BattleStarted, BattleEvent.PlayerTurnStarted(players.first()))
            assertThat(clearedBattle.pullEvents().first).isEmpty()
        }

        @Test
        fun `should not pull events when there are no pending events`() {
            // Given
            val battle = battle()
            // When
            val (events, _) = battle.pullEvents()
            // Then
            assertThat(events).isEmpty()
        }
    }
}
