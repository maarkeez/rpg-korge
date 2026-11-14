package com.mkz.tessavar.player.usecases.services

import com.mkz.tessavar.player.domain.Player

class FakePlayerLoader(
    private val players: List<Player.Dto> = emptyList(),
) : PlayerLoader {
    override fun loadPlayers(): List<Player.Dto> = players
}
