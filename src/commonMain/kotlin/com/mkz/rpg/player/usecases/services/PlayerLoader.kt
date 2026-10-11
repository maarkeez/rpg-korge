package com.mkz.rpg.player.usecases.services

import com.mkz.rpg.player.domain.Player

interface PlayerLoader {
    suspend fun initResources() {}

    fun loadPlayers(): List<Player.Dto>
}
