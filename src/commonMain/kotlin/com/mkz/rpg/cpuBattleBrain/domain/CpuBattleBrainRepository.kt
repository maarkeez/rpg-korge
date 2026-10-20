package com.mkz.rpg.cpuBattleBrain.domain

interface CpuBattleBrainRepository {
    fun save(cpuBattleBrain: CpuBattleBrain)

    fun search(): CpuBattleBrain?
}
