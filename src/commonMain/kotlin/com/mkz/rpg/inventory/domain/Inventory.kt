package com.mkz.rpg.inventory.domain

import com.mkz.rpg.inventory.domain.Inventory.Dto.GiveItemRefusalDto
import com.mkz.rpg.inventory.domain.Inventory.Dto.TradeRefusalDto
import com.mkz.rpg.inventory.domain.InventoryError.EquippedItemCanNotBeDiscarded
import com.mkz.rpg.inventory.domain.InventoryError.EquippedItemCanNotBeGiven
import com.mkz.rpg.inventory.domain.InventoryError.EquippedItemCanNotBeSold
import com.mkz.rpg.inventory.domain.InventoryError.GoldAboveMaximum
import com.mkz.rpg.inventory.domain.InventoryError.InvalidGoldAmount
import com.mkz.rpg.inventory.domain.InventoryError.ItemAlreadyEquipped
import com.mkz.rpg.inventory.domain.InventoryError.ItemNotEquipped
import com.mkz.rpg.inventory.domain.InventoryError.ItemNotStored
import com.mkz.rpg.inventory.domain.InventoryError.NegativeGold
import com.mkz.rpg.inventory.domain.InventoryError.NoStorageSpaceLeft
import com.mkz.rpg.inventory.domain.InventoryError.NotEnoughGold
import com.mkz.rpg.inventory.domain.InventoryEvent.GoldGiven
import com.mkz.rpg.inventory.domain.InventoryEvent.GoldReceived
import com.mkz.rpg.inventory.domain.InventoryEvent.InventoryFull
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemBought
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemConsumed
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemDiscarded
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemEquipped
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemGiven
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemReceived
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemSold
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemStored
import com.mkz.rpg.inventory.domain.InventoryEvent.ItemUnequipped
import com.mkz.rpg.inventory.domain.InventoryEvent.RewardGranted
import com.mkz.rpg.lootDrop.domain.LootDropEvent.RequestDropItems
import com.mkz.rpg.lootDrop.domain.LootDropEvent.RequestLootDroppedItem
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto
import com.mkz.rpg.shared.domain.DomainEvent
import kotlin.jvm.JvmInline

