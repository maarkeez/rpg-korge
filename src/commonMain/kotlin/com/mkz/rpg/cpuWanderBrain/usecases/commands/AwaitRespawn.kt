package com.mkz.rpg.cpuWanderBrain.usecases.commands

import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainRepository

class AwaitRespawn(
    private val cpuWanderBrainRepository: CpuWanderBrainRepository,
) {
    operator fun invoke(battleUnitId: String) {
        val cpuWanderBrain = cpuWanderBrainRepository.search() ?: return
        cpuWanderBrainRepository.save(cpuWanderBrain.awaitRespawn(battleUnitId))
    }
}
