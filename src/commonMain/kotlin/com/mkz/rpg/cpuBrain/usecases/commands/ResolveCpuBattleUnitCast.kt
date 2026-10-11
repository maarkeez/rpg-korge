package com.mkz.rpg.cpuBrain.usecases.commands

import com.mkz.rpg.cpuBrain.domain.CpuBrainRepository
import com.mkz.rpg.shared.domain.EventBus

/** Lets the CPU brain continue once the cast it awaits has been resolved. Casts of other battle units are ignored. */
class ResolveCpuBattleUnitCast(
    private val cpuBrainRepository: CpuBrainRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        battleUnitId: String,
        abilityId: String,
    ) {
        val cpuBrain = cpuBrainRepository.search() ?: return
        val (events, updatedCpuBrain) = cpuBrain.resolveCast(battleUnitId = battleUnitId, abilityId = abilityId).pullEvents()
        if (events.isEmpty()) return
        cpuBrainRepository.save(updatedCpuBrain)
        eventBus.publish(events)
    }
}
