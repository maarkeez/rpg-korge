package com.mkz.rpg.cpuBrain.adapters.events

import com.mkz.rpg.cpuBrain.domain.CpuBrainEvent
import com.mkz.rpg.cpuBrain.usecases.commands.ManageCpuBattleUnit
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnCpuBattleUnitManaged(
    manageCpuBattleUnit: ManageCpuBattleUnit,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<CpuBrainEvent.CpuBattleUnitManaged> { event ->
            manageCpuBattleUnit(playerId = event.playerId)
        }
}
