package battleunit.usecases.commands

import battleunit.domain.BattleUnitRepository
import shared.domain.EventBus

class DeployBattleUnit(
    private val battleUnitRepository: BattleUnitRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        // TODO: Implement
    }
}
