package com.mkz.rpg.battlesetup.adapters.presentation

import com.mkz.rpg.battlesetup.usecases.commands.SetupBattle
import com.mkz.rpg.shared.domain.EventBus

class BattleSetupApi(
    eventBus: EventBus,
) {
    val setupBattle = SetupBattle(eventBus)
}
