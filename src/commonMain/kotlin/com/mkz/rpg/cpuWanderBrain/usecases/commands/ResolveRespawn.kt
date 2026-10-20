package com.mkz.rpg.cpuWanderBrain.usecases.commands

import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainRepository

/** The battle unit respawned on its home, so it no longer counts the party steps. */
class ResolveRespawn(
    private val cpuWanderBrainRepository: CpuWanderBrainRepository,
) {
    operator fun invoke(battleUnitId: String) {
        val cpuWanderBrain = cpuWanderBrainRepository.search() ?: return
        cpuWanderBrainRepository.save(cpuWanderBrain.respawn(battleUnitId))
    }
}
