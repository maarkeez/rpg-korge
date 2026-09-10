package com.mkz.rpg.player.usecases.queries

import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.domain.PlayerRepository

class SearchPlayerById(
    private val playerRepository: PlayerRepository,
) {
    operator fun invoke(id: String): Player.Dto? = playerRepository.searchById(id)?.toDto()
}
