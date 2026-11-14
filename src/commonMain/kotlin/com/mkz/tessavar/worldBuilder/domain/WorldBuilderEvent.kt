package com.mkz.tessavar.worldBuilder.domain

import com.mkz.tessavar.shared.domain.DomainEvent

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

    data class WorldImportRejected(
        val reason: Reason,
        val terrainId: String? = null,
        val adjacentTerrainId: String? = null,
    ) : WorldBuilderEvent {
        enum class Reason {
            NOT_A_WORLD,
            SIZE_OUT_OF_RANGE,
            UNKNOWN_TERRAIN,
            FORBIDDEN_ADJACENT_TERRAINS,
        }
    }

    data class TilePaintRejected(
        val row: Int,
        val column: Int,
        val terrainId: String,
        val adjacentTerrainId: String,
    ) : WorldBuilderEvent
}
