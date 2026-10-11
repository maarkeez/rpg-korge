package com.mkz.rpg.battlefield.usecases.services

interface BattlefieldLoader {
    suspend fun initResources() {}

    /** The terrain id of each tile, as a list of rows. */
    fun loadTiles(): List<List<String>>
}
