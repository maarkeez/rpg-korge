package com.mkz.rpg.terrain.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface TerrainEvent : DomainEvent {
    data class TerrainCreated(
        val terrainId: String,
    ) : TerrainEvent

    object RequestInitialiseTerrains : TerrainEvent
}
