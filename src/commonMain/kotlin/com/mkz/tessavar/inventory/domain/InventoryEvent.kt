package com.mkz.tessavar.inventory.domain

import com.mkz.tessavar.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto
import com.mkz.tessavar.shared.domain.DomainEvent

sealed interface InventoryEvent : DomainEvent {
    data class RequestGrantReward(
        val battleUnitId: String,
        val gold: Int,
        val lootableItemIds: List<String>,
        val row: Int,
        val column: Int,
    ) : InventoryEvent

    data class RewardGranted(
        val battleUnitId: String,
        val gold: Int,
        val lootableItemIds: List<String>,
    ) : InventoryEvent

    data class ItemStored(
        val battleUnitId: String,
        val lootableItemId: String,
        val row: Int,
        val column: Int,
    ) : InventoryEvent

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

    data class ItemBought(
        val battleUnitId: String,
        val lootableItemId: String,
        val price: Int,
    ) : InventoryEvent

    data class ItemSold(
        val battleUnitId: String,
        val lootableItemId: String,
        val sellPrice: Int,
    ) : InventoryEvent

    data class ItemGiven(
        val battleUnitId: String,
        val receiverBattleUnitId: String,
        val lootableItemId: String,
    ) : InventoryEvent

    data class ItemReceived(
        val battleUnitId: String,
        val giverBattleUnitId: String,
        val lootableItemId: String,
    ) : InventoryEvent

    data class GoldGiven(
        val battleUnitId: String,
        val receiverBattleUnitId: String,
        val gold: Int,
    ) : InventoryEvent

    data class GoldReceived(
        val battleUnitId: String,
        val giverBattleUnitId: String,
        val gold: Int,
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
