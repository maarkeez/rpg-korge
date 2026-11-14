package com.mkz.tessavar.cpuBattleBrain.usecases.commands

import com.mkz.tessavar.cpuBattleBrain.domain.CpuBattleBrainRepository
import com.mkz.tessavar.shared.domain.EventBus

class ResolveCpuBattleUnitWalk(
    private val cpuBattleBrainRepository: CpuBattleBrainRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        battleUnitId: String,
        row: Int,
        column: Int,
    ) {
        val cpuBattleBrain = cpuBattleBrainRepository.search() ?: return
        val (events, updatedCpuBattleBrain) = cpuBattleBrain.resolveWalk(battleUnitId = battleUnitId, row = row, column = column).pullEvents()
        if (events.isEmpty()) return
        cpuBattleBrainRepository.save(updatedCpuBattleBrain)
        eventBus.publish(events)
    }
}
