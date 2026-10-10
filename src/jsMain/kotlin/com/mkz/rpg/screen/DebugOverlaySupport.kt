package com.mkz.rpg.screen

import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudRepository
import com.mkz.rpg.shared.domain.EventBus

internal actual fun BattleScene.installDebugSupport(
    battleApi: BattleApi,
    playerApi: PlayerApi,
    battlefieldHudRepository: BattlefieldHudRepository,
    eventBus: EventBus,
    effectiveSeed: Long?,
) {
}
