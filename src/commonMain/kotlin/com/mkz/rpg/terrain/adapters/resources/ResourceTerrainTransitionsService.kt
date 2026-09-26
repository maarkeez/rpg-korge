package com.mkz.rpg.terrain.adapters.resources

import com.mkz.rpg.terrain.usecases.services.TerrainTransitionsService
import korlibs.io.file.VfsFile
import korlibs.io.file.std.resourcesVfs
import kotlinx.coroutines.runBlocking

class ResourceTerrainTransitionsService(
    private val transitionsRoot: VfsFile = resourcesVfs["terrain/transitions"],
) : TerrainTransitionsService {
    override fun searchTransitions(): Map<String, Set<String>> {
        val fileNames =
            runBlocking {
                if (!transitionsRoot.exists()) return@runBlocking emptyList()
                transitionsRoot.listNames()
            }
        val transitions = mutableMapOf<String, MutableSet<String>>()
        fileNames
            .filter { it.endsWith(".png") }
            .mapNotNull { fileName -> parseTransition(fileName) }
            .forEach { (terrainId, transitionToTerrainId) ->
                transitions.getOrPut(terrainId) { mutableSetOf() }.add(transitionToTerrainId)
            }
        return transitions
    }

    private fun parseTransition(fileName: String): Pair<String, String>? {
        val baseName = fileName.removeSuffix(".png")
        val parts = baseName.split(TRANSITION_SEPARATOR)
        if (parts.size != 2 || parts.any { it.isBlank() }) return null
        return parts[0] to parts[1]
    }

    private companion object {
        const val TRANSITION_SEPARATOR = "_to_"
    }
}
