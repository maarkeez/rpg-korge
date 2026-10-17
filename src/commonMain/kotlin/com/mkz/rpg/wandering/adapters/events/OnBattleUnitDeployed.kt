package com.mkz.rpg.wandering.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.wandering.usecases.commands.ClaimHome

class OnBattleUnitDeployed(
    claimHome: ClaimHome,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<BattleUnitEvent.BattleUnitDeployed> { event ->
            claimHome(battleUnitId = event.battleUnitId, row = event.row, column = event.column)
        }
}
