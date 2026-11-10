package com.mkz.rpg.battlefield.usecases.services

interface BattlefieldLoader {
    suspend fun initResources() {}

    fun loadTiles(): List<List<String>>
}
