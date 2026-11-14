package com.mkz.tessavar.cpuBattleBrain.adapters.storage

import com.mkz.tessavar.cpuBattleBrain.domain.CpuBattleBrain
import com.mkz.tessavar.cpuBattleBrain.domain.CpuBattleBrainRepository

class InMemoryCpuBattleBrainRepository : CpuBattleBrainRepository {
    private var cpuBattleBrain: CpuBattleBrain? = null

    override fun save(cpuBattleBrain: CpuBattleBrain) {
        this.cpuBattleBrain = cpuBattleBrain
    }

    override fun search() = cpuBattleBrain
}
