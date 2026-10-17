package com.mkz.rpg.wandering.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface WanderingEvent : DomainEvent {
    /** Every CPU battle unit has rested or stepped, while the player explores. */
    data class BattleUnitsWanderedWhileExploring(
        val exploringPlayerId: String,
    ) : WanderingEvent

    /** Every CPU battle unit out of the battle has rested or stepped, once a round finished. Battle units: those in the battle. */
    data class BattleUnitsWanderedAfterRound(
        val battleUnitIds: Set<String>,
    ) : WanderingEvent
}
