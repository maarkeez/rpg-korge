package com.mkz.rpg.party.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface PartyEvent : DomainEvent {
    object RequestStartExploration : PartyEvent

    /** Battle units: the party, the leader first. Units: the unit of each battle unit, in the same order. */
    data class ExplorationStarted(
        val playerId: String,
        val battleUnitIds: List<String>,
        val unitIds: List<String>,
    ) : PartyEvent

    /** Battle units: the party, the leader first. */
    data class PartyWalked(
        val playerId: String,
        val battleUnitIds: List<String>,
    ) : PartyEvent

    data class ExplorationPaused(
        val playerId: String,
    ) : PartyEvent

    /** Battle units: the party, the leader first. */
    data class ExplorationResumed(
        val playerId: String,
        val battleUnitIds: List<String>,
    ) : PartyEvent

    /** A new second starts while exploring, e.g.: the CPU battle units wander every second. */
    data class ExplorationSecondStarted(
        val playerId: String,
    ) : PartyEvent

    // Requested for the npc standing next to the leader, when A is pressed.
    data class RequestStartTalk(
        val npcId: String,
    ) : PartyEvent

    /** The party talks to the npc: time stops until the talk is finished. */
    data class NpcTalkStarted(
        val playerId: String,
        val npcId: String,
    ) : PartyEvent

    /** Npc: the npc talked to, which joins the party when it is recruitable. */
    data class NpcTalkFinished(
        val playerId: String,
        val npcId: String,
    ) : PartyEvent

    // Requested for a recruited npc, once deployed as a battle unit.
    data class RequestJoinParty(
        val battleUnitId: String,
    ) : PartyEvent

    /** The battle unit of the unit joins the party, last. */
    data class PartyJoined(
        val playerId: String,
        val battleUnitId: String,
        val unitId: String,
    ) : PartyEvent

    // Requested for a party member when an item applies an effect to it, e.g.: a consumed potion or an equipped hammer.
    data class RequestApplyMemberEffect(
        val battleUnitId: String,
        val lootableItemId: String,
        val effectId: String,
    ) : PartyEvent

    // Requested for a party member when an item is unequipped, so it loses the effects the item applied while equipped.
    data class RequestRemoveMemberEquippedEffects(
        val battleUnitId: String,
        val lootableItemId: String,
    ) : PartyEvent
}
