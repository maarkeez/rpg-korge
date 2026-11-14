package com.mkz.tessavar.lootDrop.adapters.resources

import com.mkz.tessavar.lootDrop.domain.LootDrop
import com.mkz.tessavar.lootDrop.usecases.services.LootDropLoader
import com.mkz.tessavar.shared.adapters.resources.ResourcesBundle
import com.mkz.tessavar.shared.adapters.toml.fromResource
import com.mkz.tessavar.shared.adapters.toml.readTomlFiles
import korlibs.io.file.VfsFile
import kotlinx.serialization.Serializable

class ResourcesLootDropLoader(
    private val lootDropRoot: VfsFile = ResourcesBundle.root["lootDrop"],
) : LootDropLoader {
    private var lootDrops = emptyList<LootDrop.Dto>()

    override suspend fun initResources() {
        lootDrops =
            lootDropRoot.readTomlFiles<LootDropDefinition>().map { (unitId, definition) ->
                fromResource("lootDrop/$unitId.toml") { definition.toDto(unitId) }
            }
    }

    override fun loadLootDrops(): List<LootDrop.Dto> = lootDrops

    @Serializable
    data class LootDropDefinition(
        val lootTableIds: List<String>,
    ) {
        fun toDto(unitId: String): LootDrop.Dto = LootDrop.Dto(unitId = unitId, lootTableIds = lootTableIds, droppedItems = emptyMap())
    }
}
