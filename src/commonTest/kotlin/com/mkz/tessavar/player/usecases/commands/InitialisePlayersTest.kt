package com.mkz.tessavar.player.usecases.commands

import com.mkz.tessavar.player.adapters.storage.InMemoryPlayerRepository
import com.mkz.tessavar.player.domain.Player.Dto.PlayerTypeDto.CPU
import com.mkz.tessavar.player.domain.Player.Dto.PlayerTypeDto.HUMAN
import com.mkz.tessavar.player.domain.PlayerEvent.PlayerCreated
import com.mkz.tessavar.player.domain.PlayerMother.player
import com.mkz.tessavar.player.usecases.services.FakePlayerLoader
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InitialisePlayersTest {
    private val playerRepository = InMemoryPlayerRepository()
    private val eventBus = FakeEventBus()

    @Test
    fun `should create every loaded player when players are initialised`() {
        // Given
        val human = player(id = "player-one", type = HUMAN).toDto()
        val cpu = player(id = "player-two", type = CPU).toDto()
        val initialisePlayers = InitialisePlayers(FakePlayerLoader(players = listOf(human, cpu)), playerRepository, eventBus)
        // When
        initialisePlayers()
        // Then
        playerRepository.searchById("player-one")?.toDto() shouldBe human
        playerRepository.searchById("player-two")?.toDto() shouldBe cpu
        eventBus.shouldHavePublishedEvents(
            PlayerCreated(playerId = "player-one", playerType = HUMAN),
            PlayerCreated(playerId = "player-two", playerType = CPU),
        )
    }

    @Test
    fun `should not create a player again when the player already exists`() {
        // Given
        val human = player(id = "player-one", type = HUMAN)
        playerRepository.create(human)
        val initialisePlayers = InitialisePlayers(FakePlayerLoader(players = listOf(player(id = "player-one", type = CPU).toDto())), playerRepository, eventBus)
        // When
        initialisePlayers()
        // Then
        playerRepository.searchById("player-one")?.toDto() shouldBe human.toDto()
        eventBus.shouldNotHavePublishedEvents()
    }
}
