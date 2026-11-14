package com.mkz.tessavar.player.usecases.services

import com.mkz.tessavar.player.domain.Player

interface PlayerLoader {
    suspend fun initResources() {}

    fun loadPlayers(): List<Player.Dto>
}
