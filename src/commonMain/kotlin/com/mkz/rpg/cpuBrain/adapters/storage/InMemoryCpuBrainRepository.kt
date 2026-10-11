package com.mkz.rpg.cpuBrain.adapters.storage

import com.mkz.rpg.cpuBrain.domain.CpuBrain
import com.mkz.rpg.cpuBrain.domain.CpuBrainRepository

// Holds the CPU brain of the turn in progress.
class InMemoryCpuBrainRepository : CpuBrainRepository {
    private var cpuBrain: CpuBrain? = null

    override fun save(cpuBrain: CpuBrain) {
        this.cpuBrain = cpuBrain
    }

    override fun search() = cpuBrain
}
