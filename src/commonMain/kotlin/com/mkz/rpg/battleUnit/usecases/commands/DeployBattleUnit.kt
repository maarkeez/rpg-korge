package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battleUnit.usecases.services.BattleUnitDeployment
import com.mkz.rpg.battlefield.usecases.queries.CanBattlefieldTileBeOccupied
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.unit.usecases.queries.SearchUnitById

class DeployBattleUnit(
    battleUnitRepository: BattleUnitRepository,
    private val eventBus: EventBus,
    searchUnitById: SearchUnitById,
    searchPlayerById: SearchPlayerById,
    canBattlefieldTileBeOccupied: CanBattlefieldTileBeOccupied,
) {
    private val battleUnitDeployment =
        BattleUnitDeployment(battleUnitRepository, searchUnitById, searchPlayerById, canBattlefieldTileBeOccupied)

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
