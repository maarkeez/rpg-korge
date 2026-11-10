package com.mkz.rpg.cpuBattleBrain.usecases.commands

import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainRepository
import com.mkz.rpg.shared.domain.EventBus

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
