package com.mkz.rpg.lootTable.adapters.resources

import com.mkz.rpg.lootTable.domain.LootTable
import com.mkz.rpg.lootTable.domain.LootTable.Dto.EntryDto
import com.mkz.rpg.lootTable.domain.LootTable.Dto.EntryDto.TypeDto
import com.mkz.rpg.lootTable.usecases.services.LootTableLoader
import com.mkz.rpg.shared.adapters.toml.enumOf
import com.mkz.rpg.shared.adapters.toml.fromResource
import com.mkz.rpg.shared.adapters.toml.readTomlFiles
import com.mkz.rpg.shared.adapters.toml.required
import korlibs.io.file.VfsFile
import korlibs.io.file.std.resourcesVfs
import kotlinx.serialization.Serializable

/** Loads one loot table per `lootTable/<loot table id>.toml` resource. */
class ResourcesLootTableLoader(
    private val lootTableRoot: VfsFile = resourcesVfs["lootTable"],
) : LootTableLoader {
    private var lootTables = emptyList<LootTable.Dto>()

    override suspend fun initResources() {
        lootTables =
            lootTableRoot.readTomlFiles<LootTableDefinition>().map { (lootTableId, definition) ->
                fromResource("lootTable/$lootTableId.toml") { definition.toDto(lootTableId) }
            }
    }

    override fun loadLootTables(): List<LootTable.Dto> = lootTables

    @Serializable
    data class LootTableDefinition(
        val entries: List<EntryDefinition>,
    ) {
        fun toDto(lootTableId: String): LootTable.Dto = LootTable.Dto(id = lootTableId, entries = entries.map { it.toDto() })
    }

    @Serializable
    data class EntryDefinition(
        val type: String,
        val weight: Int,
        val lootableItemId: String? = null,
    ) {
        fun toDto(): EntryDto =
            when (enumOf<TypeDto>("entries.type", type)) {
                TypeDto.LOOTABLE_ITEM -> EntryDto.lootableItem(lootableItemId = required("entries.lootableItemId", lootableItemId), weight = weight)
                TypeDto.NOTHING -> EntryDto.nothing(weight = weight)
            }
    }
}
