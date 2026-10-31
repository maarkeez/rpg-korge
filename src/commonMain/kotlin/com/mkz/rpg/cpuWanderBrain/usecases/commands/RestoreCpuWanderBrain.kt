package com.mkz.rpg.cpuWanderBrain.usecases.commands

import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrain
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainRepository

/**
 * Restores the CPU wander brain a save took, publishing no events. Nothing happens when there was none. The wandering
 * in progress is not saved: the battle units wander again on the next second.
 */
class RestoreCpuWanderBrain(
    private val cpuWanderBrainRepository: CpuWanderBrainRepository,
) {
    operator fun invoke(cpuWanderBrain: CpuWanderBrain.Dto.SnapshotDto?) {
        if (cpuWanderBrain == null) return
        cpuWanderBrainRepository.save(CpuWanderBrain.restore(cpuWanderBrain))
    }
}
