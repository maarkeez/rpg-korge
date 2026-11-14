package com.mkz.tessavar.terrain.domain

import com.mkz.tessavar.shared.domain.DomainEvent

sealed interface TerrainEvent : DomainEvent {
    data class TerrainCreated(
        val terrainId: String,
    ) : TerrainEvent

    object RequestInitialiseTerrains : TerrainEvent
}
