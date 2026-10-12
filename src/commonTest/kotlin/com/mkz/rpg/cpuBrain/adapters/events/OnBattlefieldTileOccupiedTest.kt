package com.mkz.rpg.cpuBrain.adapters.events

import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.cpuBrain.domain.CpuBrain
import com.mkz.rpg.cpuBrain.usecases.commands.ResolveCpuBattleUnitMove
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.booleans.shouldBeFalse
import kotlin.test.Test

class OnBattlefieldTileOccupiedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val listener = OnBattlefieldTileOccupied(ResolveCpuBattleUnitMove(queries.cpuBrainRepository, eventBus), eventBus)

    @Test
    fun `should resolve the cpu battle unit move when a battlefield tile is occupied is dispatched`() {
        // Given
        queries.cpuBrainRepository.save(
            CpuBrain
                .startTurn(playerId = "cpu", battleUnitIds = listOf("unit-1"))
                .requestMove(battleUnitId = "unit-1", row = 2, column = 3)
                .pullEvents()
                .second,
        )
        eventBus.publish(BattlefieldEvent.BattlefieldTileOccupied(row = 2, column = 3, battlefieldUnitId = "unit-1"))
        // When
        eventBus.dispatch()
        // Then
        queries.cpuBrainRepository
            .search()!!
            .toDto()
            .isAwaitingAction
            .shouldBeFalse()
    }
}
