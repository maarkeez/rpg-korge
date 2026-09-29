package com.mkz.rpg.terrain.domain

import com.mkz.rpg.terrain.domain.TerrainEvent.TerrainCreated
import kotlin.jvm.JvmInline

@ConsistentCopyVisibility
data class Terrain private constructor(
    private val id: Id,
    private val canBeOccupied: CanBeOccupied,
    private val allowedTransitionTo: AllowedTransitionTo,
    private val events: Set<TerrainEvent>,
) {
    companion object {
        fun create(terrain: Terrain.Dto) =
            Terrain(
                Id(terrain.id),
                CanBeOccupied(terrain.canBeOccupied),
                AllowedTransitionTo(terrain.allowedTransitionTo),
                setOf(TerrainCreated(terrainId = terrain.id)),
            )
    }

    fun canTransitionTo(terrainId: String): Boolean = allowedTransitionTo.value.contains(terrainId)

    fun toDto() =
        Dto(
            id = id.value,
            canBeOccupied = canBeOccupied.value,
            allowedTransitionTo = allowedTransitionTo.value,
        )

    fun pullEvents() = events to copy(events = emptySet())

    @JvmInline private value class Id(
        val value: String,
    )

    @JvmInline private value class CanBeOccupied(
        val value: Boolean,
    )

    @JvmInline private value class AllowedTransitionTo(
        val value: Set<String>,
    )

    data class Dto(
        val id: String,
        val canBeOccupied: Boolean,
        val allowedTransitionTo: Set<String> = emptySet(),
    )
}
