package com.mkz.rpg.cpuWanderBrain.usecases.commands

import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainRepository

class ResolveRespawn(
    private val cpuWanderBrainRepository: CpuWanderBrainRepository,
) {
    operator fun invoke(battleUnitId: String) {
        val cpuWanderBrain = cpuWanderBrainRepository.search() ?: return
        cpuWanderBrainRepository.save(cpuWanderBrain.respawn(battleUnitId))
    }
}
