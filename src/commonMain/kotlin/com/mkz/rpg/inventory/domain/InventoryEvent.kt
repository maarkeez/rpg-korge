package com.mkz.rpg.inventory.domain

import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto
import com.mkz.rpg.shared.domain.DomainEvent

sealed interface InventoryEvent : DomainEvent {
    /** The item was stored, so it leaves the tile at [row] and [column] it was lying on. */
    data class ItemStored(
        val battleUnitId: String,
        val lootableItemId: String,
        val row: Int,
        val column: Int,
    ) : InventoryEvent

    /** The item was not stored: the player chooses whether to discard an item to store it, or to leave it on the tile at [row] and [column]. */
    data class InventoryFull(
        val battleUnitId: String,
        val lootableItemId: String,
        val row: Int,
        val column: Int,
    ) : InventoryEvent

    data class ItemConsumed(
        val battleUnitId: String,
        val lootableItemId: String,
    ) : InventoryEvent

    data class ItemDiscarded(
        val battleUnitId: String,
        val lootableItemId: String,
    ) : InventoryEvent

    data class ItemEquipped(
        val battleUnitId: String,
        val lootableItemId: String,
        val equipmentZone: EquipmentZoneDto,
    ) : InventoryEvent

    data class ItemUnequipped(
        val battleUnitId: String,
        val lootableItemId: String,
        val equipmentZone: EquipmentZoneDto,
    ) : InventoryEvent
}
