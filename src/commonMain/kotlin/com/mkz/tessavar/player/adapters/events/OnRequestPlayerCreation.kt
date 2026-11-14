package com.mkz.tessavar.player.adapters.events

import com.mkz.tessavar.player.domain.Player
import com.mkz.tessavar.player.domain.PlayerEvent
import com.mkz.tessavar.player.usecases.commands.RequestPlayerCreation
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnRequestPlayerCreation(
    private val requestPlayerCreation: RequestPlayerCreation,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<PlayerEvent.RequestPlayerCreation> { event ->
            requestPlayerCreation(
                id = event.id,
                type =
                    when (event.type) {
                        Player.Dto.PlayerTypeDto.CPU -> RequestPlayerCreation.PlayerType.CPU
                        Player.Dto.PlayerTypeDto.HUMAN -> RequestPlayerCreation.PlayerType.HUMAN
                    },
            )
        }
}
