package com.mkz.rpg.cpuWanderBrain.usecases.queries

import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrain
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainRepository

/** What is saved of the CPU wander brain, if any: the home of each CPU battle unit, the ones returning home, and the respawns. */
class SearchCpuWanderBrainSnapshot(
    private val cpuWanderBrainRepository: CpuWanderBrainRepository,
) {
    operator fun invoke(): CpuWanderBrain.Dto.SnapshotDto? = cpuWanderBrainRepository.search()?.toSnapshot()
}
