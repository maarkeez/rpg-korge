package com.mkz.tessavar.cpuWanderBrain.usecases.queries

import com.mkz.tessavar.cpuWanderBrain.domain.CpuWanderBrain
import com.mkz.tessavar.cpuWanderBrain.domain.CpuWanderBrainRepository

class SearchCpuWanderBrainSnapshot(
    private val cpuWanderBrainRepository: CpuWanderBrainRepository,
) {
    operator fun invoke(): CpuWanderBrain.Dto.SnapshotDto? = cpuWanderBrainRepository.search()?.toSnapshot()
}
