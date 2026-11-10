package com.mkz.rpg.cpuWanderBrain.usecases.queries

import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrain
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainRepository

class SearchCpuWanderBrainSnapshot(
    private val cpuWanderBrainRepository: CpuWanderBrainRepository,
) {
    operator fun invoke(): CpuWanderBrain.Dto.SnapshotDto? = cpuWanderBrainRepository.search()?.toSnapshot()
}
