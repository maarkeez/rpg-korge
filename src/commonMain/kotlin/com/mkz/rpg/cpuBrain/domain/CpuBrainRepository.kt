package com.mkz.rpg.cpuBrain.domain

interface CpuBrainRepository {
    fun save(cpuBrain: CpuBrain)

    fun search(): CpuBrain?
}
