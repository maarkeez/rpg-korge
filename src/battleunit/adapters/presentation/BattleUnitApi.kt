package battleunit.adapters.presentation

import battleunit.adapters.storage.InMemoryBattleUnitRepository
import battleunit.domain.BattleUnitRepository
import battleunit.usecases.commands.DeployBattleUnit
import shared.domain.EventBus

class BattleUnitApi(eventBus: EventBus){

    // Storage
    private val battleUnitRepository: BattleUnitRepository = InMemoryBattleUnitRepository()

    // Commands
    val deployBattleUnit = DeployBattleUnit(battleUnitRepository, eventBus)

}
