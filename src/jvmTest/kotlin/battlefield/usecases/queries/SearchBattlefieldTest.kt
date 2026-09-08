package battlefield.usecases.queries

import battlefield.adapters.storage.*
import battlefield.domain.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SearchBattlefieldTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val searchBattlefield = SearchBattlefield(battlefieldRepository)

    @Test
    fun `should return battlefield when one exists`() {
        // Given
        val battlefield = BattlefieldMother.battlefield()
        battlefieldRepository.create(battlefield)
        // When
        val result = searchBattlefield()
        // Then
        assertThat(result).isEqualTo(battlefield.toDto())
    }

    @Test
    fun `should return null when no battlefield exists`() {
        // Given
        // When
        val result = searchBattlefield()
        // Then
        assertThat(result).isNull()
    }
}
