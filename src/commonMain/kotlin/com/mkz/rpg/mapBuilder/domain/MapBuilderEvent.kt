package com.mkz.rpg.mapBuilder.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface MapBuilderEvent : DomainEvent {
    data class MapCreated(
        val rows: Int,
        val columns: Int,
    ) : MapBuilderEvent

    data class MapResized(
        val rows: Int,
        val columns: Int,
    ) : MapBuilderEvent

    data class TerrainSelected(
        val terrainId: String,
    ) : MapBuilderEvent

    data class TilePainted(
        val row: Int,
        val column: Int,
        val terrainId: String,
    ) : MapBuilderEvent

    data class MapImported(
        val rows: Int,
        val columns: Int,
    ) : MapBuilderEvent

    /** The map was not replaced, as the imported one is not valid. The terrains tell which ones made it invalid, if any. */
    data class MapImportRejected(
        val reason: Reason,
        val terrainId: String? = null,
        val neighbourTerrainId: String? = null,
    ) : MapBuilderEvent {
        enum class Reason {
            // The imported text is not a map.
            NOT_A_MAP,
            SIZE_OUT_OF_RANGE,
            UNKNOWN_TERRAIN,
            FORBIDDEN_NEIGHBOURS,
        }
    }

    /** The tile was not painted, as the [terrainId] can not be next to the [neighbourTerrainId] of a neighbouring tile. */
    data class TilePaintRejected(
        val row: Int,
        val column: Int,
        val terrainId: String,
        val neighbourTerrainId: String,
    ) : MapBuilderEvent
}
