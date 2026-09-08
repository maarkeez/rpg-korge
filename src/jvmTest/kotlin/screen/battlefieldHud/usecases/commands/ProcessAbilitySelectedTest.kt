package screen.battlefieldHud.usecases.commands

import battleunit.adapters.presentation.*
import battleunit.usecases.queries.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import screen.battlefieldHud.adapters.storage.*
import screen.battlefieldHud.domain.*
import screen.battlefieldHud.domain.BattlefieldHud.*
import shared.domain.*
import shared.domain.assertThat

class ProcessAbilitySelectedTest {
    private val battleUnitApi: BattleUnitApi = mock()
    private val canCastAbility: CanCastAbility = mock()
    private val whereCanCast: WhereCanCast = mock()
    private val battlefieldHudRepository = InMemoryBattlefieldHudRepository()
    private val eventBus = FakeEventBus()
    private val processAbilitySelected = ProcessAbilitySelected(
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
        val tilesWhereCanCast = setOf(BattlefieldHudMother.tile(1, 1), BattlefieldHudMother.tile(2, 2))
        battlefieldHudRepository.create(
            BattlefieldHudMother.displayMovementRange(
                tile = BattlefieldHudMother.tile(0, 0),
                battleUnitId = "battle-unit-1",
            )
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
        assertThat(eventBus).hasPublishedEvents(
            BattlefieldHudEvent.SelectedBattleUnitAbility(
                casterTile = BattlefieldHudMother.tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                tilesWhereCanCast = tilesWhereCanCast,
            )
        )
    }

    @Test
    fun `should not select the ability when the battle unit cannot cast`() {
        // Given
        val hud = BattlefieldHudMother.displayMovementRange(battleUnitId = "battle-unit-1")
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
        val tilesWhereCanBeMoved = setOf(BattlefieldHudMother.tile(0, 1))
        battlefieldHudRepository.create(
            BattlefieldHudMother.displayAbilityCastRange(
                casterTile = BattlefieldHudMother.tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                tilesWhereCanBeMoved = tilesWhereCanBeMoved,
            )
        )
        // When
        processAbilitySelected("ability-1")
        // Then
        val storedHud = battlefieldHudRepository.search() as DisplayMovementRange
        assertThat(storedHud.tile).isEqualTo(BattlefieldHudMother.tile(0, 0))
        assertThat(storedHud.tilesWhereCanBeMoved).isEqualTo(tilesWhereCanBeMoved)
        assertThat(eventBus).hasPublishedEvents(
            BattlefieldHudEvent.AbilityDeselected(abilityId = "ability-1"),
            BattlefieldHudEvent.SelectedBattleUnit(
                tile = BattlefieldHudMother.tile(0, 0),
                battleUnitId = "battle-unit-1",
                tilesWhereCanBeMoved = tilesWhereCanBeMoved,
            ),
        )
    }

    @Test
    fun `should switch to the new ability when a different ability is selected while displaying the ability cast range`() {
        // Given
        val newTilesWhereCanCast = setOf(BattlefieldHudMother.tile(2, 2))
        battlefieldHudRepository.create(
            BattlefieldHudMother.displayAbilityCastRange(
                casterTile = BattlefieldHudMother.tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
            )
        )
        whenever(whereCanCast("battle-unit-1", "ability-2"))
            .thenReturn(newTilesWhereCanCast.map { WhereCanCast.PositionDto(row = it.row, column = it.column) })
        // When
        processAbilitySelected("ability-2")
        // Then
        val storedHud = battlefieldHudRepository.search() as DisplayAbilityCastRange
        assertThat(storedHud.abilityId).isEqualTo("ability-2")
        assertThat(storedHud.tilesWhereCanCast).isEqualTo(newTilesWhereCanCast)
        assertThat(eventBus).hasPublishedEvents(
            BattlefieldHudEvent.SelectedBattleUnitAbility(
                casterTile = BattlefieldHudMother.tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-2",
                tilesWhereCanCast = newTilesWhereCanCast,
            )
        )
    }

    @Test
    fun `should throw an invalid hud state error when the hud is idle`() {
        // Given
        battlefieldHudRepository.create(BattlefieldHudMother.idle())
        // When
        // Then
        assertThatThrownBy { processAbilitySelected("ability-1") }
            .isInstanceOf(BattlefieldHudError.InvalidBattlefieldHudState::class.java)
    }

    @Test
    fun `should throw an invalid hud state error when the hud is previewing the ability cast`() {
        // Given
        battlefieldHudRepository.create(BattlefieldHudMother.displayAbilityCastPreview())
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
