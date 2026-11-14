package com.mkz.tessavar.cpuWanderBrain.domain

interface CpuWanderBrainRepository {
    fun save(cpuWanderBrain: CpuWanderBrain)

    fun search(): CpuWanderBrain?
}
