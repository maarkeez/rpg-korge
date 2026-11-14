package com.mkz.tessavar.npc.domain

import com.mkz.tessavar.shared.domain.DomainEvent

sealed interface NpcEvent : DomainEvent {
    data class NpcPlaced(
        val npcId: String,
        val row: Int,
        val column: Int,
        val unitId: String? = null,
    ) : NpcEvent

    data class NpcRecruited(
        val npcId: String,
        val row: Int,
        val column: Int,
    ) : NpcEvent

    object RequestPlaceNpcs : NpcEvent
}
