package com.mkz.rpg.player.usecases.queries

import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.domain.PlayerRepository

/** The human player, e.g.: the one exploring, or `null` when there is none. When there are several, the first one created. */
class SearchHumanPlayer(
    private val playerRepository: PlayerRepository,
) {
    operator fun invoke(): Player.Dto? =
        playerRepository
            .searchAll()
            .map { player -> player.toDto() }
            .firstOrNull { player -> player.type == Player.Dto.PlayerTypeDto.HUMAN }
}
