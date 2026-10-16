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

    /** The tile was not painted, as the [terrainId] can not be next to the [neighbourTerrainId] of a neighbouring tile. */
    data class TilePaintRejected(
        val row: Int,
        val column: Int,
        val terrainId: String,
        val neighbourTerrainId: String,
    ) : MapBuilderEvent
}
