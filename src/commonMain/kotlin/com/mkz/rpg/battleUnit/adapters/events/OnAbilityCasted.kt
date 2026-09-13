package com.mkz.rpg.battleUnit.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.usecases.commands.ReceiveAbilityEffects
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnAbilityCasted(
    receiveAbilityEffects: ReceiveAbilityEffects,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitEvent.AbilityCasted> { event ->
            event.castGroup.forEach { position ->
                receiveAbilityEffects(
                    battleUnitId = event.battleUnitId,
                    abilityId = event.abilityId,
                    row = position.row,
                    column = position.column,
                )
            }
        }
}
