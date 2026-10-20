package com.mkz.rpg.cpuWanderBrain.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.RequestRespawnBattleUnit
import com.mkz.rpg.battlefield.usecases.queries.CanBattlefieldTileBeOccupied
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainRepository
import com.mkz.rpg.shared.domain.EventBus

/**
 * The party walked a step: every defeated CPU battle unit counts it, and those that have counted enough steps are
 * requested to respawn on their home. The ones whose home is occupied (e.g.: by a battle unit of the party) wait, and
 * are requested again on the next step.
 */
class RespawnDefeatedBattleUnits(
    private val cpuWanderBrainRepository: CpuWanderBrainRepository,
    private val canBattlefieldTileBeOccupied: CanBattlefieldTileBeOccupied,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val cpuWanderBrain = cpuWanderBrainRepository.search()?.walkPartyStep() ?: return
        cpuWanderBrainRepository.save(cpuWanderBrain)
        cpuWanderBrain
            .dueRespawns()
            .filter { home -> canBattlefieldTileBeOccupied(row = home.row, column = home.column) }
            .forEach { home -> eventBus.publish(RequestRespawnBattleUnit(battleUnitId = home.battleUnitId, row = home.row, column = home.column)) }
    }
}
