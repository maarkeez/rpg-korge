package com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.commands

import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.adapters.storage.InMemoryBattlefieldHudRepository
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class InitializeBattlefieldHudTest {
    private val battlefieldHudRepository = InMemoryBattlefieldHudRepository()
    private val initializeBattlefieldHud =
        InitializeBattlefieldHud(
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
