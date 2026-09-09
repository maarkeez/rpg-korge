package battleunit.adapters.events

import battleunit.domain.BattleUnitEvent
import battleunit.usecases.commands.ReceiveAbilityEffects
import shared.domain.EventBus
import shared.domain.subscribe

class OnAbilityCasted(
    receiveAbilityEffects: ReceiveAbilityEffects,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitEvent.AbilityCasted> { event ->
            receiveAbilityEffects(
                battleUnitId = event.battleUnitId,
                abilityId = event.abilityId,
                row = event.row,
                column = event.column,
            )
        }
}
