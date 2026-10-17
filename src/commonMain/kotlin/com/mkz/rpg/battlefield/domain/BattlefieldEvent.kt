package com.mkz.rpg.battlefield.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface BattlefieldEvent : DomainEvent {
    object BattlefieldCreated : BattlefieldEvent

    object RequestInitialiseBattlefield : BattlefieldEvent

    /** Requests the effect of each terrain for the given battle units standing on it, e.g.: those standing still while exploring. */
    data class RequestEffectApplicationToOccupants(
        val battleUnitIds: Set<String>,
    ) : BattlefieldEvent

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

    data class DroppedItemPlaced(
        val lootableItemId: String,
        val row: Int,
        val column: Int,
    ) : BattlefieldEvent

    /** A battle unit stepped on a tile with dropped items, listed in the order they were dropped. They stay on the tile until an inventory stores them. */
    data class LootFound(
        val battleUnitId: String,
        val lootableItemIds: List<String>,
        val row: Int,
        val column: Int,
    ) : BattlefieldEvent

    data class DroppedItemPickedUp(
        val lootableItemId: String,
        val row: Int,
        val column: Int,
    ) : BattlefieldEvent
}
