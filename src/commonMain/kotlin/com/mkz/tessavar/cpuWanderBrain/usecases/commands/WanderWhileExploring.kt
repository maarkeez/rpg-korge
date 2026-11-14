package com.mkz.tessavar.cpuWanderBrain.usecases.commands

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.cpuWanderBrain.domain.CpuWanderBrainRepository
import com.mkz.tessavar.cpuWanderBrain.usecases.queries.IsInSight
import com.mkz.tessavar.cpuWanderBrain.usecases.services.WANDERING_RECOVERY_PERCENTAGE
import com.mkz.tessavar.cpuWanderBrain.usecases.services.WanderingSteps
import com.mkz.tessavar.shared.domain.EventBus

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
