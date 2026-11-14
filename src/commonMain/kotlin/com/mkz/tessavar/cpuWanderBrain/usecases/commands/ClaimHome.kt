package com.mkz.tessavar.cpuWanderBrain.usecases.commands

import com.mkz.tessavar.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.tessavar.cpuBattleBrain.usecases.queries.SearchDistancesFromHome
import com.mkz.tessavar.cpuWanderBrain.domain.CpuWanderBrain
import com.mkz.tessavar.cpuWanderBrain.domain.CpuWanderBrainRepository
import com.mkz.tessavar.player.domain.Player.Dto.PlayerTypeDto
import com.mkz.tessavar.player.usecases.queries.SearchPlayerById

class ClaimHome(
    private val cpuWanderBrainRepository: CpuWanderBrainRepository,
    private val searchBattleUnitById: SearchBattleUnitById,
    private val searchPlayerById: SearchPlayerById,
    private val searchDistancesFromHome: SearchDistancesFromHome,
) {
    operator fun invoke(
        battleUnitId: String,
        row: Int,
        column: Int,
    ) {
        val battleUnit = searchBattleUnitById(id = battleUnitId) ?: return
        if (battleUnit.remainingSummonDuration != null) return
        val player = searchPlayerById(id = battleUnit.playerId) ?: return
        if (player.type != PlayerTypeDto.CPU) return
        val cpuWanderBrain = cpuWanderBrainRepository.search() ?: CpuWanderBrain.create()
        if (cpuWanderBrain.homeOf(battleUnitId) != null) return
        val distancesFromHome = searchDistancesFromHome(homeRow = row, homeColumn = column)
        cpuWanderBrainRepository.save(cpuWanderBrain.claimHome(battleUnitId = battleUnitId, row = row, column = column, distancesFromHome = distancesFromHome))
    }
}
