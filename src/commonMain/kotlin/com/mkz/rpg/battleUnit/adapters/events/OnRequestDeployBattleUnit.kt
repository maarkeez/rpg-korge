package com.mkz.rpg.battleUnit.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.usecases.commands.DeployBattleUnit
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestDeployBattleUnit(
    private val deployBattleUnit: DeployBattleUnit,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitEvent.RequestDeployBattleUnit> { event ->
            deployBattleUnit(
                battleUnitId = event.battleUnitId,
                unitId = event.unitId,
                playerId = event.playerId,
                deployAtRow = event.deployAtRow,
                deployAtColumn = event.deployAtColumn,
            )
        }
}
