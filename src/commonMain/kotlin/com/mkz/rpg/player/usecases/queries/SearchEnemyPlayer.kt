package com.mkz.rpg.player.usecases.queries

import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.domain.PlayerRepository

class SearchEnemyPlayer(
    private val playerRepository: PlayerRepository,
) {
    operator fun invoke(playerId: String): Player.Dto? = playerRepository.searchEnemy(playerId).toDto()
}
