package com.mkz.rpg.cpuBattleBrain.adapters.events

import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent
import com.mkz.rpg.cpuBattleBrain.usecases.commands.ManageCpuBattleUnit
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnCpuBattleBrainInitialized(
    manageCpuBattleUnit: ManageCpuBattleUnit,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<CpuBattleBrainEvent.CpuBattleBrainInitialized> { event ->
            manageCpuBattleUnit(playerId = event.playerId)
        }
}
