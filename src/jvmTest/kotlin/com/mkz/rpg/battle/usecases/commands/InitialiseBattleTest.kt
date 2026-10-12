package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.BattleEvent.BattleStarted
import com.mkz.rpg.battle.domain.BattleEvent.PlayerTurnStarted
import com.mkz.rpg.battle.usecases.services.FakeBattleLoader
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class InitialiseBattleTest {
    private val battleRepository = InMemoryBattleRepository()
    private val eventBus = FakeEventBus()

    @Test
    fun `should start the first round with the first loaded player when the battle is initialised`() {
        // Given
        val initialiseBattle = InitialiseBattle(FakeBattleLoader(playerOrder = listOf("player-one", "player-two")), battleRepository, eventBus)
        // When
        initialiseBattle()
        // Then
        val battle = battleRepository.search()?.toDto()
        battle?.currentRound shouldBe 1
        battle?.currentPlayerTurn shouldBe "player-one"
        eventBus.shouldHavePublishedEvents(BattleStarted, PlayerTurnStarted("player-one"))
    }
}
