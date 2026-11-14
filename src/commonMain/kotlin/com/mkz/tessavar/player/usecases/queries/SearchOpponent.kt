package com.mkz.tessavar.player.usecases.queries

import com.mkz.tessavar.player.domain.Player
import com.mkz.tessavar.player.domain.PlayerRepository

class SearchOpponent(
    private val playerRepository: PlayerRepository,
) {
    operator fun invoke(playerId: String): Player.Dto? = playerRepository.searchOpponent(playerId).toDto()
}
