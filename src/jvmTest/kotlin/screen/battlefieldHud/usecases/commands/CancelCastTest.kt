package screen.battlefieldHud.usecases.commands

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.*
import screen.battlefieldHud.adapters.storage.*
import screen.battlefieldHud.domain.*
import screen.battlefieldHud.domain.BattlefieldHud.*
import shared.domain.*
import shared.domain.assertThat

class CancelCastTest {
    private val battlefieldHudRepository = InMemoryBattlefieldHudRepository()
    private val eventBus = FakeEventBus()
    private val cancelCast = CancelCast(
        battlefieldHudRepository = battlefieldHudRepository,
        eventBus = eventBus,
    )

    @Test
    fun `should cancel the cast and go idle when the hud is previewing the ability cast`() {
        // Given
        battlefieldHudRepository.create(BattlefieldHudMother.displayAbilityCastPreview())
        // When
        cancelCast()
        // Then
        assertThat(battlefieldHudRepository.search()).isInstanceOf(Idle::class.java)
        assertThat(eventBus).hasPublishedEvents(BattlefieldHudEvent.Idle)
    }

    @Test
    fun `should throw an invalid hud state error when the hud is idle`() {
        // Given
        battlefieldHudRepository.create(BattlefieldHudMother.idle())
        // When
        // Then
        assertThatThrownBy { cancelCast() }
            .isInstanceOf(BattlefieldHudError.InvalidBattlefieldHudState::class.java)
    }

    @Test
    fun `should throw an invalid hud state error when the hud is displaying the movement range`() {
        // Given
        battlefieldHudRepository.create(BattlefieldHudMother.displayMovementRange())
        // When
        // Then
        assertThatThrownBy { cancelCast() }
            .isInstanceOf(BattlefieldHudError.InvalidBattlefieldHudState::class.java)
    }

    @Test
    fun `should throw an invalid hud state error when the hud is displaying the ability cast range`() {
        // Given
        battlefieldHudRepository.create(BattlefieldHudMother.displayAbilityCastRange())
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
