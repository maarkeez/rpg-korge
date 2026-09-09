package battlefield.adapters.events

import battlefield.usecases.commands.UpdateBattlefieldOccupancy
import battleunit.domain.BattleUnitEvent.BattleUnitMoved
import shared.domain.EventBus
import shared.domain.subscribe

class OnBattleUnitMoved(
    updateBattlefieldOccupancy: UpdateBattlefieldOccupancy,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitMoved> { event ->
            updateBattlefieldOccupancy(
                row = event.toRow,
                column = event.toColumn,
                battleUnitId = event.battleUnitId,
            )
        }
}
