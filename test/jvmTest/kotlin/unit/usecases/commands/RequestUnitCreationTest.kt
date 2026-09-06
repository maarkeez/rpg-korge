package unit.usecases.commands

import org.assertj.core.api.Assertions.assertThat
import org.junit.*
import org.mockito.kotlin.*
import shared.domain.*
import unit.adapters.storage.*
import unit.domain.UnitMother.unit

class RequestUnitCreationTest {
    private val unitRepository = InMemoryUnitRepository()
    private val eventBus: EventBus = mock()
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
    }
}
