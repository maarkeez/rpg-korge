package com.mkz.rpg.battlefield.domain

import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.rpg.shared.domain.DomainEvent

sealed interface BattlefieldEvent : DomainEvent {
    object BattlefieldCreated : BattlefieldEvent

    object RequestInitialiseBattlefield : BattlefieldEvent

    /** Requests the effect of each terrain for the given battle units standing on it, e.g.: those standing still while exploring. */
    data class RequestEffectApplicationToOccupants(
        val battleUnitIds: Set<String>,
    ) : BattlefieldEvent

    // Requested for a battle unit placed on a tile, e.g.: when it is deployed or respawns.
    data class RequestOccupyTile(
        val battleUnitId: String,
        val row: Int,
        val column: Int,
    ) : BattlefieldEvent

    // Requested for a battle unit that walked or teleported. Path: the tiles walked from the origin (excluded) to the destination (included).
    data class RequestMoveOccupant(
        val battleUnitId: String,
        val toRow: Int,
        val toColumn: Int,
        val path: List<Battlefield.Dto.PositionDto>,
    ) : BattlefieldEvent

    // Requested for a battle unit that is no longer on the battlefield, e.g.: when it is defeated or vanishes.
    data class RequestRemoveOccupant(
        val battleUnitId: String,
    ) : BattlefieldEvent

    /** The terrain under a battle unit has an effect for it, e.g.: lava hurts, water heals. The application has the terrain as its source and the battle unit as its target. */
    data class TerrainEffectTriggered(
        val application: EffectApplicationDto,
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
