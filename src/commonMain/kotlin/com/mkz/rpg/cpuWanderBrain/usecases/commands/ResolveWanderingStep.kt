package com.mkz.rpg.cpuWanderBrain.usecases.commands

import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainRepository
import com.mkz.rpg.cpuWanderBrain.usecases.services.WanderingSteps

class ResolveWanderingStep(
    private val cpuWanderBrainRepository: CpuWanderBrainRepository,
    private val wanderingSteps: WanderingSteps,
) {
    operator fun invoke(
        battleUnitId: String,
        row: Int,
        column: Int,
    ) {
        val cpuWanderBrain = cpuWanderBrainRepository.search() ?: return
        val resolvedCpuWanderBrain = cpuWanderBrain.resolveStep(battleUnitId = battleUnitId, row = row, column = column)
        if (resolvedCpuWanderBrain == cpuWanderBrain) return
        wanderingSteps.continueWandering(resolvedCpuWanderBrain)
    }
}
