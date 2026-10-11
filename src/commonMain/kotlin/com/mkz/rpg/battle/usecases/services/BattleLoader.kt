package com.mkz.rpg.battle.usecases.services

interface BattleLoader {
    suspend fun initResources() {}

    /** The ids of the players, in the order they play each round. */
    fun loadPlayerOrder(): List<String>
}
