package com.mkz.rpg.cpuBattleBrain.usecases.commands

import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainRepository

/**
 * A battle unit deployed for the CPU player while it plays its turn (e.g.: a bat a goat summons) plays the turn too,
 * after the battle units already pending. Nothing happens out of the CPU turns (e.g.: the battle units deployed when the
 * game starts). The deploy is handled before the cast deploying it is resolved, so before the next decision.
 */
class JoinCpuTurn(
    private val cpuBattleBrainRepository: CpuBattleBrainRepository,
    private val searchBattleUnitById: SearchBattleUnitById,
) {
    operator fun invoke(battleUnitId: String) {
        val cpuBattleBrain = cpuBattleBrainRepository.search() ?: return
        val battleUnit = searchBattleUnitById(id = battleUnitId) ?: return
        if (cpuBattleBrain.toDto().playerId != battleUnit.playerId || !cpuBattleBrain.isPlayingTurn()) return
        cpuBattleBrainRepository.save(cpuBattleBrain.addBattleUnit(battleUnitId))
    }
}
