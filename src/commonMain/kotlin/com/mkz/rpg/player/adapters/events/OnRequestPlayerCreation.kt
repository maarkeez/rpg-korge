package com.mkz.rpg.player.adapters.events

import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.domain.PlayerEvent
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestPlayerCreation(
    private val requestPlayerCreation: RequestPlayerCreation,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<PlayerEvent.RequestPlayerCreation> { event ->
            requestPlayerCreation(
                id = event.id,
                name = event.name,
                type =
                    when (event.type) {
                        Player.Dto.PlayerTypeDto.CPU -> RequestPlayerCreation.PlayerType.CPU
                        Player.Dto.PlayerTypeDto.HUMAN -> RequestPlayerCreation.PlayerType.HUMAN
                    },
            )
        }
}
