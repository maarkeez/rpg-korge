package com.mkz.tessavar.cpuBattleBrain.adapters.events

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.cpuBattleBrain.usecases.commands.ResolveCpuBattleUnitCast
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnAbilityCastResolved(
    resolveCpuBattleUnitCast: ResolveCpuBattleUnitCast,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitEvent.AbilityCastResolved> { event ->
            resolveCpuBattleUnitCast(battleUnitId = event.battleUnitId, abilityId = event.abilityId)
        }
}
