package com.mkz.rpg.player.usecases.queries

import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.domain.PlayerRepository

class SearchOpponent(
    private val playerRepository: PlayerRepository,
) {
    operator fun invoke(playerId: String): Player.Dto? = playerRepository.searchOpponent(playerId).toDto()
}
