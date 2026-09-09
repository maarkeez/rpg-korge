package battleunit.usecases.queries

import battleunit.adapters.storage.InMemoryBattleUnitRepository
import battleunit.domain.BattleUnitMother
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SearchBattleUnitByIdTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val searchBattleUnitById = SearchBattleUnitById(battleUnitRepository)

    @Test
    fun `should return battle unit when it exists`() {
        // Given
        val battleUnit =
            _root_ide_package_.battleunit.domain.BattleUnitMother
                .battleUnit()
        battleUnitRepository.create(battleUnit)
        // When
        val result = searchBattleUnitById(battleUnit.toDto().id)
        // Then
        assertThat(result).isEqualTo(battleUnit.toDto())
    }

    @Test
    fun `should return null when the battle unit does not exist`() {
        // Given
        // When
        val result = searchBattleUnitById("unknown-battle-unit")
        // Then
        assertThat(result).isNull()
    }
}
