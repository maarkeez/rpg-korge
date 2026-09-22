package com.mkz.rpg.terrain.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface TerrainEvent : DomainEvent {
    data class TerrainCreated(
        val terrainId: String,
    ) : TerrainEvent

    data class RequestTerrainCreation(
        val terrainDto: Terrain.Dto,
    ) : TerrainEvent
}