@ConsistentCopyVisibility
data class Inventory private constructor(
    private val battleUnitId: BattleUnitId,
    private val maximumStorageSpace: MaximumStorageSpace,
    private val storedItems: StoredItems,
    private val equippedItems: EquippedItems,
    private val gold: Gold,
    private val events: Set<DomainEvent>,
) {
    companion object {
        private const val DEFAULT_MAXIMUM_STORAGE_SPACE = 12

        private const val MAXIMUM_GOLD = 99_999

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

        fun load(dto: Dto): Inventory =
            Inventory(
                battleUnitId = BattleUnitId(dto.battleUnitId),
                maximumStorageSpace = MaximumStorageSpace(dto.maximumStorageSpace),
                storedItems = StoredItems(dto.storedItems.map(::LootableItemId)),
                equippedItems = EquippedItems(dto.equippedItems.mapValues { (_, lootableItemId) -> LootableItemId(lootableItemId) }),
                gold = Gold(dto.gold),
                events = emptySet(),
            )
    }

    fun pullEvents() = events to copy(events = emptySet())

    fun canStore(): Boolean = storedItems.value.size < maximumStorageSpace.value

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
                        RequestLootDroppedItem(lootableItemId = lootableItemId, row = row, column = column),
            )
        } else {
            copy(events = events + InventoryFull(battleUnitId = battleUnitId.value, lootableItemId = lootableItemId, row = row, column = column))
        }

    fun grantReward(
        gold: Int,
        lootableItemIds: List<String>,
        row: Int,
        column: Int,
    ): Inventory {
        val receivedGold = minOf(gold, MAXIMUM_GOLD - this.gold.value)
        val spaceLeft = maximumStorageSpace.value - storedItems.value.size
        val receivedItems = lootableItemIds.take(spaceLeft)
        val itemsLeft = lootableItemIds.drop(spaceLeft)
        return copy(
            storedItems = StoredItems(storedItems.value + receivedItems.map(::LootableItemId)),
            gold = Gold(this.gold.value + receivedGold),
            events =
                events +
                    listOfNotNull(
                        RewardGranted(battleUnitId = battleUnitId.value, gold = receivedGold, lootableItemIds = receivedItems),
                        RequestDropItems(lootableItemIds = itemsLeft, row = row, column = column).takeIf { itemsLeft.isNotEmpty() },
                    ),
        )
    }

    fun canConsume(lootableItemId: String): Boolean = storedItems.count(LootableItemId(lootableItemId)) > 0

    fun consume(lootableItemId: String): Inventory =
        copy(
            storedItems = storedItems.removeOne(LootableItemId(lootableItemId)),
            events = events + ItemConsumed(battleUnitId = battleUnitId.value, lootableItemId = lootableItemId),
        )

    fun discard(lootableItemId: String): Inventory {
        val item = LootableItemId(lootableItemId)
        if (equippedItems.isEquipped(item) && storedItems.count(item) == 1) throw EquippedItemCanNotBeDiscarded()
        return copy(
            storedItems = storedItems.removeOne(item),
            events = events + ItemDiscarded(battleUnitId = battleUnitId.value, lootableItemId = lootableItemId),
        )
    }

    fun equip(
        lootableItemId: String,
        equipmentZone: EquipmentZoneDto,
    ): Inventory {
        val item = LootableItemId(lootableItemId)
        if (storedItems.count(item) == 0) throw ItemNotStored()
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

    fun buyRefusal(price: Int): TradeRefusalDto? =
        when {
            !canStore() -> TradeRefusalDto.INVENTORY_FULL
            gold.value < price -> TradeRefusalDto.NOT_ENOUGH_GOLD
            else -> null
        }

    fun buy(
        lootableItemId: String,
        price: Int,
    ): Inventory {
        when (buyRefusal(price)) {
            TradeRefusalDto.INVENTORY_FULL -> throw NoStorageSpaceLeft()
            TradeRefusalDto.NOT_ENOUGH_GOLD -> throw NotEnoughGold()
            else -> Unit
        }
        return copy(
            storedItems = StoredItems(storedItems.value + LootableItemId(lootableItemId)),
            gold = Gold(gold.value - price),
            events = events + ItemBought(battleUnitId = battleUnitId.value, lootableItemId = lootableItemId, price = price),
        )
    }

    fun sellRefusal(
        lootableItemId: String,
        sellPrice: Int,
    ): TradeRefusalDto? {
        val item = LootableItemId(lootableItemId)
        val storedCopies = storedItems.count(item)
        return when {
            storedCopies == 0 -> TradeRefusalDto.NOT_STORED
            equippedItems.isEquipped(item) && storedCopies == 1 -> TradeRefusalDto.EQUIPPED
            gold.value + sellPrice > MAXIMUM_GOLD -> TradeRefusalDto.GOLD_FULL
            else -> null
        }
    }

    fun sell(
        lootableItemId: String,
        sellPrice: Int,
    ): Inventory {
        when (sellRefusal(lootableItemId, sellPrice)) {
            TradeRefusalDto.NOT_STORED -> throw ItemNotStored()
            TradeRefusalDto.EQUIPPED -> throw EquippedItemCanNotBeSold()
            TradeRefusalDto.GOLD_FULL -> throw GoldAboveMaximum()
            else -> Unit
        }
        return copy(
            storedItems = storedItems.removeOne(LootableItemId(lootableItemId)),
            gold = Gold(gold.value + sellPrice),
            events = events + ItemSold(battleUnitId = battleUnitId.value, lootableItemId = lootableItemId, sellPrice = sellPrice),
        )
    }

    fun giveItemRefusal(lootableItemId: String): GiveItemRefusalDto? {
        val item = LootableItemId(lootableItemId)
        val storedCopies = storedItems.count(item)
        return when {
            storedCopies == 0 -> GiveItemRefusalDto.NOT_STORED
            equippedItems.isEquipped(item) && storedCopies == 1 -> GiveItemRefusalDto.EQUIPPED
            else -> null
        }
    }

    fun giveItem(
        lootableItemId: String,
        receiverBattleUnitId: String,
    ): Inventory {
        when (giveItemRefusal(lootableItemId)) {
            GiveItemRefusalDto.NOT_STORED -> throw ItemNotStored()
            GiveItemRefusalDto.EQUIPPED -> throw EquippedItemCanNotBeGiven()
            else -> Unit
        }
        return copy(
            storedItems = storedItems.removeOne(LootableItemId(lootableItemId)),
            events = events + ItemGiven(battleUnitId = battleUnitId.value, receiverBattleUnitId = receiverBattleUnitId, lootableItemId = lootableItemId),
        )
    }

    fun receiveItem(
        lootableItemId: String,
        giverBattleUnitId: String,
    ): Inventory {
        if (!canStore()) throw NoStorageSpaceLeft()
        return copy(
            storedItems = StoredItems(storedItems.value + LootableItemId(lootableItemId)),
            events = events + ItemReceived(battleUnitId = battleUnitId.value, giverBattleUnitId = giverBattleUnitId, lootableItemId = lootableItemId),
        )
    }

    fun goldRoom(): Int = MAXIMUM_GOLD - gold.value

    fun giveGold(
        amount: Int,
        receiverBattleUnitId: String,
    ): Inventory {
        if (amount < 1) throw InvalidGoldAmount()
        if (amount > gold.value) throw NotEnoughGold()
        return copy(
            gold = Gold(gold.value - amount),
            events = events + GoldGiven(battleUnitId = battleUnitId.value, receiverBattleUnitId = receiverBattleUnitId, gold = amount),
        )
    }

    fun receiveGold(
        amount: Int,
        giverBattleUnitId: String,
    ): Inventory =
        copy(
            gold = Gold(gold.value + amount),
            events = events + GoldReceived(battleUnitId = battleUnitId.value, giverBattleUnitId = giverBattleUnitId, gold = amount),
        )

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
            if (value > MAXIMUM_GOLD) throw GoldAboveMaximum()
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
            if (index < 0) throw ItemNotStored()
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
        val equippedItems: Map<EquipmentZoneDto, String>,
        val gold: Int,
    ) {
        enum class TradeRefusalDto {
            NOT_TRADING,

            NOT_SOLD_BY_THE_MERCHANT,
            NOT_ENOUGH_GOLD,
            INVENTORY_FULL,

            NOT_STORED,
            EQUIPPED,
            GOLD_FULL,
        }

        enum class GiveItemRefusalDto {
            NOT_EXPLORING,
            NOT_STORED,
            EQUIPPED,

            NO_ONE,

            INVENTORIES_FULL,
        }

        enum class GiveGoldRefusalDto {
            NOT_EXPLORING,
            NO_GOLD,

            NO_ONE,

            GOLD_FULL,
        }

        data class GoldReceiverDto(
            val battleUnitId: String,
            val gold: Int,
            val room: Int,
        )
    }
}
