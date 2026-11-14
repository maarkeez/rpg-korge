package com.mkz.tessavar.cpuBattleBrain.adapters.events

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.cpuBattleBrain.usecases.commands.JoinCpuPlayerTurn
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnBattleUnitDeployed(
    joinCpuPlayerTurn: JoinCpuPlayerTurn,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<BattleUnitEvent.BattleUnitDeployed> { event ->
            joinCpuPlayerTurn(battleUnitId = event.battleUnitId)
        }
}
