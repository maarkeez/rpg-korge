package com.mkz.rpg.cpuWanderBrain.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.cpuWanderBrain.usecases.commands.ClaimHome
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnBattleUnitDeployed(
    claimHome: ClaimHome,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<BattleUnitEvent.BattleUnitDeployed> { event ->
            claimHome(battleUnitId = event.battleUnitId, row = event.row, column = event.column)
        }
}
