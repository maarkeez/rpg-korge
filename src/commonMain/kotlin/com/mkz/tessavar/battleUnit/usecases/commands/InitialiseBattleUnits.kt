package com.mkz.tessavar.battleUnit.usecases.commands

import com.mkz.tessavar.battleUnit.domain.BattleUnitRepository
import com.mkz.tessavar.battleUnit.usecases.services.BattleUnitDeployment
import com.mkz.tessavar.battleUnit.usecases.services.BattleUnitLoader
import com.mkz.tessavar.shared.domain.EventBus

class InitialiseBattleUnits(
    private val battleUnitLoader: BattleUnitLoader,
    private val battleUnitRepository: BattleUnitRepository,
    private val battleUnitDeployment: BattleUnitDeployment,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val events =
            battleUnitLoader
                .loadDeployments()
                .filter { deployment -> battleUnitRepository.searchById(deployment.battleUnitId) == null }
                .flatMap { deployment ->
                    battleUnitDeployment.deploy(
                        battleUnitId = deployment.battleUnitId,
                        unitId = deployment.unitId,
                        playerId = deployment.playerId,
                        deployAtRow = deployment.row,
                        deployAtColumn = deployment.column,
                    )
                }.toSet()
        eventBus.publish(events)
    }
}
