package com.mkz.tessavar.cpuWanderBrain.usecases.commands

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.cpuWanderBrain.domain.CpuWanderBrain
import com.mkz.tessavar.cpuWanderBrain.domain.CpuWanderBrainRepository
import com.mkz.tessavar.cpuWanderBrain.usecases.queries.IsInSight
import com.mkz.tessavar.cpuWanderBrain.usecases.services.WANDERING_RECOVERY_PERCENTAGE
import com.mkz.tessavar.cpuWanderBrain.usecases.services.WanderingSteps
import com.mkz.tessavar.shared.domain.EventBus

class WanderAfterRound(
    private val cpuWanderBrainRepository: CpuWanderBrainRepository,
    private val wanderingSteps: WanderingSteps,
    private val isInSight: IsInSight,
    private val eventBus: EventBus,
) {
    operator fun invoke(battleUnitIds: Set<String>) {
        val cpuWanderBrain = cpuWanderBrainRepository.search() ?: CpuWanderBrain.create()
        val battleUnitIdsOutOfBattle =
            cpuWanderBrain
                .toDto()
                .homes
                .map { home -> home.battleUnitId }
                .filter { battleUnitId -> battleUnitId !in battleUnitIds }
        if (battleUnitIdsOutOfBattle.isNotEmpty()) {
            eventBus.publish(BattleUnitEvent.RequestRecoverBattleUnits(battleUnitIds = battleUnitIdsOutOfBattle, percentage = WANDERING_RECOVERY_PERCENTAGE))
        }
        val battleUnitIdsInSight = battleUnitIdsOutOfBattle.filter { battleUnitId -> isInSight(battleUnitId = battleUnitId) }
        wanderingSteps.continueWandering(cpuWanderBrain.startAfterRound(battleUnitIdsInBattle = battleUnitIds, battleUnitIds = battleUnitIdsInSight))
    }
}
