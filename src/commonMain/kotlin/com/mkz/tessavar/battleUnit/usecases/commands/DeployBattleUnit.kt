package com.mkz.tessavar.battleUnit.usecases.commands

import com.mkz.tessavar.battleUnit.usecases.services.BattleUnitDeployment
import com.mkz.tessavar.shared.domain.EventBus

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
