package com.mkz.rpg.cpuBattleBrain.usecases.commands

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrain
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainRepository
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
import com.mkz.rpg.shared.domain.EventBus

/**
 * Starts playing the player turn of a CPU player. Its battle units in the battle are then pending, decided for one decision at a time by [MakeNextDecision].
 * Battle units: those in the battle.
 */
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
