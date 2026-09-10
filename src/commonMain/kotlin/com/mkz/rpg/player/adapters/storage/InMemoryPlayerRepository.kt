package com.mkz.rpg.player.adapters.storage

import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.domain.PlayerRepository

class InMemoryPlayerRepository : PlayerRepository {
    private val players = mutableMapOf<String, Player>()

    override fun create(player: Player) {
        players[player.toDto().id] = player
    }

    override fun searchById(id: String) = players[id]

    override fun searchEnemy(playerId: String) = players.entries.first { it.key != playerId }.value
}
