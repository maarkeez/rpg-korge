package battlefieldHud.usecases.commands

import battlefield.adapters.presentation.BattlefieldApi
import battlefield.domain.Battlefield
import battlefield.usecases.queries.SearchPosition
import battlefieldHud.domain.BattlefieldHudMother
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import screen.battlefieldHud.adapters.storage.InMemoryBattlefieldHudRepository
import screen.battlefieldHud.domain.BattlefieldHud.DisplayMovementRange
import screen.battlefieldHud.domain.BattlefieldHudEvent
import screen.battlefieldHud.usecases.services.MovementService
import shared.domain.FakeEventBus
import shared.domain.assertThat

class UpdateMovementRangeTest {
    private val battlefieldApi: BattlefieldApi = mock()
    private val searchPosition: SearchPosition = mock()
    private val movementService: MovementService = mock()
    private val battlefieldHudRepository = InMemoryBattlefieldHudRepository()
    private val eventBus = _root_ide_package_.shared.domain.FakeEventBus()
    private val updateMovementRange =
        _root_ide_package_.screen.battlefieldHud.usecases.commands.UpdateMovementRange(
            battlefieldApi = battlefieldApi,
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
            movementService = movementService,
        )

    @BeforeEach
    fun setUp() {
        whenever(battlefieldApi.searchPosition).thenReturn(searchPosition)
    }

    @Test
    fun `should update the movement range when the hud is displaying the movement range of the same battle unit`() {
        // Given
        val newTiles =
            setOf(
                _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                    .tile(3, 5),
                _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                    .tile(4, 4),
            )
        battlefieldHudRepository.create(
            _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother.displayMovementRange(
                tile =
                    _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
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
            _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                .tile(3, 4),
        )
        assertThat(storedHud.tilesWhereCanBeMoved).isEqualTo(newTiles)
        _root_ide_package_.shared.domain.assertThat(eventBus).hasPublishedEvents(
            BattlefieldHudEvent.SelectedBattleUnit(
                tile =
                    _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                        .tile(3, 4),
                battleUnitId = "battle-unit-1",
                tilesWhereCanBeMoved = newTiles,
            ),
        )
    }

    @Test
    fun `should not update the movement range when the battle unit id does not match`() {
        // Given
        val hud =
            _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                .displayMovementRange(battleUnitId = "battle-unit-1")
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
            _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                .idle()
        battlefieldHudRepository.create(hud)
        // When
        updateMovementRange("battle-unit-1")
        // Then
        assertThat(battlefieldHudRepository.search()).isEqualTo(hud)
        assertThat(eventBus.publishedEvents).isEmpty()
    }
}
