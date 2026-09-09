package battlefield.usecases.commands

import battlefield.adapters.storage.InMemoryBattlefieldRepository
import battlefield.domain.Battlefield
import battlefield.domain.BattlefieldError
import battlefield.domain.BattlefieldEvent
import battlefield.domain.BattlefieldMother
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import shared.domain.FakeEventBus
import shared.domain.assertThat

class UpdateBattlefieldOccupancyTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val eventBus = _root_ide_package_.shared.domain.FakeEventBus()
    private val updateBattlefieldOccupancy =
        UpdateBattlefieldOccupancy(
            battlefieldRepository = battlefieldRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should occupy tile when the tile is vacant`() {
        // Given
        battlefieldRepository.create(
            _root_ide_package_.battlefield.domain.BattlefieldMother
                .battlefield(),
        )
        val battleUnitId = "battle-unit-1"
        // When
        updateBattlefieldOccupancy(row = 1, column = 1, battleUnitId = battleUnitId)
        // Then
        val storedBattlefield = battlefieldRepository.search()?.toDto()
        assertThat(
            storedBattlefield?.tiles?.get(Battlefield.Dto.PositionDto(1, 1))?.battleUnitId,
        ).isEqualTo(battleUnitId)
        _root_ide_package_.shared.domain.assertThat(eventBus).hasPublishedEvents(
            BattlefieldEvent.BattlefieldTileOccupied(1, 1, battleUnitId),
        )
    }

    @Test
    fun `should throw tile error when the tile is not vacant`() {
        // Given
        val battlefield =
            _root_ide_package_.battlefield.domain.BattlefieldMother
                .battlefield()
                .occupy(1, 1, "other-battle-unit")
                .pullEvents()
                .second
        battlefieldRepository.create(battlefield)
        // When
        val error =
            org.assertj.core.api.Assertions.catchThrowable {
                updateBattlefieldOccupancy(row = 1, column = 1, battleUnitId = "battle-unit-1")
            }
        // Then
        assertThat(error)
            .isInstanceOf(BattlefieldError.TileIsNotVacant::class.java)
    }

    @Test
    fun `should not occupy tile when no battlefield exists`() {
        // Given
        // When
        updateBattlefieldOccupancy(row = 1, column = 1, battleUnitId = "battle-unit-1")
        // Then
        assertThat(battlefieldRepository.search()).isNull()
        assertThat(eventBus.publishedEvents).isEmpty()
    }
}
