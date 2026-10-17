package com.mkz.rpg.battle.usecases.services

// Loads the given player order, instead of reading it from the resources. It can be set once the players are known.
class FakeBattleLoader(
    var playerOrder: List<String> = emptyList(),
) : BattleLoader {
    override fun loadPlayerOrder(): List<String> = playerOrder
}
