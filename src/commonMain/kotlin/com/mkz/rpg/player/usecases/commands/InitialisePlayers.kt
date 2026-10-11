package com.mkz.rpg.player.usecases.commands

import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.domain.PlayerEvent
import com.mkz.rpg.player.domain.PlayerRepository
import com.mkz.rpg.player.usecases.services.PlayerLoader
import com.mkz.rpg.shared.domain.EventBus

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
                    Player.Dto.PlayerTypeDto.CPU -> Player.createCpu(playerDto.id, playerDto.name)
                    Player.Dto.PlayerTypeDto.HUMAN -> Player.createHuman(playerDto.id, playerDto.name)
                }.pullEvents()
            playerRepository.create(player)
            events += playerEvents
        }
        eventBus.publish(events)
    }
}
