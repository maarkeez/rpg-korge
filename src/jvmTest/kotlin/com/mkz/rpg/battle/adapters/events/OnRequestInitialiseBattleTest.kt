package com.mkz.rpg.battle.adapters.events

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.Battle
import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battle.usecases.commands.InitialiseBattle
import com.mkz.rpg.battle.usecases.services.FakeBattleLoader
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class OnRequestInitialiseBattleTest {
    private val eventBus = InMemoryEventBus()
    private val battleRepository = InMemoryBattleRepository()
    private val onRequestInitialiseBattle =
        OnRequestInitialiseBattle(InitialiseBattle(FakeBattleLoader(playerOrder = listOf("player-1", "player-2")), battleRepository, eventBus), eventBus)

    @Test
    fun `should initialise the battle when the request is dispatched`() {
        // Given
        eventBus.publish(BattleEvent.RequestInitialiseBattle)
        // When
        eventBus.dispatch()
        // Then
        battleRepository.search()?.toDto() shouldBe Battle.Dto(currentPlayerTurn = "player-1", currentRound = 1, isFinished = false)
    }
}
