package com.mkz.tessavar.cpuWanderBrain.usecases.commands

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent.RequestRespawnBattleUnit
import com.mkz.tessavar.battlefield.usecases.queries.IsTileVacant
import com.mkz.tessavar.cpuWanderBrain.domain.CpuWanderBrainRepository
import com.mkz.tessavar.shared.domain.EventBus

class RespawnDefeatedBattleUnits(
    private val cpuWanderBrainRepository: CpuWanderBrainRepository,
    private val isTileVacant: IsTileVacant,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val cpuWanderBrain = cpuWanderBrainRepository.search()?.walkPartyStep() ?: return
        cpuWanderBrainRepository.save(cpuWanderBrain)
        cpuWanderBrain
            .dueRespawns()
            .filter { home -> isTileVacant(row = home.row, column = home.column) }
            .forEach { home -> eventBus.publish(RequestRespawnBattleUnit(battleUnitId = home.battleUnitId, row = home.row, column = home.column)) }
    }
}
