package com.mkz.rpg.worldBuilder.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface WorldBuilderEvent : DomainEvent {
    data class WorldCreated(
        val rows: Int,
        val columns: Int,
    ) : WorldBuilderEvent

    data class WorldResized(
        val rows: Int,
        val columns: Int,
    ) : WorldBuilderEvent

    data class TerrainSelected(
        val terrainId: String,
    ) : WorldBuilderEvent

    data class TilePainted(
        val row: Int,
        val column: Int,
        val terrainId: String,
    ) : WorldBuilderEvent

    data class WorldImported(
        val rows: Int,
        val columns: Int,
    ) : WorldBuilderEvent

    /** The world was not replaced, as the imported one is not valid. The terrains tell which ones made it invalid, if any. */
    data class WorldImportRejected(
        val reason: Reason,
        val terrainId: String? = null,
        val adjacentTerrainId: String? = null,
    ) : WorldBuilderEvent {
        enum class Reason {
            // The imported text is not a world.
            NOT_A_WORLD,
            SIZE_OUT_OF_RANGE,
            UNKNOWN_TERRAIN,
            FORBIDDEN_ADJACENT_TERRAINS,
        }
    }

    /** The tile was not painted, as the [terrainId] can not be next to the [adjacentTerrainId] of an adjacent tile. */
    data class TilePaintRejected(
        val row: Int,
        val column: Int,
        val terrainId: String,
        val adjacentTerrainId: String,
    ) : WorldBuilderEvent
}
