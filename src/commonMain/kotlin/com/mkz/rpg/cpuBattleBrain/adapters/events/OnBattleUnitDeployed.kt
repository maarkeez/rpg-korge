package com.mkz.rpg.cpuBattleBrain.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.cpuBattleBrain.usecases.commands.JoinCpuTurn
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
