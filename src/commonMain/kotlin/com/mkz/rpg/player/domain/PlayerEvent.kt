package com.mkz.rpg.player.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface PlayerEvent : DomainEvent {
    data class PlayerCreated(
        val playerId: String,
        val playerType: Player.Dto.PlayerTypeDto,
    ) : PlayerEvent

    data class RequestPlayerCreation(
        val id: String,
        val type: Player.Dto.PlayerTypeDto,
    ) : PlayerEvent

    object RequestInitialisePlayers : PlayerEvent
}
