package battlefieldHud.usecases.commands

import battleUnit.usecases.queries.CanCastAbility
import battleUnit.usecases.queries.WhereCanCast
import battlefieldHud.domain.BattlefieldHudMother.displayAbilityCastRange
import battlefieldHud.domain.BattlefieldHudMother.displayMovementRange
import battlefieldHud.domain.BattlefieldHudMother.tile
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import screen.battlefieldHud.adapters.storage.InMemoryBattlefieldHudRepository
import screen.battlefieldHud.domain.BattlefieldHud.DisplayAbilityCastRange
import screen.battlefieldHud.domain.BattlefieldHud.DisplayMovementRange
import screen.battlefieldHud.domain.BattlefieldHudError
import screen.battlefieldHud.domain.BattlefieldHudEvent
import screen.battlefieldHud.usecases.commands.ProcessAbilitySelected
import shared.domain.FakeEventBus
import shared.domain.assertThat

class ProcessAbilitySelectedTest {
    private val canCastAbility: CanCastAbility = mock()
    private val whereCanCast: WhereCanCast = mock()
    private val battlefieldHudRepository = InMemoryBattlefieldHudRepository()
    private val eventBus = FakeEventBus()
    private val processAbilitySelected =
        ProcessAbilitySelected(
            canCastAbility = canCastAbility,
            whereCanCast = whereCanCast,
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should select the ability when the hud is displaying the movement range and the battle unit can cast`() {
        // Given
        val tilesWhereCanCast =
            setOf(
                tile(1, 1),
                tile(2, 2),
            )
        battlefieldHudRepository.create(
            displayMovementRange(
                tile =
                    tile(0, 0),
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
        assertThat(eventBus).hasPublishedEvents(
            BattlefieldHudEvent.SelectedBattleUnitAbility(
                casterTile =
                    tile(0, 0),
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
            displayMovementRange(battleUnitId = "battle-unit-1")
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
                tile(0, 1),
            )
        battlefieldHudRepository.create(
            displayAbilityCastRange(
                casterTile =
                    tile(0, 0),
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
            tile(0, 0),
        )
        assertThat(storedHud.tilesWhereCanBeMoved).isEqualTo(tilesWhereCanBeMoved)
        assertThat(eventBus).hasPublishedEvents(
            BattlefieldHudEvent.AbilityDeselected(abilityId = "ability-1"),
            BattlefieldHudEvent.SelectedBattleUnit(
                tile =
                    tile(0, 0),
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
                tile(2, 2),
            )
        battlefieldHudRepository.create(
            displayAbilityCastRange(
                casterTile =
                    tile(0, 0),
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
        assertThat(eventBus).hasPublishedEvents(
            BattlefieldHudEvent.SelectedBattleUnitAbility(
                casterTile =
                    tile(0, 0),
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
            battlefieldHud.domain.BattlefieldHudMother
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
            battlefieldHud.domain.BattlefieldHudMother
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
