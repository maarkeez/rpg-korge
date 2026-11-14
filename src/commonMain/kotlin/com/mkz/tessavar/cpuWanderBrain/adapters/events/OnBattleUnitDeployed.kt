package com.mkz.tessavar.cpuWanderBrain.adapters.events

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.cpuWanderBrain.usecases.commands.ClaimHome
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnBattleUnitDeployed(
    claimHome: ClaimHome,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<BattleUnitEvent.BattleUnitDeployed> { event ->
            claimHome(battleUnitId = event.battleUnitId, row = event.row, column = event.column)
        }
}
