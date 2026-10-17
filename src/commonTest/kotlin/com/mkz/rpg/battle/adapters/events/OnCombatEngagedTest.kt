package com.mkz.rpg.battle.adapters.events

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.Battle
import com.mkz.rpg.battle.usecases.commands.InitialiseBattle
import com.mkz.rpg.battle.usecases.services.FakeBattleLoader
import com.mkz.rpg.cpuBrain.domain.CpuBrainEvent
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnCombatEngagedTest {
    private val eventBus = InMemoryEventBus()
    private val battleRepository = InMemoryBattleRepository()
    private val queries = InMemoryQueries()
    private val onCombatEngaged =
        OnCombatEngaged(
            InitialiseBattle(FakeBattleLoader(playerOrder = listOf("player-1", "player-2")), queries.searchBattleUnitsByPlayerId, battleRepository, eventBus),
            eventBus,
        )

    @Test
    fun `should start the battle when the cpu engages`() {
        // Given
        eventBus.publish(CpuBrainEvent.CombatEngaged(playerId = "player-2", battleUnitIds = listOf("rat-1")))
        // When
        eventBus.dispatch()
        // Then
        battleRepository.search()?.toDto() shouldBe Battle.Dto(currentPlayerTurn = "player-1", currentRound = 1, isFinished = false, battleUnitIds = emptySet())
    }
}
