package com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.commands

import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.adapters.storage.InMemoryBattlefieldHudRepository
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

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
        battlefieldHudRepository.search().shouldBeInstanceOf<BattlefieldHud.Idle>()
    }
}
