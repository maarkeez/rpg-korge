package com.mkz.rpg.cpuBrain.usecases.commands

import com.mkz.rpg.cpuBrain.domain.CpuBrainRepository
import com.mkz.rpg.shared.domain.EventBus

/** Lets the CPU brain continue once the move it awaits has been applied. Moves of other battle units are ignored. */
class ResolveCpuBattleUnitMove(
    private val cpuBrainRepository: CpuBrainRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        battleUnitId: String,
        row: Int,
        column: Int,
    ) {
        val cpuBrain = cpuBrainRepository.search() ?: return
        val (events, updatedCpuBrain) = cpuBrain.resolveMove(battleUnitId = battleUnitId, row = row, column = column).pullEvents()
        if (events.isEmpty()) return
        cpuBrainRepository.save(updatedCpuBrain)
        eventBus.publish(events)
    }
}
