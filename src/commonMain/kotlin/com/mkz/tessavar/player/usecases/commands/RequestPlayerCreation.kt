package com.mkz.tessavar.player.usecases.commands

import com.mkz.tessavar.player.domain.Player
import com.mkz.tessavar.player.domain.PlayerRepository
import com.mkz.tessavar.shared.domain.EventBus

class RequestPlayerCreation(
    private val playerRepository: PlayerRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        id: String,
        type: PlayerType,
    ) {
        if (playerRepository.searchById(id) != null) return
        val (events, player) =
            when (type) {
                PlayerType.CPU -> Player.createCpu(id)
                PlayerType.HUMAN -> Player.createHuman(id)
            }.pullEvents()
        playerRepository.create(player)
        eventBus.publish(events)
    }

    enum class PlayerType {
        CPU,
        HUMAN,
    }
}
