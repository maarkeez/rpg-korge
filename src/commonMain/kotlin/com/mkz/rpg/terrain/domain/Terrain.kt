package com.mkz.rpg.terrain.domain

import com.mkz.rpg.terrain.domain.TerrainEvent.TerrainCreated
import kotlin.jvm.JvmInline

@ConsistentCopyVisibility
data class Terrain private constructor(
    private val id: Id,
    private val canBeOccupied: CanBeOccupied,
    private val transitionsTo: TransitionsTo,
    private val effectId: EffectId?,
    private val events: Set<TerrainEvent>,
) {
    companion object {
        fun create(terrain: Terrain.Dto) =
            Terrain(
                Id(terrain.id),
                CanBeOccupied(terrain.canBeOccupied),
                TransitionsTo(terrain.transitionsTo),
                terrain.effectId?.let { EffectId(it) },
                setOf(TerrainCreated(terrainId = terrain.id)),
            )
    }

    fun hasTransitionTo(terrainId: String): Boolean = transitionsTo.value.contains(terrainId)

    fun toDto() =
        Dto(
            id = id.value,
            canBeOccupied = canBeOccupied.value,
            transitionsTo = transitionsTo.value,
            effectId = effectId?.value,
        )

    fun pullEvents(): Pair<Set<TerrainEvent>, Terrain> = events to copy(events = emptySet())

    @JvmInline private value class Id(
        val value: String,
    )

    @JvmInline private value class CanBeOccupied(
        val value: Boolean,
    )

    @JvmInline private value class TransitionsTo(
        val value: Set<String>,
    )

    @JvmInline private value class EffectId(
        val value: String,
    )

    data class Dto(
        val id: String,
        val canBeOccupied: Boolean,
        val transitionsTo: Set<String>,
        val effectId: String?,
    )
}
