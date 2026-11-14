package com.mkz.tessavar.player.usecases.commands

import com.mkz.tessavar.player.domain.Player
import com.mkz.tessavar.player.domain.PlayerEvent
import com.mkz.tessavar.player.domain.PlayerRepository
import com.mkz.tessavar.player.usecases.services.PlayerLoader
import com.mkz.tessavar.shared.domain.EventBus

class InitialisePlayers(
    private val playerLoader: PlayerLoader,
    private val playerRepository: PlayerRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val events = mutableSetOf<PlayerEvent>()
        playerLoader.loadPlayers().forEach { playerDto ->
            if (playerRepository.searchById(playerDto.id) != null) return@forEach
            val (playerEvents, player) =
                when (playerDto.type) {
                    Player.Dto.PlayerTypeDto.CPU -> Player.createCpu(playerDto.id)
                    Player.Dto.PlayerTypeDto.HUMAN -> Player.createHuman(playerDto.id)
                }.pullEvents()
            playerRepository.create(player)
            events += playerEvents
        }
        eventBus.publish(events)
    }
}
