package com.mkz.rpg.player.usecases.services

import com.mkz.rpg.player.domain.Player

class FakePlayerLoader(
    private val players: List<Player.Dto> = emptyList(),
) : PlayerLoader {
    override fun loadPlayers(): List<Player.Dto> = players
}
