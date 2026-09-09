package battlefieldHud.usecases.commands

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import screen.battlefieldHud.adapters.storage.InMemoryBattlefieldHudRepository
import screen.battlefieldHud.domain.BattlefieldHud

class InitializeBattlefieldHudTest {
    private val battlefieldHudRepository = InMemoryBattlefieldHudRepository()
    private val initializeBattlefieldHud =
        _root_ide_package_.screen.battlefieldHud.usecases.commands.InitializeBattlefieldHud(
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
