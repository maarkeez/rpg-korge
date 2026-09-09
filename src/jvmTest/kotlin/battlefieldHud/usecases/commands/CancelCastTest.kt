package battlefieldHud.usecases.commands

import battlefieldHud.domain.BattlefieldHudMother
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import screen.battlefieldHud.adapters.storage.InMemoryBattlefieldHudRepository
import screen.battlefieldHud.domain.BattlefieldHud.Idle
import screen.battlefieldHud.domain.BattlefieldHudError
import screen.battlefieldHud.domain.BattlefieldHudEvent
import shared.domain.FakeEventBus
import shared.domain.assertThat

class CancelCastTest {
    private val battlefieldHudRepository = InMemoryBattlefieldHudRepository()
    private val eventBus = _root_ide_package_.shared.domain.FakeEventBus()
    private val cancelCast =
        _root_ide_package_.screen.battlefieldHud.usecases.commands.CancelCast(
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should cancel the cast and go idle when the hud is previewing the ability cast`() {
        // Given
        battlefieldHudRepository.create(
            _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                .displayAbilityCastPreview(),
        )
        // When
        cancelCast()
        // Then
        assertThat(battlefieldHudRepository.search()).isInstanceOf(Idle::class.java)
        _root_ide_package_.shared.domain
            .assertThat(eventBus)
            .hasPublishedEvents(BattlefieldHudEvent.Idle)
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
        assertThatThrownBy { cancelCast() }
            .isInstanceOf(BattlefieldHudError.InvalidBattlefieldHudState::class.java)
    }

    @Test
    fun `should throw an invalid hud state error when the hud is displaying the movement range`() {
        // Given
        battlefieldHudRepository.create(
            _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
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
            _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
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
