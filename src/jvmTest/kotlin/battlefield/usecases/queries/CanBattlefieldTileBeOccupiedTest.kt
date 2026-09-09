package battlefield.usecases.queries

import battlefield.adapters.storage.InMemoryBattlefieldRepository
import battlefield.domain.BattlefieldMother
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CanBattlefieldTileBeOccupiedTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val canBattlefieldTileBeOccupied = CanBattlefieldTileBeOccupied(battlefieldRepository)

    @Test
    fun `should return true when the tile is vacant and within boundaries`() {
        // Given
        battlefieldRepository.create(
            _root_ide_package_.battlefield.domain.BattlefieldMother
                .battlefield(),
        )
        // When
        val result = canBattlefieldTileBeOccupied(1, 1)
        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun `should return false when the tile is occupied`() {
        // Given
        val battlefield =
            _root_ide_package_.battlefield.domain.BattlefieldMother
                .battlefield()
                .occupy(1, 1, "battle-unit-1")
                .pullEvents()
                .second
        battlefieldRepository.create(battlefield)
        // When
        val result = canBattlefieldTileBeOccupied(1, 1)
        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun `should return false when the tile is out of boundaries`() {
        // Given
        battlefieldRepository.create(
            _root_ide_package_.battlefield.domain.BattlefieldMother
                .battlefield(),
        )
        // When
        val result = canBattlefieldTileBeOccupied(5, 5)
        // Then
        assertThat(result).isFalse()
    }
}
