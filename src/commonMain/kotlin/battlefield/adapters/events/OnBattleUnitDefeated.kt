package battlefield.adapters.events

import battleUnit.domain.BattleUnitEvent.BattleUnitDefeated
import battlefield.usecases.commands.RemoveOccupant
import shared.domain.EventBus
import shared.domain.subscribe

class OnBattleUnitDefeated(
    removeOccupant: RemoveOccupant,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitDefeated> { event ->
            removeOccupant(battleUnitId = event.battleUnitId)
        }
}
