package screen.battlefieldHud.usecases.commands

import org.assertj.core.api.Assertions.assertThat
import org.junit.*
import screen.battlefieldHud.adapters.storage.*
import screen.battlefieldHud.domain.*

class InitializeBattlefieldHudTest {
    private val battlefieldHudRepository = InMemoryBattlefieldHudRepository()
    private val initializeBattlefieldHud = InitializeBattlefieldHud(
        battlefieldHudRepository = battlefieldHudRepository,
    )

    @Test
    fun `should create an idle battlefield hud when the battlefield hud is initialized`() {
        // Given
        // When
        initializeBattlefieldHud()
        // Then
        assertThat(battlefieldHudRepository.search()).isInstanceOf(BattlefieldHud.Idle::class.java)
    }
}
