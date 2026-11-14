package com.mkz.tessavar.cpuWanderBrain.usecases.commands

import com.mkz.tessavar.cpuWanderBrain.domain.CpuWanderBrainRepository

class AwaitRespawn(
    private val cpuWanderBrainRepository: CpuWanderBrainRepository,
) {
    operator fun invoke(battleUnitId: String) {
        val cpuWanderBrain = cpuWanderBrainRepository.search() ?: return
        cpuWanderBrainRepository.save(cpuWanderBrain.awaitRespawn(battleUnitId))
    }
}
