package com.mkz.rpg.cpuBrain.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.cpuBrain.usecases.commands.ResolveCpuBattleUnitCast
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnAbilityCastResolved(
    resolveCpuBattleUnitCast: ResolveCpuBattleUnitCast,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitEvent.AbilityCastResolved> { event ->
            resolveCpuBattleUnitCast(battleUnitId = event.battleUnitId, abilityId = event.abilityId)
        }
}
