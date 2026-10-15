package com.mkz.rpg.inventory.domain

import com.mkz.rpg.inventory.domain.InventoryError.InventoryItemNotFound
import com.mkz.rpg.inventory.domain.InventoryEvent.InventoryFull
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemConsumed
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemDiscarded
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemStored
import kotlin.jvm.JvmInline

/** The lootable items a battle unit of the human player has looted. Each item takes one storage space. */
@ConsistentCopyVisibility
data class Inventory private constructor(
    private val battleUnitId: BattleUnitId,
    private val maximumStorageSpace: MaximumStorageSpace,
    private val storedItems: StoredItems,
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
                events = emptySet(),
            )
    }

    fun pullEvents() = events to copy(events = emptySet())

    fun canStore(): Boolean = storedItems.value.size < maximumStorageSpace.value

    /** Stores the item, or announces the inventory is full, so the player chooses whether to discard an item to make room. */
    fun store(lootableItemId: String): Inventory =
        if (canStore()) {
            copy(
                storedItems = StoredItems(storedItems.value + LootableItemId(lootableItemId)),
                events = events + ItemStored(battleUnitId = battleUnitId.value, lootableItemId = lootableItemId),
            )
        } else {
            copy(events = events + InventoryFull(battleUnitId = battleUnitId.value, lootableItemId = lootableItemId))
        }

    fun consume(lootableItemId: String): Inventory =
        copy(
            storedItems = storedItems.removeOne(LootableItemId(lootableItemId)),
            events = events + ItemConsumed(battleUnitId = battleUnitId.value, lootableItemId = lootableItemId),
        )

    fun discard(lootableItemId: String): Inventory =
        copy(
            storedItems = storedItems.removeOne(LootableItemId(lootableItemId)),
            events = events + ItemDiscarded(battleUnitId = battleUnitId.value, lootableItemId = lootableItemId),
        )

    fun toDto() =
        Dto(
            battleUnitId = battleUnitId.value,
            maximumStorageSpace = maximumStorageSpace.value,
            storedItems = storedItems.value.map(LootableItemId::value),
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
    }

    data class Dto(
        val battleUnitId: String,
        val maximumStorageSpace: Int,
        val storedItems: List<String>,
    )
}
