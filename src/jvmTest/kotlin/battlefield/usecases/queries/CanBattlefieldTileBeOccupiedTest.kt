package battlefield.usecases.queries

import battlefield.adapters.storage.*
import battlefield.domain.*
import org.junit.*

class CanBattlefieldTileBeOccupiedTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val canBattlefieldTileBeOccupied = CanBattlefieldTileBeOccupied(battlefieldRepository)

    @Test
    fun `should return true when the tile is vacant and within boundaries`() {
        // Given
        battlefieldRepository.create(BattlefieldMother.battlefield())
        // When
        val result = canBattlefieldTileBeOccupied(1, 1)
        // Then
        org.assertj.core.api.Assertions.assertThat(result).isTrue()
    }

    @Test
    fun `should return false when the tile is occupied`() {
        // Given
        val battlefield = BattlefieldMother.battlefield().occupy(1, 1, "battle-unit-1").pullEvents().second
        battlefieldRepository.create(battlefield)
        // When
        val result = canBattlefieldTileBeOccupied(1, 1)
        // Then
        org.assertj.core.api.Assertions.assertThat(result).isFalse()
    }

    @Test
    fun `should return false when the tile is out of boundaries`() {
        // Given
        battlefieldRepository.create(BattlefieldMother.battlefield())
        // When
        val result = canBattlefieldTileBeOccupied(5, 5)
        // Then
        org.assertj.core.api.Assertions.assertThat(result).isFalse()
    }
}
