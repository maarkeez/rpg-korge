package com.mkz.rpg.cpuBattleBrain.adapters.storage

import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrain
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainRepository

class InMemoryCpuBattleBrainRepository : CpuBattleBrainRepository {
    private var cpuBattleBrain: CpuBattleBrain? = null

    override fun save(cpuBattleBrain: CpuBattleBrain) {
        this.cpuBattleBrain = cpuBattleBrain
    }

    override fun search() = cpuBattleBrain
}
