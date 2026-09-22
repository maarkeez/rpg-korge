package com.mkz.rpg.terrain.domain

import com.mkz.rpg.terrain.domain.TerrainEvent.TerrainCreated
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@ConsistentCopyVisibility
data class Terrain private constructor(
    private val id: Id,
    private val canBeOccupied: CanBeOccupied,
    private val events: Set<TerrainEvent>,
) {
    companion object {
        fun create(terrain: Terrain.Dto) =
            Terrain(
                Id(terrain.id),
                CanBeOccupied(terrain.canBeOccupied),
                setOf(TerrainCreated(terrainId = terrain.id)),
            )
    }

    fun toDto() =
        Dto(
            id = id.value,
            canBeOccupied = canBeOccupied.value,
        )

    fun pullEvents() = events to copy(events = emptySet())

    @JvmInline private value class Id(
        val value: String,
    )

    @JvmInline private value class CanBeOccupied(
        val value: Boolean,
    )

    @Serializable
    data class Dto(
        val id: String,
        val canBeOccupied: Boolean,
    )
}
