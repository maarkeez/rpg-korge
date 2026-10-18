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

    /** The party talks to the npc: time stops until the talk is finished. */
    data class NpcTalkStarted(
        val playerId: String,
        val npcId: String,
    ) : ExplorationEvent

    /** Npc: the npc talked to, which joins the party when it is recruitable. */
    data class NpcTalkFinished(
        val playerId: String,
        val npcId: String,
    ) : ExplorationEvent

    // Requested for a recruited npc, once deployed as a battle unit.
    data class RequestJoinParty(
        val battleUnitId: String,
    ) : ExplorationEvent

    /** The battle unit joins the party, last. */
    data class PartyJoined(
        val playerId: String,
        val battleUnitId: String,
    ) : ExplorationEvent
}
