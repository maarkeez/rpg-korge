package com.mkz.rpg.screen.battlefieldHud.usecases.commands

import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.Idle
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudRepository

class InitializeBattlefieldHud(
    private val battlefieldHudRepository: BattlefieldHudRepository,
) {
    operator fun invoke() {
        battlefieldHudRepository.create(Idle.create())
    }
}
