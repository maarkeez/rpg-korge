package com.mkz.rpg.cpuWanderBrain.adapters.storage

import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrain
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainRepository

// Holds the homes of the CPU battle units of the game in progress, and their wandering.
class InMemoryCpuWanderBrainRepository : CpuWanderBrainRepository {
    private var cpuWanderBrain: CpuWanderBrain? = null

    override fun save(cpuWanderBrain: CpuWanderBrain) {
        this.cpuWanderBrain = cpuWanderBrain
    }

    override fun search() = cpuWanderBrain
}
