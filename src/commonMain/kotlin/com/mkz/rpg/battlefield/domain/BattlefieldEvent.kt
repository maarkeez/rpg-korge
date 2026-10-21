package com.mkz.rpg.battlefield.domain

import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.rpg.shared.domain.DomainEvent

sealed interface BattlefieldEvent : DomainEvent {
    object BattlefieldCreated : BattlefieldEvent

    object RequestInitialiseBattlefield : BattlefieldEvent

    /** Requests the effect of each terrain for the given occupants standing on it, e.g.: the battle units standing still while exploring. */
    data class RequestEffectApplicationToOccupants(
        val occupantIds: Set<String>,
    ) : BattlefieldEvent

    // Requested for an occupant placed on a tile, e.g.: a battle unit deployed or respawned.
    data class RequestOccupyTile(
        val occupantId: String,
        val row: Int,
        val column: Int,
    ) : BattlefieldEvent

    // Requested for an occupant that walked or teleported. Path: the tiles walked from the origin (excluded) to the destination (included).
    data class RequestMoveOccupant(
        val occupantId: String,
        val toRow: Int,
        val toColumn: Int,
        val path: List<Battlefield.Dto.PositionDto>,
    ) : BattlefieldEvent

    // Requested for an occupant that is no longer on the battlefield, e.g.: a battle unit defeated or vanished.
    data class RequestRemoveOccupant(
        val occupantId: String,
    ) : BattlefieldEvent

    // Requested for an item stored by an inventory, which leaves the tile at row and column it was lying on.
    data class RequestPickUpDroppedItem(
        val lootableItemId: String,
        val row: Int,
        val column: Int,
    ) : BattlefieldEvent

    /** The terrain under an occupant has an effect for it, e.g.: lava hurts, water heals. The application has the terrain as its source and the occupant as its target. */
    data class TerrainEffectTriggered(
        val application: EffectApplicationDto,
    ) : BattlefieldEvent

    // Occupant: whoever stands on the tile, e.g.: a battle unit or an npc.
    data class BattlefieldTileOccupied(
        val row: Int,
        val column: Int,
        val occupantId: String,
    ) : BattlefieldEvent

    data class OccupantRemoved(
        val occupantId: String,
        val row: Int,
        val column: Int,
    ) : BattlefieldEvent

    data class DroppedItemPlaced(
        val lootableItemId: String,
        val row: Int,
        val column: Int,
    ) : BattlefieldEvent

    /** An occupant stepped on a tile with dropped items, listed in the order they were dropped. They stay on the tile until an inventory stores them. */
    data class LootFound(
        val occupantId: String,
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
