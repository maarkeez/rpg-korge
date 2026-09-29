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

    suspend fun initResources() {
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
                )
            }
    }

    private fun computeAllowedTransitions(terrainId: String): Set<String> {
        val allowedTransitions = mutableSetOf(terrainId)
        transitions.forEach { (transitionFrom, transitionTo) ->
            if (transitionFrom == terrainId) {
                allowedTransitions += transitionTo
            }
            transitionTo.forEach { counterpart ->
                if (counterpart == terrainId) {
                    allowedTransitions += transitionFrom
                }
            }
        }
        return allowedTransitions
    }

    private suspend fun loadTransitions(): Map<String, Set<String>> {
        val transitionsRoot = terrainRoot[TRANSITIONS_FOLDER]
        val transitionFileNames =
            if (transitionsRoot.exists()) transitionsRoot.listNames() else emptyList()
        val transitions = mutableMapOf<String, MutableSet<String>>()
        transitionFileNames
            .filter { it.endsWith(PNG_EXTENSION) }
            .mapNotNull { fileName -> parseTransition(fileName) }
            .forEach { (terrainId, transitionToTerrainId) ->
                transitions.getOrPut(terrainId) { mutableSetOf() }.add(transitionToTerrainId)
            }
        return transitions
    }

    private fun parseTransition(fileName: String): Pair<String, String>? {
        val baseName = fileName.removeSuffix(PNG_EXTENSION)
        val parts = baseName.split(TRANSITION_SEPARATOR)
        if (parts.size != 2 || parts.any { it.isBlank() }) return null
        return parts[0] to parts[1]
    }

    @Serializable
    data class TerrainDefinition(
        val canBeOccupied: Boolean,
    )

    private companion object {
        const val TOML_EXTENSION = ".toml"
        const val PNG_EXTENSION = ".png"
        const val TRANSITIONS_FOLDER = "transitions"
        const val TRANSITION_SEPARATOR = "_to_"
    }
}
