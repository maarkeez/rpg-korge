package battlefield.adapters.events

import battlefield.usecases.commands.*
import battleunit.domain.BattleUnitEvent.BattleUnitDeployed
import shared.domain.*

class OnBattleUnitDeployed(
    updateBattlefieldOccupancy: UpdateBattlefieldOccupancy,
    eventBus: EventBus
) {
    val subscription = eventBus.subscribe<BattleUnitDeployed> { event ->
        updateBattlefieldOccupancy(
            row = event.row,
            column = event.column,
            battleUnitId = event.battleUnitId,
        )
    }
}
