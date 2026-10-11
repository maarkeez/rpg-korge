package com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.commands

import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.adapters.storage.InMemoryBattlefieldHudRepository
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudError
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudEvent
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.displayAbilityCastPreview
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.displayAbilityCastRange
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.displayMovementRange
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.idle
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.tile
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.services.MovementService
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.assertThat
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class RefreshMovementRangeTest {
    private val searchPosition: SearchPosition = mock()
    private val movementService: MovementService = mock()
    private val battlefieldHudRepository = InMemoryBattlefieldHudRepository()
    private val eventBus = FakeEventBus()
    private val refreshMovementRange =
        RefreshMovementRange(
            searchPosition = searchPosition,
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
            movementService = movementService,
        )

    @Test
    fun `should display the current tile and movement range of the selected battle unit when the hud is displaying its movement range`() {
        // Given
        val newTiles = setOf(tile(3, 5), tile(4, 4))
        battlefieldHudRepository.create(displayMovementRange(tile = tile(0, 0), battleUnitId = "battle-unit-1"))
        whenever(searchPosition("battle-unit-1")).thenReturn(Battlefield.Dto.PositionDto(row = 3, column = 4))
        whenever(movementService.tilesWhereCanMove("battle-unit-1")).thenReturn(newTiles)
        // When
        refreshMovementRange()
        // Then
        assertThat(battlefieldHudRepository.search()).isEqualTo(
            displayMovementRange(tile = tile(3, 4), battleUnitId = "battle-unit-1", tilesWhereCanBeMoved = newTiles),
        )
        assertThat(eventBus).hasPublishedEvents(
            BattlefieldHudEvent.SelectedBattleUnit(tile = tile(3, 4), battleUnitId = "battle-unit-1", tilesWhereCanBeMoved = newTiles),
        )
    }

    @Test
    fun `should go idle when the selected battle unit is no longer on the battlefield`() {
        // Given
        battlefieldHudRepository.create(displayMovementRange(battleUnitId = "battle-unit-1"))
        whenever(searchPosition("battle-unit-1")).thenReturn(null)
        // When
        refreshMovementRange()
        // Then
        assertThat(battlefieldHudRepository.search()).isInstanceOf(BattlefieldHud.Idle::class.java)
        assertThat(eventBus).hasPublishedEvents(BattlefieldHudEvent.Idle)
    }

    @Test
    fun `should not refresh when the hud is idle`() {
        // Given
        val hud = idle()
        battlefieldHudRepository.create(hud)
        // When
        refreshMovementRange()
        // Then
        assertThat(battlefieldHudRepository.search()).isEqualTo(hud)
        assertThat(eventBus.publishedEvents).isEmpty()
    }

    @Test
    fun `should not refresh when the hud is displaying an ability cast range`() {
        // Given
        val hud = displayAbilityCastRange()
        battlefieldHudRepository.create(hud)
        // When
        refreshMovementRange()
        // Then
        assertThat(battlefieldHudRepository.search()).isEqualTo(hud)
        assertThat(eventBus.publishedEvents).isEmpty()
    }

    @Test
    fun `should not refresh when the hud is previewing an ability cast`() {
        // Given
        val hud = displayAbilityCastPreview()
        battlefieldHudRepository.create(hud)
        // When
        refreshMovementRange()
        // Then
        assertThat(battlefieldHudRepository.search()).isEqualTo(hud)
        assertThat(eventBus.publishedEvents).isEmpty()
    }

    @Test
    fun `should throw a battlefield hud not found error when no battlefield hud exists`() {
        // Given
        // When
        // Then
        assertThatThrownBy { refreshMovementRange() }
            .isInstanceOf(BattlefieldHudError.BattlefieldHudNotFound::class.java)
    }
}
