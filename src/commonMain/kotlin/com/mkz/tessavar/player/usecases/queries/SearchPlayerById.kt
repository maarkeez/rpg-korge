package com.mkz.tessavar.player.usecases.queries

import com.mkz.tessavar.player.domain.Player
import com.mkz.tessavar.player.domain.PlayerRepository

class SearchPlayerById(
    private val playerRepository: PlayerRepository,
) {
    operator fun invoke(id: String): Player.Dto? = playerRepository.searchById(id)?.toDto()
}
