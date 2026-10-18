package com.mkz.rpg.npc.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface NpcEvent : DomainEvent {
    // Unit: the unit a recruitable npc joins the party as, whose images it is displayed with.
    data class NpcPlaced(
        val npcId: String,
        val row: Int,
        val column: Int,
        val unitId: String? = null,
    ) : NpcEvent

    // The npc leaves its tile, as it joins the party.
    data class NpcRecruited(
        val npcId: String,
        val row: Int,
        val column: Int,
    ) : NpcEvent

    object RequestInitialiseNpcs : NpcEvent
}
