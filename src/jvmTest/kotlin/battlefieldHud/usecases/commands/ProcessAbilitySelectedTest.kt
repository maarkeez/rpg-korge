package battlefieldHud.usecases.commands

import battlefieldHud.domain.BattlefieldHudMother
import battleunit.adapters.presentation.BattleUnitApi
import battleunit.usecases.queries.CanCastAbility
import battleunit.usecases.queries.WhereCanCast
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import screen.battlefieldHud.adapters.storage.InMemoryBattlefieldHudRepository
import screen.battlefieldHud.domain.BattlefieldHud.DisplayAbilityCastRange
import screen.battlefieldHud.domain.BattlefieldHud.DisplayMovementRange
import screen.battlefieldHud.domain.BattlefieldHudError
import screen.battlefieldHud.domain.BattlefieldHudEvent
import shared.domain.FakeEventBus
import shared.domain.assertThat

class ProcessAbilitySelectedTest {
    private val battleUnitApi: BattleUnitApi = mock()
    private val canCastAbility: CanCastAbility = mock()
    private val whereCanCast: WhereCanCast = mock()
    private val battlefieldHudRepository = InMemoryBattlefieldHudRepository()
    private val eventBus = _root_ide_package_.shared.domain.FakeEventBus()
    private val processAbilitySelected =
        _root_ide_package_.screen.battlefieldHud.usecases.commands.ProcessAbilitySelected(
            battleUnitApi = battleUnitApi,
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
        )

    @BeforeEach
    fun setUp() {
        whenever(battleUnitApi.canCastAbility).thenReturn(canCastAbility)
        whenever(battleUnitApi.whereCanCast).thenReturn(whereCanCast)
    }

    @Test
    fun `should select the ability when the hud is displaying the movement range and the battle unit can cast`() {
        // Given
        val tilesWhereCanCast =
            setOf(
                _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                    .tile(1, 1),
                _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                    .tile(2, 2),
            )
        battlefieldHudRepository.create(
            _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother.displayMovementRange(
                tile =
                    _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                        .tile(0, 0),
                battleUnitId = "battle-unit-1",
            ),
        )
        whenever(canCastAbility("battle-unit-1", "ability-1")).thenReturn(true)
        whenever(whereCanCast("battle-unit-1", "ability-1"))
            .thenReturn(tilesWhereCanCast.map { WhereCanCast.PositionDto(row = it.row, column = it.column) })
        // When
        processAbilitySelected("ability-1")
        // Then
        val storedHud = battlefieldHudRepository.search() as DisplayAbilityCastRange
        assertThat(storedHud.abilityId).isEqualTo("ability-1")
        assertThat(storedHud.tilesWhereCanCast).isEqualTo(tilesWhereCanCast)
        _root_ide_package_.shared.domain.assertThat(eventBus).hasPublishedEvents(
            BattlefieldHudEvent.SelectedBattleUnitAbility(
                casterTile =
                    _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                        .tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                tilesWhereCanCast = tilesWhereCanCast,
            ),
        )
    }

    @Test
    fun `should not select the ability when the battle unit cannot cast`() {
        // Given
        val hud =
            _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                .displayMovementRange(battleUnitId = "battle-unit-1")
        battlefieldHudRepository.create(hud)
        whenever(canCastAbility("battle-unit-1", "ability-1")).thenReturn(false)
        // When
        processAbilitySelected("ability-1")
        // Then
        assertThat(battlefieldHudRepository.search()).isEqualTo(hud)
        assertThat(eventBus.publishedEvents).isEmpty()
    }

    @Test
    fun `should deselect the ability when the same ability is selected while displaying the ability cast range`() {
        // Given
        val tilesWhereCanBeMoved =
            setOf(
                _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                    .tile(0, 1),
            )
        battlefieldHudRepository.create(
            _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother.displayAbilityCastRange(
                casterTile =
                    _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                        .tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                tilesWhereCanBeMoved = tilesWhereCanBeMoved,
            ),
        )
        // When
        processAbilitySelected("ability-1")
        // Then
        val storedHud = battlefieldHudRepository.search() as DisplayMovementRange
        assertThat(storedHud.tile).isEqualTo(
            _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                .tile(0, 0),
        )
        assertThat(storedHud.tilesWhereCanBeMoved).isEqualTo(tilesWhereCanBeMoved)
        _root_ide_package_.shared.domain.assertThat(eventBus).hasPublishedEvents(
            BattlefieldHudEvent.AbilityDeselected(abilityId = "ability-1"),
            BattlefieldHudEvent.SelectedBattleUnit(
                tile =
                    _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                        .tile(0, 0),
                battleUnitId = "battle-unit-1",
                tilesWhereCanBeMoved = tilesWhereCanBeMoved,
            ),
        )
    }

    @Test
    fun `should switch to the new ability when a different ability is selected while displaying the ability cast range`() {
        // Given
        val newTilesWhereCanCast =
            setOf(
                _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                    .tile(2, 2),
            )
        battlefieldHudRepository.create(
            _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother.displayAbilityCastRange(
                casterTile =
                    _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                        .tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
            ),
        )
        whenever(whereCanCast("battle-unit-1", "ability-2"))
            .thenReturn(newTilesWhereCanCast.map { WhereCanCast.PositionDto(row = it.row, column = it.column) })
        // When
        processAbilitySelected("ability-2")
        // Then
        val storedHud = battlefieldHudRepository.search() as DisplayAbilityCastRange
        assertThat(storedHud.abilityId).isEqualTo("ability-2")
        assertThat(storedHud.tilesWhereCanCast).isEqualTo(newTilesWhereCanCast)
        _root_ide_package_.shared.domain.assertThat(eventBus).hasPublishedEvents(
            BattlefieldHudEvent.SelectedBattleUnitAbility(
                casterTile =
                    _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                        .tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-2",
                tilesWhereCanCast = newTilesWhereCanCast,
            ),
        )
    }

    @Test
    fun `should throw an invalid hud state error when the hud is idle`() {
        // Given
        battlefieldHudRepository.create(
            _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                .idle(),
        )
        // When
        // Then
        assertThatThrownBy { processAbilitySelected("ability-1") }
            .isInstanceOf(BattlefieldHudError.InvalidBattlefieldHudState::class.java)
    }

    @Test
    fun `should throw an invalid hud state error when the hud is previewing the ability cast`() {
        // Given
        battlefieldHudRepository.create(
            _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                .displayAbilityCastPreview(),
        )
        // When
        // Then
        assertThatThrownBy { processAbilitySelected("ability-1") }
            .isInstanceOf(BattlefieldHudError.InvalidBattlefieldHudState::class.java)
    }

    @Test
    fun `should throw a battlefield hud not found error when no battlefield hud exists`() {
        // Given
        // When
        // Then
        assertThatThrownBy { processAbilitySelected("ability-1") }
            .isInstanceOf(BattlefieldHudError.BattlefieldHudNotFound::class.java)
    }
}
