package com.mkz.rpg.cpuBrain.usecases.commands

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.rpg.cpuBrain.domain.CpuBrain
import com.mkz.rpg.cpuBrain.domain.CpuBrainRepository
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
import com.mkz.rpg.shared.domain.EventBus

/**
 * Starts playing the turn of a CPU player. Its battle units in the battle are then managed one action at a time by [ManageCpuBattleUnit].
 * Battle units: those in the battle.
 */
class PlayTurn(
    private val searchPlayerById: SearchPlayerById,
    private val searchBattleUnitsByPlayerId: SearchBattleUnitsByPlayerId,
    private val cpuBrainRepository: CpuBrainRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        playerId: String,
        battleUnitIds: Set<String>,
    ) {
        val player = searchPlayerById(playerId) ?: return
        if (player.type != Player.Dto.PlayerTypeDto.CPU) return
        val managedBattleUnitIds = searchBattleUnitsByPlayerId(playerId).map { it.id }.filter { it in battleUnitIds }
        if (managedBattleUnitIds.isEmpty()) {
            eventBus.publish(BattleEvent.RequestFinishPlayerTurn)
            return
        }
        val (events, cpuBrain) = CpuBrain.startTurn(playerId = playerId, battleUnitIds = managedBattleUnitIds).pullEvents()
        cpuBrainRepository.save(cpuBrain)
        eventBus.publish(events)
    }
}
