package com.mkz.rpg.terrain.adapters.resources

import com.mkz.rpg.shared.adapters.toml.TomlService
import com.mkz.rpg.terrain.domain.Terrain
import com.mkz.rpg.terrain.domain.TerrainError.TerrainNotFound
import com.mkz.rpg.terrain.usecases.services.TerrainLoader
import korlibs.io.file.VfsFile
import korlibs.io.file.std.resourcesVfs
import kotlinx.serialization.Serializable

class ResourcesTerrainLoader(
    private val terrainRoot: VfsFile = resourcesVfs["terrain"],
) : TerrainLoader {
    private val tomlService = TomlService()
    private var terrainDefinitions = emptyMap<String, TerrainDefinition>()
    private var transitions = emptyMap<String, Set<String>>()

    override suspend fun initResources() {
        terrainDefinitions =
            if (terrainRoot.exists()) {
                terrainRoot
                    .listNames()
                    .filter { it.endsWith(TOML_EXTENSION) }
                    .associate { fileName ->
                        val terrainId = fileName.removeSuffix(TOML_EXTENSION)
                        terrainId to tomlService.deserialize<TerrainDefinition>(terrainRoot[fileName].readString())
                    }
            } else {
                emptyMap()
            }
        transitions = loadTransitions()
    }

    override fun loadTerrains(): List<Terrain.Dto> {
        val referencedTerrainIds = (transitions.keys + transitions.values.flatten()).distinct()
        referencedTerrainIds.forEach { terrainId ->
            if (!terrainDefinitions.containsKey(terrainId)) throw TerrainNotFound(terrainId)
        }
        return terrainDefinitions
            .entries
            .sortedBy { it.key }
            .map { (terrainId, definition) ->
                Terrain.Dto(
                    id = terrainId,
                    canBeOccupied = definition.canBeOccupied,
                    allowedTransitionTo = computeAllowedTransitions(terrainId),
                    effectId = definition.effectId,
                )
            }
    }

    private fun computeAllowedTransitions(terrainId: String): Set<String> {
        val allowedTransitions = mutableSetOf(terrainId)
        transitions.forEach { (transitionFrom, transitionTo) ->
            if (transitionFrom == terrainId) {
                allowedTransitions += transitionTo
            }
        }
        return allowedTransitions
    }

    /**
     * Allowed adjacency comes from `transitionsTo` in each `terrain/<id>.toml`. The images in `terrain/transitions/`
     * are art only, so their names and format can change without touching gameplay.
     */
    private fun loadTransitions(): Map<String, Set<String>> =
        terrainDefinitions
            .filterValues { it.transitionsTo.isNotEmpty() }
            .mapValues { (_, definition) -> definition.transitionsTo.toSet() }

    @Serializable
    data class TerrainDefinition(
        val canBeOccupied: Boolean,
        val effectId: String? = null,
        /** Terrains this one may border. The rule works both ways, so it only needs to be written on one side. */
        val transitionsTo: List<String> = emptyList(),
    )

    private companion object {
        const val TOML_EXTENSION = ".toml"
    }
}
