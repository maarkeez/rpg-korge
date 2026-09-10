package com.mkz.rpg.battlefield.usecases.commands

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.assertThat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RemoveOccupantTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val eventBus = FakeEventBus()
    private val removeOccupant =
        RemoveOccupant(
            battlefieldRepository = battlefieldRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should remove occupant when the battle unit is deployed`() {
        // Given
        val battleUnitId = "battle-unit-1"
        val battlefield =
            battlefield()
                .occupy(1, 1, battleUnitId)
                .pullEvents()
                .second
        battlefieldRepository.create(battlefield)
        // When
        removeOccupant(battleUnitId)
        // Then
        val storedBattlefield = battlefieldRepository.search()?.toDto()
        assertThat(
            storedBattlefield?.tiles?.get(Battlefield.Dto.PositionDto(1, 1))?.battleUnitId,
        ).isNull()
        assertThat(eventBus).hasPublishedEvents(
            BattlefieldEvent.OccupantRemoved(battleUnitId, 1, 1),
        )
    }

    @Test
    fun `should not remove occupant when the battle unit is not deployed`() {
        // Given
        battlefieldRepository.create(battlefield())
        // When
        removeOccupant("unknown-battle-unit")
        // Then
        assertThat(eventBus.publishedEvents).isEmpty()
    }
}
