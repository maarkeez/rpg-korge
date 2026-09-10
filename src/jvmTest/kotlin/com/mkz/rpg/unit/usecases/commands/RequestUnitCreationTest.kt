package com.mkz.rpg.unit.usecases.commands

import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.assertThat
import com.mkz.rpg.unit.adapters.storage.InMemoryUnitRepository
import com.mkz.rpg.unit.domain.UnitEvent
import com.mkz.rpg.unit.domain.UnitMother.unit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RequestUnitCreationTest {
    private val unitRepository = InMemoryUnitRepository()
    private val eventBus = FakeEventBus()
    private val requestUnitCreation =
        RequestUnitCreation(
            unitRepository = unitRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should create unit`() {
        // Given
        val unit = unit().toDto()
        // When
        requestUnitCreation(unitDto = unit)
        // Then
        val storedUnit = unitRepository.searchById(unit.id)?.toDto()
        assertThat(storedUnit).isEqualTo(unit)
        assertThat(eventBus).hasPublishedEvents(UnitEvent.UnitCreated(unit.id))
    }
}
