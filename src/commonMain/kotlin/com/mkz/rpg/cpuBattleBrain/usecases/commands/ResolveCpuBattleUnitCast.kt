package com.mkz.rpg.cpuBattleBrain.usecases.commands

import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainRepository
import com.mkz.rpg.shared.domain.EventBus

class ResolveCpuBattleUnitCast(
    private val cpuBattleBrainRepository: CpuBattleBrainRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        battleUnitId: String,
        abilityId: String,
    ) {
        val cpuBattleBrain = cpuBattleBrainRepository.search() ?: return
        val (events, updatedCpuBattleBrain) = cpuBattleBrain.resolveCast(battleUnitId = battleUnitId, abilityId = abilityId).pullEvents()
        if (events.isEmpty()) return
        cpuBattleBrainRepository.save(updatedCpuBattleBrain)
        eventBus.publish(events)
    }
}
