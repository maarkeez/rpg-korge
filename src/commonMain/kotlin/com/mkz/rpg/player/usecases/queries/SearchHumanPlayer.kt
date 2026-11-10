package com.mkz.rpg.player.usecases.queries

import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.domain.PlayerRepository

class SearchHumanPlayer(
    private val playerRepository: PlayerRepository,
) {
    operator fun invoke(): Player.Dto? =
        playerRepository
            .searchAll()
            .map { player -> player.toDto() }
            .firstOrNull { player -> player.type == Player.Dto.PlayerTypeDto.HUMAN }
}
