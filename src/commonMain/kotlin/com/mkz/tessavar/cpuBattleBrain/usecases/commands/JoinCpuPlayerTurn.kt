package com.mkz.tessavar.cpuBattleBrain.usecases.commands

import com.mkz.tessavar.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.tessavar.cpuBattleBrain.domain.CpuBattleBrainRepository

class JoinCpuPlayerTurn(
    private val cpuBattleBrainRepository: CpuBattleBrainRepository,
    private val searchBattleUnitById: SearchBattleUnitById,
) {
    operator fun invoke(battleUnitId: String) {
        val cpuBattleBrain = cpuBattleBrainRepository.search() ?: return
        val battleUnit = searchBattleUnitById(id = battleUnitId) ?: return
        if (cpuBattleBrain.toDto().playerId != battleUnit.playerId || !cpuBattleBrain.isPlayingPlayerTurn()) return
        cpuBattleBrainRepository.save(cpuBattleBrain.addBattleUnit(battleUnitId))
    }
}
