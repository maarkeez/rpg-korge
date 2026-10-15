package com.mkz.rpg.inventory.domain

import com.mkz.rpg.inventory.domain.InventoryError.EquippedItemCanNotBeDiscarded
import com.mkz.rpg.inventory.domain.InventoryError.InventoryItemNotFound
import com.mkz.rpg.inventory.domain.InventoryError.ItemAlreadyEquipped
import com.mkz.rpg.inventory.domain.InventoryError.ItemNotEquipped
import com.mkz.rpg.inventory.domain.InventoryEvent.InventoryFull
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemConsumed
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemDiscarded
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemEquipped
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemStored
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemUnequipped
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto
import kotlin.jvm.JvmInline

/**
 * The lootable items a battle unit of the human player has looted. Each item takes one storage space, even while equipped. Each equipment zone holds
 * one equipped item.
 */
@ConsistentCopyVisibility
data class Inventory private constructor(
    private val battleUnitId: BattleUnitId,
    private val maximumStorageSpace: MaximumStorageSpace,
    private val storedItems: StoredItems,
    private val equippedItems: EquippedItems,
    private val events: Set<InventoryEvent>,
) {
    companion object {
        private const val DEFAULT_MAXIMUM_STORAGE_SPACE = 12

        /** Creates the empty inventory of a battle unit, when it loots its first item. */
        fun create(battleUnitId: String): Inventory =
            Inventory(
                battleUnitId = BattleUnitId(battleUnitId),
                maximumStorageSpace = MaximumStorageSpace(DEFAULT_MAXIMUM_STORAGE_SPACE),
                storedItems = StoredItems(emptyList()),
                equippedItems = EquippedItems(emptyMap()),
                events = emptySet(),
            )
    }

    fun pullEvents() = events to copy(events = emptySet())

    fun canStore(): Boolean = storedItems.value.size < maximumStorageSpace.value

    /**
     * Stores the item lying on the tile at [row] and [column], or announces the inventory is full, so the player chooses whether to discard an item
     * to make room.
     */
    fun store(
        lootableItemId: String,
        row: Int,
        column: Int,
    ): Inventory =
        if (canStore()) {
            copy(
                storedItems = StoredItems(storedItems.value + LootableItemId(lootableItemId)),
                events = events + ItemStored(battleUnitId = battleUnitId.value, lootableItemId = lootableItemId, row = row, column = column),
            )
        } else {
            copy(events = events + InventoryFull(battleUnitId = battleUnitId.value, lootableItemId = lootableItemId, row = row, column = column))
        }

    fun consume(lootableItemId: String): Inventory =
        copy(
            storedItems = storedItems.removeOne(LootableItemId(lootableItemId)),
            events = events + ItemConsumed(battleUnitId = battleUnitId.value, lootableItemId = lootableItemId),
        )

    /** Discards a stored copy that is not equipped: an equipped item must be unequipped first. */
    fun discard(lootableItemId: String): Inventory {
        val item = LootableItemId(lootableItemId)
        if (equippedItems.isEquipped(item) && storedItems.count(item) == 1) throw EquippedItemCanNotBeDiscarded()
        return copy(
            storedItems = storedItems.removeOne(item),
            events = events + ItemDiscarded(battleUnitId = battleUnitId.value, lootableItemId = lootableItemId),
        )
    }

    /** Equips a stored item in its zone, unequipping the item the zone held, if any. */
    fun equip(
        lootableItemId: String,
        equipmentZone: EquipmentZoneDto,
    ): Inventory {
        val item = LootableItemId(lootableItemId)
        if (storedItems.count(item) == 0) throw InventoryItemNotFound()
        if (equippedItems.isEquipped(item)) throw ItemAlreadyEquipped()
        val unequippedItem = equippedItems.value[equipmentZone]
        val newEvents =
            buildList {
                if (unequippedItem != null) {
                    add(ItemUnequipped(battleUnitId = battleUnitId.value, lootableItemId = unequippedItem.value, equipmentZone = equipmentZone))
                }
                add(ItemEquipped(battleUnitId = battleUnitId.value, lootableItemId = lootableItemId, equipmentZone = equipmentZone))
            }
        return copy(
            equippedItems = EquippedItems(equippedItems.value + (equipmentZone to item)),
            events = events + newEvents,
        )
    }

    /** Unequips an item, which stays stored. */
    fun unequip(lootableItemId: String): Inventory {
        val item = LootableItemId(lootableItemId)
        val equipmentZone = equippedItems.zoneOf(item) ?: throw ItemNotEquipped()
        return copy(
            equippedItems = EquippedItems(equippedItems.value - equipmentZone),
            events = events + ItemUnequipped(battleUnitId = battleUnitId.value, lootableItemId = lootableItemId, equipmentZone = equipmentZone),
        )
    }

    fun toDto() =
        Dto(
            battleUnitId = battleUnitId.value,
            maximumStorageSpace = maximumStorageSpace.value,
            storedItems = storedItems.value.map(LootableItemId::value),
            equippedItems = equippedItems.value.mapValues { (_, lootableItemId) -> lootableItemId.value },
        )

    @JvmInline private value class BattleUnitId(
        val value: String,
    )

    @JvmInline private value class MaximumStorageSpace(
        val value: Int,
    )

    @JvmInline private value class LootableItemId(
        val value: String,
    )

    @JvmInline private value class StoredItems(
        val value: List<LootableItemId>,
    ) {
        fun removeOne(lootableItemId: LootableItemId): StoredItems {
            val index = value.indexOf(lootableItemId)
            if (index < 0) throw InventoryItemNotFound()
            return StoredItems(value.filterIndexed { storedIndex, _ -> storedIndex != index })
        }

        fun count(lootableItemId: LootableItemId): Int = value.count { it == lootableItemId }
    }

    @JvmInline private value class EquippedItems(
        val value: Map<EquipmentZoneDto, LootableItemId>,
    ) {
        fun isEquipped(lootableItemId: LootableItemId): Boolean = zoneOf(lootableItemId) != null

        fun zoneOf(lootableItemId: LootableItemId): EquipmentZoneDto? = value.entries.firstOrNull { it.value == lootableItemId }?.key
    }

    data class Dto(
        val battleUnitId: String,
        val maximumStorageSpace: Int,
        val storedItems: List<String>,
        // The equipped items are also part of the stored items.
        val equippedItems: Map<EquipmentZoneDto, String>,
    )
}
