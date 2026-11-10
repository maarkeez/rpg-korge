package com.mkz.rpg.cpuWanderBrain.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainRepository
import com.mkz.rpg.cpuWanderBrain.usecases.queries.IsInSight
import com.mkz.rpg.cpuWanderBrain.usecases.services.WANDERING_RECOVERY_PERCENTAGE
import com.mkz.rpg.cpuWanderBrain.usecases.services.WanderingSteps
import com.mkz.rpg.shared.domain.EventBus

class WanderWhileExploring(
    private val cpuWanderBrainRepository: CpuWanderBrainRepository,
    private val wanderingSteps: WanderingSteps,
    private val isInSight: IsInSight,
    private val eventBus: EventBus,
) {
    operator fun invoke(exploringPlayerId: String) {
        val cpuWanderBrain = cpuWanderBrainRepository.search() ?: return
        val battleUnitIds = cpuWanderBrain.toDto().homes.map { home -> home.battleUnitId }
        if (battleUnitIds.isNotEmpty()) {
            eventBus.publish(BattleUnitEvent.RequestRecoverBattleUnits(battleUnitIds = battleUnitIds, percentage = WANDERING_RECOVERY_PERCENTAGE))
        }
        val battleUnitIdsInSight = battleUnitIds.filter { battleUnitId -> isInSight(battleUnitId = battleUnitId) }
        wanderingSteps.continueWandering(cpuWanderBrain.startWhileExploring(exploringPlayerId = exploringPlayerId, battleUnitIds = battleUnitIdsInSight))
    }
}
