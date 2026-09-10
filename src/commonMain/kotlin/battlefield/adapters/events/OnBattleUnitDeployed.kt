package battlefield.adapters.events

import battleUnit.domain.BattleUnitEvent.BattleUnitDeployed
import battlefield.usecases.commands.UpdateBattlefieldOccupancy
import shared.domain.EventBus
import shared.domain.subscribe

class OnBattleUnitDeployed(
    updateBattlefieldOccupancy: UpdateBattlefieldOccupancy,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitDeployed> { event ->
            updateBattlefieldOccupancy(
                row = event.row,
                column = event.column,
                battleUnitId = event.battleUnitId,
            )
        }
}
