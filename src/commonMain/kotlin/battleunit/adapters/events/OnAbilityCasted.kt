package battleunit.adapters.events

import battleunit.domain.*
import battleunit.usecases.commands.*
import shared.domain.*

class OnAbilityCasted(
    receiveAbilityEffects: ReceiveAbilityEffects,
    eventBus: EventBus,
) {
    val subscription = eventBus.subscribe<BattleUnitEvent.AbilityCasted> { event ->
        receiveAbilityEffects(
            battleUnitId= event.battleUnitId,
            abilityId= event.abilityId,
            row= event.row,
            column= event.column,
        )
    }
}
