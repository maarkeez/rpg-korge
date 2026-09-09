package battlefield.usecases.commands

import battlefield.adapters.storage.InMemoryBattlefieldRepository
import battlefield.domain.Battlefield
import battlefield.domain.BattlefieldEvent
import battlefield.domain.BattlefieldMother
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import shared.domain.FakeEventBus
import shared.domain.assertThat

class RemoveOccupantTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val eventBus = _root_ide_package_.shared.domain.FakeEventBus()
    private val removeOccupant =
        RemoveOccupant(
            battlefieldRepository = battlefieldRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should remove occupant when the battle unit is deployed`() {
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
        removeOccupant(battleUnitId)
        // Then
        val storedBattlefield = battlefieldRepository.search()?.toDto()
        assertThat(
            storedBattlefield?.tiles?.get(Battlefield.Dto.PositionDto(1, 1))?.battleUnitId,
        ).isNull()
        _root_ide_package_.shared.domain.assertThat(eventBus).hasPublishedEvents(
            BattlefieldEvent.OccupantRemoved(battleUnitId, 1, 1),
        )
    }

    @Test
    fun `should not remove occupant when the battle unit is not deployed`() {
        // Given
        battlefieldRepository.create(
            _root_ide_package_.battlefield.domain.BattlefieldMother
                .battlefield(),
        )
        // When
        removeOccupant("unknown-battle-unit")
        // Then
        assertThat(eventBus.publishedEvents).isEmpty()
    }
}
