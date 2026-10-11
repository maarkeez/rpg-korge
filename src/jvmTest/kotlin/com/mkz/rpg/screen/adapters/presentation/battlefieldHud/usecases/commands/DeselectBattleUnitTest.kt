package com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.commands

import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.adapters.storage.InMemoryBattlefieldHudRepository
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudError
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudEvent
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.displayAbilityCastPreview
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.displayAbilityCastRange
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.displayMovementRange
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.idle
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.assertThat
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class DeselectBattleUnitTest {
    private val battlefieldHudRepository = InMemoryBattlefieldHudRepository()
    private val eventBus = FakeEventBus()
    private val deselectBattleUnit =
        DeselectBattleUnit(
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should go idle when the battle unit is deselected and the hud is idle`() {
        // Given
        battlefieldHudRepository.create(idle())
        // When
        deselectBattleUnit()
        // Then
        assertThat(battlefieldHudRepository.search()).isEqualTo(idle())
        assertThat(eventBus).hasPublishedEvents(BattlefieldHudEvent.Idle)
    }

    @Test
    fun `should go idle when the battle unit is deselected and the hud is displaying the movement range`() {
        // Given
        battlefieldHudRepository.create(displayMovementRange())
        // When
        deselectBattleUnit()
        // Then
        assertThat(battlefieldHudRepository.search()).isEqualTo(idle())
        assertThat(eventBus).hasPublishedEvents(BattlefieldHudEvent.Idle)
    }

    @Test
    fun `should go idle when the battle unit is deselected and the hud is displaying an ability cast range`() {
        // Given
        battlefieldHudRepository.create(displayAbilityCastRange())
        // When
        deselectBattleUnit()
        // Then
        assertThat(battlefieldHudRepository.search()).isEqualTo(idle())
        assertThat(eventBus).hasPublishedEvents(BattlefieldHudEvent.Idle)
    }

    @Test
    fun `should go idle when the battle unit is deselected and the hud is previewing an ability cast`() {
        // Given
        battlefieldHudRepository.create(displayAbilityCastPreview())
        // When
        deselectBattleUnit()
        // Then
        assertThat(battlefieldHudRepository.search()).isEqualTo(idle())
        assertThat(eventBus).hasPublishedEvents(BattlefieldHudEvent.Idle)
    }

    @Test
    fun `should throw a battlefield hud not found error when no battlefield hud exists`() {
        // Given
        // When
        // Then
        assertThatThrownBy { deselectBattleUnit() }
            .isInstanceOf(BattlefieldHudError.BattlefieldHudNotFound::class.java)
    }
}
