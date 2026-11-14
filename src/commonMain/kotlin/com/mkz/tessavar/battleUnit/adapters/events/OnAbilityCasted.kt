package com.mkz.tessavar.battleUnit.adapters.events

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.battleUnit.usecases.commands.RequestEffectApplicationToCastTargets
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnAbilityCasted(
    requestEffectApplicationToCastTargets: RequestEffectApplicationToCastTargets,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitEvent.AbilityCasted> { event ->
            requestEffectApplicationToCastTargets(
                battleUnitId = event.battleUnitId,
                abilityId = event.abilityId,
                castGroup = event.castGroup,
            )
        }
}
