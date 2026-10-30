package com.mkz.rpg.inventory.adapters.save

import com.mkz.rpg.inventory.domain.Inventory
import com.mkz.rpg.inventory.domain.InventoryRepository
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto
import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.rpg.shared.adapters.save.SnapshotJson
import com.mkz.rpg.shared.domain.Snapshottable
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

/**
 * The inventory of every battle unit: its stored and equipped items, and its gold. Restoring fails when an item is a
 * lootable item the game no longer has.
 */
class InventorySnapshottable(
    private val inventoryRepository: InventoryRepository,
    private val searchLootableItemById: SearchLootableItemById,
) : Snapshottable {
    override val key = "inventory"

    override fun snapshot(): String =
        SnapshotJson.encodeToString(
            inventoryRepository.searchAll().map { inventory ->
                val dto = inventory.toDto()
                InventorySnapshot(
                    battleUnitId = dto.battleUnitId,
                    maximumStorageSpace = dto.maximumStorageSpace,
                    storedItems = dto.storedItems,
                    equippedItems = dto.equippedItems.mapKeys { (equipmentZone, _) -> equipmentZone.name },
                    gold = dto.gold,
                )
            },
        )

    override fun restore(snapshot: String) {
        val inventories =
            SnapshotJson.decodeFromString<List<InventorySnapshot>>(snapshot).map { inventory ->
                Inventory.Dto(
                    battleUnitId = inventory.battleUnitId,
                    maximumStorageSpace = inventory.maximumStorageSpace,
                    storedItems = inventory.storedItems,
                    equippedItems = inventory.equippedItems.mapKeys { (equipmentZone, _) -> EquipmentZoneDto.valueOf(equipmentZone) },
                    gold = inventory.gold,
                )
            }
        inventories.flatMap(Inventory.Dto::storedItems).forEach { lootableItemId -> requireNotNull(searchLootableItemById(lootableItemId)) { "Unknown lootable item $lootableItemId" } }
        inventoryRepository.deleteAll()
        inventories.forEach { inventory -> inventoryRepository.create(Inventory.restore(inventory)) }
    }
}

// Equipped items: by the name of their equipment zone, e.g.: HEAD. They are also stored.
@Serializable
private data class InventorySnapshot(
    val battleUnitId: String,
    val maximumStorageSpace: Int,
    val storedItems: List<String>,
    val equippedItems: Map<String, String>,
    val gold: Int,
)
