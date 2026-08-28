package battlefield.adapters.events

import battlefield.usecases.commands.*
import battleunit.domain.BattleUnitEvent
import battleunit.domain.BattleUnitEvent.BattleUnitDefeated
import battleunit.domain.BattleUnitEvent.BattleUnitDeployed
import battleunit.domain.BattleUnitEvent.BattleUnitMoved
import shared.domain.*

class OnBattleUnitDefeated(
    removeOccupant: RemoveOccupant,
    eventBus: EventBus
) {
    val subscription = eventBus.subscribe<BattleUnitDefeated> { event ->
        removeOccupant(battleUnitId = event.battleUnitId)
    }
}
