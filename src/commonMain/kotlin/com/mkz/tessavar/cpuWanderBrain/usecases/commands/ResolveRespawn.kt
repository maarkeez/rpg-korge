package com.mkz.tessavar.cpuWanderBrain.usecases.commands

import com.mkz.tessavar.cpuWanderBrain.domain.CpuWanderBrainRepository

class ResolveRespawn(
    private val cpuWanderBrainRepository: CpuWanderBrainRepository,
) {
    operator fun invoke(battleUnitId: String) {
        val cpuWanderBrain = cpuWanderBrainRepository.search() ?: return
        cpuWanderBrainRepository.save(cpuWanderBrain.respawn(battleUnitId))
    }
}
