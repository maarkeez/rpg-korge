package com.mkz.rpg.cpuWanderBrain.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrain
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainRepository
import com.mkz.rpg.cpuWanderBrain.usecases.queries.IsInSight
import com.mkz.rpg.cpuWanderBrain.usecases.services.WANDERING_RECOVERY_PERCENTAGE
import com.mkz.rpg.cpuWanderBrain.usecases.services.WanderingSteps
import com.mkz.rpg.shared.domain.EventBus

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
