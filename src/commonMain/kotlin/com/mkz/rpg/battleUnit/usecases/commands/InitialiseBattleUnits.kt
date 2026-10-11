package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battleUnit.usecases.services.BattleUnitDeployment
import com.mkz.rpg.battleUnit.usecases.services.BattleUnitLoader
import com.mkz.rpg.battlefield.usecases.queries.CanBattlefieldTileBeOccupied
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.unit.usecases.queries.SearchUnitById

class InitialiseBattleUnits(
    private val battleUnitLoader: BattleUnitLoader,
    private val battleUnitRepository: BattleUnitRepository,
    searchUnitById: SearchUnitById,
    searchPlayerById: SearchPlayerById,
    canBattlefieldTileBeOccupied: CanBattlefieldTileBeOccupied,
    private val eventBus: EventBus,
) {
    private val battleUnitDeployment =
        BattleUnitDeployment(battleUnitRepository, searchUnitById, searchPlayerById, canBattlefieldTileBeOccupied)

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
