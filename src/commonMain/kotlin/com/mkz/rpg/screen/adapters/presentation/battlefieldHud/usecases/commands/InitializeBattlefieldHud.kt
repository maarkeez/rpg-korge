package com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.commands

import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.Idle
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudRepository

class InitializeBattlefieldHud(
    private val battlefieldHudRepository: BattlefieldHudRepository,
) {
    operator fun invoke() {
        battlefieldHudRepository.create(Idle.create())
    }
}
