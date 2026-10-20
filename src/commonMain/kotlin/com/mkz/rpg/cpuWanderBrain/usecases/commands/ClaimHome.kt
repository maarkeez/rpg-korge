package com.mkz.rpg.cpuWanderBrain.usecases.commands

import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrain
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainRepository
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto
import com.mkz.rpg.player.usecases.queries.SearchPlayerById

/**
 * Remembers the tile a CPU battle unit is deployed on as its home, which it wanders around while out of a battle. The
 * summons claim none, as they never wander: they vanish once their turns are over.
 */
class ClaimHome(
    private val cpuWanderBrainRepository: CpuWanderBrainRepository,
    private val searchBattleUnitById: SearchBattleUnitById,
    private val searchPlayerById: SearchPlayerById,
) {
    operator fun invoke(
        battleUnitId: String,
        row: Int,
        column: Int,
    ) {
        val battleUnit = searchBattleUnitById(id = battleUnitId) ?: return
        if (battleUnit.remainingTurns != null) return
        val player = searchPlayerById(id = battleUnit.playerId) ?: return
        if (player.type != PlayerTypeDto.CPU) return
        val cpuWanderBrain = cpuWanderBrainRepository.search() ?: CpuWanderBrain.create()
        cpuWanderBrainRepository.save(cpuWanderBrain.claimHome(battleUnitId = battleUnitId, row = row, column = column))
    }
}
