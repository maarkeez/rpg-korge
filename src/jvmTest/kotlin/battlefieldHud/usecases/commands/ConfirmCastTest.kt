package battlefieldHud.usecases.commands

import battleUnit.usecases.commands.CastAbility
import battlefieldHud.domain.BattlefieldHudMother.displayAbilityCastPreview
import battlefieldHud.domain.BattlefieldHudMother.tile
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import screen.battlefieldHud.adapters.storage.InMemoryBattlefieldHudRepository
import screen.battlefieldHud.domain.BattlefieldHud.Idle
import screen.battlefieldHud.domain.BattlefieldHudError
import screen.battlefieldHud.domain.BattlefieldHudEvent
import screen.battlefieldHud.usecases.commands.ConfirmCast
import shared.domain.FakeEventBus
import shared.domain.assertThat

class ConfirmCastTest {
    private val castAbility: CastAbility = mock()
    private val battlefieldHudRepository = InMemoryBattlefieldHudRepository()
    private val eventBus = FakeEventBus()
    private val confirmCast =
        ConfirmCast(
            castAbility = castAbility,
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should cast the ability and go idle when the hud is previewing the ability cast`() {
        // Given
        val castTile = tile(1, 1)
        battlefieldHudRepository.create(
            displayAbilityCastPreview(
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                castTile = castTile,
            ),
        )
        // When
        confirmCast()
        // Then
        verify(castAbility).invoke(
            battleUnitId = "battle-unit-1",
            abilityId = "ability-1",
            row = castTile.row,
            column = castTile.column,
        )
        assertThat(battlefieldHudRepository.search()).isInstanceOf(Idle::class.java)
        assertThat(eventBus)
            .hasPublishedEvents(BattlefieldHudEvent.Idle)
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
        assertThatThrownBy { confirmCast() }
            .isInstanceOf(BattlefieldHudError.InvalidBattlefieldHudState::class.java)
    }

    @Test
    fun `should throw an invalid hud state error when the hud is displaying the movement range`() {
        // Given
        battlefieldHudRepository.create(
            battlefieldHud.domain.BattlefieldHudMother
                .displayMovementRange(),
        )
        // When
        // Then
        assertThatThrownBy { confirmCast() }
            .isInstanceOf(BattlefieldHudError.InvalidBattlefieldHudState::class.java)
    }

    @Test
    fun `should throw an invalid hud state error when the hud is displaying the ability cast range`() {
        // Given
        battlefieldHudRepository.create(
            battlefieldHud.domain.BattlefieldHudMother
                .displayAbilityCastRange(),
        )
        // When
        // Then
        assertThatThrownBy { confirmCast() }
            .isInstanceOf(BattlefieldHudError.InvalidBattlefieldHudState::class.java)
    }

    @Test
    fun `should throw a battlefield hud not found error when no battlefield hud exists`() {
        // Given
        // When
        // Then
        assertThatThrownBy { confirmCast() }
            .isInstanceOf(BattlefieldHudError.BattlefieldHudNotFound::class.java)
    }
}
