package com.mkz.rpg.wandering.domain

object WanderingMother {
    /** Homes: the tile each CPU battle unit was deployed on, by battle unit id. */
    fun wandering(homes: Map<String, Pair<Int, Int>> = mapOf("rat-1" to (0 to 0), "rat-2" to (2 to 2))): Wandering =
        homes.entries.fold(Wandering.create()) { wandering, (battleUnitId, home) ->
            wandering.claimHome(battleUnitId = battleUnitId, row = home.first, column = home.second)
        }
}
