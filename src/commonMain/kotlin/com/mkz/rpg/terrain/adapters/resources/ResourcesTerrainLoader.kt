package com.mkz.rpg.terrain.adapters.resources

import com.mkz.rpg.shared.adapters.resources.ResourcesBundle
import com.mkz.rpg.shared.adapters.toml.readTomlFiles
import com.mkz.rpg.terrain.domain.Terrain
import com.mkz.rpg.terrain.domain.TerrainError.TerrainNotFound
import com.mkz.rpg.terrain.usecases.services.TerrainLoader
import korlibs.io.file.VfsFile
import kotlinx.serialization.Serializable

class ResourcesTerrainLoader(
    private val terrainRoot: VfsFile = ResourcesBundle.root["terrain"],
) : TerrainLoader {
    private var terrainDefinitions = emptyMap<String, TerrainDefinition>()
    private var transitions = emptyMap<String, Set<String>>()

    override suspend fun initResources() {
        terrainDefinitions = terrainRoot.readTomlFiles<TerrainDefinition>()
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
        val effectId: String? = null,
    )

    private companion object {
        const val PNG_EXTENSION = ".png"
        const val TRANSITIONS_FOLDER = "transitions"
        const val TRANSITION_SEPARATOR = "_to_"
    }
}
