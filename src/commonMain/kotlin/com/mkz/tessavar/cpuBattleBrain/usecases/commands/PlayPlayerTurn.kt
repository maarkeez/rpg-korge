package com.mkz.tessavar.cpuBattleBrain.usecases.commands

import com.mkz.tessavar.battle.domain.BattleEvent
import com.mkz.tessavar.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.tessavar.cpuBattleBrain.domain.CpuBattleBrain
import com.mkz.tessavar.cpuBattleBrain.domain.CpuBattleBrainRepository
import com.mkz.tessavar.player.domain.Player
import com.mkz.tessavar.player.usecases.queries.SearchPlayerById
import com.mkz.tessavar.shared.domain.EventBus

class PlayPlayerTurn(
    private val searchPlayerById: SearchPlayerById,
    private val searchBattleUnitsByPlayerId: SearchBattleUnitsByPlayerId,
    private val cpuBattleBrainRepository: CpuBattleBrainRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        playerId: String,
        battleUnitIds: Set<String>,
    ) {
        val player = searchPlayerById(playerId) ?: return
        if (player.type != Player.Dto.PlayerTypeDto.CPU) return
        val pendingBattleUnitIds = searchBattleUnitsByPlayerId(playerId).map { it.id }.filter { it in battleUnitIds }
        if (pendingBattleUnitIds.isEmpty()) {
            eventBus.publish(BattleEvent.RequestFinishPlayerTurn)
            return
        }
        val (events, cpuBattleBrain) = CpuBattleBrain.startPlayerTurn(playerId = playerId, battleUnitIds = pendingBattleUnitIds).pullEvents()
        cpuBattleBrainRepository.save(cpuBattleBrain)
        eventBus.publish(events)
    }
}
