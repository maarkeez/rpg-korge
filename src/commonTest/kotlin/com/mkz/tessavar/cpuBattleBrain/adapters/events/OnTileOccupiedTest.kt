package com.mkz.tessavar.cpuBattleBrain.adapters.events

import com.mkz.tessavar.battlefield.domain.BattlefieldEvent
import com.mkz.tessavar.cpuBattleBrain.domain.CpuBattleBrain
import com.mkz.tessavar.cpuBattleBrain.usecases.commands.ResolveCpuBattleUnitWalk
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import io.kotest.matchers.booleans.shouldBeFalse
import kotlin.test.Test

class OnTileOccupiedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val listener = OnTileOccupied(ResolveCpuBattleUnitWalk(queries.cpuBattleBrainRepository, eventBus), eventBus)

    @Test
    fun `should resolve the cpu battle unit walk when a battlefield tile is occupied is dispatched`() {
        // Given
        queries.cpuBattleBrainRepository.save(
            CpuBattleBrain
                .startPlayerTurn(playerId = "cpu", battleUnitIds = listOf("unit-1"))
                .requestWalk(battleUnitId = "unit-1", row = 2, column = 3)
                .pullEvents()
                .second,
        )
        eventBus.publish(BattlefieldEvent.TileOccupied(row = 2, column = 3, occupantId = "unit-1"))
        // When
        eventBus.dispatch()
        // Then
        queries.cpuBattleBrainRepository
            .search()!!
            .toDto()
            .isAwaitingDecision
            .shouldBeFalse()
    }
}
