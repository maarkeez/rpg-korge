package battlefieldHud.usecases.commands

import battlefield.domain.Battlefield
import battlefield.usecases.queries.SearchPosition
import battlefieldHud.domain.BattlefieldHudMother
import battlefieldHud.domain.BattlefieldHudMother.displayMovementRange
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import screen.battlefieldHud.adapters.storage.InMemoryBattlefieldHudRepository
import screen.battlefieldHud.domain.BattlefieldHud.DisplayMovementRange
import screen.battlefieldHud.domain.BattlefieldHudEvent
import screen.battlefieldHud.usecases.commands.UpdateMovementRange
import screen.battlefieldHud.usecases.services.MovementService
import shared.domain.FakeEventBus
import shared.domain.assertThat

class UpdateMovementRangeTest {
    private val searchPosition: SearchPosition = mock()
    private val movementService: MovementService = mock()
    private val battlefieldHudRepository = InMemoryBattlefieldHudRepository()
    private val eventBus = FakeEventBus()
    private val updateMovementRange =
        UpdateMovementRange(
            searchPosition = searchPosition,
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
            movementService = movementService,
        )

    @Test
    fun `should update the movement range when the hud is displaying the movement range of the same battle unit`() {
        // Given
        val newTiles =
            setOf(
                BattlefieldHudMother
                    .tile(3, 5),
                BattlefieldHudMother
                    .tile(4, 4),
            )
        battlefieldHudRepository.create(
            displayMovementRange(
                tile =
                    BattlefieldHudMother
                        .tile(0, 0),
                battleUnitId = "battle-unit-1",
            ),
        )
        whenever(searchPosition("battle-unit-1")).thenReturn(Battlefield.Dto.PositionDto(row = 3, column = 4))
        whenever(movementService.tilesWhereCanMove("battle-unit-1")).thenReturn(newTiles)
        // When
        updateMovementRange("battle-unit-1")
        // Then
        val storedHud = battlefieldHudRepository.search() as DisplayMovementRange
        assertThat(storedHud.tile).isEqualTo(
            BattlefieldHudMother
                .tile(3, 4),
        )
        assertThat(storedHud.tilesWhereCanBeMoved).isEqualTo(newTiles)
        assertThat(eventBus).hasPublishedEvents(
            BattlefieldHudEvent.SelectedBattleUnit(
                tile =
                    BattlefieldHudMother
                        .tile(3, 4),
                battleUnitId = "battle-unit-1",
                tilesWhereCanBeMoved = newTiles,
            ),
        )
    }

    @Test
    fun `should not update the movement range when the battle unit id does not match`() {
        // Given
        val hud = displayMovementRange(battleUnitId = "battle-unit-1")
        battlefieldHudRepository.create(hud)
        // When
        updateMovementRange("battle-unit-2")
        // Then
        assertThat(battlefieldHudRepository.search()).isEqualTo(hud)
        assertThat(eventBus.publishedEvents).isEmpty()
    }

    @Test
    fun `should not update the movement range when the hud is idle`() {
        // Given
        val hud =
            BattlefieldHudMother
                .idle()
        battlefieldHudRepository.create(hud)
        // When
        updateMovementRange("battle-unit-1")
        // Then
        assertThat(battlefieldHudRepository.search()).isEqualTo(hud)
        assertThat(eventBus.publishedEvents).isEmpty()
    }
}
