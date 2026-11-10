package com.mkz.rpg.inventory.adapters.save

import com.mkz.rpg.inventory.domain.Inventory
import com.mkz.rpg.inventory.usecases.commands.LoadInventoriesSnapshot
import com.mkz.rpg.inventory.usecases.queries.SearchInventoriesSnapshot
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto
import com.mkz.rpg.shared.adapters.save.SnapshotJson
import com.mkz.rpg.shared.domain.Snapshottable
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class InventorySnapshottable(
    private val searchInventoriesSnapshot: SearchInventoriesSnapshot,
    private val loadInventoriesSnapshot: LoadInventoriesSnapshot,
) : Snapshottable {
    override val key = "inventory"

    override fun snapshot(): String =
        SnapshotJson.encodeToString(
            searchInventoriesSnapshot().map { inventory ->
                InventorySnapshot(
                    battleUnitId = inventory.battleUnitId,
                    maximumStorageSpace = inventory.maximumStorageSpace,
                    storedItems = inventory.storedItems,
                    equippedItems = inventory.equippedItems.mapKeys { (equipmentZone, _) -> equipmentZone.name },
                    gold = inventory.gold,
                )
            },
        )

    override fun load(snapshot: String) =
        loadInventoriesSnapshot(
            SnapshotJson.decodeFromString<List<InventorySnapshot>>(snapshot).map { inventory ->
                Inventory.Dto(
                    battleUnitId = inventory.battleUnitId,
                    maximumStorageSpace = inventory.maximumStorageSpace,
                    storedItems = inventory.storedItems,
                    equippedItems = inventory.equippedItems.mapKeys { (equipmentZone, _) -> EquipmentZoneDto.valueOf(equipmentZone) },
                    gold = inventory.gold,
                )
            },
        )
}

@Serializable
private data class InventorySnapshot(
    val battleUnitId: String,
    val maximumStorageSpace: Int,
    val storedItems: List<String>,
    val equippedItems: Map<String, String>,
    val gold: Int,
)
