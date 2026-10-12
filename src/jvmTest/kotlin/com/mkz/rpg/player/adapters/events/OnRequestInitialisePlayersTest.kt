package com.mkz.rpg.player.adapters.events

import com.mkz.rpg.player.adapters.storage.InMemoryPlayerRepository
import com.mkz.rpg.player.domain.PlayerEvent
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.player.usecases.commands.InitialisePlayers
import com.mkz.rpg.player.usecases.services.FakePlayerLoader
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class OnRequestInitialisePlayersTest {
    private val eventBus = InMemoryEventBus()
    private val playerRepository = InMemoryPlayerRepository()
    private val human = player(id = "human").toDto()
    private val onRequestInitialisePlayers = OnRequestInitialisePlayers(InitialisePlayers(FakePlayerLoader(players = listOf(human)), playerRepository, eventBus), eventBus)

    @Test
    fun `should initialise the players when the request is dispatched`() {
        // Given
        eventBus.publish(PlayerEvent.RequestInitialisePlayers)
        // When
        eventBus.dispatch()
        // Then
        playerRepository.searchById("human")?.toDto() shouldBe human
    }
}
