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

    /** Battle units: the party, the leader first. */
    data class ExplorationResumed(
        val playerId: String,
        val battleUnitIds: List<String>,
    ) : ExplorationEvent

    /** A new second starts while exploring, e.g.: the CPU battle units wander every second. */
    data class ExplorationSecondStarted(
        val playerId: String,
    ) : ExplorationEvent
}
