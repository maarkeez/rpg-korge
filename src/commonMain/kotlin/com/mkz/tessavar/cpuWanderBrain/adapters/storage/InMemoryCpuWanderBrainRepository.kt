package com.mkz.tessavar.cpuWanderBrain.adapters.storage

import com.mkz.tessavar.cpuWanderBrain.domain.CpuWanderBrain
import com.mkz.tessavar.cpuWanderBrain.domain.CpuWanderBrainRepository

class InMemoryCpuWanderBrainRepository : CpuWanderBrainRepository {
    private var cpuWanderBrain: CpuWanderBrain? = null

    override fun save(cpuWanderBrain: CpuWanderBrain) {
        this.cpuWanderBrain = cpuWanderBrain
    }

    override fun search() = cpuWanderBrain
}
