package com.mkz.tessavar.cpuBattleBrain.domain

interface CpuBattleBrainRepository {
    fun save(cpuBattleBrain: CpuBattleBrain)

    fun search(): CpuBattleBrain?
}
