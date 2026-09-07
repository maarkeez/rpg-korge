package battlefield.usecases.queries

import battlefield.adapters.storage.*
import battlefield.domain.*
import org.junit.*

class SearchPositionTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val searchPosition = SearchPosition(battlefieldRepository)

    @Test
    fun `should return position when the battle unit is deployed`() {
        // Given
        val battleUnitId = "battle-unit-1"
        val battlefield = BattlefieldMother.battlefield().occupy(1, 1, battleUnitId).pullEvents().second
        battlefieldRepository.create(battlefield)
        // When
        val result = searchPosition(battleUnitId)
        // Then
        org.assertj.core.api.Assertions.assertThat(result).isEqualTo(Battlefield.Dto.PositionDto(1, 1))
    }

    @Test
    fun `should return null when the battle unit is not deployed`() {
        // Given
        battlefieldRepository.create(BattlefieldMother.battlefield())
        // When
        val result = searchPosition("unknown-battle-unit")
        // Then
        org.assertj.core.api.Assertions.assertThat(result).isNull()
    }
}
