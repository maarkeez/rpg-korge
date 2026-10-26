package com.mkz.rpg.player.domain

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.test.Test

class PlayerTest {
    @Test
    fun `CreateHuman should create a human player when the id is valid`() {
        // Given
        val playerId = "player-1"
        // When
        val createdPlayer = Player.createHuman(playerId)
        // Then
        createdPlayer.toDto() shouldBe Player.Dto(id = playerId, type = Player.Dto.PlayerTypeDto.HUMAN)
    }

    @Test
    fun `CreateHuman should publish the player created event when a human player is created`() {
        // Given
        val playerId = "player-1"
        // When
        val createdPlayer = Player.createHuman(playerId)
        // Then
        val (events, _) = createdPlayer.pullEvents()
        events.shouldContainExactly(PlayerEvent.PlayerCreated(playerId = playerId, playerType = Player.Dto.PlayerTypeDto.HUMAN))
    }

    @Test
    fun `CreateHuman should fail when the player id is empty`() {
        // When
        val result = runCatching { Player.createHuman("") }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<PlayerError.EmptyPlayerId>()
    }

    @Test
    fun `CreateCpu should create a cpu player when the id is valid`() {
        // Given
        val playerId = "player-2"
        // When
        val createdPlayer = Player.createCpu(playerId)
        // Then
        createdPlayer.toDto() shouldBe Player.Dto(id = playerId, type = Player.Dto.PlayerTypeDto.CPU)
    }

    @Test
    fun `CreateCpu should publish the player created event when a cpu player is created`() {
        // Given
        val playerId = "player-2"
        // When
        val createdPlayer = Player.createCpu(playerId)
        // Then
        val (events, _) = createdPlayer.pullEvents()
        events.shouldContainExactly(PlayerEvent.PlayerCreated(playerId = playerId, playerType = Player.Dto.PlayerTypeDto.CPU))
    }

    @Test
    fun `CreateCpu should fail when the player id is empty`() {
        // When
        val result = runCatching { Player.createCpu("") }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<PlayerError.EmptyPlayerId>()
    }

    @Test
    fun `ToDto should expose the player data when converted to dto`() {
        // Given
        val player = Player.createHuman("player-1")
        // When
        val result = player.toDto()
        // Then
        result shouldBe Player.Dto(id = "player-1", type = Player.Dto.PlayerTypeDto.HUMAN)
    }

    @Test
    fun `PullEvents should pull the created event when the player has pending events`() {
        // Given
        val createdPlayer = Player.createHuman("player-1")
        // When
        val (events, _) = createdPlayer.pullEvents()
        // Then
        events.shouldNotBeEmpty()
    }

    @Test
    fun `PullEvents should not pull events when there are no pending events`() {
        // Given
        val player = PlayerMother.player()
        // When
        val (events, _) = player.pullEvents()
        // Then
        events.shouldBeEmpty()
    }
}
