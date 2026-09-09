package battlefieldHud.usecases.commands

import battlefieldHud.domain.BattlefieldHudMother
import battleunit.adapters.presentation.BattleUnitApi
import battleunit.usecases.commands.CastAbility
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import screen.battlefieldHud.adapters.storage.InMemoryBattlefieldHudRepository
import screen.battlefieldHud.domain.BattlefieldHud.Idle
import screen.battlefieldHud.domain.BattlefieldHudError
import screen.battlefieldHud.domain.BattlefieldHudEvent
import shared.domain.FakeEventBus
import shared.domain.assertThat

class ConfirmCastTest {
    private val battleUnitApi: BattleUnitApi = mock()
    private val castAbility: CastAbility = mock()
    private val battlefieldHudRepository = InMemoryBattlefieldHudRepository()
    private val eventBus = _root_ide_package_.shared.domain.FakeEventBus()
    private val confirmCast =
        _root_ide_package_.screen.battlefieldHud.usecases.commands.ConfirmCast(
            battleUnitApi = battleUnitApi,
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
        )

    @BeforeEach
    fun setUp() {
        whenever(battleUnitApi.castAbility).thenReturn(castAbility)
    }

    @Test
    fun `should cast the ability and go idle when the hud is previewing the ability cast`() {
        // Given
        val castTile =
            _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother
                .tile(1, 1)
        battlefieldHudRepository.create(
            _root_ide_package_.battlefieldHud.domain.BattlefieldHudMother.displayAbilityCastPreview(
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
        assertThatThrownBy { confirmCast() }
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
        assertThatThrownBy { confirmCast() }
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
