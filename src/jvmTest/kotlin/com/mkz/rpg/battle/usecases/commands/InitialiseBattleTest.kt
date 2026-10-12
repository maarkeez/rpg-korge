package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.BattleEvent.BattleStarted
import com.mkz.rpg.battle.domain.BattleEvent.PlayerTurnStarted
import com.mkz.rpg.battle.usecases.services.BattleLoader
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class InitialiseBattleTest {
    private val battleRepository = InMemoryBattleRepository()
    private val eventBus = FakeEventBus()
    private val battleLoader: BattleLoader = mock()
    private val initialiseBattle = InitialiseBattle(battleLoader, battleRepository, eventBus)

    @Test
    fun `should start the first round with the first loaded player when the battle is initialised`() {
        // Given
        whenever(battleLoader.loadPlayerOrder()).thenReturn(listOf("player-one", "player-two"))
        // When
        initialiseBattle()
        // Then
        val battle = battleRepository.search()?.toDto()
        battle?.currentRound shouldBe 1
        battle?.currentPlayerTurn shouldBe "player-one"
        eventBus.shouldHavePublishedEvents(BattleStarted, PlayerTurnStarted("player-one"))
    }
}
