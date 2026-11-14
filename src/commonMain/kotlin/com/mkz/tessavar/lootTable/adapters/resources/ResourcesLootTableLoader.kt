package com.mkz.tessavar.lootTable.adapters.resources

import com.mkz.tessavar.lootTable.domain.LootTable
import com.mkz.tessavar.lootTable.domain.LootTable.Dto.EntryDto
import com.mkz.tessavar.lootTable.domain.LootTable.Dto.EntryDto.TypeDto
import com.mkz.tessavar.lootTable.usecases.services.LootTableLoader
import com.mkz.tessavar.shared.adapters.resources.ResourcesBundle
import com.mkz.tessavar.shared.adapters.toml.enumOf
import com.mkz.tessavar.shared.adapters.toml.fromResource
import com.mkz.tessavar.shared.adapters.toml.readTomlFiles
import com.mkz.tessavar.shared.adapters.toml.required
import korlibs.io.file.VfsFile
import kotlinx.serialization.Serializable

class ResourcesLootTableLoader(
    private val lootTableRoot: VfsFile = ResourcesBundle.root["lootTable"],
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
