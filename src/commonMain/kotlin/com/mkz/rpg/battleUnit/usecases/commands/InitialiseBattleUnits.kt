package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battleUnit.usecases.services.BattleUnitDeployment
import com.mkz.rpg.battleUnit.usecases.services.BattleUnitLoader
import com.mkz.rpg.shared.domain.EventBus

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
