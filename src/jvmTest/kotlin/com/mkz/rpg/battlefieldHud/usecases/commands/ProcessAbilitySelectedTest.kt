package com.mkz.rpg.battlefieldHud.usecases.commands

import com.mkz.rpg.battleUnit.usecases.queries.CanCastAbility
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast
import com.mkz.rpg.battlefieldHud.domain.BattlefieldHudMother.displayAbilityCastRange
import com.mkz.rpg.battlefieldHud.domain.BattlefieldHudMother.displayMovementRange
import com.mkz.rpg.battlefieldHud.domain.BattlefieldHudMother.tile
import com.mkz.rpg.screen.battlefieldHud.adapters.storage.InMemoryBattlefieldHudRepository
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.DisplayAbilityCastRange
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.DisplayMovementRange
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudError
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudEvent
import com.mkz.rpg.screen.battlefieldHud.usecases.commands.ProcessAbilitySelected
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.assertThat
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

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
        val castGroupsWhereCanCast =
            listOf(
                com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.Dto
                    .CastGroupDto(tiles = listOf(tile(1, 1))),
                com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.Dto
                    .CastGroupDto(tiles = listOf(tile(2, 2))),
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
            .thenReturn(
                castGroupsWhereCanCast.map { castGroup ->
                    WhereCanCast.CastGroup(
                        positions =
                            castGroup.tiles
                                .map { tile -> WhereCanCast.PositionDto(row = tile.row, column = tile.column) },
                    )
                },
            )
        // When
        processAbilitySelected("ability-1")
        // Then
        val storedHud = battlefieldHudRepository.search() as DisplayAbilityCastRange
        assertThat(storedHud.abilityId).isEqualTo("ability-1")
        assertThat(storedHud.castGroupsWhereCanCast).isEqualTo(castGroupsWhereCanCast)
        assertThat(eventBus).hasPublishedEvents(
            BattlefieldHudEvent.SelectedBattleUnitAbility(
                casterTile =
                    tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                castGroupsWhereCanCast = castGroupsWhereCanCast,
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
        val newCastGroupsWhereCanCast =
            listOf(
                com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.Dto
                    .CastGroupDto(tiles = listOf(tile(2, 2))),
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
            .thenReturn(
                newCastGroupsWhereCanCast.map { castGroup ->
                    WhereCanCast.CastGroup(
                        positions =
                            castGroup.tiles
                                .map { tile -> WhereCanCast.PositionDto(row = tile.row, column = tile.column) },
                    )
                },
            )
        // When
        processAbilitySelected("ability-2")
        // Then
        val storedHud = battlefieldHudRepository.search() as DisplayAbilityCastRange
        assertThat(storedHud.abilityId).isEqualTo("ability-2")
        assertThat(storedHud.castGroupsWhereCanCast).isEqualTo(newCastGroupsWhereCanCast)
        assertThat(eventBus).hasPublishedEvents(
            BattlefieldHudEvent.SelectedBattleUnitAbility(
                casterTile =
                    tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-2",
                castGroupsWhereCanCast = newCastGroupsWhereCanCast,
            ),
        )
    }

    @Test
    fun `should throw an invalid hud state error when the hud is idle`() {
        // Given
        battlefieldHudRepository.create(
            com.mkz.rpg.battlefieldHud.domain.BattlefieldHudMother
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
            com.mkz.rpg.battlefieldHud.domain.BattlefieldHudMother
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
