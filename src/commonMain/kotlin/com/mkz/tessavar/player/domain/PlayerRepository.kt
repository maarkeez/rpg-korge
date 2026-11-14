package com.mkz.tessavar.player.domain

interface PlayerRepository {
    fun create(player: Player)

    fun searchById(id: String): Player?

    fun searchOpponent(playerId: String): Player

    fun searchAll(): List<Player>
}
