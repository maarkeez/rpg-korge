package com.mkz.rpg.battlefield.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface BattlefieldEvent : DomainEvent {
    object BattlefieldCreated : BattlefieldEvent

    data class BattlefieldTileOccupied(
        val row: Int,
        val column: Int,
        val battlefieldUnitId: String,
    ) : BattlefieldEvent

    data class OccupantRemoved(
        val battleUnitId: String,
        val row: Int,
        val column: Int,
    ) : BattlefieldEvent
}
