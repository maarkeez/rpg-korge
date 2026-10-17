package com.mkz.rpg.exploration.domain

object ExplorationMother {
    fun exploration(
        playerId: String = "human",
        battleUnitIds: List<String> = listOf("knight-1", "knight-2"),
    ): Exploration = Exploration.start(playerId = playerId, battleUnitIds = battleUnitIds).pullEvents().second
}
