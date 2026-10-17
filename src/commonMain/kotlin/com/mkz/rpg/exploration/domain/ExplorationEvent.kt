package com.mkz.rpg.exploration.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface ExplorationEvent : DomainEvent {
    object RequestStartExploration : ExplorationEvent

    /** Battle units: the party, the leader first. */
    data class ExplorationStarted(
        val playerId: String,
        val battleUnitIds: List<String>,
    ) : ExplorationEvent

    /** Battle units: the party, the leader first. */
    data class PartyWalked(
        val playerId: String,
        val battleUnitIds: List<String>,
    ) : ExplorationEvent

    data class ExplorationPaused(
        val playerId: String,
    ) : ExplorationEvent
}
