package screen.battlefieldHud.usecases.commands

import battleunit.adapters.presentation.*
import battleunit.usecases.commands.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.*
import org.mockito.kotlin.*
import screen.battlefieldHud.adapters.storage.*
import screen.battlefieldHud.domain.*
import screen.battlefieldHud.domain.BattlefieldHud.*
import shared.domain.*
import shared.domain.assertThat

class ConfirmCastTest {
    private val battleUnitApi: BattleUnitApi = mock()
    private val castAbility: CastAbility = mock()
    private val battlefieldHudRepository = InMemoryBattlefieldHudRepository()
    private val eventBus = FakeEventBus()
    private val confirmCast = ConfirmCast(
        battleUnitApi = battleUnitApi,
        battlefieldHudRepository = battlefieldHudRepository,
        eventBus = eventBus,
    )

    @Before
    fun setUp() {
        whenever(battleUnitApi.castAbility).thenReturn(castAbility)
    }

    @Test
    fun `should cast the ability and go idle when the hud is previewing the ability cast`() {
        // Given
        val castTile = BattlefieldHudMother.tile(1, 1)
        battlefieldHudRepository.create(
            BattlefieldHudMother.displayAbilityCastPreview(
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                castTile = castTile,
            )
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
        assertThat(eventBus).hasPublishedEvents(BattlefieldHudEvent.Idle)
    }

    @Test
    fun `should throw an invalid hud state error when the hud is idle`() {
        // Given
        battlefieldHudRepository.create(BattlefieldHudMother.idle())
        // When
        // Then
        assertThatThrownBy { confirmCast() }
            .isInstanceOf(BattlefieldHudError.InvalidBattlefieldHudState::class.java)
    }

    @Test
    fun `should throw an invalid hud state error when the hud is displaying the movement range`() {
        // Given
        battlefieldHudRepository.create(BattlefieldHudMother.displayMovementRange())
        // When
        // Then
        assertThatThrownBy { confirmCast() }
            .isInstanceOf(BattlefieldHudError.InvalidBattlefieldHudState::class.java)
    }

    @Test
    fun `should throw an invalid hud state error when the hud is displaying the ability cast range`() {
        // Given
        battlefieldHudRepository.create(BattlefieldHudMother.displayAbilityCastRange())
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
