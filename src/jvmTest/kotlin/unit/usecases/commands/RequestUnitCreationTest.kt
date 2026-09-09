package unit.usecases.commands

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import shared.domain.FakeEventBus
import shared.domain.assertThat
import unit.adapters.storage.InMemoryUnitRepository
import unit.domain.UnitEvent
import unit.domain.UnitMother.unit

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
