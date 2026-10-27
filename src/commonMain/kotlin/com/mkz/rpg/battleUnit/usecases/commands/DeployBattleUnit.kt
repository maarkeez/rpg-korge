package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.usecases.services.BattleUnitDeployment
import com.mkz.rpg.shared.domain.EventBus

class DeployBattleUnit(
    private val battleUnitDeployment: BattleUnitDeployment,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        battleUnitId: String,
        unitId: String,
        playerId: String,
        deployAtRow: Int,
        deployAtColumn: Int,
    ) {
        eventBus.publish(battleUnitDeployment.deploy(battleUnitId, unitId, playerId, deployAtRow, deployAtColumn))
    }
}
