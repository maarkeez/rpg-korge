package battlefield.usecases.queries

import battlefield.adapters.storage.InMemoryBattlefieldRepository
import battlefield.domain.BattlefieldMother
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SearchOccupantTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val searchOccupant = SearchOccupant(battlefieldRepository)

    @Test
    fun `should return occupant when the tile is occupied`() {
        // Given
        val battleUnitId = "battle-unit-1"
        val battlefield =
            _root_ide_package_.battlefield.domain.BattlefieldMother
                .battlefield()
                .occupy(1, 1, battleUnitId)
                .pullEvents()
                .second
        battlefieldRepository.create(battlefield)
        // When
        val result = searchOccupant(1, 1)
        // Then
        assertThat(result).isEqualTo(battleUnitId)
    }

    @Test
    fun `should return null when the tile is vacant`() {
        // Given
        battlefieldRepository.create(
            _root_ide_package_.battlefield.domain.BattlefieldMother
                .battlefield(),
        )
        // When
        val result = searchOccupant(1, 1)
        // Then
        assertThat(result).isNull()
    }
}
