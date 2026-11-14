package com.mkz.tessavar.cpuWanderBrain.usecases.commands

import com.mkz.tessavar.cpuWanderBrain.domain.CpuWanderBrain
import com.mkz.tessavar.cpuWanderBrain.domain.CpuWanderBrainRepository

class LoadCpuWanderBrainSnapshot(
    private val cpuWanderBrainRepository: CpuWanderBrainRepository,
) {
    operator fun invoke(cpuWanderBrain: CpuWanderBrain.Dto.SnapshotDto?) {
        if (cpuWanderBrain == null) return
        cpuWanderBrainRepository.save(CpuWanderBrain.load(cpuWanderBrain))
    }
}
