package com.mkz.rpg.battle.usecases.services

// Loads the given player order, instead of reading it from the resources.
class FakeBattleLoader(
    private val playerOrder: List<String> = emptyList(),
) : BattleLoader {
    override fun loadPlayerOrder(): List<String> = playerOrder
}
