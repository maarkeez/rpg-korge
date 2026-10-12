package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.battlefield.usecases.commands.UpdateBattlefieldOccupancy
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnBattleUnitMovedTest {
    private val eventBus = InMemoryEventBus()
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val onBattleUnitMoved =
        OnBattleUnitMoved(
            updateBattlefieldOccupancy = UpdateBattlefieldOccupancy(battlefieldRepository, eventBus),
            eventBus = eventBus,
        )

    @Test
    fun `should update battlefield occupancy when a battle unit is moved`() {
        // Given
        battlefieldRepository.create(battlefield(". . ."))
        eventBus.publish(
            BattleUnitEvent.BattleUnitMoved(
                battleUnitId = "battle-unit-1",
                fromRow = 0,
                fromColumn = 0,
                toRow = 0,
                toColumn = 2,
                path = listOf(PositionDto(row = 0, column = 1), PositionDto(row = 0, column = 2)),
            ),
        )
        // When
        eventBus.dispatch()
        // Then
        battlefieldRepository.search()?.occupant(row = 0, column = 2) shouldBe "battle-unit-1"
    }
}
