package com.mkz.tessavar.battlefield.adapters.resources

import com.mkz.tessavar.battlefield.usecases.services.BattlefieldLoader
import com.mkz.tessavar.shared.adapters.resources.ResourcesBundle
import com.mkz.tessavar.shared.adapters.toml.fromResource
import com.mkz.tessavar.shared.adapters.toml.readTomlFile
import korlibs.io.file.VfsFile
import kotlinx.serialization.Serializable

class ResourcesBattlefieldLoader(
    private val battlefieldFile: VfsFile = ResourcesBundle.root["battlefield/battlefield.toml"],
) : BattlefieldLoader {
    private var tiles = emptyList<List<String>>()

    override suspend fun initResources() {
        val definition = battlefieldFile.readTomlFile<BattlefieldDefinition>()
        tiles = fromResource("battlefield/battlefield.toml") { definition.toTiles() }
    }

    override fun loadTiles(): List<List<String>> = tiles

    @Serializable
    data class BattlefieldDefinition(
        val tiles: List<List<String>>,
    ) {
        fun toTiles(): List<List<String>> {
            require(tiles.isNotEmpty()) { "'tiles' must have at least one row" }
            require(tiles.all { row -> row.size == tiles.first().size }) { "'tiles' rows must all have the same number of columns" }
            return tiles
        }
    }
}
