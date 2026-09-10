package com.mkz.rpg.battlefieldHud.usecases.commands

import com.mkz.rpg.screen.battlefieldHud.adapters.storage.InMemoryBattlefieldHudRepository
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.Idle
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudError
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudEvent
import com.mkz.rpg.screen.battlefieldHud.usecases.commands.CancelCast
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.assertThat
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class CancelCastTest {
    private val battlefieldHudRepository = InMemoryBattlefieldHudRepository()
    private val eventBus = FakeEventBus()
    private val cancelCast =
        CancelCast(
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should cancel the cast and go idle when the hud is previewing the ability cast`() {
        // Given
        battlefieldHudRepository.create(
            com.mkz.rpg.battlefieldHud.domain.BattlefieldHudMother
                .displayAbilityCastPreview(),
        )
        // When
        cancelCast()
        // Then
        assertThat(battlefieldHudRepository.search()).isInstanceOf(Idle::class.java)
        assertThat(eventBus)
            .hasPublishedEvents(BattlefieldHudEvent.Idle)
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
        assertThatThrownBy { cancelCast() }
            .isInstanceOf(BattlefieldHudError.InvalidBattlefieldHudState::class.java)
    }

    @Test
    fun `should throw an invalid hud state error when the hud is displaying the movement range`() {
        // Given
        battlefieldHudRepository.create(
            com.mkz.rpg.battlefieldHud.domain.BattlefieldHudMother
                .displayMovementRange(),
        )
        // When
        // Then
        assertThatThrownBy { cancelCast() }
            .isInstanceOf(BattlefieldHudError.InvalidBattlefieldHudState::class.java)
    }

    @Test
    fun `should throw an invalid hud state error when the hud is displaying the ability cast range`() {
        // Given
        battlefieldHudRepository.create(
            com.mkz.rpg.battlefieldHud.domain.BattlefieldHudMother
                .displayAbilityCastRange(),
        )
        // When
        // Then
        assertThatThrownBy { cancelCast() }
            .isInstanceOf(BattlefieldHudError.InvalidBattlefieldHudState::class.java)
    }

    @Test
    fun `should throw a battlefield hud not found error when no battlefield hud exists`() {
        // Given
        // When
        // Then
        assertThatThrownBy { cancelCast() }
            .isInstanceOf(BattlefieldHudError.BattlefieldHudNotFound::class.java)
    }
}
