package com.mkz.rpg.cpuWanderBrain.domain

object CpuWanderBrainMother {
    /** Homes: the tile each CPU battle unit was deployed on, by battle unit id. */
    fun cpuWanderBrain(homes: Map<String, Pair<Int, Int>> = mapOf("rat-1" to (0 to 0), "rat-2" to (2 to 2))): CpuWanderBrain =
        homes.entries.fold(CpuWanderBrain.create()) { cpuWanderBrain, (battleUnitId, home) ->
            cpuWanderBrain.claimHome(battleUnitId = battleUnitId, row = home.first, column = home.second)
        }
}
