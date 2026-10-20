package com.mkz.rpg.lootableItem.adapters.resources

import com.mkz.rpg.lootableItem.domain.LootableItem
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.ConsumableDto
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.TypeDto
import com.mkz.rpg.lootableItem.usecases.services.LootableItemLoader
import com.mkz.rpg.shared.adapters.toml.enumOf
import com.mkz.rpg.shared.adapters.toml.fromResource
import com.mkz.rpg.shared.adapters.toml.readTomlFiles
import com.mkz.rpg.shared.adapters.toml.required
import korlibs.io.file.VfsFile
import korlibs.io.file.std.resourcesVfs
import kotlinx.serialization.Serializable

/** Loads one lootable item per `lootableItem/<lootable item id>.toml` resource. */
class ResourcesLootableItemLoader(
    private val lootableItemRoot: VfsFile = resourcesVfs["lootableItem"],
) : LootableItemLoader {
    private var lootableItems = emptyList<LootableItem.Dto>()

    override suspend fun initResources() {
        lootableItems =
            lootableItemRoot.readTomlFiles<LootableItemDefinition>().map { (lootableItemId, definition) ->
                fromResource("lootableItem/$lootableItemId.toml") { definition.toDto(lootableItemId) }
            }
    }

    override fun loadLootableItems(): List<LootableItem.Dto> = lootableItems

    @Serializable
    data class LootableItemDefinition(
        val name: String,
        val price: Int,
        val type: String,
        val consumable: ConsumableDefinition? = null,
        val equippable: EquippableDefinition? = null,
    ) {
        fun toDto(lootableItemId: String): LootableItem.Dto =
            LootableItem.Dto(
                id = lootableItemId,
                name = name,
                price = price,
                lootType =
                    when (enumOf<TypeDto>("type", type)) {
                        TypeDto.CONSUMABLE -> LootTypeDto.consumable(required("consumable", consumable).toDto())
                        TypeDto.EQUIPPABLE -> LootTypeDto.equippable(required("equippable", equippable).toDto())
                    },
            )
    }

    @Serializable
    data class ConsumableDefinition(
        val effectIds: List<String>,
    ) {
        fun toDto(): ConsumableDto = ConsumableDto(effectIds = effectIds.toSet())
    }

    @Serializable
    data class EquippableDefinition(
        val effectIds: List<String>,
        val equipmentZone: String,
    ) {
        fun toDto(): EquippableDto =
            EquippableDto(
                effectIds = effectIds.toSet(),
                equipmentZone = enumOf<EquipmentZoneDto>("equippable.equipmentZone", equipmentZone),
            )
    }
}
