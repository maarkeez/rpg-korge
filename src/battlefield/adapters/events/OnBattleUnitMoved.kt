package battlefield.adapters.events

import battlefield.usecases.commands.*
import battleunit.domain.BattleUnitEvent
import battleunit.domain.BattleUnitEvent.BattleUnitDeployed
import battleunit.domain.BattleUnitEvent.BattleUnitMoved
import shared.domain.*

class OnBattleUnitMoved(
    updateBattlefieldOccupancy: UpdateBattlefieldOccupancy,
    eventBus: EventBus
) {
    val subscription = eventBus.subscribe<BattleUnitMoved> { event ->
        updateBattlefieldOccupancy(
            row = event.toRow,
            column = event.toColumn,
            battleUnitId = event.battleUnitId,
        )
    }
}
