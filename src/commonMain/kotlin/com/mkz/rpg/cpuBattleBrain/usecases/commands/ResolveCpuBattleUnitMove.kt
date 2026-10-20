package com.mkz.rpg.cpuBattleBrain.usecases.commands

import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainRepository
import com.mkz.rpg.shared.domain.EventBus

/** Lets the CPU battle brain continue once the move it awaits has been applied. Moves of other battle units are ignored. */
class ResolveCpuBattleUnitMove(
    private val cpuBattleBrainRepository: CpuBattleBrainRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        battleUnitId: String,
        row: Int,
        column: Int,
    ) {
        val cpuBattleBrain = cpuBattleBrainRepository.search() ?: return
        val (events, updatedCpuBattleBrain) = cpuBattleBrain.resolveMove(battleUnitId = battleUnitId, row = row, column = column).pullEvents()
        if (events.isEmpty()) return
        cpuBattleBrainRepository.save(updatedCpuBattleBrain)
        eventBus.publish(events)
    }
}
