package com.mkz.rpg.inventory.domain

import com.mkz.rpg.inventory.domain.InventoryError.EquippedItemCanNotBeDiscarded
import com.mkz.rpg.inventory.domain.InventoryError.EquippedItemCanNotBeSold
import com.mkz.rpg.inventory.domain.InventoryError.GoldAboveLimit
import com.mkz.rpg.inventory.domain.InventoryError.InventoryItemNotFound
import com.mkz.rpg.inventory.domain.InventoryError.ItemAlreadyEquipped
import com.mkz.rpg.inventory.domain.InventoryError.ItemNotEquipped
import com.mkz.rpg.inventory.domain.InventoryError.NegativeGold
import com.mkz.rpg.inventory.domain.InventoryError.NoStorageSpaceLeft
import com.mkz.rpg.inventory.domain.InventoryError.NotEnoughGold
import com.mkz.rpg.inventory.domain.InventoryEvent.InventoryFull
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemBought
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemConsumed
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemDiscarded
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemEquipped
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemSold
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemStored
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemUnequipped
import com.mkz.rpg.lootDrop.domain.LootDropEvent.RequestPickUpDroppedItem
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto
import com.mkz.rpg.shared.domain.DomainEvent
import com.mkz.rpg.shared.domain.Gold.MAXIMUM
import kotlin.jvm.JvmInline

/**
 * The lootable items a battle unit of the human player carries, and its gold. Each item takes one storage space, even while equipped. Each
 * equipment zone holds one equipped item. Items are looted, bought and sold, and the gold goes from 0 to 99,999.
 */
@ConsistentCopyVisibility
data class Inventory private constructor(
    private val battleUnitId: BattleUnitId,
    private val maximumStorageSpace: MaximumStorageSpace,
    private val storedItems: StoredItems,
    private val equippedItems: EquippedItems,
    private val gold: Gold,
    // Its own events, each followed by the battlefield request it needs (e.g.: picking up the item it stored),
    // published together so the battlefield is up to date before anything reacting to the next events.
    private val events: Set<DomainEvent>,
) {
    companion object {
        private const val DEFAULT_MAXIMUM_STORAGE_SPACE = 12

        /** Creates the empty inventory of a battle unit, with its starting gold, once it is deployed. */
        fun create(
            battleUnitId: String,
            gold: Int = 0,
        ): Inventory =
            Inventory(
                battleUnitId = BattleUnitId(battleUnitId),
                maximumStorageSpace = MaximumStorageSpace(DEFAULT_MAXIMUM_STORAGE_SPACE),
                storedItems = StoredItems(emptyList()),
                equippedItems = EquippedItems(emptyMap()),
                gold = Gold(gold),
                events = emptySet(),
            )
    }

    fun pullEvents() = events to copy(events = emptySet())

    fun canStore(): Boolean = storedItems.value.size < maximumStorageSpace.value

    /**
     * Stores the item lying on the tile at [row] and [column], and requests its pick-up from the tile. Or announces the inventory is full, so the
     * player chooses whether to discard an item to make room.
     */
    fun store(
        lootableItemId: String,
        row: Int,
        column: Int,
    ): Inventory =
        if (canStore()) {
            copy(
                storedItems = StoredItems(storedItems.value + LootableItemId(lootableItemId)),
                events =
                    events +
                        ItemStored(battleUnitId = battleUnitId.value, lootableItemId = lootableItemId, row = row, column = column) +
                        RequestPickUpDroppedItem(lootableItemId = lootableItemId, row = row, column = column),
            )
        } else {
            copy(events = events + InventoryFull(battleUnitId = battleUnitId.value, lootableItemId = lootableItemId, row = row, column = column))
        }

    /** Whether the item is stored, so it can be consumed. */
    fun canConsume(lootableItemId: String): Boolean = storedItems.count(LootableItemId(lootableItemId)) > 0

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

    /** Whether it can pay the price of an item, and has space to store it. */
    fun canBuy(price: Int): Boolean = canStore() && gold.value >= price

    /** Pays the price of an item and stores it. Fails when it can not pay it, or has no space left. */
    fun buy(
        lootableItemId: String,
        price: Int,
    ): Inventory {
        if (!canStore()) throw NoStorageSpaceLeft()
        if (gold.value < price) throw NotEnoughGold()
        return copy(
            storedItems = StoredItems(storedItems.value + LootableItemId(lootableItemId)),
            gold = Gold(gold.value - price),
            events = events + ItemBought(battleUnitId = battleUnitId.value, lootableItemId = lootableItemId, price = price),
        )
    }

    /** Whether it can sell a stored copy that is not equipped for the price, without going above the most gold it can carry. */
    fun canSell(
        lootableItemId: String,
        price: Int,
    ): Boolean {
        val item = LootableItemId(lootableItemId)
        val unequippedCopies = storedItems.count(item) - if (equippedItems.isEquipped(item)) 1 else 0
        return unequippedCopies > 0 && gold.value + price <= MAXIMUM
    }

    /** Removes a stored copy that is not equipped, and receives its price. The item leaves the game. */
    fun sell(
        lootableItemId: String,
        price: Int,
    ): Inventory {
        val item = LootableItemId(lootableItemId)
        if (storedItems.count(item) == 0) throw InventoryItemNotFound()
        if (equippedItems.isEquipped(item) && storedItems.count(item) == 1) throw EquippedItemCanNotBeSold()
        if (gold.value + price > MAXIMUM) throw GoldAboveLimit()
        return copy(
            storedItems = storedItems.removeOne(item),
            gold = Gold(gold.value + price),
            events = events + ItemSold(battleUnitId = battleUnitId.value, lootableItemId = lootableItemId, price = price),
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
            gold = gold.value,
        )

    @JvmInline private value class BattleUnitId(
        val value: String,
    )

    @JvmInline private value class MaximumStorageSpace(
        val value: Int,
    )

    @JvmInline private value class Gold(
        val value: Int,
    ) {
        init {
            if (value < 0) throw NegativeGold()
            if (value > MAXIMUM) throw GoldAboveLimit()
        }
    }

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
        val gold: Int,
    )
}
