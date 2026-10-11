package com.mkz.rpg.battle.adapters.resources

import com.mkz.rpg.battle.usecases.services.BattleLoader
import com.mkz.rpg.shared.adapters.toml.fromResource
import com.mkz.rpg.shared.adapters.toml.readTomlFile
import korlibs.io.file.VfsFile
import korlibs.io.file.std.resourcesVfs
import kotlinx.serialization.Serializable

/** Loads the battle from the `battle/battle.toml` resource. */
class ResourcesBattleLoader(
    private val battleFile: VfsFile = resourcesVfs["battle/battle.toml"],
) : BattleLoader {
    private var playerOrder = emptyList<String>()

    override suspend fun initResources() {
        val definition = battleFile.readTomlFile<BattleDefinition>()
        playerOrder = fromResource("battle/battle.toml") { definition.toPlayerOrder() }
    }

    override fun loadPlayerOrder(): List<String> = playerOrder

    @Serializable
    data class BattleDefinition(
        val playerOrder: List<String>,
    ) {
        fun toPlayerOrder(): List<String> {
            require(playerOrder.isNotEmpty()) { "'playerOrder' must have at least one player" }
            return playerOrder
        }
    }
}
