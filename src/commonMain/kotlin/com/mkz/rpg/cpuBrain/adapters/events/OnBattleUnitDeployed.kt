package com.mkz.rpg.cpuBrain.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.cpuBrain.usecases.commands.JoinCpuTurn
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnBattleUnitDeployed(
    joinCpuTurn: JoinCpuTurn,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<BattleUnitEvent.BattleUnitDeployed> { event ->
            joinCpuTurn(battleUnitId = event.battleUnitId)
        }
}
