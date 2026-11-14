package com.mkz.tessavar.battlefield.usecases.services

interface BattlefieldLoader {
    suspend fun initResources() {}

    fun loadTiles(): List<List<String>>
}
