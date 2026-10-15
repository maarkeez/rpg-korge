package com.mkz.rpg.lootDrop.adapters.resources

import com.mkz.rpg.lootDrop.domain.LootDrop
import com.mkz.rpg.lootDrop.usecases.services.LootDropLoader
import com.mkz.rpg.shared.adapters.toml.fromResource
import com.mkz.rpg.shared.adapters.toml.readTomlFiles
import korlibs.io.file.VfsFile
import korlibs.io.file.std.resourcesVfs
import kotlinx.serialization.Serializable

/** Loads one loot drop per `lootDrop/<unit id>.toml` resource. */
class ResourcesLootDropLoader(
    private val lootDropRoot: VfsFile = resourcesVfs["lootDrop"],
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
        fun toDto(unitId: String): LootDrop.Dto = LootDrop.Dto(unitId = unitId, lootTableIds = lootTableIds)
    }
}
