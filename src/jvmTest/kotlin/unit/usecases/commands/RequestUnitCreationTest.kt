package unit.usecases.commands

import org.assertj.core.api.Assertions.assertThat
import org.junit.*
import shared.domain.*
import shared.domain.assertThat
import unit.adapters.storage.*
import unit.domain.*
import unit.domain.UnitMother.unit

class RequestUnitCreationTest {
    private val unitRepository = InMemoryUnitRepository()
    private val eventBus = FakeEventBus()
    private val requestUnitCreation = RequestUnitCreation(
        unitRepository = unitRepository,
        eventBus = eventBus
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
