package com.mkz.rpg.player.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PlayerTest {
    @Nested
    inner class CreateHuman {
        @Test
        fun `should create a human player when the id and name are valid`() {
            // Given
            val playerId = "player-1"
            val playerName = "David"
            // When
            val createdPlayer = Player.createHuman(playerId, playerName)
            // Then
            assertThat(createdPlayer.toDto())
                .isEqualTo(Player.Dto(id = playerId, name = playerName, type = Player.Dto.PlayerTypeDto.HUMAN))
        }

        @Test
        fun `should publish the player created event when a human player is created`() {
            // Given
            val playerId = "player-1"
            val playerName = "David"
            // When
            val createdPlayer = Player.createHuman(playerId, playerName)
            // Then
            val (events, _) = createdPlayer.pullEvents()
            assertThat(events)
                .containsExactly(
                    PlayerEvent.PlayerCreated(
                        playerId = playerId,
                        playerName = playerName,
                        playerType = Player.Dto.PlayerTypeDto.HUMAN,
                    ),
                )
        }

        @Test
        fun `should fail when the player id is empty`() {
            // When
            val result = runCatching { Player.createHuman("", "David") }
            // Then
            assertThat(result.exceptionOrNull()).isExactlyInstanceOf(PlayerError.EmptyPlayerId::class.java)
        }

        @Test
        fun `should fail when the player name is empty`() {
            // When
            val result = runCatching { Player.createHuman("player-1", "") }
            // Then
            assertThat(result.exceptionOrNull()).isExactlyInstanceOf(PlayerError.EmptyPlayerName::class.java)
        }

        @Test
        fun `should fail when the player name is longer than 50 characters`() {
            // Given
            val playerName = "a".repeat(51)
            // When
            val result = runCatching { Player.createHuman("player-1", playerName) }
            // Then
            assertThat(result.exceptionOrNull()).isExactlyInstanceOf(PlayerError.PlayerNameLongerThanExpected::class.java)
        }
    }

    @Nested
    inner class CreateCpu {
        @Test
        fun `should create a cpu player when the id and name are valid`() {
            // Given
            val playerId = "player-2"
            val playerName = "Robo"
            // When
            val createdPlayer = Player.createCpu(playerId, playerName)
            // Then
            assertThat(createdPlayer.toDto())
                .isEqualTo(Player.Dto(id = playerId, name = playerName, type = Player.Dto.PlayerTypeDto.CPU))
        }

        @Test
        fun `should publish the player created event when a cpu player is created`() {
            // Given
            val playerId = "player-2"
            val playerName = "Robo"
            // When
            val createdPlayer = Player.createCpu(playerId, playerName)
            // Then
            val (events, _) = createdPlayer.pullEvents()
            assertThat(events)
                .containsExactly(
                    PlayerEvent.PlayerCreated(
                        playerId = playerId,
                        playerName = playerName,
                        playerType = Player.Dto.PlayerTypeDto.CPU,
                    ),
                )
        }

        @Test
        fun `should fail when the player id is empty`() {
            // When
            val result = runCatching { Player.createCpu("", "Robo") }
            // Then
            assertThat(result.exceptionOrNull()).isExactlyInstanceOf(PlayerError.EmptyPlayerId::class.java)
        }
    }

    @Nested
    inner class ToDto {
        @Test
        fun `should expose the player data when converted to dto`() {
            // Given
            val player = Player.createHuman("player-1", "David")
            // When
            val result = player.toDto()
            // Then
            assertThat(result)
                .isEqualTo(Player.Dto(id = "player-1", name = "David", type = Player.Dto.PlayerTypeDto.HUMAN))
        }
    }

    @Nested
    inner class PullEvents {
        @Test
        fun `should pull the created event when the player has pending events`() {
            // Given
            val createdPlayer = Player.createHuman("player-1", "David")
            // When
            val (events, _) = createdPlayer.pullEvents()
            // Then
            assertThat(events).isNotEmpty()
        }

        @Test
        fun `should not pull events when there are no pending events`() {
            // Given
            val player = PlayerMother.player()
            // When
            val (events, _) = player.pullEvents()
            // Then
            assertThat(events).isEmpty()
        }
    }
}
