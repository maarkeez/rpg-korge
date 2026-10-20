package com.mkz.rpg.cpuWanderBrain.domain

interface CpuWanderBrainRepository {
    fun save(cpuWanderBrain: CpuWanderBrain)

    fun search(): CpuWanderBrain?
}
