package com.mkz.tessavar.battleUnit.adapters.events

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.battleUnit.usecases.commands.DeployBattleUnit
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestDeployBattleUnit(
    deployBattleUnit: DeployBattleUnit,
    eventBus: EventBus,
) {
    private val subscription =
        eventBus.subscribe<BattleUnitEvent.RequestDeployBattleUnit> { event ->
            deployBattleUnit(
                battleUnitId = event.battleUnitId,
                unitId = event.unitId,
                playerId = event.playerId,
                deployAtRow = event.row,
                deployAtColumn = event.column,
            )
        }
}
