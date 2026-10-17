package com.mkz.rpg.wandering.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface WanderingEvent : DomainEvent {
    /** Every CPU battle unit has rested or stepped, while the player explores. */
    data class BattleUnitsWanderedWhileExploring(
        val exploringPlayerId: String,
    ) : WanderingEvent
}
