package com.mkz.rpg.player.usecases.commands

import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.domain.PlayerRepository
import com.mkz.rpg.shared.domain.EventBus

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
