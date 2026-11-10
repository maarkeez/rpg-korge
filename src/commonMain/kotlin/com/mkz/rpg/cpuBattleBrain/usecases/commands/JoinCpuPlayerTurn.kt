package com.mkz.rpg.cpuBattleBrain.usecases.commands

import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainRepository

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
